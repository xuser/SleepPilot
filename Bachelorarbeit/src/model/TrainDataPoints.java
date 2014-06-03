package model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds training data.
 * IMPORTANT: This model keeps just the current epoch. After calculating the features
 * the current data will be deleted.
 * 
 * @author Nils Finke
 */
public class TrainDataPoints {
	
	/**
	 * Holds all samples from the current epoch.
	 */
	private List<Double> samplesFromCurrentEpoch = new LinkedList<Double>();
	
	/**
	 * The number of the current epoch.
	 */
	private int currentEpoch = 0;
	
	/**
	 * If this flag is false the TrainDataReaderController is still busy with reading
	 * the current epoch. If the flag is true the current epoch has been read.
	 */
	private boolean epochHasBeenReadFlag = false;
	
	/**
	 * If this flag is false the TranDataReaderController is still busy with reading the
	 * training file. If the flag is true reading has been finished.
	 */
	private boolean readingHasBeenFinishedFlag = false;
	
	/**
	 * @return	the list with the samples from the current epoch.
	 */
	public List<Double> getSamplesFromCurrentEpoch() {
		return samplesFromCurrentEpoch;
	}
	
	/**
	 * Add sample to list.
	 * @param sample	value which have to be set.
	 */
	public void setSamplesFromCurrentEpoch(Double sample) {
		samplesFromCurrentEpoch.add(sample);
	}
	
	/**
	 * Clears the list with the samples for the current epoch.
	 */
	public void clearSampleList() {
		samplesFromCurrentEpoch.clear();
	}
	
	/**
	 * @return	the current number of samples in the list.
	 */
	public int getSizeOfSampleList() {
		return samplesFromCurrentEpoch.size();
	}
	
	/**
	 * If this flag is false the TrainDataReaderController is still busy with reading
	 * the current epoch. If the flag is true the current epoch has been read.
	 * 
	 * @param status
	 * 			the current status.
	 */
	public void setEpochHasBeenReadFlag(boolean status) {
		epochHasBeenReadFlag = status;
	}
	
	/**
	 * @return	the current EpochHasBeenRead status flag. If this flag is false the TrainDataReaderController is still busy with reading
	 * the current epoch. If the flag is true the current epoch has been read.
	 */
	public boolean getEpochHasBeenReadFlag() {
		return epochHasBeenReadFlag;
	}
	
	/**
	 * If this flag is false the TranDataReaderController is still busy with reading the
	 * training file. If the flag is true reading has been finished.
	 * 
	 * @param status
	 * 			the current status.
	 */
	public void setReadingHasBeenFinishedFlag(boolean status) {
		readingHasBeenFinishedFlag = status;
	}
	
	/**
	 * @return	the current ReadingHasBeenFinished status flag. If this flag is false the TranDataReaderController is still busy with reading the
	 * training file. If the flag is true reading has been finished.
	 */
	public boolean getReadingHasBeenFinishedFlag() {
		return readingHasBeenFinishedFlag;
	}
	
	/**
	 * Set the number of the epoch which is currently read.
	 * @param number
	 */
	public void setNumberOfCurrentEpoch(int number) {
		currentEpoch = number;
	}
	
	/**
	 * @return	the number of the epoch which have been read currently.
	 */
	public int getNumberOfCurrentEpoch() {
		return currentEpoch;
	}
	
	
}
