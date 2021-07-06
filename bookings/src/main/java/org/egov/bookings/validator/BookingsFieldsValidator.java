package org.egov.bookings.validator;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;

import org.egov.bookings.contract.BookingLockRequest;
import org.egov.bookings.contract.CommercialGrndAvailabiltyLockRequest;
import org.egov.bookings.contract.CommercialGroundAvailabiltySearchCriteria;
import org.egov.bookings.contract.CommercialGroundFeeSearchCriteria;
import org.egov.bookings.contract.JurisdictionAvailabilityRequest;
import org.egov.bookings.contract.MasterRequest;
import org.egov.bookings.contract.OsbmSearchCriteria;
import org.egov.bookings.contract.ParkAndCommunitySearchCriteria;
import org.egov.bookings.contract.ParkCommunityFeeMasterRequest;
import org.egov.bookings.contract.RoomFeeFetchRequest;
import org.egov.bookings.model.BookingLockModel;
import org.egov.bookings.model.BookingsModel;
import org.egov.bookings.model.CommercialGrndAvailabilityModel;
import org.egov.bookings.model.RoomsModel;
import org.egov.bookings.repository.BookingsRepository;
import org.egov.bookings.repository.impl.BillingServiceRepository;
import org.egov.bookings.utils.BookingsConstants;
import org.egov.bookings.web.models.BookingsRequest;
import org.egov.bookings.web.models.NewLocationRequest;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class BookingsFieldsValidator.
 */
@Component
public class BookingsFieldsValidator {

	@Autowired
	private BillingServiceRepository billingServiceRepository;
	
	@Autowired
	private BookingsRepository bookingsRepository;
	
	/**
	 * Validate tenant id.
	 *
	 * @param tenantId the tenant id
	 */
	public void validateTenantId(String tenantId) {
		if (tenantId == null || tenantId == "")
			throw new IllegalArgumentException("Invalid TenantId");
	}

	/**
	 * Validate business service.
	 *
	 * @param businessService the business service
	 */
	public void validateBusinessService(String businessService) {
		if (businessService == null || businessService == "")
			throw new IllegalArgumentException("Invalid businessService");
	}

	/**
	 * Validate action.
	 *
	 * @param action the action
	 */
	public void validateAction(String action) {
		if (action == null || action == "")
			throw new IllegalArgumentException("Invalid Action");
	}

	/**
	 * Checks if is null or empty.
	 *
	 * @param object the object
	 * @return true, if is null or empty
	 */
	public static boolean isNullOrEmpty(final Object object) {
		if (object == null)
			return true;
		if (object instanceof String)
			return ((String) object).length() == 0;
		if (object instanceof Collection)
			return ((Collection<?>) object).isEmpty();
		if (object instanceof Map)
			return ((Map<?, ?>) object).isEmpty();
		if (object.getClass().isArray()) {
			if (Array.getLength(object) == 0) {
				return true;
			} else {
				// test 1st dim array
				for (int i = 0; i < Array.getLength(object); i++) {
					if (Array.get(object, i) != null) {
						// check if 2 dim array
						if (Array.get(object, i).getClass().isArray()) {
							if (Array.getLength(Array.get(object, i)) == 0) {
								return true;
							}
							for (int j = 0; j < Array.getLength(Array.get(object, i)); j++) {
								if (Array.get(Array.get(object, j), i) != null) {
									// means found at least one data not null
									return false;
								}
							}
							// means all data of a row are null
							return true;
						} else {
							// means 1 dim array and one data not null
							return false;
						}
					}
				}
				// all data are null
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate osbm search criteria.
	 *
	 * @param osbmSearchCriteria the osbm search criteria
	 */
	public void validateOsbmSearchCriteria(OsbmSearchCriteria osbmSearchCriteria) {

		if (null == osbmSearchCriteria) {
			throw new IllegalArgumentException("Invalid Search Criteria Field");
		} else if (null == osbmSearchCriteria.getConstructionType() || osbmSearchCriteria.getConstructionType() == "") {
			throw new IllegalArgumentException("Invalid constructionType");
		}

		else if (null == osbmSearchCriteria.getDurationInMonths() || osbmSearchCriteria.getDurationInMonths() == "") {
			throw new IllegalArgumentException("Invalid durationInMonths");
		}

		else if (null == osbmSearchCriteria.getResidentialCommercial()
				|| osbmSearchCriteria.getResidentialCommercial() == "") {
			throw new IllegalArgumentException("Invalid residentialCommercial");
		}

		else if (null == osbmSearchCriteria.getStorage() || osbmSearchCriteria.getStorage() == "") {
			throw new IllegalArgumentException("Invalid getStorage");
		}

		else if (null == osbmSearchCriteria.getVillageCity() || osbmSearchCriteria.getVillageCity() == "") {
			throw new IllegalArgumentException("Invalid villageCity");
		}

	}

	/**
	 * Validate commercial ground criteria.
	 *
	 * @param commercialGroundFeeSearchCriteria the commercial ground fee search
	 *                                          criteria
	 */
	public void validateCommercialGroundCriteria(CommercialGroundFeeSearchCriteria commercialGroundFeeSearchCriteria) {
		if (null == commercialGroundFeeSearchCriteria) {
			throw new IllegalArgumentException("Invalid Commercial Ground Fee Search Criteria");
		} else if (null == commercialGroundFeeSearchCriteria.getCategory()
				|| commercialGroundFeeSearchCriteria.getCategory() == "") {
			throw new IllegalArgumentException("Invalid Category");
		}

		else if (null == commercialGroundFeeSearchCriteria.getBookingVenue()
				|| commercialGroundFeeSearchCriteria.getBookingVenue() == "") {
			throw new IllegalArgumentException("Invalid Booking Venue");
		}
	}

	/**
	 * Validate commercial ground availability criteria.
	 *
	 * @param commercialGroundAvailabiltySearchCriteria the commercial ground
	 *                                                  availabilty search criteria
	 */
	public void validateCommercialGroundAvailabilityCriteria(
			CommercialGroundAvailabiltySearchCriteria commercialGroundAvailabiltySearchCriteria) {
		if (null == commercialGroundAvailabiltySearchCriteria) {
			throw new IllegalArgumentException("Invalid Commercial Ground Availability Search Criteria");
		} else if (null == commercialGroundAvailabiltySearchCriteria.getBookingVenue()) {
			throw new IllegalArgumentException("Invalid Commercial Ground Booking Venue");
		}
	}

	/**
	 * Validate approver body.
	 *
	 * @param masterRequest the master request
	 */
	public void validateApproverBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getApproverList())) {
			throw new IllegalArgumentException("Invalid Approver Request Body");
		} else if (isNullOrEmpty(masterRequest.getApproverList().get(0).getSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		} else if (isNullOrEmpty(masterRequest.getApproverList().get(0).getId())) {
			throw new IllegalArgumentException("Invalid Id");
		}else if (isNullOrEmpty(masterRequest.getApproverList().get(0).getUuid())) {
			throw new IllegalArgumentException("Invalid uuid");
		}else if (isNullOrEmpty(masterRequest.getApproverList().get(0).getRoleCode())) {
			throw new IllegalArgumentException("Invalid role code");
		}
	}
	
	/**
	 * Validate OSBM fee body.
	 *
	 * @param masterRequest the master request
	 */
	public void validateOSBMFeeBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getOsbmFeeList())) {
			throw new IllegalArgumentException("Invalid OSBM Fee Request Body");
		} else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getResidentialCommercial())) {
			throw new IllegalArgumentException("Invalid Residential Commercial");
		} else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getConstructionType())) {
			throw new IllegalArgumentException("Invalid Construction type");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getDurationInMonths())) {
			throw new IllegalArgumentException("Invalid duration ");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getStorage())) {
			throw new IllegalArgumentException("Invalid Storage");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getVillageCity())) {
			throw new IllegalArgumentException("Invalid Village/City");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getFromDate())) {
			throw new IllegalArgumentException("Invalid From Date");
		}
	}
	
	/**
	 * Validate OSUJM fee body.
	 *
	 * @param masterRequest the master request
	 */
	public void validateOSUJMFeeBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getOsujmFeeList())) {
			throw new IllegalArgumentException("Invalid OSBM Fee Request Body");
		} else if (isNullOrEmpty(masterRequest.getOsujmFeeList().get(0).getSector())) {
			throw new IllegalArgumentException("Invalid sector");
		} else if (isNullOrEmpty(masterRequest.getOsujmFeeList().get(0).getSlab())) {
			throw new IllegalArgumentException("Invalid slab");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getFromDate())) {
			throw new IllegalArgumentException("Invalid From Date");
		}
	}
	
	/**
	 * Validate GFCP fee body.
	 *
	 * @param masterRequest the master request
	 */
	public void validateGFCPFeeBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getGfcpFeeList())) {
			throw new IllegalArgumentException("Invalid GFCP Fee Request Body");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getCategory())) {
			throw new IllegalArgumentException("Invalid Category");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getRatePerDay())) {
			throw new IllegalArgumentException("Invalid Rate per day");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getBookingVenue())) {
			throw new IllegalArgumentException("Invalid Booking venue");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getFromDate())) {
			throw new IllegalArgumentException("Invalid From Date");
		}
	}
	
	/**
	 * Validate PACC fee body.
	 *
	 * @param masterRequest the master request
	 */
	public void validatePACCFeeBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getPaccFeeList())) {
			throw new IllegalArgumentException("Invalid GFCP Fee Request Body");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getName())) {
			throw new IllegalArgumentException("Invalid Name");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}else if (isNullOrEmpty(masterRequest.getGfcpFeeList().get(0).getVenueType())) {
			throw new IllegalArgumentException("Invalid Venue Name");
		}else if (isNullOrEmpty(masterRequest.getOsbmFeeList().get(0).getFromDate())) {
			throw new IllegalArgumentException("Invalid From Date");
		}
	}
	

	/**
	 * Validate new location request.
	 *
	 * @param newLocationRequest the new location request
	 */
	public void validateNewLocationRequest(NewLocationRequest newLocationRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest)) {
			throw new IllegalArgumentException("Invalid New Location Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getRequestInfo())) {
			throw new IllegalArgumentException("Invalid Request Info");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel())) {
			throw new IllegalArgumentException("Invalid New NewLocationModel object");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getAction())) {
			throw new IllegalArgumentException("Invalid Action");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getApplicantAddress())) {
			throw new IllegalArgumentException("Invalid APPLICANT ADDRESS");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getApplicantName())) {
			throw new IllegalArgumentException("Invalid APPLICANT NAME");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getAreaRequirement())) {
			throw new IllegalArgumentException("Invalid AREA REQUIREMENT");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getBusinessService())) {
			throw new IllegalArgumentException("Invalid BUSINESS SERVICE");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getContact())) {
			throw new IllegalArgumentException("Invalid CONTACT");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getIdProof())) {
			throw new IllegalArgumentException("Invalid ID PROOF");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getLandmark())) {
			throw new IllegalArgumentException("Invalid LANDMARK");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getLocalityAddress())) {
			throw new IllegalArgumentException("Invalid LOCALITY ADDRESS");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getMailAddress())) {
			throw new IllegalArgumentException("Invalid MAIL ADDRESS");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getSector())) {
			throw new IllegalArgumentException("Invalid SECTOR");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getTenantId())) {
			throw new IllegalArgumentException("Invalid TENANT ID");
		}
	}

	/**
	 * Validate new location request for update.
	 *
	 * @param newLocationRequest the new location request
	 */
	public void validateNewLocationRequestForUpdate(NewLocationRequest newLocationRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest)) {
			throw new IllegalArgumentException("Invalid New Location Request");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel())) {
			throw new IllegalArgumentException("Invalid New NewLocationModel object");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getApplicationNumber())) {
			throw new IllegalArgumentException("Invalid APPLICATION NUMBER");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getAction())) {
			throw new IllegalArgumentException("Invalid Action");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getBusinessService())) {
			throw new IllegalArgumentException("Invalid BUSINESS SERVICE");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(newLocationRequest.getNewLocationModel().getTenantId())) {
			throw new IllegalArgumentException("Invalid TENANT ID");
		}
	}

	/**
	 * Validate commercial ground availability model.
	 *
	 * @param commercialGrndAvailabilityModel the commercial grnd availability model
	 */
	public void validateCommercialGroundAvailabilityModel(
			CommercialGrndAvailabiltyLockRequest commercialGrndAvailabiltyLockRequest) {

		for (CommercialGrndAvailabilityModel availabilityModel : commercialGrndAvailabiltyLockRequest
				.getCommercialGrndAvailabilityLock()) {
			if (BookingsFieldsValidator
					.isNullOrEmpty(commercialGrndAvailabiltyLockRequest.getCommercialGrndAvailabilityLock())) {
				throw new IllegalArgumentException("Invalid commercialGrndAvailabilityModel object");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(availabilityModel.getBookingVenue())) {
				throw new IllegalArgumentException("Invalid Booking Venue");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(availabilityModel.getFromDate())) {
				throw new IllegalArgumentException("Invalid From Date");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(availabilityModel.getToDate())) {
				throw new IllegalArgumentException("Invalid To Date");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(availabilityModel.isLocked())) {
				throw new IllegalArgumentException("Invalid isLocked");
			}
		}
	}

	/**
	 * Validate fee request.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void validateFeeRequest(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Bookings Request");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking object");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkAreaRequired())) {
			throw new IllegalArgumentException("Invalid bkAreaRequired");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}

	}

	/**
	 * Validate jurisdiction availablity request.
	 *
	 * @param jurisdictionAvailabilityRequest the jurisdiction availability request
	 */
	public void validateJurisdictionAvailablityRequest(
			JurisdictionAvailabilityRequest jurisdictionAvailabilityRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(jurisdictionAvailabilityRequest)) {
			throw new IllegalArgumentException("Invalid Jurisdiction Availability Request");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(jurisdictionAvailabilityRequest.getBookingVenue())) {
			throw new IllegalArgumentException("Invalid Booking Venue");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(jurisdictionAvailabilityRequest.getBkSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}

	}

	public void validateGrndAvailabilityRequest(BookingsRequest bookingsRequest) {

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkBookingVenue())) {
			throw new IllegalArgumentException("Invalid Booking Venue");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkBookingType())) {
			throw new IllegalArgumentException("Invalid Booking Type");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
			throw new IllegalArgumentException("Invalid Application Number");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkFromDate())) {
			throw new IllegalArgumentException("Invalid Booking From Date");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkToDate())) {
			throw new IllegalArgumentException("Invalid Booking To Date");
		}

	}

	public void validateJurisdictionAvailablityRqst(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkBookingVenue())) {
			throw new IllegalArgumentException("Invalid Booking Venue");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkBookingType())) {
			throw new IllegalArgumentException("Invalid Booking Type");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkSector())) {
			throw new IllegalArgumentException("Invalid Booking Sector");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkFromDate())) {
			throw new IllegalArgumentException("Invalid Booking From Date");
		}
		
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkToDate())) {
			throw new IllegalArgumentException("Invalid Booking To Date");
		}
	}

	public void validateUpdateBookingRequest(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getRequestInfo())) {
			throw new IllegalArgumentException("Invalid Request Info");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
			throw new IllegalArgumentException("Invalid Application Number");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBusinessService())) {
			throw new IllegalArgumentException("Invalid Business Service");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkAction())) {
			throw new IllegalArgumentException("Invalid Action");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getTenantId())) {
			throw new IllegalArgumentException("Invalid Tenant Id");
		}
		if (BookingsConstants.APPLY.equals(bookingsRequest.getBookingsModel().getBkAction()) && BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}

	}

	public void validateCreateBookingRequest(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getRequestInfo())) {
			throw new IllegalArgumentException("Invalid Request Info");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBusinessService())) {
			throw new IllegalArgumentException("Invalid Business Service");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkAction())) {
			throw new IllegalArgumentException("Invalid Action");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getTenantId())) {
			throw new IllegalArgumentException("Invalid Tenant Id");
		}

	}

	public void validatePAndCBookingRequest(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getRequestInfo())) {
			throw new IllegalArgumentException("Invalid Request Info");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBusinessService())) {
			throw new IllegalArgumentException("Invalid Business Service");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkAction())) {
			throw new IllegalArgumentException("Invalid Action");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getTenantId())) {
			throw new IllegalArgumentException("Invalid Tenant Id");
		}
	}

	public void validateParkAndCommunityMasterRequest(ParkCommunityFeeMasterRequest parkCommunityFeeMasterRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(parkCommunityFeeMasterRequest.getVenueType())) {
			throw new IllegalArgumentException("Invalid Venue Type");
		}
		if (BookingsFieldsValidator.isNullOrEmpty(parkCommunityFeeMasterRequest.getSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}
		
	}

	public void validateRefundAmount(BookingsRequest bookingsRequest) {
		BookingsModel bookingsModel = bookingsRepository
				.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
		if (isNullOrEmpty(bookingsModel)) {
			throw new CustomException("INVALID_APPLICATION_NO",
					"Application Number provided does not exist or is in an invalid");
		} else {
			long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(bookingsModel.getBkFromDate() + ""));
			for (Role role : bookingsRequest.getRequestInfo().getUserInfo().getRoles()) {
				if (role.getCode().equals(BookingsConstants.PAYMENT_PROCESSING_AUTHORITY)) {
					if (days <= BookingsConstants.DAYS_FIFTEEN
							&& BookingsConstants.PAY.equals(bookingsRequest.getBookingsModel().getBkAction())) {
						throw new CustomException("INVALID_DAYS_DIFFERENCE",
								"Less than or equal to 15 days left you cannot process refund");
					}
				}

			}
		}
	}

	public void validatePACCSearchCriteria(ParkAndCommunitySearchCriteria parkAndCommunitySearchCriteria) {
		
		if(isNullOrEmpty(parkAndCommunitySearchCriteria)) {
			throw new IllegalArgumentException("Invalid park and community search criteria");
		}
		
		if(isNullOrEmpty(parkAndCommunitySearchCriteria.getSector())) {
			throw new IllegalArgumentException("Invalid Sector in park and community search criteria");
		}
		if(isNullOrEmpty(parkAndCommunitySearchCriteria.getBookingType())) {
			throw new IllegalArgumentException("Invalid booking Type in park and community search criteria");
		}
		if(isNullOrEmpty(parkAndCommunitySearchCriteria.getBookingVenue())) {
			throw new IllegalArgumentException("Invalid booking benue in park and community search criteria");
		}
		if(isNullOrEmpty(parkAndCommunitySearchCriteria.getApplicationNumber())) {
			parkAndCommunitySearchCriteria.setApplicationNumber("");
		}
		
	}
	
	/**
	 * Validate community center room fee body.
	 *
	 * @param masterRequest the master request
	 */
	public void validateCommunityCenterRoomFeeBody(MasterRequest masterRequest) {
		if (isNullOrEmpty(masterRequest) || isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList())) {
			throw new IllegalArgumentException("Invalid Community Center Room Fee Request Body");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getTotalNumberOfRooms())) {
			throw new IllegalArgumentException("Invalid Total number of Rooms");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getTypeOfRoom())) {
			throw new IllegalArgumentException("Invalid Type of Room");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getRentForOneDay())) {
			throw new IllegalArgumentException("Invalid Rent for One Day");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getRentFor3Hrs())) {
			throw new IllegalArgumentException("Invalid Rent for One Day");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getRentFor6Hrs())) {
			throw new IllegalArgumentException("Invalid Rent for One Day");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getRentFor9Hrs())) {
			throw new IllegalArgumentException("Invalid Rent for One Day");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}else if (isNullOrEmpty(masterRequest.getCommunityCenterRoomFeeList().get(0).getCommunityCenterName())) {
			throw new IllegalArgumentException("Invalid Community Center Name");
		}
	}

	public void validateRoomFeeFetchRequest(RoomFeeFetchRequest roomFeeFetchRequest) {

		if (BookingsFieldsValidator.isNullOrEmpty(roomFeeFetchRequest)) {
			throw new IllegalArgumentException("Invalid Fee Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(roomFeeFetchRequest.getSector())) {
			throw new IllegalArgumentException("Invalid Sector");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(roomFeeFetchRequest.getTotalNumberOfRooms())) {
			throw new IllegalArgumentException("Invalid Total Number of rooms");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(roomFeeFetchRequest.getTypeOfRomm())) {
			throw new IllegalArgumentException("Invalid type of rooms");
		}

	}

	public void validateRoomBookingRequest(BookingsRequest bookingsRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getRoomsModel())) {
			throw new IllegalArgumentException("Invalid Rooms Model");
		}
		for (RoomsModel roomModel : bookingsRequest.getBookingsModel().getRoomsModel()) {
			if (BookingsFieldsValidator.isNullOrEmpty(roomModel.getAction())) {
				throw new IllegalArgumentException("Invalid Action For Room Booking");
			}

			if (BookingsFieldsValidator.isNullOrEmpty(roomModel.getTotalNoOfRooms())) {
				throw new IllegalArgumentException("Invalid Total Number For Room Booking");
			}

			if (BookingsFieldsValidator.isNullOrEmpty(roomModel.getTypeOfRoom())) {
				throw new IllegalArgumentException("Invalid type of rooms For Room Booking");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(roomModel.getRoomBusinessService())) {
				throw new IllegalArgumentException("Invalid room business service For Room Booking");
			}
		}
	}

	public void validateCommercialGroundAvailabilityUpdateModel(
			CommercialGrndAvailabiltyLockRequest commercialGrndAvailabiltyLockRequest) {

		for (CommercialGrndAvailabilityModel availabilityModel : commercialGrndAvailabiltyLockRequest
				.getCommercialGrndAvailabilityLock()) {
			if (BookingsFieldsValidator
					.isNullOrEmpty(commercialGrndAvailabiltyLockRequest.getCommercialGrndAvailabilityLock())) {
				throw new IllegalArgumentException("Invalid commercialGrndAvailabilityModel object");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(availabilityModel.getId())) {
				throw new IllegalArgumentException("Invalid ID");
			}
		}
	}

	public void validateBookingCardDetails(BookingsRequest bookingsRequest) {

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
			throw new IllegalArgumentException("Invalid Card Details Can't be null or blank");
		}
	}

	public void validateBookingLockDetails(BookingLockRequest bookingLockRequest) {
		if (BookingsFieldsValidator.isNullOrEmpty(bookingLockRequest)) {
			throw new IllegalArgumentException("Invalid Booking Request");
		}

		if (BookingsFieldsValidator.isNullOrEmpty(bookingLockRequest.getBookingLockModel())) {
			throw new IllegalArgumentException("Invalid Booking Model object");
		}

		for (BookingLockModel bookingLockModel : bookingLockRequest.getBookingLockModel()) {
			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getApplicationNumber())) {
				throw new IllegalArgumentException("Invalid application number Can't be null or blank");
			}

			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getBookingType())) {
				throw new IllegalArgumentException("Invalid booking type Can't be null or blank");
			}

			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getBookingVenue())) {
				throw new IllegalArgumentException("Invalid booking venue Can't be null or blank");
			}

			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getSector())) {
				throw new IllegalArgumentException("Invalid sector Can't be null or blank");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getFromDate())) {
				throw new IllegalArgumentException("Invalid from date Can't be null or blank");
			}
			if (BookingsFieldsValidator.isNullOrEmpty(bookingLockModel.getToDate())) {
				throw new IllegalArgumentException("Invalid to date Can't be null or blank");
			}
		}
	}
	
}
