package view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.primitives.Doubles;

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
import controller.FeatureExtractionController;
import controller.ModelReaderWriterController;
import gnu.trove.list.array.TDoubleArrayList;
import help.ChannelNames;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
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
import org.apache.commons.math.stat.StatUtils;
import tools.NeuralNetworks;
import tools.Signal;
import tools.Util;

public class FXApplicationController implements Initializable {

    private FXHypnogrammController hypnogramm;
    private FXEvaluationWindowController evaluationWindow;
    private FXScatterPlot scatterPlot;

    //This epoch is just an puffer
//    private LinkedList<LinkedList<Double>> nextEpoch = new LinkedList<LinkedList<Double>>();
    private ArrayList<double[]> thisEpoch = new ArrayList();

    private FXPopUp popUp = new FXPopUp();
    private FXViewModel viewModel;

    private DataReaderController dataReaderController;
    private RawDataModel dataPointsModel;
    private FeatureExtractionModel featureExtractionModel;
    private FeatureExtractionController featureExtractionController;
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

    private HashMap<String, Double[]> activeChannels = new HashMap();

    private LinkedList<Line> lines = new LinkedList();

    private Stage primaryStage;
    private BorderPane mainGrid;
    final private Scene scene;

    private DoubleProperty scale = new SimpleDoubleProperty(1e-1);
    private DoubleProperty mouseX = new SimpleDoubleProperty(0.);
    private DoubleProperty mouseY = new SimpleDoubleProperty(0.);

    int modulo = 1;

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
    @FXML
    private Button classifyButton;
    @FXML
    private Button visualizeButton;

    private boolean kComplexFlag = false;

    @FXML
    private Label statusBarLabel1;
    @FXML
    private Label statusBarLabel2;
    @FXML
    private Label kComplexLabel;
    @FXML
    private TextField toolBarGoto;

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

    @FXML
    private StackPane stackPane;
    @FXML
    private HBox statusBarHBox;

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

    @FXML
    ProgressBar progressBar;

    public FXApplicationController(DataReaderController dataReaderController, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel, boolean autoMode, boolean recreateModelMode) {
        modulo = 2; //take every value for display

        primaryStage = new Stage();
        this.dataReaderController = dataReaderController;
        this.dataPointsModel = dataReaderController.getDataModel();
        this.featureExtractionModel = featureExtractionModel;
        this.featureExtractionController = new FeatureExtractionController(dataPointsModel, featureExtractionModel);
        this.viewModel = viewModel;

        featureExtractionModel.setAutoMode(autoMode);

        this.autoMode = featureExtractionModel.isAutoMode();
        this.recreateModelMode = recreateModelMode;

        if (!recreateModelMode) {
            featureExtractionModel.init(dataPointsModel.getNumberOf30sEpochs());
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

        statusBarLabel1.setText("/" + (dataPointsModel.getNumberOf30sEpochs()));

        //Configure lineChart
        lineChart.setSnapToPixel(true);
        lineChart.requestFocus();

        // Set Choice Box for the channels
        channelNames = dataPointsModel.getChannelNames();

        //Set properties for the channels
        for (int i = 0; i < channelNames.length; i++) {
            if (i < 6) {
                //The first value represents wheater the channel is shown
                //The second value represents the current zoom level
                Double[] channelProp = new Double[2];
                channelProp[0] = 1.0;
                channelProp[1] = 1.0;
                activeChannels.put(channelNames[i], channelProp);
            } else {
                //The first value represents wheater the channel is shown
                //The second value represents the current zoom level
                Double[] channelProp = new Double[2];
                channelProp[0] = 0.0;
                channelProp[1] = 1.0;
                activeChannels.put(channelNames[i], channelProp);
            }

        }

        loadEpoch(currentEpoch);
        showEpoch();
        computeKCfeatures();

        showLabelsForEpoch(returnActiveChannels());

        checkProp();

        if (autoMode || recreateModelMode) {
            updateStage();
        }

        updateProbabilities();

        initStarted = true;

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        overlay3.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouse) {
                mouseX.set(mouse.getX());
                mouseY.set(mouse.getY());

                if (kComplexFlag) {
                    if (mouse.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        Line line = new Line();
                        line.setStyle("-fx-stroke: red;");

                        overlay3.getChildren().add(line);

                        line.setStartX(0);
                        line.setStartY(0);
                        line.setLayoutX(mouseX.get());
                        line.setLayoutY(mouseY.get());

                        lines.add(line);

                    }

                    if (mouse.isPrimaryButtonDown()) {
                        Line line = lines.getLast();
                        double endXPos = mouseX.get() - line.getLayoutX();
                        double endYPos = mouseY.get() - line.getLayoutY();

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
                mouseX.set(mouse.getX());
                mouseY.set(mouse.getY());

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

                            zoom = getZoomFromChannel(activeChannels.get(i));
                            System.out.println("Actice Channel: " + activeChannels.get(i));
                            System.out.println("Actice Zoom:  " + zoom);
                        }
                    }

                    double space = 75.0 * zoom / yAxis.getUpperBound() * yAxis.getHeight();
                    System.out.println("Space: " + space);

                    paintSpacing(space);
                }

            }

        });

        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

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

                growCoefHeight = overlay3.getHeight() / oldHeight;
                oldHeight = overlay3.getHeight();

                for (int i = 0; i < lines.size(); i++) {
                    Line line = lines.get(i);
                    line.setLayoutY(line.getLayoutY() * growCoefHeight);
                    line.setEndY(line.getEndY() * growCoefHeight);
                    lines.set(i, line);
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
                keyAction(ke);
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
                        valueTextField = currentEpoch + 1;
                    }

                    if (valueTextField > dataPointsModel.getNumberOf30sEpochs()) {
                        valueTextField = dataPointsModel.getNumberOf30sEpochs();
                    }

                    if (valueTextField < 1) {
                        valueTextField = 1;
                    }

                    if ((valueTextField <= dataPointsModel.getNumberOf30sEpochs()) && (valueTextField > 0)) {

                        currentEpoch = valueTextField - 1;

                        loadEpoch(currentEpoch);
                        updateEpoch();
                        computeKCfeatures();

                        toolBarGoto.setText((currentEpoch + 1) + "");
                        statusBarLabel1.setText("/" + (dataPointsModel.getNumberOf30sEpochs()));

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

        kComplexOnAction();
        help1OnAction();
    }

    private void refreshZoom(double zoom) {

        if (zoom == 1.) {
            scale.set(scale.get() * 1.1);
        }

        if (zoom == -1.) {
            scale.set(scale.get() / 1.1);
        }

        updateEpoch();

        lineChart.requestFocus();
    }

    public void bringToFront() {
        primaryStage.toFront();
    }

    @SuppressWarnings("static-access")
    private void updateProbabilities() {
        if (featureExtractionModel.isFeaturesComputed()) {
            double[] probabilities = featureExtractionModel.getPredictProbabilities(currentEpoch);

            double total = StatUtils.sum(probabilities);
            int wake = (int) Math.round((probabilities[0] / total * 100));
            int n1 = (int) Math.round((probabilities[1] / total * 100));
            int n2 = (int) Math.round((probabilities[2] / total * 100));
            int n3 = (int) Math.round((probabilities[3] / total * 100));
            int rem = (int) Math.round((probabilities[4] / total * 100));

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

            double realOffset = (i + 1.) * offsetSize;

            Label label = new Label(getNameFromChannel(activeChannels.get(i)));
            label.setTextFill(Color.GRAY);
            label.setStyle("-fx-font-family: sans-serif;");
            label.setLayoutX(1);

            label.layoutYProperty()
                    .bind(
                            yAxis.heightProperty()
                            .multiply(realOffset)
                            .add(yAxis.layoutYProperty())
                    );

            overlay.getChildren().add(label);

        }

    }

    public void goToEpoch(int epoch) {

        if (epoch < 0) {
            epoch = 0;
        }

        if (epoch > (dataPointsModel.getNumberOf30sEpochs() - 1)) {
            epoch = dataPointsModel.getNumberOf30sEpochs() - 1;
        }

        overlay3.getChildren().clear();
        lines.clear();

        currentEpoch = epoch;

        loadEpoch(currentEpoch);
        updateEpoch();
        computeKCfeatures();

        toolBarGoto.setText((currentEpoch + 1) + "");
        statusBarLabel1.setText("/" + (dataPointsModel.getNumberOf30sEpochs()));

        lineChart.requestFocus();
        updateStage();
        updateProbabilities();

        if (viewModel.isHypnogrammActive()) {
            hypnogramm.changeCurrentEpochMarker(currentEpoch);
        }

        if (viewModel.isScatterPlotActive()) {
            scatterPlot.changeCurrentEpochMarker();
        }

        lineChart.requestFocus();
    }

    private void paintSpacing(double space) {

        double tmpSpace = space / 2;

        line1.layoutYProperty().bind(scale.multiply(+tmpSpace).add(mouseY));
        line1.endXProperty().bind(overlay3.widthProperty());
        line2.layoutYProperty().bind(scale.multiply(-tmpSpace).add(mouseY));
        line2.endXProperty().bind(overlay3.widthProperty());

    }

    final public void loadEpoch(int numberOfEpoch) {
        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();

        for (int i = 0; i < activeChannelNumbers.size(); i++) {

            TDoubleArrayList epoch = dataReaderController.read(activeChannelNumbers.get(i), numberOfEpoch);
            thisEpoch.add(i, epoch.toArray());
        }

        filterEpoch();
    }

    final public void filterEpoch() {
        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();
        for (int i = 0; i < activeChannelNumbers.size(); i++) {
            Signal.filtfilt(thisEpoch.get(i), viewModel.getDisplayHighpassCoefficients());
            Signal.filtfilt(thisEpoch.get(i), viewModel.getDisplayLowpasCoefficients());
        }
    }

    final public void showEpoch() {

        double[] epoch;

        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();

        double offsetSize = 0;
        if (activeChannelNumbers.size() != 0) {
            offsetSize = 1. / (activeChannelNumbers.size() + 1.);
        }

        for (int i = 0; i < activeChannelNumbers.size(); i++) {

            epoch = thisEpoch.get(i);

            double zoom = getZoomFromChannel(activeChannelNumbers.get(i));

            // in local yAxis-coordinates
            double realOffset = (1 - (i + 1.) * offsetSize) * yAxis.getUpperBound();

            @SuppressWarnings("rawtypes")
            XYChart.Series series = new XYChart.Series();

            double epochSize = epoch.length / modulo;
            double xAxis = 0;

            for (int j = 0; j < epoch.length; j++) {
                if (j % modulo == 0) {
                    double tmp = xAxis / epochSize;
                    tmp = tmp * this.xAxis.getUpperBound();

                    double value = epoch[j];
//                    double value = Math.sin(2 * Math.PI * i / 100.) * 75 / 2.; //test signal

                    value = value * zoom * scale.get();
                    value = value + realOffset;

                    XYChart.Data dataItem = new XYChart.Data(tmp, value);

                    series.getData().add(dataItem);

                    xAxis++;
                }
            }

            lineChart.getData().add(series);
        }

        computeKCfeatures();

        showLabelsForEpoch(returnActiveChannels());
        lineChart.requestFocus();

    }

    public void updateEpoch() {

        // works on list of XYChart.series
        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();

        double offsetSize = 0;

        if (activeChannelNumbers.size() != 0) {
            offsetSize = 1. / (activeChannelNumbers.size() + 1.);
        }

        double[] epoch = null;

        for (int i = 0; i < activeChannelNumbers.size(); i++) {

            epoch = thisEpoch.get(i);

            double zoom = getZoomFromChannel(activeChannelNumbers.get(i));

            // in local yAxis-coordinates
            double realOffset = (1 - (i + 1.) * offsetSize) * yAxis.getUpperBound();

            int k = 0;
            for (int j = 0; j < epoch.length; j++) {
                if (j % modulo == 0) {

                    double value = epoch[j];

                    value = value * zoom * scale.get();
                    value = value + realOffset;

                    lineChart.getData().get(i).getData().get(k).setYValue(value);
                    k++;
                }
            }

        }
    }

    final public void computeKCfeatures() {

        LinkedList<Integer> activeChannelNumbers = returnActiveChannels();

        Set<Range<Integer>> kcPlotRanges = null;
        ArrayList<KCdetection.KC> kcList = new ArrayList();
        double[] epoch2 = null;

        for (Integer activeChannelNumber : activeChannelNumbers) {
            epoch2 = thisEpoch.get(activeChannelNumber).clone();

            Signal.lowpass(epoch2, 4., 7., 100);
            KCdetection.KC[] kcs = getKCs(epoch2);
            kcs = filterKCs(kcs, 20, 100, 0, 65);
            kcList.addAll(Arrays.asList(kcs));
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

        for (Range<Integer> next : kcPlotRanges) {
            start = next.lowerEndpoint();
            stop = next.upperEndpoint();

            Rectangle r = new Rectangle();
            r.layoutXProperty()
                    .bind(this.xAxis.widthProperty()
                            .multiply((start + 1.) / (double) epoch2.length)
                            .add(this.xAxis.layoutXProperty())
                    );

            r.setLayoutY(0);
            r.widthProperty()
                    .bind(xAxis.widthProperty()
                            .multiply((stop - start) / (double) epoch2.length));

            r.heightProperty()
                    .bind(overlay4.heightProperty());
            r.fillProperty().setValue(Color.LIGHTBLUE);
            r.opacityProperty().set(0.5);

            overlay4.getChildren().add(r);
        }

        showLabelsForEpoch(returnActiveChannels());
        lineChart.requestFocus();

        //            //test KC detection
//            for (int i = 0; i < kcs.length; i++) {
//                Line line = new Line();
//                line.setStyle("-fx-stroke: black;");
//
//                line.layoutXProperty()
//                        .bind(this.xAxis.widthProperty()
//                                .multiply((kcs[i].indexNeg + 1)/ (double) epoch2.length)
//                                .add(this.xAxis.layoutXProperty())
//                        );
//
//                line.setLayoutY(0);
//                line.endYProperty()
//                        .bind(overlay3.heightProperty());
//
//                overlay3.getChildren().add(line);
//            }
//        for (KCdetection.KC kc : kcList) {
//            System.out.println(kc.indexPrePos);
//            System.out.println(kc.indexNeg);
//            System.out.println(kc.indexPostPos);
//        }
//        for (Range range : kcPlotRanges) {
//            System.out.println(range.toString());
//        }
//        System.out.println("======================");
    }

    @FXML
    protected void help1OnAction() {
        if (help1Flag) {
            help1Flag = false;
            line1.setVisible(false);
            line2.setVisible(false);

            if (help1.isSelected()) {
                help1.setSelected(false);
            }

        } else {
            help1Flag = true;
            line1.setVisible(true);
            line2.setVisible(true);

            if (!help1.isSelected()) {
                help1.setSelected(true);
            }
        }

        lineChart.requestFocus();
    }

    @FXML
    protected void kComplexOnAction() {
        if (kComplexFlag) {
            kComplexFlag = false;

            overlay3.getChildren().clear();
            lines.clear();

            if (kComplex.isSelected()) {
                kComplex.setSelected(false);
            }

        } else {
            kComplexFlag = true;
            kComplexLabel.setVisible(true);

            if (!kComplex.isSelected()) {
                kComplex.setSelected(true);
            }
        }

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

        if (viewModel.isScatterPlotActive() == false) {
            scatterPlot = new FXScatterPlot(dataReaderController, dataPointsModel, featureExtractionModel, featureExtractionController, viewModel);
            viewModel.setScatterPlotActive(true);
        } else {
            scatterPlot.stage.close();
            viewModel.setScatterPlotActive(false);
        }
    }

    @FXML
    protected void aboutAction() {
        Stage stage = new Stage();
        AnchorPane addGrid = new AnchorPane();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class
                .getResource("About.fxml"));
        loader.setController(
                this);

        // Try to load fxml file
        try {
            addGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading About.fxml file!");
            //e.printStackTrace();
        }

        Scene scene = new Scene(addGrid);

        stage.setResizable(
                false);
        stage.setScene(scene);

        stage.show();

        stage.setTitle(
                "About");
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
        featureExtractionModel.setFeatureClassLabel(currentEpoch, 1);
        updateWindows();
    }

    @FXML
    protected void s1ButtonOnAction() {
        featureExtractionModel.setFeatureClassLabel(currentEpoch, 2);
        updateWindows();
    }

    @FXML
    protected void s2ButtonOnAction() {
        featureExtractionModel.setFeatureClassLabel(currentEpoch, 3);
        updateWindows();
    }

    @FXML
    protected void s3ButtonOnAction() {
        featureExtractionModel.setFeatureClassLabel(currentEpoch, 4);
        updateWindows();
    }

    @FXML
    protected void remButtonOnAction() {
        featureExtractionModel.setFeatureClassLabel(currentEpoch, 5);
        updateWindows();
    }

    @FXML
    protected void artefactButtonOnAction() {
        featureExtractionModel.addArtefactToEpochProperty(currentEpoch);
        updateWindows();
    }

    @FXML
    protected void arrousalButtonOnAction() {
        featureExtractionModel.addArrousalToEpochProperty(currentEpoch);
        updateWindows();
    }

    @FXML
    protected void stimulationButtonOnAction() {
        featureExtractionModel.addStimulationToEpochProperty(currentEpoch);
        updateWindows();
    }

    @FXML
    protected void clearButtonOnAction() {
        featureExtractionModel.clearProperties(currentEpoch);
        updateWindows();
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

    @FXML
    protected void classifyButtonAction() {

//        ObservableList<String> choices = FXCollections.observableArrayList();
//
//        File folder = new File(".").getAbsoluteFile();
//        for (File file : folder.listFiles()) {
//            if (file.getName().contains("model")) {
//                choices.add(file.getName());
//            }
//        }
//        popUp.showPopupMessage("This will overwrite previous classification", primaryStage);
        if (featureExtractionModel.isClassificationDone() == false) {

            if (!featureExtractionModel.isReadinDone()) {
                dataReaderController.readAll(5);
                featureExtractionModel.setReadinDone(true);

                dataPointsModel.setFeatureChannelData(
                        featureExtractionController.assembleData(
                                dataPointsModel.rawEpochs,
                                dataPointsModel.getNumberOf30sEpochs() * 3000)
                );

                Signal.filtfilt(dataPointsModel.getFeatureChannelData(),
                        featureExtractionModel.getHighpassCoefficients());

            }

            if (!featureExtractionModel.isFeaturesComputed()) {
                featureExtractionController.start();
                featureExtractionModel.setFeaturesComputed(true);
            }

            if (!featureExtractionModel.isClassificationDone()) {
                classify();
                featureExtractionModel.setClassificationDone(true);
            }

            updateProbabilities();
            updateStage();

            hypnogramAction();

            classifyButton.setDisable(true);
            featureExtractionModel.setClassificationDone(true);
        } else {
            classifyButton.setDisable(true);
        }
    }

    @FXML
    protected void visualizeButtonAction() {
        classifyButtonAction();
        showScatterPlot();
    }

    public void classify() {

//          if ((startModel.getSelectedModel() != null) || (startModel.isAutoModeFlag() == false)) {
//            featureExtractionModel.setSelectedModel(startModel.getSelectedModel());
//            startAction(file);
//        } else {
//            popUp.showPopupMessage("Please first go to settings and select a model!", primaryStage);
//        }
        // Creats a new controller which reads the declared file
//        featureExtractionModel.setFileLocation(fileLocation);
        for (int i = 0; i < channelNames.length; i++) {
            String channel = channelNames[i];

            switch (channel) {
                case "Fz":
                    featureExtractionModel.setChannelName(ChannelNames.Fz);
                    break;
                default:
                    featureExtractionModel.setChannelName(ChannelNames.UNKNOWN);
                    break;
            }
        }

//        // Check whether the the SVM Model is trained for one of the given channels
//        File folder = new File(".").getAbsoluteFile();
//        for (File file : folder.listFiles()) {
//            for (int i = 0; i < channelNames.length; i++) {
//                if (file.getName().contains(channelNames[i]) && file.getName().contains("model")) {
//                    flag = true;
//                    channelNumbersToRead.add(i);
//                }
//            }
//        }
        NeuralNetworks nn = new NeuralNetworks("D:\\annDefinition1");

        double[] output;
        for (int i = 0; i < featureExtractionModel.getNumberOfEpochs(); i++) {
            output = nn.net(Util.floatToDouble(featureExtractionModel.getFeatureVector(i)));

            int classLabel = Doubles.indexOf(output, Doubles.max(output)) + 1;
            featureExtractionModel.setPredictProbabilities(i, output.clone());
            featureExtractionModel.setFeatureClassLabel(i, classLabel);
        }
        featureExtractionModel.setClassificationDone(true);

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

    public void requestFocus() {
        lineChart.requestFocus();
    }

    private void hypnogramAction() {
        if (!featureExtractionModel.isReadinDone()) {
            dataReaderController.readAll(5);
            featureExtractionModel.setReadinDone(true);

            dataPointsModel.setFeatureChannelData(
                    featureExtractionController.assembleData(
                            dataPointsModel.rawEpochs,
                            dataPointsModel.getNumberOf30sEpochs() * 3000)
            );

            Signal.filtfilt(dataPointsModel.getFeatureChannelData(),
                    featureExtractionModel.getHighpassCoefficients());

        }

        if (hypnogramm == null) {
            hypnogramm = new FXHypnogrammController(dataPointsModel, featureExtractionModel, viewModel);
        }

        if (viewModel.isHypnogrammActive() == false) {
            hypnogramm.reloadHypnogramm();
            hypnogramm.changeCurrentEpochMarker(currentEpoch);

            hypnogramm.show();
            hypnogramm.bringToFront();
            viewModel.setHypnogrammActive(true);

        } else {
            hypnogramm.bringToFront();
            viewModel.setHypnogrammActive(true);
            hypnogramm.reloadHypnogramm();
        }
    }

    public void keyAction(KeyEvent ke) {
        if (ke.getCode() == KeyCode.RIGHT) {
            goToEpoch(currentEpoch + 1);
        }

        if (ke.getCode() == KeyCode.LEFT) {
            goToEpoch(currentEpoch - 1);
        }

        if (ke.getCode() == KeyCode.H) {
            hypnogramAction();
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
        }

        if (ke.getCode() == KeyCode.K) {
            kComplexOnAction();

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

        if (ke.getCode() == KeyCode.PAGE_DOWN) {
            goToEpoch(currentEpoch - 10);
        }

        if (ke.getCode() == KeyCode.PAGE_UP) {
            goToEpoch(currentEpoch + 10);
        }

        if (ke.getCode() == KeyCode.END) {
            goToEpoch(dataPointsModel.getNumberOf30sEpochs() - 1);
        }

        if (ke.getCode() == KeyCode.HOME) {
            goToEpoch(0);
        }
    }

    private void updateWindows() {
        updateStage();

        if (viewModel.isEvaluationWindowActive()) {
            evaluationWindow.reloadEvaluationWindow();
        }

        if (viewModel.isHypnogrammActive()) {
            hypnogramm.reloadHypnogramm();
            hypnogramm.changeCurrentEpochMarker(currentEpoch);
        }

        if (viewModel.isScatterPlotActive()) {
            scatterPlot.updateScatterPlot();
            scatterPlot.changeCurrentEpochMarker();
        }

        lineChart.requestFocus();
    }
}
