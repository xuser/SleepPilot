package view;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.DataPoints;
import model.FeatureExtraxtionValues;
import controller.DataReaderController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
	private double yAxisHeight = 0;
	
	private Stage primaryStage;
	private BorderPane mainGrid;	
	private Scene scene;
	
	@SuppressWarnings("rawtypes")
	private XYChart.Series series;
	
	@FXML Label statusBarLabel1;
	@FXML TextField toolBarGoto;
	
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
		yAxisHeight = yAxis.getHeight();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		lineChart.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	yAxisHeight = yAxis.getHeight();
		    }
		});
		
		//Key Listener
		primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.RIGHT) {
					
					if (currentEpoch < dataPointsModel.getNumberOf30sEpochs()) {
						series.getData().clear();
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
						series.getData().clear();
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
						series.getData().clear();
						lineChart.getData().clear();
						
						currentEpoch = valueTextField;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText(currentEpoch + "");
						statusBarLabel1.setText("Epoch " + currentEpoch);
						
						toolBarGoto.setFocusTraversable(false);
					} else {
						popUp.showPopupMessage("Only " + dataPointsModel.getNumberOf30sEpochs() + " epochs available!", primaryStage);
					}
				}

			}
		});

		showEpoch(currentEpoch);
	}
	
	
	private void showEpoch(int numberOfEpoch) {
		
		
        series = new XYChart.Series();
        int modulo = 2;					// Take every second sample

        LinkedList<Double> epoch = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), 0, numberOfEpoch);
        epoch.removeFirst(); 							//First element is just the number of the current epoch
        double epochSize = epoch.size() / modulo;
        double xAxis = 1;
        
        for (int i= 0; i < epoch.size(); i++) {
        	if (i % modulo == 0) {
            	double tmp = xAxis / epochSize;
            	tmp = tmp * 100;	
        		
            	double value = epoch.get(i);
            	value = value / 100;
            	
            	value = value * 5;					//This is just for testing
            	value = value + (100/2);
            	
            	series.getData().add(new XYChart.Data<Double, Double>(tmp, value));
        		
        		xAxis++;
        	}
        }
        

		lineChart.getData().add(series);
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
