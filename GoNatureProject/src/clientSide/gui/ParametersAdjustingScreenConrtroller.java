package clientSide.gui;

import clientSide.control.GoNatureUsersController;
import clientSide.control.ParametersController;
import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Park;
import entities.ParkManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ParametersAdjustingScreenConrtroller extends AbstractScreen {
	private ParametersController control; // controller
	private ParkManager parkManager;

	public ParametersAdjustingScreenConrtroller() {
		control = new ParametersController();
		parkManager = (ParkManager) GoNatureUsersController.getInstance().restoreUser();
	}

	//////////////////////////////////
	/// JAVAFX AND FXML COMPONENTS ///
	//////////////////////////////////

	@FXML
	private Button backButton, maximumVisitorsBtn, maximumOrderBtn, timeLimitBtn;
	@FXML
	private ImageView goNatureLogo;
	@FXML
	private TextField maximumVisitorsTxt, maximumOrderTxt, timeLimitTxt;
	@FXML
	private VBox mainVbox, vboxCurrent1, vboxCurrent2, vboxCurrent3;
	@FXML
	private HBox hboxCurrent, hboxUpdate1, hboxUpdate2, hboxUpdate3;
	@FXML
	private Label titleLbl, parkLbl, maximumVisitorsLbl, maximumOrdersLbl, timeLimitLbl, lbl1, lbl2, lbl3;
	@FXML
	private Pane pane;

	//////////////////////////////
	/// EVENT HANDLING METHODS ///
	//////////////////////////////

	@FXML
	/**
	 * When the Send to Approval Button of the maximum visitors is pressed, this
	 * method is called. If there is no existing request to adjust this park's
	 * maximum visitors capacity - will add one to the pending adjustment table (by
	 * calling the method: adjustMaximumVisitorsCapacity)
	 */
	void adjustMaximumVisitorsCapacity(ActionEvent event) {
		maximumVisitorsTxt.setStyle(setFieldToRegular());
		maximumVisitorsLbl.setStyle(setFieldToRegular());

		String maximumVisitorsCapacityAfterChange = maximumVisitorsTxt.getText();
		// validating maximumVisitorsCapacityTextField is not empty
		if (maximumVisitorsCapacityAfterChange.isEmpty()) {
			showErrorAlert("Please enter Maximum Visitors Capacity if you want to update it");
			maximumVisitorsTxt.setStyle(setFieldToError());
		}
		// validating maximumVisitorsCapacityTextField contains only digits
		else if (!maximumVisitorsCapacityAfterChange.matches("\\d+")) {
			showErrorAlert("Please make sure that the Maximum Visitors Capacity number contains only numbers");
			maximumVisitorsTxt.setStyle(setFieldToError());
		} else if (maximumVisitorsCapacityAfterChange.equals(maximumVisitorsLbl.getText())) {
			showErrorAlert("Please make sure the new value is not the same as the current value");
			maximumVisitorsTxt.setStyle(setFieldToError());
			maximumVisitorsLbl.setStyle(setFieldToError());
		} else {
			int maximumVisitorsCapacityAfter = Integer.parseInt(maximumVisitorsCapacityAfterChange);
			if (control.adjustMaximumVisitorsCapacity(parkManager, maximumVisitorsCapacityAfter)) {
				showInformationAlert(
						"Your request to adjust maximum visitor capacity has been successfully sent to department manager");
				maximumVisitorsTxt.setDisable(true);
				maximumVisitorsBtn.setDisable(true);
			} else {
				showErrorAlert(
						"There's still a pending for aproval adjustment. It is not possible to send new requests before it is approved/disapproved");
				maximumVisitorsTxt.setDisable(true);
				maximumVisitorsTxt.setText("");
				maximumVisitorsBtn.setDisable(true);
			}
		}

	}

	@FXML
	/**
	 * When the Send to Approval Button of the maximum orders is pressed, this
	 * method is called. If there is no existing request to adjust this park's
	 * maximum Order Amount - will add one to the pending adjustment table (by
	 * calling the method: adjustMaximumOrdersAmount)
	 */
	void adjustMaximumOrderAmount(ActionEvent event) {
		maximumOrderTxt.setStyle(setFieldToRegular());
		maximumOrdersLbl.setStyle(setFieldToRegular());

		String maximumOrdersAfterChange = maximumOrderTxt.getText();
		// validating maximumVisitorsCapacityTextField is not empty
		if (maximumOrdersAfterChange.isEmpty()) {
			maximumOrderTxt.setStyle(setFieldToError());
			showErrorAlert("Please enter a value in order to update the Maximum Orders Amount parameter");
		}
		// validating maximumVisitorsCapacityTextField contains only digits
		else if (!maximumOrdersAfterChange.matches("\\d+")) {
			maximumOrderTxt.setStyle(setFieldToError());
			showErrorAlert("Please make sure the entered value contains digits only");
		} else if (maximumOrdersAfterChange.equals(maximumOrdersLbl.getText())) {
			showErrorAlert("Please make sure the new value is not the same as the current value");
			maximumOrderTxt.setStyle(setFieldToError());
			maximumOrdersLbl.setStyle(setFieldToError());
		} else {
			int maximumOrdersAmountAfter = Integer.parseInt(maximumOrdersAfterChange);
			if (control.adjustMaximumOrdersAmount(parkManager, maximumOrdersAmountAfter)) {
				showInformationAlert(
						"Your request to adjust maximum order amount has been successfully sent to department manager");
				maximumOrderTxt.setDisable(true);
				maximumOrderBtn.setDisable(true);
			} else {
				showErrorAlert(
						"There's still a pending for aproval adjustment. It is not possible to send new requests before it is approved/disapproved");
				maximumOrderTxt.setDisable(true);
				maximumOrderTxt.setText("");
				maximumOrderBtn.setDisable(true);
			}
		}
	}

	@FXML
	/**
	 * When the Send to Approval Button of the time limit parameter is pressed, this
	 * method is called. If there is no existing request to adjust this park's time
	 * limit - will add one to the pending adjustment table (by calling the method:
	 * adjustMaximumTimeLimit)
	 */
	void adjustMaximumTimeLimits(ActionEvent event) {
		timeLimitTxt.setStyle(setFieldToRegular());
		timeLimitLbl.setStyle(setFieldToRegular());

		String maximumTimeLimitsAfterChange = timeLimitTxt.getText();
		// validating maximumTimeLimitsTextField is not empty :
		if (maximumTimeLimitsAfterChange.isEmpty()) {
			showErrorAlert("Please enter Maximum Time Limits if you want to update it");
			timeLimitTxt.setStyle(setFieldToError());
		}
		// validating maximumTimeLimitsTextField contains only numbers:
		else if (!maximumTimeLimitsAfterChange.matches("\\d+")) {
			showErrorAlert("Please make sure that the Maximum Time Limits number contains only numbers");
			timeLimitTxt.setStyle(setFieldToError());
		} else if (maximumTimeLimitsAfterChange.equals(timeLimitLbl.getText())) {
			showErrorAlert("Please make sure the new value is not the same as the current value");
			timeLimitTxt.setStyle(setFieldToError());
			timeLimitLbl.setStyle(setFieldToError());
		} else {
			int maximumTimeLimitsAfter = Integer.parseInt(maximumTimeLimitsAfterChange);
			if (control.adjustMaximumTimeLimit(parkManager, maximumTimeLimitsAfter)) {
				showInformationAlert(
						"Your request to adjust time limits has been successfully sent to department manager");
				timeLimitTxt.setDisable(true);
				timeLimitBtn.setDisable(true);
			} else {
				showErrorAlert(
						"There's still a pending for aproval adjustment. It is not possible to send new requests before it is approved/disapproved");
				timeLimitTxt.setDisable(true);
				timeLimitTxt.setText("");
				timeLimitBtn.setDisable(true);
			}
		}

	}

	@FXML
	/**
	 * Returns to the previous screen
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

	@FXML
	/**
	 * Sets the focus to the pain when clicked
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

	private void setLabels() {
		Park park = parkManager.getParkObject();
		maximumVisitorsLbl.setText(park.getMaximumVisitors() + "");
		maximumVisitorsLbl.setAlignment(Pos.CENTER);
		maximumOrdersLbl.setText(park.getMaximumOrders() + "");
		maximumOrdersLbl.setAlignment(Pos.CENTER);
		timeLimitLbl.setText(park.getTimeLimit() + "");
		timeLimitLbl.setAlignment(Pos.CENTER);
		parkLbl.setText("Of " + park.getParkName() + " Park");
		parkLbl.setAlignment(Pos.CENTER);
		lbl1.setAlignment(Pos.CENTER);
		lbl2.setAlignment(Pos.CENTER);
		lbl3.setAlignment(Pos.CENTER);
	}

	///////////////////////////////
	/// ABSTRACT SCREEN METHODS ///
	///////////////////////////////

	@Override
	public void initialize() {
		// setting the logo image
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));

		// setting the back button image
		ImageView backImage = new ImageView(new Image(getClass().getResourceAsStream("/backButtonImage.png")));
		backImage.setFitHeight(30);
		backImage.setFitWidth(30);
		backImage.setPreserveRatio(true);
		backButton.setGraphic(backImage);
		backButton.setPadding(new Insets(1, 1, 1, 1));

		// centering the components of the VBox
		mainVbox.setAlignment(Pos.CENTER);
		vboxCurrent1.setAlignment(Pos.CENTER);
		vboxCurrent2.setAlignment(Pos.CENTER);
		vboxCurrent3.setAlignment(Pos.CENTER);
		hboxCurrent.setAlignment(Pos.CENTER);
		hboxUpdate1.setAlignment(Pos.CENTER);
		hboxUpdate2.setAlignment(Pos.CENTER);
		hboxUpdate3.setAlignment(Pos.CENTER);

		setLabels();
	}

	@Override
	public void loadBefore(Object information) {
		// irrelevant here
	}

	@Override
	public String getScreenTitle() {
		return "Parameters Adjusting";
	}
}