package clientSide.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clientSide.control.ReportsController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.DepartmentManager;
import entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;


public class DepartmentManagerReportsScreenController extends AbstractScreen implements Stateful{
	
	private ReportsController control;//controller
	private DepartmentManager departmentManager;
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
    private Label DepartmentName;
    /**
   	 * Constructor, initializes the department manager controller instance
   	 */
       public DepartmentManagerReportsScreenController() {
       	control = ReportsController.getInstance();
       }
       
     /**
   	 * This method is called after an event of clicking on "Cancellation report" button
   	 * is occurring
   	 * 
   	 * @param event
   	 */   
    @FXML
    void cancellationReport(ActionEvent event) {
    	String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
        String selectedParkName= choiceBoxPark.getValue();
        // Validate that month,park and year are selected
        if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select month, year and park before generating the report.");
            return;
        }  
        try {
            // Find the Park object that matches the selected park name
            Park selectedPark = parks.stream()
                                     .filter(p -> p.getParkName().equals(selectedParkName))
                                     .findFirst()
                                     .orElseThrow(() -> new IllegalArgumentException("Selected park not found")); 
            // Check if data is available for the selected parameters
            if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "cancelled")) {
            	showErrorAlert(ScreenManager.getInstance().getStage(), "No data available for the selected time period.");                return;
            }
            Map<String, List<XYChart.Data<String, Number>>> cancelData= control.generateCancellationReport(selectedMonth, selectedYear,selectedPark);
    	    ScreenManager.getInstance().showScreen("CancellationReportController", "/clientSide/fxml/CancellationReport.fxml", true, true, StageSettings.defaultSettings("Cancellation Report"), cancelData);
    	} catch (StatefulException | ScreenException e) {
    		e.printStackTrace(); 
    		showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred while generating the report.");
    	}
   }

    /**
 	 * This method is called after an event of clicking on "Visit report" button
 	 * is occurring
 	 * 
 	 * @param event
 	 */
    @FXML
    void visitReport(ActionEvent event) {
        String selectedMonth = choiceBoxMonth.getValue();
        String selectedYear = choiceBoxYear.getValue();
        String selectedParkName= choiceBoxPark.getValue();
        // Validate that month, park, and year are selected
        if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
            showErrorAlert(ScreenManager.getInstance().getStage(), "Please select month, year, and park before generating the report.");
            return;
        }  
        try {
            if ("All Parks".equals(selectedParkName)) {
                // Assuming generateReportForAllParks is correctly implemented to handle report generation for all parks
            	 Map<String, Object> allParksData = control.generateReportForAllParks(selectedMonth, selectedYear, "done"); 
            } else {
                // Find the Park object that matches the selected park name
                Park selectedPark = parks.stream()
                                         .filter(p -> p.getParkName().equals(selectedParkName))
                                         .findFirst()
                                         .orElseThrow(() -> new IllegalArgumentException("Selected park not found"));
                // Check if data is available for the selected parameters
                if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "done")) {
                    showErrorAlert(ScreenManager.getInstance().getStage(), "No data available for the selected time period.");
                    return;
                }
                Map<String, List<XYChart.Data<Number, Number>>> visitData = control.generateVisitReport(selectedMonth, selectedYear, selectedPark);
                ScreenManager.getInstance().showScreen("VisitReportController", "/clientSide/fxml/VisitReport.fxml", true, true, StageSettings.defaultSettings("Visit Report"), visitData);
            }
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
	    // setting the back button image
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
		if (information instanceof DepartmentManager) {
	        departmentManager = (DepartmentManager)information;
	        parks = control.getDepartmentParks(departmentManager.getManagesDepartment());

	        // Clear existing items and add park names managed by this department manager
	        choiceBoxPark.getItems().clear();
	        choiceBoxPark.getItems().add("All Parks"); // Add option for all parks
	        for (Park park : parks) {
	            choiceBoxPark.getItems().add(park.getParkName());
	        }
	        DepartmentName.getStyleClass().add("label-center");
	        DepartmentName.setText("Hello "+departmentManager.getManagesDepartment()+" manager!");
	    }
	}
	/**
	 * This method is called if this screen needs to save its current state for
	 * later restoring
	 */
	@Override
	public void saveState() {
		control.saveDepartmentManager(departmentManager);
		
	}
	/**
	 * This method is called if this screen saved its past state, and now needs to
	 * restore it
	 */
	@Override
	public void restoreState() {
		departmentManager = control.restoreDepartmentManager();
		parks = control.getDepartmentParks(departmentManager.getManagesDepartment());
		choiceBoxPark.getItems().clear();
        for (Park park : parks) {
            choiceBoxPark.getItems().add(park.getParkName());
        }
		DepartmentName.getStyleClass().add("label-center");
	    DepartmentName.setText("Hello "+departmentManager.getManagesDepartment()+" manager!");
	}
	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Department manager reports";
	}

}
