package clientSide.gui;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import clientSide.control.ParkController;
import clientSide.control.ReportsController;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

public class ParkManagerReportScreenController extends AbstractScreen implements Stateful{
	
	private ReportsController control;//controller
	private ParkManager parkManager;
	private Park park;
	
	/// FXML AND JAVAFX COMPONENTS
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
    private Label parkName;

    /**
	 * Constructor, initializes the park manager controller instance
	 */
    public ParkManagerReportScreenController() {
    	control = ReportsController.getInstance();
    }
    
    
    /**
	 * This method is called after an event of clicking on "Total number of visitors report" button
	 * is occurring
	 * 
	 * @param event
	 */
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
        	  if (!control.isReportDataAvailable(selectedMonth, selectedYear, this.park, "done")) {
               	showErrorAlert(ScreenManager.getInstance().getStage(), "No data available for the selected time period.");                
               	return;
        	   }
            Pair<Integer, Integer> visitorsData = control.generateTotalNumberOfVisitorsReport(selectedMonth, selectedYear, this.park);
            boolean saveSuccess = control.saveTotalNumberOfVisitorsReport(selectedMonth, selectedYear, this.park);
            System.out.println(saveSuccess); 
            ScreenManager.getInstance().showScreen("TotalNumberOfVisitorsReportController", "/clientSide/fxml/TotalNumberOfVisitorsReport.fxml",true, true, StageSettings.defaultSettings("Total number of visitors Report"), visitorsData);
        } catch (StatefulException | ScreenException e) {
            e.printStackTrace();
            showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred while generating the report.");
        }
        
    }
    /**
	 * This method is called after an event of clicking on "Usage report" button
	 * is occurring
	 * 
	 * @param event
	 */
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
        	if (!control.isReportDataAvailable(selectedMonth, selectedYear, this.park, "done")) {
               	showErrorAlert(ScreenManager.getInstance().getStage(), "No data available for the selected time period.");                
               	return;
        	   }
           	List<Pair<LocalDate, Integer>> usageData = control.generateUsageReport(selectedMonth, selectedYear, this.park);
           	boolean saveSuccess = control.saveUsageReport(selectedMonth, selectedYear, this.park);
            System.out.println(saveSuccess); 
           	ScreenManager.getInstance().showScreen("UsageReportController", "/clientSide/fxml/UsageReport.fxml", true, true, StageSettings.defaultSettings("Total number of visitors Report"), usageData);
        } catch (StatefulException | ScreenException e) {
           e.printStackTrace();
           showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred while generating the report.");
        }
    }

    /**
	 * This method is called after an event is created with clicking on the Back
	 * button. Returns the user to the previous screen
	 * 
	 * @param event
	 */
	@FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(false,true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
    }
    
	/**
	 * This method is called by the FXML and JAVAFX and initializes the screen
	 */
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
    /**
	 * This method is called in order to set pre-info into the GUI components
	 */
    @Override
	public void loadBefore(Object information) {
    	control = ReportsController.getInstance(); // Ensure control is initialized
    	// in case the user is logged in
    	if (information instanceof ParkManager) {
    		parkManager = (ParkManager)information;
    		park= parkManager.getParkObject();
    		parkName.getStyleClass().add("label-center");
        	parkName.setText("Hello "+parkManager.getManages()+" manager!");
    	}
    	
    }
    
    /**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Park Manger Reports";
		
	}
	/**
	 * This method is called if this screen needs to save its current state for
	 * later restoring
	 */
	@Override
	public void saveState() {
		control.saveParkManager(parkManager);
		
	}
	/**
	 * This method is called if this screen saved its past state, and now needs to
	 * restore it
	 */
	@Override
	public void restoreState() {
		parkManager = control.restoreParkManager();
		park = ParkController.getInstance().restorePark();
		parkName.getStyleClass().add("label-center");
    	parkName.setText("Hello "+parkManager.getManages()+" manager!");
	}
	
}
