package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import clientSide.entities.ParkVisitor;
import clientSide.entities.ParkVisitor.VisitorType;
import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.NotificationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.SecondaryRequest;
import common.entities.Booking;
import common.entities.Park;
import common.entities.Booking.VisitType;

/**
 * The ParkController class serves as a controller for managing park-related
 * operations within the application. It provides functionalities to interact
 * with park data, including fetching park details from the database, managing
 * bookings, checking visitor and booking existence, updating park capacities,
 * and handling visitor bookings. This class follows the Singleton pattern to
 * ensure that only one instance of the controller is used throughout the
 * application, providing a centralized point of access to the park data and
 * operations.
 */
public class ParkController {
	private static ParkController instance;
	private Park park;
	private Booking booking;
	
	

	private Map<String, String> bookingDetails = new HashMap<>();

	// emailTxt, phoneTxt, visitorsAmountTxt, visitorIDTxt, nameTxt, lastNameTxt;
	/**
	 * Private constructor to prevent instantiation from outside the class.
	 * Initializes the controller instance. This approach is part of the Singleton
	 * pattern, ensuring that ParkController is only instantiated once throughout
	 * the application life cycle.
	 */
	private ParkController() {

	}

	public static ParkController getInstance() {
		if (instance == null)
			instance = new ParkController();
		return instance;
	}

	/**
	 * Restores the previously saved Park object from the controller's state. This
	 * method is typically used to retrieve the park information that was last set
	 * or modified within the application session.
	 * 
	 * @return The last saved Park object or null if no park has been saved.
	 */
	public Park restorePark() {
		return park;
	}

	/**
	 * Saves a Park object to the controller's state. This method is typically used
	 * to store the park information that needs to be retained across different
	 * operations within the application session.
	 * 
	 * @param park The Park object to save.
	 */
	public void savePark(Park park) {
		this.park = park;
	}

	public Booking restoreBooking() {
		return booking;
	}

	public void saveBooking(Booking booking) {
		this.booking = booking;
	}
	
	public void setBookingDetails(Map<String, String> bookingDetails) {
		this.bookingDetails = bookingDetails;
	}

	public Map<String, String> getBookingDetails() {
		return new HashMap<>(bookingDetails); // Return a copy to prevent direct modification
	}

	/**
	 * Generates a database-friendly table name from a park name by converting it to
	 * lower case and replacing spaces with underscores.
	 * 
	 * @param park The Park object whose name is to be converted into a table name.
	 * @return A string representing the table name derived from the park name.
	 */
	public String nameOfTable(Park park) {

		return park.getParkName().toLowerCase().replaceAll(" ", "_");
	}

	/**
	 * Converts a database table name back to a more readable park name by replacing
	 * underscores with spaces and capitalizing the first letter of each word.
	 * 
	 * @param park The string representing the database table name to convert back
	 *             to a park name.
	 * @return A string representing the more readable park name.
	 */
	public String nameOfPark(String park) {
		return park.toLowerCase().replaceAll("_", " ");
	}

	/**
	 * Fetches park data from the database, using a communication request - 'SELECT'
	 * query, retrieves the result, and maps it to Park objects.
	 * 
	 * @return An ArrayList of Park objects containing the fetched park data.
	 */
	public ArrayList<Park> fetchParks() {
		// creating a communication request to fetch the data from the database
		Communication requestParks = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestParks.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		requestParks.setTables(Arrays.asList(Communication.park));
		requestParks.setSelectColumns(Arrays.asList("*"));
		GoNatureClientUI.client.accept(requestParks); // sending to server side

		ArrayList<Park> parkList = new ArrayList<>();
		// getting the result
		if (parkList != null && !parkList.isEmpty())
			parkList.removeAll(parkList);
		// setting the Object[] from DB to the parkList
		for (Object[] row : requestParks.getResultList()) {
			Park newPark = new Park(Integer.parseInt(row[0].toString()), // parkId
					row[1].toString(), // parkName
					row[2].toString(), // parkCity
					row[3].toString(), // parkState
					row[4].toString(), // parkDepartment
					row[5].toString(), // parkManagerId
					row[6].toString(), // departmentManagerId
					Integer.parseInt(row[7].toString()), // maximumVisitors
					Integer.parseInt(row[8].toString()), // maximumOrders
					Integer.parseInt(row[9].toString()), // timeLimit
					Integer.parseInt(row[10].toString()) // currentCapacity
			);
			parkList.add(newPark);
		}
		return parkList;
	}

	/**
	 * A 'SELECT' SQL query is generated to access the relevant table (traveler or
	 * group_guide) in the database. This indicates if the visitor is already exist.
	 * 
	 * @param table   The table to query.
	 * @param IDfield The column name of the visitor's ID.
	 * @param ID      The visitor's ID value.
	 * @return A ParkVisitor object if the visitor exists, null otherwise. It
	 *         returns a ParkVisitor instance if exists, otherwise return null.
	 */
	public ParkVisitor checkIfVisitorExists(String table, String IDfield, String ID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(table));
			request.setSelectColumns(Arrays.asList("*")); // returns all the data according to the inserted ID
			request.setWhereConditions(Arrays.asList(IDfield), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (!result.isEmpty()) {
			Object[] row = result.get(0); // Get the first and only row
			// Adjust the instantiation to match the ParkVisitor constructor
			if (table.equals(Communication.groupGuide)) {
				ParkVisitor visitor = new ParkVisitor(row[0].toString(), // idNumber
						row[1].toString(), // firstName
						row[2].toString(), // lastName
						row[3].toString(), // emailAddress
						row[4].toString(), // phoneNumber
						row[5].toString(), // username
						row[6].toString(), // password
						row[7].toString().equals("1"), // isLoggedIn, when '1' represents logged in
						VisitorType.GROUPGUIDE// visitorType
				);
				return visitor;
			} else {
				ParkVisitor visitor = new ParkVisitor(row[0].toString(), // idNumber
						null, // firstName
						null, // lastName
						null, // emailAddress
						null, // phoneNumber
						null, // username
						null, // password
						(Integer) row[1] == 1 ? true : false, // isLoggedIn, when '1' represents logged in
						VisitorType.TRAVELLER // visitorType
				);
				return visitor;
			}
		}
		return null; // If the visitor does not exist, null will be returned
	}

	/**
	 * A 'SELECT' SQL query is generated to access the relevant table. This
	 * indicates if there is a corresponding booking in the database.
	 * 
	 * @param table The table to query for a booking.
	 * @param ID    The ID of the booker.
	 * @param field The specific id field in table
	 * @return It returns a Booking instance if exists, otherwise return null.
	 */
	public ArrayList<Booking> checkIfBookingExists(String table, String field, String ID) {
		Communication requestBookings = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestBookings.setQueryType(QueryType.SELECT);
			requestBookings.setTables(Arrays.asList(table));
			requestBookings.setSelectColumns(Arrays.asList("*")); // returns all the data according to the inserted ID
			requestBookings.setWhereConditions(Arrays.asList(field), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(requestBookings);
		ArrayList<Object[]> result = requestBookings.getResultList();
		if (!result.isEmpty()) {
			ArrayList<Booking> bookings = new ArrayList<>();
			for (Object[] row : requestBookings.getResultList()) {
				// Get row
				// Adjust the instantiation to match the Booking constructor
				if (row.length > 14) {
					// The reservation exists in the active bookings table of the park
					LocalTime entryParkTime = (row[14] != null) ? LocalTime.parse(row[14].toString()) : null;
					LocalTime exitParkTime = (row[15] != null) ? LocalTime.parse(row[15].toString()) : null;
					LocalTime reminderArrivalTime = (row[17] != null) ? LocalTime.parse(row[17].toString()) : null;
					Booking booking = new Booking(row[0].toString(), // bookingId
							LocalDate.parse(row[1].toString()), // dayOfVisit
							LocalTime.parse(row[2].toString()), // timeOfVisit
							LocalDate.parse(row[3].toString()), // dayOfBooking
							row[4].toString().equals("individual") ? VisitType.INDIVIDUAL : VisitType.GROUP, // visitType
							Integer.parseInt(row[5].toString()), // numberOfVisitors
							row[6].toString(), // idNumber
							row[7].toString(), // firstName
							row[8].toString(), // lastName
							row[9].toString(), // emailAddress
							row[10].toString(), // phoneNumber
							Integer.parseInt(row[11].toString()), // finalPrice
							row[16] != null && row[12].toString().equals("1"), // paid ---------
							row[16] != null && row[13].toString().equals("1"), // confirmed ---------
							entryParkTime, // entryParkTime
							exitParkTime, // exitParkTime
							row[16] != null && row[16].toString().equals("1"), // isReceivedReminder ---------
							reminderArrivalTime, // reminderArrivalTime
							park);
					bookings.add(booking);
				} else if (row.length == 14) {
					// The reservation exists in the done bookings table of the park
					Booking booking = new Booking(row[0].toString(), // bookingId
							LocalDate.parse(row[1].toString()), // dayOfVisit
							LocalTime.parse(row[2].toString()), // timeOfVisit
							LocalDate.parse(row[3].toString()), // dayOfBooking
							row[4].toString().equals("individual") ? VisitType.INDIVIDUAL : VisitType.GROUP, // visitType
							Integer.parseInt(row[5].toString()), // numberOfVisitors
							row[6].toString(), // idNumber
							row[7].toString(), // firstName
							row[8].toString(), // lastName
							row[9].toString(), // emailAddress
							row[10].toString(), // phoneNumber
							Integer.parseInt(row[11].toString()), // finalPrice
							true, // paid
							true, // confirmed
							LocalTime.parse(row[12].toString()), // entryParkTime
							LocalTime.parse(row[13].toString()), // exitParkTime
							true, // isReceivedReminder
							LocalTime.parse(row[12].toString()), // reminderArrivalTime - notRelevant
							park);
					bookings.add(booking);
				} else {
					// The reservation exists in the cancelled bookings table of the park
					Booking booking = new Booking(row[0].toString(), // bookingId
							LocalDate.parse(row[1].toString()), // dayOfVisit
							LocalTime.parse(row[2].toString()), // timeOfVisit
							LocalDate.parse(row[3].toString()), // dayOfBooking
							row[4].toString().equals("individual") ? VisitType.INDIVIDUAL : VisitType.GROUP, // visitType
							Integer.parseInt(row[5].toString()), // numberOfVisitors
							row[6].toString(), // idNumber
							row[7].toString(), // firstName
							row[8].toString(), // lastName
							row[9].toString(), // emailAddress
							row[10].toString(), // phoneNumber
							-1, // finalPrice
							false, // paid
							false, // confirmed
							null, // entryParkTime
							null, // exitParkTime
							false, // isReceivedReminder
							null, // reminderArrivalTime - notRelevant
							park);
					bookings.add(booking);
				}
			}
			return bookings;
		}
		return null; // If the booking does not exist, null will be returned
	}

	/**
	 * A 'SELECT' SQL query is generated to access 'park' table in the database. The
	 * query retrieves from the DB the list of parks managed by the department
	 * responsible for them. In case the maneger is Park Manager only one park will
	 * retrieve from the DB
	 * 
	 * @param park
	 * @return It returns an array list of parks if exists. otherwise returns null
	 */
	public ArrayList<Park> fetchManagerParksList(String field, String ID) {
		ArrayList<Park> parks = new ArrayList<>();
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.park));
			request.setSelectColumns(Arrays.asList("*")); // returns all the data according to the inserted department
			request.setWhereConditions(Arrays.asList(field), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				Object[] row = result.get(i); // Get the relevant row each time
				// Adjust the instantiation to match the Park constructor
				Park park = new Park(Integer.parseInt(row[0].toString()), // parkId
						row[1].toString(), // parkName
						row[2].toString(), // parkCity
						row[3].toString(), // parkState
						row[4].toString(), // parkDepartment
						row[5].toString(), // parkManagerId
						row[6].toString(), // departmentManagerId
						Integer.parseInt(row[7].toString()), // maximumVisitors
						Integer.parseInt(row[8].toString()), // maximumOrders
						Integer.parseInt(row[9].toString()), // timeLimit
						Integer.parseInt(row[10].toString()) // currentCapacity
				);
				// adds new park to 'parks' arrayList
				parks.add(park);
			}
		}
		return parks; // If the visitor does not exist, null will be returned
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
		@SuppressWarnings("static-access")
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
		int result = parkOfBooking.getMaximumVisitors() - countVisitors - numberOfVisitors;
		return result >= 0;
	}

	/**
	 * An 'UPDATE' SQL query is generated to access the 'park' table in the database
	 * and change the 'currentCapacity' field for the relevant park. In case
	 * visitors only arrive at the park, the currentCapacity will increase.
	 * Otherwise, the opposite. This indicates to the managers the capacity of a
	 * specific park.
	 * 
	 * @param park
	 * @param amount
	 * @return It returns a String describing the capacity
	 */
	public boolean updateCurrentCapacity(String parkName, int amount, boolean add) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		request.setCritical(true, park.getParkId() - 1);
		try {
			request.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setTables(Arrays.asList(Communication.park));
		request.setSelectColumns(Arrays.asList("currentCapacity"));
		request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="), Arrays.asList(parkName));

		// setting the secondary request
		request.setSecondaryRequest(SecondaryRequest.UPDATE_CAPACITY);
		request.setNumberOfVisitors(add ? amount : amount * -1);

		GoNatureClientUI.client.accept(request);

		return request.getQueryResult();
	}

	/**
	 * Retrieves current capacity and limits information for a specified park from
	 * the database. This method executes a 'SELECT' SQL query to obtain details
	 * such as the maximum visitors capacity, maximum order amount, maximum time
	 * limit, and the current capacity of the park.
	 * 
	 * @return A String array containing four elements: 1. Maximum visitors capacity
	 *         (retValue[0]) 2. Maximum order amount (retValue[1]) 3. Maximum time
	 *         limit for visits (retValue[2]) 4. Current capacity of the park
	 *         (retValue[3]) If no information is found, returns null.
	 */
	public String[] checkCurrentCapacity(String parkName) {
		String[] retValue = new String[4];
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.park));
			request.setSelectColumns(Arrays.asList("maximumVisitorsCapacity", "maximumOrderAmount", "maximumTimeLimit",
					"currentCapacity"));
			request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="), Arrays.asList(parkName));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		// Saves the retrieved values from the database in order to return them to the
		// requesting employee
		try {
			System.out.println(result.size());
			if (!result.isEmpty()) {
				Object[] capacityDB = result.get(0);
				// Validate the result array to ensure it has the expected number of elements
				// and none are null.
				if (capacityDB.length == 4 && Arrays.stream(capacityDB).noneMatch(Objects::isNull)) {
					retValue[0] = capacityDB[0].toString(); // maximumVisitorsCapacity
					retValue[1] = capacityDB[1].toString(); // maximumOrderAmount
					retValue[2] = capacityDB[2].toString(); // maximumTimeLimit
					retValue[3] = capacityDB[3].toString(); // currentCapacity
				} else {
					System.out.println("Received incomplete or invalid data for park capacity.");
					return null;
				}
			}
		} catch (NullPointerException e) {
			System.out.println("No results found for park: " + parkName);
			return null;
		}
		return retValue;
	}

	/**
	 * Checks if a booking is locked for processing by another park employee. This
	 * method checks the status of a booking lock in the database. A booking lock
	 * indicates whether the booking is currently being processed by another
	 * employee.
	 * 
	 * @param ID The ID of the booking to check.
	 * @return An integer representing the status of the booking lock. 0 - Indicates
	 *         that the employee can work on this booking. 1 - Indicates that no one
	 *         is working on that booking but the process of locking has failed. 2 -
	 *         Indicates that the booking is open in another screen of another park
	 *         employee.
	 */
	public boolean checkIfBookingIsLock(String ID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		request.setCritical(true, park.getParkId() - 1);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.bookingLock));
			request.setSelectColumns(Arrays.asList("bookingId")); // returns all the data according to the inserted ID
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setSecondaryRequest(SecondaryRequest.LOCK_BOOKING);
		request.setBookingId(ID);
		GoNatureClientUI.client.accept(request);

		return request.getQueryResult();
	}

	/**
	 * An 'UPDATE' SQL query is generated to access the relevant table in the
	 * database and change the 'confirmed' field to true (represents by '1'). This
	 * indicating the traveler intends to arrive.
	 * 
	 * @param table
	 * @param bookingID
	 * @return It returns a boolean if the update succeed, otherwise false
	 */
	public boolean updateConfirmed(String table, String bookingID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
			request.setTables(Arrays.asList(table + Communication.activeBookings));
			request.setColumnsAndValues(Arrays.asList("confirmed"), Arrays.asList('1'));
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(bookingID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (result)
			return true;
		return false;
	}

	/**
	 * An 'UPDATE' SQL query is generated to access relevant park_bookings_table in
	 * the database and update the 'exitParkTime' or 'entryParkTime' field for
	 * relevant bookingID.
	 * 
	 * @param park
	 * @param ID
	 * @return It returns a boolean value indicating whether the update succeeded
	 *         (true) or not(false).
	 */
	public boolean updateTimeInPark(String park, String timeField, String ID) {
		// Define a formatter that formats the time as hour and minute
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		// Format the current time using the formatter
		String formattedTime = LocalTime.now().format(formatter);
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
			request.setTables(Arrays.asList(park));
			request.setColumnsAndValues(Arrays.asList(timeField), Arrays.asList(formattedTime));
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (result)
			return true;
		return false;
	}

	/**
	 * An 'UPDATE' SQL query is generated to access relevant park_bookings_table in
	 * the database and update the 'paid' field for 1 - paid.
	 * 
	 * @param parkTable
	 * @return It returns a boolean value indicating whether the update succeeded
	 *         (true) or not(false).
	 */
	public boolean payForBooking(String parkTable, String ID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
			request.setTables(Arrays.asList(parkTable+Communication.activeBookings));
			request.setColumnsAndValues(Arrays.asList("paid"), Arrays.asList('1'));
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (result)
			return true;
		return false;
	}
	
	/**
	 * An 'UPDATE' SQL query is generated to access relevant park_bookings_table in
	 * the database and update the 'paid' field for 1 - paid.
	 * 
	 * @param parkTable
	 * @return It returns a boolean value indicating whether the update succeeded
	 *         (true) or not(false).
	 */
	public boolean revertPaymentForBooking(String parkTable, String ID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
			request.setTables(Arrays.asList(parkTable+Communication.activeBookings));
			request.setColumnsAndValues(Arrays.asList("paid","entryParkTime"), Arrays.asList('0',"(NULL)"));
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (result)
			return true;
		return false;
	}

	/**
	 * A 'Delete' SQL query is generated to access relevant park_bookings_table in
	 * the database and update the 'exitParkTime' field for relevant bookingID.
	 * 
	 * @param park
	 * @param ID
	 * @return It returns a boolean value indicating whether the update succeeded
	 *         (true) or not(false).
	 */
	public boolean removeBooking(String table, String bookingID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.DELETE);
			request.setTables(Arrays.asList(table));
			request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList(bookingID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (result)
			return true;
		return false;
	}

	/**
	 * Inserts a new booking record into a specified table within the database,
	 * based on the type of the booking. This method supports inserting bookings
	 * into tables designated for active bookings, completed bookings, or cancelled
	 * bookings. The table into which the booking is inserted is determined by the
	 * combination of the park's name and the specified booking type (active, done,
	 * or canceled). Depending on the booking type, different sets of booking
	 * details are included in the insert query.
	 *
	 * @param newBooking    The new Booking object containing the details of the
	 *                      booking to be inserted.
	 * @param relevantTable A suffix indicating the specific table to insert the
	 *                      booking into (e.g., "_park_active_booking").
	 * @param type          The type of the booking, determining which table the
	 *                      booking is inserted into ("active", "done", "canceled").
	 * @return true if the insert operation is successful and the booking is
	 *         correctly added to the database; false otherwise.
	 */
	public boolean insertBookingToTable(Booking newBooking, String table, String type) {
		// creating the request for the new booking insert

		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		switch (type) {
		case "done":
			insertRequest.setTables(Arrays.asList(table + Communication.doneBookings));
			System.out.println("new booking entry time: " + newBooking.getEntryParkTime());
			insertRequest.setColumnsAndValues(
					Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
							"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
							"finalPrice", "entryParkTime", "exitParkTime"),
					Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
							newBooking.getDayOfBooking(),
							newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
							newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
							newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
							newBooking.getFinalPrice(), newBooking.getEntryParkTime(), LocalTime.now()));
			break;
		case "cancelled":
			insertRequest.setTables(Arrays.asList(table + Communication.cancelledBookings));
			insertRequest.setColumnsAndValues(
					Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
							"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
							"cancellationReason"),
					Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
							newBooking.getDayOfBooking(),
							newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
							newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
							newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
							Communication.userCancelled));
			break;
		default: // active
			insertRequest.setTables(Arrays.asList(table + Communication.activeBookings));
			insertRequest.setColumnsAndValues(
					Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType",
							"numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
							"finalPrice", "paid", "confirmed", "entryParkTime", "exitParkTime", "isRecievedReminder",
							"reminderArrivalTime"),
					Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
							newBooking.getDayOfBooking(),
							newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
							newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(),
							newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(),
							newBooking.getFinalPrice(), newBooking.isPaid() == false ? 0 : 1,
							newBooking.isConfirmed() == false ? 0 : 1, newBooking.getEntryParkTime(),
							newBooking.getExitParkTime(), newBooking.isRecievedReminder() == false ? 0 : 1,
							newBooking.getReminderArrivalTime()));
			break;
		}
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
		Communication notifyBooking = new Communication(CommunicationType.NOTIFICATION);

		notifyBooking.setNotificationType(
				isCancel ? NotificationType.SEND_CANCELLATION : NotificationType.SEND_CONFIRMATION);

		notifyBooking.setFirstName(notify.getFirstName());
		notifyBooking.setLastName(notify.getLastName());
		notifyBooking.setEmailAddress(notify.getEmailAddress());
		notifyBooking.setPhoneNumber(notify.getPhoneNumber());
		notifyBooking.setFinalPrice(notify.getFinalPrice());
		notifyBooking.setPaid(notify.isPaid());
		notifyBooking.setNumberOfVisitors(notify.getNumberOfVisitors());
		notifyBooking.setDayOfVisit(notify.getDayOfVisit());
		notifyBooking.setTimeOfVisit(notify.getTimeOfVisit());
		notifyBooking.setParkName(notify.getParkBooked().getParkName() + " Park");
		notifyBooking
				.setParkLocation(notify.getParkBooked().getParkCity() + ", " + notify.getParkBooked().getParkState());

		GoNatureClientUI.client.accept(notifyBooking);
	}

}