package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.BookingController;
import clientSide.control.ParkController;
import common.communication.Communication;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Booking.VisitType;
import entities.Park;
import entities.ParkVisitor;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * The BookingEditingScreenController is called after the visitor chose a
 * reservation to edit/cancel from the table view in the
 * BookingViewScreenController. In this screen, the visitor can modify his
 * reservation, check park availability and cancel his reservation.
 */
public class BookingEditingScreenController extends BookingScreenController {
	private BookingController control; // controller
	private ObservableList<Park> parksList; // list of the parks
	private ObservableList<String> parksStrings; // list of the parks as strings for the parks combox box
	private ObservableList<LocalTime> hours; // list of booking hours

	// the user enters the screen with a visitor instance or only with an id number
	private ParkVisitor visitor;
	private boolean isGroupReservation; // determines if this is a regular or guided group

	// booking objects and data
	private Booking booking;
	private int parkIndexInCombobox;

	/**
	 * Constructor, initializes the booking controller instance
	 */
	public BookingEditingScreenController() {
		control = BookingController.getInstance();
	}

	/// FXML AND JAVAFX COMPONENTS
	@FXML
	private Label dateLbl, hourLbl, parkLbl, visitorsLbl, titleLbl, bookingLbl, typeLbl;
	@FXML
	private Button backButton, cancelReservationBtn, updateReservationBtn, availabilityBtn;
	@FXML
	private DatePicker datePicker;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private ComboBox<LocalTime> hourCombobox;
	@FXML
	private ComboBox<String> parkComboBox;
	@FXML
	private TextField visitorsTxt;
	@FXML
	private Pane pane;

	/////////////////////
	/// EVENT METHODS ///
	/////////////////////

	@FXML
	/**
	 * This method is called after the user clicked on "Cancel Reservation" button
	 * 
	 * @param event
	 */
	void cancelReservation(ActionEvent event) {
		int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"You are about to cancel your " + booking.getParkBooked().getParkName() + "Park reservation for "
						+ booking.getDayOfVisit() + ", " + booking.getTimeOfVisit() + ".\nThis action can't be undone.",
				Arrays.asList("Don't Cancel", "Continue and Cancel"));

		switch (choise) {
		case 1: // chose not to cancel the reservation
			event.consume();
			return;

		case 2: // chose to cancel
			if (control.deleteBookingFromActiveTable(booking)) {
				if (control.insertBookingToCancelledTable(booking, Communication.userCancelled)) {
					// showing the cancellation screen
					try {
						ScreenManager.getInstance().showScreen("CancellationScreenController",
								"/clientSide/fxml/CancellationScreen.fxml", true, false,
								StageSettings.defaultSettings("Cancellation"),
								new Pair<Booking, ParkVisitor>(booking, visitor));
					} catch (StatefulException | ScreenException e) {
						e.printStackTrace();
					}
				}
			} else {

			}
			event.consume();
		}
	}

	@FXML
	/**
	 * This method is called after the user clicked on "Check Park Availability"
	 * button
	 * 
	 * @param event
	 */
	void availabilityClicked(ActionEvent event) {
		// first checking if the user changed any detail before sending a query request
		if (!checkIfChanged()) {
			showInformationAlert(ScreenManager.getInstance().getStage(),
					"You need to change one/more of the details in order to update your booking");
		} else { // if changed any of the details
			if (!validateDetails()) { // if details are not valid
				return;
			} else { // if details are valid
				Booking clone = booking.cloneBooking();
				Park parkDesired = parksList.get(parkComboBox.getSelectionModel().getSelectedIndex());
				clone.setParkBooked(parkDesired);
				clone.setDayOfVisit(datePicker.getValue());
				clone.setTimeOfVisit(hourCombobox.getValue());
				clone.setNumberOfVisitors(Integer.parseInt(visitorsTxt.getText()));

				if (control.checkParkAvailabilityForExistingBooking(booking, clone)) {
					showInformationAlert(ScreenManager.getInstance().getStage(),
							parkDesired.getParkName() + " Park is available for " + clone.getNumberOfVisitors()
									+ " visitors on " + clone.getDayOfVisit() + ", " + clone.getTimeOfVisit()
									+ "\nAvailability can change quickly due to high volume of orders");
				} else {
					showInformationAlert(ScreenManager.getInstance().getStage(),
							"Unfortunately, " + parkDesired.getParkName() + " Park is not available for "
									+ clone.getNumberOfVisitors() + " visitors on " + clone.getDayOfVisit() + ", "
									+ clone.getTimeOfVisit());
				}
			}
		}
	}

	@FXML
	/**
	 * This method is called after the user clicked on the "Update Reservation"
	 * button
	 * 
	 * @param event
	 */
	void updateReservation(ActionEvent event) {
		if (!checkIfChanged()) {
			showInformationAlert(ScreenManager.getInstance().getStage(),
					"You need to change one/more of the details in order to check park availability");
			return;
		}

		// making sure the user wants to replace his old booking with the new one
		int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"You are about to update your " + booking.getParkBooked().getParkName() + "Park reservation for "
						+ booking.getDayOfVisit() + ", " + booking.getTimeOfVisit() + ".\nThis action can't be undone.",
				Arrays.asList("Don't Update", "Ok, Continue"));

		switch (choise) {
		case 1: // chose not to update
			event.consume();
			return;

		case 2: // chose to update

			// first checking if the user changed any detail before sending a query request

			// if the user changed any of the details
			if (!validateDetails()) { // if details are not valid
				return;
			} else { // if details are valid
				Booking newBooking = booking.cloneBooking();
				Park parkDesired = parksList.get(parkComboBox.getSelectionModel().getSelectedIndex());
				newBooking.setParkBooked(parkDesired);
				newBooking.setDayOfVisit(datePicker.getValue());
				newBooking.setTimeOfVisit(hourCombobox.getValue());
				newBooking.setNumberOfVisitors(Integer.parseInt(visitorsTxt.getText()));
				boolean isAvailable = control.checkParkAvailabilityForExistingBooking(booking, newBooking);

				if (!isAvailable) { // if the entered date and time are not available

					dateIsNotAvailable(newBooking);
				}

				else { // if the date and time are available

					dateIsAvailable(newBooking);
				}
			}
		}
	}

	@FXML
	/**
	 * Returns to the previous screen
	 * 
	 * @param event
	 */
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(true, true);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * This method is called after an event of a value chosen inside the park combo
	 * box has occured
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

	//////////////////////////////////////
	/// JAVAFX COMPONENTS FLOW METHODS ///
	//////////////////////////////////////

	@FXML
	/**
	 * transfers the focus from the park combobox to the date picker
	 */
	void parkTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			datePicker.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from the date picker to the hour combobox
	 */
	void dateTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			hourCombobox.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from the hour combobox to the visitors text
	 */
	void hourTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			visitorsTxt.requestFocus();
		}
	}

	@FXML
	/**
	 * tranfers the focus from the visitor text field to the pane
	 */
	void visitorsTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			pane.requestFocus();
		}
	}

	/**
	 * tranfers the focus from the button to the pane
	 */
	@FXML
	void btnTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			pane.requestFocus();
		}
	}

	@FXML
	/**
	 * the pane takes the focus from any other component when clicked
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	/**
	 * Ignores tab pressed when on the pane
	 */
	void paneTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

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

		// checking the visitors number
		if (visitorsTxt.getText().isEmpty() || !visitorsTxt.getText().matches("\\d+")) {
			visitorsTxt.setStyle(setFieldToError());
			error += "• enter a digit-only number of visitors\n";
			valid = false;
		} else {
			if (Integer.parseInt(visitorsTxt.getText()) < control.minimumVisitorsInReservation
					|| Integer.parseInt(visitorsTxt.getText()) > control.maximumVisitorsInReservation) {
				visitorsTxt.setStyle(setFieldToError());
				error += "• enter a number of visitors in range of " + control.minimumVisitorsInReservation + " to "
						+ control.maximumVisitorsInReservation + "\n";
				valid = false;
			}
		}

		if (!valid)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
		return valid;
	}

	/**
	 * This method gets a park and returns its element from the park list
	 * 
	 * @param park
	 * @return the park if exists, null if not
	 */
	private Park getParkFromList(Park park) {
		for (int i = 0; i < parksList.size(); i++) {
			if (parksList.get(i).getParkId() == park.getParkId()) {
				return parksList.get(i);
			}
		}
		return null;
	}

	/**
	 * This method is called to check if the user changed any of the deatils of his
	 * reservation
	 * 
	 * @return true if changed, false if not
	 */
	private boolean checkIfChanged() {
		// checking the park
		if (!parksList.get(parkComboBox.getSelectionModel().getSelectedIndex()).equals(booking.getParkBooked())) {
			return true;
		}
		if (!datePicker.getValue().equals(booking.getDayOfVisit())) {
			return true;
		}
		if (!hourCombobox.getValue().equals(booking.getTimeOfVisit())) {
			return true;
		}
		if (!visitorsTxt.getText().equals(booking.getNumberOfVisitors() + "")) {
			return true;
		}
		return false;
	}

	/**
	 * This method is called in case the chosen date and time are available at the
	 * chosen park
	 * 
	 * @param event
	 */
	private void dateIsAvailable(Booking newBooking) {
		// first inserting the new booking to the database to update capacities and save
		// the visitor's spot
		boolean result = control.updateBooking(booking, newBooking);
		if (!result) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"There was an issue with updating your reservation. Please try again later");
			return;
		}

		// calculating the final price for the booking. Sending visitor's type cause the
		// price defers between regular and guided groups
		int finalPrice = control.calculateFinalRegularPrice(newBooking, visitor.getVisitorType());
		int discountPrice = control.calculateFinalDiscountPrice(newBooking, visitor.getVisitorType());

		// creating the pop up message
		String payMessage = "Woohoo! You're almost set.";
		payMessage += booking.isPaid() ? "\nYour old booking is fully refunded." : "";
		payMessage += "\nPay now and get a special discount for pre-ordering:";
		payMessage += "\n        Your new reservation's final price: " + finalPrice + "$";
		payMessage += "\n        Your new reservetion's price after the special discount: " + discountPrice + "$";
		int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(), payMessage,
				Arrays.asList("Pay Now and Get Discount", "Pay Upon Arrival", "Exit Reservations"));

		switch (choise) {
		// chose to pay now and get discount
		case 1: {
			newBooking.setPaid(true);
			newBooking.setFinalPrice(discountPrice);
			// updating the payment columns in the database
			control.updateBookingPayment(newBooking);
			// showing the confirmation screen
			try {
				ScreenManager.getInstance().showScreen("LoadingScreenController", "/clientSide/fxml/LoadingScreen.fxml",
						true, false, StageSettings.defaultSettings("Payment"),
						new Pair<Booking, ParkVisitor>(newBooking, visitor));
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
			return;
		}

		// chose to pay upon arrival
		case 2: {
			newBooking.setPaid(false);
			newBooking.setFinalPrice(finalPrice);
			// updating the payment columns in the database
			control.updateBookingPayment(newBooking);
			break;
		}

		// chose to cancel
		case 3: {
			// deleting the new booking from the database cause it wat inserted in order to
			// save the spot for the visitor, and returning to acount screen
			control.deleteBookingFromActiveTable(newBooking);
			returnToPreviousScreen(null);
			return;
		}
		}

		// showing the confirmation screen
		try {
			ScreenManager.getInstance().showScreen("ConfirmationScreenController",
					"/clientSide/fxml/ConfirmationScreen.fxml", true, false,
					StageSettings.defaultSettings("Confirmation"), new Pair<Booking, ParkVisitor>(newBooking, visitor));
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called in case the chosen date and time are not available at
	 * the chosen park
	 * 
	 * @param event
	 */
	private void dateIsNotAvailable(Booking newBooking) {
		// creating a pop up message for the user to choose what to do next
		int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"We care for your experience in " + newBooking.getParkBooked().getParkName()
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
						"/clientSide/fxml/RescheduleScreen.fxml", true, true,
						StageSettings.defaultSettings("Reschedule"),
						new Pair<Booking, ParkVisitor>(newBooking, visitor));
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
						"/clientSide/fxml/WaitingListScreen.fxml", true, true,
						StageSettings.defaultSettings("Waiting List"), newBooking);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
		}
	}

	/////////////////////////////////////////////
	/// JAVAFX, FXML, ABSTRACT SCREEN METHODS ///
	/////////////////////////////////////////////

	@Override
	/**
	 * This method initialized all the fxml and javafx components
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
		dateLbl.getStyleClass().add("label-center-right");
		hourLbl.getStyleClass().add("label-center-right");
		parkLbl.getStyleClass().add("label-center-right");
		visitorsLbl.getStyleClass().add("label-center-right");
		bookingLbl.getStyleClass().add("label-center-right");

		// setting texts to font family and size
		parkComboBox.getStyleClass().add("combo-box-text");
		hourCombobox.getStyleClass().add("combo-box-text");

		// setting the visitors text field to recognize digits only
		setupTextFieldToDigitsOnly(visitorsTxt);

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	}

	@Override
	/**
	 * When the screen called with the information object, putting its properties in
	 * the GUI components
	 */
	public void loadBefore(Object information) {
		if (information instanceof Pair) {
			@SuppressWarnings("unchecked")
			Pair<Booking, ParkVisitor> pair = (Pair<Booking, ParkVisitor>) information;
			booking = pair.getKey();
			visitor = pair.getValue();
			// setting all the info into the components of the screen
			bookingLbl.setText("Booking ID: " + booking.getBookingId());
			parkComboBox.getSelectionModel().select(parksList.indexOf(getParkFromList(booking.getParkBooked())));
			datePicker.setValue(booking.getDayOfVisit());
			hourCombobox.setValue(booking.getTimeOfVisit());
			visitorsTxt.setText(((Integer) booking.getNumberOfVisitors()).toString());
			isGroupReservation = booking.getVisitType() == VisitType.GROUP ? true : false;
			typeLbl.setText((isGroupReservation == true ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
					+ booking.getIdNumber());
			parkChosen(null);
		}
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Booking Editing";
	}
}