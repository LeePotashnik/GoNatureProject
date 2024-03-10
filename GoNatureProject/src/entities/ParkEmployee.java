package entities;

public class ParkEmployee extends SystemUser {
	private Park workingIn;

	public ParkEmployee(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String username, String password, boolean isLoggedIn) {
		super(idNumber, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
	}

	public Park getWorkingIn() {
		return workingIn;
	}

	public void setWorkingIn(Park workingIn) {
		this.workingIn = workingIn;
	}
}