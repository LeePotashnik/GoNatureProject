package clientSide.control;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkManager;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.util.Pair;

public class ReportsController {
	
	private static ReportsController instance;
	// for saving and restoring purposes of the screen
	private ParkManager savedParkManager;
	private DepartmentManager savedDepartmentManager;

	/**
	 * An empty and private controller, for the singelton design pattern
	 */
	private ReportsController() {

	}
	/**
	 * The ReportsController is defined as a Singleton class. This method allows
	 * creating an instance of the class only once during runtime of the
	 * application.
	 * 
	 * @return the ReportsController instance
	 */
	public static ReportsController getInstance() {
		if (instance == null)
			instance = new ReportsController();
		return instance;
	}

	/**
	 * Retrieves a Park object from the database based on the provided park name.
	 * This method constructs a SELECT query to fetch all the details of a park
	 * that matches the specified name. It uses a Communication object to send the
	 * query request to the server side for execution.
	 *
	 * @param parkName The name of the park to retrieve. This should match the
	 *                 'parkName' column in the 'park' table in the database.
	 * @return A Park object populated with data from the database if a matching
	 *         park is found. Returns null if no park with the specified name is found.
	 */
	public Park getManagerPark(String parkName) {
		// creating a communication request to fetch the data from the database
		Communication getPark = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			getPark.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		getPark.setTables(Arrays.asList("park"));
		getPark.setSelectColumns(Arrays.asList("*"));
		getPark.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="), Arrays.asList(parkName));
		// sending the request to the server side
		GoNatureClientUI.client.accept(getPark);

		// getting the result
		if (getPark.getResultList().isEmpty()) { // no park like that
			return null;
		}

		// setting the park's data into a park object and returning it
		Object[] row = getPark.getResultList().get(0);
		Park park = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4],
				(String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8], (Integer) row[9],
				(Integer) row[10]);
		return park;
	}

	/**
	 * Retrieves a list of Park objects from the database associated with a specific department.
	 * This method constructs a SELECT query to fetch details of all parks that belong to the specified
	 * department name. It utilizes a Communication object to send the query request to the server side
	 * for execution.
	 *
	 * @param departmentName The name of the department for which parks are to be retrieved. The method
	 *                       matches this name against the 'department' column in the 'park' table in the
	 *                       database.
	 * @return An ArrayList of Park objects, each populated with data from the database for a single park
	 *         associated with the specified department. Returns an empty list if no parks are found for
	 *         the specified department, or null if there's an issue with the database query execution.
	 */
	public ArrayList<Park> getDepartmentParks(String departmentName) {
		Communication getParks = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			getParks.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		getParks.setTables(Arrays.asList("park"));
		getParks.setSelectColumns(Arrays.asList("*"));
		getParks.setWhereConditions(Arrays.asList("department"), Arrays.asList("="), Arrays.asList(departmentName));
		// sending the request to the server side
		GoNatureClientUI.client.accept(getParks);

		// getting the result
		if (getParks.getResultList().isEmpty()) { // no park like that
			return null;
		}

		// setting the park's data into a park object and returning it
		ArrayList<Park> departmentParks = new ArrayList<>();
		for (Object[] row : getParks.getResultList()) {
			Park park = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4],
					(String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8], (Integer) row[9],
					(Integer) row[10]);
			departmentParks.add(park);
		}
		return departmentParks;
	}
	
	/**
	 * Generates a usage report for a specific park over a given month and year. The report includes
	 * the daily visitor count within the specified time frame. This method constructs a SELECT query
	 * to fetch the total number of visitors for each day of visit within the selected month and year
	 * for the specified park. 
	 *
	 * @param selectedMonth The month for which the report is to be generated
	 * @param selectedYear  The year for which the report is to be generated
	 * @param park          The Park object for which the report is to be generated. This object must not
	 *                      be null and should contain valid park information.
	 * @return A list of objects, where each pair consists of a representing
	 *         a day within the specified month and year, and an Integer representing the total number of
	 *         visitors for that day.
	 */
	 public List<Pair<LocalDate, Integer>> generateUsageReport(String selectedMonth, String selectedYear, Park park) {
    	// Create a new Communication object for a query request
    	if (park == null) {
    		System.out.println("null");
    		throw new IllegalArgumentException("Park cannot be null");
    	}
    	String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_done_booking";
    	Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
    	// Set the type of the query
    	try {
    		comm.setQueryType(Communication.QueryType.SELECT);
    	} catch (CommunicationException e) {
    		e.printStackTrace();
    	}
    		comm.setTables(Arrays.asList(parkTableName)); 
    		comm.setSelectColumns(Arrays.asList("dayOfVisit", "numberOfVisitors"));    
    		int month=Integer.parseInt(selectedMonth);
    		int year=Integer.parseInt(selectedYear);
    		LocalDate from= LocalDate.of(year, month, 1);
    		LocalDate to=from.plusMonths(1).minusDays(1);
    		comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<=") , Arrays.asList(from,to)); 
    		// sending the request to the server side
    		GoNatureClientUI.client.accept(comm);
    		return processFetchedData(comm.getResultList());  		
	}

	 /**
	  * Processes data fetched from the database to aggregate visitor counts by date. This method
	  * takes a list of data entries, where each entry consists of a date and a corresponding number
	  * of visitors for that date. It then aggregates the visitor counts for each date and sorts the aggregated
	  * data chronologically.
	  *
	  * @param resultList A list of entries, where each Object array is expected to contain
	  *                   two elements: the first element (index 0) is representing the
	  *                   day of the visit, and the second element (index 1) is representing
	  *                   the number of visitors on that day.
	  * @return A sorted list of objects, each representing a unique date and the aggregated
	  *         number of visitors for that date.
	  *         Each Pair consists the key (representing the day of visit) and the value (representing the total number of visitors for that day).
	  */ 
    public List<Pair<LocalDate, Integer>> processFetchedData(List<Object[]> resultList) {
        Map<LocalDate, Integer> aggregatedData = new HashMap<>();
        for (Object[] row : resultList) {
        	LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Integer visitors = (Integer) row[1];
            aggregatedData.merge(date, visitors, Integer::sum);
        }
        
        // Convert aggregated data into a list of pairs
        List<Pair<LocalDate, Integer>> occupancyData = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : aggregatedData.entrySet()) {
            occupancyData.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
     // Sort the list based on LocalDate
        occupancyData.sort(Comparator.comparing(Pair::getKey));
        return occupancyData;
    }
    
    /**
     * Generates a report of the total number of visitors for a specific park, by visitor type,
     * over a given month and year. This method constructs a SELECT query to fetch counts of visitors based
     * on their visit type ('individual' or 'group') within the selected time frame for the specified park.
     *
     * @param selectedMonth The month for which the report is to be generated.
     * @param selectedYear  The year for which the report is to be generated.
     * @param park          The Park object for which the report is to be generated. This object must not
     *                      be null and should contain valid park information to identify the park in the database.
     * @return A Pair where the key is the total number of individual visitors and the value is the
     *         total number of group visitors for the specified park, month, and year. Returns a pair of zeros if no visitors 
     *         are found for the given criteria.
	 */
	public Pair<Integer, Integer> generateTotalNumberOfVisitorsReport(String selectedMonth,String selectedYear ,Park park) {
		// Create a new Communication object for a query request
		if (park == null) {
			System.out.println("null");
		    throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_done_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("visitType","numberOfVisitors")); 

	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	    LocalDate from= LocalDate.of(year, month, 1);
	    LocalDate to=from.plusMonths(1).minusDays(1);
	    comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<=") , Arrays.asList(from,to));
	 // sending the request to the server side
	    GoNatureClientUI.client.accept(comm);
	    
	    int countIndividual=0;
	    int countGroup=0;
	    if (comm.getResultList() != null) {
	    	for (Object[] row : comm.getResultList()) {
	        	if(((String)row[0]).equals("group"))
	        		countGroup+=(Integer)row[1];
	        	else
	        		countIndividual+=(Integer)row[1];
	    	}
	    }
	    Pair<Integer, Integer> pairResult = new Pair<>(countIndividual, countGroup);
		return pairResult;
	}
	//????????????????????????????????
    public  Map<String, List<XYChart.Data<Number, Number>>> generateVisitReport(String selectedMonth, String selectedYear, Park selectedPark) {
    	 // Check for null park
        if (selectedPark == null) {
            throw new IllegalArgumentException("Park cannot be null");
        }
		String parkTableName=ParkController.getInstance().nameOfTable(selectedPark)+"_park_done_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("entryParkTime", "exitParkTime", "visitType"));   
	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	    LocalDate from = LocalDate.of(year, month, 1);
	    LocalDate to = from.plusMonths(1).minusDays(1);
	    comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<="), Arrays.asList(from.toString(), to.toString())); 
	    // sending the request to the server side
	    GoNatureClientUI.client.accept(comm);
	    return processFetchedDataForChart(comm.getResultList()); 
	    
    }
    
    private Map<String, List<XYChart.Data<Number, Number>>> processFetchedDataForChart(List<Object[]> resultList) {
        List<XYChart.Data<Number, Number>> groupVisits = new ArrayList<>();
        List<XYChart.Data<Number, Number>> singleVisits = new ArrayList<>();
        
        for (Object[] row : resultList) {
            LocalTime entryTime = ((java.sql.Time) row[0]).toLocalTime();
            LocalTime exitTime = ((java.sql.Time) row[1]).toLocalTime();
            String visitType = (String) row[2];
            
            // Calculate the duration as the difference between exit time and entry time
            long durationInMinutes = Duration.between(entryTime, exitTime).toMinutes();
            double durationInHours = durationInMinutes / 60.0;
            
            // Convert entry time to a decimal format for the scatter chart
            double entryTimeDecimal = entryTime.getHour() + (entryTime.getMinute() / 60.0);
            
            if ("Group".equals(visitType)) {
                groupVisits.add(new XYChart.Data<>(entryTimeDecimal, durationInHours));
            } else { // Assuming "Individual" or any other type is treated as single visitor
                singleVisits.add(new XYChart.Data<>(entryTimeDecimal, durationInHours));
            }
        }
        
        Map<String, List<XYChart.Data<Number, Number>>> chartData = new HashMap<>();
        chartData.put("Group", groupVisits);
        chartData.put("Single", singleVisits);
        
        return chartData;
    }
 
    
	//????????????????????????????????
    public HashMap<Integer, Pair<Integer, Integer>> generateCancellationReport(String selectedMonth, String selectedYear, String selectedPark, Park park) {
		// Create a new Communication object for a query request
		if (park == null) {
		    throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_cancelled_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("dayOfVisit", "cancellationReason", "numberOfVisitors"));   
	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	 // Adjust where conditions to filter by month and year
	    comm.setWhereConditions(Arrays.asList("MONTH(dayOfVisit)", "YEAR(dayOfVisit)"),
	                            Arrays.asList("=", "AND", "="),
	                            Arrays.asList(selectedMonth, selectedYear));

	    String res1="Client cancelled the reminder";
	    String res2="Did not arrive";
	    //comm.setWhereConditions(Arrays.asList("cancellationReason","cancellationReason"),Arrays.asList("=", "AND", "=") , Arrays.asList(res1,res2));
	    GoNatureClientUI.client.accept(comm);
	    //dailyCounts map will contain a Pair for each day of the month, where the first item in the Pair is the count of cancellations and the second item is the count of no-shows.
	    HashMap<Integer, Pair<Integer, Integer>> dailyCounts = new HashMap<>();
	    if (comm.getResultList() != null) {
	        for (Object[] row : comm.getResultList()) {
	            // Extract the day from dayOfVisit
	            LocalDate date = LocalDate.parse((String)row[0]);
	            int day = date.getDayOfMonth();

	            Pair<Integer, Integer> counts = dailyCounts.getOrDefault(day, new Pair<>(0, 0));

	            if (((String)row[1]).equals(res1)) {
	                counts = new Pair<>(counts.getKey() + (Integer)row[2], counts.getValue());
	            } else if (((String)row[1]).equals(res2)) {
	                counts = new Pair<>(counts.getKey(), counts.getValue() + (Integer)row[2]);
	            }

	            dailyCounts.put(day, counts);
	        }
	    }

	    return dailyCounts;
	}

    /**
	 * @param savedParkManager the savedParkManager to be saved
	 */
    public void saveParkManager(ParkManager savedParkManager) {
    	this.savedParkManager = savedParkManager;
    }
    
    /**
	 * @return the saved park manager
	 */
    public ParkManager restoreParkManager() {
    	return savedParkManager;
    }
    /**
	 * @param saveDepartmentManager the savedDepartmentManager to be saved
	 */
    public void saveDepartmentManager(DepartmentManager savedDepartmentManager) {
    	this.savedDepartmentManager = savedDepartmentManager;
    }
    
    /**
	 * @return the saved department manager
	 */
    public DepartmentManager restoreDepartmentManager() {
    	return savedDepartmentManager;
    }

}
