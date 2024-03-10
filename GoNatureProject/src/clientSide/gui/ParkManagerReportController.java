package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import clientSide.control.ParkController;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Park;
import entities.ParkManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

public class ParkManagerReportController extends AbstractScreen implements Stateful{
	
	private ParkManager manager;
	private Park park;
    @FXML
    private Button backButton;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private ChoiceBox<String> choiceBoxMonth;
    @FXML
    private ChoiceBox<String> choiceBoxYear;
    @FXML
    private Button UsageReportBtn;

    @FXML
    private Button TotalVisitorsBtn;

    @FXML
    void TotalVisitorsReport(ActionEvent event) {
   	 try {
	        ScreenManager.getInstance().showScreen("TotalVisitorsReportScreen", "/clientSide/fxml/TotalNumberOfVisitorsReport.fxml", false, false, StageSettings.defaultSettings("Total number of visitors Report"), null);
	    } catch (StatefulException | ScreenException e) {
			e.printStackTrace();
	    }
    }

    @FXML
    void UsageReport(ActionEvent event) {
   	 try {
	        ScreenManager.getInstance().showScreen("UsageReportScreen", "/clientSide/fxml/UsageReport.fxml", false, false, StageSettings.defaultSettings("Usage Report"), null);
	    } catch (StatefulException | ScreenException e) {
			e.printStackTrace();
	    }
    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
    }
	@Override
	public void initialize() {
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		List<String> months = Arrays.asList("01", "02", "03", "04", "05", "06","07", "08", "09", "10", "11", "12");
		List<String> years=Arrays.asList("2022","2023","2024");
		// Add all months  and years to the ChoiceBox
		choiceBoxMonth.getItems().addAll(months);
		choiceBoxYear.getItems().addAll(years);
			    
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
	 	backImage.setFitWidth(30);
	 	backImage.setPreserveRatio(true);
	 	backButton.setGraphic(backImage);
	 	backButton.setPadding(new Insets(1, 1, 1, 1));
	 	
	}
	/**
	 * Generate the report based on the selected month.
	 * @param month the month selected from the ChoiceBox.
	 */
	private Pair<Integer, Integer> generateUsageReport(String selectedMonth,String selectedYear ,Park park) throws CommunicationException {
		// Create a new Communication object for a query request
	    String parkTableName=ParkController.getInstance().nameOfTable(park)+"_done_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("visitType","numberOfVisitors"));    
	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	    LocalDate from= LocalDate.of(year, month, 1);
	    LocalDate to=from.plusMonths(1).minusDays(1);
	    comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<=") , Arrays.asList(from,to));
	    GoNatureClientUI.client.accept(comm);
	    
	    int countIndividual=0;
	    int countGroup=0;
	    for (Object[] row : comm.getResultList())
	    {
	    	if(((String)row[0]).equals("group"))
	    		countGroup+=(Integer)row[1];
	    	else
	    		countIndividual+=(Integer)row[1];
	    }
	    Pair<Integer, Integer> pairResult = new Pair<>(countIndividual, countGroup);
	    return pairResult;
//	    כדי לגשת לראשון:
//	    	pair.getKey();
//	    	כדי לגשת לשני:
//	    	pair.getValue();
	}
	    

	@Override
	public void loadBefore(Object information) {
		manager=(ParkManager)information;
		park=manager.getManages();
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState() {
		// TODO Auto-generated method stub
		
	}

}
