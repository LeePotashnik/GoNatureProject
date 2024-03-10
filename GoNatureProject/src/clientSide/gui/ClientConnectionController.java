package clientSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import common.controllers.TemporaryRunner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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
	@FXML
	private Pane pane;

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
		hostTxtField.setStyle(setFieldToRegular());
		portTxtField.setStyle(setFieldToRegular());
		String showMessage = "";
		boolean valid = true;

		// validating the host
		if (host.trim().isEmpty() || (!host.equals("localhost") && !host
				.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))) {
			valid = false;
			hostTxtField.setStyle(setFieldToError());
			showMessage += "\nThe host field must be 'localhost' or a valid IPV4";
		}

		// validating the port
		if (port.trim().isEmpty() || !port.matches("\\d+")) {
			valid = false;
			portTxtField.setStyle(setFieldToError());
			showMessage += "\nYou must enter a valid digits-only port number";
		} else if (!(Integer.parseInt(port) >= 1024 && Integer.parseInt(port) <= 65535)) {
			valid = false;
			portTxtField.setStyle(setFieldToError());
			showMessage += "\nPort number must be in range (1024-65535)";
		}

		if (valid) {
			int portNumber = Integer.parseInt(port); // changing the string to int
			GoNatureClientUI.createClient(host, portNumber); // creating an instance of the PrototypeClient
			boolean result = GoNatureClientUI.client.connectClientToServer(host, portNumber);
			if (!result) { // if the client connection failed
				showErrorAlert(ScreenManager.getInstance().getStage(),
						"Establishing connection to server (host: " + host + ", port: " + port + ") failed");
				GoNatureClientUI.client = null;
			} else { // if the client connection succeed
				showInformationAlert(ScreenManager.getInstance().getStage(),
						"Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				System.out
						.println("Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				runClientSide(); // running the client side
			}
		} else {
			showErrorAlert(ScreenManager.getInstance().getStage(), "Errors:" + showMessage);
		}
	}

	/**
	 * This method is called after the client-server connection is established
	 * successfully. Starts the client-side Main Screen.
	 */
	public void runClientSide() {
		// DO NOT TOUCH PLEASE
		try {
			new TemporaryRunner().showScreen();
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
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

	@Override
	public void loadBefore(Object information) {
		String[] info = ((String)information).split(" ");
		hostTxtField.setText(info[0]);
		portTxtField.setText(info[1]);
		
	}

	@Override
	public String getScreenTitle() {
		return "Client Connection";
	}
	
	/// TEXT FIELDS TABS FLOW METHODS ///
	@FXML
	/**
	 * transfers the focus from hostTxtField to portTxtField
	 * @param event
	 */
    void hostTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			portTxtField.requestFocus();
		}
    }
	
	@FXML
	/**
	 * transfers the focus from portTxtField to the connect button
	 * @param event
	 */
    void portTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			connectBtn.requestFocus();
		}
    }
	
	@FXML
	/**
	 * transfers the focus from the button to the pane
	 * @param event
	 */
    void btnTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			pane.requestFocus();
		}
    }
		
	@FXML
    /**
     * sets the focus to the pane
     * @param event
     */
    void paneClicked(MouseEvent event) {
    	pane.requestFocus();
    }
}