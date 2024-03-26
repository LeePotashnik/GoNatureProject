package clientSide.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Controller for the Visit Report screen. This class manages the user
 * interaction and data visualization for visitation patterns within a park,
 * displaying group and individual visitor data on a scatter chart.
 */
public class VisitReportController extends AbstractScreen {

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Label visitReport;
	@FXML
	private ScatterChart<Number, Number> visitScatterChart;
	@FXML
	private NumberAxis hoursAxis;
	@FXML
	private NumberAxis durationAxis;
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

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * Populates a scatter chart with data for group visits and individual visits.
	 * The method expects a map where keys are strings representing the type of
	 * visit ("Group" or "Individual Visitor") and values are lists of XYChart.Data
	 * objects, with each object representing a data point for the chart.
	 *
	 * @param chartData A map containing the data to be displayed on the chart. The
	 *                  keys should be "Group" and "Individual Visitor", and the
	 *                  values should be lists of XYChart.Data objects.
	 */
	@SuppressWarnings("unchecked")
	public void populateChart(Map<String, List<XYChart.Data<Number, Number>>> chartData) {
		List<XYChart.Data<Number, Number>> groupVisits = chartData.getOrDefault("Group", new ArrayList<>());
		List<XYChart.Data<Number, Number>> individualVisits = chartData.getOrDefault("Individual Visitor",
				new ArrayList<>());
		XYChart.Series<Number, Number> seriesGroup = new XYChart.Series<>();
		seriesGroup.setName("Group");
		seriesGroup.getData().addAll(groupVisits);

		// Series for Individual Visitor Visits
		XYChart.Series<Number, Number> seriesIndividual = new XYChart.Series<>();
		seriesIndividual.setName("Individual Visitor");
		seriesIndividual.getData().addAll(individualVisits);

		visitScatterChart.getData().clear(); // Clear any existing data first
		visitScatterChart.getData().addAll(seriesGroup, seriesIndividual);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
	 * This method is called by the FXML and JAVAFX and initializes the screen
	 */
	@Override
	public void initialize() {
		// initializing the image component of the logo and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title label
		visitReport.setAlignment(Pos.CENTER);
		visitReport.layoutXProperty().bind(pane.widthProperty().subtract(visitReport.widthProperty()).divide(2));

		hoursAxis.setLabel("Entrance Time");
		hoursAxis.setAutoRanging(false);
		hoursAxis.setLowerBound(7.5);// The park opens at 8 AM
		hoursAxis.setUpperBound(18.5);// The park closes at 10 PM
		hoursAxis.setTickUnit(0.5); // Corresponds to 30 minutes
		hoursAxis.setMinorTickCount(0);
		durationAxis.setLabel("Duration in Park (Hours)");
		// Set the custom formatter for the axis
		hoursAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(hoursAxis) {
			@Override
			public String toString(Number object) {
				int hour = object.intValue();
				int minute = (int) ((object.doubleValue() - hour) * 60);
				return String.format("%02d:%02d", hour, minute);
			}
		});
		durationAxis.setLabel("Duration in Park (Hours)");

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// setting the application's background
		setApplicationBackground(pane);
	}

	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@Override
	public void loadBefore(Object information) {
		if (information instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, List<XYChart.Data<Number, Number>>> chartData = (Map<String, List<XYChart.Data<Number, Number>>>) information;
			populateChart(chartData);
		} else {
			showErrorAlert("An error occurred. Occupancy data is not available.");
		}
	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Visits Report";
	}

}