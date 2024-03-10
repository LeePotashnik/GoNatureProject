package clientSide.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.DepartmentalReport;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CancellationReportController extends AbstractScreen implements Stateful{

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
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true);
    	  } catch (ScreenException | StatefulException e) {
    	        e.printStackTrace();
    	  }
	}

    @FXML
    public void initialize() {
    	goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
        List<String> categories = Arrays.asList("Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        daysAxis.setCategories(FXCollections.observableArrayList(categories));
        populateChart(); // Populate the chart with data
        
     // setting the back button image
     	 	ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
    	 	backImage.setFitHeight(30);
     	 	backImage.setFitWidth(30);
     	 	backImage.setPreserveRatio(true);
     	 	backButton.setGraphic(backImage);
     	 	backButton.setPadding(new Insets(1, 1, 1, 1));
    }
    protected static DepartmentalReport reportDetails;
    protected static boolean RecievedData = false;

	//This method receives data from the server about the cancellation report.
	public static void recieveDataFromServer(DepartmentalReport report) {
		reportDetails = report;
		RecievedData = true;
		return;
//		String selectQuery = "SELECT * FROM go_nature_prototype_db.order;";
//		sendRequestToServer(selectQuery);
	} 
	
//    @FXML
//    void generateReport(ActionEvent event) {
//        // Get the selected park name
//        String selectedPark = parkChoiceBox.getValue();
//
//        // Fetch cancellation data from the database for the selected park
//        List<CancellationData> data = fetchCancellationData(selectedPark);
//
//        // Clear previous data from the chart
//        cancellationChart.getData().clear();
//
//        // Generate and populate the graph with the fetched data
//        populateChart(cancellationChart, data);
//    }
//
//    private List<CancellationData> fetchCancellationData(String parkName) {
//        // Replace this with the actual database access code
//        return new ArrayList<>();
//    }
//
//    private void populateChart(BarChart<String, Number> chart, List<CancellationData> data) {
//        // Use the data to create a data series and add it to the chart
//    }
//}
	public void populateChart() 
	{
		// Assume you have a method to get your cancellation data that returns a Map<String, Integer>
	    // where the key is the day of the week and the value is the average amount of cancellations
	    Map<String, Integer> cancelledOrders = getCancellationData();
	    Map<String, Integer> noShowVisitors = getNoShowData();
	    
	    // Series for Cancelled Orders
	    XYChart.Series<String, Number> seriesCancelled = new XYChart.Series<>();
	    seriesCancelled.setName("Cancelled Orders");
	    
	    // Series for No-Show Visitors
	    XYChart.Series<String, Number> seriesNoShow = new XYChart.Series<>();
	    seriesNoShow.setName("No-Show Visitors");
	    
	    cancellationBarChart.setLegendVisible(true);
	    
	    // Populate the series with data
	    for (String day : daysAxis.getCategories()) {
	        seriesCancelled.getData().add(new XYChart.Data<>(day, cancelledOrders.getOrDefault(day, 0)));
	        seriesNoShow.getData().add(new XYChart.Data<>(day, noShowVisitors.getOrDefault(day, 0)));
	    }
		// Add series to bar chart
	    cancellationBarChart.getData().addAll(seriesCancelled, seriesNoShow);
	
	}
	private Map<String, Integer> getCancellationData() {
	    Map<String, Integer> data = new HashMap<>();
	    data.put("Sunday", 10);
	    data.put("Monday", 40);
	    data.put("Tuesday", 3);
	    data.put("Wednesday", 6);
	    data.put("Thursday", 9);
	    data.put("Friday", 1);
	    data.put("Saturday", 4);
	    return data;
	}

	private Map<String, Integer> getNoShowData() {
	    Map<String, Integer> data = new HashMap<>();
	    data.put("Sunday", 3);
	    data.put("Monday", 2);
	    data.put("Tuesday", 5);
	    data.put("Wednesday", 8);
	    data.put("Thursday", 6);
	    data.put("Friday", 9);
	    data.put("Saturday", 4);
	    return data;
	
	}

	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}
}
