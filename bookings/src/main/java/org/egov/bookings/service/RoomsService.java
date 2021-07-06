package org.egov.bookings.service;

import java.math.BigDecimal;
import java.util.Map;

import org.egov.bookings.contract.RoomFeeFetchRequest;
import org.egov.bookings.contract.RoomFeeFetchResponse;
import org.egov.bookings.dto.SearchCriteriaFieldsDTO;
import org.egov.bookings.model.BookingsModel;
import org.egov.bookings.web.models.BookingsRequest;
import org.springframework.stereotype.Service;

/**
 * The Interface RoomsService.
 */
@Service
public interface RoomsService {

	/**
	 * Creates the room for community booking.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	BookingsModel createRoomForCommunityBooking(BookingsRequest bookingsRequest);

	/**
	 * Gets the room amount.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the room amount
	 */
	BigDecimal getRoomAmount(BookingsRequest bookingsRequest);

	/**
	 * Checks if is room booking exists.
	 *
	 * @param bkApplicationNumber the bk application number
	 * @return true, if is room booking exists
	 */
	boolean isRoomBookingExists(String bkApplicationNumber);

	/**
	 * Update room for community booking.
	 *
	 * @param bookingsRequest the bookings request
	 * @return the bookings model
	 */
	BookingsModel updateRoomForCommunityBooking(BookingsRequest bookingsRequest);
	
	/**
	 * Community center room availbility fetch.
	 *
	 * @param searchCreteriaFieldDto the search creteria field dto
	 * @return the map
	 */
	public Map<String, String> communityCenterRoomAvailbilityFetch(SearchCriteriaFieldsDTO searchCreteriaFieldDto);

	/**
	 * Fetch room fee.
	 *
	 * @param roomFeeFetchRequest the room fee fetch request
	 * @return the room fee fetch response
	 */
	RoomFeeFetchResponse fetchRoomFee(RoomFeeFetchRequest roomFeeFetchRequest);
}
