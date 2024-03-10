package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import clientSide.control.BookingController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Booking.VisitType;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class BookingScreenController extends AbstractScreen implements Stateful {

	private BookingController control; // controller
	private ObservableList<Park> parksList;
	private ObservableList<String> parksStrings;
	private ObservableList<LocalTime> hours;

	// date validation parameters
	private final int futureBookingsRange = 4; // 4 month
	private final int openHour = 4;
	private final int closeHour = 18;
	private final int minimumVisitorsInReservation = 1;
	private final int maximumVisitorsInReservation = 15;

	// data objects
	private ParkVisitor visitor;
	private Booking booking;
	private String bookingId;
	private int parkIndexInCombobox;
	private boolean isGroupReservation; // determines if this is a regular or guided group

	/**
	 * Constructor, initializes the booking controller instance
	 */
	public BookingScreenController() {
		control = BookingController.getInstance();
	}

	/// FXML AND JAVAFX COMPONENTS
	@FXML
	private Label nameLbl, dateLbl, emailLbl, hourLbl, parkLbl, phoneLbl, visitorsLbl, titleLbl, bookingLbl, typeLbl;
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
	/**
	 * This method is called after an event of clicking on "Make Reservation" button
	 * is occuring
	 * 
	 * @param event
	 */
	void makeReservation(ActionEvent event) {
		// firstly, validating the details the user entered
		boolean valid = validateDetails();
		if (!valid) {
			event.consume();
			return;
		}
		// creating a booking object with the entered details
		makeBookingObject();

		// checking the park availability for the chosen date and time
		boolean isAvailable = control.checkAvailability(booking);

		// if the entered date and time are not available
		if (!isAvailable) {
			// creating a pop up message for the user to choose what to do next
			int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"Unfortunately, the date and time chosen are not available in "
							+ booking.getParkBooked().getParkName() + " Park",
					Arrays.asList("Exit Reservations", "Reschedule the Booking", "Enter the Waiting List"));
			switch (choise) {
			// chose to exit
			case 1:
				event.consume();
				// HERE: should be returned to his account page
				// chose to reschedule
				break;
			case 2:
				datePicker.setValue(null);
				hourCombobox.setValue(null);
				datePicker.setStyle(setFieldToError());
				hourCombobox.setStyle(setFieldToError());
				break;
			// chose to enter the waiting list
			case 3:
				if (control.insertToWaitingList(booking, visitor)) {
					event.consume();
					showInformationAlert(ScreenManager.getInstance().getStage(),
							"Your reservation entered the waiting list of " + booking.getParkBooked().getParkName()
									+ " successfully.\nWe will notify you if we'll find a place for your group.\nHoping to see you soon!");
					setDisable();
					// now going back to the account screen
				}
			}
		}

		// if the date and time are available
		else {
			// first inserting the new booking to the database to update capacities
			control.insertNewBooking(booking, visitor);

			// calculating the final price for the booking
			// sending visitor cause the price defers between regular and guided groups
			int finalPrice = control.calculateFinalRegularPrice(booking, visitor.getVisitorType());
			int discountPrice = control.calculateFinalDiscountPrice(booking, visitor.getVisitorType());

			// creating the pop up message
			String payMessage = "Your reservation to " + booking.getParkBooked().getParkName() + " Park is almost set.";
			payMessage += "\nPay now and get a special discount for pre-ordering:";
			payMessage += "\n        Your reservation final price: " + finalPrice + "$";
			payMessage += "\n        Your reservetion price after the special discount: " + discountPrice + "$";
			int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(), payMessage,
					Arrays.asList("Pay Now and Get Discount", "Pay Upon Arrival"));

			switch (choise) {
			// chose to pay now and get discount
			case 1:
				booking.setPaid(true);
				booking.setFinalPrice(discountPrice);
				// updating the payment columns in the database
				control.updateBookingPayment(booking);
				showInformationAlert(ScreenManager.getInstance().getStage(), "Your payment is accepted. Thank You!");
				break;

			// chose to pay upon arrival
			case 2:
				booking.setPaid(false);
				booking.setFinalPrice(finalPrice);
				// updating the payment columns in the database
				control.updateBookingPayment(booking);
				break;
			}

			// showing the confirmation screen
			try {
				ScreenManager.getInstance().showScreen("ConfirmationScreenController",
						"/clientSide/fxml/ConfirmationScreen.fxml", true, false,
						StageSettings.defaultSettings("Confirmation"),
						new Pair<Booking, ParkVisitor>(booking, visitor));
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
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
		try {
			ScreenManager.getInstance().goToPreviousScreen(false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/// JAVAFX METHODS FOR CONTROLLING FLOW AND FOCUS ///

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
	 * When a park is chosen, updating the background to this park
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

	/// INSTANCE METHODS ///
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
		visitorsTxt.setStyle(setFieldToRegular());
		visitorsTxt.setStyle(setFieldToRegular());
		emailTxt.setStyle(setFieldToRegular());
		phoneTxt.setStyle(setFieldToRegular());

		boolean valid = true;

		String error = "Please make sure you:\n";
		// checking if the user chose a park from the list
		if (parkComboBox.getValue() == null) {
			parkComboBox.setStyle(setFieldToError());
			error += "• choose a park from the parks list\n";
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
				LocalDate maximumFutureRange = (LocalDate.now()).plusMonths(futureBookingsRange);

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

		// checking the visitors number
		if (visitorsTxt.getText().isEmpty() || !visitorsTxt.getText().matches("\\d+")) {
			visitorsTxt.setStyle(setFieldToError());
			error += "• enter a digit-only number of visitors\n";
			valid = false;
		} else {
			if (Integer.parseInt(visitorsTxt.getText()) < minimumVisitorsInReservation
					|| Integer.parseInt(visitorsTxt.getText()) > maximumVisitorsInReservation) {
				visitorsTxt.setStyle(setFieldToError());
				error += "• enter a number of visitors in range of " + minimumVisitorsInReservation + " to "
						+ maximumVisitorsInReservation + "\n";
				valid = false;
			}
		}

		// checking the email address
		if (emailTxt.getText().isEmpty()
				|| !emailTxt.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			emailTxt.setStyle(setFieldToError());
			error += "• enter a valid email address\n";
			valid = false;
		}

		// checking the phone number
		if (phoneTxt.getText().isEmpty() || !phoneTxt.getText().matches("^(052|054|050)\\d{7}$")) {
			phoneTxt.setStyle(setFieldToError());
			error += "• enter a valid phone number\n";
			valid = false;
		}

		if (!valid)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
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
		} else {
			booking = new Booking(bookingId, datePicker.getValue(), hourCombobox.getValue(), LocalDate.now(),
					visitor.getVisitorType() == VisitorType.GROUPGUIDE ? VisitType.GROUP : VisitType.INDIVIDUAL,
					Integer.parseInt(visitorsTxt.getText()), visitor.getFirstName(), visitor.getLastName(),
					emailTxt.getText(), phoneTxt.getText(), -1, false, false, null, null, false, null, parkDesired);
		}
	}

	/// ABSTRACT SCREEN AND STATEFUL METHODS ///

	@Override
	/**
	 * This method is called if this screen needs to save its current state for
	 * later restoring
	 */
	public void saveState() {
		makeBookingObject();
		control.setBooking(booking);
		control.setParkIndexInCombobox(parkIndexInCombobox);
		control.setSavedState(true);
		control.setVisitor(visitor);
	}

	@Override
	/**
	 * This method is called if this screen saved its past state, and now needs to
	 * restore it
	 */
	public void restoreState() {
		booking = control.getBooking();
		visitor = control.getVisitor();
		parkIndexInCombobox = control.getParkIndexInCombobox();
		control.setSavedState(false);

		parkComboBox.getSelectionModel().select(parkIndexInCombobox);
		datePicker.setValue(booking.getDayOfVisit());
		hourCombobox.setValue(booking.getTimeOfVisit());
		visitorsTxt.setText(((Integer) booking.getNumberOfVisitors()).toString());
		emailTxt.setText(booking.getEmailAddress());
		phoneTxt.setText(booking.getPhoneNumber());
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
		// initializing the image component and centering it
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

		// generating booking id
		bookingId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
		bookingLbl.setText("Booking ID: " + bookingId);
	}

	@Override
	/**
	 * This method returns the screen's name
	 */
	public String getScreenTitle() {
		return "Booking Reservations";
	}

	/**
	 * This method sets the hours combo box with the relevant hours for visiting
	 */
	private void setHours() {
		ArrayList<LocalTime> hoursString = new ArrayList<>();
		for (int hour = openHour; hour <= closeHour; hour++) {
			hoursString.add(LocalTime.of(hour, 0));
			if (hour != closeHour)
				hoursString.add(LocalTime.of(hour, 30));
		}
		hours = FXCollections.observableArrayList(hoursString);
		hourCombobox.setItems(hours);
	}

	@Override
	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	public void loadBefore(Object information) {
		// in case the user is logged in
		if (information instanceof ParkVisitor) {
			visitor = (ParkVisitor) information;
			// setting email and phone of the visitor into the text fields
			firstNameTxt.setText(visitor.getFirstName());
			lastNameTxt.setText(visitor.getLastName());
			firstNameTxt.setDisable(true);
			lastNameTxt.setDisable(true);
			emailTxt.setText(visitor.getEmailAddress());
			phoneTxt.setText(visitor.getPhoneNumber());
			// setting the reservation type
			isGroupReservation = visitor.getVisitorType() == VisitorType.GROUPGUIDE ? true : false;
			typeLbl.setText((isGroupReservation == true ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
					+ visitor.getIdNumber());
		}

		// in case the user in not logged in, entered only with id
		if (information instanceof String) {

		}

		if (information instanceof Booking) {
			// in case the visitor wants to edit his reservation
			// will arrive this screen after being in the booking managing screen
		}
	}

	/**
	 * TEMPORARY METHOD
	 */
	private void setDisable() {
		parkComboBox.setDisable(true);
		datePicker.setDisable(true);
		hourCombobox.setDisable(true);
		visitorsTxt.setDisable(true);
		emailTxt.setDisable(true);
		phoneTxt.setDisable(true);
		makeReservationBtn.setDisable(true);

	}

}