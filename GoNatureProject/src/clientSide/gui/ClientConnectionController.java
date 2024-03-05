package clientSide.gui;

import java.time.LocalTime;
import java.util.Arrays;

import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller class for the Client Connection JavaFX screen
 */
public class ClientConnectionController extends AbstractScreen {

	// JAVA FX COMPONENTS
	@FXML
	private Button connectBtn;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private TextField hostTxtField, portTxtField;

	///// --- EVENT METHODS --- /////
	@FXML
	/**
	 * this method is called after clicking on the "Connect to Server" button.
	 * Validates the entered host and port and tries to establish a connection with
	 * the server. If succeed, runs the client-side (Main Screen)
	 * 
	 * @param event the event of clicking on the "Connect to Server" button
	 */
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
				showErrorAlert(ScreenController.getInstance().getStage(),
						"Establishing connection to server (host: " + host + ", port: " + port + ") failed");
				GoNatureClientUI.client = null;
			} else { // if the client connection succeed
				showInformationAlert(ScreenController.getInstance().getStage(),
						"Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				System.out
						.println("Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				runClientSide(); // running the client side
			}
		} else {
			showErrorAlert(ScreenController.getInstance().getStage(), "Errors:" + showMessage);
		}
	}

	/**
	 * This method is called after the client-server connection is established
	 * successfully. Starts the client-side Main Screen.
	 */
	public void runClientSide() {
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
		request2.setColumnsAndValues(Arrays.asList("numberOfVisitors", "parkEntryTime"), Arrays.asList(8, LocalTime.of(10,30,0)));
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

	///// --- FXML / JAVA FX METHODS --- /////
	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		portTxtField.setPromptText("Enter port number");
		hostTxtField.setPromptText("Enter host address");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
	}
}