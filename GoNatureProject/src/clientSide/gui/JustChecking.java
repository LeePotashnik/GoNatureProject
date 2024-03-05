package clientSide.gui;

public class JustChecking {
	private String bookingId;
	private String firstName;
	private int finalPrice;
	
	public JustChecking(String bookingId, String firstName, int finalPrice) {
		this.bookingId = bookingId;
		this.firstName = firstName;
		this.finalPrice = finalPrice;
	}

	@Override
	public String toString() {
		return "JustChecking [bookingId=" + bookingId + ", firstName=" + firstName + ", finalPrice=" + finalPrice + "]";
	}
}
