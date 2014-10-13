package view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
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

import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import controller.DataReaderController;
import controller.ModelReaderWriterController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.Button;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import kcdetection.KCdetection;
import static kcdetection.KCdetection.filterKCs;
import static kcdetection.KCdetection.getKCs;
import static kcdetection.KCdetection.mergeKCs;
import tools.Signal;
import tools.Util;

public class FXApplicationController implements Initializable {

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
    private FXElectrodeConfiguratorController config;

    private final boolean autoMode;
    private final boolean recreateModelMode;
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

    private DoubleProperty scale;

    @FXML
    private ToggleButton awakeButton;
    @FXML
    private ToggleButton s1Button;
    @FXML
    private ToggleButton s2Button;
    @FXML
    private ToggleButton s3Button;
    @FXML
    private ToggleButton remButton;
    @FXML
    private ToggleButton artefactButton;
    @FXML
    private ToggleButton arrousalButton;
    @FXML
    private ToggleButton stimulationButton;
    @FXML
    private Button clearButton;

    @FXML
    private ToggleButton help1;
    private boolean help1Flag = false;

    @FXML
    private ToggleButton kComplex;

    @FXML
    private Button electrodeConfiguratorButton;

    private boolean kComplexFlag = false;

    @FXML
    private Label statusBarLabel1;
    @FXML
    private Label statusBarLabel2;
    @FXML
    private Label kComplexLabel;
    @FXML
    private TextField toolBarGoto;
//    @FXML
//    private TextField toolBarZoom;

    @FXML
    private GridPane statusBarGrid;
    @FXML
    private ToolBar statusBar;

    @FXML
    private Pane overlay;
    @FXML
    private Pane overlay2;
    @FXML
    private Pane overlay3;
    @FXML
    private Pane overlay4;

//	Canvas canvas = new Canvas();
//	ResizableCanvas canvas = new ResizableCanvas();
    @FXML
    private StackPane stackPane;
    @FXML
    private HBox statusBarHBox;

//    @FXML
//    private ChoiceBox<String> toolBarChoiceBox;
//    @FXML
//    private CheckBox toolBarCheckBox;
    @FXML
    private MenuItem showAdtVisualization;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;

    @FXML
    private Line line1;
    @FXML
    private Line line2;

    public FXApplicationController(DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel, boolean autoMode, boolean recreateModelMode) {
        scale = new SimpleDoubleProperty(1.);

        primaryStage = new Stage();
        this.dataReaderController = dataReaderController;
        this.dataPointsModel = dataPointsModel;
        this.featureExtractionModel = featureExtractionModel;
        this.viewModel = viewModel;

        featureExtractionModel.setAutoMode(autoMode);

        this.autoMode = featureExtractionModel.isAutoMode();
        this.recreateModelMode = recreateModelMode;

        if ((!autoMode) && (!recreateModelMode)) {
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
//        toolBarChoiceBox.setItems(choices);
//        toolBarChoiceBox.getSelectionModel().selectFirst();
//        currentChannelName = toolBarChoiceBox.getItems().get(0);

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

        if (autoMode || recreateModelMode) {
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

                        calculatePercentageKComplex();
                    }

                }
            }

        });

        overlay3.setOnMouseMoved(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouse) {

                if (help1Flag) {

                    LinkedList<Integer> activeChannels = returnActiveChannels();

                    double offsetSize = 1. / (activeChannels.size() + 1.);

                    double posOnOverlay = mouse.getY();
                    System.out.println("PosOnOverlay: " + posOnOverlay);

                    double zoom = 1.0;
                    for (int i = 0; i < activeChannels.size(); i++) {

                        double realOffset
                                = (1 - (i + 1.) * offsetSize);

                        double upperBound = yAxis.getDisplayPosition(
                                (realOffset - offsetSize / 2.) * yAxis.getUpperBound()
                        ) + yAxis.getLayoutY();

                        double lowerBound = yAxis.getDisplayPosition(
                                (realOffset + offsetSize / 2.) * yAxis.getUpperBound()
                        ) + yAxis.getLayoutY();

                        if ((posOnOverlay <= upperBound)
                                && (posOnOverlay > lowerBound)) {

                            System.out.println("realoffset" + yAxis.getDisplayPosition(
                                    realOffset * yAxis.getUpperBound())
                            );
                            System.out.println("upper" + upperBound);
                            System.out.println("lower" + lowerBound);

                            zoom = getZoomFromChannel(activeChannels.get(i));
                            System.out.println("Actice Channel: " + activeChannels.get(i));
                            System.out.println("Actice Zoom:  " + zoom);
                        }
                    }

                    double space = 75.0 / yAxis.getUpperBound() * zoom * scale.get();
                    System.out.println("Space: " + space);

                    // Now calculate the number of pixels from the microvolt size
                    space = (space / yAxis.getUpperBound()) * yAxis.getHeight();

                    paintSpacing(mouse.getY(), space);
                }

            }

        });

        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

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
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

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

        overlay3.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {

                lineChart.requestFocus();

            }
        });

        //Key Listener
        lineChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.RIGHT) {

                    if (currentEpoch < (dataPointsModel.getNumberOf30sEpochs() - 1)) {
                        lineChart.getData().clear();

                        overlay3.getChildren().clear();
                        lines.clear();

                        currentEpoch = currentEpoch + 1;
                        showEpoch(currentEpoch);

                        toolBarGoto.setText((currentEpoch + 1) + "");
                        statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));
                        updateStage();
                        updateProbabilities();

                        if (viewModel.isHypnogrammActive()) {
                            hypnogramm.changeCurrentEpochMarker(currentEpoch);
                        }

//                        if (kComplexFlag) {
//                            calculatePercentageKComplex();
//                        }
                    }

                }

                if (ke.getCode() == KeyCode.LEFT) {
                    if (currentEpoch > 0) {
                        lineChart.getData().clear();

                        overlay3.getChildren().clear();
                        lines.clear();

                        currentEpoch = currentEpoch - 1;
                        showEpoch(currentEpoch);

                        toolBarGoto.setText((currentEpoch + 1) + "");
                        statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));
                        updateStage();
                        updateProbabilities();

                        if (viewModel.isHypnogrammActive()) {
                            hypnogramm.changeCurrentEpochMarker(currentEpoch);
                        }

//                        if (kComplexFlag) {
//                            calculatePercentageKComplex();
//                        }
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

        toolBarGoto.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {

                    int valueTextField = -1;

                    try {
                        valueTextField = Integer.parseInt(toolBarGoto.getText());

                    } catch (NumberFormatException e) {
                        toolBarGoto.setText((currentEpoch + 1) + "");
                    }

                    if (valueTextField > dataPointsModel.getNumberOf30sEpochs()) {
                        valueTextField = dataPointsModel.getNumberOf30sEpochs();
                    }

                    if ((valueTextField <= dataPointsModel.getNumberOf30sEpochs()) && (valueTextField > 0)) {
                        lineChart.getData().clear();

                        currentEpoch = valueTextField - 1;
                        showEpoch(currentEpoch);

                        toolBarGoto.setText((currentEpoch + 1) + "");
                        statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));

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
                        toolBarGoto.setText((currentEpoch + 1) + "");
                    }
                }

            }
        });

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });
    }

    private void refreshZoom(double zoom) {
        lineChart.getData().clear();

        if (zoom == 1.) {
            scale.set(scale.get() * 1.1);
        }

        if (zoom == -1.) {
            scale.set(scale.get() / 1.1);
        }

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

            double wake = (Math.round((probabilities[1] * 100) * Math.pow(10d, 2)) / Math.pow(10d, 2));
            double n1 = (Math.round((probabilities[2] * 100) * Math.pow(10d, 2)) / Math.pow(10d, 2));
            double n2 = (Math.round((probabilities[0] * 100) * Math.pow(10d, 2)) / Math.pow(10d, 2));
            double n3 = (Math.round((probabilities[3] * 100) * Math.pow(10d, 2)) / Math.pow(10d, 2));
            double rem = (Math.round((probabilities[4] * 100) * Math.pow(10d, 2)) / Math.pow(10d, 2));

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

    public LinkedList<Integer> returnActiveChannels() {

        LinkedList<Integer> channels = new LinkedList<Integer>();

        for (int i = 0; i < channelNames.length; i++) {
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

    private String getNameFromChannel(int channelNumber) {

        String channel = channelNames[channelNumber];
        return channel;
    }

    public void showLabelsForEpoch(LinkedList<Integer> activeChannels) {

        overlay.getChildren().clear();
        double offsetSize = 1. / (activeChannels.size() + 1);

        for (int i = 0; i < activeChannels.size(); i++) {

            double realOffset = (1 - (i + 1.) * offsetSize) * yAxis.getUpperBound();

            Label label = new Label(getNameFromChannel(activeChannels.get(i)));
            label.setTextFill(Color.GRAY);
            label.setStyle("-fx-font-family: sans-serif;");
            label.setLayoutX(1);

            double labelPos = yAxis.getDisplayPosition(realOffset) + yAxis.getLayoutY();
            label.setLayoutY(labelPos);

            overlay.getChildren().add(label);

        }

    }

    public void goToEpoch(int epoch) {

        lineChart.getData().clear();

        currentEpoch = epoch;
        showEpoch(currentEpoch);

        toolBarGoto.setText((currentEpoch + 1) + "");
        statusBarLabel1.setText("Epoch " + (currentEpoch + 1) + "/" + (dataPointsModel.getNumberOf30sEpochs()));

        lineChart.requestFocus();
        updateStage();
        updateProbabilities();

        if (viewModel.isHypnogrammActive()) {
            hypnogramm.changeCurrentEpochMarker(currentEpoch);
        }
    }

    private void paintSpacing(double yAxis, double space) {

        double tmpSpace = space / 2;

        line1.setLayoutY(yAxis + tmpSpace);
        line2.setLayoutY(yAxis - tmpSpace);

    }

    public void showEpoch(int numberOfEpoch) {
        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();

        double offsetSize = 0;

        if (activeChannelNumbers.size() != 0) {
            offsetSize = 1. / (activeChannelNumbers.size() + 1.);
        }

        int modulo = 3;					// Take every second sample

        Set<Range<Integer>> kcPlotRanges = null;
        ArrayList<KCdetection.KC> kcList = new ArrayList();
        double[] epoch2 = null;

        for (int x = 0; x < activeChannelNumbers.size(); x++) {

            double zoom = getZoomFromChannel(activeChannelNumbers.get(x));

            // in local yAxis-coordinates
            double realOffset = (1 - (x + 1.) * offsetSize) * yAxis.getUpperBound();

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

            epoch2 = Util.LinkedList2Double(epoch);
            Signal.highpass(epoch2, 0.1, 0.5, 100);
            Signal.lowpass(epoch2, 4., 7., 100);

            for (int i = 0; i < epoch.size(); i++) {
                if (i % modulo == 0) {
                    double tmp = xAxis / epochSize;
                    tmp = tmp * 100;

//                    double value = epoch2[i];
                    double value = Math.sin(2*Math.PI*i/100.)*75/2.;
                    value = value / yAxis.getUpperBound();

                    value = value * zoom * scale.get();
                    value = value + realOffset;

                    XYChart.Data dataItem = new XYChart.Data(tmp, value);
                    series.getData().add(dataItem);

                    xAxis++;
                }
            }

            lineChart.getData().add(series);

            KCdetection.KC[] kcs = getKCs(epoch2);
            kcs = filterKCs(kcs, 20, 100, 0, 65);
            kcList.addAll(Arrays.asList(kcs));

            for (int i = 0; i < kcs.length; i++) {
                Line line = new Line();
                line.setStyle("-fx-stroke: black;");

                line.layoutXProperty()
                        .bind(this.xAxis.widthProperty()
                                .multiply(kcs[i].indexNeg / (double) epoch2.length)
                                .add(this.xAxis.getLocalToParentTransform().getTx())
                        );

                line.setLayoutY(0);
                line.endYProperty()
                        .bind(overlay3.heightProperty());

                overlay3.getChildren().add(line);
            }
        }

        kcPlotRanges = mergeKCs(kcList.toArray(new KCdetection.KC[0]));

        double percentageSum = 0;
        for (Range<Integer> e : kcPlotRanges) {
            percentageSum += (e.upperEndpoint() - e.lowerEndpoint()) / (double) epoch2.length;
        }
        percentageSum *= 100;

        kComplexLabel.setVisible(true);
        kComplexLabel.setText("K-Complex: " + roundValues(percentageSum) + "%");

        //draw yellow rectangles for every pair of coordinates in kcPlotRanges
        overlay4.getChildren().clear();

        double start;
        double stop;

        for (Iterator<Range<Integer>> iterator = kcPlotRanges.iterator(); iterator.hasNext();) {
            Range<Integer> next = iterator.next();
            start = next.lowerEndpoint();
            stop = next.upperEndpoint();

            Rectangle r = new Rectangle();
            r.layoutXProperty()
                    .bind(xAxis.widthProperty()
                            .multiply(start / (double) epoch2.length));
            r.setLayoutY(0);
            r.widthProperty()
                    .bind(xAxis.widthProperty()
                            .multiply((stop - start) / (double) epoch2.length));
            r.heightProperty()
                    .bind(overlay4.heightProperty());
            r.fillProperty().setValue(Color.LIGHTYELLOW);
            r.opacityProperty().set(1);

            overlay4.getChildren().add(r);
        }

        showLabelsForEpoch(returnActiveChannels());
        lineChart.requestFocus();

//		for (int y = 0; y < activeChannelNumbers.size(); y++) {
//			
//			LinkedList<Double> tmp = dataReaderController.readDataFileInt(dataPointsModel.getDataFile(), activeChannelNumbers.get(y), (numberOfEpoch + 1));	
//		
//		}
    }

    @FXML
    protected void help1OnAction() {
        if (help1Flag) {
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
        RangeSet<Double> rangeset = TreeRangeSet.create();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);

            double lengthOfLine;

            Range r = Range.closed(
                    Math.min(
                            line.getLayoutX(), line.getEndX() + line.getLayoutX()
                    ) / xAxis.getWidth() * 100. - 1e-9,
                    Math.max(
                            line.getLayoutX(), line.getEndX() + line.getLayoutX()
                    ) / xAxis.getWidth() * 100. + 1e-9
            );

            rangeset.add(r);

        }

        percentageSum = rangeset.asRanges()
                .stream()
                .mapToDouble(e
                        -> (e.upperEndpoint() - e.lowerEndpoint())
                )
                .sum();

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

    @FXML
    protected void showScatterPlot() {

        if (autoMode) {
            if (viewModel.isScatterPlotActive() == false) {
                scatterPlot = new FXScatterPlot(dataPointsModel, featureExtractionModel, viewModel);
                viewModel.setScatterPlotActive(true);
            } else {
                scatterPlot.bringToFront();
            }
        } else {
            popUp.showPopupMessage("Scatter plot only available in auto mode!", primaryStage);
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

        while (((row = in.readLine()) != null) && (epoch < dataPointsModel.getNumberOf30sEpochs() - 1)) {
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

        //Open directory from existing directory 
        File dir = null;
        dir = tools.Util.loadDir(
                new File(
                        new File(
                                getClass()
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation().getPath())
                        .getParentFile(),
                        "LastDirectory.txt"
                )
        );

        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        Util.saveDir(file,
                new File(
                        new File(
                                getClass()
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .getPath()
                        ).getParentFile(),
                        "LastDirectory.txt"
                )
        );

        if (file != null) {
            saveFile(file);
        }
    }

    // First Column: 0 -> W, 1 -> S1, 2 -> S2, 3 -> N, 5 -> REM
    // Second Column: 0 -> Nothing, 1 -> Movement arrousal, 2 -> Artefact, 3 -> Stimulation
    private void saveFile(File file) {
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
    protected void saveAsAction() {

        FileChooser fileChooser = new FileChooser();

        //Open directory from existing directory 
        File dir = null;
        dir = tools.Util.loadDir(
                new File(
                        new File(
                                getClass()
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation().getPath())
                        .getParentFile(),
                        "LastDirectory.txt"
                )
        );

        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("AS files (*.as)", "*.as");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        Util.saveDir(file,
                new File(
                        new File(
                                getClass()
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .getPath()
                        ).getParentFile(),
                        "LastDirectory.txt"
                )
        );

        if (file != null) {
            ModelReaderWriterController modelReaderWriter = new ModelReaderWriterController(dataPointsModel, featureExtractionModel, file, true);
            modelReaderWriter.start();

        }

    }

    @FXML
    protected void awakeButtonOnAction() {
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

    @FXML
    protected void electrodeConfiguratorButtonAction() {
        if (viewModel.isElectrodeConfiguratorActive() == false) {
            config = new FXElectrodeConfiguratorController(this.dataPointsModel, this.activeChannels, this.viewModel);
            viewModel.setElectrodeConfiguratorActive(true);
        } else {
            config.stage.close();
            viewModel.setElectrodeConfiguratorActive(false);
        }

        lineChart.requestFocus();
    }

    private void checkProp() {

        for (int i = 0; i < channelNames.length; i++) {
            Double[] prop = activeChannels.get(channelNames[i]);
            System.out.println(channelNames[i] + " " + prop[0] + " " + prop[1]);
        }
        System.out.println("----------------------------");
        lineChart.requestFocus();
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }

    public void clearLineChart() {
        lineChart.getData().clear();
        lineChart.requestFocus();
    }

}
