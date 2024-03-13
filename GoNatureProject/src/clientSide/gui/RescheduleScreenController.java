package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.BookingController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.Booking;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class RescheduleScreenController extends AbstractScreen {
	private BookingController control;
	private Booking booking;
	private ParkVisitor visitor;
	private ObservableList<AvailableSlot> availableSlots;
	private AvailableSlot chosenSlot = null;
	private Stage loadingStage;

	public RescheduleScreenController() {
		control = BookingController.getInstance();
	}

	/**
	 * A class for holding an available slot
	 */
	public static class AvailableSlot {
		private LocalDate date;
		private LocalTime time;

		/**
		 * Constructor for the avaialble slot
		 */
		public AvailableSlot(LocalDate date, LocalTime time) {
			this.date = date;
			this.time = time;
		}

		/**
		 * @return the date
		 */
		public LocalDate getDate() {
			return date;
		}

		/**
		 * @return the time
		 */
		public LocalTime getTime() {
			return time;
		}

		/**
		 * @param date
		 */
		public void setDate(LocalDate date) {
			this.date = date;
		}

		/**
		 * @param time
		 */
		public void setTime(LocalTime time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return "AvailableSlot [date=" + date + ", time=" + time + "]";
		}
	}

	@FXML
	private TableView<AvailableSlot> availableTable;
	@FXML
	private TableColumn<AvailableSlot, LocalDate> dateColumn;
	@FXML
	private TableColumn<AvailableSlot, LocalTime> arrivalColumn;

	@FXML
	private Button backButton, showBtn, makeReservationBtn;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private Pane pane;

	@FXML
	private Label titleLbl, typeLbl, bookingLbl;

	@FXML
	private DatePicker fromDate, toDate;

	///// EVENT METHODS /////

	@FXML
	void btnTabPressed(KeyEvent event) {

	}

	@FXML
	void showClicked(ActionEvent event) {
		if (!validate()) {
			event.consume();
			return;
		} else {
			// showing the loading dialog
			showLoadingDialog();

			// moving the data fetching operation to a background thread
			new Thread(() -> {
				// this operation is now off the JavaFX Application Thread
				final ObservableList<AvailableSlot> newSlots = FXCollections.observableArrayList(
						control.getParkAvailabilitySlots(booking, fromDate.getValue(), toDate.getValue()));

				// once fetching is complete, updating the UI on the JavaFX Application Thread
				Platform.runLater(() -> {
					availableTable.setItems(newSlots);
					// closing the loading dialog after updating the table
					closeLoadingDialog();
				});
			}).start();
			makeReservationBtn.setDisable(false);
		}
	}

	@FXML
	void makeReservation(ActionEvent event) {
		if (chosenSlot == null) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"Please choose a row from the table in order to proceed");
		} else {
			int choise = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"You're about to change your reservation dates:\n" + "From: " + booking.getDayOfVisit() + ", "
							+ booking.getTimeOfVisit() + ", To: " + chosenSlot.getDate() + ", " + chosenSlot.getTime(),
					Arrays.asList("Cancel", "Confirm"));

			switch (choise) {
			case 1: {
				event.consume();
				return;
			}

			case 2: {
				booking.setDayOfVisit(chosenSlot.getDate());
				booking.setTimeOfVisit(chosenSlot.getTime());
			}

				// first inserting the new booking to the database to update capacities and save
				// the visitor's spot
				control.insertNewBookingToActiveTable(booking);

				// calculating the final price for the booking. Sending visitor's type cause the
				// price defers between regular and guided groups
				int finalPrice = control.calculateFinalRegularPrice(booking,
						visitor == null ? VisitorType.TRAVELLER : visitor.getVisitorType());
				int discountPrice = control.calculateFinalDiscountPrice(booking,
						visitor == null ? VisitorType.TRAVELLER : visitor.getVisitorType());

				// creating the pop up message
				String payMessage = "Woohoo! You're almost set.";
				payMessage += "\nPay now and get a special discount for pre-ordering:";
				payMessage += "\n        Your reservation final price: " + finalPrice + "$";
				payMessage += "\n        Your reservetion price after the special discount: " + discountPrice + "$";
				choise = showConfirmationAlert(ScreenManager.getInstance().getStage(), payMessage,
						Arrays.asList("Pay Now and Get Discount", "Pay Upon Arrival", "Exit Reservations"));

				switch (choise) {
				// chose to pay now and get discount
				case 1: {
					booking.setPaid(true);
					booking.setFinalPrice(discountPrice);
					// updating the payment columns in the database
					control.updateBookingPayment(booking);
					// showing the confirmation screen
					try {
						ScreenManager.getInstance().showScreen("LoadingScreenController",
								"/clientSide/fxml/LoadingScreen.fxml", true, false,
								StageSettings.defaultSettings("Payment"),
								new Pair<Booking, ParkVisitor>(booking, visitor));
					} catch (StatefulException | ScreenException e) {
						e.printStackTrace();
					}
					return;
				}

				// chose to pay upon arrival
				case 2: {
					booking.setPaid(false);
					booking.setFinalPrice(finalPrice);
					// updating the payment columns in the database
					control.updateBookingPayment(booking);
					break;
				}

				// chose to cancel
				case 3: {
					// deleting the new booking from the database cause it wat inserted in order to
					// save the spot for the visitor, and returning to acount screen
					control.deleteBookingFromActiveTable(booking);
					returnToPreviousScreen(null);
					return;
				}
				}

				// showing the confirmation screen
				try {
					ScreenManager.getInstance().showScreen("ConfirmationScreenController",
							"/clientSide/fxml/ConfirmationScreen.fxml", true, false,
							StageSettings.defaultSettings("Confirmation"),
							new Pair<Booking, ParkVisitor>(booking, visitor));
				} catch (StatefulException | ScreenException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	void paneTabPressed(KeyEvent event) {
		event.consume();
	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) {

	}

	private void showLoadingDialog() {
		// Run later on the JavaFX thread
		Platform.runLater(() -> {
			Label loadingLabel = new Label("Loading information from " + booking.getParkBooked().getParkName()
					+ " park\nIt could take several seconds...");
			loadingLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Calibri'; -fx-font-weight: bold;");
			StackPane layout = new StackPane(loadingLabel);
			layout.setStyle(
					"-fx-padding: 20; -fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");
			Scene scene = new Scene(layout);

			loadingStage = new Stage();
			loadingStage.setWidth(400);
			loadingStage.setHeight(200);
			loadingStage.initModality(Modality.APPLICATION_MODAL);
			loadingStage.initStyle(StageStyle.UNDECORATED);
			loadingStage.setScene(scene);

			loadingStage.show();
		});
	}

	/**
	 * Closing the loading dialog message
	 */
	private void closeLoadingDialog() {
		Platform.runLater(() -> {
			if (loadingStage != null) {
				loadingStage.close();
			}
		});
	}

	///// INSTANCE METHODS /////

	/**
	 * This method validates the entered dates for the date range
	 * @return true if valid, false if not
	 */
	private boolean validate() {
		// set styles to regular
		fromDate.setStyle(setFieldToRegular());
		toDate.setStyle(setFieldToRegular());

		// validating the entered dates
		String error = "Please make sure you:\n";
		boolean valid = true;

		// checking the 'from' date
		if (fromDate.getValue() == null) {
			fromDate.setStyle(setFieldToError());
			error += "• enter a 'from' date";
			valid = false;
		} else {
			if (fromDate.getValue().compareTo(LocalDate.now()) < 0) { // past
				fromDate.setStyle(setFieldToError());
				error += "• choose a 'from' date that is " + LocalDate.now() + " and on\n";
				valid = false;
			} else { // future
				// calculating the date that is in the future allowed range
				LocalDate maximumFutureRange = (LocalDate.now()).plusMonths(control.futureBookingsRange);

				if (fromDate.getValue().compareTo(maximumFutureRange) > 0) {
					fromDate.setStyle(setFieldToError());
					error += "• choose a 'from' date that is before " + maximumFutureRange + "\n";
					valid = false;
				}
			}
		}

		// checking the 'to' date
		if (toDate.getValue() == null) {
			toDate.setStyle(setFieldToError());
			error += "• enter a 'to' date";
			valid = false;
		} else {
			if (toDate.getValue().compareTo(LocalDate.now()) < 0) { // past
				toDate.setStyle(setFieldToError());
				error += "• choose a 'to' date that is " + LocalDate.now() + " and on\n";
				valid = false;
			} else { // future
				// calculating the date that is in the future allowed range
				LocalDate maximumFutureRange = (LocalDate.now()).plusMonths(control.futureBookingsRange);

				if (toDate.getValue().compareTo(maximumFutureRange) > 0) {
					toDate.setStyle(setFieldToError());
					error += "• choose a 'to' date that is before " + maximumFutureRange + "\n";
					valid = false;
				}
			}
		}

		// checking that 'from' is smaller than 'to'
		if (fromDate.getValue() != null && toDate.getValue() != null) {
			if (toDate.getValue().compareTo(fromDate.getValue()) < 0) {
				fromDate.setStyle(setFieldToError());
				toDate.setStyle(setFieldToError());
				error += "• the 'to' date must be later than the 'from' date\n";
				valid = false;
			}
		}

		if (!valid)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);

		return valid;
	}

	///// JAVA-FX AND FXML METHODS /////

	@Override
	/**
	 * Initializes the FXML components
	 */
	public void initialize() {
		// initializing the image component and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		dateColumn.setResizable(false);
		arrivalColumn.setResizable(false);

		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		makeReservationBtn.setDisable(true);
		bookingLbl.getStyleClass().add("label-center-right");

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// setting the table's columns
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
		arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

		// setting a listener for the table view
		availableTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AvailableSlot>() {
			@Override
			public void changed(ObservableValue<? extends AvailableSlot> observable, AvailableSlot oldValue,
					AvailableSlot newValue) {
				if (newValue != null) {
					// newValue is the selected Person instance
					chosenSlot = newValue;
				}
			}
		});
	}

	///// ABSTRACT SCREEN METHODS /////

	@Override
	public void loadBefore(Object information) {
		if (information instanceof Pair) {
			@SuppressWarnings("unchecked")
			Pair<Booking, ParkVisitor> pair = (Pair<Booking, ParkVisitor>) information;
			booking = pair.getKey();
			visitor = pair.getValue();

			// setting the reservation type
			typeLbl.setText("Review available slots in " + booking.getParkBooked().getParkName() + " park");

			// setting the booking id
			bookingLbl.setText("Booking ID: " + booking.getBookingId());

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
		}
	}

	@Override
	public String getScreenTitle() {
		return "Reschedule";
	}

}
