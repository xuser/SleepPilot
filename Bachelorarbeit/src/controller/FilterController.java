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
