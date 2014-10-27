package model;

import view.FXApplicationController;

public class FXViewModel {

    private boolean hypnogrammActive = false;

    private boolean evaluationWindowActive = false;

    private boolean scatterPlotActive = false;

    private boolean electrodeConfigurator = false;

    private boolean kcMarkersActive = true;

    private boolean filtersActive = true;

    private boolean dcRemoveActive = true;

    private FXApplicationController appController;

    public FXViewModel() {
    }

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

    public boolean isElectrodeConfiguratorActive() {
        return electrodeConfigurator;
    }

    public void setElectrodeConfiguratorActive(boolean electrodeConfiguratorActive) {
        this.electrodeConfigurator = electrodeConfiguratorActive;
    }

    
    public void setKcMarkersActive(boolean kcMarkersActive) {
        this.kcMarkersActive = kcMarkersActive;
    }

    public boolean isKcMarkersActive() {
        return kcMarkersActive;
    }

    public boolean isFiltersActive() {
        return filtersActive;
    }

    public void setFiltersActive(boolean filtersActive) {
        this.filtersActive = filtersActive;
    }

    public void setDcRemoveActive(boolean dcRemoveActive) {
        this.dcRemoveActive = dcRemoveActive;
    }

    public boolean isDcRemoveActive() {
        return dcRemoveActive;
    }

    
}
