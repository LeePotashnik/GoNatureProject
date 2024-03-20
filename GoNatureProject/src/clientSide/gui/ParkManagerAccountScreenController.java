package clientSide.gui;

 import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
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
		returnsVal = parkControl.checkCurrentCapacity(parkManager.getParkObject().getParkName());
		if (returnsVal != null) {
			//sets the parameters
			parkManager.getParkObject().setMaximumVisitors(Integer.parseInt(returnsVal[0]));
			parkManager.getParkObject().setMaximumOrders(Integer.parseInt(returnsVal[1]));
			parkManager.getParkObject().setTimeLimit(Integer.parseInt(returnsVal[2])); 
			parkManager.getParkObject().setCurrentCapacity(Integer.parseInt(returnsVal[3])); 
		}
    	int actualCapacity = parkManager.getParkObject().getMaximumOrders() * parkManager.getParkObject().getMaximumVisitors() / 100;
    	showInformationAlert(ScreenManager.getInstance().getStage(), "The maximum visitors capacity: " +
    	parkManager.getParkObject().getMaximumVisitors() + "\nThe maximum allowable quantity of visitors: " + actualCapacity +
    	"\nThe current amount of visitors: " + parkManager.getParkObject().getCurrentCapacity() + "\nThe time limit for each visit: " +
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
					"/clientSide/fxml/ParkManagerReportScreen.fxml", false, true,
					StageSettings.defaultSettings("GoNature System - Client Connection"), parkManager);
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
    	if (userControl.checkLogOut("park_manager","parkManagerId",parkManager.getIdNumber()))
        	parkManager.setLoggedIn(false);
        else 
        	showErrorAlert(ScreenManager.getInstance().getStage(), "Failed to log out");
    	try {
    		ScreenManager.getInstance().showScreen("MainScreenConrtroller", "/clientSide/fxml/MainScreen.fxml", true,
    				false, StageSettings.defaultSettings("GoNature System - Reservations"), null);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
    }

	/**
	 * This method is called after the FXML is invoked
	 */
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		Title.setStyle("-fx-alignment: center-right;"); //label component
		capacityBTN.setStyle("-fx-alignment: center-right;");
		AdjustinDataBTN.setStyle("-fx-alignment: center-right;");
		reportsBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	/**
	 * The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class instance- parkManager.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkManager PM = (ParkManager)information;
		setParkManager(PM);
		//set the relevant park to the parkManager from database
		parkManager.setParkObject(parkControl.fetchManagerParksList("parkManagerId", parkManager.getIdNumber()).get(0)); 
		//parkManager.setParkObject(park);
		this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkManager.getParkObject().getParkName() + "'s " + getScreenTitle());
	    this.Title.underlineProperty();
	}
	
	/**
	 * Activated after the X is clicked on the window.
	 *  The default isto show a Confirmation Alert with "Yes" and "No" options for the user to choose. 
	 * "Yes" will check if the client is connected to the server, disconnected from the server and the system.
	 */
	@Override
	public void handleCloseRequest(WindowEvent event) {
		int decision = showConfirmationAlert(ScreenManager.getInstance().getStage(), "Are you sure you want to leave?",
				Arrays.asList("Yes", "No"));
		if (decision == 2) // if the user clicked on "No"
			event.consume();
		else { // if the user clicked on "Yes" and he is connected to server
			logOut(null); //log out from go nature system
    		System.out.println("User logged out");
			userControl.disconnectClientFromServer(); 

		}
	}
	
	public ParkManager getParkManager() {
		return parkManager;
	}

	public void setParkManager(ParkManager parkManager) {
		this.parkManager = parkManager;
	}

	@Override
	public String getScreenTitle() {
		return "Park Manager";
	}

	@Override
	public void saveState() {
		userControl.saveUser(parkManager);
	}

	/**
	 * Updating all the screen details that were present before the user pressed
	 * the button and moved to another screen.
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