package entities;

import java.sql.Time;
import java.util.Date;

public class Order {
	public enum VisitType {
		INDIVIDUAL, GROUP;
	}
	
	private String bookingIdNumber;
	private Date dayOfVisit;
	private Time timeOfVisit;
	private Date dayOfBooking;
	private VisitType visitType;
	private int numberOfVisitors;
	private String firstName, lastName, emailAddress, phoneNumber;
	private int finalPrice;
	private boolean paid, confirmed;
	private Time entryParkTime, exitParkTime;
	private boolean isRecievedReminder;
	private Time reminderArrivalTime;
	

	public Order(String bookingIdNumber, Date dayOfVisit, Time timeOfVisit, Date dayOfBooking, VisitType visitType,
			int numberOfVisitors, String firstName, String lastName, String emailAddress, String phoneNumber,
			int finalPrice, boolean paid, boolean confirmed, Time entryParkTime, Time exitParkTime,
			boolean isRecievedReminder, Time reminderArrivalTime) {
		this.bookingIdNumber = bookingIdNumber;
		this.dayOfVisit = dayOfVisit;
		this.timeOfVisit = timeOfVisit;
		this.dayOfBooking = dayOfBooking;
		this.visitType = visitType;
		this.numberOfVisitors = numberOfVisitors;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.finalPrice = finalPrice;
		this.paid = paid;
		this.confirmed = confirmed;
		this.entryParkTime = entryParkTime;
		this.exitParkTime = exitParkTime;
		this.isRecievedReminder = isRecievedReminder;
		this.reminderArrivalTime = reminderArrivalTime;
	}


	@Override
	public String toString() {
		return "Order [bookingIdNumber=" + bookingIdNumber + ", dayOfVisit=" + dayOfVisit + ", timeOfVisit="
				+ timeOfVisit + ", dayOfBooking=" + dayOfBooking + ", visitType=" + visitType + ", numberOfVisitors="
				+ numberOfVisitors + ", firstName=" + firstName + ", lastName=" + lastName + ", emailAddress="
				+ emailAddress + ", phoneNumber=" + phoneNumber + ", finalPrice=" + finalPrice + ", paid=" + paid
				+ ", confirmed=" + confirmed + ", entryParkTime=" + entryParkTime + ", exitParkTime=" + exitParkTime
				+ ", isRecievedReminder=" + isRecievedReminder + ", reminderArrivalTime=" + reminderArrivalTime + "]";
	}

//	public Order(String bookingIdNumber, int numberOfVisitors, Park parkBooked, OrderStatus status, Date dayOfVisit,
//			Time timeOfVisit, Time entryParkTime, Time exitParkTime, VisitType visitType,
//			String phoneNumber, String emailAddress, float finalPrice) {
//		this.bookingIdNumber = bookingIdNumber;
//		this.numberOfVisitors = numberOfVisitors;
//		this.parkBooked = parkBooked;
//		this.status = status;
//		this.dayOfVisit = dayOfVisit;
//		this.timeOfVisit = timeOfVisit;
//		this.entryParkTime = entryParkTime;
//		this.exitParkTime = exitParkTime;
//		this.visitType = visitType;
//		this.phoneNumber = phoneNumber;
//		this.emailAddress = emailAddress;
//		this.finalPrice = finalPrice;
//	}
//
//	public String getBookingIdNumber() {
//		return bookingIdNumber;
//	}
//
//
//	public int getNumberOfVisitors() {
//		return numberOfVisitors;
//	}
//
//
//	public Park getParkBooked() {
//		return parkBooked;
//	}
//
//
//	public OrderStatus getStatus() {
//		return status;
//	}
//
//
//	public Date getDayOfVisit() {
//		return dayOfVisit;
//	}
//
//
//	public Time getTimeOfVisit() {
//		return timeOfVisit;
//	}
//
//
//	public Time getEntryParkTime() {
//		return entryParkTime;
//	}
//
//
//	public Time getExitParkTime() {
//		return exitParkTime;
//	}
//
//
//	public VisitType getVisitType() {
//		return visitType;
//	}
//
//
//	public String getPhoneNumber() {
//		return phoneNumber;
//	}
//
//
//	public String getEmailAddress() {
//		return emailAddress;
//	}
//
//
//	public float getFinalPrice() {
//		return finalPrice;
//	}
//
//
//	public void setBookingIdNumber(String bookingIdNumber) {
//		this.bookingIdNumber = bookingIdNumber;
//	}
//
//
//	public void setNumberOfVisitors(int numberOfVisitors) {
//		this.numberOfVisitors = numberOfVisitors;
//	}
//
//
//	public void setParkBooked(Park parkBooked) {
//		this.parkBooked = parkBooked;
//	}
//
//
//	public void setStatus(OrderStatus status) {
//		this.status = status;
//	}
//
//
//	public void setDayOfVisit(Date dayOfVisit) {
//		this.dayOfVisit = dayOfVisit;
//	}
//
//
//	public void setTimeOfVisit(Time timeOfVisit) {
//		this.timeOfVisit = timeOfVisit;
//	}
//
//
//	public void setEntryParkTime(Time entryParkTime) {
//		this.entryParkTime = entryParkTime;
//	}
//
//
//	public void setExitParkTime(Time exitParkTime) {
//		this.exitParkTime = exitParkTime;
//	}
//
//
//	public void setVisitType(VisitType visitType) {
//		this.visitType = visitType;
//	}
//
//
//	public void setPhoneNumber(String phoneNumber) {
//		this.phoneNumber = phoneNumber;
//	}
//
//
//	public void setEmailAddress(String emailAddress) {
//		this.emailAddress = emailAddress;
//	}
//
//
//	public void setFinalPrice(float finalPrice) {
//		this.finalPrice = finalPrice;
//	}


//	@Override
//	public String toString() {
//		return "Order [bookingIdNumber=" + bookingIdNumber + ", numberOfVisitors=" + numberOfVisitors + ", parkBooked="
//				+ parkBooked + ", status=" + status + ", dayOfVisit=" + dayOfVisit + ", timeOfVisit=" + timeOfVisit
//				+ ", entryParkTime=" + entryParkTime + ", exitParkTime=" + exitParkTime + ", visitType=" + visitType
//				+ ", phoneNumber=" + phoneNumber + ", emailAddress=" + emailAddress + ", finalPrice=" + finalPrice
//				+ "]";
//	}
	
	
}