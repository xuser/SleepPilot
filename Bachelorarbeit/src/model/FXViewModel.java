package model;

import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import view.FXApplicationController;

public class FXViewModel {

    private boolean hypnogrammActive = false;

    private boolean evaluationWindowActive = false;

    private boolean scatterPlotActive = false;

    private boolean electrodeConfigurator = false;
    
    private FilterCoefficients displayHighpassCoefficients;
    
    private FilterCoefficients displayLowpasCoefficients;

    private FXApplicationController appController;

    public FXViewModel() {
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            double fs = 100;
            coefficients = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.BUTTERWORTH,
                    FilterType.HIGHPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 20.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        
        setDisplayHighpassCoefficients(coefficients);
        
        FilterCoefficients coefficients2 = null;
        try {
            double fstop = 35;
            double fpass = 25;
            double fs = 100;
            coefficients2 = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.CHEBYSHEV2,
                    FilterType.LOWPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 40.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        
        setDisplayLowpasCoefficients(coefficients2);
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

    public void setDisplayHighpassCoefficients(FilterCoefficients displayHighpassCoefficients) {
        this.displayHighpassCoefficients = displayHighpassCoefficients;
    }

    public void setDisplayLowpasCoefficients(FilterCoefficients displayLowpasCoefficients) {
        this.displayLowpasCoefficients = displayLowpasCoefficients;
    }

    public FilterCoefficients getDisplayHighpassCoefficients() {
        return displayHighpassCoefficients;
    }

    public FilterCoefficients getDisplayLowpasCoefficients() {
        return displayLowpasCoefficients;
    }
    
    

}
