package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controller.MainController;
import tsne.TSNE;
import model.FXViewModel;
import model.FeatureExtractionModel;
import model.RawDataModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXScatterPlot implements Initializable {
	
	private FXApplicationController appController;
	private FXViewModel viewModel;
	private FeatureExtractionModel featureExtractionModel;
	private RawDataModel dataPointsModel;
	
	private Stage stage;
	
	@FXML private ScatterChart<Number, Number> scatterChart;
	@FXML private ProgressIndicator progressIndicator;
	
	public FXScatterPlot(RawDataModel dataPointsModel, FeatureExtractionModel featureExtractionModel, FXViewModel viewModel) {
		this.featureExtractionModel = featureExtractionModel;
		this.viewModel = viewModel;
		this.dataPointsModel = dataPointsModel;
		
		appController = viewModel.getAppController();
		
		stage = new Stage();
		BorderPane addGrid = new BorderPane();
		
		// Creating FXML Loader
		FXMLLoader loader = new FXMLLoader(FXStartController.class.getResource("ScatterPlot.fxml"));
		loader.setController(this);
		
		// Try to load fxml file
		try {
			addGrid = loader.load();
		} catch (IOException e) {
			System.err.println("Error during loading ScatterPlot.fxml file!");
			//e.printStackTrace();
		}
		
		Scene scene = new Scene(addGrid);
		
//		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.setTitle("Scatter Plot");
		
		progressIndicator.setVisible(false);
		paintScatterChart();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		// Event will be fired when closing the application
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				viewModel.setScatterPlotActive(false);
				System.out.println("Scatter Plot is closing.");
			}			
		
		});	
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void paintScatterChart() {
		
		Task<Void> task = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.setVisible(true);
					}
				});
									
				TSNE tsne = new TSNE(featureExtractionModel.getFeatureValueMatrix());
				final double[][] output = tsne.tsne();		
				
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						
						XYChart.Series seriesRem = new XYChart.Series();
						XYChart.Series seriesS1 = new XYChart.Series();
						XYChart.Series seriesS2 = new XYChart.Series();
						XYChart.Series seriesN = new XYChart.Series();
						XYChart.Series seriesWake = new XYChart.Series();
						
						for (int i = 0; i < dataPointsModel.getNumberOf30sEpochs(); i++) {
							
							switch (featureExtractionModel.getFeatureClassLabel(i)) {
							case 1:	seriesWake.getData().add(new XYChart.Data(output[i][0], output[i][1]));
								break;
							case 2: seriesS1.getData().add(new XYChart.Data(output[i][0], output[i][1]));
								break;			
							case 3: seriesS2.getData().add(new XYChart.Data(output[i][0], output[i][1]));
								break;
							case 4: seriesN.getData().add(new XYChart.Data(output[i][0], output[i][1]));
								break;
							case 5: seriesRem.getData().add(new XYChart.Data(output[i][0], output[i][1]));
								break;
							default: System.err.println("Error during painting the scatter plot!");
								break;
							}
						}
						
						scatterChart.getData().add(seriesRem);
						scatterChart.getData().add(seriesS1);
						scatterChart.getData().add(seriesS2);
						scatterChart.getData().add(seriesN);
						scatterChart.getData().add(seriesWake);				
						
					}
				});
        
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.setVisible(false);
					}
				});	
				
		        return null;
			}
			
		};
		
		new Thread(task).start();

        
	}
	
	public void bringToFront() {
		stage.toFront();
	}
	
}
