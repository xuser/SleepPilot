package view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class StartController {
	
	private Stage primaryStage;
	
	private AnchorPane mainGrid;
	
	private Scene scene;
	
	@FXML RadioButton trainModeOn;
	@FXML RadioButton trainModeOff;
	@FXML TextField lengthOfEpoch;
	@FXML TextField numberOfEpochs;
	@FXML Button start;
	
	public StartController(Stage stage) {
		
		primaryStage = stage;
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(StartController.class.getResource("Start.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			mainGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading Start.fxml file!");
			e.printStackTrace();
		}
		
		// Create stage with mainGrid
		scene = new Scene(mainGrid);
		primaryStage.setScene(scene);
		
		//Properties for stage
		primaryStage.setResizable(false);
		primaryStage.show();
		primaryStage.setTitle("Automatic Sleep Staging - Settings");
		
		// Start settings
		lengthOfEpoch.setDisable(true);
		numberOfEpochs.setDisable(true);
	}
	
	@FXML
	protected void trainModeOnAction() {
		if (trainModeOn.isSelected()) {
			lengthOfEpoch.setDisable(false);
			numberOfEpochs.setDisable(false);
		}
	}
	
	@FXML
	protected void trainModeOffAction() {
		if (trainModeOff.isSelected()) {
			lengthOfEpoch.setDisable(true);
			numberOfEpochs.setDisable(true);
		}
	}
	
	@FXML
	protected void startAction() {
		
	}
	
	
}
