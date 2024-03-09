package clientSide.control;

import java.time.LocalDate;

import entities.Park;

public class ParkController {
	private static ParkController instance;
	
	private ParkController() {
		
	}
	
	public static ParkController getInstance() {
		if (instance == null)
			instance = new ParkController();
		return instance;
	}
	
	public String nameOfTable(Park park) {
		return park.getParkName().toLowerCase().replaceAll(" ", "_");
	}
	
	public static void main(String[] args) {
		LocalDate to = LocalDate.of(2024, 2, 1);
		to =to.plusMonths(1);
		to = to.minusDays(1);
		System.out.println(to);
	}
}
