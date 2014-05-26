package model;

/**
 * This class is the respective model for the FeatureExtractionController.
 * 
 * @author Nils Finke
 *
 */
public class FeatureExtraxtionValues {
	
	/**
	 * This matrix saves continuously for each epoch their permutation entropies.
	 * The first value (y-axis) represent the PE values for the different epochs
	 * and the second value (x-axis) represent the different channels.
	 * 
	 * IMPORTANT: The first column is for training mode. Holds the manual classified sleep stage.
	 */
	private float[][] featureValuesPE;
	
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
	 * Creates the feature value matrix with the needed size.
	 * The first column holds the classified sleep stage.
	 * Test mode: The first column has the default value 99.00
	 * Traing mode: The first column has the respective value for the actual sleep stage
	 * 
	 * NOTATION: 	1		Sleep stage 1
	 * 				2		Sleep stage 2
	 * 				3		Sleep stage 3
	 * 				4		Wake
	 * 				0		REM sleep stage
	 * 				99		Unscored
	 */
	public void createDataMatrix(int rows, int columns) {
		
		numberOfFeatureValues = rows;
		numberOfChannels = columns;
		featureValuesPE = new float[rows][columns + 1];
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
	
	
	
}
