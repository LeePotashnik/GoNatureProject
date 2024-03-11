package entities;

public class ParkManager extends ParkEmployee {
	private String managesPark;
	private Park parkObject;

	public ParkManager(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String managesPark, String username, String password, boolean isLoggedIn) {
		super(idNumber, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
		this.managesPark = managesPark;
	}
	
	/**
	 * 
	 * @return parkObject
	 */
	public Park getParkObject() {
		return parkObject;
	}

	/**
	 * 
	 * @param parkObject
	 */
	public void setParkObject(Park parkObject) {
		this.parkObject = parkObject;
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