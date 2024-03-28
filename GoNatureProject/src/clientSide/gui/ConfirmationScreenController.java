package clientSide.gui;

import java.time.Duration;
import java.time.LocalDateTime;

import clientSide.control.BookingController;
import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.ParkEmployee;
import entities.ParkVisitor;
import entities.SystemUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * The ConfirmationScreenController is called after a booking if successfully
 * proccessed, showing a confirmation (invoice) to the user.
 */
public class ConfirmationScreenController extends AbstractScreen {

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Label bookingIdLabel, dateLabel, emailLabel, holderLabel, isPaidLabel, parkAddressLabel, parkNameLabel,
			phoneLabel, priceLabel, timeLabel, visitorsLabel, titleLbl, secondLbl;
	@FXML
	private ImageView goNatureLogo, parkImage;
	@FXML
	private Button returnToAccountBtn;
	@FXML
	private Pane pane;

	/////////////////////
	/// EVENT METHODS ///
	/////////////////////

	@FXML
	/**
	 * This method is called after the user clicked on "Return to Account" button
	 * 
	 * @param event
	 */
	void returnToAccount(ActionEvent event) {
		SystemUser user = GoNatureUsersController.getInstance().restoreUser();
		if (user instanceof ParkVisitor) {
			ScreenManager.getInstance().resetScreensStack();
			try {
				ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController",
						"/clientSide/fxml/ParkVisitorAccountScreen.fxml", false, false, null);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
		if (user instanceof ParkEmployee) {
			ScreenManager.getInstance().resetScreensStack();
			try {
				ScreenManager.getInstance().showScreen("ParkEmployeeAccountScreenController",
						"/clientSide/fxml/ParkEmployeeAccountScreen.fxml", false, false, null);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
	}

	///////////////////////////////
	/// JAVAFX AND FXML METHODS ///
	///////////////////////////////

	@Override
	/**
	 * This method initialized all the fxml and javafx components
	 */
	public void initialize() {
		// initializing the image component and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title labels
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setStyle("-fx-text-alignment: center;");
		secondLbl.setAlignment(Pos.CENTER);
		secondLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		secondLbl.setStyle("-fx-text-alignment: center;");

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	/**
	 * This method gets a pair object of booking and park visitor and sets the GUI
	 * components with information from these instances.
	 */
	public void loadBefore(Object information) {
		Booking booking;
		if (information != null && information instanceof Booking) {
			booking = (Booking) information;

			parkNameLabel.setText("Park Name: " + booking.getParkBooked().getParkName());
			parkAddressLabel.setText("Park Location: " + booking.getParkBooked().getParkCity() + ", "
					+ booking.getParkBooked().getParkState());
			bookingIdLabel.setText("Booking ID: " + booking.getBookingId());
			holderLabel.setText("Full Name: " + booking.getFirstName() + " " + booking.getLastName());
			emailLabel.setText("Email: " + booking.getEmailAddress());
			phoneLabel.setText("Phone: " + booking.getPhoneNumber());
			dateLabel.setText("Date of Visit: " + booking.getDayOfVisit() + "");
			timeLabel.setText("Time of Visit: " + booking.getTimeOfVisit() + "");
			visitorsLabel.setText("Group Size: " + booking.getNumberOfVisitors() + "");
			priceLabel.setText("Final Price: " + booking.getFinalPrice() + "$");
			
			// checking if the booking is for less than 24 hours from now
			LocalDateTime bookingTime = LocalDateTime.of(booking.getDayOfVisit(), booking.getTimeOfVisit());
			LocalDateTime now = LocalDateTime.now();
			String reminder = "";
			if (Math.abs(Duration.between(bookingTime, now).toHours()) <= BookingController.getInstance().reminderSendingTime) {
				reminder += " Your reservation is confirmed.";
			} else {
				reminder += " 24 hours before arrival, you'll get a reminder.";
			}
			if (booking.isPaid()) {
				isPaidLabel.setText("Your reservation is fully paid." + reminder);
			} else {
				isPaidLabel.setText("Your reservation is not paid." + reminder);
			}

			String parkImagePath = "/" + ParkController.getInstance().nameOfTable(booking.getParkBooked()) + ".jpg";
			parkImage.setImage(new Image(getClass().getResourceAsStream(parkImagePath)));
		}
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Reservation Confirmation";
	}
}