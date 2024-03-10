package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

public class JustChecking {
	private String bookingId;
	private String firstName;
	private int finalPrice;

	public JustChecking(String bookingId, String firstName, int finalPrice) {
		this.bookingId = bookingId;
		this.firstName = firstName;
		this.finalPrice = finalPrice;
	}

	@Override
	public String toString() {
		return "JustChecking [bookingId=" + bookingId + ", firstName=" + firstName + ", finalPrice=" + finalPrice + "]";
	}

	public static void main(String[] args) {
		int futureBookingsRange = 4;
		LocalDate now = LocalDate.of(2024, 03, 14);
		int year, month = now.getMonthValue(), day;
		// moving to next year if needed
		if (month + futureBookingsRange > 12) {
			year = now.getYear() + 1;
			month = month + futureBookingsRange - 12;
		} else {
			year = now.getYear();
			month = month + futureBookingsRange;
		}
		// adjusting the day to fit the month (e.g February 31 is not valid)
		if (Arrays.asList(1, 3, 5, 7, 8, 10, 12).contains(month)) {
			day = now.getDayOfMonth();
		} else if (Arrays.asList(4, 6, 9, 11).contains(month)) {
			if (now.getDayOfMonth() == 31) {
				day = 1;
				month = month == 12 ? 1 : month + 1;
				year = month == 1 ? year + 1 : year;
			} else {
				day = now.getDayOfMonth();
			}
		} else {
			if (now.getDayOfMonth() > 28) {
				day = 1;
				month = month == 12 ? 1 : month + 1;
				year = month == 1 ? year + 1 : year;
			} else {
				day = now.getDayOfMonth();
			}
		}

		LocalDate future = LocalDate.of(year, month, day);
		System.out.println("NOW: " + LocalTime.now());
		System.out.println("FUTURE: " + future);
	}
}
