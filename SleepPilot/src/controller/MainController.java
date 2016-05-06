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

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javafx.application.Application;
import javafx.application.Platform;
import view.FXApplicationController;
import view.FXStartController;
import javafx.stage.Stage;
import model.FXViewModel;
import model.DataModel;
import model.FeatureModel;

/**
 * Starts the application and creates necessary initial controllers.
 *
 * @author Arne Weigenand
 * @author Nils Finke
 */
public class MainController extends Application {

    /**
     * Filepath of .vhdr header file.
     */
    private static Stage primaryStage;
    private static DataModel dataModel;
    private static FeatureModel featureModel;
    private static FXStartController startController;

    /**
     * The main entry point for all JavaFX applications. The start method is
     * called after the init method has returned, and after the system is ready
     * for the application to begin running.
     */
    @Override
    public void start(final Stage stage) throws Exception {
        String dir = System.getProperty("user.dir");
        System.setProperty("java.library.path", dir);
        
        if (false) {
            String filename = "SleepPilot.log";
            String fullPath = dir + File.separator + filename;
            PrintStream outPS
                    = new PrintStream(
                            new BufferedOutputStream(
                                    new FileOutputStream(fullPath)));  // append is false
            System.setErr(outPS);    // redirect System.err
            System.setOut(outPS);    // and System.out
        }

        primaryStage = stage;

        //Create start controller
        dataModel = new DataModel();
        featureModel = new FeatureModel();
        startController = new FXStartController(primaryStage, dataModel, featureModel);

    }

    public static void recreateSystemState(File file) throws IllegalArgumentException {

        try {
            String filePath = file.getParent() + File.separator + featureModel.getDataFileLocation().getName();
            System.out.println(filePath);

            File relativeFile = new File(filePath);

            if (relativeFile.canRead()) {

                final DataController dataReaderController = new DataController(relativeFile);

                //Create application controller
                System.out.println("AppController starting!");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        FXViewModel viewModel = new FXViewModel();
                        FXApplicationController appController = new FXApplicationController(dataReaderController, featureModel, viewModel, true);
                        viewModel.setAppController(appController);
                        primaryStage.close();
                    }
                });

            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        startController.showPopUp("Cannot find EEG file at " + filePath + ". Move EEG data file to into directory of SleepPilot .as file.");

                    }
                });
            }

        } catch (IOException e) {
            System.err.println("Error occured during recreation of the old system state!");
            e.printStackTrace();
        }
    }

    public static void setFeatureModel(FeatureModel model) {
        featureModel = model;
    }

    /**
     * Starts the application with the needed parameters.
     *
     * @param args no starting arguments are needed.
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        launch(args);

    }

}
