package clientSide.gui;

import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import clientSide.entities.ParkEmployee;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Controller for the Park Employee Account Screen within the GoNature
 * application. This controller manages the interaction of park employees with
 * their account interface. It extends AbstractScreen to navigation within the
 * application effectively.
 * 
 * The class integrates with the GoNatureUsersController and ParkController to
 * perform its operations, managing user session states and park-related data.
 */
public class ParkEmployeeAccountScreenController extends AbstractScreen {
	private GoNatureUsersController userControl;
	private ParkEmployee parkEmployee;

	/**
	 * Initializes a new instance of the ParkEmployeeAccountScreenController class.
	 * It sets up the necessary controller instances for managing user and park
	 * operations.
	 */
	public ParkEmployeeAccountScreenController() {
		userControl = GoNatureUsersController.getInstance();
	}

	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 9; // Display 9 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Label titleLbl, nameLbl;
	@FXML
	private Button logOutBtn, parkEntryManagementBtn, parkEntryCasualBtn;
	@FXML
	private ImageView goNatureLogo, image1, image2, image3, image4, image5, image6, image7, image8, image9;
	@FXML
	private VBox imagesVbox, controlVbox;
	@FXML
	private Pane pane;
	@FXML
	private Rectangle rec;

	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	/**
	 * @param event When the 'Visitor Reservation' button is pressed, the park
	 *              employee will be redirected to the 'ParkEntryResevationScreen'
	 */
	@FXML
	void goToParkEntryResevationScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("ParkEntryReservationScreenController",
					"/clientSide/fxml/ParkEntryReservationScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event When the 'parkEntryManagementBTN' button is pressed, the park
	 *              employee will be redirected to the 'ParkEntryManagementScreen'
	 */
	@FXML
	void goToParkEntryManagementScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("ParkEntryManagementScreenController",
					"/clientSide/fxml/ParkEntryManagementScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event parkEmployee clicked on 'Log out' button, an update query is
	 *              executed to alter the value of the 'isLoggedIn' field in
	 *              database. The user will return to main Screen.
	 * @throws CommunicationException
	 */
	@FXML
	void logOut(ActionEvent event) {
		int choise = showConfirmationAlert("Are you sure you want to log out?", Arrays.asList("Yes", "No"));
		switch (choise) {
		case 1: // clicked "Yes"
			if (userControl.logoutUser())
				parkEmployee.setLoggedIn(false);
			try {
				ScreenManager.getInstance().showScreen("MainScreenController",
						"/clientSide/fxml/MainScreen.fxml", true, false, null);
			} catch (ScreenException | StatefulException e) {
				e.printStackTrace();
			}
			
		case 2: // clicked "No"
			event.consume();
			break;
		}
	}

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

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

		FadeTransition fade4 = new FadeTransition(Duration.millis(2000), image4);
		fade4.setFromValue(0.0);
		fade4.setToValue(1.0);
		fade4.play();

		FadeTransition fade5 = new FadeTransition(Duration.millis(2000), image5);
		fade5.setFromValue(0.0);
		fade5.setToValue(1.0);
		fade5.play();

		FadeTransition fade6 = new FadeTransition(Duration.millis(2000), image6);
		fade6.setFromValue(0.0);
		fade6.setToValue(1.0);
		fade6.play();

		FadeTransition fade7 = new FadeTransition(Duration.millis(2000), image7);
		fade7.setFromValue(0.0);
		fade7.setToValue(1.0);
		fade7.play();

		FadeTransition fade8 = new FadeTransition(Duration.millis(2000), image8);
		fade8.setFromValue(0.0);
		fade8.setToValue(1.0);
		fade8.play();

		FadeTransition fade9 = new FadeTransition(Duration.millis(2000), image9);
		fade9.setFromValue(0.0);
		fade9.setToValue(1.0);
		fade9.play();

		Runnable changeImagesTask = () -> {
			if (currentIndex >= imagePaths.size()) {
				currentIndex = 0; // Reset index to loop
			}

			// Duration for each fade transition
			final long fadeDuration = 1000; // 1000 milliseconds (1 second)

			// The delay increment between the start of each fade-out transition
			// This determines how quickly after one image starts fading out the next image
			// will start fading out.
			// For smoother transitions between 9 images, you might want a shorter delay
			// between starts,
			// e.g., starting the next fade-out after half the duration of the fade effect.
			final long delayIncrement = fadeDuration / 3; // Adjust this for smoother transitions between more images

			for (int i = 0; i < IMAGE_VIEW_COUNT; i++) {
				final int imageIndex = (currentIndex + i) % imagePaths.size();
				ImageView imageView = imageViews[i];
				Image newImage = new Image(imagePaths.get(imageIndex));

				// Apply fade-out transition on image change
				FadeTransition fadeOut = new FadeTransition(Duration.millis(fadeDuration), imageView);
				fadeOut.setFromValue(1.0);
				fadeOut.setToValue(0.0);
				fadeOut.setDelay(Duration.millis(i * delayIncrement)); // Adjust delay based on position in the sequence
				fadeOut.setOnFinished(event -> {
					imageView.setImage(newImage);
					FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeDuration), imageView);
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
	 * @return A greeting according to the current time of the day
	 */
	private String getGreeting() {
		LocalTime now = LocalTime.now();
		int hour = now.getHour();
		if (hour >= 6 && hour <= 12) {
			return "Good Morning, ";
		} else if (hour > 12 && hour <= 18) {
			return "Good Afternoon, ";
		} else if (hour > 18 && hour <= 22) {
			return "Good Evening, ";
		} else {
			return "Good Night, ";
		}
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
	 * Initializes the controller class. This method is automatically called after
	 * the FXML file has been loaded. It prepares the screen for display by setting
	 * up user details, park information, and UI components.
	 */
	@Override
	public void initialize() {
		// Restore the ParkEmployee user from the session state.
		parkEmployee = (ParkEmployee) userControl.restoreUser();
		ParkController.getInstance().savePark(parkEmployee.getWorkingIn());
		// Sets the greeting text with the department manager's name to personalize the
		// UI.
		nameLbl.setText(getGreeting() + parkEmployee.getFirstName() + " " + parkEmployee.getLastName() + "!");
		nameLbl.underlineProperty(); // Adds an underline for visual emphasis.

		// Set the park name in the title label to indicate the current park context.
		titleLbl.setText(parkEmployee.getWorkingIn().getParkName() + " Park");
		titleLbl.underlineProperty(); // Underline the title for emphasis.

		// Sets the GoNature logo and the labels on the screen to the center.
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		// Apply styling to ensure consistent alignment and appearance of UI elements.
		nameLbl.setAlignment(Pos.CENTER);
		nameLbl.layoutXProperty().bind(pane.widthProperty().subtract(nameLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		// setting the image view array
		imageViews[0] = image1;
		imageViews[1] = image2;
		imageViews[2] = image3;
		imageViews[3] = image4;
		imageViews[4] = image5;
		imageViews[5] = image6;
		imageViews[6] = image7;
		imageViews[7] = image8;
		imageViews[8] = image9;

		// setting 9 first images
		for (int i = 0; i <= 8; i++) {
			imageViews[i].setImage(new Image(imagePaths.get(i)));
		}

		currentIndex = 9;
		startSlideshow();

		// setting the rectangle's shadow
		DropShadow dropShadow = new DropShadow();
		dropShadow.setRadius(10.0);
		dropShadow.setOffsetX(5.0);
		dropShadow.setOffsetY(5.0);
		dropShadow.setColor(Color.rgb(50, 50, 50));
		rec.setEffect(dropShadow);

		// setting the application's background
		setApplicationBackground(pane);
	}

	/**
	 * @param information Information passed from the previous screen, expected to
	 *                    be a ParkEmployee instance.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return parkEmployee.getWorkingIn().getParkName() + " Park - Employee Account";
	}
}