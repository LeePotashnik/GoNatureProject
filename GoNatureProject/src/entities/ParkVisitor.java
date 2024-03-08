package entities;

/**
 * 
 */
/**
 * 
 */
public class ParkVisitor extends SystemUser {
	public enum VisitorType {
		TRAVELER, GROUPGUIDE;
	}

	private VisitorType visitorType;

	public ParkVisitor(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, VisitorType visitorType) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn);
		this.visitorType = visitorType;
	}

	/**
	 * @return visitor type (enum)
	 */
	public VisitorType getVisitorType() {
		return visitorType;
	}
	
	/**
	 * @param visitorType
	 * initialize visitor type
	 */
	public void setVisitorType(VisitorType visitorType) {
		this.visitorType = visitorType;
	}

}