package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalTime;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import entities.Booking.VisitType;
import entities.Booking;

public class ParkController {
	private static ParkController instance;
	private Park park;
	private ArrayList<Park> parks;

	private ParkController() {
		
	}
	
	public static ParkController getInstance() {
		if (instance == null)
			instance = new ParkController();
		return instance;
	}
	
	public Park restorePark() {
		return park;
	}

	public void savePark(Park park) {
		this.park = park;
	}
	
	public ArrayList<Park> restoreParkList() {
		return parks;
	}

	public void saveParkList(ArrayList<Park> parks) {
		this.parks = parks;
	}
	
	public String nameOfTable(Park park) {
		
		return park.getParkName().toLowerCase().replaceAll(" ", "_");
	}
	
	public String nameOfPark(String park) {
		return park.toLowerCase().replaceAll("_", " ");
	}
	
	/**
	 * Fetches park data from the database, using a communication request - 'SELECT' query,
	 * retrieves the result, and maps it to Park objects.
	 * @return
	 * An ArrayList of Park objects containing the fetched park data.
	 */
	public ArrayList<Park> fetchParks() {
		// creating a communication request to fetch the data from the database
		Communication requestParks = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestParks.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		requestParks.setTables(Arrays.asList("park"));
		requestParks.setSelectColumns(Arrays.asList("*"));
		GoNatureClientUI.client.accept(requestParks); // sending to server side

		ArrayList<Park> parkList = new ArrayList<>();
		// getting the result
		if (parkList != null && !parkList.isEmpty())
			parkList.removeAll(parkList);
		// setting the Object[] from DB to the parkList
		for (Object[] row : requestParks.getResultList()) {
			Park newPark = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4],
					(String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8], (Integer) row[9],
					(Integer) row[10]);
			parkList.add(newPark);
		}

		return parkList;
	}
	
	/**
     * A 'SELECT' SQL query is generated to access the relevant table (traveler or group_guide) in the database.
     * This indicates if the visitor is already exist.
     * @param park
     * @return 
     * 		It returns a ParkVisitor instance if exists, otherwise return null. 
     */
	public ParkVisitor  checkIfVisitorExists(String table, String IDfield, String ID) {	
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList(table));
	    	request.setSelectColumns(Arrays.asList("*")); //returns all the data according to the inserted ID
	    	request.setWhereConditions(Arrays.asList(IDfield), Arrays.asList("="),Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
        if (!result.isEmpty()) {
        	Object[] row = result.get(0); // Get the first and only row
	        // Adjust the instantiation to match the ParkVisitor constructor
        	ParkVisitor visitor = new ParkVisitor(
	        row[0].toString(), //idNumber
	        row[1].toString(), //firstName
	        row[2].toString(), //lastName
	        row[3].toString(), //emailAddress
	        row[4].toString(), //phoneNumber
	        row[5].toString(), //username
	        row[6].toString(), //password
	        row[7].toString().equals("1"), // isLoggedIn, when '1' represents logged in  
	        table.equals("traveller") ? ParkVisitor.VisitorType.TRAVELLER : ParkVisitor.VisitorType.GROUPGUIDE //visitorType
	        );
        	return visitor;
        }              
		return null; //If the visitor does not exist, null will be returned
    }
	
	/**
     * A 'SELECT' SQL query is generated to access the relevant table.
     * This indicates if there is a corresponding booking in the database.
     * @param park
     * @return 
     * 		It returns a Booking instance if exists, otherwise return null. 
     */
	public Booking checkIfBookingExists(String table, String ID) {	
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList(table));
	    	request.setSelectColumns(Arrays.asList("*")); //returns all the data according to the inserted ID
	    	request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
        if (!result.isEmpty()) {
        	Object[] row = result.get(0); // Get the first and only row 
	        // Adjust the instantiation to match the Booking constructor
        	LocalTime entryParkTime = (row[14] != null) ? LocalTime.parse(row[14].toString()) : null;
        	LocalTime exitParkTime = (row[15] != null) ? LocalTime.parse(row[15].toString()) : null;
        	LocalTime reminderArrivalTime = (row[17] != null) ? LocalTime.parse(row[17].toString()) : null;
        	// Adjust the instantiation to match the Booking constructor
            Booking booking = new Booking(
                    row[0].toString(), // bookingId
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
                    row[12].toString().equals("1"), // paid
                    row[13].toString().equals("1"), // confirmed
                    //fix from here to the end
                    //LocalDate.parse(row[14].toString()),
                    //LocalDate.parse(row[15].toString()),
                    entryParkTime, // entryParkTime
                    exitParkTime, // exitParkTime
                    row[16].toString().equals("1"), // isReceivedReminder
                    reminderArrivalTime, // reminderArrivalTime
                    park
                );
                return booking;
        }              
		return null; //If the booking does not exist, null will be returned
    }
	
	/**
     * A 'SELECT' SQL query is generated to access 'park' table in the database.
     * The query retrieves from the DB the list of parks managed by the department responsible for them.
     * @param park
     * @return 
     * 		It returns an array list of parks if exists. otherwise returns null 
     */
	public ArrayList<Park> fetchManagerParksList(String field, String ID) {
		ArrayList<Park> parks = new ArrayList<>();
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList("park"));
	    	request.setSelectColumns(Arrays.asList("*")); //returns all the data according to the inserted department
	    	request.setWhereConditions(Arrays.asList(field), Arrays.asList("="),Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
        if (!result.isEmpty()) {
        	for(int i =0; i< result.size(); i++) {
	        	Object[] row = result.get(i); // Get the relevant row each time
		        // Adjust the instantiation to match the Park constructor
	        	Park park = new Park(
		        Integer.parseInt(row[0].toString()), //parkId
		        row[1].toString(), //parkName
		        row[2].toString(), //parkCity
		        row[3].toString(), //parkState
		        row[4].toString(), //parkDepartment
		        row[5].toString(), //parkManagerId
		        row[6].toString(), //departmentManagerId
		        Integer.parseInt(row[7].toString()), //maximumVisitors
		        Integer.parseInt(row[8].toString()), //maximumOrders
		        Integer.parseInt(row[9].toString()), //timeLimit
		        Integer.parseInt(row[10].toString()) //currentCapacity
		        );
	        	//adds new park to 'parks' arrayList
	        	parks.add(park);
        	} 
        }              
		return parks; //If the visitor does not exist, null will be returned
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
		int result = parkOfBooking.getMaximumOrders() - countVisitors - numberOfVisitors;
		return result > 0;		
	}
	
    /**
     * @param park
     * @param amount
     * @return 
     * An 'UPDATE' SQL query is generated to access the 'park' table in the database and change the 'currentCapacity' 
     * field for the relevant park. 
     * In case visitors only arrive at the park, the value of 'amount' will be positive and the currentCapacity
     * will increase. Otherwise, the opposite.
     * This indicates to the managers the capacity of a specific park.
     * It returns a String describing the capacity 
     */
    public boolean updateCurrentCapacity(String park, int amount) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList("park"));
	    	request.setColumnsAndValues(Arrays.asList("currentCapacity"), Arrays.asList(amount));
	    	request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),Arrays.asList(park));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;   
    }
    
    /**
     * @param table
     * @param bookingID
     * @return 
     * An 'UPDATE' SQL query is generated to access the relevant table in the database and change the 'confirmed' 
     * field to true (represents by '1'. 
     * This indicating the traveler intends to arrive
     * It returns a boolean if the update succeed, otherwise false
     */
    public boolean updateConfirmed(String table, String bookingID) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList(table + "_park_active_booking"));
	    	request.setColumnsAndValues(Arrays.asList("confirmed"), Arrays.asList('1'));
	    	request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),Arrays.asList(bookingID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;   
    }
    
    /**
     * @param park
     * @param ID
     * @return 
     * An 'UPDATE' SQL query is generated to access relevant park_bookings_table in the database and update the 
     * 'exitParkTime' or 'entryParkTime' field for relevant bookingID.
     * It returns a boolean value indicating whether the update succeeded (true) or not(false). 
     */
    public boolean updateTimeInPark(String park, String timeField, String ID) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList(park));
	    	request.setColumnsAndValues(Arrays.asList(timeField), Arrays.asList(LocalTime.now()));
	    	request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),Arrays.asList(ID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;   
    }
    
    /**
     * An 'UPDATE' SQL query is generated to access relevant park_bookings_table in the database and update the 
     * 'paid' field for 1 - paid.
     * @param parkTable
     * @return 
     * 	It returns a boolean value indicating whether the update succeeded (true) or not(false). 
     */
    public boolean payForBooking(String parkTable) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList(parkTable));
	    	request.setColumnsAndValues(Arrays.asList("paid"), Arrays.asList('1'));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;   
    }
    
    /** 
     * @param park
     * @param ID
     * @return 
     * A 'Delete' SQL query is generated to access relevant park_bookings_table in the database and update the 
     * 'exitParkTime' field for relevant bookingID.
     * It returns a boolean value indicating whether the update succeeded (true) or not(false). 
     */
    public boolean removeBookingFromActiveBookings(String table, String bookingID) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.DELETE);
	    	request.setTables(Arrays.asList(table));
	    	request.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="),Arrays.asList(bookingID));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;   
    }
    
    /**
     * @param park
     * @return 
     * A 'SELECT' SQL query is generated to access the 'park' table in the database and retrieve the 'currentCapacity' 
     * 'maximumOrderAmount' and 'maximumVisitorsCapacity' fields for the relevant park. 
     * This indicates to the employees the capacity of a specific park, in order to determine if a specific 
     * number of visitors can currently be accommodated.
     * It returns 3 Strings describing the currentCapacity and the maximumVisitorsCapacity.
     */
    public String[] checkCurrentCapacity(String park) {
		String[] retValue = new String[4];
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList("park"));
	    	request.setSelectColumns(Arrays.asList("maximumVisitorsCapacity", "maximumOrderAmount","maximumTimeLimit", "currentCapacity"));
	    	request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),Arrays.asList(park));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
    	System.out.println(result.size());
    	//Saves the retrieved values from the database in order to return them to the requesting employee
        if (!result.isEmpty()) {
        	Object[] capacityDB = result.get(0);
        	if (capacityDB.length > 1) {
        		retValue[0] = capacityDB[0].toString(); //maximumVisitorsCapacity
        		retValue[1] = capacityDB[1].toString(); //maximumOrderAmount
        		retValue[2] = capacityDB[2].toString(); //maximumTimeLimit
        		retValue[3] = capacityDB[3].toString(); //currentCapacity
        	}
    	}
    	return retValue;
    }
    
	/**
	 * This method gets a new booking and the booker details and inserts the booking into the relevant table
	 * 
	 * @param newBooking		newBooking the booking to insert
	 * @param booker 			the booker of the booking
	 * @param relevantTable		
	 * @param type				type of table to insert the booking
	 * @return 					true if the insert query succeed, false if not
	 */
	public boolean insertBookingToTable(Booking newBooking, ParkVisitor booker, String relevantTable, String type) {
		// creating the request for the new booking insert
		Communication insertRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			insertRequest.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		String parkTableName = ParkController.getInstance().nameOfTable(newBooking.getParkBooked())+relevantTable;
		insertRequest.setTables(Arrays.asList(parkTableName));		
		switch (type){
			case "done":
				insertRequest.setColumnsAndValues(Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking",
			    	"visitType", "numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
			    	"finalPrice","entryParkTime", "exitParkTime"),
			    	Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
					newBooking.getDayOfBooking(), newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
					newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(), 
					newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(), 
					newBooking.getFinalPrice(), newBooking.getEntryParkTime(), LocalTime.now()));
		        System.out.println("inserted to done bookings");
		        break;
			case "canceled":
		    	insertRequest.setColumnsAndValues(Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking",
			    	"visitType", "numberOfVisitors", "idNumber", "firstName", "lastName", "emailAddress", "phoneNumber",
			    	"cancellationReason"),
		    		Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
							newBooking.getDayOfBooking(), newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
							newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(), 
							newBooking.getLastName(), newBooking.getEmailAddress(), newBooking.getPhoneNumber(), 
							"client has canceled"));
			    break;
			default: //active
			    insertRequest.setColumnsAndValues(Arrays.asList("bookingId", "dayOfVisit", "timeOfVisit", "dayOfBooking", "visitType", "numberOfVisitors",
					"idNumber", "firstName", "lastName", "emailAddress", "phoneNumber", "finalPrice", "paid", "confirmed",
					"entryParkTime", "exitParkTime", "isRecievedReminder", "reminderArrivalTime"),
			    Arrays.asList(newBooking.getBookingId(), newBooking.getDayOfVisit(), newBooking.getTimeOfVisit(),
					newBooking.getDayOfBooking(),
					newBooking.getVisitType() == VisitType.GROUP ? "group" : "individual",
					newBooking.getNumberOfVisitors(), newBooking.getIdNumber(), newBooking.getFirstName(), newBooking.getLastName(),
					newBooking.getEmailAddress(), newBooking.getPhoneNumber(), newBooking.getFinalPrice(),
					newBooking.isPaid() == false ? 0 : 1, newBooking.isConfirmed() == false ? 0 : 1,
					newBooking.getEntryParkTime(), newBooking.getExitParkTime(),
					newBooking.isRecievedReminder() == false ? 0 : 1, newBooking.getReminderArrivalTime()));
			    System.out.println("inserted to active bookings");
			    break;
		}	
		// sending the request to the server side
		GoNatureClientUI.client.accept(insertRequest);

		// getting the result from the database
		return insertRequest.getQueryResult();
	}
}