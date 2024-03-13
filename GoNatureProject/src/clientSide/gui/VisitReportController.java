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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VisitReportController extends AbstractScreen {
	/// FXML AND JAVAFX COMPONENTS
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
    
    /**
   	 * This method is called after an event is created with clicking on the Back
   	 * button. Returns the user to the previous screen
   	 * 
   	 * @param event
   	 */
    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true,false);
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
		hoursAxis.setLabel("Entrance Time");
        durationAxis.setLabel("Duration in Park (Hours)");
        //populateChart(); // Populate the chart with data;
	    
	    ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
	 	backImage.setFitHeight(30);
	 	backImage.setFitWidth(30);
	 	backImage.setPreserveRatio(true);
	 	backButton.setGraphic(backImage);
	 	backButton.setPadding(new Insets(1, 1, 1, 1));
		
	}

	public void populateChart(Map<String, List<XYChart.Data<Number, Number>>> chartData) {
	    List<XYChart.Data<Number, Number>> groupVisits = chartData.getOrDefault("Group", new ArrayList<>());
	    List<XYChart.Data<Number, Number>> singleVisits = chartData.getOrDefault("Single", new ArrayList<>());

	    // Series for Group Visits
	    XYChart.Series<Number, Number> seriesGroup = new XYChart.Series<>();
	    seriesGroup.setName("Groups");
	    seriesGroup.getData().addAll(groupVisits);

	    // Series for Single Visitor Visits
	    XYChart.Series<Number, Number> seriesSingle = new XYChart.Series<>();
	    seriesSingle.setName("Single Visitor");
	    seriesSingle.getData().addAll(singleVisits);

	    VisitScatterChart.getData().clear(); // Clear any existing data first
	    VisitScatterChart.getData().addAll(seriesGroup, seriesSingle);
	}


	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}
	 /**
	  * This method returns the screen's name
	  */
	@Override
	public String getScreenTitle() {
		return "Visit report";
	}

}