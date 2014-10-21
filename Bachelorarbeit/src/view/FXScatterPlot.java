package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.sun.javafx.css.converters.ShapeConverter;
import controller.DataReaderController;
import controller.FeatureExtractionController;

import controller.MainController;
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
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
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

    public FXScatterPlot(DataReaderController dataReaderController, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FeatureExtractionController featureExtractionController, FXViewModel viewModel) {
        this.featureExtractionModel = featureExtractionModel;
        this.featureExtractionController = featureExtractionController;
        this.viewModel = viewModel;
        this.dataPointsModel = dataPointsModel;
        this.dataReaderController = dataReaderController;

        appController = viewModel.getAppController();

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

                        for (int i = 0; i < featureExtractionModel.getNumberOfEpochs(); i++) {

                            System.out.println(featureExtractionModel.getFeatureClassLabel(i));
                            
                            switch (featureExtractionModel.getFeatureClassLabel(i)) {
                                case 1:
                                    seriesWake.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 2:
                                    seriesN1.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 3:
                                    seriesN2.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 4:
                                    seriesN3.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 5:
                                    seriesRem.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                                case 0:
                                    seriesUnclassified.getData().add(new XYChart.Data(output[i][0], output[i][1]));
                                    break;
                            }
                        }

                        scatterChart.getData().add(seriesRem);
                        scatterChart.getData().add(seriesN1);
                        scatterChart.getData().add(seriesN2);
                        scatterChart.getData().add(seriesN3);
                        scatterChart.getData().add(seriesWake);
                        scatterChart.getData().add(seriesUnclassified);

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
