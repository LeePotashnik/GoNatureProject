package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.Stateful;
import common.controllers.StatefulException;
import entities.Booking;
import entities.Park;
import entities.ParkVisitor;
import entities.ParkVisitor.VisitorType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

/**
 * Controls the park visitor account screen in the GoNature application, providing functionality
 * for managing bookings, confirming visit arrivals, and user logout. It adjusts available options
 * based on visitor type and specific booking details, enhancing the user experience for park visitors.
 * 
 * The controller leverages GoNatureUsersController for user-related operations and interacts with
 * ParkController for booking and park capacity queries. It ensures that visitors can efficiently
 * manage their park visits and related activities directly from their account interface.
 */
public class ParkVisitorAccountScreenController extends AbstractScreen implements Stateful{

	private GoNatureUsersController userControl;
	private ParkVisitor parkVisitor;
	ArrayList<Booking> bookingsList = null;
	
    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, managingBookingBTN, visitBookingBTN, arrivalConfirmationBTN;
    
    @FXML
    private Label NameLable;

    public ParkVisitorAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
	}
    
    /**
     * @param event
     * When the 'Managing Booking' button is pressed, 
     * the park visitor will be redirected to the 'ManagingBookingScreen'
     */
    @FXML
    void goTOManagingBookingScreen(ActionEvent event) {
    	try {
    		ScreenManager.getInstance().showScreen("BookingViewScreenController",
    				"/clientSide/fxml/BookingViewScreen.fxml", true, true, bookingsList);
    		} catch (ScreenException | StatefulException e) {
    				e.printStackTrace();
		}
    }
    
    /**
     * @param event
     * When the 'Visit Booking' button is pressed, 
     * the park visitor will be redirected to the 'ManagingBookingScreen'
     */
    @FXML
    void goTOVisitBookingScreen(ActionEvent event) {
    	try {
    		ScreenManager.getInstance().showScreen("CheckingNotoficationsScreenScreenConrtroller",
    				"/clientSide/fxml/CheckingNotoficationsScreenScreen.fxml", true, true, bookingsList);
    		} catch (ScreenException | StatefulException e) {
    				e.printStackTrace();
		}
    }
    
    /**
     * @param event
     * When the 'Visit Booking' button is pressed, 
     * the park visitor will be redirected to the 'ManagingBookingScreen'
     */
    @FXML
    void ArrivalConfirmationPopUp(ActionEvent event) {
    	if (!showButton()) 
			showErrorAlert("There is currently no reservation to confirm.");
    	else {
    		//the park visitor will be redirected to the 'CheckingNotoficationsScreenScreen'
    		try {
        		ScreenManager.getInstance().showScreen("CheckingNotoficationsScreenScreenConrtroller",
        				"/clientSide/fxml/CheckingNotoficationsScreenScreen.fxml", true, true, bookingsList);
        		} catch (ScreenException | StatefulException e) {
        				e.printStackTrace();
    		}
    	}
    }

    /**
     * @param event
     * parkEmplyee clicked on 'Log out' button, an update query is executed to alter the value of the 
     * 'isLoggedIn' field in database. The user will return to main Screen.
     * @throws CommunicationException 
     */
    @FXML
    void logOut(ActionEvent event) {
    	if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
    		if (userControl.logoutUser())
    			parkVisitor.setLoggedIn(false); 
    		System.out.println("User logged out");
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
    
    
    boolean showButton() {
    	ParkController parkControl = ParkController.getInstance();
    	ArrayList<Park> parks = parkControl.fetchParks(); 
    	ArrayList<Booking> bookings = new ArrayList<>(); 
    	for (Park park : parks) {
    		String parkTable = parkControl.nameOfTable(park);
    		System.out.println(parkTable);
    		try {
    			Booking booking = parkControl.checkIfBookingExists(parkTable,"idNumber",parkVisitor.getIdNumber());
    			if (booking.isRecievedReminder() && booking.getReminderArrivalTime().equals(LocalDate.now()))
    				bookings.add(booking);
    		} catch (NullPointerException e) {
    			
    		}
    	}
    	if (bookings.size() == 0)
    		return false;
    	bookingsList = bookings;
    	return true;
    }

    /**
     * Sets up initial UI elements and loads resources upon the FXML file loading. This includes styling
     * buttons, labels, and loading the GoNature logo, ensuring the user interface is ready for interaction.
     */
	@Override
	public void initialize() {
		/*
		 * parkVisitor = (ParkVisitor) userControl.restoreUser();
		if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
			this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
			this.NameLable.underlineProperty();
		} else 
			logOutBTN.setDisable(true);
		if (!showButton())
			showErrorAlert("There is currently no reservation to confirm.");
		 */
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
		managingBookingBTN.setStyle("-fx-alignment: center-right;");
		visitBookingBTN.setStyle("-fx-alignment: center-right;");
		logOutBTN.setStyle("-fx-alignment: center-right;");	
	}

	/**
	 * Prepares visitor-specific data on the screen before it's displayed. It sets the visitor's information
	 * and adjusts UI elements based on the visitor's type, such as showing or hiding the arrival confirmation button.
	 *
	 * @param information 	A ParkVisitor object containing details about the visitor.
	 */
	@Override
	public void loadBefore(Object information) {
		ParkVisitor PV = (ParkVisitor)information;
		setParkVisitor(PV);	
		if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
			this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
			this.NameLable.underlineProperty();
		}
		userControl.saveUser(parkVisitor);
		//need to run with thread
		if (showButton())
			showErrorAlert("There is a reservation to confirm.");

	}

	public ParkVisitor getParkManager() {
		return parkVisitor;
	}

	public void setParkVisitor(ParkVisitor parkVisitor) {
		this.parkVisitor = parkVisitor;
	}

	@Override
	public String getScreenTitle() {
		return null;
	}

	/**
	 * Persists the current state of the visitor's session for later restoration. This includes saving the
	 * visitor's details, allowing for a seamless experience when navigating between screens.
	 */
	@Override
	public void saveState() {
		userControl.saveUser(parkVisitor);
	}

	/**
	 * Restores the previously saved state of the visitor's session. This ensures that the visitor's
	 * information and screen settings are maintained, providing continuity in the user experience.
	 */
	@Override
	public void restoreState() {
		parkVisitor = (ParkVisitor) userControl.restoreUser();
		if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
			this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
			this.NameLable.underlineProperty();
		}
		if (!showButton())
			showErrorAlert("There is currently no reservation to confirm.");

	}

}
