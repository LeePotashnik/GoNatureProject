package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.controllers.ScreenManager;
import common.communication.CommunicationException;

public class RegistrationController {
	private static RegistrationController instance;
	private boolean isIdOfTravelerExists = false; // New flag to track conversion
	
	/**
     * Private constructor to prevent instantiation from outside the class.
     */
	private RegistrationController() {
	}

	/**
     * Retrieves the single instance of the RegistrationController class.
     * If the instance does not exist, it is created.
     * 
     * @return the singleton instance of RegistrationController.
     */
	public static RegistrationController getInstance() {
		if (instance == null)
			instance = new RegistrationController();
		return instance;
	}

	 /**
     * Sets the flag indicating whether the ID of a traveler exists.
     * 
     * @param isIdOfTravelerExists true if the ID exists, false otherwise.
     */
	public void SetIsIdOfTravelerExists(boolean isIdOfTravelerExists) {
		this.isIdOfTravelerExists = isIdOfTravelerExists;
	}

	/**
     * Determines the appropriate column name for user identification based on the provided table name.
     * 
     * @param tableName the name of the table to identify the user from.
     * @return the column name used for user identification in the specified table.
     */
	private String identifyUser(String tableName) {
		switch (tableName) {
		case "traveller":
			return "travellerId";
		case "group_guide":
			return "groupGuideId";
		case "park_manager":
			return "parkManagerId";
		case "department_manager":
			return "departmentManagerId";
		case "representative":
			return "representativeId";
		default:
			return "employeeId";
		}
	}
	
	/**
     * Checks for the existence of a traveler's registration details (userName, email, ID) in a specified table.
     * It returns a string message indicating which of the details already exist in the system.
     * 
     * @param userName the userName to be check for existence.
     * @param email the email to be check for existence.
     * @param id the ID to be check for existence.
     * @param tableName the name of the table to check in.
     * @return a string message detailing existing registration information, if any.
     */
	public String checkExistenceForTravellerRegistration(String userName, String email, String id, String tableName) {
		String ret="";
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(tableName));
			request.setSelectColumns(Arrays.asList("userName","emailAddress",identifyUser(tableName)));
			request.setWhereConditions(Arrays.asList("userName", "emailAddress", identifyUser(tableName)),
					Arrays.asList("=", "OR", "=", "OR", "="), Arrays.asList(userName, email, id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty()) {
			return ret;
		} else {
			for (Object[] row : result) {
				if (((String)row[0]).equals(userName)) {
					ret += "This username already exists in our system\n";
				}
				if (((String)row[1]).equals(email)) {
					ret += "This email address already exists in our system\n";
				}
				if (((String)row[2]).equals(id)) {
					ret += "This id number already exists in our system\n";
				}
			}
			return ret;
		}
			
	}
	
	/**
     * Checks if the given ID exists in the specified table for group guide registration queries.
     * 
     * @param id the ID to be check for existence.
     * @param tableName the name of the table to check in.
     * @return true if the ID exists, false otherwise.
     */
	public boolean checkIdExistenceForGGRegistrationQuary(String id, String tableName) { // check if the given id exists in this table return true if it exists
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(tableName));
			request.setSelectColumns(Arrays.asList(identifyUser(tableName)));
			request.setWhereConditions(Arrays.asList(identifyUser(tableName)),
					Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty()) {
			return false;
		}
		return true;
	}
		
	
	/**
     * Checks for the existence of group guide registration details (userName, email) in a specified table.
     * It returns a string message indicating which of the details already exist in the system.
     * 
     * @param userName the userName to check for existence.
     * @param email the email to check for existence.
     * @param tableName the name of the table to check in.
     * @return a string message detailing existing registration information, if any.
     */
	public String checkExistenceForGGRegistration(String userName, String email, String tableName) {
		String ret="";
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(tableName));
			request.setSelectColumns(Arrays.asList("userName","emailAddress"));
			request.setWhereConditions(Arrays.asList("userName", "emailAddress"),
					Arrays.asList("=", "OR", "="), Arrays.asList(userName, email));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty()) {
			return ret;
		} else {
			for (Object[] row : result) {
				if (((String)row[0]).equals(userName)) {
					ret += "This username already exists in our system\n";
				}
				if (((String)row[1]).equals(email)) {
					ret += "This email address already exists in our system\n";
				}
			}
			return ret;
		}
	}
	
 


	/**
     * Retrieves the details of a traveler from the database based on the provided ID.
     * 
     * @param id the ID of the traveler whose details are to be fetched.
     * @return an ArrayList containing the details of the traveler.
     */
	public ArrayList<Object[]> getTravellerDetails(String id) {
		ArrayList<String> travelerRow = new ArrayList<String>();
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.traveller));
			request.setSelectColumns(Arrays.asList("firstName", "lastName", "emailAddress", "userName",
					"phoneNumber", "password"));
			request.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		return result;

	}

	/**
     * Deletes a traveler's information from the database based on the provided ID.
     * 
     * @param id the ID of the traveler to be deleted.
     * @return true if the deletion was successful, false otherwise.
     */
	public boolean travellerDeleteFromDB(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.DELETE);
			request.setTables(Arrays.asList(Communication.traveller));
			request.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (!result) {
			return false;
		}
		return true;
	}

	/**
     * Inserts a new traveler into the database with the provided details.
     * 
     * @param id the ID of the new traveler.
     * @param firstName the first name of the new traveler.
     * @param lastName the last name of the new traveler.
     * @param email the email of the new traveler.
     * @param phone the phone number of the new traveler.
     * @param userName the userName of the new traveler.
     * @param password the password for the new traveler.
     * @return true if the insertion was successful, false otherwise.
     */
	public boolean travellerInsertToDB(String id, String firstName, String lastName, String email, String phone,
			String userName, String password) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList("traveller"));
			request.setColumnsAndValues(
					Arrays.asList("travellerId", "firstName", "lastName", "emailAddress", "phoneNumber", "userName",
							"password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (!result) {
			return false;
		}
		return true;
	}

	/**
     * Inserts a new group guide into the database with the provided details.
     * 
     * @param id the ID of the new group guide.
     * @param firstName the first name of the new group guide.
     * @param lastName the last name of the new group guide.
     * @param email the email of the new group guide.
     * @param phone the phone number of the new group guide.
     * @param userName the userName of the new group guide.
     * @param password the password for the new group guide.
     * @return true if the insertion was successful, false otherwise.
     */
	public boolean groupGuideInsertToDB(String id, String firstName, String lastName, String email, String phone,
			String userName, String password) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList("group_guide"));
			request.setColumnsAndValues(
					Arrays.asList("groupGuideId", "firstName", "lastName", "emailAddress", "phoneNumber", "userName",
							"password", "isLoggedIn"),
					Arrays.asList(id, firstName, lastName, email, phone, userName, password, 0));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result = request.getQueryResult();
		if (!result) {
			return false;
		}
		return true;
	}
	
	
	/**
     * Checks if the ID of a traveler exists in the system.
     * 
     * @return true if the ID exists, false otherwise.
     */
	public boolean isIdOfTravelerExists() {
	    return this.isIdOfTravelerExists;
	}

}
