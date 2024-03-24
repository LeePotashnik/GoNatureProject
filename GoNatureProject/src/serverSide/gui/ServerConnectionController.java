package serverSide.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalTime;
import java.util.Arrays;

import common.controllers.AbstractScreen;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import serverSide.control.GoNatureServer;
import serverSide.control.GoNatureServer.ConnectedClient;

public class ServerConnectionController extends AbstractScreen {
	private static final String digitsOnly = "\\d+";

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button connectBtn, disconnectBtn, importBtn;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private TextField hostTxtField, portTxtField, databaseTxtField, rootTxtField;
	@FXML
	private TableColumn<ConnectedClient, String> clientIpColumn, clientStatusColumn, clientEnterColumn,
			clientExitColumn;
	@FXML
	private TableView<ConnectedClient> connectedClientsTable;
	@FXML
	private Label hostLbl, portLbl, databaseLbl, rootLbl, passwordLbl, titleLbl, statusLabel;
	@FXML
	private Pane pane;
	@FXML
	private PasswordField passwordTxtField;
	@FXML
	private TextArea consoleArea;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

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
				showErrorAlert(result);
				System.out.println(result);
			} else {
				showInformationAlert(result);
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
				importBtn.setDisable(false);

				connectedClientsTable.setItems(GoNatureServer.connectedToGUI);
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
			disconnectBtn.setDisable(true);
			pane.requestFocus();
			statusLabel.setText("Disconnected");
			statusLabel.setStyle("-fx-background-color: #ffe6e6; -fx-text-alignment: center;");
		}
	}

	@FXML
	/**
	 * This method is called after the user has clicked on the "Import Users"
	 * button. It calls a method in the server class to import users data from the
	 * Users Management System
	 * 
	 * @param event
	 */
	void importUsersFromExternalSystem(ActionEvent event) {
		int choise = showConfirmationAlert(
				"You are about to import users data from the Users Management System into GoNature database",
				Arrays.asList("Cancel", "Continue"));

		switch (choise) {
		case 1:
			event.consume();
			return;

		case 2:
			if (GoNatureServerUI.server.importUsersFromExternalSystem()) {
				showInformationAlert("The users data import succeed");
				importBtn.setDisable(true);
			} else {
				showInformationAlert("The users data import failed");
			}
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

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
			showErrorAlert("Not all clients are disconnected!");
			return false;
		} else {
			GoNatureServerUI.disconnectServer();
			showInformationAlert("Server is disconnected");
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
			showErrorAlert(error);
		return result;
	}

	/**
	 * This method gets a text field and makes it recoginze digits only
	 * 
	 * @param textField
	 */
	protected void setupTextFieldToDigitsOnly(TextField textField) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches(digitsOnly)) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	///////////////////////////////////
	/// JAVAFX FLOW CONTROL METHODS ///
	///////////////////////////////////

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

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		disconnectBtn.setVisible(false);
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		clientIpColumn.setResizable(false);
		clientStatusColumn.setResizable(false);
		clientEnterColumn.setResizable(false);
		clientExitColumn.setResizable(false);
		clientIpColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
		clientStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
		clientEnterColumn.setCellValueFactory(cellData -> {
			ConnectedClient client = cellData.getValue();
			LocalTime enterTime = client.getEnterTime();
			String timeString = (enterTime.getHour() < 10 ? "0" + enterTime.getHour() : enterTime.getHour()) + ":"
					+ (enterTime.getMinute() < 10 ? "0" + enterTime.getMinute() : enterTime.getMinute()) + ":"
					+ (enterTime.getSecond() < 10 ? "0" + enterTime.getSecond() : enterTime.getSecond());
			return new ReadOnlyStringWrapper(timeString);
		});
		clientExitColumn.setCellValueFactory(cellData -> {
			ConnectedClient client = cellData.getValue();
			LocalTime exitTime = client.getExitTime();
			String timeString = exitTime == null ? ""
					: (exitTime.getHour() < 10 ? "0" + exitTime.getHour() : exitTime.getHour()) + ":"
							+ (exitTime.getMinute() < 10 ? "0" + exitTime.getMinute() : exitTime.getMinute()) + ":"
							+ (exitTime.getSecond() < 10 ? "0" + exitTime.getSecond() : exitTime.getSecond());
			return new ReadOnlyStringWrapper(timeString);
		});

		// setting the empty-table labels
		connectedClientsTable.setPlaceholder(new Label("No connected clients"));

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

		setupTextFieldToDigitsOnly(portTxtField);

		importBtn.setDisable(true);

		// for later use
//		consoleArea.setEditable(false);
//		ConsoleOutput consoleOutput = new ConsoleOutput(consoleArea);
//		System.setOut(new PrintStream(consoleOutput, true));
//		System.setErr(new PrintStream(consoleOutput, true));
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
		else {
			showInformationAlert("Server is disconnected");
		}
	}

	@Override
	/**
	 * Irrelevant here.
	 */
	public void loadBefore(Object information) {
	}

	@Override
	/**
	 * Returns the screen's name
	 */
	public String getScreenTitle() {
		return "Server Connection";
	}
}