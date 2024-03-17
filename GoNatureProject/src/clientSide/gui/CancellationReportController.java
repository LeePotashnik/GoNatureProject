package clientSide.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CancellationReportController extends AbstractScreen {
	/// FXML AND JAVAFX COMPONENTS
    @FXML
    private ImageView goNatureLogo;
    @FXML
    private Button backButton;
    @FXML
    private BarChart<String, Number> cancellationBarChart;
    @FXML
    private CategoryAxis daysAxis;
    @FXML
    private NumberAxis amountAxis;

    /**
   	 * This method is called after an event is created with clicking on the Back
   	 * button. Returns the user to the previous screen
   	 * 
   	 * @param event
   	 */
    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true,true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
	}
    /**
 	 * This method is called by the FXML and JAVAFX and initializes the screen
 	 */
    @FXML
    public void initialize() {
    	goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
        List<String> categories = Arrays.asList("Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        daysAxis.setCategories(FXCollections.observableArrayList(categories));
        
        // setting the back button image
     	 ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
    	 backImage.setFitHeight(30);
     	 backImage.setFitWidth(30);
     	 backImage.setPreserveRatio(true);
     	 backButton.setGraphic(backImage);
     	 backButton.setPadding(new Insets(1, 1, 1, 1));
    }

    /**
     * Populates a bar chart with cancellation data. This method expects a map with cancellation reasons
     * as keys and lists of XYChart.Data as values.
     *
     * @param chartData A map containing the data to be displayed on the chart. The keys are expected to be
     *                  strings representing the cancellation reasons, and the values are lists of XYChart.Data
     *                  objects, each corresponding to a specific day and the number of cancellations for that reason.
     */
	public void populateChart(Map<String, List<XYChart.Data<String, Number>>> chartData) 
	{
		// Initializes two series for the chart, one for each cancellation reason
		XYChart.Series<String, Number> seriesCancelledOrders = new XYChart.Series<>();
	    seriesCancelledOrders.setName("Cancelled Orders");

	    XYChart.Series<String, Number> seriesNoShowVisitors = new XYChart.Series<>();
	    seriesNoShowVisitors.setName("No-Show Visitors");

	    cancellationBarChart.getData().clear(); // Clear previous data
	    // Iterates over the entries in the chartData map. For each entry (cancellation reason),
	    // it adds the corresponding data to the relevant series.
	    chartData.forEach((seriesName, dataList) -> {
	        if ("Client cancelled the reminder".equals(seriesName)) {
	            seriesCancelledOrders.getData().addAll(dataList);
	        } else if ("Did not arrive".equals(seriesName)) {
	            seriesNoShowVisitors.getData().addAll(dataList);
	        }
	    });

	    // Add the series to the bar chart
	    cancellationBarChart.getData().addAll(seriesCancelledOrders, seriesNoShowVisitors);
	    cancellationBarChart.setCategoryGap(10);
	    cancellationBarChart.setBarGap(3);

	}



	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@Override
	public void loadBefore(Object information) {
	    if (information instanceof Map) {
	    	Map<String, List<XYChart.Data<String, Number>>> cancellationData = (Map<String, List<XYChart.Data<String, Number>>>) information;
	        populateChart(cancellationData);
	    }
	 else {
        showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred. Cancellation data is not available.");
	 }
	}
		
	
	 /**
	  * This method returns the screen's name
	  */
	@Override
	public String getScreenTitle() {
		return "Cancellation report";
	}
}
