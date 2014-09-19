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
	
	@FXML private Label awakePer;
	@FXML private Label s1Per;
	@FXML private Label s2Per;
	@FXML private Label nPer;
	@FXML private Label remPer;
	@FXML private Label artefactPer;
	@FXML private Label arrousalPer;
	@FXML private Label stimulationPer;
	
	@FXML private Label awakeMin;
	@FXML private Label s1Min;
	@FXML private Label s2Min;
	@FXML private Label nMin;
	@FXML private Label remMin;
	@FXML private Label artefactMin;
	@FXML private Label arrousalMin;
	@FXML private Label stimulationMin;
	
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
	
	public void updateLabels() {
		
		double epochs = dataPointsModel.getNumberOf30sEpochs();
		System.out.println("#Epochs: " + epochs);
		
		double awakePerD = featureExtractionModel.getCountWake();
		System.out.println("#awake: " + awakePerD);
		
		double awakeMinD = (awakePerD * 30.0) / 60;
		System.out.println("#awakeMin: " + awakeMinD);
		
		awakePerD = (awakePerD / epochs) * 100.0;
		System.out.println("#awakePer: " + awakePerD);
		
		awakePer.setText(awakePerD + "%");
		awakeMin.setText(awakeMinD + " min");
		
		double s1PerD = featureExtractionModel.getCountS1();
		System.out.println("#s1: " + s1PerD);
		
		double s1MinD = (s1PerD * 30.0) / 60;
		System.out.println("#s1Min: " + s1MinD);
		
		s1PerD = (s1PerD / epochs) * 100.0;
		System.out.println("#s1Per: " + s1PerD);
		
		s1Per.setText(s1PerD + "%");
		s1Min.setText(s1MinD + " min");
	}
	
	
	public void bringToFront() {
		stage.toFront();
	}
	
	public void reloadHypnogramm() {
		updateLabels();
	}
}
