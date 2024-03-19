package clientSide.gui;

import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

public class ParkVisitorAccountScreenController extends AbstractScreen implements Stateful{

	private GoNatureUsersController userControl;
	private ParkVisitor parkVisitor;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, managingBookingBTN, visitBookingBTN;
    
    @FXML
    private Label NameLable;

    public ParkVisitorAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
	}
    
    @FXML
    void goTOManagingBookingScreen(ActionEvent event) {

    }
    
    @FXML
    void goTOVisitBookingScreen(ActionEvent event) {

    }

    /**
     * @param event
     * parkVisitor clicked on "Log out" button, an update query is executed to alter the value of the 'isLoggedIn' field
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event){
    	//Only GROUPGUIDE (not TRAVELLER) is a user, thus only they need to log out.    	
    	if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE)
    		if (userControl.checkLogOut("group_guide","groupGuideId",parkVisitor.getIdNumber()))
    			parkVisitor.setLoggedIn(false);
        else 
        	showErrorAlert(ScreenManager.getInstance().getStage(), "Failed to log out");
    	try {
			ScreenManager.getInstance().goToPreviousScreen(false,false);
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
		managingBookingBTN.setStyle("-fx-alignment: center-right;");
		visitBookingBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	    
	}

	/**
	 *The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class instance- parkVisitor.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkVisitor PV = (ParkVisitor)information;
		setParkVisitor(PV);	
		this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
	    this.NameLable.underlineProperty();
	    
	}

	public ParkVisitor getParkManager() {
		return parkVisitor;
	}

	public void setParkVisitor(ParkVisitor parkVisitor) {
		this.parkVisitor = parkVisitor;
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveState() {
		userControl.saveUser(parkVisitor);
	}

	@Override
	public void restoreState() {
		parkVisitor = (ParkVisitor) userControl.restoreUser();
		this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
	    this.NameLable.underlineProperty();
	}
	
	/**
	 * Activated after the X is clicked on the window.
	 *  The default is to show a Confirmation Alert with "Yes" and "No" options for the user tochoose. 
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
