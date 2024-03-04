package common.controllers;

public class ScreenException extends Exception {
	private static final long serialVersionUID = 1L;

	public ScreenException(String reason) {
		super(reason);
	}
}