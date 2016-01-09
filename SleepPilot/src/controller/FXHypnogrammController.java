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
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.DataModel;
import model.FeatureModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import view.FXApplicationController;
import view.FXStartController;

/**
 * Hypnogramm which shows grafically the evolution of the sleep stages
 *
 * @author Nils Finke
 */
public class FXHypnogrammController implements Initializable {

    private FXApplicationController appController;
    private FXViewModel viewModel;
    private FeatureModel featureModel;
    private DataModel dataModel;

    private XYChart.Series series;
    private XYChart.Series seriesArtefact;
    private XYChart.Series seriesArrousal;
    private XYChart.Series seriesStimulation;

    private Stage stage;

    @FXML
    LineChart<Number, Number> lineChart;
    @FXML
    Label toolBarLabel;

    /**
     * Basis constructor
     *
     * @param dataModel initial instance of raw model
     * @param featureModel initial instance of feature extraction model
     * @param viewModel initial instance of view model
     * @param appController
     *
     */
    public FXHypnogrammController(DataModel dataModel, FeatureModel featureModel, FXViewModel viewModel, FXApplicationController appController) {

        this.featureModel = featureModel;
        this.viewModel = viewModel;
        this.dataModel = dataModel;
        this.appController = appController;
        this.stage = new Stage();

        BorderPane addGrid = new BorderPane();

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

    /**
     * Starts with creation of this controller
     */
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
                appController.keyAction(ke);
            }
        });

        /**
         * Implements navigation in EEG dataset via clicks in hypnogram.
         */
        lineChart.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double numberOfEpochs = dataModel.getNumberOf30sEpochs();

                double widthLineChart = 514;			// Hard coded, because the view is a little bit smaller than the max size of the chart

                double mouseXPos = event.getX() - 13;	// Hard coded, same reason

                double relativePos = (mouseXPos / widthLineChart) * 100.0;

                int currentEpoch = (int) ((relativePos / 100.0) * numberOfEpochs);

                if ((currentEpoch < numberOfEpochs) && (currentEpoch >= 0)) {
                    appController.goToEpoch(currentEpoch);
                }

            }

        });

        showHypnogramm();
    }

    private void showHypnogramm() {
        lineChart.getData().clear();

        series = new XYChart.Series();
        seriesArtefact = new XYChart.Series();
        seriesArrousal = new XYChart.Series();
        seriesStimulation = new XYChart.Series();

        int xAxis = 1;
        int numberOfEpochs = dataModel.getNumberOf30sEpochs();

        for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
            double tmp = xAxis / (double) numberOfEpochs;
            tmp = tmp * 100;

            series.getData().add(new XYChart.Data<Double, Double>(tmp, 4.0));			//tmp is xaxis
            series.getData().add(new XYChart.Data<Double, Double>(tmp, 4.0));			//tmp is xaxis

            seriesArtefact.getData().add(new XYChart.Data<Double, Double>(tmp, 3.0));			//tmp is xaxis
//            seriesArtefact.getData().add(new XYChart.Data<Double, Double>(tmp, 3.0));			//tmp is xaxis

            seriesArrousal.getData().add(new XYChart.Data<Double, Double>(tmp, 2.0));			//tmp is xaxis					
//            seriesArrousal.getData().add(new XYChart.Data<Double, Double>(tmp, 2.0));			//tmp is xaxis

            seriesStimulation.getData().add(new XYChart.Data<Double, Double>(tmp, 1.0));			//tmp is xaxis
//            seriesStimulation.getData().add(new XYChart.Data<Double, Double>(tmp, 1.0));			//tmp is xaxis

            xAxis++;
        }

        lineChart.getData().add(series);
        lineChart.getData().add(seriesArtefact);
        lineChart.getData().add(seriesArrousal);
        lineChart.getData().add(seriesStimulation);

        @SuppressWarnings("rawtypes")
        XYChart.Series marker = new XYChart.Series();
        marker.setName("marker");

        xAxis = featureModel.getCurrentEpoch();
        double tmp = xAxis / numberOfEpochs;
        tmp = tmp * 100;

        marker.getData().add(new XYChart.Data<Double, Double>(tmp, 0.0));
        marker.getData().add(new XYChart.Data<Double, Double>(tmp, 9.0));

        lineChart.getData().add(marker);

        System.out.println("hypnogram.showHypnogram() called");

    }

    private void update(int epoch) {
        //update hypnogram

        if (epoch == 0) {
            lineChart.getData().get(0).getData().get(2 * epoch).setYValue(
                    getYValueForPlotting(featureModel.getLabel(epoch))
            );
        } else {
            lineChart.getData().get(0).getData().get(2 * epoch).setYValue(
                    getYValueForPlotting(featureModel.getLabel(epoch - 1))
            );
        }
        lineChart.getData().get(0).getData().get(2 * epoch + 1).setYValue(
                getYValueForPlotting(featureModel.getLabel(epoch))
        );

        double flag;
        //update artifact linechart
        flag = featureModel.getArtefact(epoch) != 1 ? 3 : 3.8;
        lineChart.getData().get(1).getData().get(epoch).setYValue(flag);

        //update arousal linechart
        flag = featureModel.getArousal(epoch) != 1 ? 2 : 2.8;
        lineChart.getData().get(2).getData().get(epoch).setYValue(flag);

        //update arousal linechart
        flag = featureModel.getStimulation(epoch) != 1 ? 1 : 1.8;
        lineChart.getData().get(3).getData().get(epoch).setYValue(flag);
    }

    public void changeCurrentEpochMarker() {
        double tmp = 100. * featureModel.getCurrentEpoch() / (double) featureModel.getNumberOfEpochs();
        lineChart.getData().get(4).getData().get(0).setXValue(tmp);
        lineChart.getData().get(4).getData().get(1).setXValue(tmp);
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
            case 0:
                label = 9.0;
                break;
            case 1:
                label = 7.0;
                break;
            case 2:
                label = 6.0;
                break;
            case 3:
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

    public void update() {
        update(featureModel.getCurrentEpoch());
    }

    public void updateAll() {
        for (int i = 0; i < featureModel.getNumberOfEpochs(); i++) {
            update(i);
        }
        update(featureModel.getCurrentEpoch());
    }

    /**
     * Bring hypnogramm window to front.
     */
    public void bringToFront() {
        stage.toFront();
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

    public boolean isShowing() {
        return stage.isShowing();
    }

}
