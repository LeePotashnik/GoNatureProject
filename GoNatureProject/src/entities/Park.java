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

	/**
	 * @return park id number
	 */
	public int getParkID() {
		return parkID;
	}

	/**
	 * @return park name
	 */
	public String getParkName() {
		return parkName;
	}

	/**
	 * @return park address
	 */
	public String getParkAddress() {
		return parkAddress;
	}

	/**
	 * @return park manager
	 */
	public ParkManager getParkManager() {
		return parkManager;
	}

	/**
	 * @return list of the park employees
	 */
	public ArrayList<ParkEmployee> getEmployees() {
		return employees;
	}

	/**
	 * @return park's department manager
	 */
	public DepartmentManager getDepartmentManager() {
		return departmentManager;
	}

	/**
	 * @return the park's maximum visitor capacity
	 */
	public int getMaximumVisitorsCapacity() {
		return maximumVisitorsCapacity;
	}

	/**
	 * @return park's maximum order amount
	 */
	public int getMaximumOrderAmount() {
		return maximumOrderAmount;
	}

	/**
	 * @return park's current capacity
	 */
	public int getCurrentCapacity() {
		return currentCapacity;
	}

	/**
	 * @return park's maximum time limit 
	 */
	public float getMaximumTimeLimit() {
		return maximumTimeLimit;
	}

	/**
	 * @param parkID	 * 
	 */
	public void setParkID(int parkID) {
		this.parkID = parkID;
	}

	/**
	 * @param parkName
	 */
	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	/**
	 * @param parkAddress
	 */
	public void setParkAddress(String parkAddress) {
		this.parkAddress = parkAddress;
	}

	/**
	 * @param parkManager
	 */
	public void setParkManager(ParkManager parkManager) {
		this.parkManager = parkManager;
	}

	/**
	 * @param employees
	 */
	public void setEmployees(ArrayList<ParkEmployee> employees) {
		this.employees = employees;
	}

	/**
	 * @param departmentManager
	 */
	public void setDepartmentManager(DepartmentManager departmentManager) {
		this.departmentManager = departmentManager;
	}

	/**
	 * @param maximumVisitorsCapacity
	 */
	public void setMaximumVisitorsCapacity(int maximumVisitorsCapacity) {
		this.maximumVisitorsCapacity = maximumVisitorsCapacity;
	}

	/**
	 * @param maximumOrderAmount
	 */
	public void setMaximumOrderAmount(int maximumOrderAmount) {
		this.maximumOrderAmount = maximumOrderAmount;
	}

	/**
	 * @param currentCapacity
	 */
	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

	/**
	 * @param maximumTimeLimit
	 */
	public void setMaximumTimeLimit(float maximumTimeLimit) {
		this.maximumTimeLimit = maximumTimeLimit;
	}

	/**
	 * @param employeeToAdd
	 * @return boolean if the employee added to the park
	 */
	public boolean addEmployeeToPark(ParkEmployee employeeToAdd) {
		if (employees == null)
			throw new NullPointerException();
		return employees.add(employeeToAdd);
	}

	/**
	 * @param employeeToDelete
	 * @return boolean if the employee removed from the park
	 */
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