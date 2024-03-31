package clientSide.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import clientSide.control.RegistrationController;
import clientSide.entities.SystemUser;
import common.communication.Communication;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.application.Platform;
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
import javafx.stage.WindowEvent;
import javafx.util.Pair;

public class GroupGuideRegistrationScreenController extends AbstractScreen {
	private final String DIGITS_ONLY_REGEX = "\\d+";
	private RegistrationController registerController = (RegistrationController) RegistrationController.getInstance();
	private SystemUser systemUser;

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

		// reseting the text fields
		firstNameTxt.setText("");
		lastNameTxt.setText("");
		emailTxt.setText("");
		phoneTxt.setText("");
		UserNameTxt.setText("");
		passwordTxt.setText("");

		if (id.isEmpty() || !(id.matches(DIGITS_ONLY_REGEX) && id.length() == 9)) { // check if not empty and 9 digits
			idTxt.setStyle(setFieldToError());
			showErrorAlert("- ID number is not valid.");
		} else {
			AtomicBoolean haveAccess = new AtomicBoolean();
			new Thread(() -> {
				Pair<SystemUser, Boolean> pair = registerController.getUserDetails(id);
				systemUser = pair.getKey();
				final boolean canHaveAccess = pair.getValue();
				haveAccess.set(canHaveAccess);

				Platform.runLater(() -> {
					if (systemUser == null) { // if this id does not exist
						idTxt.setStyle(setFieldToError());
						showErrorAlert("This id number does not exist in the system");
					} else { // if exists
						if (!haveAccess.get()) { // user is locked and can't be accessed
							idTxt.setStyle(setFieldToError());
							showErrorAlert("This id number can't be registered at the moment");
						} else { // can be accessed
							// checking if already exist in the group guide table
							boolean isInGroupGuideTable = registerController.checkGroupGuideExistence(id);
							if (isInGroupGuideTable) {
								showErrorAlert("This group guide is already registered to the system.");
								registerbtn.setDisable(true);
								checkIDbtn.setDisable(false);
								idTxt.setDisable(false);
								idTxt.setStyle(setFieldToError());
								registerController.userDeleteFromTable(id, Communication.systemUser);
							} else {
								showInformationAlert("User Details loaded for conversion.");
								idTxt.setStyle(setFieldToRegular());
								idTxt.setDisable(true);
								checkIDbtn.setDisable(true);
								registerbtn.setDisable(false);
								loadUserDetails(systemUser);
							}
						}
					}
				});
			}).start();
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
		String id = idTxt.getText();

		// checking if somehow in the traveller's table if so, removes him
		registerController.checkIdExistenceAndDeleteFromTravellerTable(id);
		registerController.userDeleteFromTable(id, Communication.systemUser);

		boolean insertResult = registerController.groupGuideInsertToDB(systemUser);
		if (insertResult) {
			showInformationAlert("Registration process succeed");
			// reseting the text fields
			idTxt.setText("");
			firstNameTxt.setText("");
			lastNameTxt.setText("");
			emailTxt.setText("");
			phoneTxt.setText("");
			UserNameTxt.setText("");
			passwordTxt.setText("");
			registerbtn.setDisable(true);
			checkIDbtn.setDisable(false);
			idTxt.setDisable(false);
		} else {
			showInformationAlert("Registration process failed");
			// reseting the text fields
			idTxt.setText("");
			firstNameTxt.setText("");
			lastNameTxt.setText("");
			emailTxt.setText("");
			phoneTxt.setText("");
			UserNameTxt.setText("");
			passwordTxt.setText("");
			registerbtn.setDisable(true);
			checkIDbtn.setDisable(false);
			idTxt.setDisable(false);
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
	void returnToPreviousScreen(ActionEvent event) {
		new Thread(() -> { // deleting the locking, if this representative had access
			if (idTxt.getText() != null && !idTxt.getText().isEmpty() && checkIDbtn.isDisable()
					&& !registerbtn.isDisable()) {
				registerController.unlockUser(idTxt.getText());
			}
		}).start();

		try {
			ScreenManager.getInstance().goToPreviousScreen(true, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}

	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

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
	 * Loads guides details into the registration form fields. Intended for use when
	 * a user ID is found and their details are to be converted into a group guide
	 * registration.
	 *
	 * @param user the systemUser's object of the group guide
	 */
	private void loadUserDetails(SystemUser user) {
		firstNameTxt.setText(user.getFirstName());
		lastNameTxt.setText(user.getLastName());
		emailTxt.setText(user.getEmailAddress());
		phoneTxt.setText(user.getPhoneNumber());
		UserNameTxt.setText(user.getUsername());
		passwordTxt.setText(user.getPassword());
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
	/**
	 * Handles the close request when the user clicks on the X
	 */
	public void handleCloseRequest(WindowEvent event) {
		new Thread(() -> { // deleting the locking, if this representative had access
			if (idTxt.getText() != null && !idTxt.getText().isEmpty() && checkIDbtn.isDisable()
					&& !registerbtn.isDisable()) {
				registerController.unlockUser(idTxt.getText());
			}
		}).start();

		super.handleCloseRequest(event);
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
