package serverSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

public class StaffController {
	private DatabaseController database;

	public enum ImportStatus {
		SUCCESS, NOTHING_TO_IMPORT, FAILURE
	}

	/**
	 * Private constructor to prevent external instantiation.
	 * 
	 * @param database The database controller used for database operations.
	 */
	public StaffController(DatabaseController database) {
		this.database = database;
	}

	/**
	 * Imports users into their respective tables based on user type.
	 * 
	 * @return An ImportStatus indicating the outcome of the import process.
	 *         Possible return values are: - SUCCESS if at least one user type is
	 *         successfully imported. - NOTHING_TO_IMPORT if no users of any type
	 *         are found to import. - FAILURE if an error occurs during the import
	 *         process.
	 * @throws DatabaseException if there's a problem accessing the database.
	 */
	public ImportStatus importUsers() {
		try {
			boolean atLeastOneImported = false;
			if (importUsersType("Employee"))
				atLeastOneImported = true;
			if (importUsersType("Park Manager"))
				atLeastOneImported = true;
			if (importUsersType("Department Manager"))
				atLeastOneImported = true;
			if (importUsersType("Representative"))
				atLeastOneImported = true;

			if (!atLeastOneImported) {
				return ImportStatus.NOTHING_TO_IMPORT;
			}
			return ImportStatus.SUCCESS;
		} catch (DatabaseException e) {
			e.printStackTrace();
			return ImportStatus.FAILURE;
		}
	}

	private String nameOfTable(String park) {

		return park.toLowerCase().replaceAll(" ", "_");
	}

	////////////////////////////////////
	/// IMPORT USERS BY TYPE METHOD ////
	////////////////////////////////////

	/**
	 * Imports users of a specific type from the system_users table and inserts them
	 * into their respective tables. After successful insertion, the user is deleted
	 * from the system_users table.
	 * 
	 * @param type The type of users to import (e.g., Employee, Park Manager,
	 *             Department Manager).
	 * @return true if users of the specified type are imported successfully, false
	 *         if no users of that type exist.
	 * @throws DatabaseException if there's an issue executing database operations.
	 */
	public boolean importUsersType(String type) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList("system_users"));
			request.setSelectColumns(Arrays.asList("userId", "firstName", "lastName", "emailAddress", "phoneNumber",
					"park", "managesDepartment", "userName", "password"));
			request.setWhereConditions(Arrays.asList("type"), Arrays.asList("="), Arrays.asList(type));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		ArrayList<Object[]> results = database.executeSelectQuery(request);
		if (results.isEmpty()) {
			return false;
		} else {
			if (type.matches("Employee")) {
				for (Object[] row : results) {
					employeeInsertToEmplyeesTable((String) row[0], (String) row[1], (String) row[2], (String) row[3],
							(String) row[4], (String) row[5], (String) row[7], (String) row[8]);
					userDeleteFromUserTable((String) row[0]);
				}
			}
			if (type.matches("Park Manager")) {
				for (Object[] row : results) {
					parkManagerInsertToParkManagerTable((String) row[0], (String) row[1], (String) row[2],
							(String) row[3], (String) row[4], (String) row[5], (String) row[7], (String) row[8]);
					userDeleteFromUserTable((String) row[0]);
				}
			}
			if (type.matches("Department Manager")) {
				for (Object[] row : results) {
					DepartmentManagerInsertToDepartmentManagerTable((String) row[0], (String) row[1], (String) row[2],
							(String) row[3], (String) row[4], (String) row[6], (String) row[7], (String) row[8]);
					userDeleteFromUserTable((String) row[0]);
				}
			}
			if (type.matches("Representative")) {
				for (Object[] row : results) {
					RepresentativeInsertToRepresentativeTable((String) row[0], (String) row[1], (String) row[2],
							(String) row[3], (String) row[4], (String) row[7], (String) row[8]);
					userDeleteFromUserTable((String) row[0]);
				}
			}
		}
		return true;
	}

	//////////////////////////////
	/// DB DELETE USER METHOD ////
	//////////////////////////////

	/**
	 * Deletes a user from the system_users table based on their userID.
	 * 
	 * @param id The userID of the user to be deleted.
	 * @return true if the user is successfully deleted, false otherwise.
	 * @throws DatabaseException if there's an issue executing the delete operation.
	 */
	public boolean userDeleteFromUserTable(String id) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.DELETE);
			request.setTables(Arrays.asList("system_users"));
			request.setWhereConditions(Arrays.asList("userId"), Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		boolean deleteResult = database.executeDeleteQuery(request);
		if (!deleteResult) {
			throw new DatabaseException("Problem with Users DELETE query");
		}
		return true;
	}

	//////////////////////////////////////////////////
	//// DB INSERT USER TO RELEVANT TABLE METHODS ////
	//////////////////////////////////////////////////

	/**
	 * Inserts employee details into the relevant employees table.
	 * 
	 * @param id        The employee's ID.
	 * @param firstName The employee's first name.
	 * @param lastName  The employee's last name.
	 * @param email     The employee's email address.
	 * @param phone     The employee's phone number.
	 * @param park      The park the employee is associated with.
	 * @param userName  The employee's username.
	 * @param password  The employee's password.
	 * @throws DatabaseException if there's an issue executing the insert operation.
	 */
	private void employeeInsertToEmplyeesTable(String id, String firstName, String lastName, String email, String phone,
			String park, String userName, String password) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList(nameOfTable(park) + Communication.parkEmployees));
			request.setColumnsAndValues(
					Arrays.asList("employeeId", "firstName", "lastName", "emailAddress", "phoneNumber", "userName",
							"password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		boolean insertResult = database.executeInsertQuery(request);
		if (!insertResult) {
			throw new DatabaseException("Problem with Emplyee INSERT query");
		}
	}

	/**
	 * Inserts park manager details into the park_manager table.
	 * 
	 * @param id        The park manager's ID.
	 * @param firstName The park manager's first name.
	 * @param lastName  The park manager's last name.
	 * @param email     The park manager's email address.
	 * @param phone     The park manager's phone number.
	 * @param park      The park the manager is responsible for.
	 * @param userName  The park manager's username.
	 * @param password  The park manager's password.
	 * @throws DatabaseException if there's an issue executing the insert operation.
	 */
	public void parkManagerInsertToParkManagerTable(String id, String firstName, String lastName, String email,
			String phone, String park, String userName, String password) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList(Communication.parkManager));
			request.setColumnsAndValues(
					Arrays.asList("parkManagerId", "firstName", "lastName", "emailAddress", "phoneNumber",
							"managesPark", "userName", "password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, park, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		boolean insertResult = database.executeInsertQuery(request);
		if (!insertResult) {
			throw new DatabaseException("Problem with Park Manager INSERT query");
		}
	}

	/**
	 * Inserts department manager details into the department_manager table.
	 * 
	 * @param id         The department manager's ID.
	 * @param firstName  The department manager's first name.
	 * @param lastName   The department manager's last name.
	 * @param email      The department manager's email address.
	 * @param phone      The department manager's phone number.
	 * @param department The department the manager oversees.
	 * @param userName   The department manager's username.
	 * @param password   The department manager's password.
	 * @throws DatabaseException if there's an issue executing the insert operation.
	 */
	public void DepartmentManagerInsertToDepartmentManagerTable(String id, String firstName, String lastName,
			String email, String phone, String department, String userName, String password) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList(Communication.departmentManager));
			request.setColumnsAndValues(
					Arrays.asList("departmentManagerId", "firstName", "lastName", "emailAddress", "phoneNumber",
							"managesDepartment", "userName", "password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, department, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		boolean insertResult = database.executeInsertQuery(request);
		if (!insertResult) {
			throw new DatabaseException("Problem with Department Manager INSERT query");
		}
	}

	/**
	 * Inserts Representative details into the Representative table.
	 * 
	 * @param id        The Representative's ID.
	 * @param firstName The Representative's first name.
	 * @param lastName  The Representative's last name.
	 * @param email     The Representative's email address.
	 * @param phone     The Representative's phone number.
	 * @param userName  The Representative's username.
	 * @param password  The Representative's password.
	 * @throws DatabaseException if there's an issue executing the insert operation.
	 */
	public void RepresentativeInsertToRepresentativeTable(String id, String firstName, String lastName, String email,
			String phone, String userName, String password) throws DatabaseException {
		Communication request = new Communication(CommunicationType.SELF);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList(Communication.representative));
			request.setColumnsAndValues(
					Arrays.asList("representativeId", "firstName", "lastName", "emailAddress", "phoneNumber",
							"userName", "password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		boolean insertResult = database.executeInsertQuery(request);
		if (!insertResult) {
			throw new DatabaseException("Problem with Representative INSERT query");
		}
	}

	public ArrayList<Object[]> getNewUserDetails(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList("system_users"));
			request.setSelectColumns(
					Arrays.asList("firstName", "lastName", "emailAddress", "phoneNumber", "userName", "password"));
			request.setWhereConditions(Arrays.asList("userId", "type"), Arrays.asList("=", "AND", "="),
					Arrays.asList(id, "User"));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		return result;

	}
}