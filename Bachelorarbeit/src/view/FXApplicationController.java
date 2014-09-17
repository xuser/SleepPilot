package view;

import help.BinaryFormat;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import sun.security.jca.GetInstance.Instance;
import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
	
	private FXHypnogrammController hypnogramm;
	//This epoch is just an puffer
	private LinkedList<LinkedList<Double>> nextEpoch = new LinkedList<LinkedList<Double>>();
	
	private FXPopUp popUp = new FXPopUp();
	private FXViewModel viewModel;
	
	private DataReaderController dataReaderController;
	private RawDataModel dataPointsModel;
	private FeatureExtractionModel featureExtractionModel;
	
	private boolean initStarted = false;
	private String currentChannelName = null;
	private double generalZoom = 1.0;
	
	private int currentEpoch = 0;
	private String[] channelNames;
	
	private HashMap<String, Double[]> activeChannels = new HashMap<String, Double[]>();
		
	private Stage primaryStage;
	private BorderPane mainGrid;	
	private Scene scene;
	
	@FXML ToggleButton awakeButton;
	@FXML ToggleButton s1Button;
	@FXML ToggleButton s2Button;
	@FXML ToggleButton s3Button;
	@FXML ToggleButton remButton;
	@FXML ToggleButton artefactButton;
	@FXML ToggleButton arrousalButton;
	
	@FXML Label statusBarLabel1;
	@FXML Label statusBarLabel2;
	@FXML TextField toolBarGoto;
	@FXML TextField toolBarZoom;
	
	@FXML GridPane statusBarGrid;
	@FXML ToolBar statusBar;
	
	@FXML Pane overlay;
	@FXML StackPane stackPane;
	@FXML HBox statusBarHBox;
	
	@FXML ChoiceBox<String> toolBarChoiceBox;
	@FXML CheckBox toolBarCheckBox;
	
	@FXML MenuItem showAdtVisualization;
	@FXML LineChart<Number, Number> lineChart;
	@FXML NumberAxis yAxis;
	
	public FXApplicationController(DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel) {
		primaryStage = new Stage();
		this.dataReaderController = dataReaderController;
		this.dataPointsModel = dataPointsModel;
		this.featureExtractionModel = featureExtractionModel;
		this.viewModel = viewModel;
		
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
			if (i < 6) {
				choices.add(channelNames[i]);
			
				//The first value represents wheater the channel is shown
				//The second value represents the current zoom level
				Double[] channelProp = new Double[2];
				channelProp[0] = 1.0;
				channelProp[1] = 5.0;
				activeChannels.put(channelNames[i], channelProp);
			} else {
				choices.add(channelNames[i]);
				
				//The first value represents wheater the channel is shown
				//The second value represents the current zoom level
				Double[] channelProp = new Double[2];
				channelProp[0] = 0.0;
				channelProp[1] = 5.0;
				activeChannels.put(channelNames[i], channelProp);
			}
			
		}	
		toolBarChoiceBox.setItems(choices);
		toolBarChoiceBox.getSelectionModel().selectFirst();
		currentChannelName = toolBarChoiceBox.getItems().get(0);
		
		statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
		statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
		statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
		
		showEpoch(currentEpoch);
		LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
		showLabelsForEpoch(activeChannelNumbers);
				
		checkProp();
		updateStage();
		updateProbabilities();
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

				statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
				statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
				statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
		    	
		    }
		});
		
		primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

				statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
				statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
				statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
				
				if (initStarted) {
					LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
					showLabelsForEpoch(activeChannelNumbers);
				}
		    	
		    }
		});
		
		lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				
				lineChart.requestFocus();
				
			}
		});
		
		
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
						updateStage();
						updateProbabilities();
						
						if (viewModel.isHypnogrammActive()) {
							hypnogramm.changeCurrentEpochMarker(currentEpoch);
						}
						
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
						updateStage();
						updateProbabilities();
						
						if (viewModel.isHypnogrammActive()) {
							hypnogramm.changeCurrentEpochMarker(currentEpoch);
						}
						
					}
				}
				
				if (ke.getCode() == KeyCode.H) {
					if (viewModel.isHypnogrammActive() == false) {
						hypnogramm = new FXHypnogrammController(dataPointsModel, featureExtractionModel, viewModel);
						hypnogramm.changeCurrentEpochMarker(currentEpoch);
						viewModel.setHypnogrammActive(true);
					} else {
						hypnogramm.bringToFront();
					}
				}
				
//				if (ke.getCode() == KeyCode.UP) {
//					refreshZoom(generalZoom + 0.25);
//				}
//				
//				if (ke.getCode() == KeyCode.DOWN) {
//					refreshZoom(generalZoom - 0.25);
//				}
				
				if (ke.getCode() == KeyCode.W) {
					awakeButtonOnAction();
					
					if (viewModel.isHypnogrammActive()) {
						hypnogramm.reloadHypnogramm();
					}
				}
				
				if (ke.getCode() == KeyCode.R) {
					remButtonOnAction();
					
					if (viewModel.isHypnogrammActive()) {
						hypnogramm.reloadHypnogramm();
					}
				}
				
				if (ke.getCode() == KeyCode.DIGIT1) {
					s1ButtonOnAction();
					
					if (viewModel.isHypnogrammActive()) {
						hypnogramm.reloadHypnogramm();
					}
				}
				
				if (ke.getCode() == KeyCode.DIGIT2) {
					s2ButtonOnAction();
					
					if (viewModel.isHypnogrammActive()) {
						hypnogramm.reloadHypnogramm();
					}
				}
				
				if (ke.getCode() == KeyCode.DIGIT3) {
					s3ButtonOnAction();
					
					if (viewModel.isHypnogrammActive()) {
						hypnogramm.reloadHypnogramm();
					}
				}
			}
			
		});
		
		toolBarGoto.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
					
					// TODO: Unsauberer Code => Verbessern!
					int valueTextField = -1;
					
					try {
						valueTextField = Integer.parseInt(toolBarGoto.getText());
						
					} catch (NumberFormatException e) {
						toolBarGoto.setText((currentEpoch+1) + "");
					}
					
					if ((valueTextField <= dataPointsModel.getNumberOf30sEpochs()) && (valueTextField > 0)) {
						lineChart.getData().clear();
						
						currentEpoch = valueTextField - 1;
						showEpoch(currentEpoch);
						
						toolBarGoto.setText((currentEpoch+1) + "");
						statusBarLabel1.setText("Epoch " + (currentEpoch+1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));

						lineChart.requestFocus();
						updateStage();
						updateProbabilities();
						
						if (viewModel.isHypnogrammActive()) {
							hypnogramm.changeCurrentEpochMarker(currentEpoch);
						}

					} else {
						toolBarGoto.setText((currentEpoch+1) + "");
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
					
					
					checkProp();				// This is just for testing
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
				
				LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
				showLabelsForEpoch(activeChannelNumbers);
				
				checkProp();			// This is just for testing
		
				
			}

        });

	}
	
	//TODO: FIX Zoom
	private void refreshZoom(double zoom) {
		lineChart.getData().clear();
		
		generalZoom = zoom;
		for(int i = 0; i < channelNames.length; i++) {
			Double[] tempProp = activeChannels.get(channelNames[i]);
			activeChannels.remove(channelNames[i]);
			
			tempProp[1] = zoom;
			
			activeChannels.put(channelNames[i], tempProp);
		}
		
		showEpoch(currentEpoch);
	}
	
	
	public void bringToFront() {
		primaryStage.toFront();
	}
	
	private void updateProbabilities() {
		double[] probabilities = featureExtractionModel.getPredictProbabilities(currentEpoch);
		
		double wake = (Math.round((probabilities[1] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2)) ;
		double n1 = (Math.round((probabilities[2] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
		double n2 = (Math.round((probabilities[0] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
		double n3 = (Math.round((probabilities[3] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
		double rem = (Math.round((probabilities[4] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
		

		String print = "W: " + wake + "%  N1: " + n1 + "%  N2: " + n2 + "%  N3: " + n3 + "%  REM: " + rem + "%";

		statusBarLabel2.setText(print);
		statusBarHBox.setHgrow(statusBarLabel2, Priority.ALWAYS);
	}
	
	
	
	private void updateStage() {
		// (1: W, 2: N1, 3: N2, 4: N3, 5: REM)
		int label = featureExtractionModel.getFeatureClassLabel(currentEpoch);
		switch (label) {
		case 1:	
			awakeButton.setSelected(true);
			s1Button.setSelected(false);
			s2Button.setSelected(false);
			s3Button.setSelected(false);
			remButton.setSelected(false);
			break;
		case 2:	
			awakeButton.setSelected(false);
			s1Button.setSelected(true);
			s2Button.setSelected(false);
			s3Button.setSelected(false);
			remButton.setSelected(false);
			break;
		case 3:	
			awakeButton.setSelected(false);
			s1Button.setSelected(false);
			s2Button.setSelected(true);
			s3Button.setSelected(false);
			remButton.setSelected(false);
			break;
		case 4:
			awakeButton.setSelected(false);
			s1Button.setSelected(false);
			s2Button.setSelected(false);
			s3Button.setSelected(true);
			remButton.setSelected(false);
			break;
		case 5:	
			awakeButton.setSelected(false);
			s1Button.setSelected(false);
			s2Button.setSelected(false);
			s3Button.setSelected(false);
			remButton.setSelected(true);
			break;
		default: 	
			awakeButton.setSelected(false);
			s1Button.setSelected(false);
			s2Button.setSelected(false);
			s3Button.setSelected(false);
			remButton.setSelected(false);
			break;
		}
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
		
		overlay.getChildren().clear();
		double labelHeight = overlay.getHeight() / activeChannels.size();
		double offset = (overlay.getHeight()-labelHeight) / activeChannels.size();
		
		for(int i = 0; i < activeChannels.size(); i++) {
						
			Label label = new Label(getNameFromChannel(activeChannels.get(i)));
			label.setTextFill(Color.GRAY);
			label.setStyle("-fx-font-family: sans-serif;");
			label.setLayoutX(18);
			
			double labelPos = ((i+1) * offset);			
			label.setLayoutY(labelPos + 15);
			 
			overlay.getChildren().add(label);
	
		}
		
	}
	
	public void goToEpoch(int epoch) {
		
		lineChart.getData().clear();
		
		currentEpoch = epoch;
		showEpoch(currentEpoch);
		
		toolBarGoto.setText((currentEpoch+1) + "");
		statusBarLabel1.setText("Epoch " + (currentEpoch+1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));
		
		lineChart.requestFocus();
		updateStage();
		updateProbabilities();
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
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
			
			LinkedList<Double> epoch = null;
			
			if (dataPointsModel.getOrgFile().getName().toLowerCase().endsWith(".vhdr")) {
				
				if (dataPointsModel.getBinaryFormat() == BinaryFormat.INT_16) {	
					epoch = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), activeChannelNumbers.get(x), numberOfEpoch);
				} else {
					epoch = dataReaderController.readDataFileFloat(dataPointsModel.getDataFile(), activeChannelNumbers.get(x), numberOfEpoch);
				}
				
			} else if (dataPointsModel.getOrgFile().getName().toLowerCase().endsWith(".smr")) {
				epoch = dataReaderController.readSMRChannel(dataPointsModel.getDataFile(), activeChannelNumbers.get(x), numberOfEpoch);
			}
			
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
//		loader.setController(this);
		
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
	
	@FXML
	protected void showHypnogrammAction() {
		if (viewModel.isHypnogrammActive() == false) {
			hypnogramm = new FXHypnogrammController(dataPointsModel, featureExtractionModel, viewModel);
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
			viewModel.setHypnogrammActive(true);
		} else {
			hypnogramm.bringToFront();
		}
		
	}
	
	@FXML
	protected void awakeButtonOnAction(){
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 1.0);
		updateStage();
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s1ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 2.0);
		updateStage();
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s2ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 3.0);
		updateStage();
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s3ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 4.0);
		updateStage();
		lineChart.requestFocus();
	}
	
	@FXML
	protected void remButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 5.0);
		updateStage();
		lineChart.requestFocus();
	}
	
	private void checkProp() {
			
		for (int i = 0; i < channelNames.length; i++) {
			Double[] prop = activeChannels.get(channelNames[i]);
			System.out.println(channelNames[i] + " " + prop[0] + " " + prop[1]);
		}
		System.out.println("----------------------------");
	}
	

}
