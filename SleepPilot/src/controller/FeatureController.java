/*
 * Copyright (C) 2016 Arne Weigenand
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package controller;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;
import gnu.trove.list.array.TDoubleArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import model.DataModel;
import model.FeatureModel;
import org.apache.commons.math.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import tools.Entropies;
import tools.KCdetection;
import tools.Math2;
import tools.Signal;
import static tools.Signal.filtfilt;
import tools.Util;
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
    private DataModel dataModel;
    public KCdetection kcDetector;
    private sincWindowDecimator decimator;
    public FilterCoefficients highpassCoefficients;
    public FilterCoefficients lowpassCoefficients;
    public FilterCoefficients displayHighpassCoefficients;
    public FilterCoefficients displayLowpassCoefficients;
    
    /**
     * Data holds reference to feature channel epochList (usually in a
     * RawDataModel), for use in instance of FeatureExtractioController.
     */
    private ArrayList<float[]> epochList;

    public FeatureController(FeatureModel featureModel, DataModel dataModel) {
        this.featureModel = featureModel;
        this.dataModel = dataModel;

        if (featureModel.getLabels() == null) {
            init(dataModel.getNumberOf30sEpochs());
        }
        featureModel.setDataFileLocation(dataModel.getFile());

        createFilters(100.);

        setDecimator(new sincWindowDecimator());
        sincWindowDecimator decimator = getDecimator();
        decimator.designFilter((int) FastMath.round(dataModel.getSrate() / 100.), 31);

        kcDetector = new KCdetection();
        kcDetector.setMaxWidth(90);
        kcDetector.setMinWidth(20);
        kcDetector.setPeakToPeak(70);
    }

    public void start() {
        ArrayList<float[]> data = dataModel.getEpochList();
        if ((int) (dataModel.getSrate()) != (int) 100) {
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

        float[][] features = computeFeatures(data);

        featureModel.setFeatures(features);
    }

    public static float[][] computeFeatures(List<float[]> data) {
        /**
         * generate sublists from data array (which contains all data of a
         * channel. This way data can stay in array form and efficiently be
         * processed in parallel without copying.
         */

        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            double fs = 100;
            coefficients = IIRDesigner.designDigitalFilter(ApproximationFunctionType.BUTTERWORTH,
                    FilterType.HIGHPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 20.0, fs);
        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }
        final FilterCoefficients coeffs = coefficients;

        List<float[]> featureList = data.stream()
                .parallel()
                .map(e -> Util.doubleToFloat(computeFeatures(e, coeffs)))
                .collect(Collectors.toList());

        return featureList.toArray(new float[featureList.size()][]);
    }

    public static double[] computeFeatures(float[] y, FilterCoefficients filterCoefficients) {
        double[] x = Util.floatToDouble(y);

        TDoubleArrayList features = new TDoubleArrayList();

        int wlen = 100, noutput = 100;

        float[][] stft = new float[noutput][wlen];

        filtfilt(x, filterCoefficients);

        features.add(FastMath.sqrt(StatUtils.variance(x)));
        Skewness skew = new Skewness();
        features.add(skew.evaluate(x));
        Kurtosis kurt = new Kurtosis();
        features.add(kurt.evaluate(x));

        x = Math2.zscore(x);

        double[] d = Math2.diff(Math2.diff(x));
        features.add(Doubles.max(Math2.abs(d)));

        List<float[]> wdec = Entropies.wavedecomposition(Util.doubleToFloat(x));

        features.add(wdec.stream().mapToDouble(e -> FastMath.log(DoubleMath.mean(Math2.abs(Util.floatToDouble(e))))).toArray());

        features.add(Entropies.wpentropy(Util.doubleToFloat(x), 6, 1));
        features.add(wdec.stream().mapToDouble(e -> Entropies.wpentropy(e, 6, 1)).toArray());

        features.add(Signal.lineSpectralPairs(x, 10));
        features.add(Arrays.copyOf(
                Util.floatToDouble(
                        tools.Signal.logAbsStft(Util.doubleToFloat(x), wlen, noutput, stft)
                ), 10)
        );

        double[] x_pos = new double[x.length];
        double[] x_neg = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_pos[i] = (x[i] >= 0) ? x[i] : 0;
            x_neg[i] = (x[i] <= 0) ? x[i] : 0;
        }

        features.add(Arrays.copyOf(
                Util.floatToDouble(
                        tools.Signal.logAbsStft(Util.doubleToFloat(x_pos), wlen, noutput, stft)
                ), 10)
        );

        features.add(Arrays.copyOf(
                Util.floatToDouble(
                        tools.Signal.logAbsStft(Util.doubleToFloat(x_neg), wlen, noutput, stft)
                ), 10)
        );
        return features.toArray();
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

//    public ArrayList<float[]> getEpochList() {
//        return epochList;
//    }
//
//    public void setEpochList(ArrayList<float[]> data) {
//        this.epochList = data;
//    }

    /**
     * Creates the feature value matrix with the needed size. The first column
     * holds the classified sleep stage. Test mode: The first column has the
     * default value 99.00 Traing mode: The first column has the respective
     * value for the actual sleep stage
     *
     * NOTATION: 1	Wake 2	Sleep stage 1 3	Sleep stage 2 4	Sleep stage 3 5	REM
     * sleep stage 99	Unscored
     */
    public void init(int rows) {
        featureModel.setNumberOfEpochs(rows);
        featureModel.setLabels(new int[rows]);
        Arrays.fill(featureModel.getLabels(), -1);

        featureModel.setArtefacts(new int[rows]);
        featureModel.setArousals(new int[rows]);
        featureModel.setStimulation(new int[rows]);
        featureModel.setPredictProbabilities(new double[rows][]);
        featureModel.setCurrentEpoch(0);
    }

    public void clearProperties(int epoch) {
        featureModel.setLabel(epoch, -1);
        featureModel.setArousal(epoch, 0);
        featureModel.setArtefact(epoch, 0);
        featureModel.setStimulation(epoch, 0);
    }

    public void saveFile(File file) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);

            String content = "Stage" + " "
                    + "Arousal" + " "
                    + "Artefact" + " "
                    + "Stimulation"
                    + "\n";
            fileWriter.write(content);

            for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
                featureModel.getLabel(i);

                content = featureModel.getLabel(i) + " "
                        + featureModel.getArousal(i) + " "
                        + featureModel.getArtefact(i) + " "
                        + featureModel.getStimulation(i) + " "
                        + "\n";
                fileWriter.write(content);
            }

            System.out.println("Finish writing!");
        } catch (IOException ex) {            
//            popUp.createPopup("Could not save Hypnogramm!");
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
