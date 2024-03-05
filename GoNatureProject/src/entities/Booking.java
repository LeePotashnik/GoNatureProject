package entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
	public enum VisitType {
		INDIVIDUAL, GROUP;
	}
	
	public enum Status {
		PENDING, CANCELLED, INWAITINGLIST, DONE;
	}
	
	private int bookingId, numberOfVisitors;
	private Park parkBooked;
	private OrderStatus status;
	private LocalDate dayOfVisit, dayOfBooking;
	private LocalTime timeOfVisit, entryParkTime, exitParkTime, reminderArrivalTime;
	private VisitType visitType;
	private String firstName, lastName, phoneNumber, emailAddress;
	private float finalPrice;
	private boolean paid, confirmed, isRecievedReminder;
	

	public Booking(int bookingId, int numberOfVisitors, Park parkBooked, OrderStatus status, LocalDate dayOfVisit, 
			LocalDate dayOfBooking, LocalTime timeOfVisit, LocalTime entryParkTime, LocalTime exitParkTime,
			LocalTime reminderArrivalTime, VisitType visitType, String firstName, String lastName, boolean isRecievedReminder, 
			String phoneNumber, String emailAddress, float finalPrice, boolean paid, boolean confirmed) {
		this.bookingId = bookingId;
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

	/**
	 * @return bookingId
	 */
	public int getBookingId() {
		return bookingId;
	}

	/**
	 * @param bookingId
	 */
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	/**
	 * @return dayOfBooking
	 */
	public LocalDate getDayOfBooking() {
		return dayOfBooking;
	}

	/**
	 * @param dayOfBooking
	 */
	public void setDayOfBooking(LocalDate dayOfBooking) {
		this.dayOfBooking = dayOfBooking;
	}

	/**
	 * @return reminderArrivalTime
	 */
	public LocalTime getReminderArrivalTime() {
		return reminderArrivalTime;
	}

	/**
	 * @param reminderArrivalTime
	 */
	public void setReminderArrivalTime(LocalTime reminderArrivalTime) {
		this.reminderArrivalTime = reminderArrivalTime;
	}

	/**
	 * @return firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return paid
	 */
	public boolean isPaid() {
		return paid;
	}

	/**
	 * @param paid
	 */
	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	/**
	 * @return confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	/**
	 * @param confirmed
	 */
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	/**
	 * @return isRecievedReminder
	 */
	public boolean isRecievedReminder() {
		return isRecievedReminder;
	}

	/**
	 * @param isRecievedReminder
	 */
	public void setRecievedReminder(boolean isRecievedReminder) {
		this.isRecievedReminder = isRecievedReminder;
	}
	
	/**
	 * @return bookingId
	 */
	public int getBookingIdNumber() {
		return bookingId;
	}

	/**
	 * @return numberOfVisitors
	 */
	public int getNumberOfVisitors() {
		return numberOfVisitors;
	}


	/**
	 * @return parkBooked
	 */
	public Park getParkBooked() {
		return parkBooked;
	}

	/**
	 * @return status (can be PENDING, CANCELLED, INWAITINGLIST, DONE)
	 */
	public OrderStatus getStatus() {
		return status;
	}

	/**
	 * @return dayOfVisit
	 */
	public LocalDate getDayOfVisit() {
		return dayOfVisit;
	}

	/**
	 * @return timeOfVisit
	 */
	public LocalTime getTimeOfVisit() {
		return timeOfVisit;
	}

	/**
	 * @return entryParkTime
	 */
	public LocalTime getEntryParkTime() {
		return entryParkTime;
	}

	/**
	 * @return exitParkTime
	 */
	public LocalTime getExitParkTime() {
		return exitParkTime;
	}


	/**
	 * @return visitType (can be INDIVIDUAL or GROUP)
	 */
	public VisitType getVisitType() {
		return visitType;
	}


	/**
	 * @return phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}


	/**
	 * @return emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}


	/**
	 * @return finalPrice
	 */
	public float getFinalPrice() {
		return finalPrice;
	}


	/**
	 * @param bookingIdNumber
	 */
	public void setBookingIdNumber(int bookingIdNumber) {
		this.bookingId = bookingIdNumber;
	}


	/**
	 * @param numberOfVisitors
	 */
	public void setNumberOfVisitors(int numberOfVisitors) {
		this.numberOfVisitors = numberOfVisitors;
	}


	/**
	 * @param parkBooked
	 */
	public void setParkBooked(Park parkBooked) {
		this.parkBooked = parkBooked;
	}


	/**
	 * @param status
	 */
	public void setStatus(OrderStatus status) {
		this.status = status;
	}


	/**
	 * @param dayOfVisit
	 */
	public void setDayOfVisit(LocalDate dayOfVisit) {
		this.dayOfVisit = dayOfVisit;
	}


	/**
	 * @param timeOfVisit
	 */
	public void setTimeOfVisit(LocalTime timeOfVisit) {
		this.timeOfVisit = timeOfVisit;
	}


	/**
	 * @param entryParkTime
	 */
	public void setEntryParkTime(LocalTime entryParkTime) {
		this.entryParkTime = entryParkTime;
	}


	/**
	 * @param exitParkTime
	 */
	public void setExitParkTime(LocalTime exitParkTime) {
		this.exitParkTime = exitParkTime;
	}


	/**
	 * @param visitType	 
	 */
	public void setVisitType(VisitType visitType) {
		this.visitType = visitType;
	}


	/**
	 * @param phoneNumber
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	/**
	 * @param emailAddress
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	/**
	 * @param finalPrice
	 */
	public void setFinalPrice(float finalPrice) {
		this.finalPrice = finalPrice;
	}


	@Override
	public String toString() {
		return "Order [bookingIdNumber=" + bookingId + ", numberOfVisitors=" + numberOfVisitors + ", parkBooked="
				+ parkBooked + ", status=" + status + ", dayOfVisit=" + dayOfVisit + ", timeOfVisit=" + timeOfVisit
				+ ", entryParkTime=" + entryParkTime + ", exitParkTime=" + exitParkTime + ", visitType=" + visitType
				+ ", phoneNumber=" + phoneNumber + ", emailAddress=" + emailAddress + ", finalPrice=" + finalPrice
				+ "]";
	}
}