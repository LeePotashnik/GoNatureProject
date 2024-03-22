package entities;

public class Representative extends SystemUser{
	private String representativeId, firstName, lastName, emailAddress, phoneNumber, username, password;
	private boolean isLoggedIn;

	public Representative(String representativeId, String firstName, String lastName, String emailAddress, String phoneNumber,
			String username, String password, boolean isLoggedIn) {
		super(representativeId,firstName,lastName,emailAddress,phoneNumber,username,password,isLoggedIn);	}	

}
