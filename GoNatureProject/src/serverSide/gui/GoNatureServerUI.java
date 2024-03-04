package serverSide.gui;

import java.io.IOException;

import common.controllers.ScreenChanger;
import javafx.application.Application;
import javafx.stage.Stage;
import serverSide.control.GoNatureServer;

public class GoNatureServerUI extends Application {
	protected static GoNatureServer server;
	protected static ServerConnectionController serverConnectionController;

	/**
	 * @param portString - the port number input from the server connection GUI
	 * @return - a message about the connection status 
	 * This method runs the server with the entered port number (from the text field on the GUI)
	 */
	public static String runServer(String portString) {
		int chosenPort = 0;
		try {
			chosenPort = Integer.parseInt(portString); // get port from command line
		} catch (Throwable t) {
			return "Error: Could not connect to server.";
		}
		server = new GoNatureServer(chosenPort);

		try {
			server.listen(); // start listening for client connections
		} catch (Exception ex) {
			return "Error: Could not listen for clients!";
		}
		return "Server is connected to port " + chosenPort;
	}
	
	public static void disconnectServer() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server is disconnected");
	}
	
	public boolean areAllClientsDisconnected() {
		return server.areAllClientsDisconnected();
	}

	/**
	 * Runs the server's fxml through the prototype server controller
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		serverConnectionController = new ServerConnectionController();
		ScreenChanger.primaryStage = primaryStage;
		serverConnectionController.start(primaryStage);
	}
	
	public static void main(String[] args) {
		System.out.println("Launching server side");
		launch(args); // invokes the start method
	}
}
