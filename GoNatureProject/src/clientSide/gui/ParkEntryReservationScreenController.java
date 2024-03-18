package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;

import clientSide.control.ParkController;
import clientSide.control.PaymentController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Park;
import entities.ParkEmployee;
import entities.ParkVisitor;
import entities.Booking.VisitType;
import entities.ParkVisitor.VisitorType;
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
import javafx.stage.WindowEvent;
import javafx.util.Pair;

public class ParkEntryReservationScreenController extends AbstractScreen{

	private ParkEmployee parkEmployee;
	private Booking newBooking; 
	private static ParkController parkControl;
	private ParkVisitor parkVisitor;
	private Park park;
	
	@FXML
    private Button backButton, makeReservationBtn;

    @FXML
    private Label bookingLbl, dateLbl, emailLbl, phoneLbl, titleLbl, visitorsLbl, nameLbl, lastNameLbl;

    @FXML
    private TextField emailTxt, phoneTxt, visitorsAmountTxt, visitorIDTxt, nameTxt, lastNameTxt;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Pane pane;

    /**
	 * Constructor, initializes the Park Controller instance
	 */
	public ParkEntryReservationScreenController() {
		parkControl = ParkController.getInstance();
	}
	
    /**
     * @param event
     * Process reservation for a customer who physically arrived at the park by an employee. 
     * If all provided details are valid and there is available space in the park for the booking's quantity of visitors,
     *  a booking will be created in the system, and the price will be determined based on the customer type. 
     *  If the customer is a group guide exist in the database, the visitors will receive a 10% discount for each visitor. 
     *  Otherwise, the regular price will apply.
     *  Finally, the booking will be added to the park_active_booking table in the relevant park's database.
     */
    @FXML
    void makeReservation(ActionEvent event) {
    	boolean valid = false;
    	if (validate()) { 
    		//in case all the inserted values are valid, 
    		//checks if there is place for new group reservation at current date
    		//Update parameters if they have changed in the previous screen.
    		String[] returnsVal = new String[4]; 
    		returnsVal = parkControl.checkCurrentCapacity(park.getParkName());
    		if (returnsVal != null) {
    			//updates park parameters
    			park.setMaximumVisitors(Integer.parseInt(returnsVal[0]));
    			park.setMaximumOrders(Integer.parseInt(returnsVal[1]));
    			park.setTimeLimit(Integer.parseInt(returnsVal[2])); 
    			park.setCurrentCapacity(Integer.parseInt(returnsVal[3])); 
    		}
    		int available = park.getMaximumVisitors() * park.getMaximumOrders() / 100;
    		if (available - park.getCurrentCapacity() > Integer.parseInt(visitorsAmountTxt.getText()))
				valid = true;    		
    	}
    	if (valid) {
    		//if there is a place for the new group reservation
    		int flag = 0;
    		int finalPrice, amount;
    		parkVisitor = null;
    		parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists("group_guide", "groupGuideId", visitorIDTxt.getText());
    		if (parkVisitor != null) //indicates the visitor is exist in database as a 'groupGuide'
    			flag = 1; 	
    		amount = Integer.parseInt(visitorsAmountTxt.getText()); 		

    		String bookingId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			newBooking = new Booking(bookingId, LocalDate.now(), LocalTime.now(), LocalDate.now(), 
				flag == 1 ? VisitType.GROUP : VisitType.INDIVIDUAL, amount , visitorIDTxt.getText(),
				nameTxt.getText(), lastNameTxt.getText(), emailTxt.getText(), phoneTxt.getText(), 0, true, true,
				LocalTime.now(), null, true, LocalTime.now(), parkEmployee.getWorkingIn());
			
			if (flag ==1) 
				finalPrice = PaymentController.getInstance().calculateRegularPriceGuidedGroup(newBooking);
			else
				finalPrice = PaymentController.getInstance().calculateRegularPriceTravelersGroup(newBooking);
			//updates the final price due visitors amount and relevant price
			newBooking.setFinalPrice(finalPrice);
			System.out.println("final price =" + finalPrice);
			if(parkControl.checkParkAvailabilityForBooking(newBooking))
				parkControl.insertBookingToTable(newBooking, parkVisitor,"_park_active_booking", "active");
			//updating park capacity
			parkControl.updateCurrentCapacity(newBooking.getParkBooked().getParkName(), amount);
			int decision = showConfirmationAlert(ScreenManager.getInstance().getStage(),
					"Please charge the customer: " + finalPrice,
					Arrays.asList("Cash", "CreditCard"));
			if (decision == 2) {// if the user clicked on "Credit Card" he will redirect to pay screen and then to confirmation screen
				event.consume();
				try {
					ScreenManager.getInstance().showScreen("PaymentSystemScreenController", "/clientSide/fxml/PaymentSystemScreen.fxml",
							true, false, StageSettings.defaultSettings("Payment"), new Pair<Booking, ParkEmployee>(newBooking, parkEmployee));
				} catch (StatefulException | ScreenException e) {
					e.printStackTrace();
				} 
			}// else // user clicked on "Cash", showing the confirmation screen
			//	try {
			//		ScreenManager.getInstance().showScreen("ConfirmationScreenController",
			//				"/clientSide/fxml/ConfirmationScreen.fxml", true, true,
			//				StageSettings.defaultSettings("Confirmation"), new Pair<Booking, ParkEmployee>(newBooking, parkEmployee));
			//	} catch (StatefulException | ScreenException e) {
			//		e.printStackTrace();
			//	}	
    		}
    }
    
	/**
	 * @return
	 * Validation is performed on the values entered by the employee.
	 * An error message appears on the screen for each incorrect input.
	 * It returns true if the input correct, otherwise, it returns false.
	 */
	private boolean validate() {
		boolean valid = true;
		String error = "";

		// ID validation: 
		// Ensuring that the inserted ID is valid in terms of length and contains only digits.
		String insertedID = visitorIDTxt.getText();
		if (insertedID.length() != 9 || !insertedID.matches("\\d+")) {
			error += "You must enter a valid ID number with exactly 9 digits.\n";
			valid = false;
		}
		
		// Visitors amount validation: 
		// Ensuring that the inserted number of visitors falls within the range of 1 to 15.
		String amount = visitorsAmountTxt.getText();
		if (!amount.matches("\\d+")) {
				error += "You must enter a valid amount number of visits with only digits between 1-15.\n";
				valid = false;
		} else {
			int amountInt = Integer.parseInt(amount);
			if (amountInt > 15 || amountInt < 1) {
				error += "You must enter a valid amount number of visits with only digits between 1-15.\n";
				valid = false;
			}
		}
		
		// Phone number validation: 
		// Verifying if the inserted phone number is valid in terms of length and consists only of digits.
		String phoneNum = phoneTxt.getText();
		if (phoneNum.length() != 10 || !phoneNum.matches("\\d+")) {
			error += "You must enter a valid phone number with exactly 10 digits.\n";
			valid = false;
		}

		//Email validation: 
		// Checking if the inserted email conforms to the standard email format to ensure its validity.	
		if (emailTxt.getText().isEmpty()
				|| !emailTxt.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			error += "You must enter a valid email\n";
			valid = false;
		}

		if (!valid)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
		return valid;
	}

    @FXML
    void paneClicked(MouseEvent event) {

    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(true, true);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
    }

	/**
	 * This method is called after the FXML is invoked
	 */
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		bookingLbl.setStyle("-fx-alignment: center-right;"); //label component
		dateLbl.setStyle("-fx-alignment: center-right;"); //label component
		emailLbl.setStyle("-fx-alignment: center-right;");
		phoneLbl.setStyle("-fx-alignment: center-right;");
		titleLbl.setStyle("-fx-alignment: center-right;");
		visitorsLbl.setStyle("-fx-alignment: center-right;");
		makeReservationBtn.setStyle("-fx-alignment: center-right;");
		
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
		backButton.setStyle("-fx-alignment: center-right;");

	}
	
	/**
	 *  Activated after the X is clicked on the window.
	 *  The default is to show a Confirmation Alert with "Yes" and "No" options for the user to choose. 
	 *  "Yes" will check if the client is connected to the server, disconnect it from the server and the system.
	 */ /*
	@Override
	public void handleCloseRequest(WindowEvent event) {
		int decision = showConfirmationAlert(ScreenManager.getInstance().getStage(), "Are you sure you want to leave?",
				Arrays.asList("Yes", "No"));
		if (decision == 2) // if the user clicked on "No"
			event.consume();
		else { // if the user clicked on "Yes" and he is connected to server
			logOut(null); //log out from go nature system
    		System.out.println("User logged out");
			UuserController.getInstance().disconnectClientFromServer(); 
		}
	}*/

	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		setParkEmployee(PE);	
		park = PE.getWorkingIn();
		this.bookingLbl.setText(getScreenTitle());
	    this.bookingLbl.underlineProperty();
	}

	@Override
	public String getScreenTitle() {
		return parkEmployee.getWorkingIn().getParkName();
	}
	
	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}
			

}