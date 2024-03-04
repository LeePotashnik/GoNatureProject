package clientSide.control;

import common.communication.Communication;

// CONTROLLER CLASS FOR THE CLIENT LOGIC LAYER
// BASICALLY ITS MAIN USE IS BEING A STATIC INSTANCE AT THE CLIENT UI CLASS
public class GoNatureClientController {
	private GoNatureClient client;

	public GoNatureClientController(String host, int port) { // Constructor
		client = new GoNatureClient(host, port);
	}

	/**
	 * @param host
	 * @param port
	 * @return - true if the connection succeed, false otherwise
	 */
	// calls the GoNatureClient method for connection to the server
	public boolean connectClientToServer(String host, int port) {
		return client.connectClientToServer(host, port);
	}

	/**
	 * @param request - the Communication object from the client side
	 */
	public void accept(Communication request) {
		client.handleMessageFromClientUI(request);
	}
}