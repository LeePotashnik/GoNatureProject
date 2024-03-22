package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.control.BookingController;
import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
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

/**
 * Controls the functionality related to managing park entries and exits from the perspective of a park employee.
 * It provides mechanisms for park employees to update entry and exit times of bookings, based on the visitors'
 * actual arrival and departure, and to issue invoices for park services.
 *
 * This controller leverages `ParkController` for interactions with the park-related data and `GoNatureUsersController`
 * for session management. It supports validating booking IDs against current park bookings, updating the park's
 * current capacity, and navigating to invoice generation or payment processing screens as needed.
 * 
 */
public class ParkEntryManagementScreenController extends AbstractScreen{

	private ParkController parkControl;
	private GoNatureUsersController userControl;
	private ParkEmployee parkEmployee;
	
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
    private Label nameLbl, titleLbl, visitorsLbl, bookingIDLbl;

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
	 * Handles the event triggered by pressing the 'Invoice production' button. It attempts to retrieve
	 * a booking from the database using the provided booking ID. If successful, navigates the park employee
	 * to the Payment System Screen for processing the payment or generating an invoice for the booking. 
	 * Displays an error alert if the booking ID does not correspond to an existing reservation in the park.
	 *
	 * @param event The ActionEvent triggered by pressing the 'Invoice production' button.
	 * @throws ScreenException If there's an issue loading the Payment System Screen.
	 * @throws StatefulException If there's an issue with the application's state during navigation.
	 */
    @FXML
    void InvoiceScreen(ActionEvent event) throws StatefulException, ScreenException {
        Booking booking = findBookingAcrossTables(bookingIDTxt.getText());
        if (booking == null) {
            showErrorAlert("No existing reservation found for the provided booking ID.");
            return;
        }
        ScreenManager.getInstance().showScreen("ConfirmationScreenController",
				"/clientSide/fxml/ConfirmationScreen.fxml", true, false, booking);
    }

    /**
     * Updates the entry time for a visitor based on a valid booking ID. It first validates the booking ID,
     * then checks for the booking's existence and its eligibility for entry at the current time.  
     * If all conditions are met, the entry time is updated in the database, and the park's
     * current capacity is adjusted accordingly. 
     * This method also handles payment verification.
     *
     * @param event The ActionEvent triggered by the employee's interaction with the UI to update a booking's entry time.
     */
    @FXML
    void UpdateEntryTime(ActionEvent event) {
    	if(!validate())
    		return;
		String bookingId = bookingIDTxt.getText();
		String parkTable = parkControl.nameOfTable(parkEmployee.getWorkingIn()) + "_park_active_booking";
		Booking booking = null;
		
		booking = parkControl.checkIfBookingExists(parkTable,"bookingId",bookingId);
		if (booking == null) {
			//booking ID does not exist in the database
			showErrorAlert("No reservation exists for the given bookingID in this park.");
			return;
		}
    	
		if (booking.getEntryParkTime() != null) {
			//entry park time for selected bookingId is not empty
			showErrorAlert("Entry time already exist.");
			return;
		}	
			
		if (!booking.getDayOfVisit().equals(LocalDate.now())) {
			showErrorAlert("No reservation exists today for the given bookingID in this park.");
			return;
		}
		
		if (LocalTime.now().isBefore(booking.getTimeOfVisit())) {
			//Checking if the visitor did not arrive earlier than his reservation
			showErrorAlert("The visitor arrived too early.");
			return;
		}

		if (booking.getTimeOfVisit().plusHours(parkEmployee.getWorkingIn().getTimeLimit()).isBefore(LocalTime.now())) {
			//Checking if the visitor did not arrive after his time limit has expired
			showErrorAlert("The visitor missed his time reservation.");
			return;
		}	
		
		updateParkCapacity();
		parkControl.updateTimeInPark(parkTable, "entryParkTime", bookingId); //updates entry time
		int updateCapacity = parkEmployee.getWorkingIn().getCurrentCapacity() + booking.getNumberOfVisitors();
		parkEmployee.getWorkingIn().setCurrentCapacity(updateCapacity);
		if(parkControl.updateCurrentCapacity(parkEmployee.getWorkingIn().getParkName(),updateCapacity))//updates park current capacity
			System.out.println("Park capacity updated");
		if (!booking.isPaid()) {//needs to update DB: "paid" ?
			int decision = showConfirmationAlert("Please charge the customer: " + booking.getFinalPrice(),
					Arrays.asList("Cash", "CreditCard"));
			if (decision == 2) {// if the user clicked on "Credit Card" he will redirect to pay screen and then to confirmation screen
				event.consume();
				try {
					ScreenManager.getInstance().showScreen("PaymentSystemScreenController",
							"/clientSide/fxml/PaymentSystemScreen.fxml",true, false, booking);
				} catch (StatefulException | ScreenException e) {
					e.printStackTrace();
				} 
			} else { // user clicked on "Cash", showing the confirmation screen
				try {
					ScreenManager.getInstance().showScreen("ConfirmationScreenController",
							"/clientSide/fxml/ConfirmationScreen.fxml", true, false, booking);
				} catch (StatefulException | ScreenException e1) {
					e1.printStackTrace();
				}
			} 
			if(parkControl.payForBooking(parkTable)) //
				System.out.println("Payment successful.");
		}
    } 

    /**
     * Updates the exit time for a visitor based on a valid booking ID.
     * This method validates the booking ID and confirms the booking's existence. 
     * It ensures that an entry time has been set for the booking, indicating that the visitor did enter the park. 
     * If the booking is for the current day and an entry time exists without a corresponding exit time,
     * the exit time is updated in the database. 
     * The park's current capacity is then adjusted to reflect the visitor's departure. 
     * Additionally, the booking is moved from the active bookings table to the done bookings table, 
     * finalizing the visitor's park attendance record.
     *
     * @param event The ActionEvent triggered by the employee's interaction with the UI to update a booking's exit time.
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
			booking = parkControl.checkIfBookingExists(parkTable,"bookingId",bookingId);
		} catch (NullPointerException e) {
			//booking ID is not exists in the database
			showErrorAlert("No reservation exists for the given bookingID in this park.");
			return;
		}
    	
		if (booking.getEntryParkTime() == null) {
			//entry park time for selected bookingId is empty
			showErrorAlert("Entry time does not exist; cannot update exit time.");
			return;
		}
		
		if (booking.getExitParkTime() != null){
			//exit park time for selected bookingId is not empty
			showErrorAlert("Exit time already exist.");
			return;
		}
		
		if (!booking.getDayOfVisit().equals(LocalDate.now())){
			showErrorAlert("No reservation exists today for the given bookingID in this park.");
			return;
		}
		updateParkCapacity();
		parkControl.updateTimeInPark(parkTable, "exitParkTime", bookingId); //updates exit time
		int updateCapacity = parkEmployee.getWorkingIn().getCurrentCapacity() - booking.getNumberOfVisitors();
		System.out.println(updateCapacity);
		System.out.println(parkEmployee.getWorkingIn().getCurrentCapacity());
		System.out.println(booking.getNumberOfVisitors());

		parkControl.updateCurrentCapacity(parkEmployee.getWorkingIn().getParkName(),updateCapacity);//updates park current capacity
		//remove the booking from active park table
		parkControl.removeBookingFromActiveBookings(parkTable,booking.getBookingId()); 
		//insert the booking to done park table
		parkControl.insertBookingToTable(booking, "_park_done_booking", "done");
    }
    
    /**
     * Attempts to locate a booking within the active and completed bookings tables of the park, 
     * specific to the park employee.
     * 
     * @param bookingID The booking ID to search for.
     * @return The found Booking object, or null if no booking is found.
     */
    private Booking findBookingAcrossTables(String bookingID) {
    	String bookingId = bookingIDTxt.getText();
        String[] tables = {
        	parkControl.nameOfTable(parkEmployee.getWorkingIn()) + "_park_active_booking",
        	parkControl.nameOfTable(parkEmployee.getWorkingIn()) + "_park_done_booking",
        };
        for (String table : tables) {
            Booking booking = parkControl.checkIfBookingExists(table,"bookingId",bookingId);
            if (booking != null)
            	return booking;
        }
        return null;
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
			showErrorAlert(error);
		}
		return valid;
	}
	
	private void updateParkCapacity() {
		String[] returnsVal = new String[4]; 
		returnsVal = parkControl.checkCurrentCapacity(parkEmployee.getWorkingIn().getParkName());
		if (returnsVal != null) {
			//sets the parameters
			parkEmployee.getWorkingIn().setMaximumVisitors(Integer.parseInt(returnsVal[0]));
			parkEmployee.getWorkingIn().setMaximumOrders(Integer.parseInt(returnsVal[1]));
			parkEmployee.getWorkingIn().setTimeLimit(Integer.parseInt(returnsVal[2])); 
			parkEmployee.getWorkingIn().setCurrentCapacity(Integer.parseInt(returnsVal[3])); 
		}
	}

	/**
	 * Initializes the screen with default settings, styles buttons, and loads the GoNature logo.
	 * Sets up the back button with an image and ensures labels and buttons are properly aligned.
	 */
	@Override
	public void initialize() {
		this.parkEmployee = (ParkEmployee) userControl.restoreUser();
		this.titleLbl.setText(parkEmployee.getWorkingIn().getParkName());
	    this.titleLbl.underlineProperty();
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		exitTimeBTN.setStyle("-fx-alignment: center-right;");
		entryTimeBTN.setStyle("-fx-alignment: center-right;");
		titleLbl.setStyle("-fx-alignment: center-right;");
		visitorsLbl.setStyle("-fx-alignment: center-right;");
		invoiceButton.setStyle("-fx-alignment: center-right;");
		bookingIDLbl.setStyle("-fx-alignment: center-right;");
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));	
	}

	/**
	 * Prepares the screen with park employee-specific data. Displays the park name managed by the employee
	 * and ensures the screen is ready for interaction upon navigation.
	 *
	 * @param information ParkEmployee instance containing the employee's information and the park they manage.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return parkEmployee.getWorkingIn().getParkName() + " Entry Park Management";
	}
}