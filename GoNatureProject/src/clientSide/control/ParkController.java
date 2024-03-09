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
}
