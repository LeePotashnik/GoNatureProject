package common.controllers;

public class StatefulException extends Exception {
	private static final long serialVersionUID = 1L;

	public StatefulException(String reason) {
		super(reason);
	}
}