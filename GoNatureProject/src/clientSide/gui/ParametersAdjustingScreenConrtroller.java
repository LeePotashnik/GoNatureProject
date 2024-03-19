package clientSide.gui;

import clientSide.control.ParametersController;
import common.communication.CommunicationException;
import common.controllers.AbstractScreen;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import entities.Park;
import entities.ParkManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ParametersAdjustingScreenConrtroller extends AbstractScreen
{ 
	private ParkManager parkManager;
	
	@FXML
    private Button backButton;

    @FXML
    private ImageView goNatureLogo;

    @FXML
    private TextField maximumVisitorsCapacityTextField;

    @FXML
    private TextField maximumOrderAmountTextField;

    @FXML
    private Button BackToMainScreenButton;

    @FXML
    private Button AdjustMaximumVisitorsCapacityButton;

    @FXML
    private Button AdjustMaximumOrderAmountButton;

    @FXML
    private TextField maximumTimeLimitTextField;

    @FXML
    private Button AdjustMaximumTimeLimitsButton;

    /**
     *When the Adjust Maximum Order Amount Button is pressed, this method is called. 
     *If there is no existing request to adjust this park's maximum Order Amount - will add one to the pending adjustment table
     *(by calling the method: adjustMaximumOrdersCapacity)
     */
    @FXML
    void adjustMaximumOrderAmount(ActionEvent event)
    {
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	ParkManager parkManager=new ParkManager("151071559","Reese","Moore",
    			"reese.moore@gonature.com","0503868747","Hawaii Volcanoes","reese.moore","Reese8##649112",false);
    	Park park= new Park(9,"Hawaii Volcanoes","Hilo","Hawaii","Pacific","151071559","638683080",100,80,4,0);
    	parkManager.setParkObject(park);
    	/////////////////////////////////////////////////////////////////////////////////////////////////////
    	String maximumOrderAmountAfter1=maximumOrderAmountTextField.getText();
    	maximumOrderAmountTextField.setStyle(setFieldToRegular());
    	// validating maximumVisitorsCapacityTextField is not empty :
    	if(maximumOrderAmountAfter1.isEmpty())
    	{
    		showErrorAlert(ScreenManager.getInstance().getStage(),"Please enter Maximum Order Amount if you want to update it");
    		maximumOrderAmountTextField.setStyle(setFieldToError());
    	}
		// validating maximumVisitorsCapacityTextField contains only numbers:
		else if (!maximumOrderAmountAfter1.matches("\\d+")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"Please make sure that the Maximum Order Amount number contains only numbers");
			maximumOrderAmountTextField.setStyle(setFieldToError());
		}
		else
		{
			int maximumOrdersAmountAfter = Integer.parseInt(maximumOrderAmountAfter1);
			if(ParametersController.adjustMaximumOrdersAmount(parkManager,maximumOrdersAmountAfter))
			{
				showInformationAlert(ScreenManager.getInstance().getStage(),"Your request to adjust maximum order amount has been successfully forwarded to the department manager!");
			}
			else
			{
    			showErrorAlert(ScreenManager.getInstance().getStage(),"It is not possible to create another request to adjust the maximum order amount until the department manager approves/disapproves the previous one");
			}
		}
    }

    
    @FXML
    void adjustMaximumVisitorsCapacity(ActionEvent event)
    {
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	ParkManager parkManager=new ParkManager("151071559","Reese","Moore",
    	"reese.moore@gonature.com","0503868747","Hawaii Volcanoes","reese.moore","Reese8##649112",false);
    	Park park= new Park(9,"Hawaii Volcanoes","Hilo","Hawaii","Pacific","151071559","638683080",100,80,4,0);
    	parkManager.setParkObject(park);
    	/////////////////////////////////////////////////////////////////////////////////////////////////////
    	String maximumVisitorsCapacityAfter1=maximumVisitorsCapacityTextField.getText();
	 	maximumVisitorsCapacityTextField.setStyle(setFieldToRegular());
    	// validating maximumVisitorsCapacityTextField is not empty :
    	if(maximumVisitorsCapacityAfter1.isEmpty())
    	{
    		showErrorAlert(ScreenManager.getInstance().getStage(),"Please enter Maximum Visitors Capacity if you want to update it");
    		maximumVisitorsCapacityTextField.setStyle(setFieldToError());
    	}
		// validating maximumVisitorsCapacityTextField contains only numbers:
		else if (!maximumVisitorsCapacityAfter1.matches("\\d+")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"Please make sure that the Maximum Visitors Capacity number contains only numbers");
			maximumVisitorsCapacityTextField.setStyle(setFieldToError());
		}
		else
		{
			int maximumVisitorsCapacityAfter = Integer.parseInt(maximumVisitorsCapacityAfter1);
			if(ParametersController.adjustMaximumVisitorsCapacity(parkManager,maximumVisitorsCapacityAfter))
			{
				showInformationAlert(ScreenManager.getInstance().getStage(),"Your request to adjust maximum visitors capacity has been successfully forwarded to the department manager!");
			}
			else
			{
    			showErrorAlert(ScreenManager.getInstance().getStage(),"It is not possible to create another request to adjust the maximum visitor capacity until the department manager approves/disapproves the previous one");
			}
		}

    }
    
    @FXML
    void adjustMaximumTimeLimits(ActionEvent event) 
    {
    	
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	ParkManager parkManager=new ParkManager("210396607","Jamie","Moore",
    			"jamie.moore@gonature.com","0546954837","Shenandoah Volcanoes","jamie.moore","Jamie3%0@8768",false);
    	Park park= new Park(13,"Shenandoah","Luray","Virginia","Eastern","210396607","249903204",100,80,4,0);
    	parkManager.setParkObject(park);
    	/////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	
    	
    	String maximumTimeLimitsAfter1=maximumTimeLimitTextField.getText();
    	maximumTimeLimitTextField.setStyle(setFieldToRegular());
    	// validating maximumTimeLimitsTextField is not empty :
    	if(maximumTimeLimitsAfter1.isEmpty())
    	{
    		showErrorAlert(ScreenManager.getInstance().getStage(),"Please enter Maximum Time Limits if you want to update it");
    		maximumTimeLimitTextField.setStyle(setFieldToError());
    	}
		// validating maximumTimeLimitsTextField contains only numbers:
		else if (!maximumTimeLimitsAfter1.matches("\\d+")) {
			showErrorAlert(ScreenManager.getInstance().getStage(),
					"Please make sure that the Maximum Time Limits number contains only numbers");
			maximumTimeLimitTextField.setStyle(setFieldToError());
		}
		else
		{
			int maximumTimeLimitsAfter = Integer.parseInt(maximumTimeLimitsAfter1);
			if(ParametersController.adjustMaximumTimeLimit(parkManager,maximumTimeLimitsAfter))
			{
				showInformationAlert(ScreenManager.getInstance().getStage(),"Your request to adjust maximum Time Limits has been successfully forwarded to the department manager!");
			}
			else
			{
    			showErrorAlert(ScreenManager.getInstance().getStage(),"It is not possible to create another request to adjust the maximum Time Limits until the department manager approves/disapproves the previous one");
			}
		}

    }

 

    @FXML
    void openMainScreen(ActionEvent event) {

    }

    @FXML
    void returnToPreviousScreen(ActionEvent event) {

    }
    
    
    
   // public void setParkManager(ParkManager parkManager)
	public void setParkManager() {
		//this.parkManager = parkManager;

	}
	
	
	


	@Override
	public void initialize() {
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

		//inserting an instance of ParkManager received from a previous screen, into the field
		ParkManager PM = (ParkManager)information;
		//setParkManager(PM);	
	}

	@Override
	public String getScreenTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
