package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.ParkEmployee;
import entities.Park;
import entities.Booking;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

public class ParkEntryManagementScreenController extends AbstractScreen implements Stateful{

	private ParkController parkControl;
	private GoNatureUsersController userControl;
	private ParkEmployee parkEmployee;
	private Park park;
	
    public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}

	@FXML
    private Button backButton, entryTimeBTN, exitTimeBTN, invoiceButton;

    @FXML
    private TextField bookingIDTxt;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Label nameLbl, titleLbl, visitorsLbl;

    @FXML
    private Pane pane;

    
    /**
	 * Constructor, initializes the Park Controller instance
	 */
	public ParkEntryManagementScreenController() {
		parkControl = ParkController.getInstance();
		userControl = GoNatureUsersController.getInstance();
	}
	
    /**
     * @param event
     * When the 'Invoiceproduction' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryCasualScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void InvoiceScreen(ActionEvent event) throws StatefulException, ScreenException {
    	
    	ScreenManager.getInstance().showScreen("ConfirmationScreenController",
				"/clientSide/fxml/ConfirmationScreen.fxml", false, true,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee );
    }

    /**
     * @param event
     * In case of a valid booking ID, a search will be performed in the database to find the corresponding order,
     *  the entry time to the park and current capacity will be updated accordingly.
     */
    @FXML
    void UpdateEntryTime(ActionEvent event) {
    	if(!validate())
    		return;
		String bookingId = bookingIDTxt.getText();
		String parkTable = parkControl.nameOfTable(parkEmployee.getWorkingIn()) + "_park_active_booking";
		Booking booking = null;
		try {
			booking = parkControl.checkIfBookingExists(parkTable, bookingId);
		} catch (NullPointerException e) {
			//booking ID is not exists in the database
			showErrorAlert(ScreenManager.getInstance().getStage(), 
					"No reservation exists for the given bookingID in this park.");
			return;
		}
    	
		if (booking.getEntryParkTime() != null) {
			//entry park time for selected bookingId is not empty
			showErrorAlert(ScreenManager.getInstance().getStage(), "Entry time already exist.");
			return;
		}	
			
		if (!booking.getDayOfVisit().equals(LocalDate.now())) {
			showErrorAlert(ScreenManager.getInstance().getStage(), 
					"No reservation exists today for the given bookingID in this park.");
			return;
		}
	
		if (booking.getTimeOfVisit().plusHours(park.getTimeLimit()).isAfter(LocalTime.now())) {
			//Checking if the visitor did not arrive after his time limit has expired
			showErrorAlert(ScreenManager.getInstance().getStage(), "The visitor missed his time reservation.");
			return;
		}
		
		if (LocalTime.now().isBefore(booking.getTimeOfVisit())) {
			//Checking if the visitor did not arrive earlier than his reservation
			showErrorAlert(ScreenManager.getInstance().getStage(), "The visitor arrived too early.");
			return;
		}
		
		parkControl.updateTimeInPark(parkTable, "entryParkTime", bookingId); //updates entry time
		int updateCapacity = park.getCurrentCapacity() + booking.getNumberOfVisitors();
		if(parkControl.updateCurrentCapacity(parkTable,updateCapacity))//updates park current capacity
			System.out.println("Park capacity updated");
		if (!booking.isPaid()) {//needs to update DB: "paid" ?
			int decision = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"Please charge the customer: " + booking.getFinalPrice(),
					Arrays.asList("Cash", "CreditCard"));
			if (decision == 2) {// if the user clicked on "Credit Card" he will redirect to pay screen and then to confirmation screen
				event.consume();
				try {
					ScreenManager.getInstance().showScreen("PaymentSystemScreenController", "/clientSide/fxml/PaymentSystemScreen.fxml",
							true, false, StageSettings.defaultSettings("Payment"), new Pair<Booking, ParkEmployee>(booking, parkEmployee));
				} catch (StatefulException | ScreenException e) {
					e.printStackTrace();
				} 
			}// else // user clicked on "Cash", showing the confirmation screen
			//	try {
			//		ScreenManager.getInstance().showScreen("ConfirmationScreenController",
			//				"/clientSide/fxml/ConfirmationScreen.fxml", true, true,
			//				StageSettings.defaultSettings("Confirmation"), new Pair<Booking, ParkEmployee>(booking, parkEmployee));
			//	} catch (StatefulException | ScreenException e) {
			//		e.printStackTrace();
			//	}		
			if(parkControl.payForBooking(parkTable)) //
				System.out.println("Payment successful.");
			
		}
    } 

    /**
     * @param event
     * In case of a valid booking ID, a search will be performed in the database to find the corresponding order,
     *  the exit time to the park and current capacity will be updated accordingly.
     */
    @FXML
    void UpdateExitTime(ActionEvent event) {
    	if (!validate()) {
            return;
        }
    	
    	String bookingId = bookingIDTxt.getText();
		String parkTable = parkControl.nameOfTable(parkEmployee.getWorkingIn()) + "_park_active_booking";
		Booking booking = null;
		
		try {
			booking = parkControl.checkIfBookingExists(parkTable, bookingId);
		} catch (NullPointerException e) {
			//booking ID is not exists in the database
			showErrorAlert(ScreenManager.getInstance().getStage(), 
					"No reservation exists for the given bookingID in this park.");
			return;
		}
    	
		if (booking.getEntryParkTime() == null) {
			//entry park time for selected bookingId is empty
			showErrorAlert(ScreenManager.getInstance().getStage(), "Entry time does not exist; cannot update exit time.");
			return;
		}
		
		if (booking.getExitParkTime() != null){
			//exit park time for selected bookingId is not empty
			showErrorAlert(ScreenManager.getInstance().getStage(), "Exit time already exist.");
			return;
		}
		
		if (!booking.getDayOfVisit().equals(LocalDate.now())){
			showErrorAlert(ScreenManager.getInstance().getStage(), 
					"No reservation exists today for the given bookingID in this park.");
			return;
		}
		
		parkControl.updateTimeInPark(parkTable, "exitParkTime", bookingId); //updates exit time
		int updateCapacity = park.getCurrentCapacity() - booking.getNumberOfVisitors();
		parkControl.updateCurrentCapacity(parkTable,updateCapacity);//updates park current capacity
		//remove the booking from active park table
		parkControl.removeBookingFromActiveBookings(parkTable,booking.getBookingId()); 
		//insert the booking to done park table
		parkControl.insertBookingToTable(booking, null, "_park_done_booking", "done");
    }

    @FXML
    void paneClicked(MouseEvent event) {

    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(false, true);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
    }
    
	/**
	 * booking ID validation: 
	 * Ensuring that the inserted ID is valid in terms of length and contains only digits.
	 * @return
	 * return true if the bookingId is valid
	 */
	private boolean validate() {
		boolean valid = true;
		String error = "";
		String insertedID = bookingIDTxt.getText();
		if (insertedID.length() != 10 || !insertedID.matches("\\d+")) {
			error = "You must enter a valid booking ID number with exactly 10 digits";
			valid = false;
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
		}
		return valid;
	}

	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		exitTimeBTN.setStyle("-fx-alignment: center-right;");
		entryTimeBTN.setStyle("-fx-alignment: center-right;");
		titleLbl.setStyle("-fx-alignment: center-right;");
		visitorsLbl.setStyle("-fx-alignment: center-right;");
		invoiceButton.setStyle("-fx-alignment: center-right;");
		
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));	
	}

	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		setParkEmployee(PE);	
		setPark(PE.getWorkingIn());
		this.titleLbl.setText(park.getParkName());
	    this.titleLbl.underlineProperty();
		}

	public Park getPark() {
		return park;
	}

	public void setPark(Park park) {
		this.park = park;
	}

	@Override
	public String getScreenTitle() {
		//		return parkEmployee.getWorkingIn().getParkName() + " Entry Park Management";

		return " Entry Park Management";
	}

	@Override
	public void saveState() {
		userControl.saveUser(parkEmployee);
		parkControl.savePark(park);
	}

	@Override
	public void restoreState() {
		this.parkEmployee = (ParkEmployee) userControl.restoreUser();
		this.park = parkControl.restorePark();
		this.titleLbl.setText(park.getParkName());
	    this.titleLbl.underlineProperty();
	}

}
