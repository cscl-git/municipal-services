package org.egov.bookings.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.egov.bookings.config.BookingsConfiguration;
import org.egov.bookings.contract.Booking;
import org.egov.bookings.contract.BookingsRequestKafka;
import org.egov.bookings.contract.RoomFeeFetchRequest;
import org.egov.bookings.contract.RoomFeeFetchResponse;
import org.egov.bookings.dto.SearchCriteriaFieldsDTO;
import org.egov.bookings.model.BookingsModel;
import org.egov.bookings.model.ParkCommunityHallV1MasterModel;
import org.egov.bookings.model.RoomMasterModel;
import org.egov.bookings.model.RoomsModel;
import org.egov.bookings.producer.BookingsProducer;
import org.egov.bookings.repository.CommunityCenterRoomFeeRepository;
import org.egov.bookings.repository.ParkCommunityHallV1MasterRepository;
import org.egov.bookings.repository.RoomsRepository;
import org.egov.bookings.service.BookingsService;
import org.egov.bookings.service.RoomsService;
import org.egov.bookings.utils.BookingsCalculatorConstants;
import org.egov.bookings.utils.BookingsConstants;
import org.egov.bookings.utils.BookingsUtils;
import org.egov.bookings.validator.BookingsFieldsValidator;
import org.egov.bookings.web.models.BookingsRequest;
import org.egov.bookings.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RoomsServiceImpl implements RoomsService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger( RoomsServiceImpl.class.getName() );
	
	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private CommunityCenterRoomFeeRepository communityCenterRoomFeeRepository;

	@Autowired
	private BookingsConfiguration config;

	@Autowired
	private WorkflowIntegrator workflowIntegrator;

	@Autowired
	private BookingsProducer bookingsProducer;
	
	@Autowired
	private RoomsRepository roomsRepository; 
	
	@Autowired
	private UserService userService;
	
	/** The bookings service. */
	@Autowired
	private BookingsService bookingsService;
	
	/** The bookings utils. */
	@Autowired
	private BookingsUtils bookingsUtils;

	/** The park community hall V 1 master repository. */
	@Autowired
	private ParkCommunityHallV1MasterRepository parkCommunityHallV1MasterRepository;
	
	@Autowired
	private BookingsFieldsValidator bookingsFieldsValidator;
	
	@Override
	public BookingsModel createRoomForCommunityBooking(BookingsRequest bookingsRequest) {
		BookingsModel bookingObject = new BookingsModel();
		RoomsModel roomsModel = new RoomsModel();
		List<RoomsModel> roomsModelList = new ArrayList<>();
		roomsModel.setRoomApplicationStatus(bookingsRequest.getBookingsModel().getRoomsModel().get(0).getRoomApplicationStatus());
		roomsModelList.add(roomsModel);
		bookingObject.setRoomsModel(roomsModelList);
		boolean flag = isRoomBookingExists(bookingsRequest.getBookingsModel().getRoomsModel().get(0).getRoomApplicationNumber());
		if(BookingsConstants.EMPLOYEE.equals(bookingsRequest.getRequestInfo().getUserInfo().getType()))
			userService.createUser(bookingsRequest, false);
		if (!flag)
			enrichmentService.enrichRoomForCommunityBookingRequest(bookingsRequest);
		enrichmentService.enrichRoomDetails(bookingsRequest,flag);
		enrichmentService.generateDemandForRoom(bookingsRequest);
		if (config.getIsExternalWorkFlowEnabled()) {
			if (!flag)
			workflowIntegrator.callRoomWorkFlow(bookingsRequest);
		}
		BookingsRequestKafka kafkaBookingRequest = enrichmentService.enrichForKafka(bookingsRequest);
		if(!flag)
		bookingsProducer.push(config.getSaveRoomDetails(), kafkaBookingRequest);
		else {
			bookingsProducer.push(config.getUpdateRoomDetails(), kafkaBookingRequest);	
		}
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getRoomsModel())) {
			String applicantName = bookingsRequest.getBookingsModel().getBkApplicantName().trim();
			if (!BookingsFieldsValidator.isNullOrEmpty(applicantName)) {
				bookingsRequest.getBookingsModel().setBkApplicantName(applicantName.split(" ")[0]);
			}
			if(!BookingsFieldsValidator.isNullOrEmpty(bookingObject.getRoomsModel()) && !BookingsConstants.INITIATED.equals(bookingObject.getRoomsModel().get(0).getRoomApplicationStatus())) {
				bookingsProducer.push(config.getSaveRoomBookingSMSTopic(), bookingsRequest);
			}
		}
		return bookingsRequest.getBookingsModel();
	}

	@Override
	public BigDecimal getRoomAmount(BookingsRequest bookingsRequest) {
		List<RoomMasterModel> roomMasterModelListForAC = null;
		List<RoomMasterModel> roomMasterModelListForNONAC = null;
		LocalDate currentDate = LocalDate.now();
		BigDecimal acAmount = BigDecimal.ZERO;
		BigDecimal nonAcAmount = BigDecimal.ZERO;
		BigDecimal finalAmount = BigDecimal.ZERO;
		for (RoomsModel roomsModel : bookingsRequest.getBookingsModel().getRoomsModel()) {
			String typeOfRoom = roomsModel.getTypeOfRoom();
			String totalNoOfRooms = roomsModel.getTotalNoOfRooms();
			String sector = bookingsRequest.getBookingsModel().getBkSector();
			String communityCenterName = bookingsRequest.getBookingsModel().getBkLocation();
			if (BookingsConstants.AC.equals(typeOfRoom)) {
				roomMasterModelListForAC = communityCenterRoomFeeRepository
						.findByTypeOfRoomAndSectorNameAndCommunityCenterName(typeOfRoom, sector, communityCenterName);
			}
			if (BookingsConstants.NON_AC.equals(typeOfRoom)) {
				roomMasterModelListForNONAC = communityCenterRoomFeeRepository
						.findByTypeOfRoomAndSectorNameAndCommunityCenterName(typeOfRoom, sector, communityCenterName);
			}
		}
		if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelListForAC)
				&& BookingsFieldsValidator.isNullOrEmpty(roomMasterModelListForNONAC)) {
			throw new CustomException("DATA_NOT_FOUND",
					"There is not any amount for this room body criteria in database");
		}
		if (!BookingsFieldsValidator.isNullOrEmpty(roomMasterModelListForAC)) {
			for (RoomMasterModel roomMasterModelForAC : roomMasterModelListForAC) {
				if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForAC.getFromDate())) {
					throw new CustomException("DATA_NOT_FOUND",
							"There is no from date for this room criteria in database");
				}
				String pattern = "yyyy-MM-dd";
				DateFormat df = new SimpleDateFormat(pattern);
				String fromDateInString = df.format(roomMasterModelForAC.getFromDate());
				LocalDate fromDate = LocalDate.parse(fromDateInString);
				// LocalDate toDate = LocalDate.parse(toDateInString);
				if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForAC.getToDate())
						&& currentDate.isAfter(fromDate) || currentDate.isEqual(fromDate)) {
					// toDateInString = df.format(osbmFeeModel1.getToDate());
					acAmount = new BigDecimal(roomMasterModelForAC.getRentForOneDay());
				}
				if (!BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForAC.getToDate())
						&& (fromDate.isEqual(currentDate) || fromDate.isBefore(currentDate))
						&& (currentDate.isBefore(LocalDate.parse(df.format(roomMasterModelForAC.getToDate()))))) {
					acAmount = new BigDecimal(roomMasterModelForAC.getRentForOneDay());
					break;
				}
			}
			
		}
		for (RoomsModel roomModelForAC : bookingsRequest.getBookingsModel().getRoomsModel()) {
			if(BookingsConstants.AC.equals(roomModelForAC.getTypeOfRoom())) {
			BigDecimal days = enrichmentService.extractDaysBetweenTwoDates(roomModelForAC.getFromDate(),
					roomModelForAC.getToDate());
			finalAmount = acAmount.multiply(BigDecimal.valueOf(Double.valueOf(roomModelForAC.getTotalNoOfRooms())));
			finalAmount = finalAmount.multiply(days);
			
			}
		}
		if (!BookingsFieldsValidator.isNullOrEmpty(roomMasterModelListForNONAC)) {
			for (RoomMasterModel roomMasterModelForNONAC : roomMasterModelListForNONAC) {
				if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForNONAC.getFromDate())) {
					throw new CustomException("DATA_NOT_FOUND",
							"There is no from date for this room criteria in database");
				}
				String pattern = "yyyy-MM-dd";
				DateFormat df = new SimpleDateFormat(pattern);
				String fromDateInString = df.format(roomMasterModelForNONAC.getFromDate());
				LocalDate fromDate = LocalDate.parse(fromDateInString);
				// LocalDate toDate = LocalDate.parse(toDateInString);
				if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForNONAC.getToDate())
						&& currentDate.isAfter(fromDate) || currentDate.isEqual(fromDate)) {
					// toDateInString = df.format(osbmFeeModel1.getToDate());
					nonAcAmount = new BigDecimal(roomMasterModelForNONAC.getRentForOneDay());
				}
				if (!BookingsFieldsValidator.isNullOrEmpty(roomMasterModelForNONAC.getToDate())
						&& (fromDate.isEqual(currentDate) || fromDate.isBefore(currentDate))
						&& (currentDate.isBefore(LocalDate.parse(df.format(roomMasterModelForNONAC.getToDate()))))) {
					nonAcAmount = new BigDecimal(roomMasterModelForNONAC.getRentForOneDay());
					break;
				}
			}
		}
		for (RoomsModel roomModelForNonAC : bookingsRequest.getBookingsModel().getRoomsModel()) {
			if(BookingsConstants.NON_AC.equals(roomModelForNonAC.getTypeOfRoom())) {
			BigDecimal days = enrichmentService.extractDaysBetweenTwoDates(roomModelForNonAC.getFromDate(),
					roomModelForNonAC.getToDate());
			BigDecimal amountForTotalNoOfRooms = nonAcAmount.multiply(BigDecimal.valueOf(Double.valueOf(roomModelForNonAC.getTotalNoOfRooms())));
			finalAmount = finalAmount.add(amountForTotalNoOfRooms.multiply(days));
			}
		}
		return finalAmount;
	}

	@Override
	public boolean isRoomBookingExists(String roomApplicationNumber) {
		List<RoomsModel> roomsModel = roomsRepository.findByRoomApplicationNumber(roomApplicationNumber);

		if (BookingsFieldsValidator.isNullOrEmpty(roomsModel)) {
			return false;
		} else {
			return true;
		}

	}
	
	
	@Override
	public BookingsModel updateRoomForCommunityBooking(BookingsRequest bookingsRequest) {
		boolean flag = isRoomBookingExists(bookingsRequest.getBookingsModel().getRoomsModel().get(0).getRoomApplicationNumber());
		enrichmentService.enrichRoomDetails(bookingsRequest,flag);
		if (config.getIsExternalWorkFlowEnabled()) {
			workflowIntegrator.callRoomWorkFlow(bookingsRequest);
		}
		BookingsRequestKafka kafkaBookingRequest = enrichmentService.enrichForKafka(bookingsRequest);
			bookingsProducer.push(config.getUpdateRoomDetails(), kafkaBookingRequest);	
		//br.save(bookingsRequest.getBookingsModel());
		if (!BookingsFieldsValidator.isNullOrEmpty(bookingsRequest.getBookingsModel().getRoomsModel())) {
			String applicantName = bookingsRequest.getBookingsModel().getBkApplicantName().trim();
			if (!BookingsFieldsValidator.isNullOrEmpty(applicantName)) {
				bookingsRequest.getBookingsModel().setBkApplicantName(applicantName.split(" ")[0]);
			}
			bookingsProducer.push(config.getUpdateRoomBookingSMSTopic(), bookingsRequest);
		}
		return bookingsRequest.getBookingsModel();
	}

	/**
	 * Community center room availbility fetch.
	 *
	 * @param searchCriteriaFieldsDTO the search criteria fields DTO
	 * @return the map
	 */
	@Override
	public Map<String, String> communityCenterRoomAvailbilityFetch(SearchCriteriaFieldsDTO searchCriteriaFieldsDTO) {
		if (BookingsFieldsValidator.isNullOrEmpty(searchCriteriaFieldsDTO)) 
		{
			throw new IllegalArgumentException("Invalid searchCreteriaFieldDto");
		}
		String applicationNumber = searchCriteriaFieldsDTO.getApplicationNumber().trim();
		if (BookingsFieldsValidator.isNullOrEmpty(applicationNumber)) 
		{
			throw new IllegalArgumentException("Invalid applicationNumber");
		}
		List<RoomsModel> roomBookingList = new ArrayList<>();
		Map<String, String> typesOfRoomMap = new HashMap<>();
		try {
			Booking booking = bookingsService.getCommunityCenterBookingSearch(searchCriteriaFieldsDTO);
			if (!BookingsFieldsValidator.isNullOrEmpty(booking) && !BookingsFieldsValidator.isNullOrEmpty(booking.getBookingsModelList())) {
				List<BookingsModel> bookingsList = booking.getBookingsModelList();
				if (!BookingsFieldsValidator.isNullOrEmpty(bookingsList) && !BookingsFieldsValidator.isNullOrEmpty(bookingsList.get(0))
						&& !BookingsFieldsValidator.isNullOrEmpty(bookingsList.get(0).getBkBookingVenue())) {
					typesOfRoomMap = getAllTypeOfCommunityCenterRooms(bookingsList.get(0).getBkBookingVenue());
				}
			}
			roomBookingList = roomsRepository.findByCommunityApplicationNumber(applicationNumber);
			if (!BookingsFieldsValidator.isNullOrEmpty(typesOfRoomMap)) {
				typesOfRoomMap = calculateAvailableRoom(roomBookingList, typesOfRoomMap);
			}
		}
		catch (Exception e) {
			LOGGER.error("Exception occur in the communityCenterRoomAvailbilityFetch " + e);
			e.printStackTrace();
		}
		return typesOfRoomMap;
	}
	
	/**
	 * Gets the all type of community center rooms.
	 *
	 * @param communityCenterNameId the community center name id
	 * @return the all type of community center rooms
	 */
	private Map<String, String> getAllTypeOfCommunityCenterRooms(String communityCenterNameId) {
		Map<String, String> typesOfRoomMap = new HashMap<>();
		List<RoomMasterModel> roomMasterList = new ArrayList<>();
		try {
			if(!BookingsFieldsValidator.isNullOrEmpty(communityCenterNameId)) {
				ParkCommunityHallV1MasterModel parkCommunityHallV1MasterModel = parkCommunityHallV1MasterRepository.findById(communityCenterNameId);
				if(!BookingsFieldsValidator.isNullOrEmpty(parkCommunityHallV1MasterModel)) {
					String communityCenterName = parkCommunityHallV1MasterModel.getName().trim();
					if(!BookingsFieldsValidator.isNullOrEmpty(communityCenterName)) {
						roomMasterList = communityCenterRoomFeeRepository.findRoomMasterList(new java.util.Date(), communityCenterName);
						if(!BookingsFieldsValidator.isNullOrEmpty(roomMasterList)) {
							roomMasterList.forEach(roomMaster -> {
								if(BookingsConstants.AC.equals(roomMaster.getTypeOfRoom())) {
									typesOfRoomMap.put(BookingsConstants.AC, roomMaster.getTotalNumberOfRooms());
								}
								else if(BookingsConstants.NON_AC.equals(roomMaster.getTypeOfRoom())) {
									typesOfRoomMap.put(BookingsConstants.NON_AC, roomMaster.getTotalNumberOfRooms());
								}
							});
						}
						else {
							roomMasterList = communityCenterRoomFeeRepository.getRoomMasterList(new java.util.Date(), communityCenterName);
							roomMasterList.forEach(roomMaster ->{
								if(BookingsFieldsValidator.isNullOrEmpty(roomMaster.getToDate())) {
									if(BookingsConstants.AC.equals(roomMaster.getTypeOfRoom())) {
										typesOfRoomMap.put(BookingsConstants.AC, roomMaster.getTotalNumberOfRooms());
									}
									else if(BookingsConstants.NON_AC.equals(roomMaster.getTypeOfRoom())) {
										typesOfRoomMap.put(BookingsConstants.NON_AC, roomMaster.getTotalNumberOfRooms());
									}
								}
							});
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOGGER.error("Exception occur in the getAllTypeOfCommunityCenterRooms " + e);
			e.printStackTrace();
		}
		return typesOfRoomMap;
	}
	
	/**
	 * Calculate available room.
	 *
	 * @param roomBookingList the room booking list
	 * @param roomTypeMap the room type map
	 * @return the map
	 */
	private Map<String, String> calculateAvailableRoom(List<RoomsModel> roomBookingList, Map<String, String> typesOfRoomMap) {
		int totalNumberOfAcRoom = 0;
		int totalNumberOfNonAcRoom = 0;
		try {
			typesOfRoomMap.put(BookingsConstants.TOTAL_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.AC));
			typesOfRoomMap.put(BookingsConstants.TOTAL_NON_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.NON_AC));
			typesOfRoomMap.put(BookingsConstants.BOOKED_AC_ROOMS, String.valueOf(totalNumberOfAcRoom));
			typesOfRoomMap.put(BookingsConstants.BOOKED_NON_AC_ROOMS, String.valueOf(totalNumberOfNonAcRoom));
			typesOfRoomMap.put(BookingsConstants.AVAILABLE_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.AC));
			typesOfRoomMap.put(BookingsConstants.AVAILABLE_NON_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.NON_AC));
			if(!BookingsFieldsValidator.isNullOrEmpty(roomBookingList)) {
				for(RoomsModel roomModel : roomBookingList) {
					if(BookingsConstants.AC.equals(roomModel.getTypeOfRoom()) && !BookingsConstants.CANCEL.equals(roomModel.getRoomStatus())) {
						totalNumberOfAcRoom = totalNumberOfAcRoom + Integer.parseInt(roomModel.getTotalNoOfRooms());
					}
					else if(BookingsConstants.NON_AC.equals(roomModel.getTypeOfRoom()) && !BookingsConstants.CANCEL.equals(roomModel.getRoomStatus())) {
						totalNumberOfNonAcRoom = totalNumberOfNonAcRoom + Integer.parseInt(roomModel.getTotalNoOfRooms());
					}
				}
				typesOfRoomMap.put(BookingsConstants.BOOKED_AC_ROOMS, String.valueOf(totalNumberOfAcRoom));
				typesOfRoomMap.put(BookingsConstants.BOOKED_NON_AC_ROOMS, String.valueOf(totalNumberOfNonAcRoom));
				typesOfRoomMap.put(BookingsConstants.AVAILABLE_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.AC));
				typesOfRoomMap.put(BookingsConstants.AVAILABLE_NON_AC_ROOMS, typesOfRoomMap.get(BookingsConstants.NON_AC));
				if(totalNumberOfAcRoom > 0) {
					typesOfRoomMap.put(BookingsConstants.AVAILABLE_AC_ROOMS, String.valueOf(Integer.parseInt(typesOfRoomMap.get(BookingsConstants.AC)) - totalNumberOfAcRoom));
				}
				if(totalNumberOfNonAcRoom > 0) {
					typesOfRoomMap.put(BookingsConstants.AVAILABLE_NON_AC_ROOMS, String.valueOf(Integer.parseInt(typesOfRoomMap.get(BookingsConstants.NON_AC)) - totalNumberOfNonAcRoom));
				}
			}
		}
		catch (Exception e) {
			LOGGER.error("Exception occur in the calculateAvailableRoom " + e);
			e.printStackTrace();
		}
		return typesOfRoomMap;
	}

	@Override
	public RoomFeeFetchResponse fetchRoomFee(RoomFeeFetchRequest roomFeeFetchRequest) {
		List<RoomMasterModel> roomMasterModelList = null;
		LocalDate currentDate = LocalDate.now();
		BigDecimal amount = null;
		BigDecimal ugstAndCgst = null;
		BigDecimal totalAmount = null;
		RoomFeeFetchResponse roomFeeFetchResponse = null;
		roomMasterModelList = communityCenterRoomFeeRepository
				.findBySectorNameAndTypeOfRoomAndCommunityCenterName(
						roomFeeFetchRequest.getSector(),
						roomFeeFetchRequest.getTypeOfRomm(), roomFeeFetchRequest.getCommunityCenterName());
		if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModelList)) {
			throw new CustomException("DATA_NOT_FOUND",
					"There is not any data with this fee criteria in bk room master table");
		}

		for (RoomMasterModel roomMasterModel : roomMasterModelList) {
			if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModel.getFromDate())) {
				throw new CustomException("DATA_NOT_FOUND",
						"There is no from date for this room fee fetch criteria in database");
			}
			String pattern = "yyyy-MM-dd";
			DateFormat df = new SimpleDateFormat(pattern);
			String fromDateInString = df.format(roomMasterModel.getFromDate());
			LocalDate fromDate = LocalDate.parse(fromDateInString);
			// LocalDate toDate = LocalDate.parse(toDateInString);
			if (BookingsFieldsValidator.isNullOrEmpty(roomMasterModel.getToDate()) && currentDate.isAfter(fromDate)
					|| currentDate.isEqual(fromDate)) {
				// toDateInString = df.format(osbmFeeModel1.getToDate());
				amount = BigDecimal.valueOf(Double.valueOf(roomMasterModel.getRentForOneDay()));
				ugstAndCgst = (BigDecimal.valueOf(Double.valueOf(roomMasterModel.getRentForOneDay()))
						.divide(new BigDecimal(100))).multiply(BookingsCalculatorConstants.UGST_AND_CGST_TAX);
				totalAmount = amount.add(ugstAndCgst);
				roomFeeFetchResponse = RoomFeeFetchResponse.builder().amount(amount).ugstAndCgst(ugstAndCgst)
						.totalAmount(totalAmount).build();

			}
			if (!BookingsFieldsValidator.isNullOrEmpty(roomMasterModel.getToDate())
					&& (fromDate.isEqual(currentDate) || fromDate.isBefore(currentDate))
					&& (currentDate.isBefore(LocalDate.parse(df.format(roomMasterModel.getToDate()))))) {
				amount = BigDecimal.valueOf(Double.valueOf(roomMasterModel.getRentForOneDay()));
				ugstAndCgst = (BigDecimal.valueOf(Double.valueOf(roomMasterModel.getRentForOneDay()))
						.divide(new BigDecimal(100))).multiply(BookingsCalculatorConstants.UGST_AND_CGST_TAX);
				totalAmount = amount.add(ugstAndCgst);
				roomFeeFetchResponse = RoomFeeFetchResponse.builder().amount(amount).ugstAndCgst(ugstAndCgst)
						.totalAmount(totalAmount).build();

				break;
			}
		}

		return roomFeeFetchResponse;
	}
}
