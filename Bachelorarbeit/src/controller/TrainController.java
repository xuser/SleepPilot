package controller;

import help.ChannelNames;
import help.LPC;
import help.MathFunctions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.FeatureExtraxtionValues;
import model.TrainDataPoints;

/**
 * This controller is only for reading training data with special format
 * specification. You can use the matlab scripts, which you can find in the appended folder.
 * 
 * @author Nils Finke
 */
public class TrainController extends Thread {
	
	private Thread t;
	private boolean fPause = false;
	
	private RandomAccessFile trainDataFile;
	
	private TrainDataPoints respectiveTrainDataPointsModel;
	private String fileLocationPath;
	
	private int numberOfReadSamples = 0;
	
	/**
	 * This TreeMap holds all values for the current window.
	 * The TreeMap automatically sorts the map by its key value in log(n) time.
	 */
	private Map<Double, Integer> window = new TreeMap<Double, Integer>();
	
	/**
	 * This HashMap counts for each permutation the number of occurrences in the current set of samples.
	 */
	private HashMap<String, Integer> countPermutations = new HashMap<String, Integer>();
	
	private LPC lpcExtraction;
	
	private FeatureExtraxtionValues respectiveFeatureExtraxtionModel;
	
	public TrainController(TrainDataPoints trainModel, String fileLocation, FeatureExtraxtionValues model) {
		
		respectiveTrainDataPointsModel = trainModel;
		fileLocationPath = fileLocation;
		
		respectiveFeatureExtraxtionModel = model;
		
		try {
			trainDataFile = new RandomAccessFile(fileLocationPath, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			System.err.println("Could not find the TrainDataFile!");
		}
		
		// Create instance of LPC class
		lpcExtraction = new LPC(10);
		
	}
	
	public void run(){
		readTrainDataFile(trainDataFile);
	}
	
	/**
	 * Reads the given train data file and prints it on hard disk
	 * 
	 * @param dataFile
	 */
	private void readTrainDataFile(RandomAccessFile trainDataFile) {
		
		try {
			FileChannel inChannel = trainDataFile.getChannel();
						
			ByteBuffer buf = ByteBuffer.allocate((int) trainDataFile.length());
			buf.order(ByteOrder.LITTLE_ENDIAN);
		
			int bytesRead = inChannel.read(buf);
				
			// Marks the position in current epoch
			int currentEpochPos = 0;
			
			// Keeps the current epoch
			int currentEpoch = 0;
			
			// Saves all samples from current epoch.
			List<Double> epoch = new LinkedList<Double>();
			
			while (bytesRead != -1) {

				//Make buffer ready for read
				buf.flip();
				
				
				// This is important for setting the correct channel name
				// Add here new channel names
				switch ((int) buf.getFloat()) {
				case 1: respectiveFeatureExtraxtionModel.setChannelName(ChannelNames.Fz);
					break;
				/*case 12: respectiveFeatureExtraxtionModel.setChannelName(ChannelNames.Fz);
						 respectiveFeatureExtraxtionModel.setChannelName(ChannelNames.VEOG1);
					break;*/
				default: respectiveFeatureExtraxtionModel.setChannelName(ChannelNames.UNKNOWN);
					break;
				}
				
				respectiveFeatureExtraxtionModel.setLengthOfOneEpoch((int) buf.getFloat());
				respectiveFeatureExtraxtionModel.setNumberOfEpochs((int) buf.getFloat());
				
				// 1 Column for the PE of one channel and 10 columns for the LSP
				// coefficients
				respectiveFeatureExtraxtionModel.createDataMatrix(respectiveFeatureExtraxtionModel.getNumberOfEpochs(), (1 + 10));
				
				// The next values are zeros, so set to the beginn of the actual data.
				// +1 is important, because we always have one label at the beginning of the epoch
				// * 4 because one value insists of 4 bytes
				buf.position((respectiveFeatureExtraxtionModel.getLengthOfOneEpoch() + 1) * 4);
				
				while (buf.hasRemaining()) {
					
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
					
					Double value = (double) buf.getFloat();
					
					// Rounded a mantisse with value 4
					BigDecimal pe = new BigDecimal(value);
					pe = pe.setScale(4, BigDecimal.ROUND_HALF_UP);
					value = pe.doubleValue();
					
					epoch.add(value);
					
					currentEpochPos++;
					
					// We add 1, because of the class label
					if (currentEpochPos == (respectiveFeatureExtraxtionModel.getLengthOfOneEpoch() + 1)) {
						
						double label = epoch.get(0);
						respectiveFeatureExtraxtionModel.setFeatureClassLabel(currentEpoch, label);
						
						epoch.remove(0);
						
						float tmp = calculatePermutationEntropy(epoch, 6, 1);
						
						// Set the PE value into the 1. column and not into the 0. column.
						// For more information see the FeatureExtractionValues.java
						respectiveFeatureExtraxtionModel.setFeatureValuesPE(currentEpoch, 1, tmp);
						
						double[] coefficients = calculateLSP(epoch);
						
						for(int y = 0; y < coefficients.length; y++){
							
							// Rounded a mantisse with value 4
							BigDecimal lpc = new BigDecimal(coefficients[y]);
							lpc = lpc.setScale(4, BigDecimal.ROUND_HALF_UP);
							
							// Insert in column y+2 because first column if for the class label and second column for the PE
							respectiveFeatureExtraxtionModel.setFeatureValuesPE(currentEpoch, y+2, lpc.floatValue());
							
						}
						
						
						System.out.println("Calculated Epoch: " + currentEpoch);
						
						currentEpoch++;
						currentEpochPos = 0;
						epoch.clear();
						
					}
					
			
					
				}
								
				buf.clear(); //make buffer ready for writing
				bytesRead = inChannel.read(buf);
			}
			
			System.out.println("Finished training wihout problems!");
			respectiveFeatureExtraxtionModel.setReadingAndCalculatingDone(true);
			
			trainDataFile.close();
		
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Error during reading the training data file!");
		}
	}
	
	private double[] calculateLSP(List<Double> dataPoints) {
		
		// Convert the list of samples to an array
		Double[] epoch = dataPoints.toArray(new Double[dataPoints.size()]);
		
		// Array for the autocorrelation values
		long[] R = new long[11];
		
		LPC.createAutoCorrelation(R, epoch, epoch.length, 0, 1, 10);
		LPC.calculate(lpcExtraction, R);
		
		double[] LPCcoefficients = lpcExtraction.getCoefficients();
		
		double[] LSPcoeficients = new double[LPCcoefficients.length - 1];
		LSPcoeficients = LPC.lpc2lsp(LPCcoefficients, 10, LSPcoeficients, 4, 0.02);
		
		
		return LSPcoeficients;
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
		System.out.println("Starting Train Data Reader Thread");
		if (t == null) {
			t = new Thread(this, "TrainDataReader");
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
