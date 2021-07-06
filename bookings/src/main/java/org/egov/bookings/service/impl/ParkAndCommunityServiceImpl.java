package org.egov.bookings.service.impl;

import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.Transactional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.egov.bookings.config.BookingsConfiguration;
import org.egov.bookings.contract.AvailabilityResponse;
import org.egov.bookings.contract.BookingLockRequest;
import org.egov.bookings.contract.BookingsRequestKafka;
import org.egov.bookings.contract.MdmsJsonFields;
import org.egov.bookings.contract.ParkAndCommunitySearchCriteria;
import org.egov.bookings.contract.ParkCommunityFeeMasterRequest;
import org.egov.bookings.contract.ParkCommunityFeeMasterResponse;
import org.egov.bookings.model.BookingLockModel;
import org.egov.bookings.model.BookingsModel;
import org.egov.bookings.model.CommercialGrndAvailabilityModel;
import org.egov.bookings.model.ParkCommunityHallV1MasterModel;
import org.egov.bookings.producer.BookingsProducer;
import org.egov.bookings.repository.CommercialGrndAvailabilityRepository;
import org.egov.bookings.repository.ParkAndCommunityRepository;
import org.egov.bookings.repository.ParkCommunityHallV1MasterRepository;
import org.egov.bookings.service.BookingLockService;
import org.egov.bookings.service.BookingsService;
import org.egov.bookings.service.ParkAndCommunityService;
import org.egov.bookings.utils.BookingsConstants;
import org.egov.bookings.utils.BookingsUtils;
import org.egov.bookings.validator.BookingsFieldsValidator;
import org.egov.bookings.web.models.BookingsRequest;
import org.egov.bookings.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO: Auto-generated Javadoc
/**
 * The Class ParkAndCommunityServiceImpl.
 */
@Service
@Transactional
public class ParkAndCommunityServiceImpl implements ParkAndCommunityService {

	/** The bookings repository. */
	@Autowired
	private ParkAndCommunityRepository parkAndCommunityRepository;

	/** The save topic. */
	@Value("${kafka.topics.save.service}")
	private String saveTopic;

	/** The config. */
	@Autowired
	private BookingsConfiguration config;

	/** The workflow integrator. */
	@Autowired
	private WorkflowIntegrator workflowIntegrator;

	/** The enrichment service. */
	@Autowired
	private EnrichmentService enrichmentService;

	/** The park community hall V 1 master repository. */
	@Autowired
	private ParkCommunityHallV1MasterRepository parkCommunityHallV1MasterRepository;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(BookingsServiceImpl.class.getName());

	/** The booking service. */
	@Autowired
	private BookingsService bookingService;
	
	/** The lock. */
	private Lock lock = new ReentrantLock();

	/** The bookings producer. */
	@Autowired
	private BookingsProducer bookingsProducer;
	
	/** The user service. */
	@Autowired
	private UserService userService;
	
	/** The bookings fields validator. */
	@Autowired
	private BookingsFieldsValidator bookingsFieldsValidator;
	
	/** The commercial grnd availability repo. */
	@Autowired
	private CommercialGrndAvailabilityRepository commercialGrndAvailabilityRepo;
	
	/** The bookings utils. */
	@Autowired
	private BookingsUtils bookingsUtils;
	
	@Autowired
	private BookingLockService bookingLockService;
	
	@Autowired
	BookingsServiceImpl bookingsServiceImpl;
	
	/**
	 * Creates the park and community booking.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.egov.bookings.service.ParkAndCommunityService#
	 * createParkAndCommunityBooking(org.egov.bookings.web.models.BookingsRequest)
	 */
	@Override
	public BookingsModel createParkAndCommunityBooking(BookingsRequest bookingsRequest) {
		BookingsModel bookingObject = new BookingsModel();
		bookingObject.setBkApplicationStatus(bookingsRequest.getBookingsModel().getBkApplicationStatus());
		boolean flag = bookingService.isBookingExists(bookingsRequest.getBookingsModel().getBkApplicationNumber());
		enrichmentService.enrichReInitiatedRequest(bookingsRequest,flag);
		if(BookingsConstants.EMPLOYEE.equals(bookingsRequest.getRequestInfo().getUserInfo().getType()))
		userService.createUser(bookingsRequest, false);
		if (!flag) {
			enrichmentService.enrichParkCommunityCreateRequest(bookingsRequest);
			enrichmentService.enrichBookingsDetails(bookingsRequest);
		}
		enrichmentService.generateDemand(bookingsRequest);

		if (config.getIsExternalWorkFlowEnabled()) {
			if (!flag || bookingsRequest.getBookingsModel().isReInitiateStatus())
			workflowIntegrator.callWorkFlow(bookingsRequest);
		}
		try {
		BookingsRequestKafka kafkaBookingRequest = enrichmentService.enrichForKafka(bookingsRequest);
		if (!flag)
		bookingsProducer.push(config.getSaveBookingTopic(), kafkaBookingRequest);
		else
			bookingsProducer.push(config.getUpdateBookingTopic(), kafkaBookingRequest);
		}catch (Exception e) {
			throw new CustomException("PARK_COMMUNITY_CREATE_ERROR",e.getLocalizedMessage());
		}
		//parkAndCommunityRepository.save(bookingsRequest.getBookingsModel());
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
//			Map<String, MdmsJsonFields> mdmsJsonFieldsMap = bookingsServiceImpl.mdmsJsonField(bookingsRequest);
//			if (!BookingsFieldsValidator.isNullOrEmpty(mdmsJsonFieldsMap)) {
//				bookingsRequest.getBookingsModel().setBkBookingType(
//						mdmsJsonFieldsMap.get(bookingsRequest.getBookingsModel().getBkBookingType()).getModifiedName());
//				String applicantName = bookingsRequest.getBookingsModel().getBkApplicantName().trim();
//				if (!BookingsFieldsValidator.isNullOrEmpty(applicantName)) {
//					bookingsRequest.getBookingsModel().setBkApplicantName(applicantName.split(" ")[0]);
//				}
				if(!BookingsFieldsValidator.isNullOrEmpty(bookingObject) && !BookingsConstants.INITIATED.equals(bookingObject.getBkApplicationStatus())) {
					bookingsProducer.push(config.getSaveBookingSMSTopic(), bookingsRequest);
				}
//			}
		}
		return bookingsRequest.getBookingsModel();
	}

	/**
	 * Update park and community booking.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.egov.bookings.service.ParkAndCommunityService#
	 * updateParkAndCommunityBooking(org.egov.bookings.web.models.BookingsRequest)
	 */
	@Override
	public BookingsModel updateParkAndCommunityBooking(BookingsRequest bookingsRequest) {

		/*if(BookingsConstants.APPLY.equals(bookingsRequest.getBookingsModel().getBkAction()))
			enrichmentService.enrichBookingsAssignee(bookingsRequest);*/
		DateFormat formatter = bookingsUtils.getSimpleDateFormat();
			bookingsRequest.getBookingsModel().setLastModifiedDate(formatter.format(new java.util.Date()));
		String businessService = bookingsRequest.getBookingsModel().getBusinessService();
		bookingLockService.deleteLockDates(bookingsRequest.getBookingsModel().getBkApplicationNumber());
		bookingsFieldsValidator.validateRefundAmount(bookingsRequest);
		if (config.getIsExternalWorkFlowEnabled())
			workflowIntegrator.callWorkFlow(bookingsRequest);

		BookingsModel bookingsModel = null;
		if (!BookingsConstants.APPLY.equals(bookingsRequest.getBookingsModel().getBkAction())
				&& !BookingsConstants.OFFLINE_APPLY.equals(bookingsRequest.getBookingsModel().getBkAction())
				&& BookingsConstants.BUSINESS_SERVICE_PACC.equals(businessService)
				&& !BookingsConstants.PACC_ACTION_CANCEL.equals(bookingsRequest.getBookingsModel().getBkAction())
				&& !BookingsConstants.PACC_ACTION_MODIFY.equals(bookingsRequest.getBookingsModel().getBkAction())) {
			bookingsModel = enrichmentService.enrichPaccDetails(bookingsRequest);
			bookingsRequest.setBookingsModel(bookingsModel);
			BookingsRequestKafka kafkaBookingRequest = enrichmentService.enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getUpdateBookingTopic(), kafkaBookingRequest);
			//bookingsModel = parkAndCommunityRepository.save(bookingsModel);
		} else {
			enrichmentService.enrichPaccPaymentDetails(bookingsRequest);
			BookingsRequestKafka kafkaBookingRequest = enrichmentService.enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getUpdateBookingTopic(), kafkaBookingRequest);
			//bookingsModel = parkAndCommunityRepository.save(bookingsRequest.getBookingsModel());
		}
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel())) {
//			Map<String, MdmsJsonFields> mdmsJsonFieldsMap = bookingsServiceImpl.mdmsJsonField(bookingsRequest);
//			if (!BookingsFieldsValidator.isNullOrEmpty(mdmsJsonFieldsMap)) {
//				bookingsRequest.getBookingsModel()
//						.setBkBookingType(mdmsJsonFieldsMap.get(bookingsRequest.getBookingsModel().getBkBookingType()).getModifiedName());
//				String applicantName = bookingsRequest.getBookingsModel().getBkApplicantName().trim();
//				if (!BookingsFieldsValidator.isNullOrEmpty(applicantName)) {
//					bookingsRequest.getBookingsModel().setBkApplicantName(applicantName.split(" ")[0]);
//				}
				bookingsProducer.push(config.getUpdateBookingSMSTopic(), bookingsRequest);
//			}
		}
		return bookingsRequest.getBookingsModel();
	}

	/**
	 * Fetch park community master.
	 *
	 * @param parkCommunityFeeMasterRequest the park community fee master request
	 * @return the list
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.egov.bookings.service.ParkAndCommunityService#fetchParkCommunityMaster()
	 */
	@Override
	public List<ParkCommunityHallV1MasterModel> fetchParkCommunityMaster(ParkCommunityFeeMasterRequest parkCommunityFeeMasterRequest) {

		List<ParkCommunityHallV1MasterModel> parkCommunityHallV1Master = null;
		try {

			parkCommunityHallV1Master = parkCommunityHallV1MasterRepository.findByVenueTypeAndSectorAndIsActive(parkCommunityFeeMasterRequest.getVenueType(),parkCommunityFeeMasterRequest.getSector(),true);
			return parkCommunityHallV1Master;

		} catch (Exception e) {
			throw new CustomException("DATABASE_ERROR", e.getLocalizedMessage());

		}
	}

	/**
	 * Availability search.
	 *
	 * @param parkAndCommunitySearchCriteria the park and community search criteria
	 * @return the sets the
	 */
	/* (non-Javadoc)
	 * @see org.egov.bookings.service.ParkAndCommunityService#availabilitySearch(org.egov.bookings.contract.ParkAndCommunitySearchCriteria)
	 */
	@Override
	public Set<AvailabilityResponse> availabilitySearch(ParkAndCommunitySearchCriteria parkAndCommunitySearchCriteria) {

		// Date date = commercialGroundAvailabiltySearchCriteria.getDate();
		LocalDate date = LocalDate.now();
		Date date1 = Date.valueOf(date);
		Set<AvailabilityResponse> bookedDates = new HashSet<>();
		Set<CommercialGrndAvailabilityModel> availabilityLockModelList = commercialGrndAvailabilityRepo
				.findByBookingVenueAndIsLocked(parkAndCommunitySearchCriteria.getBookingVenue(), date1);
		Set<BookingsModel> bookingsModel = new HashSet<>();
		if(parkAndCommunitySearchCriteria.getBookingType().equals(BookingsConstants.COMMUNITY_CENTER)) {
			bookingsModel = parkAndCommunityRepository.fetchBookedDatesOfCommunity(
					parkAndCommunitySearchCriteria.getBookingType(),
					parkAndCommunitySearchCriteria.getSector(), date1, BookingsConstants.PAYMENT_SUCCESS_STATUS,
					parkAndCommunitySearchCriteria.getApplicationNumber());
		}
		else {
				bookingsModel = parkAndCommunityRepository.fetchBookedDatesOfParkAndCommunity(
				parkAndCommunitySearchCriteria.getBookingVenue(), parkAndCommunitySearchCriteria.getBookingType(),
				parkAndCommunitySearchCriteria.getSector(), date1, BookingsConstants.PAYMENT_SUCCESS_STATUS,
				parkAndCommunitySearchCriteria.getApplicationNumber());
		}
		if (null != bookingsModel) {
			for (BookingsModel bkModel : bookingsModel) {
				if (!BookingsConstants.PACC_ACTION_APPROVE_CLERK_DEO.equals(bkModel.getBkAction())) {
					bookedDates.add(AvailabilityResponse.builder().fromDate(bkModel.getBkFromDate())
							.toDate(bkModel.getBkToDate()).timeslots(bkModel.getTimeslots()).build());
				}
			}
		}

		if (!BookingsFieldsValidator.isNullOrEmpty(availabilityLockModelList)) {
			for (CommercialGrndAvailabilityModel availabilityLockModel : availabilityLockModelList) {
				if (availabilityLockModel.isLocked()) {
					bookedDates.add(AvailabilityResponse.builder().fromDate(availabilityLockModel.getFromDate())
							.toDate(availabilityLockModel.getToDate()).build());

				}
			}
		}
		return bookedDates;

	}

	/**
	 * Fetch booked dates.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the sets the
	 */
	/* (non-Javadoc)
	 * @see org.egov.bookings.service.ParkAndCommunityService#fetchBookedDates(org.egov.bookings.web.models.BookingsRequest)
	 */
	@Override
	public Set<Date> fetchBookedDates(BookingsRequest bookingsRequest) {

		enrichmentService.enrichLockDates(bookingsRequest);
		// Date date = commercialGroundAvailabiltySearchCriteria.getDate();
		LocalDate date = LocalDate.now();
		//Date date1 = Date.valueOf(date);
		SortedSet<Date> bookedDates = new TreeSet<>();
		//LocalDate sixMonthsBeforNow = date.minusMonths(1);
		Date date1 = Date.valueOf(date);
		LocalDate sixMonthsFromNow = date.plusMonths(6);
		Date currentDate = Date.valueOf(date);
		Date sixMonthsFromNowSql = Date.valueOf(sixMonthsFromNow);
		Set<BookingsModel> bookingsModelSet = new HashSet<>();
		try {
			//lock.lock();
			Set<LocalDate> toBeBooked = enrichmentService.extractAllDatesBetweenTwoDates(bookingsRequest);
			if (config.isParkAndCommunityLock()) {
		
				if(bookingsRequest.getBookingsModel().getBkBookingType().equals(BookingsConstants.COMMUNITY_CENTER)) {
					bookingsModelSet = parkAndCommunityRepository.fetchBookedDatesOfCommunity(
							bookingsRequest.getBookingsModel().getBkBookingType(),
							bookingsRequest.getBookingsModel().getBkSector(), date1, BookingsConstants.PAYMENT_SUCCESS_STATUS,
							bookingsRequest.getBookingsModel().getBkApplicationNumber());
				}
				else {
				bookingsModelSet = parkAndCommunityRepository.fetchBookedDatesOfParkAndCommunity(
						bookingsRequest.getBookingsModel().getBkBookingVenue(),
						bookingsRequest.getBookingsModel().getBkBookingType(),
						bookingsRequest.getBookingsModel().getBkSector(), date1, BookingsConstants.PAYMENT_SUCCESS_STATUS, bookingsRequest.getBookingsModel().getBkApplicationNumber());
				}
				Set<LocalDate> fetchBookedDates = enrichmentService.enrichBookedDates(bookingsModelSet);
				List<CommercialGrndAvailabilityModel> lockList = commercialGrndAvailabilityRepo
						.findLockedDatesFromNowTo6Months(currentDate, sixMonthsFromNowSql);

				for (LocalDate toBeBooked1 : toBeBooked) {

					for (LocalDate fetchBookedDates1 : fetchBookedDates) {
						
						if(bookingsRequest.getBookingsModel().getBkBookingType().equals(BookingsConstants.COMMUNITY_CENTER)) {
							if(bookingsRequest.getBookingsModel().getTimeslots() != null && toBeBooked1.equals(fetchBookedDates1)) {
								Set<Date> timeslotDate =  enrichmentService.checkTimeslotsAvailabilty(bookingsRequest, bookingsModelSet,
										bookedDates);
								if(BookingsFieldsValidator.isNullOrEmpty(timeslotDate)) {
//								    bookedDates.add(Date.valueOf(toBeBooked1));
									bookedDates.add(Date.valueOf(LocalDate.now()));
								    return bookedDates;
								}
									
							}
							else {
								if (toBeBooked1.equals(fetchBookedDates1)) {
									bookedDates.add(Date.valueOf(toBeBooked1));
									return bookedDates;
								}
							}
						}
						else {
							if (toBeBooked1.equals(fetchBookedDates1)) {
								bookedDates.add(Date.valueOf(toBeBooked1));
								return bookedDates;
							}
						}
					}
				}
				for (LocalDate toBeBooked1 : toBeBooked) {
					for (CommercialGrndAvailabilityModel commGrndAvailModel : lockList) {
						if (BookingsConstants.VENUE_TYPE_PARKS
								.equals(bookingsRequest.getBookingsModel().getBkBookingType())
								&& commGrndAvailModel.isLocked()
								&& bookingsRequest.getBookingsModel().getBkSector()
										.equals(commGrndAvailModel.getSector())
								&& bookingsRequest.getBookingsModel().getBkBookingVenue()
										.equals(commGrndAvailModel.getBookingVenue())) {
							if (toBeBooked1.equals(commGrndAvailModel.getFromDate().toLocalDate())) {
								bookedDates.add(commGrndAvailModel.getFromDate());
								return bookedDates;
							}
						} else if (BookingsConstants.VENUE_TYPE_COMMUNITY_CENTER
								.equals(bookingsRequest.getBookingsModel().getBkBookingType())
								&& commGrndAvailModel.isLocked()
								&& bookingsRequest.getBookingsModel().getBkSector()
										.equals(commGrndAvailModel.getSector())) {
							if (toBeBooked1.equals(commGrndAvailModel.getFromDate().toLocalDate())) {
								bookedDates.add(commGrndAvailModel.getFromDate());
								return bookedDates;
							}
						}
					}
				}
			} else {
				//lock.unlock();
				throw new CustomException("OTHER_PAYMENT_IN_PROCESS", "Please try after few seconds");
			}
			//lock.unlock();

		} catch (Exception e) {
			//lock.unlock();
			config.setParkAndCommunityLock(true);
			throw new CustomException("OTHER_PAYMENT_IN_PROCESS", "Please try after few seconds");
		}
		return bookedDates;

	}

	/**
	 * Find park and community fee.
	 *
	 * @param bookingVenue the booking venue
	 * @return the park community hall V 1 master model
	 */
	/* (non-Javadoc)
	 * @see org.egov.bookings.service.ParkAndCommunityService#findParkAndCommunityFee(java.lang.String)
	 */
	@Override
	public ParkCommunityHallV1MasterModel findParkAndCommunityFee(String bookingVenue) {
		ParkCommunityHallV1MasterModel parkCommunityHallFee = null;
		try {
			parkCommunityHallFee = parkCommunityHallV1MasterRepository.findById(bookingVenue);
			return parkCommunityHallFee;
		} catch (Exception e) {
			throw new CustomException("DATABASE_ERROR", e.getLocalizedMessage());
		}
	}

	/**
	 * Fetch amount.
	 *
	 * @param parkCommunityFeeMasterRequest the park community fee master request
	 * @return the park community fee master response
	 */
	/* (non-Javadoc)
	 * @see org.egov.bookings.service.ParkAndCommunityService#fetchAmount(org.egov.bookings.contract.ParkCommunityFeeMasterRequest)
	 */
	@Override
	public ParkCommunityFeeMasterResponse fetchAmount(ParkCommunityFeeMasterRequest parkCommunityFeeMasterRequest) {
		ParkCommunityHallV1MasterModel parkCommunityHallFee = null;
		try {
			parkCommunityHallFee = parkCommunityHallV1MasterRepository.findById(parkCommunityFeeMasterRequest.getBookingVenue());
			if(BookingsFieldsValidator.isNullOrEmpty(parkCommunityHallFee)) {
				throw new CustomException("DATABASE_ERROR", "Invalid Fee Master Request");
			}
			else {
				ParkCommunityFeeMasterResponse parkCommunityFeeMasterResponse= enrichmentService.enrichParkCommunityAmount(parkCommunityHallFee);
				return parkCommunityFeeMasterResponse;
			}
			
		} catch (Exception e) {
			throw new CustomException("DATABASE_ERROR", e.getLocalizedMessage());
		}
	}

	
	/**
	 * Fetch sector.
	 *
	 * @param venueType the venue type
	 * @return the list
	 */
	@Override
	public List<ParkCommunityHallV1MasterModel> fetchSector(String venueType) {
		if (BookingsFieldsValidator.isNullOrEmpty(venueType)) {
			throw new IllegalArgumentException("Invalid venueType");
		}
		List<ParkCommunityHallV1MasterModel> paccSectorList = new ArrayList<>();
		List<ParkCommunityHallV1MasterModel> paccSector = new ArrayList<>();
		try {
			paccSector = parkCommunityHallV1MasterRepository.findByVenueType(venueType); 
			paccSector.forEach(parkCommunityHallV1MasterModel -> {
				ParkCommunityHallV1MasterModel paccMasterModel = new ParkCommunityHallV1MasterModel();
				paccMasterModel.setSector(parkCommunityHallV1MasterModel.getSector());
				if(!paccSectorList.contains(paccMasterModel)){
					paccSectorList.add(paccMasterModel);
				}
			});
		}
		catch (Exception e) {
			LOGGER.error("Exception occur in the fetchSector " + e);
			e.printStackTrace();
		}
		return paccSectorList;
	}

	@Override
	public BookingLockModel bookingLock(BookingLockRequest bookingLockRequest) {
		BookingsModel bookingsModel = BookingsModel.builder()
				.bkBookingType(bookingLockRequest.getBookingLockModel().get(0).getBookingType())
				.bkBookingVenue(bookingLockRequest.getBookingLockModel().get(0).getBookingVenue())
				.bkSector(bookingLockRequest.getBookingLockModel().get(0).getSector())
				.bkFromDate(bookingLockRequest.getBookingLockModel().get(0).getFromDate())
				.bkToDate(bookingLockRequest.getBookingLockModel().get(0).getToDate()).build();

		BookingsRequest bookingsRequest = BookingsRequest.builder().bookingsModel(bookingsModel).applicationNumber("")
				.build();
		try {
			enrichmentService.enrichLockDates(bookingsRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DateFormat formatter = bookingsUtils.getSimpleDateFormat();
		for (BookingLockModel bookingLockModel : bookingLockRequest.getBookingLockModel()) {
			bookingLockModel.setId(UUID.randomUUID().toString());
			bookingLockModel.setLastModifiedDate(formatter.format(new java.util.Date()));
			bookingLockModel.setCreatedDate(formatter.format(new java.util.Date()));
		}

		try {
			// BookingsRequestKafka kafkaBookingRequest =
			// enrichmentService.enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getBookingLockDates(), bookingLockRequest);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
		return bookingLockRequest.getBookingLockModel().get(0);
	}

}
