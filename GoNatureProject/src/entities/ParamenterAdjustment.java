package entities;

/**
 * 
 */
public class ParamenterAdjustment {
	private String parkName;
	private int visitorsMaximumCapacityBeforeChange, visitorsMaximumCapacityAfterChange;
	private int ordersAmountBeforeChange,ordersAmountAfterChange;
	private float timeLimitsBeforeChange,timeLimitsAfterChange;
	public enum Status {
		PENDING, APPROVED,REJECTED;
	}
	private Status status;
	
	public ParamenterAdjustment(String parkName, int visitorsMaximumCapacityBeforeChange,
			int visitorsMaximumCapacityAfterChange, int ordersAmountBeforeChange, int ordersAmountAfterChange,
			float timeLimitsBeforeChange, float timeLimitsAfterChange, Status status) {
		this.parkName = parkName;
		this.visitorsMaximumCapacityBeforeChange = visitorsMaximumCapacityBeforeChange;
		this.visitorsMaximumCapacityAfterChange = visitorsMaximumCapacityAfterChange;
		this.ordersAmountBeforeChange = ordersAmountBeforeChange;
		this.ordersAmountAfterChange = ordersAmountAfterChange;
		this.timeLimitsBeforeChange = timeLimitsBeforeChange;
		this.timeLimitsAfterChange = timeLimitsAfterChange;
		this.status = status;
	}

	/**
	 * @return parkName
	 */
	public String getParkName() {
		return parkName;
	}

	/**
	 * @param parkName
	 */
	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	/**
	 * @return visitorsMaximumCapacityBeforeChange
	 */
	public int getVisitorsMaximumCapacityBeforeChange() {
		return visitorsMaximumCapacityBeforeChange;
	}

	/**
	 * @param visitorsMaximumCapacityBeforeChange
	 */
	public void setVisitorsMaximumCapacityBeforeChange(int visitorsMaximumCapacityBeforeChange) {
		this.visitorsMaximumCapacityBeforeChange = visitorsMaximumCapacityBeforeChange;
	}

	/**
	 * @return visitorsMaximumCapacityAfterChange
	 */
	public int getVisitorsMaximumCapacityAfterChange() {
		return visitorsMaximumCapacityAfterChange;
	}

	/**
	 * @param visitorsMaximumCapacityAfterChange
	 */
	public void setVisitorsMaximumCapacityAfterChange(int visitorsMaximumCapacityAfterChange) {
		this.visitorsMaximumCapacityAfterChange = visitorsMaximumCapacityAfterChange;
	}

	/**
	 * @return ordersAmountBeforeChange
	 */
	public int getOrdersAmountBeforeChange() {
		return ordersAmountBeforeChange;
	}

	/**
	 * @param ordersAmountBeforeChange
	 */
	public void setOrdersAmountBeforeChange(int ordersAmountBeforeChange) {
		this.ordersAmountBeforeChange = ordersAmountBeforeChange;
	}

	/**
	 * @return ordersAmountAfterChange
	 */
	public int getOrdersAmountAfterChange() {
		return ordersAmountAfterChange;
	}

	/**
	 * @param ordersAmountAfterChange
	 */
	public void setOrdersAmountAfterChange(int ordersAmountAfterChange) {
		this.ordersAmountAfterChange = ordersAmountAfterChange;
	}

	/**
	 * @return timeLimitsBeforeChange
	 */
	public float getTimeLimitsBeforeChange() {
		return timeLimitsBeforeChange;
	}

	/**
	 * @param timeLimitsBeforeChange
	 */
	public void setTimeLimitsBeforeChange(float timeLimitsBeforeChange) {
		this.timeLimitsBeforeChange = timeLimitsBeforeChange;
	}

	/**
	 * @return timeLimitsAfterChange
	 */
	public float getTimeLimitsAfterChange() {
		return timeLimitsAfterChange;
	}

	/**
	 * @param timeLimitsAfterChange
	 */
	public void setTimeLimitsAfterChange(float timeLimitsAfterChange) {
		this.timeLimitsAfterChange = timeLimitsAfterChange;
	}

	/**
	 * @return status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 * initialize status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
