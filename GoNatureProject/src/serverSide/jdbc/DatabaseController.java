package serverSide.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.TimeZone;

import common.communication.Communication;
import common.communication.CommunicationException;

public class DatabaseController {
	private Connection conn;

	/**
	 * The constructor establishes a connection to the local MySQL databse
	 * 
	 * @param database the local MySQL database path
	 * @param root     the root name
	 * @param password the database password
	 * @throws DatabaseException if there is a problem with the connection
	 */
	public DatabaseController(String database, String root, String password) throws DatabaseException {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jerusalem"));
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) { // an error with driver definition
			System.out.println("Driver definition failed");
		}

		try {
			conn = DriverManager.getConnection(database, root, password);
		} catch (SQLException ex) {
			System.out.println("Database connection failed to be established");
			throw new DatabaseException("Can't establish connection to database");
		}
		System.out.println("Database connection established successfully");
	}

	/**
	 * This method gets an array list of communication requets. Executes these
	 * requests as an atomic transaction.
	 * 
	 * @param requests
	 * @return true if all the requests executions succeeded, false otherwise
	 */
	public boolean executeTransaction(Communication transaction) {
		boolean success = false;
		int i = 0;
		try {
			conn.setAutoCommit(false); // disabling auto-commit to manage transactions manually
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Communication Request no. " + transaction.getUniqueId()
							+ ": Initiating transaction execution");
			for (i = 0; i < transaction.getRequestsList().size(); i++) {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(transaction.getRequestsList().get(i).combineQuery());
				System.out.println("        "
						+ LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(),
								LocalTime.now().getSecond())
						+ ": " + transaction.getRequestsList().get(i).getQueryType() + " query execution succeed");
			}

			conn.commit(); // committing the transaction if all queries succeed
			success = true;
		} catch (SQLException | CommunicationException e) {
			try {
				System.out.println(LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(),
						LocalTime.now().getSecond()) + ": Communication Request no. " + transaction.getUniqueId()
						+ ": Transaction execution failed");
				System.out.println("        Caused by: " + transaction.getRequestsList().get(i).getQueryType());
				conn.rollback(); // roll back the transaction on error
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				conn.setAutoCommit(true); // re-enabling auto-commit
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * Gets a communication request (of a SELECT query), executes the query and
	 * returns an ArrayList<Object[]> representing the ResultSet
	 * 
	 * @param request Communication request
	 * @return ArrayList<Object[]> representing the ResultSet
	 */
	public ArrayList<Object[]> executeSelectQuery(Communication request) {
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery(request.combineQuery());
		} catch (SQLException | CommunicationException e) {
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Communication Request no. " + request.getUniqueId()
							+ ": SELECT query execution failed");
			e.printStackTrace();
			return null;
		}
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication Request no. " + request.getUniqueId() + ": SELECT query execution succeed");
		return resultSetToList(rs);
	}

	/**
	 * Gets a communication request (of an UPDATE query), executes the query and
	 * returns a boolean value representing the result of the update
	 * 
	 * @param request Communication request
	 * @return true if the query succeed, false if failed
	 */
	public boolean executeUpdateQuery(Communication request) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(request.combineQuery());
		} catch (SQLException | CommunicationException e) {
			e.printStackTrace();
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Communication Request no. " + request.getUniqueId()
							+ ": UPDATE query execution failed");
			return false;
		}
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication Request no. " + request.getUniqueId() + ": UPDATE query execution succeed");
		return true;
	}

	/**
	 * Gets a communication request (of an INSERT query), executes the query and
	 * returns a boolean value representing the result of the insertion
	 * 
	 * @param request Communication request
	 * @return true if the query succeed, false if failed
	 */
	public boolean executeInsertQuery(Communication request) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(request.combineQuery());
		} catch (SQLException | CommunicationException e) {
			e.printStackTrace();
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Communication Request no. " + request.getUniqueId()
							+ ": INSERT query execution failed");
			return false;
		}
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication Request no. " + request.getUniqueId() + ": INSERT query execution succeed");
		return true;
	}

	/**
	 * Gets a communication request (of a DELETE query), executes the query and
	 * returns a boolean value representing the result of the deletion
	 * 
	 * @param request Communication request
	 * @return true if the query succeed, false if failed
	 */
	public boolean executeDeleteQuery(Communication request) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(request.combineQuery());
		} catch (SQLException | CommunicationException e) {
			e.printStackTrace();
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Communication Request no. " + request.getUniqueId()
							+ ": DELETE query execution failed");
			return false;
		}
		System.out.println(
				LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
						+ ": Communication Request no. " + request.getUniqueId() + ": DELETE query execution succeed");
		return true;
	}

	/**
	 * This method gets a ResultSet object, runs over its columns and rows and
	 * returns a Serializable ArrayList<Object[]> representing the ResultSet
	 * 
	 * @param rs the ResultSet sent from the executeSelectQuery method
	 * @return an ArrayList of Object[] of the result set elements
	 */
	private ArrayList<Object[]> resultSetToList(ResultSet rs) {

		ArrayList<Object[]> resultList = new ArrayList<>();
		try {
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) { // while there are more rows to add to the list
				Object[] row = new Object[columns];
				for (int i = 1; i <= columns; i++)
					row[i - 1] = rs.getObject(i);
				resultList.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultList;
	}
}