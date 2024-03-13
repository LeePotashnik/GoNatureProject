package clientSide.gui;

import java.util.TimeZone;

import clientSide.control.GoNatureClientController;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import javafx.application.Application;
import javafx.stage.Stage;

public class GoNatureClientUI extends Application {
	public static GoNatureClientController client;

	public static void main(String args[]) throws Exception {
		System.out.println("Launching client side");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jerusalem"));
		launch(args);
	}

	/**
	 * Runs the client-side connection screen
	 */
	@Override
	public void start(Stage arg0) throws Exception {
		ScreenManager.getInstance().showScreen("ClientConnectionController",
				"/clientSide/fxml/ClientConnection.fxml", false, false,
				StageSettings.defaultSettings("GoNature System - Client Connection"), "localhost 5555");
	}

	/**
	 * The method creates a client instance, only once
	 * 
	 * @param host the host address of the server
	 * @param port the port number of the server *
	 */
	public static void createClient(String host, int port) {
		if (client == null)
			client = new GoNatureClientController(host, port);
	}
}
