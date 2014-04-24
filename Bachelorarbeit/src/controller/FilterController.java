package controller;

import model.DataPoints;

/**
 * This class contains methods for filtering the given data.
 * FILTER: High-pass filter
 * 
 * @author Nils Finke
 */
public class FilterController extends Thread {
	
	DataPoints respectiveModel;
	
	private double firstValueFromFilterWindow;
	private double secondValueFromFilterWindow;
	private double thirdValueFromFilterWindow;
	
	private double filteredDataValue;
	
	private double tmpFirst;
	private double tmpSecond;
	
	private double[] a = {1, -1.99374814332721, 0.993767820931055};
	private double[] b = {0.996879040258575, -1.99375788374111, 0.996879040258575};
	
	/**
	 * Constructor for initializing this class.
	 */
	public FilterController(DataPoints dataPointsFromModel) {
		respectiveModel = dataPointsFromModel;
		run();
	}
	
	/**
	 * Starts the thread. Will exist when the method reaches his end.
	 */
	public void run() {
		int i = 1;
		while (i <= respectiveModel.getNumberOfDataPoints()) {
			// TODO: Bisher wird der Filter nur auf den ersten Channel angewandt.
			
			firstValueFromFilterWindow = 0;
			secondValueFromFilterWindow = 0;
			thirdValueFromFilterWindow = respectiveModel.printValueFromData(i, 1);
			
			tmpFirst = ((b[0] * thirdValueFromFilterWindow) + (b[1] * secondValueFromFilterWindow) + (b[2] * firstValueFromFilterWindow));
			// TODO: tmpSecond = ;
			
			filteredDataValue = tmpFirst - tmpSecond;
			
			respectiveModel.setDataPoints(filteredDataValue, i, 1);
			
			
			
			
			
			firstValueFromFilterWindow = respectiveModel.printValueFromData(i, 1);
			
			if (i+1 <= respectiveModel.getNumberOfDataPoints()) {
				secondValueFromFilterWindow = respectiveModel.printValueFromData(i+1, 1);
			}
			
			if (i+2 <= respectiveModel.getNumberOfDataPoints()) {
				thirdValueFromFilterWindow = respectiveModel.printValueFromData(i+2, 1);
			}
			

			i++;
		}
	}
	
	
}
