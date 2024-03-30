package clientSide.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ReportsController;
import clientSide.entities.ParkManager;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
 * Controller for the Park Manager Report Screen. This class is responsible for
 * handling user interactions on the park manager's report screen.
 */
public class ParkManagerReportScreenController extends AbstractScreen {
	private ReportsController control; // controller
	private ParkManager parkManager;
	private Map<String, String> months = new TreeMap<>();

	/**
	 * Constructor, initializes the park manager controller instance
	 */
	public ParkManagerReportScreenController() {
		control = ReportsController.getInstance();
	}

	//////////////////////////////////
	/// FXML AND JAVAFX COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton, generateButton;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private ChoiceBox<String> choiceBoxMonth, choiceBoxYear;
	@FXML
	private Label titleLbl;
	@FXML
	private HBox hbox;
	@FXML
	private VBox vbox1, vbox2;
	@FXML
	private Pane pane;
	@FXML
	private RadioButton radioUsage, radioTotal;
	private ToggleGroup group = new ToggleGroup();

	//////////////////////////////
	/// EVENT HANDLUNG METHODS ///
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

		if (validate(selectedMonth, selectedYear)) {
			RadioButton selected = (RadioButton) group.getSelectedToggle();
			switch ((String) selected.getUserData()) {
			case "usage":
				usageReport(selectedMonth, selectedYear);
				break;
			case "total":
				totalNumberOfVisitorsReport(selectedMonth, selectedYear);
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
	 * This method is called after an event of clicking on "Total number of visitors
	 * report" button is occurring
	 */
	private void totalNumberOfVisitorsReport(String selectedMonth, String selectedYear) {
		try {
			if (!control.isReportDataAvailable(selectedMonth, selectedYear, parkManager.getParkObject(), "done")) {
				showErrorAlert("No data available for the selected time period.");
				return;
			}
			Pair<Integer, Integer> visitorsData = control.generateTotalNumberOfVisitorsReport(selectedMonth,
					selectedYear, parkManager.getParkObject());
			boolean saveSuccess = control.saveTotalNumberOfVisitorsReport(selectedMonth, selectedYear,
					parkManager.getParkObject());
			ScreenManager.getInstance().showScreen("TotalNumberOfVisitorsReportController",
					"/clientSide/fxml/TotalNumberOfVisitorsReport.fxml", true, false, visitorsData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
			showErrorAlert("An error occurred while generating the report.");
		}

	}

	/**
	 * This method is called after an event of clicking on "Usage report" button is
	 * occurring
	 */
	private void usageReport(String selectedMonth, String selectedYear) {
		try {
			if (!control.isReportDataAvailable(selectedMonth, selectedYear, parkManager.getParkObject(), "done")) {
				showErrorAlert("No data available for the selected time period.");
				return;
			}
			List<Pair<LocalDate, Integer>> usageData = control.generateUsageReport(selectedMonth, selectedYear,
					parkManager.getParkObject());
			boolean saveSuccess = control.saveUsageReport(selectedMonth, selectedYear, parkManager.getParkObject());
			ScreenManager.getInstance().showScreen("UsageReportController", "/clientSide/fxml/UsageReport.fxml", true,
					false, usageData);
		} catch (StatefulException | ScreenException e) {
			e.printStackTrace();
			showErrorAlert("An error occurred while generating the report.");
		}
	}

	/**
	 * This methods validates the user's choices
	 * 
	 * @return true if valid, false if not
	 */
	private boolean validate(String selectedMonth, String selectedYear) {
		choiceBoxMonth.setStyle(setFieldToRegular());
		choiceBoxYear.setStyle(setFieldToRegular());

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

		if (!valid) {
			showErrorAlert(error);
		}

		return valid;
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

	/**
	 * This method is called by the FXML and JAVAFX and initializes the screen
	 */
	@Override
	public void initialize() {
		parkManager = (ParkManager) GoNatureUsersController.getInstance().restoreUser();
		GoNatureUsersController.getInstance().saveUser(parkManager);

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
		hbox.setAlignment(Pos.CENTER);

		// setting the radio buttons toggle group
		radioUsage.setToggleGroup(group);
		radioUsage.setUserData("usage");
		radioTotal.setToggleGroup(group);
		radioTotal.setUserData("total");

		// setting title and image
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.setText(parkManager.getParkObject().getParkName() + " Park Reports");

		// setting the application's background
		setApplicationBackground(pane);
	}

	/**
	 * This method is called in order to set pre-info into the GUI components
	 */
	@Override
	public void loadBefore(Object information) {
		// irrelevant here
	}

	/**
	 * This method returns the screen's name
	 */
	@Override
	public String getScreenTitle() {
		return parkManager.getParkObject().getParkName() + " - Park Reports";
	}
}