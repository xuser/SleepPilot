package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.FeatureExtractionModel;
import model.RawDataModel;

/**
 * This controller is used to write the current system state to hard disk.
 * 
 * @author Nils Finke
 */
public class ModelReaderWriterController extends Thread{
	
	private Thread t;
	
	private RawDataModel rawDataModel;
	private FeatureExtractionModel featureExtractionModel;
	private File file;
	
	/**
	 * True, if Writing or 
	 * False, if Reading
	 */
	private boolean readWriteFlag;
	
	private ObjectOutputStream oos = null;
	private FileOutputStream fos = null;
	
	private ObjectInputStream ois = null;
	private FileInputStream fis = null;
	
	public ModelReaderWriterController(RawDataModel rawDataModel, FeatureExtractionModel featureExtractionModel, File file, boolean readWriteFlag) {
	
		this.rawDataModel = rawDataModel;
		this.featureExtractionModel = featureExtractionModel;
		this.file = file;
		this.readWriteFlag = readWriteFlag;
		
	}
	
	/**
	 * Read or write model to hard disk depending on the readWriteFlag.
	 */
	public void run() {
		
		//WRITING
		if (readWriteFlag) {
			try {
				fos = new FileOutputStream(file);
				oos = new ObjectOutputStream(fos);
				
	//			oos.writeObject(rawDataModel);
				oos.writeObject(featureExtractionModel);
				
			} catch (IOException e) {
				System.err.println("Error occured during saving models!");
			}
	
			finally {
				if (oos != null)
					try {
						oos.close();
					} catch (IOException e) {
						System.err.println("Error occured during closing OOS!");
					}
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						System.err.println("Error occured during closing FOS!");
	
					}
			}
			
			System.out.println("Finished writing model on hard disk.");
		
		// READING
		} else {
			
			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				Object obj = ois.readObject();
				
				if (obj instanceof FeatureExtractionModel) {
										
					FeatureExtractionModel fem = (FeatureExtractionModel) obj;
					
					featureExtractionModel.setFeatures(fem.getFeatures());
                                        featureExtractionModel.setLabels(fem.getLabels());
                                        featureExtractionModel.setArousals(fem.getArousals());
                                        featureExtractionModel.setArtefacts(fem.getArtefacts());
					featureExtractionModel.setEpochProperties(fem.getEpochProperties());
					featureExtractionModel.setPredictProbabilities(fem.getPredictProbabilities());
					featureExtractionModel.setSelectedModel(fem.getSelectedModel());
					featureExtractionModel.setNumberOfChannels(fem.getNumberOfChannels());
					featureExtractionModel.setClassificationDone(fem.isClassificationDone());
					featureExtractionModel.setLengthOfOneEpoch(fem.getLengthOfOneEpoch());
					featureExtractionModel.setNumberOfEpochs(fem.getNumberOfEpochs());
					featureExtractionModel.setChannelNames(fem.getChannelNames());
					featureExtractionModel.setAutoMode(fem.isAutoMode());
					featureExtractionModel.setFileLocation(fem.getFileLocation());
					
					
					MainController.recreateSystemState(file);
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (ois != null)
					try {
						ois.close();
					} catch (IOException e) {
						System.err.println("Error occured during closing OIS!");
					}
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						System.err.println("Error occured during closing FIS!");
					}
			}
			
			System.out.println("Finished reading model from hard disk.");
		}
	}
	

	
	/**
	 * This method starts the Data Read/Write Thread.
	 */
	public void start() {
		System.out.println("Starting Data Reader/Writer Thread");
		if (t == null) {
			t = new Thread(this, "DataReadWrite");
			t.start();
		}
	}

	

}
