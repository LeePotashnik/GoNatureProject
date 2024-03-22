package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.ParkEmployee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for the Park Employee Account Screen within the GoNature application. 
 * This controller manages
 * the interaction of park employees with their account interface.
 * It extends AbstractScreen to navigation within the application effectively.
 * 
 * The class integrates with the GoNatureUsersController and ParkController to perform its operations, managing
 * user session states and park-related data.
 */
public class ParkEmployeeAccountScreenController extends AbstractScreen{

	private GoNatureUsersController userControl;
	private ParkController parkControl;
	private ParkEmployee parkEmployee;

    @FXML
    private Label Title, privateName;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, parkEntryManagementBTN, parkEntryCasualBTN;

    /**
     * Initializes a new instance of the ParkEmployeeAccountScreenController class. 
     * It sets up the necessary controller instances for managing user and park operations. 
     */
    public ParkEmployeeAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
    	parkControl = ParkController.getInstance();
	}

	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}
	
    /**
     * @param event
     * When the 'Visitor Reservation' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryResevationScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryResevationScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryReservationScreenController",
				"/clientSide/fxml/ParkEntryReservationScreen.fxml", true, false, null);
    }
    
    /**
     * @param event
     * When the 'parkEntryManagementBTN' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryManagementScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryManagementScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryManagementScreenController",
				"/clientSide/fxml/ParkEntryManagementScreen.fxml", false, false,null);
    }

    /**
     * @param event
     * parkEmplyee clicked on 'Log out' button, an update query is executed to alter the value of the 
     * 'isLoggedIn' field in database. The user will return to main Screen.
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.logoutUser()) {
    		parkEmployee.setLoggedIn(false);
    		System.out.println("User logged out");
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
     * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
     * It prepares the screen for display by setting up user details, park information, and UI components.
     */
    @Override
    public void initialize() {
        // Restore the ParkEmployee user from the session state.
        parkEmployee = (ParkEmployee) userControl.restoreUser();
        
        // Save the park associated with the employee for session state.
        parkControl.savePark(parkEmployee.getWorkingIn());
        
        // Update the greeting label with the employee's name.
        this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
        this.privateName.underlineProperty(); // Underline the name for emphasis.
        
        // Set the park name in the title label to indicate the current park context.
        this.Title.setText(parkEmployee.getWorkingIn().getParkName());
        this.Title.underlineProperty(); // Underline the title for emphasis.

        // Load and set the logo for GoNature in the ImageView.
        goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
        
        // Set the style for the labels and buttons for consistent UI presentation.
        privateName.setStyle("-fx-alignment: center-right;");
        parkEntryManagementBTN.setStyle("-fx-alignment: center-right;");
        parkEntryCasualBTN.setStyle("-fx-alignment: center-right;");
        logOutBTN.setStyle("-fx-alignment: center-right;");
    }

	/**
	 * @param information Information passed from the previous screen, expected to be a ParkEmployee instance.
	 */
	@Override
	public void loadBefore(Object information) {
	}
	
	@Override
	public String getScreenTitle() {
		//return parkEmployee.getWorkingIn().getParkName();
		return null;
	}
}