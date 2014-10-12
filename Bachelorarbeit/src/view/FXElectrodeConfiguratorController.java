/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.RawDataModel;
import view.FXElectrodeConfiguratorController.Channel;

/**
 * FXML Controller class
 *
 * @author Arne
 */
public class FXElectrodeConfiguratorController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TableColumn<Channel, Double> zoomCol;
    @FXML
    private TableColumn<Channel, Boolean> visibilityCol;
    @FXML
    private TableColumn<Channel, String> nameCol;
    @FXML
    private TableColumn<Channel, String> typeCol;

    @FXML
    private Button btnSelectAll;
    @FXML
    private Button btnDeselectAll;
    @FXML
    private Button btnInvertSelection;

    @FXML
    private TableView table;

    String[] channelNames;

    Stage stage;
    ObservableList<Channel> observableChannels;

    @FXML
    void invertSelectionAction() {
        for (int i = 0; i < observableChannels.size(); i++) {
            System.out.println(observableChannels.get(i).visibility.get());
            observableChannels.get(i).setVisibility(!observableChannels.get(i).visibilityProperty().get());
        }
    }

    @FXML
    void deselectAllAction() {
        for (int i = 0; i < observableChannels.size(); i++) {
            observableChannels.get(i).setVisibility(false);
        }
    }

    @FXML
    void selectAllAction() {

        for (int i = 0; i < observableChannels.size(); i++) {
            System.out.println(observableChannels.get(i).visibility.get());
            observableChannels.get(i).setVisibility(true);

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public FXElectrodeConfiguratorController(RawDataModel dataPointsModel, HashMap<String, Double[]> activeChannels) {

        stage = new Stage();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXElectrodeConfiguratorController.class.getResource("ElectrodeConfigurator.fxml"));
        loader.setController(this);

        AnchorPane configurator = new AnchorPane();

        // Try to load fxml file
        try {
            configurator = loader.load();
        } catch (IOException e) {
            System.err.println("Error during loading ElectrodeConfigurator.fxml file!");
            e.printStackTrace();
        }

        Scene scene = new Scene(configurator);

        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Select electrodes to display...");
        stage.toFront();

        // Set Choice Box for the channels
        channelNames = dataPointsModel.getChannelNames();
        observableChannels = FXCollections.observableArrayList();

        //Set properties for the channels
        for (int i = 0; i < channelNames.length; i++) {
            Channel channel = new Channel();
            channel.setName(channelNames[i]);
            channel.setType("EEG");
            channel.setVisibility(false);
            channel.setZoom(1.);
            observableChannels.add(channel);

//            //The first value represents wheater the channel is shown
//            //The second value represents the current zoom level
//            Double[] channelProp = new Double[2];
//            channelProp[0] = 1.0;
//            channelProp[1] = 5.0;
//            activeChannels.put(channelNames[i], channeProp);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(observableChannels);

        visibilityCol.setCellValueFactory(new PropertyValueFactory("visibility"));
        visibilityCol.setCellFactory(CheckBoxTableCell.forTableColumn(visibilityCol));

        visibilityCol.setEditable(true);
        table.setEditable(true);

        nameCol.setCellValueFactory(new PropertyValueFactory("name"));

        typeCol.setCellValueFactory(new PropertyValueFactory("type"));

        zoomCol.setCellValueFactory(new PropertyValueFactory("zoom"));
        zoomCol.setCellFactory(
                ChoiceBoxTableCell
                .forTableColumn(
                        new Double[]{0.01,0.05,0.1,0.5,1.,5.,10.,50.,100.}
                )
        );

        table.getColumns().setAll(visibilityCol, nameCol, zoomCol);

    }

    public static class Channel {

        StringProperty name = new SimpleStringProperty("");
        BooleanProperty visibility = new SimpleBooleanProperty(true);
        DoubleProperty zoom = new SimpleDoubleProperty(1.);
        StringProperty type = new SimpleStringProperty("");

        public Channel() {
        }

        public String getName() {
            return name.get();
        }

        public StringProperty typeProperty() {
            return type;
        }

        public DoubleProperty zoomProperty() {
            return zoom;
        }

        public BooleanProperty visibilityProperty() {
            return visibility;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setType(String type) {
            this.type.set(type);
        }

        public void setVisibility(boolean visibility) {
            this.visibility.set(visibility);
        }

        public void setZoom(double zoom) {
            this.zoom.set(zoom);
        }

    }

}
