package view;

import java.io.IOException;
import java.math.BigDecimal;
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
import javafx.stage.WindowEvent;

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
	
	@FXML private Label totalCount;
	
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
		updateLabels();
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		// Event will be fired when closing the application
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				viewModel.setEvaluationWindowActive(false);
				System.out.println("EvaluationWindow is closing.");
			}			
		
		});
		
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
		
		double epochs = dataPointsModel.getNumberOf30sEpochs();
		
		double awakePerD = featureExtractionModel.getCountWake();
		double awakeMinD = (awakePerD * 30.0) / 60;
		awakePerD = (awakePerD / epochs) * 100.0;	
		awakePer.setText(roundValues(awakePerD) + "%");
		awakeMin.setText(roundValues(awakeMinD) + " min");
		
		double s1PerD = featureExtractionModel.getCountS1();
		double s1MinD = (s1PerD * 30.0) / 60;
		s1PerD = (s1PerD / epochs) * 100.0;		
		s1Per.setText(roundValues(s1PerD) + "%");
		s1Min.setText(roundValues(s1MinD) + " min");
		
		double s2PerD = featureExtractionModel.getCountS2();
		double s2MinD = (s2PerD * 30.0) / 60;
		s2PerD = (s2PerD / epochs) * 100.0;
		s2Per.setText(roundValues(s2PerD) + "%");
		s2Min.setText(roundValues(s2MinD) + " min");
		
		double nPerD = featureExtractionModel.getCountN();
		double nMinD = (nPerD * 30.0) / 60;
		nPerD = (nPerD / epochs) * 100.0;
		nPer.setText(roundValues(nPerD) + "%");
		nMin.setText(roundValues(nMinD) + " min");
		
		double remPerD = featureExtractionModel.getCountREM();
		double remMinD = (remPerD * 30.0) / 60;
		remPerD = (remPerD / epochs) * 100.0;
		remPer.setText(roundValues(remPerD) + "%");
		remMin.setText(roundValues(remMinD) + " min");
		
		double aPerD = featureExtractionModel.getCountA();
		double aMinD = (aPerD * 30.0) / 60;
		aPerD = (aPerD / epochs) * 100.0;
		artefactPer.setText(roundValues(aPerD) + "%");
		artefactMin.setText(roundValues(aMinD) + " min");
		
		double maPerD = featureExtractionModel.getCountMA();
		double maMinD = (maPerD * 30.0) / 60;
		maPerD = (maPerD / epochs) * 100.0;
		arrousalPer.setText(roundValues(maPerD) + "%");
		arrousalMin.setText(roundValues(maMinD) + " min");
		
		double sPerD = featureExtractionModel.getCountS();
		double sMinD = (sPerD * 30.0) / 60;
		sPerD = (sPerD / epochs) * 100.0;
		stimulationPer.setText(roundValues(sPerD) + "%");
		stimulationMin.setText(roundValues(sMinD) + " min");
		
		double total = awakeMinD + s1MinD + s2MinD + nMinD + remMinD + sMinD;
		totalCount.setText(roundValues(total) + " min");
	}
	
	private double roundValues(double value) {
		BigDecimal myDec = new BigDecimal(value);
		myDec = myDec.setScale(1, BigDecimal.ROUND_HALF_UP);
		
		return myDec.doubleValue();
	}
	
	
	public void bringToFront() {
		stage.toFront();
	}
	
	public void reloadEvaluationWindow() {
		updateLabels();
	}
}
