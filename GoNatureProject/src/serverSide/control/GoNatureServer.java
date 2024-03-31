package serverSide.control;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import common.communication.Communication;
import common.communication.Communication.ClientMessageType;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import common.communication.Communication.SecondaryRequest;
import common.communication.Communication.ServerMessageType;
import common.communication.CommunicationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverSide.control.StaffController.ImportStatus;
import serverSide.jdbc.DatabaseController;
import serverSide.jdbc.DatabaseException;

public class GoNatureServer extends AbstractServer {
	private DatabaseController database;
	private BackgroundManager backgroundManager;
	private ArrayList<ConnectionToClient> clientsConnected = new ArrayList<>();
	public static final ObservableList<ConnectedClient> connectedToGUI = FXCollections.observableArrayList();
	private NotificationsController notifications = NotificationsController.getInstance();
	private StaffController staffController;
	private static final int parkAmount = 18; // 17 parks and 1 more for other uses
	private ArrayList<Semaphore> parksSemaphores = new ArrayList<>(); // for park capacities critical section control

	/**
	 * The constructor creates a new server on the given port, and also creates an
	 * instance of the DatabaseController.
	 * 
	 * @param port the port number the server will work on
	 */
	public GoNatureServer(int port) {
		super(port);
		initializeSemaphores();
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
	public ImportStatus importUsersFromExternalSystem() {
		if (staffController == null) {
			staffController = new StaffController(database);
		}
		// initiating the users import from the external system into the database
		return staffController.importUsers();
	}

	/**
	 * This method initializes the semaphores array list for the critical sections
	 * control
	 */
	private void initializeSemaphores() {
		for (int i = 0; i < parkAmount; i++) {
			parksSemaphores.add(new Semaphore(1, true));
		}
	}

	@Override
	/**
	 * This method gets a message from client-side and handles it
	 * 
	 * @param msg    the Communication object
	 * @param client the ConnectionToClient who sent this request
	 */
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		// announcing the request from the client side has arrived
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication recieved from client " + client.toString());

		Communication request = (Communication) msg;
		Communication response; // will be sent over to the client side

		// first checking if the request requires a critical section for this specific
		// park.
		// if so: acquires the semaphore, or waiting for it to be released if already
		// acquired
		int isRequestCritical = request.isCritical(); // returns -1 if not requires a critical section
		if (isRequestCritical != -1) { // acquiring the critical section
			try {
				parksSemaphores.get(isRequestCritical).acquire();
				System.out.println("Semaphore is aquired for park #" + isRequestCritical);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// getting the communication type
		CommunicationType type = request.getCommunicationType();

		switch (type) {
		/////////////////////
		/// QUERY REQUEST ///
		/////////////////////
		case QUERY_REQUEST: // if this is a query request
		{
			// creating a communication response to be sent later to the client side
			response = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
			response.setServerMessageType(ServerMessageType.RESPONSE);
			// making the request and the response communication with the same unique id
			// this is done for later identification in the client side
			response.setUniqueId(request.getUniqueId());

			// checking which type of query is requested
			switch (request.getQueryType()) {
			case SELECT:
				ArrayList<Object[]> resultList = database.executeSelectQuery(request);
				if (resultList != null)
					response.setResultList(resultList);
				response.setQueryResult(true);
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

			// some requests may contain a secondary request, which is a second request from
			// the server side to be executed at the same time
			SecondaryRequest secondaryRequest = request.getSecondaryRequest();
			if (secondaryRequest != null) {
				switch (secondaryRequest) {
				case UPDATE_WAITING_LIST: {
					// if the original request is an active booking cancellation
					// there's a need to check the park's waiting list and possibly release some
					// bookings and transfer them to the active booking table
					backgroundManager.checkWaitingListReleasePossibility(request.getParkId(), request.getDayOfVisit(),
							request.getTimeOfVisit());
					if (request.getQueryType() == QueryType.NONE)
						response.setQueryResult(true);
					break;
				}
				case LOCK_BOOKING: {
					if (response.getResultList() == null || response.getResultList().isEmpty()) { // inserting to the
																									// locked table
						try {
							request.setQueryType(QueryType.INSERT); // changing from select to insert
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
						request.setColumnsAndValues(Arrays.asList("bookingId"), Arrays.asList(request.getBookingId()));
						boolean insertResult = database.executeInsertQuery(request);
						response.setQueryResult(insertResult);

					} else {
						// not empty
						response.setQueryResult(false);
					}
					break;
				}
				case UPDATE_CAPACITY: {
					int visitorsBooking = request.getNumberOfVisitors();
					int currentCapacity = (Integer) (response.getResultList().get(0)[0]);
					try {
						request.setQueryType(QueryType.UPDATE);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
					request.setColumnsAndValues(Arrays.asList("currentCapacity"),
							Arrays.asList(currentCapacity + visitorsBooking));
					boolean updateQueryResult = database.executeUpdateQuery(request);
					response.setQueryResult(updateQueryResult);
					break;
				}
				case INSERT_BOOKING_AFTER_CHECKING_CAPACITIES: {
					int maximumCapacity = request.getParkCapacities();
					int bookingVisitors = request.getNumberOfVisitors();
					try {
						request.setQueryType(QueryType.INSERT);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}

					// if the returned value is null/empty, it means the selection returned no rows
					// thus, the booking can be inserted to the active bookings table of the park
					// as long as it does not exceed from the park limits
					ArrayList<Object[]> result = response.getResultList();
					if (result == null || result.isEmpty()) {
						if (maximumCapacity >= bookingVisitors) { // inserting
							boolean insertQueryResult = database.executeInsertQuery(request);
							response.setQueryResult(insertQueryResult);
						} else {
							response.setQueryResult(false); // insertion is not possible
						}
					} else { // otherwise, checking the current orders capacity with the maximum
						// calculating the current number of visitors
						int sumOfVisitors = 0;
						for (Object[] row : result) {
							sumOfVisitors += (Integer) row[0];
						}

						if (maximumCapacity - sumOfVisitors - bookingVisitors >= 0) { // inserting
							boolean insertQueryResult = database.executeInsertQuery(request);
							response.setQueryResult(insertQueryResult);
						} else { // insertion is not possible
							response.setQueryResult(false);
						}
					}
					break;
				}
				case CHECK_USER_LOCKED: {
					ArrayList<Object[]> selectResult = response.getResultList();
					if (selectResult == null || selectResult.isEmpty()) { // user does not exist
						response.setResultList(null);
						response.setQueryResult(false);

					} else { // if this user exists
						boolean isLocked = (Integer) selectResult.get(0)[6] == 0 ? false : true;
						if (isLocked) { // if locked
							response.setQueryResult(false);
						} else { // if not locked
							// changing the communication to update query
							try {
								request.setQueryType(QueryType.UPDATE);
							} catch (CommunicationException e) {
								e.printStackTrace();
							}
							request.setColumnsAndValues(Arrays.asList("isLocked"), Arrays.asList("1"));
							boolean updateResult = database.executeUpdateQuery(request);
							if (!updateResult) { // if failed
								response.setQueryResult(false);
							} else { // if succeed
								response.setQueryResult(true);
							}
						}
					}
					// returning the communication to select query
					// for client-side data retrieval
					try {
						request.setQueryType(QueryType.SELECT);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
				}
			}
			// sending the response to the client side
			try {
				client.sendToClient(response);
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		}

		/////////////////////////////
		/// CLIENT SERVER MESSAGE ///
		/////////////////////////////
		case CLIENT_SERVER_MESSAGE: {
			// disconnecting the client from the server
			if (request.getClientMessageType() == ClientMessageType.DISCONNECT) {
				clientDisconnected(client);
			}
			break;
		}

		///////////////////
		/// TRANSACTION ///
		///////////////////
		case TRANSACTION: // if this is a combined query (transaction) request
		{
			response = new Communication(CommunicationType.SERVER_CLIENT_MESSAGE);
			response.setServerMessageType(ServerMessageType.RESPONSE);
			response.setUniqueId(request.getUniqueId());
			boolean transactionResult = database.executeTransaction(request);
			response.setQueryResult(transactionResult);

			// sending the response to the client side
			try {
				client.sendToClient(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}

		////////////////////
		/// NOTIFICATION ///
		////////////////////
		case NOTIFICATION: // if this is a notification sending request
		{
			switch (request.getNotificationType()) {
			case SEND_CONFIRMATION: // sending confirmation
				notifications.sendConfirmationEmailNotification(Arrays.asList(request.getEmailAddress(),
						request.getPhoneNumber(), request.getParkName(), request.getDayOfVisit(),
						request.getTimeOfVisit(), request.getFirstName(), request.getParkLocation(),
						request.getNumberOfVisitors(), request.getFinalPrice(), request.isPaid()));
				break;
			case SEND_CANCELLATION: // sending cancellation
				notifications
						.sendCancellationEmailNotification(
								Arrays.asList(request.getEmailAddress(), request.getPhoneNumber(),
										request.getParkName(), request.getDayOfVisit(), request.getTimeOfVisit(),
										request.getFirstName(), request.getParkLocation(),
										request.getNumberOfVisitors(), request.getFinalPrice(), request.isPaid()),
								"Visitor chose to cancel.");
				break;
			case SEND_CONFIRMATION_WITHOUT_REMINDER: // sending confirmation without the need of reminder
				notifications.sendConfirmationWithoudReminderEmailNotification(
						Arrays.asList(request.getEmailAddress(), request.getPhoneNumber(), request.getParkName(),
								request.getDayOfVisit(), request.getTimeOfVisit(),
								request.getFirstName() + " " + request.getLastName(), request.getParkLocation(),
								request.getNumberOfVisitors(), request.getFinalPrice(), request.isPaid()));
				break;
			case SEND_WAITING_LIST_ENTRANCE: // sending a waiting list entrance approval
				notifications.sendWaitingListEnteranceEmailNotification(Arrays.asList(request.getEmailAddress(),
						request.getPhoneNumber(), request.getParkName(), request.getDayOfVisit(),
						request.getTimeOfVisit(), request.getFirstName() + " " + request.getLastName(),
						request.getParkLocation(), request.getNumberOfVisitors(), request.getFinalPrice()));
				break;
			case NONE:
				return;
			}
			break;
		}

		default: // server-client or self communciations are not handled here
			return;
		}

		if (isRequestCritical != -1) { // releaseing the critical section of the park
			parksSemaphores.get(isRequestCritical).release();
			System.out.println("Semaphore is released for park #" + isRequestCritical);
		}
	}
}