package common.controllers;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This class determines the settings of a stage, so before it shown on the
 * screen, these settings applied.
 */
public class StageSettings {
	private String title;
	private boolean resizable;
	private String iconPath;

	/**
	 * @param title     the title of the stage (shown on the top-left side of the
	 *                  window)
	 * @param resizable if the stage can be resizable
	 * @param icon      a path to an icon to be shown on the window (null for
	 *                  nothing)
	 */
	public StageSettings(String title, boolean resizable, String iconPath) {
		this.title = title;
		this.resizable = resizable;
		this.iconPath = iconPath;
	}

	/**
	 * @param title the title of the stage (shown on the top-left side of the
	 *              window)
	 * @return an instance of the stage default settings
	 */
	public static StageSettings defaultSettings(String title) {
		return new StageSettings(title, false, "/GoNatureSquareLogo.png");
	}

	/**
	 * This method implements the requested settings on the stage parameter, before
	 * it is shown on the screen.
	 * 
	 * @param settings the StageSettings object with the relevant settings
	 * @param stage    the stage to perform the settings implementation on
	 */
	public void implementSettings(StageSettings settings, Stage stage) {
		stage.setTitle(settings.title);
		stage.setResizable(settings.resizable);
		if (settings.iconPath != null)
			stage.getIcons().add(new Image(settings.iconPath));
	}
}
