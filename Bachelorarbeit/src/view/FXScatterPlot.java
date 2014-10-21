package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controller.DataReaderController;
import controller.FeatureExtractionController;

import gnu.trove.map.hash.TIntIntHashMap;
import java.util.Arrays;
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
import javafx.scene.Group;
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
    private TIntIntHashMap plotItemsMap;

    public Stage stage;

    @FXML
    private ScatterChart<Number, Number> scatterChart;
    @FXML
    private ProgressIndicator progressIndicator;

    public FXScatterPlot(DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FeatureExtractionController featureExtractionController, FXViewModel viewModel) {
        this.featureExtractionModel = featureExtractionModel;
        this.featureExtractionController = featureExtractionController;
        this.viewModel = viewModel;
        this.dataPointsModel = dataPointsModel;
        this.dataReaderController = dataReaderController;
        this.appController = viewModel.getAppController();

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
        stage.show();
        stage.setTitle("Scatter Plot");

        progressIndicator.setVisible(false);
        paintScatterChart();

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Event will be fired when closing the application
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                viewModel.setScatterPlotActive(false);
                System.out.println("Scatter Plot is closing.");
            }

        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void paintScatterChart() {

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        progressIndicator.setVisible(true);
                    }
                });

                if (!featureExtractionModel.isTsneComputed()) {
                    TSNE tsne = new TSNE(Util.floatToDouble(featureExtractionModel.getFeatures()));
                    featureExtractionModel.setTsneFeatures(tsne.tsne());
                    featureExtractionModel.setTsneComputed(true);
                }

                final double[][] output = featureExtractionModel.getTsneFeatures();

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {

                        XYChart.Series seriesRem = new XYChart.Series();
                        seriesRem.setName("REM");

                        XYChart.Series seriesN1 = new XYChart.Series();
                        seriesN1.setName("N1");

                        XYChart.Series seriesN2 = new XYChart.Series();
                        seriesN2.setName("N2");

                        XYChart.Series seriesN3 = new XYChart.Series();
                        seriesN3.setName("N3");

                        XYChart.Series seriesWake = new XYChart.Series();
                        seriesWake.setName("Awake");

                        XYChart.Series seriesUnclassified = new XYChart.Series();
                        seriesUnclassified.setName("Unclassified");

                        plotItemsMap = new TIntIntHashMap();

                        for (int i = 0; i < featureExtractionModel.getNumberOfEpochs(); i++) {

                            XYChart.Data dataItem;
                            switch (featureExtractionModel.getFeatureClassLabel(i)) {
                                case 1:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesWake.getData().add(dataItem);
                                    break;
                                case 2:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesN1.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 3:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesN2.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 4:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesN3.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 5:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesRem.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 0:
                                    dataItem = new XYChart.Data(output[i][0], output[i][1]);
                                    plotItemsMap.put(Arrays.hashCode(new double[]{output[i][0], output[i][1]}), i);
                                    seriesUnclassified.getData().add(dataItem);
                                    break;
                            }
                        }

                        scatterChart.getData().add(seriesRem);
                        scatterChart.getData().add(seriesN1);
                        scatterChart.getData().add(seriesN2);
                        scatterChart.getData().add(seriesN3);
                        scatterChart.getData().add(seriesWake);
                        scatterChart.getData().add(seriesUnclassified);
                                           
                        

                        final InnerShadow ds = new InnerShadow(20, Color.YELLOW);

                        for (XYChart.Series<Number, Number> series : scatterChart.getData()) {
                            for (XYChart.Data<Number, Number> d : series.getData()) {
                                d.getNode().opacityProperty().set(0.5);
                                BlendMode bm = d.getNode().blendModeProperty().get();
                                
                                final double[] hash = new double[]{d.getXValue().doubleValue(),
                                    d.getYValue().doubleValue()};

                                d.getNode().setOnMouseClicked(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent event) {
                                        appController.goToEpoch(plotItemsMap
                                                .get(Arrays.hashCode(hash))
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
                                        d.getNode().setOpacity(0.5);
                                        d.getNode().blendModeProperty().set(bm);
                                    }
                                });
                            }
                        }
                        scatterChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

                            @Override
                            public void handle(KeyEvent event) {
                                appController.keyAction(event);
                            }
                           
                        });
                        
                        scatterChart.requestFocus();
                    }

                });

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        progressIndicator.setVisible(false);
                    }
                });

                return null;
            }

        };

        new Thread(task).start();

    }

    public void bringToFront() {
        stage.toFront();
    }

}
