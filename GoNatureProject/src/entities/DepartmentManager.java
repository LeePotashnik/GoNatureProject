package entities;

import java.util.ArrayList;

public class DepartmentManager extends ParkEmployee {
	private ArrayList<Park> responsible;
	private String managesDepartment;

	public DepartmentManager(String idNumber, String firstName, String lastName, String emailAddress, String phoneNumber,
			String managesDepartment, String username, String password, boolean isLoggedIn) {
		super(idNumber, firstName, lastName, emailAddress, phoneNumber, username, password, isLoggedIn);
		this.managesDepartment = managesDepartment;
	}
	
	public String getManagesDepartment() {
		return managesDepartment;
	}

	public void setManagesDepartment(String managesDepartment) {
		this.managesDepartment = managesDepartment;
	}

	public ArrayList<Park> getResponsible() {
		return responsible;
	}

	public void setResponsible(ArrayList<Park> responsible) {
		this.responsible = responsible;
	}
	
	public boolean addParkToList(Park parkToAdd) {
		if (responsible == null)
			throw new NullPointerException();
		return responsible.add(parkToAdd);
	}
	
	public boolean deleteParkFromList(Park parkToDelete) {
		if (responsible == null)
			throw new NullPointerException();
		return responsible.remove(parkToDelete);
	}
}