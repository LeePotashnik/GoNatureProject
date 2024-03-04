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
	private String uniqueId; // will hold a unique id for client-server identification

	// the communication type
	public enum CommunicationType {
		QUERY_REQUEST, CLIENT_SERVER_MESSAGE, RESPONSE;
	}

	private CommunicationType communicationType;

	public Communication(CommunicationType communicationType) { // Constructor
		this.communicationType = communicationType;
		if (communicationType == CommunicationType.QUERY_REQUEST) { // if it's a query request
			messageType = MessageType.NONE;
		} else if (communicationType == CommunicationType.CLIENT_SERVER_MESSAGE) { // if it's a client-server message
			queryType = QueryType.NONE;
		} else { // if it's a response from server-side to client-side
			messageType = MessageType.NONE;
			queryType = QueryType.NONE;
		}
	}

	
	
	// --- TYPE 1 OF COMMUNICATION: SQL QUERY REQUEST ---
	// determines the type of the SQL requested query
	public enum QueryType {
		SELECT, UPDATE, INSERT, DELETE, NONE;
	}

	private QueryType queryType;

	// determines the table/s query is going to work on
	private ArrayList<String> tables;

	// for SELECT query, determines the selected columns (can also be '*')
	private ArrayList<String> selectColumns;

	// determines the "where" part conditions
	private ArrayList<String> whereColumns;
	private ArrayList<Object> whereValues;
	private ArrayList<String> whereOperators;

	// determines the columns and values for "set" and "insert"
	private ArrayList<String> columns;
	private ArrayList<Object> values;

	// --- TYPE 2 OF COMMUNICATION: CLIENT-SERVER MESSAGES
	public enum MessageType {
		CONNECT, DISCONNECT, NONE;
	}

	private MessageType messageType;

	// --- TYPE 3 OF COMMUNICATION: RESPONSE FROM SERVER SIDE
	private ArrayList<Object[]> resultList; // a container for the result set from the database, as ArrayList
	private boolean queryResult; // holds the result of update/insert/delete queries

	///// --- GENERAL METHODS --- /////
	// returns the communication type
	public CommunicationType getCommunicationType() {
		return communicationType;
	}

	// returns the unique id
	public String getUniqueId() {
		return uniqueId;
	}

	// sets the unique id
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	
	
	///// --- METHODS FOR HANDLING THE FIRST TYPE OF COMMUNICAIONS --- /////
	// --- GETTERS --- //
	/**
	 * This method returns the QueryType enum of the communication
	 * 
	 * @return the query type (an enum constant)
	 */
	public QueryType getQueryType() {
		return queryType;
	}

	/**
	 * This method returns if the communication is a query request
	 * 
	 * @return true if the CommunicationType is QUERY_REQUEST
	 */
	public boolean isQuery() {
		return communicationType == CommunicationType.QUERY_REQUEST;
	}

	// --- SETTERS --- //
	/**
	 * Determines the type of the SQL query requested
	 * 
	 * @param queryType the type of the SQL query requested
	 * @throws CommunicationException if trying to set a QueryType where the CommunicationType is not a QUERY_REQUEST
	 */
	public void setQueryType(QueryType queryType) throws CommunicationException {
		if (communicationType != CommunicationType.QUERY_REQUEST) {
			throw new CommunicationException("The communication type is not a query request");
		}
		this.queryType = queryType;
	}

	/**
	 * Sets the tables the SQL query will execute on
	 * 
	 * @param tables the tables the query will execute on
	 */
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

	// --- QUERY COMBINATION METHODS --- //
	// this method redirects the query creation to the specific method according to
	// the query type
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

	// this method creates and returns a SELECT query
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
					query += tables.get(i) + "";
				} else {
					query += tables.get(i) + ",";
				}
			}
		}

		// adding the where part
		query += createWherePart() + ";";

		return query;
	}

	// this method creates and returns an UPDATE query
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

	// this method creates and returns an INSERT query
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

	// this method creates and returns a DELETE query
	private String combineDeleteQuery() throws CommunicationException {
		String query = "DELETE FROM ";
		// adding the table name
		if (tables == null)
			throw new CommunicationException("Table is not included");
		query += tables.get(0) + "";

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
			where += " WHERE ";
			int j = 0;
			for (int i = 0; i < whereColumns.size(); i++) {
				where += whereColumns.get(i) + " " + whereOperators.get(j++) + " ";
				where += prepareValue(whereValues.get(i)) + (j < whereOperators.size() ? " " : "");
				where += j < whereOperators.size() ? whereOperators.get(j++) + " " : "";
			}
		}
		return where;
	}

	///// --- METHODS FOR HANDLING THE SECOND TYPE OF COMMUNICAIONS --- /////
	// --- GETTERS --- //
	// a getter for returning if the communication is a client-server message
	public boolean isMessage() {
		return communicationType == CommunicationType.CLIENT_SERVER_MESSAGE;
	}

	// returns the message type
	public MessageType getMessageType() {
		return messageType;
	}

	// --- SETTERS --- //
	// sets the message type
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}


	
	///// --- METHODS FOR HANDLING THE THIRD TYPE OF COMMUNICATIONS --- /////
	///// --- GETTERS --- /////
	// getter for the result list (to use on the client-side)
	public ArrayList<Object[]> getResultList() {
		return resultList;
	}

	// getter for the query result (true = succeed, false = failed)
	public boolean getQueryResult() {
		return queryResult;
	}

	///// --- SETTERS --- /////
	// setter for the result set (to use on the server-side)
	public void setResultList(ArrayList<Object[]> resultList) {
		this.resultList = resultList;
	}

	// setter for the query result (true = succeed, false = failed)
	public void setQueryResult(boolean queryResult) {
		this.queryResult = queryResult;
	}
}