package controller;

import help.MathFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class calls the feature extraction methods for extracting relevant features to create 
 * feature vectors, which can be used for the support vector maschine.
 * 
 * @author Nils Finke
 *
 */
public class FeatureExtractionController {
	
	private List<Double> samples = new LinkedList<Double>();
	
	/**
	 * This TreeMap holds all values for the current window.
	 * The TreeMap automatically sorts the map by its key value in log(n) time.
	 */
	private TreeMap<Double, Integer> window = new TreeMap<Double, Integer>();
	
	/**
	 * This HashMap counts for each permutation the number of occurrences in the current set of samples.
	 */
	private HashMap<String, Integer> countPermutations = new HashMap<String, Integer>();
	
	/**
	 * Constructor which initializes the class.
	 */
	public FeatureExtractionController(){
		
		// H = petropy([6,9,11,12,8,13,5],3,1,'order') mit dem Ergebnis H = 1.5219
		
//		samples.add(6.0);
//		samples.add(9.0);
//		samples.add(11.0);
//		samples.add(12.0);		
//		samples.add(8.0);
//		samples.add(13.0);
//		samples.add(5.0);

		samples.add(4.0);
		samples.add(7.0);
		samples.add(9.0);
		samples.add(10.0);
		samples.add(6.0);
		samples.add(11.0);
		samples.add(3.0);
		
		// Über die Liste laufen und die eigentlichen Samples als Keys einsortieren, sodas dann basierend
		// auf den Keys der Value, welcher die Position in der Liste kennzeichnet, herausgefunden werden kann.
		// window.put(10.0, 0);
		// window.put(8.0, 1);
		// window.put(12.0, 2);
	
		// System.out.println(window.values().toString());
		// System.out.println(window.toString());
		
		System.out.println(calculatePermutationEntropy(samples, 2, 1));
	}
	
	private double calculatePermutationEntropy(List<Double> dataPoints, int orderOfPermutation, int tau){
		
		double permutationEntropy = 0;
		
		int runIndex = (samples.size() - orderOfPermutation + 1);
		double relativeFrequency;
		
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
		
		List<Integer> tmp = new LinkedList<Integer>();
		tmp.addAll(countPermutations.values());
		
		for(int x = 0; x < tmp.size(); x++) {			
			relativeFrequency = ( tmp.get(x) / runIndex);	
			
			permutationEntropy = permutationEntropy + (relativeFrequency * MathFunctions.lb(relativeFrequency));
			
		}
		
		permutationEntropy = (permutationEntropy * (-1));

		
		return permutationEntropy;
	}
	

}
