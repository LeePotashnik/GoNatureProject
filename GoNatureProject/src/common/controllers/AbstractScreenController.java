package common.controllers;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class AbstractScreenController {
	
	// show an error alert window
	public void showErrorAlert(Stage stage, String content) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.initOwner(stage);
	    alert.setHeaderText(null);
	    alert.setContentText(content);
	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.setStyle("-fx-background-color: #ffdddd; -fx-font-size: 14px;");
	   
	    alert.showAndWait();
	}
	
	// show an information alert window
	public void showInformationAlert(Stage stage, String content) {
	    Alert alert = new Alert(AlertType.INFORMATION);
	    alert.initOwner(stage);
	    alert.setHeaderText(null);
	    alert.setContentText(content);
	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.setStyle("-fx-background-color: #ddfeff; -fx-font-size: 14px;");
	   
	    alert.showAndWait();
	}
	
	// show a confirmation alert window, with buttons to choose from
	public int showConfirmationAlert(Stage stage, String content, String btn1, String btn2) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.initOwner(stage);
	    alert.setHeaderText(null);
	    alert.setContentText(content);
	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.setStyle("-fx-font-size: 14px;");
	    
	    // setting the buttons of the alert
	    ButtonType button1 = new ButtonType(btn1), button2 = new ButtonType(btn2);
	    alert.getButtonTypes().setAll(button1, button2);
	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.isPresent() && result.get() == button1)
	    	return 1; // button 1 clicked
	    else
	    	return 2; // button 2 clicked
	}
	
	// setting a CSS style for indicating an error in a text field input
	public String setTextFieldToError() {
		return "-fx-border-color: red; -fx-border-width: 0.8px; -fx-border-radius: 2px; -fx-background-color: #ffe6e6;";
	}
	
	// setting a CSS style for reversing the error style
	public String setTextFieldToRegular() {
		return "";
//		return "-fx-border-color: null; -fx-border-width: 0px; -fx-background-color: transparent;";
	}
}

