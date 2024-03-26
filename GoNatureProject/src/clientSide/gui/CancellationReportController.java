package clientSide.gui;

import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Controls the Cancellation Report screen, displaying a chart with cancellation
 * data by reason and day, along with median values for cancellations and
 * no-shows.
 */
public class CancellationReportController extends AbstractScreen {
	private Map<String, List<XYChart.Data<String, Number>>> currentChartData;

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
	 * Populates a bar chart with cancellation data. This method expects a map with
	 * cancellation reasons as keys and lists of XYChart.Data as values.
	 *
	 * @param chartData A map containing the data to be displayed on the chart. The
	 *                  keys are expected to be strings representing the
	 *                  cancellation reasons, and the values are lists of
	 *                  XYChart.Data objects, each corresponding to a specific day
	 *                  and the number of cancellations for that reason.
	 */
	@SuppressWarnings("unchecked")
	public void populateChart(Map<String, List<XYChart.Data<String, Number>>> chartData) {
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

		List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
				"Saturday");
		daysOrder.forEach(day -> {
			Number countCancelled = aggregatedData.getOrDefault("Cancelled Orders", new HashMap<>()).getOrDefault(day,
					0);
			seriesCancelledOrders.getData().add(new XYChart.Data<>(day, countCancelled));

			Number countNoShow = aggregatedData.getOrDefault("No-Show Visitors", new HashMap<>()).getOrDefault(day, 0);
			seriesNoShowVisitors.getData().add(new XYChart.Data<>(day, countNoShow));
		});
		aggregatedData.forEach((seriesName, dayCounts) -> {
			if ("User Cancelled".equals(seriesName) || "Did not confirm".equals(seriesName)) {
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
				if ("User Cancelled".equals(reason) || "Did not confirm".equals(reason)) {
					cancelledOrdersValues.add(amount);
				} else if ("Did not arrive".equals(reason)) {
					noShowVisitorsValues.add(amount);
				}
			});
		});
		// Calculate and update medians for Cancelled Orders and No-Show Visitors
		double medianCancelled = calculateMedian(cancelledOrdersValues);
		double medianNoShow = calculateMedian(noShowVisitorsValues);
		medianLabelCancelled.setText(String.format("The median for cancelled booking: %.0f", medianCancelled));
		medianLabelNoShow.setText(String.format("The median for No-Show visitors: %.0f", medianNoShow));

		// Add the series to the bar chart
		cancellationBarChart.getData().addAll(seriesCancelledOrders, seriesNoShowVisitors);
		cancellationBarChart.setCategoryGap(10);
		cancellationBarChart.setBarGap(3);

	}

	/**
	 * This method is calculate the median
	 */
	private double calculateMedian(List<Number> values) {
		List<Double> nonZeroValues = values.stream().map(Number::doubleValue).filter(value -> value > 0).sorted()
				.collect(Collectors.toList());
		if (nonZeroValues.isEmpty()) {
			return 0; // Return 0 if there are no relevant data points
		}
		int middle = nonZeroValues.size() / 2;
		if (nonZeroValues.size() % 2 == 1) {
			return nonZeroValues.get(middle);
		} else {
			// Need to check if the list has at least 2 elements before calculating the
			// average of the middle two
			return (nonZeroValues.get(middle - 1) + nonZeroValues.get(middle)) / 2.0;
		}
	}

	/**
	 * Aggregates cancellation data by day, preparing it for chart display.
	 * 
	 * @param chartData The raw chart data with cancellation reasons and
	 *                  corresponding day-wise counts.
	 * @return A map with aggregated data ready for display on the bar chart.
	 */
	private Map<String, Map<String, Number>> aggregateDataByDay(
			Map<String, List<XYChart.Data<String, Number>>> chartData) {
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
	 * Populates the line chart with data. This method creates series for each
	 * cancellation reason and adds them to the line chart.
	 * 
	 * @param chartData The data to be displayed in the line chart.
	 */
	@SuppressWarnings("unchecked")
	private void populateLineChart(Map<String, List<XYChart.Data<String, Number>>> chartData) {
		cancellationLineChart.getData().clear(); // Clear previous data
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
			if ("User Cancelled".equals(seriesName) || "Did not confirm".equals(seriesName)) {
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
				if ("User Cancelled".equals(reason) || "Did not confirm".equals(reason)) {
					cancelledOrdersValues.add(amount);
				} else if ("Did not arrive".equals(reason)) {
					noShowVisitorsValues.add(amount);
				}
			});
		});
		// Calculate and update medians for Cancelled Orders and No-Show Visitors
		double medianCancelled = calculateMedian(cancelledOrdersValues);
		double medianNoShow = calculateMedian(noShowVisitorsValues);
		medianLabelCancelled.setText(String.format("The median for cancelled booking: %.0f", medianCancelled));
		medianLabelNoShow.setText(String.format("The median for No-Show visitors: %.0f", medianNoShow));

		// Add the series to the bar chart
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
		if (information instanceof Map) {
			currentChartData = (Map<String, List<XYChart.Data<String, Number>>>) information;
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