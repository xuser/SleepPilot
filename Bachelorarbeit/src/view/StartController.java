package view;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import controller.MainController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartController implements Initializable {
	
	//MainController mainController;

	private boolean trainMode = false;
	private int amountOfEpochs;
	private int numberOfDataPointsForOneEpoche;	
	
	
	// JavaFx components
	private Stage primaryStage;
	
	private AnchorPane mainGrid;
	
	private Scene scene;
	
	private File file;
		
	@FXML RadioButton trainModeOn;
	@FXML RadioButton trainModeOff;
	@FXML TextField lengthOfEpoch;
	@FXML TextField numberOfEpochs;
	@FXML Button selectFile;
	@FXML Button start;
	@FXML ProgressBar progressBar;
	@FXML ProgressIndicator progressIndicator;
	@FXML Text readingLabel;
	@FXML Text filteringLabel;
	@FXML Text featureExtractionLabel;
	@FXML Text classificationLabel;
	@FXML Label fileHint;
	
	
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
		start.setDisable(true);
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		selectFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				
				if (trainMode == true) {
					// Set extension filter
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"TXT files (*.txt)", "*.txt");
					fileChooser.getExtensionFilters().add(extFilter);
				} else {
					// Set extension filter
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"BrainVision files (*.vhdr)", "*.vhdr");
					fileChooser.getExtensionFilters().add(extFilter);
				}
				
				// Show open file dialog
				file = fileChooser.showOpenDialog(null);
				
				if (file != null) {
					fileHint.setText(file.getName() + " selected!");
					start.setDisable(false);
				}
				
			}
		
		});
		
	}
	
	@FXML
	protected void trainModeOnAction() {
		if (trainModeOn.isSelected()) {
			trainMode = true;
			lengthOfEpoch.setDisable(false);
			numberOfEpochs.setDisable(false);
		}
	}
	
	@FXML
	protected void trainModeOffAction() {
		if (trainModeOff.isSelected()) {
			trainMode = false;
			lengthOfEpoch.setDisable(true);
			numberOfEpochs.setDisable(true);
		}
	}
	
	
	@FXML
	protected void startAction() {
		if (trainMode == true) {
			amountOfEpochs = Integer.parseInt(numberOfEpochs.getText());
			numberOfDataPointsForOneEpoche = Integer.parseInt(lengthOfEpoch.getText());
		}
		
		progressIndicator.setProgress(-1);
		
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				MainController.startClassifier(file, trainMode, numberOfDataPointsForOneEpoche, amountOfEpochs);
				return null;
			}

		};
		
		new Thread(task).start();
	}
	
	
	public void setProgressBar(double value) {
		progressBar.setProgress(value);
	}
	
	public void setProgressIndicator(double value) {
		progressIndicator.setProgress(value);
	}
	
}
