package clientSide.gui;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.control.ParkController;
import clientSide.control.RegistrationController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TravellerRegistrationScreenController extends AbstractScreen {
	private static final String LETTERS_ONLY_REGEX = "^[a-zA-Z]{1,40}$";
	private static final String DIGITS_ONLY_REGEX = "\\d+";
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final String USERNAME_REGEX = "^(?=.*[a-zA-Z])[A-Za-z\\d.]{1,45}$";
	private static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).{8,45}$";

	private RegistrationController registerController;
	
	public TravellerRegistrationScreenController() {
		this.registerController=RegistrationController.getInstance();
	}
	

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////
	
	@FXML
	private Button backButton, registerbtn;
	@FXML
	private TextField UserNameTxt, emailTxt, firstNameTxt, idTxt, lastNameTxt, phoneTxt;
	@FXML
	private Label name, userNameTakenError;
	@FXML
	private PasswordField passwordTxt;
	@FXML
	private ImageView goNatureLogo;


	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	
	/**
     * Handles the registration process for a new traveller. Validates input details and,
     * if validation passes, attempts to insert the new traveller into the database.
     *
     * @param event ActionEvent triggered by clicking the register button.
     */
	@FXML
	void register(ActionEvent event) {
		String username = UserNameTxt.getText().trim();
		String email = emailTxt.getText().trim();
		String firstName = firstNameTxt.getText().trim();
		String lastName = lastNameTxt.getText().trim();
		String id = idTxt.getText().trim();
		String phone = phoneTxt.getText().trim();
		String password = passwordTxt.getText();
		boolean valid = detailsValidation(firstName,lastName,id,email,phone,username,password);
		if (valid) {
			boolean insertedToDB = registerController.travellerInsertToDB(id, firstName, lastName, email, phone,
					username, password);
			if (!insertedToDB) {
				showErrorAlert(ScreenManager.getInstance().getStage(), "Oops.. Somthing went wrong, please try again later.");
				try {
					returnToPreviousScreen(null);
				} catch (ScreenException | StatefulException e) {
					e.printStackTrace();
				}
			} else {
				showInformationAlert(ScreenManager.getInstance().getStage(), "Your registration process completed succesfully.");
				try {
					returnToPreviousScreen(null);
				} catch (ScreenException | StatefulException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * Returns the user to the previous screen.
     *
     * @param event ActionEvent triggered by clicking the back button.
     * @throws ScreenException If an error occurs during screen transition.
     * @throws StatefulException If an error related to state management occurs.
     */
	@FXML
	void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
		ScreenManager.getInstance().goToPreviousScreen(false,false);
	}
	
	

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	
	/**
     * Validates the input fields for the registration form.
     *
     * @param firstName User's first name.
     * @param lastName User's last name.
     * @param id User's ID.
     * @param email User's email.
     * @param phone User's phone number.
     * @param username User's chosen username.
     * @param password User's chosen password.
     * @return true if all fields are valid, false otherwise.
     */
	public boolean detailsValidation(String firstName, String lastName,String id,String email,String phone,String username,String password ) {
		UserNameTxt.setStyle(setFieldToRegular());
		emailTxt.setStyle(setFieldToRegular());
		firstNameTxt.setStyle(setFieldToRegular());
		lastNameTxt.setStyle(setFieldToRegular());
		idTxt.setStyle(setFieldToRegular());
		phoneTxt.setStyle(setFieldToRegular());
		passwordTxt.setStyle(setFieldToRegular());
		String showMessage = "";
		boolean valid = true;

		if (firstName.isEmpty() || !firstName.matches(LETTERS_ONLY_REGEX)) {
			valid = false;
			firstNameTxt.setStyle(setFieldToError());
			showMessage += "\n- First name is not valid.";
		}
		if (lastName.isEmpty() || !lastName.matches(LETTERS_ONLY_REGEX)) {
			valid = false;
			lastNameTxt.setStyle(setFieldToError());
			showMessage += "\n- Last name is not valid.";
		}
		if (id.isEmpty() || !(id.matches(DIGITS_ONLY_REGEX) && id.length() == 9)) {
			valid = false;
			idTxt.setStyle(setFieldToError());
			showMessage += "\n- ID number is not valid.";
		}

		if (email.isEmpty() || !email.matches(EMAIL_REGEX) || email.length() > 45) {
			valid = false;
			emailTxt.setStyle(setFieldToError());
			showMessage += "\n- Email address is not valid.";
		}

		if (phone.isEmpty() || !(phone.matches(DIGITS_ONLY_REGEX) && phone.length() == 10)) {
			valid = false;
			phoneTxt.setStyle(setFieldToError());
			showMessage += "\n- Phone number is not valid.";
		}

		if (username.isEmpty() || !username.matches(USERNAME_REGEX)) {
			valid = false;
			UserNameTxt.setStyle(setFieldToError());
			showMessage += "\n- The user name is not valid.";
		}
		if (password.isEmpty() || !password.matches(PASSWORD_REGEX)) {
			valid = false;
			passwordTxt.setStyle(setFieldToError());
			showMessage += "\n- The password is not valid.";
		}
		String retTraveller, retGuide, retParkManager, retDepartment, retRep, retEmp;
		if (valid && !(retTraveller = registerController.checkExistenceForTravellerRegistration(username, email, id, "traveller")).equals("")) {
			showMessage += retTraveller;
			if(retTraveller.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retTraveller.contains("email"))
				emailTxt.setStyle(setFieldToError());
			if(retTraveller.contains("id"))
				idTxt.setStyle(setFieldToError());
			valid = false;
		}
		if (valid && !(retGuide = registerController.checkExistenceForTravellerRegistration(username, email, id, "group_guide")).equals("")) {
			showMessage += retGuide;
			if(retGuide.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retGuide.contains("email"))
				emailTxt.setStyle(setFieldToError());
			if(retGuide.contains("id"))
				idTxt.setStyle(setFieldToError());
			valid = false;
		}
		if (valid && !(retRep = registerController.checkExistenceForTravellerRegistration(username, email, id, "representative")).equals("")) {
			showMessage += retRep;
			if(retRep.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retRep.contains("email"))
				emailTxt.setStyle(setFieldToError());
			if(retRep.contains("id"))
				idTxt.setStyle(setFieldToError());
			valid = false;
		}
		if (valid && !(retParkManager = registerController.checkExistenceForTravellerRegistration(username, email, id, "park_manager")).equals("")) {
			showMessage += retParkManager;
			if(retParkManager.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retParkManager.contains("email"))
				emailTxt.setStyle(setFieldToError());
			if(retParkManager.contains("id"))
				idTxt.setStyle(setFieldToError());
			valid = false;
		}
		if (valid && !(retDepartment = registerController.checkExistenceForTravellerRegistration(username, email, id, "department_manager")).equals("")) {
			showMessage += retDepartment;
			if(retDepartment.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retDepartment.contains("email"))
				emailTxt.setStyle(setFieldToError());
			if(retDepartment.contains("id"))
				idTxt.setStyle(setFieldToError());
			valid = false;
		}
		ArrayList<Park> parklist = ParkController.getInstance().fetchParks();
		for(Park park: parklist) {
			if (valid && !(retEmp = registerController.checkExistenceForTravellerRegistration(username, email, id, ParkController.getInstance().nameOfTable(park) + "_park_employees")).equals("")) {
				showMessage += retEmp;
				if(retEmp.contains("username"))
					UserNameTxt.setStyle(setFieldToError());
				if(retEmp.contains("email"))
					emailTxt.setStyle(setFieldToError());
				if(retEmp.contains("id"))
					idTxt.setStyle(setFieldToError());
				valid = false;
			}
		}		
		if (!valid) {
			showErrorAlert(ScreenManager.getInstance().getStage(), "Errors:\n" + showMessage);
		}
		return valid;
	}



	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
     * Initializes the controller class. Sets up necessary UI elements such as images and
     * initial field states.
     */
	@Override
	public void initialize() {
//		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
		

	}

	/**
     * Loads any necessary data before the screen is displayed. Can be used to set up
     * information based on previous screens or user actions.
     *
     * @param information Information required to initialize the screen, if any.
     */
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub

	}

	/**
     * Returns the title of the screen.
     *
     * @return A String representing the title of the Traveller Registration Screen.
     */
	@Override
	public String getScreenTitle() {
		return "Traveller Registration";
	}

}
