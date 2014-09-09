package model;

import java.io.File;
import java.io.RandomAccessFile;
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
	
	private RandomAccessFile dataFile;
	
	private File orgFile;
	
	/**
	 * This variable holds the status if the reader has finsihed his work
	 */
	private boolean readingComplete = false;
	
	private boolean filteringComplete = false;
	
	private boolean readingHeaderComplete = false;
	
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
	private int numberOfChannels = 0;					// This variable will be used for the Spike2 Format as well
	private int samplingIntervall;
	
	// [Channel Infos]
	private String[] channelNames;
	
	// *** Brainvision Format ***
	
	private int samplingRateConvertedToHertz;
	
	// *** Spike2 Format ***
	
	// Information from File Header
	private int systemId;
	private int usPerTime;
	private int timePerADC;
	private int fileState;
	private int firstData;
	private int channels;
	private int chanSize;
	private int extraData;
	private int bufferSize;
	private int osFormat;
	private int maxFTime;
	private double dTimeBase;

	// Information from Channel Header

	// We dont know the exact number of channels. This is why we create lists,
	// which will be filled up. Example: All information for channel one are in
	// list position zero etc..
	private LinkedList<Integer> delSize = new LinkedList<Integer>();
	private LinkedList<Integer> nextDelBlock = new LinkedList<Integer>();
	private LinkedList<Integer> firstBlock = new LinkedList<Integer>();
	private LinkedList<Integer> lastBlock = new LinkedList<Integer>();
	private LinkedList<Integer> blocks = new LinkedList<Integer>();
	private LinkedList<Integer> nExtra = new LinkedList<Integer>();
	private LinkedList<Integer> preTrig = new LinkedList<Integer>();
	private LinkedList<Integer> free0 = new LinkedList<Integer>();
	private LinkedList<Integer> phySz = new LinkedList<Integer>();
	private LinkedList<Integer> maxData = new LinkedList<Integer>();
	private LinkedList<Integer> maxChanTime = new LinkedList<Integer>();
	private LinkedList<Integer> lChanDvd = new LinkedList<Integer>();
	private LinkedList<Integer> phyChan = new LinkedList<Integer>();
	private LinkedList<String> titel = new LinkedList<String>();
	private LinkedList<Float> idealRate = new LinkedList<Float>();
	private LinkedList<Integer> kind = new LinkedList<Integer>();
	private LinkedList<Integer> pad = new LinkedList<Integer>();
	private LinkedList<Float> scale = new LinkedList<Float>();
	private LinkedList<Float> offset = new LinkedList<Float>();
	private LinkedList<Integer> divide = new LinkedList<Integer>();
	private LinkedList<Integer> interleave = new LinkedList<Integer>();
	private LinkedList<Integer> sizeOfLastBlock = new LinkedList<Integer>();
	
	/**
	 * The position in the list corresponds to the epoch. The entry is the channel position in the file.
	 */
	private LinkedList<Integer> posOfEpochsInFile = new LinkedList<Integer>();
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
	public int getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}

	/**
	 * @return the usPerTime
	 */
	public int getUsPerTime() {
		return usPerTime;
	}

	/**
	 * @param usPerTime the usPerTime to set
	 */
	public void setUsPerTime(int usPerTime) {
		this.usPerTime = usPerTime;
	}

	/**
	 * @return the timePerADC
	 */
	public int getTimePerADC() {
		return timePerADC;
	}

	/**
	 * @param timePerADC the timePerADC to set
	 */
	public void setTimePerADC(int timePerADC) {
		this.timePerADC = timePerADC;
	}

	/**
	 * @return the fileState
	 */
	public int getFileState() {
		return fileState;
	}

	/**
	 * @param fileState the fileState to set
	 */
	public void setFileState(int fileState) {
		this.fileState = fileState;
	}

	/**
	 * @return the firstData
	 */
	public int getFirstData() {
		return firstData;
	}

	/**
	 * @param firstData the firstData to set
	 */
	public void setFirstData(int firstData) {
		this.firstData = firstData;
	}

	/**
	 * @return the channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}

	/**
	 * @return the chanSize
	 */
	public int getChanSize() {
		return chanSize;
	}

	/**
	 * @param chanSize the chanSize to set
	 */
	public void setChanSize(int chanSize) {
		this.chanSize = chanSize;
	}

	/**
	 * @return the extraData
	 */
	public int getExtraData() {
		return extraData;
	}

	/**
	 * @param extraData the extraData to set
	 */
	public void setExtraData(int extraData) {
		this.extraData = extraData;
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the osFormat
	 */
	public int getOsFormat() {
		return osFormat;
	}

	/**
	 * @param osFormat the osFormat to set
	 */
	public void setOsFormat(int osFormat) {
		this.osFormat = osFormat;
	}

	/**
	 * @return the maxFTime
	 */
	public int getMaxFTime() {
		return maxFTime;
	}

	/**
	 * @param maxFTime the maxFTime to set
	 */
	public void setMaxFTime(int maxFTime) {
		this.maxFTime = maxFTime;
	}

	/**
	 * @return the dTimeBase
	 */
	public double getdTimeBase() {
		return dTimeBase;
	}

	/**
	 * @param dTimeBase the dTimeBase to set
	 */
	public void setdTimeBase(double dTimeBase) {
		this.dTimeBase = dTimeBase;
	}

	/**
	 * @return the delSize element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getDelSize(int pos) {
		return delSize.get(pos);
	}

	/**
	 * @param element the delSize element to set
	 */
	public void addDelSize(int element) {
		this.delSize.add(element);
	}

	/**
	 * @return the nextDelBlock element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getNextDelBlock(int pos) {
		return nextDelBlock.get(pos);
	}

	/**
	 * @param nextDelBlock the nextDelBlock to set
	 */
	public void addNextDelBlock(int element) {
		this.nextDelBlock.add(element);
	}

	/**
	 * @return the firstBlock element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getFirstBlock(int pos) {
		return firstBlock.get(pos);
	}

	/**
	 * @param firstBlock the firstBlock to set
	 */
	public void addFirstBlock(int element) {
		this.firstBlock.add(element);
	}

	/**
	 * @return the lastBlock element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getLastBlock(int pos) {
		return lastBlock.get(pos);
	}

	/**
	 * @param lastBlock the lastBlock to set
	 */
	public void addLastBlock(int element) {
		this.lastBlock.add(element);
	}

	/**
	 * @return the blocks element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getBlocks(int pos) {
		return blocks.get(pos);
	}

	/**
	 * @param blocks the blocks to set
	 */
	public void addBlocks(int element) {
		this.blocks.add(element);
	}

	/**
	 * @return the nExtra element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getnExtra(int pos) {
		return nExtra.get(pos);
	}

	/**
	 * @param nExtra the nExtra to set
	 */
	public void addnExtra(int element) {
		this.nExtra.add(element);
	}

	/**
	 * @return the preTrig element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getPreTrig(int pos) {
		return preTrig.get(pos);
	}

	/**
	 * @param preTrig the preTrig to set
	 */
	public void addPreTrig(int element) {
		this.preTrig.add(element);
	}

	/**
	 * @return the free0 element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getFree0(int pos) {
		return free0.get(pos);
	}

	/**
	 * @param free0 the free0 to set
	 */
	public void addFree0(int element) {
		this.free0.add(element);
	}

	/**
	 * @return the phySz element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getPhySz(int pos) {
		return phySz.get(pos);
	}

	/**
	 * @param phySz the phySz to set
	 */
	public void addPhySz(int element) {
		this.phySz.add(element);
	}

	/**
	 * @return the maxData element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getMaxData(int pos) {
		return maxData.get(pos);
	}

	/**
	 * @param maxData the maxData to set
	 */
	public void addMaxData(int element) {
		this.maxData.add(element);
	}

	/**
	 * @return the maxChanTime element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getMaxChanTime(int pos) {
		return maxChanTime.get(pos);
	}

	/**
	 * @param maxChanTime the maxChanTime to set
	 */
	public void addMaxChanTime(int element) {
		this.maxChanTime.add(element);
	}

	/**
	 * @return the lChanDvd element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getlChanDvd(int pos) {
		return lChanDvd.get(pos);
	}

	/**
	 * @param lChanDvd the lChanDvd to set
	 */
	public void addlChanDvd(int element) {
		this.lChanDvd.add(element);
	}

	/**
	 * @return the phyChan element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getPhyChan(int pos) {
		return phyChan.get(pos);
	}

	/**
	 * @param phyChan the phyChan to set
	 */
	public void addPhyChan(int element) {
		this.phyChan.add(element);
	}

	/**
	 * @return the titel element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public String getTitel(int pos) {
		return titel.get(pos);
	}

	/**
	 * @param titel the titel to set
	 */
	public void addTitel(String element) {
		this.titel.add(element);
	}

	/**
	 * @return the idealRate element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public float getIdealRate(int pos) {
		return idealRate.get(pos);
	}

	/**
	 * @param idealRate the idealRate to set
	 */
	public void addIdealRate(float element) {
		this.idealRate.add(element);
	}

	/**
	 * @return the kind element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getKind(int pos) {
		return kind.get(pos);
	}

	/**
	 * @param kind the kind to set
	 */
	public void addKind(int element) {
		this.kind.add(element);
	}
	
	/**
	 * @return the whole LinkedList of Kinds
	 */
	public LinkedList<Integer> getListOfKind() {
		return kind;
	}

	/**
	 * @return the pad element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getPad(int pos) {
		return pad.get(pos);
	}

	/**
	 * @param pad the pad to set
	 */
	public void addPad(int element) {
		this.pad.add(element);
	}

	/**
	 * @return the scale element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public float getScale(int pos) {
		return scale.get(pos);
	}

	/**
	 * @param scale the scale to set
	 */
	public void addScale(float element) {
		this.scale.add(element);
	}

	/**
	 * @return the offset element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public float getOffset(int pos) {
		return offset.get(pos);
	}

	/**
	 * @param offset the offset to set
	 */
	public void addOffset(float element) {
		this.offset.add(element);
	}

	/**
	 * @return the divide element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getDivide(int pos) {
		return divide.get(pos);
	}

	/**
	 * @param divide the divide to set
	 */
	public void addDivide(int element) {
		this.divide.add(element);
	}

	/**
	 * @return the interleave element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getInterleave(int pos) {
		return interleave.get(pos);
	}

	/**
	 * @param interleave the interleave to set
	 */
	public void addInterleave(int element) {
		this.interleave.add(element);
	}
	
	/**
	 * @return the sizeOfLastBlock element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getSizeOfLastBlock(int pos) {
		return sizeOfLastBlock.get(pos);
	}

	/**
	 * @param sizeOfLastBlock the sizeOfLastBlock to set
	 */
	public void addSizeOfLastBlock(int element) {
		this.sizeOfLastBlock.add(element);
	}
	
	/**
	 * @return the posOfEpochsInFile element at the specified pos
	 * @param pos
	 * 			the position in LinkedList
	 */
	public int getPosOfEpochsInFile(int pos) {
		return posOfEpochsInFile.get(pos);
	}

	/**
	 * @param posOfEpochsInFile the posOfEpochsInFile to set
	 */
	public void addPosOfEpochsInFile(int element) {
		this.posOfEpochsInFile.add(element);
	}

	/**
	 * @return the readingHeaderComplete
	 */
	public boolean isReadingHeaderComplete() {
		return readingHeaderComplete;
	}

	/**
	 * @param readingHeaderComplete the readingHeaderComplete to set
	 */
	public void setReadingHeaderComplete(boolean readingHeaderComplete) {
		this.readingHeaderComplete = readingHeaderComplete;
	}

	/**
	 * @return the dataFile
	 */
	public RandomAccessFile getDataFile() {
		return dataFile;
	}

	/**
	 * @param dataFile the dataFile to set
	 */
	public void setDataFile(RandomAccessFile dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * @return the orgFile
	 */
	public File getOrgFile() {
		return orgFile;
	}

	/**
	 * @param orgFile the orgFile to set
	 */
	public void setOrgFile(File orgFile) {
		this.orgFile = orgFile;
	}


}
