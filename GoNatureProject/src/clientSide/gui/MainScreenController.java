package clientSide.gui;

import java.util.Arrays;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainScreenController extends AbstractScreen {

	@FXML
	private Button Loginbtn;

	@FXML
	private Button fastReseravtion;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private Button registerNowbtn;

	/**
     * Handles the event when the "Register Now" button is clicked.
     * Shows a confirmation alert before proceeding with the registration process.
     * If the user confirms, navigates to the Traveller Registration Screen.
     *
     * @param event The ActionEvent triggered by clicking the button.
     */
	@FXML
	void registerNowbtn(ActionEvent event) {
		if ((showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"Attention: For GroupGuide registration please contact our service represantative.",
				Arrays.asList("Quit", "Continue registarion")) == 2)) {
			try {
				ScreenManager.getInstance().showScreen("TravellerRegistrationScreenController",
						"/clientSide/fxml/TravellerRegistrationScreen.fxml", false, false,
						StageSettings.defaultSettings("GoNature System - Main Screen"), null);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
	}

	/**
     * Handles the event when the "Fast Reservation" button is clicked.
     * This method is intended to provide fast reservation functionality.
     *
     * @param event The ActionEvent triggered by clicking the button.
     */
	@FXML
	void fastReservation(ActionEvent event) {

	}

	/**
     * Handles the event when the "Login" button is clicked.
     * This method is intended to navigate the user to the login screen.
     *
     * @param event The ActionEvent triggered by clicking the button.
     */
	@FXML
	void loginButtonClicked(ActionEvent event) {

	}

	 /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded. Initializes the GoNature logo.
     */
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));

	}

	
	/**
     * Prepares the screen before it is displayed. This method can be used to load
     * any necessary data or perform setup operations specific to the screen's requirements.
     *
     * @param information Optional information that might be required for initializing the screen.
     */
	@Override
	public void loadBefore(Object information) {
		// TODO Auto-generated method stub

	}

	
	 /**
     * Returns the title of the screen.
     *
     * @return A string representing the title of the Main Screen.
     */
	@Override
	public String getScreenTitle() {
		return "Main Screen";
	}

}
