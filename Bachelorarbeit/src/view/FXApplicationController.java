package view;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.DataPoints;
import model.FeatureExtraxtionValues;
import controller.DataReaderController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXApplicationController implements Initializable{
	
	//This epoch is just an puffer
	private LinkedList<LinkedList<Double>> nextEpoch = new LinkedList<LinkedList<Double>>();
	
	private FXPopUp popUp = new FXPopUp();
	
	private DataReaderController dataReaderController;
	private DataPoints dataPointsModel;
	private FeatureExtraxtionValues featureExtractionModel;
	
	private boolean initStarted = false;
	private String currentChannelName = null;
	
	private int currentEpoch = 0;
	private String[] channelNames;
	
	private HashMap<String, Double[]> activeChannels = new HashMap<String, Double[]>();
		
	private Stage primaryStage;
	private BorderPane mainGrid;	
	private Scene scene;
	
	@FXML Label statusBarLabel1;
	@FXML TextField toolBarGoto;
	@FXML TextField toolBarZoom;
	
	@FXML Pane overlay;
	@FXML GridPane overlayGrid;
	@FXML StackPane stackPane;
	
	@FXML ChoiceBox<String> toolBarChoiceBox;
	@FXML CheckBox toolBarCheckBox;
	
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
		primaryStage.setTitle(dataPointsModel.getOrgFile().getName());
		
		statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + dataPointsModel.getNumberOf30sEpochs());
		
		//Configure lineChart
		lineChart.setSnapToPixel(true);
		lineChart.requestFocus();
		
		// Set Choice Box for the channels
		channelNames = dataPointsModel.getChannelNames();
		ObservableList<String> choices = FXCollections.observableArrayList();

		
		//Set properties for the channels
		for (int i = 0; i < channelNames.length; i++) {
			choices.add(channelNames[i]);
			
			//The first value represents wheater the channel is shown
			//The second value represents the current zoom level
			Double[] channelProp = new Double[2];
			channelProp[0] = 1.0;
			channelProp[1] = 5.0;
			activeChannels.put(channelNames[i], channelProp);
			
		}	
		toolBarChoiceBox.setItems(choices);
		toolBarChoiceBox.getSelectionModel().selectFirst();
		currentChannelName = toolBarChoiceBox.getItems().get(0);

		showEpoch(currentEpoch);
//		showLabelsForEpoch(returnActiveChannels());
		
		checkProp();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//Key Listener
		lineChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.RIGHT) {
					
					if (currentEpoch < (dataPointsModel.getNumberOf30sEpochs()-1)) {
						lineChart.getData().clear();
						
						currentEpoch = currentEpoch + 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText((currentEpoch+1) + "");
						statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));
					} else {
						popUp.showPopupMessage("Only " + dataPointsModel.getNumberOf30sEpochs() + " epochs available!", primaryStage);
					}
				}
				
				if (ke.getCode() == KeyCode.LEFT) {
					if (currentEpoch > 0) {
						lineChart.getData().clear();
						
						currentEpoch = currentEpoch - 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText((currentEpoch+1) + "");
						statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));
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
						
						currentEpoch = valueTextField - 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText((currentEpoch+1) + "");
						statusBarLabel1.setText("Epoch " + (currentEpoch+1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));

						lineChart.requestFocus();

					} else {
						popUp.showPopupMessage("Only " + (dataPointsModel.getNumberOf30sEpochs()-1) + " epochs available!", primaryStage);
					}
				}

			}
		});
		
		toolBarZoom.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
						
					double zoom = Double.parseDouble(toolBarZoom.getText());
					String currentChannel = currentChannelName;
					
					Double[] tempProp = activeChannels.get(currentChannel);
					tempProp[1] = zoom;
					
					activeChannels.remove(currentChannel);
					activeChannels.put(currentChannel, tempProp);					
					
					lineChart.getData().clear();
							
					showEpoch(currentEpoch);
							
					toolBarZoom.setText(zoom + "");
					
					lineChart.requestFocus();
					
					checkProp();
				}
			}
			
		});
		
		toolBarChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number oldValue, Number newValue) {
				
				
				if (initStarted) { 
					String tempChannel = toolBarChoiceBox.getItems().get(newValue.intValue());
					currentChannelName = tempChannel;
					System.out.println("Current Channel: " + tempChannel);
					Double[] tempProp = activeChannels.get(tempChannel);
					
					if (tempProp[0] == 1.0) {
						toolBarCheckBox.setSelected(true);
					} else {
						toolBarCheckBox.setSelected(false);
					}
					toolBarZoom.setText(tempProp[1] + "");
					
					
					checkProp();
				} else {
					initStarted = true;
				}
				
				
			}
			
			
		});
		
		toolBarCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				
				String currentChannel = currentChannelName;
				Double[] tempProp = activeChannels.get(currentChannel);
				
				double show;
				if (toolBarCheckBox.isSelected()) {
					show = 1.0;
				} else {
					show = 0.0;
				}
				tempProp[0] = show;
				
				activeChannels.remove(currentChannel);
				activeChannels.put(currentChannel, tempProp);					
				
				lineChart.getData().clear();
						
				showEpoch(currentEpoch);
				
				lineChart.requestFocus();
				
				checkProp();
		
				
			}

        });

	}
	
	private LinkedList<Integer> returnActiveChannels() {
		
		LinkedList<Integer> channels = new LinkedList<Integer>();
		
		for(int i = 0; i< channelNames.length; i++) {
			Double[] tempProp = activeChannels.get(channelNames[i]);
			if (tempProp[0] == 1.0) {
				channels.add(i);
			}
		}
		
		return channels;
		
	}
	
	private double getZoomFromChannel(int channelNumber) {
		
		String channel = channelNames[channelNumber];

		Double[] tempProp = activeChannels.get(channel);
		double channelZoom = tempProp[1];
		
		return channelZoom;
		
	}
	
	private String getNameFromChannel(int channelNumber){
		
		String channel = channelNames[channelNumber];
		return channel;
	}
	
	private void showLabelsForEpoch(LinkedList<Integer> activeChannels) {
		
		for(int i = 0; i < activeChannels.size(); i++) {
			
			Label label = new Label(getNameFromChannel(activeChannels.get(i)));
			label.setTextFill(Color.GRAY);
			label.setStyle("-fx-padding: 0 0 0 10");
			
			overlayGrid.addRow(i, label);
			
		}
		
	}
	
	
	private void showEpoch(int numberOfEpoch) {
		LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
		
		double offsetSize = 100 / (activeChannelNumbers.size() + 1);
		int modulo = 3;					// Take every second sample
		
		for (int x = 0; x < activeChannelNumbers.size(); x++) {

			double zoom = getZoomFromChannel(activeChannelNumbers.get(x));
			
			double realOffset = ((100-offsetSize) - (x * offsetSize));
			
			XYChart.Series series = new XYChart.Series();
	
	        LinkedList<Double> epoch = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), activeChannelNumbers.get(x), numberOfEpoch);
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
		
//		for (int y = 0; y < activeChannelNumbers.size(); y++) {
//			
//			LinkedList<Double> tmp = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), activeChannelNumbers.get(y), (numberOfEpoch + 1));	
//		
//		}
		
		
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
	
	
	
	private void checkProp() {
			
		for (int i = 0; i < channelNames.length; i++) {
			Double[] prop = activeChannels.get(channelNames[i]);
			System.out.println(channelNames[i] + " " + prop[0] + " " + prop[1]);
		}
		System.out.println("----------------------------");
	}
	

}
