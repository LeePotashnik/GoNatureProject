package entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

public class PendingAdjustment
{
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

	public PendingAdjustment(String adjusmentId, int parkId, String parkName, String department,
			LocalDate dayOfAdjusting,LocalTime timeOfAdjusting,String adjustedBy,int parameterBefore, int parameterAfter,String parameterType)
	{
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
	
	public String getAdjusmentId()
	{
		return this.adjusmentId;
	}
	public int getParkId()
	{
		return this.parkId;
	}
	
	public String getDepartment()
	{
		return this.department;
	}
	public String getParkName()
	{
		return this.parkName;
	}

	public LocalDate getDayOfAdjusting()
	{
		return this.dayOfAdjusting;
	}
	public LocalTime getTimeOfAdjusting()
	{
		return this.timeOfAdjusting;
	}
	public String getAdjustedBy()
	{
		return this.adjustedBy;
	}
	public int getParameterBefore()
	{
		return this.parameterBefore;
	}
	public int getParameterAfter()
	{
		return this.parameterAfter;
	}
	public String getParameterType()
	{
		return this.parameterType;
	}
	

	
	
	
	
	
	
}
