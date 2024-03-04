package clientSide.gui;

import java.util.Arrays;

import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.MessageType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
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

// CONTROLLER CLASS FOR THE CLIENT CONNECTION JAVA FX SCREEN
public class ClientConnectionController extends AbstractScreenController {

	// JAVA FX COMPONENTS
	@FXML
	private Button connectBtn;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private TextField hostTxtField, portTxtField;

	// EVENT METHODS
	// this method is called after clicking on the "Connect to Server" button
	// Validates the entered host and port and tries to establish a connection
	// with the server. If succeed, runs the client-side (Main Screen)
	@FXML
	void connectToServer(ActionEvent event) {
		String host = hostTxtField.getText();
		String port = portTxtField.getText();
		hostTxtField.setStyle(setTextFieldToRegular());
		portTxtField.setStyle(setTextFieldToRegular());
		String showMessage = "";
		boolean valid = true;

		// validating the host
		if (host.trim().isEmpty() || (!host.equals("localhost") && !host
				.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))) {
			valid = false;
			hostTxtField.setStyle(setTextFieldToError());
			showMessage += "\nThe host field must be 'localhost' or a valid IPV4";
		}

		// validating the port
		if (port.trim().isEmpty() || !port.matches("\\d+")) {
			valid = false;
			portTxtField.setStyle(setTextFieldToError());
			showMessage += "\nYou must enter a valid digits-only port number";
		} else if (!(Integer.parseInt(port) >= 1024 && Integer.parseInt(port) <= 65535)) {
			valid = false;
			portTxtField.setStyle(setTextFieldToError());
			showMessage += "\nPort number must be in range (1024-65535)";
		}

		if (valid) {
			int portNumber = Integer.parseInt(port); // changing the string to int
			GoNatureClientUI.createClient(host, portNumber); // creating an instance of the PrototypeClient
			boolean result = GoNatureClientUI.client.connectClientToServer(host, portNumber);
			if (!result) { // if the client connection failed
				showErrorAlert(ScreenChanger.primaryStage,
						"Establishing connection to server (host: " + host + ", port: " + port + ") failed");
				GoNatureClientUI.client = null;
			} else { // if the client connection succeed
				showInformationAlert(ScreenChanger.primaryStage,
						"Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				System.out
						.println("Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				runClientSide(event);
			}
		} else {
			showErrorAlert(ScreenChanger.primaryStage, "Errors:" + showMessage);
		}
	}

	// This method is called after the client-server connection is established
	// successfully. Starts the client-side Main Screen.
	public void runClientSide(ActionEvent event) {
		System.out.println("Trying to print acadia_park_active_booking table:");
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request.setSelectColumns(Arrays.asList("bookingId", "firstName", "finalPrice"));
		request.setTables(Arrays.asList("acadia_park_active_booking"));
		GoNatureClientUI.client.accept(request);
		for (Object[] o : request.getResultList()) {
			JustChecking just = new JustChecking((String) o[0], (String) o[1], (Integer) o[2]);
			System.out.println(just);
		}
		
		Communication request2 = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request2.setQueryType(QueryType.UPDATE);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		request2.setTables(Arrays.asList("acadia_park_active_booking"));
		request2.setColumnsAndValues(Arrays.asList("numberOfVisitors"), Arrays.asList(10));
		request2.setWhereConditions(Arrays.asList("bookingId"), Arrays.asList("="), Arrays.asList("4552040587"));
		GoNatureClientUI.client.accept(request2);
		try {
			System.out.println(request2.combineQuery());
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(request2.getQueryResult());
		

		// starting the MainScreen
	}

	// FXML / JAVA FX METHODS
	// This method initializes the JavaFX components
	@FXML
	public void initialize() {
		portTxtField.setPromptText("Enter port number");
		hostTxtField.setPromptText("Enter host address");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
	}

	// Starts the applicaiton javafx screens flow
	public void start(Stage primaryStage) throws Exception {
		Pane root = FXMLLoader.load(getClass().getResource("/clientSide/fxml/ClientConnection.fxml"));
		root.setStyle("-fx-background-color: white;");
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("GoNature System - Client Connection");
		root.requestFocus();
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> { // when closing the window
			// showing a "Yes" and "No" decision alert
			int decision = showConfirmationAlert(primaryStage, "Are you sure you want to leave?", "Yes", "No");
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
		});
	}
}