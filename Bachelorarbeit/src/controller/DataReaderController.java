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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

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
	
	// [ASCII Infos]
	private int skipLines;
	private int skipColumns;
	
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
		printProperties();
		
		if (dataOrientation.equals(DataOrientation.MULTIPLEXED) && dataType.equals(DataType.TIMEDOMAIN)) {
			
			if (dataFormat.equals(DataFormat.BINARY)) {
				switch (binaryFormat) {
				case INT_16: readDataFileInt(dataFile);
					break;
				case IEEE_FLOAT_32: readDataFileFloat(dataFile);
					break;
				default: System.err.println("No compatible binary format!");
					break;
				}
				
			} else if (dataFormat.equals(DataFormat.ASCII)) {
				readDataFileAscii(dataFile);
				
			} else {
				System.err.println("No compatible data format!");
				
			}
			
		} else {
			System.err.println("No supported data orientation or data type!");
		}
		
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
				
				// Read skip lines
				if (zeile.startsWith("SkipLines=")) {
					skipLines = Integer.parseInt(zeile.substring(10));
				}
				
				// Read skip columns
				if (zeile.startsWith("SkipColumns=")) {
					skipColumns = Integer.parseInt(zeile.substring(12));
				}
				
				// Read channel resolution
				// IMPORTANT: It could be possible, that each channel has a different resolution!
				if (zeile.startsWith("Ch1=")) {
					String[] tmp = zeile.split(",");
					
					if (tmp.length == 4) {
						if (tmp[2].isEmpty()) {
							channelResolution = 1.0;
						} else {
						channelResolution = Double.parseDouble(tmp[2]);
						}
					}
				}				
			}
			
		in.close();
			
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
	private void readDataFileInt(File dataFile) {		

		try {
			File file = new File("/Users/Nils/Desktop/Decodierte Ascii Werte.txt");
			FileWriter writer = new FileWriter(file, true);
			
			InputStream in = new FileInputStream(dataFile);

			for (int i = 1; i <= (dataPoints * numberOfChannels); i++) {
			
				String firstByte = Integer.toHexString(in.read());			
				String secondByte = Integer.toHexString(in.read());
				
				writer.write(decodeInteger16(firstByte, secondByte) + " ");
				writer.flush();
				
				// Modulo
				if (i % numberOfChannels == 0) {
					writer.write(System.getProperty("line.separator"));
					writer.flush();
				}
				
			}
			in.close();	
			writer.close();
			
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Reads the given data file and prints it on hard disk
	 * 
	 * @param dataFile
	 */
	private void readDataFileFloat(File dataFile) {
		try {
			File file = new File("/Users/Nils/Desktop/Decodierte Float Werte.txt");
			FileWriter writer = new FileWriter(file, true);
			
			InputStream in = new FileInputStream(dataFile);
			
			for (int i = 1; i <= (dataPoints * numberOfChannels); i++) {

				String firstByte = Integer.toHexString(in.read());
				String secondByte = Integer.toHexString(in.read());
				String thirdByte = Integer.toHexString(in.read());
				String fourthByte = Integer.toHexString(in.read());
				
				writer.write(decodeFloat32(firstByte, secondByte, thirdByte, fourthByte) + " ");
				writer.flush();
				
				// Modulo
				if (i % numberOfChannels == 0) {
					writer.write(System.getProperty("line.separator"));
					writer.flush();
				}
			}
			
			in.close();
			writer.close();

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Reads the given ascii file and prints it on hard disk.
	 * 
	 * @param dataFile
	 */
	private void readDataFileAscii(File dataFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String zeile = null;

			File file = new File("/Users/Nils/Desktop/Ascii Werte.txt");
			FileWriter writer = new FileWriter(file, true);
			
			while ((zeile = in.readLine()) != null) {
				
				String[] tmp = zeile.split(" ");
				
				for (int i = 0; i < tmp.length; i++) {
					writer.write(tmp[i] + " ");
					writer.flush();
				}
				
				writer.write(System.getProperty("line.separator"));
				writer.flush();
				
			}
			
			in.close();
			writer.close();	
			
			
			
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
	 * @return the decoded signed integer 16 bit value of the 16 bit hex coded value.
	 */
	private double decodeInteger16(String firstByte, String secondByte) {
		String tmp;
		double value;
		
		if (useBigEndianOrder == false) {
			tmp = secondByte + firstByte;	
			value = (short) Integer.parseInt(tmp, 16);
		} else {
			tmp = firstByte + secondByte;
			value = (short) Integer.parseInt(tmp, 16);
		}
		
		// Converting the readed value to microvolt
		value = value * channelResolution;
		
		// Rounded a mantisse with value 3
		BigDecimal myDec = new BigDecimal(value);
		myDec = myDec.setScale(3, BigDecimal.ROUND_HALF_UP);
		value = myDec.doubleValue();
		
		return value;
	}
	
	/**
	 * This method decodes the given 32 bit hex value to the respective signed float value.
	 * The four bytes changed their order because of little indian storage definded by Intel.
	 * 
	 * @param firstByte
	 * @param secondByte
	 * @param thirdByte
	 * @param fourthByte
	 * @return the decoded signed float 32 bit value of the 32 bit hex coded value.
	 */
	private double decodeFloat32(String firstByte, String secondByte, String thirdByte, String fourthByte) {
		String tmp;
		double value;
		
		if (useBigEndianOrder == false) {
			tmp = fourthByte + thirdByte + secondByte + firstByte;

			Long i = Long.parseLong(tmp, 16);
			value = Float.intBitsToFloat(i.intValue());

			
		} else {
			tmp = firstByte + secondByte + thirdByte + fourthByte;
			Long i = Long.parseLong(tmp, 16);
			value = Float.intBitsToFloat(i.intValue());
		}
		
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
		System.out.println("SkipLines: " + skipLines);
		System.out.println("SkipColumns: " + skipColumns);
		System.out.println("UseBigEndianOrdner: " + useBigEndianOrder);
		System.out.println("ChannelResolution: " + channelResolution);
		
	}

}
