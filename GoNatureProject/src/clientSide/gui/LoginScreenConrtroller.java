package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.LoginController;
import clientSide.control.ParametersController;
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
import entities.Representative;
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
	
	private LoginController loginController; // controller
	private GoNatureUsersController goNatureUsersController;
	
	public LoginScreenConrtroller() {
		loginController = new LoginController();
		goNatureUsersController = GoNatureUsersController.getInstance();

	}

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
    		showErrorAlert("Please enter userName");
    		userNameID.setStyle(setFieldToError());
    	}
    	// validating the password 
    	if(password.isEmpty())
    	{
       		showErrorAlert("Please enter password");
       		passwordID.setStyle(setFieldToError());
    	}
    	////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	//The entered details are correct in terms of validation.
    	
    	
    	
    	else
    	{
    		//Create a query to check if this visitorUser exists:
    		ParkVisitor visitorUser=loginController.checkVisitorCredential(userName, password); //returned ParkVisitor if the visitorUser exist,null if not
    		//check if user with this userName&password exist as GroupGuide(ParkVisitor)
    		if(visitorUser!=null) 
        	{
        		//check if this user is already loggedIn:
        		if(!loginController.checkAlreadyLoggedIn(visitorUser))  //the user is not already loggedIn
        		{
        			//case that the exist parkVisitorUser is groupGuide
        			if(!loginController.checkAlreadyLoggedIn(visitorUser)) 
            		{
            			if(visitorUser.getVisitorType()==VisitorType.GROUPGUIDE) 
            			{
            				if(loginController.updateUserIsLoggedIn("group_guide","groupGuideId",visitorUser.getIdNumber()))//update that the visitorUser is logged in, in the DB
            				{
            					//update that the visitorUser is logged in, in the instance field
            					visitorUser.setLoggedIn(true);
            					//////////////////////////////
            					//Temporary:
            					showInformationAlert("the user:"+visitorUser.getUsername()+" successfully login");
            					/////////////////////////////
            					
            					goNatureUsersController.saveUser(visitorUser); //save the object in the goNatureUsersController
            					
            					//open the groupGuide account screen:
            					ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController", "/clientSide/fxml/ParkVisitorAccountScreen.fxml", false,
            							false,null);
            				}
            			}
            		}      			
        		}
        		//the user is already loggedIn =>show error alert
        		else
        		{
        			showErrorAlert("This user is already logged in.Please make sure you are disconnected from all devices and try again.");
        		}
        	}
    
    		
        	////the user is not found as a visitorUser or stuffUser
    		//check if user with this userName&password exist as ParkManager/DepartmentManager/ParkEmployee(stuffVisitor)
        	else
        	{
           		////returned SystemUser if the stuffUser exist,null if not
        		SystemUser stuffUser=loginController.checkEmployeeCredential(userName,password);    		
            	if(stuffUser!=null)
            	{
            		//check if this user is already loggedIn:
            		if(!loginController.checkAlreadyLoggedIn(stuffUser))//the user is not already loggedIn
            		{
            			
            			//case that the exist stuffUser is ParkManager:
            			if(stuffUser instanceof ParkManager ) 
            			{
            				if(loginController.updateUserIsLoggedIn("park_manager","parkManagerId",stuffUser.getIdNumber()))  //update in the DB that the stuffUser is logged in
            				{
            					//update that the stuffUser is logged in, in the instance field
            		    		System.out.println("ParkManager is logged in");

            					stuffUser.setLoggedIn(true);
            					showInformationAlert("the user:"+stuffUser.getUsername()+" successfully login");
            					
            					goNatureUsersController.saveUser(stuffUser); //save the object in the goNatureUsersController

            					//open the ParkManager account screen:
            					ScreenManager.getInstance().showScreen("ParkManagerAccountScreenController",
            									"/clientSide/fxml/ParkManagerAccountScreen.fxml", false, false,null);
            				}
            			}
            			
            			//case that the exist stuffUser is DepartmentManager:
            			else if(stuffUser instanceof DepartmentManager ) 
            			{
            				if(loginController.updateUserIsLoggedIn("department_manager","departmentManagerId",stuffUser.getIdNumber()))  //update in the DB that the stuffUser is logged in
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					//////////////////////////////////////////////////////////////
            					//teporary:
            					showInformationAlert("the user:"+stuffUser.getUsername()+" successfully login");
            					/////////////////////////////////////////////////////////////
            					
            					goNatureUsersController.saveUser(stuffUser); //save the object in the goNatureUsersController

            					//open the ParkManager account screen:
            					ScreenManager.getInstance().showScreen("DepartmentManagerAccountScreenController",
            							"/clientSide/fxml/DepartmentManagerAccountScreen.fxml", false, false,null);
            				}
            			}
            			//case that the exist stuffUser is Representative:
            			else if(stuffUser instanceof Representative ) 
            			{
            				if(loginController.updateUserIsLoggedIn("representative","representativeId",stuffUser.getIdNumber()))  //update in the DB that the stuffUser is logged in
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					showInformationAlert("the user:"+stuffUser.getUsername()+" successfully login");
            					
            					goNatureUsersController.saveUser(stuffUser); //save the object in the goNatureUsersController

            					//open the ParkManager account screen:
            					ScreenManager.getInstance().showScreen("RepresentativeManagerAccountScreenController",
            							"/clientSide/fxml/RepresentativeManagerAccountScreen.fxml", false, false,null);
            				}
            			}
            			
            			//case that the exist stuffUser is ParkEmployee:
            			else  
            			{
            				//find the name of the table where we found the user:
            			
            				Park park=((ParkEmployee) stuffUser).getWorkingIn(); //getting the park the user works in
            				ParkController parkController=ParkController.getInstance();//getting instance of ParkController, to use its methods
            				String nameOfTable=parkController.nameOfTable(park); //will return the park name in lowercase and with "_" between each word
            				//adding suffix to the table name(to be more specific about the table name)
            				nameOfTable=nameOfTable+"_park_employees";
            							
            				//update in the DB that the stuffUser is logged in:		
            				if(loginController.updateUserIsLoggedIn(nameOfTable,"employeeId",stuffUser.getIdNumber()))  
            				{
            					//update that the stuffUser is logged in, in the instance field
            					stuffUser.setLoggedIn(true);
            					///////////////////////////////////////////////////
            					//temporary:
            					showInformationAlert("the user:"+stuffUser.getUsername()+" successfully login");
            					//////////////////////////////////////////////////////////
            					goNatureUsersController.saveUser(stuffUser); //save the object in the goNatureUsersController

            					//open the ParkManager account screen:
            					ScreenManager.getInstance().showScreen("ParkEmployeeAccountScreenController",
            									"/clientSide/fxml/ParkEmployeeAccountScreen.fxml", false, false,null);
            				}
            			}
            		}            		
            		//the user is already loggedIn =>show error alert:
            		else
            		{
            			showErrorAlert("This user is already logged in.Please make sure you are disconnected from all devices and try again.");	
            		}
            	}
 
            	
            	//the user is not found as a visitorUser and as stuffUser  => show error alert
        		else showErrorAlert("No system user was found with this username and password.Please make sure you enter the correct details.");
        	}
    	}
    }
    
    
   

    /** 
     *This method is called when the return button is pressed. It opens the main screen
     * @throws ScreenException 
     * @throws StatefulException,ScreenException
     */
    @FXML
    void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
    	ScreenManager.getInstance().goToPreviousScreen(false, false);
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
	}



	@Override
	public String getScreenTitle() {
		
		return "Visitors Login";
	}

}
