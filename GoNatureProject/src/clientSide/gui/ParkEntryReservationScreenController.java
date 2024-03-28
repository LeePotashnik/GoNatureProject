package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import clientSide.control.GoNatureUsersController;
import clientSide.control.LoginController;
import clientSide.control.ParkController;
import clientSide.control.PaymentController;
import clientSide.entities.ParkEmployee;
import clientSide.entities.ParkVisitor;
import clientSide.entities.ParkVisitor.VisitorType;
import common.communication.Communication;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import common.entities.Booking;
import common.entities.Booking.VisitType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * The ParkEntryReservationScreenController class is responsible for managing
 * the park entry reservation process from the employee's perspective within the
 * GoNature application. It facilitates the creation of new bookings for
 * visitors who physically arrive at the park, ensuring that all necessary
 * information is collected and validated before proceeding with the
 * reservation.
 *
 * This controller collaborates with ParkController for capacity checks and
 * PaymentController for pricing calculations. It handles form input validation,
 * booking creation, and navigational actions on the park entry reservation
 * screen.
 *
 * This class extends AbstractScreen, leveraging common functionalities provided
 * for screen management and state handling within the application.
 */
public class ParkEntryReservationScreenController extends AbstractScreen implements Stateful {
	private static ParkController parkControl;
	private ParkEmployee parkEmployee;
	private Booking newBooking;
	private ParkVisitor parkVisitor;
	private static final String emailInput = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	private static final String phoneInput = "^(052|054|050)\\d{7}$";
	Map<String, String> bookingDetails = new HashMap<>();

	/**
	 * Constructor, initializes the Park Controller instance
	 */
	public ParkEntryReservationScreenController() {
		parkControl = ParkController.getInstance();
	}

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton, makeReservationBtn;
	@FXML
	private Label bookingLbl, dateLbl, emailLbl, phoneLbl, titleLbl, visitorsLbl, nameLbl, lastNameLbl;
	@FXML
	private TextField emailTxt, phoneTxt, visitorsAmountTxt, visitorIDTxt, nameTxt, lastNameTxt;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Pane pane;
	@FXML
	private RadioButton guidedRadio, individualRadio;
	private ToggleGroup group = new ToggleGroup();

	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}

	//////////////////////////////
	/// EVENT HANDLUNG METHODS ///
	//////////////////////////////

	/**
	 * Processes a park entry reservation made by the park employee for a visitor.
	 * This method first validates the input details. If the input is valid and the
	 * park has available capacity for the specified number of visitors, a new
	 * booking is created.
	 *
	 * Discounts are applied if the visitor is recognized as a group guide in the
	 * database. The booking is then added to the active bookings table for the
	 * park, and the park's current capacity is updated accordingly.
	 *
	 * Upon successful reservation, a confirmation dialog is shown to indicate the
	 * payment method. Depending on the selection, the user may be directed to the
	 * payment screen or shown a confirmation screen directly.
	 *
	 * @param event The ActionEvent triggered by pressing the 'Make Reservation'
	 *              button.
	 */
	@FXML
	void makeReservation(ActionEvent event) {
		boolean valid = false;
		if (validate()) {
			// in case all the inserted values are valid,
			// checks if there is place for new group reservation at current date
			// Update parameters if they have changed in the previous screen.
			String[] returnsVal = new String[4];
			returnsVal = parkControl.checkCurrentCapacity(parkEmployee.getWorkingIn().getParkName());
			if (returnsVal != null) {
				// updates park parameters
				parkEmployee.getWorkingIn().setMaximumVisitors(Integer.parseInt(returnsVal[0]));
				parkEmployee.getWorkingIn().setMaximumOrders(Integer.parseInt(returnsVal[1]));
				parkEmployee.getWorkingIn().setTimeLimit(Integer.parseInt(returnsVal[2]));
				parkEmployee.getWorkingIn().setCurrentCapacity(Integer.parseInt(returnsVal[3]));

			}
			int available = parkEmployee.getWorkingIn().getMaximumVisitors();
			if (available - parkEmployee.getWorkingIn().getCurrentCapacity() >= Integer
					.parseInt(visitorsAmountTxt.getText()))
				valid = true;
			else {
				showErrorAlert(
						"Unfortunately, there is no place available in the park for the number of people in the reservation");
				visitorIDTxt.setStyle(setFieldToError());
			}
		}

		if (valid) {
			// if there is a place for the new group reservation
			int finalPrice, amount;

			// checking the group type selection
			RadioButton selected = (RadioButton) group.getSelectedToggle();
			switch ((String) selected.getUserData()) {
			case "guided":
				parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists(Communication.groupGuide, "groupGuideId",
						visitorIDTxt.getText());
				if (parkVisitor == null) { // there's no group guide with this id number
					showErrorAlert("The provided Id number is not found connected to an authorized group guide.");
					return;
				} else {
					break;
				}
			case "individual":
				parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists(Communication.traveller, "travellerId",
						visitorIDTxt.getText());
				if (parkVisitor == null) { // there's no traveller with this id in the db
					// inserting him to the db so he will be able to view his booking in the future
					LoginController loginControl = new LoginController();
					loginControl.insertNewTraveller(visitorIDTxt.getText());
					parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists(Communication.traveller, "travellerId",
							visitorIDTxt.getText()); // now will get the instance of the traveller
				} else {
					break;
				}
			}

			amount = Integer.parseInt(visitorsAmountTxt.getText());

			// Define a formatter that formats the time as hour and minute
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

			// Format the current time using the formatter
			String formattedTime = LocalTime.now().format(formatter);
			LocalTime now = LocalTime.parse(formattedTime);
			String bookingId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			newBooking = new Booking(bookingId, LocalDate.now(), now, LocalDate.now(),
					parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE ? VisitType.GROUP : VisitType.INDIVIDUAL,
					amount, visitorIDTxt.getText(), prepareName(nameTxt.getText()), prepareName(lastNameTxt.getText()),
					emailTxt.getText(), phoneTxt.getText(), 0, true, true, now, null, true, now,
					parkEmployee.getWorkingIn());

			// calculating the final price
			if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
				finalPrice = PaymentController.getInstance().calculateRegularPriceGuidedGroup(newBooking);
			} else {
				finalPrice = PaymentController.getInstance().calculateRegularPriceTravelersGroup(newBooking);
			}

			// updates the final price due visitors amount and relevant price
			newBooking.setFinalPrice(finalPrice);

			if (parkControl.checkParkAvailabilityForBooking(newBooking)) {
				int decision = showConfirmationAlert(
						"Please charge the customer:\nFinal reservation's price is: " + finalPrice + "$",
						Arrays.asList("By Cash", "By Credit Card", "Edit Booking"));

				switch (decision) {
				case 1: // Cash
					try {
						processBooking(amount);
						ScreenManager.getInstance().showScreen("ConfirmationScreenController",
								"/clientSide/fxml/ConfirmationScreen.fxml", true, false, newBooking);
					} catch (StatefulException | ScreenException e) {
						e.printStackTrace();
					}
					break;
				case 2: // Credit Card
					event.consume();
					try {
						processBooking(amount);
						ScreenManager.getInstance().showScreen("PaymentSystemScreenController",
								"/clientSide/fxml/PaymentSystemScreen.fxml", true, true,
								new Pair<Booking, String>(newBooking, "casual"));
					} catch (StatefulException | ScreenException e) {
						e.printStackTrace();
					}
					break;
				default: // Edit Booking
					event.consume();
					break;
				}
			} else {
				showErrorAlert(
						"Unfortunately, there is no space available in the park for the number of people in the reservation");
				visitorIDTxt.setStyle(setFieldToError());

			}
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

	@FXML
	/**
	 * Returns to the previous screen
	 * 
	 * @param event
	 */
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(false, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method inserts the new booking to the database
	 */
	private void processBooking(int amount) {
		newBooking.setPaid(true);
		String parkTable = parkControl.nameOfTable(parkEmployee.getWorkingIn()) + Communication.activeBookings;
		parkControl.insertBookingToTable(newBooking, parkTable, "active");
		// updating park capacity
		int update = amount + parkEmployee.getWorkingIn().getCurrentCapacity();
		parkControl.updateCurrentCapacity(newBooking.getParkBooked().getParkName(), update);
	}

	/**
	 * Validation is performed on the values entered by the employee. An error
	 * message appears on the screen for each incorrect input. It returns true if
	 * the input correct, otherwise, it returns false.
	 * 
	 * @return true if valid, false if not
	 */
	private boolean validate() {
		boolean valid = true;
		nameTxt.setStyle(setFieldToRegular());
		lastNameTxt.setStyle(setFieldToRegular());
		visitorIDTxt.setStyle(setFieldToRegular());
		visitorsAmountTxt.setStyle(setFieldToRegular());
		phoneTxt.setStyle(setFieldToRegular());
		emailTxt.setStyle(setFieldToRegular());
		// Radio Button choosing validation
		RadioButton selected = (RadioButton) group.getSelectedToggle();
		String error = "";

		// First name validation
		String insertedName = nameTxt.getText();
		if (!insertedName.matches("[A-Za-z]+( [A-Za-z]+)?")) {
			error += "You must enter a valid first name.\n";
			nameTxt.setStyle(setFieldToError());
			valid = false;
		}

		// Last name validation
		String insertedLastName = lastNameTxt.getText();
		if (!insertedLastName.matches("[A-Za-z]+( [A-Za-z]+)?")) {
			error += "You must enter a valid last name.\n";
			lastNameTxt.setStyle(setFieldToError());
			valid = false;
		}

		// ID validation:
		// Ensuring that the inserted ID is valid in terms of length and contains only
		// digits.
		String insertedID = visitorIDTxt.getText();
		if (insertedID.length() != 9 || !insertedID.matches("\\d+")) {
			error += "You must enter a valid ID number with exactly 9 digits.\n";
			visitorIDTxt.setStyle(setFieldToError());
			valid = false;
		}

		// Visitors amount validation:
		// Ensuring that the inserted number of visitors falls within the range of 1 to
		// 15.
		
		String amount = visitorsAmountTxt.getText();
		if (!amount.matches("\\d+")) {
			error += "You must enter a valid amount number of visits with only digits between 1-15.\n";
			visitorsAmountTxt.setStyle(setFieldToError());
			valid = false;
		} else {
			int amountInt = Integer.parseInt(amount);
			if (((String) selected.getUserData()).equals("guided") && (amountInt  > 15 || amountInt < 1)) {
				error += "You must enter a valid amount number of visits with only digits between 1-15.\n";
				visitorsAmountTxt.setStyle(setFieldToError());
				valid = false;
			}
		}

		// Phone number validation:
		// Verifying if the inserted phone number is valid in terms of length and
		// consists only of digits.
		String phoneNum = phoneTxt.getText();
		if (phoneNum.length() != 10 || !phoneNum.matches("\\d+") || !phoneNum.matches(phoneInput)) {
			error += "You must enter a valid phone number with exactly 10 digits.\n";
			phoneTxt.setStyle(setFieldToError());
			valid = false;
		}

		// Email validation:
		// Checking if the inserted email conforms to the standard email format to
		// ensure its validity.
		if (emailTxt.getText().isEmpty()
				|| !emailTxt.getText().matches(emailInput)) {
			error += "You must enter a valid email\n";
			emailTxt.setStyle(setFieldToError());
			valid = false;
		}

		if (selected == null) {
			error += "Please choose group type\n";
			valid = false;
		}

		if (!valid)
			showErrorAlert(error);
		return valid;
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

	////////////////////////////////////////////
	/// ABSTRACT SCREEN AND STATEFUL METHODS ///
	////////////////////////////////////////////

	/**
	 * Initializes the class. This method is automatically invoked after the FXML
	 * file has been loaded. It sets up the initial appearance of the GUI elements,
	 * including styling for labels and the reservation button, and configures the
	 * back button with an icon. The GoNature logo is also loaded and displayed on
	 * the screen.
	 */
	@Override
	public void initialize() {
		// Restoring the park employee's session details for use in initializing the
		// screen.
		parkEmployee = (ParkEmployee) GoNatureUsersController.getInstance().restoreUser();

		// setting title and image
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);

		// Setting the park name in the booking label and making it underline to
		// highlight.
		bookingLbl.setText("For " + parkEmployee.getWorkingIn().getParkName() + " Park");
		bookingLbl.layoutXProperty().bind(pane.widthProperty().subtract(bookingLbl.widthProperty()).divide(2));
		bookingLbl.underlineProperty();

		// Configuring the back button with an icon for visual consistency and
		// usability.
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// setting the radio buttons toggle group
		guidedRadio.setToggleGroup(group);
		guidedRadio.setUserData("guided");
		individualRadio.setToggleGroup(group);
		individualRadio.setUserData("individual");

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	public void loadBefore(Object information) {
		// irrelevant here
	}

	@Override
	public String getScreenTitle() {
		return parkEmployee.getWorkingIn().getParkName() + " - Park Entrance Reservations";
	}

	/**
	 * Saves the current state of the booking details entered in the text fields
	 * into a shared controller. This method captures the contents of text fields
	 * related to the booking process and stores them.
	 */
	@Override
	public void saveState() {
		bookingDetails.put("email", emailTxt.getText());
		bookingDetails.put("phone", phoneTxt.getText());
		bookingDetails.put("visitorsAmount", visitorsAmountTxt.getText());
		bookingDetails.put("visitorID", visitorIDTxt.getText());
		bookingDetails.put("name", nameTxt.getText());
		bookingDetails.put("lastName", lastNameTxt.getText());
		parkControl.setBookingDetails(bookingDetails);
	}

	/**
	 * Restores the state of booking details previously saved, updating the text
	 * fields on the screen with these values. This method retrieves a map of
	 * booking details from the {@code ParkController} and uses it to populate the
	 * text fields on the screen. It ensures that if the user previously entered
	 * information into these fields and then navigated away from the screen, the
	 * same information will be displayed upon returning. If the retrieved details
	 * are not null or empty, each text field is updated with its value from the
	 * map.
	 */
	@Override
	public void restoreState() {
		Map<String, String> bookingDetails = parkControl.getBookingDetails();

		if (bookingDetails != null && !bookingDetails.isEmpty()) {
			emailTxt.setText(bookingDetails.getOrDefault("email", ""));
			phoneTxt.setText(bookingDetails.getOrDefault("phone", ""));
			visitorsAmountTxt.setText(bookingDetails.getOrDefault("visitorsAmount", ""));
			visitorIDTxt.setText(bookingDetails.getOrDefault("visitorID", ""));
			nameTxt.setText(bookingDetails.getOrDefault("name", ""));
			lastNameTxt.setText(bookingDetails.getOrDefault("lastName", ""));
		}

	}
}