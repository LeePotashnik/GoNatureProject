package common.controllers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScreenManager {
	private static ScreenManager instance;
	private boolean isActive = false;
	private final Stage stage;
	private Stage conquerprStage;
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
	 * @param information  an object that contains information for the screen,
	 *                     before its load
	 * @throws StatefulException if the saveState flag is true while the current
	 *                           screen does not implement Stateful
	 * @throws ScreenException   if the loading of the FXML file failed
	 */
	public void showScreen(String screenName, String fxmlResource, boolean showOnce, boolean saveState,
			Object information) throws StatefulException, ScreenException {
		if (!isActive) {
			isActive = true;
		}
		System.out.println("screensStack: " + screensStack);
		System.out.println("screensMap: " + screensMap);
		// checking if the saveState flag is true. In this case, calling the saveState()
		// method of the current screen's controller
		if (saveState && !screensStack.isEmpty()) {
			AbstractScreen currentScreenController = screensMap.get(screensStack.peek());
			if (!(currentScreenController instanceof Stateful)) {
				throw new StatefulException("Saving screen's state is allowed only for screens who implement Stateful");
			} else {
				Stateful stateScreen = (Stateful) screensMap.get(screensStack.peek());
				Platform.runLater(() -> {
					stateScreen.saveState(); // saving the state
				});
			}
		}

		// now loading the requested screen
		AbstractScreen controller;
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			root = loader.load();
			controller = loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ScreenException(e.getMessage());
		}

		if (!showOnce) { // if the screen is not meant to be shown only once
			// adding the screen to the screens map and stack
			screensMap.put(screenName, controller);
			screensStack.push(screenName);
		}

		// sending information to the new screen, if needed and existed
		if (information != null)
			controller.loadBefore(information);

		addRootAndStart(root, controller);
	}

	/**
	 * This method is called when a "Back" button is clicked from some screen, or if
	 * a screen has finished its action and wants the previous screen to be shown.
	 * The screen who called this method is on the top of the stack, so it is
	 * removed and then the new top is displayed on the stage.
	 * 
	 * @param wasShownOnce indicates if the calling screen was shown only once.
	 *                     Therefore, it was not in the screens stack so there is
	 *                     not need to pop anything
	 * @param restoreState indicates whether the state of the previous screen needs
	 *                     to be restored, or the previous screen should be loaded
	 *                     as new screen
	 * @throws ScreenException   if trying to access a previous screen that does not
	 *                           exist
	 * @throws StatefulException if the restoreState flag is true while the screen
	 *                           does not implement Stateful
	 */
	public void goToPreviousScreen(boolean wasShownOnce, boolean restoreState)
			throws ScreenException, StatefulException {
		System.out.println("screensStack: " + screensStack);
		System.out.println("screensMap: " + screensMap);
		// checking if there's a screen to go back to
		if (screensStack.size() <= 1)
			throw new ScreenException("No screens to go back to");

		if (screensMap.containsKey(screensStack.peek())) // removing the current screen from the map
			screensMap.remove(screensStack.peek());
		if (!wasShownOnce)
			screensStack.pop(); // removing the current screen from the stack

		Parent root = null;
		AbstractScreen controller = null;
		String fxmlResource = "/clientSide/fxml/"
				+ (screensStack.peek()).substring(0, screensStack.peek().length() - "Controller".length()) + ".fxml";
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			root = loader.load();
			controller = loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ScreenException(e.getMessage());
		}

		// updating the controller if necessary
		screensMap.put(screensStack.peek(), controller);

		// checking if the saveState flag is true. In this case, calling the saveState()
		// method of the current screen's controller
		if (restoreState) {
			if (!(controller instanceof Stateful)) {
				throw new StatefulException(
						"Restoring screen's state is allowed only for screens who implement Stateful");
			} else {
				Stateful stateScreen = (Stateful) controller;
				Platform.runLater(() -> {
					stateScreen.restoreState(); // restoring the state
				});
			}
		}

		addRootAndStart(root, controller);
	}

	/**
	 * This method gets a root, a controller and shows the root on the screen
	 * 
	 * @param root       the fxml root
	 * @param controller the screen's controller
	 * @throws StatefulException if the restoreState flag is true while the screen
	 *                           does not implement Stateful
	 */
	private void addRootAndStart(Parent root, AbstractScreen controller) throws StatefulException {
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/clientSide/fxml/Styles.css").toExternalForm()); // setting
																											// styles
		stage.setScene(scene); // setting the scene
		stage.setTitle("GoNature - " + controller.getScreenTitle());
		stage.setResizable(false);
		stage.getIcons().add(new Image("/GoNatureSquareLogo.png"));
		stage.setOnCloseRequest(controller::handleCloseRequest); // handleCloseRequest method of the
																	// controller will be called
		root.requestFocus(); // setting the focus on the root, not on the GUI components
		((Pane) root).setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		stage.show(); // showing the new scene
	}

	/**
	 * This method is called if the stack needs to be resetted. This can happen if
	 * an operation is fully completed, and returning to the main/account screen is
	 * required, without any other screen to go back to.
	 */
	public void resetScreensStack() {
		screensStack.removeAll(screensStack);
	}

	public void conquerFocus(String message) {
		// run later on the JavaFX thread
		Platform.runLater(() -> {
			Label textlabel = new Label();
			textlabel.setText(message);
			textlabel.setStyle("-fx-font-size: 18px; -fx-font-family: 'Calibri';");
			textlabel.setWrapText(true);

			StackPane layout = new StackPane(textlabel);
			layout.setStyle(
					"-fx-padding: 20; -fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");
			Scene scene = new Scene(layout, 400, 200);

			conquerprStage = new Stage();
			conquerprStage.initModality(Modality.APPLICATION_MODAL);
			conquerprStage.initStyle(StageStyle.UNDECORATED);
			conquerprStage.setScene(scene);
			conquerprStage.show();
		});
	}

	public void unconquer() {
		Platform.runLater(() -> {
			if (conquerprStage != null) {
				conquerprStage.close();
			}
		});
	}

	/**
	 * @return if there's a stage active right now
	 */
	public boolean isActive() {
		return isActive;
	}
}
