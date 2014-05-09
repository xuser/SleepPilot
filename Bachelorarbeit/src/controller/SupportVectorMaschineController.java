package controller;

import sun.security.jca.GetInstance.Instance;
import libsvm.*;

/**
 * This class starts the support vector maschine and uses the LIBSVM framework.
 * 
 * @author Nils Finke
 */
public class SupportVectorMaschineController {

	/**
	 * This constructor initializes the class.
	 */
	public SupportVectorMaschineController() {
		
		// Setup parameters
		svm_parameter parameter = new svm_parameter();
		
		// Register the kernel function
		parameter.kernel_type = svm_parameter.RBF;
		
		
	}
}
