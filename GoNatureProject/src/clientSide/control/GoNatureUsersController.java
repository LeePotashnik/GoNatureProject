package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.MessageType;
import common.communication.Communication.QueryType;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Representative;
import entities.SystemUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;

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
	/**
	 * @param table
	 * @param IdCol
	 * @param ID
	 * @return 
	 * An 'UPDATE' SQL query is generated to access the relevant table in the database and modify the 'isLoggedIn' field,
	 * indicating that the user is no longer connected to the system. 
	 * It returns true if the update succeeds, otherwise, it returns false.
	 */
	public boolean checkLogOut(String table, String IdCol, String ID) {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList(table));
	    	request.setColumnsAndValues(Arrays.asList("isLoggedIn"), Arrays.asList('0'));
	    	request.setWhereConditions(Arrays.asList(IdCol), Arrays.asList("="),Arrays.asList(ID));
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
     * creating a communication request for disconnecting from the server port
     */
    public void disconnectClientFromServer() {
		Communication message = new Communication(CommunicationType.CLIENT_SERVER_MESSAGE);
		message.setMessageType(MessageType.DISCONNECT);
		GoNatureClientUI.client.accept(message);
    }
    
    public SystemUser restoreUser() {
    	return this.user;
    }

	public void saveUser(SystemUser user) {
    	this.user = user;
	}

	public Representative restoreRepresentative() {
    	return this.representative;
    }
	public void saveRepresentative(Representative representative) {
		this.representative = representative;
	}	
	public String restoreTitle() {
		return title;
	}

	public void saveTitle(String title) {
		this.title = title;
	}
    
}