package clientSide.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
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
import javafx.util.Pair;

public class CancellationReportController extends AbstractScreen {
	/// FXML AND JAVAFX COMPONENTS
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
    @FXML
    public void initialize() {
    	goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
        //List<String> categories = Arrays.asList("Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
       // daysAxis.setCategories(FXCollections.observableArrayList(categories));
        //populateChart(); // Populate the chart with data
        
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
	public void populateChart(HashMap<Integer, Pair<Integer, Integer>> dailyData) 
	{
		// Assume you have a method to get your cancellation data that returns a Map<String, Integer>
	    // where the key is the day of the week and the value is the average amount of cancellations
	    //Pair<String, Integer> cancelledOrders = getCancellationData();
	    //Pair<String, Integer> noShowVisitors = getNoShowData();
	    
	    // Series for Cancelled Orders
	    XYChart.Series<String, Number> seriesCancelled = new XYChart.Series<>();
	    seriesCancelled.setName("Cancelled Orders");
	    
	    // Series for No-Show Visitors
	    XYChart.Series<String, Number> seriesNoShow = new XYChart.Series<>();
	    seriesNoShow.setName("No-Show Visitors");
	    
	    cancellationBarChart.getData().clear(); // Clear previous data
	    cancellationBarChart.setLegendVisible(true);
	    
	    // Iterate over the hashmap and add data to the series
	    for (Map.Entry<Integer, Pair<Integer, Integer>> entry : dailyData.entrySet()) {
	        Integer day = entry.getKey();
	        Pair<Integer, Integer> counts = entry.getValue();
	        Integer cancelledCount = counts.getKey();
	        Integer noShowCount = counts.getValue();

	        seriesCancelled.getData().add(new XYChart.Data<>(day.toString(), cancelledCount));
	        seriesNoShow.getData().add(new XYChart.Data<>(day.toString(), noShowCount));
	    }

	    // Add series to bar chart
	    cancellationBarChart.getData().addAll(seriesCancelled, seriesNoShow);

	    // Optional: Adjust the category axis to display days of the month
	    daysAxis.setLabel("Day of the Month");
	    List<String> daysOfMonth = IntStream.rangeClosed(1, 31)
	                                        .mapToObj(Integer::toString)
	                                        .collect(Collectors.toList());
	    daysAxis.setCategories(FXCollections.observableArrayList(daysOfMonth));
	}

//	    // Populate the series with data
//	    for (String day : daysAxis.getCategories()) {
//	        seriesCancelled.getData().add(new XYChart.Data<>(day, cancelledOrders.getOrDefault(day, 0)));
//	        seriesNoShow.getData().add(new XYChart.Data<>(day, noShowVisitors.getOrDefault(day, 0)));
//	    }
//		// Add series to bar chart
//	    cancellationBarChart.getData().addAll(seriesCancelled, seriesNoShow);
//	
//	}
	// Method that would be called when data is received from the server
	public void onDataReceived(HashMap<Integer, Pair<Integer, Integer>> dailyData) {
	    populateChart(dailyData); // Call populateChart with the received data
	}
//	private Pair<String, Integer> getCancellationData(int countres1,int countres2) {
//		BarChart.Data CancelledOrdersData = new BarChart.Data("Cancelled orders", countres1);
//		BarChart.Data NoShowVisitorsData = new BarChart.Data("No-Show Visitors", countres2);
//	    
//		cancellationBarChart.getData().clear(); // Clear previous data if any
//		cancellationBarChart.getData().addAll(CancelledOrdersData, NoShowVisitorsData);
//	    
//		cancellationBarChart.getData().forEach(data ->data.nameProperty().bind(javafx.beans.binding.Bindings.concat(data.getName(), " ", data.chartProperty())));
//	
//	}

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
		return "Cancellation report";
	}
}
