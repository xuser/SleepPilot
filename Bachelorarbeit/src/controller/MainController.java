package controller;

import java.io.IOException;

import javafx.application.Application;
import view.ChartController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.DataPoints;
import model.FeatureExtraxtionValues;

/**
 * Starts the application and creates necessary initial controllers.
 * 
 * @author Nils Finke
 */
public class MainController extends Application {
	
	/**
	 * Filepath of .vhdr header file.
	 */
	private static String fileLocation;
	
	private static Stage primaryStage;
	
	private static DataPoints dataPointsModel;
	
	private static FeatureExtraxtionValues featureExtractionModel;
	
	private static boolean filterThreadStartedFlag = false;
	private static boolean featureExtractionThreadStartedFlag = false;
	private static boolean supportVectorMaschineThreadStartedFlag = false;
	
	/**
	 * The main entry point for all JavaFX applications. 
	 * The start method is called after the init method has returned, 
	 * and after the system is ready for the application to begin running.
	 */
	@Override
	public void start(final Stage stage) throws Exception {
		
		primaryStage = stage;
		// Creating chart controller
//		new ChartController(primaryStage, dataPointsModel);
	
	}

	/**
	 * Starts the application with the needed parameters.
	 * 
	 * @param args
	 * 			no starting arguments are needed.
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		// IMPORTANT: The absolute filepath on current filesystem is required.
		try {
			fileLocation = args[0];
		} catch (Exception e) {
			System.err.println("No valid file location for runtime argument.");
		}
		
		// Creats a new controller which reads the declared file
		try {
			dataPointsModel = new DataPoints();
			
			// Start Data Reader Controller
			DataReaderController dataReaderController = new DataReaderController(fileLocation, dataPointsModel);
			dataReaderController.setPriority(10);
			dataReaderController.start();
			
			// Start Filter Controller
			FilterController filterController = new FilterController(dataPointsModel, dataReaderController);
			filterController.setPriority(9);
			
			while (filterThreadStartedFlag == false) {
				if (dataPointsModel.getRowInSampleFile() >= 1) {
					filterController.start();		
					
					filterThreadStartedFlag = true;
				}
			}
			
			// During hole reading process we pause the reading process to avoid nullpointer exceptions.
			while(dataPointsModel.getReadingCompleteStatus() == false) {
				
				// Synchronize the access on readed data.
				if (((dataPointsModel.getRowInSampleFile() + 1) % 4) == 0) {
					synchronized (filterController) {
						filterController.proceed();
					}
				}		
				 else {
					synchronized (filterController) {
						filterController.pause();
					}
					
				}		
			}
			
			synchronized (filterController) {
				filterController.proceed();
			}
			
			// Start Feature Extraction Controller
			featureExtractionModel = new FeatureExtraxtionValues();
			FeatureExtractionController featureExtractionController = new FeatureExtractionController(dataPointsModel, featureExtractionModel);
			featureExtractionController.setPriority(8);
			
			while (featureExtractionThreadStartedFlag == false) {
				if (dataPointsModel.getRowFilteredValues() > (dataPointsModel.getSamplingRateConvertedToHertz() * 30)) {
					featureExtractionController.start();
					
					featureExtractionThreadStartedFlag = true;
				}
			}
			
			while (dataPointsModel.getFilteringComplete() == false) {
				synchronized (featureExtractionController) {
					featureExtractionController.pause();
				}
			}
			
			synchronized (featureExtractionController) {
				featureExtractionController.proceed();
			}
			
			
			// Start Support Vector Maschine Controller
			SupportVectorMaschineController svmController = new SupportVectorMaschineController(featureExtractionModel, false);
			svmController.setPriority(7);
			
			while(supportVectorMaschineThreadStartedFlag == false) {
				if (featureExtractionModel.getNumberOfcalculatedEpoch() >= dataPointsModel.getNumberOf30sEpochs()) {
					svmController.start();
					
					supportVectorMaschineThreadStartedFlag = true;
				}
			}
			
			
			launch(args);
			
		} catch (IOException e) {
			System.err.println("Unexpected error occured during reading the file.");
			e.printStackTrace();
		}
		
	}

}
