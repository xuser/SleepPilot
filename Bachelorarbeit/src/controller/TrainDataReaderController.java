package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

import model.FeatureExtraxtionValues;
import model.TrainDataPoints;

/**
 * This controller is only for reading training data with special format
 * specification.
 * 
 * @author Nils Finke
 */
public class TrainDataReaderController extends Thread {
	
	private Thread t;
	private boolean fPause = false;
	
	private RandomAccessFile trainDataFile;
	
	private TrainDataPoints respectiveTrainDataPointsModel;
	private String fileLocationPath;
	private int numberOfDataPointsForOneEpoche;
	private int numberOfEpochs;
	private FeatureExtraxtionValues respectiveFeatureExtractionModel;
	private FeatureExtractionController featureExtractionController;
	
	private int numberOfReadSamples = 0;
	
	
	public TrainDataReaderController(TrainDataPoints trainModel, String fileLocation, int numberOfDataPointsForOneEpoche, int numberOfEpochs, FeatureExtraxtionValues featureExtractionModel, FeatureExtractionController featureExtractionController) {
		
		respectiveTrainDataPointsModel = trainModel;
		fileLocationPath = fileLocation;
		this.numberOfDataPointsForOneEpoche = numberOfDataPointsForOneEpoche;
		this.numberOfEpochs = numberOfEpochs;		
		respectiveFeatureExtractionModel = featureExtractionModel;
		this.featureExtractionController = featureExtractionController;
		
		try {
			trainDataFile = new RandomAccessFile(fileLocationPath, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			System.err.println("Could not find the TrainDataFile!");
		}
		
		// 1 Column for the PE of one channel and 11 columns for the LPC coefficients
		respectiveFeatureExtractionModel.createDataMatrix(numberOfEpochs, 1+11);
		
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
			
			// Just for testing:
			int test = 0;
			
			while (bytesRead != -1) {

				//Make buffer ready for read
				buf.flip();
				
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
					BigDecimal myDec = new BigDecimal(value);
					myDec = myDec.setScale(4, BigDecimal.ROUND_HALF_UP);
					value = myDec.doubleValue();
					
					respectiveTrainDataPointsModel.setSamplesFromCurrentEpoch(value);
					
					numberOfReadSamples++;
					
					if (numberOfReadSamples == numberOfDataPointsForOneEpoche) {
						test++;
						
						System.out.println("Reading Epoch: " + test);
						
//						System.out.println(numberOfReadSamples + " == " + numberOfDataPointsForOneEpoche);

						synchronized (this) {
							pause();
						}
						
						numberOfReadSamples = 0;
						respectiveTrainDataPointsModel.setEpochHasBeenReadFlag(true);
						
						synchronized (featureExtractionController) {
							featureExtractionController.proceed();
						}
							
					}
								

				}
				
				respectiveTrainDataPointsModel.setReadingHasBeenFinishedFlag(true);
				
				buf.clear(); //make buffer ready for writing
				bytesRead = inChannel.read(buf);
			}
			
			trainDataFile.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error during reading the training data file!");
		}
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
