package view;

import com.google.common.primitives.Doubles;
import gnu.trove.list.array.TDoubleArrayList;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXHypnogrammController implements Initializable {

    private FXApplicationController appController;
    private FXViewModel viewModel;
    private FeatureExtractionModel featureExtractionModel;
    private RawDataModel dataPointsModel;

    private Stage stage;

    @FXML
    LineChart<Number, Number> lineChart;
    @FXML
    Label toolBarLabel;
    @FXML
    LineChart<Number, Number> lineChartOverview;
    @FXML
    NumberAxis xAxisOverview;
    @FXML
    NumberAxis yAxisOverview;
    @FXML
    Pane pane;
    @FXML
    NumberAxis yAxisHypnogram;
    @FXML
    NumberAxis xAxisHypnogram;

    XYChart.Series series;
    XYChart.Series seriesArtefact;
    XYChart.Series seriesArrousal;
    XYChart.Series seriesStimulation;
    XYChart.Series marker;

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
        appController.requestFocus();

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Event will be fired when closing the application
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                viewModel.setHypnogrammActive(false);
                System.out.println("Hypnogramm is closing.");
            }

        });

        //Key Listener
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.H) {
                    stage.hide();
                    viewModel.setHypnogrammActive(false);
                }
                
                appController
                        .keyAction(ke);
            }
        });

        lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();

                double widthLineChart = xAxisHypnogram.getWidth();			// Hard coded, because the view is a little bit smaller than the max size of the chart

                double mouseXPos = event.getX() - xAxisHypnogram.getLayoutX() - lineChart.getPadding().getLeft();	// Hard coded, same reason

                double relativePos = (mouseXPos / widthLineChart);

                int currentEpoch = (int) (relativePos * numberOfEpochs);

                appController.goToEpoch(currentEpoch);

            }

        });

        lineChartOverview.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();

                double widthLineChart = xAxisOverview.getWidth();			// Hard coded, because the view is a little bit smaller than the max size of the chart

                double mouseXPos = event.getX() - xAxisOverview.getLayoutX() - lineChartOverview.getPadding().getLeft();	// Hard coded, same reason

                double relativePos = (mouseXPos / widthLineChart);

                int currentEpoch = (int) (relativePos * numberOfEpochs);

                appController.goToEpoch(currentEpoch);

            }

        });

        //order of intiation is important!

        loadHypnogramm();

        updateHypnogramm();

        loadOverview();

        createEpochMarker(appController.getCurrentEpoch());

    }

    @SuppressWarnings("unchecked")
    private void loadHypnogramm() {
//        @SuppressWarnings("rawtypes")

        lineChart.getData().clear();

        series = new XYChart.Series();
        seriesArtefact = new XYChart.Series();
        seriesArrousal = new XYChart.Series();
        seriesStimulation = new XYChart.Series();

        double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();
        xAxisHypnogram.setUpperBound(numberOfEpochs);

        for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {

            for (int j = 0; j < 2; j++) {

                series.getData().add(new XYChart.Data((double) i, 10.));			//tmp is xaxis

            }

            for (int j = 0; j < 3; j++) {

                seriesArtefact.getData().add(new XYChart.Data<Double, Double>((double) i, 3.));			//tmp is xaxis

                seriesArrousal.getData().add(new XYChart.Data<Double, Double>((double) i, 2.));			//tmp is xaxis

                seriesStimulation.getData().add(new XYChart.Data<Double, Double>((double) i, 1.));			//tmp is xaxis

            }

        }

        lineChart.getData().add(series);
        lineChart.getData().add(seriesArtefact);
        lineChart.getData().add(seriesArrousal);
        lineChart.getData().add(seriesStimulation);
        
        setLabels();
    }

    @SuppressWarnings("unchecked")
    private void updateHypnogramm() {
        @SuppressWarnings("rawtypes")

        double numberOfEpochs = dataPointsModel.getNumberOf30sEpochs();

        for (int i = 0; i < (dataPointsModel.getNumberOf30sEpochs()); i++) {

            double label = getYValueForPlotting(featureExtractionModel.getFeatureClassLabel(i));
            double tmpLabel = 10.;
            if (i > 0) {
                tmpLabel = getYValueForPlotting(featureExtractionModel.getFeatureClassLabel(i - 1));
            } else {
                tmpLabel = getYValueForPlotting(featureExtractionModel.getFeatureClassLabel(0));
            }

            lineChart.getData().get(0).getData().get(2 * i).setYValue(tmpLabel);
            lineChart.getData().get(0).getData().get(2 * i + 1).setYValue(label);

            if (featureExtractionModel.getEpochProperty(i) != null) {
                Integer[] prob = featureExtractionModel.getEpochProperty(i);

                if (prob[0] == 1) {
                    lineChart.getData().get(1).getData().get(3 * i + 1).setYValue(3.8);
                } else {
                    lineChart.getData().get(1).getData().get(3 * i + 1).setYValue(3);
                }

                if (prob[1] == 1) {
                    lineChart.getData().get(2).getData().get(3 * i + 1).setYValue(2.8);
                } else {
                    lineChart.getData().get(2).getData().get(3 * i + 1).setYValue(2.);
                }

                if (prob[2] == 1) {
                    lineChart.getData().get(3).getData().get(3 * i + 1).setYValue(1.8);
                } else {
                    lineChart.getData().get(3).getData().get(3 * i + 1).setYValue(1.);
                }

            }

        }

        setLabels();
        appController.requestFocus();
    }

    /**
     * This method returns the adapted yAxis value for plotting the hypnogramm.
     * It is necessary to add an offset to the orginal class label, because we
     * want to plot additional epoch properties directly below the hypnogram in
     * the same chart.
     *
     * @return the yAxis value for plotting.
     */
    private double getYValueForPlotting(double classLabel) {

        double label = classLabel;
        switch ((int) label) {
            case 1:
                label = 9.0;
                break;
            case 2:
                label = 7.0;
                break;
            case 3:
                label = 6.0;
                break;
            case 4:
                label = 5.0;
                break;
            case 5:
                label = 8.0;
                break;
            default:
                label = 4.0;
                break;
        }

        return label;
    }

    @SuppressWarnings("unchecked")
    public void createEpochMarker(int currentEpoch) {

//        @SuppressWarnings("rawtypes")
        marker = new XYChart.Series();
        marker.setName("marker");

        double tmp = currentEpoch;

        marker.getData().add(new XYChart.Data<Double, Double>(tmp, 0.0));
        marker.getData().add(new XYChart.Data<Double, Double>(tmp, 9.0));

//		for (int i = 0; i < lineChart.getData().size(); i++) {
//		    for (Node node : lineChart.lookupAll(".series4")) {
//		        node.getStyleClass().add("default-color4");
//		    }
//		}
        lineChart.getData().add(marker);
        
        
        marker = new XYChart.Series();
        marker.setName("marker");

        marker.getData().add(new XYChart.Data<Double, Double>(tmp, yAxisOverview.getLowerBound()));
        marker.getData().add(new XYChart.Data<Double, Double>(tmp, yAxisOverview.getUpperBound()));

        lineChartOverview.getData().add(marker);

    }

    @SuppressWarnings("unchecked")
    public void changeCurrentEpochMarker(int currentEpoch) {
//		for (int i = 0; i < lineChart.getData().size(); i++) {
//		    for (Node node : lineChart.lookupAll(".series4")) {
//		        node.getStyleClass().add("default-color4");
//		    }
//		}
        lineChart.getData().get(4).getData().get(0).setXValue(currentEpoch);
        lineChart.getData().get(4).getData().get(1).setXValue(currentEpoch);
        
        lineChartOverview.getData().get(1).getData().get(0).setXValue(2*currentEpoch);
        lineChartOverview.getData().get(1).getData().get(1).setXValue(2*currentEpoch);
    }

    public void bringToFront() {
        stage.toFront();
    }

    public void reloadHypnogramm() {
        updateHypnogramm();
    }

    private void loadOverview() {
        
        List data = Doubles.asList(dataPointsModel.getFeatureChannelData());

        XYChart.Series series = new XYChart.Series();

        xAxisOverview.setUpperBound(2 * dataPointsModel.getNumberOf30sEpochs());
        xAxisOverview.setLowerBound(0);

        int index = 0;
        for (int j = 0; j < dataPointsModel.getNumberOf30sEpochs(); j++) {

            List sublist = data.subList((int) j * 3000, (int) (j + 1) * 3000);

            double[] yValue = new double[]{
                Doubles.max(Doubles.toArray(sublist)),
                Doubles.min(Doubles.toArray(sublist))
            };

            for (int i = 0; i < 2; i++) {
                XYChart.Data dataItem = new XYChart.Data(index, yValue[i]);
                series.getData().add(dataItem);
                index++;
            }

        }

        yAxisOverview.setUpperBound(1000);
        yAxisOverview.setLowerBound(-1000);

        lineChartOverview.getData().add(series);
        appController.requestFocus();
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

}
