package view;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.DataPoints;
import model.FeatureExtraxtionValues;
import controller.DataReaderController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FXApplicationController implements Initializable{
	
	private FXPopUp popUp = new FXPopUp();
	
	private DataReaderController dataReaderController;
	private DataPoints dataPointsModel;
	private FeatureExtraxtionValues featureExtractionModel;
	
	private int currentEpoch = 0;
	private double zoom = 1;
	private String[] channelNames;
		
	private Stage primaryStage;
	private BorderPane mainGrid;	
	private Scene scene;
	
	@FXML Label statusBarLabel1;
	@FXML TextField toolBarGoto;
	@FXML TextField toolBarZoom;
	
	@FXML ChoiceBox<String> toolBarChoiceBox;
	
	@FXML MenuItem showAdtVisualization;
	@FXML LineChart<Number, Number> lineChart;
	@FXML NumberAxis yAxis;
	
	public FXApplicationController(DataReaderController dataReaderController, DataPoints dataPointsModel, FeatureExtraxtionValues featureExtractionModel) {
		primaryStage = new Stage();
		this.dataReaderController = dataReaderController;
		this.dataPointsModel = dataPointsModel;
		this.featureExtractionModel = featureExtractionModel;
		
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
		
		lineChart.setSnapToPixel(true);
		
		channelNames = dataPointsModel.getChannelNames();
		ObservableList<String> choices = FXCollections.observableArrayList();
		choices.addAll(channelNames);
		toolBarChoiceBox.setItems(choices);
		
		showEpoch(currentEpoch);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//Key Listener
		lineChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.RIGHT) {
					
					if (currentEpoch < dataPointsModel.getNumberOf30sEpochs()) {
						lineChart.getData().clear();
						
						currentEpoch = currentEpoch + 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText(currentEpoch + "");
						statusBarLabel1.setText("Epoch " + currentEpoch);
					} else {
						popUp.showPopupMessage("Only " + dataPointsModel.getNumberOf30sEpochs() + " epochs available!", primaryStage);
					}
				}
				
				if (ke.getCode() == KeyCode.LEFT) {
					if (currentEpoch > 0) {
						lineChart.getData().clear();
						
						currentEpoch = currentEpoch - 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText(currentEpoch + "");
						statusBarLabel1.setText("Epoch " + currentEpoch);
					}
				}
			}
			
		});
		
		toolBarGoto.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
					
					int valueTextField = Integer.parseInt(toolBarGoto.getText());
					
					if ((valueTextField < dataPointsModel.getNumberOf30sEpochs()) && (valueTextField >= 0)) {
						lineChart.getData().clear();
						
						currentEpoch = valueTextField;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText(currentEpoch + "");
						statusBarLabel1.setText("Epoch " + currentEpoch);

						lineChart.requestFocus();

					} else {
						popUp.showPopupMessage("Only " + dataPointsModel.getNumberOf30sEpochs() + " epochs available!", primaryStage);
					}
				}

			}
		});
		
		toolBarZoom.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
						
					zoom = Double.parseDouble(toolBarZoom.getText());
					
					lineChart.getData().clear();
							
					showEpoch(currentEpoch);
							
					toolBarZoom.setText(zoom + "");
					
					lineChart.requestFocus();
				}
			}
			
		});

	}
	
	
	private void showEpoch(int numberOfEpoch) {
		double offsetSize = 100 / (dataPointsModel.getNumberOfChannels() + 1);
		int modulo = 3;					// Take every second sample
		
		for (int x = 0; x < dataPointsModel.getNumberOfChannels(); x++) {
			
			double realOffset = ((100-offsetSize) - (x * offsetSize));
			
			XYChart.Series series = new XYChart.Series();
	
	        LinkedList<Double> epoch = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), x, numberOfEpoch);
	        epoch.removeFirst(); 							//First element is just the number of the current epoch
	        double epochSize = epoch.size() / modulo;
	        double xAxis = 1;
	        
	        for (int i= 0; i < epoch.size(); i++) {
	        	if (i % modulo == 0) {
	            	double tmp = xAxis / epochSize;
	            	tmp = tmp * 100;	
	        		
	            	double value = epoch.get(i);
	            	value = value / 100;
	            	
	            	value = value * zoom;
	            	value = value + realOffset;
	            	
	            	series.getData().add(new XYChart.Data<Double, Double>(tmp, value));
	        		
	        		xAxis++;
	        	}
	        }
	        
	
			lineChart.getData().add(series);
		}
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
