package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.RegistrationController;
import common.communication.Communication;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GroupGuideRegistrationScreenController extends AbstractScreen {
	private final String DIGITS_ONLY_REGEX = "\\d+";
	private RegistrationController registerController = (RegistrationController) RegistrationController.getInstance();

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton, registerbtn, checkIDbtn;
	@FXML
	private TextField UserNameTxt, emailTxt, firstNameTxt, idTxt, lastNameTxt, phoneTxt;
	@FXML
	private Label name, emaillbl, passwordlbl, phonelbl, userNamelbl, titleLbl;
	@FXML
	private PasswordField passwordTxt;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Pane pane;
	@FXML
	private VBox vbox;
	@FXML
	private HBox hbox1, hbox2, hbox3, hbox4, hbox5;

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
				if (RegistrationController.getInstance().checkGroupGuideExistence(id)) {
					idTxt.setStyle(setFieldToError());
					showErrorAlert("This ID is already registered as a Group Guide");
				} else {
					idTxt.setStyle(setFieldToError());
					showErrorAlert("This ID does not exists in our System.");
				}
			} else {
				showInformationAlert("User Details loaded for conversion.");
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
	 * RegistrationController to convert an existing system user to a group guide.
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

		if (DetailsValidation(firstName, lastName, email, phone, username, password)) {
			registerController.checkIdExistenceAndDeleteFromTravellerTable(id);

			// if the the details are valid
			// in the aspect of not empty
			boolean deleteUserSuccess = registerController.userDeleteFromTable(id, Communication.systemUser);
			// if the fields with correct format want to delete this user with this id from the 'system_user'
			// table and insert him to the group_guide table
			if (!deleteUserSuccess) { // delete from 'system_users'
				showErrorAlert("Failed to register the group guide.");
				return;
			}

			boolean insertSuccess = registerController.groupGuideInsertToDB(id, firstName, lastName, email, phone,
					username, password);
			if (insertSuccess) { // insert to 'group_guide'
				showInformationAlert("Registration Successful.");
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

	@FXML
	/**
	 * If the pane clicked, all focus from the GUI components will be disabled
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
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
		ScreenManager.getInstance().goToPreviousScreen(true, false);

	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method validates the entered details by the user
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param phone
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean DetailsValidation(String firstName, String lastName, String email, String phone, String username,
			String password) {
		if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty()
				|| password.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Shows all registration fields and the register button, making them enabled
	 */
	private void showAllFieldsAndRegisterButton() {
		hbox1.setDisable(true);
		hbox2.setDisable(false);
		hbox3.setDisable(false);
		hbox4.setDisable(false);
		hbox5.setDisable(false);
		registerbtn.setDisable(false);
	}

	/**
	 * Hides all fields except for the ID field and the check ID button, making them
	 * disabled
	 */
	private void hideAllExceptIDAndCheckID() {
		hbox1.setDisable(false);
		hbox2.setDisable(true);
		hbox3.setDisable(true);
		hbox4.setDisable(true);
		hbox5.setDisable(true);
		registerbtn.setDisable(true);
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
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
		hideAllExceptIDAndCheckID();

		// setting the application's background
		setApplicationBackground(pane);

	}

	@Override
	public void loadBefore(Object information) {
		// irrelevant here
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
