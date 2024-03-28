package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;

import clientSide.control.BookingController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Booking.VisitType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * The WaitingListScreenController class manages the waiting list observation of
 * a specific park for specific date and time frame.
 */
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

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Button enterWaitingBtn, returnToAccountBtn, backButton;
	@FXML
	private Pane pane;
	@FXML
	private Label titleLbl, yourOrderLabel, waitLabel;
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
	private ProgressIndicator progressIndicator;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * This method is called if the user chose to enter into the waiting list
	 * 
	 * @param event
	 */
	void enterWaitingList(ActionEvent event) {
		int finalPrice = control.calculateFinalDiscountPrice(booking,
				booking.getVisitType() == VisitType.GROUP ? true : false, false);
		booking.setFinalPrice(finalPrice);

		AtomicBoolean success = new AtomicBoolean();
		setVisible(true);
		enterWaitingBtn.setDisable(true);
		backButton.setDisable(true);

		// moving the operations to a background thread
		new Thread(() -> {
			boolean result = control.insertBookingToWaitingList(booking);
			if (result) {
				new Thread(() -> {
					control.sendWaitingListEntranceNotification(booking);
				}).start();
			}
			success.set(result);

			Platform.runLater(() -> {
				if (success.get()) {
					setVisible(false);
					backButton.setDisable(true);
					enterWaitingBtn.setDisable(true);
					booking.setWaitingListPriority(waitingList.size() + 1);
					waitingList.add(booking);
					showInformationAlert("Your reservation entered the waiting list of "
							+ booking.getParkBooked().getParkName() + " park successfully");
					event.consume();
				} else {
					showErrorAlert(
							"An error occured while trying to enter you to the waiting list. Please try again later.");
					enterWaitingBtn.setDisable(true);
					event.consume();
				}
			});
		}).start();
	}

	@FXML
	/**
	 * returns the user to his account screen
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

	///////////////////////////////////
	/// JAVAFX FOCUS CONTROL METHOD ///
	///////////////////////////////////

	@FXML
	/**
	 * sets the request to the root
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

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
		waitingListTable.setPlaceholder(new Label("You are the first to enter the waiting list"));
	}

	/**
	 * Shows/hides the progress indicator and its label
	 * 
	 * @param visible
	 */
	private void setVisible(boolean visible) {
		progressIndicator.setVisible(visible);
		waitLabel.setVisible(visible);
		waitingListTable.setVisible(!visible);
		returnToAccountBtn.setDisable(visible);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

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

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// setting the application's background
		setApplicationBackground(pane);

		// setting the labels
		waitLabel.setAlignment(Pos.CENTER);
		waitLabel.layoutXProperty().bind(pane.widthProperty().subtract(waitLabel.widthProperty()).divide(2));
		waitLabel.setText("Entering You to the Waiting List...");
		waitLabel.setStyle("-fx-text-alignment: center;");
		// setting the progress indicator
		progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressIndicator.layoutXProperty()
				.bind(pane.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));
		setVisible(false);
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

			yourOrderLabel.setText("For " + booking.getDayOfVisit() + ", At arround " + booking.getTimeOfVisit()
					+ ". Your place in the waiting list will be: " + priority);

			// setting the background image
			ImageView backgroundImage = new ImageView(
					new Image("/" + ParkController.getInstance().nameOfTable(booking.getParkBooked()) + ".jpg"));

			backgroundImage.fitWidthProperty().bind(ScreenManager.getInstance().getStage().widthProperty());
			backgroundImage.fitHeightProperty().bind(ScreenManager.getInstance().getStage().heightProperty());
			backgroundImage.setPreserveRatio(false);
			backgroundImage.setOpacity(0.2);

			if (pane.getChildren().get(0) instanceof ImageView) {
				pane.getChildren().remove(0);
			}
			pane.getChildren().add(0, backgroundImage);

			// now adding the waiting list to the table view
			setTable();
		}
	}

	@Override
	/**
	 * returns the screen's title
	 */
	public String getScreenTitle() {
		return "Park's Waiting List";
	}
}