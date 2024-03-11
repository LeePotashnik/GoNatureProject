package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clientSide.control.ParkController;
import clientSide.control.ParkManagerReportsController;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
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

public class ParkManagerReportControllerScreen extends AbstractScreen implements Stateful{
	
	private ParkManagerReportsController PMR;
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
    	String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
        // Validate that both month and year are selected
        if (selectedMonth == null || selectedYear == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select both a month and a year before generating the report.");
            return;
        }
        try {
            Pair<Integer, Integer> visitorsData = generateTotalNumberOfVisitorsReport(selectedMonth, selectedYear, this.park);
            ScreenManager.getInstance().showScreen("TotalVisitorsReportScreen", "/clientSide/fxml/TotalNumberOfVisitorsReport.fxml", false, false, StageSettings.defaultSettings("Total number of visitors Report"), visitorsData);
        } catch (CommunicationException | StatefulException | ScreenException e) {
            e.printStackTrace();
            showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred while generating the report.");
        }
    }

    @FXML
    void UsageReport(ActionEvent event) {
    	String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
    	// Validate that both month and year are selected
        if (selectedMonth == null || selectedYear == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select both a month and a year before generating the report.");
            return;
        }
        try {
        	//List<Pair<LocalDate, Integer>> visitorsData = control.generateUsageReport(selectedMonth, selectedYear, this.park);
        	List<Pair<LocalDate, Integer>> visitorsData = generateUsageReport(selectedMonth, selectedYear, this.park);
            ScreenManager.getInstance().showScreen("UsageReportScreen", "/clientSide/fxml/UsageReport.fxml", false, false, StageSettings.defaultSettings("Total number of visitors Report"), visitorsData);
        } catch (CommunicationException | StatefulException | ScreenException e) {
            e.printStackTrace();
            showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred while generating the report.");
        }

    }

    private List<Pair<LocalDate, Integer>> generateUsageReport(String selectedMonth, String selectedYear, Park park) throws CommunicationException {
    	// Create a new Communication object for a query request
    	if (park == null) {
    		System.out.println("null");
    		throw new IllegalArgumentException("Park cannot be null");
    	}
    	String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_done_booking";
    	Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
    	// Set the type of the query
    	try {
    		comm.setQueryType(Communication.QueryType.SELECT);
    	} catch (CommunicationException e) {
    		e.printStackTrace();
    	}
    		comm.setTables(Arrays.asList(parkTableName)); 
    		comm.setSelectColumns(Arrays.asList("dayOfVisit", "numberOfVisitors"));    
    		int month=Integer.parseInt(selectedMonth);
    		int year=Integer.parseInt(selectedYear);
    		LocalDate from= LocalDate.of(year, month, 1);
    		LocalDate to=from.plusMonths(1).minusDays(1);
    		comm.setWhereConditions(Arrays.asList("dayOfVisit","dayOfVisit"),Arrays.asList(">=", "AND", "<=") , Arrays.asList(from,to)); 
    		GoNatureClientUI.client.accept(comm);
    		return processFetchedData(comm.getResultList());

    		
	}
    private List<Pair<LocalDate, Integer>> processFetchedData(List<Object[]> resultList) {
        Map<LocalDate, Integer> aggregatedData = new HashMap<>();
        for (Object[] row : resultList) {
        	LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Integer visitors = (Integer) row[1];
            aggregatedData.merge(date, visitors, Integer::sum);
        }
        
        // Convert aggregated data into a list of pairs
        List<Pair<LocalDate, Integer>> occupancyData = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : aggregatedData.entrySet()) {
            occupancyData.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        
        return occupancyData;
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
		List<String> years=Arrays.asList("2021","2022","2023","2024");
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
    @Override
	public void loadBefore(Object information) {
		ParkManager manager = (ParkManager) information;
		park=manager.getManages();
    }
    
    
    
    /**
	 * Generate the report based on the selected month.
	 * @param month the month selected from the ChoiceBox.
	 */
	private Pair<Integer, Integer> generateTotalNumberOfVisitorsReport(String selectedMonth,String selectedYear ,Park park) throws CommunicationException {
		// Create a new Communication object for a query request
		if (park == null) {
			System.out.println("null");
		    throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_done_booking";
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
	    if (comm.getResultList() != null) {
	    	for (Object[] row : comm.getResultList()) {
	        	if(((String)row[0]).equals("group"))
	        		countGroup+=(Integer)row[1];
	        	else
	        		countIndividual+=(Integer)row[1];
	    	}
	    }
	    Pair<Integer, Integer> pairResult = new Pair<>(countIndividual, countGroup);
		return pairResult;
	}

	@Override
	public String getScreenTitle() {
		return "Park Manger Report";
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
