package org.egov.integration.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FinanceRequest {
	
	@NotNull
	@JsonProperty("owners")
	@Valid
	private List<OwnerInfo> owners = new ArrayList<>();
	
	@JsonProperty("applicationNumber")
	private String applicationNumber = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("applicationType")
	private String applicationType = null;

	@JsonProperty("financialYear")
	private String financialYear = null;
}
