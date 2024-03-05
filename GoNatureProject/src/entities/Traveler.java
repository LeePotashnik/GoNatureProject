package entities;

public class Traveler extends ParkVisitor {
	public Traveler(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, VisitorType visitorType) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, visitorType);
	}
}
