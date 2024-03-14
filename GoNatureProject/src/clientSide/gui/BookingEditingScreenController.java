package clientSide.gui;

import java.time.LocalTime;

import clientSide.control.BookingController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenManager;
import entities.Booking;
import entities.Park;
import entities.ParkVisitor;
import entities.Booking.VisitType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class BookingEditingScreenController extends BookingScreenController {
	private BookingController control; // controller
	private ObservableList<Park> parksList; // list of the parks
	private ObservableList<String> parksStrings; // list of the parks as strings for the parks combox box
	private ObservableList<LocalTime> hours; // list of booking hours

	// the user enters the screen with a visitor instance or only with an id number
	private ParkVisitor visitor;
	private String userId;
	private boolean isGroupReservation; // determines if this is a regular or guided group

	// booking objects and data
	private Booking booking;
	private String bookingId;
	private int parkIndexInCombobox;

	/**
	 * Constructor, initializes the booking controller instance
	 */
	public BookingEditingScreenController() {
		control = BookingController.getInstance();
	}

	/// FXML AND JAVAFX COMPONENTS
	@FXML
	private Label dateLbl, hourLbl, parkLbl, visitorsLbl, titleLbl, bookingLbl, typeLbl;
	@FXML
	private Button backButton, cancelReservationBtn, updateReservationBtn, availabilityBtn;
	@FXML
	private DatePicker datePicker;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private ComboBox<LocalTime> hourCombobox;
	@FXML
	private ComboBox<String> parkComboBox;
	@FXML
	private TextField visitorsTxt;
	@FXML
	private Pane pane;

	/// BUTTONS EVENTS ///

	@FXML
	void cancelReservation(ActionEvent event) {

	}

	@FXML
	void availabilityClicked(ActionEvent event) {

	}

	@FXML
	void updateReservation(ActionEvent event) {

	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) {

	}

	/// JAVAFX FLOW METHODS ///

	@FXML
	/**
	 * transfers the focus from the park combobox to the date picker
	 */
	void parkTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			datePicker.requestFocus();
		}
	}

	@FXML
	/**
	 * transfers the focus from the date picker to the hour combobox
	 */
	void dateTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			hourCombobox.requestFocus();
		}
	}
	
	@FXML
	/**
	 * transfers the focus from the hour combobox to the visitors text
	 */
	void hourTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			visitorsTxt.requestFocus();
		}
	}
	
	@FXML
	/**
	 * tranfers the focus from the visitor text field to the pane
	 */
	void visitorsTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			pane.requestFocus();
		}
	}

	@FXML
	/**
	 * the pane takes the focus from any other component when clicked
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
	}

	@FXML
	void btnTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
			pane.requestFocus();
		}
	}

	@FXML
	void paneTabPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.TAB) {
			event.consume();
		}
	}

	@FXML
	void parkChosen(ActionEvent event) {
		parkIndexInCombobox = parkComboBox.getSelectionModel().getSelectedIndex();
		Park parkChosen = parksList.get(parkIndexInCombobox);
		ImageView backgroundImage = new ImageView(
				new Image("/" + ParkController.getInstance().nameOfTable(parkChosen) + ".jpg"));

		backgroundImage.fitWidthProperty().bind(ScreenManager.getInstance().getStage().widthProperty());
		backgroundImage.fitHeightProperty().bind(ScreenManager.getInstance().getStage().heightProperty());
		backgroundImage.setPreserveRatio(false);
		backgroundImage.setOpacity(0.2);

		if (pane.getChildren().get(0) instanceof ImageView) {
			pane.getChildren().remove(0);
		}
		pane.getChildren().add(0, backgroundImage);

	}

	@Override
	public void initialize() {
		// setting the park details in the parks combobox
		Pair<ObservableList<Park>, ObservableList<String>> pair = control.fetchParks();
		parksList = pair.getKey();
		parksStrings = pair.getValue();
		parkComboBox.setItems(parksStrings);

		// setting the hours combobox
		if (hours == null)
			setHours();

		// initializing the image component of the logo and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));

		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));

		// setting all the labels to right alignment
		dateLbl.getStyleClass().add("label-center-right");
		hourLbl.getStyleClass().add("label-center-right");
		parkLbl.getStyleClass().add("label-center-right");
		visitorsLbl.getStyleClass().add("label-center-right");
		bookingLbl.getStyleClass().add("label-center-right");

		// setting texts to font family and size
		parkComboBox.getStyleClass().add("combo-box-text");
		hourCombobox.getStyleClass().add("combo-box-text");

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	}

	private Park getParkIndex(Park park) {
		for (int i = 0; i < parksList.size(); i++) {
			if (parksList.get(i).getParkId() == park.getParkId()) {
				return parksList.get(i);
			}
		}
		return null;
	}

	@Override
	public void loadBefore(Object information) {
		if (information instanceof Booking) {
			booking = (Booking) information;
			// setting all the info into the components of the screen
			bookingLbl.setText("Booking ID: " + booking.getBookingId());
			parkComboBox.getSelectionModel().select(parksList.indexOf(getParkIndex(booking.getParkBooked())));
			datePicker.setValue(booking.getDayOfVisit());
			hourCombobox.setValue(booking.getTimeOfVisit());
			visitorsTxt.setText(((Integer) booking.getNumberOfVisitors()).toString());
			isGroupReservation = booking.getVisitType() == VisitType.GROUP ? true : false;
			typeLbl.setText((isGroupReservation == true ? "Guided Group | Your Id: " : "Regular Group | Your Id: ")
					+ booking.getIdNumber());
			parkChosen(null);
		}
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
