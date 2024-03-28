package clientSide.entities;

public class Representative extends SystemUser {
	public Representative(String representativeId, String firstName, String lastName, String emailAddress,
			String phoneNumber, String username, String password, boolean isLoggedIn) {
		super(representativeId, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
	}

}
