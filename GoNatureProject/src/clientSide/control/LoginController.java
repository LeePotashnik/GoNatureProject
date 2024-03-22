package clientSide.control;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.Booking;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkEmployee;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.SystemUser;
import entities.ParkVisitor.VisitorType;
import entities.Representative;

public class LoginController {

	/**
	 * This method gets userName and password of Traveler/GroupGuide
	 * 
	 * @return instance of systemUser of the traveler/groupGuide or null if there is
	 *         no user with this userName and password
	 * @throws CommunicationException
	 */
	public ParkVisitor checkVisitorCredential(String userName, String password) throws CommunicationException {
		// check if there is groupGuideUser with this userName&password: return
		// GruopGuide if there is and null if not
		ParkVisitor visitorUser=checkgroupGuideCredential(userName, password);
		return visitorUser;	
	}


	/**
	 * This method gets userName and password
	 * 
	 * @return instance of GroupGuide if there is groupGuideUser with this
	 *         userName&password
	 * @throws CommunicationException
	 */
	private ParkVisitor checkgroupGuideCredential(String userName, String password)
			throws CommunicationException {
		// Creating a select query in order to check if such a groupGuideUser exists in
		// the group_guide table and get all his details
		Communication requestForGrupGuide = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForGrupGuide.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForGrupGuide.setTables(Arrays.asList("group_guide"));
		requestForGrupGuide.setSelectColumns(Arrays.asList("*"));
		requestForGrupGuide.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(userName, password));
		GoNatureClientUI.client.accept(requestForGrupGuide); // sending the query to the server that will connect to the
																// DB

		if (!requestForGrupGuide.getResultList().isEmpty()) // getResultList will return list if there is groupGuideUser
															// with this userName&password
		{
			// insert the groupGuideUser details to an ArrayList
			ArrayList<Object[]> groupGuideResult = requestForGrupGuide.getResultList();

			// insert relevant objects from the ArrayList to values
			String idNumber = (String) groupGuideResult.get(0)[0];
			String firstName = (String) groupGuideResult.get(0)[1];
			String lastName = (String) groupGuideResult.get(0)[2];
			String emailAddress = (String) groupGuideResult.get(0)[3];
			String phoneNumber = (String) groupGuideResult.get(0)[4];
			boolean isLoggedIn = ((Integer) groupGuideResult.get(0)[7]) == 0 ? false : true;

			// creating an instance of the GroupGuideUser and return it
			ParkVisitor groupGuideUser = new ParkVisitor(idNumber, firstName, lastName, emailAddress, phoneNumber,
					userName, password, isLoggedIn, ParkVisitor.VisitorType.GROUPGUIDE);
			return groupGuideUser;
		} else {
			return null;
		} // There is no GroupGuideUser with this userName and password
	}

	/**
	 * This method gets userName and password of
	 * ParkManager/DepartmentManager/ParkEmployee
	 * 
	 * @return instance of systemUser of the traveler/groupGuide or null if there is
	 *         no user with this userName and password
	 * @throws CommunicationException
	 */
	public SystemUser checkEmployeeCredential(String userName, String password) throws CommunicationException {
		// check if there is parkManagerUser with this userName&password: return
		// ParkManager if there is and null if not
		SystemUser systemUser = checkParkManagerCredential(userName, password);
		if (systemUser != null) {
			System.out.println("found the user from loginController");
			return systemUser;
		}
		// check if there is departmentManagerUser with this userName&password: return
		// DepartmentManager if there is and null if not
		else if ((systemUser = checkDepartmentManagerCredential(userName, password)) != null) {
			return systemUser;
		}
		// check if there is parkEmployeeUser with this userName&password: return
		// ParkEmployee if there is and null if not
		else if ((systemUser = checkParkEmployeeCredential(userName, password)) != null) {
			return systemUser;
		}
		// check if there is parkRepresentativeUser with this userName&password: return
		// ParkEmployee if there is and null if not
		else if ((systemUser = checkParkRepresentativeCredential(userName, password)) != null) {
			return systemUser;
		}
		// there is no parkManagerUser,departmentManagerUser,parkEmployeeUser with this
		// userName&password (will return null)
		return systemUser;
	}

	/**
	 * This method gets userName and password of
	 * ParkManager/DepartmentManager/ParkEmployee
	 * 
	 * @return instance of systemUser of the ParkManager or null if there is no
	 *         parkManagerUser with this userName and password
	 * @throws CommunicationException
	 */
	private SystemUser checkParkManagerCredential(String userName, String password)
			throws CommunicationException {
		// Creating a select query in order to check if such a parkManagerUser exists in
		// the park_manager table and get all his details
		Communication requestForParkManager = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForParkManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForParkManager.setTables(Arrays.asList("park_manager"));
		requestForParkManager.setSelectColumns(Arrays.asList("*"));
		requestForParkManager.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(userName, password));
		GoNatureClientUI.client.accept(requestForParkManager); // sending the query to the server that will connect to
																// the DB

		if (!requestForParkManager.getResultList().isEmpty()) // getResultList will return list if there is
																// parkManagerUser with this userName&password
		{
			// insert the parkManagerUser details to an ArrayList
			ArrayList<Object[]> parkManagerResult = requestForParkManager.getResultList();

			// insert relevant objects from the ArrayList to values
			String parkManagerId = (String) parkManagerResult.get(0)[0];
			String firstName = (String) parkManagerResult.get(0)[1];
			String lastName = (String) parkManagerResult.get(0)[2];
			String emailAddress = (String) parkManagerResult.get(0)[3];
			String phoneNumber = (String) parkManagerResult.get(0)[4];
			String managesPark = (String) parkManagerResult.get(0)[5];
			boolean isLoggedIn = ((Integer) parkManagerResult.get(0)[8]) == 0 ? false : true;
			// creating an instance of the parkManagerUser and return it
			ParkManager parkManagerUser = new ParkManager(parkManagerId, firstName, lastName, emailAddress, phoneNumber,
					managesPark, userName, password, isLoggedIn);
			return parkManagerUser;
		} else {
			return null;
		} // There is no parkManagerUser with this userName and password
	}

	/**
	 * This method gets userName and password of
	 * ParkManager/DepartmentManager/ParkEmployee
	 * 
	 * @return instance of systemUser of the ParkManager or null if there is no
	 *         parkManagerUser with this userName and password
	 * @throws CommunicationException
	 */
	private SystemUser checkDepartmentManagerCredential(String userName, String password)
			throws CommunicationException {
		// Creating a select query in order to check if such a departmentManagerUser
		// exists in the department_manager table and get all his details
		Communication requestForDepartmentManager = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForDepartmentManager.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForDepartmentManager.setTables(Arrays.asList("department_manager"));
		requestForDepartmentManager.setSelectColumns(Arrays.asList("*"));
		requestForDepartmentManager.setWhereConditions(Arrays.asList("userName", "password"),
				Arrays.asList("=", "AND", "="), Arrays.asList(userName, password));
		GoNatureClientUI.client.accept(requestForDepartmentManager); // sending the query to the server that will
																		// connect to the DB

		if (!requestForDepartmentManager.getResultList().isEmpty()) // getResultList will return list if there is
																	// departmentManagerUser with this userName&password
		{
			// insert the departmentManagerUser details to an ArrayList
			ArrayList<Object[]> departmentManagerResult = requestForDepartmentManager.getResultList();

			// insert relevant objects from the ArrayList to values
			String departmentManagerId = (String) departmentManagerResult.get(0)[0];
			String firstName = (String) departmentManagerResult.get(0)[1];
			String lastName = (String) departmentManagerResult.get(0)[2];
			String emailAddress = (String) departmentManagerResult.get(0)[3];
			String phoneNumber = (String) departmentManagerResult.get(0)[4];
			String managesDepartment = (String) departmentManagerResult.get(0)[5];
			boolean isLoggedIn = ((Integer) departmentManagerResult.get(0)[8]) == 0 ? false : true;

			// creating an instance of the parkManagerUser and return it
			DepartmentManager departmentManager = new DepartmentManager(departmentManagerId, firstName, lastName,
					emailAddress, phoneNumber, managesDepartment, userName, password, isLoggedIn);
			return departmentManager;
		} else {
			return null;
		} // There is no parkManagerUser with this userName and password
	}

	/**
	 * This method gets userName and password of
	 * ParkManager/DepartmentManager/ParkEmployee
	 * 
	 * @return instance of systemUser of the ParkManager or null if there is no
	 *         parkManagerUser with this userName and password
	 * @throws CommunicationException
	 */
	private SystemUser checkParkEmployeeCredential(String userName, String password)
			throws CommunicationException {
		String[] parkEmployees = { "acadia_park_employees", "big_bend_park_employees", "congaree_park_employees",
				"everglades_park_employees", "gateway_arch_park_employees", "glacier_park_employees",
				"grand_canyon_park_employees", "great_smoky_mountains_park_employees",
				"hawaii_volcanoes_park_employees", "hot_springs_park_employees", "mammoth_cave_park_employees",
				"olympic_park_employees", "shenandoah_park_employees", "theodore_roosevelt_park_employees",
				"voyageurs_park_employees", "yellowstone_park_employees", "yosemite_park_employees" };

		for (String park_employees : parkEmployees) {
			System.out.println(park_employees);

			// Creating a select query in order to check if such a parkEmployeeUser exists
			// in the parkEmployees tables and get all his details
			Communication requestForparkEmployee = new Communication(CommunicationType.QUERY_REQUEST);
			// creating the request for the availability check
			try {
				requestForparkEmployee.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			requestForparkEmployee.setTables(Arrays.asList(park_employees));
			requestForparkEmployee.setSelectColumns(Arrays.asList("*"));
			requestForparkEmployee.setWhereConditions(Arrays.asList("userName", "password"),
					Arrays.asList("=", "AND", "="), Arrays.asList(userName, password));
			GoNatureClientUI.client.accept(requestForparkEmployee); // sending the query to the server that will connect
																	// to the DB

			if (!requestForparkEmployee.getResultList().isEmpty()) // getResultList will return list if there is
																	// parkEmployeeUser with this userName&password
			{
				// insert the parkEmployeeUser details to an ArrayList
				ArrayList<Object[]> parkEmployeesResult = requestForparkEmployee.getResultList();

				// insert relevant objects from the ArrayList to values
				String employeeId = (String) parkEmployeesResult.get(0)[0];
				String firstName = (String) parkEmployeesResult.get(0)[1];
				String lastName = (String) parkEmployeesResult.get(0)[2];
				String emailAddress = (String) parkEmployeesResult.get(0)[3];
				String phoneNumber = (String) parkEmployeesResult.get(0)[4];
				boolean isLoggedIn = ((Integer) parkEmployeesResult.get(0)[7]) == 0 ? false : true;

				// creating an instance of the parkEmployeeUser and return it:

				ParkEmployee parkEmployeeUser = new ParkEmployee(employeeId, firstName, lastName, emailAddress,
						phoneNumber, userName, password, isLoggedIn);

				// set the park this parkEmployeeUser workingIn:

				// Changing the name of the table in which we found the employee to the name of
				// the park it represents
				String parkName = converFromParkTableToParkName(park_employees, "_park_employees"); // convert the name
																									// of table
				// getting the park instance:
				Park park = getPark(parkName);
				// set the workingIn field of ParkEmployee
				parkEmployeeUser.setWorkingIn(park);

				return parkEmployeeUser;
			}
		}
		return null;
	}

	public String converFromParkTableToParkName(String nameOfParkTable, String parkTableType) {

		// remove the suffix from the table name .
		// for example for:nameOfParkTable="mammoth_cave_park_employees" and
		// parkTableType="_park_employees"
		// parkName will be: "mammoth_cave"
		String[] parkNameInParts = nameOfParkTable.split(parkTableType);
		String parkName = parkNameInParts[0];

		// Capitalize the first letter of each word and replace "_" in " "
		// for example mammoth_cave to Mammoth Cave
		String[] parkNameInParts2 = parkName.split("_");
		StringBuilder capitalizedParkName = new StringBuilder();
		for (String word : parkNameInParts2) {
			// Capitalize the first letter of each word
			capitalizedParkName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase())
					.append(" "); // Add a space after each word
		}
		// convert the value from StringBuilder to String
		// and removing spaces from the end of the word
		return capitalizedParkName.toString().trim();
	}

	public SystemUser checkParkRepresentativeCredential(String userName, String password) {

		// Creating a select query in order to check if such a parkEmployeeUser exists in
		// the representative table and get all his details
		Communication requestForRepresentativeUser = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForRepresentativeUser.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForRepresentativeUser.setTables(Arrays.asList("representative"));
		requestForRepresentativeUser.setSelectColumns(Arrays.asList("*"));
		requestForRepresentativeUser.setWhereConditions(Arrays.asList("userName", "password"), Arrays.asList("=", "AND", "="),
				Arrays.asList(userName, password));
		GoNatureClientUI.client.accept(requestForRepresentativeUser); // sending the query to the server that will connect to
																// the DB

		if (!requestForRepresentativeUser.getResultList().isEmpty()) // getResultList will return list if there is
																// representativeUser with this userName&password
		{
			// insert the representativeUser details to an ArrayList
			ArrayList<Object[]> parkRepresentativeResult = requestForRepresentativeUser.getResultList();

			// insert relevant objects from the ArrayList to values
			String representativeId = (String) parkRepresentativeResult.get(0)[0];
			String firstName = (String) parkRepresentativeResult.get(0)[1];
			String lastName = (String) parkRepresentativeResult.get(0)[2];
			String emailAddress = (String) parkRepresentativeResult.get(0)[3];
			String phoneNumber = (String) parkRepresentativeResult.get(0)[4];
			boolean isLoggedIn = ((Integer) parkRepresentativeResult.get(0)[7]) == 0 ? false : true;
			// creating an instance of the representativeUser and return it
			Representative representativeUser = new Representative(representativeId, firstName, lastName, emailAddress,
					phoneNumber, userName, password, isLoggedIn);
			return representativeUser;
		} 
		else 
		{
			return null;  // There is no representativeUser with this userName and password
		}

	}

	private Park getPark(String parkName) {
		// Creating a select query in order to get all the details of specific park
		// (according to the park name)
		Communication requestForPark = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForPark.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForPark.setTables(Arrays.asList("park"));
		requestForPark.setSelectColumns(Arrays.asList("*"));
		requestForPark.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="), Arrays.asList(parkName));
		GoNatureClientUI.client.accept(requestForPark); // sending the query to the server that will connect to the DB

		if (!requestForPark.getResultList().isEmpty()) // getResultList will return list if there is park with this
														// parkName
		{
			// insert the park details to an ArrayList
			ArrayList<Object[]> parkResult = requestForPark.getResultList();

			// insert relevant objects from the ArrayList to values
			int parkId = (int) parkResult.get(0)[0];
			String city = (String) parkResult.get(0)[2];
			String state = (String) parkResult.get(0)[3];
			String department = (String) parkResult.get(0)[4];
			String parkManagerId = (String) parkResult.get(0)[5];
			String departmentManagerId = (String) parkResult.get(0)[6];
			int maximumVisitorsCapacity = (int) parkResult.get(0)[7];
			int maximumOrderAmount = (int) parkResult.get(0)[8];
			int maximumTimeLimit = (int) parkResult.get(0)[9];
			int currentCapacity = (int) parkResult.get(0)[10];

			// creating an instance of the parkManagerUser and return it
			Park park = new Park(parkId, parkName, city, state, department, parkManagerId, departmentManagerId,
					maximumVisitorsCapacity, maximumOrderAmount, maximumTimeLimit, currentCapacity);
			return park;
		} else {
			return null;
		} // There is no parkManagerUser with this userName and password
	}

	/**
	 * This method gets instance of SystemUser according to it's isLoggedIn field
	 * 
	 * @return True-if this SystemUser is already logged in,false-if not
	 */
	public boolean checkAlreadyLoggedIn(SystemUser systemUser) {
		return systemUser.isLoggedIn();
	}

	/**
	 * This method gets String of idNumber
	 * 
	 * @return String of: "traveler"/"grouGuide"/"none" according to if there is
	 *         user with this id and what type of user
	 */
	public boolean checkIdInTravelerUsers(String idNumber) {

		// check if there is travelerUser with this idNumber:
		Communication requestForTraveler = new Communication(CommunicationType.QUERY_REQUEST);

		// creating the request for the availability check
		try {
			requestForTraveler.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForTraveler.setTables(Arrays.asList("traveller")); /// ********///
		requestForTraveler.setSelectColumns(Arrays.asList("*"));
		requestForTraveler.setWhereConditions(Arrays.asList("travellerId"), /// ********///
				Arrays.asList("="), Arrays.asList(idNumber));

		GoNatureClientUI.client.accept(requestForTraveler); // sending the query to the server that will connect to the
															// DB

		if (!requestForTraveler.getResultList().isEmpty()) // getResultList will return list if there is travelerUser
															// with this id
		{
			return true;
		}
		return false;

//		//there is no travelerUser with this idNumber
//		//check if there is a groupGuideUser with this idNumber:
//		else 
//		{
//			Communication requestForGrupGuide = new Communication(CommunicationType.QUERY_REQUEST);
//			// creating the request for the availability check
//			try
//			{
//				requestForGrupGuide.setQueryType(QueryType.SELECT);
//			} 
//			catch (CommunicationException e)
//			{
//				e.printStackTrace();
//			}
//					
//			requestForGrupGuide.setTables(Arrays.asList("group_guide"));
//			requestForGrupGuide.setSelectColumns(Arrays.asList("*"));
//			requestForGrupGuide.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="),      
//					  						Arrays.asList(idNumber));
//			GoNatureClientUI.client.accept(requestForGrupGuide); //sending the query to the server that will connect to the DB
//					
//			if(!requestForGrupGuide.getResultList().isEmpty())  //getResultList will return list if there is groupGuideUser with this userName&password
//			{
//				identity="groupGuide";
//				return 	identity;			
//			}			
//		}
//		//there is no user with this id (return "none")
//		return identity;
	}

	/**
	 * The method receives:name of a table that contains details of system
	 * users,name of a column that stores id, and id number of a system user who
	 * wants to login
	 * 
	 * @return true if the isLoggedIn information of the user with this id has been
	 *         successfully updated to 1 in the DB, otherwise false.
	 */
	public boolean updateUserIsLoggedIn(String tableName, String IdCol, String idUser) {
		// update that the user LoggedIn -> change the isLoggedIn value from 0 (user was
		// logged out),to 1(user logged in)
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
			request.setTables(Arrays.asList(tableName));
			request.setColumnsAndValues(Arrays.asList("isLoggedIn"), Arrays.asList('1')); // change isLoggedIn column
																							// value to 1
			request.setWhereConditions(Arrays.asList(IdCol), Arrays.asList("="), Arrays.asList(idUser));
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
	 * This method gets idNumber of travelerUser
	 * 
	 * @return True-if this travelerUser is already logged in,false-if not
	 */
	public boolean checkAlreadyLoggedInForTraveler(String idNumber) {
		// check travelerUser with this idNumber is logged in:
		Communication requestForTraveler = new Communication(CommunicationType.QUERY_REQUEST);

		// creating the request for the availability check
		try {
			requestForTraveler.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForTraveler.setTables(Arrays.asList("traveller")); 
		requestForTraveler.setSelectColumns(Arrays.asList("*"));
		requestForTraveler.setWhereConditions(Arrays.asList("travellerId"),
				Arrays.asList("="), Arrays.asList(idNumber));

		GoNatureClientUI.client.accept(requestForTraveler); // sending the query to the server that will connect to the
															// DB

		if (!requestForTraveler.getResultList().isEmpty()) // getResultList will return list if there is travelerUser
															// with this id
		{
			// insert the travelerUser details to an ArrayList
			ArrayList<Object[]> travelerUserResult = requestForTraveler.getResultList();
			boolean isLoggedIn = ((Integer) travelerUserResult.get(0)[1]) == 0 ? false : true;
			if (isLoggedIn) // the travelerUser is already loggedIn
			{
				return true;
			}
			return false; // the travelerUser is not loggedIn
		}
		return false;
	}

	/**
	 * This method gets idNumber
	 * 
	 * @return True-if exist groupGuide with this id,false-if not
	 */
	public boolean checkIfIdOfGroupGuide(String idNumber) {

		// Creating a select query in order to check if such a groupGuideUser with this
		// idNumber exists in
		// the group_guide table
		Communication requestForGrupGuide = new Communication(CommunicationType.QUERY_REQUEST);
		// creating the request for the availability check
		try {
			requestForGrupGuide.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		requestForGrupGuide.setTables(Arrays.asList("group_guide"));
		requestForGrupGuide.setSelectColumns(Arrays.asList("*"));
		requestForGrupGuide.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="),
				Arrays.asList(idNumber));
		GoNatureClientUI.client.accept(requestForGrupGuide); // sending the query to the server that will connect to the
																// DB

		if (!requestForGrupGuide.getResultList().isEmpty()) // getResultList will return list if there is groupGuideUser
															// with this idNumber
		{
			return true; // groupGuideUser with this idNumber is exists
		}
		return false;

	}

	/**
	 * This method gets idNumber
	 * 
	 * @return True-if there is active booking with this id,false-if not
	 */
	public boolean checkIfIdOfActivetBooking(String idNumber) {
		String[] parkActiveBooking = { "acadia_park_active_booking", "big_bend_park_active_booking",
				"congaree_park_active_booking", "everglades_park_active_booking", "gateway_arch_park_active_booking",
				"glacier_park_active_booking", "grand_canyon_park_active_booking",
				"great_smoky_mountains_park_active_booking", "hawaii_volcanoes_park_active_booking",
				"hot_springs_park_active_booking", "mammoth_cave_park_active_booking", "olympic_park_active_booking",
				"shenandoah_park_active_booking", "theodore_roosevelt_park_active_booking",
				"voyageurs_park_active_booking", "yellowstone_park_active_booking", "yosemite_park_active_booking" };

		for (String park_active_booking : parkActiveBooking) {
			// Creating a select query in order to check if such a parkActiveBooking with
			// this idNumber is exists
			// in the parkActiveBooking tables
			Communication requestForparkActiveBooking = new Communication(CommunicationType.QUERY_REQUEST);
			// creating the request for the availability check
			try {
				requestForparkActiveBooking.setQueryType(QueryType.SELECT);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}

			requestForparkActiveBooking.setTables(Arrays.asList(park_active_booking));
			requestForparkActiveBooking.setSelectColumns(Arrays.asList("*"));
			requestForparkActiveBooking.setWhereConditions(Arrays.asList("idNumber"), Arrays.asList("="),
					Arrays.asList(idNumber));
			GoNatureClientUI.client.accept(requestForparkActiveBooking); // sending the query to the server that will
																			// connect to the DB

			if (!requestForparkActiveBooking.getResultList().isEmpty()) // getResultList will return list if there is
																		// parkActiveBooking with this idNumber
			{
				return true; // parkActiveBooking with this idNumber is exists
			}
		}
		return false; // parkActiveBooking with this idNumber is not exists
	}

	/**
	 * This method gets idNumber
	 * 
	 * @return True-if successfully add new traveler to the traveller table ,false
	 *         otherwise
	 */
	public boolean addNewTraveler(String idNumber) {

		// Creating an insert query in order to add
		// new traveler to the traveller table
		Communication RequestForUpdateParkparameter = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			RequestForUpdateParkparameter.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		RequestForUpdateParkparameter.setTables(Arrays.asList("traveller"));
		RequestForUpdateParkparameter.setColumnsAndValues(Arrays.asList("travellerId", "isLoggedIn"),
				Arrays.asList(idNumber, 1));

		GoNatureClientUI.client.accept(RequestForUpdateParkparameter); // sending the query to the server that will
																		// connect to the DB

		// getQueryResult will return true if the insert query was successful, false
		// otherwise
		return RequestForUpdateParkparameter.getQueryResult();
	}

}
