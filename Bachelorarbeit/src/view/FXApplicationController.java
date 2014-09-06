package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controller.DataReaderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXApplicationController implements Initializable{
	
	private Stage primaryStage;
	private DataReaderController dataReaderController;
	
	private BorderPane mainGrid;
	
	private Scene scene;
	
	@FXML MenuItem showAdtVisualization;
	
	public FXApplicationController(DataReaderController dataReaderController) {
		primaryStage = new Stage();
		this.dataReaderController = dataReaderController;
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Application.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			mainGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading Application.fxml file!");
			//e.printStackTrace();
		}
		
		// Create stage with mainGrid
		scene = new Scene(mainGrid);
		primaryStage.setScene(scene);
		
		//Properties for stage
		primaryStage.setResizable(true);
		primaryStage.show();
		primaryStage.setTitle("Automatic Sleep Staging - Application");
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
	
	@FXML
	protected void showAdtVisualizationAction() {
		
		Stage stage = new Stage();
		BorderPane addGrid = new BorderPane();
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("AdditionalVisualization.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			addGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading AdditionalVisualization.fxml file!");
			//e.printStackTrace();
		}
		
		Scene scene = new Scene(addGrid);
		
		stage.setScene(scene);
		stage.show();
		stage.setTitle("Additional Visualization");
		
	}
}
