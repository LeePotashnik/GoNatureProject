package serverSide.control;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NotificationsController {
	private static NotificationsController instance;
	// Constant values for the sender's email and password
	private static final String SENDER_EMAIL = "gonaturesystem@gmail.com";
	private static final String SENDER_PASSWORD = "qczl yrss oqql rldi";

	// Private constructor to prevent instantiation from other classes
	private NotificationsController() {
	}

	// Public method to get the instance of the class
	public static NotificationsController getInstance() {
		if (instance == null)
			instance = new NotificationsController();
		return instance;
	}

	/**
	 * Sends an email notification to a user on the waiting list once a booking
	 * becomes active. Tailors the message based on the time until the scheduled
	 * visit.
	 *
	 * @param ArrayList that contains The booking details for which the notification
	 *                  is sent.
	 */
	public void sendWaitingListEmailNotification(List<Object> details) {
		String emailAddress = (String) details.get(0);
// 		String phoneNumber = (String)details.get(1);
		String parkName = (String) details.get(2);
		LocalDate dayofvisit = (LocalDate) details.get(3);
		LocalTime timeOfVisit = (LocalTime) details.get(4);
		String fullName = (String) details.get(5);
		String parkLocation = (String) details.get(6);
		Integer numberOfVisitors = (int) details.get(7);
		Integer finalPrice = (int) details.get(8);
		boolean isPaid = (boolean) details.get(9);
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime visitDateTime = LocalDateTime.of(dayofvisit, timeOfVisit);
		long hoursUntilVisit = Duration.between(now, visitDateTime).toHours();
		String message = "Hello, " + fullName + "!";
		if (hoursUntilVisit <= 24) {
			message += "\n\nWe are happy to inform you that we have found place to your booking to " + parkName + "!";
			message += "\nWe removed you from the waiting list and now your booking is active.";
			message += "\nThis is a gentle reminder that your reservation is scheduled for tomorrow.";
			message += "\nThis reminder will have to be confirmed within 2 hours in order to save your booking.";
			message += "\n\nIf you will not confirm, your booking will automatically be cancelled.";
			message += "\n\nThank you and we are looking forward to see you!";
		} else {
			message += "\n\nWe are happy to inform you that we have found place to your booking.";
			message += "\nWe removed you from the waiting list and now your booking is active.";
			message += "\nYou will get a reminder 24 hours before the day of your booking,";
			message += "\nThis reminder will have to be confirmed within 2 hours in order to save your booking.";
			message += "\n\nIf you will not confirm, your booking will automatically be cancelled.";
			message += "\n\nThank you and we are looking forward to see you!";
		}

		message += "\n\nYour booking details:";
		message += "\nPark: " + parkName + " in " + parkLocation;
		message += "\nDate: " + dayofvisit + "    Time: " + timeOfVisit;
		message += "\nNumber of Visitors: " + numberOfVisitors + "    Final Price: " + finalPrice + "$";
		message += isPaid ? "\nYour booking is fully paid!"
				: "\nYour booking is not paid, you will need to pay at the park entrance.";

		sendEmail(emailAddress, "GoNature - No More Waiting!", message);
	}

	/**
	 * Sends a booking confirmation email notification to a user.
	 *
	 * @param ArrayList that contains The booking details for which the confirmation
	 *                  is sent.
	 */
	public void sendConfirmationEmailNotification(List<Object> details) {
		String emailAddress = (String) details.get(0);
// 		String phoneNumber = (String)details.get(1);
		String parkName = (String) details.get(2);
		LocalDate dayofvisit = (LocalDate) details.get(3);
		LocalTime timeOfVisit = (LocalTime) details.get(4);
		String fullName = (String) details.get(5);
		String parkLocation = (String) details.get(6);
		Integer numberOfVisitors = (int) details.get(7);
		Integer finalPrice = (Integer) details.get(8);
		boolean isPaid = (Boolean) details.get(9);
		String message = "Hello, " + fullName + "!";
		message += "\n\nWe are pleased to confirm your reservation to " + parkName + "!";
		message += "\nYou will get a reminder 24 hours before the day of your booking,";
		message += "\nThis reminder will have to be confirmed within 2 hours in order to save your booking.";
		message += "\n\nIf you will not confirm, your booking will automatically be cancelled.";
		message += "\n\nThank you and we are looking forward to see you!";
		message += "\n\nYour booking details:";
		message += "\nPark: " + parkName + " in " + parkLocation;
		message += "\nDate: " + dayofvisit + "    Time: " + timeOfVisit;
		message += "\nNumber of Visitors: " + numberOfVisitors + "    Final Price: " + finalPrice + "$";
		message += isPaid ? "\nYour booking is fully paid!"
				: "\nYour booking is not paid, you will need to pay at the park entrance.";

		sendEmail(emailAddress, "GoNature - Confirmation", message);
	}

	/**
	 * Sends a booking confirmation email notification to a user.
	 *
	 * @param ArrayList that contains The booking details for which the confirmation
	 *                  is sent.
	 */
	public void sendCancellationEmailNotification(List<Object> details) {
		String emailAddress = (String) details.get(0);
// 		String phoneNumber = (String)details.get(1);
		String parkName = (String) details.get(2);
		LocalDate dayofvisit = (LocalDate) details.get(3);
		LocalTime timeOfVisit = (LocalTime) details.get(4);
		String fullName = (String) details.get(5);
		String parkLocation = (String) details.get(6);
		Integer numberOfVisitors = (int) details.get(7);
		Integer finalPrice = (int) details.get(8);
		boolean isPaid = (boolean) details.get(9);
		String message = "Hello, " + fullName + "!";
		message += "\n\nWe are sorry to confirm that your booking to " + parkName + " is now cancelled.";
		message += isPaid ? "\nYou will be fully refunded within 48 hours." : "";
		message += "\n\nThank you and we are looking forward to see you some day in the near future!";
		message += "\n\nYour booking details:";
		message += "\nPark: " + parkName + " in " + parkLocation;
		message += "\nDate: " + dayofvisit + "    Time: " + timeOfVisit;
		message += "\nNumber of Visitors: " + numberOfVisitors + "    Final Price: " + finalPrice + "$";

		sendEmail(emailAddress, "GoNature - Cancellation", message);
	}

	/**
	 * Sends a reminder email notification to a user about their upcoming booking.
	 *
	 * @param ArrayList that contains The booking details for which the reminder is
	 *                  sent.
	 */
	public void sendReminderEmailNotification(List<Object> details) {
		details = (ArrayList<Object>) details;
		String emailAddress = (String) details.get(0);
// 		String phoneNumber = (String)details.get(1);
		String parkName = (String) details.get(2);
		LocalDate dayofvisit = (LocalDate) details.get(3);
		LocalTime timeOfVisit = (LocalTime) details.get(4);
		String fullName = (String) details.get(5);
		String parkLocation = (String) details.get(6);
		Integer numberOfVisitors = (int) details.get(7);
		Integer finalPrice = (int) details.get(8);
		boolean isPaid = (boolean) details.get(9);
		String message = "Hello, " + fullName + "!";
		message += "\n\nWe hope this message finds you well.";
		message += "\nThis is a gentle reminder that your reservation is scheduled for tomorrow.";
		message += "\nthis reminder will have to be confirmed within 2 hours in order to save your booking.";
		message += "\n\nIf you will not confirm, your booking will automatically be cancelled.";
		message += "\n\nThank you and we are looking forward to see you!";
		message += "\n\nYour booking details:";
		message += "\nPark: " + parkName + " in " + parkLocation;
		message += "\nDate: " + dayofvisit + "    Time: " + timeOfVisit;
		message += "\nNumber of Visitors: " + numberOfVisitors + "    Final Price: " + finalPrice;
		message += isPaid ? "\nYour booking is fully paid!"
				: "\nYour booking is not paid, you will need to pay at the park entrance.";

		sendEmail(emailAddress, "GoNature - Booking Reminder", message);
	}

	/**
	 * Core method for sending an email using SMTP protocol.
	 *
	 * @param to          The recipient's email address.
	 * @param subject     The subject line of the email.
	 * @param textMessage The body text of the email.
	 */
	public static void sendEmail(String to, String subject, String textMessage) {
		// Set up the SMTP server properties
		Properties p = new Properties();
		p.put("mail.smtp.auth", "true");
		p.put("mail.transport.protocol", "smtp");
		p.put("mail.smtp.host", "smtp.gmail.com");
		p.put("mail.smtp.port", "587");
		p.put("mail.smtp.starttls.enable", "true");
		p.put("mail.smtp.starttls.required", "true");

		// Create a Session object
		Session session = Session.getInstance(p, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
			}
		});

		try {
			// Create a default MimeMessage object
			MimeMessage message = new MimeMessage(session);

			// Set From: header field
			message.setFrom(new InternetAddress(SENDER_EMAIL));

			// Set To: header field
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject(subject);

			// Set the actual message
			message.setText(textMessage);

			// Send message
			Transport.send(message);
			System.out.println(
					LocalTime.of(LocalTime.now().getHour(), LocalTime.now().getMinute(), LocalTime.now().getSecond())
							+ ": Email sent successfully to " + to);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
}
