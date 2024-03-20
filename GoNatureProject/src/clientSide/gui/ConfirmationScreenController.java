package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
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
			showInformationAlert("Return to park visitor's screen.");
//			ScreenManager.getInstance().resetScreensStack();
//			ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController, getScreenTitle(), false, false, null, user);
		}
		if (user instanceof ParkEmployee) {
			showInformationAlert("Return to park employee's screen.");
//			ScreenManager.getInstance().resetScreensStack();
//			ScreenManager.getInstance().showScreen("ParkEmployeeAccountScreenController, getScreenTitle(), false, false, null, user);
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
			if (booking.isPaid()) {
				isPaidLabel.setText("Your reservation is fully paid.");
			} else {
				isPaidLabel.setText("Your reservation is not paid. You will need to pay at the park entrance.");
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