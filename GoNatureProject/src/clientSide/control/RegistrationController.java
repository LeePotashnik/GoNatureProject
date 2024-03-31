package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.entities.SystemUser;
import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.SecondaryRequest;
import common.communication.CommunicationException;
import javafx.util.Pair;

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
	 * @return a Pair indicating a SystemUser and if it can be accessed, setermined
	 *         by a boolean value
	 */
	public Pair<SystemUser, Boolean> getUserDetails(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		request.setCritical(true, 17); // uses the mutex semaphore on the server side
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList("system_users"));
			request.setSelectColumns(Arrays.asList("firstName", "lastName", "emailAddress", "phoneNumber", "userName",
					"password", "isLocked"));
			request.setWhereConditions(Arrays.asList("userId", "type"), Arrays.asList("=", "AND", "="),
					Arrays.asList(id, "User"));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		// setting a secondary request for updating the database
		request.setSecondaryRequest(SecondaryRequest.CHECK_USER_LOCKED);
		request.setIdNumber(id);

		// sending the request to the server side
		GoNatureClientUI.client.accept(request);

		Object[] user = request.getResultList() == null ? null : request.getResultList().get(0);
		SystemUser systemUser = user == null ? null
				: new SystemUser(id, (String) user[0], (String) user[1], (String) user[2], (String) user[3],
						(String) user[4], (String) user[5], false);

		return new Pair<SystemUser, Boolean>(systemUser, request.getQueryResult());

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
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setTables(Arrays.asList(Communication.groupGuide));
		request.setSelectColumns(Arrays.asList("*"));
		request.setWhereConditions(Arrays.asList("groupGuideId"), Arrays.asList("="), Arrays.asList(id));

		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result == null || result.isEmpty())
			return false;
		return true;
	}

	/**
	 * Checks if a traveler exists in the database by ID and deletes them if they
	 * do.
	 * 
	 * @param id The ID of the traveler to check and possibly delete.
	 */
	public void checkIdExistenceAndDeleteFromTravellerTable(String id) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setTables(Arrays.asList(Communication.traveller));
		request.setSelectColumns(Arrays.asList("*"));
		request.setWhereConditions(Arrays.asList("travellerId"), Arrays.asList("="), Arrays.asList(id));

		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result == null || result.isEmpty())
			return;
		else {
			boolean deleteSuccess = userDeleteFromTable(id, Communication.traveller);
			if (!deleteSuccess) {
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
	 * @param id        The ID of the user to be deleted.
	 * @param tableName The table from which the user will be deleted.
	 * @return true if the deletion was successful, false otherwise.
	 */
	public boolean userDeleteFromTable(String id, String tableName) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.DELETE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setTables(Arrays.asList(tableName));
		request.setWhereConditions(Arrays.asList(identifyUser(tableName)), Arrays.asList("="), Arrays.asList(id));

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
	 * @param user the SystemUser instance of the group guide
	 */
	public boolean groupGuideInsertToDB(SystemUser user) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.INSERT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setTables(Arrays.asList(Communication.groupGuide));
		request.setColumnsAndValues(
				Arrays.asList("groupGuideId", "firstName", "lastName", "emailAddress", "phoneNumber", "userName",
						"password", "isLoggedIn"),
				Arrays.asList(user.getIdNumber(), user.getFirstName(), user.getLastName(), user.getEmailAddress(),
						user.getPhoneNumber(), user.getUsername(), user.getPassword(), 0));

		GoNatureClientUI.client.accept(request);

		return request.getQueryResult();
	}

	/**
	 * This method gets a user id and unlocks it for future access
	 * 
	 * @param id the user's id
	 * @return true if the update succeed, false if not
	 */
	public boolean unlockUser(String id) {
		Communication unlockUser = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			unlockUser.setQueryType(QueryType.UPDATE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		unlockUser.setCritical(true, 17); // mutex semaphore acquiry
		unlockUser.setTables(Arrays.asList(Communication.systemUser));
		unlockUser.setColumnsAndValues(Arrays.asList("isLocked"), Arrays.asList(0));
		unlockUser.setWhereConditions(Arrays.asList("userId"), Arrays.asList("="), Arrays.asList(id));

		// sending the request to the server side
		GoNatureClientUI.client.accept(unlockUser);

		// getting the result
		return unlockUser.getQueryResult();
	}

	//////////////////////////
	//// INSTANCE METHODS ////
	//////////////////////////

	/**
	 * Identifies the column name to use for user identification based on the table
	 * name.
	 * 
	 * @param tableName The name of the table for which to identify the user column.
	 * @return The column name used for user identification.
	 */
	private String identifyUser(String tableName) {
		switch (tableName) {
		case "traveller":
			return "travellerId";
		case "system_users":
			return "userId";
		default:
			return "";
		}
	}

}
