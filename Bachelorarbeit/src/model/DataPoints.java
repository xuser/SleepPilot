package model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the data, that has been read by the DataReaderController.
 * Model for the DataReaderController and the FilterController.
 * 
 * @author Nils Finke
 */
public class DataPoints {
	
	private Double[][] dataPoints;
	
	/**
	 * This variable holds the status if the reader has finsihed his work
	 */
	private boolean readingComplete = false;
	
	private boolean filteringComplete = false;
	
	/**
	 * This variable holds the value which number of the samples has been read.
	 */
	private int rowInSampleFile = 0;
	
	/**
	 * This variable holds the value of the row to which filtering is done.
	 */
	private int rowFilteredValues = 0;
	
	// [Common Infos]
	private int numberOfDataPoints;
	private int numberOfChannels;
	private int samplingIntervall;
	
	// [Channel Infos]
	private String[] channelNames;
	
	private int samplingRateConvertedToHertz;
	
	/**
	 * @param dataPoints the dataPoints to set
	 */
	public void setDataPoints(Double dataPoints, int row, int column) {
		this.dataPoints[row][column] = dataPoints;
	}

	/**
	 * @return the numberOfDataPoints
	 */
	public int getNumberOfDataPoints() {
		return numberOfDataPoints;
	}

	/**
	 * @param numberOfDataPoints the numberOfDataPoints to set
	 */
	public void setNumberOfDataPoints(int numberOfDataPoints) {
		this.numberOfDataPoints = numberOfDataPoints;
	}

	/**
	 * @return the numberOfChannels
	 */
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	/**
	 * @param numberOfChannels the numberOfChannels to set
	 */
	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	/**
	 * @return the samplingIntervall
	 */
	public int getSamplingIntervall() {
		return samplingIntervall;
	}

	/**
	 * @param samplingIntervall the samplingIntervall to set
	 */
	public void setSamplingIntervall(int samplingIntervall) {
		this.samplingIntervall = samplingIntervall;
		
		samplingRateConvertedToHertz = (1000000 / samplingIntervall);
		
	}
	
	/**
	 * @return the samplingRateConvertedToHertz
	 */
	public double getSamplingRateConvertedToHertz() {
		return samplingRateConvertedToHertz;
	}

	/**
	 * @return the channelNames
	 */
	public String[] getChannelNames() {
		return channelNames;
	}

	/**
	 * @param channelNames the channelNames to set
	 */
	public void setChannelNames(String[] channelNames) {
		this.channelNames = channelNames;
	}
	
	/**
	 * Creates the data matrix with the needed size.
	 */
	public void createDataMatrix() {
		dataPoints = new Double[numberOfDataPoints][numberOfChannels];
	}
	
	/**
	 * This method returns a value from a specific position in the data points.
	 * IMPORTANT: Just use the function for printing a needed data point!
	 * 
	 * @param row
	 * 			the row from where you want to get the value 
	 * @param channel
	 * 			the channel from where you want to get the value
	 * @return
	 * 		the needed value
	 */
	public double printValueFromData(int row, int channel) {
		return dataPoints[row - 1][channel - 1];
	}
	
	/**
	 * This method returns a value from a specific position in the data points.
	 * IMPORTANT: Just use this function for filtering and not, if you want to print the value!
	 * 
	 * @param row
	 * 			the row from where you want to get the value 
	 * @param channel
	 * 			the channel from where you want to get the value
	 * @return
	 * 		the needed value
	 */
	public double getValueFromData(int row, int channel) {
		return dataPoints[row][channel];
	}
	
	
	/**
	 * Use this method to get all samples from one 30s epoch. The iteration over
	 * the different epochs have to be done in the controller class.
	 * 
	 * IMPORTANT: The first epoch is epoch #0. So start iterating from zero.
	 * 
	 * @param numberOfEpoch
	 * 			the row from which on the needed values have to be returned.
	 * @param channel
	 * 			the channel from which the List have to be returned
	 * @return
	 * 		a list of double values of all samples from one 30s epoch.
	 */
	public List<Double> getAllSamplesFromOneEpoche(int numberOfEpoch, int channel, int numberOf30sEpochs) {
		
		List<Double> samples = new LinkedList<Double>();
		
		// Es wird an dieser Stelle zu weit gegangen.
		// Also in der letzten Epoche extieren nicht genügend Datenpunkte
		
		// Calculate the startingSample for the needed epoch.
		int startingPoint = numberOfEpoch * 30 * samplingRateConvertedToHertz;
		int endPoint = (numberOfEpoch+1) * 30 * samplingRateConvertedToHertz;
		
		if (((numberOfEpoch + 1) == numberOf30sEpochs) && (endPoint != numberOfDataPoints)) {
			return null;
		} else {
			
			for (int i = startingPoint; i < endPoint; i++) {
				samples.add(dataPoints[i][channel]);
			}
			
			// TODO: Dies ist nur eine Testausgabe. Kann später entfernt werden.
			if (numberOfEpoch % 10 == 0) {
				System.out.println("Epoche # " + numberOfEpoch);
			}
		
		return samples;
		}
	}
	
	public int getNumberOf30sEpochs() {
		return (int) (numberOfDataPoints / (30 * samplingRateConvertedToHertz));
	}
	
	public void setReadingComplete(boolean value) {
		readingComplete = value;
	}
	
	public boolean getReadingCompleteStatus() {
		return readingComplete;
	}
	
	public void setRowInSampleFile(int value) {
		rowInSampleFile = value;
	}
	
	public int getRowInSampleFile() {
		return rowInSampleFile;
	}
	
	public void setFilteringComplete(boolean value) {
		filteringComplete = value;
	}
	
	public boolean getFilteringComplete() {
		return filteringComplete;
	}
	
	public void setRowFilteredValues(int value) {
		rowFilteredValues = value;
	}
	
	public int getRowFilteredValues() {
		return rowFilteredValues;
	}

}
