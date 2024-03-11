package common.controllers;

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

		ScreenManager.getInstance().showScreen("VisitorsLoginScreen",
				"/clientSide/fxml/VisitorsLogin.fxml", false, false,
				StageSettings.defaultSettings("GoNature System - Reservations"), null);

	}
}
