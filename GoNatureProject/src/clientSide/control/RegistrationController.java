package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;

public class RegistrationController {
	private static RegistrationController instance;
//	private boolean isIdOfTravelerExists = false; // New flag to track conversion

	/**
	 * Private constructor to prevent instantiation from outside the class.
	 */
	private RegistrationController() {
	}

	/**
	 * Retrieves the single instance of the RegistrationController class. If the
	 * instance does not exist, it is created.
	 * 
	 * @return the singleton instance of RegistrationController.
	 */
	public static RegistrationController getInstance() {
		if (instance == null)
			instance = new RegistrationController();
		return instance;
	}

	//////////////////////////////////
	/// DB FETCHING DETAILS METHOD ///
	//////////////////////////////////

	/**
	 * Retrieves the details of a user from the database based on the provided ID.
	 * 
	 * @param id the ID of the users whose details are to be fetched.
	 * @return an ArrayList containing the details of the traveler.
	 */
	public ArrayList<Object[]> getUserDetails(String id) {
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

	/**
     * Checks for the existence of a group guide based on ID and returns a boolean
     * indicating the existence.
     * 
     * @param id The ID of the group guide to check.
     * @return true if the group guide exists, false otherwise.
     */
	public boolean checkGroupGuideExistence(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.groupGuide));
			request.setSelectColumns(Arrays.asList("*"));
			request.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty())
			return false;
		return true;
	}
	
	 /**
     * Checks if a traveler exists in the database by ID and deletes them if they do.
     * 
     * @param id The ID of the traveler to check and possibly delete.
     */
	public void checkIdExistenceAndDeleteFromTravellerTable(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.traveller));
			request.setSelectColumns(Arrays.asList("*"));
			request.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="), Arrays.asList(id));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty())
			return;
		else {
			boolean deleteSuccess = userDeleteFromTable(id, Communication.traveller);
			if(!deleteSuccess) {
				return;
			}
		}
	}

	////////////////////////////////////
	/// DB DELETING TRAVELLER METHOD ///
	////////////////////////////////////

	/**
     * Deletes a user's information from the database based on the provided ID.
     * 
     * @param id The ID of the user to be deleted.
     * @param tableName The table from which the user will be deleted.
     * @return true if the deletion was successful, false otherwise.
     */
	public boolean userDeleteFromTable(String id, String tableName) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.DELETE);
			request.setTables(Arrays.asList(tableName));
			request.setWhereConditions(Arrays.asList(identifyUser(tableName)), Arrays.asList("="), Arrays.asList(id));
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

	/////////////////////////
	/// DB INSERT METHODS ///
	/////////////////////////

	/**
	 * Inserts a new group guide into the database with the provided details.
	 * 
	 * @param id        the ID of the new group guide.
	 * @param firstName the first name of the new group guide.
	 * @param lastName  the last name of the new group guide.
	 * @param email     the email of the new group guide.
	 * @param phone     the phone number of the new group guide.
	 * @param userName  the userName of the new group guide.
	 * @param password  the password for the new group guide.
	 * @return true if the insertion was successful, false otherwise.
	 */
	public boolean groupGuideInsertToDB(String id, String firstName, String lastName, String email, String phone,
			String userName, String password) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.INSERT);
			request.setTables(Arrays.asList(Communication.groupGuide));
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

	

	//////////////////////////
	//// INSTANCE METHODS ////
	//////////////////////////

	
	/**
     * Identifies the column name to use for user identification based on the table name.
     * 
     * @param tableName The name of the table for which to identify the user column.
     * @return The column name used for user identification.
     */
	@SuppressWarnings("unused")
	private String identifyUser(String tableName) {
		switch (tableName) {
		case "traveller":
			return "travellerId";
		case "system_users":
			return "userId";
		}
		defult:
			return "";
	}
	

}
