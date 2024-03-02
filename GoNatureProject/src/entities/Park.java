package entities;

import java.util.ArrayList;

public class Park {
	private int parkID;
	private String parkName, parkAddress;
	private ParkManager parkManager;
	private ArrayList<ParkEmployee> employees;
	private DepartmentManager departmentManager;
	private int maximumVisitorsCapacity, maximumOrderAmount, currentCapacity;
	private float maximumTimeLimit;

	public Park(int parkID, String parkName, String parkAddress, ParkManager parkManager,
			ArrayList<ParkEmployee> employees, DepartmentManager departmentManager, int maximumVisitorsCapacity,
			int maximumOrderAmount, int currentCapacity, float maximumTimeLimit) {
		this.parkID = parkID;
		this.parkName = parkName;
		this.parkAddress = parkAddress;
		this.parkManager = parkManager;
		this.employees = employees;
		this.departmentManager = departmentManager;
		this.maximumVisitorsCapacity = maximumVisitorsCapacity;
		this.maximumOrderAmount = maximumOrderAmount;
		this.currentCapacity = currentCapacity;
		this.maximumTimeLimit = maximumTimeLimit;
	}

	public int getParkID() {
		return parkID;
	}

	public String getParkName() {
		return parkName;
	}

	public String getParkAddress() {
		return parkAddress;
	}

	public ParkManager getParkManager() {
		return parkManager;
	}

	public ArrayList<ParkEmployee> getEmployees() {
		return employees;
	}

	public DepartmentManager getDepartmentManager() {
		return departmentManager;
	}

	public int getMaximumVisitorsCapacity() {
		return maximumVisitorsCapacity;
	}

	public int getMaximumOrderAmount() {
		return maximumOrderAmount;
	}

	public int getCurrentCapacity() {
		return currentCapacity;
	}

	public float getMaximumTimeLimit() {
		return maximumTimeLimit;
	}

	public void setParkID(int parkID) {
		this.parkID = parkID;
	}

	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	public void setParkAddress(String parkAddress) {
		this.parkAddress = parkAddress;
	}

	public void setParkManager(ParkManager parkManager) {
		this.parkManager = parkManager;
	}

	public void setEmployees(ArrayList<ParkEmployee> employees) {
		this.employees = employees;
	}

	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}

	public void setMaximumVisitorsCapacity(int maximumVisitorsCapacity) {
		this.maximumVisitorsCapacity = maximumVisitorsCapacity;
	}

	public void setMaximumOrderAmount(int maximumOrderAmount) {
		this.maximumOrderAmount = maximumOrderAmount;
	}

	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

	public void setMaximumTimeLimit(float maximumTimeLimit) {
		this.maximumTimeLimit = maximumTimeLimit;
	}

	public boolean addEmployeeToPark(ParkEmployee employeeToAdd) {
		if (employees == null)
			throw new NullPointerException();
		return employees.add(employeeToAdd);
	}

	public boolean deleteEmployeeFromPark(ParkEmployee employeeToDelete) {
		if (employees == null)
			throw new NullPointerException();
		return employees.remove(employeeToDelete);
	}

	@Override
	public String toString() {
		return "Park [parkID=" + parkID + ", parkName=" + parkName + ", parkAddress=" + parkAddress + ", parkManager="
				+ parkManager + ", employees=" + employees + ", departmentManager=" + departmentManager
				+ ", maximumVisitorsCapacity=" + maximumVisitorsCapacity + ", maximumOrderAmount=" + maximumOrderAmount
				+ ", currentCapacity=" + currentCapacity + ", maximumTimeLimit=" + maximumTimeLimit + "]";
	}
}