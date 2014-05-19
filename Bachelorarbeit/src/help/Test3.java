package help;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class Test3 {
	static double[][] train = new double[1000][]; 

	
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

	    svm_parameter param = new svm_parameter();
	    param.probability = 1;
	    param.gamma = 0.5;
	    param.nu = 0.5;
	    param.C = 1;
	    param.svm_type = svm_parameter.C_SVC;
	    param.kernel_type = svm_parameter.RBF;       
	    param.cache_size = 20000;
	    param.eps = 0.001;
	    
	    double[] target = new double[prob.l];	    
	    svm.svm_cross_validation(prob, param, 10, target);

	    svm_model model = svm.svm_train(prob, param);
	    System.out.println("C: " + model.param.C);

	    return model;
	}
	
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
