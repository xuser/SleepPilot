package controller;

import java.util.ArrayList;
import model.FeatureModel;
import org.apache.commons.math3.util.FastMath;
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import tools.KCdetection;
import tools.Signal;
import tools.sincWindowDecimator;

/**
 * This class calls the feature extraction methods for extracting relevant
 * features to create feature vectors, which can be used for the support vector
 * maschine.
 *
 * @author Arne Weigenand
 * @author Nils Finke
 *
 */
public class FeatureController {

    private final FeatureModel featureModel;
    public KCdetection kcDetector;
    private sincWindowDecimator decimator;
    public FilterCoefficients highpassCoefficients;
    public FilterCoefficients lowpassCoefficients;
    public FilterCoefficients displayHighpassCoefficients;
    public FilterCoefficients displayLowpassCoefficients;
    /**
     * Data holds reference to feature channel epochList (usually in a RawDataModel), for use in instance of
    FeatureExtractioController.
     */
    private ArrayList<float[]> epochList;

    public FeatureController(FeatureModel featureModel) {
        this.featureModel = featureModel;

        createFilters(100.);

        setDecimator(new sincWindowDecimator());
        sincWindowDecimator decimator = getDecimator();
        decimator.designFilter((int) FastMath.round(featureModel.getSrate() / 100.), 31);

        kcDetector = new KCdetection();
        kcDetector.setMaxWidth(90);
        kcDetector.setMinWidth(20);
        kcDetector.setPeakToPeak(70);
    }

    public void start() {
        ArrayList<float[]> data = getEpochList();
        if ((int) (featureModel.getSrate()) != (int) 100) {
            for (int i = 0; i < data.size(); i++) {
                data.set(i, getDecimator().decimate(data.get(i)));
            }
        }

        //KCdetection is not thread safe!!!
        float[] kcPercentage = new float[featureModel.getNumberOfEpochs()];
        
        for (int i = 0; i < data.size(); i++) {
            kcDetector.detect(data.get(i));
            kcPercentage[i] = (float) kcDetector.getPercentageSum();
        }
        featureModel.setKcPercentage(kcPercentage);

        float[][] features = Signal.computeFeatures(data);

        featureModel.setFeatures(features);
        
    }

    private void createFilters(double fs) {
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            coefficients = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 80.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        setDisplayHighpassCoefficients(coefficients);

        FilterCoefficients coefficients2 = null;
        try {
            double fstop = 48;
            double fpass = 40;
            coefficients2 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        setDisplayLowpassCoefficients(coefficients2);

        FilterCoefficients coefficients4 = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            coefficients4 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        setHighpassCoefficients(coefficients4);

        FilterCoefficients coefficients3 = null;
        try {
            double fstop = 7.0;
            double fpass = 4.0;
            coefficients3 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 40.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        setLowpassCoefficients(coefficients3);
    }

    public sincWindowDecimator getDecimator() {
        return decimator;
    }

    public void setDecimator(sincWindowDecimator decimator) {
        this.decimator = decimator;
    }

    public void setLowpassCoefficients(FilterCoefficients lowpassCoefficients) {
        this.lowpassCoefficients = lowpassCoefficients;
    }

    public void setDisplayLowpassCoefficients(FilterCoefficients displayLowpasCoefficients) {
        this.displayLowpassCoefficients = displayLowpasCoefficients;
    }

    public void setHighpassCoefficients(FilterCoefficients highpassCoefficients) {
        this.highpassCoefficients = highpassCoefficients;
    }

    public void setDisplayHighpassCoefficients(FilterCoefficients displayHighpassCoefficients) {
        this.displayHighpassCoefficients = displayHighpassCoefficients;
    }

    public FilterCoefficients getHighpassCoefficients() {
        return highpassCoefficients;
    }

    public FilterCoefficients getDisplayLowpassCoefficients() {
        return displayLowpassCoefficients;
    }

    public FilterCoefficients getLowpassCoefficients() {
        return lowpassCoefficients;
    }

    public FilterCoefficients getDisplayHighpassCoefficients() {
        return displayHighpassCoefficients;
    }

    public ArrayList<float[]> getEpochList() {
        return epochList;
    }

    public void setEpochList(ArrayList<float[]> data) {
        this.epochList = data;
    }

}
