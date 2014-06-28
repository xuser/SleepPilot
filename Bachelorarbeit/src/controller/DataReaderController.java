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
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sun.text.normalizer.CharTrie.FriendAgent;
import model.DataPoints;

public class DataReaderController extends Thread {
	
	private String fileLocationPath;
	private DataPoints respectiveModel;
	private Thread t;
	
	// These two variables are necessary for setting the value into the right dataPoint position.
	int column = 0;
	int row = 0;
	
	// [Common Infos]
	private File headerFile;
	private RandomAccessFile dataFile;
	private File dataFileForAscii;
	
	@SuppressWarnings("unused")
	private File markerFile;
	
	private DataFormat dataFormat;
	private DataOrientation dataOrientation;
	private DataType dataType;
	
	// [Binary Infos]
	private BinaryFormat binaryFormat;
	private boolean useBigEndianOrder;
	
	// [ASCII Infos]
	private int skipLines = 0;
	private int skipColumns = 0;
	
	// [Channel Infos]
	private double channelResolution;
	
	String[] channelNames;
	int countChannels = 0;
	
	/**
	 * Constructor which initialize this reader class.
	 * @throws IOException 
	 */
	public DataReaderController(File fileLocation, DataPoints dataPointsModel) throws IOException {
		headerFile = fileLocation;
		respectiveModel = dataPointsModel;
	
	}
	public void run() {
//		headerFile = new File(fileLocationPath);
		
		readHeaderFile();
		respectiveModel.setChannelNames(channelNames);

		printProperties();
		
		if (dataOrientation.equals(DataOrientation.MULTIPLEXED) && dataType.equals(DataType.TIMEDOMAIN) && skipColumns == 0) {
			
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
				readDataFileAscii(dataFileForAscii);
				
			} else {
				System.err.println("No compatible data format!");
				
			}
			
		} else {
			System.err.println("No supported data orientation, data type or count of skip columns! ");
		}
		
		System.out.println("Finished Reading!!");
	}

	
	/**
	 * Reads the header file and select the necessary information.
	 */
	private void readHeaderFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(headerFile));
			String zeile = null;
			String dataFileLocation = null;
			
			while ((zeile = in.readLine()) != null) {
				
				// Open DataFile
				if (zeile.startsWith("DataFile=")) {
					dataFileLocation = headerFile.getParent() + File.separator + zeile.substring(9);
					dataFile = new RandomAccessFile(dataFileLocation, "rw");
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
								dataFileForAscii = new File(dataFileLocation);
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
					respectiveModel.setNumberOfChannels(Integer.parseInt(zeile.substring(17)));
					channelNames = new String[respectiveModel.getNumberOfChannels()];
				}
				
				// Read number of data points
				if (zeile.startsWith("DataPoints=")) {
					respectiveModel.setNumberOfDataPoints(Integer.parseInt(zeile.substring(11)));
				}
				
				// Read sampling intervall
				if (zeile.startsWith("SamplingInterval")) {
					respectiveModel.setSamplingIntervall(Integer.parseInt(zeile.substring(17)));
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
				// TODO: IMPORTANT: It could be possible, that each channel has a different resolution!
				if (zeile.startsWith("Ch")) {
					String[] tmp = zeile.split(",");

					if (tmp.length == 4) {
						channelNames[countChannels] = tmp[0].substring(4);
						if (tmp[2].isEmpty()) {
							channelResolution = 1.0;
						} else {
							channelResolution = Double.parseDouble(tmp[2]);
						}
						countChannels++;
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
	private void readDataFileInt(RandomAccessFile dataFile) {		
					
		try {
			/* ---- Start just for testing ---- */
//			File file = new File("/Users/Nils/Desktop/Decodierte Ascii Werte.txt");
//			FileWriter writer = new FileWriter(file, true);
			/* ---- End just for testing ---- */
			
			FileChannel inChannel = dataFile.getChannel();

			
			
			ByteBuffer buf = ByteBuffer.allocate((int) dataFile.length());
			buf.order(ByteOrder.LITTLE_ENDIAN);
			
			int bytesRead = inChannel.read(buf);
					
			// This function has to be called here, because you now know how big the matrix have to be
			respectiveModel.createDataMatrix();

			while (bytesRead != -1) {
				
				// Make buffer ready for read
				buf.flip();
				
				while (buf.hasRemaining()) {
					Double value = (buf.getShort() * channelResolution);
					
					// Rounded a mantisse with value 3
					BigDecimal myDec = new BigDecimal(value);
					myDec = myDec.setScale(3, BigDecimal.ROUND_HALF_UP);
					value = myDec.doubleValue();
					
					if (row < respectiveModel.getNumberOfDataPoints()) {
						respectiveModel.setDataPoints(value, row, column);
					}
					
					if ((buf.position()/2) % respectiveModel.getNumberOfChannels() == 0) {
						column = 0;
						row = row + 1;
						respectiveModel.setRowInSampleFile(row);
					} else {
						column = column + 1;
					}
									
				/* ---- Start just for testing ---- */
//				writer.write(value + " ");
//				writer.flush();
//				
//				// Modulo
//				if ((buf.position()/2) % respectiveModel.getNumberOfChannels() == 0) {
//					writer.write(System.getProperty("line.separator"));
//					writer.flush();
//				}
				/* ---- End just for testing ---- */
					
				}
				
				buf.clear(); //make buffer ready for writing
				bytesRead = inChannel.read(buf);
			}	
			dataFile.close();
									
//			writer.close();  // Just for testing
			
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		respectiveModel.setReadingComplete(true);
	}
	
	/**
	 * Reads the given data file and prints it on hard disk
	 * 
	 * @param dataFile
	 */
	private void readDataFileFloat(RandomAccessFile dataFile) {
		try {
			/* ---- Start just for testing ---- */
//			File file = new File("/Users/Nils/Desktop/Decodierte Float Werte.txt");
//			FileWriter writer = new FileWriter(file, true);
			/* ---- End just for testing ---- */
			
			FileChannel inChannel = dataFile.getChannel();
			
			ByteBuffer buf = ByteBuffer.allocate((int) dataFile.length());
			buf.order(ByteOrder.LITTLE_ENDIAN);
			
			int bytesRead = inChannel.read(buf);
			
			// This function has to be called here, because you now know how big the matrix have to be
			respectiveModel.createDataMatrix();
			
			while (bytesRead != -1) {

				//Make buffer ready for read
				buf.flip();
				
				while (buf.hasRemaining()) {
					
					Double value = (buf.getFloat() * channelResolution);
					
					// Rounded a mantisse with value 3
					BigDecimal myDec = new BigDecimal(value);
					myDec = myDec.setScale(3, BigDecimal.ROUND_HALF_UP);
					value = myDec.doubleValue();
					
					if (row < respectiveModel.getNumberOfDataPoints()) {
						respectiveModel.setDataPoints(value, row, column);
					}
					
					if ((buf.position()/4) % respectiveModel.getNumberOfChannels() == 0) {
						column = 0;
						row = row + 1;
						respectiveModel.setRowInSampleFile(row);
					} else {
						column = column + 1;
					}			
				
				/* ---- Start just for testing ---- */
//				writer.write(value + " ");
//				writer.flush();
//				
//				// Modulo
//				if ((buf.position()/4) % respectiveModel.getNumberOfChannels() == 0) {
//					writer.write(System.getProperty("line.separator"));
//					writer.flush();
//				}
				/* ---- End just for testing ---- */
					
				}
				
				buf.clear(); //make buffer ready for writing
				bytesRead = inChannel.read(buf);
			}
			
			dataFile.close();
//			writer.close(); //Just for testing

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		respectiveModel.setReadingComplete(true);
	}
	
	/**
	 * Reads the given ascii file and prints it on hard disk.
	 * 
	 * @param dataFile
	 */
	private void readDataFileAscii(File dataFile) {
		try {
			/* ---- Start just for testing ---- */
//			File file = new File("/Users/Nils/Desktop/Ascii Werte.txt");
//			FileWriter writer = new FileWriter(file, true);
			/* ---- End just for testing ---- */

			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String zeile = null;
			
			// This function has to be called here, because you now know how big the matrix have to be
			respectiveModel.createDataMatrix();
			
			for (int i = 0; i < skipLines; i++) {
				zeile = in.readLine();
			}
			
			while ((zeile = in.readLine()) != null) {
				
				String[] tmp = zeile.split(" ");
				
				for (int i = 0; i < tmp.length; i++) {
					
					tmp[i] = tmp[i].replaceAll(",", ".");
					
					/* ---- Start just for testing ---- */
//					writer.write(Double.valueOf(tmp[i]) + " ");
//					writer.flush();
					/* ---- End just for testing ---- */
					
					respectiveModel.setDataPoints(Double.valueOf(tmp[i]), row, i);
					
				}
				
				row = row + 1;
				respectiveModel.setRowInSampleFile(row);
						
				/* ---- Start just for testing ---- */
//				writer.write(System.getProperty("line.separator"));
//				writer.flush();
				/* ---- End just for testing ---- */
				
			}
			
			in.close();
//			writer.close();		// Just for testing
			
		} catch (FileNotFoundException e) {
			System.err.println("No file found on current location.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		respectiveModel.setReadingComplete(true);
	}
	
	/**
	 * Testfunction: Proof manually, if properties are correct.
	 */
	private void printProperties() {
		
		System.out.println("DataFormat: " + dataFormat);
		System.out.println("DataOrientation: " + dataOrientation);
		System.out.println("DataType: " + dataType);
		System.out.println("NumberOfChannels: " + respectiveModel.getNumberOfChannels());
		System.out.println("DataPoints: " + respectiveModel.getNumberOfDataPoints());
		System.out.println("SamplingIntervall: " + respectiveModel.getSamplingIntervall());
		System.out.println("BinaryFormat: " + binaryFormat);
		System.out.println("SkipLines: " + skipLines);
		System.out.println("SkipColumns: " + skipColumns);
		System.out.println("UseBigEndianOrdner: " + useBigEndianOrder);
		System.out.println("ChannelResolution: " + channelResolution);
		
		String[] tmp = respectiveModel.getChannelNames();
		System.out.print("ChannelNames:");
		for (int i = 0; i < tmp.length; i++) {
			System.out.print(" " + tmp[i]);
		}
		System.out.println();
		
		System.out.println("SamplingRate in Hertz: " + respectiveModel.getSamplingRateConvertedToHertz());
		
	}
	
	/**
	 * This method starts the Data Reader Thread.
	 */
	public void start() {
		System.out.println("Starting Data Reader Thread");
		if (t == null) {
			t = new Thread(this, "DataReader");
			t.start();
		}
	}

}
