package model;

/**
 * This class holds the data, that has been read by the DataReaderController.
 * 
 * @author Nils Finke
 */
public class DataPoints {
	
	private Double[][] dataPoints;
	
	// [Common Infos]
	private int numberOfDataPoints;
	private int numberOfChannels;
	private int samplingIntervall;
	
	// [Channel Infos]
	private String[] channelNames;
	
	private double samplingRateConvertedToHertz;
	
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

}
