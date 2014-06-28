import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import com.sun.xml.internal.ws.util.StringUtils;

public class ReadSMR {
	
	private static String filePath;
	private static RandomAccessFile dataFile;
	
	private static int numberOfChannels = 0;
	
	// Information from File Header
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
	
	// Information from Channel Header
	
	// We dont know the exact number of channels. This is why we create lists,
	// which will be filled up. Example: All information for channel one are in
	// list position zero etc..
	private static LinkedList<Integer> delSize = new LinkedList<Integer>();
	private static LinkedList<Integer> nextDelBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> firstBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> lastBlock = new LinkedList<Integer>();
	private static LinkedList<Integer> blocks = new LinkedList<Integer>();
	private static LinkedList<Integer> nExtra = new LinkedList<Integer>();
	private static LinkedList<Integer> preTrig = new LinkedList<Integer>();
	private static LinkedList<Integer> free0 = new LinkedList<Integer>();
	private static LinkedList<Integer> phySz = new LinkedList<Integer>();
	private static LinkedList<Integer> maxData = new LinkedList<Integer>();
	private static LinkedList<Integer> maxChanTime = new LinkedList<Integer>();
	private static LinkedList<Integer> lChanDvd = new LinkedList<Integer>();
	private static LinkedList<Integer> phyChan = new LinkedList<Integer>();
	private static LinkedList<String> titel = new LinkedList<String>();
	private static LinkedList<Float> idealRate = new LinkedList<Float>();
	private static LinkedList<Integer> kind = new LinkedList<Integer>();
	private static LinkedList<Integer> pad = new LinkedList<Integer>();
	private static LinkedList<Float> scale = new LinkedList<Float>();
	private static LinkedList<Float> offset = new LinkedList<Float>();
	private static LinkedList<Integer> divide = new LinkedList<Integer>();
	private static LinkedList<Integer> interleave = new LinkedList<Integer>();

	
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
		System.out.println("------------------------");
		printChannelInformation(0);
		
	}
	
	private static void readSMRDataFile(RandomAccessFile smrFile) {
	
		try {
			FileChannel inChannel = smrFile.getChannel();
						
			// Saves the first 512 byte for the file header
			ByteBuffer buf = ByteBuffer.allocate((int) smrFile.length());
			buf.order(ByteOrder.LITTLE_ENDIAN);
		
			int bytesRead = inChannel.read(buf);

			//Make buffer ready for read
			buf.flip();
			
			// ************** Get File Header Information **************
			
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
			
			// ************** Get Channel Header Information **************
			
			for (int i = 0; i < channels; i++){
				
				// Offset due to file header and preceding channel headers
				int offset = 512 + (140 * (i));
				buf.position(offset);
				
				delSize.add((int) buf.getShort());
				nextDelBlock.add(buf.getInt());
				firstBlock.add(buf.getInt());
				lastBlock.add(buf.getInt());
				blocks.add((int) buf.getShort());
				nExtra.add((int) buf.getShort());
				preTrig.add((int) buf.getShort());
				free0.add((int) buf.getShort());
				phySz.add((int) buf.getShort());
				maxData.add((int) buf.getShort());
				
				// Set new position, because we skip reading the comment
				buf.position(buf.position() + (1+71));
				
				maxChanTime.add(buf.getInt());
				lChanDvd.add(buf.getInt());
				phyChan.add((int) buf.getShort());
				
				int actPos = buf.position();
				//TODO: Hier muss der Title herausgelesen werden, um herauszufinden welcher Kanal Fz ist.			
				// Set new position, because we skip reading the title
				byte[] bytes = new byte[9];
				buf.get(bytes, 0, 9);
				
				String fileString = new String(bytes,StandardCharsets.UTF_8);
				fileString = fileString.trim();
				
				String tmp = "untitled";
				int diff = 0;
				for (int y = tmp.length()-1; y > 0; y--) {
					if ((tmp.charAt(y) == fileString.charAt(y))) {
						diff = y;
					}
				}
				fileString = fileString.substring(0, diff);
				
				titel.add(fileString);
				buf.position(actPos + (1 + 9));
				
				idealRate.add(buf.getFloat());
				kind.add((int) buf.get());
				pad.add((int) buf.get());
				
				if (kind.get(i) == 1) {
					scale.add(buf.getFloat());
					ReadSMR.offset.add(buf.getFloat());
					
					// Set new position, because we skip reading units
					buf.position(buf.position() + (1+5));
					
					if (systemId < 6) {
						divide.add((int) buf.getShort());
					} else {
						interleave.add((int) buf.getShort());
					}	
				}				
			}
			
			// Get the number of channels
			for (int i = 0; i < kind.size(); i++) {
				if (kind.get(i) == 1) {
					numberOfChannels++;
				}
			}
			
			// ************** Get data for each channel **************
			
			// Run over each channel
			for (int i = 0; i < numberOfChannels; i++) {
				
				if (kind.get(i) == 1) {
					buf.position(firstBlock.get(i) + 20);
					System.out.println("FirstValue: " + buf.getShort());
					
				} else {
					System.err.println("Channel #" + i + ": No waveform data found!");
				}
				
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
		
		System.out.println(firstBlock.get(0));
	}
	
	/**
	 * Returns all channel information for the given parameter.
	 * @param channel
	 * 			the channel from which you want to get the channel information.
	 */
	private static void printChannelInformation(int channel) {
		System.out.println("NextDelBlock: " + nextDelBlock.get(channel));
		System.out.println("FirstBlock: " + firstBlock.get(channel));
		System.out.println("LastBlock: " + lastBlock.get(channel));
		System.out.println("Blocks: " + blocks.get(channel));
		System.out.println("nExtra: " + nExtra.get(channel));
		System.out.println("PreTrig: " + preTrig.get(channel));
		System.out.println("free0: " + free0.get(channel));
		System.out.println("phySz: " + phySz.get(channel));
		System.out.println("MaxData: " + maxData.get(channel));
		System.out.println("MaxChanTime: " + maxChanTime.get(channel));
		System.out.println("lChanDvD: " + lChanDvd.get(channel));
		System.out.println("phyChan: " + phyChan.get(channel));
		System.out.println("Title: " + titel.get(channel));
		System.out.println("IdealRate: " + idealRate.get(channel));
		System.out.println("Kind: " + kind.get(channel));
		System.out.println("Pad: " + pad.get(channel));
		System.out.println("Scale: " + scale.get(channel));
		System.out.println("Offset: " + offset.get(channel));		
		
		// To avoid errors we do not print divide- and interleave-information
	}
}

