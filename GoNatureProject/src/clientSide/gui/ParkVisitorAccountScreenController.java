package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
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

/**
 * Controls the park visitor account screen in the GoNature application, providing functionality
 * for managing bookings, confirming visit arrivals, and user logout. It adjusts available options
 * based on visitor type and specific booking details, enhancing the user experience for park visitors.
 * 
 * The controller leverages GoNatureUsersController for user-related operations and interacts with
 * ParkController for booking and park capacity queries. It ensures that visitors can efficiently
 * manage their park visits and related activities directly from their account interface.
 */
public class ParkVisitorAccountScreenController extends AbstractScreen{

	private GoNatureUsersController userControl;
	private ParkVisitor parkVisitor;
	private ArrayList<Booking> bookingsList = null;
	
    @FXML
    private ImageView goNatureLogo;

    @FXML
    private Button logOutBTN, managingBookingBTN, visitBookingBTN, arrivalConfirmationBTN;
    
    @FXML
    private Label NameLable;

    public ParkVisitorAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
	}
    
    public ParkVisitor getParkManager() {
		return parkVisitor;
	}

	public void setParkVisitor(ParkVisitor parkVisitor) {
		this.parkVisitor = parkVisitor;
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
    				"/clientSide/fxml/BookingViewScreen.fxml", true, false, null);
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
    				"/clientSide/fxml/CheckingNotoficationsScreenScreen.fxml", true, false, null);
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
			showErrorAlert("No reservations are available for confirmation at the moment.");
    	else {
    		//the park visitor will be redirected to the 'CheckingNotoficationsScreenScreen'
    		try {
        		ScreenManager.getInstance().showScreen("CheckingNotoficationsScreenConrtroller",
        				"/clientSide/fxml/CheckingNotoficationsScreen.fxml", true, false, bookingsList);
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
    
    /**
     * Determines if the button for booking confirmation should be shown to the user.
     * This decision is based on checking all parks for any active bookings associated with
     * the current park visitor. A booking qualifies if the visitor has received a reminder
     * for it, indicating an upcoming visit. This method iterates through all parks, checks
     * for such bookings, and aggregates them into a list. If at least one qualifying booking
     * is found, the method returns true, signaling that the confirmation button should be displayed.
     * 
     * @return true if the visitor has at least one active booking with a received reminder,
     *         otherwise false.
     */
    private boolean showButton() {
    	ParkController parkControl = ParkController.getInstance();
    	ArrayList<Park> parks = parkControl.fetchParks(); 
    	ArrayList<Booking> bookings = new ArrayList<>(); 
    	// Iterates through each park to check for active bookings associated with the visitor
    	for (Park park : parks) {
    		String parkTable = parkControl.nameOfTable(park) + "_park_active_booking";
    		System.out.println(parkTable);
        	ArrayList<Booking> tempBookings = parkControl.checkIfBookingExists(parkTable,"idNumber",parkVisitor.getIdNumber());
        	// Adds bookings with received reminders to the aggregate list
        	if (tempBookings!=null) {
				for (Booking booking : tempBookings) {
					if (booking.isRecievedReminder() && booking.getReminderArrivalTime() != null) 
		    			bookings.add(booking);
					} 
			}
    	}
        // Determines the visibility of the confirmation button based on the presence of qualifying bookings
    	if (bookings.size() == 0)
    		return false;
    	System.out.println(bookings.size());
    	// Updates the global list of bookings for further processing
    	bookingsList = bookings;
    	return true;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It performs initial setup for the screen,
     * including setting text for the user's name, determining visibility of certain buttons,
     * and applying styles to UI components.
     */
    @Override
    public void initialize() {
        // Restores the park visitor from the saved state to ensure continuity in user experience.
        parkVisitor = (ParkVisitor) userControl.restoreUser();
        // Saves the current state of the park visitor for potential future use.
        userControl.saveUser(parkVisitor);

        // Sets greeting text dynamically based on the visitor's information.
        if (parkVisitor.getVisitorType() == VisitorType.GROUPGUIDE) {
            this.NameLable.setText("Hello " + parkVisitor.getFirstName() + " " + parkVisitor.getLastName());
            this.NameLable.underlineProperty(); // Adds underline to emphasize the name label.
        } else {
            logOutBTN.setVisible(false); // Hides the logout button if not a group guide.
        }

        // Shows an alert reminding the user to confirm their reservation if applicable.
        if (showButton()) {
            showInformationAlert("Please confirm your reservation");
        }
        
        // Sets the GoNature logo image.
        goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNature.png")));
        
        // Applies alignment styling to buttons for consistency across the user interface.
        managingBookingBTN.setStyle("-fx-alignment: center-right;");
        visitBookingBTN.setStyle("-fx-alignment: center-right;");
        logOutBTN.setStyle("-fx-alignment: center-right;");
    }


	/**
	 * @param information 	A ParkVisitor object containing details about the visitor.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return null;
	}

}
