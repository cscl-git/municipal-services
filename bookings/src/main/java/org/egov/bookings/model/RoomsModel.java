package org.egov.bookings.model;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Builder
@ToString
@Entity(name = "RoomsModel")
@Table(name = "BK_ROOMS_MODEL")
public class RoomsModel {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "ROOM_APPLICATION_NUMBER")
	private String roomApplicationNumber;

	@Column(name = "TYPE_OF_ROOM")
	private String typeOfRoom;

	@Column(name = "TOTAL_NO_OF_ROOMS")
	private String totalNoOfRooms;

	@Column(name = "COMMUNITY_APPLICATION_NUMBER")
	private String communityApplicationNumber;

	@Column(name = "ROOM_APPLICATION_STATUS")
	private String roomApplicationStatus;

	@Column(name = "ROOM_BUSINESS_SERVICE")
	private String roomBusinessService;

	@Column(name = "REMARKS")
	private String remarks;

	@Column(name = "ACTION")
	private String action;

	@Column(name = "LAST_MODIFIED_DATE")
	private Date lastModifiedDate;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "FROM_DATE")
	private Date fromDate;

	@Column(name = "TO_DATE")
	private Date toDate;

	@Column(name = "discount")
	private BigDecimal discount;

	@Column(name = "facilation_charge")
	private BigDecimal facilationCharge;

	@Column(name = "room_payment_status")
	private String roomPaymentStatus;
	
	@Column(name = "room_status")
	private String roomStatus;
	
	@Column(name = "room_created_date")
	private String roomCreatedDate;
	
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
