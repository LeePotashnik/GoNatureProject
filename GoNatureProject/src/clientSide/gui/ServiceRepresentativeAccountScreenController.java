package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Representative;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * /**
 * The ServiceRepresentativeAccountScreenController class is responsible for handling all interactions
 * within the service representative account screen. 
 * This class allows manage functionalities specific to their role. 
 * It extends AbstractScreen.
 * 
 * This controller manages user data through the GoNatureUsersController, enabling operations such as
 * authentication, registration, and user session management.
 */
public class ServiceRepresentativeAccountScreenController extends AbstractScreen{

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
    
    public Representative getRepresentative() {
		return representative;
	}

	public void setRepresentative(Representative representative) {
		this.representative = representative;
	}
	
    /**
     * @param event
     * When the 'Guide Registration' button is pressed, 
     * the Service Representative will be redirected to the 'GuideRegistrationScreen'
     */
    @FXML
    void GoToGuideRegistrationScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().showScreen("GroupGuideRegistrationScreenController",
					"/clientSide/fxml/GroupGuideRegistrationScreen.fxml", true, false,null );
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		} 
    }

    /**
     * @param event
     * park manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.logoutUser()) {
    		representative.setLoggedIn(false);
    		System.out.println("Service Representative logged out");
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
	
	@Override
	public String getScreenTitle() {
		return "Service Representative";
	}
	
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the FXML file has been loaded, setting the initial state of the UI components.
	 */
	@Override
	public void initialize() {
	    // Restore the Representative user from the saved state.
	    representative = (Representative) userControl.restoreUser();

	    // Keep the representative's information updated in the session.
	    userControl.saveUser(representative);

	    // Set the greeting message with the representative's name and underline it for emphasis.
	    this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty(); // Emphasize the name for better visibility.

	    // Set the GoNature logo to enhance brand consistency.
	    goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));

	    // Apply alignment styles to ensure that UI elements are consistently presented.
	    privateName.setStyle("-fx-alignment: center-right;"); // Ensure the label is right-aligned.
	    Title.setStyle("-fx-alignment: center-right;"); // Ensure the title label is right-aligned.
	    GuideRegistrationBTN.setStyle("-fx-alignment: center-right;"); // Align button for consistency.
	    logOutBTN.setStyle("-fx-alignment: center-right;"); // Align logout button to match other UI elements.
	}
		

	/**
	 * @param information The data passed from the previous screen, expected to be a Representative instance.
	 */
	@Override
	public void loadBefore(Object information) {
	}

}