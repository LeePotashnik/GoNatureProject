package clientSide.control;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Date;
import java.sql.Time;
import java.util.Random;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import entities.Booking;
import entities.DepartmentManager;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.PendingAdjustment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ParametersController {

	/**
	 * This method gets parkManager and maximum visitor capacity parameters and add
	 * to the pending adjustment table a request to adjust the parks maximum visitor
	 * capacity (if there is no an existing request)
	 * 
	 * @return true if added the request, false otherwise
	 */
	public static boolean adjustMaximumVisitorsCapacity(ParkManager parkManager, int parameterAfter) {
		String parkName = parkManager.getManages();

		// Creating a select query in order to make sure there is no an exist request to
		// adjust this park's maximum visitor capacity
		Communication requestForPendingAdjustment = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForPendingAdjustment.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForPendingAdjustment.setTables(Arrays.asList("pending_adjustment"));
		requestForPendingAdjustment.setSelectColumns(Arrays.asList("*"));
		requestForPendingAdjustment.setWhereConditions(Arrays.asList("parkName", "parameterType"),
				Arrays.asList("=", "AND", "="), Arrays.asList(parkName, "maximumVisitorsCapacity"));

		GoNatureClientUI.client.accept(requestForPendingAdjustment); // sending the query to the server that will
																		// connect to the DB

		// getResultList will return list if there is already an exist update request
		// for this park & type of parameters
		if (requestForPendingAdjustment.getResultList().isEmpty()) {
			// there is no an exist update request for this park & type of parameters=>
			// insert this RequestForUpdateParkMaximumVisitorsCapacity to the
			// pending_adjustment table:

			// Creating the parameters we want to insert into the table:
			String adjusmentId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			int parkId = parkManager.getParkObject().getParkId();
			String department = parkManager.getParkObject().getParkDepartment();
			LocalDate dayOfAdjusting = LocalDate.now();
			LocalTime timeOfAdjusting = LocalTime.now();
			String adjustedBy = parkManager.getFirstName() + " " + parkManager.getLastName();
			int parameterBefore = parkManager.getParkObject().getMaximumVisitors();

			// Creating an insert query in order to add
			// RequestForUpdateParkMaximumVisitorsCapacity to the pending_adjustment table
			Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			RequestForUpdateParkparameter.setTables(Arrays.asList("pending_adjustment"));
			RequestForUpdateParkparameter.setColumnsAndValues(
					Arrays.asList("adjusmentId", "parkId", "parkName", "department", "dayOfAdjusting",
							"timeOfAdjusting", "adjustedBy", "parameterBefore", "parameterAfter", "parameterType"),
					Arrays.asList(adjusmentId, parkId, parkName, department, dayOfAdjusting, timeOfAdjusting,
							adjustedBy, parameterBefore, parameterAfter, "maximumVisitorsCapacity"));

			GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																			// connect to the DB

			// getQueryResult will return true if the insert query was successful, false
			// otherwise
			return RequestForUpdateParkparameter.getQueryResult();

		}
		return false; // there is already an exist update request for this park & type of parameters /
						// the insert failed
	}

	/**
	 * This method gets parkManager and maximum Orders capacity parameters and add
	 * to the pending adjustment table a request to adjust the parks maximum orders
	 * capacity (if there is no an existing request)
	 * 
	 * @return true if added the request, false otherwise
	 */
	public static boolean adjustMaximumOrdersAmount(ParkManager parkManager, int parameterAfter) {
		String parkName = parkManager.getManages();

		// Creating a select query in order to make sure there is no an exist request to
		// adjust this park's maximum orders capacity
		Communication requestForPendingAdjustment = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForPendingAdjustment.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForPendingAdjustment.setTables(Arrays.asList("pending_adjustment"));
		requestForPendingAdjustment.setSelectColumns(Arrays.asList("*"));
		requestForPendingAdjustment.setWhereConditions(Arrays.asList("parkName", "parameterType"),
				Arrays.asList("=", "AND", "="), Arrays.asList(parkName, "maximumOrderesCapacity"));

		GoNatureClientUI.client.accept(requestForPendingAdjustment); // sending the query to the server that will
																		// connect to the DB

		// getResultList will return list if there is already an exist update request
		// for this park & type of parameters
		if (requestForPendingAdjustment.getResultList().isEmpty()) {
			// there is no an exist update request for this park & type of parameters=>
			// insert this RequestForUpdateParkMaximumOrdersCapacity to the
			// pending_adjustment table:

			// Creating the parameters we want to insert into the table:
			String adjusmentId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			int parkId = parkManager.getParkObject().getParkId();
			String department = parkManager.getParkObject().getParkDepartment();
			LocalDate dayOfAdjusting = LocalDate.now();
			LocalTime timeOfAdjusting = LocalTime.now();
			String adjustedBy = parkManager.getFirstName() + " " + parkManager.getLastName();
			int parameterBefore = parkManager.getParkObject().getMaximumOrders();

			// Creating an insert query in order to add
			// RequestForUpdateParkMaximumOrderesCapacity to the pending_adjustment table
			Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			RequestForUpdateParkparameter.setTables(Arrays.asList("pending_adjustment"));
			RequestForUpdateParkparameter.setColumnsAndValues(
					Arrays.asList("adjusmentId", "parkId", "parkName", "department", "dayOfAdjusting",
							"timeOfAdjusting", "adjustedBy", "parameterBefore", "parameterAfter", "parameterType"),
					Arrays.asList(adjusmentId, parkId, parkName, department, dayOfAdjusting, timeOfAdjusting,
							adjustedBy, parameterBefore, parameterAfter, "maximumOrderesCapacity"));

			GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																			// connect to the DB

			// getQueryResult will return true if the insert query was successful, false
			// otherwise
			return RequestForUpdateParkparameter.getQueryResult();

		}
		return false; // there is already an exist update request for this park & type of parameters /
						// the insert failed
	}

	/**
	 * This method gets parkManager and time limit parameters and add to the pending
	 * adjustment table a request to adjust the parks time limit (if there is no an
	 * existing request)
	 * 
	 * @return true if added the request, false otherwise
	 */
	public static boolean adjustMaximumTimeLimit(ParkManager parkManager, int parameterAfter) {
		String parkName = parkManager.getManages();

		// Creating a select query in order to make sure there is no an exist request to
		// adjust this park's Time Limit
		Communication requestForPendingAdjustment = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForPendingAdjustment.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForPendingAdjustment.setTables(Arrays.asList("pending_adjustment"));
		requestForPendingAdjustment.setSelectColumns(Arrays.asList("*"));
		requestForPendingAdjustment.setWhereConditions(Arrays.asList("parkName", "parameterType"),
				Arrays.asList("=", "AND", "="), Arrays.asList(parkName, "maximumTimeLimit"));

		GoNatureClientUI.client.accept(requestForPendingAdjustment); // sending the query to the server that will
																		// connect to the DB

		// getResultList will return list if there is already an exist update request
		// for this park & type of parameters
		if (requestForPendingAdjustment.getResultList().isEmpty()) {
			// there is no an exist update request for this park & type of parameters=>
			// insert this RequestForUpdateParkTimeLimit to the pending_adjustment table:

			// Creating the parameters we want to insert into the table:
			String adjusmentId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			int parkId = parkManager.getParkObject().getParkId();
			String department = parkManager.getParkObject().getParkDepartment();
			LocalDate dayOfAdjusting = LocalDate.now();
			LocalTime timeOfAdjusting = LocalTime.now();
			String adjustedBy = parkManager.getFirstName() + " " + parkManager.getLastName();
			int parameterBefore = parkManager.getParkObject().getTimeLimit();

			// Creating an insert query in order to add RequestForUpdateParkTimeLimit to the
			// pending_adjustment table
			Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			RequestForUpdateParkparameter.setTables(Arrays.asList("pending_adjustment"));
			RequestForUpdateParkparameter.setColumnsAndValues(
					Arrays.asList("adjusmentId", "parkId", "parkName", "department", "dayOfAdjusting",
							"timeOfAdjusting", "adjustedBy", "parameterBefore", "parameterAfter", "parameterType"),
					Arrays.asList(adjusmentId, parkId, parkName, department, dayOfAdjusting, timeOfAdjusting,
							adjustedBy, parameterBefore, parameterAfter, "maximumTimeLimit"));

			GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																			// connect to the DB

			// getQueryResult will return true if the insert query was successful, false
			// otherwise
			return RequestForUpdateParkparameter.getQueryResult();

		}
		return false; // there is already an exist update request for this park & type of parameters /
						// the insert failed
	}

	/**
	 * This method gets DepartmentManager takes all the rows in the
	 * pending_adjustment table (that are relevant to a specific department) into a
	 * ArrayList of PendingAdjustment objects and convert it to ObservableList of
	 * PendingAdjustment objects
	 * 
	 * @return ObservableList of PendingAdjustment objects
	 */
	public ObservableList<PendingAdjustment> getParameterAdjustmentListForDepartment(
			DepartmentManager departmentManager) {

		String managesDepartment = departmentManager.getManagesDepartment();

		// Creating a select query in order to get the adjustment the relevant to the
		// department of this manager
		Communication requestForPendingAdjustment = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForPendingAdjustment.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForPendingAdjustment.setTables(Arrays.asList("pending_adjustment"));
		requestForPendingAdjustment.setSelectColumns(Arrays.asList("*"));
		requestForPendingAdjustment.setWhereConditions(Arrays.asList("department"), Arrays.asList("="),
				Arrays.asList(managesDepartment));

		GoNatureClientUI.client.accept(requestForPendingAdjustment); // sending the query to the server that will
																		// connect to the DB

		// create ArrayList of PendingAdjustment objects,
		// and get every row from the ArrayList that the query returned into
		// PendingAdjustment object:
		ArrayList<PendingAdjustment> pendingAdjustmentList = new ArrayList<>();
		for (Object[] row : requestForPendingAdjustment.getResultList()) // get the ArrayList query result and move with
																			// object on each row
		{
			// creating PendingAdjustment object and add to it every cell in the row(of the
			// ArrayList query result)
			PendingAdjustment addToPendingAdjustmentList = new PendingAdjustment((String) row[0], // adjusmentId
					(Integer) row[1], // parkId
					(String) row[2], // parkName
					(String) row[3], // department
					((Date) row[4]).toLocalDate(), // dayOfAdjusting
					((Time) row[5]).toLocalTime(), // timeOfAdjusting
					(String) row[6], // adjustedBy
					(Integer) row[7], // parameterBefore
					(Integer) row[8], // parameterAfter
					(String) row[9] // parameterType
			);
			pendingAdjustmentList.add(addToPendingAdjustmentList); // add the PendingAdjustment object to the ArrayList
																	// we build
		}
		return FXCollections.observableArrayList(pendingAdjustmentList); // convert the ArrayList of PendingAdjustment
																			// the we build to observableArrayList
	}

	
	
	
	
	public boolean updateParkParameter(PendingAdjustment pendingAdjustment,
			DepartmentManager departmentManager, boolean isApproved) {
		// Creating the values we will use:

		String adjusmentId = pendingAdjustment.getAdjusmentId();
		String parkName = pendingAdjustment.getParkName();
		int parkId = pendingAdjustment.getParkId();
		String department = pendingAdjustment.getDepartment();
		LocalDate dayOfAdjusting = pendingAdjustment.getDayOfAdjusting();
		LocalTime timeOfAdjusting = pendingAdjustment.getTimeOfAdjusting();
		String adjustedBy = pendingAdjustment.getAdjustedBy();
		int parameterBefore = pendingAdjustment.getParameterBefore();
		int parameterAfter = pendingAdjustment.getParameterAfter();
		String parameterType = (String)pendingAdjustment.getParameterType();

		String reviewdBy = departmentManager.getFirstName() + " " + departmentManager.getLastName();

		LocalDate dateOfReview = LocalDate.now();
		LocalTime timeOfReview = LocalTime.now();
		Integer ans=0;
		if(isApproved)
		{
			ans=1;
		}
	

		// Creating an insert query in order to add the requests that have been viewed 
		// the viewed_adjustment table
		Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		RequestForUpdateParkparameter.setTables(Arrays.asList("viewed_adjustment"));
		RequestForUpdateParkparameter.setColumnsAndValues(
				Arrays.asList("adjusmentId", "parkId", "parkName", "department", "dayOfAdjusting", "timeOfAdjusting",
						"adjustedBy", "parameterBefore", "parameterAfter", "parameterType", "reviewdBy","isApproved","dateOfReview", "timeOfReview"),
				Arrays.asList(adjusmentId, parkId, parkName, department, dayOfAdjusting, timeOfAdjusting, adjustedBy,
						parameterBefore, parameterAfter, parameterType, reviewdBy, ans, dateOfReview,
						timeOfReview));

		GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																		// connect to the DB

		// getQueryResult will return true if the insert query was successful, false
		// otherwise
		if (!RequestForUpdateParkparameter.getQueryResult()) {
			return false;
		}
		// the insert query was successful
		// Creating a delete query in order to delete the pendingAdjustment from the
		// pending_adjustment table (already received response from the
		// departmentManager so it is not pending anymore)
		Communication deletePendingAdjustmentRequest = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			deletePendingAdjustmentRequest.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		deletePendingAdjustmentRequest.setTables(Arrays.asList("pending_adjustment"));
		deletePendingAdjustmentRequest.setWhereConditions(Arrays.asList("adjusmentId"), Arrays.asList("="),
				Arrays.asList(adjusmentId));

		// sending the request to the server side
		GoNatureClientUI.client.accept(deletePendingAdjustmentRequest);

		// getQueryResult will return true if the delete query was successful, false
		// otherwise
		if (!deletePendingAdjustmentRequest.getQueryResult()) {
			return false;
		}

		// if the departmentManager approved the adjustment=> update the parks parameters
		if (isApproved) {
			// Creating a update query in order to update the new parameter of this park in
			// the parks table
			// update that the user LoggedIn -> change the isLoggedIn value from 0 (user was
			// logged out),to 1(user logged in)
			Communication requestForUpdateParksParameter = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				requestForUpdateParksParameter.setQueryType(QueryType.UPDATE);
				requestForUpdateParksParameter.setTables(Arrays.asList("park"));
				// we will access to the park table column which it's name is equal to the type
				// of the parameter that has been changed:
				requestForUpdateParksParameter.setColumnsAndValues(Arrays.asList(parameterType),
						Arrays.asList(parameterAfter));
				requestForUpdateParksParameter.setWhereConditions(Arrays.asList("parkId"), Arrays.asList("="),
						Arrays.asList(parkId));
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			GoNatureClientUI.client.accept(requestForUpdateParksParameter);
			if (!requestForUpdateParksParameter.getQueryResult()) {
				return false;
			}
		}
		return true;
	}

}
