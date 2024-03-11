package clientSide.gui;

import java.util.ArrayList;
import java.util.List;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VisitReportController extends AbstractScreen {

    @FXML
    private Button backButton;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Label VisitReport;
   
    @FXML
    private ScatterChart<Number, Number> VisitScatterChart;

    @FXML
    private NumberAxis hoursAxis;

    @FXML
    private NumberAxis durationAxis;

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
		hoursAxis.setLabel("Entrance Time");
        durationAxis.setLabel("Duration in Park (Hours)");
        populateChart(); // Populate the chart with data;
	    
	    ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
	 	backImage.setFitWidth(30);
	 	backImage.setPreserveRatio(true);
	 	backButton.setGraphic(backImage);
	 	backButton.setPadding(new Insets(1, 1, 1, 1));
		
	}

	private void populateChart() {
		List<XYChart.Data<Number, Number>> groupVisits = getGroupVisitData();
        List<XYChart.Data<Number, Number>> singleVisits = getSingleVisitData();

        // Series for Group Visits
	    XYChart.Series<Number, Number> seriesGroup = new XYChart.Series<>();
	    seriesGroup.setName("Groups");
	    seriesGroup.getData().addAll(groupVisits);

	    
	    // Series for Single Visitor Visits
	    XYChart.Series<Number, Number> seriesSingle = new XYChart.Series<>();
	    seriesSingle.setName("Single visitor");
	    seriesSingle.getData().addAll(singleVisits);
	    
	    VisitScatterChart.getData().addAll(seriesGroup, seriesSingle);
	
	}
    private List<XYChart.Data<Number, Number>> getGroupVisitData() {
        // This method should return actual data points
        List<XYChart.Data<Number, Number>> data = new ArrayList<>();
        data.add(new XYChart.Data<>(8.00, 2)); // 8:00 AM, 2-hour duration
        data.add(new XYChart.Data<>(9.35, 1.5));

        return data;
    }

    private List<XYChart.Data<Number, Number>> getSingleVisitData() {
        List<XYChart.Data<Number, Number>> data = new ArrayList<>();
        // Example: Entrance time, Duration
        data.add(new XYChart.Data<>(9.30, 1.5)); // 9:30 AM, 1.5-hour duration
        data.add(new XYChart.Data<>(10.30, 3.5));
        data.add(new XYChart.Data<>(17.00, 4));
        data.add(new XYChart.Data<>(9.30, 2.5));

        return data;
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