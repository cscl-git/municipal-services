package org.egov.integration.model;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentInfo {
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("consumerCode")
	private String consumerCode;

	@JsonProperty("totalAmountPaid")
	private Long totalAmountPaid;

	@JsonProperty("transactionNumber")
	private String transactionNumber;

	@JsonProperty("transactionDate")
	private Long transactionDate;

	@NotNull
	@JsonProperty("paymentMode")
	private PaymentModeEnum paymentMode;

	@JsonProperty("instrumentDate")
	private Long instrumentDate;

	@JsonProperty("instrumentNumber")
	private String instrumentNumber;

	@JsonProperty("instrumentStatus")
	private InstrumentStatusEnum instrumentStatus;

	@JsonProperty("paidBy")
	private String paidBy;

	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@JsonProperty("payerAddress")
	private String payerAddress;

	@JsonProperty("payerEmail")
	private String payerEmail;

	@JsonProperty("paymentStatus")
	private PaymentStatusEnum paymentStatus;

	@JsonProperty("narration")
	private String narration;

	@JsonProperty("bankName")
	private String bankName;

	@JsonProperty("bankBranch")
	private String bankBranch;

	@JsonProperty("ifscCode")
	private String ifscCode;

	@JsonProperty("subdivison")
	private String subdivison;

	@JsonProperty("servicename")
	private String servicename;

	@JsonProperty("collectedbyname")
	private String collectedbyname;

	@JsonProperty("gstno")
	private String gstno;

	@JsonProperty("paymentDetails")
	@Valid
	private List<PaymentDetails> paymentDetails = null;
}
