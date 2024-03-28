package clientSide.gui;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import clientSide.control.BookingController;
import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import clientSide.entities.ParkVisitor;
import clientSide.entities.ParkVisitor.VisitorType;
import common.communication.Communication;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import common.entities.Booking;
import common.entities.Park;
import common.entities.Booking.VisitType;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * This class is a controller class for the booking reservations process. It
 * includes all the functionality for getting a new booking details, validating
 * them and sending, if necessary, to the booking logic controller for database
 * operations.
 */
public class BookingScreenController extends AbstractScreen implements Stateful {
	private BookingController control; // controller
	private ObservableList<Park> parksList; // list of the parks
	private ObservableList<String> parksStrings; // list of the parks as strings for the parks combox box
	private ObservableList<LocalTime> hours; // list of booking hours

	// the user enters the screen with a visitor instance or only with an id number
	private ParkVisitor visitor;
	private boolean isGroupReservation; // determines if this is a regular or guided group

	// booking objects and data
	private Booking booking;
	private String bookingId;
	private int parkIndexInCombobox;

	private static final String digitsOnly = "\\d+";
	private static final String lettersInput = "[a-zA-Z]+( [a-zA-Z]+)?";
	private static final String emailInput = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	private static final String phoneInput = "^(052|054|050)\\d{7}$";

	/**
	 * Constructor, initializes the booking controller instance
	 */
	public BookingScreenController() {
		control = BookingController.getInstance();
	}

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Label nameLbl, dateLbl, emailLbl, hourLbl, parkLbl, phoneLbl, visitorsLbl, titleLbl, bookingLbl, typeLbl,
			waitLabel;
	@FXML
	private Button backButton, makeReservationBtn;
	@FXML
	private DatePicker datePicker;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private ComboBox<LocalTime> hourCombobox;
	@FXML
	private ComboBox<String> parkComboBox;
	@FXML
	private TextField firstNameTxt, lastNameTxt, emailTxt, phoneTxt, visitorsTxt;
	@FXML
	private Pane pane;
	@FXML
	private ProgressIndicator progressIndicator;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * This method is called after an event of clicking on "Make Reservation" button
	 * is occuring
	 * 
	 * @param event
	 */
	void makeReservation(ActionEvent event) {
		// firstly, validating the details the user entered
		if (!validateDetails()) {
			event.consume();
			return;
		}

		// creating a booking object with the entered details
		makeBookingObject();

		// checking if the booking is for less than 24 hours from now
		// in this case: making the visitor understand that editing won't be allowed
		// and the booking will be automatically confirmed
		LocalDateTime bookingTime = LocalDateTime.of(booking.getDayOfVisit(), booking.getTimeOfVisit());
		LocalDateTime now = LocalDateTime.now();
		if (Math.abs(Duration.between(bookingTime, now).toHours()) <= control.reminderSendingTime) {
			int choise = showConfirmationAlert(
					"Your reservation occurs in less than " + control.reminderSendingTime + " hours."
							+ "\nIf we find place for your reservation, it will be"
							+ "\nautomatically confirmed and won't be able to be"
							+ "\nedited or cancelled.",
					Arrays.asList("Cancel", "Ok, Continue"));
			switch (choise) {
			case 1: // chose to cancel
				event.consume();
				return;

			case 2: // chose to continue
				break;
			}
		}

		// starting the booking process
		setVisible(false); // showing the progress indicator
		AtomicBoolean isAvailable = new AtomicBoolean(false);

		// moving the data fetching operation to a background thread
		new Thread(() -> {
			// checking the park availability for the chosen date and time
			// if the date is available for this booking
			// the new booking is INSERTED to the table in order to save its spot
			boolean availability = control.checkAndInsertNewBooking(booking);
			isAvailable.set(availability);

			// once fetching is complete, updating the UI on the JavaFX Application Thread
			Platform.runLater(() -> {
				setVisible(true); // hiding the progress indicator
				if (!isAvailable.get()) { // if the entered date and time are not available
					dateIsNotAvailable();
				} else { // if the date and time are available
					dateIsAvailable();
				}
			});
		}).start();
	}

	/**
	 * This method is called in case the chosen date and time are available at the
	 * chosen park
	 */
	private void dateIsAvailable() {
		// calculating the final price for the booking. Sending visitor's type cause the
		// price defers between regular and guided groups
		int discountPrice = control.calculateFinalDiscountPrice(booking, isGroupReservation, false);
		int preOrderPrice = control.calculateFinalDiscountPrice(booking, true, true);

		// creating the pop up message
		String payMessage = "Woohoo! Your reservation is now set!";
		if (isGroupReservation) {
			payMessage += "\nPay now and get a special discount for paying ahead:";
			payMessage += "\n        Your reservation's final price: " + discountPrice + "$";
			payMessage += "\n        Your reservetion's price after paying ahead discount: " + preOrderPrice + "$";
		} else {
			payMessage += "\nYour reservation's final price (after pre-order discount) is: " + discountPrice + "$";
		}

		int choise = showConfirmationAlert(payMessage,
				Arrays.asList("Pay Now", "Pay Upon Arrival", "Drop Reservation"));

		switch (choise) {
		// chose to pay now
		case 1: {
			booking.setPaid(true);
			booking.setFinalPrice(isGroupReservation ? preOrderPrice : discountPrice);

			waitLabel.setText("Opening the Payment System");
			setVisible(false);

			PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(2));
			pause.setOnFinished(e -> {
				try {
					new Thread(() -> {
						// updating the payment columns in the database
						control.updateBookingPayment(booking);
					}).start();

					// showing the payment screen
					ScreenManager.getInstance().showScreen("PaymentSystemScreenController",
							"/clientSide/fxml/PaymentSystemScreen.fxml", true, true,
							new Pair<Booking, String>(booking, "online"));
				} catch (StatefulException | ScreenException e1) {
					e1.printStackTrace();
				}
			});
			pause.play();
			break;
		}

		// chose to pay upon arrival
		case 2: {
			booking.setPaid(false);
			booking.setFinalPrice(discountPrice);

			waitLabel.setText("Processing Your Reservation");
			setVisible(false);

			PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(2));
			pause.setOnFinished(e -> {
				try {
					new Thread(() -> {
						// updating the payment columns in the database
						control.updateBookingPayment(booking);
						// sending a notification
						control.sendNotification(booking, false);
					}).start();

					// showing the confirmation screen
					ScreenManager.getInstance().showScreen("ConfirmationScreenController",
							"/clientSide/fxml/ConfirmationScreen.fxml", true, false, booking);
				} catch (StatefulException | ScreenException e1) {
					e1.printStackTrace();
				}
			});
			pause.play();
			break;
		}

		// chose to drop reservation
		case 3: {
			// deleting the new booking from the database cause it wat inserted in order to
			// save the spot for the visitor, and returning to acount screen
			control.deleteBooking(booking, Communication.activeBookings);
			returnToPreviousScreen(null);
			return;
		}
		}
	}

	/**
	 * This method is called in case the chosen date and time are not available at
	 * the chosen park
	 */
	private void dateIsNotAvailable() {
		// creating a pop up message for the user to choose what to do next
		int choise = showConfirmationAlert(
				"We care for your experience in " + booking.getParkBooked().getParkName()
						+ " Park, so we limit the volume of visitors in the park."
						+ "\nUnfortunately, the date and time you chose are not available at this moment.",
				Arrays.asList("Exit", "See Available Dates and Times", "Have a Peek on the Waiting List"));
		switch (choise) {
		// chose to exit
		case 1: {
			returnToPreviousScreen(null);
			break;
		}

		// chose to reschedule (see available dates and times)
		case 2: {
			try {
				ScreenManager.getInstance().showScreen("RescheduleScreenController",
						"/clientSide/fxml/RescheduleScreen.fxml", true, true, booking);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
			break;
		}

		// chose to enter the waiting list
		case 3: {
			// showing the waiting list screen
			try {
				ScreenManager.getInstance().showScreen("WaitingListScreenController",
						"/clientSide/fxml/WaitingListScreen.fxml", true, true, booking);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
		}
	}

	@FXML
	/**
	 * This method is called after an event is created with clicking on the Back
	 * button. Returns the user to the previous screen
	 * 
	 * @param event
	 */
	void returnToPreviousScreen(ActionEvent event) {
		// if the user is an individual (not a group guide) and entered from the main
		// screen
		if (ScreenManager.getInstance().whoIsBefore().equals("MainScreenController")) {
			// logging him out
			GoNatureUsersController.getInstance().logoutUser();
			try {
				ScreenManager.getInstance().goToPreviousScreen(false, false);
			} catch (ScreenException | StatefulException e) {
				e.printStackTrace();
			}
		} else { // return to account screen
			try {
				ScreenManager.getInstance().goToPreviousScreen(false, false);
			} catch (ScreenException | StatefulException e) {
				e.printStackTrace();
			}
		}
	}

	/////////////////////////////////////////////////////
	/// JAVAFX METHODS FOR CONTROLLING FLOW AND FOCUS ///
	/////////////////////////////////////////////////////

	@FXML
	/**
	 * If the park combobox pressed with tab, moves to the date picker
	 * 
	 * @param event
	 */
	void parkTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			datePicker.requestFocus();
		}
	}

	@FXML
	/**
	 * If the date picker pressed with tab, moves to the hour combobox
	 * 
	 * @param event
	 */
	void dateTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			hourCombobox.requestFocus();
		}
	}

	@FXML
	/**
	 * If the hour combobox pressed with tab, moves to the first name text
	 * 
	 * @param event
	 */
	void hourTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			if (!firstNameTxt.isDisabled())
				firstNameTxt.requestFocus();
			else
				visitorsTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * If the first name text pressed with tab, moves to the last name text
	 * 
	 * @param event
	 */
	void firstNameTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			lastNameTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * If the last name text pressed with tab, moves to the visitors text
	 * 
	 * @param event
	 */
	void lastNameTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			visitorsTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * If the visitors text pressed with tab, moves to the phone text
	 * 
	 * @param event
	 */
	void visitorsTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			phoneTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * If the phone text pressed with tab, moves to the email text
	 * 
	 * @param event
	 */
	void phoneTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			emailTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * If the email text pressed with tab, moves to the button
	 * 
	 * @param event
	 */
	void emailTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			makeReservationBtn.requestFocus();
		}
	}

	@FXML
	/**
	 * If the pane clicked, all focus from the GUI components will be disabled
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	/**
	 * transfers the focus from the button to the pane, if tab pressed
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
	 * ignores event of pressing a tab on the pane
	 * 
	 * @param event
	 */
	void paneTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
		}
	}

	@FXML
	/**
	 * When a park is chosen, updating the background image to this park image
	 * 
	 * @param event
	 */
	void parkChosen(ActionEvent event) {
		parkIndexInCombobox = parkComboBox.getSelectionModel().getSelectedIndex();
		Park parkChosen = parksList.get(parkIndexInCombobox);
		ImageView backgroundImage = new ImageView(
				new Image("/" + ParkController.getInstance().nameOfTable(parkChosen) + ".jpg"));

		backgroundImage.fitWidthProperty().bind(ScreenManager.getInstance().getStage().widthProperty());
		backgroundImage.fitHeightProperty().bind(ScreenManager.getInstance().getStage().heightProperty());
		backgroundImage.setPreserveRatio(false);
		backgroundImage.setOpacity(0.2);

		if (pane.getChildren().get(0) instanceof ImageView) {
			pane.getChildren().remove(0);
		}
		pane.getChildren().add(0, backgroundImage);

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
		parkLbl.setVisible(visible);
		parkComboBox.setVisible(visible);
		dateLbl.setVisible(visible);
		datePicker.setVisible(visible);
		hourLbl.setVisible(visible);
		hourCombobox.setVisible(visible);
		nameLbl.setVisible(visible);
		firstNameTxt.setVisible(visible);
		lastNameTxt.setVisible(visible);
		visitorsLbl.setVisible(visible);
		visitorsTxt.setVisible(visible);
		phoneLbl.setVisible(visible);
		phoneTxt.setVisible(visible);
		emailLbl.setVisible(visible);
		emailTxt.setVisible(visible);
		makeReservationBtn.setDisable(!visible);
	}

	/**
	 * This method is called after the button for make reservations is clicked
	 * Checks and validates all the reservation details
	 * 
	 * @return true if all details are valid, false if not
	 */
	private boolean validateDetails() {
		// set styles to regular
		parkComboBox.setStyle(setFieldToRegular());
		datePicker.setStyle(setFieldToRegular());
		hourCombobox.setStyle(setFieldToRegular());
		firstNameTxt.setStyle(setFieldToRegular());
		lastNameTxt.setStyle(setFieldToRegular());
		visitorsTxt.setStyle(setFieldToRegular());
		emailTxt.setStyle(setFieldToRegular());
		phoneTxt.setStyle(setFieldToRegular());

		boolean valid = true;

		String error = "Please make sure you:\n";
		// checking if the user chose a park from the list
		if (parkComboBox.getValue() == null) {
			parkComboBox.setStyle(setFieldToError());
			error += "• choose a park from the parks list\n";
			valid = false;
		}
		// checking if the user chose a date from the date picker
		if (datePicker.getValue() == null) {
			datePicker.setStyle(setFieldToError());
			error += "• choose a date from the date picker\n";
			valid = false;
		}
		// checking if the chosen date is valid (not past, and in future range)
		if (datePicker.getValue() != null) {
			if (datePicker.getValue().compareTo(LocalDate.now()) < 0) { // past
				datePicker.setStyle(setFieldToError());
				error += "• choose a date that is " + LocalDate.now() + " and on\n";
				valid = false;
			} else { // future
				// calculating the date that is in the future allowed range
				LocalDate maximumFutureRange = (LocalDate.now()).plusMonths(control.futureBookingsRange);

				if (datePicker.getValue().compareTo(maximumFutureRange) > 0) {
					datePicker.setStyle(setFieldToError());
					error += "• choose a date that is before " + maximumFutureRange + "\n";
					valid = false;
				}
			}
		}

		// checking if the chosen hour is valid
		if (hourCombobox.getValue() == null) {
			hourCombobox.setStyle(setFieldToError());
			error += "• choose an hour\n";
			valid = false;
		} else {
			if (datePicker.getValue() != null && datePicker.getValue().equals(LocalDate.now())
					&& hourCombobox.getValue().compareTo(LocalTime.now()) < 0) {
				hourCombobox.setStyle(setFieldToError());
				error += "• choose a valid hour\n";
				valid = false;
			}
		}

		// checking the first name
		if (!firstNameTxt.isDisabled() && (firstNameTxt.getText() == null || firstNameTxt.getText().isEmpty()
				|| !firstNameTxt.getText().matches(lettersInput))) {
			firstNameTxt.setStyle(setFieldToError());
			error += "• enter a valid first name\n";
			valid = false;
		}

		// checking the last name
		if (!lastNameTxt.isDisabled() && (lastNameTxt.getText() == null || lastNameTxt.getText().isEmpty()
				|| !lastNameTxt.getText().matches(lettersInput))) {
			lastNameTxt.setStyle(setFieldToError());
			error += "• enter a valid last name\n";
			valid = false;
		}

		// checking the visitors number
		if (visitorsTxt.getText() == null || visitorsTxt.getText().isEmpty()
				|| !visitorsTxt.getText().matches(digitsOnly)) {
			visitorsTxt.setStyle(setFieldToError());
			error += "• enter a digit-only number of visitors\n";
			valid = false;
		} else {
			if (isGroupReservation) {
				if (Integer.parseInt(visitorsTxt.getText()) < control.minimumVisitorsInReservation
						|| Integer.parseInt(visitorsTxt.getText()) > control.maximumVisitorsInGroupReservation) {
					visitorsTxt.setStyle(setFieldToError());
					error += "• enter a number of visitors in range of " + control.minimumVisitorsInReservation + " to "
							+ control.maximumVisitorsInGroupReservation + "\n";
					valid = false;
				}
			} else {
				parkIndexInCombobox = parkComboBox.getSelectionModel().getSelectedIndex();
				Park parkDesired = parksList.get(parkIndexInCombobox);
				if (Integer.parseInt(visitorsTxt.getText()) < control.minimumVisitorsInReservation) {
					visitorsTxt.setStyle(setFieldToError());
					error += "• group reservations have to have at least " + control.minimumVisitorsInReservation
							+ " visitors\n";
					valid = false;
				}
				if (Integer.parseInt(visitorsTxt.getText()) > parkDesired.getMaximumOrders()) {
					visitorsTxt.setStyle(setFieldToError());
					error += "• reservations can't exceed a total of " + parkDesired.getMaximumOrders() + " visitors\n";
					valid = false;
				}
			}
		}

		// checking the email address
		if (emailTxt.getText() == null || emailTxt.getText().isEmpty() || !emailTxt.getText().matches(emailInput)) {
			emailTxt.setStyle(setFieldToError());
			error += "• enter a valid email address\n";
			valid = false;
		}

		// checking the phone number
		if (phoneTxt.getText() == null || phoneTxt.getText().isEmpty() || !phoneTxt.getText().matches(phoneInput)) {
			phoneTxt.setStyle(setFieldToError());
			error += "• enter a valid phone number\n";
			valid = false;
		}

		if (!valid)
			showErrorAlert(error);
		return valid;
	}

	/**
	 * This method takes all the data from the GUI components and builds a new
	 * booking object from it.
	 */
	private void makeBookingObject() {
		parkIndexInCombobox = parkComboBox.getSelectionModel().getSelectedIndex();
		Park parkDesired = parksList.get(parkIndexInCombobox);
		if (booking != null) {
			booking.setDayOfVisit(datePicker.getValue());
			booking.setTimeOfVisit(hourCombobox.getValue());
			booking.setNumberOfVisitors(Integer.parseInt(visitorsTxt.getText()));
			booking.setEmailAddress(emailTxt.getText());
			booking.setPhoneNumber(phoneTxt.getText());
			booking.setParkBooked(parkDesired);
			booking.setFirstName(prepareName(firstNameTxt.getText()));
			booking.setLastName(prepareName(lastNameTxt.getText()));
		} else {
			booking = new Booking(bookingId, datePicker.getValue(), hourCombobox.getValue(), LocalDate.now(),
					visitor == null ? VisitType.INDIVIDUAL
							: (visitor.getVisitorType() == VisitorType.GROUPGUIDE ? VisitType.GROUP
									: VisitType.INDIVIDUAL),
					Integer.parseInt(visitorsTxt.getText()), visitor.getIdNumber(), prepareName(firstNameTxt.getText()),
					prepareName(lastNameTxt.getText()), emailTxt.getText(), phoneTxt.getText(), -1, false, false, null,
					null, false, null, parkDesired);
		}
	}

	/**
	 * This method gets a name string and converts it to capital letter form
	 * 
	 * @param name
	 * @return the prepared name
	 */
	private String prepareName(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}

		StringBuilder titleCase = new StringBuilder(name.length());
		boolean nextTitleCase = true;

		for (char c : name.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			} else {
				c = Character.toLowerCase(c);
			}
			titleCase.append(c);
		}

		return titleCase.toString();
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

	/**
	 * This method gets a text field and makes it recoginze letters/spaces only
	 * 
	 * @param textField
	 */
	protected void setupTextFieldToLettersOnly(TextField textField) {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue != null && !newValue.matches(lettersInput)) {
					textField.setText(newValue.replaceAll("[^a-zA-Z ]", ""));
				}
			}
		});
	}

	/**
	 * This method sets the hours combo box with the relevant hours for visiting
	 */
	protected void setHours() {
		ArrayList<LocalTime> hoursString = new ArrayList<>();
		for (int hour = control.openHour; hour <= control.closeHour; hour++) {
			hoursString.add(LocalTime.of(hour, 0));
			// sets the minutes intervals. if 0 - only in full hours
			if (hour != control.closeHour && control.minutes != 0)
				hoursString.add(LocalTime.of(hour, control.minutes));
		}
		hours = FXCollections.observableArrayList(hoursString);
		hourCombobox.setItems(hours);
	}

	////////////////////////////////////////////
	/// ABSTRACT SCREEN AND STATEFUL METHODS ///
	////////////////////////////////////////////

	@Override
	/**
	 * This method is called if this screen needs to save its current state for
	 * later restoring
	 */
	public void saveState() {
		makeBookingObject();
		control.setBooking(booking);
		control.setParkIndexInCombobox(parkIndexInCombobox);
		control.setVisitor(visitor);
		control.setSavedState(true);
	}

	@Override
	/**
	 * This method is called if this screen saved its past state, and now needs to
	 * restore it
	 */
	public void restoreState() {
		// getting the booking and visitor details
		booking = control.getBooking();
		visitor = control.getVisitor();
		parkIndexInCombobox = control.getParkIndexInCombobox();
		control.setSavedState(false);

		// setting all the info into the components of the screen
		bookingLbl.setText("Booking ID: " + booking.getBookingId());
		parkComboBox.getSelectionModel().select(parkIndexInCombobox);
		datePicker.setValue(booking.getDayOfVisit());
		hourCombobox.setValue(booking.getTimeOfVisit());
		visitorsTxt.setText(((Integer) booking.getNumberOfVisitors()).toString());
		emailTxt.setText(booking.getEmailAddress());
		phoneTxt.setText(booking.getPhoneNumber());
		firstNameTxt.setText(booking.getFirstName());
		lastNameTxt.setText(booking.getLastName());
		if (booking.getVisitType() == VisitType.GROUP) {
			firstNameTxt.setDisable(true);
			lastNameTxt.setDisable(true);
		}
		isGroupReservation = booking.getVisitType() == VisitType.GROUP ? true : false;
		typeLbl.setText((isGroupReservation == true ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
				+ booking.getIdNumber());

		// setting the background image of the park chosen before
		parkChosen(null);
	}

	@Override
	/**
	 * This method is called by the FXML and JAVAFX and initializes the screen
	 */
	public void initialize() {
		// setting the park details in the parks combobox
		Pair<ObservableList<Park>, ObservableList<String>> pair = control.fetchParks();
		parksList = pair.getKey();
		parksStrings = pair.getValue();
		parkComboBox.setItems(parksStrings);

		// setting the hours combobox
		if (hours == null)
			setHours();

		// initializing the image component of the logo and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		// setting all the labels to right alignment
		nameLbl.getStyleClass().add("label-center-right");
		dateLbl.getStyleClass().add("label-center-right");
		emailLbl.getStyleClass().add("label-center-right");
		hourLbl.getStyleClass().add("label-center-right");
		parkLbl.getStyleClass().add("label-center-right");
		phoneLbl.getStyleClass().add("label-center-right");
		visitorsLbl.getStyleClass().add("label-center-right");
		bookingLbl.getStyleClass().add("label-center-right");

		// setting texts to font family and size
		parkComboBox.getStyleClass().add("combo-box-text");
		hourCombobox.getStyleClass().add("combo-box-text");

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// setting text fields to recognize specific chars
		setupTextFieldToDigitsOnly(visitorsTxt);
		setupTextFieldToDigitsOnly(phoneTxt);
		setupTextFieldToLettersOnly(firstNameTxt);
		setupTextFieldToLettersOnly(lastNameTxt);

		// setting the labels
		waitLabel.setAlignment(Pos.CENTER);
		waitLabel.layoutXProperty().bind(pane.widthProperty().subtract(waitLabel.widthProperty()).divide(2));
		waitLabel.setText("Checking Park Availability");
		waitLabel.setStyle("-fx-text-alignment: center;");

		// setting the porgress indicator
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));

		setVisible(true);

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	/**
	 * This method is called in order to set pre-info into the GUI components,
	 * according to the object parameter it gets
	 * 
	 * @param information contains one of the following objects: ParkVisitor if the
	 *                    user entered this screen from his account screen, String
	 *                    if the user entered this string from the main screen,
	 *                    Booking if the user returned from the waiting list or the
	 *                    reschedule screen
	 */
	public void loadBefore(Object information) {
		// in case the user is logged in (as a group guide) and enters this screen from
		// his account screen
		if (information instanceof ParkVisitor) {
			visitor = (ParkVisitor) information;

			// setting the visitor's details into the text fields
			firstNameTxt.setText(visitor.getFirstName());
			lastNameTxt.setText(visitor.getLastName());
			if (visitor.getVisitorType() == VisitorType.GROUPGUIDE) {
				firstNameTxt.setDisable(true);
				lastNameTxt.setDisable(true);
			}
			emailTxt.setText(visitor.getEmailAddress());
			phoneTxt.setText(visitor.getPhoneNumber());

			// setting the reservation type
			isGroupReservation = visitor.getVisitorType() == VisitorType.GROUPGUIDE ? true : false;
			typeLbl.setText((isGroupReservation ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
					+ visitor.getIdNumber());

			// generating booking id
			bookingId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			bookingLbl.setText("Booking ID: " + bookingId);
		}

		// in case the user returned from the waiting list or reschedule screen
		if (information instanceof Booking) {
			booking = (Booking) information;

			// setting all the info into the components of the screen
			bookingLbl.setText("Booking ID: " + booking.getBookingId());
			parkComboBox.getSelectionModel().select(parksList.indexOf(booking.getParkBooked()));
			datePicker.setValue(booking.getDayOfVisit());
			hourCombobox.setValue(booking.getTimeOfVisit());
			visitorsTxt.setText(((Integer) booking.getNumberOfVisitors()).toString());
			emailTxt.setText(booking.getEmailAddress());
			phoneTxt.setText(booking.getPhoneNumber());
			firstNameTxt.setText(booking.getFirstName());
			lastNameTxt.setText(booking.getLastName());
			firstNameTxt.setDisable(true);
			lastNameTxt.setDisable(true);
			isGroupReservation = booking.getVisitType() == VisitType.GROUP ? true : false;
			typeLbl.setText((isGroupReservation ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
					+ booking.getIdNumber());
		}
	}

	@Override
	/**
	 * This method returns the screen's name
	 */
	public String getScreenTitle() {
		return "Booking Reservations";
	}
}