package common.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import clientSide.control.GoNatureUsersController;
import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * The AbstractScreen is the abstract class of each screen class in the
 * application. Includes multiple methods that are common for all screens,
 * including Alert showing, CSS and styles methods, etc. A screen can also be
 * Stateful (implementing the Stateful interface), if in some cases it has to
 * save its current data and state, for future restoring and use.
 */
public abstract class AbstractScreen {
	//////////////////
	/// PROPERTIES ///
	//////////////////
	protected final ArrayList<String> imagePaths = new ArrayList<>(); // List to hold all the image paths
	private final Stage primaryStage = ScreenManager.getInstance().getStage();

	public AbstractScreen() {
		setParksPaths();
	}

	////////////////////////
	/// ABSTRACT METHODS ///
	////////////////////////

	/**
	 * This method is called after the FXML is invoked
	 */
	public abstract void initialize();

	/**
	 * This method is called if a screen has to get information before it is shown.
	 * 
	 * @param information an object with the specific information for the screen
	 */
	public abstract void loadBefore(Object information);

	public abstract String getScreenTitle();

	/**
	 * This method is activated after the X is clicked on the window. The default is
	 * to show a Confirmation Alert with "Yes" and "No" options for the user to
	 * choose. "Yes" will check if the client is connected to the server, disconnect
	 * it if necessary and close the window. "No" will "consume" the request,
	 * meaning it will cancel the closing request and will keep the window open.
	 * This method can be overriden for different implementations.
	 * 
	 * @param event the event of clicking on the X of the window
	 */
	public void handleCloseRequest(WindowEvent event) {
		// if the client is not connected or the user did not log in yet
		if (GoNatureClientUI.client == null || GoNatureUsersController.getInstance().restoreUser() == null) {
			int decision = showConfirmationAlert("Are you sure you want to leave?", Arrays.asList("Yes", "No"));
			if (decision == 2) // if the user clicked on "No"
				event.consume();
			else { // if the user clicked on "Yes"
				System.out.println("Client exited the application"); // just exit
			}
		} else {
			int decision = showConfirmationAlert("Are you sure you want to log out from the system?",
					Arrays.asList("Yes", "No"));
			if (decision == 2) // if the user clicked on "No"
				event.consume();
			else { // if the user clicked on "Yes"
				GoNatureUsersController.getInstance().logoutUser();
				// creating a communication request for disconnecting from the server port
				Communication message = new Communication(CommunicationType.CLIENT_SERVER_MESSAGE);
				message.setClientMessageType(ClientMessageType.DISCONNECT);
				GoNatureClientUI.client.accept(message);
			}
		}
	}

	/**
	 * This method uses Alert to show an error alert message on the screen
	 * 
	 * @param stage   the application's primary stage
	 * @param content the content to be displayed inside the error alert
	 */
	public final void showErrorAlert(String content) {
		// setting the alert's properties
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(primaryStage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle(
				"-fx-background-color: #ffdddd; -fx-font-size: 14px; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");
		alert.initStyle(StageStyle.UNDECORATED);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setWidth(400);
		alert.setHeight(200);

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

		// setting animations
		final double[] startYPosition = new double[] { -200 }; // starts above visible area
		final double endYPosition = alertStage.getWidth() / 2; // ends within visible area
		final double exitYPosition = -200; // exits going up off-screen

		// starts the alert off-screen
		alertStage.setY(startYPosition[0]);

		// AnimationTimer for entrance animation
		AnimationTimer enterTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (startYPosition[0] < endYPosition) {
					startYPosition[0] += 25; // speed of entrance animation
					alertStage.setY(startYPosition[0]);
					double current = primaryStage.getScene().getRoot().getOpacity();
					primaryStage.getScene().getRoot().setOpacity(current - 0.05);
				} else {
					this.stop();
				}
			}
		};

		// shows the alert and starts the entrance animation
		enterTimer.start();
		alert.show();

		// AnimationTimer for exit animation
		AnimationTimer exitTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (startYPosition[0] > exitYPosition) {
					startYPosition[0] -= 25; // speed of exit animation
					alertStage.setY(startYPosition[0]);
					double current = primaryStage.getScene().getRoot().getOpacity();
					if (current < 1) {
						primaryStage.getScene().getRoot().setOpacity(current + 0.05);
					}

				} else {
					this.stop();
					alert.close();
				}
			}
		};

		// finds the ButtonBar in the DialogPane
		ButtonBar buttonBar = null;
		for (Node node : dialogPane.getChildren()) {
			if (node instanceof ButtonBar) {
				buttonBar = (ButtonBar) node;
				break;
			}
		}

		if (buttonBar != null) {
			Button button = (Button) buttonBar.getButtons().get(0);
			button.setOnAction(event -> {
				alert.show();
				event.consume();
				startYPosition[0] = endYPosition;
				enterTimer.stop();
				exitTimer.start();
			});
		}
	}

	/**
	 * This method uses Alert to show an information alert message on the screen
	 * 
	 * @param content the content to be displayed inside the information alert
	 */
	public final void showInformationAlert(String content) {
		// setting the alert's properties
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(primaryStage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle(
				"-fx-background-color: #ddfeff; -fx-font-size: 14px; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");
		alert.initStyle(StageStyle.UNDECORATED);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setWidth(400);
		alert.setHeight(200);

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

		// setting animations
		final double[] startYPosition = new double[] { -200 }; // starts above visible area
		final double endYPosition = alertStage.getWidth() / 2; // ends within visible area
		final double exitYPosition = -200; // exits going up off-screen

		// starts the alert off-screen
		alertStage.setY(startYPosition[0]);

		// AnimationTimer for entrance animation
		AnimationTimer enterTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (startYPosition[0] < endYPosition) {
					startYPosition[0] += 25; // speed of entrance animation
					alertStage.setY(startYPosition[0]);
					double current = primaryStage.getScene().getRoot().getOpacity();
					primaryStage.getScene().getRoot().setOpacity(current - 0.05);
				} else {
					this.stop();
				}
			}
		};

		// shows the alert and starts the entrance animation
		enterTimer.start();
		alert.show();

		// AnimationTimer for exit animation
		AnimationTimer exitTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (startYPosition[0] > exitYPosition) {
					startYPosition[0] -= 25; // speed of exit animation
					alertStage.setY(startYPosition[0]);
					double current = primaryStage.getScene().getRoot().getOpacity();
					if (current < 1) {
						primaryStage.getScene().getRoot().setOpacity(current + 0.05);
					}

				} else {
					this.stop();
					alert.close();
				}
			}
		};

		// finds the ButtonBar in the DialogPane
		ButtonBar buttonBar = null;
		for (Node node : dialogPane.getChildren()) {
			if (node instanceof ButtonBar) {
				buttonBar = (ButtonBar) node;
				break;
			}
		}

		if (buttonBar != null) {
			Button button = (Button) buttonBar.getButtons().get(0);
			button.setOnAction(event -> {
				alert.show();
				event.consume();
				startYPosition[0] = endYPosition;
				enterTimer.stop();
				exitTimer.start();
			});
		}
	}

	/**
	 * @param content     the content to be displayed inside the confirmation alert
	 * @param buttonsText an array list of the text to be shown in every button. The
	 *                    size of this array list of strings, will determine how
	 *                    many buttons will be displayed.
	 * @return the index of the button which was clicked
	 */
	public final int showConfirmationAlert(String content, List<Object> buttonsText) {
		// setting the alert's properties
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(primaryStage);
		alert.setHeaderText(null);
		alert.setContentText(content);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle(
				"-fx-background-color: #ddfeff; -fx-font-size: 14px; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");
		alert.initStyle(StageStyle.UNDECORATED);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setWidth(400);
		alert.setHeight(200);

		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

		// setting animations
		final double[] startYPosition = new double[] { -200 }; // starts above visible area
		final double endYPosition = alertStage.getWidth() / 2; // ends within visible area

		// starts the alert off-screen
		alertStage.setY(startYPosition[0]);

		// AnimationTimer for entrance animation
		AnimationTimer enterTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (startYPosition[0] < endYPosition) {
					startYPosition[0] += 25; // speed of entrance animation
					alertStage.setY(startYPosition[0]);
				} else {
					this.stop();
				}
			}
		};

		// setting the buttons of the alert
		ArrayList<ButtonType> buttons = new ArrayList<>();
		for (Object text : buttonsText) {
			buttons.add(new ButtonType((String) text));
		}
		alert.getButtonTypes().setAll(buttons);
		enterTimer.start();

		Optional<ButtonType> result = alert.showAndWait();

		// checking which button is pressed
		for (ButtonType button : buttons) {
			if (result.isPresent() && result.get() == button)
				return buttons.indexOf(button) + 1; // button #i clicked
		}
		return -1; // in case of an error
	}

	/**
	 * Setting a CSS style for indicating an error in a text field input
	 * 
	 * @return the string to be set inside setStyle() method of JavaFX
	 */
	public final String setFieldToError() {
		return "-fx-border-color: red; -fx-border-width: 0.8px; -fx-border-radius: 2px; -fx-background-color: #ffe6e6;";
	}

	/**
	 * Setting a CSS style for restoring the original state of the text field
	 * 
	 * @return the string to be set inside setStyle() method of JavaFX
	 */
	public final String setFieldToRegular() {
		return "";
	}

	/**
	 * This method is used in order to add all parks images paths to the array list
	 */
	private void setParksPaths() {
		if (imagePaths == null || imagePaths.isEmpty()) {
			imagePaths.add("/acadia.jpg");
			imagePaths.add("/big_bend.jpg");
			imagePaths.add("/congaree.jpg");
			imagePaths.add("/everglades.jpg");
			imagePaths.add("/gateway_arch.jpg");
			imagePaths.add("/glacier.jpg");
			imagePaths.add("/grand_canyon.jpg");
			imagePaths.add("/great_smoky_mountains.jpg");
			imagePaths.add("/hawaii_volcanoes.jpg");
			imagePaths.add("/hot_springs.jpg");
			imagePaths.add("/mammoth_cave.jpg");
			imagePaths.add("/olympic.jpg");
			imagePaths.add("/shenandoah.jpg");
			imagePaths.add("/theodore_roosevelt.jpg");
			imagePaths.add("/voyageurs.jpg");
			imagePaths.add("/yellowstone.jpg");
			imagePaths.add("/yosemite.jpg");
		}
	}
}
