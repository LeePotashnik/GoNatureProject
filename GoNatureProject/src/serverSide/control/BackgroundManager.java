package serverSide.control;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.control.ParkController;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.ServerMessageType;
import common.communication.CommunicationException;
import entities.Booking;
import entities.Booking.VisitType;
import entities.Park;
import serverSide.gui.GoNatureServerUI;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

public class BackgroundManager {
//	private final ScheduledExecutorService scheduler;
//	private int numberOfTasks = 4;
//	private NotificationsController notifications;
	private DatabaseController database;
	private ParkController parkControl;
	private ArrayList<Park> parks;

	public BackgroundManager(DatabaseController database) {
//		scheduler = Executors.newScheduledThreadPool(numberOfTasks);
//		notification = new NotificationsController();
		this.database = database;
		parkControl = ParkController.getInstance();
	}

//	public void startBackgroundOperations() {
//		scheduler.scheduleAtFixedRate(this::updateWaitingLists, 0, 1, TimeUnit.HOURS);
//		scheduler.scheduleAtFixedRate(this::updateActiveTables, 0, 1, TimeUnit.HOURS);
//		scheduler.scheduleAtFixedRate(this::sendReminders, 0, 1, TimeUnit.HOURS);
//		scheduler.scheduleAtFixedRate(this::checkReminders, 0, 1, TimeUnit.HOURS);
//
//	}

	@SuppressWarnings("static-access")
	public void updateWaitingLists() throws DatabaseException {
		// fetching parks information from the database
		fetchParks();

		// setting auto commit of the database to false
		try {
			database.toggleAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// creating a conquer request
		Communication conquer = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
		conquer.setServerMessageType(ServerMessageType.CONQUER);
		conquer.setServerMessageContent("Updating Information.\nThis could take several seconds...");
		GoNatureServerUI.server.sendToAllClients(conquer);

		// executing queries
		for (Park park : parks) {
			// creating the select check for this specific park
			Communication checkWaitingBookings = new Communication(CommunicationType.SELF);
			try {
				checkWaitingBookings.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			String tableName = parkControl.nameOfTable(park) + checkWaitingBookings.waitingList;
			checkWaitingBookings.setTables(Arrays.asList(tableName));
			checkWaitingBookings.setSelectColumns(Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit"));
			checkWaitingBookings.setWhereConditions(Arrays.asList("dayOfVisit"), Arrays.asList("<="),
					Arrays.asList(LocalDate.now()));

			// getting the result from the database
			// all bookings in the waiting list table of this park
			// that their date has passed AND their hour has passed also
			ArrayList<Object[]> results = database.executeSelectQuery(checkWaitingBookings);
			ArrayList<String> idNumbers = new ArrayList<>();
			LocalDate today = LocalDate.now();
			LocalTime now = LocalTime.now();

			for (Object[] row : results) {
				LocalDate visitDate = ((Date) row[1]).toLocalDate();
				LocalTime visitTime = ((Time) row[2]).toLocalTime();
				boolean isTimeBeforeNow = visitTime.compareTo(now) <= 0;

				if ((visitDate.isEqual(today) && isTimeBeforeNow) || visitDate.isBefore(today)) {
					String idNumber = (String) row[0];
					idNumbers.add(idNumber);
				}
			}

			// in case there are bookings to delete, creating a request to delete them
			if (!idNumbers.isEmpty()) {
				Communication deleteBookings = new Communication(CommunicationType.SELF);
				try {
					deleteBookings.setQueryType(QueryType.DELETE);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
				deleteBookings.setTables(Arrays.asList(tableName));

				String value = "(";
				// creating the booking ids values
				for (int i = 0; i < idNumbers.size(); i++) {
					value += "'" + idNumbers.get(i) + "'";
					if (i + 1 < idNumbers.size())
						value += ", ";
				}
				value += ")";
				deleteBookings.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("IN"),
						Arrays.asList(value));

				boolean deleteResult = database.executeDeleteQuery(deleteBookings);
				if (!deleteResult) {
					throw new DatabaseException("Problem with DELETE query");
				}
			}
		}

		// commiting all the waiting queries
		try {
			database.commit();
		} catch (SQLException e) {
			try {
				// if a problem occures, rolling back all queries
				database.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			// toggeling auto commit to allow other transactions
			database.toggleAutoCommit(true);
		} catch (SQLException e1) {
			e1.printStackTrace();

		}
	}

	@SuppressWarnings("static-access")
	public void updateActiveTables() throws DatabaseException {
		// fetching parks information from the database
		fetchParks();

		// setting auto commit of the database to false
		try {
			database.toggleAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// creating a conquer request
		Communication conquer = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
		conquer.setServerMessageType(ServerMessageType.CONQUER);
		conquer.setServerMessageContent("Updating Information.\nThis could take several seconds...");
		GoNatureServerUI.server.sendToAllClients(conquer);

		// executing queries
		for (Park park : parks) {
			// creating the select check for this specific park
			Communication checkWaitingBookings = new Communication(CommunicationType.SELF);
			try {
				checkWaitingBookings.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			String tableName = parkControl.nameOfTable(park) + checkWaitingBookings.activeBookings;
			checkWaitingBookings.setTables(Arrays.asList(tableName));
			checkWaitingBookings.setSelectColumns(Arrays.asList("*"));
			checkWaitingBookings.setWhereConditions(Arrays.asList("dayOfVisit"), Arrays.asList("<="),
					Arrays.asList(LocalDate.now()));

			// getting the result from the database
			// all bookings in the active bookings table of this park
			// that their date has passed AND their hour has passed also, and they did not
			// arrive to the park
			ArrayList<Object[]> results = database.executeSelectQuery(checkWaitingBookings);
			ArrayList<String> idNumbers = new ArrayList<>();
			ArrayList<Booking> cancelledBookings = new ArrayList<>();
			LocalDate today = LocalDate.now();
			LocalTime now = LocalTime.now();

			for (Object[] row : results) {
				LocalDate visitDate = ((Date) row[1]).toLocalDate();
				LocalTime visitTime = ((Time) row[2]).toLocalTime();
				boolean isTimeBeforeNow = visitTime.compareTo(now) <= 0;
				boolean areTimesNull = (Time) row[14] == null && (Time) row[15] == null;

				if ((visitDate.isEqual(today) && isTimeBeforeNow && areTimesNull) || visitDate.isBefore(today)) {
					String idNumber = (String) row[0];
					idNumbers.add(idNumber);
					cancelledBookings.add(new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
							((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
							((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
							(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
							(String) row[10], (Integer) row[11], (Integer) row[12] == 0 ? false : true,
							(Integer) row[13] == 0 ? false : true,
							((Time) row[14]) == null ? null : ((Time) row[14]).toLocalTime(),
							((Time) row[15]) == null ? null : ((Time) row[15]).toLocalTime(),
							(Integer) row[16] == 0 ? false : true,
							((Time) row[17]) == null ? null : ((Time) row[17]).toLocalTime(), park));
				}
			}

			// in case there are bookings to delete, creating a request to delete them
			if (!idNumbers.isEmpty()) {
				Communication deleteBookings = new Communication(CommunicationType.SELF);
				try {
					deleteBookings.setQueryType(QueryType.DELETE);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
				deleteBookings.setTables(Arrays.asList(tableName));

				String value = "(";
				// creating the booking ids values
				for (int i = 0; i < idNumbers.size(); i++) {
					value += "'" + idNumbers.get(i) + "'";
					if (i + 1 < idNumbers.size())
						value += ", ";
				}
				value += ")";
				deleteBookings.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("IN"),
						Arrays.asList(value));

				boolean deleteResult = database.executeDeleteQuery(deleteBookings);
				if (!deleteResult) {
					throw new DatabaseException("Problem with DELETE query");
				}
			}

			// if there were bookings to delete, inserting them to the cancelled table
			if (!cancelledBookings.isEmpty()) {
				for (Booking cancelledBooking : cancelledBookings) {
					// creating a communication request
					Communication insertCancelled = new Communication(CommunicationType.SELF);
					try {
						insertCancelled.setQueryType(QueryType.INSERT);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					insertCancelled.setTables(
							Arrays.asList(parkControl.nameOfTable(park) + insertCancelled.cancelledBookings));
					insertCancelled.setColumnsAndValues(
							Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
									"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress",
									"phoneNumber", "cancellationReason"),
							Arrays.asList(cancelledBooking.getBookingId(), cancelledBooking.getDayOfVisit(),
									cancelledBooking.getTimeOfVisit(), cancelledBooking.getDayOfBooking(),
									cancelledBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
									cancelledBooking.getNumberOfVisitors(), cancelledBooking.getIdNumber(),
									cancelledBooking.getFirstName(), cancelledBooking.getLastName(),
									cancelledBooking.getEmailAddress(), cancelledBooking.getPhoneNumber(), "Did not arrive"));
					
					// sending the request to the database
					boolean insertResult = database.executeInsertQuery(insertCancelled);
					if (!insertResult) {
						throw new DatabaseException("Problem with INSERT query");
					}
				}
			}
		}

		// commiting all the waiting queries
		try {
			database.commit();
		} catch (SQLException e) {
			try {
				// if a problem occures, rolling back all queries
				database.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			// toggeling auto commit to allow other transactions
			database.toggleAutoCommit(true);
		} catch (SQLException e1) {
			e1.printStackTrace();

		}
	}

	private void sendReminders() {
		
	}

//	private void checkReminders() {
//
//	}

	/**
	 * This method is called in order to insert parks details into the parks array
	 * list property
	 */
	private void fetchParks() {
		// creating a communication instance for fetching the up to date parks
		Communication getParks = new Communication(CommunicationType.SELF);
		try {
			getParks.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		getParks.setTables(Arrays.asList(Communication.park));
		getParks.setSelectColumns(Arrays.asList("*"));

		// executing the SELECT query
		ArrayList<Object[]> results = database.executeSelectQuery(getParks);

		// getting the result
		if (parks != null) {
			if (!parks.isEmpty()) {
				parks.removeAll(parks);
			}
		} else {
			parks = new ArrayList<>();
		}
		if (parks != null) {
			// setting the Object[] from DB to the parkList
			for (Object[] row : results) {
				Park parkToAdd = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3],
						(String) row[4], (String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8],
						(Integer) row[9], (Integer) row[10]);
				parks.add(parkToAdd);
			}
		}
	}

	// method for checking every time interval: all the park's active tables:
	// if the booking's time +
}
