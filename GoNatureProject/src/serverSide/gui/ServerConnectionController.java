package serverSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

public class ServerConnectionController extends AbstractScreen {

	@FXML
	private Button connectBtn, disconnectBtn;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private TextField portTxtField;

	@FXML
	/**
	 * This method is called after the user clicked on the "Connect to Server"
	 * button on the server GUI.
	 * 
	 * @param event an event of clicking to the Connect to Server button
	 */
	void connectToServer(ActionEvent event) {
		String portNumber = portTxtField.getText();
		portTxtField.setStyle(setTextFieldToRegular());

		// validating the port number
		if (portNumber.trim().isEmpty() || !portNumber.matches("\\d+")) {
			portTxtField.setStyle(setTextFieldToError());
			showErrorAlert(ScreenController.getInstance().getStage(), "You must enter a valid digits-only port number");
		} else if (!(Integer.parseInt(portNumber) >= 1024 && Integer.parseInt(portNumber) <= 65535)) {
			portTxtField.setStyle(setTextFieldToError());
			showErrorAlert(ScreenController.getInstance().getStage(), "Port number must be in range (1024-65535)");
		} else { // if the port number is valid
			String result = GoNatureServerUI.runServer(portNumber);
			if (result.contains("Error")) {
				showErrorAlert(ScreenController.getInstance().getStage(), result);
				System.out.println(result);
			} else {
				showInformationAlert(ScreenController.getInstance().getStage(), result);
				System.out.println(result);
				portTxtField.setDisable(true);
				connectBtn.setVisible(false);
				disconnectBtn.setVisible(true);
			}
		}
	}

	@FXML
	/**
	 * This method is called after the user clicked on the "Disconnect from Server"
	 * button on the server GUI.
	 * 
	 * @param event an event of clicking to the Disconnect from Server button
	 */
	void disconnectFromServer(ActionEvent event) {
		if (disconnect()) {
			showInformationAlert(ScreenController.getInstance().getStage(), "Server is disconnected");
			disconnectBtn.setDisable(true);
		}
	}

	/**
	 * This method is called when the server GUI gets a request to disconnect. Won't
	 * disconnect until all the clients are also disconnected.
	 * 
	 * @return true if the disconnection succeed, false otherwise.
	 */
	private boolean disconnect() {
		if (GoNatureServerUI.server == null)
			return true;
		if (!GoNatureServerUI.server.areAllClientsDisconnected()) {
			showErrorAlert(ScreenController.getInstance().getStage(), "Not all clients are disconnected!");
			return false;
		} else {
			GoNatureServerUI.disconnectServer();
			return true;
		}
	}

	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		disconnectBtn.setVisible(false);
		portTxtField.setPromptText("Enter port number here");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
	}

	@Override
	/**
	 * This is an override for the method from AbstractScreen
	 */
	public void handleCloseRequest(WindowEvent event) {
		boolean disconnectionResult = disconnect();
		if (!disconnectionResult)
			event.consume();
	}

}
