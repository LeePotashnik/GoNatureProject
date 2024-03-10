package clientSide.gui;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ParkVisitorAccountScreen extends AbstractScreen{

	private String PV_Table, PV_Col;
	
	private GoNatureUsersController UC;

	private ParkVisitor parkVisitor;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, managingBookingBTN, visitBookingBTN;
    
    @FXML
    private Label NameLable, Title;

    @FXML
    void goTOManagingBookingScreen(ActionEvent event) {

    }
    
    @FXML
    void goTOVisitBookingScreen(ActionEvent event) {

    }

    /**
     * @param event
     * parkVisitor clicked on "Log out" button, an update query is executed to alter the value of the 'isLoggedIn' field
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event){
    	if (UC != null) {
    		if (this.UC.checkLogOut(PV_Table,PV_Col,parkVisitor.getIdNumber()))
    			parkVisitor.setLoggedIn(false);
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
		Title.setStyle("-fx-alignment: center-right;"); //label component
		managingBookingBTN.setStyle("-fx-alignment: center-right;");
		visitBookingBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
		UC = new GoNatureUsersController();
	    
	}

	/**
	 *The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class instance- parkVisitor.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkVisitor PV = (ParkVisitor)information;
		setParkVisitor(PV);	
		setPV_Table_andPV_Col(PV.getVisitorType());
		this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
	    this.NameLable.underlineProperty();
	    
	}

	/**
	 * @param visitorType
	 * sets PV_Table and PV_Col to reflect the correct table and ID column from the database according to the type of customer.
	 */
	public void setPV_Table_andPV_Col(VisitorType visitorType) {
		if (visitorType.equals("TRAVELLER")) {
			PV_Col = "travellerId";
			PV_Table = "traveller";
		}	
		else {
			PV_Table = "group_guide";
			PV_Col = "groupGuideId";
		}
	}

	public ParkVisitor getParkManager() {
		return parkVisitor;
	}

	public void setParkVisitor(ParkVisitor parkVisitor) {
		this.parkVisitor = parkVisitor;
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
