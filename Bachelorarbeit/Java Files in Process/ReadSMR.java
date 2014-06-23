import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class ReadSMR {
	
	private static String filePath;
	private static RandomAccessFile dataFile;
	
	// Information from Header file
	private static int systemId;
	private static int usPerTime;
	private static int timePerADC;
	private static int fileState;
	private static int firstData;
	private static int channels;
	private static int chanSize;
	private static int extraData;
	private static int bufferSize;
	private static int osFormat;
	private static int maxFTime;
	private static double dTimeBase;
	
	
	public static void main(String args[]) {
		
		filePath = args[0];
		try {
			dataFile = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException e) {
			System.err.println("Error occured during reading the *.smr data file!");
			//e.printStackTrace();
		}
		
		readSMRDataFile(dataFile);
		printHeaderInformation();
		
	}
	
	private static void readSMRDataFile(RandomAccessFile smrFile) {
	
		try {
			FileChannel inChannel = smrFile.getChannel();
						
			// Saves the first 512 byte for the file header
			ByteBuffer buf = ByteBuffer.allocate(512);
			buf.order(ByteOrder.LITTLE_ENDIAN);
		
			int bytesRead = inChannel.read(buf);

			//Make buffer ready for read
			buf.flip();
			
			systemId = buf.getShort();
			
			// We skip copyright and creator information
			buf.position(buf.position() + 18);
			
			usPerTime = buf.getShort();
			timePerADC = buf.getShort();
			fileState = buf.getShort();
			firstData = buf.getInt();
			channels = buf.getShort();
			chanSize = buf.getShort();
			extraData = buf.getShort();
			bufferSize = buf.getShort();
			osFormat = buf.getShort();
			maxFTime = buf.getInt();
			dTimeBase = buf.getDouble();
			
			if (systemId < 6){
				dTimeBase = 1e-6;
			}
						
			smrFile.close();
			
		
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Error during reading the *.smr data file!");
		}
	}
	
	private static void printHeaderInformation() {
		System.out.println("SystemID: " + systemId);
		System.out.println("UsPerTime: " + usPerTime);
		System.out.println("TimerPerADC: " + timePerADC);
		System.out.println("FileState: " + fileState);
		System.out.println("FirstData: " + firstData);
		System.out.println("Channels: " + channels);
		System.out.println("ChanSize: " + chanSize);
		System.out.println("ExtraData: " + extraData);
		System.out.println("BufferSize: " + bufferSize);
		System.out.println("OsFormat: " + osFormat);
		System.out.println("MaxFTime: " + maxFTime);
		System.out.println("DTimeBase: " + dTimeBase);
	}
}

