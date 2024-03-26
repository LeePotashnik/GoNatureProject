package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParametersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.DepartmentManager;
import entities.PendingAdjustment;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class ParametersApprovingScreenConrtroller extends AbstractScreen {

	private ParametersController control; // controller
	private DepartmentManager departmentManager;
	ObservableList<PendingAdjustment> pendingAdjustmentList = FXCollections.observableArrayList();

	/**
	 * Constructor
	 */
	public ParametersApprovingScreenConrtroller() {
		control = new ParametersController();
		departmentManager = (DepartmentManager) GoNatureUsersController.getInstance().restoreUser();
	}

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Pane pane;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private Label titleLbl, infoLbl;
	@FXML
	private TableView<PendingAdjustment> pendingAdjustmentTable;
	@FXML
	private TableColumn<PendingAdjustment, String> parkNameColumn, byColumn, typeColumn;
	@FXML
	private TableColumn<PendingAdjustment, LocalDate> dayColumn;
	@FXML
	private TableColumn<PendingAdjustment, LocalTime> timeColumn;
	@FXML
	private TableColumn<PendingAdjustment, Integer> beforeColumn, afterColumn;
	@FXML
	private Button returnToAccountBtn, backButton;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * Sets the focus to the pane when clicked
	 * 
	 * @param event
	 */
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}

	@FXML
	/**
	 * returns to the previous screen
	 * 
	 * @param event
	 * @throws ScreenException
	 * @throws StatefulException
	 */
	void returnToPreviousScreen(ActionEvent event) {
		try {
			ScreenManager.getInstance().goToPreviousScreen(false, false);
		} catch (ScreenException | StatefulException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when the user double-click on a row of the table view
	 * 
	 * @param chosenParameter
	 */
	private void parameterClicked(PendingAdjustment chosenParameter) {
		String message = "Pending adjustment of " + chosenParameter.getParkName() + " Park:";
		message += "\nParameter type: " + getParameterType(chosenParameter.getParameterType());
		message += "\nBefore: " + chosenParameter.getParameterBefore();
		message += "\nAfter: " + chosenParameter.getParameterAfter();
		message += "\nPlease select: ";
		int choise = showConfirmationAlert(message, Arrays.asList("Disapprove", "Approve"));

		switch (choise) {
		case 1: // disapprove
			pendingAdjustmentList.remove(chosenParameter);
			control.updateParkParameter(chosenParameter, departmentManager, false);
			break;

		case 2: // approve
			pendingAdjustmentList.remove(chosenParameter);
			control.updateParkParameter(chosenParameter, departmentManager, true);
			break;
		}

		pane.requestFocus();
	}

	////////////////////////
	/// INSTANCE METHODS ///
	////////////////////////

	/**
	 * This method sets the table view and its columns
	 */
	private void setTable() {
		parkNameColumn.setCellValueFactory(new PropertyValueFactory<>("parkName"));
		dayColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfAdjusting"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeOfAdjusting"));
		byColumn.setCellValueFactory(new PropertyValueFactory<>("adjustedBy"));
		beforeColumn.setCellValueFactory(new PropertyValueFactory<>("parameterBefore"));
		afterColumn.setCellValueFactory(new PropertyValueFactory<>("parameterAfter"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<>("parameterType"));
		typeColumn.setCellValueFactory(cellData -> {
			PendingAdjustment adjustment = cellData.getValue();
			String type = getParameterType(adjustment.getParameterType());
			return new ReadOnlyStringWrapper(type);
		});

		pendingAdjustmentTable.setItems(pendingAdjustmentList);
		pendingAdjustmentTable.getSortOrder().add(timeColumn);

	}

	private String getParameterType(String parameterType) {
		switch (parameterType) {
		case "maximumVisitorsCapacity":
			return "Visitors Capacity";
		case "maximumOrdersCapacity":
			return "Orders Amount";
		case "maximumTimeLimit":
			return "Time Limit";
		default:
			return "";
		}
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@Override
	/**
	 * Initializes the fxml components
	 */
	public void initialize() {
		// setting title and image
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		titleLbl.setAlignment(Pos.CENTER);
		// setting the back button image:
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		parkNameColumn.setResizable(false);
		dayColumn.setResizable(false);
		timeColumn.setResizable(false);
		byColumn.setResizable(false);
		beforeColumn.setResizable(false);
		afterColumn.setResizable(false);
		typeColumn.setResizable(false);

		// setting what will occur when double-clicking on a row of the table
		pendingAdjustmentTable.setRowFactory(tv -> {
			TableRow<PendingAdjustment> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					PendingAdjustment clickedRowData = row.getItem();
					parameterClicked(clickedRowData);
				}
			});
			return row;
		});

		pendingAdjustmentTable.setPlaceholder(new Label("No Pending Adjustments"));

		// getting the pending adjustment list of the departmentManager department
		pendingAdjustmentList = control.getParameterAdjustmentListForDepartment(departmentManager);

		infoLbl.setText("Of " + departmentManager.getManagesDepartment() + " Department");
		infoLbl.setAlignment(Pos.CENTER);

		// setting the application's background
		setApplicationBackground(pane);

		// now adding the pendingAdjustment list to the table view
		setTable();
	}

	@Override
	public void loadBefore(Object information) {
		// irrelevant here
	}

	@Override
	/**
	 * Returns the screen's title
	 */
	public String getScreenTitle() {
		return "Parameters Approving";
	}
}
