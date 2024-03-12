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
import common.communication.CommunicationException;
import entities.Booking;
import entities.Booking.VisitType;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class BookingController {
	private static BookingController instance;
	ArrayList<Park> parkList;

	// date validation parameters
	/**
	 * What is the future booking range, in months. Default: 4 months
	 */
	public final int futureBookingsRange = 4;
	/**
	 * opening hour for the parks. Default: 8AM
	 */
	public final int openHour = 8;
	/**
	 * closing hour for the parks. Default: 18PM + Park's time limit
	 */
	public final int closeHour = 18;
	/**
	 * sets the minutes of the available hours. Default: 0, which means reservations
	 * can be made in jumpings of one hour
	 */
	public final int minutes = 0;
	/**
	 * minimum visitors in reservation
	 */
	public final int minimumVisitorsInReservation = 1;
	/**
	 * maximum visitors in reservation
	 */
	public final int maximumVisitorsInReservation = 15;

	// for saving and restoring purposes of the screen
	private Booking booking;
	private int parkIndexInCombobox;
	private ParkVisitor visitor;
	private boolean isSavedState = false;

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

	/// COMMUNICATION METHODS ///

	/**
	 * This method requests parks details from the database and returns a pair of
	 * observable lists of the parks, one of Park and the second of String
	 */
	public Pair<ObservableList<Park>, ObservableList<String>> fetchParks() {
		parkList = ParkController.getInstance().fetchParks();
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
	 * This method checks the availability for a specific booking, by checking the
	 * specific park parameters and active bookings ob the same date and time range
	 * of the checked order
	 * 
	 * @param booking the booking that is checked
	 * @return true if there's enough place for this group, false if not
	 */
	public boolean checkParkAvailabilityForBooking(Booking booking) {
		// pre-setting data for request
		Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
		Park parkOfBooking = booking.getParkBooked();
		String parkTableName = ParkController.getInstance().nameOfTable(parkOfBooking)
				+ availabilityRequest.activeBookings;
		int parkTimeLimit = parkOfBooking.getTimeLimit();
		int numberOfVisitors = booking.getNumberOfVisitors();

		// creating the request for the availability check
		try {
			availabilityRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		availabilityRequest.setTables(Arrays.asList(parkTableName));
		availabilityRequest.setSelectColumns(Arrays.asList("numberOfVisitors"));
		availabilityRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(booking.getDayOfVisit(), booking.getTimeOfVisit().minusHours(parkTimeLimit),
						booking.getTimeOfVisit().plusHours(parkTimeLimit)));

		// sending the request to the server side
		GoNatureClientUI.client.accept(availabilityRequest);

		// getting the result from the database
		int countVisitors = 0;
		// checking the orders amount for the specific time
		for (Object[] row : availabilityRequest.getResultList()) {
			countVisitors += (Integer) row[0];
		}
		// checking park parameters
		return parkOfBooking.getMaximumOrders() - countVisitors - numberOfVisitors >= 0;
	}

	/**
	 * This method connects with the PaymentController in order to calculate the
	 * price for the group with the most updated prices, without discount
	 * 
	 * @param newBooking
	 * @param visitorType
	 * @return the calculated price without discount
	 */
	public int calculateFinalRegularPrice(Booking newBooking, VisitorType visitorType) {
		if (visitorType == VisitorType.TRAVELLER) {
//			return PaymentController.getInstance().calculateRegularPriceTravelersGroup(newBooking);
		} else {
//			return PaymentController.getInstance().calculateRegularPriceGuidedGroup(newBooking);
		}
		// for now:
		return newBooking.getNumberOfVisitors() * 50;
	}

	/**
	 * This method connects with the PaymentController in order to calculate the
	 * price for the group with the most updated prices, with discounts
	 * 
	 * @param newBooking
	 * @param visitorType
	 * @return the calculated price with discount
	 */
	public int calculateFinalDiscountPrice(Booking newBooking, VisitorType visitorType) {
		if (visitorType == VisitorType.TRAVELLER) {
//			return PaymentController.getInstance().calculateDiscountPriceTravelersGroup(newBooking);
		} else {
//			return PaymentController.getInstance().calculateDiscountPriceGuidedGroup(newBooking);
		}
		// for now:
		return (int) (newBooking.getNumberOfVisitors() * 50 * 0.9);
	}

	/**
	 * This method gets a new booking and the booker details and inserts the booking
	 * into the park booked active bookings table
	 * 
	 * @param newBooking the booking to insert
	 * @param booker     the booker of the booking
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
		String parkTableName = ParkController.getInstance().nameOfTable(newBooking.getParkBooked())
				+ insertRequest.activeBookings;
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
	 * This method gets a new booking and the booker details and inserts the booking
	 * into the park booked waiting list table
	 * 
	 * @param newBooking the booking to insert
	 * @param booker     the booker of the booking
	 * @return true if the insert query succeed, false if not
	 */
	public boolean insertBookingToWaitingList(Booking newBooking) {
		System.out.println(newBooking.toString());
		System.out.println(newBooking.getTimeOfVisit() + " " + newBooking.getDayOfBooking());
		System.out
				.println(Time.valueOf(newBooking.getTimeOfVisit()) + " " + Date.valueOf(newBooking.getDayOfBooking()));
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
		String parkTableName = ParkController.getInstance().nameOfTable(newBooking.getParkBooked())
				+ insertRequest.waitingList;
		insertRequest.setTables(Arrays.asList(parkTableName));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "waitingListOrder", "visitType",
						"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(), bookingPriority + 1,
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
						newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber()));

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
		String parkTableName = ParkController.getInstance().nameOfTable(newBooking.getParkBooked())
				+ waitingListRequest.waitingList;
		waitingListRequest.setTables(Arrays.asList(parkTableName));
		waitingListRequest.setSelectColumns(Arrays.asList("dayOfVisit", "timeOfVisit"));
		int parkTimeLimit = newBooking.getParkBooked().getTimeLimit();
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
		String parkTableName = ParkController.getInstance().nameOfTable(newBooking.getParkBooked())
				+ waitingListRequest.waitingList;
		waitingListRequest.setTables(Arrays.asList(parkTableName));
		waitingListRequest.setSelectColumns(Arrays.asList("bookingId", "timeOfVisit", "dayOfBooking",
				"waitingListOrder", "visitType", "numberOfVisitors"));
		int parkTimeLimit = newBooking.getParkBooked().getTimeLimit();
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

		System.out.println(Time.valueOf(LocalTime.now()));
		System.out.println(LocalTime.now());
		System.out.println(Date.valueOf(LocalDate.now()));

		return FXCollections.observableArrayList(waitingListBookings);
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
		String parkTableName = ParkController.getInstance().nameOfTable(updateBooking.getParkBooked())
				+ updateRequest.activeBookings;
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

	/**
	 * This method is called for deleteing a booking from the active booking table
	 * of the specific park
	 * 
	 * @param deleteBooking
	 * @return true if the deletion s
	 */
	public boolean deleteBookingFromActiveTable(Booking deleteBooking) {
		// creating the request for the booking deletion
		Communication deleteRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			deleteRequest.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		String parkTableName = ParkController.getInstance().nameOfTable(deleteBooking.getParkBooked())
				+ deleteRequest.activeBookings;
		deleteRequest.setTables(Arrays.asList(parkTableName));
		deleteRequest.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
				Arrays.asList(deleteBooking.getBookingId()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(deleteRequest);

		// getting the result from the database
		return deleteRequest.getQueryResult();
	}

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
					for (int minute = 0; minute < (60); minute += minutes) {
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

	private boolean isSpecificTimeAvailable(Park parkToCheck, AvailableSlot slot, int numberOfVisitors) {
		// pre-setting data for request
		Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
		String parkTableName = ParkController.getInstance().nameOfTable(parkToCheck)
				+ availabilityRequest.activeBookings;
		int parkTimeLimit = parkToCheck.getTimeLimit();

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
		return parkToCheck.getMaximumOrders() - countVisitors - numberOfVisitors > 0;
	}

	/// GETTERS ///
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

	/// SETTERS ///
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

}
