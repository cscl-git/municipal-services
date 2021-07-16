package org.egov.integration.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentDetails {
	 @JsonProperty("tenantId")
     private String tenantId;

     @JsonProperty("amount")
     private BigDecimal amount;
     
     @JsonProperty("taxHeadCode")
     private String taxHeadCode;
     
}
