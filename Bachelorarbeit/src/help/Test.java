package help;
import java.math.BigDecimal;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class Test {

	static double[][] train = new double[1000][]; 
	static double[][] test = new double[10][];

	private static svm_model svmTrain() {
	    svm_problem prob = new svm_problem();
	    int dataCount = train.length;
	    prob.y = new double[dataCount];
	    prob.l = dataCount;
	    prob.x = new svm_node[dataCount][];

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
	    
	    // Creater parameters. These parameters are necessary for classification
	    svm_parameter param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.gamma = 0;	// 1/k
		
		// Further parameters are just important for traning and can be leaved out
		/*
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		*/

		// Do some crossvalidation
	    double[] target = new double[prob.l];	    
	    svm.svm_cross_validation(prob, param, 10, target);
	    
	    // Train and generate model
	    svm_model model = svm.svm_train(prob, param);
    
	    return model;
	}
	
	// svmTrain model has been proved
	
	
	public static double evaluate(double[] features, svm_model model) 
	{
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

	    double[] prob_estimates = new double[totalClasses];
	    double v = svm.svm_predict_probability(model, nodes, prob_estimates);

	    for (int i = 0; i < totalClasses; i++){
	        System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
	    }
	    System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            

	    return v;
	}
	
	public static void main(String[] args) {

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
		
		double[] features1 = {1, 1040, 1043, 1100, 1201};
		System.out.println("v " + evaluate(features1, model));
		
		double[] features2 = {0, -10, -24, -63, -45};
		System.out.println("v " + evaluate(features2, model));
		
		double[] features3 = {0, -30, -52, -63, -48};
		System.out.println("v " + evaluate(features3, model));
		
		double[] features4 = {1, 11, 32, 43, 42};
		System.out.println("v " + evaluate(features4, model));
		
	}

}
