package clientSide.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import javafx.scene.control.Label;
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
    @FXML
    private Label medianLabelCancelled;
    @FXML
    private Label medianLabelNoShow;
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
		cancellationBarChart.getData().clear(); // Clear previous data
		Map<String, Map<String, Number>> aggregatedData = aggregateDataByDay(chartData);
		// Initializes two series for the chart, one for each cancellation reason
		XYChart.Series<String, Number> seriesCancelledOrders = new XYChart.Series<>();
	    seriesCancelledOrders.setName("Cancelled Orders");

	    XYChart.Series<String, Number> seriesNoShowVisitors = new XYChart.Series<>();
	    seriesNoShowVisitors.setName("No-Show Visitors");

	 // Prepare to calculate medians
	    List<Number> cancelledOrdersValues = new ArrayList<>();
	    List<Number> noShowVisitorsValues = new ArrayList<>();
	    aggregatedData.forEach((seriesName, dayCounts) -> {
        if ("Client cancelled the reminder".equals(seriesName)) {
            dayCounts.forEach((day, count) -> {
                seriesCancelledOrders.getData().add(new XYChart.Data<>(day, count));
            });
        } else if ("Did not arrive".equals(seriesName)) {
            dayCounts.forEach((day, count) -> {
                seriesNoShowVisitors.getData().add(new XYChart.Data<>(day, count));
               
            });
        }
    });
	 // Aggregate data for the median calculation
	    chartData.forEach((reason, dataList) -> {
	        dataList.forEach(data -> {
	            // Assuming data.getYValue() returns the cancellation amount
	            Number amount = data.getYValue();
	            if ("Client cancelled the reminder".equals(reason)) {
	                cancelledOrdersValues.add(amount);
	            } else if ("Did not arrive".equals(reason)) {
	                noShowVisitorsValues.add(amount);
	            }
	        });
	    });
	 // Calculate and update medians for Cancelled Orders and No-Show Visitors
	    double medianCancelled = calculateMedian(cancelledOrdersValues);
	    double medianNoShow = calculateMedian(noShowVisitorsValues);
//	    System.out.println("Cancelled Orders Values: " + cancelledOrdersValues);
//	    System.out.println("No-Show Visitors Values: " + noShowVisitorsValues);
//	    System.out.println("Cancelled Orders Values: " + medianCancelled);
//	    System.out.println("Cancelled Orders Values: " + medianNoShow);
	 // Set the median labels
	    medianLabelCancelled.setText(String.format("The median for cancelled booking: %.0f", medianCancelled));
	    medianLabelNoShow.setText(String.format("The median for No-Show visitors: %.0f", medianNoShow));

	    // Add the series to the bar chart
	    cancellationBarChart.getData().addAll(seriesCancelledOrders, seriesNoShowVisitors);
	    cancellationBarChart.setCategoryGap(10);
	    cancellationBarChart.setBarGap(3);


	}
	private double calculateMedian(List<Number> values) {
	    List<Double> nonZeroValues = values.stream()
	                                       .map(Number::doubleValue)
	                                       .filter(value -> value > 0)
	                                       .sorted()
	                                       .collect(Collectors.toList());

	    if (nonZeroValues.isEmpty()) {
	        return 0; // Return 0 if there are no relevant data points
	    }

	    int middle = nonZeroValues.size() / 2;
	    if (nonZeroValues.size() % 2 == 1) {
	        return nonZeroValues.get(middle);
	    } else {
	        // Need to check if the list has at least 2 elements before calculating the average of the middle two
	        return (nonZeroValues.get(middle - 1) + nonZeroValues.get(middle)) / 2.0;
	    }
	}
	  
	private Map<String, Map<String, Number>> aggregateDataByDay(Map<String, List<XYChart.Data<String, Number>>> chartData) {
	    Map<String, Map<String, Number>> aggregatedData = new HashMap<>();

	    chartData.forEach((seriesName, dataList) -> {
	        Map<String, Number> dayCounts = new HashMap<>();
	        dataList.forEach(data -> {
	            String day = data.getXValue();
	            Number count = data.getYValue();
	            dayCounts.merge(day, count, (oldValue, newValue) -> oldValue.doubleValue() + newValue.doubleValue());
	        });
	        aggregatedData.put(seriesName, dayCounts);
	    });

	    return aggregatedData;
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
