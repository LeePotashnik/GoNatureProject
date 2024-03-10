package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class GoNatureUsersController {

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
     * @param park
     * @return 
     * A 'SELECT' SQL query is generated to access the 'park' table in the database and retrieve the 'maximumVisitorsCapacity' 
     * field for the relevant park. 
     * This indicates to the managers the capacity of a specific park.
     * It returns a String describing the capacity 
     */
    public String checkCurrentMaximumCapacity(String park) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList("park"));
	    	request.setSelectColumns(Arrays.asList("maximumVisitorsCapacity"));
	    	request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),Arrays.asList(park));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
    	if (!result.isEmpty()) {
    		Object[] capacityDB = result.get(0);
    		 if (capacityDB.length > 0) {
                 return capacityDB[0].toString();   
    		 }	
    	}
    	return null;
    }
}