package org.egov.integration.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestObj {
	
	
	 @JsonProperty("moduleName")
	 private String moduleName;
	 
	 @JsonProperty("serviceType")
	 private String serviceType;
}
