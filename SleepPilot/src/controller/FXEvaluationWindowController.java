package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import model.FXViewModel;
import model.FeatureModel;
import model.DataModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import view.FXApplicationController;
import view.FXStartController;

public class FXEvaluationWindowController implements Initializable {

    private FXApplicationController appController;
    private FXViewModel viewModel;
    private FeatureModel featureModel;
    private DataModel dataModel;

    private int W;
    private int N1;
    private int N2;
    private int N3;
    private int REM;
    private int A;
    private int MA;
    private int S;
    private double TIB;
    private double SPT1;
    private double SPT2;
    private double TST2;
    private double SEI;
    private double AI;
    private int firstN1;
    private int firstN2;
    private int firstREM;
    private int wakeUp;

    private Stage stage;

    @FXML
    private TableColumn typeCol;
    @FXML
    private TableColumn percentCol;
    @FXML
    private TableColumn minutesCol;
    @FXML
    public TableView statsTable;

    ObservableList<Stats> list = FXCollections.observableArrayList();

    public FXEvaluationWindowController(DataModel dataModel, FeatureModel featureModel, FXViewModel viewModel) {

        this.featureModel = featureModel;
        this.viewModel = viewModel;
        this.dataModel = dataModel;
        this.appController = viewModel.getAppController();

        stage = new Stage();

        // Creating FXML Loader
        FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("EvaluationWindow2.fxml"));
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
        stage.setTitle("PSG statistics");
        stage.toFront();

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Event will be fired when closing the application
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                viewModel.setEvaluationWindowActive(false);
                appController.updateWindows();
                System.out.println("EvaluationWindow is closing.");
            }

        });

        //Key Listener
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                appController.keyAction(ke);
            }
        });

        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        updateStats(featureModel);
        updateLabels();

        statsTable.setEditable(false);

        typeCol.setCellValueFactory(new PropertyValueFactory("type"));
        percentCol.setCellValueFactory(new PropertyValueFactory("percent"));
        minutesCol.setCellValueFactory(new PropertyValueFactory("minutes"));
        statsTable.getColumns().setAll(typeCol, percentCol, minutesCol);

        typeCol.setSortable(false);
        percentCol.setSortable(false);
        minutesCol.setSortable(false);

        percentCol.setCellFactory(new Callback<TableColumn, TableCell>() {

            public TableCell call(TableColumn param) {
                TableCell cell = new TableCell() {
                    @Override
                    public void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        }
                    }
                };

                //SET EITHER CENTER OR RIGHT OR WHAT EVER AS YOUR WISH
                cell.setAlignment(Pos.CENTER_RIGHT);

                return cell;
            }

        });

        minutesCol.setCellFactory(new Callback<TableColumn, TableCell>() {

            public TableCell call(TableColumn param) {
                TableCell cell = new TableCell() {
                    @Override
                    public void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        }
                    }
                };

                //SET EITHER CENTER OR RIGHT OR WHAT EVER AS YOUR WISH
                cell.setAlignment(Pos.CENTER_RIGHT);

                return cell;
            }

        });

    }

    private void updateLabels() {
        list.clear();
        Stats stats = new Stats();
        stats.setType("W");
        stats.setPercent("" + rnd(W / (double) SPT1 * 100));
        stats.setMinutes("" + W * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("N1");
        stats.setPercent("" + rnd(N1 / (double) SPT1) * 100);
        stats.setMinutes("" + N1 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("N2");
        stats.setPercent("" + rnd(N2 / (double) SPT1 * 100));
        stats.setMinutes("" + N2 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("N3");
        stats.setPercent("" + rnd(N3 / (double) SPT1 * 100));
        stats.setMinutes("" + N3 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("REM");
        stats.setPercent("" + rnd(REM / (double) SPT1 * 100));
        stats.setMinutes("" + REM * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("TST");
        stats.setPercent("");
        stats.setMinutes("" + TST2 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("TIB");
        stats.setPercent("");
        stats.setMinutes("" + TIB * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("SPT1");
        stats.setPercent("");
        stats.setMinutes("" + SPT1 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("SPT2");
        stats.setPercent("");
        stats.setMinutes("" + SPT2 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("SOL1");
        stats.setPercent("");
        stats.setMinutes("" + firstN1 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("SOL2");
        stats.setPercent("");
        stats.setMinutes("" + firstN2 * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("REML");
        stats.setPercent("");
        stats.setMinutes("" + firstREM * 0.5);
        list.add(stats);

        stats = new Stats();
        stats.setType("AI [events/h]");
        stats.setPercent("");
        stats.setMinutes("" + rnd(AI));
        list.add(stats);

        stats = new Stats();
        stats.setType("SEI");
        stats.setPercent("" + rnd(SEI));
        stats.setMinutes("");
        list.add(stats);

        statsTable.setItems(list);
    }

    private void updateStats(FeatureModel fm) {
        W = 0;
        N1 = 0;
        N2 = 0;
        N3 = 0;
        REM = 0;
        MA = 0;
        A = 0;
        S = 0;

        int[] labels = fm.getLabels();
        int[] arousals = fm.getArousals();
        int[] artefacts = fm.getArtefacts();
        int[] stimulation = fm.getStimulation();

        firstN1 = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == 1) {
                firstN1 = i;
                break;
            }
        }

        firstN2 = 0;
        for (int i = 2; i < labels.length; i++) {
            if (((labels[i] == 2 | labels[i] == 3)
                    & (labels[i - 1] == 2 | labels[i - 1] == 3)
                    & (labels[i - 2] == 2 | labels[i - 2] == 3))) {
                firstN2 = i;
                break;
            }
        }

        firstN1 = firstN1 > firstN2 ? firstN2 : firstN1;

        firstREM = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == 5) {
                firstREM = i;
                break;
            }
        }

        wakeUp = labels.length - 1;
        for (int i = labels.length - 1; i > -1; i--) {
            if (labels[i] != 0) {
                wakeUp = i;
                break;
            }
        }

        for (int i = firstN1; i < wakeUp; i++) {
            if (labels[i] == 0) {
                W++;
            }
            if (labels[i] == 1) {
                N1++;
            }
            if (labels[i] == 2) {
                N2++;
            }
            if (labels[i] == 3) {
                N3++;
            }
            if (labels[i] == 5) {
                REM++;
            }
            if (arousals[i] == 1) {
                MA++;
            }
            if (artefacts[i] == 1) {
                A++;
            }
            if (stimulation[i] == 1) {
                S++;
            }
        }

        TIB = fm.getNumberOfEpochs();
        SPT1 = wakeUp + 1 - firstN1;
        SPT2 = wakeUp + 1 - firstN2;
        TST2 = SPT2 - W;
        SEI = TST2 / (double) TIB * 100.; //sleep efficiency index
        AI = MA / (SPT1 * 0.5 / 60.);

    }

    public void bringToFront() {
        stage.toFront();
    }

    public void reloadEvaluationWindow() {

        updateStats(featureModel);
        updateLabels();
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

    public static class Stats {

        StringProperty type = new SimpleStringProperty("");
        StringProperty percent = new SimpleStringProperty();
        StringProperty minutes = new SimpleStringProperty();

        public Stats() {
        }

        public StringProperty minutesProperty() {
            return minutes;
        }

        public StringProperty percentProperty() {
            return percent;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public void setMinutes(String minutes) {
            this.minutes.set(minutes);
        }

        public void setPercent(String percent) {
            this.percent.set(percent);
        }

        public void setType(String type) {
            this.type.set(type);
        }

    }

    private static double rnd(double x) {
        return Math.round(x * 10) / 10;
    }
}
