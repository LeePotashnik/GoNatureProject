package entities;

import java.time.LocalTime;

public class GroupGuide extends ParkVisitor {
	private boolean isVerifiedGuide;

	public GroupGuide(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, VisitorType visitorType,
			boolean isVerifiedGuide) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, visitorType);
		this.isVerifiedGuide = isVerifiedGuide;
	}

	public boolean isVerifiedGuide() {
		return isVerifiedGuide;
	}

	public void setVerifiedGuide(boolean isVerifiedGuide) {
		this.isVerifiedGuide = isVerifiedGuide;
	}
}