package clientSide.gui;

import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.entities.Representative;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * /** The ServiceRepresentativeAccountScreenController class is responsible for
 * handling all interactions within the service representative account screen.
 * This class allows manage functionalities specific to their role. It extends
 * AbstractScreen.
 * 
 * This controller manages user data through the GoNatureUsersController,
 * enabling operations such as authentication, registration, and user session
 * management.
 */
public class ServiceRepresentativeAccountScreenController extends AbstractScreen {
	private GoNatureUsersController userControl;
	private Representative representative;

	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 9; // Display 9 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	/**
	 * Constructor
	 */
	public ServiceRepresentativeAccountScreenController() {
		userControl = GoNatureUsersController.getInstance();
	}

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button guideRegisterBtn, logOutBtn;
	@FXML
	private Label titleLbl, nameLbl;
	@FXML
	private ImageView goNatureLogo, image1, image2, image3, image4, image5, image6, image7, image8, image9;
	@FXML
	private VBox imagesVbox, controlVbox;
	@FXML
	private Pane pane;
	@FXML
	private Rectangle rec;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	/**
	 * @param event When the 'Guide Registration' button is pressed, the Service
	 *              Representative will be redirected to the
	 *              'GuideRegistrationScreen'
	 */
	@FXML
	void goToGuideRegistrationScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("GroupGuideRegistrationScreenController",
					"/clientSide/fxml/GroupGuideRegistrationScreen.fxml", true, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event park manager clicked on 'Log out' button, an update query is
	 *              executed to alter the value of the 'isLoggedIn' field
	 */
	@FXML
	void logOut(ActionEvent event) {
		int choise = showConfirmationAlert("Are you sure you want to log out?", Arrays.asList("Yes", "No"));
		switch (choise) {
		case 1: // clicked "Yes"
			if (userControl.logoutUser())
				representative.setLoggedIn(false);
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
	/**
	 * Sets the focus to the pane when clicked
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * @return the service representative instance
	 */
	public Representative getRepresentative() {
		return representative;
	}

	/**
	 * Sets the service representative
	 * 
	 * @param representative
	 */
	public void setRepresentative(Representative representative) {
		this.representative = representative;
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
	 * the FXML file has been loaded, setting the initial state of the UI
	 * components.
	 */
	@Override
	public void initialize() {
		// Restore the Representative user from the saved state.
		representative = (Representative) userControl.restoreUser();

		// Keep the representative's information updated in the session.
		userControl.saveUser(representative);

		// Set the greeting message with the representative's name and underline it for
		// emphasis.
		nameLbl.setText(getGreeting() + representative.getFirstName() + " " + representative.getLastName() + "!");
		nameLbl.underlineProperty(); // Emphasize the name for better visibility.

		// Sets the GoNature logo on the screen.
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// Applies alignment and style configurations to UI components to ensure
		// consistency with the application's design.
		nameLbl.setAlignment(Pos.CENTER);
		nameLbl.layoutXProperty().bind(pane.widthProperty().subtract(nameLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setText("Service Representative");

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
		imageViews[0].setImage(new Image(imagePaths.get(0)));
		imageViews[1].setImage(new Image(imagePaths.get(1)));
		imageViews[2].setImage(new Image(imagePaths.get(2)));
		imageViews[3].setImage(new Image(imagePaths.get(3)));
		imageViews[4].setImage(new Image(imagePaths.get(4)));
		imageViews[5].setImage(new Image(imagePaths.get(5)));
		imageViews[6].setImage(new Image(imagePaths.get(6)));
		imageViews[7].setImage(new Image(imagePaths.get(7)));
		imageViews[8].setImage(new Image(imagePaths.get(8)));

		currentIndex = 9;
		startSlideshow();

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	public void loadBefore(Object information) {
		// irrelevant here
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Service Representative Account";
	}
}