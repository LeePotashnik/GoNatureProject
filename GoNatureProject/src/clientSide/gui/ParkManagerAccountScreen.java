package clientSide.gui;

import clientSide.control.GoNatureUsersController;
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

public class ParkManagerAccountScreen extends AbstractScreen{
	
	private GoNatureUsersController UC;
	
	private ParkManager parkManager;

	@FXML
    private Label Title, privateName;

    @FXML
    private Button capacityBTN, logOutBTN, AdjustinDataBTN, reportsBTN;

    @FXML
    private ImageView goNatureLogo;

	private String currCapacity;

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
     * the park manager will see a pop-up screen with the current capacity according to park parameters at the DB
     * 
     */
    @FXML
    void GetCurrentMaximumCapacity(ActionEvent event) {
    	if (UC != null) {
    	    this.currCapacity = UC.checkCurrentMaximumCapacity(getScreenTitle());
    	    showInformationAlert(ScreenManager.getInstance().getStage(), "The maximum visitors capacity is: " + currCapacity);
    	} else {
    		showErrorAlert(ScreenManager.getInstance().getStage(), "UC IS NULL");
    	}

    }

    /**
     * @param event
     * When the 'Reports' button is pressed, 
     * the park MANAGER will be redirected to the 'ParkManagerReportsScreen'
     */
    @FXML
    void GoToParkManagerReportsScreen(ActionEvent event) {

    }


    /**
     * @param event
     * park manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (UC != null) {
    		if (this.UC.checkLogOut("park_manager","parkManagerId",parkManager.getIdNumber()))
        		parkManager.setLoggedIn(false);
        	else 
        		showErrorAlert(ScreenManager.getInstance().getStage(), "Failed to log out");
    	}
    	else 
    		showErrorAlert(ScreenManager.getInstance().getStage(), "UC IS NULL");
    	try {
			ScreenManager.getInstance().goToPreviousScreen(false);
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
		UC = new GoNatureUsersController();
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
		this.privateName.setText("Hello " + parkManager.getFirstName() + " " + parkManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(getScreenTitle());
	    this.Title.underlineProperty();
	}
	
	public ParkManager getParkManager() {
		return parkManager;
	}

	public void setParkManager(ParkManager parkManager) {
		this.parkManager = parkManager;
	}

	@Override
	public String getScreenTitle() {
		return parkManager.getWorkingIn().getParkName();
	}
}