package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.BookingController;
import common.communication.Communication;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 * The BookingViewScreenController is called after the visitor clicks on edit
 * bookings button in his account screen. This screen shows all the
 * active/cancelled/done/waiting list bookings of the visitor, and lets him do
 * operations on these bookings.
 */
public class BookingViewScreenController extends AbstractScreen implements Stateful {
	private BookingController control; // controller
	private Booking booking;
	private ObservableList<Booking> pastBookings = null;
	private ObservableList<Booking> futureBookings = null;
	private ParkVisitor visitor;

	/**
	 * The constructor gets the instance of the booking controller
	 */
	public BookingViewScreenController() {
		control = BookingController.getInstance();
	}

	// past bookings (done/cancelled) table view
	@FXML
	private TableView<Booking> pastTable;
	@FXML
	private TableColumn<Booking, String> bookingIdPastColumn;
	@FXML
	private TableColumn<Booking, String> parkPastColumn;
	@FXML
	private TableColumn<Booking, LocalDate> datePastColumn;
	@FXML
	private TableColumn<Booking, LocalTime> timePastColumn;
	@FXML
	private TableColumn<Booking, Integer> sizePastColumn;
	@FXML
	private TableColumn<Booking, String> pricePastColumn;
	@FXML
	private TableColumn<Booking, String> paidPastColumn;
	@FXML
	private TableColumn<Booking, String> statusPastColumn;

	// future bookings (active/in waiting list) table view
	@FXML
	private TableView<Booking> futureTable;
	@FXML
	private TableColumn<Booking, String> bookingIdFutureColumn;
	@FXML
	private TableColumn<Booking, String> parkFutureColumn;
	@FXML
	private TableColumn<Booking, LocalDate> dateFutureColumn;
	@FXML
	private TableColumn<Booking, LocalTime> timeFutureColumn;
	@FXML
	private TableColumn<Booking, Integer> sizeFutureColumn;
	@FXML
	private TableColumn<Booking, String> priceFutureColumn;
	@FXML
	private TableColumn<Booking, String> paidFutureColumn;
	@FXML
	private TableColumn<Booking, String> statusFutureColumn;

	// other GUI components
	@FXML
	private Button backButton;
	@FXML
	private Label titleLbl, doubleClickLabel, futureLabel, pastLabel, waitLabel;
	@FXML
	private Separator seperator1, seperator2;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Pane pane;
	@FXML
	private ProgressIndicator progressIndicator;

	/////////////////////
	/// EVENT METHODS ///
	/////////////////////

	@FXML
	/**
	 * Sets the focus to the pane when clicked
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();

	}

	@FXML
	/**
	 * Returns to the previous screen
	 * 
	 * @param event
	 */
	void returnToPreviousScreen(ActionEvent event) {
		try {
			// was not shown once, the previous screen is the account screen which needs to
			// restore its state
			ScreenManager.getInstance().goToPreviousScreen(false, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method gets a chosen booking from the table (a row from the table) and
	 * checks the next steps the user wants to do with this booking
	 * 
	 * @param chosenBooking
	 */
	private void bookingClicked(Booking chosenBooking) {
		// first checking if the chosen booking is an active booking (which can be
		// edited) or a waiting list booking (which can only be cancelled)
		if (chosenBooking.getStatus().equals("Active")) { // IF ACTIVE
			// creating a pop up message for the user to choose what to do next
			int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"Edit booking to " + chosenBooking.getParkBooked().getParkName() + " park for "
							+ chosenBooking.getDayOfVisit() + ", " + chosenBooking.getTimeOfVisit(),
					Arrays.asList("Return", "Edit"));
			switch (choise) {
			// chose to return
			case 1: {
				return;
			}
			// chose to edit
			case 2: {
				try {
					booking = chosenBooking;
					ScreenManager.getInstance().showScreen("BookingEditingScreenController",
							"/clientSide/fxml/BookingEditingScreen.fxml", true, true,
							StageSettings.defaultSettings("Booking Editing"),
							new Pair<Booking, ParkVisitor>(booking, visitor));
				} catch (StatefulException | ScreenException e) {
					e.printStackTrace();
				}
			}
			}
		} else { // IF WAITING LIST
			int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"Cancel waiting list spot to " + chosenBooking.getParkBooked().getParkName() + " park for "
							+ chosenBooking.getDayOfVisit() + ", " + chosenBooking.getTimeOfVisit()
							+ "\nThis action can't be undone",
					Arrays.asList("Return", "Cancel"));
			switch (choise) {
			// chose to return
			case 1: {
				return;
			}
			// chose to cancel
			case 2: {
				if (!control.deleteBookingFromWaitingList(chosenBooking)) {
					showErrorAlert(ScreenManager.getInstance().getStage(),
							"We were unable to cancel your waiting list spot.\nPlease try again later.");
					return;
				} else {
					showInformationAlert(ScreenManager.getInstance().getStage(),
							"Your waiting list booking to " + chosenBooking.getParkBooked().getParkName() + " Park for "
									+ chosenBooking.getDayOfVisit() + ", " + chosenBooking.getTimeOfVisit()
									+ " was cancelled successfully");
					futureTable.getItems().remove(chosenBooking);
				}
			}
			}
		}
	}

	/**
	 * This method sets the tables and their columns with the retrieved data
	 */
	private void setTables() {
		// setting the past bookings table
		bookingIdPastColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		parkPastColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			Park park = booking.getParkBooked();
			String parkName = park.getParkName();
			return new ReadOnlyStringWrapper(parkName);
		});
		datePastColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfVisit"));
		timePastColumn.setCellValueFactory(new PropertyValueFactory<>("timeOfVisit"));
		sizePastColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVisitors"));
		pricePastColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String price = booking.getFinalPrice() == -1 ? "N/A" : booking.getFinalPrice() + "$";
			return new ReadOnlyStringWrapper(price);
		});
		paidPastColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String paid = booking.getFinalPrice() == -1 ? "N/A" : (booking.isPaid() ? "Yes" : "No");
			return new ReadOnlyStringWrapper(paid);
		});
		statusPastColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

		pastTable.setItems(pastBookings);
		pastTable.getSortOrder().add(datePastColumn);

		// setting the future bookings table
		bookingIdFutureColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		parkFutureColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			Park park = booking.getParkBooked();
			String parkName = park.getParkName();
			return new ReadOnlyStringWrapper(parkName);
		});
		dateFutureColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfVisit"));
		timeFutureColumn.setCellValueFactory(new PropertyValueFactory<>("timeOfVisit"));
		sizeFutureColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVisitors"));
		priceFutureColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String price = booking.getFinalPrice() == -1 ? "N/A" : booking.getFinalPrice() + "$";
			return new ReadOnlyStringWrapper(price);
		});
		paidFutureColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String paid = booking.getFinalPrice() == -1 ? "N/A" : (booking.isPaid() ? "Yes" : "No");
			return new ReadOnlyStringWrapper(paid);
		});
		statusFutureColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

		futureTable.setItems(futureBookings);
		futureTable.getSortOrder().add(dateFutureColumn);
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
		pastTable.setVisible(visible);
		futureTable.setVisible(visible);
		futureLabel.setVisible(visible);
		pastLabel.setVisible(visible);
		seperator1.setVisible(visible);
		seperator2.setVisible(visible);
		doubleClickLabel.setVisible(visible);
		backButton.setVisible(visible);
	}

	///////////////////////////////////////////////////////
	/// JAVAFX, FXML, ABSTRACT SCREEN, STATEFUL METHODS ///
	///////////////////////////////////////////////////////

	@Override
	/**
	 * This method initialized all the fxml and javafx components
	 */
	public void initialize() {
		// initializing the image component and labels, and centering them
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		waitLabel.setAlignment(Pos.CENTER);
		waitLabel.layoutXProperty().bind(pane.widthProperty().subtract(waitLabel.widthProperty()).divide(2));
		waitLabel.setText("We are checking your bookings in our systems\nThis could take several seconds...");
		waitLabel.setStyle("-fx-text-alignment: center;");

		doubleClickLabel.getStyleClass().add("label-center-right");

		// setting all the columns resizable property to false
		bookingIdPastColumn.setResizable(false);
		parkPastColumn.setResizable(false);
		datePastColumn.setResizable(false);
		timePastColumn.setResizable(false);
		sizePastColumn.setResizable(false);
		pricePastColumn.setResizable(false);
		paidPastColumn.setResizable(false);
		statusPastColumn.setResizable(false);
		bookingIdFutureColumn.setResizable(false);
		parkFutureColumn.setResizable(false);
		dateFutureColumn.setResizable(false);
		timeFutureColumn.setResizable(false);
		sizeFutureColumn.setResizable(false);
		priceFutureColumn.setResizable(false);
		paidFutureColumn.setResizable(false);
		statusFutureColumn.setResizable(false);

		// setting the empty-table labels
		futureTable.setPlaceholder(new Label(
				"You don't have any upcoming bookings.\nYou can always reserve a new booking by returning to you account screen\nand click on the Reserve a New Booking button"));
		pastTable.setPlaceholder(new Label("No past bookings"));

		// setting what will occur when double-clicking on a row of the future table
		futureTable.setRowFactory(tv -> {
			TableRow<Booking> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					Booking clickedRowData = row.getItem();
					bookingClicked(clickedRowData);
				}
			});
			return row;
		});

		// setting what will occur when double-clicking on a row of the past table
		pastTable.setRowFactory(tv -> {
			TableRow<Booking> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					showInformationAlert(ScreenManager.getInstance().getStage(), "Past bookings cannot be edited");
				}
			});
			return row;
		});

		// setting the porgress indicator, hiding other elements
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));
		progressIndicator.layoutYProperty()
				.bind(pane.heightProperty().subtract(progressIndicator.heightProperty()).divide(2));

		// hiding all the elements but the progress indicator and its label
		setVisible(false);

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
	 * Saving relevant information from the screen for future restoring
	 */
	public void saveState() {
		Pair<ObservableList<Booking>, ObservableList<Booking>> pair = new Pair<>(pastBookings, futureBookings);
		control.setPair(pair);
		control.setBooking(booking);
		control.setSavedState(true);
	}

	@Override
	/**
	 * Restoring past saved information
	 */
	public void restoreState() {
		Pair<ObservableList<Booking>, ObservableList<Booking>> pair = control.getPair();
		pastBookings = pair.getKey();
		futureBookings = pair.getValue();
		booking = control.getBooking();
		control.setSavedState(false);
		setTables(); // updating the table views with the saved data
		setVisible(true); // showing gui elements, hiding progress indicator
	}

	@Override
	/**
	 * When the screen called with the information object, putting its properties in
	 * the GUI components
	 */
	public void loadBefore(Object information) {
		if (information instanceof ParkVisitor) {
			visitor = (ParkVisitor) information;
			if (pastBookings == null || futureBookings == null) {
				new Thread(() -> {
					// performing database operations
					pastBookings = control.getVisitorBookings(visitor, Communication.doneBookings);
					pastBookings.addAll(control.getVisitorBookings(visitor, Communication.cancelledBookings));
					futureBookings = control.getVisitorBookings(visitor, Communication.activeBookings);
					futureBookings.addAll(control.getVisitorBookings(visitor, Communication.waitingList));

					Platform.runLater(() -> {
						setTables(); // updating the table views with the fetched data
						setVisible(true); // showing gui elements, hiding progress indicator
					});
				}).start();
			}
		}
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Booking View";
	}
}