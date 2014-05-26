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
	
	private Thread t;
	private boolean fPause = false;
	
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
	public FilterController(DataPoints dataPointsFromModel, DataReaderController controller) {
		respectiveModel = dataPointsFromModel;
	}
	
	/**
	 * Starts the thread. Will exist when the method reaches his end.
	 */
	public void run() {
		
		// Run over each channel
		for (int y = 0; y < respectiveModel.getNumberOfChannels(); y++) {
			
			int i = 0;
			
			firstValueFromFilterWindow = 0;
			secondValueFromFilterWindow = 0;
			thirdValueFromFilterWindow = respectiveModel.getValueFromData(i, y);
			
			// Run over each value from one channel
			while (i < respectiveModel.getNumberOfDataPoints()) {
				
				//Check if thread have to pause.
				synchronized (this) {
					while (fPause) {
						try {
							wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				tmpFirst = ((b[0] * thirdValueFromFilterWindow) + (b[1] * secondValueFromFilterWindow) + (b[2] * firstValueFromFilterWindow));
				
				if (i >= 2) {				
					tmpSecond = ((a[1] * respectiveModel.getValueFromData(i-1, y)) + (a[2] * respectiveModel.getValueFromData(i-2, y)));
				} else if (i == 1) {
					tmpSecond = ((a[1] * respectiveModel.getValueFromData(i-1, y)) + (a[2] * y));
				} else if (i == 0) {
					tmpSecond = ((a[1] * 0) + (a[2] * 0));
				}
				
				filteredDataValue = tmpFirst - tmpSecond;
				
				respectiveModel.setDataPoints(filteredDataValue, i, y);
				
				if (i < (respectiveModel.getNumberOfDataPoints() - 1)) {
					firstValueFromFilterWindow = secondValueFromFilterWindow;
					secondValueFromFilterWindow = thirdValueFromFilterWindow;
					Thread.yield();
					thirdValueFromFilterWindow = respectiveModel.getValueFromData(i+1, y);
					
				}
				
				respectiveModel.setRowFilteredValues(i);
				i++;
				

			}
		}
		
		respectiveModel.setFilteringComplete(true);
		System.out.println("Finished Filtering!");
	}
	
	public void start()
	   {
	      System.out.println("Starting Filter Thread");
	      if (t == null)
	      {
	         t = new Thread (this, "Filter");
	         t.start ();
	      }
	   }
	
	public void pause() {
		fPause = true;
	}

	public void proceed() {
		fPause = false;
		notify();
	}
	
}
