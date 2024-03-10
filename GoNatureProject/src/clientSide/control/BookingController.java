package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
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
	public boolean checkAvailability(Booking booking) {
		// pre-setting data for request
		Communication availabilityRequest = new Communication(CommunicationType.QUERY_REQUEST);
		Park parkOfBooking = booking.getParkBooked();
		String parkTableName = ParkController.getInstance().nameOfTable(parkOfBooking) + availabilityRequest.activeBookings;
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
		return parkOfBooking.getMaximumOrders() - countVisitors - numberOfVisitors > 0;
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
	public boolean insertNewBooking(Booking newBooking, ParkVisitor booker) {
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
						"firstName", "lastName", "emailAddress", "phoneNumber", "finalPrice", "paid", "confirmed",
						"entryParkTime", "exitParkTime", "isRecievedReminder", "reminderArrivalTime"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(),
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), booker.getFirstName(), booker.getLastName(),
						booker.getEmailAddress(), booker.getPhoneNumber(), newBooking.getFinalPrice(),
						newBooking.isPaid() == false ? 0 : 1, newBooking.isConfirmed() == false ? 0 : 1,
						newBooking.getEntryParkTime(), newBooking.getExitParkTime(),
						newBooking.isRecievedReminder() == false ? 0 : 1, newBooking.getReminderArrivalTime()));

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
	public boolean insertToWaitingList(Booking newBooking, ParkVisitor booker) {
		// first checking the waiting list for the specific date and time
		// this is done in order to determine the new booking's waiting list priority

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
		int bookingPriority = waitingListRequest.getResultList().size();

		// now, creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		insertRequest.setTables(Arrays.asList(parkTableName));
		insertRequest.setColumnsAndValues(
				Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "waitingListOrder", "visitType",
						"numberOfVisitors", "firstName", "lastName", "emailAddress", "phoneNumber"),
				Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
						newBooking.getDayOfBooking(), bookingPriority + 1,
						newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
						newBooking.getNumberOfVisitors(), booker.getFirstName(), booker.getLastName(),
						booker.getEmailAddress(), booker.getPhoneNumber()));

		// sending the request to the server side
		GoNatureClientUI.client.accept(insertRequest);

		// getting the result from the database
		return insertRequest.getQueryResult();
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
	
	public boolean cancelBooking(Booking deleteBooking) {
		return true;
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
