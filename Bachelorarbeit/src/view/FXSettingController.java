package view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.FXStartModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class FXSettingController implements Initializable {

	private Stage stage;
	private FXStartModel startModel = null;
	private boolean autoModeFlag = true;
	
	@FXML private Button classificationFlag;
	@FXML private ChoiceBox<String> choiceBox;
	@FXML private Label label2;
	
	public FXSettingController(FXStartModel startModel) {
		
		this.startModel = startModel;
		
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
		stage.setTitle("Start Settings");
		
		ObservableList<String> choices = FXCollections.observableArrayList();
		
		File folder = new File(".").getAbsoluteFile();
		for( File file : folder.listFiles() ) {	
			if (file.getName().contains("model")) {
				choices.add(file.getName());
			}
		}
		
		choiceBox.setItems(choices);
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	protected void applyAction(){
		startModel.setAutoModeFlag(autoModeFlag);
		
		String item = choiceBox.getSelectionModel().getSelectedItem();
		startModel.setSelectedModel(item);
		
		stage.close();
	}
	
	@FXML
	protected void classificationFlagAction() {
		if (classificationFlag.getText().equals("ON")) {
			classificationFlag.setText("OFF");
			autoModeFlag = false;
			choiceBox.setDisable(true);
			label2.getStyleClass().removeAll("textLabel");
			label2.getStyleClass().add("textLabelDisabled");
			
		} else {
			classificationFlag.setText("ON");
			autoModeFlag = true;
			choiceBox.setDisable(false);
			label2.getStyleClass().removeAll("textLabelDisabled");
			label2.getStyleClass().add("textLabel");
		}
	}
	
	
}
