package org.egov.bookings.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "BookingsModel")
@Table(name = "BK_BOOKINGS")
@Builder
@ToString
public class BookingsModel {

	@Id
	@Column(name = "BK_APPLICATION_NUMBER")
	@JsonProperty("bkApplicationNumber")
	private String bkApplicationNumber;

	
	@Column(name = "BK_REMARKS")
	private String bkRemarks;

	@JsonProperty("bkHouseNo")
	@Column(name = "BK_HOUSE_NO")
	private String bkHouseNo;

	@OneToMany
	@JoinColumn(name = "application_number")
	private List<TimeslotsModel> timeslots;
	
	@OneToMany
	@JoinColumn(name = "COMMUNITY_APPLICATION_NUMBER")
	private List<RoomsModel> roomsModel;
	
	@JsonProperty("bkAddress")
	@Column(name = "BK_ADDRESS")
	private String bkAddress;

	@JsonProperty("bkSector")
	@Column(name = "BK_SECTOR")
	private String bkSector;

	@JsonProperty("bkVillCity")
	@Column(name = "BK_VILL_CITY")
	private String bkVillCity;

	@JsonProperty("bkAreaRequired")
	@Column(name = "BK_AREA_REQUIRED")
	private String bkAreaRequired;

	@JsonProperty("bkDuration")
	@Column(name = "BK_DURATION")
	private String bkDuration;

	@JsonProperty("bkCategory")
	@Column(name = "BK_CATEGORY")
	private String bkCategory;

	@JsonProperty("bkEmail")
	@Column(name = "BK_EMAIL")
	private String bkEmail;

	@JsonProperty("bkContactNo")
	@Column(name = "BK_CONTACT_NO")
	private String bkContactNo;

	@JsonProperty("bkDocumentUploadedUrl")
	@Column(name = "BK_DOCUMENT_UPLOADED_URL")
	private String bkDocumentUploadedUrl;

	@JsonProperty("bkDateCreated")
	@Column(name = "BK_DATE_CREATED")
	private Date bkDateCreated;

	@JsonProperty("bkCreatedBy")
	@Column(name = "BK_CREATED_BY")
	private Long bkCreatedBy;

	@JsonProperty("bkWfStatus")
	@Column(name = "BK_WF_STATUS")
	private String bkWfStatus;

	@JsonProperty("bkAmount")
	@Column(name = "BK_AMOUNT")
	private String bkAmount;

	@JsonProperty("bkPaymentStatus")
	@Column(name = "BK_PAYMENT_STATUS")
	private String bkPaymentStatus;

	/*@JsonProperty("bkPaymentDate")
	@Column(name = "BK_PAYMENT_DATE")
	private Date bkPaymentDate;
*/
	@JsonProperty("bkBookingType")
	@Column(name = "BK_BOOKING_TYPE")
	private String bkBookingType;

	
	@JsonProperty("bkFromDate")
	@Column(name = "BK_FROM_DATE")
	private Date bkFromDate;

	
	@JsonProperty("bkToDate")
	@Column(name = "BK_TO_DATE")
	private Date bkToDate;

	@JsonProperty("bkApplicantName")
	@Column(name = "BK_APPLICANT_NAME")
	private String bkApplicantName;

	@JsonProperty("bkBookingPurpose")
	@Column(name = "BK_BOOKING_PURPOSE")
	private String bkBookingPurpose;

	@JsonProperty("bkVillage")
	@Column(name = "BK_VILLAGE")
	private String bkVillage;

	@JsonProperty("bkDimension")
	@Column(name = "BK_DIMENSION")
	private String bkDimension;

	@JsonProperty("bkLocation")
	@Column(name = "BK_LOCATION")
	private String bkLocation;

	@JsonProperty("bkStartingDate")
	@Column(name = "BK_STARTING_DATE")
	private Date bkStartingDate;

	@JsonProperty("bkEndingDate")
	@Column(name = "BK_ENDING_DATE")
	private Date bkEndingDate;

	@JsonProperty("bkType")
	@Column(name = "BK_TYPE")
	private String bkType;

	@JsonProperty("bkResidenceProof")
	@Column(name = "BK_RESIDENCE_PROOF")
	private String bkResidenceProof;

	@JsonProperty("bkCleansingCharges")
	@Column(name = "BK_CLEANSING_CHARGES")
	private String bkCleansingCharges;

	@JsonProperty("bkRent")
	@Column(name = "BK_RENT")
	private String bkRent;

	@JsonProperty("bkSurchargeRent")
	@Column(name = "BK_SURCHARGE_RENT")
	private String bkSurchargeRent;

	@JsonProperty("bkFacilitationCharges")
	@Column(name = "BK_FACILITATION_CHARGES")
	private String bkFacilitationCharges;

	@JsonProperty("bkUtgst")
	@Column(name = "BK_UTGST")
	private String bkUtgst;

	@JsonProperty("bkCgst")
	@Column(name = "BK_CGST")
	private String bkCgst;

	@JsonProperty("bkMobileNumber")
	@Column(name = "BK_MOBILE_NUMBER")
	private String bkMobileNumber;

	@JsonProperty("bkCustomerGstNo")
	@Column(name = "BK_CUSTOMER_GST_NO")
	private String bkCustomerGstNo;

	@JsonProperty("bkCurrentCharges")
	@Column(name = "BK_CURRENT_CHARGES")
	private String bkCurrentCharges;

	@JsonProperty("bkLocationChangeAmount")
	@Column(name = "BK_LOCATION_CHANGE_AMOUNT")
	private String bkLocationChangeAmount;

	@JsonProperty("bkVenue")
	@Column(name = "BK_VENUE")
	private String bkVenue;

	@JsonProperty("bkDate")
	@Column(name = "BK_DATE")
	private Date bkDate;

	@JsonProperty("bkFatherName")
	@Column(name = "BK_FATHER_NAME")
	private String bkFatherName;

	@JsonProperty("bkBookingVenue")
	@Column(name = "BK_BOOKING_VENUE")
	private String bkBookingVenue;

	@JsonProperty("bkBookingDuration")
	@Column(name = "BK_BOOKING_DURATION")
	private String bkBookingDuration;

	@JsonProperty("bkIdProof")
	@Column(name = "BK_ID_PROOF")
	private String bkIdProof;

	@JsonProperty("bkApplicantContact")
	@Column(name = "BK_APPLICANT_CONTACT")
	private String bkApplicantContact;

	@JsonProperty("bkOpenSpaceLocation")
	@Column(name = "BK_OPEN_SPACE_LOCATION")
	private String bkOpenSpaceLocation;

	@JsonProperty("bkLandmark")
	@Column(name = "BK_LANDMARK")
	private String bkLandmark;

	@JsonProperty("bkRequirementArea")
	@Column(name = "BK_REQUIREMENT_AREA")
	private String bkRequirementArea;

	@JsonProperty("bkLocationPictures")
	@Column(name = "BK_LOCATION_PICTURES")
	private String bkLocationPictures;

	/*@JsonProperty("bkBookingReferenceNumber")
	@Column(name = "BK_BOOKING_REFERENCE_NUMBER")
	private String bkBookingReferenceNumber;

	@JsonProperty("bkPaymentReceiptNumber")
	@Column(name = "BK_PAYMENT_RECEIPT_NUMBER")
	private String bkPaymentReceiptNumber;*/

	@JsonProperty("bkParkOrCommunityCenter")
	@Column(name = "BK_PARK_OR_COMMUNITY_CENTER")
	private String bkParkOrCommunityCenter;

	@JsonProperty("bkRefundAmount")
	@Column(name = "BK_REFUND_AMOUNT")
	private String bkRefundAmount;

	@JsonProperty("bkBankAccountNumber")
	@Column(name = "BK_BANK_ACCOUNT_NUMBER")
	private String bkBankAccountNumber;

	@JsonProperty("bkBankName")
	@Column(name = "BK_BANK_NAME")
	private String bkBankName;

	@JsonProperty("bkIfscCode")
	@Column(name = "BK_IFSC_CODE")
	private String bkIfscCode;

	@JsonProperty("bkAccountType")
	@Column(name = "BK_ACCOUNT_TYPE")
	private String bkAccountType;
	
	
	@JsonProperty("bkBankAccountHolder")
	@Column(name = "BK_BANK_ACCOUNT_HOLDER")
	private String bkBankAccountHolder;
	
	
	

	@JsonProperty("bkPropertyOwnerName")
	@Column(name = "BK_PROPERTY_OWNER_NAME")
	private String bkPropertyOwnerName;

	@JsonProperty("bkCompleteAddress")
	@Column(name = "BK_COMPLETE_ADDRESS")
	private String bkCompleteAddress;

	@JsonProperty("bkResidentialOrCommercial")
	@Column(name = "BK_RESIDENTIAL_OR_COMMERCIAL")
	private String bkResidentialOrCommercial;

	@JsonProperty("bkMaterialStorageArea")
	@Column(name = "BK_MATERIAL_STORAGE_AREA")
	private String bkMaterialStorageArea; // used for payment mode in pacc

	@JsonProperty("bkPlotSketch")
	@Column(name = "BK_PLOT_SKETCH")
	private String bkPlotSketch; // used for type of discount in pacc

	@JsonProperty("bkApplicationStatus")
	@Column(name = "BK_APPLICATION_STATUS")
	private String bkApplicationStatus;

	@JsonProperty("bkTime")
	@Column(name = "BK_TIME")
	private String bkTime;

	@JsonProperty("bkStatusUpdateRequest")
	@Column(name = "BK_STATUS_UPDATE_REQUEST")
	private String bkStatusUpdateRequest;

	@JsonProperty("bkStatus")
	@Column(name = "BK_STATUS")
	private String bkStatus;

	@JsonProperty("bkDriverName")
	@Column(name = "BK_DRIVER_NAME")
	private String bkDriverName;

	@JsonProperty("bkVehicleNumber")
	@Column(name = "BK_VEHICLE_NUMBER")
	private String bkVehicleNumber;

	@JsonProperty("bkEstimatedDeliveryTime")
	@Column(name = "BK_ESTIMATED_DELIVERY_TIME")
	private String bkEstimatedDeliveryTime;

	@JsonProperty("bkActualDeliveryTime")
	@Column(name = "BK_ACTUAL_DELIVERY_TIME")
	private String bkActualDeliveryTime;

	@JsonProperty("bkNormalWaterFailureRequest")
	@Column(name = "BK_NORMAL_WATER_FAILURE_REQUEST")
	private String bkNormalWaterFailureRequest;

	@JsonProperty("bkUpdateStatusOption")
	@Column(name = "BK_UPDATE_STATUS_OPTION")
	private String bkUpdateStatusOption;

	@JsonProperty("bkAddSpecialRequestDetails")
	@Column(name = "BK_ADD_SPECIAL_REQUEST_DETAILS")
	private String bkAddSpecialRequestDetails;

	@JsonProperty("bkBookingTime")
	@Column(name = "BK_BOOKING_TIME")
	private String bkBookingTime;

	@JsonProperty("bkApprovedBy")
	@Column(name = "BK_APPROVED_BY")
	private String bkApprovedBy;

	@JsonProperty("bkModuleType")
	@Column(name = "BK_MODULE_TYPE")
	private String bkModuleType;

	@JsonProperty("uuid")
	@Column(name = "UUID")
	private String uuid;

	@JsonProperty("tenantId")
	@Column(name = "TENANT_ID")
	private String tenantId;

	@JsonProperty("bkAction")
	@Column(name = "BK_ACTION")
	private String bkAction;

	@JsonProperty("bkConstructionType")
	@Column(name = "BK_CONSTRUCTION_TYPE")
	private String bkConstructionType;
	
	@JsonProperty("businessService")
	@Column(name = "BUSINESS_SERVICE")
	private String businessService;
	
	@JsonProperty("bkApproverName")
	@Column(name = "APPROVER_NAME")
	private String bkApproverName;

	
	@JsonProperty("discount")
	@Column(name = "discount")
	private BigDecimal discount;
	
	@Size(max = 64)
	@JsonProperty("assignee")
	@Transient
	private String assignee = null;

	@Valid
	@JsonProperty("wfDocuments")
	@Transient
	private List<Document> wfDocuments;
	
	
    @Size(max=64)
    @JsonProperty("financialYear")
    private String financialYear = null;
    
    @Transient
    private boolean reInitiateStatus;
    
    @JsonProperty("financeBusinessService")
    @Transient
    private String financeBusinessService;
    
	@Column(name = "created_date")
	private String createdDate;

	@Column(name = "last_modified_date")
	private String lastModifiedDate;
	
	@Column(name = "BK_NOMINEE_NAME")
	private String bkNomineeName;
	
	@Column(name = "REFUNDABLE_SECURITY_MONEY")
	private BigDecimal refundableSecurityMoney;
	
	@Column(name = "CARD_NUMBER")
	private String cardNumber;
	
	@Column(name = "TRANSACTION_NUMBER")
	private String transactionNumber;
	
	@Column(name = "PAYMENT_COLLECTION_TYPE")
	private String paymentCollectionType;
	
	@Column(name = "PAID_BY")
	private String paidBy;
	
	@Column(name = "PAYER_NAME")
	private String payerName;
	
	@Column(name = "PAYER_MOBILE_NUMBER")
	private String payerMobileNumber;
	
	@Column(name = "CHEQUE_NUMBER")
	private String chequeNumber;
	
	@Column(name = "PAYMENT_DATE")
	private String paymentDate;
	
	@Column(name = "IFSC_CODE")
	private String ifscCode;
	
	@Column(name = "BANK_NAME")
	private String bankName;
	
	@Column(name = "BANK_BRANCH")
	private String bankBranch;
	
}
