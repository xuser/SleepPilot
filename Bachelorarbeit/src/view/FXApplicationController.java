package view;

import help.BinaryFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import tsne.TSNE;
import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import controller.DataReaderController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXApplicationController implements Initializable{
	
	private FXHypnogrammController hypnogramm;
	private FXEvaluationWindowController evaluationWindow;
	private FXScatterPlot scatterPlot;
	
	//This epoch is just an puffer
	private LinkedList<LinkedList<Double>> nextEpoch = new LinkedList<LinkedList<Double>>();
	
	private FXPopUp popUp = new FXPopUp();
	private FXViewModel viewModel;
	
	private DataReaderController dataReaderController;
	private RawDataModel dataPointsModel;
	private FeatureExtractionModel featureExtractionModel;
	
	private final boolean autoMode;
	private boolean initStarted = false;
	private String currentChannelName = null;
	
	private double oldWidth;
	private double growCoefWidth = 1.0;
	
	private double oldHeight;
	private double growCoefHeight = 1.0;
	
	private int currentEpoch = 0;
	private String[] channelNames;
	
	private HashMap<String, Double[]> activeChannels = new HashMap<String, Double[]>();
	
	private LinkedList<Line> lines = new LinkedList<Line>();
		
	private Stage primaryStage;
	private BorderPane mainGrid;	
	private Scene scene;
	
	@FXML private ToggleButton awakeButton;
	@FXML private ToggleButton s1Button;
	@FXML private ToggleButton s2Button;
	@FXML private ToggleButton s3Button;
	@FXML private ToggleButton remButton;
	@FXML private ToggleButton artefactButton;
	@FXML private ToggleButton arrousalButton;
	@FXML private ToggleButton stimulationButton;
	@FXML private Button clearButton;
	
	@FXML private ToggleButton help1;
	private boolean help1Flag = false;
	
	@FXML private ToggleButton kComplex;
	private boolean kComplexFlag = false;
	
	@FXML private Label statusBarLabel1;
	@FXML private Label statusBarLabel2;
	@FXML private Label kComplexLabel;
	@FXML private TextField toolBarGoto;
	@FXML private TextField toolBarZoom;
	
	@FXML private GridPane statusBarGrid;
	@FXML private ToolBar statusBar;
	
	@FXML private Pane overlay;
	@FXML private Pane overlay2;
	@FXML private Pane overlay3;
	
//	Canvas canvas = new Canvas();
//	ResizableCanvas canvas = new ResizableCanvas();
	
	@FXML private StackPane stackPane;
	@FXML private HBox statusBarHBox;
	
	@FXML private ChoiceBox<String> toolBarChoiceBox;
	@FXML private CheckBox toolBarCheckBox;
	
	@FXML private MenuItem showAdtVisualization;
	@FXML private LineChart<Number, Number> lineChart;
	@FXML private NumberAxis yAxis;
	
	@FXML private Line line1;
	@FXML private Line line2;
	
	public FXApplicationController(DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel, boolean autoMode) {
		primaryStage = new Stage();
		this.dataReaderController = dataReaderController;
		this.dataPointsModel = dataPointsModel;
		this.featureExtractionModel = featureExtractionModel;
		this.viewModel = viewModel;
		this.autoMode = autoMode;
		
		if (!autoMode) {
			featureExtractionModel.createDataMatrix(dataPointsModel.getNumberOf30sEpochs(), 1);
		}
		
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
		
		oldWidth = overlay3.getWidth();
		oldHeight = overlay3.getHeight();
		
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
		
		line1.setVisible(false);
		line2.setVisible(false);
		kComplexLabel.setVisible(false);
		
		statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
		statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
		statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
		
		showEpoch(currentEpoch);
		LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
		showLabelsForEpoch(activeChannelNumbers);
				
		checkProp();
		
		if (autoMode) {
			updateStage();			
		}
		
		updateProbabilities();
		
	}
	

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		overlay3.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouse) {
				if (kComplexFlag) {
					if (mouse.getEventType() == MouseEvent.MOUSE_PRESSED) {
						Line line = new Line();
						line.setStyle("-fx-stroke: red;");
						
						overlay3.getChildren().add(line);
						
						line.setStartX(0);
						line.setStartY(0);
						line.setLayoutX(mouse.getX());
						line.setLayoutY(mouse.getY());
						
						lines.add(line);
						
					}
					
					if (mouse.isPrimaryButtonDown()) {
						Line line = lines.getLast();
						double endXPos = mouse.getX() - line.getLayoutX();
						double endYPos = mouse.getY() - line.getLayoutY();
							
						line.setEndX(endXPos);						
						line.setEndY(endYPos);
					}
					
					calculatePercentageKComplex();
				}
			}
			
		});
		
		overlay3.setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouse) {
								
				if (help1Flag) {					
					
					LinkedList<Integer> activeChannels = returnActiveChannels();
					double overlay2Height = overlay2.getHeight();
//					overlay2Height = overlay2Height - (overlay2Height / activeChannels.size());
					
					double heigtForChannel = overlay2Height / activeChannels.size();
//					heigtForChannel = (overlay2.getHeight() + heigtForChannel) / activeChannels.size();
					
					double posOnOverlay = mouse.getY();
					System.out.println("PosOnOverlay: " + posOnOverlay);
					
					double activeZoom = 0.0;
					for (int i = 1; i <= activeChannels.size(); i++) {
						if (posOnOverlay < (heigtForChannel * i) && posOnOverlay > (heigtForChannel * (i-1))) {
							activeZoom = getZoomFromChannel(activeChannels.get(i-1));
							System.out.println("Actice Channel: " + activeChannels.get(i-1));
							System.out.println("Actice Zoom:  " + activeZoom);
						}
					}
										
					double space = 75.0/100.0 * activeZoom;
					System.out.println("Space: " + space);
					
					// Now calculate the number of pixels from the microvolt size
					space = (space/100.0) * overlay2.getHeight();
				
					paintSpacing(mouse.getY(), space);
				}
				
			}
			
		});
		
		primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

				statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
				statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
				statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
				
				if (help1Flag) {
					line1.setEndX(lineChart.getWidth());
					line2.setEndX(lineChart.getWidth());
				}
				
				growCoefWidth = overlay3.getWidth() / oldWidth;
				oldWidth = overlay3.getWidth();
				
				for (int i = 0; i < lines.size(); i++) {
					Line line = lines.get(i);
					line.setLayoutX(line.getLayoutX() * growCoefWidth);
					line.setEndX(line.getEndX() * growCoefWidth);
					lines.set(i, line);
				}
				
		    	
		    }
		});
		
		primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

				statusBarGrid.setMinWidth(statusBar.getWidth() - 20);
				statusBarGrid.setMaxWidth(statusBar.getWidth() - 20);
				statusBarGrid.setPrefWidth(statusBar.getWidth() - 20);
				
				growCoefHeight = overlay3.getHeight() / oldHeight;
				oldHeight = overlay3.getHeight();
				
				for (int i = 0; i < lines.size(); i++) {
					Line line = lines.get(i);
					line.setLayoutY(line.getLayoutY() * growCoefHeight);
					line.setEndY(line.getEndY() * growCoefHeight);
					lines.set(i, line);
				}
				
				if (initStarted) {
					LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
					showLabelsForEpoch(activeChannelNumbers);
				}
		    	
		    }
		});
		
		overlay2.setOnMouseClicked(new EventHandler<MouseEvent>() {

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
						
						overlay3.getChildren().clear();
						lines.clear();
						
						if (kComplexFlag) {
							calculatePercentageKComplex();
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
						
						
						overlay3.getChildren().clear();
						lines.clear();
						
						if (kComplexFlag) {
							calculatePercentageKComplex();
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
				
				if (ke.getCode() == KeyCode.E) {
					if (viewModel.isEvaluationWindowActive() == false) {
						evaluationWindow = new FXEvaluationWindowController(dataPointsModel, featureExtractionModel, viewModel);
						viewModel.setEvaluationWindowActive(true);
					} else {
						evaluationWindow.bringToFront();
					}
				}
				
				if (ke.getCode() == KeyCode.L) {
					help1OnAction();
					
					if (help1.isSelected()) {
						help1.setSelected(false);
					} else {
						help1.setSelected(true);
					}
				}
				
				if (ke.getCode() == KeyCode.K) {
					kComplexOnAction();
					
					if (kComplex.isSelected()) {
						kComplex.setSelected(false);
					} else {
						kComplex.setSelected(true);
					}
					
				}
				
				if (ke.getCode() == KeyCode.UP) {					
					refreshZoom(+1);
				}
				
				if (ke.getCode() == KeyCode.DOWN) {
					refreshZoom(-1);
				}
				
				
				if (ke.getCode() == KeyCode.W) {
					awakeButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.R) {
					remButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.DIGIT1) {
					s1ButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.DIGIT2) {
					s2ButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.DIGIT3) {
					s3ButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.A) {
					artefactButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.M) {
					arrousalButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.S) {
					stimulationButtonOnAction();
				}
				
				if (ke.getCode() == KeyCode.C) {
					clearButtonOnAction();
				}
			}
			
		});
		
		toolBarGoto.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.ENTER) {
					
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
												
						overlay3.getChildren().clear();
						lines.clear();
						
						if (kComplexFlag) {
							calculatePercentageKComplex();
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
	
	private void refreshZoom(double zoom) {
		lineChart.getData().clear();		
		
		for(int i = 0; i < channelNames.length; i++) {
			
			Double[] tempProp = activeChannels.get(channelNames[i]);
			
			if (zoom < 0) {
				if (tempProp[1] > 1.0) {
					activeChannels.remove(channelNames[i]);
					tempProp[1] = tempProp[1] + zoom;
					activeChannels.put(channelNames[i], tempProp);
				}
			} else {
				activeChannels.remove(channelNames[i]);
				tempProp[1] = tempProp[1] + zoom;
				activeChannels.put(channelNames[i], tempProp);
			}
		}
		
		Double[] tempProp = activeChannels.get(currentChannelName);
		toolBarZoom.setText(tempProp[1] + "");
				
		showEpoch(currentEpoch);
		lineChart.requestFocus();
	}
	
	
	public void bringToFront() {
		primaryStage.toFront();
	}
	
	@SuppressWarnings("static-access")
	private void updateProbabilities() {
		if (autoMode) {
			double[] probabilities = featureExtractionModel.getPredictProbabilities(currentEpoch);
		
			double wake = (Math.round((probabilities[1] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2)) ;
			double n1 = (Math.round((probabilities[2] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
			double n2 = (Math.round((probabilities[0] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
			double n3 = (Math.round((probabilities[3] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
			double rem = (Math.round((probabilities[4] * 100) * Math.pow(10d, 2))/Math.pow(10d, 2));
		

			String print = "W: " + wake + "%  N1: " + n1 + "%  N2: " + n2 + "%  N: " + n3 + "%  REM: " + rem + "%";

			statusBarLabel2.setText(print);
			statusBarHBox.setHgrow(statusBarLabel2, Priority.ALWAYS);
		} else {
			statusBarLabel2.setText("Manual Mode");
		}
	}
	
	private double roundValues(double value) {
		BigDecimal myDec = new BigDecimal(value);
		myDec = myDec.setScale(1, BigDecimal.ROUND_HALF_UP);
		
		return myDec.doubleValue();
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
		
		if (featureExtractionModel.getEpochProperty(currentEpoch) != null) {
			Integer[] prop = featureExtractionModel.getEpochProperty(currentEpoch);
			
			if (prop[0] == 1) {
				artefactButton.setSelected(true);
			} else {				
				artefactButton.setSelected(false);
			}
			
			if (prop[1] == 1) {
				arrousalButton.setSelected(true);
			} else {				
				arrousalButton.setSelected(false);
			}
			
			if (prop[2] == 1) {
				stimulationButton.setSelected(true);
			} else {				
				stimulationButton.setSelected(false);
			}
			
		} else {
			artefactButton.setSelected(false);
			arrousalButton.setSelected(false);
			stimulationButton.setSelected(false);
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
		
		for(int i = 0; i < activeChannels.size(); i++) {
						
			Label label = new Label(getNameFromChannel(activeChannels.get(i)));
			label.setTextFill(Color.GRAY);
			label.setStyle("-fx-font-family: sans-serif;");
			label.setLayoutX(18);
			
			double labelPos = ((i+1) * labelHeight - (labelHeight/2));			
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
	
	private void paintSpacing(double yAxis, double space) {
		
		double tmpSpace = space/2;
		
		line1.setLayoutY(yAxis + tmpSpace);
		line2.setLayoutY(yAxis - tmpSpace);

	}
	
	private void showEpoch(int numberOfEpoch) {
		LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
		
		double offsetSize = 100 / (activeChannelNumbers.size());
		int modulo = 3;					// Take every second sample
				
		for (int x = 0; x < activeChannelNumbers.size(); x++) {

			double zoom = getZoomFromChannel(activeChannelNumbers.get(x));
			
			double realOffset = 100 - ((x+1) * offsetSize) + (offsetSize / 2);
			
			@SuppressWarnings("rawtypes")
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
	protected void help1OnAction() {
		if (help1Flag){
			help1Flag = false;
			line1.setVisible(false);
			line2.setVisible(false);
		} else {
			help1Flag = true;
			line1.setVisible(true);
			line2.setVisible(true);
			
			line1.setEndX(lineChart.getWidth());
			line2.setEndX(lineChart.getWidth());
			
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void kComplexOnAction() {
		if (kComplexFlag) {
			kComplexFlag = false;
			kComplexLabel.setVisible(false);
			
			overlay3.getChildren().clear();
			lines.clear();
			
		} else {
			kComplexFlag = true;
			kComplexLabel.setVisible(true);
			
		}
		System.out.println(kComplexFlag);
		
		lineChart.requestFocus();
	}
	
	private void calculatePercentageKComplex() {
		
		double percentageSum = 0.0;
		
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			
			double lengthOfLine;
			
			if (line.getEndX() > line.getStartX()) {
				lengthOfLine = line.getEndX() - line.getStartX();
			} else {
				lengthOfLine = line.getStartX() - line.getEndX();
			}
			
			double percentageOneLine = lengthOfLine / overlay3.getWidth() * 100;
			percentageSum = percentageSum + percentageOneLine;
		}
		
		kComplexLabel.setText("K-Complex: " + roundValues(percentageSum) + "%");		
	}
	
	@FXML
	protected void help1MenuItemOnAction() {
		help1OnAction();
	}
	
	@FXML
	protected void kComplexMenuItemOnAction() {
		kComplexOnAction();
	}
	
	@FXML
	protected void showAdtVisualizationAction() {
		
		if (viewModel.isEvaluationWindowActive() == false) {
			evaluationWindow = new FXEvaluationWindowController(dataPointsModel, featureExtractionModel, viewModel);
			viewModel.setEvaluationWindowActive(true);
		} else {
			evaluationWindow.bringToFront();
		}
		
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
	protected void closeAction() {
		System.exit(0);
	}
	
	//TODO
	@FXML
	protected void showScatterPlot() {
		if (viewModel.isScatterPlotActive() == false) {
			scatterPlot = new FXScatterPlot(dataPointsModel, featureExtractionModel, viewModel);
			viewModel.setScatterPlotActive(true);
		} else {
			scatterPlot.bringToFront();
		}
	}
	
	@FXML
	protected void aboutAction() {
		Stage stage = new Stage();
		AnchorPane addGrid = new AnchorPane();
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("About.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			addGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading About.fxml file!");
			//e.printStackTrace();
		}
		
		Scene scene = new Scene(addGrid);
		
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.setTitle("About");
	}
	
	@FXML
	protected void importHypnogrammAction() {
		FileChooser fileChooser = new FileChooser();
		
		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);	
		
		// Show open file dialog
		final File file = fileChooser.showOpenDialog(primaryStage);
		
		if (file != null) {
			try {
				openFile(file);
				updateStage();
				
				if (viewModel.isHypnogrammActive()) {
					hypnogramm.reloadHypnogramm();
				}
				
				if (viewModel.isEvaluationWindowActive()) {
					evaluationWindow.reloadEvaluationWindow();
				}
				
				System.out.println("Finished importing Hypnogramm!");
				
			} catch (IOException e) {
				System.err.println("Error during importing Hypnogramm!");
				popUp.createPopup("Error during importing Hypnogramm!");
				e.printStackTrace();
			}
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
	}
	
	// First Column: 0 -> W, 1 -> S1, 2 -> S2, 3 -> N, 5 -> REM
	// Second Column: 0 -> Nothing, 1 -> Movement arrousal, 2 -> Artefact, 3 -> Stimulation
	private void openFile(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String row = null;
		int epoch = 0;
		
		while (((row = in.readLine()) != null) && (epoch < dataPointsModel.getNumberOf30sEpochs()-1)) {
			String[] rowArray = row.split(" ");
			
			if (Integer.parseInt(rowArray[0]) == 5) {
				featureExtractionModel.setFeatureClassLabel(epoch, 5);
			} else {
				int label = Integer.parseInt(rowArray[0]);
				featureExtractionModel.setFeatureClassLabel(epoch, label + 1);
			}
			
			if (Integer.parseInt(rowArray[1]) == 1) {
				featureExtractionModel.addArrousalToEpochProperty(epoch);
			}
			
			if (Integer.parseInt(rowArray[1]) == 2) {
				featureExtractionModel.addArrousalToEpochProperty(epoch);
			}
			
			if (Integer.parseInt(rowArray[1]) == 3) {
				featureExtractionModel.addStimulationToEpochProperty(epoch);
			}
			
			epoch++;
		}
		
		in.close();

	}
	
	@FXML
	protected void exportHypnogrammAction() {
		FileChooser fileChooser = new FileChooser();
		  
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
        
        if(file != null){
            saveFile(file);
        }
	}
	
	// First Column: 0 -> W, 1 -> S1, 2 -> S2, 3 -> N, 5 -> REM
	// Second Column: 0 -> Nothing, 1 -> Movement arrousal, 2 -> Artefact, 3 -> Stimulation
	 private void saveFile(File file){
	        try {
	            FileWriter fileWriter = null;
	             
	            fileWriter = new FileWriter(file);
	            
	            for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
	            	int prop = 0;
	            	
	            	if (featureExtractionModel.getEpochProperty(i) != null) {
	            		Integer[] property = featureExtractionModel.getEpochProperty(i);
	            		
	            		if (property[0] == 1) {
	            			prop = 2;
	            		}
	            		
	            		if (property[1] == 1) {
	            			prop = 1;
	            		}
	            		
	            		if (property[2] == 1) {
	            			prop = 3;
	            		}
	            	}
	            	
	            	int classLabel = featureExtractionModel.getFeatureClassLabel(i);
	            	
	            	if (classLabel == 5) {
	            		classLabel = 5;
	            	} else {
	            		classLabel = classLabel - 1;
	            	}
	            	
	            	String content = classLabel + " " + prop + "\n";
					fileWriter.write(content);
				}
	            
	            fileWriter.close();
	            System.out.println("Finish writing!");
	        } catch (IOException ex) {
	        	popUp.createPopup("Could not save Hypnogramm!");
	        }
	         
	    }
	
	@FXML
	protected void awakeButtonOnAction(){
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 1.0);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s1ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 2.0);
		updateStage();

		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s2ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 3.0);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void s3ButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 4.0);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void remButtonOnAction() {
		featureExtractionModel.setFeatureClassLabel(currentEpoch, 5.0);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void artefactButtonOnAction() {
		featureExtractionModel.addArtefactToEpochProperty(currentEpoch);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void arrousalButtonOnAction() {
		featureExtractionModel.addArrousalToEpochProperty(currentEpoch);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void stimulationButtonOnAction() {
		featureExtractionModel.addStimulationToEpochProperty(currentEpoch);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
		lineChart.requestFocus();
	}
	
	@FXML
	protected void clearButtonOnAction() {
		featureExtractionModel.clearProperties(currentEpoch);
		updateStage();
		
		if (viewModel.isEvaluationWindowActive()) {
			evaluationWindow.reloadEvaluationWindow();			
		}
		
		if (viewModel.isHypnogrammActive()) {
			hypnogramm.reloadHypnogramm();
			hypnogramm.changeCurrentEpochMarker(currentEpoch);
		}
		
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
