package serverSide.jdbc;

public class DatabaseException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatabaseException(String reason) {
		super(reason);
	}
}
