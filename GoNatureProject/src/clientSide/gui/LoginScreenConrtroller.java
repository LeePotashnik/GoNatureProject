package clientSide.gui;

import clientSide.control.LoginController;
import clientSide.control.ParkController;
import common.controllers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkEmployee;
import entities.ParkManager;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import entities.SystemUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginScreenConrtroller extends AbstractScreen {

    @FXML
    private TextField userNameID;
    
    @FXML
    private PasswordField passwordID;

    @FXML
    private Button backButton;

    @FXML
    private Button loginButton;

    @FXML
    private ImageView goNatureLogo;
	
	

    
    
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
    	userNameID.setStyle(setFieldToRegular());
    	passwordID.setStyle(setFieldToRegular());
    	// validating the userName :
    	if(userName.isEmpty())
    	{
    		showErrorAlert(ScreenManager.getInstance().getStage(),"Please enter userName");
    		userNameID.setStyle(setFieldToError());
    	}
    	// validating the password 
    	if(password.isEmpty())
    	{
       		showErrorAlert(ScreenManager.getInstance().getStage(),"Please enter password");
       		passwordID.setStyle(setFieldToError());
    	}
    	////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	//The entered details are correct in terms of validation.
    	
    	
    	
    	else
    	{
    		//Create a query to check if this visitorUser exists:
    		ParkVisitor visitorUser=LoginController.checkVisitorCredential(userName, password); //returned ParkVisitor if the visitorUser exist,null if not
    		//check if user with this userName&password exist as Traveler/GroupGuide(ParkVisitor)
    		if(visitorUser!=null) 
        	{
        		//check if this user is already loggedIn:
        		if(!LoginController.checkAlreadyLoggedIn(visitorUser))  //the user is not already loggedIn
        		{
        			//case that the exist parkVisitorUser is traveler:
        			if(visitorUser.getVisitorType()==VisitorType.TRAVELLER) 
        			{
        				if(LoginController.updateUserIsLoggedIn("traveller","travellerId",visitorUser.getIdNumber()))  //update that the visitorUser is logged in, in the DB
        				{
        					//update that the visitorUser is logged in, in the instance field
        					visitorUser.setLoggedIn(true);
        					/////////////////////////////////////////
        					showInformationAlert(ScreenManager.getInstance().getStage(),"the user:"+visitorUser.getUsername()+" successfully login");
        					//////////////////////////////////
        					//open the traveler account screen:
        					//ScreenManager.getInstance().showScreen("TravelerAccountScreen","/clientSide/fxml/TravelerAccountScreen.fxml",
        					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), visitorUser);
        				}
        			}
        			//case that the exist parkVisitorUser is groupGuide
            		if(!LoginController.checkAlreadyLoggedIn(visitorUser)) 
            		{
            			if(visitorUser.getVisitorType()==VisitorType.GROUPGUIDE) 
            			{
            				if(LoginController.updateUserIsLoggedIn("group_guide","groupGuideId",visitorUser.getIdNumber()))//update that the visitorUser is logged in, in the DB
            				{
            					//update that the visitorUser is logged in, in the instance field
            					visitorUser.setLoggedIn(true);
            					//////////////////////////////
            					showInformationAlert(ScreenManager.getInstance().getStage(),"the user:"+visitorUser.getUsername()+" successfully login");
            					/////////////////////////////
            					
            					//open the groupGuide account screen:
            					//ScreenManager.getInstance().showScreen("GroupGuideAccountScreen","/clientSide/fxml/GroupGuideAccount.fxml",
            					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), visitorUser);
            				}
            			}
            		}      			
        		}
        		//the user is already loggedIn =>show error alert
        		else
        		{
        			showErrorAlert(ScreenManager.getInstance().getStage(),"This user is already logged in.Please make sure you are disconnected from all devices and try again.");
        		}
        	}
    		
    		
    		
        	////the user is not found as a visitorUser or stuffUser
    		//check if user with this userName&password exist as ParkManager/DepartmentManager/ParkEmployee(stuffVisitor)
        	else
        	{
           		////returned SystemUser if the stuffUser exist,null if not
        		SystemUser stuffUser=LoginController.checkEmployeeCredential(userName,password);    		
            	if(stuffUser!=null)
            	{
            		//check if this user is already loggedIn:
            		if(!LoginController.checkAlreadyLoggedIn(stuffUser))//the user is not already loggedIn
            		{
            			
            			//case that the exist stuffUser is ParkManager:
            			if(stuffUser instanceof ParkManager ) 
            			{
            				if(LoginController.updateUserIsLoggedIn("park_manager","parkManagerId",stuffUser.getIdNumber()))  //update in the DB that the stuffUser is logged in
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					showInformationAlert(ScreenManager.getInstance().getStage(),"the user:"+stuffUser.getUsername()+" successfully login");
            					//open the ParkManager account screen:
            					//ScreenManager.getInstance().showScreen("ParkManagerAccountScreen","/clientSide/fxml/ParkManagerScreen.fxml",
                    					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), stuffUser);
            				}
            			}
            			
            			//case that the exist stuffUser is DepartmentManager:
            			if(stuffUser instanceof DepartmentManager ) 
            			{
            				if(LoginController.updateUserIsLoggedIn("department_manager","departmentManagerId",stuffUser.getIdNumber()))  //update in the DB that the stuffUser is logged in
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					//////////////////////////////////////////////////////////////
            					showInformationAlert(ScreenManager.getInstance().getStage(),"the user:"+stuffUser.getUsername()+" successfully login");
            					/////////////////////////////////////////////////////////////
            					//open the ParkManager account screen:
            					//ScreenManager.getInstance().showScreen("DepartmentManagerAccountScreen","/clientSide/fxml/DepartmentManagerScreen.fxml",
                    					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), stuffUser);
            				}
            			}
            			
            			//case that the exist stuffUser is ParkEmployee:
            			if(stuffUser instanceof ParkEmployee ) 
            			{
            				//find the name of the table where we found the user:
            			
            				Park park=((ParkEmployee) stuffUser).getWorkingIn(); //getting the park the user works in
            				ParkController parkController=ParkController.getInstance();//getting instance of ParkController, to use its methods
            				String nameOfTable=parkController.nameOfTable(park); //will return the park name in lowercase and with "_" between each word
            				//adding suffix to the table name(to be more specific about the table name)
            				nameOfTable=nameOfTable+"_park_employees";
            							
            				//update in the DB that the stuffUser is logged in:		
            				if(LoginController.updateUserIsLoggedIn(nameOfTable,"employeeId",stuffUser.getIdNumber()))  
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					///////////////////////////////////////////////////
            					showInformationAlert(ScreenManager.getInstance().getStage(),"the user:"+stuffUser.getUsername()+" successfully login");
            					//////////////////////////////////////////////////////////
            					//open the ParkManager account screen:
            					//ScreenManager.getInstance().showScreen("DepartmentManagerAccountScreen","/clientSide/fxml/DepartmentManagerScreen.fxml",
                    					//false, false,StageSettings.defaultSettings("GoNature System - Reservations"), stuffUser);
            				}
            			}
            		}            		
            		//the user is already loggedIn =>show error alert:
            		else
            		{
            			showErrorAlert(ScreenManager.getInstance().getStage(),"This user is already logged in.Please make sure you are disconnected from all devices and try again.");	
            		}
            	}
 
            	
            	//the user is not found as a visitorUser and as stuffUser  => show error alert
        		else showErrorAlert(ScreenManager.getInstance().getStage(),"No system user was found with this username and password.Please make sure you enter the correct details.");
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

	
	
	///TO DO:
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String getScreenTitle() {
		
		return "Visitors Login";
	}

}
