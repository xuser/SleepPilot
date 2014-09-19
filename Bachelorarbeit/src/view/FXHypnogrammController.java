package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXHypnogrammController implements Initializable{
	
	private FXApplicationController appController;
	private FXViewModel viewModel;
	private FeatureExtractionModel featureExtractionModel;
	private RawDataModel dataPointsModel;
	
	private Stage stage;
	
	@FXML LineChart<Number, Number> lineChart;
	@FXML Label toolBarLabel;
	
	public FXHypnogrammController(RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel) {
		
		this.featureExtractionModel = featureExtractionModel;
		this.viewModel = viewModel;
		this.dataPointsModel = dataPointsModel;
		
		appController = viewModel.getAppController();
		
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
				viewModel.setHypnogrammActive(false);
				System.out.println("Hypnogramm is closing.");
			}			
		
		});
		
		//Key Listener
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.H) {	
//					viewModel.setHypnogrammActive(false);
//					stage.close();
					appController.bringToFront();
				}
			}
		});
		
		
		//TODO: Fix Jump to different epochs
		lineChart.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		    	
		    	double widthLineChart = lineChart.getWidth();
		    	
		    	System.out.println("Width LineChart: " + widthLineChart);
		    	
		    	double mouseXPos = mouseEvent.getX();
		    	
		    	double tmpRelationPos = (mouseXPos / widthLineChart);
		    	tmpRelationPos = tmpRelationPos * 100;
		    	System.out.println("Relativ Mousepos: " + tmpRelationPos);
		    	
		    	double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
		    	
		    	int currentEpoch = (int) ((tmpRelationPos * numberOfEpochs) / 100);		    
		 			    	
		    	if ((currentEpoch <= numberOfEpochs)  && (currentEpoch >= 0)) {
		    		appController.goToEpoch(currentEpoch);
		    	}
		    			    	
		        System.out.println("X: " + mouseEvent.getX());
		        System.out.println("Y: " + mouseEvent.getY());

		    }
		});
		
		loadHypnogramm();
	}
	
	@SuppressWarnings("unchecked")
	private void loadHypnogramm() {
		@SuppressWarnings("rawtypes")
		XYChart.Series series = new XYChart.Series();
		XYChart.Series seriesArtefact = new XYChart.Series();
		XYChart.Series seriesArrousal = new XYChart.Series();
		XYChart.Series seriesStimulation = new XYChart.Series();
		
		double xAxis = 1.0;
		double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
		
		for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
        	double tmp = xAxis / numberOfEpochs;
        	tmp = tmp * 100;	
        	
        	double label;
        	switch ((int) featureExtractionModel.getFeatureClassLabel(i)) {
			case 1: label = 9.0;
				break;
			case 2: label = 7.0;
			break;
			case 3: label = 6.0;
			break;
			case 4: label = 5.0;
			break;
			case 5: label = 8.0;
			break;
			default: label = 4.0;
				break;
			}
			
			series.getData().add(new XYChart.Data<Double, Double>(tmp,label));			//tmp is xaxis
			
			if (featureExtractionModel.getEpochProperty(i) != null) {
				Integer[] prob = featureExtractionModel.getEpochProperty(i);
				
				if (prob[0] == 1) {
					seriesArtefact.getData().add(new XYChart.Data<Double, Double>(tmp,3.0));			//tmp is xaxis
					seriesArtefact.getData().add(new XYChart.Data<Double, Double>(tmp,3.8));			//tmp is xaxis
				}
				
				if (prob[1] == 1) {
					seriesArrousal.getData().add(new XYChart.Data<Double, Double>(tmp,2.0));			//tmp is xaxis					
					seriesArrousal.getData().add(new XYChart.Data<Double, Double>(tmp,2.8));			//tmp is xaxis
				}
				
				if (prob[2] == 1) {
					seriesStimulation.getData().add(new XYChart.Data<Double, Double>(tmp,1.0));			//tmp is xaxis
					seriesStimulation.getData().add(new XYChart.Data<Double, Double>(tmp,1.8));			//tmp is xaxis
					
				}

			} else {
				seriesArtefact.getData().add(new XYChart.Data<Double, Double>(tmp,3.0));			//tmp is xaxis
				seriesArrousal.getData().add(new XYChart.Data<Double, Double>(tmp,2.0));			//tmp is xaxis
				seriesStimulation.getData().add(new XYChart.Data<Double, Double>(tmp,1.0));			//tmp is xaxis


			}
			
			xAxis++;
		}		
		
		lineChart.getData().add(series);
		lineChart.getData().add(seriesArtefact);
		lineChart.getData().add(seriesArrousal);
		lineChart.getData().add(seriesStimulation);
	}
	
	@SuppressWarnings("unchecked")
	public void changeCurrentEpochMarker(double currentEpoch) {
//		lineChart.getData().get(1).getData().clear();
		lineChart.getData().clear();
		loadHypnogramm();
		
		@SuppressWarnings("rawtypes")
		XYChart.Series marker = new XYChart.Series();
		marker.setName("marker");
		
		double xAxis = currentEpoch;
		double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
    	double tmp = xAxis / numberOfEpochs;
    	tmp = tmp * 100;
		
		
		marker.getData().add(new XYChart.Data<Double, Double>(tmp,0.0));
		marker.getData().add(new XYChart.Data<Double, Double>(tmp,9.0));
		
//		for (int i = 0; i < lineChart.getData().size(); i++) {
//		    for (Node node : lineChart.lookupAll(".series4")) {
//		        node.getStyleClass().add("default-color4");
//		    }
//		}
		
		lineChart.getData().add(marker);
		
	}
	
	public void bringToFront() {
		stage.toFront();
	}
	
	
	public void reloadHypnogramm() {
		lineChart.getData().clear();
		loadHypnogramm();
	}


}
