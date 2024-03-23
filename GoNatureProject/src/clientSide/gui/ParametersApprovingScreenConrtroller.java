package clientSide.gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import clientSide.control.ParametersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Booking;
import entities.DepartmentManager;
import entities.ParamenterAdjustment;
import entities.PendingAdjustment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
	
	public ParametersApprovingScreenConrtroller() {
		control = new ParametersController();
	}

	@FXML
	private Pane pane;

	@FXML
	private ImageView goNatureLogo;

	@FXML
	private Label titleLbl,infoLbl;

	@FXML
	private TableView<PendingAdjustment> pendingAdjustmentTable;

	@FXML
	private TableColumn<PendingAdjustment, String> parkNameColumn;

	@FXML
	private TableColumn<PendingAdjustment, LocalDate> dayColumn;

	@FXML
	private TableColumn<PendingAdjustment, LocalTime> timeColumn;

	@FXML
	private TableColumn<PendingAdjustment, String> byColumn;

	@FXML
	private TableColumn<PendingAdjustment, Integer> beforeColumn;

	@FXML
	private TableColumn<PendingAdjustment, Integer> afterColumn;

	@FXML
	private TableColumn<PendingAdjustment, String> typeColumn;

	@FXML
	private Button returnToAccountBtn, backButton;

	@FXML
	void paneClicked(MouseEvent event) {
		pane.requestFocus();
		event.consume();
	}


	@FXML
	void returnToPreviousScreen(ActionEvent event) throws ScreenException, StatefulException {
    	ScreenManager.getInstance().goToPreviousScreen(false, false);
	}
	

	private void parameterClicked(PendingAdjustment chosenParameter) {
		int choise = showConfirmationAlert("Please choose", Arrays.asList("Disapprove", "Approve"));
		
		switch (choise) {
		case 1: // disapprove
			pendingAdjustmentList.remove(chosenParameter);
			control.updateParkParameter(chosenParameter,departmentManager,false);

			break;
		
		case 2: // approve
			pendingAdjustmentList.remove(chosenParameter);
			control.updateParkParameter(chosenParameter,departmentManager,true);
			break;
		}
		pane.requestFocus();
		
	}

	@Override
	public void initialize() {
		////////////////////////////////////////////////////////////////////////////
		//realTime:
		//inserting an instance of ParkManager received from a previous screen, into the field
		//departmentManager = (DepartmentManager)GoNatureUsersController.getInstance().restoreUser();
		////////////////////////////////////////////////////////////////////////////
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
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
		
		pendingAdjustmentTable.setPlaceholder(new Label("No pending adjustments"));

	}

	@Override
	public void loadBefore(Object information) {
		
		
		if (information instanceof DepartmentManager) {
			////////////////////////////////////////////////////////////////////////////////////////
			//temporary:
			departmentManager = (DepartmentManager) information;
			////////////////////////////////////////////////////////////////////////////////////
			// getting the pending adjustment list of the departmentManager department
			pendingAdjustmentList = control.getParameterAdjustmentListForDepartment(departmentManager);

			infoLbl.setText("hello "+departmentManager.getFirstName()+" these are the approval pending adjustments for "+departmentManager.getManagesDepartment()+" department"+":");
			
			// now adding the pendingAdjustment list to the table view
			setTable();
		}

	}

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

		pendingAdjustmentTable.setItems(pendingAdjustmentList);
		// pendingAdjustmentTable.getSortOrder().add(waitingOrderColumn);

	}

	@Override
	public String getScreenTitle() {
		return "Parameters Approving";
	}

}
