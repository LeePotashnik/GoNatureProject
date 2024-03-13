package common.controllers;

import java.util.ArrayList;

import entities.DepartmentManager;
import entities.Park;
import entities.ParkManager;

/**
 * This class has one method for running a specific screen. It is a temporary
 * class for testing functionality. Will not be part of the final project. No
 * need to change other screens/classes.
 */
public class TemporaryRunner {
	public void showScreen() throws StatefulException, ScreenException {
		/**
		 * The showScreen method gets: 1. Screen name (String) 2. Screen fxml file,
		 * including packages (String) 3. showOnce (boolean) - if we need the screen to
		 * be shown only ONCE (like - a payment screen for example) 4. saveState
		 * (boolean) - if we need to save the CURRENT screen's data 5. StageSettings
		 * (Use the default) 6. Information object to the screen (if needed to have
		 * information BEFORE load)
		 */

		Park Hawaii = new Park(9,"Hawaii Volcanoes", "Hilo", "Hawaii", "Pacific", "151071559", "638683080", 100, 80, 4, 0);
		Park BigBend = new Park(2,"Big Bend", "Alpine", "Texas", "Southern", "883879564", "104666977", 100, 80, 4, 0);
		Park Congaree = new Park(3,"Congaree", "Columbia", "South Carolina", "Southern", "223549857", "104666977", 100, 80, 4, 0);
		Park Everglades = new Park(4,"Everglades", "Homestead", "Florida", "Southern", "450849137", "104666977", 100, 80, 4, 0);
		ArrayList<Park> parks = new ArrayList<>();
		parks.add(BigBend);
		parks.add(Congaree);
		parks.add(Everglades);
//		parks.add(new Park(2,"Big Bend", "Alpine", "Texas", "Southern", "883879564", "104666977", 100, 80, 4, 0));
//        parks.add(new Park(3,"Congaree", "Columbia", "South Carolina", "Southern", "223549857", "104666977", 100, 80, 4, 0));
//        parks.add(new Park(4,"Everglades", "Homestead", "Florida", "Southern", "450849137", "104666977", 100, 80, 4, 0));
		ParkManager pm = new ParkManager("151071559", "Reese", "Moore", "reese.moore@gonature.com","0503868747","Hawaii Volcanoes","reese.moore", "Reese8##649112", false);
		DepartmentManager dm = new DepartmentManager("104666977", "Peyton", "Martin","Peyton.Martin@gonature.com","0547654321","Southern", "Peyton.Martin", "Peyton5678%",
		false);
		    //Add parks that are under the 'Southern' department
        

        // Now pass the relevant parks to the DepartmentManagerReportsController along with the department manager
        Object[] infoForScreen = { dm, parks };
//
        ScreenManager.getInstance().showScreen("DepartmentManagerReportsController",
                "/clientSide/fxml/DepartmentManagerReports.fxml", false, false,
                StageSettings.defaultSettings("GoNature System - Reservations"), infoForScreen);
    //}

//		ScreenManager.getInstance().showScreen("ParkManagerReportScreenController",
//				"/clientSide/fxml/ParkManagerReportScreen.fxml", false, false,
//				StageSettings.defaultSettings("GoNature System - Reservations"),pm);
//		ScreenManager.getInstance().showScreen("DepartmentManagerReportsController",
//				"/clientSide/fxml/DepartmentManagerReports.fxml", false, false,
//				StageSettings.defaultSettings("GoNature System - Reservations"),dm);
	//	ParkVisitor visitor = new ParkVisitor("1234567890", "Elad", "Krauz", "eladkrauz0905@gmail.com", "0526687878",
//				"elad.krauz", "password", true, VisitorType.TRAVELLER);
//		ScreenManager.getInstance().showScreen("BookingScreenController", "/clientSide/fxml/BookingScreen.fxml", false,
//				false, StageSettings.defaultSettings("GoNature System - Reservations"), visitor);

	}
}

	

