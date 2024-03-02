package entities;

import java.time.LocalTime;

public class SystemUser {
	private String idNumber, firstName, lastName, username, password, emailAddress, phoneNumber;
	private boolean isLoggedIn;
	
	public SystemUser(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn) {
		this.idNumber = idNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.isLoggedIn = isLoggedIn;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	

	@Override
	public String toString() {
		return "SystemUser [idNumber=" + idNumber + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", username=" + username + ", password=" + password + ", emailAddress=" + emailAddress
				+ ", phoneNumber=" + phoneNumber + ", isLoggedIn=" + isLoggedIn + "]";
	}
}