package entities;

import java.sql.Time;
import java.time.LocalTime;

public class DepartmentalReport {
	
	public enum ReportType {
		VISITREPORT, CANCELLATIONREPORT;
	}
	private LocalTime timeOfProducing;
	private DepartmentManager produceBy;
	
	
	public DepartmentalReport(LocalTime timeOfProducing, DepartmentManager produceBy) {
		super();
		this.timeOfProducing = timeOfProducing;
		this.produceBy = produceBy;
	}
	public LocalTime getTimeOfProducing() {
		return timeOfProducing;
	}
	public void setTimeOfProducing(LocalTime timeOfProducing) {
		this.timeOfProducing = timeOfProducing;
	}
	public DepartmentManager getProduceBy() {
		return produceBy;
	}
	public void setProduceBy(DepartmentManager produceBy) {
		this.produceBy = produceBy;
	}
	
		
		
	

}
