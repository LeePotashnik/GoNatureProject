package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.GoNatureUsersController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.DepartmentManager;
import entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DepartmentManagerAccountScreen extends AbstractScreen{

	private GoNatureUsersController UC;
	
	private DepartmentManager departmentManager;
	
    @FXML
    private Label Title, privateName;

    @FXML
    private Button approvingDataBTN, reportsBTN, logOutBTN, currentCapacitiesBTN;

    @FXML
    private ImageView goNatureLogo;

    /**
     * @param event
     * When the 'approvingData' button is pressed, 
     * the park MANAGER will be redirected to the 'ParametersApprovingScreen'
     */
    @FXML
    void GoToApprovingParksDataScreen(ActionEvent event) {
    	
    }

    /**
     * @param event
     * When the 'reports' button is pressed, 
     * the park MANAGER will be redirected to the 'ParkDepartmentalReportsScreen'
     */
    @FXML
    void GoToReportsScreen(ActionEvent event) {

    }

  /**
  * @param event
  * When the 'Current Capacity' button is pressed, 
  * the department manager will see a pop-up screen with the current capacity according to park parameters at the DB
  */
    @FXML
    void getCurrenetCapacities(ActionEvent event) {
    	int size = departmentManager.getResponsible().size();
    	int i = 0;
    	String[] parks = new String[size];
    	String output = "";
    	for (Park park : departmentManager.getResponsible())
    		parks[i++] = park.getParkName();
    	if (UC != null) {
    		for (i = 0; i<size; i++){
        	    String currCap = UC.checkCurrentMaximumCapacity(parks[i]);
    			output += "Capacity in " + parks[i] + "park: " + currCap + "\n";
        	}
    	    showInformationAlert(ScreenManager.getInstance().getStage(), output);
    	} else {
    		showErrorAlert(ScreenManager.getInstance().getStage(), "UC IS NULL");
    	}
    }

    /**
     * @param event
     * department manager clicked on 'Log out' button, an update query is executed to alter the value of the 'isLoggedIn' field
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (UC != null) {
    		if (this.UC.checkLogOut("department_manager","departmentManagerId",departmentManager.getIdNumber()))
    			departmentManager.setLoggedIn(false);
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
    
	public DepartmentManager getParkManager() {
		return departmentManager;
	}

	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}
	
	/**
	 * This method is called after the FXML is invoked
	 */
	@Override
	public void initialize() {
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		approvingDataBTN.setStyle("-fx-alignment: center-right;");
		reportsBTN.setStyle("-fx-alignment: center-right;");
		currentCapacitiesBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
		UC = new GoNatureUsersController(); 
	}

	/**
	 * The method receives data from the previous screen it came from. 
	 * Retrieving the data is done to populate relevant class instance- DepartmentManager.
	 * It updates JavaFX labels components for their display on the screen.
	 */
	@Override
	public void loadBefore(Object information) {
		DepartmentManager DM = (DepartmentManager)information;
		setDepartmentManager(DM);		
		this.privateName.setText("Hello " + departmentManager.getFirstName() + " " + departmentManager.getLastName());
	    this.privateName.underlineProperty();
		this.Title.setText(getScreenTitle());
	    this.Title.underlineProperty();	
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return departmentManager.getManagesDepartment() + " Departmrnt Manager";
	}
}
