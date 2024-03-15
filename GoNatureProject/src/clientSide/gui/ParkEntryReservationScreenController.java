package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
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

public class ParkEntryReservationScreenController extends AbstractScreen{

	private ParkEmployee parkEmployee;
	private Booking newBooking; 
	private static ParkController parkControl;
	private ParkVisitor parkVisitor;
	
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
    		Park park = parkEmployee.getWorkingIn();
    		int available = park.getMaximumVisitors() * park.getMaximumOrders() / 100;
    		if (available - park.getCurrentCapacity() > Integer.parseInt(visitorsAmountTxt.getText()))
				valid = true;    		
    	}
    	if (valid) {
    		//if there is a place for the new group reservation
    		int flag = -1;
    		int price, amount;
    		parkVisitor = null;
    		parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists("traveller", "travellerId", visitorIDTxt.getText());
    		if (parkVisitor != null) {//indicates the visitor is exist in database as a 'traveller'
    			flag = 1; 
    		} if (flag == -1) { 		
    			parkVisitor = (ParkVisitor) parkControl.checkIfVisitorExists("group_guide", "groupGuideId", visitorIDTxt.getText());
    			if (parkVisitor != null) 
    				flag = 2; //indicates the visitor is exist in database as a 'groupGuide'
    		} if (flag == -1) { //the visitor is not exist in database.
    			parkVisitor = new ParkVisitor(visitorIDTxt.getText(), nameTxt.getText(), lastNameTxt.getText(),
    					emailTxt.getText(), phoneTxt.getText(), "", "", false, VisitorType.TRAVELLER);
    			flag = 0;
    		}
    		price = parkControl.checkPrice(parkVisitor); //price per visitor
    		amount = Integer.parseInt(visitorsAmountTxt.getText());

    		String bookingId = ((Integer) (1000000000 + new Random().nextInt(900000000))).toString();
			newBooking = new Booking(bookingId, LocalDate.now(), LocalTime.now(), LocalDate.now(), 
				parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE ? VisitType.GROUP : VisitType.INDIVIDUAL, 
				amount , parkVisitor.getIdNumber(), parkVisitor.getFirstName(),
				parkVisitor.getLastName(), parkVisitor.getEmailAddress(), parkVisitor.getPhoneNumber(), amount*price, true, true,
				LocalTime.now(), null, true, LocalTime.now(), parkEmployee.getWorkingIn());
			if(parkControl.checkParkAvailabilityForBooking(newBooking))
				parkControl.insertBookingToTable(newBooking, parkVisitor,"_park_active_booking", "active");
			
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
//bookingLbl, dateLbl, emailLbl, hourLbl, parkLbl, phoneLbl, titleLbl, visitorsLbl;
	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		setParkEmployee(PE);	
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