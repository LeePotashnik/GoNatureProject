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
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GroupGuideRegistrationScreenController extends AbstractScreen{
	private  final String LETTERS_ONLY_REGEX = "^[a-zA-Z]{1,40}$";
	private  final String DIGITS_ONLY_REGEX = "\\d+";
	private  final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private  final String USERNAME_REGEX = "^(?=.*[a-zA-Z])[A-Za-z\\d.]{1,45}$";
	private  final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).{8,45}$";
	
	private RegistrationController registerController = (RegistrationController) RegistrationController.getInstance();
	@FXML
	private Button backButton, registerbtn;
	@FXML
	private TextField UserNameTxt, emailTxt, firstNameTxt, idTxt, lastNameTxt, phoneTxt;
	@FXML
	private Label name, emaillbl, passwordlbl, phonelbl, userNamelbl;
	@FXML
    private Separator secondSeparator;
	@FXML
	private PasswordField passwordTxt;
	@FXML
	private ImageView goNatureLogo;
	@FXML
    private Button checkIDbtn;

	/**
     * Handles ID check action. Validates the ID field and performs necessary validation
     * and state updates based on ID existence and validity.
     *
     * @param event the action event triggering this method.
     */
    @FXML
    void checkId(ActionEvent event) {
	    registerController.SetIsIdOfTravelerExists(true);// i think its here check later
    	String id = idTxt.getText().trim();
    	if (id.isEmpty() || !(id.matches(DIGITS_ONLY_REGEX) && id.length() == 9)) {    //check if not empty and 9 digits
			idTxt.setStyle(setFieldToError());
			showErrorAlert(ScreenManager.getInstance().getStage(), "- ID number is not valid."); 
		}
    	else {
			ArrayList<Object[]> travelerDetails = registerController.getTravellerDetails(id);
			if (travelerDetails.isEmpty()) {           //return the details of a traveler with that id, if this id exists skips to else that load this id information.
				if(checkIdExistenceForGGRegistration(id)) {        // checks the id on all the other tables if this id exists in the other table return true
					idTxt.setStyle(setFieldToError());
					showErrorAlert(ScreenManager.getInstance().getStage(), "This id number already exists in our System.");
				}
				else {   // id is not existed in our data base at all.
					idTxt.setStyle(setFieldToRegular());
					idTxt.setDisable(true);
				    showAllFieldsAndRegisterButton();
				}
				
			}
			else {    // if we got here it means that this id exists on the 'traveller' table so we want to load this traveler details.
				showInformationAlert(ScreenManager.getInstance().getStage(),"ID exists for a traveler. Details loaded for conversion.");
				idTxt.setStyle(setFieldToRegular());
				idTxt.setDisable(true);
			    showAllFieldsAndRegisterButton();
			    loadTravelerDetails(travelerDetails.get(0));
			    registerController.SetIsIdOfTravelerExists(true);
			}
		}
		

    }

    /**
     * Handles the registration action. Validates form data and interacts with the RegistrationController
     * to register a new group guide or convert an existing traveler to a group guide.
     *
     * @param event the action event triggering this method.
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
		boolean loaded=false;
		if(registerController.isIdOfTravelerExists()) { //the details of the existing traveler id is loaded and we dont want to check the existence of any fields on the 'traveller' table , want to validate that they are not empty and similar to field on other tables and delete him from the 'traveller' and insert him to the 'group_guide'
			loaded=true;
			if(DetailsValidation(firstName,lastName,email,phone,username,password,loaded)){   //if the the details are valid in the aspect of not empty , correct format and there are no  similar username and email on all the tables except 'traveller'
				boolean deleteSuccess = registerController.travellerDeleteFromDB(id);   //if the fields with correct format we want to delete this traveller with this id from the 'Traveller' table and insert him to the 'group_guide' table 
				if (!deleteSuccess) {  // delete from 'Traveller'
					showErrorAlert(ScreenManager.getInstance().getStage(),"Failed to delete existing traveler data.");
					return;
				}
				boolean insertSuccess = registerController.groupGuideInsertToDB(id, firstName, lastName, email, phone,
						username, password);
				if (insertSuccess) {   // insert to 'group_guide'
					showInformationAlert(ScreenManager.getInstance().getStage(),"Registration successful.");
					try {
						returnToPreviousScreen(null);
					} catch (ScreenException | StatefulException e) {
						e.printStackTrace();
					}
				} else {
					showErrorAlert(ScreenManager.getInstance().getStage(),"Failed to register the group guide.");
				}
			}
		} 
		else {  // there is no details with this id we want to check the validation of the details- not empty, correct form, username and email not exists on all of the table including 'Traveller' = table
			DetailsValidation(firstName,lastName,email,phone,username,password,loaded);
			boolean insertSuccess = registerController.groupGuideInsertToDB(id, firstName, lastName, email, phone,
					username, password);
			if (insertSuccess) {   // insert to 'group_guide'
				showInformationAlert(ScreenManager.getInstance().getStage(),"Registration successful.");
				try {
					returnToPreviousScreen(null);
				} catch (ScreenException | StatefulException e) {
					e.printStackTrace();
				}
			} else {
				showErrorAlert(ScreenManager.getInstance().getStage(),"Failed to register the group guide.");
			}
			
		}
    }

    /**
     * Navigates back to the previous screen.
     *
     * @param event the action event triggering this method.
     * @throws ScreenException if there is an issue navigating back.
     * @throws StatefulException if there is a state management issue during navigation.
     */
    @FXML
    void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
		ScreenManager.getInstance().goToPreviousScreen(false,false);

    }

    /**
     * Initializes the screen state and sets up initial UI elements.
     */
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	    hideAllExceptIDAndCheckID();

	}

	
	/**
     * Loads any necessary information before the screen is displayed.
     * Currently not implemented.
     *
     * @param information optional information that might be needed for initialization.
     */
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
     * Validates the existence of the provided ID across different registration tables except 'traveller'.
     * Utilizes RegistrationController's method for existence checking in specified tables.
     *
     * @param id the ID to check for existence.
     * @return true if the ID exists in any table except 'traveller', false otherwise.
     */
	public boolean checkIdExistenceForGGRegistration(String id) {   
		if (registerController.checkIdExistenceForGGRegistrationQuary(id,"group_guide")) {
			return true;			
		}
		if (registerController.checkIdExistenceForGGRegistrationQuary(id,"representative")) {
			return true;			
		}
		if (registerController.checkIdExistenceForGGRegistrationQuary(id,"park_manager")) {
			return true;			
		}
		if (registerController.checkIdExistenceForGGRegistrationQuary(id,"department_manager")) {
			return true;			
		}
		ArrayList<Park> parklist = ParkController.getInstance().fetchParks();
		for(Park park: parklist) {
			if (registerController.checkIdExistenceForGGRegistrationQuary(id,ParkController.getInstance().nameOfTable(park) + "_park_employees")) {
				return true;
			}
		}
		return false;
	}
	
	/**
     * Validates the details of a new or existing traveler converting to a group guide.
     * Ensures fields are not empty, formats are correct, and checks for existing userName or email.
     *
     * @param firstName the first name to validate.
     * @param lastName the last name to validate.
     * @param email the email to validate.
     * @param phone the phone number to validate.
     * @param userName the userName to validate.
     * @param password the password to validate.
     * @param loaded indicates whether the traveler details were pre-loaded.
     * @return true if all details are valid and unique (as required), false otherwise.
     */
	public boolean DetailsValidation(String firstName, String lastName,String email,String phone,String userName,String password, boolean loaded ) {  
		firstNameTxt.setStyle(setFieldToRegular());
		lastNameTxt.setStyle(setFieldToRegular());
		emailTxt.setStyle(setFieldToRegular());
		phoneTxt.setStyle(setFieldToRegular());
		UserNameTxt.setStyle(setFieldToRegular());
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

		if (userName.isEmpty() || !userName.matches(USERNAME_REGEX)) {
			valid = false;
			UserNameTxt.setStyle(setFieldToError());
			showMessage += "\n- The username is not valid.";
		}
		if (password.isEmpty() || !password.matches(PASSWORD_REGEX)) {
			valid = false;
			passwordTxt.setStyle(setFieldToError());
			showMessage += "\n- The password is not valid.";
		}
		String  retTraveller, retGuide, retParkManager, retDepartment, retRep, retEmp;
		if(!(loaded)) {
			if(valid && !(retTraveller = registerController.checkExistenceForGGRegistration(userName, email, "traveller")).equals("")) {
				showMessage += retTraveller;
				if(retTraveller.contains("username"))
					UserNameTxt.setStyle(setFieldToError());
				if(retTraveller.contains("email"))
					emailTxt.setStyle(setFieldToError());
				valid= false;
			}
		}
		if(valid && !(retGuide = registerController.checkExistenceForGGRegistration(userName, email, "group_guide")).equals("")) {
			showMessage += retGuide;
			if(retGuide.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retGuide.contains("email"))
				emailTxt.setStyle(setFieldToError());
			valid= false;
		}
		if(valid && !(retParkManager = registerController.checkExistenceForGGRegistration(userName, email, "park_manager")).equals("")) {
			showMessage += retParkManager;
			if(retParkManager.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retParkManager.contains("email"))
				emailTxt.setStyle(setFieldToError());
			valid= false;
		}
		if(valid && !(retDepartment = registerController.checkExistenceForGGRegistration(userName, email, "department_manager")).equals("")) {
			showMessage += retDepartment;
			if(retDepartment.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retDepartment.contains("email"))
				emailTxt.setStyle(setFieldToError());
			valid= false;
		}
		if(valid && !(retRep = registerController.checkExistenceForGGRegistration(userName, email, "representative")).equals("")) {
			showMessage += retRep;
			if(retRep.contains("username"))
				UserNameTxt.setStyle(setFieldToError());
			if(retRep.contains("email"))
				emailTxt.setStyle(setFieldToError());
			valid= false;
		}
		ArrayList<Park> parklist = ParkController.getInstance().fetchParks();
		for(Park park: parklist) {
			if (valid && !(retEmp = registerController.checkExistenceForGGRegistration(userName, email, ParkController.getInstance().nameOfTable(park) + "_park_employees")).equals("")) {
				showMessage += retEmp;
				if(retEmp.contains("username"))
					UserNameTxt.setStyle(setFieldToError());
				if(retEmp.contains("email"))
					emailTxt.setStyle(setFieldToError());
				valid = false;
			}
		}
		if (!valid) {
			showErrorAlert(ScreenManager.getInstance().getStage(), "Errors:" + showMessage);
		}
		return valid;
	}

	/**
     * Shows all registration fields and the register button, making them visible and managed within the layout.
     */
	private void showAllFieldsAndRegisterButton() {
	    // Make all elements visible and take space
	    firstNameTxt.setVisible(true);
	    firstNameTxt.setManaged(true);
	    lastNameTxt.setVisible(true);
	    lastNameTxt.setManaged(true);
	    emailTxt.setVisible(true);
	    emailTxt.setManaged(true);
	    phoneTxt.setVisible(true);
	    phoneTxt.setManaged(true);
	    UserNameTxt.setVisible(true);
	    UserNameTxt.setManaged(true);
	    passwordTxt.setVisible(true);
	    passwordTxt.setManaged(true);
	    registerbtn.setVisible(true);
	    registerbtn.setManaged(true);
	    
	    name.setVisible(true);
	    name.setManaged(true);
	    emaillbl.setVisible(true);
	    emaillbl.setManaged(true);
	    passwordlbl.setVisible(true);
	    passwordlbl.setManaged(true);
	    phonelbl.setVisible(true);
	    phonelbl.setManaged(true);
	    secondSeparator.setVisible(true);
	    secondSeparator.setManaged(true);
	    userNamelbl.setVisible(true);
	    userNamelbl.setManaged(true);
	}
	
	/**
     * Hides all fields except for the ID field and the check ID button, making them invisible and not managed within the layout.
     */
	private void hideAllExceptIDAndCheckID() {
	    firstNameTxt.setVisible(false);
	    firstNameTxt.setManaged(false);
	    lastNameTxt.setVisible(false);
	    lastNameTxt.setManaged(false);
	    emailTxt.setVisible(false);
	    emailTxt.setManaged(false);
	    phoneTxt.setVisible(false);
	    phoneTxt.setManaged(false);
	    UserNameTxt.setVisible(false);
	    UserNameTxt.setManaged(false);
	    passwordTxt.setVisible(false);
	    passwordTxt.setManaged(false);
	    registerbtn.setVisible(false);
	    registerbtn.setManaged(false);
	    name.setVisible(false);
	    name.setManaged(false);
	    emaillbl.setVisible(false);
	    emaillbl.setManaged(false);
	    passwordlbl.setVisible(false);
	    passwordlbl.setManaged(false);
	    phonelbl.setVisible(false);
	    phonelbl.setManaged(false);
	    secondSeparator.setVisible(false);
	    secondSeparator.setManaged(false);
	    userNamelbl.setVisible(false);
	    userNamelbl.setManaged(false);
	}
	
	/**
     * Loads traveler details into the registration form fields.
     * Intended for use when a traveler's ID is found and their details are to be converted into a group guide registration.
     *
     * @param details the traveler details to load, expected in a specific order.
     */
	private void loadTravelerDetails(Object[] details) {
		firstNameTxt.setText((String) details[0]);
		lastNameTxt.setText((String) details[1]);
		emailTxt.setText((String) details[2]);
		phoneTxt.setText((String) details[4]);
		UserNameTxt.setText((String) details[3]);
		passwordTxt.clear();
	}

	/**
     * Returns the screen title for this controller.
     *
     * @return the title of the screen.
     */
	@Override
	public String getScreenTitle() {
		return "Group guide Registration";
	}

}
