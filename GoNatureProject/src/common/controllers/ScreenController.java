package common.controllers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ScreenController {
	private static ScreenController instance;

	private final Stage stage;
	private final Map<String, Pair<Parent, Object>> screensMap = new HashMap<>();
	private final Deque<String> screensStack = new ArrayDeque<>();

	private ScreenController() {
		stage = new Stage();
	}

	/**
	 * The ScreenChanger is defined as a Singleton class. This method allows
	 * creating an instance of the class only once during runtime of the
	 * application.
	 * 
	 * @return the ScreenChanger instance
	 */
	public static ScreenController getInstance() {
		if (instance == null)
			instance = new ScreenController();
		return instance;
	}

	/**
	 * A getter for the application's parimary stage
	 * 
	 * @return the stage of the application
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * This method is called when a screen needs to shown only once, and not to be
	 * saved on the screens stack. This way, clicking on "Back" button from the next
	 * screen would not show this screen again.
	 * 
	 * @param screenName   the screen's name to be shown
	 * @param fxmlResource the fxml file name of the screen to be shown
	 * @param settings     the settings to be implemented on the stage
	 * @throws ScreenException if the loading of the FXML file failed
	 */
	public void showScreenOnce(String screenName, String fxmlResource, StageSettings settings) throws ScreenException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new ScreenException(e.getMessage());
		}
		Object controller = loader.getController();
		stage.setScene(new Scene(root)); // setting the scene
		settings.implementSettings(settings, stage); // implementing the settings of the stage
		stage.setOnCloseRequest(((AbstractScreen) controller)::handleCloseRequest); // handleCloseRequest method of the
																					// controller will be called
		root.requestFocus(); // setting the focus on the root, not on the GUI components
		stage.show(); // showing the new scene
	}

	/**
	 * This method is called when a screen needs to be shown. It is pushed to the
	 * stack as the most current screen displayed, and to the map for identification
	 * purposes.
	 * 
	 * @param screenName   the screen's to be shown
	 * @param fxmlResource the fxml file name of the screen to be shown
	 * @param saveState    true if the current screen needs to save its current
	 *                     state, false otherwise. The screen in this case has to
	 *                     implement Stateful.
	 * @param settings     the settings to be implemented on the stage
	 * @throws StatefulException if the saveState flag is true while the current
	 *                           screen does not implement Stateful
	 * @throws ScreenException   if the loading of the FXML file failed
	 */
	public void showScreen(String screenName, String fxmlResource, boolean saveState, StageSettings settings)
			throws StatefulException, ScreenException {
		// checking if the saveState flag is true. In this case, calling the saveState()
		// method of the current screen's controller
		if (saveState) {
			Object currentScreenController = screensMap.get(screensStack.peek()).getValue();
			if (!(currentScreenController instanceof Stateful))
				throw new StatefulException("Saving screen's state is allowed only for screens who implement Stateful");
			((Stateful) screensMap.get(screensStack.peek()).getValue()).saveState();
		}

		// now loading the new screen
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new ScreenException(e.getMessage());
		}
		Object controller = loader.getController();
		screensMap.put(screenName, new Pair<>(root, controller)); // adding the screen to the screens map
		screensStack.push(screenName); // pushing the screen to the screens stack
		stage.setScene(new Scene(screensMap.get(screenName).getKey())); // setting the scene
		settings.implementSettings(settings, stage); // implementing the settings of the stage
		stage.setOnCloseRequest(((AbstractScreen) controller)::handleCloseRequest); // handleCloseRequest method of the
																					// controller will be called
		root.requestFocus(); // setting the focus on the root, not on the GUI components
		stage.show(); // showing the new scene
	}

	/**
	 * This method is called when a "Back" button is clicked from some screen, or if
	 * a screen has finished its action and wants the previous screen to be shown.
	 * The screen who called this method is on the top of the stack, so it is
	 * removed and then the new top is displayed on the stage.
	 *
	 * @param restoreState indicates whether the state of the previous screen needs
	 *                     to be restored, or the previous screen should be loaded
	 *                     as new screen
	 * @throws ScreenException   if trying to access a previous screen that does not
	 *                           exist
	 * @throws StatefulException if the restoreState flag is true while the screen
	 *                           does not implement Stateful
	 */
	public void goToPreviousScreen(boolean restoreState) throws ScreenException, StatefulException {
		// checking if there's a screen to go back to
		if (screensStack.size() <= 1)
			throw new ScreenException("No screens to go back to");

		screensStack.pop();

		// checking if the restoreState flag is true. In this case, calling the
		// restoreState()
		// method of the current screen's controller
		if (restoreState) {
			Object currentScreenController = screensMap.get(screensStack.peek()).getValue();
			if (!(currentScreenController instanceof Stateful))
				throw new StatefulException(
						"Restoring screen's state is allowed only for screens who implement Stateful");
			((Stateful) screensMap.get(screensStack.peek()).getValue()).restoreState();
		}

		String previousScreen = screensStack.peek();
		stage.setScene(new Scene(screensMap.get(previousScreen).getKey()));
		stage.show();
	}
}
