package controller;

import help.ChannelNames;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javafx.application.Application;
import javafx.application.Platform;
import view.FXApplicationController;
import view.FXStartController;
import javafx.stage.Stage;
import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import model.TrainDataModel;

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

    private static Stage primaryStage;

    private static RawDataModel dataPointsModel;
    private static TrainDataModel trainDataPointsModel;

    private static FeatureExtractionModel featureExtractionModel;
    private static FXStartController startController;

    private static boolean filterThreadStartedFlag = false;
    private static boolean featureExtractionThreadStartedFlag = false;
    private static boolean supportVectorMaschineThreadStartedFlag = false;
    private static boolean finishedClassificationFlag = false;

    /**
     * The main entry point for all JavaFX applications. The start method is
     * called after the init method has returned, and after the system is ready
     * for the application to begin running.
     */
    @Override
    public void start(final Stage stage) throws Exception {

        primaryStage = stage;

        //Create start controller
        dataPointsModel = new RawDataModel();
        featureExtractionModel = new FeatureExtractionModel();
        startController = new FXStartController(primaryStage, dataPointsModel, featureExtractionModel);

        // Creating chart controller
        //new ChartController(primaryStage, dataPointsModel);
    }

    public static void startClassifier(File fileLocation, boolean trainMode, LinkedList<Integer> channelNumbersToRead, String[] channelNames, final boolean autoMode) {

        featureExtractionModel.setFileLocation(fileLocation);

        if (trainMode == false) {
            //removed
        } else {

            FeatureExtractionModel featureExtractionModel = new FeatureExtractionModel();

            // Start/ Create Train Data Reader Controller
            TrainController trainController = new TrainController(
                    trainDataPointsModel, fileLocation.getAbsolutePath(),
                    featureExtractionModel);
            trainController.setPriority(10);
            trainController.start();

            // Start Support Vector Maschine Controller
            SupportVectorMaschineController svmController = new SupportVectorMaschineController(
                    featureExtractionModel, true);
//            svmController.setPriority(9);

            while (supportVectorMaschineThreadStartedFlag == false) {
                if (featureExtractionModel.getReadingAndCalculatingDone() == true) {

                    svmController.start();
                    supportVectorMaschineThreadStartedFlag = true;
                }
            }

        }
    }

    public static void recreateSystemState(File file) throws IllegalArgumentException {

        try {
            String filePath = file.getParent() + File.separator + featureExtractionModel.getFileLocation().getName();
            System.out.println(filePath);

            File relativeFile = new File(filePath);

            if (relativeFile.canRead()) {

                final DataReaderController dataReaderController = new DataReaderController(relativeFile);

                //Create application controller
                System.out.println("AppController starting!");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        FXViewModel viewModel = new FXViewModel();
                        FXApplicationController appController = new FXApplicationController(dataReaderController, featureExtractionModel, viewModel, true);
                        viewModel.setAppController(appController);
                        primaryStage.close();
                    }
                });

            } else {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        startController.showPopUp("Can't find EEG Data! Put *.as file in the same directory like the EEG Data.");
                    }
                });
            }

        } catch (IOException e) {
            System.err.println("Error occured during recreation of the old system state!");
            e.printStackTrace();
        }
    }

    /**
     * Starts the application with the needed parameters.
     *
     * @param args no starting arguments are needed.
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        launch(args);

    }

}
