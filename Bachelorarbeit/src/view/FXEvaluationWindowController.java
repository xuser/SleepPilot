package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.FeatureExtractionModel;
import model.RawDataModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXEvaluationWindowController implements Initializable {

	private FXApplicationController appController;
	private FXViewModel viewModel;
	private FeatureExtractionModel featureExtractionModel;
	private RawDataModel dataPointsModel;
	
	private Stage stage;
	
	@FXML private Label toolBarLabel;
	
	public FXEvaluationWindowController(RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel) {
		
		this.featureExtractionModel = featureExtractionModel;
		this.viewModel = viewModel;
		this.dataPointsModel = dataPointsModel;
		
		appController = viewModel.getAppController();
		
		stage = new Stage();
		BorderPane addGrid = new BorderPane();
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("EvaluationWindow.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			addGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading Hypnogramm.fxml file!");
			//e.printStackTrace();
		}
		
		Scene scene = new Scene(addGrid);
		
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.setTitle("Evaluation Window");
		
		toolBarLabel.setText("Experimentee: " + dataPointsModel.getOrgFile().getName());
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//Key Listener
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.E) {	
					appController.bringToFront();
				}
			}
		});
	}
	
	private void updateLabels() {
		
	}
	
	
	public void bringToFront() {
		stage.toFront();
	}
	
	public void reloadHypnogramm() {
		updateLabels();
	}
}
