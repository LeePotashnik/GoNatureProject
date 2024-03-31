package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ReportsController;
import clientSide.entities.DepartmentManager;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import common.entities.Park;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 * Controller for the Department Manager Reports Screen. This class is
 * responsible for handling user interactions with the screen for generating and
 * viewing various reports related to park visitations.
 */

public class DepartmentManagerReportsScreenController extends AbstractScreen {

	private ReportsController control;// controller
	private DepartmentManager departmentManager;
	private ArrayList<Park> parks;
	private Map<String, String> months = new TreeMap<>();

	/**
	 * Constructor, initializes the department manager controller instance
	 */
	public DepartmentManagerReportsScreenController() {
		control = ReportsController.getInstance();
	}

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Button backButton, generateButton;
	@FXML
	private ChoiceBox<String> choiceBoxMonth, choiceBoxPark, choiceBoxYear;
	@FXML
	private Label titleLbl;
	@FXML
	private HBox hbox;
	@FXML
	private VBox vbox1, vbox2, vbox3;
	@FXML
	private Pane pane;
	@FXML
	private RadioButton radioVisit, radioUsage, radioCancel, radioTotal;
	private ToggleGroup group = new ToggleGroup();

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * This method is called if the Generate Report button is clicked. Validates the
	 * chosen values and calls the relevant method to generate the report
	 * 
	 * @param event
	 */
	void generateButtonClicked(ActionEvent event) {
		String selectedMonth = null;

		// getting the chosen month's code
		if (choiceBoxMonth.getValue() != null) {
			for (Map.Entry<String, String> entry : months.entrySet()) {
				if (entry.getValue().equals(choiceBoxMonth.getValue())) {
					selectedMonth = entry.getKey();
				}
			}
		}
		String selectedYear = choiceBoxYear.getValue();
		String selectedParkName = choiceBoxPark.getValue();

		if (validate(selectedMonth, selectedYear, selectedParkName)) {
			RadioButton selected = (RadioButton) group.getSelectedToggle();
			switch ((String) selected.getUserData()) {
			case "visit":
				visitReport(selectedMonth, selectedYear, selectedParkName);
				break;
			case "usage":
				usageReport(selectedMonth, selectedYear, selectedParkName);
				break;
			case "cancel":
				cancellationReport(selectedMonth, selectedYear, selectedParkName);
				break;
			case "total":
				totalNumberOfVisitorsReport(selectedMonth, selectedYear, selectedParkName);
				break;
			}
		}

	}

	/**
	 * This method is called after an event is created with clicking on the Back
	 * button. Returns the user to the previous screen
	 * 
	 * @param event
	 */
	@FXML
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(false, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	@FXML
	/**
	 * Sets the focus to the pane
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This methods validates the user's choices
	 * 
	 * @return true if valid, false if not
	 */
	private boolean validate(String selectedMonth, String selectedYear, String selectedParkName) {
		choiceBoxMonth.setStyle(setFieldToRegular());
		choiceBoxYear.setStyle(setFieldToRegular());
		choiceBoxPark.setStyle(setFieldToRegular());

		boolean valid = true;
		String error = "Errors:";

		if (selectedMonth == null) {
			choiceBoxMonth.setStyle(setFieldToError());
			valid = false;
			error += "\n• Please choose a month";
		}

		if (selectedYear == null) {
			choiceBoxYear.setStyle(setFieldToError());
			valid = false;
			error += "\n• Please choose a year";
		}

		if (selectedParkName == null) {
			choiceBoxPark.setStyle(setFieldToError());
			valid = false;
			error += "\n• Please choose park/s";
		}

		RadioButton selected = (RadioButton) group.getSelectedToggle();
		if (selected == null) {
			valid = false;
			error += "\n• Please choose a report type";
		} else if (selected.getUserData().equals("total") && selectedParkName != null) {
			if (selectedParkName.equals("All Parks")) {
				error += "\n• Total number of visitors report can be generated for a specific park only";
				valid = false;
			}
		} else if (selected.getUserData().equals("usage") && selectedParkName != null) {
			if (selectedParkName.equals("All Parks")) {
				error += "\n• Usage report can be generated for a specific park only";
				valid = false;
			}
		}
		if (!valid) {
			showErrorAlert(error);
		}

		return valid;
	}

	/**
	 * Generates and displays the Total Number of Visitors report for a selected
	 * park, month, and year. This method first verifies the existence of the
	 * selected park and checks if the report data is available for the given time
	 * period and park. If the data is available, it proceeds to generate the
	 * report.
	 * 
	 * @param selectedMonth    The code of the month for which the report is to be
	 *                         generated.
	 * @param selectedYear     The year for which the report is to be generated.
	 * @param selectedParkName The name of the park for which the report is to be
	 *                         generated.
	 */
	private void totalNumberOfVisitorsReport(String selectedMonth, String selectedYear, String selectedParkName) {
		// getting the park object based on the selected park name
		Park selectedPark = parks.stream().filter(p -> p.getParkName().equals(selectedParkName)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Selected park not found"));

		// Check if report data is available for the selected park, month, and year.
		if (!control.isParkManagerReportAvailable(selectedMonth, selectedYear, selectedPark, "total_visitors")) {
			// If data is not available, show a message to the user.
			showErrorAlert("The park manager has not produced the report yet.");
			return;
		}
		Pair<Integer, Integer> visitorsData = control.generateTotalNumberOfVisitorsReport(selectedMonth, selectedYear,
				selectedPark);
		try {
			ScreenManager.getInstance().showScreen("TotalNumberOfVisitorsReportController",
					"/clientSide/fxml/TotalNumberOfVisitorsReport.fxml", true, false, visitorsData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates and displays a usage report for a specific park, month, and year.
	 * This method validates the existence of the park based on the selected name
	 * and checks if the park manager has available data for the specified period.
	 * If the data is available, it retrieves usage statistics.
	 * 
	 * @param selectedMonth    The month for which the report is being generated.
	 * @param selectedYear     The year for which the report is being generated.
	 * @param selectedParkName The name of the park for which the report is being
	 *                         generated.
	 */
	private void usageReport(String selectedMonth, String selectedYear, String selectedParkName) {
		Park selectedPark = parks.stream().filter(p -> p.getParkName().equals(selectedParkName)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Selected park not found"));

		// Check if report data is available for the selected park, month, and year.
		if (!control.isParkManagerReportAvailable(selectedMonth, selectedYear, selectedPark, "usage_report")) {
			// If data is not available, show a message to the user.
			showErrorAlert("The park manager has not produced the report yet.");
			return;
		}
		List<Pair<LocalDate, Integer>> usageData = control.generateUsageReport(selectedMonth, selectedYear,
				selectedPark);
		try {
			ScreenManager.getInstance().showScreen("UsageReportController", "/clientSide/fxml/UsageReport.fxml", true,
					false, usageData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Generates and displays a cancellation report for a specific park, or all
	 * parks collectively, for a given month and year. The method handles both
	 * individual park data and aggregated data for all parks. It checks for report
	 * availability before generating or displaying the data.
	 * 
	 * @param selectedMonth    The month for which the cancellation report is being
	 *                         generated.
	 * @param selectedYear     The year for which the cancellation report is being
	 *                         generated.
	 * @param selectedParkName The name of the park for the cancellation report, or
	 *                         "All Parks" for aggregated data.
	 */
	private void cancellationReport(String selectedMonth, String selectedYear, String selectedParkName) {
		// Handle the "All Parks" option
		if ("All Parks".equals(selectedParkName)) {
			boolean isAvailable = false;
			for (Park park : parks) {
				if (control.isReportDataAvailable(selectedMonth, selectedYear, park, "cancelled")) {
					isAvailable = true;
					break;
				}
			}

			if (!isAvailable) { // no data available to generate report
				showErrorAlert("No data available for the selected time period.");
				return;
			}

			Pair<Map<String, List<XYChart.Data<String, Number>>>, Pair<Integer, Integer>> pair = control
					.generateCancellationReport(selectedMonth, selectedYear, parks);

			// Show the aggregated report
			try {
				ScreenManager.getInstance().showScreen("CancellationReportController",
						"/clientSide/fxml/CancellationReport.fxml", true, false, pair);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
			
		} else {
			// Find the Park object that matches the selected park name
			Park selectedPark = parks.stream().filter(p -> p.getParkName().equals(selectedParkName)).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Selected park not found"));
			// Check if data is available for the selected parameters
			if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "cancelled")) {
				showErrorAlert("No data available for the selected time period.");
				return;
			}
			Pair<Map<String, List<Data<String, Number>>>, Pair<Integer, Integer>> pair = control
					.generateCancellationReport(selectedMonth, selectedYear, selectedPark);
			try {
				ScreenManager.getInstance().showScreen("CancellationReportController",
						"/clientSide/fxml/CancellationReport.fxml", true, false, pair);
			} catch (StatefulException | ScreenException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generates and displays a visitation report for either a specific park or
	 * aggregated across all parks, based on user selection for a particular month
	 * and year. This method ensures the selected park exists checks report data
	 * availability, and then displays the visitation data.
	 *
	 * @param selectedMonth    The month for which the visit report is being
	 *                         generated.
	 * @param selectedYear     The year for which the visit report is being
	 *                         generated.
	 * @param selectedParkName The name of the park for the visit report, or "All
	 *                         Parks" for aggregated data.
	 */
	private void visitReport(String selectedMonth, String selectedYear, String selectedParkName) {
		try {
			// Handle the "All Parks" option
			if ("All Parks".equals(selectedParkName)) {
				// Aggregate data for all parks
				Map<String, List<XYChart.Data<Number, Number>>> visitData = new HashMap<>();
				for (Park park : parks) {
					if (control.isReportDataAvailable(selectedMonth, selectedYear, park, "done")) {
						Map<String, List<XYChart.Data<Number, Number>>> parkVisitData = control
								.generateVisitReport(selectedMonth, selectedYear, park);
						parkVisitData.forEach((key, value) -> visitData.merge(key, value, (v1, v2) -> {
							List<XYChart.Data<Number, Number>> mergedList = new ArrayList<>(v1);
							mergedList.addAll(v2);
							return mergedList;
						}));
					}
				}
				ScreenManager.getInstance().showScreen("VisitReportController", "/clientSide/fxml/VisitReport.fxml",
						true, false, visitData);
			} else {
				// Find the Park object that matches the selected park name
				Park selectedPark = parks.stream().filter(p -> p.getParkName().equals(selectedParkName)).findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Selected park not found"));
				// Check if data is available for the selected parameters
				if (!control.isReportDataAvailable(selectedMonth, selectedYear, selectedPark, "done")) {
					showErrorAlert("No data available for the selected time period.");
					return;
				}
				Map<String, List<XYChart.Data<Number, Number>>> visitData = control.generateVisitReport(selectedMonth,
						selectedYear, selectedPark);
				ScreenManager.getInstance().showScreen("VisitReportController", "/clientSide/fxml/VisitReport.fxml",
						true, false, visitData);
			}
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
		}
	}

	private void setMonths() {
		months.put("01", "January");
		months.put("02", "February");
		months.put("03", "March");
		months.put("04", "April");
		months.put("05", "May");
		months.put("06", "June");
		months.put("07", "July");
		months.put("08", "August");
		months.put("09", "September");
		months.put("10", "October");
		months.put("11", "November");
		months.put("12", "December");
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@FXML
	/**
	 * This method initializes the JavaFX components
	 */
	public void initialize() {
		departmentManager = (DepartmentManager) GoNatureUsersController.getInstance().restoreUser();
		GoNatureUsersController.getInstance().saveUser(departmentManager);

		// setting the department's parks
		parks = departmentManager.getResponsible();
		choiceBoxPark.getItems().clear();
		choiceBoxPark.getItems().add("All Parks"); // Add option for all parks
		for (Park park : parks) {
			choiceBoxPark.getItems().add(park.getParkName());
		}

		// setting months and years choice boxes
		setMonths();
		choiceBoxMonth.getItems().addAll(months.values());

		List<String> years = new ArrayList<>();
		for (LocalDate start = LocalDate.of(2021, 01, 01); start.compareTo(LocalDate.now()) <= 0; start = start
				.plusYears(1)) {
			years.add(start.getYear() + "");
		}
		choiceBoxYear.getItems().addAll(years);

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		vbox1.setAlignment(Pos.CENTER);
		vbox2.setAlignment(Pos.CENTER);
		vbox3.setAlignment(Pos.CENTER);
		hbox.setAlignment(Pos.CENTER);

		// setting the radio buttons toggle group
		radioVisit.setToggleGroup(group);
		radioVisit.setUserData("visit");
		radioUsage.setToggleGroup(group);
		radioUsage.setUserData("usage");
		radioCancel.setToggleGroup(group);
		radioCancel.setUserData("cancel");
		radioTotal.setToggleGroup(group);
		radioTotal.setUserData("total");

		// setting title and image
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.setText(departmentManager.getManagesDepartment() + " Departmental Reports");

		// setting the application's background
		setApplicationBackground(pane);
	}

	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@Override
	public void loadBefore(Object information) {

	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return "Departmental Reports";
	}

}
