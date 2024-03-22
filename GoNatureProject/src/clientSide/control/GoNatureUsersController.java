package clientSide.control;

import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import entities.DepartmentManager;
import entities.ParkEmployee;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import entities.Representative;
import entities.SystemUser;

/**
 * Manages user sessions and interactions within the GoNature application. Utilizing the Singleton design pattern,
 * this controller ensures consistent and centralized management of user states, including logging out users,
 * saving and restoring user sessions, and handling server disconnections.
 * 
 * It supports operations for both SystemUser and Representative entities, allowing for specific session management
 * tasks tailored to the type of user. Communication with the server is managed through the Communication class,
 * facilitating updates and queries to the system's backend.
 */
public class GoNatureUsersController {

	private SystemUser user;
	private String title;
	private Representative representative;
	private static GoNatureUsersController instance;
	/**
	 * An empty and private controller, for the singleton design pattern
	 */
	private GoNatureUsersController() {
	}

	/**
	 * The GoNatureUsersController is defined as a Singleton class. This method allows
	 * creating an instance of the class only once during runtime of the
	 * application.
	 * 
	 * @return the GoNatureUsersController instance
	 */
	public static GoNatureUsersController getInstance() {
		if (instance == null)
			instance = new GoNatureUsersController();
		return instance;
	}
	
    public SystemUser restoreUser() {
    	return this.user;
    }

	public void saveUser(SystemUser user) {
    	this.user = user;
	}	
	
	/**
	 * An 'UPDATE' SQL query is generated to access the relevant table in the database and modify the 'isLoggedIn' field,
	 * indicating that the user is no longer connected to the system. 
	 * It returns true if the update succeeds, otherwise, it returns false.
	 * @param table The database table containing the user's information.
	 * @param IdCol The column name of the user's ID.
	 * @return true if the logout operation succeeds, false otherwise.
	 
	 */
	public boolean checkLogOut(String table, String IdCol) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList(table));
	    	request.setColumnsAndValues(Arrays.asList("isLoggedIn"), Arrays.asList('0'));
	    	request.setWhereConditions(Arrays.asList(IdCol), Arrays.asList("="),Arrays.asList(user.getIdNumber()));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		boolean result=request.getQueryResult();
		if (result)
			return true;
		return false;
	}
    
	
	/**
	 * Initiates the logout process for the current user based on their specific user type.
	 *
	 * The method supports different logic paths for various user types within the GoNature system, including
	 * ParkManager, DepartmentManager, ParkEmployee, Representative, ParkVisitor (specifically group guides), and
	 * a default case for traveler.
	 * @return
	 * If the user instance is null, indicating no user is currently logged in, or if the logout process fails
	 * for any reason (database update failure), the method returns false. 
	 * Otherwise, it returns true, signaling a successful logout.

	 */
	public boolean logoutUser() {
	    if (user != null) {
	        
	        if (user instanceof ParkManager) {
	            // Perform logout logic specific to ParkManager
	            return checkLogOut("park_manager", "parkManagerId");
	        } else if (user instanceof DepartmentManager) {
	            // Perform logout logic specific to DepartmentManager
	            return checkLogOut("department_manager", "departmentManagerId");
	        } else if (user instanceof ParkEmployee) {
	        	// Perform logout logic specific to ParkEmployee
	        	ParkEmployee employee = (ParkEmployee) user;
	            String parkTable = ParkController.getInstance().nameOfTable(employee.getWorkingIn()) + "_park_employees";
	            return checkLogOut(parkTable, "employeeId");
	        } else if (user instanceof Representative) {
	            // Perform logout logic specific to Representative
	            return checkLogOut("representative", "representativeId");
	        } else if (user instanceof ParkVisitor) {
	            // Perform logout logic specific to GroupGuide
	            ParkVisitor visitor = (ParkVisitor) user;
	            if (visitor.getVisitorType() == VisitorType.GROUPGUIDE)
	            	return checkLogOut("group_guide", "groupGuideId");
	        } else {
	            // Default logout logic if none of the above matches
	            return checkLogOut("traveller", "userId");
	        }
	    }
	    return false; // Return false if user is null or logout fails
	}

	
	/**
	 * Initiates the disconnection of the client from the server. This method creates a communication request
	 * to signal the server that the client intends to disconnect, ensuring a clean termination of the session.
	 */
    public void disconnectClientFromServer() {
    	Communication message = new Communication(CommunicationType.CLIENT_SERVER_MESSAGE);
		message.setClientMessageType(ClientMessageType.DISCONNECT);
		GoNatureClientUI.client.accept(message);
    }
    
}