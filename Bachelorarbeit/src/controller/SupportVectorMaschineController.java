package controller;

import help.svm_scale;
import libsvm.*;
import model.FeatureExtraxtionValues;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 * 
 * @author Nils Finke
 */
public class SupportVectorMaschineController {
	
	private FeatureExtraxtionValues respectiveFeatureExtractionModel;
	
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
		
		
		// Setup parameters
		svm_parameter parameter = new svm_parameter();
		
		// 1. Start scalingData
		String store = "range";
		// Save the ranges to file. Scaling for training data. Be sure that you use the same ranges for testing data later
		
		svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1, store, null);
		
		// 2. Register the kernel function
		parameter.kernel_type = svm_parameter.RBF;
		
		// 3. Cross-validation to find best parameter C and Gamma
		
		// 4. Train with whole training set by using the parameters c and gamma
	
		// 5. Classify whole testing set by using the trained model
	}
}
