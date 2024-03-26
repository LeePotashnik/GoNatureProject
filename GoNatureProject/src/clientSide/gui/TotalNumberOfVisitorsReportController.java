package clientSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * Controller class for the Total Number Of Visitors Report screen. This class
 * handles the user interactions and data presentation for the total number of
 * visitors report. It populates a pie chart with the distribution of single
 * visitors versus visitors in groups based on the provided data.
 */
public class TotalNumberOfVisitorsReportController extends AbstractScreen {

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Label totalVisitorsReport;
	@FXML
	private PieChart totalVisitorsPaiChart;
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
	 * Populates the pie chart with data representing the distribution of single
	 * visitors and groups. This method takes the counts of individual visitors and
	 * groups, creates two segments in the pie chart for these categories, and
	 * updates the chart to reflect the current data.
	 *
	 * @param countIndividual The total number of individual visitors.
	 * @param countGroup      The total number of visitors in groups.
	 */
	private void populateChart(int countIndividual, int countGroup) {
		PieChart.Data singleVisitorsData = new PieChart.Data("Single visitors", countIndividual);
		PieChart.Data groupVisitorsData = new PieChart.Data("Groups", countGroup);

		totalVisitorsPaiChart.getData().clear(); // Clear previous data if any
		totalVisitorsPaiChart.getData().addAll(singleVisitorsData, groupVisitorsData);

		totalVisitorsPaiChart.getData().forEach(data -> data.nameProperty()
				.bind(javafx.beans.binding.Bindings.concat(data.getName(), " ", data.pieValueProperty())));
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
		totalVisitorsReport.setAlignment(Pos.CENTER);
		totalVisitorsReport.layoutXProperty()
				.bind(pane.widthProperty().subtract(totalVisitorsReport.widthProperty()).divide(2));

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
		if (information instanceof Pair) {
			@SuppressWarnings("unchecked")
			Pair<Integer, Integer> visitorsData = (Pair<Integer, Integer>) information;
			populateChart(visitorsData.getKey(), visitorsData.getValue());
		}
	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Total Number Of Visitors Report";
	}
}