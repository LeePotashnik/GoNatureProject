package serverSide.gui;

import java.io.IOException;

import common.controllers.AbstractScreenController;
import common.controllers.ScreenChanger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ServerConnectionController extends AbstractScreenController {

	@FXML
	private Button connectBtn, disconnectBtn;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private TextField portTxtField;

	@FXML
	void connectToServer(ActionEvent event) {
		String portNumber = portTxtField.getText();
		portTxtField.setStyle(setTextFieldToRegular());

		// validating the port number
		if (portNumber.trim().isEmpty() || !portNumber.matches("\\d+")) {
			portTxtField.setStyle(setTextFieldToError());
			showErrorAlert(ScreenChanger.primaryStage, "You must enter a valid digits-only port number");
		} else if (!(Integer.parseInt(portNumber) >= 1024 && Integer.parseInt(portNumber) <= 65535)) {
			portTxtField.setStyle(setTextFieldToError());
			showErrorAlert(ScreenChanger.primaryStage, "Port number must be in range (1024-65535)");
		} else { // if the port number is valid
			String result = GoNatureServerUI.runServer(portNumber);
			if (result.contains("Error")) {
				showErrorAlert(ScreenChanger.primaryStage, result);
				System.out.println(result);
			} else {
				showInformationAlert(ScreenChanger.primaryStage, result);
				System.out.println(result);
				portTxtField.setDisable(true);
				connectBtn.setVisible(false);
				disconnectBtn.setVisible(true);
			}
		}
	}

	@FXML
	void disconnectFromServer(ActionEvent event) {
		if (disconnect()) {
			showInformationAlert(ScreenChanger.primaryStage, "Server is disconnected");
			disconnectBtn.setDisable(true);
		}
	}

	private boolean disconnect() {
		if (GoNatureServerUI.server == null)
			return true;
		if (!GoNatureServerUI.server.areAllClientsDisconnected()) {
			showErrorAlert(ScreenChanger.primaryStage, "Not all clients are disconnected!");
			return false;
		} else {
			GoNatureServerUI.disconnectServer();
			return true;
		}
	}

	// This method initializes the JavaFX components
	@FXML
	public void initialize() {
		disconnectBtn.setVisible(false);
		portTxtField.setPromptText("Enter port number here");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
	}

	// Starts the applicaiton javafx screen
	public void start(Stage primaryStage) {
		Pane root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/serverSide/fxml/ServerConnection.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		root.setStyle("-fx-background-color: white;");
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("GoNature System - Server Connection");
		primaryStage.show();
		root.requestFocus();
		primaryStage.setOnCloseRequest(event -> { // when closing the window
			boolean disconnectionResult = disconnect();
			if (!disconnectionResult)
				event.consume();
		});
	}

}
