package clientSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Controller class for the Client Connection JavaFX screen
 */
public class ClientConnectionController extends AbstractScreen {
	// properties for the image animation
	private final static int IMAGE_VIEW_COUNT = 3;
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT];
	private int currentIndex = 0;

	// validation constants
	private static final String localHost = "localhost";
	private static final String hostInput = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	private static final String digitsOnly = "\\d+";

	//////////////////////////
	/// JAVA FX COMPONENTS ///
	//////////////////////////

	@FXML
	private Button connectBtn;
	@FXML
	private ImageView goNatureLogo, image1, image2, image3;
	@FXML
	private HBox hbox;
	@FXML
	private TextField hostTxtField, portTxtField;
	@FXML
	private Pane pane;
	@FXML
	private Label titleLbl, instrLbl;
	@FXML
	private Rectangle rectangle;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

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
		if (host.trim().isEmpty() || (!host.equals(localHost) && !host.matches(hostInput))) {
			valid = false;
			hostTxtField.setStyle(setFieldToError());
			showMessage += "\nThe host field must be 'localhost' or a valid IPV4";
		}

		// validating the port
		if (port.trim().isEmpty() || !port.matches(digitsOnly)) {
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
				showErrorAlert("Establishing connection to server (host: " + host + ", port: " + port + ") failed");
				GoNatureClientUI.client = null;
			} else { // if the client connection succeed
				System.out
						.println("Establishing connection to server (host: " + host + ", port: " + port + ") succeed");
				runClientSide(); // running the client side
			}
		} else {
			showErrorAlert("Errors:" + showMessage);
		}
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
	 * transfers the focus from portTxtField to the connect button
	 * 
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
	 * 
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
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method is called after the client-server connection is established
	 * successfully. Starts the client-side Main Screen.
	 */
	public void runClientSide() {
		// showing the main screen
		try {
			ScreenManager.getInstance().showScreen("MainScreenController", "/clientSide/fxml/MainScreen.fxml", false,
					false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method starts the parks images slide show using fade transitions
	 */
	private void startSlideshow() {
		// Create a runnable task for changing images
		FadeTransition fade1 = new FadeTransition(Duration.millis(2000), image1);
		fade1.setFromValue(0.0);
		fade1.setToValue(1.0);
		fade1.play();

		FadeTransition fade2 = new FadeTransition(Duration.millis(2000), image2);
		fade2.setFromValue(0.0);
		fade2.setToValue(1.0);
		fade2.play();

		FadeTransition fade3 = new FadeTransition(Duration.millis(2000), image3);
		fade3.setFromValue(0.0);
		fade3.setToValue(1.0);
		fade3.play();

		Runnable changeImagesTask = () -> {
			if (currentIndex >= imagePaths.size()) {
				currentIndex = 0; // Reset index to loop
			}

			for (int i = 0; i < IMAGE_VIEW_COUNT; i++) {
				final int imageIndex = (currentIndex + i) % imagePaths.size();
				ImageView imageView = imageViews[i];
				Image newImage = new Image(imagePaths.get(imageIndex));

				// Apply fade-out transition on image change
				FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), imageView);
				fadeOut.setFromValue(1.0);
				fadeOut.setToValue(0.0);
				fadeOut.setDelay(Duration.millis(i * 1000));
				fadeOut.setOnFinished(event -> {
					imageView.setImage(newImage);
					FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), imageView);
					fadeIn.setFromValue(0.0);
					fadeIn.setToValue(1.0);
					fadeIn.play();
				});
				fadeOut.play();
			}
			currentIndex += IMAGE_VIEW_COUNT; // Move to the next set of images
		};

		// Schedule the task to run periodically
		javafx.animation.Timeline timeline = new javafx.animation.Timeline(
				new javafx.animation.KeyFrame(Duration.seconds(5), // Change images every 5 seconds
						event -> changeImagesTask.run()));
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();
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

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		// setting the image view array
		imageViews[0] = image1;
		imageViews[1] = image2;
		imageViews[2] = image3;

		// setting 3 first images
		imageViews[0].setImage(new Image(imagePaths.get(0)));
		imageViews[1].setImage(new Image(imagePaths.get(1)));
		imageViews[2].setImage(new Image(imagePaths.get(2)));
		currentIndex = 3;

		startSlideshow();

		// centering the labels
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		instrLbl.setAlignment(Pos.CENTER);
		instrLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		portTxtField.setPromptText("Enter port number");
		hostTxtField.setPromptText("Enter host address");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));

		setupTextFieldToDigitsOnly(portTxtField);

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	/**
	 * TEMPORARY - SHOULD BE REMOVED
	 */
	public void loadBefore(Object information) {
		String[] info = ((String) information).split(" ");
		hostTxtField.setText(info[0]);
		portTxtField.setText(info[1]);

	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Client Connection";
	}
}