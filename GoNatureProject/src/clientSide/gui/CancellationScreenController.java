package clientSide.gui;

import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import common.entities.Booking;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * The CancellationScreenController is called after a booking if successfully
 * cancelled, showing a cancellation confirmation to the user.
 */
public class CancellationScreenController extends AbstractScreen {
	private Booking booking;

	//////////////////////////////////
	/// JAVAFX ANF FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Label bookingIdLabel, dateLabel, emailLabel, holderLabel, isPaidLabel, parkAddressLabel, parkNameLabel,
			phoneLabel, priceLabel, timeLabel, visitorsLabel;
	@FXML
	private ImageView goNatureLogo, parkImage, sentImage;
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
		ScreenManager.getInstance().resetScreensStack();
		try {
			ScreenManager.getInstance().showScreen("ParkVisitorAccountScreenController",
					"/clientSide/fxml/ParkVisitorAccountScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method sets the tooltip over the information circle image
	 */
	private void setToolTip() {
		String message = "";
		message += "You will recieve this confirmation to your inboxes.";
		message += "\nIt takes up to 48 hours to refund pre-paid reservations.";
		message += "\nFor any further question regarding cencallations,";
		message += "\nyou can contact our customr service center.";
		Tooltip info = new Tooltip(message);
		Tooltip.install(sentImage, info);
		info.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"); // Make text bold and increase size
		info.setShowDelay(javafx.util.Duration.ZERO);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@Override
	/**
	 * This method initialized all the fxml and javafx components
	 */
	public void initialize() {
		// initializing the image component and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		sentImage.setImage(new Image(getClass().getResourceAsStream("/confirmationSent.png")));

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	/**
	 * This method gets a pair object of booking and park visitor and sets the GUI
	 * components with information from these instances.
	 */
	public void loadBefore(Object information) {
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
				isPaidLabel.setText("Your reservation was paid. We will initiate a refund in the next 48 hours.");
			} else {
				isPaidLabel.setText("Your reservation was not paid.");
			}

			String parkImagePath = "/" + ParkController.getInstance().nameOfTable(booking.getParkBooked()) + ".jpg";
			parkImage.setImage(new Image(getClass().getResourceAsStream(parkImagePath)));

			setToolTip();
		}
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Cancellation";
	}
}