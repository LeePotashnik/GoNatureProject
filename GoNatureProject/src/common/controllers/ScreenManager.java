package common.controllers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ScreenManager {
	private static ScreenManager instance;

	private final Stage stage;
	private final Map<String, AbstractScreen> screensMap = new HashMap<>();
	private final Deque<String> screensStack = new ArrayDeque<>();

	private ScreenManager() {
		stage = new Stage();
	}

	/**
	 * The ScreenChanger is defined as a Singleton class. This method allows
	 * creating an instance of the class only once during runtime of the
	 * application.
	 * 
	 * @return the ScreenChanger instance
	 */
	public static ScreenManager getInstance() {
		if (instance == null)
			instance = new ScreenManager();
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
	 * This method is called when a screen needs to be shown. It is pushed to the
	 * stack as the most current screen displayed, and to the map for identification
	 * purposes.
	 * 
	 * @param screenName   the screen's name to be shown
	 * @param fxmlResource the fxml file name of the screen to be shown
	 * @param showOnce     true if the screen needs to be shown only once. On this
	 *                     case, the screen won't be pushed to the screensStack and
	 *                     the the screensMap.
	 * @param saveState    true if the current screen needs to save its current
	 *                     state, false otherwise. The screen in this case has to
	 *                     implement Stateful.
	 * @param settings     the settings to be implemented on the stage
	 * @param information  an object that contains information for the screen,
	 *                     before its load
	 * @throws StatefulException if the saveState flag is true while the current
	 *                           screen does not implement Stateful
	 * @throws ScreenException   if the loading of the FXML file failed
	 */
	public void showScreen(String screenName, String fxmlResource, boolean showOnce, boolean saveState,
			StageSettings settings, Object information) throws StatefulException, ScreenException {
		// checking if the saveState flag is true. In this case, calling the saveState()
		// method of the current screen's controller
		if (saveState && !screensStack.isEmpty()) {
			AbstractScreen currentScreenController = screensMap.get(screensStack.peek());
			if (!(currentScreenController instanceof Stateful))
				throw new StatefulException("Saving screen's state is allowed only for screens who implement Stateful");
			else
				((Stateful) screensMap.get(screensStack.peek())).saveState(); // saving the state
		}

		// now loading the requested screen
		// in case it was already loaded before, retrieveing its controller
		AbstractScreen controller;
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new ScreenException(e.getMessage());
		}
		if (screensMap.get(screenName) != null) {
			controller = screensMap.get(screenName);
		} else {
			controller = loader.getController();
		}

		if (!showOnce) { // if the screen is not meant to be shown only once
			screensMap.put(screenName, controller); // adding the screen to the screens map
		}

		if (!showOnce) { // if the screen is not meant to be shown only once
			screensStack.push(screenName); // pushing the screen to the screens stack
		}

		// sending information to the new screen, if needed and existed
		if (information != null)
			controller.loadBefore(information);

		addRootAndStart(root, controller, settings);
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
		if (restoreState && !screensMap.isEmpty()) {
			AbstractScreen currentScreenController = screensMap.get(screensStack.peek());
			if (!(currentScreenController instanceof Stateful))
				throw new StatefulException(
						"Restoring screen's state is allowed only for screens who implement Stateful");
			((Stateful) screensMap.get(screensStack.peek())).restoreState();
		}

		Parent root = null;
		String previousScreen = screensStack.peek();
		screensMap.get(previousScreen).initialize();
		String fxmlResource = "/clientSide/fxml/"
				+ (screensStack.peek()).substring(0, screensStack.peek().length() - "Controller".length()) + ".fxml";
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new ScreenException(e.getMessage());
		}

		addRootAndStart(root, screensMap.get(previousScreen),
				StageSettings.defaultSettings("GoNature System - Client Connection"));
	}

	/**
	 * This method gets a root, a controller and stage settings and shows the root on the screen
	 * @param root the fxml root
	 * @param controller the screen's controller
	 * @param settings the stage settings
	 */
	private void addRootAndStart(Parent root, AbstractScreen controller, StageSettings settings) {
		stage.setScene(new Scene(root)); // setting the scene
		settings.implementSettings(settings, stage); // implementing the settings of the stage
		stage.setOnCloseRequest(controller::handleCloseRequest); // handleCloseRequest method of the
																	// controller will be called
		root.requestFocus(); // setting the focus on the root, not on the GUI components
		((Pane) root).setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		stage.show(); // showing the new scene
		System.out.println("Current Screens Stack: " + screensStack);
		System.out.println("Current Screens Map: " + screensMap);
	}
}