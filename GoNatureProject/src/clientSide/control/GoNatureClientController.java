package clientSide.control;

import common.communication.Communication;

/**
 * Controller class for the client logic layer. Its main use is being a static
 * instance in the GoNatureClientUI class.
 */
public class GoNatureClientController {
	private GoNatureClient client;

	public GoNatureClientController(String host, int port) { // Constructor
		client = new GoNatureClient(host, port);
	}

	/**
	 * Calls the GoNatureClient method for connection to the server
	 * 
	 * @param host the host address of the server
	 * @param port the port number of the server
	 * @return true if the connection succeed, false otherwise.
	 */
	public boolean connectClientToServer(String host, int port) {
		return client.connectClientToServer(host, port);
	}

	/**
	 * Gets a request from the client side and transfers it to the GoNatureClient
	 * handleMessageFromClientUI method.
	 * 
	 * @param request the Communication object from the client side
	 */
	public void accept(Communication request) {
		client.handleMessageFromClientUI(request);
	}
}