package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import entities.Park;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class ParkController {
	private static ParkController instance;
	
	private ParkController() {
		
	}
	
	public static ParkController getInstance() {
		if (instance == null)
			instance = new ParkController();
		return instance;
	}
	
	public String nameOfTable(Park park) {
		return park.getParkName().toLowerCase().replaceAll(" ", "_");
	}
	
	public ArrayList<Park> fetchParks() {
		// creating a communication request to fetch the data from the database
		Communication requestParks = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			requestParks.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		requestParks.setTables(Arrays.asList("park"));
		requestParks.setSelectColumns(Arrays.asList("*"));
		GoNatureClientUI.client.accept(requestParks); // sending to server side

		ArrayList<Park> parkList = new ArrayList<>();
		// getting the result
		if (parkList != null && !parkList.isEmpty())
			parkList.removeAll(parkList);
		// setting the Object[] from DB to the parkList
		for (Object[] row : requestParks.getResultList()) {
			Park park = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4],
					(String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8], (Integer) row[9],
					(Integer) row[10]);
			parkList.add(park);
		}

		return parkList;
	}
	
    /**
     * @param park
     * @return 
     * An 'UPDATE' SQL query is generated to access the 'park' table in the database and retrieve the 'currentCapacity' 
     * field for the relevant park. 
     * In case visitors only arrive at the park, the value of 'amount' will be positive and the currentCapacity
     * will increase. Otherwise, the opposite.
     * This indicates to the managers the capacity of a specific park.
     * It returns a String describing the capacity 
     */
    public boolean updateCurrentCapacity(String park, int amount) {
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.UPDATE);
	    	request.setTables(Arrays.asList("park"));
	    	request.setColumnsAndValues(Arrays.asList("currentCapacity"), Arrays.asList(+amount));
	    	request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),Arrays.asList(park));
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
     * A 'SELECT' SQL query is generated to access the 'park' table in the database and retrieve the 'currentCapacity' 
     * field for the relevant park. 
     * This indicates to the employees the capacity of a specific park, in order to determine if a specific 
     * number of visitors can currently be accommodated.
     * It returns 2 Strings describing the currentCapacity and the maximumVisitorsCapacity.
     */
    public String[] checkCurrentCapacity(String park) {
		String[] retValue = new String[2];
    	Communication request = new Communication(CommunicationType.QUERY_REQUEST);
    	try {
			request.setQueryType(QueryType.SELECT);
	    	request.setTables(Arrays.asList("park"));
	    	request.setSelectColumns(Arrays.asList("currentCapacity","maximumVisitorsCapacity"));
	    	request.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="),Arrays.asList(park));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
    	GoNatureClientUI.client.accept(request);
    	ArrayList<Object[]> result = request.getResultList();
    	//Saves the retrieved values from the database in order to return them to the requesting employee
    	for (int i = 0; i < 2; i++) {
        	if (!result.isEmpty()) {
        		Object[] capacityDB = result.get(i);
        		 if (capacityDB.length > 0) {
        			 retValue[i] = capacityDB[0].toString();   
        		 }	
        	}
    	}
    	return retValue;
    }
    


}
