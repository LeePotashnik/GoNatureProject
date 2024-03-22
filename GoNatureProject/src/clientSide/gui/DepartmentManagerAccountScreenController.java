package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.DepartmentManager;
import entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The DepartmentManagerAccountScreenController class manages the interactions within the department manager's
 * account screen in the GoNature application. It facilitates various operations. 
 * This class extends AbstractScreen for UI control and implements the Stateful interface to maintain and 
 * manage the state across different screens.
 *
 * It utilizes GoNatureUsersController for user management and ParkController for park-related operations, 
 * ensuring that department managers can effectively oversee and manage park data and reports.
 */
public class DepartmentManagerAccountScreenController extends AbstractScreen implements Stateful{

	private static GoNatureUsersController userControl;
	private ParkController parkControl;
	private DepartmentManager departmentManager;
	
    @FXML
    private Label title, privateName;

    @FXML
    private Button approvingDataBTN, reportsBTN, logOutBTN, currentCapacitiesBTN;

    @FXML
    private ImageView goNatureLogo;

    /**
     * Initializes a new instance of the DepartmentManagerAccountScreenController. It sets up the necessary
     * controller instances for user and park operations, facilitating the management of department-specific
     * functionalities within the application.
     */
    public DepartmentManagerAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
    	parkControl = ParkController.getInstance();
	}
    
    /**
     * @param event
     * When the 'approvingData' button is pressed, 
     * the park MANAGER will be redirected to the 'ParametersApprovingScreen'
     */
    @FXML
    void GoToApprovingParksDataScreen(ActionEvent event){
    }

    /**
     * @param event
     * When the 'reports' button is pressed, 
     * the park MANAGER will be redirected to the 'ParkDepartmentalReportsScreen'
     */
    @FXML
    void GoToReportsScreen(ActionEvent event) {
    	try {
			ScreenManager.getInstance().showScreen("DepartmentManagerReportsScreenController",
					"/clientSide/fxml/DepartmentManagerReportsScreen.fxml", false, true, departmentManager);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
    }

  /**
  * @param event
  * When the 'Current Capacity' button is pressed, 
  * the department manager will see a pop-up screen with the current capacity according to park parameters at the DB
  */
    @FXML
    void getCurrenetCapacities(ActionEvent event) {
    	//updates for each park the latest relevant parameters
    	for (int i = 0; i<departmentManager.getResponsible().size(); i++){
    		String parkName = departmentManager.getResponsible().get(i).getParkName();
        	String[] currCap = parkControl.checkCurrentCapacity(parkName);
    		if (currCap != null) {
    			//updates park parameters
    			departmentManager.getResponsible().get(i).setMaximumVisitors(Integer.parseInt(currCap[0]));
    			departmentManager.getResponsible().get(i).setMaximumOrders(Integer.parseInt(currCap[1]));
    			departmentManager.getResponsible().get(i).setTimeLimit(Integer.parseInt(currCap[2])); 
    			departmentManager.getResponsible().get(i).setCurrentCapacity(Integer.parseInt(currCap[3])); 
    		}	
    	}
    	int i = 0;
    	String output = "";
    	for (i = 0; i<departmentManager.getResponsible().size(); i++){
    		Park park = departmentManager.getResponsible().get(i);
        	//String[] currCap = parkControl.checkCurrentCapacity(parkName);
        	output+= "Capacity parameters in " + park.getParkName() + " park:\n	maximum visitors: " + park.getMaximumVisitors() +
        			"\n	maximum allowable quantity of visitors: " +park.getMaximumOrders() +"\n	current capacity:  " + park.getCurrentCapacity() +
        			"\n	time limit: " + park.getTimeLimit() + "\n";
        }
    	showInformationAlert(output);
    }
    /**
     * Logs out the department manager when the 'Log Out' button is pressed. This method updates the 'isLoggedIn'
     * status in the database and redirects the user to the main screen of the application.
     *
     * @param event The ActionEvent triggered by pressing the 'Log Out' button.
     * @throws CommunicationException If there is a communication issue with the server during the logout process.
     */ 
    @FXML
    void logOut(ActionEvent event) {
    	if (userControl.logoutUser()) {
    		departmentManager.setLoggedIn(false);
    		System.out.println("Department Manager logged out");
    		try {
        		ScreenManager.getInstance().showScreen("MainScreenConrtroller", "/clientSide/fxml/MainScreen.fxml", true,
        				false, null);
        	} catch (ScreenException | StatefulException e) {
        				e.printStackTrace();
    		}
    	}
        else 
        	showErrorAlert("Failed to log out"); 	
    }
    
	public DepartmentManager getParkManager() {
		return departmentManager;
	}

	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}
	
	/**
	 * Initializes the class. This method is automatically called after the FXML file has been loaded.
	 * It sets up initial configurations for UI components such as labels and buttons and loads the GoNature logo.
	 * Styles are applied to ensure consistent alignment and presentation according to the application's design standards.
	 */
	@Override
	public void initialize() {
		/*
		departmentManager = (DepartmentManager) userControl.restoreUser();
		this.screenTitle = departmentManager.getManagesDepartment()+"'s Department Manager";
		this.privateName.setText("Hello " + departmentManager.getFirstName() + " " + departmentManager.getLastName());
	    this.privateName.underlineProperty();
		this.title.setText(screenTitle);
	    this.title.underlineProperty();
		//updating the list of parks managed by the department manager
	    ArrayList<Park> parks = new ArrayList<>();
		parks = parkControl.fetchManagerParksList("departmentManagerId", departmentManager.getIdNumber());
    	try {
    		departmentManager.setResponsible(parks);	
    	}catch (NullPointerException e) {
    		System.out.println("cannot fetch parks list");
    	}
		 */
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		privateName.setStyle("-fx-alignment: center-right;"); //label component
		approvingDataBTN.setStyle("-fx-alignment: center-right;");
		reportsBTN.setStyle("-fx-alignment: center-right;");
		currentCapacitiesBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	/**
	 * Prepares the screen with necessary data before it is displayed. This method receives the DepartmentManager
	 * instance from the previous screen, updates UI labels with the department manager's name and department, and
	 * fetches a list of parks the department manager is responsible for. This setup ensures that the screen displays
	 * personalized and relevant information to the department manager upon entry.
	 *
	 * @param information The DepartmentManager object passed from the logIn screen, containing the manager's details.
	 */
	@Override
	public void loadBefore(Object information) {
		DepartmentManager DM = (DepartmentManager)information;
		setDepartmentManager(DM);
		userControl.saveUser(departmentManager);
		this.privateName.setText("Hello " + departmentManager.getFirstName() + " " + departmentManager.getLastName());
	    this.privateName.underlineProperty();
		this.title.setText(getScreenTitle());
	    this.title.underlineProperty();
		//updating the list of parks managed by the department manager
	    ArrayList<Park> parks = new ArrayList<>();
		parks = parkControl.fetchManagerParksList("departmentManagerId", departmentManager.getIdNumber());
    	try {
    		departmentManager.setResponsible(parks);	
    	}catch (NullPointerException e) {
    		System.out.println("cannot fetch parks list");
    	}
	}

	@Override
	public String getScreenTitle() { 
		return departmentManager.getManagesDepartment()+"'s Department Manager";
	}

	/**
	 * Saves the current state of the department manager's session. This includes persisting the department manager's
	 * details and the current screen title to maintain session continuity. 
	 */
	@Override
	public void saveState() {
		userControl.saveUser(departmentManager);
	}

	/**
	 * Restores the previously saved state of the department manager's session. This method is called to repopulate
	 * the department manager's details and the screen title, ensuring that the user experience remains consistent
	 * when navigating back to this screen. It retrieves the saved DepartmentManager instance and the screen title,
	 * updating the UI components to reflect the restored state.
	 */
	@Override
	public void restoreState() {
		departmentManager = (DepartmentManager) userControl.restoreUser();
		this.privateName.setText("Hello " + departmentManager.getFirstName() + " " + departmentManager.getLastName());
	    this.privateName.underlineProperty();
		this.title.setText(getScreenTitle());
	    this.title.underlineProperty();
	}
		
}
