package clientSide.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
 * Controls the park visitor account screen in the GoNature application,
 * providing functionality for managing bookings, confirming visit arrivals, and
 * user logout. It adjusts available options based on visitor type and specific
 * booking details, enhancing the user experience for park visitors.
 * 
 * The controller leverages GoNatureUsersController for user-related operations
 * and interacts with ParkController for booking and park capacity queries. It
 * ensures that visitors can efficiently manage their park visits and related
 * activities directly from their account interface.
 */
public class ParkVisitorAccountScreenController extends AbstractScreen {
	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 9; // Display 9 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	private GoNatureUsersController userControl;
	private ParkVisitor parkVisitor;

	@FXML
	private ImageView goNatureLogo, image1, image2, image3, image4, image5, image6, image7, image8, image9;
	@FXML
	private Button logOutBTN, managingBookingBTN, visitBookingBTN, arrivalConfirmationBTN;
	@FXML
	private Label NameLable, waitLabel;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private VBox imagesVbox, controlVbox;
	@FXML
	private Pane pane;
	@FXML
	private Rectangle rec;

	public ParkVisitorAccountScreenController() {
		userControl = GoNatureUsersController.getInstance();
	}

	public ParkVisitor getParkVisitor() {
		return parkVisitor;
	}

	public void setParkVisitor(ParkVisitor parkVisitor) {
		this.parkVisitor = parkVisitor;
	}

	/**
	 * @param event When the 'Managing Booking' button is pressed, the park visitor
	 *              will be redirected to the 'ManagingBookingScreen'
	 */
	@FXML
	void goTOManagingBookingScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("BookingViewScreenController",
					"/clientSide/fxml/BookingViewScreen.fxml", false, false, parkVisitor);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event When the 'Visit Booking' button is pressed, the park visitor
	 *              will be redirected to the 'ManagingBookingScreen'
	 */
	@FXML
	void goTOVisitBookingScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("BookingScreenConrtroller", "/clientSide/fxml/BookingScreen.fxml",
					false, false, parkVisitor);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event When the 'Visit Booking' button is pressed, the park visitor
	 *              will be redirected to the 'CheckingNotificarionsScreen'
	 */
	@FXML
	void ArrivalConfirmationPopUp(ActionEvent event) {
		// showing the loading dialog
		setVisible(false);
		// moving the data fetching operation to a background thread
		new Thread(() -> {
			// this operation is now off the JavaFX Application Thread
			final AtomicBoolean reminders = new AtomicBoolean();
			reminders.set(showButton());

			// once fetching is complete, updating the UI on the JavaFX Application Thread
			Platform.runLater(() -> {
				if (reminders.get()) {
					// the park visitor will be redirected to the
					// 'CheckingNotoficationsScreenScreen'
					try {
						ScreenManager.getInstance().showScreen("CheckingNotoficationsScreenConrtroller",
								"/clientSide/fxml/CheckingNotoficationsScreen.fxml", true, false, null);
					} catch (ScreenException | StatefulException e) {
						e.printStackTrace();
					}
				} else {
					showErrorAlert("No reservations are available for confirmation at the moment.");

				}
				setVisible(true);
			});
		}).start();
	}

	/**
	 * @param event parkEmplyee clicked on 'Log out' button, an update query is
	 *              executed to alter the value of the 'isLoggedIn' field in
	 *              database. The user will return to main Screen.
	 * @throws CommunicationException
	 */
	@FXML
	void logOut(ActionEvent event) {
		int choise = showConfirmationAlert("Are you sure you want to log out?", Arrays.asList("No","Yes"));
		
		switch (choise) {
		case 1: // clicked "No"
			event.consume();
			break;
		case 2: // clicked "Yes"
			if (userControl.logoutUser())
				parkVisitor.setLoggedIn(false);
			try {
				ScreenManager.getInstance().goToPreviousScreen(false, false);
			} catch (ScreenException | StatefulException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}

	/**
	 * Determines if the button for booking confirmation should be shown to the
	 * user. This decision is based on checking all parks for any active bookings
	 * associated with the current park visitor. A booking qualifies if the visitor
	 * has received a reminder for it, indicating an upcoming visit. This method
	 * iterates through all parks, checks for such bookings, and aggregates them
	 * into a list. If at least one qualifying booking is found, the method returns
	 * true, signaling that the confirmation button should be displayed.
	 * 
	 * @return true if the visitor has at least one active booking with a received
	 *         reminder, otherwise false.
	 */
	private boolean showButton() {
		ParkController parkControl = ParkController.getInstance();
		ArrayList<Park> parks = parkControl.fetchParks();
		ArrayList<Booking> bookings = new ArrayList<>();
		// Iterates through each park to check for active bookings associated with the
		// visitor
		for (Park park : parks) {
			String parkTable = parkControl.nameOfTable(park) + Communication.activeBookings;
			ArrayList<Booking> tempBookings = parkControl.checkIfBookingExists(parkTable, "idNumber",
					parkVisitor.getIdNumber());
			// Adds bookings with received reminders to the aggregate list
			if (tempBookings != null) {
				for (Booking booking : tempBookings) {
					if (!booking.isConfirmed() && booking.isRecievedReminder()) {
						booking.setParkBooked(park);
						bookings.add(booking);
					}
				}
			}
		}
		// Determines the visibility of the confirmation button based on the presence of
		// qualifying bookings
		if (bookings.size() == 0)
			return false;
		// Updates the global list of bookings for further processing
		userControl.setBookingsList(bookings);
		return true;
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

	private void setVisible(boolean visible) {
		progressIndicator.setVisible(!visible);
		waitLabel.setVisible(!visible);
		imagesVbox.setVisible(visible);
		rec.setVisible(visible);
		arrivalConfirmationBTN.setDisable(!visible);
		logOutBTN.setDisable(!visible);
		managingBookingBTN.setDisable(!visible);
		visitBookingBTN.setDisable(!visible);
	}

	/**
	 * Initializes the controller class. This method is automatically called after
	 * the FXML file has been loaded. It performs initial setup for the screen,
	 * including setting text for the user's name, determining visibility of certain
	 * buttons, and applying styles to UI components.
	 */
	@Override
	public void initialize() {
		setVisible(true);
		// Restores the park visitor from the saved state to ensure continuity in user
		// experience.
		parkVisitor = (ParkVisitor) userControl.restoreUser();
		// Saves the current state of the park visitor for potential future use.
		userControl.saveUser(parkVisitor);

		// Sets greeting text dynamically based on the visitor's information.
		if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
			this.NameLable.setText("Hello, " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName() + "!");
			this.NameLable.underlineProperty(); // Adds underline to emphasize the name label.
		} else {
			logOutBTN.setVisible(false); // Hides the logout button if not a group guide.
		}

		// setting the porgress indicator
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

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

		// setting 3 first images
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

		// showing the loading dialog
		setVisible(false);
		waitLabel.setText("Loading Your Bookings Information");
		// moving the data fetching operation to a background thread
		new Thread(() -> {
			// this operation is now off the JavaFX Application Thread
			final AtomicBoolean reminders = new AtomicBoolean();
			reminders.set(showButton());

			// once fetching is complete, updating the UI on the JavaFX Application Thread
			Platform.runLater(() -> {
				if (reminders.get()) {
					showInformationAlert("Please confirm your reservation");
				}
				setVisible(true);
				startSlideshow();
			});
		}).start();

		// Sets the GoNature logo image.
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		NameLable.setAlignment(Pos.CENTER);

		// setting the rectangle's shadow
		DropShadow dropShadow = new DropShadow();
		dropShadow.setRadius(10.0);
		dropShadow.setOffsetX(5.0);
		dropShadow.setOffsetY(5.0);
		dropShadow.setColor(Color.rgb(50, 50, 50));
		rec.setEffect(dropShadow);
	}

	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return "Account Screen";
	}

}
