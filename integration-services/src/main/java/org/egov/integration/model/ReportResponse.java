package org.egov.integration.model;

import org.egov.common.contract.request.RequestInfo;
import org.json.simple.JSONObject;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponse {


	private String applicantId;
	private String applicantGender;
	private String applicantAge;
	private String applicantSector;
	private String applicantSumbmissionDate;
	private String departmentId;
	private String departmentName;
	private String serviceId;
	private String serviceName;
	private String serviceType;


}
