package serverSide.control;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import common.communication.Communication;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.SecondaryRequest;
import common.communication.Communication.ServerMessageType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

public class GoNatureServer extends AbstractServer {
	private DatabaseController database;
	private BackgroundManager backgroundManager;
	private ArrayList<ConnectionToClient> clientsConnected = new ArrayList<>();
	public static final ObservableList<ConnectedClient> connectedToGUI = FXCollections.observableArrayList();
	private NotificationsController notifications = NotificationsController.getInstance();
	private StaffController staffController;
	private Semaphore semaphore = new Semaphore(1, true); // for park capacities critical section updates

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

		// starting the background operations
		backgroundManager.startBackgroundOperations();
	}

	//////////////////////////////////////
	/// INNER CLASS - CONNECTED CLIENT ///
	//////////////////////////////////////

	/**
	 * A class for holding an available slot
	 */
	public static class ConnectedClient {
		private String ip, status;
		private LocalTime enterTime, exitTime;

		/**
		 * Constructor for the connected client
		 */
		public ConnectedClient(String ip, String status, LocalTime enterTime, LocalTime exitTime) {
			this.ip = ip;
			this.status = status;
			this.enterTime = enterTime;
			this.exitTime = exitTime;
		}

		/**
		 * @return the ip address
		 */
		public String getIp() {
			return ip;
		}

		/**
		 * @return the connection status
		 */
		public String getStatus() {
			return status;
		}

		/**
		 * @return the entrance time
		 */
		public LocalTime getEnterTime() {
			return enterTime;
		}

		/**
		 * @return the exit time
		 */
		public LocalTime getExitTime() {
			return exitTime;
		}

		/**
		 * @param ip
		 */
		public void setIp(String ip) {
			this.ip = ip;
		}

		/**
		 * @param status
		 */
		public void setStatus(String status) {
			this.status = status;
		}

		/**
		 * @param enterTime
		 */
		public void setEnterTime(LocalTime enterTime) {
			this.enterTime = enterTime;
		}

		/**
		 * @param exitTime
		 */
		public void setExitTime(LocalTime exitTime) {
			this.exitTime = exitTime;
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
			connectedToGUI.add(new ConnectedClient(client.getInetAddress().getHostAddress().toString(), "Connected",
					LocalTime.now(), null));
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
			for (int i = 0; i < connectedToGUI.size(); i++) {
				if (connectedToGUI.get(i).getIp().equals(client.getInetAddress().getHostAddress().toString())
						&& connectedToGUI.get(i).getExitTime() == null) {
					ConnectedClient update = new ConnectedClient(connectedToGUI.get(i).getIp(), "Disconnected",
							connectedToGUI.get(i).getEnterTime(), LocalTime.now());
					connectedToGUI.set(i, update);
				}
			}
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

	/**
	 * This method is called in order to import users data from the Users Management
	 * System into GoNature database
	 */
	public boolean importUsersFromExternalSystem() {
		if (staffController == null) {
			staffController = new StaffController(database);
		}
		// initiating the users import from the external system into the database
		return staffController.importUsers();
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
		
		if (request.isCritical()) { // aquiring the critical section
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

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
			default: // NONE
				break;
			}

			SecondaryRequest secondaryRequest = request.getSecondaryRequest();
			if (secondaryRequest != null) {
				// if the original request is an active booking cancellation
				// there's a need to check the park's waiting list and possibly release some
				// bookings and transfer them to the active booking table
				switch (secondaryRequest) {
				case UPDATE_WAITING_LIST:
					backgroundManager.checkWaitingListReleasePossibility(request.getParkId(), request.getDate(),
							request.getTime());
					break;
				case SEND_CONFIRMATION:
					notifications.sendConfirmationEmailNotification(Arrays.asList(request.getEmail(),
							request.getPhone(), request.getParkName(), request.getDate(), request.getTime(),
							request.getFullName(), request.getParkLocation(), request.getVisitors(), request.getPrice(),
							request.isPaid()));
					break;
				case SEND_CANCELLATION:
					notifications.sendCancellationEmailNotification(Arrays.asList(request.getEmail(),
							request.getPhone(), request.getParkName(), request.getDate(), request.getTime(),
							request.getFullName(), request.getParkLocation(), request.getVisitors(), request.getPrice(),
							request.isPaid()), "Visitor chose to cancel.");
					break;
				case SEND_REMINDER:
					notifications.sendReminderEmailNotification(Arrays.asList(request.getEmail(), request.getPhone(),
							request.getParkName(), request.getDate(), request.getTime(), request.getFullName(),
							request.getParkLocation(), request.getVisitors(), request.getPrice(), request.isPaid()));
				}
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
			response.setServerMessageType(ServerMessageType.RESPONSE);
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

		if (request.isCritical()) { // releaseing the critical section
			semaphore.release();
		}
	}
}