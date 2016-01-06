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

import libsvm.*;
import tools.Util;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 *
 * @author Arne Weigenand
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
        double[][] scaling = (double[][]) Util.load(modelName.replace("[model]", "[scaling]"));
        mean = scaling[0];
        std = scaling[1];

        model = (svm_model) Util.load(modelName);

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
            node.value = features[j];
            nodes[j] = node;
        }

        svm.svm_predict_probability(model, nodes, labels);

        return labels.clone();
    }
}
