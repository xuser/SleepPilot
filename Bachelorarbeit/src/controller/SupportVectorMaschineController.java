package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;

import help.svm_scale;
import libsvm.*;
import model.FeatureExtractionModel;


/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 * 
 * @author Nils Finke
 */
public class SupportVectorMaschineController extends Thread {
	
	private FeatureExtractionModel respectiveFeatureExtractionModel;
	
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
	public SupportVectorMaschineController(FeatureExtractionModel featureExtractionModel, boolean trainMode) {
		
		respectiveFeatureExtractionModel = featureExtractionModel;
		this.trainMode = trainMode;

	}
	
	/**
	 * Starts the thread. Will exist when the method reaches his end.
	 */
	public void run() {
				
		// 1. Start scalingData
		String store = "range" + respectiveFeatureExtractionModel.getChannelName().toString().replaceAll(" ", "");
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
		    
		    // We set these parameters during the crossvalidation
		    param.gamma = 0.5;
		    param.C = 32768;
		    
		    param.svm_type = svm_parameter.C_SVC;
		    param.kernel_type = svm_parameter.RBF;
		    param.cache_size = 20000;
		    param.eps = 0.001;
		    
		    
		    // *************** START OF CROSSVALIDATION ***************
		    	    
		    // IMPORTANT: 	Kind of Grid Search for different parameters of C and Gamma have to be done.
		    //				Chose the best values of C and Gamma so that the cross validation accuracy is at his best.
		    
		    // These to variables are overwriten each time when the validation accuracy is at his best
		    /*
		    double tmpC = 0;
		    double tmpGamma = 0;
		    double tmpAccuracy = 0; 
		    
		    double c[] = {Math.pow(2, -5), Math.pow(2, 1), Math.pow(2, -3), Math.pow(2, -1), 
		    		 	  Math.pow(2, 3), Math.pow(2, 5),
		    			  Math.pow(2, 7), Math.pow(2, 7), Math.pow(2, 9),
		    			  Math.pow(2, 11), Math.pow(2, 13), Math.pow(2, 15)};
		    
		    double gamma[] = {Math.pow(2, -15), Math.pow(2, -13), Math.pow(2, -11), 
	    			  		  Math.pow(2, -9), Math.pow(2, -7), Math.pow(2, -5),
	    			  		  Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 1),
	    			  		  Math.pow(2, 3)};
		    
		    for (int x = 0; x < c.length; x++) {
		    	for (int y = 0; y < gamma.length; y++) {
		    		
		    		param.C = c[x];
		    		param.gamma = gamma[y];
		    		
		    		double[] target = new double[prob.l];	    
		    		svm.svm_cross_validation(prob, param, 5, target);
		    		
		    		int total_correct = 0;
		    		double accuracy = 0.0;
		    		
		    		for (int i = 0; i < prob.l; i++) {
		    			if (target[i] == prob.y[i]) {
		    				++total_correct;
		    			}
		    		}
		    		
		    		accuracy = 100.0 * total_correct / prob.l;
		    		
		    		if (accuracy > tmpAccuracy) {
		    			tmpC = c[x];
		    			tmpGamma = gamma[y];
		    			tmpAccuracy = accuracy;
		    		}
		    		
		    		String output = "Cross Validation Accuracy = " + accuracy + "%"+ " C = " + param.C + " g = " + param.gamma;    		
					System.out.println(output);
					
					try {
						String name = "Cross" + x + "" + y + ".txt";
						PrintWriter writer = new PrintWriter(name, "UTF-8");
						writer.println(output);
						writer.close();
					} catch (FileNotFoundException | UnsupportedEncodingException e) {
						e.printStackTrace();
					}
		    		
		    	}	    	
		    }
		    
		    // Overwrite the original parameters; 
		    param.C = tmpC;
		    param.gamma = tmpGamma;
		    // We set the probability mode after the crossvalidation, because with this option an additional cross-validation will be performed
		    
	        */
	        // *************** END OF CROSSVALIDATION ***************
		    // Train the SVM and generate the model on which the actual classification have to be done.
		    param.probability = 1;
		    model = svm.svm_train(prob, param);
		    
		    String storeModelName = "model" + respectiveFeatureExtractionModel.getChannelName().toString().replaceAll(" ", "");
		    
		    try {
				svm.svm_save_model(storeModelName, model);
			} catch (IOException e) {
				System.err.println("Error occured during saving the svm model!");
				e.printStackTrace();
			}
		    
		    System.out.println("Finished training of the SVM! C: " + param.C + " Gamma: " + param.gamma);
//		    respectiveFeatureExtractionModel.setClassificationDone(true);
		    
		} else {
			
//			String loadModelName = "model" + respectiveFeatureExtractionModel.getChannelName().toString().replaceAll(" ", "");
			String loadModelName = respectiveFeatureExtractionModel.getSelectedModel().replaceAll(" ", "");
			System.out.println("LoadName: " + loadModelName);
			
			try {
				model = svm.svm_load_model(loadModelName);
			} catch (IOException e) {
				System.err.println("Error occured during loading the svm model!");
				e.printStackTrace();
			}
			
			// **** START OUTPUT ON DESKTOP *****
//			Path target = Paths.get("/Users/Nils/Desktop/Labels.txt");
//			 
//		    Path file = null;
//			try {
//				file = Files.createFile(target);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
			// **** END OUTPUT ON DESKTOP *****
			
			// Run over each feature vector
			for(int i = 0; i < respectiveFeatureExtractionModel.getNumberOfFeatureValues(); i++) {
				
				// This step is needed to delete zero values.
				LinkedList<Double> featureVector = new LinkedList<Double>();
				LinkedList<Integer> indiciesFeatureVector = new LinkedList<Integer>();
				
				// Run over each value from one feature vector
				for(int y = 1; y < (respectiveFeatureExtractionModel.getNumberOfChannels() + 1); y++) {
					
					double tmp = (double) respectiveFeatureExtractionModel.getFeatureValuePE(i, y);
					if (tmp != 0.0) {
						featureVector.add(tmp);
						indiciesFeatureVector.add(y);
					}
				}
								
				// Create array of nodes.
				svm_node[] nodes = new svm_node[featureVector.size()];
				
				int lengthOfFeatureVector = featureVector.size();
			    for (int x = 0; x < lengthOfFeatureVector; x++)
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
			    
			    respectiveFeatureExtractionModel.setPredictProbabilities(i, prob_estimates);
			    respectiveFeatureExtractionModel.setFeatureClassLabel(i, v);
			    
			 // **** START OUTPUT ON DESKTOP *****
//			    String tmp = v + "";
//			    try {
//					Files.write(file, tmp.getBytes(), StandardOpenOption.APPEND);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			 // **** END OUTPUT ON DESKTOP *****
			    
			}
			
			System.out.println("Finished testing!");
		}
		
		respectiveFeatureExtractionModel.setClassificationDone(true);
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
