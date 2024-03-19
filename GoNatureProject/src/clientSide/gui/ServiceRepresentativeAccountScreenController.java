package clientSide.gui;

import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.ParkEmployee;
import entities.Representative;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

public class ServiceRepresentativeAccountScreenController extends AbstractScreen implements Stateful{

	private GoNatureUsersController userControl;

	private Representative representative;
    @FXML
    private Button GuideRegistrationBTN, logOutBTN;

    @FXML
    private Label Title, privateName;

    @FXML
    private ImageView goNatureLogo;

    public ServiceRepresentativeAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
	}
    
    /**
     * @param event
     * When the 'Guide Registration' button is pressed, 
     * the Service Representative will be redirected to the 'GuideRegistrationScreen'
     */
    @FXML
    void GoToGuideRegistrationScreen(ActionEvent event) {
    	//
    	try {
			ScreenManager.getInstance().showScreen("ConfirmationScreenController",
					"/clientSide/fxml/ConfirmationScreen.fxml", false, true,
					StageSettings.defaultSettings("GoNature System - Client Connection"),representative );
		} catch (StatefulException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScreenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * @param event
     * park manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.checkLogOut("representative","representativeId",representative.getIdNumber()))
    		representative.setLoggedIn(false);
        else 
        	showErrorAlert(ScreenManager.getInstance().getStage(), "Failed to log out");
    	try {
    		ScreenManager.getInstance().showScreen("MainScreenConrtroller", "/clientSide/fxml/MainScreen.fxml", true,
    				false, StageSettings.defaultSettings("GoNature System - Reservations"), null);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		Title.setStyle("-fx-alignment: center-right;"); //label component
		GuideRegistrationBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}		

	/**
	 * The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class instance- representative.
	 * It updates JavaFX label component for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		Representative R = (Representative)information;
		setRepresentative(R);		
		this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty();
	}

	public Representative getRepresentative() {
		return representative;
	}

	public void setRepresentative(Representative representative) {
		this.representative = representative;
	}

	@Override
	public String getScreenTitle() {
		return "Service Representative";
	}

	@Override
	public void saveState() {
		userControl.saveRepresentative(representative);
	}

	@Override
	public void restoreState() {
		representative = (Representative) userControl.restoreRepresentative();
		this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty();
	}
	
	/**
	 * Activated after the X is clicked on the window.
	 *  The default isto show a Confirmation Alert with "Yes" and "No" options for the user tochoose. 
	 * "Yes" will check if the client is connected to the server, disconnectit if from the server and the system.
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
			//    	if (userControl.checkLogOut() {

		}
	}

}
