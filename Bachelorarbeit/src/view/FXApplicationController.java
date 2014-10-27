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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
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
import org.jdsp.iirfilterdesigner.IIRDesigner;
import org.jdsp.iirfilterdesigner.exceptions.BadFilterParametersException;
import org.jdsp.iirfilterdesigner.model.ApproximationFunctionType;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;
import org.jdsp.iirfilterdesigner.model.FilterType;

public class FXApplicationController implements Initializable {

    private FilterCoefficients displayHighpassCoefficients;

    private FilterCoefficients displayLowpasCoefficients;

    private FilterCoefficients highpassCoefficients;

    private FilterCoefficients lowpassCoefficients;

    private FXHypnogrammController hypnogram;
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
    private FXElectrodeConfiguratorController electrodeConfigurator;

    private final boolean recreateModelMode;

    private double oldWidth;
    private double growCoefWidth = 1.0;

    private double oldHeight;
    private double growCoefHeight = 1.0;

    private int currentEpoch = 0;
    private String[] channelNames;

    private HashMap<String, Double[]> allChannels = new HashMap();
    LinkedList<Integer> activeChannels = new LinkedList();

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
    private ToggleButton arousalButton;
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
    private ToggleButton electrodeConfiguratorButton;
    @FXML
    private ToggleButton classifyButton;
    @FXML
    private ToggleButton visualizeButton;

    @FXML
    private ToggleButton kcMarkersButton;
    @FXML
    private ToggleButton dcRemoveButton;
    @FXML
    private ToggleButton filterButton;
    @FXML
    private ToggleButton hypnogramButton;

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

    @FXML
    ChoiceBox<String> choiceBox;

    public FXApplicationController(DataReaderController dataReaderController, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel, boolean recreateModelMode) {
        modulo = 2; //take every value for display

        primaryStage = new Stage();
        this.dataReaderController = dataReaderController;
        this.dataPointsModel = dataReaderController.getDataModel();
        this.featureExtractionModel = featureExtractionModel;
        this.featureExtractionController = new FeatureExtractionController(dataPointsModel, featureExtractionModel);
        this.viewModel = viewModel;

        this.recreateModelMode = recreateModelMode;

        if (!recreateModelMode) {
            featureExtractionModel.init(dataPointsModel.getNumberOf30sEpochs());
        } else {
            currentEpoch = featureExtractionModel.getCurrentEpoch();
        }

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Application.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            mainGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading Application.fxml file!");
            e.printStackTrace();
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
                allChannels.put(channelNames[i], channelProp);
            } else {
                //The first value represents wheater the channel is shown
                //The second value represents the current zoom level
                Double[] channelProp = new Double[2];
                channelProp[0] = 0.0;
                channelProp[1] = 1.0;
                allChannels.put(channelNames[i], channelProp);
            }

        }

        ObservableList<String> choices = FXCollections.observableArrayList(Arrays.asList(channelNames));
        choiceBox.setItems(choices);
        choiceBox.getSelectionModel().select(5);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                featureExtractionModel.setFeatureChannel(newValue.intValue());
            }

        });
        choiceBox.setTooltip(new Tooltip("Select the channel to classify from"));

        tooltips();

        createFilters();
        loadEpoch(currentEpoch);
        showEpoch();

        showLabelsForEpoch();

        checkProp();

        updateWindows();

//        classifyButton.setSelected(viewModel.);
//        hypnogramButton.setSelected(viewModel.isHypnogrammActive());
        featureExtractionModel.setFileLocation(dataPointsModel.getOrgFile());
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

                    if (mouse.isPrimaryButtonDown() && (!lines.isEmpty())) {
                        Line line = lines.getLast();
                        double endXPos = mouseX.get() - line.getLayoutX();
                        double endYPos = mouseY.get() - line.getLayoutY();

                        line.setEndX(endXPos);
                        line.setEndY(endYPos);

                        if (kComplexFlag) {
                            calculatePercentageKComplex();
                        }

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

                    double offsetSize = 1. / (activeChannels.size() + 1.);

                    double posOnOverlay = mouse.getY();

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
                        }
                    }

                    double space = 75.0 * zoom / yAxis.getUpperBound() * yAxis.getHeight();
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

                        toolBarGoto.setText((currentEpoch + 1) + "");
                        statusBarLabel1.setText("/" + (dataPointsModel.getNumberOf30sEpochs()));

                        lineChart.requestFocus();
                        updateStage();
                        updateProbabilities();

                        if (viewModel.isHypnogrammActive()) {
                            hypnogram.changeCurrentEpochMarker();
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
                try {
                    System.out.println("RandomAccessFile closed");
                    dataPointsModel.getDataFile().close();
                } catch (IOException ex) {
                    Logger.getLogger(FXApplicationController.class.getName()).log(Level.SEVERE, null, ex);
                }
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

            String print = "W: " + wake + "%  N1: " + n1 + "%  N2: " + n2 + "%  N3: " + n3 + "%  REM: " + rem + "%";

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
        int label = featureExtractionModel.getLabel(currentEpoch);
        switch (label) {
            case 0:
                awakeButton.setSelected(true);
                s1Button.setSelected(false);
                s2Button.setSelected(false);
                s3Button.setSelected(false);
                remButton.setSelected(false);
                break;
            case 1:
                awakeButton.setSelected(false);
                s1Button.setSelected(true);
                s2Button.setSelected(false);
                s3Button.setSelected(false);
                remButton.setSelected(false);
                break;
            case 2:
                awakeButton.setSelected(false);
                s1Button.setSelected(false);
                s2Button.setSelected(true);
                s3Button.setSelected(false);
                remButton.setSelected(false);
                break;
            case 3:
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
            case -1:
                awakeButton.setSelected(false);
                s1Button.setSelected(false);
                s2Button.setSelected(false);
                s3Button.setSelected(false);
                remButton.setSelected(false);
                break;
        }

        if (featureExtractionModel.getArtefact(currentEpoch) == 1) {
            artefactButton.setSelected(true);
        } else {
            artefactButton.setSelected(false);
        }

        if (featureExtractionModel.getArousal(currentEpoch) == 1) {
            arousalButton.setSelected(true);
        } else {
            arousalButton.setSelected(false);
        }

        if (featureExtractionModel.getStimulation(currentEpoch) == 1) {
            stimulationButton.setSelected(true);
        } else {
            stimulationButton.setSelected(false);
        }

        kcMarkersButton.setSelected(viewModel.isKcMarkersActive());
        filterButton.setSelected(viewModel.isFiltersActive());
        dcRemoveButton.setSelected(viewModel.isDcRemoveActive());
        visualizeButton.setSelected(viewModel.isScatterPlotActive());
        electrodeConfiguratorButton.setSelected(viewModel.isElectrodeConfiguratorActive());
        hypnogramButton.setSelected(viewModel.isHypnogrammActive());
    }

    public LinkedList<Integer> returnActiveChannels() {

        activeChannels = new LinkedList<Integer>();

        for (int i = 0; i < channelNames.length; i++) {
            Double[] tempProp = allChannels.get(channelNames[i]);
            if (tempProp[0] == 1.0) {
                activeChannels.add(i);
            }
        }

        return activeChannels;

    }

    private double getZoomFromChannel(int channelNumber) {

        String channel = channelNames[channelNumber];

        Double[] tempProp = allChannels.get(channel);
        double channelZoom = tempProp[1];

        return channelZoom;

    }

    private String getNameFromChannel(int channelNumber) {

        String channel = channelNames[channelNumber];
        return channel;
    }

    public void showLabelsForEpoch() {

        overlay.getChildren().clear();

        double offsetSize = 1. / (activeChannels.size() + 1);

        for (int i = 0; i < activeChannels.size(); i++) {

            double realOffset = (i + 1.) * offsetSize;

            Label label = new Label(channelNames[activeChannels.get(i)]);
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

        currentEpoch = epoch;

        overlay3.getChildren().clear();
        lines.clear();

        loadEpoch(currentEpoch);
        updateEpoch();

        updateWindows();

        featureExtractionModel.setCurrentEpoch(currentEpoch);

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
        returnActiveChannels();
        for (Integer activeChannel : activeChannels) {
            TDoubleArrayList epoch = dataReaderController.read(activeChannel, numberOfEpoch);
            thisEpoch.add(activeChannel, epoch.toArray());
        }

        if (dataPointsModel.getSamplingRateConvertedToHertz() != 100) {
            decimateSignal();
        }

        if (viewModel.isKcMarkersActive()) {
            computeKCfeatures();
        } else {
            overlay4.getChildren().clear();
        }

        if (viewModel.isFiltersActive() == true) {
            filterEpoch();
        }

        if ((viewModel.isDcRemoveActive() == true) && (viewModel.isFiltersActive() == false)) {
            removeDcOffset();
        }

    }

    final public void filterEpoch() {
        for (Integer activeChannel : activeChannels) {
            Signal.filtfilt(thisEpoch.get(activeChannel), getDisplayHighpassCoefficients());
            Signal.filtfilt(thisEpoch.get(activeChannel), getDisplayLowpasCoefficients());
        }
    }

    final public void removeDcOffset() {
        for (Integer activeChannel : activeChannels) {
            Signal.removeDC(thisEpoch.get(activeChannel));
        }
    }

    final public void decimateSignal() {
        for (Integer activeChannel : activeChannels) {
            thisEpoch.set(activeChannel, Signal.resample(thisEpoch.get(activeChannel), (int) dataPointsModel.getSamplingRateConvertedToHertz()));
        }
    }

    final public void showEpoch() {

        lineChart.getData().clear();

        double[] epoch;

        double offsetSize = 0;
        if (activeChannels.size() != 0) {
            offsetSize = 1. / (activeChannels.size() + 1.);
        }

        for (int i = 0; i < activeChannels.size(); i++) {

            epoch = thisEpoch.get(activeChannels.get(i));

            double zoom = getZoomFromChannel(activeChannels.get(i));

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

        showLabelsForEpoch();
        lineChart.requestFocus();

    }

    public void updateEpoch() {

        // works on list of XYChart.series
        returnActiveChannels();

        double offsetSize = 0;

        if (activeChannels.size() != 0) {
            offsetSize = 1. / (activeChannels.size() + 1.);
        }

        double[] epoch = null;

        for (int i = 0; i < activeChannels.size(); i++) {

            epoch = thisEpoch.get(activeChannels.get(i));

            double zoom = getZoomFromChannel(activeChannels.get(i));

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
        overlay4.getChildren()
                .clear();

        Set<Range<Integer>> kcPlotRanges = null;
        ArrayList<KCdetection.KC> kcList = new ArrayList();
        double[] epoch2 = null;

        for (Integer activeChannel : activeChannels) {
            epoch2 = thisEpoch.get(activeChannel).clone();
            Signal.filtfilt(epoch2, getLowpassCoefficients());
            Signal.filtfilt(epoch2, getDisplayHighpassCoefficients());
            KCdetection.KC[] kcs = getKCs(epoch2);
            kcs = filterKCs(kcs, 15, 100, 0, 65);
            if (kcs != null) {
                kcList.addAll(Arrays.asList(kcs));
            }
        }

        kcPlotRanges = mergeKCs(kcList.toArray(new KCdetection.KC[0]));

        double percentageSum = 0;
        for (Range<Integer> e : kcPlotRanges) {
            percentageSum += (e.upperEndpoint() - e.lowerEndpoint()) / (double) thisEpoch.get(0).length;
        }
        percentageSum *= 100;

        kComplexLabel.setVisible(
                true);
        kComplexLabel.setText(
                "K-Complex: " + roundValues(percentageSum) + "%");

        System.out.println(roundValues(percentageSum));
        //draw yellow rectangles for every pair of coordinates in kcPlotRanges
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

        showLabelsForEpoch();

        lineChart.requestFocus();
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
    protected void closeAction() {
        System.exit(0);
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
                    hypnogram.updateHypnogram();
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
            hypnogram.changeCurrentEpochMarker();
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
                featureExtractionModel.setLabel(epoch, 5);
            } else {
                int label = Integer.parseInt(rowArray[0]);
                featureExtractionModel.setLabel(epoch, label);
            }

            if (Integer.parseInt(rowArray[1]) == 1) {
                featureExtractionModel.addArousalToEpochProperty(epoch);
            }

            if (Integer.parseInt(rowArray[1]) == 2) {
                featureExtractionModel.addArousalToEpochProperty(epoch);
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
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);

            String content = "Stage" + " "
                    + "Arousal" + " "
                    + "Artefact" + " "
                    + "Stimulation"
                    + "\n";
            fileWriter.write(content);

            for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
                featureExtractionModel.getLabel(i);

                content = featureExtractionModel.getLabel(i) + " "
                        + featureExtractionModel.getArousal(i) + " "
                        + featureExtractionModel.getArtefact(i) + " "
                        + featureExtractionModel.getStimulation(i) + " "
                        + "\n";
                fileWriter.write(content);
            }

            System.out.println("Finish writing!");
        } catch (IOException ex) {
            popUp.createPopup("Could not save Hypnogramm!");
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        featureExtractionModel.setLabel(currentEpoch, 0);
        updateWindows();
    }

    @FXML
    protected void s1ButtonOnAction() {
        featureExtractionModel.setLabel(currentEpoch, 1);
        updateWindows();
    }

    @FXML
    protected void s2ButtonOnAction() {
        featureExtractionModel.setLabel(currentEpoch, 2);
        updateWindows();
    }

    @FXML
    protected void s3ButtonOnAction() {
        featureExtractionModel.setLabel(currentEpoch, 3);
        updateWindows();
    }

    @FXML
    protected void remButtonOnAction() {
        featureExtractionModel.setLabel(currentEpoch, 5);
        updateWindows();
    }

    @FXML
    protected void artefactButtonOnAction() {
        featureExtractionModel.addArtefactToEpochProperty(currentEpoch);
        updateWindows();
    }

    @FXML
    protected void arousalButtonOnAction() {
        if ((featureExtractionModel.getArousal(currentEpoch) == 0)
                && (featureExtractionModel.getArtefact(currentEpoch) == 0)) {
            featureExtractionModel.addArtefactToEpochProperty(currentEpoch);
        }
        featureExtractionModel.addArousalToEpochProperty(currentEpoch);
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
    protected void classifyButtonAction() {

        if (featureExtractionModel.isClassificationDone() == false) {

            if (!featureExtractionModel.isReadinDone()) {
                dataReaderController.readAll(featureExtractionModel.getFeatureChannel());
                featureExtractionModel.setReadinDone(true);

                dataPointsModel.setFeatureChannelData(
                        featureExtractionController.assembleData(
                                dataPointsModel.rawEpochs,
                                dataPointsModel.getNumberOf30sEpochs() * 3000)
                );

                Signal.filtfilt(dataPointsModel.getFeatureChannelData(),
                        getHighpassCoefficients());

            }

            if (!featureExtractionModel.isFeaturesComputed()) {
                featureExtractionController.start();
                featureExtractionModel.setFeaturesComputed(true);
            }

            if (!featureExtractionModel.isClassificationDone()) {
                classify();
                featureExtractionModel.setClassificationDone(true);
            }

            viewModel.setHypnogrammActive(true);
            updateWindows();

            classifyButton.setDisable(true);
            featureExtractionModel.setClassificationDone(true);
        } else {
            classifyButton.setDisable(true);
        }
    }

    @FXML
    protected void visualizeButtonAction() {
        if (viewModel.isScatterPlotActive()) {
            viewModel.setScatterPlotActive(false);
        } else {
            viewModel.setScatterPlotActive(true);
        }

        classifyButtonAction();
        updateWindows();

        lineChart.requestFocus();
    }

    public void classify() {

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
            if (featureExtractionModel.getLabel(i) == -1) {
                output = nn.net(Util.floatToDouble(featureExtractionModel.getFeatureVector(i)));

                int classLabel = Doubles.indexOf(output, Doubles.max(output));
                if (classLabel == 4) {
                    classLabel = 5;
                }
                featureExtractionModel.setPredictProbabilities(i, output.clone());
                featureExtractionModel.setLabel(i, classLabel);
            }
        }
        featureExtractionModel.setClassificationDone(true);

    }

    private void checkProp() {

        for (int i = 0; i < channelNames.length; i++) {
            Double[] prop = allChannels.get(channelNames[i]);
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

    @FXML
    protected void electrodeConfiguratorButtonAction() {
        if (viewModel.isElectrodeConfiguratorActive()) {
            viewModel.setElectrodeConfiguratorActive(false);
        } else {
            viewModel.setElectrodeConfiguratorActive(true);
        }
        updateWindows();
    }

    private void showElectrodeConfigurator() {
        if (viewModel.isElectrodeConfiguratorActive()) {
            if (electrodeConfigurator == null) {
                electrodeConfigurator = new FXElectrodeConfiguratorController(this.dataPointsModel, this.allChannels, this.viewModel);
            }
            electrodeConfigurator.show();
        } else {
            if (electrodeConfigurator != null) {
                electrodeConfigurator.hide();
            }
        }

        lineChart.requestFocus();
    }

    @FXML
    protected void showAdtVisualizationAction() {
        if (viewModel.isEvaluationWindowActive()) {
            viewModel.setEvaluationWindowActive(false);
        } else {
            viewModel.setEvaluationWindowActive(true);
        }
        updateWindows();

    }

    private void showEvaluationWindow() {
        if (viewModel.isEvaluationWindowActive()) {
            if (evaluationWindow == null) {
                evaluationWindow = new FXEvaluationWindowController(dataPointsModel, featureExtractionModel, viewModel);
            }
            evaluationWindow.reloadEvaluationWindow();
            evaluationWindow.show();
        } else {
            if (evaluationWindow != null) {
                evaluationWindow.hide();
            }
        }
    }

    @FXML
    protected void showScatterPlot() {
        if (viewModel.isScatterPlotActive()) {
            if (scatterPlot == null) {
                scatterPlot = new FXScatterPlot(dataReaderController, dataPointsModel, featureExtractionModel, featureExtractionController, viewModel);
            }
            if (scatterPlot.scatterChartDone) {
                scatterPlot.updateScatterPlot();
                scatterPlot.changeCurrentEpochMarker();
                scatterPlot.show();
            }

        } else {
            if (scatterPlot != null) {
                scatterPlot.hide();
            }
        }
    }

    @FXML
    private void hypnogramAction() {
        if (viewModel.isHypnogrammActive()) {
            hypnogramButton.setSelected(false);
            viewModel.setHypnogrammActive(false);
        } else {
            hypnogramButton.setSelected(true);
            viewModel.setHypnogrammActive(true);
        }
        updateWindows();
    }

    private void showHypnogram() {
        if (viewModel.isHypnogrammActive()) {
            if (hypnogram == null) {
                hypnogram = new FXHypnogrammController(dataPointsModel, featureExtractionModel, viewModel);
            }
            hypnogram.updateHypnogram();
            hypnogram.changeCurrentEpochMarker();
            hypnogram.show();
        } else {
            if (hypnogram != null) {
                hypnogram.hide();
            }
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
            showAdtVisualizationAction();
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
            if (featureExtractionModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.R) {
            remButtonOnAction();
            if (featureExtractionModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT1) {
            s1ButtonOnAction();
            if (featureExtractionModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT2) {
            s2ButtonOnAction();
            if (featureExtractionModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT3) {
            s3ButtonOnAction();
            if (featureExtractionModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.A) {
            artefactButtonOnAction();
        }

        if (ke.getCode() == KeyCode.M) {
            arousalButtonOnAction();
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
        if (ke.getCode() == KeyCode.F7) {
            classifyButtonAction();
        }
        if (ke.getCode() == KeyCode.F8) {
            visualizeButtonAction();
        }
        if (ke.getCode() == KeyCode.F9) {
            electrodeConfiguratorButtonAction();
        }
        if (ke.getCode() == KeyCode.F10) {
            filterButtonAction();
        }
        if (ke.getCode() == KeyCode.F11) {
            kcMarkersButtonAction();
        }
        if (ke.getCode() == KeyCode.F12) {
            dcRemoveButtonAction();
        }

    }

    private void updateWindows() {
        updateStage();
        updateProbabilities();

        toolBarGoto.setText((currentEpoch + 1) + "");
        statusBarLabel1.setText("/" + (dataPointsModel.getNumberOf30sEpochs()));

        showHypnogram();
        showScatterPlot();
        showEvaluationWindow();
        showElectrodeConfigurator();

        lineChart.requestFocus();
    }

    @FXML
    private void dcRemoveButtonAction() {
        if (viewModel.isDcRemoveActive()) {
            dcRemoveButton.setSelected(false);
            viewModel.setDcRemoveActive(false);
            loadEpoch(currentEpoch);
            updateEpoch();

        } else {
            dcRemoveButton.setSelected(true);
            viewModel.setDcRemoveActive(true);
            loadEpoch(currentEpoch);
            updateEpoch();
        }
        lineChart.requestFocus();
    }

    @FXML
    private void kcMarkersButtonAction() {
        if (viewModel.isKcMarkersActive()) {
            kcMarkersButton.setSelected(false);
            viewModel.setKcMarkersActive(false);
        } else {
            kcMarkersButton.setSelected(true);
            viewModel.setKcMarkersActive(true);
        }
        loadEpoch(currentEpoch);
        updateEpoch();
        lineChart.requestFocus();
    }

    @FXML
    private void filterButtonAction() {
        if (viewModel.isFiltersActive()) {
            filterButton.setSelected(false);
            viewModel.setFiltersActive(false);
            loadEpoch(currentEpoch);
            updateEpoch();

        } else {
            filterButton.setSelected(true);
            viewModel.setFiltersActive(true);
            loadEpoch(currentEpoch);
            updateEpoch();
        }
        lineChart.requestFocus();
    }

    private void tooltips() {
        help1.setTooltip(new Tooltip("75µV bars (L)"));
        kComplex.setTooltip(new Tooltip("K-complex measurement tool (K)"));
        classifyButton.setTooltip(new Tooltip("Perform automatic sleep stage classification (F7)"));
        visualizeButton.setTooltip(new Tooltip("Show cluster plot (F8)"));
        electrodeConfiguratorButton.setTooltip(new Tooltip("Select electrodes for display... (F9)"));
        filterButton.setTooltip(new Tooltip("Filters on/off (F10)"));
        kcMarkersButton.setTooltip(new Tooltip("Highlight K-complexes on/off (F11)"));
        dcRemoveButton.setTooltip(new Tooltip("DC remove on/off (F12)"));
        s1Button.setTooltip(new Tooltip("Sleep stage N1 (1)"));
        s2Button.setTooltip(new Tooltip("Sleep stage N2 (2)"));
        s3Button.setTooltip(new Tooltip("Sleep stage N3 (3)"));
        awakeButton.setTooltip(new Tooltip("Wake (W)"));
        remButton.setTooltip(new Tooltip("REM (R)"));
        arousalButton.setTooltip(new Tooltip("Movement arousal (M)"));
        artefactButton.setTooltip(new Tooltip("Artefact (A)"));
        stimulationButton.setTooltip(new Tooltip("Stimulation (S)"));
        clearButton.setTooltip(new Tooltip("Clear (C)"));
        hypnogramButton.setTooltip(new Tooltip("Show hypnogram (H)"));
    }

    private void createFilters() {
        FilterCoefficients coefficients = null;
        try {
            double fstop = 0.1;
            double fpass = 0.5;
            double fs = 100;
            coefficients = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.BUTTERWORTH,
                    FilterType.HIGHPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 20.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }

        setDisplayHighpassCoefficients(coefficients);

        FilterCoefficients coefficients2 = null;
        try {
            double fstop = 35;
            double fpass = 25;
            double fs = 100;
            coefficients2 = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.CHEBYSHEV2,
                    FilterType.LOWPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 40.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }

        setDisplayLowpasCoefficients(coefficients2);

        FilterCoefficients coefficients4 = null;
        try {
            double fstop = 0.01;
            double fpass = 0.3;
            double fs = 100;
            coefficients4 = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.BUTTERWORTH,
                    FilterType.HIGHPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 20.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }

        setHighpassCoefficients(coefficients4);

        FilterCoefficients coefficients3 = null;
        try {
            double fstop = 7.;
            double fpass = 4.;
            double fs = 100;
            coefficients3 = IIRDesigner.designDigitalFilter(
                    ApproximationFunctionType.CHEBYSHEV2,
                    FilterType.LOWPASS,
                    new double[]{fpass},
                    new double[]{fstop},
                    1.0, 40.0, fs);

        } catch (BadFilterParametersException ex) {
            ex.printStackTrace();
        }

        setLowpassCoefficients(coefficients3);

    }

    public void setDisplayHighpassCoefficients(FilterCoefficients displayHighpassCoefficients) {
        this.displayHighpassCoefficients = displayHighpassCoefficients;
    }

    public void setDisplayLowpasCoefficients(FilterCoefficients displayLowpasCoefficients) {
        this.displayLowpasCoefficients = displayLowpasCoefficients;
    }

    public FilterCoefficients getDisplayHighpassCoefficients() {
        return displayHighpassCoefficients;
    }

    public FilterCoefficients getDisplayLowpasCoefficients() {
        return displayLowpasCoefficients;
    }

    public void setHighpassCoefficients(FilterCoefficients highpassCoefficients) {
        this.highpassCoefficients = highpassCoefficients;
    }

    public FilterCoefficients getHighpassCoefficients() {
        return highpassCoefficients;
    }

    public void setLowpassCoefficients(FilterCoefficients lowpassCoefficients) {
        this.lowpassCoefficients = lowpassCoefficients;
    }

    public FilterCoefficients getLowpassCoefficients() {
        return lowpassCoefficients;
    }

    
}
