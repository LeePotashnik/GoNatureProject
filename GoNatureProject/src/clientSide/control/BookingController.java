package clientSide.control;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import clientSide.gui.RescheduleScreenController.AvailableSlot;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.SecondaryRequest;
import common.communication.CommunicationException;
import entities.Booking;
import entities.Booking.VisitType;
import entities.Park;
import entities.ParkVisitor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class BookingController {
	private static BookingController instance;
	private ArrayList<Park> parkList;
	private PaymentController paymentControl = PaymentController.getInstance();
	private ParkController parkControl = ParkController.getInstance();

	// date validation parameters
	/**
	 * What is the future booking range, in months. Default: 4 months
	 */
	public int futureBookingsRange = 4;
	/**
	 * opening hour for the parks. Default: 8AM
	 */
	public int openHour = 8;
	/**
	 * closing hour for the parks. Default: 18PM + Park's time limit
	 */
	public int closeHour = 18;
	/**
	 * sets the minutes of the available hours. Default: 0, which means reservations
	 * can be made in jumpings of one hour
	 */
	public int minutes = 0;
	/**
	 * minimum visitors in reservation
	 */
	public int minimumVisitorsInReservation = 1;
	/**
	 * maximum visitors in reservation
	 */
	public int maximumVisitorsInGroupReservation = 15;
	/**
	 * reminder sending time before arrival
	 */
	public int reminderSendingTime = 24; // hours

	// for saving and restoring purposes of the screens
	private Booking booking;
	private int parkIndexInCombobox;
	private ParkVisitor visitor;
	private boolean isSavedState = false;
	private Pair<ObservableList<Booking>, ObservableList<Booking>> pair;

	/**
	 * An empty and private controller, for the singelton design pattern
	 */
	private BookingController() {
	}

	/**
	 * The BookingController is defined as a Singleton class. This method allows
	 * creating an instance of the class only once during runtime of the
	 * application.
	 * 
	 * @return the BookingController instance
	 */
	public static BookingController getInstance() {
		if (instance == null)
			instance = new BookingController();
		return instance;
	}

	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	///// COMMON METHODS FOR ALL CONTROLLED SCREENS /////
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////

	/**
	 * This method requests parks details from the database and returns a pair of
	 * observable lists of the parks, one of Park and the second of String
	 */
	public Pair<ObservableList<Park>, ObservableList<String>> fetchParks() {
		parkList = parkControl.fetchParks();
		// returning the pair of the observable lists
		Pair<ObservableList<Park>, ObservableList<String>> pair = new Pair<>(
				FXCollections.observableArrayList(parkList), parksAsString());
		return pair;
	}

	/**
	 * This method translates the park list to a string list to be shown in the
	 * combobox of the screen
	 * 
	 * @return an observable list of strings
	 */
	private ObservableList<String> parksAsString() {
		ArrayList<String> parksStrings = new ArrayList<>();
		for (Park park : parkList) {
			parksStrings.add(park.getParkName() + " - " + park.getParkCity() + ", " + park.getParkState());
		}
		return FXCollections.observableArrayList(parksStrings);
	}

	/**
	 * This method is called for deleteing a booking from the active booking table
	 * of the specific park
	 * 
	 * @param deleteBooking
	 * @param table         the table the booking need to be deleted from
	 * @return true if the deletion succeed, false if not
	 */
	public boolean deleteBooking(Booking deleteBooking, String table) {
		// creating the request for the booking deletion
		Communication deleteRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			deleteRequest.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		String parkTableName = parkControl.nameOfTable(deleteBooking.getParkBooked()) + table;
		deleteRequest.setTables(Arrays.asList(parkTableName));
		deleteRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
				Arrays.asList(deleteBooking.getBookingId()));

		if (table.equals(Communication.activeBookings)) {
			deleteRequest.setSecondaryRequest(SecondaryRequest.UPDATE_WAITING_LIST);
		}

		deleteRequest.setSecondaryRequest(SecondaryRequest.UPDATE_WAITING_LIST);
		deleteRequest.setParkId(deleteBooking.getParkBooked().getParkId());
		deleteRequest.setDate(deleteBooking.getDayOfVisit());
		deleteRequest.setTime(deleteBooking.getTimeOfVisit());

		// sending the request to the server side
		GoNatureClientUI.client.accept(deleteRequest);
		boolean result = deleteRequest.getQueryResult();

		// returning the result
		return result;
	}

	/**
	 * This method gets a park object and returns its updated orders amount and time
	 * limit parameters from the database
	 * 
	 * @param parkToCheck
	 * @return the updated parameters as a pair: the key is the maximum order
	 *         amount, the value is the maximum time limit
	 */
	private Pair<Integer, Integer> getParkUpdatedParameters(Park parkToCheck) {
		// creating the request for the booking deletion
		Communication selectRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			selectRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		selectRequest.setTables(Arrays.asList("park"));
		selectRequest.setSelectColumns(Arrays.asList("maximumOrderAmount", "maximumTimeLimit"));
		selectRequest.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),
				Arrays.asList(parkToCheck.getParkName()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(selectRequest);

		// getting the result from the database
		Pair<Integer, Integer> pair = new Pair<>((Integer) selectRequest.getResultList().get(0)[0],
				(Integer) selectRequest.getResultList().get(0)[1]);
		return pair;
	}

	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	///// METHODS FOR CONTROLLED BOOKING SCREEN /////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////

	/**
	 * This method checks the availability for a specific booking, by checking the
	 * specific park parameters and active bookings on the same date and time range
	 * of the checked order
	 * 
	 * @param booking the booking that is checked
	 * @return true if there's enough place for this group, false if not
	 */
	public boolean checkParkAvailabilityForNewBooking(Booking newBooking) {
		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(newBooking.getParkBooked());
		int numberOfVisitors = newBooking.getNumberOfVisitors();

		// checking park parameters
		int countVisitors = getCurrentParkCapacities(newBooking.getParkBooked(), newBooking.getDayOfVisit(),
				newBooking.getTimeOfVisit(), pair.getValue());
		return pair.getKey() - countVisitors - numberOfVisitors >= 0;
	}

	/**
	 * This method gets a park, a date, a time and time limits, and return the
	 * park's current orders capacity in this time frame
	 * 
	 * @param park
	 * @param date
	 * @param time
	 * @param timeLimit
	 * @return the park's current capacity for the specified time frame
	 */
	private int getCurrentParkCapacities(Park park, LocalDate date, LocalTime time, int timeLimit) {
		// creating the request for the availability check
		Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			availabilityRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(park) + availabilityRequest.activeBookings;

		availabilityRequest.setTables(Arrays.asList(parkTableName));
		availabilityRequest.setSelectColumns(Arrays.asList("numberOfVisitors"));
		availabilityRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(date, time.minusHours(timeLimit), time.plusHours(timeLimit)));

		// sending the request to the server side
		GoNatureClientUI.client.accept(availabilityRequest);

		// getting the result from the database
		int countVisitors = 0;
		// checking the orders amount for the specific time
		for (Object[] row : availabilityRequest.getResultList()) {
			countVisitors += (Integer) row[0];
		}

		return countVisitors;
	}

	/**
	 * This method connects with the PaymentController in order to calculate the
	 * price for the group with the most updated prices, with discounts applied
	 * 
	 * @param newBooking
	 * @return the calculated price with discount
	 */
	public int calculateFinalDiscountPrice(Booking newBooking, boolean isGroup, boolean isPrePaid) {
		if (isGroup) { // guided group
			return paymentControl.calculateDiscountPriceGuidedGroup(newBooking, isPrePaid);

		} else { // individual group
			return paymentControl.calculateDiscountPriceTravelersGroup(newBooking);
		}
	}

	/**
	 * This method gets a new booking details and inserts the booking into the park
	 * booked active bookings table
	 * 
	 * @param newBooking the booking to insert
	 * @return true if the insert query succeed, false if not
	 */
	public boolean insertNewBookingToActiveTable(Booking newBooking) {
		// creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(newBooking.getParkBooked()) + insertRequest.activeBookings;
		insertRequest.setTables(Arrays.asList(parkTableName));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType", "numberOfVisitors",
						"idNumber", "firstName", "lastName", "emailAddress", "phoneNumber", "finalPrice", "paid",
						"confirmed", "entryParkTime", "exitParkTime", "isRecievedReminder", "reminderArrivalTime"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(),
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
						newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
						newBooking.getFinalPrice(), newBooking.isPaid() == false ? 0 : 1,
						newBooking.isConfirmed() == false ? 0 : 1, newBooking.getEntryParkTime(),
						newBooking.getExitParkTime(), newBooking.isRecievedReminder() == false ? 0 : 1,
						newBooking.getReminderArrivalTime()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(insertRequest);

		// getting the result from the database
		return insertRequest.getQueryResult();
	}

	/**
	 * This method is called in order to send a confirmation or cancellation to the
	 * booker
	 * 
	 * @param notify   the booking to notify its holder
	 * @param isCancel true if a cancellation notification is needed to be sent,
	 *                 false if confirmation notification is needed to be sent
	 */
	public void sendNotification(Booking notify, boolean isCancel) {
		Communication notifyBooking = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			notifyBooking.setQueryType(QueryType.NONE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		notifyBooking.setSecondaryRequest(
				isCancel ? SecondaryRequest.SEND_CANCELLATION : SecondaryRequest.SEND_CONFIRMATION);
		notifyBooking.setFullName(notify.getFirstName() + " " + notify.getLastName());
		notifyBooking.setEmail(notify.getEmailAddress());
		notifyBooking.setPhone(notify.getPhoneNumber());
		notifyBooking.setPrice(notify.getFinalPrice());
		notifyBooking.setPaid(notify.isPaid());
		notifyBooking.setVisitors(notify.getNumberOfVisitors());
		notifyBooking.setDate(notify.getDayOfVisit());
		notifyBooking.setTime(notify.getTimeOfVisit());
		notifyBooking.setParkName(notify.getParkBooked().getParkName() + " Park");
		notifyBooking
				.setParkLocation(notify.getParkBooked().getParkCity() + ", " + notify.getParkBooked().getParkState());

		GoNatureClientUI.client.accept(notifyBooking);
	}

	/**
	 * This method is called if a booking got paid, and updating its price columns
	 * is required
	 * 
	 * @param updateBooking the booking to update
	 * @return true if the update query succeed, false if failed
	 */
	public boolean updateBookingPayment(Booking updateBooking) {
		// creating the request for the new booking payment update
		Communication updateRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			updateRequest.setQueryType(QueryType.UPDATE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(updateBooking.getParkBooked()) + updateRequest.activeBookings;
		updateRequest.setTables(Arrays.asList(parkTableName));
		updateRequest.setColumnsAndValues(Arrays.asList("finalPrice", "paid"),
				Arrays.asList(updateBooking.getFinalPrice(), updateBooking.isPaid() == true ? 1 : 0));
		updateRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
				Arrays.asList(updateBooking.getBookingId()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(updateRequest);

		// getting the result from the database
		return updateRequest.getQueryResult();
	}

	//////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	///// METHODS FOR CONTROLLED BOOKING MANAGING SCREEN /////
	//////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////

	/**
	 * This method gets a visitor and returns all his active/cancelled/done bookings
	 * in all parks
	 * 
	 * @param visitorCheck
	 * @param tableEnding  determines if checking in the active/cancelled/done
	 *                     tables
	 * @return the bookings list
	 */
	@SuppressWarnings("static-access")
	public ObservableList<Booking> getVisitorBookings(ParkVisitor visitorCheck, String tableEnding) {
		ObservableList<Booking> returnList = FXCollections.observableArrayList();
		// creating the requests for the bookings retrieval
		if (parkList == null)
			fetchParks();
		for (Park park : parkList) {
			String tableName = parkControl.nameOfTable(park) + tableEnding;
			Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				availabilityRequest.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			availabilityRequest.setTables(Arrays.asList(tableName));
			availabilityRequest.setSelectColumns(Arrays.asList("*"));
			availabilityRequest.setWhereConditions(Arrays.asList("idNumber"), Arrays.asList("="),
					Arrays.asList(visitorCheck.getIdNumber()));
			// sending the request to the server side
			GoNatureClientUI.client.accept(availabilityRequest);
			// getting the result from the server side
			if (!availabilityRequest.getResultList().isEmpty()) {
				for (Object[] row : availabilityRequest.getResultList()) {
					Booking addBooking;

					// if this is the active booking table to check in
					if (tableEnding == availabilityRequest.activeBookings) {
						addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
								((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
								((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
								(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
								(String) row[10], (Integer) row[11], (Integer) row[12] == 0 ? false : true,
								(Integer) row[13] == 0 ? false : true,
								((Time) row[14]) == null ? null : ((Time) row[14]).toLocalTime(),
								((Time) row[15]) == null ? null : ((Time) row[15]).toLocalTime(),
								(Integer) row[16] == 0 ? false : true,
								((Time) row[17]) == null ? null : ((Time) row[17]).toLocalTime(), park);
						addBooking.setStatus("Active");

					} else if (tableEnding == availabilityRequest.cancelledBookings) {
						// if this is the cancelled booking table to check in
						addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
								((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
								((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
								(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
								(String) row[10], -1, false, false, null, null, false, null, park);
						addBooking.setStatus("Cancelled");

					} else if (tableEnding == availabilityRequest.doneBookings) {
						// if this is the done booking table to check in
						addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
								((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
								((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
								(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
								(String) row[10], (Integer) row[11], true, false,
								((Time) row[12]) == null ? null : ((Time) row[12]).toLocalTime(),
								((Time) row[13]) == null ? null : ((Time) row[13]).toLocalTime(), false, null, park);
						addBooking.setStatus("Finished");

					} else {
						// if this is the waiting list table to check in
						addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
								((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
								((String) row[5]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
								(Integer) row[6], (String) row[7], (String) row[8], (String) row[9], (String) row[10],
								(String) row[11], (Integer) row[12], false, false, null, null, false, null, park);
						addBooking.setWaitingListPriority((Integer) row[4]);
						addBooking.setStatus("Waiting List");
					}
					returnList.add(addBooking);
				}
			}
		}
		return returnList;
	}

	/**
	 * This method checks the availability for a specific booking, by checking the
	 * specific park parameters and active bookings on the same date and time range
	 * of the checked order. The editing booking screen allows users to modify their
	 * bookings and check availabilty after the modification. There is a need to, in
	 * same cases, substract the old booking's number of visitors from the total
	 * park capacity in order to return correct and real answer
	 * 
	 * @param oldBooking the old booking that is required to be changed
	 * @param newBooking the new booking to be checked
	 * @return true if there's enough place for this group, false if not
	 */
	public boolean checkParkAvailabilityForExistingBooking(Booking oldBooking, Booking newBooking) {
		// checking if the new booking is in the same park as the old booking
		// if not: the old one has no effect of the new booking's park
		if (!oldBooking.getParkBooked().equals(newBooking.getParkBooked())) {
			return checkParkAvailabilityForNewBooking(newBooking);
		}

		// if this is not the same park, checking if the new booking's date is the same
		// as the old booking's date
		// if not: the old one has no effect of the new booking's capacities check
		if (!oldBooking.getDayOfVisit().equals(newBooking.getDayOfVisit())) {
			return checkParkAvailabilityForNewBooking(newBooking);
		}

		// if this is the same park and the same date, checking if the time range of the
		// new booking is overlapping the old booking time
		// if not: the old one has no effect of the new booking's capacities check

		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(newBooking.getParkBooked());
		int parkTimeLimit = pair.getValue();
		if (!(newBooking.getTimeOfVisit().compareTo(oldBooking.getTimeOfVisit().minusHours(parkTimeLimit)) > 0
				&& newBooking.getTimeOfVisit().compareTo(oldBooking.getTimeOfVisit().plusHours(parkTimeLimit)) < 0)) {
			return checkParkAvailabilityForNewBooking(newBooking);
		}

		// if arrived here, the new booking has the same park, date and time range of
		// the old booking
		int parkCurrentCapacity = getCurrentParkCapacities(newBooking.getParkBooked(), newBooking.getDayOfVisit(),
				newBooking.getTimeOfVisit(), pair.getValue());
		return pair.getKey() - parkCurrentCapacity - newBooking.getNumberOfVisitors()
				+ oldBooking.getNumberOfVisitors() >= 0;

	}

	/**
	 * This method gets a booking and inserts the booking into the park cancelled
	 * bookings table
	 * 
	 * @param newBooking the booking to insert
	 * @return true if the insert query succeed, false if not
	 */
	public boolean insertBookingToCancelledTable(Booking cancelledBooking, String reason) {
		// creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(cancelledBooking.getParkBooked())
				+ insertRequest.cancelledBookings;
		insertRequest.setTables(Arrays.asList(parkTableName));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType", "numberOfVisitors",
						"idNumber", "firstName", "lastName", "emailAddress", "phoneNumber", "cancellationReason"),
				Arrays.asList(cancelledBooking.getBookingId(), cancelledBooking.getDayOfVisit(),
						cancelledBooking.getTimeOfVisit(), cancelledBooking.getDayOfBooking(),
						cancelledBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						cancelledBooking.getNumberOfVisitors(), cancelledBooking.getIdNumber(),
						cancelledBooking.getFirstName(), cancelledBooking.getLastName(),
						cancelledBooking.getEmailAddress(), cancelledBooking.getPhoneNumber(), reason));

		// sending the request to the server side
		GoNatureClientUI.client.accept(insertRequest);

		// getting the result from the database
		return insertRequest.getQueryResult();
	}

	/**
	 * This method gets an old booking, deletes it from the active bookings table
	 * and inserts the new booking, all within a signle transaction
	 * 
	 * @param oldBooking
	 * @param newBooking
	 * @return
	 */
	public boolean updateBooking(Booking oldBooking, Booking newBooking) {
		Communication transaction = new Communication(CommunicationType.TRANSACTION);
		// creating the request for the old booking deletion
		Communication deleteRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			deleteRequest.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("static-access")
		String deleteTable = parkControl.nameOfTable(oldBooking.getParkBooked()) + deleteRequest.activeBookings;
		deleteRequest.setTables(Arrays.asList(deleteTable));
		deleteRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
				Arrays.asList(oldBooking.getBookingId()));

		// adding the request to the requests list
		transaction.addRequestToList(deleteRequest);

		// creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("static-access")
		String insertTable = parkControl.nameOfTable(newBooking.getParkBooked()) + insertRequest.activeBookings;
		insertRequest.setTables(Arrays.asList(insertTable));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType", "numberOfVisitors",
						"idNumber", "firstName", "lastName", "emailAddress", "phoneNumber", "finalPrice", "paid",
						"confirmed", "entryParkTime", "exitParkTime", "isRecievedReminder", "reminderArrivalTime"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(),
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
						newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
						newBooking.getFinalPrice(), newBooking.isPaid() == false ? 0 : 1,
						newBooking.isConfirmed() == false ? 0 : 1, newBooking.getEntryParkTime(),
						newBooking.getExitParkTime(), newBooking.isRecievedReminder() == false ? 0 : 1,
						newBooking.getReminderArrivalTime()));

		// adding the request to the requests list
		transaction.addRequestToList(insertRequest);
		// sending the requests list to the server side
		GoNatureClientUI.client.accept(transaction);

		// getting the results from the database
		return transaction.getQueryResult();
	}

	/**
	 * This method gets a park, a date and a time and updates the waiting list for
	 * this time range by deleting reservations that have place now after a booking
	 * is cancelled
	 * 
	 * @param park
	 * @param date
	 * @param time
	 */
	public void updateWaitingList(Park park, LocalDate date, LocalTime time) {
		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(park);

		// getting current capacities and time limit
		int currentCapacity = getCurrentParkCapacities(park, date, time, pair.getValue());
		int moreCanEnter = pair.getKey() - currentCapacity;
		int timeLimit = pair.getValue();

		// getting all the relevant bookings from the waiting list table
		Communication getWaiting = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			getWaiting.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		getWaiting.setTables((Arrays.asList(parkControl.nameOfTable(park))));
		getWaiting.setSelectColumns(Arrays.asList("bookingId"));
		getWaiting.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit", "numberOfVisitors"),
				Arrays.asList("=", "AND", ">", "AND", "<", "AND", "<="),
				Arrays.asList(date, time.minusHours(timeLimit), time.plusHours(timeLimit), moreCanEnter));

		// sending the requests list to the server side
		GoNatureClientUI.client.accept(getWaiting);

		// getting the results from the database
		ArrayList<Booking> waitingResults = new ArrayList<>();
		for (Object[] row : getWaiting.getResultList()) {
			Booking add = new Booking((String) row[0], ((Date) row[1]).toLocalDate(), ((Time) row[2]).toLocalTime(),
					((Date) row[3]).toLocalDate(),
					((String) row[5]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL, (Integer) row[6],
					(String) row[7], (String) row[8], (String) row[9], (String) row[10], (String) row[11], -1, false,
					false, null, null, false, null, park);
			add.setWaitingListPriority((Integer) row[4]);
			add.setFinalPrice(calculateFinalDiscountPrice(add, add.getVisitType() == VisitType.GROUP, false));
		}

		// waitingResults holds all the bookings that CAN be entered in terms of their
		// group size
		waitingResults.sort(Booking.waitingListComparator); // sorting these bookings

		ArrayList<Booking> transferBookings = new ArrayList<>();
		int decreasePriority = 0;
		for (Booking currectBooking : waitingResults) {
			int currentBookingSize = currectBooking.getNumberOfVisitors();
			if (currentBookingSize <= moreCanEnter) {
				moreCanEnter -= currentBookingSize;
				decreasePriority++;

				transferBookings.add(currectBooking);
				waitingResults.remove(currectBooking);
			} else {
				currectBooking.setWaitingListPriority(currectBooking.getWaitingListPriority() - decreasePriority);
			}
		}
	}

	////////////////////////////////////////////////////
	////////////////////////////////////////////////////
	///// METHODS FOR CONTROLLED RESCHEDULE SCREEN /////
	////////////////////////////////////////////////////
	////////////////////////////////////////////////////

	/**
	 * This method is used for showing all the slots available in a specific park
	 * for date range
	 * 
	 * @param parkBooked the relevant park
	 * @param fromDate   starts from
	 * @param toDate     ends in
	 */
	@SuppressWarnings("unused")
	public ArrayList<AvailableSlot> getParkAvailabilitySlots(Booking newBooking, LocalDate fromDate, LocalDate toDate) {
		// setting the data structure
		ArrayList<AvailableSlot> available = new ArrayList<>();
		// running toDate - fromDate times (days)
		for (LocalDate start = fromDate; start.compareTo(toDate) <= 0; start = start.plusDays(1)) {
			// running closeHour - openHours times (hours)
			for (int hour = openHour; hour <= closeHour; hour++) {
				// if the hour has minutes intervals
				if (minutes == 0) {
					AvailableSlot slot = new AvailableSlot(
							LocalDate.of(start.getYear(), start.getMonth(), start.getDayOfMonth()),
							LocalTime.of(hour, 0));
					if (isSpecificTimeAvailable(newBooking.getParkBooked(), slot, newBooking.getNumberOfVisitors())) {
						available.add(slot);
					}
				} else {
					for (int minute = 0; minute < 60; minute += minutes) {
						AvailableSlot slot = new AvailableSlot(
								LocalDate.of(start.getYear(), start.getMonth(), start.getDayOfMonth()),
								LocalTime.of(hour, minute));
						if (isSpecificTimeAvailable(newBooking.getParkBooked(), slot,
								newBooking.getNumberOfVisitors())) {
							available.add(slot);
						}
					}
				}
			}
		}

		return available;
	}

	/**
	 * Thus method gets a time slot to check its availability in a specific park
	 * 
	 * @param parkToCheck
	 * @param slot
	 * @param numberOfVisitors
	 * @return true if available, false if not
	 */
	private boolean isSpecificTimeAvailable(Park parkToCheck, AvailableSlot slot, int numberOfVisitors) {
		// pre-setting data for request
		Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(parkToCheck) + availabilityRequest.activeBookings;
		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(parkToCheck);
		int parkTimeLimit = pair.getValue();

		// creating the request for the availability check
		try {
			availabilityRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		availabilityRequest.setTables(Arrays.asList(parkTableName));
		availabilityRequest.setSelectColumns(Arrays.asList("numberOfVisitors"));
		availabilityRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"), Arrays.asList(slot.getDate(),
						slot.getTime().minusHours(parkTimeLimit), slot.getTime().plusHours(parkTimeLimit)));

		// sending the request to the server side
		GoNatureClientUI.client.accept(availabilityRequest);

		// getting the result from the database
		int countVisitors = 0;
		// checking the orders amount for the specific time
		for (Object[] row : availabilityRequest.getResultList()) {
			countVisitors += (Integer) row[0];
		}
		// checking park parameters
		return pair.getKey() - countVisitors - numberOfVisitors > 0;
	}

	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	///// METHODS FOR CONTROLLED WAITING LIST SCREEN /////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////

	/**
	 * This method gets a new booking details and inserts the booking into the park
	 * booked waiting list table
	 * 
	 * @param newBooking the booking to insert
	 * @return true if the insert query succeed, false if not
	 */
	public boolean insertBookingToWaitingList(Booking newBooking) {
		// checking the waiting list for the specific date and time
		// this is done in order to determine the new booking's waiting list priority
		int bookingPriority = checkBookingWaitingListPriority(newBooking);
		// now, creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(newBooking.getParkBooked()) + insertRequest.waitingList;
		insertRequest.setTables(Arrays.asList(parkTableName));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "waitingListOrder", "visitType",
						"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
						"finalPrice"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(), bookingPriority + 1,
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
						newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
						newBooking.getFinalPrice()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(insertRequest);

		// getting the result from the database
		return insertRequest.getQueryResult();
	}

	/**
	 * This method gets a booking and checks the priority of the waiting list for
	 * the specific park at the specific date and time range
	 * 
	 * @param newBooking
	 * @return the waiting list priority [1...]
	 */
	public int checkBookingWaitingListPriority(Booking newBooking) {
		// creating the request for the waiting list check
		Communication waitingListRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			waitingListRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(newBooking.getParkBooked()) + waitingListRequest.waitingList;
		waitingListRequest.setTables(Arrays.asList(parkTableName));
		waitingListRequest.setSelectColumns(Arrays.asList("dayOfVisit", "timeOfVisit"));

		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(newBooking.getParkBooked());
		int parkTimeLimit = pair.getValue();
		waitingListRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(newBooking.getDayOfVisit(), newBooking.getTimeOfVisit().minusHours(parkTimeLimit),
						newBooking.getTimeOfVisit().plusHours(parkTimeLimit)));
		// sending the request to the server side
		GoNatureClientUI.client.accept(waitingListRequest);

		// getting the result from the database and counting what's the current booking
		// priority
		return waitingListRequest.getResultList().size();
	}

	/**
	 * This method gets a booking and returns its park's current waiting list for
	 * the specific date and time, as an observable list
	 * 
	 * @param newBooking
	 * @return an observable list of the booking in the park's waiting list
	 */
	public ObservableList<Booking> getWaitingListForPark(Booking newBooking) {
		// creating the request for the waiting list check
		Communication waitingListRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			waitingListRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("static-access")
		String parkTableName = parkControl.nameOfTable(newBooking.getParkBooked()) + waitingListRequest.waitingList;
		waitingListRequest.setTables(Arrays.asList(parkTableName));
		waitingListRequest.setSelectColumns(Arrays.asList("bookingId", "timeOfVisit", "dayOfBooking",
				"waitingListOrder", "visitType", "numberOfVisitors"));

		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(newBooking.getParkBooked());
		int parkTimeLimit = pair.getValue();
		waitingListRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(newBooking.getDayOfVisit(), newBooking.getTimeOfVisit().minusHours(parkTimeLimit),
						newBooking.getTimeOfVisit().plusHours(parkTimeLimit)));
		// sending the request to the server side
		GoNatureClientUI.client.accept(waitingListRequest);

		// getting the result from the database and analyzing it
		ArrayList<Booking> waitingListBookings = new ArrayList<>();
		for (Object[] row : waitingListRequest.getResultList()) {
			Booking addToList = new Booking((String) row[0], ((Time) row[1]).toLocalTime(),
					((Date) row[2]).toLocalDate(), (Integer) row[3],
					((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL, (Integer) row[5]);
			waitingListBookings.add(addToList);
		}
		return FXCollections.observableArrayList(waitingListBookings);
	}

	/**
	 * This method gets booking details and deletes the booking from the park
	 * waiting list table
	 * 
	 * @param newBooking the booking to insert
	 * @return true if the insert query succeed, false if not
	 */
	@SuppressWarnings("static-access")
	public boolean deleteBookingFromWaitingList(Booking deleteBooking) {
		// the pair holds the maximum orders amount, and maximum time limit parameters
		Pair<Integer, Integer> pair = getParkUpdatedParameters(deleteBooking.getParkBooked());
		int parkTimeLimit = pair.getValue();

		// first getting all relevant waiting list bookings with lower priority from the
		// booking that about to be deleted
		Communication selectQuery = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			selectQuery.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		String parkTableName = parkControl.nameOfTable(deleteBooking.getParkBooked()) + selectQuery.waitingList;
		selectQuery.setTables(Arrays.asList(parkTableName));
		selectQuery.setSelectColumns(Arrays.asList("bookingId", "waitingListOrder"));
		selectQuery.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit", "waitingListOrder"),
				Arrays.asList("=", "AND", ">", "AND", "<", "AND", ">"),
				Arrays.asList(deleteBooking.getDayOfVisit(), deleteBooking.getTimeOfVisit().minusHours(parkTimeLimit),
						deleteBooking.getTimeOfVisit().plusHours(parkTimeLimit),
						deleteBooking.getWaitingListPriority()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(selectQuery);

		ArrayList<Pair<String, Integer>> idNumbers = new ArrayList<>();
		for (Object[] row : selectQuery.getResultList()) {
			idNumbers.add(new Pair<String, Integer>((String) row[0], (Integer) row[1]));
		}

		// creating the request for the booking deletion
		// the request is a transaction: first deleting the booking
		// and then updating all higher-priority waiting list bookings' priorities
		Communication transaction = new Communication(CommunicationType.TRANSACTION);

		// creating the delete request
		Communication deleteRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			deleteRequest.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		deleteRequest.setTables(Arrays.asList(parkTableName));
		deleteRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
				Arrays.asList(deleteBooking.getBookingId()));

		// adding the delete request to the transaction
		transaction.addRequestToList(deleteRequest);

		for (Pair<String, Integer> row : idNumbers) {
			Communication updateRequest = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				updateRequest.setQueryType(QueryType.UPDATE);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			updateRequest.setTables(Arrays.asList(parkTableName));
			updateRequest.setColumnsAndValues(Arrays.asList("waitingListOrder"), Arrays.asList(row.getValue() - 1));
			updateRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
					Arrays.asList(row.getKey()));

			// adding the update request to the transaction
			transaction.addRequestToList(updateRequest);
		}

		// sending the request to the server side
		GoNatureClientUI.client.accept(transaction);

		// getting the result from the database
		return transaction.getQueryResult();
	}

	///////////////
	///////////////
	/// GETTERS ///
	///////////////
	///////////////

	/**
	 * @return the saved booking
	 */
	public Booking getBooking() {
		return booking;
	}

	/**
	 * @return the saved park index in the combobox
	 */
	public int getParkIndexInCombobox() {
		return parkIndexInCombobox;
	}

	/**
	 * @return true if there is a saved state currently
	 */
	public boolean isSavedState() {
		return isSavedState;
	}

	/**
	 * @return the saved park visitor
	 */
	public ParkVisitor getVisitor() {
		return visitor;
	}

	/**
	 * @return the saved observable lists pair
	 */
	public Pair<ObservableList<Booking>, ObservableList<Booking>> getPair() {
		return pair;
	}

	///////////////
	///////////////
	/// SETTERS ///
	///////////////
	///////////////

	/**
	 * @param booking the booking to be saved
	 */
	public void setBooking(Booking booking) {
		this.booking = booking;
	}

	/**
	 * @param parkIndexInCombobox the park index to be saved
	 */
	public void setParkIndexInCombobox(int parkIndexInCombobox) {
		this.parkIndexInCombobox = parkIndexInCombobox;
	}

	/**
	 * @param isSavedState if there is a saving state currently
	 */
	public void setSavedState(boolean isSavedState) {
		this.isSavedState = isSavedState;
		if (!isSavedState) {
			removeSavedData();
		}
	}

	/**
	 * @param visitor the park visitor to be saved
	 */
	public void setVisitor(ParkVisitor visitor) {
		this.visitor = visitor;
	}

	/**
	 * resets the saved data for later use
	 */
	private void removeSavedData() {
		booking = null;
		parkIndexInCombobox = -1;
		visitor = null;
	}

	/**
	 * 
	 * @param pair of observable lists
	 */
	public void setPair(Pair<ObservableList<Booking>, ObservableList<Booking>> pair) {
		this.pair = pair;
	}

}
