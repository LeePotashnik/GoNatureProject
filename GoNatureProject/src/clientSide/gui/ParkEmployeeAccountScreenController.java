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
import entities.ParkEmployee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

public class ParkEmployeeAccountScreenController extends AbstractScreen implements Stateful {

	private String parkTable;
	
	private GoNatureUsersController userControl;
	private ParkController parkControl;
	private Park park;
	private ParkEmployee parkEmployee;

    @FXML
    private Label Title, privateName;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, parkEntryManagementBTN, parkEntryCasualBTN;

    public ParkEmployeeAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
    	parkControl = ParkController.getInstance();
	}
	public String getParkTable() {
		return parkTable;
	}

	public void setParkTable() {
		this.parkTable = parkControl.nameOfTable(park) + "_park_employees";
	}

	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}
	
	public Park getPark() {
		return park;
	}

	public void setPark(Park park) {
		this.park = park;
	}
	
    /**
     * @param event
     * When the 'Visitor Reservation' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryResevationScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryCasualScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryReservationScreenController",
				"/clientSide/fxml/ParkEntryReservationScreen.fxml", true, true,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee);
    }
    
    /**
     * @param event
     * When the 'parkEntryManagementBTN' button is pressed, 
     * the park employee will be redirected to the 'GoToParkEntryManagementScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryManagementScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryManagementScreenController",
				"/clientSide/fxml/ParkEntryManagementScreen.fxml", false, true,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee);
    }

    /**
     * @param event
     * parkEmplyee clicked on 'Log out' button, an update query is executed to alter the value of the 
     * 'isLoggedIn' field in database. The user will return to main Screen.
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.checkLogOut(parkTable,"employeeId",parkEmployee.getIdNumber())) {
    		parkEmployee.setLoggedIn(false);
    		System.out.println("User logged out");
    	}
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
		parkEntryManagementBTN.setStyle("-fx-alignment: center-right;");
		parkEntryCasualBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	
	/**
	 * The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class attributes, such as updating the user's ID (IdEmpNumber),
	 * the table name (from DB) of the park they work at and the screen title.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		setParkEmployee(PE);
		setPark(PE.getWorkingIn());	
		setParkTable();	
		String[] returnsVal = new String[4]; 
		returnsVal = parkControl.checkCurrentCapacity(park.getParkName());
		if (returnsVal != null) {
			//updates park parameters
			park.setMaximumVisitors(Integer.parseInt(returnsVal[0]));
			park.setMaximumOrders(Integer.parseInt(returnsVal[1]));
			park.setTimeLimit(Integer.parseInt(returnsVal[2])); 
			park.setCurrentCapacity(Integer.parseInt(returnsVal[3])); 
		}
		parkEmployee.setWorkingIn(park);
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(park.getParkName());
	    this.Title.underlineProperty();
	}
	
	/**
	 * Activated after the X is clicked on the window.
	 *  The default is to show a Confirmation Alert with "Yes" and "No" options for the user to choose. 
	 * "Yes" will check if the client is connected to the server, disconnect it from the server and the system.
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
	
	@Override
	public String getScreenTitle() {
		return null;
	}

	/**
	 *	This method saves the current user state and the park state.
	 */
	@Override
	public void saveState() {
		userControl.saveUser(parkEmployee);
		parkControl.savePark(park);
	}

	/**
	 * This method restores the previous user state and the park state.
	 */
	@Override
	public void restoreState() {
		parkEmployee = (ParkEmployee) userControl.restoreUser();
		this.park = parkControl.restorePark();
		setParkTable();	
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(park.getParkName());
	    this.Title.underlineProperty();
	}
}