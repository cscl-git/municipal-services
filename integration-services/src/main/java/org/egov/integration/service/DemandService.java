package org.egov.integration.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.ApiConfiguration;
import org.egov.integration.model.BillResponseV2;
import org.egov.integration.model.BillV2;
import org.egov.integration.model.OwnerInfo;
import org.egov.integration.model.PaymentInfo;
import org.egov.integration.model.Payment;
import org.egov.integration.model.PaymentDetail;
import org.egov.integration.model.PaymentDetails;
import org.egov.integration.model.PaymentsRequest;
import org.egov.integration.model.RequestInfoWrapper;
import org.egov.integration.model.TaxPeriodResponse;
import org.egov.integration.repository.DemandRepository;
import org.egov.integration.web.models.demand.Demand;
import org.egov.integration.web.models.demand.DemandDetail;
import org.egov.integration.web.models.demand.DemandResponse;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DemandService {

	@Autowired
	private ApiConfiguration config;

	

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private DemandRepository demandRepository;



	public List<Payment> generate(PaymentsRequest payment) {
		
		List<PaymentInfo> createCalculations = new LinkedList<>();
		PaymentInfo paymentData = payment.getPayment();
		List<Payment> payment1=new ArrayList<>();
		List<BillV2> billResponse = new ArrayList<>();	
		List<PaymentInfo> updateCalculations = new LinkedList<>();
		StringBuilder sb=new StringBuilder();
		sb.append(paymentData.getServicename());
		sb.append("_");
		sb.append(new Date().getTime());
		paymentData.setConsumerCode(sb.toString());
		if (!(payment.getPayment() == null)) {
			List<Demand> demands = new LinkedList<>();
			Map<String, Long> taxPeriods = getTaxPeriods(payment.getRequestInfo(), paymentData);
			List<Demand> demandsData = searchDemand(paymentData.getTenantId(), paymentData.getConsumerCode(),
					payment.getRequestInfo(), paymentData.getServicename());
			Set<String> applicationNumbersFromDemands = new HashSet<>();
			if (!CollectionUtils.isEmpty(demandsData))
				applicationNumbersFromDemands = demandsData.stream().map(Demand::getConsumerCode)
						.collect(Collectors.toSet());

			// If demand already exists add it updateDemand else createDemand

			if (!applicationNumbersFromDemands.contains(sb))
				createCalculations.add(paymentData);
			else
				updateCalculations.add(paymentData);

			if (!CollectionUtils.isEmpty(createCalculations)) {
				demands = generateDemand(payment.getRequestInfo(), paymentData, taxPeriods);
				
			}
			if (!CollectionUtils.isEmpty(updateCalculations)) {
				demands = updateDemand(payment.getRequestInfo(), updateCalculations.get(0));
				

			}
			// _fetchbill
			if (!CollectionUtils.isEmpty(demands)) {
				billResponse = fetchBill(payment.getRequestInfo(), paymentData);
				System.out.println("billResponse...." + billResponse);
			}
			if (!CollectionUtils.isEmpty(billResponse)) {
				// create payment
				 payment1 =createPayment(payment,billResponse.get(0));
			}

		}
		return payment1;

	}

	private Map<String, Long> getTaxPeriods(RequestInfo requestInfo, PaymentInfo paymentData) {
		Map<String, Long> taxPeriods = new HashMap<>();
	
		StringBuilder url = new StringBuilder(config.getBillingSearchHost());
		url.append("tenantId="+paymentData.getTenantId()).append("&service="+paymentData.getServicename());
		Object result = fetchResult(url,RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		TaxPeriodResponse response = null;
		try {
			response = mapper.convertValue(result, TaxPeriodResponse.class);
			taxPeriods.put(CommonConstants.MDMS_STARTDATE,(Long) response.getTaxPeriods().get(0).getFromDate());
			taxPeriods.put(CommonConstants.MDMS_ENDDATE, (Long) response.getTaxPeriods().get(0).getToDate());
		
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of create demand");
		}
		return taxPeriods;
	}

	private List<Payment> createPayment(PaymentsRequest payment, BillV2 billResponse) {
		PaymentInfo payments=payment.getPayment();
		List<PaymentDetail> paymentDetail=new ArrayList<>();
		PaymentDetail data=new PaymentDetail();
		data.setBillId(billResponse.getId());
		data.setBill(billResponse);
		data.setTotalAmountPaid(billResponse.getTotalAmount());
		paymentDetail.add(data);
		Payment datas=Payment.builder().tenantId(payments.getTenantId()).totalDue(billResponse.getTotalAmount())
				.totalAmountPaid(billResponse.getTotalAmount()).transactionDate(payments.getTransactionDate())
				.instrumentDate(payments.getInstrumentDate()).instrumentNumber(payments.getInstrumentNumber()).instrumentStatus(payments.getInstrumentStatus())
				.ifscCode(payments.getIfscCode()).paidBy(payments.getPaidBy()).mobileNumber(payments.getMobileNumber())
				.payerName(payments.getPaidBy()).payerAddress(payments.getPayerAddress()).payerEmail(payments.getPayerEmail()).payerId(payment.getRequestInfo().getUserInfo().getId().toString())
				.paymentStatus(payments.getPaymentStatus()).paymentMode(payments.getPaymentMode()).paymentDetails(paymentDetail).build();
		return demandRepository.createPayment(payment.getRequestInfo(), datas);
	}

	private List<BillV2> fetchBill(RequestInfo requestInfo, PaymentInfo paymentData) {

		String uri = getBillSearchURL();
		uri = uri.replace("{1}", paymentData.getTenantId());
		uri = uri.replace("{2}", paymentData.getServicename());
		uri = uri.replace("{3}", paymentData.getConsumerCode());

		Object result = fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		BillResponseV2 response;
		try {
			response = mapper.convertValue(result, BillResponseV2.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

		if (CollectionUtils.isEmpty(response.getBill()))
			return null;

		else
			return response.getBill();
	}

	private List<Demand> updateDemand(RequestInfo requestInfo, PaymentInfo paymentData) {
		List<Demand> demands = new LinkedList<>();

		List<Demand> searchResult = searchDemand(paymentData.getTenantId(), paymentData.getConsumerCode(), requestInfo,
				paymentData.getServicename());

		if (CollectionUtils.isEmpty(searchResult))
			throw new CustomException("INVALID UPDATE",
					"No demand exists for ConsumerCode: " + paymentData.getConsumerCode());

		Demand demand = searchResult.get(0);
		List<DemandDetail> demandDetails = demand.getDemandDetails();
		List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(paymentData, demandDetails);
		demand.setDemandDetails(updatedDemandDetails);
		demands.add(demand);

		return demandRepository.updateDemand(requestInfo, demands);
	}

	private List<DemandDetail> getUpdatedDemandDetails(PaymentInfo calculation, List<DemandDetail> demandDetails) {

		List<DemandDetail> newDemandDetails = new ArrayList<>();
		Map<String, List<DemandDetail>> taxHeadToDemandDetail = new HashMap<>();

		demandDetails.forEach(demandDetail -> {
			if (!taxHeadToDemandDetail.containsKey(demandDetail.getTaxHeadMasterCode())) {
				List<DemandDetail> demandDetailList = new LinkedList<>();
				demandDetailList.add(demandDetail);
				taxHeadToDemandDetail.put(demandDetail.getTaxHeadMasterCode(), demandDetailList);
			} else
				taxHeadToDemandDetail.get(demandDetail.getTaxHeadMasterCode()).add(demandDetail);
		});

		BigDecimal diffInTaxAmount;
		List<DemandDetail> demandDetailList;
		BigDecimal total;

		for (PaymentDetails taxHeadEstimate : calculation.getPaymentDetails()) {
			if (!taxHeadToDemandDetail.containsKey(taxHeadEstimate.getTaxHeadCode()))
				newDemandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(calculation.getTenantId())
						.collectionAmount(BigDecimal.ZERO).build());
			else {
				demandDetailList = taxHeadToDemandDetail.get(taxHeadEstimate.getTaxHeadCode());
				total = demandDetailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO,
						BigDecimal::add);
				diffInTaxAmount = taxHeadEstimate.getAmount().subtract(total);
				if (diffInTaxAmount.compareTo(BigDecimal.ZERO) != 0) {
					newDemandDetails.add(DemandDetail.builder().taxAmount(diffInTaxAmount)
							.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(calculation.getTenantId())
							.collectionAmount(BigDecimal.ZERO).build());
				}
			}
		}
		List<DemandDetail> combinedBillDetials = new LinkedList<>(demandDetails);
		combinedBillDetials.addAll(newDemandDetails);

		return combinedBillDetials;
	}

	public List<Demand> generateDemand(RequestInfo requestInfo, PaymentInfo paymentData, Map<String, Long> taxPeriods) {
		List<Demand> demands = new LinkedList<>();

		List<DemandDetail> demandDetails = new LinkedList<>();
		paymentData.getPaymentDetails().forEach(taxHeadEstimate -> {
			demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getAmount())
					.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
					.tenantId(taxHeadEstimate.getTenantId()).build());
		});
		OwnerInfo ownerInfo = OwnerInfo.builder().permanentAddress(paymentData.getPayerAddress())
				.uuid(requestInfo.getUserInfo().getUuid()).emailId(paymentData.getPayerEmail()).build();
		User owner = ownerInfo.toCommonUser();
		//generate consumer code
		demands.add(Demand.builder().consumerCode(paymentData.getConsumerCode()).demandDetails(demandDetails)
				.payer(null).minimumAmountPayable(config.getMinimumPayableAmount()).tenantId(paymentData.getTenantId())
				.taxPeriodFrom(taxPeriods.get(CommonConstants.MDMS_STARTDATE))
				.taxPeriodTo(taxPeriods.get(CommonConstants.MDMS_ENDDATE)).consumerType("MISCELLANEOUS_RECEIPT")
				.businessService(paymentData.getServicename()).build());

		return demandRepository.saveDemand(requestInfo, demands);
	}

	private List<Demand> searchDemand(String tenantId, String consumerCodes, RequestInfo requestInfo,
			String applicationType) {

		String uri = getDemandSearchURL();
		uri = uri.replace("{1}", tenantId);
		uri = uri.replace("{2}", applicationType);
		uri = uri.replace("{3}", StringUtils.join(consumerCodes, ','));

		Object result = fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		DemandResponse response;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

		if (CollectionUtils.isEmpty(response.getDemands()))
			return null;

		else
			return response.getDemands();

	}

	public String getDemandSearchURL() {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		url.append(config.getDemandSearchEndpoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("businessService=");
		url.append("{2}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{3}");
		return url.toString();
	}

	public String getBillSearchURL() {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		url.append(config.getFetchBillEndpoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("businessService=");
		url.append("{2}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{3}");
		return url.toString();
	}

	public Object fetchResult(StringBuilder uri, Object request) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		log.info("URI: " + uri.toString());
		try {
			log.info("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from searcher: ", e);
		}

		return response;

	}

}
