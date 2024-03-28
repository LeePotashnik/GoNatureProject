package clientSide.control;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.CommunicationException;
import entities.Park;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;

/**
 * Controller class for managing report generation and data retrieval for park
 * visitation statistics. This class provides functionality to check for data
 * availability, generate various reports, and save report data.
 */
public class ReportsController {

	private static ReportsController instance;

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
	 * @param selectedYear  The year for which the report is to be generated.
	 * @param selectedPark  The Park object for which the report is to be generated.
	 * @param reportType    A String representing the type of report to check data
	 *                      availability for. This parameter allows the method to be
	 *                      flexible and used for different types of reports.
	 *
	 * @return boolean True if data is available for the specified parameters, False
	 *         otherwise. This allows calling methods to conditionally proceed with
	 *         report generation or to inform the user about the lack of data.
	 *
	 */
	public boolean isReportDataAvailable(String selectedMonth, String selectedYear, Park selectedPark,
			String reportType) {
		String parkTableName = ParkController.getInstance().nameOfTable(selectedPark)
				+ (reportType.equals("done") ? Communication.doneBookings : Communication.cancelledBookings);
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
			comm.setTables(Arrays.asList(parkTableName));
			comm.setSelectColumns(Arrays.asList("COUNT(*)"));
			LocalDate from = LocalDate.of(Integer.parseInt(selectedYear), Integer.parseInt(selectedMonth), 1);
			LocalDate to = from.plusMonths(1).minusDays(1);
			comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="),
					Arrays.asList(from, to));

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
	 * Checks if a specific report from the park manager to the department manager
	 * is available in the report tables for a given month, year, and park.
	 *
	 * @param selectedMonth The month for which the report's availability is to be
	 *                      checked.
	 * @param selectedYear  The year for which the report's availability is to be
	 *                      checked.
	 * @param park          The Park object for which the report's availability is
	 *                      to be checked.
	 * @param reportType    The type of the report to check for ("total_visitors" or
	 *                      "usage").
	 * @return true if the report is available, false otherwise.
	 */
	public boolean isParkManagerReportAvailable(String selectedMonth, String selectedYear, Park park,
			String reportType) {
		String reportTableName = reportType.equals("total_visitors") ? Communication.totalReport
				: Communication.usageReport;
		// String parkName = park.getParkName();
		String parkTableName = ParkController.getInstance().nameOfTable(park);
		if (park == null || selectedMonth == null || selectedYear == null) {
			return false;
		}
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		comm.setTables(Arrays.asList(reportTableName));
		comm.setSelectColumns(Arrays.asList("park_name", "date"));
		int month = Integer.parseInt(selectedMonth);
		int year = Integer.parseInt(selectedYear);
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1).minusDays(1);
		comm.setWhereConditions(Arrays.asList("date", "date", "park_name"),
				Arrays.asList(">=", "AND", "<=", "AND", "="),
				Arrays.asList(from.toString(), to.toString(), parkTableName));
		GoNatureClientUI.client.accept(comm);
		if (comm.getResultList() != null && !comm.getResultList().isEmpty()) {
			// The report for this date and park already exists in the database
			return true;
		}
		return false;
	}

	/**
	 * Generates a usage report for a specific park over a given month and year. The
	 * report includes the daily visitor count within the specified time frame. This
	 * method constructs a SELECT query to fetch the total number of visitors for
	 * each day of visit within the selected month and year for the specified park.
	 *
	 * @param selectedMonth The month for which the report is to be generated
	 * @param selectedYear  The year for which the report is to be generated
	 * @param park          The Park object for which the report is to be generated.
	 *                      This object must not be null and should contain valid
	 *                      park information.
	 * @return A list of objects, where each pair consists of a representing a day
	 *         within the specified month and year, and an Integer representing the
	 *         total number of visitors for that day.
	 */
	public List<Pair<LocalDate, Integer>> generateUsageReport(String selectedMonth, String selectedYear, Park park) {
		if (park == null) {
			throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName = ParkController.getInstance().nameOfTable(park) + Communication.doneBookings;
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		// Set the type of the query
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		comm.setTables(Arrays.asList(parkTableName));
		comm.setSelectColumns(Arrays.asList("dayOfVisit", "numberOfVisitors"));
		int month = Integer.parseInt(selectedMonth);
		int year = Integer.parseInt(selectedYear);
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1).minusDays(1);
		comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="),
				Arrays.asList(from, to));
		// sending the request to the server side
		GoNatureClientUI.client.accept(comm);
		return processFetchedData(comm.getResultList());
	}

	/**
	 * Attempts to save a usage report for a given park, month, and year in the
	 * database. It first checks if a report for the specified park and date range
	 * already exists to avoid duplicate entries. If no such report exists, it
	 * proceeds to insert a new record with the given park name and date into the
	 * 'usage_report' table.
	 *
	 * @param selectedMonth The month for which the report is being saved.
	 * @param selectedYear  The year for which the report is being saved.
	 * @param park          The Park object representing the park for which the
	 *                      report is being saved.
	 * @return true if the insert operation is successful, false if the report
	 *         already exists or if an error occurs during the operation.
	 */
	public boolean saveUsageReport(String selectedMonth, String selectedYear, Park park) {
		// Get the current date
		LocalDate currentDate = LocalDate.now();
		// Parse the selected year and month
		int selectedMonthInt = Integer.parseInt(selectedMonth);
		int selectedYearInt = Integer.parseInt(selectedYear);
		YearMonth selectedYearMonth = YearMonth.of(selectedYearInt, selectedMonthInt);
		// Get the last day of the selected month
		LocalDate lastDayOfSelectedMonth = selectedYearMonth.atEndOfMonth();

		// Check if the selected month is the current month and if it has not ended
		if (!currentDate.isAfter(lastDayOfSelectedMonth)) {
			// The report for the future date cannot be saved
			return false;
		}
		// Create a date string for the last day of the selected month
		String date = selectedYear + "-" + selectedMonth + "-" + lastDayOfSelectedMonth.getDayOfMonth();
		String parkTableName = ParkController.getInstance().nameOfTable(park);

		// Set the type of the query
		try {
			Communication select = new Communication(Communication.CommunicationType.QUERY_REQUEST);
			select.setQueryType(Communication.QueryType.SELECT);
			select.setTables(Arrays.asList(Communication.usageReport));
			select.setSelectColumns(Arrays.asList("park_name", "date"));
			int month = Integer.parseInt(selectedMonth);
			int year = Integer.parseInt(selectedYear);
			LocalDate from = LocalDate.of(year, month, 1);
			LocalDate to = from.plusMonths(1).minusDays(1);
			select.setWhereConditions(Arrays.asList("date", "date", "park_name"),
					Arrays.asList(">=", "AND", "<=", "AND", "="),
					Arrays.asList(from.toString(), to.toString(), parkTableName));
			GoNatureClientUI.client.accept(select);
			if (select.getResultList() != null && !select.getResultList().isEmpty()) {
				// The report for this date and park already exists in the database
				return false;
			}
			Communication comm = new Communication(CommunicationType.QUERY_REQUEST);
			comm.setQueryType(Communication.QueryType.INSERT);
			comm.setTables(Collections.singletonList(Communication.usageReport));
			comm.setColumnsAndValues(Arrays.asList("park_name", "date"), Arrays.asList(parkTableName, date));
			// Send the insert operation request
			GoNatureClientUI.client.accept(comm);
			return comm.getQueryResult();
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Processes data fetched from the database to aggregate visitor counts by date.
	 * This method takes a list of data entries, where each entry consists of a date
	 * and a corresponding number of visitors for that date. It then aggregates the
	 * visitor counts for each date and sorts the aggregated data chronologically.
	 *
	 * @param resultList A list of entries, where each Object array is expected to
	 *                   contain two elements: the first element (index 0) is
	 *                   representing the day of the visit, and the second element
	 *                   (index 1) is representing the number of visitors on that
	 *                   day.
	 * @return A sorted list of objects, each representing a unique date and the
	 *         aggregated number of visitors for that date. Each Pair consists the
	 *         key (representing the day of visit) and the value (representing the
	 *         total number of visitors for that day).
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
	 * Generates a report of the total number of visitors for a specific park, by
	 * visitor type, over a given month and year. This method constructs a SELECT
	 * query to fetch counts of visitors based on their visit type ('individual' or
	 * 'group') within the selected time frame for the specified park.
	 *
	 * @param selectedMonth The month for which the report is to be generated.
	 * @param selectedYear  The year for which the report is to be generated.
	 * @param park          The Park object for which the report is to be generated.
	 *                      This object must not be null and should contain valid
	 *                      park information to identify the park in the database.
	 * @return A Pair where the key is the total number of individual visitors and
	 *         the value is the total number of group visitors for the specified
	 *         park, month, and year. Returns a pair of zeros if no visitors are
	 *         found for the given criteria.
	 */
	public Pair<Integer, Integer> generateTotalNumberOfVisitorsReport(String selectedMonth, String selectedYear,
			Park park) {
		// Create a new Communication object for a query request
		if (park == null) {
			throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName = ParkController.getInstance().nameOfTable(park) + Communication.doneBookings;
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		// Set the type of the query
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		comm.setTables(Arrays.asList(parkTableName));
		comm.setSelectColumns(Arrays.asList("visitType", "numberOfVisitors"));

		int month = Integer.parseInt(selectedMonth);
		int year = Integer.parseInt(selectedYear);
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1).minusDays(1);
		comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="),
				Arrays.asList(from, to));
		// sending the request to the server side
		GoNatureClientUI.client.accept(comm);

		int countIndividual = 0;
		int countGroup = 0;
		if (comm.getResultList() != null) {
			for (Object[] row : comm.getResultList()) {
				if (((String) row[0]).equals("group"))
					countGroup += (Integer) row[1];
				else
					countIndividual += (Integer) row[1];
			}
		}
		Pair<Integer, Integer> pairResult = new Pair<>(countIndividual, countGroup);
		return pairResult;
	}

	/**
	 * Attempts to save a total number of visitors report for a given park, month,
	 * and year in the database. It first checks if a report for the specified park
	 * and date range already exists to avoid duplicate entries. If no such report
	 * exists, it proceeds to insert a new record with the given park name and date
	 * into the 'total_number_of_visitors_report' table.
	 *
	 * @param selectedMonth The month for which the report is being saved.
	 * @param selectedYear  The year for which the report is being saved.
	 * @param park          The Park object representing the park for which the
	 *                      report is being saved.
	 * @return true if the insert operation is successful, false if the report
	 *         already exists or if an error occurs during the operation.
	 */
	public boolean saveTotalNumberOfVisitorsReport(String selectedMonth, String selectedYear, Park park) {
		// Get the current date
		LocalDate currentDate = LocalDate.now();
		// Parse the selected year and month
		int selectedMonthInt = Integer.parseInt(selectedMonth);
		int selectedYearInt = Integer.parseInt(selectedYear);
		YearMonth selectedYearMonth = YearMonth.of(selectedYearInt, selectedMonthInt);
		// Get the last day of the selected month
		LocalDate lastDayOfSelectedMonth = selectedYearMonth.atEndOfMonth();

		// Check if the selected month is the current month and if it has not ended
		if (!currentDate.isAfter(lastDayOfSelectedMonth)) {
			// The report for the future date cannot be saved
			return false;
		}
		// Create a date string for the last day of the selected month
		String date = selectedYear + "-" + selectedMonth + "-" + lastDayOfSelectedMonth.getDayOfMonth();
		String parkTableName = ParkController.getInstance().nameOfTable(park);
		String tableName = Communication.totalReport;

		// Set the type of the query
		try {
			Communication select = new Communication(Communication.CommunicationType.QUERY_REQUEST);
			select.setQueryType(Communication.QueryType.SELECT);
			select.setTables(Arrays.asList(tableName));
			select.setSelectColumns(Arrays.asList("park_name", "date"));
			int month = Integer.parseInt(selectedMonth);
			int year = Integer.parseInt(selectedYear);
			LocalDate from = LocalDate.of(year, month, 1);
			LocalDate to = from.plusMonths(1).minusDays(1);
			select.setWhereConditions(Arrays.asList("date", "date", "park_name"),
					Arrays.asList(">=", "AND", "<=", "AND", "="),
					Arrays.asList(from.toString(), to.toString(), parkTableName));
			GoNatureClientUI.client.accept(select);
			if (select.getResultList() != null && !select.getResultList().isEmpty()) {
				// The report for this date and park already exists in the database
				return false;
			}
			Communication comm = new Communication(CommunicationType.QUERY_REQUEST);
			comm.setQueryType(Communication.QueryType.INSERT);
			comm.setTables(Collections.singletonList(tableName));
			comm.setColumnsAndValues(Arrays.asList("park_name", "date"), Arrays.asList(parkTableName, date));
			// Send the insert operation request
			GoNatureClientUI.client.accept(comm);
			return comm.getQueryResult();
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Generates a report for park visits within a specified month and year for a
	 * given park. This report differentiates visits by type (e.g., group or
	 * individual) and calculates the visit duration.
	 * 
	 * @param selectedMonth The month for which the report is being generated.
	 * @param selectedYear  The year for which the report is being generated.
	 * @param selectedPark  The park for which the report is being generated.
	 * @return A map with visit types as keys (e.g., "Group", "Individual Visitor")
	 *         and lists of XYChart.Data objects as values. Each XYChart.Data object
	 *         represents a visit's entry time and duration.
	 */
	public Map<String, List<XYChart.Data<Number, Number>>> generateVisitReport(String selectedMonth,
			String selectedYear, Park selectedPark) {
		// Ensures the selectedPark parameter is not null.
		if (selectedPark == null) {
			throw new IllegalArgumentException("Park cannot be null");
		}

		// Construct the name of the database table for the park's bookings.
		String parkTableName = ParkController.getInstance().nameOfTable(selectedPark) + Communication.doneBookings;

		// Initialize a new communication request to query the database.
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		// Set the query parameters including the table name, columns to select, and
		// date range.
		comm.setTables(Arrays.asList(parkTableName));
		comm.setSelectColumns(Arrays.asList("entryParkTime", "exitParkTime", "visitType"));
		int month = Integer.parseInt(selectedMonth);
		int year = Integer.parseInt(selectedYear);
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1).minusDays(1);
		comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="),
				Arrays.asList(from, to));

		// Send the request to the server and return the processed data for charting.
		GoNatureClientUI.client.accept(comm);
		return processFetchedDataForChart(comm.getResultList());
	}

	/**
	 * Processes fetched data for chart visualization. It calculates the duration of
	 * each visit based on entry and exit times and categorizes them by visit type.
	 * 
	 * @param resultList The list of objects arrays fetched from the database, where
	 *                   each array contains information about a single visit.
	 * @return A map with keys as visit types and values as lists of XYChart.Data,
	 *         each representing a point in the chart for a specific visit type and
	 *         duration.
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
			} else if ("individual".equals(visitType)) {
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
	 * Generates a report detailing the reasons for booking cancellations within a
	 * specific month and year for a given park. This report includes average
	 * numbers of visitors who either cancelled their booking or did not show up.
	 *
	 * @param selectedMonth The month for which the report is being generated.
	 * @param selectedYear  The year for which the report is being generated.
	 * @param selectedPark  The park for which the report is being generated.
	 * @return A map with keys representing cancellation reasons and values as lists
	 *         of XYChart.Data objects. Each XYChart.Data object pairs a day of the
	 *         week with the average number of cancellations for that reason.
	 */
	public Map<String, List<XYChart.Data<String, Number>>> generateCancellationReport(String selectedMonth,
			String selectedYear, Park selectedPark) {
		// Ensure the park parameter is not null
		if (selectedPark == null) {
			throw new IllegalArgumentException("Park cannot be null");
		}
		// Prepare a database query to select cancellation data for the specified park
		String parkTableName = ParkController.getInstance().nameOfTable(selectedPark) + Communication.cancelledBookings;
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
		// Set the type of the query
		try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		// Configure the query with table name, columns to select, and the date range
		// for filtering.
		comm.setTables(Arrays.asList(parkTableName));
		comm.setSelectColumns(Arrays.asList("dayOfVisit", "cancellationReason", "numberOfVisitors"));
		int month = Integer.parseInt(selectedMonth);
		int year = Integer.parseInt(selectedYear);
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1).minusDays(1);
		comm.setWhereConditions(Arrays.asList("dayOfVisit", "dayOfVisit"), Arrays.asList(">=", "AND", "<="),
				Arrays.asList(from, to));
		// Send the query request to the server and process the returned results for
		// charting
		GoNatureClientUI.client.accept(comm);
		return processFetchedCancellationDataForChart(comm.getResultList());
	}

	/**
	 * Processes fetched data to prepare it for visualization in a chart. This
	 * method calculates the average number of visitors for each cancellation reason
	 * and organizes the data by days of the week.
	 *
	 * @param resultList A list of object arrays, each representing a row from the
	 *                   database query result. Expected data includes the day of
	 *                   visit, cancellation reason, and number of visitors.
	 * @return A map where keys are cancellation reasons and values are lists of
	 *         XYChart.Data objects, each representing the average number of
	 *         cancellations for a day of the week.
	 */
	private Map<String, List<XYChart.Data<String, Number>>> processFetchedCancellationDataForChart(
			List<Object[]> resultList) {
		Map<String, List<Integer>> cancelledOrdersStats = new HashMap<>();
		Map<String, List<Integer>> noShowVisitorsStats = new HashMap<>();

		// Initialize lists for each day of the week
		for (DayOfWeek day : DayOfWeek.values()) {
			String dayName = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
			cancelledOrdersStats.put(dayName, new ArrayList<>());
			noShowVisitorsStats.put(dayName, new ArrayList<>());
		}

		// Process the results
		for (Object[] row : resultList) {
			LocalDate dayOfVisit = ((java.sql.Date) row[0]).toLocalDate();
			String cancellationReason = (String) row[1];
			int numberOfVisitors = (int) row[2];

			String dayName = dayOfVisit.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

			if (Communication.userCancelled.equals(cancellationReason)
					|| Communication.userDidNotConfirm.equals(cancellationReason)) {
				cancelledOrdersStats.get(dayName).add(numberOfVisitors);
			} else if (Communication.userDidNotArrive.equals(cancellationReason)) {
				noShowVisitorsStats.get(dayName).add(numberOfVisitors);
			}
		}

		// Calculate averages and prepare chart data
		List<XYChart.Data<String, Number>> cancelledOrdersAvgData = cancelledOrdersStats.entrySet().stream()
				.map(e -> new XYChart.Data<>(e.getKey(),
						e.getValue().isEmpty() ? 0
								: (Number) (e.getValue().stream().mapToInt(Integer::intValue).sum()
										/ (double) e.getValue().size())))
				.collect(Collectors.toList());
		List<XYChart.Data<String, Number>> noShowVisitorsAvgData = noShowVisitorsStats.entrySet().stream()
				.map(e -> new XYChart.Data<>(e.getKey(),
						e.getValue().isEmpty() ? 0
								: (Number) (e.getValue().stream().mapToInt(Integer::intValue).sum()
										/ (double) e.getValue().size())))
				.collect(Collectors.toList());

		Map<String, List<XYChart.Data<String, Number>>> chartData = new HashMap<>();
		chartData.put(Communication.userCancelled, cancelledOrdersAvgData);
		chartData.put(Communication.userDidNotConfirm, cancelledOrdersAvgData);
		chartData.put(Communication.userDidNotArrive, noShowVisitorsAvgData);

		return chartData;
	}
}