package view;

import com.google.common.collect.HashBiMap;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controller.DataReaderController;
import controller.FeatureExtractionController;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import tsne.TSNE;
import model.FXViewModel;
import model.FeatureExtractionModel;
import model.RawDataModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tools.Util;

public class FXScatterPlot implements Initializable {

    private FXApplicationController appController;
    private FXViewModel viewModel;
    private FeatureExtractionModel featureExtractionModel;
    private FeatureExtractionController featureExtractionController;
    private RawDataModel dataPointsModel;
    private DataReaderController dataReaderController;

    public Stage stage;

    @FXML
    private ScatterChart<Number, Number> scatterChart;
    @FXML
    private ProgressIndicator progressIndicator;

    XYChart.Series seriesRem;

    XYChart.Series seriesN1;

    XYChart.Series seriesN2;

    XYChart.Series seriesN3;

    XYChart.Series seriesWake;

    XYChart.Series seriesUnclassified;

    final InnerShadow ds = new InnerShadow(20, Color.YELLOW);

    int lastEpoch = 0;
    public boolean isPainted = false;

    BlendMode bm;

    private HashBiMap<Node, Integer> plotItemsMap;
    private HashMap<Node, XYChart.Data> nodeToXYDataMap;

    double opacity = 0.7;

    public FXScatterPlot(FXApplicationController appController, DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FeatureExtractionController featureExtractionController, FXViewModel viewModel) {
        this.featureExtractionModel = featureExtractionModel;
        this.featureExtractionController = featureExtractionController;
        this.viewModel = viewModel;
        this.dataPointsModel = dataPointsModel;
        this.dataReaderController = dataReaderController;
        this.appController = appController;

        stage = new Stage();
        BorderPane addGrid = new BorderPane();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("ScatterPlot.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            addGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading ScatterPlot.fxml file!");
            //e.printStackTrace();
        }

        Scene scene = new Scene(addGrid);

        stage.setResizable(true);
        stage.setScene(scene);
        stage.hide();
        stage.setTitle("Scatter Plot");

        progressIndicator.setVisible(false);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Event will be fired when closing the application
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                viewModel.setScatterPlotActive(false);
                appController.updateWindows();
                System.out.println("Scatter Plot is closing.");
            }

        });

        scatterChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                appController.keyAction(event);
            }

        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void paintScatterChart() {
        isPainted = false;

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        progressIndicator.setVisible(true);
                    }
                });

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        scatterChart.getData().clear();

                        computeFeatures();

                        final double[][] output = featureExtractionModel.getTsneFeatures();

                        seriesRem = new XYChart.Series();
                        seriesRem.setName("REM");

                        seriesN1 = new XYChart.Series();
                        seriesN1.setName("N1");

                        seriesN2 = new XYChart.Series();
                        seriesN2.setName("N2");

                        seriesN3 = new XYChart.Series();
                        seriesN3.setName("N3");

                        seriesWake = new XYChart.Series();
                        seriesWake.setName("Awake");

                        seriesUnclassified = new XYChart.Series();
                        seriesUnclassified.setName("Unclassified");

                        scatterChart.getData().add(seriesRem);
                        scatterChart.getData().add(seriesN1);
                        scatterChart.getData().add(seriesN2);
                        scatterChart.getData().add(seriesN3);
                        scatterChart.getData().add(seriesWake);
                        scatterChart.getData().add(seriesUnclassified);

                        plotItemsMap = HashBiMap.create();

                        for (int i = 0; i < featureExtractionModel.getNumberOfEpochs(); i++) {

                            XYChart.Data dataItem;
                            switch (featureExtractionModel.getLabel(i)) {
                                case 0:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesWake.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                                case 1:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesN1.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                                case 2:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesN2.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                                case 3:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesN3.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                                case 5:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesRem.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                                case -1:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    seriesUnclassified.getData().add(dataItem);
                                    plotItemsMap.put(dataItem.getNode(), i);
                                    break;
                            }

                        }

                        //this is a hack, because JavaFX won't update the legend otherwise
                        scatterChart.getData().add(new XYChart.Series());
                        scatterChart.getData().remove(6);

                        nodeToXYDataMap = new HashMap();

                        for (XYChart.Series<Number, Number> series : scatterChart.getData()) {
                            for (XYChart.Data<Number, Number> d : series.getData()) {
                                nodeToXYDataMap.put(d.getNode(), d);

                                d.getNode().setOpacity(opacity);
                                bm = d.getNode().blendModeProperty().get();

                                d.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent event) {
                                        appController.goToEpoch(plotItemsMap
                                                .get(d.getNode())
                                        );
                                        d.getNode().toBack();
                                    }
                                });

                                d.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent arg0) {
                                        d.getNode().setEffect(ds);
                                        d.getNode().setCursor(Cursor.HAND);
                                        d.getNode().setOpacity(1.);
                                        d.getNode().blendModeProperty().set(BlendMode.SRC_OVER);
                                    }
                                });

                                d.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent arg0) {
                                        d.getNode().setEffect(null);
                                        d.getNode().setCursor(Cursor.DEFAULT);
                                        d.getNode().setOpacity(opacity);
                                        d.getNode().blendModeProperty().set(bm);
                                    }
                                });
                            }
                        }

                        scatterChart.requestFocus();
                        isPainted = true;
                    }

                }
                );

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        progressIndicator.setVisible(false);
                    }
                });

                return null;
            }

        };
        
        Thread thread = new Thread(task);
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(FXScatterPlot.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void bringToFront() {
        stage.toFront();
    }

    public void update() {
        int currentEpoch = appController.getCurrentEpoch();

        Node node = plotItemsMap.inverse().get(currentEpoch);
        XYChart.Data xyData = nodeToXYDataMap.get(node);

        seriesN2.getData().remove(xyData);
        seriesN3.getData().remove(xyData);
        seriesRem.getData().remove(xyData);
        seriesN1.getData().remove(xyData);
        seriesWake.getData().remove(xyData);

        int label = featureExtractionModel.getLabel(currentEpoch);
        switch (label) {
            case 0:
                seriesWake.getData().add(xyData);
                break;
            case 1:
                seriesN1.getData().add(xyData);
                break;
            case 2:
                seriesN2.getData().add(xyData);
                break;
            case 3:
                seriesN3.getData().add(xyData);
                break;
            case 5:
                seriesRem.getData().add(xyData);
                break;
            case -1:
                seriesUnclassified.getData().add(xyData);
                break;
        }

    }

    public void changeCurrentEpochMarker() {
        int currentEpoch = appController.getCurrentEpoch();

        Node node = plotItemsMap.inverse().get(currentEpoch);
        node.toFront();
        node.setEffect(ds);
        node.setOpacity(1.);
        node.blendModeProperty().set(BlendMode.SRC_OVER);

        node = plotItemsMap.inverse().get(lastEpoch);
        node.setEffect(null);
        node.setOpacity(opacity);
        node.blendModeProperty().set(bm);

        lastEpoch = currentEpoch;
    }

    public void hide() {
        stage.hide();
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void computeFeatures() {
        if (!featureExtractionModel.isTsneComputed()) {
            TSNE tsne = new TSNE(Util.floatToDouble(featureExtractionModel.getFeatures()));
            featureExtractionModel.setTsneFeatures(tsne.tsne());
            featureExtractionModel.setTsneComputed(true);
        }

    }
}
