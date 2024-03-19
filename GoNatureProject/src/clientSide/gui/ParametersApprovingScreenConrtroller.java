package clientSide.gui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.time.LocalDate;
import java.time.LocalTime;

import clientSide.control.BookingController;
import clientSide.control.ParametersController;
import clientSide.control.ParkController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenManager;
import entities.Booking;
import entities.DepartmentManager;
import entities.Park;
import entities.ParkManager;
import entities.PendingAdjustment;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ParametersApprovingScreenConrtroller extends AbstractScreen
{
	
	private ParametersController control; // controller
	private DepartmentManager departmentManager;
	ObservableList<PendingAdjustment> pendingAdjustmentList = FXCollections.observableArrayList();


	@FXML
	private Pane pane;
	
	@FXML
	private ImageView goNatureLogo;

	@FXML
	private Label titleLbl;


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
	private Button returnToAccountBtn;

	@FXML
	private Button backButton;

	@FXML
	void paneClicked(MouseEvent event)
	{

	}

	@FXML
	
	void returnToAccount(ActionEvent event) 
	{

	}

	@FXML
	void returnToPreviousScreen(ActionEvent event) 
	{

	}
	@Override
	public void initialize() 
	{
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));		
		//setting the back button image:
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));
	}

	@Override
	public void loadBefore(Object information)
	{
		if (information instanceof DepartmentManager) 
		{
			departmentManager = (DepartmentManager) information;
		// getting the pending adjustment list of the departmentManager department
			pendingAdjustmentList = control.getParameterAdjustmentListForDepartment(departmentManager);

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
		//pendingAdjustmentTable.getSortOrder().add(waitingOrderColumn);

	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
