package entities;

public class Park {
	private int parkId;
	private String parkName, parkCity, parkState, parkDepartment, parkManagerId, departmentManagerId;
	private int maximumVisitors, maximumOrders, timeLimit, currentCapacity;
	
	public Park(int parkId, String parkName, String parkCity, String parkState, String parkDepartment,
			String parkManagerId, String departmentManagerId, int maximumVisitors, int maximumOrders, int timeLimit,
			int currentCapacity) {
		this.parkId = parkId;
		this.parkName = parkName;
		this.parkCity = parkCity;
		this.parkState = parkState;
		this.parkDepartment = parkDepartment;
		this.parkManagerId = parkManagerId;
		this.departmentManagerId = departmentManagerId;
		this.maximumVisitors = maximumVisitors;
		this.maximumOrders = maximumOrders;
		this.timeLimit = timeLimit;
		this.currentCapacity = currentCapacity;
	}

	public int getParkId() {
		return parkId;
	}

	public String getParkName() {
		return parkName;
	}

	public String getParkCity() {
		return parkCity;
	}

	public String getParkState() {
		return parkState;
	}

	public String getParkDepartment() {
		return parkDepartment;
	}

	public String getParkManagerId() {
		return parkManagerId;
	}

	public String getDepartmentManagerId() {
		return departmentManagerId;
	}

	public int getMaximumVisitors() {
		return maximumVisitors;
	}

	public int getMaximumOrders() {
		return maximumOrders;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public int getCurrentCapacity() {
		return currentCapacity;
	}

	public void setParkId(int parkId) {
		this.parkId = parkId;
	}

	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	public void setParkCity(String parkCity) {
		this.parkCity = parkCity;
	}

	public void setParkState(String parkState) {
		this.parkState = parkState;
	}

	public void setParkDepartment(String parkDepartment) {
		this.parkDepartment = parkDepartment;
	}

	public void setParkManagerId(String parkManagerId) {
		this.parkManagerId = parkManagerId;
	}

	public void setDepartmentManagerId(String departmentManagerId) {
		this.departmentManagerId = departmentManagerId;
	}

	public void setMaximumVisitors(int maximumVisitors) {
		this.maximumVisitors = maximumVisitors;
	}

	public void setMaximumOrders(int maximumOrders) {
		this.maximumOrders = maximumOrders;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}
	
	
}
