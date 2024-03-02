package common.communication;

import java.io.Serializable;
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
	private ArrayList<String> whereColumns;
	private ArrayList<String> selectColumns;
	// determines the "where" logic operators
	private ArrayList<String> whereOperators;

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

	private String combineSelectQuery() {
		int i, j;
		String query = "SELECT ";
		// adding the column/s to select from
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
		if (whereColumns != null && whereOperators != null) {
			query += "WHERE ";
			j = 0;
			for (i = 0; i < whereColumns.size(); i += 2) {
				query += whereColumns.get(i) + " " + whereOperators.get(j++) + " ";
				query += whereColumns.get(i + 1) + (j < whereOperators.size() ? " " : "");
				query += j < whereOperators.size() ? whereOperators.get(j++) + " " : "";
			}
		}
		query += ";";
		return query;
	}

	private String combineUpdateQuery() {
		return "";
	}

	private String combineInsertQuery() {
		return "";
	}

	private String combineDeleteQuery() {
		return "";
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

	public void setWhereColumns(List<String> whereColumns) {
		this.whereColumns = new ArrayList<String>(whereColumns);
	}

	public void setSelectColumns(List<String> selectColumns) {
		this.selectColumns = new ArrayList<String>(selectColumns);
	}

	public void setWhereOperators(List<String> whereOperators) {
		this.whereOperators = new ArrayList<String>(whereOperators);
	}

	public static void main(String[] args) {
		Communication request = new Communication();
		request.setSelect();
		request.setTables(Arrays.asList("park", "park_manager"));
		request.setSelectColumns(Arrays.asList("*"));
		request.setWhereColumns(Arrays.asList("department", "'Eastern'", "age", "'25'"));
		request.setWhereOperators(Arrays.asList("=", "AND", ">="));
		String result = request.combineSelectQuery();
		System.out.println(result);
		
	}
}
