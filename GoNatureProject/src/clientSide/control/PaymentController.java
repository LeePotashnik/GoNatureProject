package clientSide.control;

import java.util.ArrayList;
import java.util.Arrays;

import clientSide.gui.GoNatureClientUI;
import common.communication.Communication;
import common.communication.CommunicationException;
import common.communication.Communication.CommunicationType;
import common.communication.Communication.QueryType;
import entities.Booking;

public class PaymentController {
	private int individualFullPrice,individualDiscount, groupFullPrice, groupPreorderDiscount, groupPrepaidDiscount; 
	private static PaymentController instance;
	
	private PaymentController() {
		
	}
	
	public static PaymentController getInstance() {
		if (instance == null)
			instance = new PaymentController();
		return instance;
	}
	
	/**
     * Retrieves pricing details from the database and updates local fields
     * to reflect the current prices and discounts available for both individual visitors
     * and groups.
     */
	public void getPricingDetails() {
		Communication request = new Communication(CommunicationType.QUERY_REQUEST);
		try {
			request.setQueryType(QueryType.SELECT);
			request.setTables(Arrays.asList(Communication.pricing));
			request.setSelectColumns(Arrays.asList("individualFullPrice", "individualDiscount", "groupFullPrice", "groupPreorderDiscount",
					"groupPrepaidDiscount"));
			request.setWhereConditions(Arrays.asList("pricingId"), Arrays.asList("="), Arrays.asList("1"));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		GoNatureClientUI.client.accept(request);
		ArrayList<Object[]> result = request.getResultList();
		if (result.isEmpty()) {
			
		} else {
			for (Object[] row : result) {
				if (row[0] instanceof Integer) {
					this.individualFullPrice=(int) row[0];
				}
				if (row[1] instanceof Integer) {
					this.individualDiscount=(int) row[1];
				}
				if (row[2] instanceof Integer) {
					this.groupFullPrice=(int) row[2];
				}
				if (row[3] instanceof Integer) {
					this.groupPreorderDiscount=(int) row[3];
				}
				if (row[4] instanceof Integer) {
					this.groupPrepaidDiscount=(int) row[4];
				}
			}
		}
	}
	
	/**
     * Calculates the discounted price for a booking made by a travelers' group.
     *
     * @param newBooking The booking for which the price is calculated.
     * @return The total price after applying the discount for individual travelers.
     */
	public int calculateDiscountPriceTravelersGroup(Booking newBooking) {
		getPricingDetails();
		return newBooking.getNumberOfVisitors() * (individualDiscount);		
	}
	
	/**
     * Calculates the discounted price for a booking made by a guided group.
     *
     * @param newBooking The booking for which the price is calculated.
     * @param prePaid Indicates whether the booking was prepaid to apply the appropriate discount.
     * @return The total price after applying the discount for the guided group.
     */
	public int calculateDiscountPriceGuidedGroup(Booking newBooking, boolean prePaid) {
		getPricingDetails();
		if(prePaid) {
			return ((newBooking.getNumberOfVisitors() - 1) * (groupPrepaidDiscount));		

		}
		return ((newBooking.getNumberOfVisitors() - 1) * (groupPreorderDiscount));		
	}
	
	/**
     * Calculates the regular price for a booking made by a travelers' group without any discounts.
     *
     * @param newBooking The booking for which the price is calculated.
     * @return The total regular price for the travelers' group.
     */
	public int calculateRegularPriceTravelersGroup(Booking newBooking) {
		getPricingDetails();
		return newBooking.getNumberOfVisitors() * (individualFullPrice);		
	}
	
	/**
     * Calculates the regular price for a booking made by a guided group without any discounts.
     *
     * @param newBooking The booking for which the price is calculated.
     * @return The total regular price for the guided group.
     */
	public int calculateRegularPriceGuidedGroup(Booking newBooking) {
		getPricingDetails();
		return newBooking.getNumberOfVisitors() * (groupFullPrice);		
	}
	
	
}
