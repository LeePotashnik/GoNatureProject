package clientSide.control;

import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.Park;

public class ReportsController {
	private static ReportsController instance;

	private ReportsController() {

	}

	public static ReportsController getInstance() {
		if (instance == null)
			instance = new ReportsController();
		return instance;
	}

	public Park getPark(String parkName) {
		Communication getPark = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			getPark.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		getPark.setTables(Arrays.asList("park"));
		getPark.setSelectColumns(Arrays.asList("*"));
		getPark.setWhereConditions(Arrays.asList("parkName"), Arrays.asList("="), Arrays.asList(parkName));
		GoNatureClientUI.client.accept(getPark);

		// getting the result
		if (getPark.getResultList().isEmpty()) { // no park like that
			return null;
		}
		
		// setting the park's data into a park object and returning it
		Object[] row = getPark.getResultList().get(0);
		Park park = new Park((Integer) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4],
				(String) row[5], (String) row[6], (Integer) row[7], (Integer) row[8], (Integer) row[9],
				(Integer) row[10]);
		return park;
	}
}
