package common.communication;

public class CommunicationException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommunicationException(String reason) {
		super(reason);
	}
}
