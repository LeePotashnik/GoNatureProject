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
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * Controls the Cancellation Report screen, displaying a chart with cancellation
 * data by reason and day, along with median values for cancellations and
 * no-shows.
 */
public class CancellationReportController extends AbstractScreen {
	private Pair<Map<String, List<XYChart.Data<String, Number>>>, Pair<Integer, Integer>> currentChartData;

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Button backButton, lineChartBtn;
	@FXML
	private BarChart<String, Number> cancellationBarChart;
	@FXML
	private LineChart<String, Number> cancellationLineChart;
	@FXML
	private CategoryAxis daysAxis;
	@FXML
	private NumberAxis amountAxis;
	@FXML
	private Label medianLabelCancelled, medianLabelNoShow, titleLbl;
	@FXML
	private Pane pane;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	/**
	 * This method is called after an event is created with clicking on the Back
	 * button. Returns the user to the previous screen
	 * 
	 * @param event
	 */
	@FXML
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(true, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the action of toggling between the bar chart and the line chart. This
	 * method updates the visibility of the charts and the text of the toggle button
	 * based on the current state.
	 * 
	 * @param event The event that triggered this action.
	 */
	@FXML
	void toggleChartView(ActionEvent event) {
		if (cancellationBarChart.isVisible()) {
			// If the bar chart is visible, hide it and show the line chart
			cancellationBarChart.setVisible(false);
			cancellationLineChart.setVisible(true);
			populateLineChart(currentChartData); // Ensure the line chart is populated
			lineChartBtn.setText("Bar Chart"); // Update the button text
		} else {
			// If the line chart is visible, hide it and show the bar chart
			cancellationLineChart.setVisible(false);
			cancellationBarChart.setVisible(true);
			populateChart(currentChartData); // Ensure the bar chart is populated
			lineChartBtn.setText("Line Chart"); // Update the button text
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * Populates a bar chart with aggregated cancellation data by day of the week. This method organizes
	 * data into two main series based on cancellation reasons: cancelled orders and no-show visitors.
	 * It then calculates the total counts for each reason across all days and adds these counts to the
	 * chart. 
	 * The method expects a Pair containing a map for chart data and another Pair for median values:
	 * - The map's keys represent the cancellation reasons, and its values are lists of XYChart.Data objects,
	 *   each corresponding to a specific day of the week and the number of cancellations for that reason.
	 * - The second element of the Pair contains the median values for cancelled bookings and no-show visitors,
	 *   respectively, which are used to update the corresponding UI labels.
	 * 
	 * @param pair A Pair where the key is a map with cancellation reasons as keys and lists of 
	 *             XYChart.Data objects as values, and the value is a Pair containing median values for
	 *             cancelled bookings and no-show visitors, respectively.
	 */
	@SuppressWarnings("unchecked")
	public void populateChart(Pair<Map<String, List<XYChart.Data<String, Number>>>, Pair<Integer, Integer>> pair) {
		cancellationBarChart.getData().clear(); // Clear previous data
		 Map<String, List<XYChart.Data<String, Number>>> dataMap = pair.getKey();
		 Pair<Integer, Integer> medians = pair.getValue();

		// Initializes two series for the chart, one for each cancellation reason
		XYChart.Series<String, Number> seriesCancelledOrders = new XYChart.Series<>();
		seriesCancelledOrders.setName("Cancelled Orders");

		XYChart.Series<String, Number> seriesNoShowVisitors = new XYChart.Series<>();
		seriesNoShowVisitors.setName("No-Show Visitors");


		List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
				"Saturday");
		daysOrder.forEach(day -> {
			 double countCancelled = 0;
		     double countNoShow = 0;
		        
		        // Calculate the count for "Cancelled Orders" if present
		        if(dataMap.containsKey("User Cancelled")) {
		            countCancelled = dataMap.get("User Cancelled").stream()
		                                    .filter(data -> data.getXValue().equals(day))
		                                    .mapToDouble(data -> data.getYValue().doubleValue())
		                                    .sum();
		        }
		        
		        // Calculate the count for "No-Show Visitors" if present
		        if(dataMap.containsKey("Did not arrive")) {
		            countNoShow = dataMap.get("Did not arrive").stream()
		                                 .filter(data -> data.getXValue().equals(day))
		                                 .mapToDouble(data -> data.getYValue().doubleValue())
		                                 .sum();
		        }
		        if(dataMap.containsKey("Did not confirm")) {
		            countCancelled = dataMap.get("Did not confirm").stream()
		                                    .filter(data -> data.getXValue().equals(day))
		                                    .mapToDouble(data -> data.getYValue().doubleValue())
		                                    .sum();
		        }
		        // Add data to series
		        seriesCancelledOrders.getData().add(new XYChart.Data<>(day, countCancelled));
		        seriesNoShowVisitors.getData().add(new XYChart.Data<>(day, countNoShow));
		    });

		medianLabelCancelled.setText(String.format("The median for cancelled booking: %d", medians.getKey()));
		medianLabelNoShow.setText(String.format("The median for No-Show visitors: %d", medians.getValue()));

		// Add the series to the bar chart
		cancellationBarChart.getData().addAll(seriesCancelledOrders, seriesNoShowVisitors);
		cancellationBarChart.setCategoryGap(10);
		cancellationBarChart.setBarGap(3);

	}

	/**
	 * Populates the line chart with cancellation data organized by day of the week. This method
	 * creates two series: one for cancelled orders and another for no-show visitors, based on the
	 * aggregated data provided. It then calculates the total counts for each cancellation reason
	 * across the week and adds these data points to their respective series in the line chart.
	 * 
	 * @param chartData A Pair object containing two elements:
	 *                  - The first element is a map where keys are cancellation reasons 
	 *                    and values are lists of XYChart.Data objects. Each XYChart.Data object represents
	 *                    a day of the week and the count of occurrences.
	 *                  - The second element is a Pair of Integers, representing the median values for
	 *                    cancelled bookings and no-show visitors, respectively.
	 */
	@SuppressWarnings("unchecked")
	private void populateLineChart(Pair<Map<String, List<Data<String, Number>>>, Pair<Integer, Integer>> chartData) {
		cancellationLineChart.getData().clear(); // Clear previous data
		 Map<String, List<XYChart.Data<String, Number>>> dataMap = chartData.getKey();
		 Pair<Integer, Integer> medians = chartData.getValue();
		
		// Initializes two series for the chart, one for each cancellation reason
		XYChart.Series<String, Number> seriesCancelledOrders = new XYChart.Series<>();
		seriesCancelledOrders.setName("Cancelled Orders");

		XYChart.Series<String, Number> seriesNoShowVisitors = new XYChart.Series<>();
		seriesNoShowVisitors.setName("No-Show Visitors");


		List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
				"Saturday");
		daysOrder.forEach(day -> {
			 double countCancelled = 0;
		     double countNoShow = 0;
		        

		        // Calculate the count for "Cancelled Orders" if present
		        if(dataMap.containsKey("User Cancelled")) {
		            countCancelled = dataMap.get("User Cancelled").stream()
		                                    .filter(data -> data.getXValue().equals(day))
		                                    .mapToDouble(data -> data.getYValue().doubleValue())
		                                    .sum();
		        }
		        
		        // Calculate the count for "No-Show Visitors" if present
		        if(dataMap.containsKey("Did not arrive")) {
		            countNoShow = dataMap.get("Did not arrive").stream()
		                                 .filter(data -> data.getXValue().equals(day))
		                                 .mapToDouble(data -> data.getYValue().doubleValue())
		                                 .sum();
		        }
		        // Calculate the count for "Did not confirm" if present
		        if(dataMap.containsKey("Did not confirm")) {
		            countCancelled = dataMap.get("Did not confirm").stream()
		                                    .filter(data -> data.getXValue().equals(day))
		                                    .mapToDouble(data -> data.getYValue().doubleValue())
		                                    .sum();
		        }
		        // Add data to series
		        seriesCancelledOrders.getData().add(new XYChart.Data<>(day, countCancelled));
		        seriesNoShowVisitors.getData().add(new XYChart.Data<>(day, countNoShow));
		    });

		medianLabelCancelled.setText(String.format("The median for cancelled booking: %d", medians.getKey()));
		medianLabelNoShow.setText(String.format("The median for No-Show visitors: %d", medians.getValue()));

		// Add the series to the Line chart
		cancellationLineChart.getData().addAll(seriesCancelledOrders, seriesNoShowVisitors);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
	 * This method is called by the FXML and JAVAFX and initializes the screen
	 */
	@FXML
	public void initialize() {
		// initializing the image component of the logo and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
				"Saturday");
		daysAxis.setCategories(FXCollections.observableArrayList(daysOrder));

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// Initially hide the line chart
		cancellationLineChart.setVisible(false);
		// Configure Y-axis (NumberAxis) range from 0 to 100
		amountAxis.setAutoRanging(false);
		amountAxis.setLowerBound(0); // Set the lower bound to 0
		amountAxis.setUpperBound(100); // Set the upper bound to 100
		amountAxis.setTickUnit(10);
		cancellationBarChart.setVisible(true); // Ensure the bar chart is initially visible
		cancellationLineChart.setVisible(false); // Ensure the line chart is initially hidden
		lineChartBtn.setText("Line Chart"); // Set the initial button text

		// setting the application's background
		setApplicationBackground(pane);
	}

	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void loadBefore(Object information) {
		if (information instanceof Pair) {
			currentChartData = (Pair<Map<String, List<XYChart.Data<String, Number>>>, Pair<Integer, Integer>>) information;	
			populateChart(currentChartData); // Populate your initial chart as needed

		} else {
			showErrorAlert("An error occurred. Cancellation data is not available.");
		}
	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Cancellations Report";
	}
}