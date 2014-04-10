package controller;

import java.io.IOException;

/**
 * Starts the application and creates necessary initial controllers.
 * 
 * @author Nils Finke
 */
public class MainController {
	
	/**
	 * Filepath of .vhdr header file.
	 */
	private static String fileLocation;

	/**
	 * Starts the application with the needed parameters.
	 * 
	 * @param args
	 * 			no starting arguments are needed.
	 */
	public static void main(String[] args) {
		
		try {
			fileLocation = args[0];			
		} catch (Exception e) {
			System.err.println("No valid file location for runtime argument.");
		}
		
		// Creats a new controller which reads the declared file
		try {
			new DataReaderController(fileLocation);
		} catch (IOException e) {
			System.err.println("Unexpected error occured during reading the file.");
			e.printStackTrace();
		}
	}

}
