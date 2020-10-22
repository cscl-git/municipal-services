package org.egov.ps.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.validator.PropertyValidator;
import org.egov.ps.web.contracts.AccountStatementResponse;
import org.egov.ps.web.contracts.BusinessService;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.ps.web.contracts.State;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.egov.ps.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyService {

	@Autowired
	private PropertyEnrichmentService enrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	PropertyValidator propertyValidator;

	@Autowired
	PropertyRepository repository;

	@Autowired
	WorkflowIntegrator wfIntegrator;

	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private IRentCollectionService rentCollectionService;

	public List<Property> createProperty(PropertyRequest request) {
		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichPropertyRequest(request);
		producer.push(config.getSavePropertyTopic(), request);
		return request.getProperties();
	}

	/**
	 * Updates the property
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public List<Property> updateProperty(PropertyRequest request) {
		propertyValidator.validateUpdateRequest(request);
		enrichmentService.enrichPropertyRequest(request);
		String action = request.getProperties().get(0).getAction();
		if (config.getIsWorkflowEnabled() && !action.contentEquals("") && !action.contentEquals(PSConstants.ES_DRAFT)) {
			wfIntegrator.callWorkFlow(request);
		}
		producer.push(config.getUpdatePropertyTopic(), request);

		return request.getProperties();
	}

	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {
		/**
		 * Convert file number to upper case if provided.
		 */
		if (criteria.getFileNumber() != null) {
			criteria.setFileNumber(criteria.getFileNumber().toUpperCase());
		}

		if (criteria.isEmpty()) {
			/**
			 * Set the list of states to exclude draft states. Allow criteria to have
			 * creator as current user.
			 */
			BusinessService businessService = workflowService.getBusinessService(PSConstants.TENANT_ID, requestInfo,
					config.getAosBusinessServiceValue());
			List<String> states = businessService.getStates().stream().map(State::getState)
					.filter(s -> s != null && s.length() != 0).collect(Collectors.toList());
			criteria.setState(states);
			criteria.setUserId(requestInfo.getUserInfo().getUuid());
		} else if (criteria.getState() != null && criteria.getState().contains(PSConstants.PM_DRAFTED)) {
			/**
			 * If only drafted state is asked for, fetch currently logged in user's
			 * properties.
			 */
			criteria.setUserId(requestInfo.getUserInfo().getUuid());
		}

		return repository.getProperties(criteria);
	}
	
	
	public AccountStatementResponse searchPayments(AccountStatementCriteria accountStatementCriteria,
			RequestInfo requestInfo) {

		List<Property> properties = repository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("finance")).build());
		if (CollectionUtils.isEmpty(properties)) {
			return AccountStatementResponse.builder().rentAccountStatements(Collections.emptyList()).build();
		}

		Property property = properties.get(0);
		List<EstateDemand> demands = repository
				.getDemandDetailsForPropertyDetailsIds(Collections.singletonList(property.getPropertyDetails().getId()));

		List<EstatePayment> payments = repository
				.getEstatePaymentsForPropertyDetailsIds(Collections.singletonList(property.getPropertyDetails().getId()));

		return AccountStatementResponse.builder()
				.rentAccountStatements(rentCollectionService.getAccountStatement(demands, payments,
						18.00, // property.getPropertyDetails().getInterestRate(), // TODO: hard coded for now
						accountStatementCriteria.getFromDate(), accountStatementCriteria.getToDate()))
				.build();
	}

}
