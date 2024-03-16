package serverSide.control;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;

import common.communication.Communication;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.ServerMessageType;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

public class GoNatureServer extends AbstractServer {
	private DatabaseController database;
	private BackgroundManager backgroundManager;
	private ArrayList<ConnectionToClient> clientsConnected = new ArrayList<>();

	/**
	 * The constructor creates a new server on the given port, and also creates an
	 * instance of the DatabaseController.
	 * 
	 * @param port the port number the server will work on
	 */
	public GoNatureServer(int port) {
		super(port);
	}

	/**
	 * Creates a new instance of the database controller
	 * 
	 * @param database the local MySQL database path
	 * @param root     the root name
	 * @param password the database password
	 * @throws DatabaseException if there is a problem with the connection
	 */
	public void connectToDatabase(String databae, String root, String password) throws DatabaseException {
		database = new DatabaseController(databae, root, password); // creates a new instance of the db connector
	}
	
	/**
	 * This method creates a new instance of the background tasks manager
	 */
	public void initiateBackgroundManager() {
		if (database == null)
			throw new NullPointerException();
		backgroundManager = new BackgroundManager(database);
		try {
			backgroundManager.updateActiveTables();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * This method adds the sent client to the clientsConnected list.
	 */
	protected void clientConnected(ConnectionToClient client) {
		if (isClientConnected(client)) {
			System.out.println(client + " is already connected to server, can't establish a new connection");
		} else {
			clientsConnected.add(client);
			System.out.println(client + " is connected to server");
		}
	}

	@Override
	/**
	 * This method removes the sent client from the clientsConnected list.
	 */
	protected void clientDisconnected(ConnectionToClient client) {
		if (!isClientConnected(client)) {
			System.out.println(client + " is not connected to server, can't establish disconnection");
		} else {
			clientsConnected.remove(client);
			System.out.println(client + " is disconnected");
		}
	}

	/**
	 * @param client the ConnectionToClient that is checked
	 * @return true if the client is currently connected, false if not.
	 */
	private boolean isClientConnected(ConnectionToClient client) {
		for (ConnectionToClient c : clientsConnected)
			if (client.equals(c))
				return true;
		return false;
	}

	/**
	 * @return true if all clients are disconnected, false otherwise.
	 */
	public boolean areAllClientsDisconnected() {
		return clientsConnected.size() == 0;
	}

	@Override
	/**
	 * This method gets a message from client-side and handles it
	 * 
	 * @param msg    the Communication object
	 * @param client the ConnectionToClient who sent this request
	 */
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication recieved from client " + client.toString());
		Communication request = (Communication) msg;

		// if this is a regular, single query request
		if (request.getCommunicationType() == CommunicationType.QUERY_REQUEST) {
			Communication response = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
			response.setServerMessageType(ServerMessageType.RESPONSE);
			// making the request and the response communication with the same unique id
			response.setUniqueId(request.getUniqueId());

			// checking which type of query is request
			switch (request.getQueryType()) {
			case SELECT:
				ArrayList<Object[]> resultList = database.executeSelectQuery(request);
				if (resultList != null)
					response.setResultList(resultList);
				break;
			case UPDATE:
				boolean updateQueryResult = database.executeUpdateQuery(request);
				response.setQueryResult(updateQueryResult);
				break;
			case INSERT:
				boolean insertQueryResult = database.executeInsertQuery(request);
				response.setQueryResult(insertQueryResult);
				break;
			case DELETE:
				boolean deleteQueryResult = database.executeDeleteQuery(request);
				response.setQueryResult(deleteQueryResult);
				break;
			default:
				return;
			}
			try {
				client.sendToClient(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// if this is a combined query (transaction) request
		if (request.getCommunicationType() == CommunicationType.TRANSACTION) {
			Communication response = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
			response.setUniqueId(request.getUniqueId());
			boolean transactionResult = database.executeTransaction(request);
			response.setQueryResult(transactionResult);
			try {
				client.sendToClient(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (request.getCommunicationType() == CommunicationType.CLIENT_SERVER_MESSAGE) {
			if (request.getClientMessageType() == ClientMessageType.DISCONNECT) {
				clientDisconnected(client);
			}
		}

	}
}