package clientSide.gui;

import clientSide.control.GoNatureClientController;
import common.controllers.ScreenChanger;
import javafx.application.Application;
import javafx.stage.Stage;

public class GoNatureClientUI extends Application {
	public static GoNatureClientController client;
	
	public static void main(String args[]) throws Exception {
		System.out.println("Launching client side");
		launch(args);
	}

	/**
	 * Runs the client-side connection screen
	 */
	@Override
	public void start(Stage arg0) throws Exception {
		ClientConnectionController clientConnectionController = new ClientConnectionController();
		ScreenChanger.primaryStage = new Stage();
		clientConnectionController.start(ScreenChanger.primaryStage);
	}
	
	/**
	 * @param host - host address of the server
	 * @param port - port number of the server
	 * 
	 * The method creates a client instance, only once
	 */
	public static void createClient(String host, int port) {
		if (client == null)
			client = new GoNatureClientController(host, port);
	}
}
