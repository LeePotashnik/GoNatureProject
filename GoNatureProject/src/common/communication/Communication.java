package common.communication;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class for the communications between client-side to server-side Includes
 * all the elements required to transmit a valid message that is Serializable
 */
public class Communication implements Serializable {
	private static final long serialVersionUID = 1L;
	// determines the type of the SQL requested query
	private boolean isSelect;
	private boolean isUpdate;
	private boolean isInsert;
	private boolean isDelete;
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

	public String combineQuery() throws CommunicationException {
		if (isSelect) {
			return combineSelectQuery();
		} else if (isUpdate) {
			return combineUpdateQuery();
		} else if (isInsert) {
			return combineInsertQuery();
		} else if (isDelete) {
			return combineDeleteQuery();
		} else {
			throw new CommunicationException("No query type chosen");
		}
	}

	private String combineSelectQuery() throws CommunicationException {
		int i, j;
		String query = "SELECT ";
		// adding the column/s to select from
		if (selectColumns == null)
			throw new CommunicationException("Columns are not included");
		if (selectColumns.size() == 1) { // only one column, or *
			query += selectColumns.get(0) + " ";
		} else { // several columns from the table/s
			for (i = 0; i < selectColumns.size(); i++) {
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
			for (i = 0; i < tables.size(); i++) {
				if (i + 1 == tables.size()) { // if this is the last column, no ',' after it
					query += tables.get(i) + " ";
				} else {
					query += tables.get(i) + ",";
				}
			}
		}

		// adding the where part
		if (whereColumns != null && whereOperators != null && whereValues != null) {
			query += "WHERE ";
			j = 0;
			for (i = 0; i < whereColumns.size(); i++) {
				query += whereColumns.get(i) + " " + whereOperators.get(j++) + " ";
				query += prepareValue(whereValues.get(i)) + (j < whereOperators.size() ? " " : "");
				query += j < whereOperators.size() ? whereOperators.get(j++) + " " : "";
			}
		}
		query += ";";
		return query;
	}

	private String combineUpdateQuery() throws CommunicationException {
		int i, j;
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

		for (i = 0; i < columns.size(); i++) {
			query += columns.get(i) + " = ";
			query += values.get(i) + (i + 1 == columns.size() ? "" : ", ");
		}

		// adding the where part
		if (whereColumns != null && whereOperators != null && whereValues != null) {
			query += " WHERE ";
			j = 0;
			for (i = 0; i < whereColumns.size(); i++) {
				query += whereColumns.get(i) + " " + whereOperators.get(j++) + " ";
				if (whereValues.get(i) instanceof Number) {
					query += whereValues.get(i) + (j < whereOperators.size() ? " " : "");
				} else {
					query += "'" + whereValues.get(i) + "'" + (j < whereOperators.size() ? " " : "");
				}
				query += j < whereOperators.size() ? whereOperators.get(j++) + " " : "";
			}
		}
		query += ";";
		return query;
	}

	private String combineInsertQuery() throws CommunicationException {
		int i, j;
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
			throw new CommunicationException("Columns or values are not matching");
		}
		query += "VALUES (";
		for (i = 0; i < values.size(); i++) {
			if (values.get(i) instanceof Number) {
				query += values.get(i) + ((i + 1 == values.size()) ? ")" : ",");
			} else {
				query += "'" + values.get(i) + "'" + ((i + 1 == values.size()) ? ")" : ",");
			}
		}

		query += ";";
		return query;
	}

	private String combineDeleteQuery() {
		return "";
	}

	private String prepareValue(Object value) {
		if (value == null)
			return null;
		else {
			if (value instanceof LocalTime) {
				return "'" + ((LocalTime) value).getHour() + ":" + ((LocalTime) value).getMinute() + ":00'";
			} else if (value instanceof LocalDate) {
				return "'" + ((LocalDate) value).getYear() + "-" + ((LocalDate) value).getMonthValue() + "-"
						+ ((LocalDate) value).getDayOfMonth() + "'";
			} else if (value instanceof Number) {
				return ((Number) value).toString();
			} else {
				return "'" + value.toString() + "'";
			}
		}
	}

	public void setSelect() {
		isSelect = true;
	}

	public void setUpdate() {
		isUpdate = true;
	}

	public void setInsert() {
		isInsert = true;
	}

	public void setDelete() {
		isDelete = true;
	}

	public void setTables(List<String> tables) {
		this.tables = new ArrayList<String>(tables);
	}

	public void setSelectColumns(List<String> selectColumns) {
		this.selectColumns = new ArrayList<String>(selectColumns);
	}

	public void setWhereOperators(List<String> whereOperators) {
		this.whereOperators = new ArrayList<String>(whereOperators);
	}

	public void setWhereColumns(List<String> whereColumns) {
		this.whereColumns = new ArrayList<String>(whereColumns);
	}

	public void setWhereValues(List<Object> whereValues) {
		this.whereValues = new ArrayList<Object>(whereValues);
	}

	public void setColumns(List<String> columns) {
		this.columns = new ArrayList<String>(columns);
	}

	public void setValues(List<Object> values) {
		this.values = new ArrayList<Object>(values);
	}

	public static void main(String[] args) {
		Communication request1 = new Communication();
		request1.setSelect();
		request1.setTables(Arrays.asList("park", "park_manager"));
		request1.setSelectColumns(Arrays.asList("*"));
		request1.setWhereColumns(Arrays.asList("department", "age", "time", "time"));
		request1.setWhereValues(Arrays.asList("Eastern", "25", LocalTime.of(12, 0), LocalTime.of(16, 0)));
		request1.setWhereOperators(Arrays.asList("=", "AND", ">=", "AND", ">=", "AND", "<="));
		String result = null;
		try {
			result = request1.combineQuery();
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);

		Communication request2 = new Communication();
		request2.setUpdate();
		request2.setTables(Arrays.asList("acadia_park_active_booking"));
		request2.setColumns(Arrays.asList("entryParkTime", "exitParkTime"));
		request2.setValues(Arrays.asList(LocalTime.of(12, 30), LocalTime.of(15, 30)));
		request2.setWhereColumns(Arrays.asList("department", "age", "time", "time"));
		request2.setWhereValues(Arrays.asList("Eastern", "25", LocalTime.of(12, 0), LocalTime.of(16, 0)));
		request2.setWhereOperators(Arrays.asList("=", "AND", ">=", "AND", ">=", "AND", "<="));
		try {
			result = request2.combineQuery();
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);

		Communication request3 = new Communication();
		request3.setInsert();
		request3.setTables(Arrays.asList("acadia_park_active_booking"));
		request3.setColumns(Arrays.asList("entryParkTime", "exitParkTime"));
		request3.setValues(Arrays.asList(LocalTime.of(12, 30), LocalTime.of(15, 30, 00)));
		try {
			result = request3.combineQuery();
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
	}
}
