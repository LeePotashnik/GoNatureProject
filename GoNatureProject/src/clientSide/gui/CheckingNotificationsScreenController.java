package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Park;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class CheckingNotificationsScreenController extends AbstractScreen {

	private ParkController parkControl;
	ObservableList<Booking> observableBookings = null;

	@FXML
	private Button backButton;

	@FXML
	private TableView<Booking> notificationsTable;
	@FXML
	private TableColumn<Booking, String> bookingIdColumn;
	@FXML
	private TableColumn<Booking, String> parkColumn;
	@FXML
	private TableColumn<Booking, LocalDate> dateColumn;
	@FXML
	private TableColumn<Booking, LocalTime> timeColumn;
	@FXML
	private TableColumn<Booking, Integer> sizeColumn;
	@FXML
	private TableColumn<Booking, String> priceColumn;
	@FXML
	private TableColumn<Booking, String> paidColumn;

	@FXML
	private Label doubleClickLabel, futureLabel, titleLbl;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private Pane pane;

	@FXML
	private Separator seperator1;

	/**
	 * Constructor, initializes the Park Controller instance
	 */
	public CheckingNotificationsScreenController() {
		parkControl = ParkController.getInstance();
	}

	public ObservableList<Booking> getObservableBookings() {
		return observableBookings;
	}

	public void setObservableBookings(ObservableList<Booking> bookingsList) {
		this.observableBookings = bookingsList;
	}

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	void paneTabPressed(KeyEvent event) {
	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(true, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures the columns of the notifications table to display booking
	 * information. This method sets up each column in the table to show specific
	 * attributes of Booking objects. It also binds the table's data source to an
	 * observable list of bookings and sets the initial sort order by date of visit.
	 */
	private void setTables() {
		// Set the booking ID column to display the bookingId attribute from Booking
		// objects.
		bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));

		// Configure the park column to display the name of the park associated with
		// each booking.
		// This uses a custom lambda expression to extract and display the park name
		// from the Booking object.
		parkColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			Park park = booking.getParkBooked();
			String parkName = park.getParkName();
			return new ReadOnlyStringWrapper(parkName);
		});

		// Set the date column to display the dayOfVisit attribute from Booking objects.
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfVisit"));

		// Set the time column to display the timeOfVisit attribute from Booking
		// objects.
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeOfVisit"));

		// Set the size column to display the numberOfVisitors attribute from Booking
		// objects.
		sizeColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVisitors"));

		// Configure the price column to display the final price of the booking.
		// If the final price is -1 (indicating not applicable), it displays "N/A".
		// Otherwise, it appends a "$" symbol to the price.
		priceColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String price = booking.getFinalPrice() == -1 ? "N/A" : booking.getFinalPrice() + "$";
			return new ReadOnlyStringWrapper(price);
		});

		// Configure the paid column to display whether the booking has been paid for.
		// If the final price is -1, it displays "N/A". Otherwise, it shows "Yes" if
		// paid and "No" if not paid.
		paidColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String paid = booking.getFinalPrice() == -1 ? "N/A" : (booking.isPaid() ? "Yes" : "No");
			return new ReadOnlyStringWrapper(paid);
		});

		// Bind the table's items to an observable list of bookings, allowing the table
		// to update automatically as the list changes.
		notificationsTable.setItems(observableBookings);

		// Set the initial sort order of the table to be by the time of visit, making it
		// easier for users to see upcoming bookings.
		// notificationsTable.getSortOrder().add(timeColumn);
		// notificationsTable.refresh();
	}

	/**
	 * This method gets a chosen booking from the table (a row from the table) and
	 * checks the next steps the user wants to do with this booking
	 * 
	 * @param chosenBooking
	 */
	private void bookingClicked(Booking chosenBooking) {
		// first checking if the chosen booking is an active booking (which can be
		// edited) or a waiting list booking (which can only be cancelled)
		String ParkTable = parkControl.nameOfTable(chosenBooking.getParkBooked());
		int choise = showConfirmationAlert(
				"Confirm booking to " + chosenBooking.getParkBooked().getParkName() + " park for "
						+ chosenBooking.getDayOfVisit() + ", " + chosenBooking.getTimeOfVisit(),
				Arrays.asList("Confirm", "Cancelled"));
		if (choise == 2) {
			// Moving the traveler from the active bookings table to the canceled bookings
			// table
			parkControl.removeBooking(ParkTable + "_park_active_booking",
					chosenBooking.getBookingId());
			parkControl.insertBookingToTable(chosenBooking, ParkTable + "_park_cancelled_booking", "canceled");
		} else {
			// Updating that the user confirmed their arrival
			chosenBooking.setConfirmed(true);
			parkControl.updateConfirmed(ParkTable, chosenBooking.getBookingId());
		}
		// Remove the chosen booking from the observable list after a user decision has
		// been made
		observableBookings.remove(chosenBooking);
		// Convert the modified ObservableList back to an ArrayList and update the
		// central bookings list with the modified list
		ArrayList<Booking> bookings = new ArrayList<>(observableBookings);
		GoNatureUsersController.getInstance().setBookingsList(bookings);
		if (observableBookings.size() == 0) {
			// If there are no more bookings left to confirm, return to the previous screen
			try {
				ScreenManager.getInstance().goToPreviousScreen(true, false);
			} catch (ScreenException | StatefulException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize() {
		// Fetches a list of bookings from the system's logic layer
		ArrayList<Booking> bookings = GoNatureUsersController.getInstance().getBookingsList();

		// Wraps the bookings list in an observable list to bind it to the UI
		observableBookings = FXCollections.observableArrayList(bookings);
		// Sort the observable list based on the time of visit.
		observableBookings.sort(Comparator.comparing(Booking::getTimeOfVisit));

		// Configures the TableView for displaying bookings
		setTables();

		// Sets the GoNature logo on the user interface
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));

		// Prepares the back button with an image, sets its dimensions, and adjusts
		// padding
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// Defines an action for double-clicking on a row in the bookings table
		// Opens detailed view or performs an action related to the clicked booking
		notificationsTable.setRowFactory(tv -> {
			TableRow<Booking> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					Booking clickedRowData = row.getItem();
					// Handles the action to be taken when a booking row is double-clicked
					bookingClicked(clickedRowData);
				}
			});
			return row;
		});

		// setting the application's background
		setApplicationBackground(pane);
	}

	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return "Arrival Confirmation";
	}

}