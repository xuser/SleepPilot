package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	private ObjectOutputStream oos = null;
	private FileOutputStream fos = null;
	
	public ModelReaderWriterController(RawDataModel rawDataModel, FeatureExtractionModel featureExtractionModel, File file) {
	
		this.rawDataModel = rawDataModel;
		this.featureExtractionModel = featureExtractionModel;
		this.file = file;
		
	}
	
	
	public void run() {
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			
//			oos.writeObject(rawDataModel);
			oos.writeObject(featureExtractionModel);
			
		} catch (IOException e) {
			System.err.println("Error occured during saving models!");
//			e.printStackTrace();
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
	}
	

	
	/**
	 * This method starts the Data Write Thread.
	 */
	public void start() {
		System.out.println("Starting Data Writer Thread");
		if (t == null) {
			t = new Thread(this, "DataWrite");
			t.start();
		}
	}

	

}
