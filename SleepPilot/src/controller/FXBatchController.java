package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DataModel;
import model.FeatureModel;
import view.FXPopUp;
import view.FXStartController;

public class FXBatchController implements Initializable {

    private static final Logger log = Logger.getLogger(FXBatchController.class.getName());

    private Stage stage;
    private File folder;

    @FXML
    private ChoiceBox<String> choiceBoxChannels;
    @FXML
    private ChoiceBox<String> choiceBoxClassifiers;
    @FXML
    private ListView<String> fileList;

    public FXBatchController() {
        log.info("Starting FXBatchController");

        stage = new Stage();
        AnchorPane addGrid = new AnchorPane();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("StartSetting.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            addGrid = loader.load();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error during loading StartSetting.fxml file!", e);
        }

        Scene scene = new Scene(addGrid);

        stage.setResizable(false);
        stage.setScene(scene);
        stage.hide();
        stage.setTitle("Batch Processing...");

        log.info("Begin other stuff.");
        ObservableList<String> channelList = FXCollections.observableArrayList();

        log.info("Starting File Chooser");
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select folder with data for batch processing");

        // Show open file dialog
        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            showPopUp("Could not select folder.");
        } else {
            log.info("Selected file:" + file.getName());
        }

        if (file.isFile()) {
            file = file.getParentFile();
        }
        log.info("Selected folder:" + file.getName());

        folder = file.getAbsoluteFile();

        stage.show();

        ObservableList<String> batchFiles = FXCollections.observableArrayList();

        for (File files : folder.listFiles()) {
            if (files.getName().contains(".vhdr")
                    | files.getName().contains(".smr")
                    | files.getName().contains(".edf")) {

                DataController reader = null;
                try {
                    reader = new DataController(files);
                } catch (IOException ex) {
                    log.log(Level.SEVERE, files.getName(), ex);
                    showPopUp("Error reading " + files.getPath());
                }

                DataModel eeg = reader.getDataModel();
                channelList.clear();
                channelList.addAll(eeg.getChannelNames());
                batchFiles.add(new String(files.getName()));
            }
        }

        if (batchFiles.isEmpty()) {
            showPopUp("Could not find any files of type .smr, .edf or .vhdr");
        } else {
            fileList.setItems(batchFiles);
        }
        
        if (channelList.isEmpty()) {
            showPopUp("Could not find any channels");
        } else {
            choiceBoxChannels.setItems(channelList);
        }

        ObservableList<String> classifierList = FXCollections.observableArrayList();

        try {
            classifierList.addAll(ClassificationController.getClassifiers(new File("Classifiers").getAbsoluteFile()));
        } catch (Exception e) {
            log.log(Level.SEVERE,null,e);
            showPopUp("Could not find folder " + new File("Classifiers").getAbsolutePath());
        }
        
        choiceBoxClassifiers.setItems(classifierList);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1
    ) {
        // TODO Auto-generated method stub

    }

    @FXML
    protected void applyAction() {

        String classifier = choiceBoxClassifiers.getSelectionModel().getSelectedItem();
        int channel = choiceBoxChannels.getSelectionModel().getSelectedIndex();

        for (File files : folder.listFiles()) {
            if (files.getName().contains(".vhdr")
                    | files.getName().contains(".smr")
                    | files.getName().contains(".edf")) {

                DataController reader = null;
                try {
                    reader = new DataController(files);

                } catch (IOException ex) {
                    Logger.getLogger(FXStartController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                DataModel eeg = reader.getDataModel();
                //TODO: GUI select feature channel

                reader.readAll(channel);

                FeatureModel fm = new FeatureModel();
                FeatureController fc = new FeatureController(fm, eeg);
                fc.start();

                ClassificationController.classify(classifier, fm);
                File testf = new File(files.getAbsolutePath().replaceFirst("\\.[^.]*$", ".txt"));
                fc.saveFile(testf);

            }

        }

        stage.close();
    }

    public void showPopUp(String message) {
        FXPopUp popUp = new FXPopUp();
        popUp.showPopupMessage(message, stage);
        log.info(message);
    }

}
