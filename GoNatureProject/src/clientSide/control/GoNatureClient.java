package clientSide.control;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import common.communication.Communication;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.MessageType;
import common.communication.Communication.QueryType;
import ocsf.client.AbstractClient;

public class GoNatureClient extends AbstractClient {
	public static boolean awaitResponse = false;
	/**
	 * This map holds all the requests that have been sent to the server side and
	 * have not recieved a response. The mapping is by the unique id of each
	 * communication object.
	 */
	private ConcurrentHashMap<String, Communication> awaitingRequests = new ConcurrentHashMap<>();

	public GoNatureClient(String host, int port) { // Constructor
		super(host, port);
	}

	/**
	 * Calls the AbstractClient method for connection to the server. If the
	 * openConnection method threw an exception, it means the port number has no
	 * server listening on in, therefore let the user try again.
	 * 
	 * @param host the host address of the server
	 * @param port the port number of the server
	 * @return true if the connection succeed, false otherwise.
	 */
	public boolean connectClientToServer(String host, int port) {
		setPort(port);
		setHost(host);
		try {
			openConnection(); // trying to connect to server
		} catch (IOException e) { // if connection failed
			return false;
		}
		return true; // if connection succeed
	}

	/**
	 * Gets a Communication response from the server and updates the Communication
	 * request with the result
	 * 
	 * @param responseFromServer a Communication object from the server-side
	 */
	@Override
	protected void handleMessageFromServer(Object responseFromServer) {
		awaitResponse = false;
		Communication response = (Communication) responseFromServer;
		Communication originalRequest = awaitingRequests.remove(response.getUniqueId());
		if (originalRequest.getQueryType() == QueryType.SELECT) {
			originalRequest.setResultList(response.getResultList());
			originalRequest.setQueryResult(true);
		} else {
			originalRequest.setQueryResult(response.getQueryResult());
		}
	}

	/**
	 * Analyses the message and sends it to the server
	 * 
	 * @param request a Communication object from the client-side GUI
	 */
	protected void handleMessageFromClientUI(Object requestFromClientSide) {
		try {
			awaitResponse = true;
			Communication request = (Communication) requestFromClientSide;

			// creating a unique id for the request and adding it to the requests map
			String uniqueId = UUID.randomUUID().toString();
			request.setUniqueId(uniqueId);
			awaitingRequests.put(uniqueId, request);

			// if this is a Disconnection request, just closing the connection
			if (request.getCommunicationType() == CommunicationType.CLIENT_SERVER_MESSAGE) {
				if (request.getMessageType() == MessageType.DISCONNECT) {
					sendToServer(request);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					closeConnection();
					return;
				}
			}

			// any other communication request - sent to server side
			sendToServer(request);
			// waiting for response
			while (awaitResponse) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not send message to server: Terminating client." + e);
		}
	}
}