package entities;

import java.time.LocalTime;

public class ParkManager extends ParkEmployee {
	private Park manages;

	public ParkManager(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, LocalTime lastLogIn, Park workingIn, Park manages) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, lastLogIn, workingIn);
		this.manages = manages;
	}

	public Park getManages() {
		return manages;
	}

	public void setManages(Park manages) {
		this.manages = manages;
	}
}