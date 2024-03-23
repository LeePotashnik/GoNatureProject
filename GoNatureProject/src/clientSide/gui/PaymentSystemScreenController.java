package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.BookingController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PaymentSystemScreenController extends AbstractScreen {
	private Booking booking;
	private static final String nameInput = "^[a-zA-Z]+ [a-zA-Z]+$";
	private static final String fourDigits = "\\d{4}";
	private static final String digitsOnly = "\\d*";

	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 3;
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT];
	private int currentIndex = 0;

	//////////////////////////////////
	/// JAVAFX ANF FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private ImageView goNatureLogo, image1, image2, image3, visaImage, mastercardImage, amexImage;
	@FXML
	private HBox hbox;
	@FXML
	private Label amountLbl, waitLabel, label1, label2, label3, label4, label5;
	@FXML
	private Button processPayment;
	@FXML
	private TextField cardNumber1Txt, cardNumber2Txt, cardNumber3Txt, cardNumber4Txt, cvvTxt, holderNameTxt;
	@FXML
	private ComboBox<String> monthComboBox;
	@FXML
	private ComboBox<String> yearComboBox;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private Pane pane;

	/////////////////////////////
	/// ACTION EVENTS METHODS ///
	/////////////////////////////

	/**
	 * Processes the payment when the 'Process Payment' button is clicked. Validates
	 * the payment information and navigates to the confirmation screen if
	 * validation passes.
	 *
	 * @param event The action event that triggers the payment process.
	 */
	@FXML
	void processPayment(ActionEvent event) {
		boolean valid = paymentValidation();
		if (valid) {
			setVisible(false);

			PauseTransition pause = new PauseTransition(Duration.seconds(2));
			pause.setOnFinished(e -> {
				try {
					new Thread(() -> {
						BookingController.getInstance().sendNotification(booking, false);
					}).start();

					ScreenManager.getInstance().showScreen("ConfirmationScreenController",
							"/clientSide/fxml/ConfirmationScreen.fxml", true, false, booking);
				} catch (StatefulException | ScreenException e1) {
					e1.printStackTrace();
				}
			});
			pause.play();
		}
	}

	///////////////////////////////////
	/// JAVAFX FLOW CONTROL METHODS ///
	///////////////////////////////////

	// Methods for handling tab key presses in various text fields and ComboBoxes.
	// These ensure that focus moves logically through the payment form.
	@FXML
	void cvvTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			holderNameTxt.requestFocus();
		}
	}

	@FXML
	void holderNameTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			processPayment.requestFocus();
		}
	}

	@FXML
	void lastCardNumberTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			monthComboBox.requestFocus();
		}
	}

	@FXML
	void monthTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			yearComboBox.requestFocus();
		}
	}

	@FXML
	void yearTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			cvvTxt.requestFocus();
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * Validates all payment information fields for correct format and completeness.
	 * Highlights any fields with invalid input.
	 *
	 * @return true if all input fields are valid, false otherwise.
	 */
	public boolean paymentValidation() {
		cardNumber1Txt.setStyle(setFieldToRegular());
		cardNumber2Txt.setStyle(setFieldToRegular());
		cardNumber3Txt.setStyle(setFieldToRegular());
		cardNumber4Txt.setStyle(setFieldToRegular());
		monthComboBox.setStyle(setFieldToRegular());
		yearComboBox.setStyle(setFieldToRegular());
		cvvTxt.setStyle(setFieldToRegular());
		holderNameTxt.setStyle(setFieldToRegular());
		boolean valid = true;
		boolean cardValid = true;
		String showMessage = "";
		// Validate card fields: non-empty and exactly 4 digits
		if (!validateCardField(cardNumber1Txt)) {
			cardNumber1Txt.setStyle(setFieldToError());
			valid = false;
			cardValid = false;
		}
		if (!validateCardField(cardNumber2Txt)) {
			cardNumber2Txt.setStyle(setFieldToError());
			valid = false;
			cardValid = false;
		}
		if (!validateCardField(cardNumber3Txt)) {
			cardNumber3Txt.setStyle(setFieldToError());
			valid = false;
			cardValid = false;
		}
		if (!validateCardField(cardNumber4Txt)) {
			cardNumber4Txt.setStyle(setFieldToError());
			valid = false;
			cardValid = false;
		}
		if (!cardValid) {
			showMessage += "\n- Credit card number is not valid.";
		}
		// Validate ComboBox selections
		if (!validateComboBoxSelection(monthComboBox)) {
			monthComboBox.setStyle(setFieldToError());
			showMessage += "\n- Month is not valid.";
			valid = false;
		}
		if (!validateComboBoxSelection(yearComboBox)) {
			yearComboBox.setStyle(setFieldToError());
			showMessage += "\n- Year is not valid.";
			valid = false;
		}
		// Validate CVV: non-empty and exactly 3 digits
		if (cvvTxt.getText() == null || cvvTxt.getText().length() != 3) {
			cvvTxt.setStyle(setFieldToError());
			showMessage += "\n- CVV Number is not valid.";
			valid = false;
		}
		// Validate holder name: non-empty, only letters, and exactly two words
		if (holderNameTxt.getText() == null || !holderNameTxt.getText().matches(nameInput)) {
			holderNameTxt.setStyle(setFieldToError());
			showMessage += "\n- Name is not valid.";
			valid = false;
		}
		if (!valid) {
			showErrorAlert("Errors:" + showMessage);
		}
		return valid; // All validations passed
	}

	/**
	 * This method starts the slide show animation of the parks images
	 */
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

	/**
	 * Validates that a card number field contains exactly 4 digits.
	 *
	 * @param cardField The text field containing the card number to be validated.
	 * @return true if the field is valid, false otherwise.
	 */
	private boolean validateCardField(TextField cardField) {
		return cardField.getText() != null && cardField.getText().matches(fourDigits);
	}

	/**
	 * Validates that a ComboBox has a selection made.
	 *
	 * @param comboBox The ComboBox to validate.
	 * @return true if a selection has been made, false otherwise.
	 */
	private boolean validateComboBoxSelection(ComboBox<?> comboBox) {
		return comboBox.getValue() != null;
	}

	/**
	 * Configures a text field for credit card input, enforcing numerical input and
	 * automatic focus management. This method ensures that each text field only
	 * accepts digits and automatically moves the focus to the next field once the
	 * user has entered 4 digits, facilitating a smooth and efficient input process
	 * for the credit card number. If the text field is the last in the sequence
	 * (i.e., nextField is null), it does not attempt to move focus further.
	 *
	 * @param currentField The text field currently being configured. This field
	 *                     will accept only digit characters and is limited to a
	 *                     maximum of 4 digits.
	 * @param nextField    The next text field to focus on once the currentField is
	 *                     fully populated. If currentField is the last in the
	 *                     sequence, this parameter should be null.
	 */
	private void setupTextField(TextField currentField, TextField nextField) {
		currentField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches(digitsOnly)) {
					currentField.setText(newValue.replaceAll("[^\\d]", ""));
				} else {
					// Check the length of input is not greater than 4 characters
					if (newValue.length() > 4) {
						currentField.setText(newValue.substring(0, 4));
					}
					// If it is 4 characters long and there is a next field, move focus to the next
					// field
					if (newValue.length() == 4 && nextField != null) {
						nextField.requestFocus();
					}
				}
			}
		});
	}

	public void updateAmountLabel() {
		amountLbl.setText(String.valueOf(booking.getFinalPrice()) + "$"); // Update the label's text programmatically
	}

	/**
	 * Populates the month ComboBox with month numbers.
	 */
	private void populateMonthComboBox() {
		ObservableList<String> months = FXCollections.observableArrayList();
		for (int i = 1; i <= 12; i++) {
			months.add(String.format("%02d", i)); // Formats the month as two digits
		}
		monthComboBox.setItems(months); // Set all items at once
	}

	/**
	 * Populates the year ComboBox with years from the current year up to 20 years
	 * into the future.
	 */
	private void populateYearComboBox() {
		int currentYear = java.time.Year.now().getValue();

		ArrayList<String> years = new ArrayList<>();
		for (int i = currentYear; i <= currentYear + 20; i++) {
			years.add(String.valueOf(i)); // Add as string if your ComboBox type is String
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years)); // Set all items at once
	}

	/**
	 * This method is used to hide/show all elements but the progress indicator and
	 * its label
	 * 
	 * @param visible
	 */
	private void setVisible(boolean visible) {
		progressIndicator.setVisible(!visible);
		waitLabel.setVisible(!visible);
		visaImage.setVisible(visible);
		mastercardImage.setVisible(visible);
		amexImage.setVisible(visible);
		label1.setVisible(visible);
		label2.setVisible(visible);
		label3.setVisible(visible);
		label4.setVisible(visible);
		label5.setVisible(visible);
		amountLbl.setVisible(visible);
		processPayment.setVisible(visible);
		cardNumber1Txt.setVisible(visible);
		cardNumber2Txt.setVisible(visible);
		cardNumber3Txt.setVisible(visible);
		cardNumber4Txt.setVisible(visible);
		cvvTxt.setVisible(visible);
		holderNameTxt.setVisible(visible);
		monthComboBox.setVisible(visible);
		yearComboBox.setVisible(visible);

	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	/**
	 * Initializes the controller class. This method is called after the FXML file
	 * has been loaded. Sets up the month and year ComboBoxes and formats the card
	 * number and CVV text fields.
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

		populateMonthComboBox();
		populateYearComboBox();
		setupTextField(cardNumber1Txt, cardNumber2Txt);
		setupTextField(cardNumber2Txt, cardNumber3Txt);
		setupTextField(cardNumber3Txt, cardNumber4Txt);
		setupTextField(cardNumber4Txt, null);
		cvvTxt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches(digitsOnly)) { // If new value is not a digit
				cvvTxt.setText(newValue.replaceAll("[^\\d]", "")); // Remove non-digits
			} else if (newValue.length() > 3) { // If new value has more than 3 digits
				cvvTxt.setText(newValue.substring(0, 3)); // Truncate to 3 digits
			}
		});

		// setting and hiding the porgress indicator and its label
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));
		progressIndicator.setVisible(false);
		waitLabel.setText("Proccessing Payment...");
		waitLabel.setAlignment(Pos.CENTER);
		waitLabel.layoutXProperty().bind(pane.widthProperty().subtract(waitLabel.widthProperty()).divide(2));
		waitLabel.setVisible(false);
	}

	/**
	 * Loads booking and visitor information before the screen is displayed.
	 *
	 * @param information The booking and visitor information passed to the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		if (information != null && information instanceof Booking) {
			booking = (Booking) information;
			updateAmountLabel();
		}

	}

	/**
	 * Returns the title of the screen.
	 *
	 * @return A string representing the title of the Payment Screen.
	 */
	@Override
	public String getScreenTitle() {
		return "Payment Screen";
	}

}