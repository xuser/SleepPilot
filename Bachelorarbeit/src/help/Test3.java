package help;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class Test3 {
	static double[][] train = new double[1000][];
	
	/**
	 * @return
	 */
	private static svm_model svmTrain() {
		// Create new SVM problem
	    svm_problem prob = new svm_problem();
	    
	    // Length of traning file (number of feature vectors)
	    int dataCount = train.length;
	    
	    // Create array of labels for the feature vector
	    prob.y = new double[dataCount];
	    
	    // Length of vector with the labels
	    prob.l = dataCount;
	    
	    // Create a matrix of SVM Nodes. Each row is one feature vector.
	    prob.x = new svm_node[dataCount][];     
	    
	    // Create SVM Nodes of each feature value
	    for (int i = 0; i < dataCount; i++){            
	        double[] features = train[i];
	        prob.x[i] = new svm_node[features.length-1];
	        for (int j = 1; j < features.length; j++){
	            svm_node node = new svm_node();
	            node.index = j;
	            node.value = features[j];
	            prob.x[i][j-1] = node;
	        }           
	        prob.y[i] = features[0];
	    }               
	    
	    // Create SVM paramters
	    svm_parameter param = new svm_parameter();
	    param.probability = 1;
	    param.gamma = 0.5;
	    param.C = 100;
	    param.svm_type = svm_parameter.C_SVC;
	    param.kernel_type = svm_parameter.RBF;       
	    param.cache_size = 20000;
	    param.eps = 0.001;
	    
	    
	    // START OF CROSSVALIDATION
	    // IMPORTANT: 	Kind of Grid Search for different parameters of C and Gamma have to be done.
	    //				Chose the best values of C and Gamma so that the cross validation accuracy is at his best.
	    double[] target = new double[prob.l];	    
	    svm.svm_cross_validation(prob, param, 10, target);
	    
        int total_correct = 0;
        double accuracy = 0.0;
	    
        for (int i = 0; i < prob.l; i++) {
        	if (target[i] == prob.y[i]) {
        		++total_correct;
            }
        }
        
        accuracy = 100.0 * total_correct / prob.l;
        System.out.print("Cross Validation Accuracy = " + accuracy + "%"+ " C = " + param.C + " g = " + param.gamma + "\n");
        // END OF CROSSVALIDATION

        // Train the SVM and generate the model on which the actual classification have to be done.
	    svm_model model = svm.svm_train(prob, param);

	    return model;

	}
	
	public static double evaluate(double[] features, svm_model model) {	
		
		// Create nodes of given vector which feature values
	    svm_node[] nodes = new svm_node[features.length-1];
	    for (int i = 1; i < features.length; i++)
	    {
	        svm_node node = new svm_node();
	        node.index = i;
	        node.value = features[i];

	        nodes[i-1] = node;
	    }

	    int totalClasses = 2;       
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(model,labels);

	    // Calculate predict probabilities
	    // TODO: Can be used later to display "unsure" epochs
	    double[] prob_estimates = new double[totalClasses];
	    double v = svm.svm_predict_probability(model, nodes, prob_estimates);
	    
	    
	    
	    for (int i = 0; i < totalClasses; i++){
	        System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	    }
	    System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    return v;
	}
	
	
	public static void main(String[] args) {
		
		// Generate training data
		for (int i = 0; i < train.length; i++){
		    if (i+1 > (train.length/2)){        // 50% positive
		        double[] vals = {1,i+i};
		        train[i] = vals;
		    } else {
		        double[] vals = {0,i-i-i-2}; // 50% negative
		        train[i] = vals;
		    }           
		}
		
		svm_model model = svmTrain();
		
		double[] features1 = {1, 1040};
		evaluate(features1, model);
		
		double[] features2 = {0, -10};
		evaluate(features2, model);
		
		double[] features3 = {0, -30};
		evaluate(features3, model);
		
		double[] features4 = {1, 10};
		evaluate(features4, model);
		
		
	}
}
