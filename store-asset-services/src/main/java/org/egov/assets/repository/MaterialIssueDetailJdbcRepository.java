package org.egov.assets.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.JdbcRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssueDetail;
import org.egov.assets.repository.entity.IndentEntity;
import org.egov.assets.repository.entity.MaterialIssueDetailEntity;
import org.egov.assets.repository.entity.MaterialIssueEntity;
import org.egov.assets.repository.entity.PurchaseOrderDetailEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

@Service
public class MaterialIssueDetailJdbcRepository extends JdbcRepository {

	static {
		init(MaterialIssueDetailEntity.class);
	}

	public Pagination<MaterialIssueDetail> search(String issueNumber, String tenantId, String issueType) {
		String searchQuery = "select * from materialissuedetail :condition :orderby";
		StringBuffer params = new StringBuffer();
		String orderBy = "order by id";
		Map<String, Object> paramValues = new HashMap<>();
		if (issueNumber != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("materialissuenumber in (:issueNumbers)");
			paramValues.put("issueNumbers", issueNumber);
		}
		if (tenantId != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("tenantId = :tenantId");
			paramValues.put("tenantId", tenantId);
		}
		if (params.length() > 0)
			searchQuery = searchQuery.replace(":condition", " where deleted is not true and " + params.toString());
		else
			searchQuery = searchQuery.replace(":condition", "");

		searchQuery = searchQuery.replace(":orderby", orderBy);
		Pagination<MaterialIssueDetail> page = new Pagination<>();
		BeanPropertyRowMapper row = new BeanPropertyRowMapper(MaterialIssueDetailEntity.class);

		List<MaterialIssueDetail> materialIssueDetailList = new ArrayList<>();

		List<MaterialIssueDetailEntity> materialIssueDetailEntities = namedParameterJdbcTemplate
				.query(searchQuery.toString(), paramValues, row);

		for (MaterialIssueDetailEntity materialIssueDetailEntity : materialIssueDetailEntities) {

			materialIssueDetailList.add(materialIssueDetailEntity.toDomain(issueType));
		}

		page.setTotalResults(materialIssueDetailList.size());

		page.setPagedData(materialIssueDetailList);

		return page;
	}

	public Pagination<MaterialIssueDetail> searchById(String id, String tenantId, String issueType) {
		String searchQuery = "select * from materialissuedetail :condition :orderby";
		StringBuffer params = new StringBuffer();
		String orderBy = "order by id";
		Map<String, Object> paramValues = new HashMap<>();
		if (id != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("id in (:id)");
			paramValues.put("id", id);
		}
		if (tenantId != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("tenantId = :tenantId");
			paramValues.put("tenantId", tenantId);
		}
		if (params.length() > 0)
			searchQuery = searchQuery.replace(":condition", " where deleted is not true and " + params.toString());
		else
			searchQuery = searchQuery.replace(":condition", "");

		searchQuery = searchQuery.replace(":orderby", orderBy);
		Pagination<MaterialIssueDetail> page = new Pagination<>();
		BeanPropertyRowMapper row = new BeanPropertyRowMapper(MaterialIssueDetailEntity.class);

		List<MaterialIssueDetail> materialIssueDetailList = new ArrayList<>();

		List<MaterialIssueDetailEntity> materialIssueDetailEntities = namedParameterJdbcTemplate
				.query(searchQuery.toString(), paramValues, row);

		for (MaterialIssueDetailEntity materialIssueDetailEntity : materialIssueDetailEntities) {

			materialIssueDetailList.add(materialIssueDetailEntity.toDomain(issueType));
		}

		page.setTotalResults(materialIssueDetailList.size());

		page.setPagedData(materialIssueDetailList);

		return page;
	}

	public MaterialIssueDetailEntity findById(Object entity, String entityName) {
		List<String> list = allIdentitiferFields.get(entityName);

		Map<String, Object> paramValues = new HashMap<>();

		for (String s : list) {
			paramValues.put(s, getValue(getField(entity, s), entity));
		}

		List<MaterialIssueDetailEntity> indents = namedParameterJdbcTemplate.query(
				getByIdQuery.get(entity.getClass().getSimpleName()).toString(), paramValues,
				new BeanPropertyRowMapper(MaterialIssueDetailEntity.class));
		if (indents.isEmpty()) {
			return null;
		} else {
			return indents.get(0);
		}

	}

}
