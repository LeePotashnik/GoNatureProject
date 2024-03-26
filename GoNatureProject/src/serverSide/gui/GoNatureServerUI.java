package serverSide.gui;

import java.io.IOException;

import common.controllers.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;
import serverSide.control.GoNatureServer;
import serverSide.jdbc.DatabaseException;

public class GoNatureServerUI extends Application {
	public static GoNatureServer server;
	public static ServerConnectionController connection;

	/**
	 * This method runs the server with the entered port number (from the text field
	 * on the GUI)
	 * 
	 * @param portString the port number input from the server connection GUI
	 * @return a message about the connection status
	 */
	public static String runServer(int portNumber, String database, String root, String password) {
		server = new GoNatureServer(portNumber);

		try {
			server.listen(); // start listening for client connections
		} catch (Exception ex) {
			return "Error: Could not listen for clients!";
		}

		try {
			server.connectToDatabase(database, root, password);
		} catch (DatabaseException e) {
			return e.getMessage();
		}

		try {
			server.initiateBackgroundManager();
		} catch (NullPointerException e) {
			return e.getMessage();
		}

		return "Server is connected to port " + portNumber;
	}

	/**
	 * This method is called in order to disconnect the server from the port it is
	 * connected to.
	 */
	public static void disconnectServer() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server is disconnected");
		System.exit(0);
	}

	/**
	 * Returns the result of areAllClientsDisconnected method of the GoNatureServer
	 */
	public boolean areAllClientsDisconnected() {
		return server.areAllClientsDisconnected();
	}

	@Override
	/**
	 * Runs the server-side connection screen
	 */
	public void start(Stage primaryStage) throws Exception {
		ScreenManager.getInstance().showScreen("ServerConnectionController", "/serverSide/fxml/ServerConnection.fxml",
				true, false, null);
	}

	public static void main(String[] args) {
		System.out.println("Launching server side");
		launch(args); // invokes the start method
	}
}
