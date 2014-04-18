package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import model.DataPoints;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChartController implements Initializable {
	
	@FXML GridPane contentGrid;
	@FXML LineChart<Number, Number> lineChart;

	@SuppressWarnings("rawtypes")
	private XYChart.Series series;
	private DataPoints respectiveModel;
	private Stage primaryStage;
	
	/**
	 * Main pane for displaying all elements.
	 */
	private GridPane mainGrid;
	
	/**
	 * Class container for all content in the scene graph.
	 */
	private Scene scene;
	
	public ChartController(Stage stage, DataPoints dataPointsModel) {
		respectiveModel = dataPointsModel;
		
		primaryStage = stage;
		
		// creating FXML loader
		FXMLLoader loader = new FXMLLoader(ChartController.class.getResource("Chart.fxml"));
		loader.setController(this);
			
		// try to load fxml file
		try {
			mainGrid = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		// create stage with mainGrid
		scene = new Scene(mainGrid);
		primaryStage.setScene(scene);
		
		// Properties for stage
		primaryStage.setResizable(true);
		primaryStage.show();
		primaryStage.setTitle("Chart");
        
	}
	
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

        series = new XYChart.Series();
        
        // number of data points for one 30 seconds epoch
        double tmp = respectiveModel.getSamplingRateConvertedToHertz() * 30;
        
        // Check if 
        if (tmp > respectiveModel.getNumberOfDataPoints()) {
        	tmp = respectiveModel.getNumberOfDataPoints();
        }
        
        for (int i = 1; i < tmp; i++) {
        	// TODO: Bisher wird immer nur ein Channel zur Zeit geplottet.
        	series.getData().add(new XYChart.Data(i, respectiveModel.printValueFromData(i, 1)));
        }

		lineChart.getData().add(series);
	}
	
}
