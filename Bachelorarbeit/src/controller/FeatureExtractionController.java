package controller;

import help.MathFunctions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.DataPoints;
import model.FeatureExtraxtionValues;

/**
 * This class calls the feature extraction methods for extracting relevant features to create 
 * feature vectors, which can be used for the support vector maschine.
 * 
 * @author Nils Finke
 *
 */
public class FeatureExtractionController {
	
	private DataPoints respectiveModel;
	
	private FeatureExtraxtionValues respectiveFeatureExtractionModel;
	
	private List<Double> samples = new LinkedList<Double>();
	
	/**
	 * This TreeMap holds all values for the current window.
	 * The TreeMap automatically sorts the map by its key value in log(n) time.
	 */
	private Map<Double, Integer> window = new TreeMap<Double, Integer>();
	
	/**
	 * This HashMap counts for each permutation the number of occurrences in the current set of samples.
	 */
	private HashMap<String, Integer> countPermutations = new HashMap<String, Integer>();
	
	/**
	 * Constructor which initializes the class.
	 */
	public FeatureExtractionController(DataPoints dataPointsModel, FeatureExtraxtionValues featureExtractionModel){
		
		respectiveModel = dataPointsModel;
		respectiveFeatureExtractionModel = featureExtractionModel;
		
		// H = petropy([6,9,11,12,8,13,5],3,1,'order') mit dem Ergebnis H = 1.5219		
		samples.add(6.0);
		samples.add(9.0);
		samples.add(11.0);
		samples.add(12.0);		
		samples.add(8.0);
		samples.add(13.0);
		samples.add(5.0);
		
		System.out.println("Feature Extraction: " + calculatePermutationEntropy(samples, 3, 1));
		
		int numberOfFeatureExtractionValues = respectiveModel.getNumberOf30sEpochs();
		
		if ((numberOfFeatureExtractionValues % 1) != 0) {
			System.err.println("Not enough samples for an exact number of epochs!");
		} else {
			// TODO: Zum testen wird die folgenden Zeile auskommentiert.
			//respectiveFeatureExtractionModel.createDataMatrix(numberOfFeatureExtractionValues, (respectiveModel.getNumberOfChannels() + 1));
			
			respectiveFeatureExtractionModel.createDataMatrix(3, 5);
			
		}
	}
	
	private float calculatePermutationEntropy(List<Double> dataPoints, int orderOfPermutation, int tau){
		
		float permutationEntropy = 0;
		
		float runIndex = (samples.size() - orderOfPermutation + 1);
		float relativeFrequency;
		
		for(int i = 0; i < runIndex; i++) {
			
			for(int y = 0; y < orderOfPermutation; y++) {
				window.put(samples.get(i + y), y);				
			}
			
			String permutaion = window.values().toString();
			
			if (!(countPermutations.containsKey(permutaion))) {
				countPermutations.put(permutaion, 1);
			} else {
				int oldValue = countPermutations.get(permutaion);
				countPermutations.put(permutaion, oldValue + 1);
			}
			
			window.clear();
			
		}
		
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		tmp.addAll(countPermutations.values());
		
		int tmpSize = tmp.size();
		
		for(int x = 0; x < tmpSize; x++) {		
			float a = tmp.poll();
			relativeFrequency = (a / runIndex);	
			
			permutationEntropy = (permutationEntropy + (float) (relativeFrequency * MathFunctions.lb(relativeFrequency)));
		}
		
		permutationEntropy = (permutationEntropy * (-1));
		
		return permutationEntropy;
	}
	

}
