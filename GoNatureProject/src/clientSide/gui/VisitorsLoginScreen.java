package clientSide.gui;

import clientSide.control.*;
import common.controllers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.SystemUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VisitorsLoginScreen extends AbstractScreen {

    @FXML
    private TextField userNameID;

    @FXML
    private Button backButton;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button loginButton;

    @FXML
    private TextField passwordID;

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
     *When the Login button is pressed, this method is called. 
     *If the userName and password are correct, it opens the visitor's account screen
     * @throws StatefulException,ScreenException
     * @throws CommunicationException 
     */

    @FXML
    void openVisitorsAccountScreen(ActionEvent event) throws StatefulException, ScreenException, CommunicationException
    {
    	String userName=userNameID.getText();
    	String password=passwordID.getText();
    	SystemUser visitorUser=LoginController.checkVisitorCredential(userName, password);
    	if(visitorUser!=null)
    	{
    		if(!LoginController.checkAlreadyLoggedIn(visitorUser))
    		{
    			LocalTime time=LocalTime.now();
    			LocalDate date=LocalDate.now();
    			LoginController.updateLastLoggedIn(visitorUser,date, time);
    			//ScreenManager.getInstance().showScreen("VisitorsAccountScreenController","/clientSide/fxml/VisitorsAccountScreen.fxml",
    					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), visitorUser);
    		}
    	}
    }

    /** 
     *This method is called when the return button is pressed. It opens the main screen
     * @throws StatefulException,ScreenException
     */
    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	//ScreenManager.getInstance().showScreen("MainScreenController","/clientSide/fxml/MainScreen.fxml",
		//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), null);
    }

	@Override
	@FXML
	public void initialize()
	{
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
