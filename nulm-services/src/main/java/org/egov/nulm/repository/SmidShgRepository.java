
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmShgMemberRequest;
import org.egov.nulm.model.NulmShgRequest;
import org.egov.nulm.model.SmidShgGroup;
import org.egov.nulm.model.SmidShgMemberApplication;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.ColumnsRowMapper;
import org.egov.nulm.repository.rowmapper.ShgMemberListRowMapper;
import org.egov.nulm.repository.rowmapper.ShgMemberRowMapper;
import org.egov.nulm.repository.rowmapper.ShgRowMapper;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SmidShgRepository {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private ShgRowMapper shgrowMapper;
	private ColumnsRowMapper columnsRowMapper;
	private ShgMemberRowMapper shgMemberRowMapper;
	private ShgMemberListRowMapper shgMemberListRowMapper;
	

	@Autowired
	public SmidShgRepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			ShgRowMapper shgrowMapper,ColumnsRowMapper columnsRowMapper,ShgMemberRowMapper shgMemberRowMapper,ShgMemberListRowMapper shgMemberListRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.shgrowMapper = shgrowMapper;
		this.columnsRowMapper=columnsRowMapper;
		this.shgMemberRowMapper=shgMemberRowMapper;
		this.shgMemberListRowMapper=shgMemberListRowMapper;
	}

	public void createGroup(SmidShgGroup shg) {
		NulmShgRequest infoWrapper = NulmShgRequest.builder().nulmShgRequest(shg).build();
		producer.push(config.getSMIDSHGSaveTopic(), infoWrapper);
	}
	
	
	public JSONArray getGroupStatus(SmidShgGroup shg) {
	JSONArray smid = new JSONArray();
		Map<String, Object> paramValues = new HashMap<>();
		
		try {
			paramValues.put("tenantId", shg.getTenantId());
			paramValues.put("shgUuid", shg.getShgUuid());
			
					return smid = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SHG_STATUS_QUERY, paramValues,
							columnsRowMapper);

			

		} catch (Exception e) {
						throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}
	public List<SmidShgGroup> getGroup(SmidShgGroup shg, List<Role> role, Long userId) {
		List<SmidShgGroup> smid = new ArrayList<>();
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("tenantId", shg.getTenantId());
		paramValues.put("fromDate", shg.getFromDate());
		paramValues.put("toDate", shg.getToDate());
		paramValues.put("name", shg.getName());
		try {
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {
					List<Object> statusEmplyee = new ArrayList<>();
					List<Object> status = new ArrayList<>();
					if (shg.getStatus() == null) {
						statusEmplyee.add(SmidShgGroup.StatusEnum.APPROVED.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.AWAITINGFORAPPROVAL.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.REJECTED.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.CREATED.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.DELETED.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.DRAFTED.toString());
						statusEmplyee.add(SmidShgGroup.StatusEnum.UPDATED.toString());
					} else {
						statusEmplyee.add(shg.getStatus().toString());
					}					
					status.add(SmidShgMemberApplication.StatusEnum.APPROVED.toString());
					status.add(SmidShgMemberApplication.StatusEnum.AWAITINGFORAPPROVAL.toString());
					status.add(SmidShgMemberApplication.StatusEnum.DELETED.toString());
					status.add(SmidShgMemberApplication.StatusEnum.REJECTED.toString());
					status.add(SmidShgMemberApplication.StatusEnum.UPDATED.toString());
					status.add(SmidShgMemberApplication.StatusEnum.DELETIONINPROGRESS.toString());
					status.add(SmidShgMemberApplication.StatusEnum.AWAITINGFORDELETION.toString());
					paramValues.put("applicationStatus", status);
					paramValues.put("status", statusEmplyee);
					paramValues.put("createdBy", "");
					paramValues.put("shgId", shg.getShgId());

					return smid = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SHG_QUERY, paramValues,
							shgrowMapper);

				}
			}
			List<Object> statusCitizen = new ArrayList<>();
			List<Object> status = new ArrayList<>();
			if (shg.getStatus() == null) {
				statusCitizen.add(SmidShgGroup.StatusEnum.CREATED.toString());
				statusCitizen.add(SmidShgGroup.StatusEnum.DRAFTED.toString());
				statusCitizen.add(SmidShgGroup.StatusEnum.APPROVED.toString());
				statusCitizen.add(SmidShgGroup.StatusEnum.AWAITINGFORAPPROVAL.toString());
				statusCitizen.add(SmidShgGroup.StatusEnum.DELETED.toString());
				statusCitizen.add(SmidShgGroup.StatusEnum.REJECTED.toString());
			} else {
				statusCitizen.add(shg.getStatus().toString());
			}
			statusCitizen.add(shg.getStatus() == null ? "" : shg.getStatus().toString());
			status.add(SmidShgMemberApplication.StatusEnum.CREATED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.DRAFTED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.APPROVED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.AWAITINGFORAPPROVAL.toString());
			status.add(SmidShgMemberApplication.StatusEnum.DELETED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.REJECTED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.UPDATED.toString());
			status.add(SmidShgMemberApplication.StatusEnum.DELETIONINPROGRESS.toString());
			status.add(SmidShgMemberApplication.StatusEnum.AWAITINGFORDELETION.toString());
	
			paramValues.put("applicationStatus",status);
			paramValues.put("status", statusCitizen);
			paramValues.put("createdBy", userId.toString());
			paramValues.put("shgId", shg.getShgId());

			return smid = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SHG_QUERY, paramValues, shgrowMapper);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}

	public void updateGroup(SmidShgGroup shg) {
		NulmShgRequest infoWrapper = NulmShgRequest.builder().nulmShgRequest(shg).build();
		producer.push(config.getSMIDSHGUpdateTopic(), infoWrapper);
	}

	public void deleteGroup(SmidShgGroup shg) {
		NulmShgRequest infoWrapper = NulmShgRequest.builder().nulmShgRequest(shg).build();
		producer.push(config.getSMIDSHGDeleteTopic(), infoWrapper);
	}

	public void updateGroupStatus(SmidShgGroup shg) {
		NulmShgRequest infoWrapper = NulmShgRequest.builder().nulmShgRequest(shg).build();
		producer.push(config.getSMIDSHGUpdateStatusTopic(), infoWrapper);
	}
	public void updateMemberStatus(SmidShgMemberApplication smidApplication) {
		NulmShgMemberRequest infoWrapper = NulmShgMemberRequest.builder().SmidShgMemberApplication(smidApplication)
				.build();
		producer.push(config.getSmidShgMemberDeleteTopic(), infoWrapper);
	}
	public void checkMemberCount(SmidShgGroup shg) {
		Map<String, String> errorMap = new HashMap<>();
		int i = 0;
		i = jdbcTemplate.queryForObject(NULMQueryBuilder.GET_SHG_MEMBER_COUNT_QUERY,
				new Object[] { shg.getShgUuid(), shg.getTenantId() }, Integer.class);

		if (i < 10) {
			errorMap.put(CommonConstants.INVALID_SHG_REQUEST, CommonConstants.INVALID_SHG_REQUEST_MESSAGE);
			throw new CustomException(errorMap);
		}
	}

	public void checkShgUuid(SmidShgGroup shg) {
		Map<String, String> errorMap = new HashMap<>();
		int i = 0;
		i = jdbcTemplate.queryForObject(NULMQueryBuilder.SHG_UUID_EXIST_QUERY,
				new Object[] { shg.getShgUuid(), shg.getTenantId() }, Integer.class);

		if (i == 0) {
			errorMap.put(CommonConstants.INVALID_SHG_UUID, CommonConstants.INVALID_SHG_UUID_MESSAGE);
			throw new CustomException(errorMap);
		}
	}
	public List<SmidShgMemberApplication> getMemmberList(SmidShgGroup shg) {
		List<SmidShgMemberApplication> smid = new ArrayList<>();
			Map<String, Object> paramValues = new HashMap<>();
			
			try {
				paramValues.put("tenantId", shg.getTenantId());
				paramValues.put("shgUuid", shg.getShgUuid());
				
						return smid = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SHG_MEMBER_STATUS_QUERY, paramValues,
								shgMemberListRowMapper);

				

			} catch (Exception e) {
							throw new CustomException(CommonConstants.ROLE, e.getMessage());
			}

		}
	
	
}
