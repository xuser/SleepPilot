package controller;

import help.BinaryFormat;
import help.DataFormat;
import help.DataOrientation;
import help.DataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class DataReaderController {
	
	private String fileLocationPath;
	
	// [Common Infos]
	private File headerFile;
	private File dataFile;
	private File markerFile;
	private DataFormat dataFormat;
	private DataOrientation dataOrientation;
	private DataType dataType;
	private int numberOfChannels;
	private int dataPoints;
	private int samplingInterval;
	
	// [Binary Infos]
	private BinaryFormat binaryFormat;
	private boolean useBigEndianOrder;
	
	// [Channel Infos]
	private double channelResolution;
	
	/**
	 * Constructor which initialize this reader class.
	 * @throws IOException 
	 */
	public DataReaderController(String fileLocation) throws IOException {
		fileLocationPath = fileLocation;
		
		headerFile = new File(fileLocationPath);
		readHeaderFile();
		
		readDataFile(dataFile);
		
		printProperties();
	}
	
	/**
	 * Reads the header file and elect the necessary information.
	 */
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
				
				// Read DataFormat
				if (zeile.startsWith("DataFormat=")) {
					switch (zeile.substring(11)) {
					case "BINARY": dataFormat = DataFormat.BINARY;
						break;
					case "ASCII": dataFormat = DataFormat.ASCII;
						break;
					default: dataFormat = DataFormat.UNKNOWN;
						break;
					}
				}
				
				// Read DataOrientation
				if (zeile.startsWith("DataOrientation=")) {
					switch (zeile.substring(16)) {
					case "MULTIPLEXED": dataOrientation = DataOrientation.MULTIPLEXED;
						break;
					case "VECTORIZED": dataOrientation = DataOrientation.VECTORIZED;
						break;
					default: dataOrientation = DataOrientation.UNKNOWN;
						break;
					}
				}
				
				// Read DataOrientation
				if (zeile.startsWith("DataType=")) {
					switch (zeile.substring(9)) {
					case "TIMEDOMAIN": dataType = DataType.TIMEDOMAIN;
						break;
					default: dataType = DataType.UNKNOWN;
						break;
					}
				}
				
				// Read number of channels
				if (zeile.startsWith("NumberOfChannels=")) {
					numberOfChannels = Integer.parseInt(zeile.substring(17));
				}
				
				// Read number of data points
				if (zeile.startsWith("DataPoints=")) {
					dataPoints = Integer.parseInt(zeile.substring(11));
				}
				
				// Read sampling intervall
				if (zeile.startsWith("SamplingInterval")) {
					samplingInterval = Integer.parseInt(zeile.substring(17));
				}
				
				// Read binary format
				if (zeile.startsWith("BinaryFormat=")) {
					switch (zeile.substring(13)) {
					case "INT_16": binaryFormat = BinaryFormat.INT_16;
						break;
					case "IEEE_FLOAT_32": binaryFormat = BinaryFormat.IEEE_FLOAT_32;
						break;
					default: binaryFormat = BinaryFormat.UNKNOWN;
						break;
					}
				}
				
				// Read endian order
				if (zeile.startsWith("UseBigEndianOrder=")) {
					switch (zeile.substring(18)) {
					case "NO": useBigEndianOrder = false;
						break;
					case "YES": useBigEndianOrder = true;
						break;
					default: useBigEndianOrder = false;
						break;
					}
				}
				
				// Read channel resolution
				// IMPORTANT: It could be possible, that each channel has a different resolution!
				if (zeile.startsWith("Ch1=")) {
					String[] tmp = zeile.split(",");
					
					if (tmp.length == 4) {
						channelResolution = Double.parseDouble(tmp[2]);
					}
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
	private double decodeInteger16(String firstByte, String secondByte) {
		
		String tmp = secondByte + firstByte;	
		double value = (short) Integer.parseInt(tmp, 16);
		
		// Converting the readed value to microvolt
		value = value * channelResolution;
		
		// Rounded a mantisse with value 3
		BigDecimal myDec = new BigDecimal(value);
		myDec = myDec.setScale(3, BigDecimal.ROUND_HALF_UP);
		value = myDec.doubleValue();
		
		return value;
	}
	
	/**
	 * Testfunction: Proof manually, if properties are correct.
	 */
	private void printProperties() {
		
		System.out.println("DataFormat: " + dataFormat);
		System.out.println("DataOrientation: " + dataOrientation);
		System.out.println("DataType: " + dataType);
		System.out.println("NumberOfChannels: " + numberOfChannels);
		System.out.println("DataPoints: " + dataPoints);
		System.out.println("SamplingIntervall: " + samplingInterval);
		System.out.println("BinaryFormat: " + binaryFormat);
		System.out.println("UseBigEndianOrdner: " + useBigEndianOrder);
		System.out.println("ChannelResolution: " + channelResolution);
		
	}

}
