package serverSide.control;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import clientSide.control.ParkController;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.entities.Booking;
import common.entities.Park;
import common.entities.Booking.VisitType;
import common.communication.CommunicationException;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

/**
 * This class manages all the background operations that are performed
 * repeatedly. Also has some methods that are executed after a communication
 * from the client side requires them to perform
 */
public class BackgroundManager {
	private final ScheduledExecutorService scheduler;
	private NotificationsController notifications = NotificationsController.getInstance();
	private DatabaseController database;
	private ParkController parkControl = ParkController.getInstance();
	private ArrayList<Park> parks = new ArrayList<>();
	public static int reminderCancellationTime = 2; // can be updated for future development
	private static int minutesGapOfBookingTimes = 0; // can be updated for future development
	public static int reminderSendBeforeTime = 24; // can be updated for future development
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
	 * Constructor
	 * 
	 * @param database the initialized database instance of the runtime
	 */
	public BackgroundManager(DatabaseController database) {
		scheduler = Executors.newScheduledThreadPool(1);
		this.database = database;
	}

	//////////////////////////////////
	/// START BACKGROUND OPERATION ///
	//////////////////////////////////

	/**
	 * This method is called (once) and starts all background operations of the
	 * runtime on the application, every 1 hour (as default, can be changed in the
	 * "minutesGapOfBookingTimes" property)
	 */
	public void startBackgroundOperations() {
		// if the operation started not on a "full" hour, setting a delay for the first
		// operation
		long delay;
		if (LocalTime.now().getMinute() != 0) {
			delay = 60 - LocalTime.now().getMinute();
		} else {
			delay = 0;
		}

		// executing the scheduler
		scheduler.scheduleAtFixedRate(() -> {
			// executing the waiting lists background updates
			waitingListsBackgroundRemovalUpdates();

			// executing the active tables background updates
			activeTablesBackgroundRemovalUpdates();

			// executing the reminders sendings background process
			remindersSendingBackground();

			// executing the reminders checking background process
			remindersCheckingBackground();

		}, delay, minutesGapOfBookingTimes == 0 ? 60 : 60 / minutesGapOfBookingTimes, TimeUnit.MINUTES);
	}

	////////////////////////////////////////////////
	/// WAITING LISTS BACKGROUND REMOVAL UPDATES ///
	////////////////////////////////////////////////

	/**
	 * This method is a background method executed repeatedly by a background thread
	 * for clearing old bookings from waiting lists of the parks. These bookings are
	 * bookings that their time and date of arrival have passed and no spot has
	 * found for them in the requested park. The process of this method is done as a
	 * transaction where all waiting list tables are scanned and relevant bookings
	 * are deleted (not transferred to another table, but deleted from the
	 * database).
	 * 
	 * @throws DatabaseException if there is a problem with the transaction
	 */
	@SuppressWarnings("static-access")
	private void waitingListsBackgroundRemovalUpdates() {
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Starting waiting list updates background operations");

		// fetching parks information from the database
		fetchParks();

		// setting auto commit of the database to false
		try {
			database.toggleAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
					System.out.println("\n//////////////////////////////////////////////////");
					System.out.println("EXECUTING THE FOLLOWING QUERY FAILED:");
					try {
						System.out.println(deleteBookings.combineQuery());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					System.out.println("//////////////////////////////////////////////////\n");

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

		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Ending waiting list updates background operations");
	}

	////////////////////////////////////////
	/// ACTIVE TABLES BACKGROUND UPDATES ///
	////////////////////////////////////////

	/**
	 * This method is a background method executed repeatedly by a background thread
	 * for transferring active bookings from the active bookings tables of the
	 * parks. These bookings are bookings that have confirmed their arrival in the
	 * reminder sent to them, but did not show up at the park entrance at the day of
	 * visit. The bookings are deleted and transferred to the cancelled bookings
	 * table of each park.
	 * 
	 * @throws DatabaseException
	 */
	@SuppressWarnings("static-access")
	private void activeTablesBackgroundRemovalUpdates() {
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Starting active tables updates background operations");

		// fetching parks information from the database
		fetchParks();

		// setting auto commit of the database to false
		try {
			database.toggleAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// executing queries
		for (Park park : parks) {
			// creating the select check for this specific park
			Communication checkBookings = new Communication(CommunicationType.SELF);
			try {
				checkBookings.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			String tableName = parkControl.nameOfTable(park) + checkBookings.activeBookings;
			checkBookings.setTables(Arrays.asList(tableName));
			checkBookings.setSelectColumns(Arrays.asList("*"));
			checkBookings.setWhereConditions(Arrays.asList("dayOfVisit"), Arrays.asList("<="),
					Arrays.asList(LocalDate.now()));

			// getting the result from the database
			// all bookings in the active bookings table of this park
			// that their date has passed AND their hour has passed also, and they did not
			// arrive to the park
			ArrayList<Object[]> results = database.executeSelectQuery(checkBookings);
			ArrayList<String> idNumbers = new ArrayList<>();
			ArrayList<Booking> cancelledBookings = new ArrayList<>();

			for (Object[] row : results) {
				// for each booking, checking if its visit ending time has passed
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime visitTime = LocalDateTime.of(((Date) row[1]).toLocalDate(),
						(((Time) row[2]).toLocalTime()));
				visitTime = visitTime.plusHours(park.getTimeLimit()); // adding the time limit
				boolean isAfter = now.isAfter(visitTime);
				boolean areTimesNull = (Time) row[14] == null && (Time) row[15] == null;

				if (isAfter && areTimesNull) { // adding the booking to the cancellations list
					String idNumber = (String) row[0];
					idNumbers.add(idNumber);
					Booking toBeDeleted = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
							((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
							((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
							(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
							(String) row[10], (Integer) row[11], (Integer) row[12] == 0 ? false : true,
							(Integer) row[13] == 0 ? false : true,
							((Time) row[14]) == null ? null : ((Time) row[14]).toLocalTime(),
							((Time) row[15]) == null ? null : ((Time) row[15]).toLocalTime(),
							(Integer) row[16] == 0 ? false : true,
							((Time) row[17]) == null ? null : ((Time) row[17]).toLocalTime(), park);
					cancelledBookings.add(toBeDeleted);

					// sending cancellation notification
					notifications.sendCancellationEmailNotification(Arrays.asList(toBeDeleted.getEmailAddress(),
							toBeDeleted.getPhoneNumber(), toBeDeleted.getParkBooked().getParkName() + " Park",
							toBeDeleted.getDayOfVisit(), toBeDeleted.getTimeOfVisit(),
							toBeDeleted.getFirstName() + " " + toBeDeleted.getLastName(),
							toBeDeleted.getParkBooked().getParkCity() + ", "
									+ toBeDeleted.getParkBooked().getParkState(),
							toBeDeleted.getNumberOfVisitors(), toBeDeleted.getFinalPrice(), toBeDeleted.isPaid()),
							"Visitor did not arrive to the park.");
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
					System.out.println("\n//////////////////////////////////////////////////");
					System.out.println("EXECUTING THE FOLLOWING QUERY FAILED:");
					try {
						System.out.println(deleteBookings.combineQuery());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					System.out.println("//////////////////////////////////////////////////\n");
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
									cancelledBooking.getEmailAddress(), cancelledBooking.getPhoneNumber(),
									"Did not arrive"));

					// sending the request to the database
					boolean insertResult = database.executeInsertQuery(insertCancelled);
					if (!insertResult) {
						System.out.println("\n//////////////////////////////////////////////////");
						System.out.println("EXECUTING THE FOLLOWING QUERY FAILED:");
						try {
							System.out.println(insertCancelled.combineQuery());
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
						System.out.println("//////////////////////////////////////////////////\n");
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

		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Ending active tables updates background operations");
	}

	////////////////////////////////////////////
	/// REMINDERS SENDING BACKGROUND PROCESS ///
	////////////////////////////////////////////

	/**
	 * This method is a background method executed repeatedly by a background thread
	 * for scanning all active bookings tables of all parks. The scan is checking
	 * which bookings are going to occur in 24 hours from now. These bookings will
	 * be recieving a reminder for their booking.
	 */
	@SuppressWarnings("static-access")
	private void remindersSendingBackground() {
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Starting reminders sending background operations");

		ArrayList<ArrayList<Booking>> parksBookings = new ArrayList<>();

		// for each park, scanning its active bookings table
		for (Park park : parks) {
			ArrayList<Booking> toBeReminded = new ArrayList<>();

			// creating the communication request
			Communication checkPark = new Communication(CommunicationType.SELF);
			try {
				checkPark.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			String parkTableName = parkControl.nameOfTable(park) + checkPark.activeBookings;
			checkPark.setTables(Arrays.asList(parkTableName));
			checkPark.setSelectColumns(Arrays.asList("*"));

			checkPark.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit", "isRecievedReminder"),
					Arrays.asList(">=", "AND", "<=", "AND", "="),
					Arrays.asList(LocalDate.now(), LocalDate.now().plusDays(1), 0));

			// executing the request
			ArrayList<Object[]> results = database.executeSelectQuery(checkPark);

			// adding all the returned bookings
			for (Object[] row : results) {
				Booking addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
						((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
						((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL, (Integer) row[5],
						(String) row[6], (String) row[7], (String) row[8], (String) row[9], (String) row[10],
						(Integer) row[11], (Integer) row[12] == 0 ? false : true, (Integer) row[13] == 0 ? false : true,
						((Time) row[14]) == null ? null : ((Time) row[14]).toLocalTime(),
						((Time) row[15]) == null ? null : ((Time) row[15]).toLocalTime(),
						(Integer) row[16] == 0 ? false : true,
						((Time) row[17]) == null ? null : ((Time) row[17]).toLocalTime(), park);

				// checking if the booking is going to occur in 24 hours (or less) from now
				LocalDateTime bookingDate = LocalDateTime.of(addBooking.getDayOfVisit(), addBooking.getTimeOfVisit());
				LocalDateTime now = LocalDateTime.now();

				if (Math.abs(Duration.between(bookingDate, now).toHours()) <= reminderSendBeforeTime) {
					toBeReminded.add(addBooking);
				}
			}

			parksBookings.add(toBeReminded);
		}

		// sending reminders to each booking's holder
		int parkIndex = 0;
		for (ArrayList<Booking> remindingBookings : parksBookings) {

			// sending reminders with the notifications controller
			for (Booking toRemind : remindingBookings) {
				notifications.sendReminderEmailNotification(
						Arrays.asList(toRemind.getEmailAddress(), toRemind.getPhoneNumber(),
								toRemind.getParkBooked().getParkName() + " Park", toRemind.getDayOfVisit(),
								toRemind.getTimeOfVisit(), toRemind.getFirstName() + " " + toRemind.getLastName(),
								toRemind.getParkBooked().getParkCity() + ", " + toRemind.getParkBooked().getParkState(),
								toRemind.getNumberOfVisitors(), toRemind.getFinalPrice(), toRemind.isPaid()));
			}

			// creating the IN (...) part to the query, to update the visitor has been
			// reminded
			int size = remindingBookings.size();
			String bookingIDs = "(";
			// creating the booking ids values
			for (int i = 0; i < size; i++) {
				bookingIDs += "'" + remindingBookings.get(i).getBookingId() + "'";
				if (i + 1 < size)
					bookingIDs += ", ";
			}
			bookingIDs += ")";

			// creating the communication request
			Communication updateReminded = new Communication(CommunicationType.SELF);
			try {
				updateReminded.setQueryType(QueryType.UPDATE);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			updateReminded.setTables(
					Arrays.asList(parkControl.nameOfTable(parks.get(parkIndex++)) + updateReminded.activeBookings));
			updateReminded.setColumnsAndValues(Arrays.asList("isRecievedReminder", "reminderArrivalTime"),
					Arrays.asList(1, LocalTime.now()));
			updateReminded.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("IN"),
					Arrays.asList(bookingIDs));

			if (size > 0) {
				database.executeUpdateQuery(updateReminded);
			}
		}

		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Ending reminders sending background operations");
	}

	/////////////////////////////////////////////
	/// REMINDERS CHECKING BACKGROUND PROCESS ///
	/////////////////////////////////////////////

	/**
	 * This method is a background method executed repeatedly by a background thread
	 * for scanning all active bookings tables of all parks. The scan is checking
	 * which bookings have recieved a reminder but did not confirmed it within 2
	 * hours after sending. These bookings will be transferred from the active
	 * booking table to the cancelled bookings table of each park.
	 */
	@SuppressWarnings("static-access")
	private void remindersCheckingBackground() {
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Starting reminders checking background operations");

		ArrayList<ArrayList<Booking>> transferring = new ArrayList<>();

		// for each park, scanning its active bookings table
		for (Park park : parks) {
			ArrayList<Booking> toBeTransferred = new ArrayList<>();

			// creating the communication request
			Communication checkPark = new Communication(CommunicationType.SELF);
			try {
				checkPark.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			String parkTableName = parkControl.nameOfTable(park) + checkPark.activeBookings;
			checkPark.setTables(Arrays.asList(parkTableName));
			checkPark.setSelectColumns(Arrays.asList("*"));
			checkPark.setWhereConditions(Arrays.asList("confirmed"), Arrays.asList("="), Arrays.asList(0));

			// executing the request
			ArrayList<Object[]> results = database.executeSelectQuery(checkPark);

			// checking all the returned bookings
			for (Object[] row : results) {
				// checking the reminder sending time
				if ((Time) row[17] != null) {
					LocalTime reminderSentTime = ((Time) row[17]).toLocalTime();
					LocalDate remiderSentDate = reminderSentTime.compareTo(LocalTime.of(22, 00)) >= 0
							&& reminderSentTime.compareTo(LocalTime.of(23, 59)) <= 0 ? LocalDate.now().minusDays(1)
									: LocalDate.now();
					// checking the gap
					LocalDateTime reminderSent = LocalDateTime.of(remiderSentDate, reminderSentTime);
					LocalDateTime now = LocalDateTime.now();

					// if 2 hours already passed
					if (Duration.between(reminderSent, now).toHours() >= reminderCancellationTime) {
						Booking addBooking = new Booking((String) row[0], ((Date) row[1]).toLocalDate(),
								((Time) row[2]).toLocalTime(), ((Date) row[3]).toLocalDate(),
								((String) row[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
								(Integer) row[5], (String) row[6], (String) row[7], (String) row[8], (String) row[9],
								(String) row[10], (Integer) row[11], (Integer) row[12] == 0 ? false : true,
								(Integer) row[13] == 0 ? false : true,
								((Time) row[14]) == null ? null : ((Time) row[14]).toLocalTime(),
								((Time) row[15]) == null ? null : ((Time) row[15]).toLocalTime(),
								(Integer) row[16] == 0 ? false : true,
								((Time) row[17]) == null ? null : ((Time) row[17]).toLocalTime(), park);
						toBeTransferred.add(addBooking);
					}
				}
			}

			transferring.add(toBeTransferred);
		}

		int parkIndex = 0;
		ArrayList<String> bookingIDs = new ArrayList<>(); // will hold all the booking ids to be deleted
		for (ArrayList<Booking> toBeTransferred : transferring) {
			// deleting the booking from the active table and inserting it into the
			// cancelled table

			// sending cancellation notification to the relevant cancelled bookings
			for (Booking transfer : toBeTransferred) {
				notifications.sendCancellationEmailNotification(
						Arrays.asList(transfer.getEmailAddress(), transfer.getPhoneNumber(),
								transfer.getParkBooked().getParkName() + " Park", transfer.getDayOfVisit(),
								transfer.getTimeOfVisit(), transfer.getFirstName() + " " + transfer.getLastName(),
								transfer.getParkBooked().getParkCity() + ", " + transfer.getParkBooked().getParkState(),
								transfer.getNumberOfVisitors(), transfer.getFinalPrice(), transfer.isPaid()),
						"Visitor did not confirm the reminder notification.");

				// adding the id to the list, we be used as IN part of the delete query next
				bookingIDs.add(transfer.getBookingId());

				// creating the insert request into the cancelled table
				Communication insert = new Communication(CommunicationType.SELF);
				try {
					insert.setQueryType(QueryType.INSERT);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}

				Park park = parks.get(parkIndex++);
				String parkTableName = parkControl.nameOfTable(park) + insert.cancelledBookings;
				insert.setTables(Arrays.asList(parkTableName));
				insert.setColumnsAndValues(
						Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
								"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
								"cancellationReason"),
						Arrays.asList(transfer.getBookingId(), transfer.getDayOfVisit(), transfer.getTimeOfVisit(),
								transfer.getDayOfBooking(),
								transfer.getVisitType() == VisitType.GROUP ? "group" : "individual",
								transfer.getNumberOfVisitors(), transfer.getIdNumber(), transfer.getFirstName(),
								transfer.getLastName(), transfer.getEmailAddress(), transfer.getPhoneNumber(),
								"Did not confirm"));

				// executing insert query
				database.executeInsertQuery(insert);
			}

			// creating the IN (...) part to the delete query, to delete all the relevant
			// bookings from the active table
			int size = bookingIDs.size();
			String value = "(";
			// creating the booking ids values
			for (int i = 0; i < size; i++) {
				value += "'" + bookingIDs.get(i) + "'";
				if (i + 1 < size)
					value += ", ";
			}
			value += ")";

			// creating the delete request
			Communication delete = new Communication(CommunicationType.SELF);
			try {
				delete.setQueryType(QueryType.DELETE);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			Park park = parks.get(transferring.indexOf(toBeTransferred));
			String parkTableName = parkControl.nameOfTable(park) + delete.activeBookings;
			delete.setTables(Arrays.asList(parkTableName));
			delete.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("IN"), Arrays.asList(value));

			// executing the query if there are bookings to remove
			if (size > 0) {
				database.executeDeleteQuery(delete);
			}

			// emptying the IDs list
			bookingIDs.removeAll(bookingIDs);
		}

		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Ending reminders checking background operations");
	}

	///////////////////////
	/// GENERAL METHODS ///
	///////////////////////

	/**
	 * This method is called from the server side, after a parameter of the park is
	 * changed. Checking the possibility to release bookings from the waiting list
	 * of this park
	 * 
	 * @param parkId
	 */
	public void checkWaitingListsAfterParametersChanged(int parkId) {
		fetchParks();
		for (LocalDate start = LocalDate.now(); start
				.compareTo(LocalDate.now().plusMonths(futureBookingsRange)) <= 0; start = start.plusDays(1)) {
			// running closeHour - openHours times (hours)
			for (int hour = openHour; hour <= closeHour; hour++) {
				// if the hour has minutes intervals
				if (minutesGapOfBookingTimes == 0) {
					checkWaitingList(parkId, start, LocalTime.of(hour, 0));
				} else {
					for (int minute = 0; minute < 60; minute += minutesGapOfBookingTimes) {
						checkWaitingList(parkId, start, LocalTime.of(hour, minute));
					}
				}
			}
		}
	}

	/**
	 * This method is called from the server side (to itself) if a visitor cancelled
	 * his booking in a specific park. In this case, checking the waiting list in
	 * order to release booking/s from it, since there is a capacity update cause
	 * the visitor cancelled his booking.
	 * 
	 * @param park
	 * @param date
	 * @param time
	 */
	public void checkWaitingListReleasePossibility(int parkId, LocalDate date, LocalTime time) {
		fetchParks();
		checkWaitingList(parkId, date, time);
	}

	/**
	 * This method is called by checkWaitingListReleasePossibility and by
	 * checkWaitingListsAfterParametersChanged
	 * 
	 * @param parkId
	 * @param date
	 * @param time
	 */
	private void checkWaitingList(int parkId, LocalDate date, LocalTime time) {
		//////////////////
		/// FIRST PART ///
		//////////////////

		// getting all waiting list bookings that can enter the park (in terms of group
		// size) in the given time frame

		// sorting the parks with their id numbers
		parks.sort(Park.parkComparator);
		Park park = parks.get(parkId - 1);
		int timeLimit = park.getTimeLimit();
		int currentCapacity = getCurrentParkCapacities(park, date, time, timeLimit);
		int moreCanEnter = park.getMaximumOrders() - currentCapacity;
		// creating a communication for checking the possibility of releasing booking/s
		// from the waiting list
		Communication checkWaitingList = new Communication(CommunicationType.SELF);
		try {
			checkWaitingList.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		String parkTableName = parkControl.nameOfTable(park) + Communication.waitingList;
		checkWaitingList.setTables(Arrays.asList(parkTableName));
		checkWaitingList.setSelectColumns(Arrays.asList("*"));
		checkWaitingList.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(date, time.minusHours(timeLimit), time.plusHours(timeLimit)));

		ArrayList<Object[]> selectResult = database.executeSelectQuery(checkWaitingList);
		ArrayList<Booking> waitingResults = new ArrayList<>();

		// run over the result of the SELECT query and take all relevant waiting list
		// bookings that are in the same time frame as the paramteres of the method
		for (Object[] row : selectResult) {
			Booking add = new Booking((String) row[0], ((Date) row[1]).toLocalDate(), ((Time) row[2]).toLocalTime(),
					((Date) row[3]).toLocalDate(),
					((String) row[5]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL, (Integer) row[6],
					(String) row[7], (String) row[8], (String) row[9], (String) row[10], (String) row[11],
					(Integer) row[12], false, false, null, null, false, null, park);
			add.setWaitingListPriority((Integer) row[4]);
			waitingResults.add(add);
		}

		// waitingResults holds all the bookings that CAN be entered in terms of their
		// group size
		waitingResults.sort(Booking.waitingListComparator); // sorting these bookings by their priority

		///////////////////
		/// SECOND PART ///
		///////////////////

		// locking the database
		try {
			database.toggleAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// transferring all relevant bookings from the waiting list table to the active
		// bookings table

		// the bookings in transferBookings will be transferred to the active table
		ArrayList<Booking> transferBookings = new ArrayList<>();
		int decreasePriority = 0;
		for (Booking currentBooking : waitingResults) {
			int currentBookingSize = currentBooking.getNumberOfVisitors();
			if (currentBookingSize <= moreCanEnter) {
				moreCanEnter -= currentBookingSize;
				decreasePriority++;
				transferBookings.add(currentBooking);
			} else {
				currentBooking.setWaitingListPriority(currentBooking.getWaitingListPriority() - decreasePriority);
			}
		}

		waitingResults.removeAll(transferBookings);

		// now: waitingResults holds all the waiting list bookings that are going to
		// stay in the waiting list, but with possible new priority. transferBookings
		// holds all the bookings that need to be released from the waiting list.

		// first: removing all released waiting list bookings from the waiting list
		// table and inserting them to the active bookings table
		for (Booking transfer : transferBookings) {
			// deleting
			Communication delete = new Communication(CommunicationType.SELF);
			try {
				delete.setQueryType(QueryType.DELETE);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			delete.setTables(Arrays.asList(parkTableName));
			delete.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
					Arrays.asList(transfer.getBookingId()));

			database.executeDeleteQuery(delete);

			// inserting
			Communication insert = new Communication(CommunicationType.SELF);
			try {
				insert.setQueryType(QueryType.INSERT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			insert.setTables(Arrays.asList(parkControl.nameOfTable(park) + Communication.activeBookings));

			// checking if the booking is going to occur in 24 hours (or less) from now
			LocalDateTime bookingDate = LocalDateTime.of(transfer.getDayOfVisit(), transfer.getTimeOfVisit());
			LocalDateTime now = LocalDateTime.now();

			if (Math.abs(Duration.between(bookingDate, now).toHours()) <= reminderSendBeforeTime) {
				transfer.setReminderArrivalTime(LocalTime.now());
				transfer.setRecievedReminder(true);
			}

			insert.setColumnsAndValues(
					Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
							"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
							"finalPrice", "paid", "confirmed", "entryParkTime", "exitParkTime", "isRecievedReminder",
							"reminderArrivalTime"),
					Arrays.asList(transfer.getBookingId(), transfer.getDayOfVisit(), transfer.getTimeOfVisit(),
							transfer.getDayOfBooking(),
							transfer.getVisitType() == VisitType.GROUP ? "group" : "individual",
							transfer.getNumberOfVisitors(), transfer.getIdNumber(), transfer.getFirstName(),
							transfer.getLastName(), transfer.getEmailAddress(), transfer.getPhoneNumber(),
							transfer.getFinalPrice(), transfer.isPaid() == false ? 0 : 1,
							transfer.isConfirmed() == false ? 0 : 1, transfer.getEntryParkTime(),
							transfer.getExitParkTime(), transfer.isRecievedReminder() == false ? 0 : 1,
							transfer.getReminderArrivalTime()));

			database.executeInsertQuery(insert);
		}

		// second: updating all remaining waiting list bookings' priorities

		for (Booking updatePriority : waitingResults) {
			Communication update = new Communication(CommunicationType.SELF);
			try {
				update.setQueryType(QueryType.UPDATE);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			update.setTables(Arrays.asList(parkTableName));
			update.setColumnsAndValues(Arrays.asList("waitingListOrder"),
					Arrays.asList(updatePriority.getWaitingListPriority()));
			update.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),
					Arrays.asList(updatePriority.getBookingId()));

			database.executeUpdateQuery(update);
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

		//////////////////
		/// THIRD PART ///
		//////////////////

		// sending confirmation to the transferred bookings
		for (Booking transfer : transferBookings) {
			notifications.sendWaitingListEmailNotification(
					Arrays.asList(transfer.getEmailAddress(), transfer.getPhoneNumber(),
							transfer.getParkBooked().getParkName() + " Park", transfer.getDayOfVisit(),
							transfer.getTimeOfVisit(), transfer.getFirstName() + " " + transfer.getLastName(),
							transfer.getParkBooked().getParkCity() + ", " + transfer.getParkBooked().getParkState(),
							transfer.getNumberOfVisitors(), transfer.getFinalPrice(), transfer.isPaid()));
		}
	}

	/**
	 * This method gets a park, a date and a time and returns the park's current
	 * orders capacity in this time frame
	 * 
	 * @param park
	 * @param date
	 * @param time
	 * @param timeLimit
	 * @return the park's current capacity for the specified time frame
	 */
	@SuppressWarnings("static-access")
	private int getCurrentParkCapacities(Park park, LocalDate date, LocalTime time, int timeLimit) {
		// creating the request for the availability check
		Communication availabilityRequest = new Communication(CommunicationType.SELF);
		try {
			availabilityRequest.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		String parkTableName = parkControl.nameOfTable(park) + availabilityRequest.activeBookings;

		availabilityRequest.setTables(Arrays.asList(parkTableName));
		availabilityRequest.setSelectColumns(Arrays.asList("numberOfVisitors"));
		availabilityRequest.setWhereConditions(Arrays.asList("dayOfVisit", "timeOfVisit", "timeOfVisit"),
				Arrays.asList("=", "AND", ">", "AND", "<"),
				Arrays.asList(date, time.minusHours(timeLimit), time.plusHours(timeLimit)));

		ArrayList<Object[]> results = database.executeSelectQuery(availabilityRequest);

		// getting the result from the database
		int countVisitors = 0;
		// checking the orders amount for the specific time
		for (Object[] row : results) {
			countVisitors += (Integer) row[0];
		}

		return countVisitors;
	}

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
		if (!parks.isEmpty()) {
			parks.removeAll(parks);
		}
		// setting the Object[] from DB to the parkList
		for (Object[] row : results) {
			Park parkToAdd = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3],
					(String) row[4], (String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8],
					(Integer) row[9], (Integer) row[10]);
			parks.add(parkToAdd);
		}
	}
}