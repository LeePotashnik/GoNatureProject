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

public class ServiceRepresentativeAccountScreen extends AbstractScreen{

	private GoNatureUsersController UC;

	private Representative representative;
    @FXML
    private Button GuideRegistrationBTN, logOutBTN;

    @FXML
    private Label Title, privateName;

    @FXML
    private ImageView goNatureLogo;

    /**
     * @param event
     * When the 'Guide Registration' button is pressed, 
     * the Service Representative will be redirected to the 'GuideRegistrationScreen'
     */
    @FXML
    void GoToGuideRegistrationScreen(ActionEvent event) {

    }

    /**
     * @param event
     * park manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (UC != null) {
    		if (this.UC.checkLogOut("representative","representativeId",representative.getIdNumber()))
    			representative.setLoggedIn(false);
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

	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		Title.setStyle("-fx-alignment: center-right;"); //label component
		GuideRegistrationBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
		UC = new GoNatureUsersController();
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

}
