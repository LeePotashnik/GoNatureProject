package entities;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a pending adjustment for a park. This class stores information
 * about a specific adjustment including the ID of the adjustment, park details
 * (ID, name, and department), the date and time of the adjustment, who adjusted
 * it, the parameter value before and after the adjustment, and the type of
 * parameter adjusted.
 */
public class PendingAdjustment {
	private String adjusmentId;
	private int parkId;
	private String parkName;
	private String department;
	private LocalDate dayOfAdjusting;
	private LocalTime timeOfAdjusting;
	private String adjustedBy;
	private int parameterBefore;
	private int parameterAfter;
	private String parameterType;

	/**
	 * Constructs a new PendingAdjustment with the specified details.
	 * 
	 * @param adjusmentId     The unique ID of the adjustment.
	 * @param parkId          The ID of the park where the adjustment is made.
	 * @param parkName        The name of the park.
	 * @param department      The department within the park that is responsible for
	 *                        the adjustment.
	 * @param dayOfAdjusting  The date when the adjustment was made.
	 * @param timeOfAdjusting The time when the adjustment was made.
	 * @param adjustedBy      The identifier of the individual who made the
	 *                        adjustment.
	 * @param parameterBefore The value of the parameter before the adjustment.
	 * @param parameterAfter  The value of the parameter after the adjustment.
	 * @param parameterType   The type of the parameter that was adjusted.
	 */
	public PendingAdjustment(String adjusmentId, int parkId, String parkName, String department,
			LocalDate dayOfAdjusting, LocalTime timeOfAdjusting, String adjustedBy, int parameterBefore,
			int parameterAfter, String parameterType) {
		this.adjusmentId = adjusmentId;
		this.parkId = parkId;
		this.parkName = parkName;
		this.department = department;
		this.dayOfAdjusting = dayOfAdjusting;
		this.timeOfAdjusting = timeOfAdjusting;
		this.adjustedBy = adjustedBy;
		this.parameterBefore = parameterBefore;
		this.parameterAfter = parameterAfter;
		this.parameterType = parameterType;
	}

	/**
	 * Returns the adjustment ID.
	 * 
	 * @return The adjustment ID.
	 */
	public String getAdjusmentId() {
		return this.adjusmentId;
	}

	/**
	 * Returns the park ID.
	 * 
	 * @return The ID of the park.
	 */
	public int getParkId() {
		return this.parkId;
	}

	/**
	 * Returns the department name.
	 * 
	 * @return The name of the department within the park.
	 */
	public String getDepartment() {
		return this.department;
	}

	/**
	 * Returns the park name.
	 * 
	 * @return The name of the park.
	 */
	public String getParkName() {
		return this.parkName;
	}

	/**
	 * Returns the day of adjusting.
	 * 
	 * @return The date when the adjustment was made.
	 */
	public LocalDate getDayOfAdjusting() {
		return this.dayOfAdjusting;
	}

	/**
	 * Returns the time of adjusting.
	 * 
	 * @return The time when the adjustment was made.
	 */
	public LocalTime getTimeOfAdjusting() {
		return this.timeOfAdjusting;
	}

	/**
	 * Returns the identifier of the person who made the adjustment.
	 * 
	 * @return The identifier of the individual who adjusted the parameter.
	 */
	public String getAdjustedBy() {
		return this.adjustedBy;
	}

	/**
	 * Returns the parameter value before the adjustment.
	 * 
	 * @return The value of the parameter before adjustment.
	 */
	public int getParameterBefore() {
		return this.parameterBefore;
	}

	/**
	 * Returns the parameter value after the adjustment.
	 * 
	 * @return The value of the parameter after adjustment.
	 */
	public int getParameterAfter() {
		return this.parameterAfter;
	}

	/**
	 * Returns the type of the adjusted parameter.
	 * 
	 * @return The type of parameter that was adjusted.
	 */
	public String getParameterType() {
		return this.parameterType;
	}
}