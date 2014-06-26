package controller;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import view.ApplicationController;
import view.ChartController;
import view.StartController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.DataPoints;
import model.FeatureExtraxtionValues;
import model.TrainDataPoints;

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
	
	private static boolean trainMode;
	private static int numberOfDataPointsForOneEpoche;
	private static int numberOfEpochs;
	
	private static Stage primaryStage;
	
	private static DataPoints dataPointsModel;
	private static TrainDataPoints trainDataPointsModel;
	
	private static FeatureExtraxtionValues featureExtractionModel;
	private static StartController startController;
	
	private static boolean filterThreadStartedFlag = false;
	private static boolean featureExtractionThreadStartedFlag = false;
	private static boolean supportVectorMaschineThreadStartedFlag = false;
	private static boolean finishedClassificationFlag = false;
	
	
	/**
	 * The main entry point for all JavaFX applications. 
	 * The start method is called after the init method has returned, 
	 * and after the system is ready for the application to begin running.
	 */
	@Override
	public void start(final Stage stage) throws Exception {
		
		primaryStage = stage;
		
		// TODO: Ist hier nur testweise ausgeblendet.
		//Create start controller
		startController = new StartController(primaryStage);
		
		//Create application controller
//		ApplicationController appController = new ApplicationController(primaryStage);

		// Creating chart controller
		//new ChartController(primaryStage, dataPointsModel);
	
	}
	
	public static void startClassifier(File fileLocation, boolean trainMode, int numberOfDataPointsForOneEpoche, int numberOfEpochs) {
		// IMPORTANT: The absolute filepath on current filesystem is required.
//		try {
//			//fileLocation = args[0];
//			// TODO: These three parameters have to be set by the user!!!
//			trainMode = false;
//			numberOfDataPointsForOneEpoche = 3001;
//			numberOfEpochs = 17227;
//
//		} catch (Exception e) {
//			System.err.println("No valid file location for runtime argument.");
//		}

		if (trainMode == false) {

			// Creats a new controller which reads the declared file
			try {
				dataPointsModel = new DataPoints();

				// Start Data Reader Controller
				DataReaderController dataReaderController = new DataReaderController(
						fileLocation, dataPointsModel);
				dataReaderController.setPriority(10);
				dataReaderController.start();

				// Start Filter Controller
				FilterController filterController = new FilterController(
						dataPointsModel, dataReaderController);
				filterController.setPriority(9);

				while (filterThreadStartedFlag == false) {
					if (dataPointsModel.getRowInSampleFile() >= 1) {
//						startController.setProgressBar(0.18);
						filterController.start();

						filterThreadStartedFlag = true;
					}
				}

				// During hole reading process we pause the reading process to
				// avoid nullpointer exceptions.
				while (dataPointsModel.getReadingCompleteStatus() == false) {

					// Synchronize the access on readed data.
					if (((dataPointsModel.getRowInSampleFile() + 1) % 4) == 0) {
						synchronized (filterController) {
							filterController.proceed();
						}
					} else {
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
				FeatureExtractionController featureExtractionController = new FeatureExtractionController(
						dataPointsModel, featureExtractionModel, null,
						trainMode);
				featureExtractionController.setPriority(8);

				while (featureExtractionThreadStartedFlag == false) {
					if (dataPointsModel.getRowFilteredValues() > (dataPointsModel
							.getSamplingRateConvertedToHertz() * 30)) {
//						startController.setProgressBar(0.38);
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
				SupportVectorMaschineController svmController = new SupportVectorMaschineController(
						featureExtractionModel, false);
				svmController.setPriority(7);

				while (supportVectorMaschineThreadStartedFlag == false) {
					if (featureExtractionModel.getNumberOfcalculatedEpoch() >= dataPointsModel
							.getNumberOf30sEpochs()) {
						
//						startController.setProgressBar(0.69);
						svmController.start();

						supportVectorMaschineThreadStartedFlag = true;
					}
				}
				
				while (finishedClassificationFlag == false) {
					if (featureExtractionModel.getClassificationDone() == true) {
//						startController.setProgressBar(1.0);
//						startController.setProgressIndicator(1.0);
						
						finishedClassificationFlag = true;
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.err.println("Error by taking thread to sleep for one second.");
//						e.printStackTrace();
					}
					
					
					
				}

			} catch (IOException e) {
				System.err
						.println("Unexpected error occured during reading the file.");
				e.printStackTrace();
			}
		} else {

			FeatureExtraxtionValues featureExtractionModel = new FeatureExtraxtionValues();

			// 1 Column for the PE of one channel and 11 columns for the LPC
			// coefficients
			featureExtractionModel.createDataMatrix(numberOfEpochs, (1 + 11));

			// Start/ Create Train Data Reader Controller
			TrainController trainController = new TrainController(
					trainDataPointsModel, fileLocation.getAbsolutePath(),
					numberOfDataPointsForOneEpoche, numberOfEpochs,
					featureExtractionModel);
			trainController.setPriority(10);
			trainController.start();

			// Start Support Vector Maschine Controller
			SupportVectorMaschineController svmController = new SupportVectorMaschineController(
					featureExtractionModel, true);
			svmController.setPriority(9);

			while (supportVectorMaschineThreadStartedFlag == false) {
				if (featureExtractionModel.getReadingAndCalculatingDone() == true) {

					svmController.start();
					supportVectorMaschineThreadStartedFlag = true;
				}
			}

		}
	}

	/**
	 * Starts the application with the needed parameters.
	 * 
	 * @param args
	 * 			no starting arguments are needed.
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		launch(args);
		
	}

}
