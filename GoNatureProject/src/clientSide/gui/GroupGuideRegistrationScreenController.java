package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.RegistrationController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
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

public class GroupGuideRegistrationScreenController extends AbstractScreen {
	private final String DIGITS_ONLY_REGEX = "\\d+";
	private RegistrationController registerController = (RegistrationController) RegistrationController.getInstance();

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////
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

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	/**
	 * Handles ID check action. Validates the ID field and performs necessary
	 * validation and state updates based on ID existence and validity.
	 *
	 * @param event the action event triggering this method.
	 */
	@FXML
	void checkId(ActionEvent event) {
		String id = idTxt.getText().trim();
		if (id.isEmpty() || !(id.matches(DIGITS_ONLY_REGEX) && id.length() == 9)) { // check if not empty and 9 digits
			idTxt.setStyle(setFieldToError());
			showErrorAlert("- ID number is not valid.");
		} else {
			ArrayList<Object[]> userDetails = registerController.getUserDetails(id);
			if (userDetails.isEmpty()) {
				idTxt.setStyle(setFieldToError());
				showErrorAlert("This user's id doent not exists in our System.");
			} else {
				showInformationAlert("ID exists for a user. Details loaded for conversion.");
				idTxt.setStyle(setFieldToRegular());
				idTxt.setDisable(true);
				showAllFieldsAndRegisterButton();
				loadUserDetails(userDetails.get(0));
				firstNameTxt.setDisable(true);
				lastNameTxt.setDisable(true);
				emailTxt.setDisable(true);
				phoneTxt.setDisable(true);
				UserNameTxt.setDisable(true);
				passwordTxt.setDisable(true);

			}
		}

	}

	/**
	 * Handles the registration action. Validates form data and interacts with the
	 * RegistrationController to register a new group guide or convert an existing
	 * traveler to a group guide.
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

		if (DetailsValidation(firstName, lastName, email, phone, username, password)) { // if the the details are valid
																						// in the aspect of not empty
			boolean deleteSuccess = registerController.userDeleteFromTable(id); // if the fields with correct format we
																				// want to delete this user with this id
																				// from the 'system_user' table and
																				// insert him to the 'group_guide' table
			if (!deleteSuccess) { // delete from 'system_users'
				showErrorAlert("Failed to delete user from 'system_users'.");
				return;
			}
			boolean insertSuccess = registerController.groupGuideInsertToDB(id, firstName, lastName, email, phone,
					username, password);
			if (insertSuccess) { // insert to 'group_guide'
				showInformationAlert("Registration successful.");
				try {
					returnToPreviousScreen(null);
				} catch (ScreenException | StatefulException e) {
					e.printStackTrace();
				}
			} else {
				showErrorAlert("Failed to register the group guide.");
			}
		}
	}

	private boolean DetailsValidation(String firstName, String lastName, String email, String phone, String username,
			String password) {
		if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty()
				|| password.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Navigates back to the previous screen.
	 *
	 * @param event the action event triggering this method.
	 * @throws ScreenException   if there is an issue navigating back.
	 * @throws StatefulException if there is a state management issue during
	 *                           navigation.
	 */
	@FXML
	void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
		ScreenManager.getInstance().goToPreviousScreen(false, false);

	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * Shows all registration fields and the register button, making them visible
	 * and managed within the layout.
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
	 * Hides all fields except for the ID field and the check ID button, making them
	 * invisible and not managed within the layout.
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
	 * Loads traveler details into the registration form fields. Intended for use
	 * when a traveler's ID is found and their details are to be converted into a
	 * group guide registration.
	 *
	 * @param details the traveler details to load, expected in a specific order.
	 */
	private void loadUserDetails(Object[] details) {
		firstNameTxt.setText((String) details[0]);
		lastNameTxt.setText((String) details[1]);
		emailTxt.setText((String) details[2]);
		phoneTxt.setText((String) details[3]);
		UserNameTxt.setText((String) details[4]);
		passwordTxt.setText((String) details[5]);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

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
	 * Loads any necessary information before the screen is displayed. Currently not
	 * implemented.
	 *
	 * @param information optional information that might be needed for
	 *                    initialization.
	 */
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the screen title for this controller.
	 *
	 * @return the title of the screen.
	 */
	@Override
	public String getScreenTitle() {
		return "Group Guide Registration";
	}

}
