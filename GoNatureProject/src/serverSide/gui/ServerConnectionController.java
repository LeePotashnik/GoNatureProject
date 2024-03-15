package serverSide.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import common.controllers.AbstractScreen;
import common.controllers.ScreenManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

public class ServerConnectionController extends AbstractScreen {

	@FXML
	private Button connectBtn, disconnectBtn, clearBtn;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private TextField hostTxtField, portTxtField, databaseTxtField, rootTxtField;
	@FXML
	private TableColumn<String, String> clientIpColumn, clientStatusColumn;
	@FXML
	private TableView<String> connectedClientsTable;
	@FXML
	private Label hostLbl, portLbl, databaseLbl, rootLbl, passwordLbl, titleLbl, statusLabel;
	@FXML
	private Pane pane;
	@FXML
	private PasswordField passwordTxtField;
	@FXML
	private TextArea consoleArea;

	@FXML
	/**
	 * This method is called after the user clicked on the "Connect to Server"
	 * button on the server GUI. Validates the input and connects to the server
	 * 
	 * @param event an event of clicking to the Connect to Server button
	 */
	void connectToServer(ActionEvent event) {
		if (validate()) {
			String result = GoNatureServerUI.runServer(Integer.parseInt(portTxtField.getText()),
					databaseTxtField.getText(), rootTxtField.getText(), passwordTxtField.getText());
			if (result.contains("Error")) {
				showErrorAlert(ScreenManager.getInstance().getStage(), result);
				System.out.println(result);
			} else {
				showInformationAlert(ScreenManager.getInstance().getStage(), result);
				System.out.println(result);
				hostTxtField.setDisable(true);
				portTxtField.setDisable(true);
				databaseTxtField.setDisable(true);
				rootTxtField.setDisable(true);
				passwordTxtField.setDisable(true);
				statusLabel.setText("Connected");
				statusLabel.setStyle("-fx-background-color: #B2E89D; -fx-text-alignment: center;");

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
			showInformationAlert(ScreenManager.getInstance().getStage(), "Server is disconnected");
			disconnectBtn.setDisable(true);
			pane.requestFocus();
			statusLabel.setText("Disconnected");
			statusLabel.setStyle("-fx-background-color: #ffe6e6; -fx-text-alignment: center;");
		}
	}

	@FXML
	/**
	 * This method is called after the Clear button is clicked. Clears the server
	 * console text area.
	 * 
	 * @param event
	 */
	void clearBtnClicked(ActionEvent event) {
		consoleArea.setText("");
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
			showErrorAlert(ScreenManager.getInstance().getStage(), "Not all clients are disconnected!");
			return false;
		} else {
			GoNatureServerUI.disconnectServer();
			return true;
		}
	}

	/**
	 * This method validaes the user input from the text fields
	 * 
	 * @return true if the input is valid, false if not
	 */
	private boolean validate() {
		boolean result = true;
		String error = "";
		hostTxtField.setStyle(setFieldToRegular());
		portTxtField.setStyle(setFieldToRegular());

		// validating host
		String hostAddress = hostTxtField.getText();
		// checking if the host address is a valid IPV4 address, or 'localhost'
		if (hostAddress.trim().isEmpty() || (!hostAddress.matches(
				"^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
				&& !hostAddress.equals("localhost"))) {
			hostTxtField.setStyle(setFieldToError());
			result = false;
			error += "You must enter a valid IPV4 address, or 'localhost'\n";
		}
		// validating port
		String portNumber = portTxtField.getText();
		// checking if the port number is a digit-only string
		if (portNumber.trim().isEmpty() || !portNumber.matches("\\d+")) {
			portTxtField.setStyle(setFieldToError());
			result = false;
			error += "You must enter a valid digits-only port number\n";
		}
		// checking if the port number is in the correct range
		if (portNumber.matches("\\d+")
				&& !(Integer.parseInt(portNumber) >= 1024 && Integer.parseInt(portNumber) <= 65535)) {
			portTxtField.setStyle(setFieldToError());
			result = false;
			error += "Port number must be in range (1024-65535)";
		}
		if (!result)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
		return result;
	}

	/// TEXT FIELDS TABS FLOW METHODS ///
	@FXML
	/**
	 * transfers the focus from hostTxtField to portTxtField
	 * 
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
	 * transfers the focus from portTxtField to databaseTxtField
	 * 
	 * @param event
	 */
	void portTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			databaseTxtField.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from databaseTxtField to rootTxtField
	 * 
	 * @param event
	 */
	void databaseTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			rootTxtField.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from rootTxtField to passwordTxtField
	 * 
	 * @param event
	 */
	void rootTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			passwordTxtField.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from passwordTxtField to connectBtn
	 * 
	 * @param event
	 */
	void passwordTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			connectBtn.requestFocus();
		}
	}

	@FXML
	/**
	 * sets the focus to the pane
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	/**
	 * ignores any key pressing on the root pane
	 * 
	 * @param event
	 */
	void paneKeyPressed(KeyEvent event) {
		event.consume();
	}

	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		disconnectBtn.setVisible(false);
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		// setting alignment of the labels to right
		hostLbl.setStyle("-fx-alignment: center-right;");
		portLbl.setStyle("-fx-alignment: center-right;");
		databaseLbl.setStyle("-fx-alignment: center-right;");
		rootLbl.setStyle("-fx-alignment: center-right;");
		passwordLbl.setStyle("-fx-alignment: center-right;");
		databaseTxtField.setText("jdbc:mysql://localhost/go_nature?serverTimezone=Asia/Jerusalem");

		statusLabel.setText("Disconnected");
		statusLabel.setStyle("-fx-background-color: #ffe6e6; -fx-text-alignment: center;");

		consoleArea.setEditable(false);
		ConsoleOutput consoleOutput = new ConsoleOutput(consoleArea);
		System.setOut(new PrintStream(consoleOutput, true));
		System.setErr(new PrintStream(consoleOutput, true));
	}

	public static class ConsoleOutput extends OutputStream {
		private TextArea output;

		public ConsoleOutput(TextArea ta) {
			this.output = ta;
		}

		@Override
		public void write(int i) throws IOException {
			// Append the character to the TextArea (in the JavaFX Application Thread)
			javafx.application.Platform.runLater(() -> output.appendText(String.valueOf((char) i)));
		}

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

	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return "Server Connection";
	}

}
