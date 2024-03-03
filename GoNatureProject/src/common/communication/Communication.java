package common.communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for the communications between client-side to server-side Includes
 * all the elements required to transmit a valid message that is Serializable
 */
public class Communication implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sentFrom; // holds the screen/controller class name

	public enum CommunicationType {
		QUERY_REQUEST, CLIENT_SERVER_MESSAGE;
	}

	private CommunicationType communicationType;

	public Communication(CommunicationType communicationType) {
		this.communicationType = communicationType;
		if (communicationType == CommunicationType.QUERY_REQUEST) {
			messageType = MessageType.NONE;
		} else {
			queryType = QueryType.NONE;
		}
	}

	// --- TYPE 1 OF COMMUNICATION: SQL QUERY REQUEST ---
	// determines the type of the SQL requested query
	public enum QueryType {
		SELECT, UPDATE, INSERT, DELETE, NONE;
	}

	private QueryType queryType;

	// determines the table/s and column/s the query is going to work on
	private ArrayList<String> tables;
	private ArrayList<String> selectColumns;
	// determines the "where" part
	private ArrayList<String> whereColumns;
	private ArrayList<Object> whereValues;
	private ArrayList<String> whereOperators;
	// determines the columns and values for "set" and "insert"
	private ArrayList<String> columns;
	private ArrayList<Object> values;
	// a container for the result set from the database, as ArrayList
	private ArrayList<Object[]> resultList;
	private boolean queryResult;

	// --- TYPE 2 OF COMMUNICATION: CLIENT-SERVER MESSAGES
	public enum MessageType {
		CONNECT, DISCONNECT, NONE;
	}

	private MessageType messageType;

	// ------------------------------------------------------------------ //

	// returns the sentFrom property
	public String getSentFrom() {
		return sentFrom;
	}

	// sets the sentFrom property
	public void setSentFrom(String sentFrom) {
		this.sentFrom = sentFrom;
	}

	// returns the communication type
	public CommunicationType getCommunicationType() {
		return communicationType;
	}

	////////// --- METHODS FOR HANDLING THE FIRST TYPE OF COMMUNICAIONS ---

	///// --- GETTERS ---
	// getter for the result list (to use on the client-side)
	public ArrayList<Object[]> getResultList() {
		return resultList;
	}

	// getter for the query result (true = succeed, false = failed)
	public boolean getQueryResult() {
		return queryResult;
	}

	// this method returns the query type
	public QueryType getQueryType() {
		return queryType;
	}

	// a getter for returning if the communication is a query request
	public boolean isQuery() {
		return communicationType == CommunicationType.QUERY_REQUEST;
	}

	///// --- SETTERS ---
	// setter for the result set (to use on the server-side)
	public void setResultList(ArrayList<Object[]> resultList) {
		this.resultList = resultList;
	}

	// setter for the query result (true = succeed, false = failed)
	public void setQueryResult(boolean queryResult) {
		this.queryResult = queryResult;
	}

	// this setter determines the type of the SQL query requested
	public void setQueryType(QueryType queryType) throws CommunicationException {
		if (communicationType != CommunicationType.QUERY_REQUEST) {
			throw new CommunicationException("The communication type is not a query request");
		}
		this.queryType = queryType;
	}

	// the tables the query will execute on
	public void setTables(List<String> tables) {
		this.tables = new ArrayList<String>(tables);
	}

	// if the query is SELECT, the columns in the returened view
	public void setSelectColumns(List<String> selectColumns) {
		this.selectColumns = new ArrayList<String>(selectColumns);
	}

	// setter for the WHERE part of the query
	public void setWhereConditions(List<String> whereColumns, List<String> whereOperators, List<Object> whereValues) {
		this.whereColumns = new ArrayList<String>(whereColumns);
		this.whereOperators = new ArrayList<String>(whereOperators);
		this.whereValues = new ArrayList<Object>(whereValues);
	}

	// setter for the columns and values (of the INSERT and SET parts)
	public void setColumnsAndValues(List<String> columns, List<Object> values) {
		this.columns = new ArrayList<String>(columns);
		this.values = new ArrayList<Object>(values);
	}

	///// --- QUERY COMBINATION METHODS ---
	// this method redirects the query creation to the specific method
	// according to the query type
	public String combineQuery() throws CommunicationException {
		switch (queryType) {
		case SELECT:
			return combineSelectQuery();
		case UPDATE:
			return combineUpdateQuery();
		case INSERT:
			return combineInsertQuery();
		case DELETE:
			return combineDeleteQuery();
		default: // NONE
			throw new CommunicationException("No query type chosen");
		}
	}

	// this method creates and returns a SELECT method
	private String combineSelectQuery() throws CommunicationException {
		String query = "SELECT ";
		// adding the column/s to select from
		if (selectColumns == null)
			throw new CommunicationException("Columns are not included");
		if (selectColumns.size() == 1) { // only one column, or *
			query += selectColumns.get(0) + " ";
		} else { // several columns from the table/s
			for (int i = 0; i < selectColumns.size(); i++) {
				if (i + 1 == selectColumns.size()) { // if this is the last column, no ',' after it
					query += selectColumns.get(i) + " ";
				} else {
					query += selectColumns.get(i) + ",";
				}
			}
		}

		// adding the tables to select from
		query += "FROM ";
		if (tables == null)
			throw new CommunicationException("Tables are not included");
		if (tables.size() == 1) { // only one column, or *
			query += tables.get(0) + " ";
		} else { // several columns from the table/s
			for (int i = 0; i < tables.size(); i++) {
				if (i + 1 == tables.size()) { // if this is the last column, no ',' after it
					query += tables.get(i) + " ";
				} else {
					query += tables.get(i) + ",";
				}
			}
		}

		// adding the where part
		query += createWherePart() + ";";

		return query;
	}

	// this method creates and returns an UPDATE method
	private String combineUpdateQuery() throws CommunicationException {
		String query = "UPDATE ";
		// adding the table name
		if (tables == null)
			throw new CommunicationException("Tables are not included");
		query += tables.get(0) + " ";

		// adding the column/s to set values to
		query += "SET ";
		if (columns == null) {
			throw new CommunicationException("Columns are not included");
		}
		if (values == null) {
			throw new CommunicationException("Values are not included");
		}
		if (values.size() != columns.size()) {
			throw new CommunicationException("Columns or values are missing");
		}

		for (int i = 0; i < columns.size(); i++) {
			query += columns.get(i) + " = ";
			query += prepareValue(values.get(i)) + (i + 1 == columns.size() ? "" : ", ");
		}

		// adding the where part
		query += createWherePart() + ";";
		return query;
	}

	// this method creates and returns an INSERT method
	private String combineInsertQuery() throws CommunicationException {
		int i;
		String query = "INSERT INTO ";
		// adding the table name
		if (tables == null)
			throw new CommunicationException("Table is not included");
		query += tables.get(0) + " (";

		// adding the columns
		if (columns == null) {
			throw new CommunicationException("Columns are not included");
		}
		for (i = 0; i < columns.size(); i++) {
			query += columns.get(i) + ((i + 1 == columns.size()) ? ") " : ",");
		}

		// adding the values
		if (values == null) {
			throw new CommunicationException("Values are not included");
		}
		if (values.size() != columns.size()) {
			throw new CommunicationException("Columns and values are not matching");
		}
		query += "VALUES (";
		for (i = 0; i < values.size(); i++) {
			query += prepareValue(values.get(i)) + ((i + 1 == values.size()) ? ")" : ",");
		}

		query += ";";
		return query;
	}

	// this method creates and returns a DELETE method
	private String combineDeleteQuery() throws CommunicationException {
		String query = "DELETE FROM ";
		// adding the table name
		if (tables == null)
			throw new CommunicationException("Table is not included");
		query += tables.get(0) + " ";

		// adding the where part
		query += createWherePart() + ";";
		return query;
	}

	// this method gets a value object and prepares it to fit the SQL syntax
	private String prepareValue(Object value) {
		if (value == null)
			return null;
		else {
			String ret = "";
			if (value instanceof LocalTime) {
				ret += "'" + (((LocalTime) value).getHour() < 10 ? "0" : "");
				ret += ((LocalTime) value).getHour() + ":";
				ret += (((LocalTime) value).getMinute() < 10 ? "0" : "");
				ret += ((LocalTime) value).getMinute() + ":00'";
				return ret;
			} else if (value instanceof LocalDate) {
				ret += "'" + ((LocalDate) value).getYear() + "-";
				ret += (((LocalDate) value).getMonthValue() < 10 ? "0" : "");
				ret += ((LocalDate) value).getMonthValue() + "-";
				ret += (((LocalDate) value).getDayOfMonth() < 10 ? "0" : "");
				ret += ((LocalDate) value).getDayOfMonth() + "'";
				return ret;
			} else if (value instanceof Number) {
				return ((Number) value).toString();
			} else {
				return "'" + value.toString() + "'";
			}
		}
	}

	// this method creates, if relevant, the where part of the query, as string
	private String createWherePart() throws CommunicationException {
		String where = "";
		if (whereColumns != null && whereOperators != null && whereValues != null) {
			if (whereValues.size() != whereColumns.size()) {
				throw new CommunicationException("Columns and values are not matching");
			}
			where += "WHERE ";
			int j = 0;
			for (int i = 0; i < whereColumns.size(); i++) {
				where += whereColumns.get(i) + " " + whereOperators.get(j++) + " ";
				where += prepareValue(whereValues.get(i)) + (j < whereOperators.size() ? " " : "");
				where += j < whereOperators.size() ? whereOperators.get(j++) + " " : "";
			}
		}
		return where;
	}

	////////// --- METHODS FOR HANDLING THE SECOND TYPE OF COMMUNICAIONS ---

	// a getter for returning if the communication is a client-server message
	public boolean isMessage() {
		return communicationType == CommunicationType.CLIENT_SERVER_MESSAGE;
	}

	// sets the message type
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	// returns the message type
	public MessageType getMessageType() {
		return messageType;
	}

//	public static void main(String[] args) throws CommunicationException {
//		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
//		request.setQueryType(QueryType.SELECT);
//		request.setTables(Arrays.asList("olympic_park_active_booking"));
//		request.setSelectColumns(Arrays.asList("*"));
////		request.setWhereConditions(Arrays.asList("numberOfVisitors", "dayOfBooking"), Arrays.asList("<", "AND", ">="),
////				Arrays.asList(10, LocalDate.of(2024, 02, 01)));
//		GoNatureServer server = new GoNatureServer(4444);
//		server.sendSQLRequest(request);
//		ArrayList<Order> olympic_park_active_bookings = new ArrayList<>();
//		for (Object[] o : request.getResultList()) {
//			Order newOrder = new Order((String) o[0], (Date) o[1], (Time) o[2], (Date) o[3], ((String)o[4]).equals("group") ? VisitType.GROUP : VisitType.INDIVIDUAL,
//					(Integer) o[5], (String) o[6], (String) o[7], (String) o[8], (String) o[9], (Integer) o[10],
//					(Integer) o[11] != 0, (Integer) o[12] != 0, (Time) o[13], (Time) o[14], (Integer) o[15] != 0, (Time) o[16]);
//			olympic_park_active_bookings.add(newOrder);
//			System.out.println(newOrder);
//		}
//		public Order(int bookingIdNumber, int numberOfVisitors, Park parkBooked, OrderStatus status, LocalDate dayOfVisit, LocalTime timeOfVisit, LocalTime entryParkTime, LocalTime exitParkTime, VisitType visitType,	String phoneNumber, String emailAddress, float finalPrice) {
//	}
}
