package clientSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StatefulException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class LoadingScreen extends AbstractScreen {

	@FXML
	private ImageView goNatureLogo;
	
	@FXML
	private Label titleLbl;
	
	@FXML
	private Pane pane;

	@Override
	public void initialize() {
		// initializing the image component and centering it
		goNatureLogo.setImage(new Image(getClass().getResourceAsStream("/GoNatureBanner.png")));
		goNatureLogo.layoutXProperty().bind(pane.widthProperty().subtract(goNatureLogo.fitWidthProperty()).divide(2));
		// centering the title label
		titleLbl.setAlignment(Pos.CENTER);
		titleLbl.layoutXProperty().bind(pane.widthProperty().subtract(titleLbl.widthProperty()).divide(2));
		
		// Set up a PauseTransition for 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
        	try {
				ScreenManager.getInstance().goToPreviousScreen(true);
			} catch (ScreenException | StatefulException e1) {
				e1.printStackTrace();
			}
        });
        pause.play();
	}

	@Override
	public void loadBefore(Object information) {
	}

	@Override
	public String getScreenTitle() {
		return "Loading";
	}

}