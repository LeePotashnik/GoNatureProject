package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;

import clientSide.control.BookingController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Booking.VisitType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class WaitingListScreenController extends AbstractScreen {
	private BookingController control; // controller
	private Booking booking;
	ObservableList<Booking> waitingList = FXCollections.observableArrayList();

	/**
	 * Constructor
	 */
	public WaitingListScreenController() {
		control = BookingController.getInstance();
	}

	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Button enterWaitingBtn, returnToAccountBtn;
	@FXML
	private Pane pane;
	@FXML
	private Label titleLbl, yourOrderLabel;
	@FXML
	private TableView<Booking> waitingListTable;
	@FXML
	private TableColumn<Booking, Integer> waitingOrderColumn;
	@FXML
	private TableColumn<Booking, String> bookingIdColumn;
	@FXML
	private TableColumn<Booking, LocalTime> timeOfVisitColumn;
	@FXML
	private TableColumn<Booking, LocalDate> dayOfBookingColumn;
	@FXML
	private TableColumn<Booking, VisitType> visitTypeColumn;
	@FXML
	private TableColumn<Booking, Integer> groupSizeColumn;

	@FXML
	/**
	 * This method is called if the user chose to enter into the waiting list
	 * 
	 * @param event
	 */
	void enterWaitingList(ActionEvent event) {
		// inserting the user to the waiting list
		if (control.insertBookingToWaitingList(booking)) {
			// updating the waiting list table view on the GUI
			waitingListTable.setItems(control.getWaitingListForPark(booking));

			event.consume();
			showInformationAlert(ScreenManager.getInstance().getStage(), "Your reservation entered the waiting list of "
					+ booking.getParkBooked().getParkName() + " park successfully."
							+ "\nWe will notify you if a place will be found for your group.");
			enterWaitingBtn.setDisable(true);

		} else {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"An error occured while trying to enter you to the waiting list. Please try again later.");
			event.consume();
			// here: returning to account screen
		}
	}

	@FXML
	/**
	 * returns the user to his account screen
	 * 
	 * @param event
	 */
	void returnToAccount(ActionEvent event) {
		// returning to the acount screen
		try {
			ScreenManager.getInstance().goToPreviousScreen(true, true);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * sets the request to the root
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@Override
	/**
	 * Initializes the FXML components
	 */
	public void initialize() {
		// initializing the image component and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		waitingOrderColumn.setResizable(false);
		bookingIdColumn.setResizable(false);
		timeOfVisitColumn.setResizable(false);
		dayOfBookingColumn.setResizable(false);
		visitTypeColumn.setResizable(false);
		groupSizeColumn.setResizable(false);

		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
	}

	@Override
	/**
	 * This method is used in order to get the details of the user's booking
	 */
	public void loadBefore(Object information) {
		if (information instanceof Booking) {
			booking = (Booking) information;
			titleLbl.setText(booking.getParkBooked().getParkName() + " Waiting List");

			// getting the waiting list of the booked park
			waitingList = control.getWaitingListForPark(booking);

			// checking the current order's priority
			int priority = waitingList.size() + 1;

			yourOrderLabel.setText("This is the waiting list of " + booking.getParkBooked().getParkName() + " for "
					+ booking.getDayOfVisit() + ". Your place in the waiting list will be: " + priority);

			// now adding the waiting list to the table view
			setTable();
		}
	}

	/**
	 * This method sets the table view and its columns
	 */
	private void setTable() {
		waitingOrderColumn.setCellValueFactory(new PropertyValueFactory<>("waitingListPriority"));
		bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		timeOfVisitColumn.setCellValueFactory(new PropertyValueFactory<>("timeOfVisit"));
		dayOfBookingColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfBooking"));
		visitTypeColumn.setCellValueFactory(new PropertyValueFactory<>("visitType"));
		groupSizeColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVisitors"));

		waitingListTable.setItems(waitingList);
		waitingListTable.getSortOrder().add(waitingOrderColumn);

	}

	@Override
	/**
	 * returns the screen's title
	 */
	public String getScreenTitle() {
		return "Waiting List";
	}

}
