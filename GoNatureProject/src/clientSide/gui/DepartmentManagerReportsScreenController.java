package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import clientSide.control.ReportsController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
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

/**
 * Controller for the Department Manager Reports Screen.
 * This class is responsible for handling user interactions with the screen for generating and viewing various reports related to park visitations.
 */

public class DepartmentManagerReportsScreenController extends AbstractScreen {
	
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
    private Button UsageReportBtn;
    @FXML
    private Button TotalNumberOfVisitorsBtn;
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
     * This method is called after an event of clicking on "Total number of visitors report" button
     * is occurring
     * 
     * @param event
     */ 
      @FXML
      void TotalNumberOfVisitorsReport(ActionEvent event) {
    	  String selectedMonth = choiceBoxMonth.getValue();
    	  String selectedYear = choiceBoxYear.getValue();
    	  String selectedParkName = choiceBoxPark.getValue();
    	  // Validate that month, year, and park are selected
    	  if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
    	     showErrorAlert("Please select a month, year, and park before generating the report.");
    	     return;
    	  }
    	  // Check if "All Parks" is selected
    	    if ("All Parks".equals(selectedParkName)) {
    	        showErrorAlert("This report can only be generated for spesific park.");
    	        return;
    	    }
    	  Park selectedPark = parks.stream()
		                           .filter(p -> p.getParkName().equals(selectedParkName))
		                           .findFirst()
		                           .orElseThrow(() -> new IllegalArgumentException("Selected park not found"));

		   // Check if report data is available for the selected park, month, and year.
		   if (!control.isParkManagerReportAvailable(selectedMonth, selectedYear, selectedPark, "total_visitors")) {
		    // If data is not available, show a message to the user.
			   showErrorAlert("This report is not available yet.");
			   return;
		   }
		   Pair<Integer, Integer> visitorsData = control.generateTotalNumberOfVisitorsReport(selectedMonth, selectedYear, selectedPark);
           try {
			ScreenManager.getInstance().showScreen("TotalNumberOfVisitorsReportController", "/clientSide/fxml/TotalNumberOfVisitorsReport.fxml",true, false, visitorsData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
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
    	  String selectedParkName = choiceBoxPark.getValue();
    	  // Validate that month, year, and park are selected
    	  if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
    	     showErrorAlert("Please select a month, year, and park before generating the report.");
    	     return;
    	  }
    	  // Check if "All Parks" is selected
  	      if ("All Parks".equals(selectedParkName)) {
  	        showErrorAlert("This report can only be generated for spesific park.");
  	        return;
  	    }
    	  Park selectedPark = parks.stream()
		                           .filter(p -> p.getParkName().equals(selectedParkName))
		                           .findFirst()
		                           .orElseThrow(() -> new IllegalArgumentException("Selected park not found"));

		   // Check if report data is available for the selected park, month, and year.
		   if (!control.isParkManagerReportAvailable(selectedMonth, selectedYear, selectedPark, "usage_report")) {
		    // If data is not available, show a message to the user.
			   showErrorAlert("This report is not available yet.");
			   return;
		   }
		   List<Pair<LocalDate, Integer>> usageData = control.generateUsageReport(selectedMonth, selectedYear, selectedPark);
           try {
			ScreenManager.getInstance().showScreen("UsageReportController", "/clientSide/fxml/UsageReport.fxml", true, false, usageData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
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
        String selectedParkName = choiceBoxPark.getValue();
        // Validate that month, year, and park are selected
        if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
            showErrorAlert("Please select month, year, and park before generating the report.");
            return;
        }  
        try {
            // Handle the "All Parks" option
            if ("All Parks".equals(selectedParkName)) {
                // Aggregate data for all parks
            	Map<String, List<XYChart.Data<String, Number>>> cancellationData = new HashMap<>();
                for (Park park : parks) {
                    if (control.isReportDataAvailable(selectedMonth, selectedYear, park, "cancelled")) {
                        Map<String, List<XYChart.Data<String, Number>>> parkCancellationData = control.generateCancellationReport(selectedMonth, selectedYear, park);
                        // Aggregate cancellation data from parkCancellationData into cancellationData
                        parkCancellationData.forEach((key, valueList) -> {
                            cancellationData.merge(key, valueList, (existingValues, newValues) -> {
                                // Aggregate numerical values by day
                                Map<String, Double> tempMap = new HashMap<>();
                                for (XYChart.Data<String, Number> val : existingValues) {
                                    tempMap.put(val.getXValue(), val.getYValue().doubleValue());
                                }
                                for (XYChart.Data<String, Number> newVal : newValues) {
                                    tempMap.merge(newVal.getXValue(), newVal.getYValue().doubleValue(), Double::sum);
                                }
                                // Convert back to List<XYChart.Data<String, Number>>
                                List<XYChart.Data<String, Number>> mergedList = new ArrayList<>();
                                tempMap.forEach((day, sum) -> mergedList.add(new XYChart.Data<>(day, sum)));
                                return mergedList;
                            });
                        });
                    }
                }
                // Show the aggregated report
                ScreenManager.getInstance().showScreen("CancellationReportController", "/clientSide/fxml/CancellationReport.fxml", true, false, cancellationData);
            } else {
            	// Find the Park object that matches the selected park name
                Park selectedPark = parks.stream()
                                         .filter(p -> p.getParkName().equals(selectedParkName))
                                         .findFirst()
                                         .orElseThrow(() -> new IllegalArgumentException("Selected park not found")); 
                // Check if data is available for the selected parameters
                if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "cancelled")) {
                	showErrorAlert("No data available for the selected time period.");                return;
                }
                Map<String, List<XYChart.Data<String, Number>>> cancelData= control.generateCancellationReport(selectedMonth, selectedYear,selectedPark);
        	    ScreenManager.getInstance().showScreen("CancellationReportController", "/clientSide/fxml/CancellationReport.fxml", true, false,  cancelData);
            }
        } catch (StatefulException | ScreenException e) {
            e.printStackTrace();
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
        String selectedParkName = choiceBoxPark.getValue();
        // Validate that month, year, and park are selected
        if (selectedMonth == null || selectedYear == null || selectedParkName == null) {
            showErrorAlert("Please select month, year, and park before generating the report.");
            return;
        }
        try {
            // Handle the "All Parks" option
            if ("All Parks".equals(selectedParkName)) {
                // Aggregate data for all parks
                Map<String, List<XYChart.Data<Number, Number>>> visitData = new HashMap<>();
                for (Park park : parks) {
                    if (control.isReportDataAvailable(selectedMonth, selectedYear, park, "done")) {
                        Map<String, List<XYChart.Data<Number, Number>>> parkVisitData = control.generateVisitReport(selectedMonth, selectedYear, park);
                        parkVisitData.forEach((key, value) -> visitData.merge(key, value, (v1, v2) -> {
                            List<XYChart.Data<Number, Number>> mergedList = new ArrayList<>(v1);
                            mergedList.addAll(v2);
                            return mergedList;
                        }));
                    }
                }
                ScreenManager.getInstance().showScreen("VisitReportController", "/clientSide/fxml/VisitReport.fxml", true, false,  visitData);
            } else {
                // Find the Park object that matches the selected park name
                Park selectedPark = parks.stream()
                                         .filter(p -> p.getParkName().equals(selectedParkName))
                                         .findFirst()
                                         .orElseThrow(() -> new IllegalArgumentException("Selected park not found"));
                // Check if data is available for the selected parameters
                if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "done")) {
                    showErrorAlert("No data available for the selected time period.");
                    return;
                }
                Map<String, List<XYChart.Data<Number, Number>>> visitData = control.generateVisitReport(selectedMonth, selectedYear, selectedPark);
                ScreenManager.getInstance().showScreen("VisitReportController", "/clientSide/fxml/VisitReport.fxml", true, true,  visitData);
            }
        } catch (StatefulException | ScreenException e) { 
            e.printStackTrace();
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
		departmentManager=(DepartmentManager) GoNatureUsersController.getInstance().restoreUser();
		GoNatureUsersController.getInstance().saveUser(departmentManager);
		parks=departmentManager.getResponsible();
	    // Clear existing items and add park names managed by this department manager
	    choiceBoxPark.getItems().clear();
	    choiceBoxPark.getItems().add("All Parks"); // Add option for all parks
	    for (Park park : parks) {
	        choiceBoxPark.getItems().add(park.getParkName());
	    }
	    DepartmentName.getStyleClass().add("label-center");
	    DepartmentName.setText("Hello "+departmentManager.getManagesDepartment()+" manager!");
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

	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Department manager reports";
	}

}
