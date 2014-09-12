package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.DataPoints;
import model.FeatureExtraxtionValues;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXHypnogrammController implements Initializable{

	private FeatureExtraxtionValues featureExtractionModel;
	private DataPoints dataPointsModel;
	
	Stage stage;
	
	@FXML LineChart<Number, Number> lineChart;
	@FXML Label toolBarLabel;
	
	public FXHypnogrammController(DataPoints dataPointsModel, FeatureExtraxtionValues featureExtractionModel) {
		
		this.featureExtractionModel = featureExtractionModel;
		this.dataPointsModel = dataPointsModel;
		
		stage = new Stage();
		BorderPane addGrid = new BorderPane();
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Hypnogramm.fxml"));
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
		stage.setTitle("Hypnogramm");
		
		toolBarLabel.setText("Experimentee: " + dataPointsModel.getOrgFile().getName());
		
	}
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		// Event will be fired when closing the application
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				featureExtractionModel.setHypnogrammActive(false);
				System.out.println("Hypnogramm is closing.");
			}
		});
		
		loadHypnogramm();
	}
	
	private void loadHypnogramm() {
		XYChart.Series series = new XYChart.Series();
		
		double xAxis = 1.0;
		double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
		
		for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
        	double tmp = xAxis / numberOfEpochs;
        	tmp = tmp * 100;	
        	
        	double label;
        	switch ((int) featureExtractionModel.getFeatureClassLabel(i)) {
			case 1: label = 6.0;
				break;
			case 2: label = 4.0;
			break;
			case 3: label = 3.0;
			break;
			case 4: label = 2.0;
			break;
			case 5: label = 5.0;
			break;
			default: label = 0.0;
				break;
			}
			
			series.getData().add(new XYChart.Data<Double, Double>(tmp,label));			//tmp is xaxis
			
			xAxis++;
		}
		
		
		lineChart.getData().add(series);
	}
	
	
	public void reloadHypnogramm() {
		lineChart.getData().clear();
		loadHypnogramm();
	}


}
