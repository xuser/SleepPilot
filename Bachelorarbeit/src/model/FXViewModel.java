package model;

import view.FXApplicationController;

public class FXViewModel {
	
	private boolean hypnogrammActive = false;
	
	private boolean evaluationWindowActive = false;
	
	private boolean scatterPlotActive = false;
	
	private FXApplicationController appController;

	/**
	 * @return the hypnogrammActive
	 */
	public boolean isHypnogrammActive() {
		return hypnogrammActive;
	}

	/**
	 * @param hypnogrammActive the hypnogrammActive to set
	 */
	public void setHypnogrammActive(boolean hypnogrammActive) {
		this.hypnogrammActive = hypnogrammActive;
	}

	/**
	 * @return the appController
	 */
	public FXApplicationController getAppController() {
		return appController;
	}

	/**
	 * @param appController the appController to set
	 */
	public void setAppController(FXApplicationController appController) {
		this.appController = appController;
	}

	/**
	 * @return the evaluationWindowActive
	 */
	public boolean isEvaluationWindowActive() {
		return evaluationWindowActive;
	}

	/**
	 * @param evaluationWindowActive the evaluationWindowActive to set
	 */
	public void setEvaluationWindowActive(boolean evaluationWindowActive) {
		this.evaluationWindowActive = evaluationWindowActive;
	}

	/**
	 * @return the scatterPlotActive
	 */
	public boolean isScatterPlotActive() {
		return scatterPlotActive;
	}

	/**
	 * @param scatterPlotActive the scatterPlotActive to set
	 */
	public void setScatterPlotActive(boolean scatterPlotActive) {
		this.scatterPlotActive = scatterPlotActive;
	}

}
