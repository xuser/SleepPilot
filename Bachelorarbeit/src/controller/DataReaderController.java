package controller;

import help.BinaryFormat;
import help.DataOrientation;
import help.DataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class DataReaderController {
	
	private String fileLocationPath;
	
	// [Common Infos]
	private File headerFile;
	private File dataFile;
	private File markerFile;
	private DataOrientation dataOrientation;
	private DataType dataType;
	private int numberOfChannels;
	private int dataPoints;
	private int samplingInterval;
	
	// [Binary Infos]
	private BinaryFormat binaryFormat;
	private boolean useBigEndianOrder;
	
	// [Channel Infos]
	private float channelResolution;
	
	/**
	 * Constructor which initialize this reader class.
	 * @throws IOException 
	 */
	public DataReaderController(String fileLocation) throws IOException {
		fileLocationPath = fileLocation;
		
		headerFile = new File(fileLocationPath);
		readHeaderFile();
		
		readDataFile(dataFile);
	}
	
	private void readHeaderFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(headerFile));
			String zeile = null;
			
			while ((zeile = in.readLine()) != null) {
				
				// Open DataFile
				if (zeile.startsWith("DataFile=")) {
					dataFile = new File(headerFile.getParent() + File.separator + zeile.substring(9));
				}
				
				// Open MarkerFile
				if (zeile.startsWith("MarkerFile=")) {
					markerFile = new File(headerFile.getParent() + File.separator + zeile.substring(11));
				}
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Reads the first data value of channel 1
	 * 
	 * @param dataFile file with data content
	 */
	private void readDataFile(File dataFile) {		

		try {
			InputStream in = new FileInputStream(dataFile);
		
			String firstByte = Integer.toHexString(in.read());			
			String secondByte = Integer.toHexString(in.read());
			
			System.out.println(decodeInteger16(firstByte, secondByte));
			
			in.close();					
					
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	/**
	 * This method decodes the given 16 bit hex value to the respective signed integer value.
	 * The two bytes changed their order because of little indian storage definded by Intel.
	 * 
	 * @param firstByte of the 16 Bit hex value
	 * @param secondByte of the 16 Bit hex value
	 * @return the decoded signed integer 16 bit value of the 16 bit hex coded value
	 */
	private short decodeInteger16(String firstByte, String secondByte) {
		
		String tmp = secondByte + firstByte;
		
		short value = (short) Integer.parseInt(tmp, 16);
		
		return value;
	}

}
