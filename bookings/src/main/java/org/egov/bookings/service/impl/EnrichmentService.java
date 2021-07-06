package org.egov.bookings.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.egov.bookings.config.BookingsConfiguration;
import org.egov.bookings.contract.BookingsRequestKafka;
import org.egov.bookings.contract.IdResponse;
import org.egov.bookings.contract.NewLocationKafkaRequest;
import org.egov.bookings.contract.ParkCommunityFeeMasterResponse;
import org.egov.bookings.contract.TaxHeadMasterFields;
import org.egov.bookings.contract.UserDetails;
import org.egov.bookings.dto.SearchCriteriaFieldsDTO;
import org.egov.bookings.model.BookingLockModel;
import org.egov.bookings.model.BookingsModel;
import org.egov.bookings.model.CommercialGrndAvailabilityModel;
import org.egov.bookings.model.OsbmApproverModel;
import org.egov.bookings.model.OsujmNewLocationModel;
import org.egov.bookings.model.ParkCommunityHallV1MasterModel;
import org.egov.bookings.model.RoomsModel;
import org.egov.bookings.model.TimeslotsModel;
import org.egov.bookings.model.user.OwnerInfo;
import org.egov.bookings.model.user.UserDetailResponse;
import org.egov.bookings.models.demand.Demand;
import org.egov.bookings.models.demand.DemandDetail;
import org.egov.bookings.models.demand.TaxHeadEstimate;
import org.egov.bookings.producer.BookingsProducer;
import org.egov.bookings.repository.BookingLockRepository;
import org.egov.bookings.repository.BookingsRepository;
import org.egov.bookings.repository.CommonRepository;
import org.egov.bookings.repository.OsbmApproverRepository;
import org.egov.bookings.repository.OsujmNewLocationRepository;
import org.egov.bookings.repository.RoomsRepository;
import org.egov.bookings.repository.impl.IdGenRepository;
import org.egov.bookings.service.BookingsService;
import org.egov.bookings.service.DemandService;
import org.egov.bookings.service.RoomsService;
import org.egov.bookings.utils.BookingsCalculatorConstants;
import org.egov.bookings.utils.BookingsConstants;
import org.egov.bookings.utils.BookingsUtils;
import org.egov.bookings.validator.BookingsFieldsValidator;
import org.egov.bookings.web.models.BookingsRequest;
import org.egov.bookings.web.models.NewLocationRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.sql.Timestamp;
import java.util.Calendar;
// TODO: Auto-generated Javadoc
/**
 * The Class EnrichmentService.
 */
@Service
public class EnrichmentService {

	/** The bookings utils. */
	@Autowired
	private BookingsUtils bookingsUtils;

	/** The config. */
	@Autowired
	private BookingsConfiguration config;

	/** The id gen repository. */
	@Autowired
	private IdGenRepository idGenRepository;

	/** The bookings service. */
	@Autowired
	private BookingsService bookingsService;

	/** The demand service. */
	@Autowired
	private DemandService demandService;

	/** The bookings repository. */
	@Autowired
	private BookingsRepository bookingsRepository;

	/** The osujm new location repository. */
	@Autowired
	private OsujmNewLocationRepository osujmNewLocationRepository;

	/** The common repository. */
	@Autowired
	CommonRepository commonRepository;

	/** The bookings service impl. */
	@Autowired
	private BookingsServiceImpl bookingsServiceImpl;

	/** The user service. */
	@Autowired
	private UserService userService;

	/** The osbm approver repository. */
	@Autowired
	private OsbmApproverRepository osbmApproverRepository;

	@Autowired
	private RoomsService roomsService;

	@Autowired
	private RoomsRepository roomsRepository;

	@Autowired
	private BookingsProducer bookingsProducer;

	@Autowired
	private BookingLockRepository bookingLockRepository;
	/**
	 * Enrich bookings create request.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void enrichBookingsCreateRequest(BookingsRequest bookingsRequest) {
		RequestInfo requestInfo = bookingsRequest.getRequestInfo();
		/*
		 * AuditDetails auditDetails = bookingsUtil
		 * .getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		 */

		setIdgenIds(bookingsRequest);
		// setStatusForCreate(tradeLicenseRequest);
		// boundaryService.getAreaType(tradeLicenseRequest,config.getHierarchyTypeCode());
	}

	/**
	 * Sets the idgen ids.
	 *
	 * @param bookingsRequest the new idgen ids
	 */
	private void setIdgenIds(BookingsRequest bookingsRequest) {
		RequestInfo requestInfo = bookingsRequest.getRequestInfo();
		String tenantId = bookingsRequest.getBookingsModel().getTenantId();

		BookingsModel bookingsModel = bookingsRequest.getBookingsModel();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenName(),
				config.getApplicationNumberIdgenFormat());
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		/*
		 * if (applicationNumbers.size() != bookingsRequest.getBookingsModel().size()) {
		 * errorMap.put("IDGEN ERROR ",
		 * "The number of LicenseNumber returned by idgen is not equal to number of TradeLicenses"
		 * ); }
		 */

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		bookingsModel.setBkApplicationNumber(itr.next());
	}

	/**
	 * Enrich new location create request.
	 *
	 * @param newLocationRequest the new location request
	 */
	public void enrichNewLocationCreateRequest(NewLocationRequest newLocationRequest) {
		RequestInfo requestInfo = newLocationRequest.getRequestInfo();
		/*
		 * AuditDetails auditDetails = bookingsUtil
		 * .getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		 */

		setIdgenIdsForNewLocation(newLocationRequest);
		// setStatusForCreate(tradeLicenseRequest);
		// boundaryService.getAreaType(tradeLicenseRequest,config.getHierarchyTypeCode());
	}

	/**
	 * Sets the idgen ids for new location.
	 *
	 * @param newLocationRequest the new idgen ids for new location
	 */
	private void setIdgenIdsForNewLocation(NewLocationRequest newLocationRequest) {
		RequestInfo requestInfo = newLocationRequest.getRequestInfo();
		String tenantId = newLocationRequest.getNewLocationModel().getTenantId();

		OsujmNewLocationModel newLocationModel = newLocationRequest.getNewLocationModel();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenName(),
				config.getApplicationNumberIdgenFormat());
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		/*
		 * if (applicationNumbers.size() != bookingsRequest.getBookingsModel().size()) {
		 * errorMap.put("IDGEN ERROR ",
		 * "The number of LicenseNumber returned by idgen is not equal to number of TradeLicenses"
		 * ); }
		 */

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		newLocationModel.setApplicationNumber(itr.next());
	}

	/**
	 * Gets the id list.
	 *
	 * @param requestInfo the request info
	 * @param tenantId    the tenant id
	 * @param idKey       the id key
	 * @param idformat    the idformat
	 * @return the id list
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat).getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * Enrich bookings details.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void enrichBookingsDetails(BookingsRequest bookingsRequest) {
		try {
			if (!BookingsConstants.PACC_RE_INITIATED_ACTION.equals(bookingsRequest.getBookingsModel().getBkAction())) {
				if (!BookingsConstants.EMPLOYEE.equals(bookingsRequest.getRequestInfo().getUserInfo().getType()))
					bookingsRequest.getBookingsModel()
							.setUuid(bookingsRequest.getRequestInfo().getUserInfo().getUuid());
				java.sql.Date date = bookingsUtils.getCurrentSqlDate();
				bookingsRequest.getBookingsModel().setBkDateCreated(date);
				DateFormat formatter = bookingsUtils.getSimpleDateFormat();
				bookingsRequest.getBookingsModel().setCreatedDate(formatter.format(new java.util.Date()));
				bookingsRequest.getBookingsModel().setLastModifiedDate(formatter.format(new java.util.Date()));
			} else {
				BookingsModel bookingsModel = bookingsRepository
						.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
				bookingsRequest.getBookingsModel().setBkDateCreated(bookingsModel.getBkDateCreated());
			}
		} catch (Exception e) {
			throw new CustomException("INVALID_BOOKING_REQUEST", e.getMessage());
		}
	}

	/**
	 * Generate demand.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void generateDemand(BookingsRequest bookingsRequest) {

		if (!BookingsConstants.BUSINESS_SERVICE_BWT.equals(bookingsRequest.getBookingsModel().getBusinessService())) {
			if (!bookingsService.isBookingExists(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
				demandService.createDemand(bookingsRequest);
			} else
				demandService.updateDemand(bookingsRequest);
		} else {
			if (!bookingsService.isBookingExists(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
				demandService.createDemand(bookingsRequest);
			}
		}

	}

	/**
	 * Enrich osbm details.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	public BookingsModel enrichOsbmDetails(BookingsRequest bookingsRequest) {
		BookingsModel bookingsModel = null;
		try {
			bookingsModel = bookingsRepository
					.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			if (null != bookingsModel) {

				bookingsModel.setBkApplicationStatus(bookingsRequest.getBookingsModel().getBkApplicationStatus());
				bookingsModel.setBkAction(bookingsRequest.getBookingsModel().getBkAction());
				bookingsModel.setBkRemarks(bookingsRequest.getBookingsModel().getBkRemarks());
				if (BookingsConstants.PAY.equals(bookingsRequest.getBookingsModel().getBkAction())) {
					LocalDate bkFromDate = LocalDate.now();
					Date fromDate = java.sql.Date.valueOf(bkFromDate);
					LocalDate bkToDate = LocalDate.now().plusMonths(Integer.valueOf(bookingsModel.getBkDuration()));
					Date toDate = java.sql.Date.valueOf(bkToDate);
					bookingsModel.setBkFromDate(fromDate);
					bookingsModel.setBkToDate(toDate);
					if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus()))
						bookingsModel.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
				}

			}
		} catch (Exception e) {
			throw new CustomException("OSBM UPDATE ERROR", "ERROR WHILE UPDATING OSBM DETAILS ");
		}
		return bookingsModel;
	}

	/**
	 * Enrich bwt details.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	public BookingsModel enrichBwtDetails(BookingsRequest bookingsRequest) {
		BookingsModel bookingsModel = null;
		try {
			bookingsModel = bookingsRepository
					.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			bookingsModel.setBkApplicationStatus(bookingsRequest.getBookingsModel().getBkApplicationStatus());
			bookingsModel.setBkAction(bookingsRequest.getBookingsModel().getBkAction());
			bookingsModel.setBkRemarks(bookingsRequest.getBookingsModel().getBkRemarks());
			bookingsModel.setBkContactNo(bookingsRequest.getBookingsModel().getBkContactNo());
			bookingsModel.setBkDriverName(bookingsRequest.getBookingsModel().getBkDriverName());
			bookingsModel.setBkApproverName(bookingsRequest.getBookingsModel().getBkApproverName());
			if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				bookingsModel.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
			}
		} catch (Exception e) {
			throw new CustomException("WATER TANKER UPDATE ERROR", "ERROR WHILE UPDATING BWT DETAILS ");
		}
		return bookingsModel;
	}

	/**
	 * Enrich new location details.
	 *
	 * @param newLocationRequest the new location request
	 */
	public void enrichNewLocationDetails(NewLocationRequest newLocationRequest, boolean flag) {
		DateFormat formatter = bookingsUtils.getSimpleDateFormat();
		if (!flag) {

			newLocationRequest.getNewLocationModel().setCreatedDate(formatter.format(new java.util.Date()));
			newLocationRequest.getNewLocationModel().setLastModifiedDate(formatter.format(new java.util.Date()));
		} else {
			newLocationRequest.getNewLocationModel().setLastModifiedDate(formatter.format(new java.util.Date()));
		}
		newLocationRequest.getNewLocationModel().setUuid(newLocationRequest.getRequestInfo().getUserInfo().getUuid());
		java.sql.Date date = bookingsUtils.getCurrentSqlDate();
		newLocationRequest.getNewLocationModel().setDateCreated(date);
	}

	/**
	 * Enrich nlujm details.
	 *
	 * @param newLocationRequest the new location request
	 * @return the osujm new location model
	 */
	public OsujmNewLocationModel enrichNlujmDetails(NewLocationRequest newLocationRequest) {
		OsujmNewLocationModel osujmNewLocationModel = null;
		try {
			osujmNewLocationModel = osujmNewLocationRepository
					.findByApplicationNumber(newLocationRequest.getNewLocationModel().getApplicationNumber());
			osujmNewLocationModel.setApplicationStatus(newLocationRequest.getNewLocationModel().getApplicationStatus());
			osujmNewLocationModel.setAction(newLocationRequest.getNewLocationModel().getAction());
			osujmNewLocationModel.setRemarks(newLocationRequest.getNewLocationModel().getRemarks());
		} catch (Exception e) {
			throw new CustomException("NLUJM UPDATE ERROR", "ERROR WHILE UPDATING NLUJM DETAILS ");
		}
		return osujmNewLocationModel;
	}

	/**
	 * Extract days between two dates.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the big decimal
	 */
	public BigDecimal extractDaysBetweenTwoDates(BookingsRequest bookingsRequest) {
		try {
			LocalDate dateBefore = null;
			LocalDate dateAfter = null;
			if (BookingsConstants.PACC_RE_INITIATED_ACTION.equals(bookingsRequest.getBookingsModel().getBkAction())) {
				dateBefore = LocalDate.parse(bookingsRequest.getBookingsModel().getBkStartingDate() + "");
				dateAfter = LocalDate.parse(bookingsRequest.getBookingsModel().getBkEndingDate() + "");
			} else {
				dateBefore = LocalDate.parse(bookingsRequest.getBookingsModel().getBkFromDate() + "");
				dateAfter = LocalDate.parse(bookingsRequest.getBookingsModel().getBkToDate() + "");
			}
			long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
			long totalDays = noOfDaysBetween + 1;
			BigDecimal finalAmount = BigDecimal.valueOf(totalDays);
			return finalAmount;
		} catch (Exception e) {
			throw new CustomException("INVALID_REQUEST", e.getLocalizedMessage());
		}
	}

	/**
	 * Enrich osujm details.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	public BookingsModel enrichOsujmDetails(BookingsRequest bookingsRequest) {
		BookingsModel bookingsModel = null;
		try {
			bookingsModel = bookingsRepository
					.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			bookingsModel.setBkApplicationStatus(bookingsRequest.getBookingsModel().getBkApplicationStatus());
			bookingsModel.setBkAction(bookingsRequest.getBookingsModel().getBkAction());
			bookingsModel.setBkRemarks(bookingsRequest.getBookingsModel().getBkRemarks());
			if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				bookingsModel.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
			}

		} catch (Exception e) {
			throw new CustomException("OSUJM UPDATE ERROR", "ERROR WHILE UPDATING OSUJM DETAILS ");
		}
		return bookingsModel;
	}

	/**
	 * Enrich booked dates.
	 *
	 * @param bookingsModel the bookings model
	 * @return the list
	 */
	public Set<LocalDate> enrichBookedDates(Set<BookingsModel> bookingsModel) {
		List<LocalDate> listOfDates = new ArrayList<>();

		for (BookingsModel bookingsModel1 : bookingsModel) {
			if (!BookingsConstants.PACC_ACTION_APPROVE_CLERK_DEO.equals(bookingsModel1.getBkAction())) {
				LocalDate startDate = LocalDate.parse(bookingsModel1.getBkFromDate() + "");
				LocalDate endDate = LocalDate.parse(bookingsModel1.getBkToDate() + "");
				long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

				List<LocalDate> listOfDates2 = LongStream.range(0, numOfDays).mapToObj(startDate::plusDays)
						.collect(Collectors.toList());
				listOfDates.addAll(listOfDates2);
				listOfDates.add(endDate);
			}
		}

		return new HashSet<>(listOfDates);
	}

	/**
	 * Extract all dates between two dates.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the list
	 */
	public Set<LocalDate> extractAllDatesBetweenTwoDates(BookingsRequest bookingsRequest) {
		LocalDate startDate = LocalDate.parse(bookingsRequest.getBookingsModel().getBkFromDate() + "");
		LocalDate endDate = LocalDate.parse(bookingsRequest.getBookingsModel().getBkToDate() + "");
		long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

		List<LocalDate> listOfDates2 = LongStream.range(0, numOfDays).mapToObj(startDate::plusDays)
				.collect(Collectors.toList());
		listOfDates2.add(endDate);
		return new HashSet<>(listOfDates2);

	}

	/**
	 * Enrich pacc details.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	public BookingsModel enrichPaccDetails(BookingsRequest bookingsRequest) {
		BookingsModel bookingsModel = null;
		try {
			bookingsModel = bookingsRepository
					.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			bookingsModel.setBkApplicationStatus(bookingsRequest.getBookingsModel().getBkApplicationStatus());
			bookingsModel.setBkStatusUpdateRequest(bookingsRequest.getBookingsModel().getBkStatusUpdateRequest());
			bookingsModel.setBkLocationPictures(bookingsRequest.getBookingsModel().getBkLocationPictures());
			bookingsModel.setBkAction(bookingsRequest.getBookingsModel().getBkAction());
			bookingsModel.setBkRemarks(bookingsRequest.getBookingsModel().getBkRemarks());
			bookingsModel.setTimeslots(bookingsRequest.getBookingsModel().getTimeslots());
			bookingsModel.setRefundableSecurityMoney(bookingsRequest.getBookingsModel().getRefundableSecurityMoney());
			bookingsRequest.getBookingsModel().setBkDateCreated(bookingsModel.getBkDateCreated());
			if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				bookingsModel.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
			}
		} catch (Exception e) {
			throw new CustomException("PACC UPDATE ERROR", "ERROR WHILE UPDATING PACC DETAILS ");
		}
		return bookingsModel;
	}

	/**
	 * Enrich park community amount.
	 *
	 * @param parkCommunityHallFee the park community hall fee
	 * @return the park community fee master response
	 */
	public ParkCommunityFeeMasterResponse enrichParkCommunityAmount(
			ParkCommunityHallV1MasterModel parkCommunityHallFee) {
		ParkCommunityFeeMasterResponse parkCommunityFeeMasterResponse = new ParkCommunityFeeMasterResponse();
		try {
			BigDecimal amount = BigDecimal.valueOf(Long.valueOf(parkCommunityHallFee.getRent())
					+ Long.valueOf(parkCommunityHallFee.getCleaningCharges()));
			BigDecimal surchargeAmount = amount.multiply(
					BigDecimal.valueOf(Long.valueOf(parkCommunityHallFee.getSurcharge())).divide(new BigDecimal(100)));
			BigDecimal finalAmount = amount.add(surchargeAmount);

			BigDecimal finalBill = BookingsUtils.removeRoundOff(finalAmount);

			BigDecimal cgstAmount = amount.multiply(
					BigDecimal.valueOf(Long.valueOf(parkCommunityHallFee.getCgstRate())).divide(new BigDecimal(100)));
			BigDecimal ugstAmount = amount.multiply(
					BigDecimal.valueOf(Long.valueOf(parkCommunityHallFee.getUtgstRate())).divide(new BigDecimal(100)));
			parkCommunityFeeMasterResponse.setTotalAmount(finalBill);
			parkCommunityFeeMasterResponse.setAmount(amount);
			parkCommunityFeeMasterResponse.setSurchargeAmount(surchargeAmount);
			parkCommunityFeeMasterResponse.setCgstAmount(cgstAmount);
			parkCommunityFeeMasterResponse.setUgstAmount(ugstAmount);
			return parkCommunityFeeMasterResponse;
		} catch (Exception e) {
			throw new CustomException("FEE_MASTER_ERROR", "ERROR WHILE ENRICHING PARK AND COMMUNITY FEE RESPONSE");
		}
	}

	/**
	 * Enrich bookings assignee.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void enrichBookingsAssignee(BookingsRequest bookingsRequest) {
		String businessService = bookingsRequest.getBookingsModel().getBusinessService();
		SearchCriteriaFieldsDTO searchCriteriaFieldsDTO = new SearchCriteriaFieldsDTO();
		searchCriteriaFieldsDTO.setApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
		searchCriteriaFieldsDTO.setAction(BookingsConstants.ACTION_INITIATE);
		if (!BookingsConstants.BUSINESS_SERVICE_BWT.equals(businessService)
				&& !BookingsConstants.BUSINESS_SERVICE_GFCP.equals(businessService)
				&& !BookingsConstants.BUSINESS_SERVICE_PACC.equals(businessService))
			;
		searchCriteriaFieldsDTO.setSector(bookingsRequest.getBookingsModel().getBkSector());
		searchCriteriaFieldsDTO.setRequestInfo(bookingsRequest.getRequestInfo());
		List<UserDetails> userdetailsList = bookingsService.getAssignee(searchCriteriaFieldsDTO);
		if (!BookingsFieldsValidator.isNullOrEmpty(userdetailsList))
			bookingsRequest.getBookingsModel().setAssignee(userdetailsList.get(0).getUuid());
	}

	/**
	 * Enrich assignee.
	 *
	 * @param bookingsRequest the bookings request
	 * @param bookingsModel   the bookings model
	 */
	public void enrichAssignee(BookingsRequest bookingsRequest, BookingsModel bookingsModel) {
		List<String> roleCodes = new ArrayList<>();
		UserDetailResponse userDetailResponse = new UserDetailResponse();
		String applicationNumber = bookingsModel.getBkApplicationNumber();
		String action = bookingsModel.getBkAction();
		String approverName = "";
		OsbmApproverModel osbmApproverModel = new OsbmApproverModel();
		List<Role> rolesList = new ArrayList<>();
		if (!BookingsFieldsValidator.isNullOrEmpty(applicationNumber)
				&& !BookingsFieldsValidator.isNullOrEmpty(action)) {
			List<String> nextState = commonRepository.findNextState(applicationNumber, action);
			if (!BookingsFieldsValidator.isNullOrEmpty(nextState)) {
				approverName = commonRepository.findApproverName(nextState.get(0));
			}
			String[] approverArray = approverName.split(",");
			if (!BookingsFieldsValidator.isNullOrEmpty(approverArray)) {
				for (String approver : approverArray) {
					if (!BookingsConstants.CITIZEN.equals(approver)) {
						roleCodes.add(approver);
					}
				}
				if (!BookingsFieldsValidator.isNullOrEmpty(roleCodes)) {
					StringBuilder url = bookingsServiceImpl.prepareUrlForUserList();
					userDetailResponse = userService.getUserSearchDetails(roleCodes, url,
							bookingsRequest.getRequestInfo());
				}
				if (!BookingsFieldsValidator.isNullOrEmpty(userDetailResponse)) {
					List<OwnerInfo> userList = userDetailResponse.getUser();
					if (!BookingsFieldsValidator.isNullOrEmpty(userList)) {
						for (int i = 0; i < userList.size(); i++) {
							rolesList = userList.get(i).getRoles();
							for (Role role : rolesList) {
								osbmApproverModel = osbmApproverRepository.findBySectorAndUuidAndRoleCodeAndUserId(
										bookingsModel.getBkSector(), userList.get(i).getUuid(), role.getCode(),
										userList.get(i).getId());
							}
							if (!BookingsFieldsValidator.isNullOrEmpty(osbmApproverModel)) {
								bookingsRequest.setUser(userList.get(i));
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Enrich for kafka.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings request kafka
	 */
	public BookingsRequestKafka enrichForKafka(BookingsRequest bookingsRequest) {
		List<BookingsModel> bModel = new ArrayList<>();
		bModel.add(bookingsRequest.getBookingsModel());
		BookingsRequestKafka kafkaBookingRequest = BookingsRequestKafka.builder().bookingsModel(bModel)
				.requestInfo(bookingsRequest.getRequestInfo()).build();
		return kafkaBookingRequest;
	}

	/**
	 * Enrich kafka for new location.
	 *
	 * @param newLocationRequest the new location request
	 * @return the new location kafka request
	 */
	public NewLocationKafkaRequest enrichKafkaForNewLocation(NewLocationRequest newLocationRequest) {
		List<OsujmNewLocationModel> sujmNewLocationModelList = new ArrayList<>();
		sujmNewLocationModelList.add(newLocationRequest.getNewLocationModel());
		NewLocationKafkaRequest kafkaNewLocationRequest = NewLocationKafkaRequest.builder()
				.newLocationModel(sujmNewLocationModelList).requestInfo(newLocationRequest.getRequestInfo()).build();
		return kafkaNewLocationRequest;
	}

	/**
	 * Enrich park community create request.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void enrichParkCommunityCreateRequest(BookingsRequest bookingsRequest) {
		// TODO Auto-generated method stub

		RequestInfo requestInfo = bookingsRequest.getRequestInfo();
		String tenantId = bookingsRequest.getBookingsModel().getTenantId();

		BookingsModel bookingsModel = bookingsRequest.getBookingsModel();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenName(),
				config.getApplicationNumberIdgenFormat());
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		bookingsModel.setBkApplicationNumber(itr.next());
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getTimeslots())) {
			bookingsRequest.getBookingsModel().getTimeslots().forEach(slots -> {
				slots.setId(UUID.randomUUID().toString());
				slots.setApplicationNumber(bookingsModel.getBkApplicationNumber());
			});
		}
	}

	/**
	 * Enrich pacc payment details.
	 *
	 * @param bookingsRequest the bookings request
	 */
	public void enrichPaccPaymentDetails(BookingsRequest bookingsRequest) {
		String businessService = bookingsRequest.getBookingsModel().getBusinessService();

		if (BookingsConstants.PACC_ACTION_CANCEL.equals(bookingsRequest.getBookingsModel().getBkAction())) {
			List<RoomsModel> roomsList = roomsRepository
					.findByCommunityApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			roomsList.forEach(room -> {
				room.setRoomStatus(BookingsConstants.PACC_ACTION_CANCEL);
			});
			BookingsRequestKafka kafkaBookingRequest = enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getUpdateRoomStatus(), kafkaBookingRequest);
		}

		BookingsModel bookingsModel = bookingsRepository
				.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
		bookingsRequest.getBookingsModel().setBkDateCreated(bookingsModel.getBkDateCreated());
		if (BookingsConstants.APPLY.equals(bookingsRequest.getBookingsModel().getBkAction())
				&& BookingsConstants.BUSINESS_SERVICE_PACC.equals(businessService)
				&& BookingsConstants.PACC_ACTION_MODIFY.equals(bookingsRequest.getBookingsModel().getBkAction())) {
			config.setParkAndCommunityLock(true);
			if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				bookingsRequest.getBookingsModel()
						.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
			}
		}
		if (BookingsConstants.PACC_ACTION_OFFLINE_MODIFY.equals(bookingsRequest.getBookingsModel().getBkAction())
				&& BookingsConstants.BUSINESS_SERVICE_PACC.equals(businessService)
				&& BookingsConstants.EMPLOYEE.equals(bookingsRequest.getRequestInfo().getUserInfo().getType())) {
			config.setParkAndCommunityLock(true);
			if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				bookingsRequest.getBookingsModel()
						.setBkPaymentStatus(bookingsRequest.getBookingsModel().getBkPaymentStatus());
			}
		}
	}

	/**
	 * Enrich tax head estimate for PACC.
	 *
	 * @param bookingsRequest              the bookings request
	 * @param finalAmount                  the final amount
	 * @param taxHeadCode1                 the tax head code 1
	 * @param taxHeadCode2                 the tax head code 2
	 * @param taxHeadMasterFieldList       the tax head master field list
	 * @param parkCommunityHallV1FeeMaster the park community hall V 1 fee master
	 * @return the list
	 */
	private List<TaxHeadEstimate> enrichTaxHeadEstimateForPACC(BookingsRequest bookingsRequest, BigDecimal rent,
			BigDecimal cleaningCharges, String taxHeadCode1, String taxHeadCode2, String taxHeadCode3,
			String taxHeadCode4, String taxHeadCode5, List<TaxHeadMasterFields> taxHeadMasterFieldList,
			ParkCommunityHallV1MasterModel parkCommunityHallV1FeeMaster) {

		List<TaxHeadEstimate> taxHeadEstimate1 = new ArrayList<>();
		for (TaxHeadMasterFields taxHeadEstimate : taxHeadMasterFieldList) {
			if (taxHeadEstimate.getCode().equals(taxHeadCode1)) {
				taxHeadEstimate1
						.add(new TaxHeadEstimate(taxHeadEstimate.getCode(), rent, taxHeadEstimate.getCategory()));
				continue;
			}
			if (BookingsConstants.PAYMENT_SUCCESS_STATUS
					.equals(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				if (taxHeadEstimate.getCode().equals(taxHeadCode2)) {
					rent = rent.add(
							BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount())));
					BigDecimal rentPlusLocChangePlusCleansingAmnt = rent
							.add(BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getCleaningCharges())));
					BigDecimal paccTax = BookingsUtils.roundOffToNearest(rentPlusLocChangePlusCleansingAmnt
							.multiply((BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getSurcharge()))
									.divide(new BigDecimal(100)))));
					taxHeadEstimate1.add(
							new TaxHeadEstimate(taxHeadEstimate.getCode(), paccTax, taxHeadEstimate.getCategory()));
					continue;
				}
			} else {
				if (taxHeadEstimate.getCode().equals(taxHeadCode2)) {
					BigDecimal rentPlusCleansingAmount = rent.add(cleaningCharges);
					BigDecimal paccTax = BookingsUtils.roundOffToNearest(rentPlusCleansingAmount
							.multiply((BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getSurcharge()))
									.divide(new BigDecimal(100)))));
					taxHeadEstimate1.add(
							new TaxHeadEstimate(taxHeadEstimate.getCode(), paccTax, taxHeadEstimate.getCategory()));
					continue;
				}
			}

			if (taxHeadEstimate.getCode().equals(taxHeadCode3)) {
				taxHeadEstimate1.add(
						new TaxHeadEstimate(taxHeadEstimate.getCode(), cleaningCharges, taxHeadEstimate.getCategory()));
				continue;
			}
			/*
			 * if (taxHeadEstimate.getCode().equals(BookingsConstants.
			 * PACC_TAXHEAD_CODE_LUXURY_TAX)) { taxHeadEstimate1.add(new
			 * TaxHeadEstimate(taxHeadEstimate.getCode(),
			 * BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getLuxuryTax()))
			 * , taxHeadEstimate.getCategory())); continue; }
			 */
			if (taxHeadEstimate.getCode().equals(taxHeadCode4)) {
				taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
						BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getRefundabelSecurity())),
						taxHeadEstimate.getCategory()));
				continue;
			}

			if (bookingsRequest.getRequestInfo().getUserInfo().getType().equals(BookingsConstants.EMPLOYEE)) {
				if (taxHeadEstimate.getCode().equals(taxHeadCode5)) {
					taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
							BookingsCalculatorConstants.FACILATION_CHARGE, taxHeadEstimate.getCategory()));
				}
				continue;
			}
			if (BookingsConstants.PAYMENT_SUCCESS_STATUS
					.equals(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
				if (taxHeadEstimate.getCode().equals(BookingsConstants.PACC_TAXHEAD_CODE_3)) {
					taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
							BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount())),
							taxHeadEstimate.getCategory()));
				}
				if (taxHeadEstimate.getCode().equals(BookingsConstants.PACC_TAXHEAD_CODE_COMM_LOC_CHANGE)) {
					taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
							BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount())),
							taxHeadEstimate.getCategory()));
				}
				continue;
			}
		}
		return taxHeadEstimate1;
	}

	/**
	 * Enrich pacc amount for booking change.
	 *
	 * @param bookingsRequest              the bookings request
	 * @param finalAmount                  the final amount
	 * @param taxHeadCode1                 the tax head code 1
	 * @param taxHeadCode2                 the tax head code 2
	 * @param taxHeadMasterFieldList       the tax head master field list
	 * @param parkCommunityHallV1FeeMaster the park community hall V 1 fee master
	 * @return the list
	 */
	public List<TaxHeadEstimate> enrichPaccAmountForBookingChange(BookingsRequest bookingsRequest, BigDecimal rent,
			BigDecimal cleaningCharges, String taxHeadCode1, String taxHeadCode2, String taxHeadCode3,
			String taxHeadCode4, String taxHeadCode5, List<TaxHeadMasterFields> taxHeadMasterFieldList,
			ParkCommunityHallV1MasterModel parkCommunityHallV1FeeMaster) {

		BigDecimal demandDetailsAmount = BigDecimal.ZERO;
		List<TaxHeadEstimate> taxHeadEstimate1 = new ArrayList<>();

		if (BookingsConstants.PAYMENT_SUCCESS_STATUS.equals(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {
			List<DemandDetail> demandDetails = null;
			if (bookingsService.isBookingExists(bookingsRequest.getBookingsModel().getBkApplicationNumber())) {
				List<Demand> searchResult = demandService.searchDemand(bookingsRequest.getBookingsModel().getTenantId(),
						Collections.singleton(bookingsRequest.getBookingsModel().getBkApplicationNumber()),
						bookingsRequest.getRequestInfo(),
						bookingsRequest.getBookingsModel().getFinanceBusinessService());

				Demand demand = searchResult.get(0);
				demandDetails = demand.getDemandDetails();
			}
			for (DemandDetail demandDetail : demandDetails) {
				if (BookingsCalculatorConstants.OSBM_TAXHEAD_CODE_1.equals(demandDetail.getTaxHeadMasterCode())) {
					demandDetailsAmount = demandDetail.getTaxAmount();
					continue;
				}
				if (BookingsCalculatorConstants.PACC_TAX_CODE_1.equals(demandDetail.getTaxHeadMasterCode())) {
					demandDetailsAmount = demandDetail.getTaxAmount();
					continue;
				}
			}
			if (rent.compareTo(demandDetailsAmount) < 1 || rent.compareTo(demandDetailsAmount) == 0) {
				// config.setDemandFlag(false);
				BigDecimal rentAfterVenueOrLocationChange = BigDecimal.ZERO;
				BigDecimal cleansingChargeAfterVenueOrLocationChange = BigDecimal.ZERO;
				BigDecimal totalPACCTaxAmount = BigDecimal.ZERO;
				boolean paccTaxFlag = true;
				BigDecimal locationChangeTax = BigDecimal
						.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount()))
						.multiply((BigDecimal.valueOf(Long.valueOf(parkCommunityHallV1FeeMaster.getSurcharge()))
								.divide(new BigDecimal(100))));
				for (DemandDetail demandDetail : demandDetails) {
					if (demandDetail.getTaxHeadMasterCode().equals(taxHeadCode1)) {
						rentAfterVenueOrLocationChange = rentAfterVenueOrLocationChange
								.add(demandDetail.getTaxAmount());
					}
					if (demandDetail.getTaxHeadMasterCode().equals(taxHeadCode2)) {
						totalPACCTaxAmount = totalPACCTaxAmount.add(demandDetail.getTaxAmount());
					}
					if (demandDetail.getTaxHeadMasterCode().equals(taxHeadCode2)
							&& demandDetail.getTaxAmount().compareTo(locationChangeTax) == 0) {
						paccTaxFlag = false;
					}
					if (demandDetail.getTaxHeadMasterCode().equals(taxHeadCode3)) {
						cleansingChargeAfterVenueOrLocationChange = cleansingChargeAfterVenueOrLocationChange
								.add(demandDetail.getTaxAmount());
					}
				}
				if (paccTaxFlag)
					totalPACCTaxAmount = totalPACCTaxAmount.add(locationChangeTax);
				for (TaxHeadMasterFields taxHeadEstimate : taxHeadMasterFieldList) {
					if (BookingsConstants.PAYMENT_SUCCESS_STATUS
							.equals(bookingsRequest.getBookingsModel().getBkPaymentStatus())) {

						if (taxHeadEstimate.getCode().equals(taxHeadCode1)) {
							taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
									rentAfterVenueOrLocationChange, taxHeadEstimate.getCategory()));
							continue;
						}

						if (taxHeadEstimate.getCode().equals(BookingsConstants.PACC_TAXHEAD_CODE_3)) {
							taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
									BigDecimal.valueOf(
											Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount())),
									taxHeadEstimate.getCategory()));
							continue;
						}
						if (taxHeadEstimate.getCode().equals(BookingsConstants.PACC_TAXHEAD_CODE_COMM_LOC_CHANGE)) {
							taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
									BigDecimal.valueOf(
											Long.valueOf(parkCommunityHallV1FeeMaster.getLocationChangeAmount())),
									taxHeadEstimate.getCategory()));
							continue;
						}

						if (taxHeadEstimate.getCode().equals(taxHeadCode2)) {
							taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(), totalPACCTaxAmount,
									taxHeadEstimate.getCategory()));
							continue;
						}
						if (taxHeadEstimate.getCode().equals(taxHeadCode3)) {
							taxHeadEstimate1.add(new TaxHeadEstimate(taxHeadEstimate.getCode(),
									cleansingChargeAfterVenueOrLocationChange, taxHeadEstimate.getCategory()));
							continue;
						}

					}
				}

				/*
				 * taxHeadEstimate1 = enrichTaxHeadEstimateForPACC(bookingsRequest,
				 * BigDecimal.ZERO, taxHeadCode1, taxHeadCode2, taxHeadMasterFieldList,
				 * parkCommunityHallV1FeeMaster);
				 */
			}

			else {
				taxHeadEstimate1 = enrichTaxHeadEstimateForPACC(bookingsRequest, rent, cleaningCharges, taxHeadCode1,
						taxHeadCode2, taxHeadCode3, taxHeadCode4, taxHeadCode5, taxHeadMasterFieldList,
						parkCommunityHallV1FeeMaster);
			}
		} else {

			taxHeadEstimate1 = enrichTaxHeadEstimateForPACC(bookingsRequest, rent, cleaningCharges, taxHeadCode1,
					taxHeadCode2, taxHeadCode3, taxHeadCode4, taxHeadCode5, taxHeadMasterFieldList,
					parkCommunityHallV1FeeMaster);

		}
		return taxHeadEstimate1;
	}

	/**
	 * Enrich re initiated request. setting BkStartingDate and BkEndingDate from
	 * requestBody and setting BkFromDate and BkToDate from db in case of action
	 * RE_INITIATED
	 * 
	 * @param bookingsRequest the bookings request
	 * @param flag
	 */
	public void enrichReInitiatedRequest(BookingsRequest bookingsRequest, boolean flag) {
		DateFormat formatter = bookingsUtils.getSimpleDateFormat();
		if (!flag) {
			bookingsRequest.getBookingsModel().setCreatedDate(formatter.format(new java.util.Date()));
			bookingsRequest.getBookingsModel().setLastModifiedDate(formatter.format(new java.util.Date()));
		} else {
			bookingsRequest.getBookingsModel().setLastModifiedDate(formatter.format(new java.util.Date()));
		}
		if (BookingsConstants.PACC_RE_INITIATED_ACTION.equals(bookingsRequest.getBookingsModel().getBkAction()) && flag
				&& BookingsConstants.BUSINESS_SERVICE_PACC
						.equals(bookingsRequest.getBookingsModel().getBusinessService())) {
			BookingsModel findByBkApplicationNumber = bookingsRepository
					.findByBkApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			bookingsRequest.getBookingsModel().setBkStartingDate(bookingsRequest.getBookingsModel().getBkFromDate());
			bookingsRequest.getBookingsModel().setBkEndingDate(bookingsRequest.getBookingsModel().getBkToDate());
			bookingsRequest.getBookingsModel().setBkFromDate(findByBkApplicationNumber.getBkFromDate());
			bookingsRequest.getBookingsModel().setBkToDate(findByBkApplicationNumber.getBkToDate());
			bookingsRequest.getBookingsModel().setBkBookingVenue(findByBkApplicationNumber.getBkBookingVenue());
			bookingsRequest.getBookingsModel().setBkType(bookingsRequest.getBookingsModel().getBkBookingVenue());

			List<RoomsModel> roomsList = roomsRepository
					.findByCommunityApplicationNumber(bookingsRequest.getBookingsModel().getBkApplicationNumber());
			roomsList.forEach(room -> {
				room.setRoomStatus(BookingsConstants.PACC_ACTION_CANCEL);
			});
			BookingsRequestKafka kafkaBookingRequest = enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getUpdateRoomStatus(), kafkaBookingRequest);
		}
	}

	public void enrichRoomForCommunityBookingRequest(BookingsRequest bookingsRequest) {
		// TODO Auto-generated method stub

		RequestInfo requestInfo = bookingsRequest.getRequestInfo();
		String tenantId = bookingsRequest.getBookingsModel().getTenantId();

		BookingsModel bookingsModel = bookingsRequest.getBookingsModel();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenName(),
				config.getApplicationNumberIdgenRoomFormat());
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		bookingsModel.getRoomsModel().get(0).setRoomApplicationNumber(itr.next());
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getRoomsModel())) {

			bookingsRequest.getBookingsModel().getRoomsModel().forEach(roomModel -> {
				roomModel.setRoomApplicationNumber(bookingsModel.getRoomsModel().get(0).getRoomApplicationNumber());
				roomModel.setCommunityApplicationNumber(bookingsModel.getBkApplicationNumber());
				roomModel.setId(UUID.randomUUID().toString());
			});

			/*
			 * bookingsRequest.getBookingsModel().getRoomsModel().get(0)
			 * .setRoomApplicationNumber(bookingsModel.getRoomsModel().get(0).
			 * getRoomApplicationNumber());
			 * bookingsRequest.getBookingsModel().getRoomsModel().get(0)
			 * .setCommunityApplicationNumber(bookingsModel.getBkApplicationNumber());
			 */
		}
	}

	public void generateDemandForRoom(BookingsRequest bookingsRequest) {

		if (!roomsService.isRoomBookingExists(
				bookingsRequest.getBookingsModel().getRoomsModel().get(0).getRoomApplicationNumber())) {
			demandService.createDemandForRoom(bookingsRequest);
		} else
			demandService.updateDemandForRoom(bookingsRequest);

	}

	public void enrichRoomDetails(BookingsRequest bookingsRequest, boolean flag) {
		LocalDate date = LocalDate.now();
		Date date1 = Date.valueOf(date);

		for (RoomsModel roomModel : bookingsRequest.getBookingsModel().getRoomsModel()) {
			if (!flag) {
				roomModel.setCreatedDate(date1);
				DateFormat formatter = bookingsUtils.getSimpleDateFormat();
				roomModel.setRoomCreatedDate(formatter.format(new java.util.Date()));
			}
			roomModel.setLastModifiedDate(date1);
		}
		List<RoomsModel> roomsModelList = roomsRepository.findByRoomApplicationNumber(
				bookingsRequest.getBookingsModel().getRoomsModel().get(0).getRoomApplicationNumber());
		List<RoomsModel> bkRoomList = bookingsRequest.getBookingsModel().getRoomsModel();
		if (!BookingsFieldsValidator.isNullOrEmpty(roomsModelList)) {
			/*
			 * bookingsRequest.getBookingsModel().getRoomsModel().get(0)
			 * .setRoomBusinessService(bookingsRequest.getBookingsModel().
			 * getRoomBusinessService());
			 */
			for (RoomsModel roomModel : roomsModelList) {
				date = LocalDate.now();
				date1 = Date.valueOf(date);
				for (RoomsModel bkRoomModel : bkRoomList) {
					bkRoomModel.setLastModifiedDate(date1);
					bkRoomModel.setCommunityApplicationNumber((roomModel.getCommunityApplicationNumber()));
					bkRoomModel.setDiscount(bookingsRequest.getBookingsModel().getDiscount());
				}
			}
		}

	}

	public BigDecimal extractDaysBetweenTwoDates(java.sql.Date fromDate, java.sql.Date toDate) {
		try {
			LocalDate dateBefore = null;
			LocalDate dateAfter = null;
			dateBefore = LocalDate.parse(fromDate + "");
			dateAfter = LocalDate.parse(toDate + "");
			long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
			long totalDays = noOfDaysBetween + 1;
			BigDecimal finalDays = BigDecimal.valueOf(totalDays);
			return finalDays;
		} catch (Exception e) {
			throw new CustomException("INVALID_REQUEST", e.getLocalizedMessage());
		}
	}

	public void enrichLockDates(BookingsRequest bookingsRequest) {
		SortedSet<Date> bookedDates = new TreeSet<>();
		java.util.Date date = new java.util.Date();
		DateFormat formatter = bookingsUtils.getSimpleDateFormat();
		String timestampString = formatter.format(date.getTime());
		Timestamp timestamp1 = Timestamp.valueOf(timestampString);
		Set<LocalDate> toBeBooked = extractAllDatesBetweenTwoDates(bookingsRequest);
		
		List<BookingLockModel> listLock = new ArrayList<>();
		if(!BookingsConstants.VENUE_TYPE_COMMERCIAL.equals(bookingsRequest.getBookingsModel().getBkBookingVenue())){
			listLock = bookingLockRepository.fetchLockedDates(
				bookingsRequest.getBookingsModel().getBkBookingVenue(),
				bookingsRequest.getBookingsModel().getBkBookingType(),
				bookingsRequest.getBookingsModel().getBkSector());
		}
		else {
			listLock = bookingLockRepository.fetchLockedDatesForCommercial(
					bookingsRequest.getBookingsModel().getBkBookingVenue(),
					bookingsRequest.getBookingsModel().getBkBookingType());
		}
		if(BookingsFieldsValidator.isNullOrEmpty(listLock)) {
			return;
		}
		List<LocalDate> fetchBookedDates = enrichLockedDates(listLock);
		outerloop:
		for (LocalDate toBeBooked1 : toBeBooked) {

			for (LocalDate fetchBookedDates1 : fetchBookedDates) {
				if (toBeBooked1.equals(fetchBookedDates1)) {
					bookedDates.add(Date.valueOf(toBeBooked1));
					break outerloop;
				}
			}
		}
		if(BookingsFieldsValidator.isNullOrEmpty(bookedDates)) {
			return;
		}
	
		
	   
	     // create a calendar and assign it the same time
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.SECOND, 98765);
	    cal.setTimeInMillis(timestamp1.getTime());
		for(BookingLockModel bookingLockModel : listLock) {
			Timestamp timestamp2 = Timestamp.valueOf(bookingLockModel.getCreatedDate());
			 long milliseconds = timestamp1.getTime() - timestamp2.getTime();
			 int seconds = (int) milliseconds / 1000;
			 
			    // calculate hours minutes and seconds
			    int hours = seconds / 3600;
			    int minutes = (seconds % 3600) / 60;
			    seconds = (seconds % 3600) % 60;
			    if(hours == 0 && minutes <= BookingsConstants.TEN_MINUTES && minutes >= 0) {
			    	throw new IllegalArgumentException("These dates are already locked by another user");
			    }
			    else {
			    	bookingLockRepository.deleteByApplicationNumber(bookingsRequest.getApplicationNumber());
			    }
		}
	}

	
	public List<LocalDate> enrichLockedDates(List<BookingLockModel> listLock) {
		List<LocalDate> listOfDates = new ArrayList<>();

		for (BookingLockModel bookingLockModel : listLock) {
			LocalDate startDate = LocalDate.parse(bookingLockModel.getFromDate() + "");
			LocalDate endDate = LocalDate.parse(bookingLockModel.getToDate() + "");
			long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

			List<LocalDate> listOfDates2 = LongStream.range(0, numOfDays).mapToObj(startDate::plusDays)
					.collect(Collectors.toList());
			listOfDates.addAll(listOfDates2);
			listOfDates.add(endDate);
		}

		return listOfDates;
	}

	public Set<Date> checkTimeslotsAvailabilty(BookingsRequest bookingsRequest, Set<BookingsModel> bookingsModelSet,
			SortedSet<Date> bookedDates) {
		List<BookingsModel> bookingsModelList = new ArrayList<>(bookingsModelSet);

		List<TimeslotsModel> extractedTimeSlots = extractTimeSlots(bookingsModelList, bookingsRequest);
		
		for (TimeslotsModel timeslots1 : extractedTimeSlots) {
			for (TimeslotsModel timeslots : bookingsRequest.getBookingsModel().getTimeslots()) {
				if (timeslots1.getSlot().equals(timeslots.getSlot())) {
//					bookedDates.add(Date.valueOf(LocalDate.now()));
					bookedDates.add(bookingsRequest.getBookingsModel().getBkFromDate());
					return bookedDates;
				}
			}
		}
		return bookedDates;

	}

	private List<TimeslotsModel> extractTimeSlots(List<BookingsModel> bookingsModelList,
			BookingsRequest bookingsRequest) {
		List<TimeslotsModel> extractedTimeSlots = new ArrayList<>();
		for (BookingsModel model : bookingsModelList) {
			if(bookingsRequest.getBookingsModel().getBkFromDate() == model.getBkFromDate()) {
				extractedTimeSlots.addAll(model.getTimeslots());
			}
		}
		return extractedTimeSlots;
	}
}
