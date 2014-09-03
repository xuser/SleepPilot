package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.ResourceBundle;

import controller.MainController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
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

	private boolean trainMode = false;
	private LinkedList<Integer> channelNumbersToRead = new LinkedList<Integer>();
	private String[] channelNames;
	
	//Necessary for reading the smr information
	private int channels;
	private int numberOfChannels;
	private static LinkedList<String> titel = new LinkedList<String>();
	private static LinkedList<Integer> kind = new LinkedList<Integer>();

	
	// JavaFx components
	private Stage primaryStage;
	
	private AnchorPane mainGrid;
	
	private Scene scene;
	
	private File file;
	private RandomAccessFile smrFile;
	
	@FXML ProgressBar progressBar;
	@FXML ProgressIndicator progressIndicator;
	
	@FXML Button newProject;		
	@FXML Button openProject;
	@FXML Button createModel;
	
	@FXML Polygon newProjectForm;
	@FXML Polygon openProjectForm;
	@FXML Polygon createModelForm;
	
	@FXML Text label1;
	@FXML Text label2;
	@FXML Text label3;
	@FXML Text label4;
	
	@FXML Separator separator1;
	@FXML Separator separator2;
	@FXML Separator separator3;
	
	
	public FXStartController(Stage stage) {
		
		primaryStage = stage;
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("StartNew.fxml"));
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
		progressIndicator.setDisable(true);

		newProjectForm.setVisible(false);
		openProjectForm.setVisible(false);
		createModelForm.setVisible(false);
		
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		newProject.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				primaryStage.setHeight(320);
				
				label1.setVisible(true);
				label2.setVisible(true);
				label3.setVisible(true);
				label4.setVisible(true);
				
				progressBar.setVisible(true);
				progressIndicator.setVisible(true);
				
				separator1.setVisible(true);
				separator2.setVisible(true);
				separator3.setVisible(true);
				
				openProjectForm.setVisible(false);
				createModelForm.setVisible(false);
				
				newProjectForm.setVisible(true);
				
				trainMode = false;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter(
							"BrainVision files (*.vhdr)", "*.vhdr");
				
				FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter(
						"Spike2 files (*.smr)", "*.smr");
			
				fileChooser.getExtensionFilters().add(extFilter1);
				fileChooser.getExtensionFilters().add(extFilter2);
				
				
				// Show open file dialog
				file = fileChooser.showOpenDialog(null);
				
				startAction();
								
				
			}
		
		});
		
		openProject.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				primaryStage.setHeight(320);
				
				label1.setVisible(false);
				label2.setVisible(false);
				label3.setVisible(false);
				label4.setVisible(false);
				
				progressBar.setVisible(false);
				progressIndicator.setVisible(false);
				
				separator1.setVisible(false);
				separator2.setVisible(false);
				separator3.setVisible(false);
								
				newProjectForm.setVisible(false);
				createModelForm.setVisible(false);
				
				openProjectForm.setVisible(true);
				primaryStage.setHeight(440);
				
				trainMode = false;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"AutoScore files (*.as)", "*.as");
				fileChooser.getExtensionFilters().add(extFilter);
				
				
				// Show open file dialog
				file = fileChooser.showOpenDialog(null);
								
				
			}
		
		});
		
		createModel.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				primaryStage.setHeight(320);

				label1.setVisible(true);
				label2.setVisible(true);
				label3.setVisible(true);
				label4.setVisible(true);
				
				progressBar.setVisible(true);
				progressIndicator.setVisible(true);
				
				separator1.setVisible(true);
				separator2.setVisible(true);
				separator3.setVisible(true);
				
				newProjectForm.setVisible(false);
				openProjectForm.setVisible(false);
				
				createModelForm.setVisible(true);
				primaryStage.setHeight(440);
				
				trainMode = true;
				
				FileChooser fileChooser = new FileChooser();
				
				// Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
							"Text files (*.txt)", "*.txt");
				fileChooser.getExtensionFilters().add(extFilter);
				
				
				// Show open file dialog
				file = fileChooser.showOpenDialog(null);
				
				progressIndicator.setVisible(true);	
				progressIndicator.setProgress(-1);
				
				Task<Void> task = new Task<Void>() {
		
					@Override
					protected Void call() throws Exception {
						MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames);
						return null;
					}
		
				};
				
				new Thread(task).start();
				
			}
		
		});
		
	}
	
	private boolean checkChannelsVHDR() {
		
		boolean flag = false;
		channelNames = null;
		int countChannels = 0;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));

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
	
	private boolean checkChannelsSMR() {
		
		boolean flag = false;
		
		try {
			smrFile = new RandomAccessFile(file, "rw");
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
		
		// Check whether the the SVM Model is trained for one of the given channels
				File folder = new File(".").getAbsoluteFile();
				for( File file : folder.listFiles() ) {		
					for (int i = 0; i < numberOfChannels; i++) { 
						if (file.getName().contains(titel.get(i)) && file.getName().contains("model")) {
							flag = true;
							channelNumbersToRead.add(i);
						} 	
					}
				}
				
		// The flag signalizes if in the chosen dataset is one channel which can be used for the classification
		
		return flag;
		
	}
	

	private void startAction() {
		
		if (file.getName().toLowerCase().endsWith(".smr")) {
			
			if (checkChannelsSMR()) {
				
				
				
			} else {
				FXPopUp.showPopupMessage("SMR: No trained channel for the selected dataset found!", primaryStage);
			}
			
			
		} else if(file.getName().toLowerCase().endsWith(".vhdr")) {
		
			if (checkChannelsVHDR()) {
				
				// In this version we only allow to classify one channel. Not all features are implemented to use more than one channel.
				if (channelNumbersToRead.size() == 1) {
				
					primaryStage.setHeight(440);
		
					progressIndicator.setVisible(true);	
					progressIndicator.setProgress(-1);
					
					Task<Void> task = new Task<Void>() {
			
						@Override
						protected Void call() throws Exception {
							MainController.startClassifier(file, trainMode, channelNumbersToRead, channelNames);
							return null;
						}
			
					};
					
					new Thread(task).start();
				} else {
					FXPopUp.showPopupMessage("Only one channel classification is supported yet!", primaryStage);
				}
				
			} else {
				FXPopUp.showPopupMessage("No trained channel for the selected dataset found!", primaryStage);
			}
		}
		
		
	}
	

	public void setProgressBar(double value) {
		progressBar.setProgress(value);
		
		if (value == 1.0) {
			progressIndicator.setVisible(false);
		}
	}
	
	public void setProgressIndicator(double value) {
		progressIndicator.setProgress(value);
		
	}
	
}
