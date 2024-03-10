package entities;

import java.util.ArrayList;

public class DepartmentManager extends ParkEmployee {
	private ArrayList<Park> responsible;
	private String department_managercol;

	public DepartmentManager(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, Park workingIn, ArrayList<Park> responsible,
			String department_managercol) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, workingIn);
		this.responsible = responsible;
		this.department_managercol = department_managercol;
	}

	public String getDepartment_managercol() {
		return department_managercol;
	}

	public void setDepartment_managercol(String department_managercol) {
		this.department_managercol = department_managercol;
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