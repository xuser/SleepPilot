package controller;

import help.LPC;
import help.MathFunctions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.DataPoints;
import model.FeatureExtraxtionValues;
import model.TrainDataPoints;

/**
 * This class calls the feature extraction methods for extracting relevant features to create 
 * feature vectors, which can be used for the support vector maschine.
 * 
 * @author Nils Finke
 *
 */
public class FeatureExtractionController extends Thread {
	
	private DataPoints respectiveModel;
	
	private Thread t;
	private boolean fPause = false;
	
	private FeatureExtraxtionValues respectiveFeatureExtractionModel;
	private TrainDataPoints respectiveTrainDataPointsModel;
	
	private boolean trainMode;
	
	private float currentPEValue;
	
	// This List is just for testing. Can be deleted after testphase.
//	private List<Double> samples = new LinkedList<Double>();
	
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
	public FeatureExtractionController(DataPoints dataPointsModel, FeatureExtraxtionValues featureExtractionModel, TrainDataPoints trainModel, boolean trainMode){
		
		respectiveModel = dataPointsModel;
		respectiveFeatureExtractionModel = featureExtractionModel;
		respectiveTrainDataPointsModel = trainModel;
		this.trainMode = trainMode;
		
		// START JUST FOR TESTING
		// TODO: Can be deleted after test phase.
		// H = petropy([6,9,11,12,8,13,5],3,1,'order') mit dem Ergebnis H = 1.5219		
//		samples.add(6.0);
//		samples.add(9.0);
//		samples.add(11.0);
//		samples.add(12.0);		
//		samples.add(8.0);
//		samples.add(13.0);
//		samples.add(5.0);
		// END JUST FOR TESTING
		
	}
	
	public void run() {
		
		if (trainMode == false) {
		
			// Create data matrics in modell to keep the calculated feature values.
			int numberOfFeatureExtractionValues = respectiveModel.getNumberOf30sEpochs();
			
			// 1 Column for the PE of one channel and 11 columns for the LPC coefficients
			respectiveFeatureExtractionModel.createDataMatrix(numberOfFeatureExtractionValues, (1+11));			
			
			// Create instance of LPC class
			LPC lpcExtraction = new LPC(10);
			
			// TODO: Später muss in der GUI hier die Variable gesetzet werden, über welchen Channel die PE berechnet werden soll.
			// Zurzeit wird lediglich über den 0ten Channel iteriert.
			for (int i = 0; i < respectiveModel.getNumberOf30sEpochs(); i++) {
			
				//Check if thread have to pause.
				synchronized (this) {
					while (fPause) {
						try {
							wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				List<Double> samples = respectiveModel.getAllSamplesFromOneEpoche(i, 0, numberOfFeatureExtractionValues);
				
				if (!(samples == null)) {
					
					float tmp = calculatePermutationEntropy(samples, 6, 1);
					
					// Set the PE value into the 1. column and not into the 0. column.
					// For more information see the FeatureExtractionValues.java
					respectiveFeatureExtractionModel.setFeatureValuesPE(i, 1, tmp);
					
					
					// Convert the list of samples to an array
					Double[] epoch = samples.toArray(new Double[samples.size()]);
					
					// Array for the autocorrelation values
					long[] R = new long[11];
					
					LPC.createAutoCorrelation(R, epoch, epoch.length, 0, 1, 10);
					LPC.calculate(lpcExtraction, R);
					
					double[] coefficients = lpcExtraction.getCoefficients();
					
					for(int y = 0; y < coefficients.length; y++){
						
						// Rounded a mantisse with value 4
						BigDecimal myDec = new BigDecimal(coefficients[y]);
						myDec = myDec.setScale(4, BigDecimal.ROUND_HALF_UP);
						
						// Insert in column y+2 because first column if for the class label and second column for the PE
						respectiveFeatureExtractionModel.setFeatureValuesPE(i, y+2, myDec.floatValue());
						
					}
				}
				
				respectiveFeatureExtractionModel.setNumberOfcalculatedEpoch(i+1);
				
			}
			System.out.println("Finished PE and LPC Calculation!!");
		
		} else {
			//********************* START TRAIN MODE *********************
			
			// Create instance of LPC class
			LPC lpcExtraction = new LPC(10);
			
			while (respectiveTrainDataPointsModel.getReadingHasBeenFinishedFlag() == false) {
				
				//Check if thread have to pause.
				synchronized (this) {
					while (fPause) {
						try {
							wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				int currentEpoch = respectiveTrainDataPointsModel.getNumberOfCurrentEpoch();
				
				
				System.out.println("Calculation Epoch: " + currentEpoch);
				
				// TODO: Hier muss wie im not else Fall auch auf null überprüft werden.
				List<Double> samples = respectiveTrainDataPointsModel.getSamplesFromCurrentEpoch();
//				System.out.println("Sample size: " + samples.size());
								
				if (!(samples == null)) {
					
					double label = samples.get(0);
					samples.remove(0);
//					System.out.println("Label: " + label);
					respectiveFeatureExtractionModel.setFeatureClassLabel(currentEpoch, label);
					
					currentPEValue = calculatePermutationEntropy(samples, 6, 1);
//					System.out.println("PE: " + currentPEValue);
					respectiveFeatureExtractionModel.setFeatureValuesPE(currentEpoch, 1, currentPEValue);
						
					// Convert the list of samples to an array
					Double[] epoch = samples.toArray(new Double[samples.size()]);
					
					// Array for the autocorrelation values
					long[] R = new long[11];
					
					LPC.createAutoCorrelation(R, epoch, epoch.length, 0, 1, 10);
					LPC.calculate(lpcExtraction, R);
					
					double[] coefficients = lpcExtraction.getCoefficients();
					
					for(int y = 0; y < coefficients.length; y++){
						
						// Rounded a mantisse with value 4
						BigDecimal myDec = new BigDecimal(coefficients[y]);
						myDec = myDec.setScale(4, BigDecimal.ROUND_HALF_UP);
						
						// Insert in column y+2 because first column if for the class label and second column for the PE
						respectiveFeatureExtractionModel.setFeatureValuesPE(currentEpoch, y+2, myDec.floatValue());
						
					}
					
				}
				
				respectiveTrainDataPointsModel.clearSampleList();		
				
				// The current epoch which have been read/calculated.
				respectiveTrainDataPointsModel.setNumberOfCurrentEpoch(currentEpoch+1);	
				
				
				respectiveTrainDataPointsModel.setEpochHasBeenReadFlag(false);			

				synchronized (this) {
					pause();
				}
			}
		}
		
		//********************* END TRAIN MODE *********************
	}
	
	/**
	 * This method claculates the unweighted permutation entropy.
	 * 
	 * @param dataPoints
	 * 				List of samples from which the PE have to be calculated.
	 * @param orderOfPermutation
	 * 				Window size for calculation.
	 * @param tau
	 * 				Not used here. Normally tau is set to 1.
	 * @return
	 * 		claculated PE entropy. Float value.
	 */
	private float calculatePermutationEntropy(List<Double> dataPoints, int orderOfPermutation, int tau){
		
		// This first step is necessary to clear all calculations from the earlier steps
		countPermutations.clear();
		window.clear();
		
		float permutationEntropy = 0;
		
		float runIndex = (dataPoints.size() - orderOfPermutation + 1);
		float relativeFrequency;
		
		for(int i = 0; i < runIndex; i++) {
			
			for(int y = 0; y < orderOfPermutation; y++) {
				window.put(dataPoints.get(i + y), y);				
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
	
	
	public void start() {
		System.out.println("Starting Feature Extraction Thread");
		if (t == null) {
			t = new Thread(this, "FeatureExtraction");
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
