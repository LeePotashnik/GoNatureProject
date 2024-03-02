package entities;

import java.time.LocalTime;

public class ParkVisitor extends SystemUser {
	public enum VisitorType {
		TRAVELER, GROUPGUIDE;
	}

	private VisitorType visitorType;

	public ParkVisitor(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, VisitorType visitorType) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn);
		this.visitorType = visitorType;
	}

	public VisitorType getVisitorType() {
		return visitorType;
	}

	public void setVisitorType(VisitorType visitorType) {
		this.visitorType = visitorType;
	}
}