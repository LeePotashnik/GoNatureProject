package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.ParkEmployee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class parkEmployeeAccountScreen extends AbstractScreen{

	private String parkTable;
	
	private GoNatureUsersController UC;

	private ParkEmployee parkEmployee;
    @FXML
    private Button InvoiceproductionBTN;

    @FXML
    private Label Title, privateName;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, parkEntryPreBookedBTN, parkEntryCasualBTN;

    /**
     * @param event
     * When the 'Invoiceproduction' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryCasualScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void InvoiceScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("parkManagerAccountScreen",
				"/clientSide/fxml/parkManagerAccountScreen.fxml", false, false,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee);
    }

    /**
     * @param event
     * When the 'parkEntryCasual' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryCasualScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryCasualScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryCasualScreen",
				"/clientSide/fxml/ParkEntryCasualScreen.fxml", false, false,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee);
    }
    
    /**
     * @param event
     * When the 'parkEntryPreBooked' button is pressed, 
     * the park employee will be redirected to the 'ParkEntryPreBookedScreen'
     * @throws ScreenException 
     * @throws StatefulException 
     */
    @FXML
    void GoToParkEntryPreBookedScreen(ActionEvent event) throws StatefulException, ScreenException {
		ScreenManager.getInstance().showScreen("ParkEntryPreBookedScreen",
				"/clientSide/fxml/parkManagerAccountScreen.fxml", false, false,
				StageSettings.defaultSettings("GoNature System - Client Connection"), parkEmployee);
    }

    /**
     * @param event
     * parkEmplyee clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field in database
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (UC != null) {
    		if (this.UC.checkLogOut(parkTable,"employeeId",parkEmployee.getIdNumber()))
    			parkEmployee.setLoggedIn(false);
        	else 
        		showErrorAlert(ScreenManager.getInstance().getStage(), "Failed to log out");
    	}
    	else 
    		showErrorAlert(ScreenManager.getInstance().getStage(), "UC IS NULL");
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
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		parkEntryPreBookedBTN.setStyle("-fx-alignment: center-right;");
		InvoiceproductionBTN.setStyle("-fx-alignment: center-right;");
		parkEntryCasualBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
		UC = new GoNatureUsersController();
	}

	/**
	 * The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class attributes, such as updating the user's ID (IdEmpNumber),
	 * the table name (from DB) of the park they work at and the screen title.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkEmployee PE = (ParkEmployee)information;
		setParkEmployee(PE);	
		this.privateName.setText("Hello " + parkEmployee.getFirstName() + " " + parkEmployee.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(getScreenTitle());
	    this.Title.underlineProperty();
		}

	public String getParkTable() {
		return parkTable;
	}

	public void setParkTable(String parkName) {
		String[] Info = parkName.split(" ");
		this.parkTable = Info[0];
		for (int i = 1; i< Info.length; i++) {
			this.parkTable += Info[i];
			if (i != Info.length-1) //as long it is not the last word
				this.parkTable += "_";
		}
	}

	public ParkEmployee getParkEmployee() {
		return parkEmployee;
	}

	public void setParkEmployee(ParkEmployee parkEmployee) {
		this.parkEmployee = parkEmployee;
	}

	@Override
	public String getScreenTitle() {
		return parkEmployee.getWorkingIn().getParkName();
	}
}