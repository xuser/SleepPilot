package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.DataModel;
import model.FeatureModel;
import view.FXStartController;

public class FXSettingController implements Initializable {

    private Stage stage;
    private File folder;

    @FXML
    private ChoiceBox<String> choiceBoxChannels;
    @FXML
    private ChoiceBox<String> choiceBoxClassifiers;
    @FXML
    private ListView<String> fileList;

    public FXSettingController() {

        ObservableList<String> classifierList = FXCollections.observableArrayList();
        classifierList.addAll(ClassificationController.getClassifiers(new File("./Classifiers").getAbsoluteFile()));

        ObservableList<String> channelList = FXCollections.observableArrayList();

        DirectoryChooser fileChooser = new DirectoryChooser();

        fileChooser.setTitle("Select folder with data for batch processing");

        // Show open file dialog
        final File file = fileChooser.showDialog(null);

        folder = file.getAbsoluteFile();
        
        ObservableList<String> batchFiles = FXCollections.observableArrayList();
        
        for (File files : folder.listFiles()) {
            if (files.getName().contains(".vhdr")
                    | files.getName().contains(".smr")
                    | files.getName().contains(".edf")) {

                DataController reader = null;
                try {
                    reader = new DataController(files);
                } catch (IOException ex) {
                    Logger.getLogger(FXStartController.class.getName()).log(Level.SEVERE, null, ex);
                }

                DataModel eeg = reader.getDataModel();
                channelList.clear();
                channelList.addAll(eeg.getChannelNames());
                batchFiles.add(new String(files.getName()));
            }
        }
        

        stage = new Stage();
        AnchorPane addGrid = new AnchorPane();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("StartSetting.fxml"));
        loader.setController(this);

        // Try to load fxml file
        try {
            addGrid = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading StartSetting.fxml file!");
            //e.printStackTrace();
        }

        Scene scene = new Scene(addGrid);

        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Batch Processing...");

        choiceBoxClassifiers.setItems(classifierList);
        choiceBoxChannels.setItems(channelList);
        fileList.setItems(batchFiles);

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
                    Logger.getLogger(FXStartController.class.getName()).log(Level.SEVERE, null, ex);
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
}
