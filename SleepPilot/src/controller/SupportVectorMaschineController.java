package controller;

import libsvm.*;
import tools.Util;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 *
 * @author Nils Finke
 */
public class SupportVectorMaschineController {

    private double[] labels;
    svm_node[] nodes;
    double[] mean;
    double[] std;

    /**
     * This is the trained model for classification.
     */
    private svm_model model;

    /**
     * Loads the SVM model file. svmLoadModel has to be called before a call to
     * svmPredict.
     *
     * @param modelName
     */
    public void svmLoadModel(String modelName) {
        double[][] scaling = (double[][])Util.load(modelName.replace("[model]", "[scaling]"));
        mean = scaling[0]; std=scaling[1];
        
        
        model = (svm_model)Util.load(modelName);

        //initialize array of labels
        labels = new double[svm.svm_get_nr_class(model)];

        // Create a matrix of SVM Nodes. Each row is one feature vector.
        nodes = new svm_node[model.SV[0].length];
    }

    public double[] svmPredict(double[] features) {
        int nFeatures = features.length;

        // Create a matrix of SVM nodes. Each row is one feature vector.
        for (int j = 0; j < nFeatures; j++) {
            svm_node node = new svm_node();
            node.index = j;
            node.value = (features[j]-mean[j])/std[j];
            nodes[j] = node;
        }

        double out = svm.svm_predict_probability(model, nodes, labels);
        
//        System.out.println(out);
//        System.out.println(Arrays.toString(features));
//        System.out.println(Arrays.toString(labels));
        return labels.clone();
    }
}
//            Task<Void> task = new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames, startModel.isAutoModeFlag());
//
//                        return null;
//                    }
//
//                };
//
//                new Thread(task).start();

//        while (supportVectorMaschineThreadStartedFlag == false) {
//
//            if (dataPointsModel.isReadingHeaderComplete()) {
//                double epochs = dataPointsModel.getNumberOf30sEpochs();
//                double calcEpoch = featureExtractionModel.getNumberOfcalculatedEpoch();
//                double progress = calcEpoch / epochs;
//                startController.setProgressBar(progress);
//            }
//        }