package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkEmployee;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import entities.Representative;
import entities.SystemUser;

public class LoginController {

	/////////////////////////////////////
	/// METHODS TO CONTROL FULL LOGIN ///
	/////////////////////////////////////

	/**
	 * This method gets a username and a password and checks if they're exist in the
	 * database. Returns a SystemUser instance if they do, null if not
	 * 
	 * @param username
	 * @param password
	 * @return a SystemUser instance, and null if the credentials are not exist
	 */
	public SystemUser checkUserCredentials(String username, String password) {
		// checking in the group guide table
		ParkVisitor groupGuide = checkGroupGuideCredentials(username, password);
		if (groupGuide != null)
			return groupGuide;

		// chcking in the park manager table
		ParkManager parkManager = checkParkManagerCredentials(username, password);
		if (parkManager != null)
			return parkManager;

		// checking in the department manager table
		DepartmentManager deptManager = checkDepartmentManagerCredentials(username, password);
		if (deptManager != null)
			return deptManager;

		// checking in the representative table
		Representative representative = checkRepresentativeCredentials(username, password);
		if (representative != null)
			return representative;

		// checking in the park employee of each park's table
		ParkEmployee employee = checkParkEmployeeCredentials(username, password);
		if (employee != null)
			return employee;

		// if these credentials do not exist in any table
		return null;
	}

	/**
	 * This method gets a username and a password and checks if these credentials
	 * exist in the group guide table in the database
	 * 
	 * @param username
	 * @param password
	 * @return an instance of ParkVisitor if exist, null if not
	 */
	private ParkVisitor checkGroupGuideCredentials(String username, String password) {
		// creating a select query to check if there's a group guide with these
		// credentials
		Communication requestForGroupGuide = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForGroupGuide.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForGroupGuide.setTables(Arrays.asList(Communication.griupGuide));
		requestForGroupGuide.setSelectColumns(Arrays.asList("*"));
		requestForGroupGuide.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(username, password));
		GoNatureClientUI.client.accept(requestForGroupGuide); // sending the query to the server side

		// getResultList will return list if there is user with this userName&password
		if (!requestForGroupGuide.getResultList().isEmpty()) {
			// insert the results to an ArrayList
			ArrayList<Object[]> groupGuideResult = requestForGroupGuide.getResultList();

			// insert relevant objects from the ArrayList to values
			String idNumber = (String) groupGuideResult.get(0)[0];
			String firstName = (String) groupGuideResult.get(0)[1];
			String lastName = (String) groupGuideResult.get(0)[2];
			String emailAddress = (String) groupGuideResult.get(0)[3];
			String phoneNumber = (String) groupGuideResult.get(0)[4];
			boolean isLoggedIn = ((Integer) groupGuideResult.get(0)[7]) == 0 ? false : true;

			// creating an instance of the ParkVisitor and returning it
			ParkVisitor groupGuideUser = new ParkVisitor(idNumber, firstName, lastName, emailAddress, phoneNumber,
					username, password, isLoggedIn, ParkVisitor.VisitorType.GROUPGUIDE);
			return groupGuideUser;
		} else { // There is no group guide with these userName and password
			return null;
		}
	}

	/**
	 * This method gets a username and a password and checks if these credentials
	 * exist in the park manager table in the database
	 * 
	 * @param username
	 * @param password
	 * @return an instance of ParkManager if exist, null if not
	 */
	private ParkManager checkParkManagerCredentials(String username, String password) {
		// creating a select query to check if there's a park manager with these
		// credentials
		Communication requestForParkManager = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForParkManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForParkManager.setTables(Arrays.asList(Communication.parkManager));
		requestForParkManager.setSelectColumns(Arrays.asList("*"));
		requestForParkManager.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(username, password));
		GoNatureClientUI.client.accept(requestForParkManager); // sending the query to the server side

		// getResultList will return list if there is user with this userName&password
		if (!requestForParkManager.getResultList().isEmpty()) {
			// insert the results to an ArrayList
			ArrayList<Object[]> parkManagerResult = requestForParkManager.getResultList();

			// insert relevant objects from the ArrayList to values
			String idNumber = (String) parkManagerResult.get(0)[0];
			String firstName = (String) parkManagerResult.get(0)[1];
			String lastName = (String) parkManagerResult.get(0)[2];
			String emailAddress = (String) parkManagerResult.get(0)[3];
			String phoneNumber = (String) parkManagerResult.get(0)[4];
			String managesPark = (String) parkManagerResult.get(0)[5];
			boolean isLoggedIn = ((Integer) parkManagerResult.get(0)[8]) == 0 ? false : true;

			// creating an instance of the ParkManager and returning it
			ParkManager parkManagerUser = new ParkManager(idNumber, firstName, lastName, emailAddress, phoneNumber,
					managesPark, username, password, isLoggedIn);
			return parkManagerUser;
		} else { // There is no park manager with these userName and password
			return null;
		}
	}

	/**
	 * This method gets a username and a password and checks if these credentials
	 * exist in the department manager table in the database
	 * 
	 * @param username
	 * @param password
	 * @return an instance of DepartmentManager if exist, null if not
	 */
	private DepartmentManager checkDepartmentManagerCredentials(String username, String password) {
		// creating a select query to check if there's a department manager with these
		// credentials
		Communication requestForDeptManager = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForDeptManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForDeptManager.setTables(Arrays.asList(Communication.departmentManager));
		requestForDeptManager.setSelectColumns(Arrays.asList("*"));
		requestForDeptManager.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(username, password));
		GoNatureClientUI.client.accept(requestForDeptManager); // sending the query to the server side

		// getResultList will return list if there is user with this userName&password
		if (!requestForDeptManager.getResultList().isEmpty()) {
			// insert the results to an ArrayList
			ArrayList<Object[]> deptManagerResult = requestForDeptManager.getResultList();

			// insert relevant objects from the ArrayList to values
			String idNumber = (String) deptManagerResult.get(0)[0];
			String firstName = (String) deptManagerResult.get(0)[1];
			String lastName = (String) deptManagerResult.get(0)[2];
			String emailAddress = (String) deptManagerResult.get(0)[3];
			String phoneNumber = (String) deptManagerResult.get(0)[4];
			String managesDept = (String) deptManagerResult.get(0)[5];
			boolean isLoggedIn = ((Integer) deptManagerResult.get(0)[8]) == 0 ? false : true;

			// creating an instance of the DepartmentManager and returning it
			DepartmentManager deptManagerUser = new DepartmentManager(idNumber, firstName, lastName, emailAddress,
					phoneNumber, managesDept, username, password, isLoggedIn);
			return deptManagerUser;
		} else { // There is no department manager with these userName and password
			return null;
		}
	}

	/**
	 * This method gets a username and a password and checks if these credentials
	 * exist in the representative table in the database
	 * 
	 * @param username
	 * @param password
	 * @return an instance of Representative if exist, null if not
	 */
	private Representative checkRepresentativeCredentials(String username, String password) {
		// creating a select query to check if there's a representative with these
		// credentials
		Communication requestForRepresentative = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForRepresentative.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForRepresentative.setTables(Arrays.asList(Communication.representative));
		requestForRepresentative.setSelectColumns(Arrays.asList("*"));
		requestForRepresentative.setWhereConditions(Arrays.asList("userName", "password"),
				Arrays.asList("=", "AND", "="), Arrays.asList(username, password));
		GoNatureClientUI.client.accept(requestForRepresentative); // sending the query to the server side

		// getResultList will return list if there is user with this userName&password
		if (!requestForRepresentative.getResultList().isEmpty()) {
			// insert the results to an ArrayList
			ArrayList<Object[]> representativeResult = requestForRepresentative.getResultList();

			// insert relevant objects from the ArrayList to values
			String idNumber = (String) representativeResult.get(0)[0];
			String firstName = (String) representativeResult.get(0)[1];
			String lastName = (String) representativeResult.get(0)[2];
			String emailAddress = (String) representativeResult.get(0)[3];
			String phoneNumber = (String) representativeResult.get(0)[4];
			boolean isLoggedIn = ((Integer) representativeResult.get(0)[7]) == 0 ? false : true;

			// creating an instance of the Representative and returning it
			Representative representative = new Representative(idNumber, firstName, lastName, emailAddress, phoneNumber,
					username, password, isLoggedIn);
			return representative;
		} else { // There is no representative with these userName and password
			return null;
		}
	}

	/**
	 * This method gets a username and a password and checks if these credentials
	 * exist in each park's employees table in the database
	 * 
	 * @param username
	 * @param password
	 * @return an instance of ParkEmployee if exist, null if not
	 */
	private ParkEmployee checkParkEmployeeCredentials(String username, String password) {
		ArrayList<Park> parks = ParkController.getInstance().fetchParks();
		for (Park park : parks) {
			// creating a select query to check if there's a park employee with these
			// credentials in this specific park
			Communication requestForParkEmployee = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				requestForParkEmployee.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			String tableName = ParkController.getInstance().nameOfTable(park) + Communication.parkEmployees;
			requestForParkEmployee.setTables(Arrays.asList(tableName));
			requestForParkEmployee.setSelectColumns(Arrays.asList("*"));
			requestForParkEmployee.setWhereConditions(Arrays.asList("userName", "password"),
					Arrays.asList("=", "AND", "="), Arrays.asList(username, password));

			GoNatureClientUI.client.accept(requestForParkEmployee); // sending the query to the server side

			// getResultList will return list if there is user with this userName&password
			if (!requestForParkEmployee.getResultList().isEmpty()) {
				// insert the results to an ArrayList
				ArrayList<Object[]> ParkEmployeeResult = requestForParkEmployee.getResultList();

				// insert relevant objects from the ArrayList to values
				String idNumber = (String) ParkEmployeeResult.get(0)[0];
				String firstName = (String) ParkEmployeeResult.get(0)[1];
				String lastName = (String) ParkEmployeeResult.get(0)[2];
				String emailAddress = (String) ParkEmployeeResult.get(0)[3];
				String phoneNumber = (String) ParkEmployeeResult.get(0)[4];
				boolean isLoggedIn = ((Integer) ParkEmployeeResult.get(0)[7]) == 0 ? false : true;

				// creating an instance of the ParkEmployee and returning it
				ParkEmployee parkEmployee = new ParkEmployee(idNumber, firstName, lastName, emailAddress, phoneNumber,
						username, password, isLoggedIn);
				parkEmployee.setWorkingIn(park);
				return parkEmployee;
			}
		}
		// There is no park employee with these userName and password
		return null;
	}

	/**
	 * The method receives a SystemUser instance and sets its loggenIn property in
	 * the database to true (1)
	 * 
	 * @param user
	 * @return true if the update succeed, false if not
	 */
	public boolean updateUserIsLoggedIn(SystemUser user) {
		// update that the user LoggedIn -> change the isLoggedIn value from 0 (user was
		// logged out),to 1(user logged in)
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		String id = user.getIdNumber();

		// checking which table needs to be updated
		if (user instanceof ParkVisitor && ((ParkVisitor) user).getVisitorType() == VisitorType.TRAVELLER) {
			// it is an individual visitor
			request.setTables(Arrays.asList(Communication.traveller));
			request.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="), Arrays.asList(id));
		} else if (user instanceof ParkVisitor && ((ParkVisitor) user).getVisitorType() == VisitorType.GROUPGUIDE) {
			// it is a group guide
			request.setTables(Arrays.asList(Communication.griupGuide));
			request.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="), Arrays.asList(id));
		} else if (user instanceof ParkManager) {
			// it is a park manager
			request.setTables(Arrays.asList(Communication.parkManager));
			request.setWhereConditions(Arrays.asList("parkManagerId"), Arrays.asList("="), Arrays.asList(id));
		} else if (user instanceof DepartmentManager) {
			// it is a department manager
			request.setTables(Arrays.asList(Communication.departmentManager));
			request.setWhereConditions(Arrays.asList("departmentManagerId"), Arrays.asList("="), Arrays.asList(id));
		} else if (user instanceof Representative) {
			// it is a representative
			request.setTables(Arrays.asList(Communication.representative));
			request.setWhereConditions(Arrays.asList("representativeId"), Arrays.asList("="), Arrays.asList(id));
		} else {
			// it is a park employee
			Park workingIn = ((ParkEmployee) user).getWorkingIn();
			String tableName = ParkController.getInstance().nameOfTable(workingIn) + Communication.parkEmployees;
			request.setTables(Arrays.asList(tableName));
			request.setWhereConditions(Arrays.asList("employeeId"), Arrays.asList("="), Arrays.asList(id));
		}

		// setting the is logged in column from 0 to 1
		request.setColumnsAndValues(Arrays.asList("isLoggedIn"), Arrays.asList('1'));

		GoNatureClientUI.client.accept(request);

		return request.getQueryResult();
	}

	///////////////////////////////////
	/// METHODS TO CONTROL ID LOGIN ///
	///////////////////////////////////

	/**
	 * This method gets an id number and checks in the traveller table if this id
	 * exists
	 * 
	 * @param idNumber
	 * @return a ParkVisitor instance if exists, null if not
	 */
	public SystemUser checkIfTravellerExists(String idNumber) {
		// check if there is travelerUser with this idNumber
		Communication requestForTraveler = new Communication(CommunicationType.QUERY_REQUEST);

		try {
			requestForTraveler.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForTraveler.setTables(Arrays.asList(Communication.traveller));
		requestForTraveler.setSelectColumns(Arrays.asList("*"));
		requestForTraveler.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="),
				Arrays.asList(idNumber));

		GoNatureClientUI.client.accept(requestForTraveler); // sending the query to the server side

		// getResultList will return list if there is travelerUser with this id
		ArrayList<Object[]> results = requestForTraveler.getResultList();
		if (!results.isEmpty()) {
			return new ParkVisitor((String) results.get(0)[0], null, null, null, null, null, null,
					(Integer) results.get(0)[1] == 0 ? false : true, VisitorType.TRAVELLER);
		} else {
			return null;
		}
	}

	/**
	 * This method gets a traveller instance and checks if it has any booking in any
	 * park
	 * 
	 * @param traveller
	 * @return true if the traveller has bookings, false if not
	 */
	public boolean checkIfTravellerHasBookings(ParkVisitor traveller) {
		BookingController bookingControl = BookingController.getInstance();
		if (!(bookingControl.getVisitorBookings(traveller, Communication.activeBookings)).isEmpty())
			return true;
		if (!(bookingControl.getVisitorBookings(traveller, Communication.waitingList)).isEmpty())
			return true;
		if (!(bookingControl.getVisitorBookings(traveller, Communication.cancelledBookings)).isEmpty())
			return true;
		if (!(bookingControl.getVisitorBookings(traveller, Communication.doneBookings)).isEmpty())
			return true;
		return false;
	}

	/**
	 * This method gets an id number and checks if there is a registered user with
	 * this id who tried to enter the system with the id, and not with the username
	 * and password
	 * 
	 * @param idNumber
	 * @return true if exists, false if not
	 */
	public boolean checkUserId(String idNumber) {
		// checking in the group guide table
		if (checkGroupGuideId(idNumber))
			return true;

		// chcking in the park manager table
		if (checkParkManagerId(idNumber))
			return true;

		// checking in the department manager table
		if (checkDepartmentManagerId(idNumber))
			return true;

		// checking in the representative table
		if (checkRepresentativeId(idNumber))
			return true;

		// checking in the park employee of each park's table
		if (checkParkEmployeeId(idNumber))
			return true;

		return false;
	}

	/**
	 * This method gets an id number and checks if there's a group guide with this
	 * id
	 * 
	 * @param id
	 * @return true if exists, false if not
	 */
	private boolean checkGroupGuideId(String id) {
		// creating a select query to check if there's a group guide with this id
		Communication requestForGroupGuide = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForGroupGuide.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForGroupGuide.setTables(Arrays.asList(Communication.griupGuide));
		requestForGroupGuide.setSelectColumns(Arrays.asList("groupGuideId"));
		requestForGroupGuide.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="), Arrays.asList(id));
		GoNatureClientUI.client.accept(requestForGroupGuide); // sending the query to the server side

		// getResultList will return list if there is user with this id
		if (requestForGroupGuide.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method gets an id number and checks if there's a park manager with this
	 * id
	 * 
	 * @param id
	 * @return true if exists, false if not
	 */
	private boolean checkParkManagerId(String id) {
		// creating a select query to check if there's a park manager with this id
		Communication requestForParkManager = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForParkManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForParkManager.setTables(Arrays.asList(Communication.parkManager));
		requestForParkManager.setSelectColumns(Arrays.asList("parkManagerId"));
		requestForParkManager.setWhereConditions(Arrays.asList("parkManagerId"), Arrays.asList("="), Arrays.asList(id));
		GoNatureClientUI.client.accept(requestForParkManager); // sending the query to the server side

		// getResultList will return list if there is user with this id
		if (requestForParkManager.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method gets an id number and checks if there's a department manager with
	 * this id
	 * 
	 * @param id
	 * @return true if exists, false if not
	 */
	private boolean checkDepartmentManagerId(String id) {
		// creating a select query to check if there's a department manager with this id
		Communication requestForDeptManager = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForDeptManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForDeptManager.setTables(Arrays.asList(Communication.departmentManager));
		requestForDeptManager.setSelectColumns(Arrays.asList("departmentManagerId"));
		requestForDeptManager.setWhereConditions(Arrays.asList("departmentManagerId"), Arrays.asList("="),
				Arrays.asList(id));
		GoNatureClientUI.client.accept(requestForDeptManager); // sending the query to the server side

		// getResultList will return list if there is user with this id
		if (requestForDeptManager.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method gets an id number and checks if there's a representative with
	 * this id
	 * 
	 * @param id
	 * @return true if exists, false if not
	 */
	private boolean checkRepresentativeId(String id) {
		// creating a select query to check if there's a representative with this id
		Communication requestForRepresentative = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestForRepresentative.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForRepresentative.setTables(Arrays.asList(Communication.representative));
		requestForRepresentative.setSelectColumns(Arrays.asList("representativeId"));
		requestForRepresentative.setWhereConditions(Arrays.asList("representativeId"), Arrays.asList("="),
				Arrays.asList(id));
		GoNatureClientUI.client.accept(requestForRepresentative); // sending the query to the server side

		// getResultList will return list if there is user with this id
		if (requestForRepresentative.getResultList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method gets an id number and checks if there's a park employee with this
	 * id
	 * 
	 * @param id
	 * @return true if exists, false if not
	 */
	private boolean checkParkEmployeeId(String id) {
		ArrayList<Park> parks = ParkController.getInstance().fetchParks();
		for (Park park : parks) {
			// creating a select query to check if there's a park employee with this id
			Communication requestForParkEmployee = new Communication(CommunicationType.QUERY_REQUEST);
			try {
				requestForParkEmployee.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			String tableName = ParkController.getInstance().nameOfTable(park) + Communication.parkEmployees;
			requestForParkEmployee.setTables(Arrays.asList(tableName));
			requestForParkEmployee.setSelectColumns(Arrays.asList("employeeId"));
			requestForParkEmployee.setWhereConditions(Arrays.asList("employeeId"), Arrays.asList("="),
					Arrays.asList(id));

			GoNatureClientUI.client.accept(requestForParkEmployee); // sending the query to the server side

			// getResultList will return list if there is user with this id
			if (!requestForParkEmployee.getResultList().isEmpty()) {
				return true;
			}
		}
		// There is no park employee with these id
		return false;
	}

	/**
	 * This method gets an id number and inserts it as a new row in the traveller
	 * table in the database
	 * 
	 * @return True-if successfully add new traveler to the traveller table ,false
	 *         otherwise
	 */
	public boolean insertNewTraveller(String idNumber) {

		// Creating an insert query in order to add
		// new traveler to the traveller table
		Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		RequestForUpdateParkparameter.setTables(Arrays.asList(Communication.traveller));
		RequestForUpdateParkparameter.setColumnsAndValues(Arrays.asList("travellerId", "isLoggedIn"),
				Arrays.asList(idNumber, 0));

		GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																		// connect to the DB

		// getQueryResult will return true if the insert query was successful, false
		// otherwise
		return RequestForUpdateParkparameter.getQueryResult();
	}
}