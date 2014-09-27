package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.ResourceBundle;

import model.FXStartModel;
import model.FXViewModel;
import model.RawDataModel;
import model.FeatureExtractionModel;
import controller.MainController;
import controller.ModelReaderWriterController;
import javafx.concurrent.Task;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * ANNOTATION: We have to read Header information here, because we want to show PopUp Messages, if there are not valid channels.
 * It is only possible to show these messages from the FX Thread. This is the reason, why we cant print messages later on during
 * reading teh file in the DataReaderController.
 * 
 * 
 * @author Nils Finke
 *
 */
public class FXStartController implements Initializable {
	
	//MainController mainController;
	private FXPopUp popUp = new FXPopUp();

	private boolean trainMode = false;
	private LinkedList<Integer> channelNumbersToRead = new LinkedList<Integer>();
	private String[] channelNames;
	
	//Necessary for reading the smr information
	private int channels;
	private int numberOfChannels;
	private static LinkedList<String> titel = new LinkedList<String>();
	private static LinkedList<Integer> kind = new LinkedList<Integer>();

	private RawDataModel dataPointsModel;
	private FeatureExtractionModel featureExtractionModel;
	
	FXViewModel viewModel = new FXViewModel();
	private FXStartModel startModel = new FXStartModel();

	
	private FXSettingController settings;
	
	// JavaFx components
	private Stage primaryStage;
	
	private AnchorPane mainGrid;
	
	private Scene scene;
	
	private RandomAccessFile smrFile;
	
	@FXML ProgressBar progressBar;
	
	@FXML Button newProject;		
	@FXML Button openProject;
	@FXML Button createModel;
	@FXML Button setting;
	@FXML Button cancelButton;
	
	@FXML Polygon newProjectForm;
	@FXML Polygon openProjectForm;
	@FXML Polygon createModelForm;
	
	@FXML Label text1;
	@FXML Label text2;
	@FXML Label text3;
		
	
	public FXStartController(Stage stage, RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel) {
		
		primaryStage = stage;
		this.dataPointsModel = dataPointsModel;
		this.featureExtractionModel = featureExtractionModel;
		
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
				
				trainMode = false;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter0 = new FileChooser.ExtensionFilter("All Files", "*.vhdr", "*.smr", "*.VHDR", "*.SMR");
				FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter(
							"BrainVision files (*.vhdr)", "*.vhdr", "*.VHDR");
				
				FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter(
						"Spike2 files (*.smr)", "*.smr", "*.SMR");
			
				fileChooser.getExtensionFilters().addAll(extFilter0, extFilter1, extFilter2);
				
//				fileChooser.getExtensionFilters().add(extFilter1);
//				fileChooser.getExtensionFilters().add(extFilter2);
				
				
				// Show open file dialog
				final File file = fileChooser.showOpenDialog(null);
				
				if (file != null) {
					if ((startModel.getSelectedModel() != null) || (startModel.isAutoModeFlag() == false)) {
						featureExtractionModel.setSelectedModel(startModel.getSelectedModel());
						startAction(file);
					} else {
						popUp.showPopupMessage("Please first go to settings and select a model!", primaryStage);
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
				
				trainMode = false;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"AutoScore files (*.as)", "*.as", "*.AS");
				fileChooser.getExtensionFilters().add(extFilter);
				
				
				// Show open file dialog
				final File file = fileChooser.showOpenDialog(null);
				
				if (file != null) {
					ModelReaderWriterController modelReaderWriter = new ModelReaderWriterController(dataPointsModel, featureExtractionModel, file, false);
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
				
				trainMode = true;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"Text files (*.txt)", "*.txt", "*.TXT");
				fileChooser.getExtensionFilters().add(extFilter);
				
				
				// Show open file dialog
				final File file = fileChooser.showOpenDialog(null);
				
				if (file != null) {
					
					Task<Void> task = new Task<Void>() {
			
						@Override
						protected Void call() throws Exception {
							MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames, startModel.isAutoModeFlag());
							return null;
						}
			
					};
					
					new Thread(task).start();
				
				}
				
			}
		
		});
		
	}
	
	private boolean checkChannelsVHDR(File datafile) {
		
		boolean flag = false;
		channelNames = null;
		int countChannels = 0;
		channelNumbersToRead.clear();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(datafile));

			String zeile = null;
						
			
			while ((zeile = in.readLine()) != null) {
				
				// Read number of channels
				if (zeile.startsWith("NumberOfChannels=")) {
					channelNames = new String[Integer.parseInt(zeile.substring(17))];
				}
				
				// Read channel names
				if (zeile.startsWith("Ch")) {
					String[] tmp = zeile.split(",");

					if (tmp.length == 4) {
						int stringIndex = tmp[0].indexOf("=");
						channelNames[countChannels] = tmp[0].substring(stringIndex+1);
						countChannels++;
					}
				}
			}
			
			in.close();
			
		} catch (IOException e) {
			System.err.println("No file found on current location.");
			//e.printStackTrace();
		}
		
		// Check whether the the SVM Model is trained for one of the given channels
		File folder = new File(".").getAbsoluteFile();
		for( File file : folder.listFiles() ) {		
			for (int i = 0; i < channelNames.length; i++) { 
				if (file.getName().contains(channelNames[i]) && file.getName().contains("model")) {
					flag = true;
					channelNumbersToRead.add(i);
				} 	
			}
		}
		
		// The flag signalizes if in the chosen dataset is one channel which can be used for the classification
		return flag;
		
	}
	
	private boolean checkChannelsSMR(File dataFile) {
		
		channels = 0;
		titel.clear();
		kind.clear();
		numberOfChannels = 0;
		channelNames = null;
		channelNumbersToRead.clear();
		
		boolean flag = false;
		
		try {
			smrFile = new RandomAccessFile(dataFile, "rw");
			FileChannel inChannel = smrFile.getChannel();
			inChannel.position(30);
			
			ByteBuffer buf = ByteBuffer.allocate(2);
			buf.order(ByteOrder.LITTLE_ENDIAN);
		
			int bytesRead = inChannel.read(buf);

			//Make buffer ready for read
			buf.flip();
			
			channels = buf.getShort();					// Number of channels which are included in the given file
			
			for (int i = 0; i < channels; i++){
				
				// Offset due to file header and preceding channel headers
				int offset = 512 + (140 * (i) + 108);
	
				inChannel.position(offset);
				buf = ByteBuffer.allocate(16);
				buf.order(ByteOrder.LITTLE_ENDIAN);
				bytesRead = inChannel.read(buf);
				buf.flip();
				
				int actPos = buf.position();
				byte[] bytes = new byte[9];
				buf.get(bytes, 0, 9);
				
				String fileString = new String(bytes,StandardCharsets.UTF_8);
				fileString = fileString.trim();
				
				String tmp = "untitled";
				int diff = 0;
				for (int y = tmp.length()-1; y > 0; y--) {
					if ((tmp.charAt(y) == fileString.charAt(y))) {
						diff = y;
					}
				}
				fileString = fileString.substring(0, diff);
				
				titel.add(fileString);
				
				buf.position(actPos + (1 + 9 + 4));
				kind.add((int) buf.get());
				
			}
			
			// Get the number of channels
			for (int i = 0; i < kind.size(); i++) {
				if (kind.get(i) == 1) {
					numberOfChannels++;
				}
			}			
			
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
			//e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error occured during reading the *.smr File!");
			//e.printStackTrace();
		}
		
		channelNames = new String[numberOfChannels];		//To avoid handling lists and arrays in MainController we parse the list to an array now
		
		// Check whether the the SVM Model is trained for one of the given channels
				File folder = new File(".").getAbsoluteFile();
				for( File file : folder.listFiles() ) {		
					for (int i = 0; i < numberOfChannels; i++) {
						
						channelNames[i] = titel.get(i);		//To avoid handling lists and arrays in MainController we parse the list to an array now
						
						if (file.getName().contains(titel.get(i)) && file.getName().contains("model")) {
							flag = true;
							channelNumbersToRead.add(i);
						} 	
					}
				}
				
		// The flag signalizes if in the chosen dataset is one channel which can be used for the classification
		
		return flag;
		
	}
	
	private void startAction(final File file) {
		
		if (file.getName().toLowerCase().endsWith(".smr")) {
			
			if (checkChannelsSMR(file)) {
				
				// In this version we only allow to classify one channel. Not all features are implemented to use more than one channel.
				if (channelNumbersToRead.size() == 1) {
					
					
					Task<Void> task = new Task<Void>() {
			
						@Override
						protected Void call() throws Exception {
							MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames, startModel.isAutoModeFlag());
							
							return null;
						}
			
					};
					
					progressBar.setVisible(true);
					cancelButton.setVisible(true);
					newProject.setDisable(true);		
					openProject.setDisable(true);
					createModel.setDisable(true);
					setting.setDisable(true);
					
					
					text1.getStyleClass().removeAll("textLabel");
					text1.getStyleClass().add("textLabelDisabled");
					
					text2.getStyleClass().removeAll("textLabel");
					text2.getStyleClass().add("textLabelDisabled");
					
					text3.getStyleClass().removeAll("textLabel");
					text3.getStyleClass().add("textLabelDisabled");
					
					new Thread(task).start();
					
					
				} else {
					popUp.showPopupMessage("Only one channel classification is supported yet!", primaryStage);
				}
				
				
			} else {
				popUp.showPopupMessage("SMR: No trained channel for the selected dataset found!", primaryStage);
			}
			
			
		} else if(file.getName().toLowerCase().endsWith(".vhdr")) {
		
			if (checkChannelsVHDR(file)) {
				
				// In this version we only allow to classify one channel. Not all features are implemented to use more than one channel.
				if (channelNumbersToRead.size() == 1) {
					
					Task<Void> task = new Task<Void>() {
			
						@Override
						protected Void call() throws Exception {
							MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames, startModel.isAutoModeFlag());
							return null;
						}
			
					};
									
					progressBar.setVisible(true);
					cancelButton.setVisible(true);
					newProject.setDisable(true);		
					openProject.setDisable(true);
					createModel.setDisable(true);
					setting.setDisable(true);
					
					text1.getStyleClass().removeAll("textLabel");
					text1.getStyleClass().add("textLabelDisabled");
					
					text2.getStyleClass().removeAll("textLabel");
					text2.getStyleClass().add("textLabelDisabled");
					
					text3.getStyleClass().removeAll("textLabel");
					text3.getStyleClass().add("textLabelDisabled");
					
										
					new Thread(task).start();
					

				} else {
					popUp.showPopupMessage("Only one channel classification is supported yet!", primaryStage);
				}
				
			} else {
				popUp.showPopupMessage("No trained channel for the selected dataset found!", primaryStage);
			}
		}
		
		
	}
	
	@FXML
	protected void settingOnAction() {
		settings = new FXSettingController(startModel);
	}
	
	@FXML
	protected void cancelButtonOnAction() {
		StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
        
        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            cmd.append(jvmArg + " ");
        }
        
        cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        cmd.append(MainController.class.getName()).append(" ");
        
        try {
			Runtime.getRuntime().exec(cmd.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

        System.exit(0);
	}
	
	public void setProgressBar(double value) {
		progressBar.setProgress(value);
	}
	
	public void startMainApp() {


	}
	
}