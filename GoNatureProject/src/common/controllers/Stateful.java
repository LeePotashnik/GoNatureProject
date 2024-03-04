package common.controllers;

public interface Stateful {
	/**
	 * For a screen that needs to save its current state for future displaying. For
	 * example: saving the GUI components current data, so returning later to this
	 * screen will show that data (after calling restoreState() method).
	 */
	void saveState();

	/**
	 * For a screen that needs to restore its saved state from past displaying. For
	 * example: restoring the GUI components saved data in saveState() method, so
	 * the screen will show that data.
	 */
	void restoreState();
}
