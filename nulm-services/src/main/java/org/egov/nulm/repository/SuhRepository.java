
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSuhRequest;
import org.egov.nulm.model.SmidShgGroup;
import org.egov.nulm.model.SuhApplication;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.ColumnsRowMapper;
import org.egov.nulm.repository.rowmapper.SuhRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SuhRepository {

	private JdbcTemplate jdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private SuhRowMapper suhrowMapper;
	private ColumnsRowMapper columnsRowMapper;
	
	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public SuhRepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			SuhRowMapper suhrowMapper,ColumnsRowMapper columnsRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.suhrowMapper = suhrowMapper;
		this.columnsRowMapper=columnsRowMapper;
	}

	public void createSuhApplication(SuhApplication suhApplication) {
		NulmSuhRequest infoWrapper = NulmSuhRequest.builder().nulmSuhRequest(suhApplication).build();
		producer.push(config.getSuhApplicationSaveTopic(), infoWrapper);
	}
	
	public void updateSuhApplication(SuhApplication suhApplication) {
		NulmSuhRequest infoWrapper = NulmSuhRequest.builder().nulmSuhRequest(suhApplication).build();
		producer.push(config.getSuhApplicationUpdateTopic(), infoWrapper);
	}
	public void updateSuhApplicationStatus(SuhApplication suhApplication) {
		NulmSuhRequest infoWrapper = NulmSuhRequest.builder().nulmSuhRequest(suhApplication).build();
		producer.push(config.getSuhApplicationUpdateStatusTopic(), infoWrapper);
	}
	public void checkShelterName(SuhApplication suh) {
		Map<String, String> errorMap = new HashMap<>();
		int i = 0;
		i = jdbcTemplate.queryForObject(NULMQueryBuilder.GET_SUH_NAME_QUERY,
				new Object[] {suh.getNameOfShelter(),suh.getTenantId() }, Integer.class);

		if (i > 0) {
			errorMap.put(CommonConstants.INVALID_SUH_REQUEST, CommonConstants.INVALID_SUH_REQUEST_MESSAGE);
			throw new CustomException(errorMap);
		}
	}
	
	public List<SuhApplication> getSuhApplication(SuhApplication suh, List<Role> role, Long userId) {
		List<SuhApplication> suhApp = new ArrayList<>();
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("tenantId", suh.getTenantId());
		paramValues.put("fromDate", suh.getFromDate());
		paramValues.put("toDate", suh.getToDate());
		try {
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {
					List<Object> statusEmplyee = new ArrayList<>();
					if (suh.getApplicationStatus() == null) {
						statusEmplyee.add(SuhApplication.StatusEnum.APPROVED.toString());
						statusEmplyee.add(SuhApplication.StatusEnum.REJECTED.toString());
						statusEmplyee.add(SuhApplication.StatusEnum.CREATED.toString());
					} else {
						statusEmplyee.add(suh.getApplicationStatus().toString());
					}
					paramValues.put("status", statusEmplyee);
					paramValues.put("createdBy", "");
					paramValues.put("suhUuid", suh.getSuhUuid());
					

					return suhApp = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUH_QUERY, paramValues,
							suhrowMapper);

				}
			}
			List<Object> statusCitizen = new ArrayList<>();
			if (suh.getApplicationStatus() == null) {
				statusCitizen.add(SmidShgGroup.StatusEnum.CREATED.toString());
				statusCitizen.add(SuhApplication.StatusEnum.REJECTED.toString());
				statusCitizen.add(SuhApplication.StatusEnum.DRAFTED.toString());
				statusCitizen.add(SuhApplication.StatusEnum.APPROVED.toString());
			} else {
				statusCitizen.add(suh.getApplicationStatus().toString());
			}
			statusCitizen.add(suh.getApplicationStatus() == null ? "" : suh.getApplicationStatus().toString());
			paramValues.put("status", statusCitizen);
			paramValues.put("createdBy", userId.toString());
			paramValues.put("suhUuid", suh.getSuhUuid());

			return suhApp = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUH_QUERY, paramValues, suhrowMapper);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}
	public List<SuhApplication> getShelterName(SuhApplication suh, List<Role> role, Long userId) {
		List<SuhApplication> suhApp = new ArrayList<>();
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("tenantId", suh.getTenantId());
				try {
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {
					List<Object> statusEmplyee = new ArrayList<>();
					if (suh.getApplicationStatus() == null) {
						statusEmplyee.add(SuhApplication.StatusEnum.APPROVED.toString());
						
					} else {
						statusEmplyee.add(suh.getApplicationStatus().toString());
					}
					
					paramValues.put("createdBy", "");
					paramValues.put("status", statusEmplyee);
					return suhApp = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUH_SHELTER_NAME_QUERY, paramValues,
							columnsRowMapper);

				}
			}
			List<Object> statusCitizen = new ArrayList<>();
			if (suh.getApplicationStatus() == null) {
				statusCitizen.add(SuhApplication.StatusEnum.APPROVED.toString());
			} else {
				statusCitizen.add(suh.getApplicationStatus().toString());
			}
			statusCitizen.add(suh.getApplicationStatus() == null ? "" : suh.getApplicationStatus().toString());
			paramValues.put("status", statusCitizen);
			paramValues.put("createdBy", userId.toString());
		

			return suhApp = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUH_SHELTER_NAME_QUERY, paramValues, columnsRowMapper);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}
	
	
}
