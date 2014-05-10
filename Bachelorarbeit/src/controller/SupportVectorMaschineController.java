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
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 1, -2.3321208e-02F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 2, -2.3527015e-02F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 3, -2.2061187e-02F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 4, -2.2272816e-02F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(0, 5, 18.7079211e-02F);
		
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 0, 2F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 1, -1.3642082e-01F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 2, -9.2407016e-02F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 3, -1.3176912e-01F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 4, 5.7479941e-01F);
		respectiveFeatureExtractionModel.setFeatureValuesPE(1, 5, -37.3130493e-02F);
		
		// Setup parameters
		svm_parameter parameter = new svm_parameter();
		
		// Register the kernel function
		parameter.kernel_type = svm_parameter.RBF;
		
		// TODO: Start scalingData
		svm_scale scalingData = new svm_scale(respectiveFeatureExtractionModel, -1, 1);
		
//		System.out.println(respectiveFeatureExtractionModel.getFeatureValuePE());
		
	}
}
