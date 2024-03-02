package entities;

import java.time.LocalTime;
import java.util.ArrayList;

public class DepartmentManager extends ParkEmployee {
	private ArrayList<Park> responsible;

	public DepartmentManager(String idNumber, String firstName, String lastName, String username, String password,
			String emailAddress, String phoneNumber, boolean isLoggedIn, Park workingIn, ArrayList<Park> responsible) {
		super(idNumber, firstName, lastName, username, password, emailAddress, phoneNumber, isLoggedIn, workingIn);
		this.responsible = responsible;
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