/*
 * Copyright (C) 2016 Arne Weigenand
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package view;

import controller.FXBatchController;
import controller.FXScatterPlot;
import controller.FXEvaluationWindowController;
import controller.FXHypnogrammController;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.DataModel;
import model.FeatureModel;
import controller.DataController;
import controller.FeatureController;
import controller.ModelReaderWriterController;
import controller.ClassificationController;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import org.apache.commons.math.stat.StatUtils;
import tools.Signal;
import tools.Util;
import tools.KCdetection;

public final class FXApplicationController implements Initializable {

    private File projectFile = null;
    private String classifier;
    private ArrayList<String> classifierList = new ArrayList();
    private final KCdetection kcDetector;

    private FXHypnogrammController hypnogram;
    private FXEvaluationWindowController evaluationWindow;
    private final FXScatterPlot scatterPlot;

    private final float[][] displayBuffer;

    private FXPopUp popUp = new FXPopUp();
    private final FXViewModel viewModel;

    private final DataController dataController;
    private final DataModel dataModel;
    private final FeatureModel featureModel;
    private final FeatureController featureController;
    private FXElectrodeConfiguratorController electrodeConfigurator;

    private final boolean recreateModelMode;

    private int currentEpoch = 0;
    private final String[] channelNames;

    private HashMap<String, Double[]> allChannels = new HashMap();
    TIntArrayList activeChannels = new TIntArrayList();

    private LinkedList<Line> lines = new LinkedList();

    private Stage primaryStage;
    private BorderPane mainGrid;
    final private Scene scene;

    private DoubleProperty space = new SimpleDoubleProperty(1.);
    private DoubleProperty scale = new SimpleDoubleProperty(1e-1);
    private DoubleProperty mouseX = new SimpleDoubleProperty(0.);
    private DoubleProperty mouseY = new SimpleDoubleProperty(0.);

    private boolean recompute = false;
    private boolean help1Flag = false;
    private boolean kComplexFlag = false;

    ObservableList<String> choices;

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
    @FXML
    private Label statusBarLabel1;
    @FXML
    private Label statusBarLabel2;
    @FXML
    private Label kComplexLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private ProgressBar progressBar;
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
    ChoiceBox<String> choiceBox;

    @FXML
    ChoiceBox<String> choiceBoxModel;

    public FXApplicationController(DataController dataController, FeatureModel featureModel, FXViewModel viewModel, boolean recreateModelMode) {

        primaryStage = new Stage();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Application.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            mainGrid = loader.load();
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error during loading Application.fxml file!", e);
        }

        this.dataController = dataController;
        this.dataModel = dataController.getDataModel();

        //initialize important variables
        channelNames = dataModel.getChannelNames();
        displayBuffer = dataModel.data.clone();
        // Set Choice Box for the channels        
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

        // Create stage with mainGrid
        scene = new Scene(mainGrid);
        primaryStage.setScene(scene);

        //Properties for stage
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.setTitle(dataModel.getFile().getName());

        ////////////
        this.viewModel = viewModel;
        this.featureModel = featureModel;
        this.featureController = new FeatureController(featureModel, dataModel);

        this.recreateModelMode = recreateModelMode;

        kcDetector = featureController.kcDetector;
        kcDetector.setHighpassCoefficients(featureController.getDisplayHighpassCoefficients());
        kcDetector.setLowpassCoefficients(featureController.getLowpassCoefficients());

        currentEpoch = featureModel.getCurrentEpoch();
        loadEpoch(currentEpoch);
        showEpoch();

        paintSpacing();

        //Configure lineChart
        lineChart.setSnapToPixel(false);

        choices = FXCollections.observableArrayList();
        updateChoiceBox();
        choiceBox.getSelectionModel().select(0);

        ObservableList<String> choicesModel = FXCollections.observableArrayList();

        File folder = new File("./Classifiers").getAbsoluteFile();
        for (File file : folder.listFiles()) {
            if (file.getName().contains("model")) {
                choicesModel.add(file.getName().replace("[model]", "").replace(".jo", ""));
                classifierList.add(file.getAbsolutePath());
            }
        }

        choiceBoxModel.setItems(choicesModel);
        choiceBoxModel.getSelectionModel().select(0);

        tooltips();

        scatterPlot = new FXScatterPlot(this, dataController, dataModel, featureModel, featureController, viewModel);
        hypnogram = new FXHypnogrammController(dataModel, featureModel, viewModel, this);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (recreateModelMode) {
                    hypnogram.updateAll();
                    hypnogram.hide();
                }
                updateWindows();
            }
        });

        if (((int) (dataModel.getSrate()) % 50) != 0) {
            showPopUp("Sampling rate not supported. Must be a multiple of 50 Hz and > 100 Hz.");
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        System.out.println("initialize called");

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

                        calculatePercentageKComplex();

                    }

                }

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

                    space.set(75.0 * zoom / yAxis.getUpperBound() * yAxis.getHeight());

                    line1.setVisible(true);
                    line2.setVisible(true);
                } else {
                    line1.setVisible(false);
                    line2.setVisible(false);
                }
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
                        
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error during handling enter key.", e);
                    }

                    if (valueTextField > dataModel.getNumberOf30sEpochs()) {
                        valueTextField = dataModel.getNumberOf30sEpochs();
                    } else if (valueTextField < 1) {
                        valueTextField = 1;
                    }

                    currentEpoch = valueTextField - 1;

                    goToEpoch(currentEpoch);

                    if (kComplexFlag) {
                        calculatePercentageKComplex();
                    }

                }

            }
        });

        choiceBox.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        System.out.println(newValue);
                        if (newValue != null) {

                            featureModel.setFeatureChannel(
                                    Arrays.asList(channelNames)
                                    .indexOf(newValue)
                            );

                            System.out.println("########" + Arrays.asList(channelNames)
                                    .indexOf(newValue));

                            System.out.println(featureModel.getFeatureChannel());

                            featureModel.setTsneComputed(false);
                            featureModel.setFeaturesComputed(false);
                            featureModel.setClassificationDone(false);
                            featureModel.setReadinDone(false);
                            classifyButton.setDisable(false);
                            recompute = true;

                            if (viewModel.isKcMarkersActive()) {
                                computeKCfeatures();
                            } else {
                                overlay4.getChildren().clear();
                            }
                        }
                    }
                }
        );

        choiceBoxModel.getSelectionModel()
                .selectedIndexProperty().addListener(
                        new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                classifier = classifierList.get(newValue.intValue());
                                System.out.println(classifier);

                                featureModel.setClassificationDone(false);
                                featureModel.setReadinDone(false);
                                classifyButton.setDisable(false);
                                recompute = true;
                            }

                        }
                );

        primaryStage.setOnCloseRequest(
                new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {
                        System.out.println("RandomAccessFile closed");
                        dataModel.getReader().close();
                        Platform.exit();
                    }
                }
        );

        kComplexOnAction();

        help1OnAction();
    }

    @SuppressWarnings("static-access")
    private void updateProbabilities() {
        double[] probabilities = featureModel.getPredictProbabilities(currentEpoch);
        if (probabilities != null) {
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

        kComplexLabel.setText("K-Complex: " + Math.round(percentageSum) + "%");
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

                hypnogram.updateAll();
                updateWindows();

                System.out.println("Finished importing Hypnogramm!");

            } catch (IOException e) {
                popUp.createPopup("Error during importing Hypnogramm!");
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error during importing Hypnogramm!", e);
            }
        }
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
            featureController.saveFile(file);
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
            featureModel.setProjectFile(file);
            ModelReaderWriterController modelReaderWriter = new ModelReaderWriterController(featureModel, file, true);
            modelReaderWriter.start();
        }

    }

    @FXML
    protected void awakeButtonOnAction() {
        featureModel.setLabel(currentEpoch, 0);
        updateWindows();
    }

    @FXML
    protected void s1ButtonOnAction() {
        featureModel.setLabel(currentEpoch, 1);
        updateWindows();
    }

    @FXML
    protected void s2ButtonOnAction() {
        featureModel.setLabel(currentEpoch, 2);
        updateWindows();
    }

    @FXML
    protected void s3ButtonOnAction() {
        featureModel.setLabel(currentEpoch, 3);
        updateWindows();
    }

    @FXML
    protected void remButtonOnAction() {
        featureModel.setLabel(currentEpoch, 5);
        updateWindows();
    }

    @FXML
    protected void artefactButtonOnAction() {
        featureModel.setArtefact(currentEpoch, featureModel.getArtefact(currentEpoch) == 1 ? 0 : 1);
        updateWindows();
    }

    @FXML
    protected void arousalButtonOnAction() {
        featureModel.setArousal(currentEpoch, featureModel.getArousal(currentEpoch) == 1 ? 0 : 1);
        updateWindows();
    }

    @FXML
    protected void stimulationButtonOnAction() {
        featureModel.setStimulation(currentEpoch, featureModel.getStimulation(currentEpoch) == 1 ? 0 : 1);
        updateWindows();
    }

    @FXML
    protected void clearButtonOnAction() {
        featureController.clearProperties(currentEpoch);
        updateWindows();
    }

    @FXML
    protected void classifyButtonAction() {
        progressBar.setVisible(true);

        if (!featureModel.isClassificationDone()) {
            System.out.println("Classifiy!");

            classify();
        }
        classifyButton.setDisable(true);

        updateWindows();
    }

    @FXML
    protected void visualizeButtonAction() {
        if (featureModel.isClassificationDone()) {
            if (viewModel.isScatterPlotActive()) {
                visualizeButton.setSelected(false);
                viewModel.setScatterPlotActive(false);
            } else {
                visualizeButton.setSelected(true);
                viewModel.setScatterPlotActive(true);
                if (!featureModel.isFeaturesComputed()) {
                    computeFeatures();
                }
                scatterPlot.paintScatterChart();
                scatterPlot.show();
            }
        }
        updateWindows();
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

    @FXML
    protected void showEvaluationWindowAction() {
        System.out.println("showEvaluationWindow called");
        if (viewModel.isEvaluationWindowActive()) {
            viewModel.setEvaluationWindowActive(false);
        } else {
            viewModel.setEvaluationWindowActive(true);
        }
        updateWindows();

    }

    @FXML
    protected void showScatterPlot() {
        if (viewModel.isScatterPlotActive()) {
            if (scatterPlot.isPainted) {
                scatterPlot.changeCurrentEpochMarker();
                scatterPlot.show();
            }
        } else {
            scatterPlot.hide();
        }
    }

    @FXML
    private void hypnogramAction() {
        System.out.println("hypnogramAction called");
        if (viewModel.isHypnogrammActive()) {
            hypnogramButton.setSelected(false);
            viewModel.setHypnogrammActive(false);
        } else {
            hypnogramButton.setSelected(true);
            viewModel.setHypnogrammActive(true);
        }
        updateWindows();
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

    @FXML
    protected void saveAction() {
        projectFile = featureModel.getProjectFile();
        if (projectFile == null) {
            saveAsAction();
        } else {
            Util.saveDir(projectFile,
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

            ModelReaderWriterController modelReaderWriter = new ModelReaderWriterController(featureModel, projectFile, true);
            modelReaderWriter.start();

        }

    }

    private void tooltips() {
        help1.setTooltip(new Tooltip("75µV bars (L)"));
        kComplex.setTooltip(new Tooltip("K-complex measurement tool (K)"));
        classifyButton.setTooltip(new Tooltip("Perform automatic sleep stage classification"));
        visualizeButton.setTooltip(new Tooltip("Show cluster plot (F6)"));
        electrodeConfiguratorButton.setTooltip(new Tooltip("Select electrodes for display... (F12)"));
        filterButton.setTooltip(new Tooltip("Filters on/off (F7)"));
        kcMarkersButton.setTooltip(new Tooltip("Highlight K-complexes on/off (F9)"));
        dcRemoveButton.setTooltip(new Tooltip("DC remove on/off (F8)"));
        s1Button.setTooltip(new Tooltip("Sleep stage N1 (1)"));
        s2Button.setTooltip(new Tooltip("Sleep stage N2 (2)"));
        s3Button.setTooltip(new Tooltip("Sleep stage N3 (3)"));
        awakeButton.setTooltip(new Tooltip("Wake (W)"));
        remButton.setTooltip(new Tooltip("REM (R)"));
        arousalButton.setTooltip(new Tooltip("Movement arousal (M)"));
        artefactButton.setTooltip(new Tooltip("Artefact (A)"));
        stimulationButton.setTooltip(new Tooltip("Stimulation (D)"));
        clearButton.setTooltip(new Tooltip("Clear (C)"));
        hypnogramButton.setTooltip(new Tooltip("Show hypnogram (H)"));
        choiceBox.setTooltip(new Tooltip("Select the channel to classify from"));
        choiceBoxModel.setTooltip(new Tooltip("Select classifier"));
    }

    private void computeFeatures() {
        if (!featureModel.isReadinDone()) {
            dataController.readAll(featureModel.getFeatureChannel());
            featureModel.setReadinDone(true);
        }

        if (!featureModel.isFeaturesComputed()) {
            featureController.start();
            featureModel.setFeaturesComputed(true);
        }
    }

    public final void updateChoiceBox() {
        choices.clear();
        returnActiveChannels();
        for (TIntIterator it = activeChannels.iterator(); it.hasNext();) {
            choices.add(channelNames[it.next()]);
        }
        choiceBox.setItems(choices);
        choiceBox.getSelectionModel().select(0);
    }

    private void showHypnogram() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                hypnogram.update();
                hypnogram.changeCurrentEpochMarker();
            }
        });

        if (viewModel.isHypnogrammActive()) {
            hypnogram.show();
        } else {
            hypnogram.hide();
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
            showEvaluationWindowAction();
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
            if (featureModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.R) {
            remButtonOnAction();
            if (featureModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT1) {
            s1ButtonOnAction();
            if (featureModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT2) {
            s2ButtonOnAction();
            if (featureModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.DIGIT3) {
            s3ButtonOnAction();
            if (featureModel.getLabel(currentEpoch) == -1) {
                goToEpoch(currentEpoch + 1);
            }
        }

        if (ke.getCode() == KeyCode.A) {
            artefactButtonOnAction();
        }

        if (ke.getCode() == KeyCode.M) {
            arousalButtonOnAction();
        }

        if (ke.getCode() == KeyCode.D) {
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
            goToEpoch(dataModel.getNumberOf30sEpochs() - 1);
        }

        if (ke.getCode() == KeyCode.HOME) {
            goToEpoch(0);
        }

        if (ke.getCode() == KeyCode.F6) {
            visualizeButtonAction();
        }
        if (ke.getCode() == KeyCode.F7) {
            filterButtonAction();
        }
        if (ke.getCode() == KeyCode.F8) {
            dcRemoveButtonAction();
        }
        if (ke.getCode() == KeyCode.F9) {
            kcMarkersButtonAction();
        }
        if (ke.getCode() == KeyCode.F12) {
            electrodeConfiguratorButtonAction();
        }
    }

    public void updateWindows() {
        updateStage();

        showHypnogram();
        showScatterPlot();
        showEvaluationWindow();
        showElectrodeConfigurator();

        lineChart.requestFocus();
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

    public void classify() {
        final long startTime = System.nanoTime();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, featureModel.getNumberOfEpochs());

                computeFeatures();
                ClassificationController.classify(classifier, featureModel);

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        progressBar.setVisible(false);
                        hypnogram.updateAll();
                        updateWindows();
                    }
                });

                System.out.println("Elapsed time: " + (System.nanoTime() - startTime) / 1e6);
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

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

    public void requestFocus() {
        lineChart.requestFocus();
    }

    private void showEvaluationWindow() {
        if (viewModel.isEvaluationWindowActive()) {
            if (evaluationWindow == null) {
                evaluationWindow = new FXEvaluationWindowController(dataModel, featureModel, viewModel);
            }
            evaluationWindow.reloadEvaluationWindow();
            evaluationWindow.show();
        } else {
            if (evaluationWindow != null) {
                evaluationWindow.hide();
            }
        }
        lineChart.requestFocus();
    }

    private void showElectrodeConfigurator() {
        if (viewModel.isElectrodeConfiguratorActive()) {
            if (electrodeConfigurator == null) {
                electrodeConfigurator = new FXElectrodeConfiguratorController(this.dataModel, this.allChannels, this.viewModel);
            }
            electrodeConfigurator.show();
        } else {
            if (electrodeConfigurator != null) {
                electrodeConfigurator.hide();
            }
        }

        lineChart.requestFocus();
    }

    // First Column: 0 -> W, 1 -> N1, 2 -> N2, 3 -> N3, 5 -> REM
    // Second Column: 0 -> Nothing, 1 -> Movement arrousal
    // Third Column:  0 -> Nothing, 1 -> Artefact
    // Fourth Column: 0 -> Nothing, 1 -> Stimulation
    private void openFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String row = null;
        int epoch = 0;

        while (((row = in.readLine()) != null) && (epoch < dataModel.getNumberOf30sEpochs() - 1)) {
            String[] rowArray = row.split(" ");

            int label = Integer.parseInt(rowArray[0]);
            featureModel.setLabel(epoch, label);

            if (Integer.parseInt(rowArray[1]) == 1) {
                featureModel.setArousal(epoch, 1);
            }

            if (Integer.parseInt(rowArray[2]) == 1) {
                featureModel.setArtefact(epoch, 1);
            }

            if (Integer.parseInt(rowArray[3]) == 1) {
                featureModel.setStimulation(epoch, 1);
            }

            epoch++;
        }

        in.close();

    }

    private void updateStage() {
        // (1: W, 2: N1, 3: N2, 4: N3, 5: REM)
        int label = featureModel.getLabel(currentEpoch);
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

        if (featureModel.getArtefact(currentEpoch) == 1) {
            artefactButton.setSelected(true);
        } else {
            artefactButton.setSelected(false);
        }

        if (featureModel.getArousal(currentEpoch) == 1) {
            arousalButton.setSelected(true);
        } else {
            arousalButton.setSelected(false);
        }

        if (featureModel.getStimulation(currentEpoch) == 1) {
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

        toolBarGoto.setText((currentEpoch + 1) + "");
        statusBarLabel1.setText("/" + (dataModel.getNumberOf30sEpochs()));

        updateProbabilities();

        if (featureModel.isClassificationDone()) {
            visualizeButton.setDisable(false);
        } else {
            visualizeButton.setDisable(true);
        }
    }

    public TIntArrayList returnActiveChannels() {

        activeChannels.clear();

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

        if (epoch > (dataModel.getNumberOf30sEpochs() - 1)) {
            epoch = dataModel.getNumberOf30sEpochs() - 1;
        }

        currentEpoch = epoch;

        overlay3.getChildren().clear();
        lines.clear();

        loadEpoch(currentEpoch);
        updateEpoch();

        updateWindows();
        //11 (N1 vs rest) 19(wach vs rem/rest)! 32/35 (wach vs rem) 50 (N3)

        if (featureModel.isFeaturesComputed()) {
            float[] f = featureModel.getFeatureVector(currentEpoch);
            infoLabel.setText(String.format("N3:%5$.2f PE/N1:%3$.2f sk/N2: %1$.2f hf:%2$.2f  hf2:%4$.2f",
                    f[1], f[3], f[10], f[9], f[49]
            ));
        }

        lineChart.requestFocus();
    }

    private void paintSpacing() {
        System.out.println("paintSpacing called");
        line1.layoutYProperty().bind(scale.multiply(space).multiply(1 / 2.).add(mouseY));
        line1.endXProperty().bind(overlay3.widthProperty());
        line2.layoutYProperty().bind(scale.multiply(space).multiply(-1 / 2.).add(mouseY));
        line2.endXProperty().bind(overlay3.widthProperty());
    }

    final public void loadEpoch(int numberOfEpoch) {
        returnActiveChannels();
        for (int i = 0; i < activeChannels.size(); i++) {
            dataController
                    .read(activeChannels.get(i), numberOfEpoch, dataModel.data[activeChannels.get(i)]);
        }

        if (dataModel.getSrate() != 100) {
            for (int i = 0; i < activeChannels.size(); i++) {
                displayBuffer[activeChannels.get(i)] = featureController.getResampler().resample(dataModel.data[activeChannels.get(i)]);
            }
        } else {
            for (int i = 0; i < activeChannels.size(); i++) {
                displayBuffer[activeChannels.get(i)] = dataModel.data[activeChannels.get(i)];
            }
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

        featureModel.setCurrentEpoch(currentEpoch);
    }

    final public void filterEpoch() {
        for (int i = 0; i < activeChannels.size(); i++) {
            Signal.filtfilt(displayBuffer[activeChannels.get(i)], featureController.getDisplayHighpassCoefficients());
        }
    }

    final public void removeDcOffset() {
        for (int i = 0; i < activeChannels.size(); i++) {
            Signal.removeDC(displayBuffer[activeChannels.get(i)]);
        }
    }

    final public void showEpoch() {

        lineChart.getData().clear();

        float[] epoch;
        double offsetSize = 0;
        if (!activeChannels.isEmpty()) {
            offsetSize = 1. / (activeChannels.size() + 1.);
        }

        for (int i = 0; i < activeChannels.size(); i++) {

            epoch = displayBuffer[activeChannels.get(i)];

            double zoom = getZoomFromChannel(activeChannels.get(i));

            // in local yAxis-coordinates
            double realOffset = (1 - (i + 1.) * offsetSize) * yAxis.getUpperBound();

            @SuppressWarnings("rawtypes")
            XYChart.Series series = new XYChart.Series();

            double epochSize = epoch.length;
            double xAxis = 0;

            for (int j = 0; j < epoch.length; j++) {
                double tmp = xAxis / epochSize;
                tmp = tmp * this.xAxis.getUpperBound();

                double value = epoch[j];
//                double value = Math.sin(2 * Math.PI * j / 100.) * 75 / 2.; //test signal

                value = value * zoom * scale.get();
                value = value + realOffset;

                XYChart.Data dataItem = new XYChart.Data(tmp, value);
                series.getData().add(dataItem);

                xAxis++;

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

        if (!activeChannels.isEmpty()) {
            offsetSize = 1. / (activeChannels.size() + 1.);
        }

        float[] epoch;
        double zoom;
        double realOffset;
        for (int i = 0; i < activeChannels.size(); i++) {
            epoch = displayBuffer[activeChannels.get(i)];
            zoom = getZoomFromChannel(activeChannels.get(i));
            // in local yAxis-coordinates
            realOffset = (1 - (i + 1.) * offsetSize) * yAxis.getUpperBound();

            int k = 0;
            for (int j = 0; j < epoch.length; j++) {
//                epoch[j] = Math.sin(2 * Math.PI * j / 100.) * 75 / 2.; //test signal
                lineChart.getData().get(i).getData().get(k).setYValue(
                        epoch[j] * zoom * scale.get() + realOffset
                );
                k++;
            }

        }
    }

    final public void computeKCfeatures() {

        overlay4.getChildren().clear();

        kcDetector.detect(displayBuffer[featureModel.getFeatureChannel()]);
        double percentageSum = kcDetector.getPercentageSum();
        Set< Range< Integer>> kcPlotRanges = kcDetector.getKcRanges();

        kComplexLabel.setVisible(
                true);
        kComplexLabel.setText(
                "K-Complex: " + Math.round(percentageSum) + "%");

        //draw yellow rectangles for every pair of coordinates in kcPlotRanges
        double start;
        double stop;

        for (Range<Integer> next : kcPlotRanges) {
            start = next.lowerEndpoint();
            stop = next.upperEndpoint();

            Rectangle r = new Rectangle();
            r.layoutXProperty()
                    .bind(this.xAxis.widthProperty()
                            .multiply((start + 1.) / (double) this.displayBuffer[0].length)
                            .add(this.xAxis.layoutXProperty())
                    );

            r.setLayoutY(0);
            r.widthProperty()
                    .bind(xAxis.widthProperty()
                            .multiply((stop - start) / (double) this.displayBuffer[0].length));

            r.heightProperty()
                    .bind(overlay4.heightProperty());
            r.fillProperty().setValue(Color.LIGHTBLUE);
            r.opacityProperty().set(0.5);

            overlay4.getChildren().add(r);

        }

        lineChart.requestFocus();
    }

    public void showPopUp(String message) {
        FXPopUp popUp = new FXPopUp();
        popUp.showPopupMessage(message, primaryStage);
        Logger log = Logger.getLogger(this.getClass().getName());
        log.setLevel(Level.ALL);
        log.info(message);
    }

}
