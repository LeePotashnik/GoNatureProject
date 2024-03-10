package clientSide.gui;

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
		populateChart();
		// setting the back button image
 	 	ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
 	 	backImage.setFitWidth(30);
 	 	backImage.setPreserveRatio(true);
 	 	backButton.setGraphic(backImage);
 	 	backButton.setPadding(new Insets(1, 1, 1, 1));
		
	}

	private void populateChart() {
		XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Occupancy(%)");

        series.getData().add(new XYChart.Data<>("01", 50)); // "01" for 1st of the month, 50% occupancy
        series.getData().add(new XYChart.Data<>("02", 75)); // "02" for 2nd of the month, 75% occupancy
        series.getData().add(new XYChart.Data<>("05", 100));
        series.getData().add(new XYChart.Data<>("08", 20));
        UsageLineChart.getData().add(series);
		
	}

	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
