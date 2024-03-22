package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.LoginController;
import clientSide.control.ParametersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.ParkVisitor;
import entities.SystemUser;
import entities.ParkVisitor.VisitorType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

public class TravellerLoginScreenConrtroller extends AbstractScreen {
	private GoNatureUsersController goNatureUsersController;
	private LoginController loginController; // controller

	
	public TravellerLoginScreenConrtroller() {
		loginController = new LoginController();
		goNatureUsersController = GoNatureUsersController.getInstance();
	}

	@FXML
	private TextField IDNumberTextField;

	@FXML
	private Button backButton;

	@FXML
	private Button loginButton;

	@FXML
	private ImageView goNatureLogo;

	/**
	 * This method is called when the 'Login' button is pressed. It opens the
	 * booking managing screen
	 * 
	 * @throws StatefulException,ScreenException
	 */
	@FXML
	void openBookingManagingScreen(ActionEvent event) {

		String idNumber = IDNumberTextField.getText();
		IDNumberTextField.setStyle(setFieldToRegular());

		// validating the idNumber
		// validating IDNumberTextField is not empty:
		if (idNumber.isEmpty()) {
			showErrorAlert("Please enter ID number");
			IDNumberTextField.setStyle(setFieldToError());
		}
		// validating IDNumberTextField contains only numbers:
		else if (!idNumber.matches("\\d+")) {
			showErrorAlert("Please make sure that the ID number contains only numbers");
			IDNumberTextField.setStyle(setFieldToError());
		}
		// validating IDNumberTextField contains exactly 9 numbers:
		else if (idNumber.matches("\\d{1,8}")) {
			showErrorAlert("The ID you entered has less than 9 digits. Please ensure your ID is exactly 9 digits long");
			IDNumberTextField.setStyle(setFieldToError());
		} else if (idNumber.matches("\\d{10,}")) {
			showErrorAlert("The ID you entered has more than 9 digits. Please ensure your ID is exactly 9 digits long");
			IDNumberTextField.setStyle(setFieldToError());
		}
		
		
		//Validation passed successfully:
		else 
		{ 
			
			if(loginController.checkIfIdOfGroupGuide(idNumber)) //return true if groupGuideUser with this idNumber is exists
			{
				/////***********////////
				showErrorAlert("There is a user registered as a guide under this ID. Please log in with the userName and password given to you as a guide or log in with another ID.");
			}
			else
			{
				if(loginController.checkIdInTravelerUsers(idNumber)) //this traveler exist in the traveler table
				{
					if(loginController.checkAlreadyLoggedInForTraveler(idNumber)) //this traveler already logged in
					{
				
						showErrorAlert("This user is already logged in.Please make sure you are disconnected from all devices and try again.");
					}
					else
					{
						//change isLoggedIn in the traveler table:
						loginController.updateUserIsLoggedIn("traveller", "travellerId", idNumber);
					}
				}
				else
				{
					//add the traveler to the traveller table:
					loginController.addNewTraveler(idNumber);
				}
				
				//create instance of parkVisitor :
				ParkVisitor travelerVisitor=new ParkVisitor(idNumber,null,null,null,null,null,null,true,VisitorType.TRAVELLER);
				goNatureUsersController.saveUser(travelerVisitor); //save the object in the goNatureUsersController
				if(loginController.checkIfIdOfActivetBooking(idNumber)) //there is active booking with this id=>move to travelerAccountScreen travelerAccountScreenController
				{
					System.out.println("save the instance and move to ParkVisitorAccountScreenController ");
					//ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController",
					//	"/clientSide/fxml/ParkVisitorAccountScreen.fxml", false, false, null);
				}
				else //there is no active booking with id=> send the instance to bookingScreenController
				{
					System.out.println("save the instance and move to bookingScreenController ");
					//ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController",
					//	"/clientSide/fxml/bookingScreen.fxml", false, false, null);

	
				}
				
				
			}

		}
	}

	
	
	@FXML
	void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
    	ScreenManager.getInstance().goToPreviousScreen(false, false);

	}

	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		// setting the back button image:
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	}

	@Override
	public void loadBefore(Object information) {

	}

	@Override
	public String getScreenTitle() {
		return "Traveller Login";
	}

}
