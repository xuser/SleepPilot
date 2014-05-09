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
	 * This constructor initializes the class.
	 */
	public SupportVectorMaschineController(FeatureExtraxtionValues featureExtractionModel) {
		
		respectiveFeatureExtractionModel = featureExtractionModel;
		
		// Five Example values for one epoche. Each column represents the feature for one channel.	
		// IMPORTANT:	The first column is important for traning of the SVM. 
		//				This value describes the attachment to the correct sleep stage!
		
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 0, 1F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 1, 1.5432F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 2, -0.8765F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 3, 0.4321F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 4, 1.1234F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 5, 2.7890F);
		
		
		// Setup parameters
		svm_parameter parameter = new svm_parameter();
		
		// Register the kernel function
		parameter.kernel_type = svm_parameter.RBF;
		
		// TODO: Start scalingData
		svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1);
		
		System.out.println(respectiveFeatureExtractionModel.getFeatureValuePE());
		
	}
}
