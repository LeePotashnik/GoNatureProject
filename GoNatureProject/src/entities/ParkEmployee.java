package entities;

public class ParkEmployee extends SystemUser {
	private Park workingIn;

	public ParkEmployee(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, Park workingIn) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn);
		this.workingIn = workingIn;
	}

	public Park getWorkingIn() {
		return workingIn;
	}

	public void setWorkingIn(Park workingIn) {
		this.workingIn = workingIn;
	}
}