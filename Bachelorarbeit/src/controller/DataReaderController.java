package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DataReaderController {
	
	private String fileLocationPath;
	private File headerFile;
	
	/**
	 * Constructor which initialize this reader class.
	 * @throws IOException 
	 */
	public DataReaderController(String fileLocation) throws IOException {
		fileLocationPath = fileLocation;
		
		headerFile = new File(fileLocationPath);
		
		System.out.println(readHeaderFile());
	}
	
	private byte[] readHeaderFile() throws IOException {
		
		byte[] buffer = null;
		
		try {
			InputStream in = new FileInputStream(headerFile);
			buffer = new byte[4];
			
			in.read(buffer);
			in.close();
					
		} catch (Exception e) {
			System.err.println("No file found on current location.");
			e.printStackTrace();
		}
		
		return buffer;
		
	}
	
	
	
	
	
	
}
