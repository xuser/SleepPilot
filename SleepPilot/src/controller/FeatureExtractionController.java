package controller;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
import com.google.common.primitives.Doubles;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.FeatureExtractionModel;
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import tools.KCdetection;
import tools.Signal;
import static tools.Signal.filtfilt;
import tools.Util;

/**
 * This class calls the feature extraction methods for extracting relevant
 * features to create feature vectors, which can be used for the support vector
 * maschine.
 *
 * @author Nils Finke
 *
 */
public class FeatureExtractionController {
    private FeatureExtractionModel featureExtractionModel;

    public FeatureExtractionController(FeatureExtractionModel featureExtractionModel) {
        this.featureExtractionModel = featureExtractionModel;

        createFilters();
        featureExtractionModel.setKCdetector(new KCdetection());
    }

    public void start() {
        double[] data = featureExtractionModel.getData();
//        ArrayList<double[]> testl = new ArrayList();
//        testl.stream()
//                .parallel();
        
        float[][] features = computeFeatures(data, featureExtractionModel.getNumberOfEpochs());

        featureExtractionModel.setFeatures(features);
        featureExtractionModel.setFeaturesComputed(true);
    }

    /**
     *
     * @param data
     * @param epochLength length of epochs in data points (i.e. samples, not
     * seconds)
     * @return
     */
    public float[][] computeFeatures(double[] data, int epochLength) {
        /**
         * generate sublists from data array (which contains all data of a
         * channel. This way data can stay in array form and efficiently be
         * processed in parallel without copying.
         */
        List<float[]> featureList = IntStream.range(0, epochLength)
                .parallel()
                .mapToObj(i
                        -> Doubles.asList(data).subList(
                                (int) i * 3000, (int) (i + 1) * 3000)
                )
                .map(e -> Util.doubleToFloat(computeFeatures(Doubles.toArray(e))))
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

        filtfilt(x, featureExtractionModel.getDisplayHighpassCoefficients());

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

    public double[] decimate(double[] x, int r) {
        Signal.filtfilt(x, featureExtractionModel.getDecimationCoefficients());
        double[] y = new double[x.length / r];
        for (int i = 0; i < y.length; i++) {
            y[i] = x[i * r];
        }
        return y;
    }

    private void createFilters() {
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            double fs = 100;
            coefficients = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureExtractionModel.setDisplayHighpassCoefficients(coefficients);
        FilterCoefficients coefficients2 = null;
        try {
            double fstop = 45;
            double fpass = 40;
            double fs = 100;
            coefficients2 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 40.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureExtractionModel.setDisplayLowpasCoefficients(coefficients2);
        FilterCoefficients coefficients4 = null;
        try {
            double fstop = 0.1;
            double fpass = 0.3;
            double fs = 100;
            coefficients4 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH, FilterType.HIGHPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureExtractionModel.setHighpassCoefficients(coefficients4);
        FilterCoefficients coefficients3 = null;
        try {
            double fstop = 7.0;
            double fpass = 4.0;
            double fs = 100;
            coefficients3 = IIRDesigner.designDigitalFilter(ApproximationFunctionType.CHEBYSHEV2, FilterType.LOWPASS, new double[]{fpass}, new double[]{fstop}, 1.0, 40.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        featureExtractionModel.setLowpassCoefficients(coefficients3);

        IirFilterCoefficients coeffs = IirFilterDesignFisher.design(FilterPassType.lowpass, FilterCharacteristicsType.chebyshev, 8, -1, 0.4, 0.0);
        featureExtractionModel.setDecimationCoefficients(coeffs);
    }

    public double[] resample(double[] x, int fs) {
        int factor = (int) Math.round(Math.ceil(fs / 100.0));
        double scale = Math.round(Math.ceil(fs / 100.0)) / (fs / 100.0);
        double[] y = null;
        if (scale != 1.0) {
            y = Signal.upsample(x, scale);
        } else {
            y = x;
        }
        return decimate(y, factor);
    }

}
