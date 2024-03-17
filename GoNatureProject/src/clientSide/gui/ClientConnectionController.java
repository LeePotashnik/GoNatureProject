package clientSide.gui;

import java.util.ArrayList;

import common.controllers.AbstractScreen;

import common.controllers.ScreenManager;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Controller class for the Client Connection JavaFX screen
 */
public class ClientConnectionController extends AbstractScreen {
	private final static int IMAGE_VIEW_COUNT = 3; // Display 3 images at a time
	private final ArrayList<String> imagePaths = new ArrayList<>(); // List to hold all your image paths
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	// JAVA FX COMPONENTS
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

//		startSlideshow();

//		// DO NOT TOUCH PLEASE
//		try {
//			new TemporaryRunner().showScreen();
//		} catch (StatefulException | ScreenException e) {
//			e.printStackTrace();
//		}
	}

	private void startSlideshow() {
		// Create a runnable task for changing images
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

	private void setRectangleStroke() {
//		LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true,
//                javafx.scene.paint.CycleMethod.NO_CYCLE,
//                new Stop(0, javafx.scene.paint.Color.WHITE),
//                new Stop(1, javafx.scene.paint.Color.BLACK));
//
//        // Create the rectangle
//        rectangle = new Rectangle(100, 50, 300, 100);
//        rectangle.setStroke(gradient);
//        rectangle.setStrokeWidth(4);
//        rectangle.setFill(null); // No fill for the rectangle
//
//        // The dash array defines the pattern of the stroke
//        rectangle.getStrokeDashArray().addAll(25d, 20d, 5d, 20d);
//        
//        // The offset defines where the stroke pattern starts
//        final double maxOffset = rectangle.getStrokeDashArray().stream()
//                .reduce(0d, Double::sum);
//
//        // Create a Timeline animation to spin the stroke
//        Timeline timeline = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(rectangle.strokeDashOffsetProperty(), 0)),
//                new KeyFrame(Duration.seconds(2), new KeyValue(rectangle.strokeDashOffsetProperty(), maxOffset))
//        );
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        timeline.play();
		rectangle.getStrokeDashArray().addAll(25d, 25d, 25d, 25d);

		// Calculate the sum of the stroke dash array which is the length of the entire
		// pattern
		final double maxOffset = rectangle.getStrokeDashArray().stream().reduce(0d, Double::sum);

		// Create a Timeline animation that updates the stroke dash offset property
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(rectangle.strokeDashOffsetProperty(), 0)),
				new KeyFrame(Duration.seconds(2), new KeyValue(rectangle.strokeDashOffsetProperty(), maxOffset)));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	/**
	 * This method is used in order to add all parks images paths to the array list
	 */
	private void setParksPaths() {
		if (imagePaths == null || imagePaths.isEmpty()) {
			imagePaths.add("/acadia.jpg");
			imagePaths.add("/big_bend.jpg");
			imagePaths.add("/congaree.jpg");
			imagePaths.add("/everglades.jpg");
			imagePaths.add("/gateway_arch.jpg");
			imagePaths.add("/glacier.jpg");
			imagePaths.add("/grand_canyon.jpg");
			imagePaths.add("/great_smoky_mountains.jpg");
			imagePaths.add("/hawaii_volcanoes.jpg");
			imagePaths.add("/hot_springs.jpg");
			imagePaths.add("/mammoth_cave.jpg");
			imagePaths.add("/olympic.jpg");
			imagePaths.add("/shenandoah.jpg");
			imagePaths.add("/theodore_roosevelt.jpg");
			imagePaths.add("/voyageurs.jpg");
			imagePaths.add("/yellowstone.jpg");
			imagePaths.add("/yosemite.jpg");
		}

	}
	///// --- FXML / JAVA FX METHODS --- /////
	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	
	public void initialize() {
		// setting the image view array
		imageViews[0] = image1;
		imageViews[1] = image2;
		imageViews[2] = image3;

		// setting the park images paths
		setParksPaths();

		// setting 3 first images
		imageViews[0].setImage(new Image(imagePaths.get(0)));
		imageViews[1].setImage(new Image(imagePaths.get(1)));
		imageViews[2].setImage(new Image(imagePaths.get(2)));
		currentIndex = 3;

		startSlideshow();
		setRectangleStroke();

		// centering the labels
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		instrLbl.setAlignment(Pos.CENTER);
		instrLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		portTxtField.setPromptText("Enter port number");
		hostTxtField.setPromptText("Enter host address");
		// initializing the image component
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
	}

	@Override
	public void loadBefore(Object information) {
		String[] info = ((String) information).split(" ");
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
}