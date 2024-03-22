package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
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
 * It extends AbstractScreen and implements the Stateful interface for managing screen states and
 * navigation within the application.
 * 
 * This controller manages user data through the GoNatureUsersController, enabling operations such as
 * authentication, registration, and user session management.
 */
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
					"/clientSide/fxml/GroupGuideRegistrationScreen.fxml", true, true,representative );
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

	@Override
	public void saveState() {
		userControl.saveUser(representative);
	}
	
	/**
	 * This method is automatically called after the FXML file has been loaded.
	 * It initializes UI components and sets the image for the GoNature logo. It also styles various UI elements
	 * such as buttons and labels to maintain consistency with the application's design.
	 */
	@Override
	public void initialize() {
		/*
		this.representative = (Representative) userControl.restoreUser();
		userControl.saveUser(representative);
		this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty();
		 */
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		Title.setStyle("-fx-alignment: center-right;"); //label component
		GuideRegistrationBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}		

	/**
	 * Receives and loads data from the previous screen. This method updates the representative instance
	 * and UI components with the information of the currently logged-in service representative.
	 * 
	 * @param information The data passed from the previous screen, expected to be a Representative instance.
	 */
	@Override
	public void loadBefore(Object information) {
		Representative R = (Representative)information;
		this.representative = R;	
		userControl.saveUser(representative);
		this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty();
	}
	
	/**
	 * Restores the state of the representative and the screen. This method is called to repopulate
	 * the screen with data that was previously saved, ensuring continuity in the user's session.
	 */
	@Override
	public void restoreState() {
		representative = (Representative) userControl.restoreUser();
		this.privateName.setText("Hello " + representative.getFirstName() + " " + representative.getLastName());
	    this.privateName.underlineProperty();
	}

}