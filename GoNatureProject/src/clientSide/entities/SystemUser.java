package clientSide.entities;

/**
 * 
 */
public class SystemUser {
	private String idNumber, firstName, lastName, emailAddress, phoneNumber, username, password;
	private boolean isLoggedIn;

	public SystemUser(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String username, String password, boolean isLoggedIn) {
		this.idNumber = idNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.username = username;
		this.password = password;
		this.isLoggedIn = isLoggedIn;
	}

	/**
	 * @return system user ID number.
	 */
	public String getIdNumber() {
		return idNumber;
	}

	/**
	 * @return system user first name.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return system user last name.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return system user User name.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return system user password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return system user Email Address.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @return system user phone number.
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @return if the system user already logged in.
	 */

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * @param idNumber set system user ID number.
	 */
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	/**
	 * @param firstName set system user first name.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @param lastName set last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @param username set system user user name.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password set system user password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param emailAddress set system user email address.
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @param phoneNumber set system user phone number.
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @param isLoggedIn set system user as logged in.
	 */
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