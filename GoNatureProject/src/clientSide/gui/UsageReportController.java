package clientSide.gui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

public class UsageReportController extends AbstractScreen{

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
    
    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
    }

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

	private void populateChart(List<Pair<LocalDate, Integer>> occupancyData) {
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

	@Override
	public void loadBefore(Object information) {
	    if (information instanceof List) {
	        List<Pair<LocalDate, Integer>> occupancyData = (List<Pair<LocalDate, Integer>>) information;
	        populateChart(occupancyData);
	    } else {
	        showErrorAlert(ScreenManager.getInstance().getStage(), "An error occurred. Occupancy data is not available.");
	    }
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
