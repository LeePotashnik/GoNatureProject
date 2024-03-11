package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import clientSide.control.ParkController;
import common.communication.Communication.QueryType;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;


public class DepartmentManagerReportsControllerScreen extends AbstractScreen implements Stateful{
	
	private ArrayList<Park> parks;
	// JAVA FX COMPONENTS
    @FXML
    private ImageView goNatureLogo;
    @FXML
    private Button backButton;
    @FXML
    private Button visitReportBtn;
    @FXML
    private Button cancellationReportBtn;
    @FXML
    private ChoiceBox<String> choiceBoxMonth;
    @FXML
    private ChoiceBox<String> choiceBoxPark;
    @FXML
    private ChoiceBox<String> choiceBoxYear;
    
    @FXML
    void cancellationReport(ActionEvent event) {
    	String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
        String selectedPark= choiceBoxPark.getValue();
     // Validate that month,park and year are selected
        if (selectedMonth == null || selectedYear == null || selectedPark == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select month, year and park before generating the report.");
            return;
        } 
    	try {
    	        ScreenManager.getInstance().showScreen("CancellationReportScreen", "/clientSide/fxml/cancellationReport.fxml", false, false, StageSettings.defaultSettings("Cancellation Report"), null);
    	    } catch (StatefulException | ScreenException e) {
    			e.printStackTrace();
    	    }
    }
	
    private HashMap<Integer, Pair<Integer, Integer>> generateCancellationReport(String selectedMonth, String selectedYear, String selectedPark, Park park) throws CommunicationException {
		// Create a new Communication object for a query request
		if (park == null) {
		    throw new IllegalArgumentException("Park cannot be null");
		}
		String parkTableName=ParkController.getInstance().nameOfTable(park)+"_park_cancelled_booking";
		Communication comm = new Communication(Communication.CommunicationType.QUERY_REQUEST);
	    // Set the type of the query
	    try {
			comm.setQueryType(Communication.QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	    comm.setTables(Arrays.asList(parkTableName)); 
	    comm.setSelectColumns(Arrays.asList("dayOfVisit", "cancellationReason", "numberOfVisitors"));   
	    int month=Integer.parseInt(selectedMonth);
	    int year=Integer.parseInt(selectedYear);
	 // Adjust where conditions to filter by month and year
	    comm.setWhereConditions(Arrays.asList("MONTH(dayOfVisit)", "YEAR(dayOfVisit)"),
	                            Arrays.asList("=", "AND", "="),
	                            Arrays.asList(selectedMonth, selectedYear));

	    String res1="Client cancelled the reminder";
	    String res2="Did not arrive";
	    //comm.setWhereConditions(Arrays.asList("cancellationReason","cancellationReason"),Arrays.asList("=", "AND", "=") , Arrays.asList(res1,res2));
	    GoNatureClientUI.client.accept(comm);
	    //dailyCounts map will contain a Pair for each day of the month, where the first item in the Pair is the count of cancellations and the second item is the count of no-shows.
	    HashMap<Integer, Pair<Integer, Integer>> dailyCounts = new HashMap<>();
	    if (comm.getResultList() != null) {
	        for (Object[] row : comm.getResultList()) {
	            // Extract the day from dayOfVisit
	            LocalDate date = LocalDate.parse((String)row[0]);
	            int day = date.getDayOfMonth();

	            Pair<Integer, Integer> counts = dailyCounts.getOrDefault(day, new Pair<>(0, 0));

	            if (((String)row[1]).equals(res1)) {
	                counts = new Pair<>(counts.getKey() + (Integer)row[2], counts.getValue());
	            } else if (((String)row[1]).equals(res2)) {
	                counts = new Pair<>(counts.getKey(), counts.getValue() + (Integer)row[2]);
	            }

	            dailyCounts.put(day, counts);
	        }
	    }

	    return dailyCounts;
	}

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
    }

    @FXML
    void visitReport(ActionEvent event) {
    	String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
        String selectedPark= choiceBoxPark.getValue();
    	// Validate that month,park and year are selected
        if (selectedMonth == null || selectedYear == null || selectedPark == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select month, year and park before generating the report.");
            return;
        }  
    	try {
 	        ScreenManager.getInstance().showScreen("VisitReportScreen", "/clientSide/fxml/VisitReport.fxml", false, false, StageSettings.defaultSettings("Visit Report"), null);
 	    } catch (StatefulException | ScreenException e) {
 			e.printStackTrace();
 	    }
    }

  
    ///// --- FXML / JAVA FX METHODS --- /////
	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
    public void initialize() {
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
	    List<String> months = Arrays.asList("01", "02", "03", "04", "05", "06","07", "08", "09", "10", "11", "12");
	    List<String> years=Arrays.asList("2021","2022","2023","2024");
	    // Add all months and years to the ChoiceBox
	    choiceBoxMonth.getItems().addAll(months);
	    choiceBoxYear.getItems().addAll(years);
	    
	    List<String> Eastern = Arrays.asList("Acadia", "Great Smoky Mountains", "Shenandoah");
	    List<String> Southern = Arrays.asList("Big Bend", "Congaree", "Everglades");
	    List<String> Central = Arrays.asList("Gateway Arch", "Hot Springs", "Mammoth Cave");
	    List<String> Northern = Arrays.asList("Glacier", "Theodore Roosevelt", "Voyageurs");
	    List<String> Western = Arrays.asList("Grand Canyon", "Yellowstone", "Yosemite");
	    List<String> Pacific = Arrays.asList("Hawaii Volcanoes", "Olympic");
	    // Add all months to the ChoiceBox
	    choiceBoxPark.getItems().addAll(Southern);
	 // setting the back button image
	 	ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
	 	backImage.setFitWidth(30);
	 	backImage.setPreserveRatio(true);
	 	backButton.setGraphic(backImage);
	 	backButton.setPadding(new Insets(1, 1, 1, 1));
    }

	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
