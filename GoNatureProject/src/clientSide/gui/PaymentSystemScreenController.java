package clientSide.gui;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.Booking;
import entities.ParkVisitor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

public class PaymentSystemScreenController extends AbstractScreen {
	private Booking booking;
	private ParkVisitor visitor;
	private Pair<Booking, ParkVisitor> pair;

	@FXML
	private Label amountLbl;
	@FXML
	private Button processPayment;
	@FXML
	private TextField cardNumber1Txt, cardNumber2Txt, cardNumber3Txt, cardNumber4Txt, cvvTxt, holderNameTxt;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private ComboBox<String> monthComboBox;
	@FXML
	private ComboBox<String> yearComboBox;

	/**
     * Processes the payment when the 'Process Payment' button is clicked.
     * Validates the payment information and navigates to the confirmation screen if validation passes.
     *
     * @param event The action event that triggers the payment process.
     */
	@FXML
	void processPayment(ActionEvent event) {
		boolean valid = paymentValidation();
		if (valid) {
			try {
				ScreenManager.getInstance().showScreen("ConfirmationScreenController",
						"/clientSide/fxml/ConfirmationScreen.fxml", true, false,
						StageSettings.defaultSettings("Confirmation"), pair);
			} catch (StatefulException | ScreenException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
     * Returns to the previous screen.
     *
     * @param event The action event that triggers the return.
     * @throws ScreenException if there is an issue navigating back.
     * @throws StatefulException if there is a state management issue during navigation.
     */
	@FXML
	void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
		ScreenManager.getInstance().goToPreviousScreen(false, true);

	}

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
	        yearComboBox.setStyle(setFieldToError()); 
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
		if (holderNameTxt.getText() == null || !holderNameTxt.getText().matches("^[a-zA-Z]+ [a-zA-Z]+$")) {
			holderNameTxt.setStyle(setFieldToError());
			showMessage += "\n- Name is not valid.";
			valid = false;
		}
		if (!valid) {
			showErrorAlert(ScreenManager.getInstance().getStage(), "Errors:" + showMessage);
		}
		return valid; // All validations passed
	}

	/**
     * Validates that a card number field contains exactly 4 digits.
     *
     * @param cardField The text field containing the card number to be validated.
     * @return true if the field is valid, false otherwise.
     */
	private boolean validateCardField(TextField cardField) {
		return cardField.getText() != null && cardField.getText().matches("\\d{4}");
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
     * Initializes the controller class. This method is called after the FXML file has been loaded.
     * Sets up the month and year ComboBoxes and formats the card number and CVV text fields.
     */
	@Override
	public void initialize() {
//		updateAmountLabel();
		populateMonthComboBox();
		populateYearComboBox();
		setupTextField(cardNumber1Txt, cardNumber2Txt);
		setupTextField(cardNumber2Txt, cardNumber3Txt);
		setupTextField(cardNumber3Txt, cardNumber4Txt);
		setupTextField(cardNumber4Txt, null);
		cvvTxt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) { // If new value is not a digit
				cvvTxt.setText(newValue.replaceAll("[^\\d]", "")); // Remove non-digits
			} else if (newValue.length() > 3) { // If new value has more than 3 digits
				cvvTxt.setText(newValue.substring(0, 3)); // Truncate to 3 digits
			}
		});
	}

	/**
	 * Configures a text field for credit card input, enforcing numerical input and automatic focus management.
	 * This method ensures that each text field only accepts digits and automatically moves the focus to the next field
	 * once the user has entered 4 digits, facilitating a smooth and efficient input process for the credit card number.
	 * If the text field is the last in the sequence (i.e., nextField is null), it does not attempt to move focus further.
	 *
	 * @param currentField The text field currently being configured. This field will accept only digit characters
	 *                     and is limited to a maximum of 4 digits.
	 * @param nextField The next text field to focus on once the currentField is fully populated. If currentField
	 *                  is the last in the sequence, this parameter should be null.
	 */
	private void setupTextField(TextField currentField, TextField nextField) {
		currentField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
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
	
//	public void updateAmountLabel() {
//		amountLbl.setText(String.valueOf(booking.getFinalPrice()) + "$"); // Update the label's text programmatically
//    }

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
     * Populates the year ComboBox with years from the current year up to 20 years into the future.
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
     * Loads booking and visitor information before the screen is displayed.
     *
     * @param information The booking and visitor information passed to the screen.
     */
	@SuppressWarnings("unchecked")
	@Override
	public void loadBefore(Object information) {
		if (information != null && information instanceof Pair) {
			pair = (Pair<Booking, ParkVisitor>) information;
			booking = pair.getKey();
			visitor = pair.getValue();
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
}
