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

import com.google.common.primitives.Doubles;
import java.io.File;
import java.util.ArrayList;
import libsvm.*;
import model.FeatureModel;
import tools.Util;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 *
 * @author Arne Weigenand
 * @author Nils Finke
 */
public class ClassificationController {

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
    private void svmLoadModel(String modelName) {
        model = (svm_model) Util.load(modelName);

        //initialize array of labels
        labels = new double[svm.svm_get_nr_class(model)];

        // Create a matrix of SVM Nodes. Each row is one feature vector.
        nodes = new svm_node[model.SV[0].length];
    }
    
    public static ArrayList<String> getClassifiers(File pathToClassifiers){
        ArrayList<String> classifierList = new ArrayList();
        File folder = pathToClassifiers.getAbsoluteFile();
        for (File file : folder.listFiles()) {
            if (file.getName().contains("model")) {
                classifierList.add(file.getAbsolutePath());
            }
        }
        return classifierList;
    }

    private double[] svmPredict(double[] features) {
        int nFeatures = features.length;

        // Create a matrix of SVM nodes. Each row is one feature vector.
        for (int j = 0; j < nFeatures; j++) {
            svm_node node = new svm_node();
            node.index = j;
            node.value = features[j];
            nodes[j] = node;
        }

        svm.svm_predict_probability(model, nodes, labels);

        return labels.clone();
    }

    public static void classify(String classifier, FeatureModel featureModel) {
        
        //scaling of features
        float[][] features_scaled = Util.deepcopy(featureModel.getFeatures());
        double[][] scaling = (double[][]) Util.load(classifier.replace("[model]", "[scaling]"));

        double[] mean = scaling[0];
        double[] std = scaling[1];
        for (float[] feature : features_scaled) {
            for (int j = 0; j < feature.length; j++) {
                feature[j] = (float) ((feature[j] - mean[j]) / std[j]);
            }
        }

        //load classifier 1 for arousals
        ClassificationController svmArousal = new ClassificationController();
        svmArousal.svmLoadModel(classifier.replace("[model]", "[arousal]"));
                
        //load classifier 2 for sleep stages
        ClassificationController svm = new ClassificationController();
        svm.svmLoadModel(classifier);
        
        //load indices indicating the features of the feature vector of classifier 2
        String dir = System.getProperty("user.dir");
        String filename = "sparse_features.jo";
        String fullPath = dir + File.separator + "Classifiers" + File.separator + filename;
        double[] fidx = (double[]) Util.load(fullPath);

        
        float[] kcPercentage = featureModel.getKcPercentage();

        double[] output;

        int N1flag = 0;
        int Wflag = 0;

        for (int i = 0; i < featureModel.getNumberOfEpochs(); i++) {
            //get unscaled features
            float[] features_unscaled = featureModel.getFeatureVector(i);

            //use only subset of feature vector as stored in sparse_features.jo array
            float[] f1 = new float[fidx.length];
            for (int j = 0; j < f1.length; j++) {
                //use scaled features here
                f1[j] = features_scaled[i][(int) (fidx[j] - 1)];
            }
              
            output = svm.svmPredict(Util.floatToDouble(f1));
            int classLabel = Doubles.indexOf(output, Doubles.max(output));

            double W = features_unscaled[18];
            double N2 = features_unscaled[1];  //(skewness)
            double N3 = features_unscaled[49]; //cepstrum
            double MA1 = features_unscaled[3]; //max value of high passed epoch
            double MA2 = features_unscaled[9]; //energy in HF wavelet band
            double KC = kcPercentage[i];

            //hand selected features for arousals
            boolean MA3 = MA1 > 40 & MA2 > 0.4;
            
            double[] MAtmp =  svmArousal.svmPredict(Util.floatToDouble(features_scaled[i]));
            boolean MA = (MAtmp[1] > MAtmp[0]) | MA3;
            

            //convert SVM labels to SleepPilot labels (4 is REM)
            if (classLabel == 4) {
                classLabel = 5;
            }

            if (classLabel == 5 & MA) {
                classLabel = 1;
            }

            if (classLabel == 2 & KC == 0 & MA) {
                classLabel = 1;
            }

            //adjust classifier output to be consistent with SleepPilots K-complex detector
            if (classLabel == 3 & KC <= 20) {
                classLabel = 2;
            }
            if (classLabel == 2 & KC > 20 & !MA) {
                classLabel = 3;
            }

            //set artefacts and arousals to zero (standard)
            featureModel.setArtefact(i, 0);
            featureModel.setArousal(i, 0);

            if (classLabel != 0 & MA) {
                featureModel.setArtefact(i, 1);
                featureModel.setArousal(i, 1);
            }
            if (classLabel != 0 & MA1 > 80) {
                featureModel.setArtefact(i, 1);
            }

            //REM is always after N3, at least in non-pathological cases
            if (classLabel == 3) {
                N1flag += 1;
            }

            if (classLabel == 5 & N1flag < 2) {
                classLabel = 1;
            }

            featureModel.setPredictProbabilities(i, output.clone());
            featureModel.setLabel(i, classLabel);
            System.out.println("Predicted Class Label: " + classLabel);

        }

        featureModel.setClassificationDone(true);
    }
}
