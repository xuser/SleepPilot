package model;

import java.io.File;
import java.io.Serializable;
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
public class FeatureExtractionModel implements Serializable{
	
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
	
	private int countWake = 0;
	private int countS1 = 0;
	private int countS2 = 0;
	private int countN = 0;
	private int countREM = 0;
	private int countA = 0;
	private int countMA = 0;
	private int countS = 0;
	
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
	 * If automode is true, then the classification automation was used.
	 */
	private boolean autoMode = false;
	
	private String selectedModel;
	
	/**
	 * The path to the eeg data.
	 */
	private File fileLocation;
	
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
	
	public void addArtefactToEpochProperty(int epoch) {
		if (epochProperties.containsKey(epoch)) {
			Integer[] tempProp = epochProperties.get(epoch);
			epochProperties.remove(epoch);
			
			if (tempProp[0] == 1) {
				countA--;
			}
			
			if (tempProp[1] == 1) {
				countMA--;
			}
			
			if (tempProp[2] == 1) {
				countS--;
			}
			
			Integer[] prop = {1,0,0};
			countA++;
			epochProperties.put(epoch, prop);
			
		} else {
			Integer[] prop = {1,0,0};
			countA++;
			epochProperties.put(epoch, prop);
		}
	}
	
	public void addArrousalToEpochProperty(int epoch) {
		if (epochProperties.containsKey(epoch)) {
			Integer[] tempProp = epochProperties.get(epoch);
			epochProperties.remove(epoch);
			
			if (tempProp[0] == 1) {
				countA--;
			}
			
			if (tempProp[1] == 1) {
				countMA--;
			}
			
			if (tempProp[2] == 1) {
				countS--;
			}
			
			Integer[] prop = {1,1,0};
			countA++;
			countMA++;
			epochProperties.put(epoch, prop);
			
		} else {
			Integer[] prop = {1,1,0};
			countA++;
			countMA++;
			epochProperties.put(epoch, prop);
		}
	}
	
	public void addStimulationToEpochProperty(int epoch) {
		if (epochProperties.containsKey(epoch)) {
			Integer[] tempProp = epochProperties.get(epoch);
			epochProperties.remove(epoch);
			
			if (tempProp[0] == 1) {
				countA--;
			}
			
			if (tempProp[1] == 1) {
				countMA--;
			}
			
			if (tempProp[2] == 1) {
				countS--;
			}
			
			Integer[] prop = {0,0,1};
			countS++;
			epochProperties.put(epoch, prop);
			
		} else {
			Integer[] prop = {0,0,1};
			countS++;
			epochProperties.put(epoch, prop);
		}
	}
	
	public void clearProperties(int epoch) {
		if (epochProperties.containsKey(epoch)) {
			Integer[] tempProp = epochProperties.get(epoch);
			epochProperties.remove(epoch);
			
			if (tempProp[0] == 1) {
				countA--;
			}
			
			if (tempProp[1] == 1) {
				countMA--;
			}
			
			if (tempProp[2] == 1) {
				countS--;
				
				int label = getFeatureClassLabel(epoch);
				switch (label) {
				case 1: countWake++;
					break;
				case 2: countS1++;
					break;
				case 3: countS2++;
					break;
				case 4: countN++;
					break;
				case 5: countREM++;
					break;
				default: System.err.println("Error during setting stats!");
					break;
				}
			}
			
			
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
		
		if(classificationDone) {
			int tmpLabel = getFeatureClassLabel(row);
			
			switch (tmpLabel) {
			case 1: countWake--;
				break;
			case 2: countS1--;
				break;
			case 3: countS2--;
				break;
			case 4: countN--;
				break;
			case 5: countREM--;
				break;
			case 0:	System.out.print("Case 0: SVM_Scale");			// Case 0 is just caused by svm_scale				
				break;
			default: System.err.println("Error during setting the class label!");
				break;
			}
		}
		int intLabel = (int) label;
		switch (intLabel) {
		case 1: countWake++;
			break;
		case 2: countS1++;
			break;
		case 3: countS2++;
			break;
		case 4: countN++;
			break;
		case 5: countREM++;
			break;
		case 0:	System.out.print("Case 0: SVM_Scale");				// Case 0 is just caused by svm_scale
			break;
		default: System.err.println("Error during setting the class label!");
			break;
		}
		
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

	//TODO
	/**
	 * This function only returns the matrix without the class label
	 * @return the whole feature value matrix
	 */
	public double[][] getFeatureValueMatrix() {
		
		double[][] matrix = new double[featureValuesPE.length][numberOfChannels];
		
		for (int i = 0; i < featureValuesPE.length; i++) {
			for (int x = 0; x < numberOfChannels; x++) {
				matrix[i][x] = featureValuesPE[i][x+1];
			}
		}
		
		return matrix;
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


	/**
	 * @return the countWake
	 */
	public int getCountWake() {
		return countWake;
	}


	/**
	 * @return the countS1
	 */
	public int getCountS1() {
		return countS1;
	}


	/**
	 * @return the countS2
	 */
	public int getCountS2() {
		return countS2;
	}


	/**
	 * @return the countN
	 */
	public int getCountN() {
		return countN;
	}


	/**
	 * @return the countREM
	 */
	public int getCountREM() {
		return countREM;
	}


	/**
	 * @return the countA
	 */
	public int getCountA() {
		return countA;
	}


	/**
	 * @return the countMA
	 */
	public int getCountMA() {
		return countMA;
	}


	/**
	 * @return the countS
	 */
	public int getCountS() {
		return countS;
	}

	/**
	 * @return the selectedModel
	 */
	public String getSelectedModel() {
		return selectedModel;
	}

	/**
	 * @param selectedModel the selectedModel to set
	 */
	public void setSelectedModel(String selectedModel) {
		this.selectedModel = selectedModel;
	}

	/**
	 * @return the featureValuesPE
	 */
	public float[][] getFeatureValuesPE() {
		return featureValuesPE;
	}

	/**
	 * @param featureValuesPE the featureValuesPE to set
	 */
	public void setFeatureValuesPE(float[][] featureValuesPE) {
		this.featureValuesPE = featureValuesPE;
	}

	/**
	 * @return the epochProperties
	 */
	public HashMap<Integer, Integer[]> getEpochProperties() {
		return epochProperties;
	}

	/**
	 * @param epochProperties the epochProperties to set
	 */
	public void setEpochProperties(HashMap<Integer, Integer[]> epochProperties) {
		this.epochProperties = epochProperties;
	}

	/**
	 * @return the predictProbabilities
	 */
	public double[][] getPredictProbabilities() {
		return predictProbabilities;
	}

	/**
	 * @param predictProbabilities the predictProbabilities to set
	 */
	public void setPredictProbabilities(double[][] predictProbabilities) {
		this.predictProbabilities = predictProbabilities;
	}

	/**
	 * @return the channelNames
	 */
	public LinkedList<ChannelNames> getChannelNames() {
		return channelNames;
	}

	/**
	 * @param channelNames the channelNames to set
	 */
	public void setChannelNames(LinkedList<ChannelNames> channelNames) {
		this.channelNames = channelNames;
	}

	/**
	 * @param countWake the countWake to set
	 */
	public void setCountWake(int countWake) {
		this.countWake = countWake;
	}

	/**
	 * @param countS1 the countS1 to set
	 */
	public void setCountS1(int countS1) {
		this.countS1 = countS1;
	}

	/**
	 * @param countS2 the countS2 to set
	 */
	public void setCountS2(int countS2) {
		this.countS2 = countS2;
	}

	/**
	 * @param countN the countN to set
	 */
	public void setCountN(int countN) {
		this.countN = countN;
	}

	/**
	 * @param countREM the countREM to set
	 */
	public void setCountREM(int countREM) {
		this.countREM = countREM;
	}

	/**
	 * @param countA the countA to set
	 */
	public void setCountA(int countA) {
		this.countA = countA;
	}

	/**
	 * @param countMA the countMA to set
	 */
	public void setCountMA(int countMA) {
		this.countMA = countMA;
	}

	/**
	 * @param countS the countS to set
	 */
	public void setCountS(int countS) {
		this.countS = countS;
	}

	/**
	 * @param numberOfChannels the numberOfChannels to set
	 */
	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	/**
	 * @return the autoMode
	 */
	public boolean isAutoMode() {
		return autoMode;
	}

	/**
	 * @param autoMode the autoMode to set
	 */
	public void setAutoMode(boolean autoMode) {
		this.autoMode = autoMode;
	}

	/**
	 * @return the fileLocation
	 */
	public File getFileLocation() {
		return fileLocation;
	}

	/**
	 * @param fileLocation the fileLocation to set
	 */
	public void setFileLocation(File fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	
}
