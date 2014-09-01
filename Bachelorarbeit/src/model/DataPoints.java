package model;

import java.util.HashMap;
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
	 * This list keeps lists with the epochs (raw data).
	 * IMPORTANT: The head of the inter list keeps the assosiated epoch.
	 */
	private LinkedList<LinkedList<Double>> rawEpochs = new LinkedList<LinkedList<Double>>();
	
	/**
	 * This list keeps lists with the epochs (filtered data).
	 * IMPORTANT: The head of the inter list keeps the assosiated epoch.
	 */
	private LinkedList<LinkedList<Double>> filteredEpochs = new LinkedList<LinkedList<Double>>();
	
	/**
	 * This variable holds the value which number of the samples has been read.
	 */
	private int rowInSampleFile = 0;
	
	/**
	 * This variable holds the value of the row to which filtering is done.
	 */
	private int rowFilteredValues = 0;
	
	// *** Brainvision Format ***
	// [Common Infos]
	private int numberOfDataPoints;
	private int numberOfChannels;					// This variable will be used for the Spike2 Format as well
	private int samplingIntervall;
	
	// [Channel Infos]
	private String[] channelNames;
	
	// *** Brainvision Format ***
	
	private int samplingRateConvertedToHertz;
	
	// *** Spike2 Format ***
	
	// Information from File Header
	private static int systemId;
	private static int usPerTime;
	private static int timePerADC;
	private static int fileState;
	private static int firstData;
	private static int channels;
	private static int chanSize;
	private static int extraData;
	private static int bufferSize;
	private static int osFormat;
	private static int maxFTime;
	private static double dTimeBase;

	// Information from Channel Header

	// We dont know the exact number of channels. This is why we create lists,
	// which will be filled up. Example: All information for channel one are in
	// list position zero etc..
	private static LinkedList<Integer> delSize = new LinkedList<Integer>();
	private static LinkedList<Integer> nextDelBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> firstBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> lastBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> blocks = new LinkedList<Integer>();
	private static LinkedList<Integer> nExtra = new LinkedList<Integer>();
	private static LinkedList<Integer> preTrig = new LinkedList<Integer>();
	private static LinkedList<Integer> free0 = new LinkedList<Integer>();
	private static LinkedList<Integer> phySz = new LinkedList<Integer>();
	private static LinkedList<Integer> maxData = new LinkedList<Integer>();
	private static LinkedList<Integer> maxChanTime = new LinkedList<Integer>();
	private static LinkedList<Integer> lChanDvd = new LinkedList<Integer>();
	private static LinkedList<Integer> phyChan = new LinkedList<Integer>();
	private static LinkedList<String> titel = new LinkedList<String>();
	private static LinkedList<Float> idealRate = new LinkedList<Float>();
	private static LinkedList<Integer> kind = new LinkedList<Integer>();
	private static LinkedList<Integer> pad = new LinkedList<Integer>();
	private static LinkedList<Float> scale = new LinkedList<Float>();
	private static LinkedList<Float> offset = new LinkedList<Float>();
	private static LinkedList<Integer> divide = new LinkedList<Integer>();
	private static LinkedList<Integer> interleave = new LinkedList<Integer>();
	
	
	// *** Spike2 Format ***
	
	
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
	 * INFO: ChannelName for channel one is at position 0 etc. Shift!
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
	 * NOT USED ANY LONGER!
	 * 
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
	 * NOT USED ANY LONGER!
	 * 
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
	
	public void addRawEpoch(LinkedList<Double> rawEpoch) {
		rawEpochs.add(rawEpoch);
	}
	
	/**
	 * Polls the rawEpoch. Get and remove the head of the list.
	 * @return
	 * 		LinkedList with the epochs.
	 */
	public LinkedList<Double> getRawEpoch() {
		return rawEpochs.poll();
	}
	
	public int getSizeOfRawEpochList() {
		return rawEpochs.size();
	}
	
	public void addFilteredEpoch(LinkedList<Double> filteredEpoch) {
		filteredEpochs.add(filteredEpoch);
	}
	
	/**
	 * Polls the rawEpoch. Get and remove the head of the list.
	 * @return
	 * 		LinkedList with the epochs.
	 */
	public LinkedList<Double> getFilteredEpoch() {
		return filteredEpochs.poll();
	}
	
	public int getSizeOfFilteredEpochList() {
		return filteredEpochs.size();
	}

	/**
	 * @return the systemId
	 */
	public static int getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public static void setSystemId(int systemId) {
		DataPoints.systemId = systemId;
	}

	/**
	 * @return the usPerTime
	 */
	public static int getUsPerTime() {
		return usPerTime;
	}

	/**
	 * @param usPerTime the usPerTime to set
	 */
	public static void setUsPerTime(int usPerTime) {
		DataPoints.usPerTime = usPerTime;
	}

	/**
	 * @return the timePerADC
	 */
	public static int getTimePerADC() {
		return timePerADC;
	}

	/**
	 * @param timePerADC the timePerADC to set
	 */
	public static void setTimePerADC(int timePerADC) {
		DataPoints.timePerADC = timePerADC;
	}

	/**
	 * @return the fileState
	 */
	public static int getFileState() {
		return fileState;
	}

	/**
	 * @param fileState the fileState to set
	 */
	public static void setFileState(int fileState) {
		DataPoints.fileState = fileState;
	}

	/**
	 * @return the firstData
	 */
	public static int getFirstData() {
		return firstData;
	}

	/**
	 * @param firstData the firstData to set
	 */
	public static void setFirstData(int firstData) {
		DataPoints.firstData = firstData;
	}

	/**
	 * @return the channels
	 */
	public static int getChannels() {
		return channels;
	}

	/**
	 * @param channels the channels to set
	 */
	public static void setChannels(int channels) {
		DataPoints.channels = channels;
	}

	/**
	 * @return the chanSize
	 */
	public static int getChanSize() {
		return chanSize;
	}

	/**
	 * @param chanSize the chanSize to set
	 */
	public static void setChanSize(int chanSize) {
		DataPoints.chanSize = chanSize;
	}

	/**
	 * @return the extraData
	 */
	public static int getExtraData() {
		return extraData;
	}

	/**
	 * @param extraData the extraData to set
	 */
	public static void setExtraData(int extraData) {
		DataPoints.extraData = extraData;
	}

	/**
	 * @return the bufferSize
	 */
	public static int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public static void setBufferSize(int bufferSize) {
		DataPoints.bufferSize = bufferSize;
	}

	/**
	 * @return the osFormat
	 */
	public static int getOsFormat() {
		return osFormat;
	}

	/**
	 * @param osFormat the osFormat to set
	 */
	public static void setOsFormat(int osFormat) {
		DataPoints.osFormat = osFormat;
	}

	/**
	 * @return the maxFTime
	 */
	public static int getMaxFTime() {
		return maxFTime;
	}

	/**
	 * @param maxFTime the maxFTime to set
	 */
	public static void setMaxFTime(int maxFTime) {
		DataPoints.maxFTime = maxFTime;
	}

	/**
	 * @return the dTimeBase
	 */
	public static double getdTimeBase() {
		return dTimeBase;
	}

	/**
	 * @param dTimeBase the dTimeBase to set
	 */
	public static void setdTimeBase(double dTimeBase) {
		DataPoints.dTimeBase = dTimeBase;
	}

	/**
	 * @return the delSize
	 */
	public static LinkedList<Integer> getDelSize() {
		return delSize;
	}

	/**
	 * @param delSize the delSize to set
	 */
	public static void setDelSize(LinkedList<Integer> delSize) {
		DataPoints.delSize = delSize;
	}

	/**
	 * @return the nextDelBlock
	 */
	public static LinkedList<Integer> getNextDelBlock() {
		return nextDelBlock;
	}

	/**
	 * @param nextDelBlock the nextDelBlock to set
	 */
	public static void setNextDelBlock(LinkedList<Integer> nextDelBlock) {
		DataPoints.nextDelBlock = nextDelBlock;
	}

	/**
	 * @return the firstBlock
	 */
	public static LinkedList<Integer> getFirstBlock() {
		return firstBlock;
	}

	/**
	 * @param firstBlock the firstBlock to set
	 */
	public static void setFirstBlock(LinkedList<Integer> firstBlock) {
		DataPoints.firstBlock = firstBlock;
	}

	/**
	 * @return the lastBlock
	 */
	public static LinkedList<Integer> getLastBlock() {
		return lastBlock;
	}

	/**
	 * @param lastBlock the lastBlock to set
	 */
	public static void setLastBlock(LinkedList<Integer> lastBlock) {
		DataPoints.lastBlock = lastBlock;
	}

	/**
	 * @return the blocks
	 */
	public static LinkedList<Integer> getBlocks() {
		return blocks;
	}

	/**
	 * @param blocks the blocks to set
	 */
	public static void setBlocks(LinkedList<Integer> blocks) {
		DataPoints.blocks = blocks;
	}

	/**
	 * @return the nExtra
	 */
	public static LinkedList<Integer> getnExtra() {
		return nExtra;
	}

	/**
	 * @param nExtra the nExtra to set
	 */
	public static void setnExtra(LinkedList<Integer> nExtra) {
		DataPoints.nExtra = nExtra;
	}

	/**
	 * @return the preTrig
	 */
	public static LinkedList<Integer> getPreTrig() {
		return preTrig;
	}

	/**
	 * @param preTrig the preTrig to set
	 */
	public static void setPreTrig(LinkedList<Integer> preTrig) {
		DataPoints.preTrig = preTrig;
	}

	/**
	 * @return the free0
	 */
	public static LinkedList<Integer> getFree0() {
		return free0;
	}

	/**
	 * @param free0 the free0 to set
	 */
	public static void setFree0(LinkedList<Integer> free0) {
		DataPoints.free0 = free0;
	}

	/**
	 * @return the phySz
	 */
	public static LinkedList<Integer> getPhySz() {
		return phySz;
	}

	/**
	 * @param phySz the phySz to set
	 */
	public static void setPhySz(LinkedList<Integer> phySz) {
		DataPoints.phySz = phySz;
	}

	/**
	 * @return the maxData
	 */
	public static LinkedList<Integer> getMaxData() {
		return maxData;
	}

	/**
	 * @param maxData the maxData to set
	 */
	public static void setMaxData(LinkedList<Integer> maxData) {
		DataPoints.maxData = maxData;
	}

	/**
	 * @return the maxChanTime
	 */
	public static LinkedList<Integer> getMaxChanTime() {
		return maxChanTime;
	}

	/**
	 * @param maxChanTime the maxChanTime to set
	 */
	public static void setMaxChanTime(LinkedList<Integer> maxChanTime) {
		DataPoints.maxChanTime = maxChanTime;
	}

	/**
	 * @return the lChanDvd
	 */
	public static LinkedList<Integer> getlChanDvd() {
		return lChanDvd;
	}

	/**
	 * @param lChanDvd the lChanDvd to set
	 */
	public static void setlChanDvd(LinkedList<Integer> lChanDvd) {
		DataPoints.lChanDvd = lChanDvd;
	}

	/**
	 * @return the phyChan
	 */
	public static LinkedList<Integer> getPhyChan() {
		return phyChan;
	}

	/**
	 * @param phyChan the phyChan to set
	 */
	public static void setPhyChan(LinkedList<Integer> phyChan) {
		DataPoints.phyChan = phyChan;
	}

	/**
	 * @return the titel
	 */
	public static LinkedList<String> getTitel() {
		return titel;
	}

	/**
	 * @param titel the titel to set
	 */
	public static void setTitel(LinkedList<String> titel) {
		DataPoints.titel = titel;
	}

	/**
	 * @return the idealRate
	 */
	public static LinkedList<Float> getIdealRate() {
		return idealRate;
	}

	/**
	 * @param idealRate the idealRate to set
	 */
	public static void setIdealRate(LinkedList<Float> idealRate) {
		DataPoints.idealRate = idealRate;
	}

	/**
	 * @return the kind
	 */
	public static LinkedList<Integer> getKind() {
		return kind;
	}

	/**
	 * @param kind the kind to set
	 */
	public static void setKind(LinkedList<Integer> kind) {
		DataPoints.kind = kind;
	}

	/**
	 * @return the pad
	 */
	public static LinkedList<Integer> getPad() {
		return pad;
	}

	/**
	 * @param pad the pad to set
	 */
	public static void setPad(LinkedList<Integer> pad) {
		DataPoints.pad = pad;
	}

	/**
	 * @return the scale
	 */
	public static LinkedList<Float> getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public static void setScale(LinkedList<Float> scale) {
		DataPoints.scale = scale;
	}

	/**
	 * @return the offset
	 */
	public static LinkedList<Float> getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public static void setOffset(LinkedList<Float> offset) {
		DataPoints.offset = offset;
	}

	/**
	 * @return the divide
	 */
	public static LinkedList<Integer> getDivide() {
		return divide;
	}

	/**
	 * @param divide the divide to set
	 */
	public static void setDivide(LinkedList<Integer> divide) {
		DataPoints.divide = divide;
	}

	/**
	 * @return the interleave
	 */
	public static LinkedList<Integer> getInterleave() {
		return interleave;
	}

	/**
	 * @param interleave the interleave to set
	 */
	public static void setInterleave(LinkedList<Integer> interleave) {
		DataPoints.interleave = interleave;
	}


}
