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

	private String bookingId;
	private LocalDate dayOfVisit;
	private LocalTime timeOfVisit;
	private LocalDate dayOfBooking;
	private VisitType visitType;
	private int numberOfVisitors;
	private String idNumber, firstName, lastName, emailAddress, phoneNumber;
	private int finalPrice;
	private boolean paid, confirmed;
	private LocalTime entryParkTime, exitParkTime;
	private boolean isRecievedReminder;
	private LocalTime reminderArrivalTime;
	private Park parkBooked;
	private int waitingListPriority;

	/**
	 * 
	 * @param bookingId
	 * @param dayOfVisit
	 * @param timeOfVisit
	 * @param dayOfBooking
	 * @param visitType
	 * @param numberOfVisitors
	 * @param firstName
	 * @param lastName
	 * @param emailAddress
	 * @param phoneNumber
	 * @param finalPrice
	 * @param paid
	 * @param confirmed
	 * @param entryParkTime
	 * @param exitParkTime
	 * @param isRecievedReminder
	 * @param reminderArrivalTime
	 * @param parkBooked
	 */
	public Booking(String bookingId, LocalDate dayOfVisit, LocalTime timeOfVisit, LocalDate dayOfBooking,
			VisitType visitType, int numberOfVisitors, String idNumber, String firstName, String lastName,
			String emailAddress, String phoneNumber, int finalPrice, boolean paid, boolean confirmed,
			LocalTime entryParkTime, LocalTime exitParkTime, boolean isRecievedReminder, LocalTime reminderArrivalTime,
			Park parkBooked) {
		this.bookingId = bookingId;
		this.dayOfVisit = dayOfVisit;
		this.timeOfVisit = timeOfVisit;
		this.dayOfBooking = dayOfBooking;
		this.visitType = visitType;
		this.numberOfVisitors = numberOfVisitors;
		this.idNumber = idNumber;
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
		this.parkBooked = parkBooked;
	}
	
	public Booking(String bookingId, LocalTime timeOfVisit, LocalDate dayOfBooking, int waitingListPriority, VisitType visitType, int numberOfVisitor) {
		this.bookingId = bookingId;
		this.timeOfVisit = timeOfVisit;
		this.dayOfBooking = dayOfBooking;
		this.waitingListPriority = waitingListPriority;
		this.visitType = visitType;
		this.numberOfVisitors = numberOfVisitor;
	}
	
	/**
	 * @return
	 */
	public String getIdNumber() {
		return idNumber;
	}

	/**
	 * @param idNumber
	 */
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}
	
	/**
	 * 
	 * @return waitingListPriority
	 */
	public int getWaitingListPriority() {
		return waitingListPriority;
	}

	/**
	 * 
	 * @param waitingListPriority
	 */
	public void setWaitingListPriority(int waitingListPriority) {
		this.waitingListPriority = waitingListPriority;
	}

	/**
	 * @return bookingId
	 */
	public String getBookingId() {
		return bookingId;
	}

	/**
	 * @param bookingId
	 */
	public void setBookingId(String bookingId) {
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
	public void setFinalPrice(int finalPrice) {
		this.finalPrice = finalPrice;
	}

	@Override
	public String toString() {
		return "Booking [bookingId=" + bookingId + ", dayOfVisit=" + dayOfVisit + ", timeOfVisit=" + timeOfVisit
				+ ", dayOfBooking=" + dayOfBooking + ", visitType=" + visitType + ", numberOfVisitors="
				+ numberOfVisitors + ", idNumber=" + idNumber + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", emailAddress=" + emailAddress + ", phoneNumber=" + phoneNumber + ", finalPrice=" + finalPrice
				+ ", paid=" + paid + ", confirmed=" + confirmed + ", entryParkTime=" + entryParkTime + ", exitParkTime="
				+ exitParkTime + ", isRecievedReminder=" + isRecievedReminder + ", reminderArrivalTime="
				+ reminderArrivalTime + ", parkBooked=" + parkBooked + ", waitingListPriority=" + waitingListPriority
				+ "]";
	}
}