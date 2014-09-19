package model;

import java.util.HashMap;
import java.util.LinkedList;

import view.FXApplicationController;
import help.ChannelNames;

/**
 * This class is the respective model for the FeatureExtractionController.
 * 
 * @author Nils Finke
 *
 */
public class FeatureExtractionModel {
	
	/**
	 * This matrix saves continuously for each epoch their permutation entropies.
	 * The first value (y-axis) represent the PE values for the different epochs
	 * and the second value (x-axis) represent the different channels.
	 * 
	 * IMPORTANT: The first column is for training mode. Holds the manual classified sleep stage.
	 */
	private float[][] featureValuesPE;
	
	/**
	 * This HashMap keeps additional information for some epochs. The key keeps the respective epoch number
	 * and the value holds an array with the information.
	 * 
	 * IMPORTANT: Only add values to the hashmap if any additional information have to be set
	 * 
	 * 1st in array: 1 if artefact, else 0
	 * 2nd in array: 1 if arrousal, else 0
	 * 3hd in array: 1 if stimulation, else 0
	 */
	private HashMap<Integer, Integer[]> epochProperties = new HashMap<Integer, Integer[]>();
	
	/**
	 * Predict probabilities for the different classes. Each row keeps one array with
	 * the probabilities for the epoch with the rowindex. 
	 */
	private double[][] predictProbabilities;
	
	/**
	 * This variable keeps the number of the epoch to which the calculation of the PE
	 * has been done.
	 */
	private int numberOfcalculatedEpoch = 0;
	
	/**
	 * Keeps the number of feature vectors.
	 */
	private int numberOfFeatureValues;
	
	/**
	 * Keeps the number of channels from input data.
	 */
	private int numberOfChannels;
	
	
	/**
	 * This variable holds the actual positon in the PE matrix.
	 */
	private int rowPosition = 0;
	
	/**
	 * Keeps the status, if the TrainController has finished reading and calculating.
	 */
	private boolean readingAndCalculatingDone = false;
	
	/**
	 * Keeps the status, if the SVMController has finished the classification.
	 */
	private boolean classificationDone = false;
	
	/**
	 * The number of samples for one epoch.
	 */
	private int lengthOfOneEpoch;
	
	/**
	 * The number of epochs for one record.
	 */
	private int numberOfEpochs;
	
	/**
	 * The channel name of the samples in the training file.
	 */
	private LinkedList<ChannelNames> channelNames = new LinkedList<ChannelNames>();
	
	/**
	 * Creates the feature value matrix with the needed size.
	 * The first column holds the classified sleep stage.
	 * Test mode: The first column has the default value 99.00
	 * Traing mode: The first column has the respective value for the actual sleep stage
	 * 
	 * NOTATION: 	1		Wake
	 * 				2		Sleep stage 1
	 * 				3		Sleep stage 2
	 * 				4		Sleep stage 3
	 * 				5		REM sleep stage
	 * 				99		Unscored
	 */
	public void createDataMatrix(int rows, int columns) {
		
		numberOfFeatureValues = rows;
		numberOfChannels = columns;
		featureValuesPE = new float[rows][columns + 1];
		predictProbabilities = new double[rows][];
	}
	
	
	/**
	 * Add additional information to some epochs (HashMap). The key keeps the respective epoch number
	 * and the value holds an array with the information.
	 * 
	 * IMPORTANT: Only add values to the hashmap if any additional information have to be set
	 * 
	 * 1st in array: 1 if artefact, else 0
	 * 2nd in array: 1 if arrousal, else 0
	 * 3hd in array: 1 if stimulation, else 0
	 */
	public void addEpochProperty(int epoch, boolean artefact, boolean arrousal, boolean stimulation) {
						
		Integer[] prop;
		if (epochProperties.containsKey(epoch)) {
			prop = epochProperties.get(epoch);
			epochProperties.remove(epoch);
		
			if (artefact && prop[0] == 0) {
				prop[0] = 1;
			} else if (!artefact) {
				prop[0] = 0;
			}
			
			if (arrousal && prop[1] == 0) {
				prop[1] = 1;
			} else {
				prop[1] = 0;
			}
			
			if (stimulation && prop[2] == 0) {
				prop[2] = 1;
			} else {
				prop[2] = 0;
			}
			
			epochProperties.put(epoch, prop);
		
		
		} else {
			prop = new Integer[3];
			
			if (artefact) {
				prop[0] = 1;
			} else {
				prop[0] = 0;
			}
			
			if (arrousal) {
				prop[1] = 1;
			} else {
				prop[1] = 0;
			}
			
			if (stimulation) {
				prop[2] = 1;
			} else {
				prop[2] = 0;
			}
			
			epochProperties.put(epoch, prop);
		}
		
		
	}
	
	/**
	 * Return all additional epoch properties
	 * @param epoch
	 * 			the number of the needed epoch
	 * @return
	 * 		the array with the properties.
	 * 			1st in array: 1 if artefact, else 0
	 * 			2nd in array: 1 if arrousal, else 0
	 * 			3hd in array: 1 if stimulation, else 0
	 */
	public Integer[] getEpochProperty(int epoch) {
		
		if (epochProperties.containsKey(epoch)) {
			return epochProperties.get(epoch);
		} else {
			return null;
		}
	}
	
	
	/**
	 * This method builds the needed string for using the svm_scale method.
	 * 
	 * @return the featureValuePE for one epoche (Values for training)
	 */
	public String getFeatureValuePE() {
		
		StringBuilder featureVector = new StringBuilder();
		
		if (rowPosition < numberOfFeatureValues) {
			
			// Get class label.
			featureVector.append((int)(featureValuesPE[rowPosition][0]));
		
			// Get feature values.
			for(int i = 1; i <= numberOfChannels; i++) {
				featureVector.append(" " + i + ":" + featureValuesPE[rowPosition][i]);
			}
		
			rowPosition = rowPosition + 1;
		
			String featureVectorString = featureVector.toString();
			return featureVectorString;
			
		} else {
			return null;
		}
	}

	/**
	 * @param featureValuesPE the featureValuesPE to set
	 */
	public void setFeatureValuesPE(int row, int column, float featureValuePE) {
		this.featureValuesPE[row][column] = featureValuePE;
	}
	
	/**
	 * @param row
	 * 			the row position of the value.
	 * @param channel
	 * 			the channel of the value.
	 * @return
	 * 		the needed PE feature value
	 */
	public float getFeatureValuePE(int row, int channel) {
		return featureValuesPE[row][channel];
	}
	
	/**
	 * Set the scored class label for a feature vector in the matrix.
	 * @param row
	 * 			the number of scored feature value.
	 * @param label
	 * 			the class label to set.
	 */
	public void setFeatureClassLabel(int row, double label) {
		featureValuesPE[row][0] =  (float) label;
	}
	
	/**
	 * Get the scored class label for a feature vector in the matrix.
	 * @param row
	 * 			the epoch of scored feature value.
	 */
	public int getFeatureClassLabel(int row) {
		return (int) featureValuesPE[row][0];
	}
	
	/**
	 * Rewind the row in data matrics. This is needed in scaling of the data.
	 */
	public void rewindRowPosition() {
		rowPosition = 0;
	}
	
	/**
	 * Set the number of the epoch from which the PE has been calculated.
	 * @param value
	 * 			number of the epoch
	 */
	public void setNumberOfcalculatedEpoch(int value) {
		numberOfcalculatedEpoch = value;
	}
	
	/**
	 * @return	the number of the epoch from which the PE has been calculated.
	 */
	public int getNumberOfcalculatedEpoch() {
		return numberOfcalculatedEpoch;
	}
	
	/**
	 * @return	the number of feature vectors in the matrix.
	 */
	public int getNumberOfFeatureValues() {
		return numberOfFeatureValues;
	}
	
	/**
	 * @return	the number of channels for the input data.
	 */
	public int getNumberOfChannels() {
		return numberOfChannels;
	}
	
	/**
	 * @return	if the TrainController has finished reading and calculating.
	 */
	public boolean getReadingAndCalculatingDone() {
		return readingAndCalculatingDone;
	}
	
	/**
	 * Set the status of the readingAndCalculatingDone Flag.
	 * @param status
	 * 			true, if the TrainController has finished reading and calculating.
	 */
	public void setReadingAndCalculatingDone(boolean status) {
		readingAndCalculatingDone = status;
	}
	
	/**
	 * Set the statis of the classificationDone Flag.
	 * @param status
	 * 			true, if SVM Controller has finished the classification
	 */
	public void setClassificationDone(boolean status) {
		classificationDone = status;
	}
	
	/**
	 * @return	the status flag, if the SVM Controller has finished classification.
	 */
	public boolean getClassificationDone() {
		return classificationDone;
	}
	
	
	/**
	 * @param row
	 * 			the needed epoch (row) from the matrix.	
	 * @return	the feature vector from needed epoch (row).
	 */
	public double[] getFeatureVector(int row) {
		
		double[] features = new double[numberOfChannels+1];
		
		for(int i = 0; i < (numberOfChannels+1); i++) {
			features[i] = featureValuesPE[row][i];
		}
		
		return features;
	}


	/**
	 * @return the lengthOfOneEpoch
	 */
	public int getLengthOfOneEpoch() {
		return lengthOfOneEpoch;
	}


	/**
	 * @param lengthOfOneEpoch the lengthOfOneEpoch to set
	 */
	public void setLengthOfOneEpoch(int lengthOfOneEpoch) {
		this.lengthOfOneEpoch = lengthOfOneEpoch;
	}


	/**
	 * @return the numberOfEpochs
	 */
	public int getNumberOfEpochs() {
		return numberOfEpochs;
	}


	/**
	 * @param numberOfEpochs the numberOfEpochs to set
	 */
	public void setNumberOfEpochs(int numberOfEpochs) {
		this.numberOfEpochs = numberOfEpochs;
	}


	/**
	 * @return the channelNames, which have been calculated.
	 */
	public LinkedList<ChannelNames> getChannelName() {
		return channelNames;
	}


	/**
	 * @param channelName the channelName, which have been calculated.
	 */
	public void setChannelName(ChannelNames channelName) {
		channelNames.add(channelName);
	}
	
	/**
	 * Set the array with the predict probabilities for the current epoch.
	 * @param row
	 * 			the epoch for the predict probabilities.
	 * @param predictProbability
	 * 			the array with the predict probabilities.
	 */
	public void setPredictProbabilities(int row, double[] predictProbability) {
		predictProbabilities[row] = predictProbability;
	}
	
	/**
	 * Get the array with the predict probabilities for the current epoch.
	 * @param row
	 * 			the epoch for the predict probabilities.
	 * @return 
	 */
	public double[] getPredictProbabilities(int row) {
		return predictProbabilities[row];
	}
	
	
}
