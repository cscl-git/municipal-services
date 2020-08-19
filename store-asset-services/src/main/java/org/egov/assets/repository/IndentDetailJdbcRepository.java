package org.egov.assets.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.JdbcRepository;
import org.egov.assets.model.IndentDetail;
import org.egov.assets.repository.entity.IndentDetailEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class IndentDetailJdbcRepository extends JdbcRepository {
	private static final Logger LOG = LoggerFactory.getLogger(IndentDetailJdbcRepository.class);

	static {
		LOG.debug("init indentDetail");
		init(IndentDetailEntity.class);
		LOG.debug("end init indentDetail");
	}

	public IndentDetailJdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public IndentDetailEntity create(IndentDetailEntity entity) {
		super.create(entity);
		return entity;
	}

	public IndentDetailEntity update(IndentDetailEntity entity) {
		super.update(entity);
		return entity;

	}

	public boolean delete(IndentDetailEntity entity, String reason) {
		super.delete(entity, reason);
		return true;

	}

	public IndentDetailEntity findById(IndentDetailEntity entity) {
		List<String> list = allIdentitiferFields.get(entity.getClass().getSimpleName());

		Map<String, Object> paramValues = new HashMap<>();

		for (String s : list) {
			paramValues.put(s, getValue(getField(entity, s), entity));
		}

		List<IndentDetailEntity> indentdetails = namedParameterJdbcTemplate.query(
				getByIdQuery.get(entity.getClass().getSimpleName()).toString(), paramValues,
				new BeanPropertyRowMapper(IndentDetailEntity.class));
		if (indentdetails.isEmpty()) {
			return null;
		} else {
			return indentdetails.get(0);
		}

	}

	public List<IndentDetailEntity> find(List<String> indentNumbers, String tenantId, String searchPurpose) {

		if (indentNumbers.isEmpty()) {
			return new ArrayList<>();
		}
		String query = "select * from indentdetail where indentnumber in (:indentNumbers) and tenantId=:tenantId and deleted is not true ";

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("indentNumbers", indentNumbers);
		paramValues.put("tenantId", tenantId);
		if (searchPurpose != null && searchPurpose.equalsIgnoreCase("PurchaseOrder")) {
			query = query + (" and ( poOrderedQuantity is  null or indentQuantity - poOrderedQuantity > 0 )");
		}

		LOG.info(query);

		List<IndentDetailEntity> indentdetails = namedParameterJdbcTemplate.query(query, paramValues,
				new BeanPropertyRowMapper(IndentDetailEntity.class));
		if (indentdetails.isEmpty()) {
			return null;
		} else {
			return indentdetails;
		}

	}

	public IndentDetail findIndentDetailsId(List<String> ids, String tenantId) {
		if (ids.isEmpty()) {
			return null;
		}
		String query = "select * from indentdetail where id in (:id) and tenantId=:tenantId and deleted is not true ";

		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("id", ids);
		paramValues.put("tenantId", tenantId);

		List<IndentDetailEntity> indentdetails = namedParameterJdbcTemplate.query(query, paramValues,
				new BeanPropertyRowMapper(IndentDetailEntity.class));
		if (indentdetails.isEmpty()) {
			return null;
		} else {
			return indentdetails.get(0).toDomain();
		}

	}

}