package org.egov.nulm.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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

public class SmidApplication {
	
	private String applicationUuid ;
	
	private String applicationId ;
	
	private String nulmApplicationId ;
	
	@NotNull
	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus ;

	public enum StatusEnum {
	    DRAFTED("DRAFTED"),
	    CREATED("CREATED"),
	    APPROVED("APPROVED"),
		REJECTED("REJECTED");

	    private String value;

	    StatusEnum(String value) {
	      this.value = value;
	    }

	    @Override
	    @JsonValue
	    public String toString() {
	      return String.valueOf(value);
	    }

	    @JsonCreator
	    public static StatusEnum fromValue(String text) {
	      for (StatusEnum b : StatusEnum.values()) {
	        if (String.valueOf(b.value).equals(text)) {
	          return b;
	        }
	      }
	      return null;
	    }
	  }
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId ;
		
	@JsonProperty("name")
	private String name ;
	
	@JsonProperty("isUrbanPoor")
	private Boolean isUrbanPoor ;
	
	@JsonProperty("bplNo")
	private String bplNo ;	
	
	@JsonProperty("caste")
	private String caste ;
	
	@JsonProperty("isPwd")
	private Boolean isPwd ;
	
	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName ;
	
	@JsonProperty("qualification")
	private String qualification ;	 
	
	@JsonProperty("dob")
	private String dob ;
	
	@JsonProperty("emailId")
	private String emailId ;
	
	@JsonProperty("mobileNo")
	private String mobileNo ;
	
	@JsonProperty("phoneNo")
	private String phoneNo ;

	@JsonProperty("motherName")
	private String motherName ;
	
	@JsonProperty("address")
	private String address ;
	
	@JsonProperty("gender")
	private String gender ;
	
	@JsonProperty("isMinority")
	private Boolean isMinority ;
	
	@JsonProperty("minority")
	private String minority ;
	
	@JsonProperty("wardNo")
	private String wardNo ;
	
	@JsonProperty("nameAsPerAdhar")
	private String nameAsPerAdhar ;
	
	@JsonProperty("adharNo")
	private String adharNo ;
	
	@JsonProperty("adharAcknowledgementNo")
	private String adharAcknowledgementNo ;

	@JsonProperty("isInsurance")
	private Boolean isInsurance ;
	
	@JsonProperty("insuranceThrough")
	private String insuranceThrough ;
	
	@JsonProperty("isStreetVendor")
	private Boolean isStreetVendor ;
	
	@JsonProperty("isHomeless")
	private Boolean isHomeless ;
	
	@JsonProperty("documentAttachemnt")
	private String documentAttachemnt ;
	
	@JsonProperty("accountNo")
	private String accountNo ;
	
	@JsonProperty("bankName")
	private String bankName;
	
	@JsonProperty("branchName")
	private String branchName;
	
	@JsonProperty("dateOfOpeningAccount")
	private String dateOfOpeningAccount ;
	
	@JsonProperty("isActive")
	private Boolean isActive ;
	
	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;

			
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails ;
	
	@JsonProperty("remark")
	private String remark ;

}
