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
	
	/**
	 * The main entry point for all JavaFX applications. 
	 * The start method is called after the init method has returned, 
	 * and after the system is ready for the application to begin running.
	 */
	@Override
	public void start(final Stage stage) throws Exception {
		
		primaryStage = stage;
		// Creating chart controller
		new ChartController(primaryStage, dataPointsModel);
	
	}

	/**
	 * Starts the application with the needed parameters.
	 * 
	 * @param args
	 * 			no starting arguments are needed.
	 */
	public static void main(String[] args) {
		
		// IMPORTANT: The absolute filepath on current filesystem is required.
		try {
			fileLocation = args[0];			
		} catch (Exception e) {
			System.err.println("No valid file location for runtime argument.");
		}
		
		// Creats a new controller which reads the declared file
		try {
			dataPointsModel = new DataPoints();
			new DataReaderController(fileLocation, dataPointsModel);
			
		} catch (IOException e) {
			System.err.println("Unexpected error occured during reading the file.");
			e.printStackTrace();
		}
		
		launch(args);
	}

}
