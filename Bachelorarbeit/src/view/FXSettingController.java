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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class FXSettingController implements Initializable {

	private Stage stage;
	private FXStartModel startModel;
	private boolean autoModeFlag;
	
	@FXML private ToggleButton classificationFlag;
	@FXML private ChoiceBox<String> choiceBox;
	
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
			
//			for (int i = 0; i < channelNames.length; i++) { 
//				if (file.getName().contains(channelNames[i]) && file.getName().contains("model")) {
//					flag = true;
//					channelNumbersToRead.add(i);
//				} 	
//			}
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
		stage.close();
	}
	
	@FXML
	protected void classificationFlagAction() {
		if (classificationFlag.getText().equals("ON")) {
			classificationFlag.setText("OFF");
			autoModeFlag = false;
		} else {
			classificationFlag.setText("ON");
			autoModeFlag = true;
		}
	}
	
	
}
