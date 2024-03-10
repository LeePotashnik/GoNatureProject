package clientSide.gui;

import java.util.regex.Pattern;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.ParkEmployee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class ParkEntryCasualScreen extends AbstractScreen implements Stateful{

	private ParkEmployee parkEmployee;
	
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

    
    @FXML
    void makeReservation(ActionEvent event) {
    	if (validate()) {
    		
    	}
    }

    @FXML
    void paneClicked(MouseEvent event) {

    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().goToPreviousScreen(false);
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
		//backButton.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		bookingLbl.setStyle("-fx-alignment: center-right;"); //label component
		dateLbl.setStyle("-fx-alignment: center-right;"); //label component
		emailLbl.setStyle("-fx-alignment: center-right;");
		phoneLbl.setStyle("-fx-alignment: center-right;");
		titleLbl.setStyle("-fx-alignment: center-right;");
		visitorsLbl.setStyle("-fx-alignment: center-right;");
		backButton.setStyle("-fx-alignment: center-right;");
		makeReservationBtn.setStyle("-fx-alignment: center-right;");
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

	@Override
	public void saveState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState() {
		// TODO Auto-generated method stub
		
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
				error += "HERE You must enter a valid amount number of visits with only digits between 1-15.\n";
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
		String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		String email = emailTxt.getText();
		if (pattern.matcher(email) != null) {
			error += "You must enter a valid email\n";
			valid = false;
		}
		if (!valid)
			showErrorAlert(ScreenManager.getInstance().getStage(), error);
		return valid;
	}

}
