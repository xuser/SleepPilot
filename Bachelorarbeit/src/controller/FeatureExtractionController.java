package controller;

import com.google.common.primitives.Doubles;
import gnu.trove.list.array.TDoubleArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.RawDataModel;
import model.FeatureExtractionModel;
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;
import tools.NeuralNetworks;
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

    private RawDataModel rawDataModel;

    private FeatureExtractionModel featureExtractionModel;

    private boolean dataMatrixCreated = false;

    private boolean trainMode;

    public FeatureExtractionController(RawDataModel rawDataModel, FeatureExtractionModel featureExtractionModel, boolean trainMode) {

        this.rawDataModel = rawDataModel;
        this.featureExtractionModel = featureExtractionModel;
        this.trainMode = trainMode;

    }

    public void start() {

        // design filter
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.01;
            double fpass = 0.3;
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

        //get reference (no deep copy) to data of all epochs
        ArrayList<TDoubleArrayList> epochedDataList = rawDataModel.rawEpochs;

        TDoubleArrayList dataList = new TDoubleArrayList(rawDataModel.getNumberOf30sEpochs() * 3000);
        for (TDoubleArrayList list : epochedDataList) {
            dataList.addAll(list);
        }

        double[] data = dataList.toArray();

        filtfilt(data, coefficients);

        List<float[]> featureList = IntStream.range(0, rawDataModel.getNumberOf30sEpochs())
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

        featureExtractionModel.setFeatures(features);
        Util.save(features, new File("ouput2.jo"));

        NeuralNetworks nn = new NeuralNetworks("D:\\Dropbox\\Dokumente\\MATLAB\\AutoScore\\annDefinition");

        double[] output;
        for (int i = 0; i < features.length; i++) {
            output = nn.net(Util.floatToDouble(features[i]));

            for (int j = 0; j < output.length; j++) {
                output[j] *= 100;
            }

            int classLabel = Doubles.indexOf(output, Doubles.max(output)) + 1;
            featureExtractionModel.setPredictProbabilities(i, output.clone());
            featureExtractionModel.setFeatureClassLabel(i, classLabel);
        }
        featureExtractionModel.setClassificationDone(true);

    }

    public static double[] computeFeatures(double[] x) {
        TDoubleArrayList features = new TDoubleArrayList();

        features.add(Signal.lineSpectralPairs(x, 10));
        features.add(tools.Entropies.wpentropy(x, 6, 1));
        features.add(tools.Entropies.wavewpentropy(x, 6, 1));
        features.add(tools.Entropies.pentropy(x, 6, 1));

        return features.toArray();
    }

}

//        while (supportVectorMaschineThreadStartedFlag == false) {
//
//            if (dataPointsModel.isReadingHeaderComplete()) {
//                double epochs = dataPointsModel.getNumberOf30sEpochs();
//                double calcEpoch = featureExtractionModel.getNumberOfcalculatedEpoch();
//                double progress = calcEpoch / epochs;
//                startController.setProgressBar(progress);
//            }
//        }
