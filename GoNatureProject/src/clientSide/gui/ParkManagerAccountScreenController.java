package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.ParkManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The ParkManagerAccountScreenController class controls the park manager account screen.
 * It handles all the user interactions within the park manager account screen.
 * This class extends AbstractScreen.
 * 
 * The class manages park and user data through the use of ParkController and GoNatureUsersController instances,
 * enabling it to perform operations related to park management functionalities.
 */

public class ParkManagerAccountScreenController extends AbstractScreen{
	
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
    	try {
			ScreenManager.getInstance().showScreen("ParametersAdjustingScreenConrtroller",
					"/clientSide/fxml/ParametersAdjustingScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
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
					"/clientSide/fxml/ParkManagerReportScreen.fxml", false, false, null);
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
     * after the FXML file has been loaded. It performs several initial setup tasks.
     */
    @Override
    public void initialize() {
        // Restore the ParkManager user from the saved state to set up user context.
        parkManager = (ParkManager) userControl.restoreUser();

        // Fetch the park object associated with the manager and update the parkManager instance.
        parkManager.setParkObject(parkControl.fetchManagerParksList("parkManagerId", parkManager.getIdNumber()).get(0));
        System.out.println(parkManager.getParkObject() == null); // Debugging line to check if the park object is successfully retrieved.

        // Save the updated parkManager and its park object for later use within the session.
        userControl.saveUser(parkManager);
        parkControl.savePark(parkManager.getParkObject());

        // Update UI labels with personalized texts, such as greeting the manager by name.
        this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
        this.privateName.underlineProperty(); // Emphasize the name by underlining.
        this.Title.setText(parkManager.getParkObject().getParkName() + "'s " + getScreenTitle());
        this.Title.underlineProperty(); // Emphasize the title by underlining.

        // Set the GoNature logo.
        goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));

        // Apply styling to ensure consistent alignment and appearance of UI elements.
        privateName.setStyle("-fx-alignment: center-right;");
        Title.setStyle("-fx-alignment: center-right;");
        capacityBTN.setStyle("-fx-alignment: center-right;");
        AdjustinDataBTN.setStyle("-fx-alignment: center-right;");
        reportsBTN.setStyle("-fx-alignment: center-right;");
        logOutBTN.setStyle("-fx-alignment: center-right;");
    }


	/**
	 * @param information The information passed from the previous screen, expected to be a ParkManager instance.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return "Park Manager";
	}
}