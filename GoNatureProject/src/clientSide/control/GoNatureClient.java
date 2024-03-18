package clientSide.control;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;

import common.communication.Communication;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.ServerMessageType;
import common.controllers.ScreenManager;
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
	 * request with the result, or conquers the focus using ScreenManager
	 * 
	 * @param responseFromServer a Communication object from the server-side
	 */
	@Override
	protected void handleMessageFromServer(Object responseFromServer) {
		awaitResponse = false;
		Communication serverMessage = (Communication) responseFromServer;

		if (serverMessage.getServerMessageType() == ServerMessageType.RESPONSE) {

			// finding the original request
			Communication originalRequest = awaitingRequests.remove(serverMessage.getUniqueId());

			// if the original request was a single query request
			if (originalRequest.getCommunicationType() == CommunicationType.QUERY_REQUEST) {
				if (originalRequest.getQueryType() == QueryType.SELECT) {
					originalRequest.setResultList(serverMessage.getResultList());
					originalRequest.setQueryResult(true);
				} else {
					originalRequest.setQueryResult(serverMessage.getQueryResult());
				}
			}

			// if the original request was a transaction request
			if (originalRequest.getCommunicationType() == CommunicationType.TRANSACTION) {
				originalRequest.setQueryResult(serverMessage.getQueryResult());
			}
			return;
		}

		// if this is a conquer message type, then the server side requires conquering
		// the focus of the current stage
		if (serverMessage.getServerMessageType() == ServerMessageType.CONQUER) {
			if (ScreenManager.getInstance().isActive()) {
				// ScreenManager.getInstance().conquerFocus(serverMessage.getServerMessageContent());
			}
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
			awaitingRequests.put(request.getUniqueId(), request);

			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Sending Communication Request no. " + request.getUniqueId());

			// if this is a Disconnection request, just closing the connection
			if (request.getCommunicationType() == CommunicationType.CLIENT_SERVER_MESSAGE) {
				if (request.getClientMessageType() == ClientMessageType.DISCONNECT) {
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