/////////////////////////////
/////////////////////////////
///// A TEMPORARY CLASS /////
/////////////////////////////
/////////////////////////////

package clientSide.gui;

import common.controllers.AbstractScreen;
import common.controllers.ScreenException;
import common.controllers.ScreenManager;
import common.controllers.StageSettings;
import common.controllers.StatefulException;
import entities.Booking;
import entities.ParkVisitor;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.util.Pair;

public class LoadingScreenController extends AbstractScreen {
	private Booking booking;
	private ParkVisitor visitor;
	private Pair<Booking, ParkVisitor> pair;

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
		titleLbl.setText("Payment Screen");
//		 Set up a PauseTransition for 3 seconds
		PauseTransition pause = new PauseTransition(Duration.seconds(3));
		pause.setOnFinished(e -> {
			try {
				ScreenManager.getInstance().showScreen("ConfirmationScreenController",
						"/clientSide/fxml/ConfirmationScreen.fxml", true, false,
						StageSettings.defaultSettings("Confirmation"), pair);
			} catch (StatefulException | ScreenException e1) {
				e1.printStackTrace();
			}
		});
		pause.play();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadBefore(Object information) {
		if (information != null && information instanceof Pair) {
			pair = (Pair<Booking, ParkVisitor>) information;
			booking = pair.getKey();
			visitor = pair.getValue();
		}
	}

	@Override
	public String getScreenTitle() {
		return "Loading";
	}

}