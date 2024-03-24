package clientSide.gui;

import java.util.ArrayList;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParkController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.DepartmentManager;
import entities.Park;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * The DepartmentManagerAccountScreenController class manages the interactions within the department manager's
 * account screen in the GoNature application. It facilitates various operations. 
 * This class extends AbstractScreen for UI control.
 *
 * It utilizes GoNatureUsersController for user management and ParkController for park-related operations, 
 * ensuring that department managers can effectively oversee and manage park data and reports.
 */
public class DepartmentManagerAccountScreenController extends AbstractScreen{

	private static GoNatureUsersController userControl;
	private ParkController parkControl;
	private DepartmentManager departmentManager;
	
	// properties for the images animation
	private final static int IMAGE_VIEW_COUNT = 9; // Display 9 images at a time
	private final ImageView[] imageViews = new ImageView[IMAGE_VIEW_COUNT]; // Array for ImageViews
	private int currentIndex = 0; // Index to track current image set
			
    @FXML
    private Label Title, NameLable;
    @FXML
    private Button approvingDataBTN, reportsBTN, logOutBTN, currentCapacitiesBTN;
    @FXML
    private ImageView goNatureLogo, image1, image2, image3, image4, image5, image6, image7, image8, image9;
	@FXML
	private VBox imagesVbox, controlVbox;
	@FXML
	private Pane pane;
	@FXML
	private Rectangle rec;

    /**
     * Initializes a new instance of the DepartmentManagerAccountScreenController. It sets up the necessary
     * controller instances for user and park operations, facilitating the management of department-specific
     * functionalities within the application.
     */
    public DepartmentManagerAccountScreenController() {
    	userControl = GoNatureUsersController.getInstance();
    	parkControl = ParkController.getInstance();
	}
    
    public DepartmentManager getParkManager() {
		return departmentManager;
	}

	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}
    
    /**
     * @param event
     * When the 'approvingData' button is pressed, 
     * the park MANAGER will be redirected to the 'ParametersApprovingScreen'
     */
    @FXML
    void GoToApprovingParksDataScreen(ActionEvent event){
    	try {
			ScreenManager.getInstance().showScreen("ParametersApprovingScreenConrtroller",
					"/clientSide/fxml/ParametersApprovingScreen.fxml", false, false, null);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
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
					"/clientSide/fxml/DepartmentManagerReportsScreen.fxml", false, false, null);
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
    
	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}
    
    /**
	 * This method starts the parks images slide show using fade transitions
	 */
	private void startSlideshow() {
		// Create a runnable task for changing images
		FadeTransition fade1 = new FadeTransition(Duration.millis(2000), image1);
		fade1.setFromValue(0.0);
		fade1.setToValue(1.0);
		fade1.play();

		FadeTransition fade2 = new FadeTransition(Duration.millis(2000), image2);
		fade2.setFromValue(0.0);
		fade2.setToValue(1.0);
		fade2.play();

		FadeTransition fade3 = new FadeTransition(Duration.millis(2000), image3);
		fade3.setFromValue(0.0);
		fade3.setToValue(1.0);
		fade3.play();

		FadeTransition fade4 = new FadeTransition(Duration.millis(2000), image4);
		fade4.setFromValue(0.0);
		fade4.setToValue(1.0);
		fade4.play();

		FadeTransition fade5 = new FadeTransition(Duration.millis(2000), image5);
		fade5.setFromValue(0.0);
		fade5.setToValue(1.0);
		fade5.play();

		FadeTransition fade6 = new FadeTransition(Duration.millis(2000), image6);
		fade6.setFromValue(0.0);
		fade6.setToValue(1.0);
		fade6.play();

		FadeTransition fade7 = new FadeTransition(Duration.millis(2000), image7);
		fade7.setFromValue(0.0);
		fade7.setToValue(1.0);
		fade7.play();

		FadeTransition fade8 = new FadeTransition(Duration.millis(2000), image8);
		fade8.setFromValue(0.0);
		fade8.setToValue(1.0);
		fade8.play();

		FadeTransition fade9 = new FadeTransition(Duration.millis(2000), image9);
		fade9.setFromValue(0.0);
		fade9.setToValue(1.0);
		fade9.play();

		Runnable changeImagesTask = () -> {
			if (currentIndex >= imagePaths.size()) {
				currentIndex = 0; // Reset index to loop
			}

			// Duration for each fade transition
			final long fadeDuration = 1000; // 1000 milliseconds (1 second)

			// The delay increment between the start of each fade-out transition
			// This determines how quickly after one image starts fading out the next image
			// will start fading out.
			// For smoother transitions between 9 images, you might want a shorter delay
			// between starts,
			// e.g., starting the next fade-out after half the duration of the fade effect.
			final long delayIncrement = fadeDuration / 3; // Adjust this for smoother transitions between more images

			for (int i = 0; i < IMAGE_VIEW_COUNT; i++) {
				final int imageIndex = (currentIndex + i) % imagePaths.size();
				ImageView imageView = imageViews[i];
				Image newImage = new Image(imagePaths.get(imageIndex));

				// Apply fade-out transition on image change
				FadeTransition fadeOut = new FadeTransition(Duration.millis(fadeDuration), imageView);
				fadeOut.setFromValue(1.0);
				fadeOut.setToValue(0.0);
				fadeOut.setDelay(Duration.millis(i * delayIncrement)); // Adjust delay based on position in the sequence
				fadeOut.setOnFinished(event -> {
					imageView.setImage(newImage);
					FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeDuration), imageView);
					fadeIn.setFromValue(0.0);
					fadeIn.setToValue(1.0);
					fadeIn.play();
				});
				fadeOut.play();
			}
			currentIndex += IMAGE_VIEW_COUNT; // Move to the next set of images
		};

		// Schedule the task to run periodically
		javafx.animation.Timeline timeline = new javafx.animation.Timeline(
				new javafx.animation.KeyFrame(Duration.seconds(5), // Change images every 5 seconds
						event -> changeImagesTask.run()));
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();
	}

    	
    /**
     * This method is automatically invoked after the FXML has been loaded, setting up the initial state of the UI components.
     * It prepares the Department Manager's account screen by loading the user's data, setting greeting text, updating the title,
     * fetching and displaying the parks managed by the department manager, and applying styles to UI elements.
     */
    @Override
    public void initialize() {
        // Restores the DepartmentManager object from the saved user session to maintain continuity.
        departmentManager = (DepartmentManager) userControl.restoreUser();
        // Saves the current state of the DepartmentManager for potential future reference within the session.
        userControl.saveUser(departmentManager);

        // Sets the greeting text with the department manager's name to personalize the UI.
        this.NameLable.setText("Hello " + departmentManager.getFirstName() + " " + departmentManager.getLastName());
        this.NameLable.underlineProperty(); // Adds an underline for visual emphasis.

        // Dynamically sets the screen title based on the department manager's department.
        this.Title.setText(getScreenTitle());
        this.Title.underlineProperty(); // Underlines the title for emphasis.

        // Attempts to fetch and set the list of parks managed by the department manager.
        ArrayList<Park> parks = parkControl.fetchManagerParksList("departmentManagerId", departmentManager.getIdNumber());
        try {
            departmentManager.setResponsible(parks); // Sets the list of parks the department manager is responsible for.
        } catch (NullPointerException e) {
            System.out.println("cannot fetch parks list"); // Handles the case where no parks list could be fetched.
        }

        // Sets the GoNature logo on the screen.
        goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));

        // Applies alignment and style configurations to UI components to ensure consistency with the application's design.
        NameLable.setStyle("-fx-alignment: center-right;");
        approvingDataBTN.setStyle("-fx-alignment: center-right;");
        reportsBTN.setStyle("-fx-alignment: center-right;");
        currentCapacitiesBTN.setStyle("-fx-alignment: center-right;");
        logOutBTN.setStyle("-fx-alignment: center-right;");
        
        // setting the image view array
 		imageViews[0] = image1;
 		imageViews[1] = image2;
 		imageViews[2] = image3;
 		imageViews[3] = image4;
 		imageViews[4] = image5;
 		imageViews[5] = image6;
 		imageViews[6] = image7;
 		imageViews[7] = image8;
 		imageViews[8] = image9;

 		// setting 3 first images
 		imageViews[0].setImage(new Image(imagePaths.get(0)));
 		imageViews[1].setImage(new Image(imagePaths.get(1)));
 		imageViews[2].setImage(new Image(imagePaths.get(2)));
 		imageViews[3].setImage(new Image(imagePaths.get(3)));
 		imageViews[4].setImage(new Image(imagePaths.get(4)));
 		imageViews[5].setImage(new Image(imagePaths.get(5)));
 		imageViews[6].setImage(new Image(imagePaths.get(6)));
 		imageViews[7].setImage(new Image(imagePaths.get(7)));
 		imageViews[8].setImage(new Image(imagePaths.get(8)));

 		currentIndex = 9;
 		startSlideshow();
    }


	/**
	 * @param information The DepartmentManager object passed from the logIn screen, containing the manager's details.
	 */
	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() { 
		return departmentManager.getManagesDepartment()+"'s Department Manager";
	}
		
}
