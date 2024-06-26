package clientSide.gui;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import clientSide.entities.DepartmentManager;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import common.entities.Park;
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
 * The DepartmentManagerAccountScreenController class manages the interactions
 * within the department manager's account screen in the GoNature application.
 * It facilitates various operations. This class extends AbstractScreen for UI
 * control.
 *
 * It utilizes GoNatureUsersController for user management and ParkController
 * for park-related operations, ensuring that department managers can
 * effectively oversee and manage park data and reports.
 */
public class DepartmentManagerAccountScreenController extends AbstractScreen {

	private static GoNatureUsersController userControl;
	private ParkController parkControl;
	private DepartmentManager departmentManager;

	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 9; // Display 9 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	@FXML
	private Label titleLbl, nameLbl;
	@FXML
	private Button approvingDataBtn, reportsBtn, logOutBtn, currentCapacitiesBtn;
	@FXML
	private ImageView goNatureLogo, image1, image2, image3, image4, image5, image6, image7, image8, image9;
	@FXML
	private VBox imagesVbox, controlVbox;
	@FXML
	private Pane pane;
	@FXML
	private Rectangle rec;

	/**
	 * Initializes a new instance of the DepartmentManagerAccountScreenController.
	 * It sets up the necessary controller instances for user and park operations,
	 * facilitating the management of department-specific functionalities within the
	 * application.
	 */
	public DepartmentManagerAccountScreenController() {
		userControl = GoNatureUsersController.getInstance();
		parkControl = ParkController.getInstance();
	}

	public DepartmentManager getParkManager() {
		return departmentManager;
	}

	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}

	/**
	 * @param event When the 'approvingData' button is pressed, the park MANAGER
	 *              will be redirected to the 'ParametersApprovingScreen'
	 */
	@FXML
	void goToApprovingParksDataScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("ParametersApprovingScreenConrtroller",
					"/clientSide/fxml/ParametersApprovingScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event When the 'reports' button is pressed, the park MANAGER will be
	 *              redirected to the 'ParkDepartmentalReportsScreen'
	 */
	@FXML
	void goToReportsScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().showScreen("DepartmentManagerReportsScreenController",
					"/clientSide/fxml/DepartmentManagerReportsScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event When the 'Current Capacity' button is pressed, the department
	 *              manager will see a pop-up screen with the current capacity
	 *              according to park parameters at the DB
	 */
	@FXML
	void getCurrenetCapacities(ActionEvent event) {
		// updates for each park the latest relevant parameters
		for (int i = 0; i < departmentManager.getResponsible().size(); i++) {
			String parkName = departmentManager.getResponsible().get(i).getParkName();
			String[] currCap = parkControl.checkCurrentCapacity(parkName);
			if (currCap != null) {
				// updates park parameters
				departmentManager.getResponsible().get(i).setMaximumVisitors(Integer.parseInt(currCap[0]));
				departmentManager.getResponsible().get(i).setMaximumOrders(Integer.parseInt(currCap[1]));
				departmentManager.getResponsible().get(i).setTimeLimit(Integer.parseInt(currCap[2]));
				departmentManager.getResponsible().get(i).setCurrentCapacity(Integer.parseInt(currCap[3]));
			}
		}
		StringBuilder showCapacities = new StringBuilder();
		for (Park park : departmentManager.getResponsible()) {
			showCapacities.append(park.getParkName()).append(" Park Capacities:");
			showCapacities.append("\n\tCurrent Park Capacity: ").append(park.getCurrentCapacity());
			showCapacities.append("\n\tMaximum Visitors Allowance: ").append(park.getMaximumVisitors());
			showCapacities.append("\n\tMaximum Visitors by Orders: ").append(park.getMaximumOrders());
			showCapacities.append("\n\tPark's Visits Time Limits: ").append(park.getTimeLimit());
			showCapacities.append("\n");
		}
		showInformationAlert(showCapacities.toString());
	}

	/**
	 * Logs out the department manager when the 'Log Out' button is pressed. This
	 * method updates the 'isLoggedIn' status in the database and redirects the user
	 * to the main screen of the application.
	 *
	 * @param event The ActionEvent triggered by pressing the 'Log Out' button.
	 * @throws CommunicationException If there is a communication issue with the
	 *                                server during the logout process.
	 */
	@FXML
	void logOut(ActionEvent event) {
		int choise = showConfirmationAlert("Are you sure you want to log out?", Arrays.asList("Yes", "No"));
		switch (choise) {
		case 1: // clicked "Yes"
			if (userControl.logoutUser())
				departmentManager.setLoggedIn(false);
			
			try {
				ScreenManager.getInstance().resetScreensStack();
				ScreenManager.getInstance().showScreen("MainScreenController",
						"/clientSide/fxml/MainScreen.fxml", false, false, null);
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

	/**
	 * This method is automatically invoked after the FXML has been loaded, setting
	 * up the initial state of the UI components. It prepares the Department
	 * Manager's account screen by loading the user's data, setting greeting text,
	 * updating the title, fetching and displaying the parks managed by the
	 * department manager, and applying styles to UI elements.
	 */
	@Override
	public void initialize() {
		// Restores the DepartmentManager object from the saved user session to maintain
		// continuity.
		departmentManager = (DepartmentManager) userControl.restoreUser();

		// Sets the greeting text with the department manager's name to personalize the
		// UI.
		nameLbl.setText(getGreeting() + departmentManager.getFirstName() + " " + departmentManager.getLastName() + "!");
		nameLbl.underlineProperty(); // Adds an underline for visual emphasis.

		// Dynamically sets the screen title based on the department manager's
		// department.
		titleLbl.setText(departmentManager.getManagesDepartment() + " Department");
		titleLbl.underlineProperty(); // Underlines the title for emphasis.

		// Attempts to fetch and set the list of parks managed by the department
		// manager.
		ArrayList<Park> parks = parkControl.fetchManagerParksList("departmentManagerId",
				departmentManager.getIdNumber());
		try {
			departmentManager.setResponsible(parks); // Sets the list of parks the department manager is responsible
														// for.
		} catch (NullPointerException e) {
			// Handles the case where no parks list could be fetched.
		}

		// Sets the GoNature logo on the screen.
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// Applies alignment and style configurations to UI components to ensure
		// consistency with the application's design.
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
	 * @param information The DepartmentManager object passed from the logIn screen,
	 *                    containing the manager's details.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return departmentManager.getManagesDepartment() + " - Department Manager Account";
	}

}
