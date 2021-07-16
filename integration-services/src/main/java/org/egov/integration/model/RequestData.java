package org.egov.integration.model;

import org.egov.common.contract.request.RequestInfo;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class RequestData {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("applicationType")
	private String applicationType;

	@JsonProperty("fromDate")
	private Long fromDate = null;

	@JsonProperty("toDate")
	private Long toDate = null;
	
	@JsonProperty("dataPayload")
	private JSONObject dataPayload;
	
	@JsonProperty("RequestBody")
	private Object requestBody;

}
