package clientSide.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import clientSide.control.GoNatureUsersController;
import clientSide.control.LoginController;
import clientSide.entities.DepartmentManager;
import clientSide.entities.ParkEmployee;
import clientSide.entities.ParkManager;
import clientSide.entities.ParkVisitor;
import clientSide.entities.ParkVisitor.VisitorType;
import clientSide.entities.Representative;
import clientSide.entities.SystemUser;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainScreenController extends AbstractScreen {
	boolean screenIsActive;
	private javafx.animation.Timeline slideshowTimeline;
	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 3; // Display 3 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set

	private LoginController loginControl; // controller
	private GoNatureUsersController usersControl;

	private static final String digitsOnly = "\\d+";

	public MainScreenController() {
		loginControl = new LoginController();
		usersControl = GoNatureUsersController.getInstance();
		screenIsActive = true;
	}

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button signInBtn, bookNowbtn;
	@FXML
	private ImageView goNatureLogo, image1, image2, image3, info1, info2;
	@FXML
	private HBox hbox;
	@FXML
	private VBox vbox, signInVbox, orVbox, idVbox;
	@FXML
	private TextField usernameTxt, idNumberTxt;
	@FXML
	private PasswordField passwordTxt;
	@FXML
	private Label titleLbl, lbl1, lbl2, lbl3, waitLabel;
	@FXML
	private Pane pane;
	@FXML
	private Separator sep1, sep2, sep3;
	@FXML
	private ProgressIndicator progressIndicator;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * This method is called if the Book Now button is clicked
	 * 
	 * @param event
	 */
	void bookNowbtn(ActionEvent event) {
		usernameTxt.setText("");
		passwordTxt.setText("");
		// first checking if the entered id is valid
		if (validateId()) {
			// if valid, checking the database
			// moving the data fetching operation to a background thread
			setVisible(false);
			String idNumber = idNumberTxt.getText();
			Thread taskThread = new Thread(() -> {
				// this operation is now off the JavaFX Application Thread
				final SystemUser user = loginControl.checkIfTravellerExists(idNumber);
				final AtomicBoolean hasBookings = new AtomicBoolean(), isRegisteredEmployee = new AtomicBoolean();
				if (user != null) { // if exists in the database
					hasBookings.set(loginControl.checkIfTravellerHasBookings((ParkVisitor) user));
				} else { // if does not exist, checking if an employee has this id
					isRegisteredEmployee.set(loginControl.checkUserId(idNumber));
				}

				// once fetching is complete, updating the UI on the JavaFX Application Thread
				Platform.runLater(() -> {
					if (user == null) { // there's no traveller exists with this id number
						if (isRegisteredEmployee.get()) {
							setVisible(true);
							showErrorAlert("Employees of the company have to log in using username and password");
							usernameTxt.setStyle(setFieldToError());
							passwordTxt.setStyle(setFieldToError());
							idNumberTxt.setText("");
							paneFocus(null);
						} else {
							// so inserting him to the database
							loginControl.insertNewTraveller(idNumber);
							ParkVisitor traveller = new ParkVisitor(idNumber, null, null, null, null, null, null, true,
									VisitorType.TRAVELLER);
							usersControl.saveUser(traveller);
							loginControl.updateUserIsLoggedIn(traveller);
							// showing the new booking screen
							try {
								ScreenManager.getInstance().showScreen("BookingScreenController",
										"/clientSide/fxml/BookingScreen.fxml", false, false, traveller);
							} catch (StatefulException | ScreenException e) {
								e.printStackTrace();
							}
							return;
						}
					} else { // there is a traveller exists with this id number
								// now checking if this user is already logged in on another device
						if (user.isLoggedIn()) {
							setVisible(true);
							showErrorAlert(
									"You are already logged in on another device\nPlease log out from this device in order to log in");
						} else { // if not logged in >>> loggin the user in
							usersControl.saveUser(user);
							loginControl.updateUserIsLoggedIn(user);
							user.setLoggedIn(true);
							if (hasBookings.get()) { // if has bookings
								openRelevantScreen(user);
								return;
							} else { // if does not have bookings
								try {
									ScreenManager.getInstance().showScreen("BookingScreenController",
											"/clientSide/fxml/BookingScreen.fxml", false, false, user);
								} catch (StatefulException | ScreenException e) {
									e.printStackTrace();
								}
								return;
							}
						}
					}
				});
			});
			taskThread.start();
			stopSlideshow();
		}
	}

	@FXML
	/**
	 * This method is called if the Sign In button is clicked
	 * 
	 * @param event
	 */
	void signInButtonClicked(ActionEvent event) {
		idNumberTxt.setText("");
		// first checking if the entered credentials are valid
		if (validateCredentials()) {
			// if valid, checking the database
			// moving the data fetching operation to a background thread
			setVisible(false);
			new Thread(() -> {
				// this operation is now off the JavaFX Application Thread
				final SystemUser user = loginControl.checkUserCredentials(usernameTxt.getText(), passwordTxt.getText());

				// once fetching is complete, updating the UI on the JavaFX Application Thread
				Platform.runLater(() -> {
					if (user == null) { // there's no user with these credentials
						setVisible(true);
						showErrorAlert("One/more of the credentials is incorrect\nPlease try again");
					} else { // there is a user with these credentials
								// now checking if this user is already logged in on another device
						if (user.isLoggedIn()) {
							setVisible(true);
							showErrorAlert(
									"You are already logged in on another device\nPlease log out from this device in order to log in");
						} else { // if not logged in >>> loggin the user in
							usersControl.saveUser(user);
							loginControl.updateUserIsLoggedIn(user);
							user.setLoggedIn(true);
							openRelevantScreen(user);
						}
					}
				});
			}).start();
		}
	}

	@FXML
	void paneFocus(MouseEvent event) {
		pane.requestFocus();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method is used to hide/show all elements but the progress indicator and
	 * its label
	 * 
	 * @param visible
	 */
	private void setVisible(boolean visible) {
		progressIndicator.setVisible(!visible);
		waitLabel.setVisible(!visible);
		hbox.setVisible(visible);
		info1.setVisible(visible);
		info2.setVisible(visible);
	}

	/**
	 * This method gets a SystemUser instance and open its account screen according
	 * to its sub-class
	 * 
	 * @param user
	 */
	private void openRelevantScreen(SystemUser user) {
		try {
			if (user instanceof ParkVisitor) {
				ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController",
						"/clientSide/fxml/ParkVisitorAccountScreen.fxml", false, false, null);
			} else if (user instanceof ParkManager) {
				ScreenManager.getInstance().showScreen("ParkManagerAccountScreenController",
						"/clientSide/fxml/ParkManagerAccountScreen.fxml", false, false, null);
			} else if (user instanceof DepartmentManager) {
				ScreenManager.getInstance().showScreen("DepartmentManagerAccountScreenController",
						"/clientSide/fxml/DepartmentManagerAccountScreen.fxml", false, false, null);
			} else if (user instanceof Representative) {
				ScreenManager.getInstance().showScreen("ServiceRepresentativeAccountScreenController",
						"/clientSide/fxml/ServiceRepresentativeAccountScreen.fxml", false, false, null);
			} else if (user instanceof ParkEmployee) {
				ScreenManager.getInstance().showScreen("ParkEmployeeAccountScreenController",
						"/clientSide/fxml/ParkEmployeeAccountScreen.fxml", false, false, null);
			} else {
				return;
			}
		} catch (StatefulException | ScreenException e) {
			e.getStackTrace();
		}
	}

	/**
	 * This method checks if the entered username and password are valid
	 * 
	 * @return true if valid, false if not
	 */
	private boolean validateCredentials() {
		usernameTxt.setStyle(setFieldToRegular());
		passwordTxt.setStyle(setFieldToRegular());

		String error = "Errors:";
		boolean valid = true;

		if (usernameTxt.getText().isEmpty()) {
			error += "\n• Please enter a username";
			usernameTxt.setStyle(setFieldToError());
			valid = false;
		} else {
			if (usernameTxt.getText().contains(" ")) {
				error += "\n• Please enter a valid username";
				usernameTxt.setStyle(setFieldToError());
				valid = false;
			}
		}

		if (passwordTxt.getText().isEmpty()) {
			error += "\n• Please enter a password";
			passwordTxt.setStyle(setFieldToError());
			valid = false;
		} else {
			if (passwordTxt.getText().contains(" ")) {
				error += "\n• Please enter a valid username";
				passwordTxt.setStyle(setFieldToError());
				valid = false;
			}
		}

		if (!valid) {
			showErrorAlert(error);
		}

		return valid;
	}

	/**
	 * This method checks if the entered id number is valid
	 * 
	 * @return true if valid, false if not
	 */
	private boolean validateId() {
		idNumberTxt.setStyle(setFieldToRegular());

		String error = "";
		boolean valid = true;

		if (idNumberTxt.getText().isEmpty()) {
			error += "Please enter an id number in order to proceed";
			idNumberTxt.setStyle(setFieldToError());
			valid = false;
		} else {
			if (idNumberTxt.getText().length() != 9) {
				error += "Please enter a valid 9-digit id number";
				idNumberTxt.setStyle(setFieldToError());
				valid = false;
			}
		}

		if (!valid) {
			showErrorAlert(error);
		}

		return valid;
	}

	/**
	 * This method starts the parks images slide show using fade transitions
	 */
	private void startSlideshow() {
		if (slideshowTimeline != null) {
	        slideshowTimeline.stop();
	    }
	    slideshowTimeline = new javafx.animation.Timeline();
	    
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
		slideshowTimeline.getKeyFrames().add(
		        new javafx.animation.KeyFrame(Duration.seconds(5), // Change images every 5 seconds
		            event -> changeImagesTask.run()));
		    slideshowTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		    slideshowTimeline.play();
	}
	
	/**
	 * This method stops the slide show
	 */
	private void stopSlideshow() {
	    if (slideshowTimeline != null) {
	        slideshowTimeline.stop();
	    }
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
				if (newValue != null && !newValue.matches(digitsOnly)) {
					textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
	 * Initializes the controller class. This method is automatically called after
	 * the fxml file has been loaded. Initializes the GoNature logo.
	 */
	@Override
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

		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));

		hbox.setAlignment(Pos.CENTER);
		signInVbox.setAlignment(Pos.CENTER);
		orVbox.setAlignment(Pos.CENTER);
		idVbox.setAlignment(Pos.CENTER);

		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		lbl1.setAlignment(Pos.CENTER);
		lbl2.setAlignment(Pos.CENTER);
		lbl3.setAlignment(Pos.CENTER);

		setupTextFieldToDigitsOnly(idNumberTxt);
		setVisible(true);

		// setting the labels
		waitLabel.setAlignment(Pos.CENTER);
		waitLabel.layoutXProperty().bind(pane.widthProperty().subtract(waitLabel.widthProperty()).divide(2));
		waitLabel.setText("Logging You In...");
		waitLabel.setStyle("-fx-text-alignment: center;");

		// setting the porgress indicator
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));
		
		// adding tooltips
		Tooltip tooltipInfo1 = new Tooltip("Signing into the system\nfor employees, managers\nand group guides.");
		tooltipInfo1.setShowDelay(javafx.util.Duration.ZERO);
		Tooltip.install(info1, tooltipInfo1);
		tooltipInfo1.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"); // Make text bold and increase size

		Tooltip tooltipInfo2 = new Tooltip("Making fast reservations is\navailable for individual\nor family groups only.");
		tooltipInfo2.setShowDelay(javafx.util.Duration.ZERO);
		Tooltip.install(info2, tooltipInfo2);
		tooltipInfo2.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"); // Make text bold and increase size
		

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	public void loadBefore(Object information) {
		// an abstract method, irrelevant here
	}

	/**
	 * Returns the title of the screen.
	 *
	 * @return A string representing the title of the Main Screen.
	 */
	@Override
	public String getScreenTitle() {
		return "Main Screen";
	}
}