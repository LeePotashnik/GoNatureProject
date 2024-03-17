package clientSide.control;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkManager;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;

public class ReportsController {
	
	private static ReportsController instance;
	// for saving and restoring purposes of the screen
	private ParkManager savedParkManager;
	private DepartmentManager savedDepartmentManager;

	/**
	 * An empty and private controller, for the singleton design pattern
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
	 * Checks if report data is available for a specific park, month, and year. 
	 *
	 * @param selectedMonth The month for which the report is to be generated. 
	 * @param selectedYear The year for which the report is to be generated.
	 * @param selectedPark The Park object for which the report is to be generated. 
	 * @param reportType A String representing the type of report to check data availability for. This parameter allows the method
	 *                   to be flexible and used for different types of reports. 
	 *
	 * @return boolean True if data is available for the specified parameters, False otherwise. This allows calling methods
	 *                 to conditionally proceed with report generation or to inform the user about the lack of data.
	 *
	 */
	public boolean isReportDataAvailable(String selectedMonth, String selectedYear, Park selectedPark, String reportType) {
	    String parkTableName = ParkController.getInstance().nameOfTable(selectedPark) + "_park_" + reportType + "_booking";
	    Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    try {
	        comm.setQueryType(Communication.QueryType.SELECT);
	        comm.setTables(Arrays.asList(parkTableName));
	        comm.setSelectColumns(Arrays.asList("COUNT(*)")); 
	        LocalDate from = LocalDate.of(Integer.parseInt(selectedYear), Integer.parseInt(selectedMonth), 1);
	        LocalDate to = from.plusMonths(1).minusDays(1);
	        comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="), Arrays.asList(from, to));

	        // Sending the request to the server side
	        GoNatureClientUI.client.accept(comm);

	        // If count is more than 0, data is available
	        if (!comm.getResultList().isEmpty() && (Long) comm.getResultList().get(0)[0] > 0) {
	            return true;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false;
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
    	if (park == null) {
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
	

/**
 * Generates a report for park visits within a specified month and year for a given park.
 * This report differentiates visits by type (e.g., group or individual) and calculates
 * the visit duration.
 * 
 * @param selectedMonth The month for which the report is being generated.
 * @param selectedYear The year for which the report is being generated.
 * @param selectedPark The park for which the report is being generated.
 * @return A map with visit types as keys (e.g., "Group", "Individual Visitor") and lists of XYChart.Data
 *         objects as values. Each XYChart.Data object represents a visit's entry time and duration.
 */
	public Map<String, List<XYChart.Data<Number, Number>>> generateVisitReport(String selectedMonth, String selectedYear, Park selectedPark) {
    // Ensures the selectedPark parameter is not null.
		if (selectedPark == null) {
			throw new IllegalArgumentException("Park cannot be null");
		}

		// Construct the name of the database table for the park's bookings.
		String parkTableName = ParkController.getInstance().nameOfTable(selectedPark) + "_park_done_booking";

		// Initialize a new communication request to query the database.
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

	    // Set the query parameters including the table name, columns to select, and date range.
	    comm.setTables(Arrays.asList(parkTableName));
	    comm.setSelectColumns(Arrays.asList("entryParkTime", "exitParkTime", "visitType"));
	    int month = Integer.parseInt(selectedMonth);
	    int year = Integer.parseInt(selectedYear);
	    LocalDate from = LocalDate.of(year, month, 1);
	    LocalDate to = from.plusMonths(1).minusDays(1);
	    comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"), Arrays.asList(">=", "AND", "<="), Arrays.asList(from, to));
	
	    // Send the request to the server and return the processed data for charting.
	    GoNatureClientUI.client.accept(comm);
	    return processFetchedDataForChart(comm.getResultList());
}
	
	/**
	 * Processes fetched data for chart visualization. It calculates the duration of each visit
	 * based on entry and exit times and categorizes them by visit type.
	 * 
	 * @param resultList The list of objects arrays fetched from the database, where each array
	 *                   contains information about a single visit.
	 * @return A map with keys as visit types and values as lists of XYChart.Data, each representing
	 *         a point in the chart for a specific visit type and duration.
	 */
    private Map<String, List<XYChart.Data<Number, Number>>> processFetchedDataForChart(List<Object[]> resultList) {
        List<XYChart.Data<Number, Number>> groupVisits = new ArrayList<>();
        List<XYChart.Data<Number, Number>> individualVisits = new ArrayList<>();
        
        for (Object[] row : resultList) {
            LocalTime entryTime = ((java.sql.Time) row[0]).toLocalTime();
            LocalTime exitTime = ((java.sql.Time) row[1]).toLocalTime();
            String visitType = (String) row[2];
            
            // Calculate visit duration
            long durationInMinutes = Duration.between(entryTime, exitTime).toMinutes();
            double durationInHours = durationInMinutes / 60.0;
            
            // Convert entry time to a decimal format for the scatter chart
            double entryTimeDecimal = entryTime.getHour() + (entryTime.getMinute() / 60.0);
            
            if ("group".equals(visitType)) {
                groupVisits.add(new XYChart.Data<>(entryTimeDecimal, durationInHours));
            } 
            else if("individual".equals(visitType)) {  
            	individualVisits.add(new XYChart.Data<>(entryTimeDecimal, durationInHours));
            }
        }
        // Compile and return the chart data categorized by visit type.
        Map<String, List<XYChart.Data<Number, Number>>> chartData = new HashMap<>();
        chartData.put("Group", groupVisits);
        chartData.put("Individual Visitor", individualVisits);
        System.out.println(chartData);
        return chartData;
    }
 
    
    /**
     * Generates a report detailing the reasons for booking cancellations within a specific month and year for a given park.
     * This report includes average numbers of visitors who either cancelled their booking or did not show up.
     *
     * @param selectedMonth The month for which the report is being generated.
     * @param selectedYear The year for which the report is being generated.
     * @param selectedPark The park for which the report is being generated.
     * @return A map with keys representing cancellation reasons and values as lists of XYChart.Data objects.
     *         Each XYChart.Data object pairs a day of the week with the average number of cancellations for that reason.
     */
    public  Map<String, List<XYChart.Data<String, Number>>> generateCancellationReport(String selectedMonth, String selectedYear, Park selectedPark) {
    	// Ensure the park parameter is not null
		if (selectedPark == null) {
		    throw new IllegalArgumentException("Park cannot be null");
		}
		// Prepare a database query to select cancellation data for the specified park
		String parkTableName=ParkController.getInstance().nameOfTable(selectedPark)+"_park_cancelled_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    // Configure the query with table name, columns to select, and the date range for filtering.
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("dayOfVisit", "cancellationReason", "numberOfVisitors"));   
	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	    LocalDate from = LocalDate.of(year, month, 1);
	    LocalDate to = from.plusMonths(1).minusDays(1);
	    comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<="), Arrays.asList(from, to)); 
	    // Send the query request to the server and process the returned results for charting
	    GoNatureClientUI.client.accept(comm);
	    return processFetchedCancellationDataForChart(comm.getResultList());
	  
	}
    /**
     * Processes fetched data to prepare it for visualization in a chart. This method calculates the average number of
     * visitors for each cancellation reason and organizes the data by days of the week.
     *
     * @param resultList A list of object arrays, each representing a row from the database query result.
     *                   Expected data includes the day of visit, cancellation reason, and number of visitors.
     * @return A map where keys are cancellation reasons and values are lists of XYChart.Data objects,
     *         each representing the average number of cancellations for a day of the week.
     */
    private Map<String, List<XYChart.Data<String, Number>>> processFetchedCancellationDataForChart(List<Object[]> resultList) {
    	Map<String, Pair<Integer, Integer>> cancelledOrdersStats = new HashMap<>();
        Map<String, Pair<Integer, Integer>> noShowVisitorsStats = new HashMap<>();

        // Fill the maps with zeros for each day of the week
        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            cancelledOrdersStats.put(dayName, new Pair<>(0, 0)); // (total, count)
            noShowVisitorsStats.put(dayName, new Pair<>(0, 0)); // (total, count)
        }

        // Process the results from the database
        for (Object[] row : resultList) {
            LocalDate dayOfVisit = ((java.sql.Date) row[0]).toLocalDate();
            String cancellationReason = (String) row[1];
            int numberOfVisitors = (int) row[2];

            String dayName = dayOfVisit.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if ("Client cancelled the reminder".equals(cancellationReason)) {
                Pair<Integer, Integer> stats = cancelledOrdersStats.get(dayName);
                cancelledOrdersStats.put(dayName, new Pair<>(stats.getKey() + numberOfVisitors, stats.getValue() + 1));
            } else if ("Did not arrive".equals(cancellationReason)) {
                Pair<Integer, Integer> stats = noShowVisitorsStats.get(dayName);
                noShowVisitorsStats.put(dayName, new Pair<>(stats.getKey() + numberOfVisitors, stats.getValue() + 1));
            }
        }

        // Calculate averages and prepare chart data
        List<XYChart.Data<String, Number>> cancelledOrdersAvgData = cancelledOrdersStats.entrySet().stream()
              .map(e -> new XYChart.Data<String, Number>(e.getKey(), (Number) (e.getValue().getKey() / (double) e.getValue().getValue())))
              .collect(Collectors.toList());
        List<XYChart.Data<String, Number>> noShowVisitorsAvgData = noShowVisitorsStats.entrySet().stream()
              .map(e -> new XYChart.Data<String, Number>(e.getKey(), (Number) (e.getValue().getKey() / (double) e.getValue().getValue())))
              .collect(Collectors.toList());

        Map<String, List<XYChart.Data<String, Number>>> chartData = new HashMap<>();
        chartData.put("Client cancelled the reminder", cancelledOrdersAvgData);
        chartData.put("Did not arrive", noShowVisitorsAvgData);

        return chartData;
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
