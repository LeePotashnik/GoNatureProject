package entities;

/**
 * 
 */
/**
 * 
 */
public class ParkVisitor extends SystemUser {
	public enum VisitorType {
		TRAVELLER, GROUPGUIDE;
	}
	
	private VisitorType visitorType;

	public ParkVisitor(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String username, String password, boolean isLoggedIn, VisitorType visitorType) {
		super(idNumber, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
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