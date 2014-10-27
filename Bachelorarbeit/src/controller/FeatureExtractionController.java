package controller;

import com.google.common.primitives.Doubles;
import gnu.trove.list.array.TDoubleArrayList;
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
import tools.Math2;
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

    private RawDataModel dataPointsModel;

    private FeatureExtractionModel featureExtractionModel;

    public FeatureExtractionController(RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel) {

        this.dataPointsModel = dataPointsModel;
        this.featureExtractionModel = featureExtractionModel;
    }

    public void start() {

        double[] data = dataPointsModel.getFeatureChannelData();

        List<float[]> featureList = IntStream.range(0, dataPointsModel.getNumberOf30sEpochs())
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
        featureExtractionModel.setFeaturesComputed(true);

    }

    public static double[] computeFeatures(double[] x) {
        TDoubleArrayList features = new TDoubleArrayList();

        Math2.zscore(x);
        features.add(Signal.lineSpectralPairs(x, 10));
        features.add(tools.Entropies.wpentropy(x, 6, 1));
        features.add(tools.Entropies.wavewpentropy(x, 6, 1));
        features.add(tools.Entropies.pentropy(x, 6, 1));
        features.add(tools.Entropies.wavepentropy(x, 6, 1));

        return features.toArray();
    }

    public static double[] assembleData(ArrayList<TDoubleArrayList> epochedDataList, int capacity) {
        //assambled data of all epochs into one array

        TDoubleArrayList dataList = new TDoubleArrayList(capacity);
        for (TDoubleArrayList list : epochedDataList) {
            dataList.addAll(list);
        }

        return dataList.toArray();
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
