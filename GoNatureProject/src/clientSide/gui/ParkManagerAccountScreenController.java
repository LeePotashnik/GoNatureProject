package clientSide.gui;

 import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Park;
import entities.ParkManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

/**
 * The ParkManagerAccountScreenController class controls the park manager account screen.
 * It handles all the user interactions within the park manager account screen.
 * This class extends AbstractScreen and implements the Stateful interface to manage screen state
 * and navigation within the application.
 * 
 * The class manages park and user data through the use of ParkController and GoNatureUsersController instances,
 * enabling it to perform operations related to park management functionalities.
 */

public class ParkManagerAccountScreenController extends AbstractScreen implements Stateful{
	
	private GoNatureUsersController userControl;	
	private ParkController parkControl;
	private ParkManager parkManager;


	@FXML
    private Label Title, privateName;

    @FXML
    private Button capacityBTN, logOutBTN, AdjustinDataBTN, reportsBTN;

    @FXML
    private ImageView goNatureLogo;
	
	public ParkManagerAccountScreenController() {
		userControl = GoNatureUsersController.getInstance();
		parkControl = ParkController.getInstance();
	}
	
	public ParkManager getParkManager() {
		return parkManager;
	}

	public void setParkManager(ParkManager parkManager) {
		this.parkManager = parkManager;
	}
	
    /**
     * @param event
     * When the 'AdjustinData' button is pressed, 
     * the park MANAGER will be redirected to the 'ParametersAdjustingScreen'
     */
    @FXML
    void GoToParametersAdjustingScreen(ActionEvent event) {
   	
    }

    /**
     * @param event
     * When the 'Current Capacity' button is pressed, 
     * the park manager will see a pop-up screen with the capacity according to park parameters at the DB
     */
    @FXML
    void GetCurrentMaximumCapacity(ActionEvent event) {
    	//updates park parameters
		String[] returnsVal = new String[4]; 
		returnsVal = parkControl.checkCurrentCapacity(parkManager.getWorkingIn().getParkName());
		if (returnsVal != null) {
			//sets the parameters
			parkManager.getParkObject().setMaximumVisitors(Integer.parseInt(returnsVal[0]));
			parkManager.getParkObject().setMaximumOrders(Integer.parseInt(returnsVal[1]));
			parkManager.getParkObject().setTimeLimit(Integer.parseInt(returnsVal[2])); 
			parkManager.getParkObject().setCurrentCapacity(Integer.parseInt(returnsVal[3])); 
		}
    	int actualCapacity = parkManager.getParkObject().getMaximumOrders() * parkManager.getParkObject().getMaximumVisitors() / 100;
    	showInformationAlert("The maximum visitors capacity: " + parkManager.getParkObject().getMaximumVisitors() + 
    			"\nThe maximum allowable quantity of visitors: " + actualCapacity + "\nThe current amount of visitors: " +
    			parkManager.getParkObject().getCurrentCapacity() + "\nThe time limit for each visit: " +
    			parkManager.getParkObject().getTimeLimit());
    }

    
    /**
     * @param event
     * When the 'Reports' button is pressed, 
     * the park MANAGER will be redirected to the 'ParkManagerReportScreen'
     */
    @FXML
    void GoToParkManagerReportsScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().showScreen("ParkManagerReportScreenController",
					"/clientSide/fxml/ParkManagerReportScreen.fxml", false, true, parkManager);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
    }


    /**
     * @param event
     * park manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.logoutUser()) {
        	parkManager.setLoggedIn(false);
    		System.out.println("Park Manager logged out");
    		try {
        		ScreenManager.getInstance().showScreen("MainScreenConrtroller", "/clientSide/fxml/MainScreen.fxml", true,
        				false, null);
        	} catch (ScreenException | StatefulException e) {
        				e.printStackTrace();
    		}
    	}
        else 
        	showErrorAlert("Failed to log out"); 	
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It sets up the initial state of
     * the UI elements, including styles and images.
     */
	@Override
	public void initialize() {
		/*
		parkManager = (ParkManager) userControl.restoreUser();
		parkManager.setParkObject(parkControl.fetchManagerParksList("parkManagerId", parkManager.getIdNumber()).get(0)); 
		parkControl.savePark(parkManager.getParkObject());
		this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkManager.getParkObject().getParkName() + "'s " + getScreenTitle());
	    this.Title.underlineProperty();
		 */
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		Title.setStyle("-fx-alignment: center-right;"); //label component
		capacityBTN.setStyle("-fx-alignment: center-right;");
		AdjustinDataBTN.setStyle("-fx-alignment: center-right;");
		reportsBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	/**
	 * Loads data before the screen is displayed. It sets the ParkManager
	 * instance and updates UI labels to reflect the current manager's information.
	 * This method is designed to be called when navigating from another screen.
	 *
	 * @param information The information passed from the previous screen, expected to be a ParkManager instance.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkManager PM = (ParkManager)information;
		setParkManager(PM);
		userControl.saveUser(parkManager);
		//set the relevant park to the parkManager from database
		parkManager.setParkObject(parkControl.fetchManagerParksList("parkManagerId", parkManager.getIdNumber()).get(0)); 
		parkControl.savePark(parkManager.getParkObject());
		this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkManager.getParkObject().getParkName() + "'s " + getScreenTitle());
	    this.Title.underlineProperty();
	}
	


	@Override
	public String getScreenTitle() {
		return "Park Manager";
	}

	/**
	 * Saves the current state of the park manager. This includes persisting
	 * any changes made during the session that need to be retained when navigating
	 * away from the screen.
	 */
	@Override
	public void saveState() {
		userControl.saveUser(parkManager);
	}

	/**
	 * Restores the state of the park manager and the screen. This method is called
	 * to repopulate the screen with data that was previously saved, ensuring
	 * continuity in the user's session.
	 */
	@Override
	public void restoreState() {
		this.parkManager = (ParkManager) userControl.restoreUser();
		this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkManager.getParkObject().getParkName() + "'s " + getScreenTitle());
	    this.Title.underlineProperty();
	}
}