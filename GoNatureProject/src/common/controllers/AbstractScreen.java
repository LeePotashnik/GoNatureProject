package common.controllers;

import java.util.Optional;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.MessageType;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The AbstractScreen is the abstract class of each screen class in the
 * application. Includes multiple methods that are common for all screens,
 * including Alert showing, CSS and styles methods, etc. A screen can also be
 * Stateful (implementing the Stateful interface), if in some cases it has to
 * save its current data and state, for future restoring and use.
 */
public abstract class AbstractScreen {
	/**
	 * This method is called after the FXML is invoked
	 */
	public abstract void initialize();
	
	/**
	 * This method is called if a screen has to get information before it is shown.
	 * @param information an object with the specific information for the screen
	 */
	public abstract void loadBefore(Object information);
	
	public abstract String getScreenTitle();

	/**
	 * This method is activated after the X is clicked on the window. The default is
	 * to show a Confirmation Alert with "Yes" and "No" options for the user to
	 * choose. "Yes" will check if the client is connected to the server, disconnect
	 * it if necessary and close the window. "No" will "consume" the request,
	 * meaning it will cancel the closing request and will keep the window open.
	 * This method can be overriden for different implementations.
	 * 
	 * @param event the event of clicking on the X of the window
	 */
	public void handleCloseRequest(WindowEvent event) {
		// showing a "Yes" and "No" decision alert
		int decision = showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"Are you sure you want to leave?", "Yes", "No");
		if (decision == 2) // if the user clicked on "No"
			event.consume();
		else { // if the user clicked on "Yes"
			if (GoNatureClientUI.client == null) { // if the client is not connected ( = null)
				System.out.println("Client exited the application"); // just exit
			} else { // if the client is connected to the server
				// creating a communication request for disconnecting from the server port
				Communication message = new Communication(CommunicationType.CLIENT_SERVER_MESSAGE);
				message.setMessageType(MessageType.DISCONNECT);
				GoNatureClientUI.client.accept(message);
			}
		}
	}

	/**
	 * This method uses Alert to show an error alert message on the screen
	 * 
	 * @param stage   the application's primary stage
	 * @param content the content to be displayed inside the error alert
	 */
	public final void showErrorAlert(Stage stage, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(stage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle("-fx-background-color: #ffdddd; -fx-font-size: 14px;");

		alert.showAndWait();
	}

	/**
	 * This method uses Alert to show an information alert message on the screen
	 * 
	 * @param stage   the application's primary stage
	 * @param content the content to be displayed inside the information alert
	 */
	public final void showInformationAlert(Stage stage, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(stage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle("-fx-background-color: #ddfeff; -fx-font-size: 14px;");

		alert.showAndWait();
	}

	/**
	 * This method uses Alert to show a confirmation alert on the screen. This alert
	 * includes two button for decision to be taken by the user.
	 * 
	 * @param stage   the application's primary stage
	 * @param content the content to be displayed inside the confirmation alert
	 * @param btn1    the text to be shown inside the first button
	 * @param btn2    the text to be shown inside the second button
	 * @return 1 if the first button clicked, 2 if the second button clicked
	 */
	public final int showConfirmationAlert(Stage stage, String content, String btn1, String btn2) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle("-fx-font-size: 14px;");

		// setting the buttons of the alert
		ButtonType button1 = new ButtonType(btn1), button2 = new ButtonType(btn2);
		alert.getButtonTypes().setAll(button1, button2);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == button1)
			return 1; // button 1 clicked
		else
			return 2; // button 2 clicked
	}

	/**
	 * Setting a CSS style for indicating an error in a text field input
	 * 
	 * @return the string to be set inside setStyle() method of JavaFX
	 */
	public String setTextFieldToError() {
		return "-fx-border-color: red; -fx-border-width: 0.8px; -fx-border-radius: 2px; -fx-background-color: #ffe6e6;";
	}

	/**
	 * Setting a CSS style for restoring the original state of the text field
	 * 
	 * @return the string to be set inside setStyle() method of JavaFX
	 */
	public String setTextFieldToRegular() {
		return "";
	}
}
