package entities;

public class ParkManager extends ParkEmployee {
	private String managesPark;

<<<<<<< HEAD
	public ParkManager(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, Park workingIn, Park manages) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, workingIn);
		this.manages = manages;
=======
	public ParkManager(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String managesPark, String username, String password, boolean isLoggedIn) {
		super(idNumber, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
		this.managesPark = managesPark;
>>>>>>> refs/remotes/origin/master
	}

	/**
	 * @return manages
	 */
	public String getManages() {
		return managesPark;
	}

	/**
	 * @param manages
	 * set the park that the manager managing
	 */
	public void setManages(String managesPark) {
		this.managesPark = managesPark;
	}
}