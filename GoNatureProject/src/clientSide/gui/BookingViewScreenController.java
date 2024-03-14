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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

public class BookingViewScreenController extends AbstractScreen implements Stateful {
	private BookingController control; // controller
	private Booking booking;
	private ObservableList<Booking> pastBookings = FXCollections.observableArrayList();
	private ObservableList<Booking> futureBookings = FXCollections.observableArrayList();
	private ParkVisitor visitor;

	public BookingViewScreenController() {
		control = BookingController.getInstance();
	}

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
	private TableColumn<Booking, Integer> priceFutureColumn;
	@FXML
	private TableColumn<Booking, String> paidFutureColumn;

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
	private Circle circle;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();

	}

	@FXML
	void paneTabPressed(KeyEvent event) {

	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) {

	}

	@Override
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

		// setting the empty-table labels
		futureTable.setPlaceholder(new Label(
				"You don't have any upcoming bookings.\nYou can always reserve a new booking by returning to you account screen\nand click on the Reserve a New Booking button"));
		pastTable.setPlaceholder(new Label("No past bookings"));

		// setting what will occur when double-clicking on a row of the future bookings
		// table
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

		pastTable.setRowFactory(tv -> {
			TableRow<Booking> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					showInformationAlert(ScreenManager.getInstance().getStage(), "Past bookings cannot be edited");
				}
			});
			return row;
		});

		// setting the porgress indicators, hiding other elements
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));
		progressIndicator.layoutYProperty()
				.bind(pane.heightProperty().subtract(progressIndicator.heightProperty()).divide(2));

		// hiding all the elements but the progress indicator and its label
		setVisible(false);
	}

	private void bookingClicked(Booking chosenBooking) {
		// creating a pop up message for the user to choose what to do next
		int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
				"You are about to edit your " + chosenBooking.getParkBooked().getParkName() + " park booking for "
						+ chosenBooking.getDayOfVisit() + ", " + chosenBooking.getTimeOfVisit(),
				Arrays.asList("Cancel", "Continue"));
		switch (choise) {
		// chose to cancel
		case 1: {
			return;
		}
		// chose to continue
		case 2: {
			try {
				booking = chosenBooking;
				ScreenManager.getInstance().showScreen("BookingEditingScreenController",
						"/clientSide/fxml/BookingEditingScreen.fxml", true, true,
						StageSettings.defaultSettings("Booking Editing"), booking);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
		}
	}

	@Override
	public void loadBefore(Object information) {
		if (information instanceof ParkVisitor) {
			visitor = (ParkVisitor) information;

			new Thread(() -> {
				// performing database operations
				pastBookings = control.getVisitorBookings(visitor, Communication.doneBookings);
				pastBookings.addAll(control.getVisitorBookings(visitor, Communication.cancelledBookings));
				futureBookings = control.getVisitorBookings(visitor, Communication.activeBookings);

				Platform.runLater(() -> {
					setTables(); // updating the table views with the fetched data
					setVisible(true); // showing gui elements, hiding progress indicator
				});
			}).start();

		}
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
			String price = booking.getFinalPrice() == -1 ? "N/A" : booking.getFinalPrice() + "";
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
		priceFutureColumn.setCellValueFactory(new PropertyValueFactory<>("finalPrice"));
		paidFutureColumn.setCellValueFactory(cellData -> {
			Booking booking = cellData.getValue();
			String paid = booking.isPaid() ? "Yes" : "No";
			return new ReadOnlyStringWrapper(paid);
		});

		futureTable.setItems(futureBookings);
		futureTable.getSortOrder().add(dateFutureColumn);
	}

	@Override
	public String getScreenTitle() {
		return "Booking View";
	}

	@Override
	public void saveState() {
		Pair<ObservableList<Booking>, ObservableList<Booking>> pair = new Pair<>(pastBookings, futureBookings);
		control.setPair(pair);
		control.setBooking(booking);
		control.setSavedState(true);
	}

	@Override
	public void restoreState() {
		Pair<ObservableList<Booking>, ObservableList<Booking>> pair = control.getPair();
		pastBookings = pair.getKey();
		futureBookings = pair.getValue();
		booking = control.getBooking();
		control.setSavedState(false);
	}

}
