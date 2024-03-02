package entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Order {
	public enum VisitType {
		INDIVIDUAL, GROUP;
	}
	
	private int bookingIdNumber, numberOfVisitors;
	private Park parkBooked;
	private OrderStatus status;
	private LocalDate dayOfVisit;
	private LocalTime timeOfVisit, entryParkTime, exitParkTime;
	private VisitType visitType;
	private String phoneNumber, emailAddress;
	private float finalPrice;
	

	public Order(int bookingIdNumber, int numberOfVisitors, Park parkBooked, OrderStatus status, LocalDate dayOfVisit,
			LocalTime timeOfVisit, LocalTime entryParkTime, LocalTime exitParkTime, VisitType visitType,
			String phoneNumber, String emailAddress, float finalPrice) {
		this.bookingIdNumber = bookingIdNumber;
		this.numberOfVisitors = numberOfVisitors;
		this.parkBooked = parkBooked;
		this.status = status;
		this.dayOfVisit = dayOfVisit;
		this.timeOfVisit = timeOfVisit;
		this.entryParkTime = entryParkTime;
		this.exitParkTime = exitParkTime;
		this.visitType = visitType;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.finalPrice = finalPrice;
	}

	public int getBookingIdNumber() {
		return bookingIdNumber;
	}


	public int getNumberOfVisitors() {
		return numberOfVisitors;
	}


	public Park getParkBooked() {
		return parkBooked;
	}


	public OrderStatus getStatus() {
		return status;
	}


	public LocalDate getDayOfVisit() {
		return dayOfVisit;
	}


	public LocalTime getTimeOfVisit() {
		return timeOfVisit;
	}


	public LocalTime getEntryParkTime() {
		return entryParkTime;
	}


	public LocalTime getExitParkTime() {
		return exitParkTime;
	}


	public VisitType getVisitType() {
		return visitType;
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}


	public String getEmailAddress() {
		return emailAddress;
	}


	public float getFinalPrice() {
		return finalPrice;
	}


	public void setBookingIdNumber(int bookingIdNumber) {
		this.bookingIdNumber = bookingIdNumber;
	}


	public void setNumberOfVisitors(int numberOfVisitors) {
		this.numberOfVisitors = numberOfVisitors;
	}


	public void setParkBooked(Park parkBooked) {
		this.parkBooked = parkBooked;
	}


	public void setStatus(OrderStatus status) {
		this.status = status;
	}


	public void setDayOfVisit(LocalDate dayOfVisit) {
		this.dayOfVisit = dayOfVisit;
	}


	public void setTimeOfVisit(LocalTime timeOfVisit) {
		this.timeOfVisit = timeOfVisit;
	}


	public void setEntryParkTime(LocalTime entryParkTime) {
		this.entryParkTime = entryParkTime;
	}


	public void setExitParkTime(LocalTime exitParkTime) {
		this.exitParkTime = exitParkTime;
	}


	public void setVisitType(VisitType visitType) {
		this.visitType = visitType;
	}


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	public void setFinalPrice(float finalPrice) {
		this.finalPrice = finalPrice;
	}


	@Override
	public String toString() {
		return "Order [bookingIdNumber=" + bookingIdNumber + ", numberOfVisitors=" + numberOfVisitors + ", parkBooked="
				+ parkBooked + ", status=" + status + ", dayOfVisit=" + dayOfVisit + ", timeOfVisit=" + timeOfVisit
				+ ", entryParkTime=" + entryParkTime + ", exitParkTime=" + exitParkTime + ", visitType=" + visitType
				+ ", phoneNumber=" + phoneNumber + ", emailAddress=" + emailAddress + ", finalPrice=" + finalPrice
				+ "]";
	}
}