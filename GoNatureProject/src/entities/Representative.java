package entities;

public class Representative {
	private String representativeId, firstName, lastName, emailAddress, phoneNumber, username, password;
	private boolean isLoggedIn;

	public Representative(String representativeId, String firstName, String lastName, String emailAddress, String phoneNumber,
			String username, String password, boolean isLoggedIn) {
		this.representativeId = representativeId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.username = username;
		this.password = password;
		this.isLoggedIn = isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;		
	}
	
	public boolean getLoggedIn() {
		return this.isLoggedIn;		
	}

	public String getIdNumber() {
		return representativeId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
