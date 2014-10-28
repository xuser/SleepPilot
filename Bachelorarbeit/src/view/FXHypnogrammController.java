package view;

import com.google.common.collect.HashBiMap;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXHypnogrammController implements Initializable {

    private FXApplicationController appController;
    private FXViewModel viewModel;
    private FeatureExtractionModel featureExtractionModel;
    private RawDataModel dataPointsModel;

    private Stage stage;

    @FXML
    ScatterChart<Number, Number> scatterChart;
    @FXML
    Label toolBarLabel;
    @FXML
    Pane pane;
    @FXML
    NumberAxis yAxisHypnogram;
    @FXML
    NumberAxis xAxisHypnogram;

    XYChart.Series series;
    XYChart.Series seriesN1;
    XYChart.Series seriesN2;
    XYChart.Series seriesN3;
    XYChart.Series seriesWake;
    XYChart.Series seriesRem;
    XYChart.Series seriesUnclassified;
    XYChart.Series seriesArtefact;
    XYChart.Series seriesArrousal;
    XYChart.Series seriesStimulation;
    XYChart.Series marker;

    HashBiMap<Node, Integer> plotItemsMap;
    HashMap<Node, XYChart.Data> nodeToXYDataMap;

    BlendMode bm;
    final InnerShadow ds = new InnerShadow(20, Color.YELLOW);

    int lastEpoch = 0;

    Line line;

    public FXHypnogrammController(RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel) {

        this.featureExtractionModel = featureExtractionModel;
        this.viewModel = viewModel;
        this.dataPointsModel = dataPointsModel;

        appController = viewModel.getAppController();

        stage = new Stage();
        AnchorPane addGrid = new AnchorPane();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Hypnogramm.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            addGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading Hypnogramm.fxml file!");
            e.printStackTrace();
        }

        Scene scene = new Scene(addGrid);

        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Hypnogram");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Event will be fired when closing the application
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                viewModel.setHypnogrammActive(false);
                appController.updateWindows();
                System.out.println("Hypnogramm is closing.");
            }

        });

        //Key Listener
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                appController
                        .keyAction(ke);
            }
        });

        scatterChart.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();

                double widthLineChart = xAxisHypnogram.getWidth();

                double mouseXPos = event.getX() - xAxisHypnogram.getLayoutX() - scatterChart.getPadding().getLeft();

                double relativePos = (mouseXPos / widthLineChart);

                int currentEpoch = (int) (relativePos * numberOfEpochs);

                appController.goToEpoch(currentEpoch);

            }

        });

        //order of intiation is important!
        newHypnogram();
        updateHypnogram();
        setLabels();

//        loadHypnogramm();
//        loadOverview();
//        createEpochMarker(appController.getCurrentEpoch());
    }

    public void newHypnogram() {
        xAxisHypnogram.setUpperBound(featureExtractionModel.getNumberOfEpochs());
        yAxisHypnogram.setUpperBound(9.);

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
                    dataItem = new XYChart.Data(i, 9);
                    seriesWake.getData().add(dataItem);
                    plotItemsMap.put(dataItem.getNode(), i);
                    break;
                case 1:
                    dataItem = new XYChart.Data(i, 7);
                    seriesN1.getData().add(dataItem);
                    plotItemsMap.put(dataItem.getNode(), i);
                    break;
                case 2:
                    dataItem = new XYChart.Data(i, 6);
                    seriesN2.getData().add(dataItem);
                    plotItemsMap.put(dataItem.getNode(), i);
                    break;
                case 3:
                    dataItem = new XYChart.Data(i, 5);
                    seriesN3.getData().add(dataItem);
                    plotItemsMap.put(dataItem.getNode(), i);
                    break;
                case 5:
                    dataItem = new XYChart.Data(i, 8);
                    seriesRem.getData().add(dataItem);
                    plotItemsMap.put(dataItem.getNode(), i);
                    break;
                case -1:
                    dataItem = new XYChart.Data(i, 0);
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

                d.getNode().opacityProperty().set(1);
                bm = d.getNode().blendModeProperty().get();

                d.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent arg0) {
                        d.getNode().setEffect(ds);
                        d.getNode().setCursor(Cursor.HAND);
                        d.getNode().blendModeProperty().set(BlendMode.SRC_OVER);
                    }
                });

                d.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent arg0) {
                        d.getNode().setEffect(null);
                        d.getNode().setCursor(Cursor.DEFAULT);
                        d.getNode().blendModeProperty().set(bm);
                    }
                });
            }
        }

        scatterChart.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                appController.keyAction(event);
            }

        });

    }

    public void updateHypnogram() {
        int currentEpoch = appController.getCurrentEpoch();

        Node node = plotItemsMap.inverse().get(currentEpoch);
        XYChart.Data xyData = nodeToXYDataMap.get(node);

        seriesN1.getData().remove(xyData);
        seriesN2.getData().remove(xyData);
        seriesN3.getData().remove(xyData);
        seriesRem.getData().remove(xyData);
        seriesUnclassified.getData().remove(xyData);
        seriesWake.getData().remove(xyData);

        int label = featureExtractionModel.getLabel(currentEpoch);
        switch (label) {
            case 0:
                xyData.setYValue(9);
                seriesWake.getData().add(xyData);
                break;
            case 1:
                xyData.setYValue(7);
                seriesN1.getData().add(xyData);
                break;
            case 2:
                xyData.setYValue(6);
                seriesN2.getData().add(xyData);
                break;
            case 3:
                xyData.setYValue(5);
                seriesN3.getData().add(xyData);
                break;
            case 5:
                xyData.setYValue(8);
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
        node.blendModeProperty().set(BlendMode.SRC_OVER);

        node = plotItemsMap.inverse().get(lastEpoch);
        node.setEffect(null);
        node.blendModeProperty().set(bm);

        lastEpoch = currentEpoch;
    }

    private void setLabels() {
        pane.getChildren().clear();
        String[] labels = new String[]{"W", "REM", "N1", "N2", "N3", "A", "MA", "S"};
        int[] offsets = new int[]{0, 1, 2, 3, 4, 6, 7, 8};

        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.setTextFill(Color.GRAY);
            label.setStyle("-fx-font-family: sans-serif;");
            label.setLayoutX(1);

            label.layoutYProperty().bind(yAxisHypnogram.heightProperty()
                    .multiply(offsets[i] / 9.)
                    .add(yAxisHypnogram.layoutYProperty())
            );

            yAxisHypnogram.setLowerBound(0);
            pane.getChildren().add(label);

        }
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

    public void bringToFront() {
        stage.toFront();
    }
//    @SuppressWarnings("unchecked")
//    private void loadHypnogramm() {
////        @SuppressWarnings("rawtypes")
//
//        scatterChart.getData().clear();
//
//        series = new XYChart.Series();
//        seriesArtefact = new XYChart.Series();
//        seriesArrousal = new XYChart.Series();
//        seriesStimulation = new XYChart.Series();
//
//        double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
//        xAxisHypnogram.setUpperBound(numberOfEpochs);
//
//        for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
//
//            for (int j = 0; j < 2; j++) {
//
//                series.getData().add(new XYChart.Data((double) i, 10.));			//tmp is xaxis
//
//            }
//
//            for (int j = 0; j < 3; j++) {
//
//                seriesArtefact.getData().add(new XYChart.Data<Double, Double>((double) i, 3.));			//tmp is xaxis
//
//                seriesArrousal.getData().add(new XYChart.Data<Double, Double>((double) i, 2.));			//tmp is xaxis
//
//                seriesStimulation.getData().add(new XYChart.Data<Double, Double>((double) i, 1.));			//tmp is xaxis
//
//            }
//
//        }
//
//        scatterChart.getData().add(series);
//        scatterChart.getData().add(seriesArtefact);
//        scatterChart.getData().add(seriesArrousal);
//        scatterChart.getData().add(seriesStimulation);
//
//        setLabels();
//    }
//
//    @SuppressWarnings("unchecked")
//    private void updateHypnogramm() {
//        @SuppressWarnings("rawtypes")
//
//        double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
//
//        for (int i = 0; i < (dataPointsModel.getNumberOf30sEpochs()); i++) {
//
////            double label = getYValueForPlotting(featureExtractionModel.getLabel(i));
////            double tmpLabel = 10.;
////            if (i > 0) {
////                tmpLabel = getYValueForPlotting(featureExtractionModel.getLabel(i - 1));
////            } else {
////                tmpLabel = getYValueForPlotting(featureExtractionModel.getLabel(0));
////            }
////
////            scatterChart.getData().get(0).getData().get(2 * i).setYValue(tmpLabel);
////            scatterChart.getData().get(0).getData().get(2 * i + 1).setYValue(label);
////
////            if (prob[0] == 1) {
////                scatterChart.getData().get(1).getData().get(3 * i + 1).setYValue(3.8);
////            } else {
////                scatterChart.getData().get(1).getData().get(3 * i + 1).setYValue(3);
////            }
////
////            if (prob[1] == 1) {
////                scatterChart.getData().get(2).getData().get(3 * i + 1).setYValue(2.8);
////            } else {
////                scatterChart.getData().get(2).getData().get(3 * i + 1).setYValue(2.);
////            }
////
////            if (prob[2] == 1) {
////                scatterChart.getData().get(3).getData().get(3 * i + 1).setYValue(1.8);
////            } else {
////                scatterChart.getData().get(3).getData().get(3 * i + 1).setYValue(1.);
////            }
////
////        }
//            setLabels();
//            appController.requestFocus();
//        }
//    }

//    /**
//     * This method returns the adapted yAxis value for plotting the hypnogramm.
//     * It is necessary to add an offset to the orginal class label, because we
//     * want to plot additional epoch properties directly below the hypnogram in
//     * the same chart.
//     *
//     * @return the yAxis value for plotting.
//     */
//    private double getYValueForPlotting(double classLabel) {
//
//        double label = classLabel;
//        switch ((int) label) {
//            case 1:
//                label = 9.0;
//                break;
//            case 2:
//                label = 7.0;
//                break;
//            case 3:
//                label = 6.0;
//                break;
//            case 4:
//                label = 5.0;
//                break;
//            case 5:
//                label = 8.0;
//                break;
//            default:
//                label = 4.0;
//                break;
//        }
//
//        return label;
//    }
//
//    @SuppressWarnings("unchecked")
    public void createEpochMarker(int currentEpoch) {

        line = new Line();
//        @SuppressWarnings("rawtypes")
//        marker = new XYChart.Series();
//        marker.setName("marker");
//
        double tmp = currentEpoch;

        line.setStartX(tmp);
        line.setId("marker");
//        marker.getData().add(new XYChart.Data<Double, Double>(tmp, yAxisHypnogram.getLowerBound()));
//        marker.getData().add(new XYChart.Data<Double, Double>(tmp, yAxisHypnogram.getUpperBound()));
        pane.getChildren().add(line);
    }
//
//    @SuppressWarnings("unchecked")

    public void changeCurrentEpochMarker(int currentEpoch) {
//		for (int i = 0; i < scatterChart.getData().size(); i++) {
//		    for (Node node : scatterChart.lookupAll(".series4")) {
//		        node.getStyleClass().add("default-color4");
//		    }
//		}
//        scatterChart.getData().get(4).getData().get(0).setXValue(currentEpoch);
//        scatterChart.getData().get(4).getData().get(1).setXValue(currentEpoch);
        line.setStartX(lastEpoch);
    }

}
