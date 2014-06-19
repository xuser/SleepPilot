package controller;

import java.io.IOException;
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
		
		//Check if thread have to pause.
		synchronized (this) {
			while (fPause) {
				try {
					wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// 1. Start scalingData
		String store = "range";
		
		// Save the ranges to file. Scaling for training data. Be sure that you use the same ranges for testing data later
		if (trainMode == true) {
			svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1, store, null);
		} else {
			svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1, null, store);
		}
		// 2. Register the kernel function		
		// 3. Cross-validation to find best parameter C and Gamma
		// 4. Train with whole training set by using the parameters c and gamma
		// 5. Classify whole testing set by using the trained model
		
		if (trainMode == true) {
			
			// Create new SVM problem
		    svm_problem prob = new svm_problem();
		    
		    // Length of traning file (number of feature vectors)
		    int dataCount = respectiveFeatureExtractionModel.getNumberOfFeatureValues();
		    
		    // Create array of labels for the feature vector
		    prob.y = new double[dataCount];
		    
		    // Length of vector with the labels
		    prob.l = dataCount;
		    
		    // Create a matrix of SVM Nodes. Each row is one feature vector.
		    prob.x = new svm_node[dataCount][]; 
			
		    // Create a matrix of SVM nodes. Each row is one feature vector.
		    for (int i = 0; i < dataCount; i++) {
		    	double[] features = respectiveFeatureExtractionModel.getFeatureVector(i);
		    	prob.x[i] = new svm_node[features.length-1];
		    	
		    	for (int j = 1; j < features.length; j++) {
		    		svm_node node = new svm_node();
		    		node.index = j;
		    		node.value = features[j];
		    		prob.x[i][j-1] = node;
		    	}
		    	
		    	prob.y[i] = features[0];
		    }
		    
		    // Create SVM parameters
		    svm_parameter param = new svm_parameter();
		    param.probability = 1;
		    param.gamma = 0.5;
		    param.C = 100;
		    param.svm_type = svm_parameter.C_SVC;
		    param.kernel_type = svm_parameter.RBF;
		    param.cache_size = 20000;
		    param.eps = 0.001;
		    
		    
		    // *************** START OF CROSSVALIDATION ***************
		    
		    // IMPORTANT: 	Kind of Grid Search for different parameters of C and Gamma have to be done.
		    //				Chose the best values of C and Gamma so that the cross validation accuracy is at his best.
//		    double[] target = new double[prob.l];	    
//		    svm.svm_cross_validation(prob, param, 10, target);
//		    
//	        int total_correct = 0;
//	        double accuracy = 0.0;
//		    
//	        for (int i = 0; i < prob.l; i++) {
//	        	if (target[i] == prob.y[i]) {
//	        		++total_correct;
//	            }
//	        }
//	        
//	        accuracy = 100.0 * total_correct / prob.l;
//	        System.out.print("Cross Validation Accuracy = " + accuracy + "%"+ " C = " + param.C + " g = " + param.gamma + "\n");
	        
	        // *************** END OF CROSSVALIDATION ***************
			
		    // Train the SVM and generate the model on which the actual classification have to be done.
		    model = svm.svm_train(prob, param);
		    
		    try {
				svm.svm_save_model("model", model);
			} catch (IOException e) {
				System.err.println("Error occured during saving the svm model!");
				e.printStackTrace();
			}
		    
		    System.out.println("Finished training of the SVM!");
		    
		} else {
			
			try {
				model = svm.svm_load_model("model");
			} catch (IOException e) {
				System.err.println("Error occured during loading the svm model!");
				e.printStackTrace();
			}
			
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
