package clientSide.gui;

import javafx.event.ActionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.controllers.AbstractScreen;
import entities.Park;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class DepartmentManagerReportsController extends AbstractScreen{
	
	private ArrayList<Park> parks;
	// JAVA FX COMPONENTS
    @FXML
    private ImageView goNatureLogo;
    @FXML
    private Button backButton;
    @FXML
    private Button visitReportBtn;
    @FXML
    private Button cancellationReportBtn;
    @FXML
    private ChoiceBox<String> choiceBoxMonth;
    @FXML
    private ChoiceBox<String> choiceBoxPark;
    
    @FXML
    void cancellationReport(ActionEvent event) {

    }

    @FXML
    void chooseMonth(ActionEvent event) {
    	String selectedMonth = (String) choiceBoxMonth.getValue();
        // Handle the selected month
        if(selectedMonth != null) {
            System.out.println("User selected: " + selectedMonth);
            
        }
    }
    @FXML
    void choosePark(ActionEvent event) {
    	String selectedPark = (String) choiceBoxPark.getValue();
        // Handle the selected month
        if(selectedPark != null) {
            System.out.println("User selected: " + selectedPark);
            
        }
    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {

    }

    @FXML
    void visitReport(ActionEvent event) {

    }
    ///// --- FXML / JAVA FX METHODS --- /////
	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
    public void initialize() {
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
	    List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June","July", "August", "September", "October", "November", "December");
	    // Add all months to the ChoiceBox
	    choiceBoxMonth.getItems().addAll(months);
	    
	    List<String> Eastern = Arrays.asList("Acadia", "Great Smoky Mountains", "Shenandoah");
	    List<String> Southern = Arrays.asList("Big Bend", "Congaree", "Everglades");
	    List<String> Central = Arrays.asList("Gateway Arch", "Hot Springs", "Mammoth Cave");
	    List<String> Northern = Arrays.asList("Glacier", "Theodore Roosevelt", "Voyageurs");
	    List<String> Western = Arrays.asList("Grand Canyon", "Yellowstone", "Yosemite");
	    List<String> Pacific = Arrays.asList("Hawaii Volcanoes", "Olympic");
	    // Add all months to the ChoiceBox
	    choiceBoxPark.getItems().addAll(Eastern);
    }

}
