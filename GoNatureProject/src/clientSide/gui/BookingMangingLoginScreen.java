package clientSide.gui;

import clientSide.control.LoginController;
import common.controllers.AbstractScreen;
import common.controllers.StatefulException;
import entities.Booking;
import entities.SystemUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookingMangingLoginScreen extends AbstractScreen{

    @FXML
    private TextField userNameID;

    @FXML
    private Button backButton;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button loginButton;

    @FXML
    private Button BackToMainScreenButton;


    
    /** 
     *This method is called when the 'Return To Main Screen' button is pressed. It opens the main screen
     * @throws StatefulException,ScreenException
     */
    @FXML
    void openMainScreen(ActionEvent event) {
    	//ScreenManager.getInstance().showScreen("MainScreenController","/clientSide/fxml/MainScreen.fxml",
    			//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), null);
    }
    
    /** 
     *This method is called when the 'Login' button is pressed. It opens the booking managing screen
     * @throws StatefulException,ScreenException
     */
    @FXML
    void openBookingManagingScreen(ActionEvent event) {
    	
    	String idNumber=userNameID.getText();
    	Booking booking=LoginController.checkIdInActiveBookings(idNumber);
    	if(booking!=null)
    	{
    		//ScreenManager.getInstance().showScreen("BookingScreenController","/clientSide/fxml/BookingScreen.fxml",
			//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), booking);    		
    	}
    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
      	//ScreenManager.getInstance().showScreen("BookingScreenController","/clientSide/fxml/BookingScreen.fxml",
    		//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), visitorUser);
    }
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));		
		//setting the back button image:
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	}

	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
