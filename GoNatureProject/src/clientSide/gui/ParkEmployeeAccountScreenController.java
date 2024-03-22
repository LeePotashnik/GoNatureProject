package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
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
 * It extends AbstractScreen and implements the Stateful interface to handle the screen state 
 * and navigation within the application effectively.
 * 
 * The class integrates with the GoNatureUsersController and ParkController to perform its operations, managing
 * user session states and park-related data.
 */
public class ParkEmployeeAccountScreenController extends AbstractScreen implements Stateful {

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
				"/clientSide/fxml/ParkEntryReservationScreen.fxml", true, true, null);
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
				"/clientSide/fxml/ParkEntryManagementScreen.fxml", false, true,null);
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
	 * This method is called after the FXML is invoked
	 */
	@Override
	public void initialize() {
		/*
		 * 
		parkEmployee = (ParkEmployee) userControl.restoreUser();
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkEmployee.getWorkingIn().getParkName());
	    this.Title.underlineProperty();
		 */
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		parkEntryManagementBTN.setStyle("-fx-alignment: center-right;");
		parkEntryCasualBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	/**
	 * Prepares the screen before it is displayed by setting up the necessary data based on
	 * information passed from the previous screen. This includes initializing the park employee,
	 * setting the park they are associated with and updating the UI components to reflect their
	 * information. It also fetches and displays current park capacity.
	 * 
	 * @param information Information passed from the previous screen, expected to be a ParkEmployee instance.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		
		setParkEmployee(PE);
		userControl.saveUser(parkEmployee);
		parkControl.savePark(parkEmployee.getWorkingIn());
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkEmployee.getWorkingIn().getParkName());
	    this.Title.underlineProperty();
	}
	
	@Override
	public String getScreenTitle() {
		//return parkEmployee.getWorkingIn().getParkName();
		return null;
	}

	/**
	 *	This method saves the current user state and the park state.
	 */
	@Override
	public void saveState() {
		userControl.saveUser(parkEmployee);
	}

	/**
	 * Restores the screen's state, including the park employee and park details, based on
	 * the saved state. This method ensures continuity in the application's user experience by
	 * repopulating UI components with previously saved data.
	 */
	@Override
	public void restoreState() {
		parkEmployee = (ParkEmployee) userControl.restoreUser();
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(parkEmployee.getWorkingIn().getParkName());
	    this.Title.underlineProperty();
	}
}