package controller;

import java.util.LinkedList;

import help.svm_scale;
import libsvm.*;
import model.FeatureExtraxtionValues;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 * 
 * @author Nils Finke
 */
public class SupportVectorMaschineController extends Thread {
	
	private FeatureExtraxtionValues respectiveFeatureExtractionModel;
	
	private Thread t;
	private boolean fPause = false;
	
	/**
	 * This is the trained model for classification.
	 */
	private svm_model model;
	
	/**
	 * True, if you want to activate the trainigsMode and false, if you want to classify with the current model.
	 */
	private boolean trainMode = false;
	
	/**
	 * This constructor initializes the class.
	 */
	public SupportVectorMaschineController(FeatureExtraxtionValues featureExtractionModel, boolean trainMode) {
		
		respectiveFeatureExtractionModel = featureExtractionModel;
		this.trainMode = trainMode;
		
		// Five Example values for one epoche. Each column represents the feature for one channel.	
		// IMPORTANT:	The first column is important for traning of the SVM. 
		//				This value describes the attachment to the correct sleep stage!
		
		// Code is just for testing
		/*
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 0, 1F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 1, -2.3321208F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 2, -2.3527015F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 3, -2.2061187F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 4, -2.2272816F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 5, 18.7079211F);
		
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 0, 2F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 1, -1.3642082F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 2, -9.2407016F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 3, -1.3176912F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 4, 5.7479941F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 5, -37.3130493F);
		
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 0, 3F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 1, -10.3642082F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 2, -19.2407016F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 3, -12.3176912F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 4, 53.7479941F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(2, 5, -37.3130493F);
		*/
		
	}
	
	/**
	 * Starts the thread. Will exist when the method reaches his end.
	 */
	public void run() {
		
//		//Check if thread have to pause.
//		synchronized (this) {
//			while (fPause) {
//				try {
//					wait();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		// 1. Start scalingData
		String store = "range";
		// Save the ranges to file. Scaling for training data. Be sure that you use the same ranges for testing data later
		svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1, store, null);
		
		// 2. Register the kernel function		
		// 3. Cross-validation to find best parameter C and Gamma
		// 4. Train with whole training set by using the parameters c and gamma
		// 5. Classify whole testing set by using the trained model
		
		if (trainMode == true) {
			
		} else {
			// Run over each feature vector
			for(int i = 0; i < respectiveFeatureExtractionModel.getNumberOfFeatureValues(); i++) {
				
				// This step is needed to delete zero values.
				LinkedList<Double> featureVector = new LinkedList<Double>();
				LinkedList<Integer> indiciesFeatureVector = new LinkedList<Integer>();
				
				// Run over each value from one feature vector
				for(int y = 1; y <= (respectiveFeatureExtractionModel.getNumberOfChannels() + 1); y++) {
					
					double tmp = (double) respectiveFeatureExtractionModel.getFeatureValuePE(i, y);
					if (tmp != 0.0) {
						featureVector.add(tmp);
						indiciesFeatureVector.add(y);
					}
				}
				
				// Create array of nodes.
				svm_node[] nodes = new svm_node[featureVector.size()];
			    for (int x = 0; x < featureVector.size(); x++)
			    {
			        svm_node node = new svm_node();
			        node.index = indiciesFeatureVector.pollFirst();
			        node.value = featureVector.pollFirst();

			        nodes[x] = node;
			    }
			    
			    // Predict probabilities and predict class
			    int totalClasses = 5;
			    int[] labels = new int[totalClasses];
			    svm.svm_get_labels(model,labels);
			    
			    double[] prob_estimates = new double[totalClasses];
			    double v = svm.svm_predict_probability(model, nodes, prob_estimates);
			    
			    respectiveFeatureExtractionModel.setFeatureClassLabel(i, v);
			}
			
		}
		
	}
	
	
	public void start() {
		System.out.println("Starting Support Vector Maschine Thread");
		if (t == null) {
			t = new Thread(this, "SVM");
			t.start();
		}
	}
	
	public void pause() {
		fPause = true;
	}

	public void proceed() {
		fPause = false;
		notify();
	}
}
