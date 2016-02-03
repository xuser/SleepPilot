package view;

import controller.DataController;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.FXViewModel;
import model.DataModel;
import model.FeatureModel;
import controller.MainController;
import controller.ModelReaderWriterController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tools.Util;

/**
 * ANNOTATION: We have to read Header information here, because we want to show
 * PopUp Messages, if there are not valid channels. It is only possible to show
 * these messages from the FX Thread. This is the reason, why we cant print
 * messages later on during reading teh file in the DataController.
 *
 *
 * @author Nils Finke
 *
 */
public class FXStartController implements Initializable {

    //MainController mainController;
    private FXPopUp popUp = new FXPopUp();

    private static LinkedList<String> titel = new LinkedList();
    private static LinkedList<Integer> kind = new LinkedList();

    private DataModel dataModel;
    private FeatureModel featureModel;

    FXViewModel viewModel = new FXViewModel();

    // JavaFx components
    private Stage primaryStage;

    private AnchorPane mainGrid;

    private Scene scene;

    @FXML
    ProgressBar progressBar;

    @FXML
    Button newProject;
    @FXML
    Button openProject;
    @FXML
    Button createModel;
    @FXML
    Button setting;
    @FXML
    Button cancelButton;

    @FXML
    Polygon newProjectForm;
    @FXML
    Polygon openProjectForm;
    @FXML
    Polygon createModelForm;

    @FXML
    Label text1;
    @FXML
    Label text2;
    @FXML
    Label text3;

    public FXStartController(Stage stage, DataModel dataModel, FeatureModel featureModel) {

        primaryStage = stage;
        this.dataModel = dataModel;
        this.featureModel = featureModel;

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("Start.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            mainGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading Start.fxml file!");
            e.printStackTrace();
        }

        // Create stage with mainGrid
        scene = new Scene(mainGrid);
        primaryStage.setScene(scene);

        //Properties for stage
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setTitle("Automatic Sleep Staging - Start");

        // Start settings
        newProjectForm.setVisible(false);
        openProjectForm.setVisible(false);
        createModelForm.setVisible(false);

        progressBar.setVisible(false);
        cancelButton.setVisible(false);

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        newProject.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                openProjectForm.setVisible(false);
                createModelForm.setVisible(false);

                newProjectForm.setVisible(true);

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

                // Set extension filter
                FileChooser.ExtensionFilter extFilter0 = new FileChooser.ExtensionFilter("All Files",
                        "*.vhdr", "*.smr", "*.VHDR", "*.SMR", "*.edf", "*.EDF");
                FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter(
                        "BrainVision files (*.vhdr)", "*.vhdr", "*.VHDR");

                FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter(
                        "Spike2 files (*.smr)", "*.smr", "*.SMR");

                FileChooser.ExtensionFilter extFilter3 = new FileChooser.ExtensionFilter(
                        "EDF and EDF+ Files (*.edf)", "*.EDF");

                fileChooser.getExtensionFilters().addAll(extFilter0, extFilter1, extFilter2, extFilter3);

                // Show open file dialog
                final File file = fileChooser.showOpenDialog(null);

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
                    DataController dataReaderController;
                    try {
                        dataReaderController = new DataController(file);

                        Runtime.getRuntime().addShutdownHook(new Thread() {
                            public void run() {
                                if (dataModel.getReader() != null) {
                                    dataModel.getReader().close();
                                }
                            }
                        });

                        //Create application controller
                        System.out.println("AppController starting!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                FXViewModel viewModel = new FXViewModel();
                                FXApplicationController appController = new FXApplicationController(dataReaderController, featureModel, viewModel, false);
                                viewModel.setAppController(appController);
                                primaryStage.close();
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(FXStartController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        });

        openProject.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                newProjectForm.setVisible(false);
                createModelForm.setVisible(false);

                openProjectForm.setVisible(true);

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

                // Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                        "AutoScore files (*.as)", "*.as", "*.AS");
                fileChooser.getExtensionFilters().add(extFilter);

                // Show open file dialog
                final File file = fileChooser.showOpenDialog(null);

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

                //TODO
                if (file != null) {
                    ModelReaderWriterController modelReaderWriter = new ModelReaderWriterController(featureModel, file, false);
                    modelReaderWriter.start();
                }

            }

        });

        createModel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                newProjectForm.setVisible(false);
                openProjectForm.setVisible(false);
                createModelForm.setVisible(true);

                FXBatchController batch = new FXBatchController();

            }
        });

    }

    @FXML
    protected void settingOnAction() {
//        settings = new FXBatchController(startModel);
    }

    @FXML
    protected void cancelButtonOnAction() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");

        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            cmd.append(jvmArg + " ");
        }

        cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        cmd
                .append(MainController.class
                        .getName()).append(" ");

        try {
            Runtime.getRuntime().exec(cmd.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(
                0);

    }

    public void showPopUp(String message) {
        popUp.showPopupMessage(message, primaryStage);
        
    }

    public void startMainApp() {

    }

}
