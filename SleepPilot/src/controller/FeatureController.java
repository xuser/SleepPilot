package controller;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.FeatureModel;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import tools.KCdetection;
import tools.Signal;
import static tools.Signal.filtfilt;
import tools.Util;
import tools.sincWindowDecimator;

/**
 * This class calls the feature extraction methods for extracting relevant
 * features to create feature vectors, which can be used for the support vector
 * maschine.
 *
 * @author Nils Finke
 *
 */
public class FeatureController {

    private FeatureModel featureModel;

    public FeatureController(FeatureModel featureModel) {
        this.featureModel = featureModel;

        createFilters(featureModel.getSrate());
        featureModel.setDecimator(new sincWindowDecimator());
        sincWindowDecimator decimator = featureModel.getDecimator();
        decimator.designFilter((int)FastMath.round(featureModel.getSrate() / 100.), 31);
        featureModel.setKCdetector(new KCdetection());
    }

    public void start() {
        ArrayList<double[]> data = featureModel.getEpochList();
        float[][] features = computeFeatures(data);

        featureModel.setFeatures(features);
        featureModel.setFeaturesComputed(true);
    }

    /**
     *
     * @param data
     * @return
     */
    public float[][] computeFeatures(ArrayList<double[]> data) {
        /**
         * generate sublists from data array (which contains all data of a
         * channel. This way data can stay in array form and efficiently be
         * processed in parallel without copying.
         */
        List<float[]> featureList = data.stream()
                .parallel()
                .map(e -> Util.doubleToFloat(computeFeatures(e)))
                .collect(Collectors.toList());

        float[][] features = new float[featureList.size()][];
        for (int i = 0; i < featureList.size(); i++) {
            features[i] = featureList.get(i);
        }
        return features;
    }

    public double[] computeFeatures(double[] x) {
        TDoubleArrayList features = new TDoubleArrayList();

        int nOutput = 100, windowSize = 200;
        float[][] stft = new float[nOutput][windowSize];

        if (featureModel.getSrate()!= 100) {
            x=featureModel.getDecimator().decimate(x);
        }
        
        filtfilt(x, featureModel.getHighpassCoefficients());

        Signal.removeDC(x);
        features.add(Signal.lineSpectralPairs(x, 10));
        features.add(Util.floatToDouble(tools.Signal.logAbsStft(Util.doubleToFloat(x), windowSize, nOutput, stft)));

        return features.toArray();
    }

    public static double[] assembleData(ArrayList<double[]> epochedDataList, int capacity) {
        //assamble data of all epochs of a single channel into one array

        TDoubleArrayList dataList = new TDoubleArrayList(capacity);
        for (double[] list : epochedDataList) {
            dataList.addAll(list);
        }

        return dataList.toArray();
    }

    private void createFilters(double fs) {
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            coefficients = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureModel.setDisplayHighpassCoefficients(coefficients);
        
        FilterCoefficients coefficients2 = null;
        try {
            double fstop = 49;
            double fpass = 30;
            coefficients2 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureModel.setDisplayLowpassCoefficients(coefficients2);
        
        FilterCoefficients coefficients4 = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            coefficients4 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureModel.setHighpassCoefficients(coefficients4);
        
        FilterCoefficients coefficients3 = null;
        try {
            double fstop = 7.0;
            double fpass = 4.0;
            coefficients3 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 40.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureModel.setLowpassCoefficients(coefficients3);
    }

    
}
