package clientSide.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;


import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Pair;

public class UsageReportController extends AbstractScreen{
	
	// JAVA FX COMPONENTS
    
	@FXML
    private Button backButton;
    @FXML
    private ImageView goNatureLogo;
    @FXML
    private Label UsageReport;
    @FXML
    private LineChart<String, Number> UsageLineChart;
    @FXML
    private CategoryAxis datesAxis;
    @FXML
    private NumberAxis occupancyAxis;
    
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
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		// setting the back button image
 	 	ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
 	 	backImage.setFitWidth(30);
 	 	backImage.setPreserveRatio(true);
 	 	backButton.setGraphic(backImage);
 	 	backButton.setPadding(new Insets(1, 1, 1, 1));
		
	}
	/**
	 * Populates the chart with occupancy data, displaying the occupancy percentage
	 * on the Y-axis against dates on the X-axis.
	 * It then creates a series of data points from the provided occupancy data, where each
	 * data point represents the occupancy percentage for a given date.
	 *
	 * @param occupancyData A list of objects containing the date and the corresponding
	 *                      occupancy percentage. The occupancy
	 *                      percentage is expected to be within the range of 0 to 100.
	 */
	private void populateChart(List<Pair<LocalDate, Integer>> occupancyData) {
		 occupancyAxis.setAutoRanging(false); // Disable auto-ranging
		 occupancyAxis.setLowerBound(0); // Set the lower bound of Y-axis to 0
		 occupancyAxis.setUpperBound(100); // Set the upper bound of Y-axis to 100
		 occupancyAxis.setTickUnit(10); // Set the tick unit to 10 for better readability
		XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Occupancy(%)");
        // Format for displaying dates on the chart
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

        for (Pair<LocalDate, Integer> data : occupancyData) {
            LocalDate date = data.getKey();
            Integer occupancy = data.getValue();
            String formattedDate = date.format(formatter); // Convert LocalDate to String
            series.getData().add(new XYChart.Data<>(formattedDate, occupancy));
        }

        UsageLineChart.getData().clear(); // Clear previous data
        UsageLineChart.getData().add(series);

	}
	
	
	  /**
	   * This method is called in order to set pre-info into the GUI components
	   */
	@Override
	public void loadBefore(Object information) {
	    if (information instanceof List) {
	        List<Pair<LocalDate, Integer>> occupancyData = (List<Pair<LocalDate, Integer>>) information;
	        populateChart(occupancyData);
	    } else {
	        showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred. Occupancy data is not available.");
	    }
	}
	
	/**
	  * This method returns the screen's name
	  */
	@Override
	public String getScreenTitle() {
		return "Usage report";
	}

}
