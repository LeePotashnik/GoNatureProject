package clientSide.gui;

import clientSide.control.LoginController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenManager;
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
import javafx.util.Pair;

public class BookingMangingLoginScreenConrtroller extends AbstractScreen {

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
			showErrorAlert(ScreenManager.getInstance().getStage(), "Please enter ID number");
			IDNumberTextField.setStyle(setFieldToError());
		}
		// validating IDNumberTextField contains only numbers:
		else if (!idNumber.matches("\\d+")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"Please make sure that the ID number contains only numbers");
			IDNumberTextField.setStyle(setFieldToError());
		}
		// validating IDNumberTextField contains exactly 9 numbers:
		else if (idNumber.matches("\\d{1,8}")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"The ID you entered has less than 9 digits. Please ensure your ID is exactly 9 digits long");
			IDNumberTextField.setStyle(setFieldToError());
		} else if (idNumber.matches("\\d{10,}")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"The ID you entered has more than 9 digits. Please ensure your ID is exactly 9 digits long");
			IDNumberTextField.setStyle(setFieldToError());
		} else {
			String identity = LoginController.checkIdInVisitorUsers(idNumber);
			Pair<String, String> pair = new Pair<>(idNumber, identity);
			///////////////////////////////////////
			showInformationAlert(ScreenManager.getInstance().getStage(), "identity is: " + identity);
			/////////////////////////////////////////
			// ScreenManager.getInstance().showScreen("BookingScreenController","/clientSide/fxml/BookingScreen.fxml",
			// false, false,StageSettings.defaultSettings("GoNature System - Reservations"),
			///////////////////////////////////////// pair);
		}
	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) {
		// ScreenManager.getInstance().showScreen("BookingScreenController","/clientSide/fxml/BookingScreen.fxml",
		// false, false,StageSettings.defaultSettings("GoNature System - Reservations"),
		// visitorUser);
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
