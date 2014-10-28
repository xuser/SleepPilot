package controller;

import gnu.trove.list.array.TDoubleArrayList;
import help.BinaryFormat;
import help.DataFormat;
import help.DataOrientation;
import help.DataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.RawDataModel;

public class DataReaderController {

    private String fileLocationPath;
    private RawDataModel respectiveModel;
    private LinkedList<Integer> channelsToRead;
    private int numberOfSamplesForOneEpoch;

    // These two variables are necessary for setting the value into the right dataPoint position.
    int column = 0;
    int row = 0;

    // [Common Infos]
    private File file;
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

    // Temp Epoch. Will be overridden by each reading operation
    TDoubleArrayList tmpEpoch;
    Reader reader;

    /**
     * Constructor which initialize this reader class.
     *
     * @param autoMode
     * @throws IOException
     */
    public DataReaderController(File fileLocation) throws IOException {
        file = fileLocation;
        respectiveModel = new RawDataModel();
        respectiveModel.setOrgFile(file);

        init();
    }

    // TODO: DataFile have to be closed at the end!
    public void readAll(int channel) {
        respectiveModel.rawEpochs.clear();
        for (int i = 0; i < respectiveModel.getNumberOf30sEpochs(); i++) {
            tmpEpoch = read(channel, i);
            respectiveModel.addRawEpoch(tmpEpoch);
        }
        respectiveModel.setReadingComplete(true);

    }

    public TDoubleArrayList read(int channel, int epoch) {
        return reader.read(channel, epoch);
    }

    public void init() {
//		headerFile = new File(fileLocationPath);

        if (file.getName().toLowerCase().endsWith(".smr")) {

            readHeaderFromSMR();

            reader = new Reader() {
                @Override
                public TDoubleArrayList read(int channel, int epoch) {
                    int tmp = (int) (respectiveModel.getlChanDvd(channel) * respectiveModel.getUsPerTime() * (1e6 * respectiveModel.getdTimeBase()));
                    respectiveModel.setSamplingIntervall(tmp);

                    int numberOfDataPoints = ((respectiveModel.getBlocks(channel) - 1) * respectiveModel.getMaxData(channel) + respectiveModel.getSizeOfLastBlock(channel));
                    respectiveModel.setNumberOfDataPoints(numberOfDataPoints);

                    respectiveModel.setReadingHeaderComplete(true);

                    numberOfSamplesForOneEpoch = (int) (respectiveModel.getSamplingRateConvertedToHertz() * 30);
                    return readSMRChannel(dataFile, channel, epoch);
                }
            };

            System.out.println(
                    "numberOfSamplesForOneEpoch: " + numberOfSamplesForOneEpoch);
            System.out.println(
                    "MaxData for one block: " + respectiveModel.getMaxData(16));
            System.out.println(
                    "#30s Epochs: " + respectiveModel.getNumberOf30sEpochs());

            printPropertiesSMR();

            printChannelInformationSMR(
                    16);

        } else if (file.getName().toLowerCase().endsWith(".vhdr")) {

            readHeaderFromVHDR();
            numberOfSamplesForOneEpoch = (int) (respectiveModel.getSamplingRateConvertedToHertz() * 30);

            long bytes = 2;
            if (respectiveModel.getNumberOfDataPoints() == 0) {
                if (dataFormat.equals(DataFormat.BINARY)) {
                    if (binaryFormat == BinaryFormat.INT_16) {
                        bytes = 2;
                    }
                    if (binaryFormat == BinaryFormat.IEEE_FLOAT_32) {
                        bytes = 4;
                    }
                }
                try {
                    respectiveModel.setNumberOfDataPoints((int) (dataFile.length()/bytes/(long)respectiveModel.getNumberOfChannels()));
                } catch (IOException ex) {
                    Logger.getLogger(DataReaderController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            printPropertiesVHDR();

            if (dataType == null) {
                dataType = DataType.TIMEDOMAIN;
            }

            if (dataOrientation.equals(DataOrientation.MULTIPLEXED) && dataType.equals(DataType.TIMEDOMAIN) && skipColumns == 0) {

                if (dataFormat.equals(DataFormat.BINARY)) {
                    switch (binaryFormat) {
                        case INT_16:
                            reader = new Reader() {
                                @Override
                                public TDoubleArrayList read(int channel, int epoch) {
                                    return readDataFileInt(dataFile, channel, epoch);
                                }
                            };

                            break;
                        case IEEE_FLOAT_32:
                            reader = new Reader() {
                                @Override
                                public TDoubleArrayList read(int channel, int epoch) {
                                    return readDataFileFloat(dataFile, channel, epoch);
                                }
                            };
                            break;
                        default:
                            System.err.println("No compatible binary format!");
                            break;
                    }
                    //} else if (dataFormat.equals(DataFormat.ASCII)) {
                    //	readDataFileAscii(dataFileForAscii);

                } else {
                    System.err.println("No compatible data format!");

                }

            } else {
                System.err.println("No supported data orientation, data type or count of skip columns! ");
            }

        }

        System.out.println("Finished Reading!!");
    }

    private void printPropertiesSMR() {

        System.out.println("SystemID: " + respectiveModel.getSystemId());
        System.out.println("UsPerTime: " + respectiveModel.getUsPerTime());
        System.out.println("TimerPerADC: " + respectiveModel.getTimePerADC());
        System.out.println("FileState: " + respectiveModel.getFileState());
        System.out.println("FirstData: " + respectiveModel.getFirstData());
        System.out.println("Channels: " + respectiveModel.getChannels());
        System.out.println("ChanSize: " + respectiveModel.getChanSize());
        System.out.println("ExtraData: " + respectiveModel.getExtraData());
        System.out.println("BufferSize: " + respectiveModel.getBufferSize());
        System.out.println("OsFormat: " + respectiveModel.getOsFormat());
        System.out.println("MaxFTime: " + respectiveModel.getMaxFTime());
        System.out.println("DTimeBase: " + respectiveModel.getdTimeBase());
    }

    private void readHeaderFromSMR() {
        try {
            dataFile = new RandomAccessFile(file, "r");
            respectiveModel.setDataFile(dataFile);

            FileChannel inChannel = dataFile.getChannel();

            // Saves the first 512 byte for the file header
            ByteBuffer buf = ByteBuffer.allocate(60);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            int bytesRead = inChannel.read(buf);

            //Make buffer ready for read
            buf.flip();

            // ************** Get File Header Information **************
            respectiveModel.setSystemId(buf.getShort());

            // We skip copyright and creator information
            buf.position(buf.position() + 18);

            respectiveModel.setUsPerTime(buf.getShort());
            respectiveModel.setTimePerADC(buf.getShort());
            respectiveModel.setFileState(buf.getShort());
            respectiveModel.setFirstData(buf.getInt());
            respectiveModel.setChannels(buf.getShort());
            respectiveModel.setChanSize(buf.getShort());
            respectiveModel.setExtraData(buf.getShort());
            respectiveModel.setBufferSize(buf.getShort());
            respectiveModel.setOsFormat(buf.getShort());
            respectiveModel.setMaxFTime(buf.getInt());
            respectiveModel.setdTimeBase(buf.getDouble());

            if (respectiveModel.getSystemId() < 6) {
                respectiveModel.setdTimeBase(1e-6);
            }

            // ************** Get Channel Header Information **************
            for (int i = 0; i < respectiveModel.getChannels(); i++) {

                // Offset due to file header and preceding channel headers
                int offset = 512 + (140 * (i));

                inChannel.position(offset);
                buf = ByteBuffer.allocate(160);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                bytesRead = inChannel.read(buf);
                buf.flip();

                respectiveModel.addDelSize((int) buf.getShort());
                respectiveModel.addNextDelBlock(buf.getInt());
                respectiveModel.addFirstBlock(buf.getInt());
                respectiveModel.addLastBlock(buf.getInt());
                respectiveModel.addBlocks((int) buf.getShort());
                respectiveModel.addnExtra((int) buf.getShort());
                respectiveModel.addPreTrig((int) buf.getShort());
                respectiveModel.addFree0((int) buf.getShort());
                respectiveModel.addPhySz((int) buf.getShort());
                respectiveModel.addMaxData((int) buf.getShort()); // 26

                // Set new position, because we skip reading the comment
                buf.position(buf.position() + (1 + 71)); // 98

                respectiveModel.addMaxChanTime(buf.getInt());
                respectiveModel.addlChanDvd(buf.getInt());
                respectiveModel.addPhyChan((int) buf.getShort()); // 108

                int actPos = buf.position();
                byte[] bytes = new byte[9];
                buf.get(bytes, 0, 9);

                String fileString = new String(bytes, StandardCharsets.UTF_8);
                fileString = fileString.trim();

                String tmp = "untitled";
                int diff = 0;
                for (int y = tmp.length() - 1; y > 0; y--) {
                    if ((tmp.charAt(y) == fileString.charAt(y))) {
                        diff = y;
                    }
                }
                fileString = fileString.substring(0, diff);

                respectiveModel.addTitel(fileString);
                buf.position(actPos + (1 + 9));

                respectiveModel.addIdealRate(buf.getFloat());
                respectiveModel.addKind((int) buf.get());
                respectiveModel.addPad((int) buf.get());

                if (respectiveModel.getKind(i) == 1) {
                    respectiveModel.addScale(buf.getFloat());
                    respectiveModel.addOffset(buf.getFloat());

                    // Set new position, because we skip reading units
                    buf.position(buf.position() + (1 + 5));

                    if (respectiveModel.getSystemId() < 6) {
                        respectiveModel.addDivide((int) buf.getShort());
                    } else {
                        respectiveModel.addInterleave((int) buf.getShort());
                    }
                } else {
                    respectiveModel.addScale(1);
                    respectiveModel.addOffset(0);
                }

                inChannel.position(respectiveModel.getLastBlock(i) + 18);
                buf = ByteBuffer.allocate(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                bytesRead = inChannel.read(buf);
                buf.flip();

                int sizeOfLastBlock = buf.getShort();
                respectiveModel.addSizeOfLastBlock(sizeOfLastBlock);

            }

            // Get the number of channels
            LinkedList<Integer> kind = respectiveModel.getListOfKind();
            int tmp = respectiveModel.getNumberOfChannels();

            for (int i = 0; i < kind.size(); i++) {
                if (kind.get(i) == 1) {
                    tmp++;
                }
            }
            respectiveModel.setNumberOfChannels(tmp);

//			dataFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
//			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] channelNames = new String[respectiveModel.getNumberOfChannels()];
        for (int i = 0; i < respectiveModel.getNumberOfChannels(); i++) {
            channelNames[i] = respectiveModel.getTitel(i);
        }

        respectiveModel.setChannelNames(channelNames);

    }

    public TDoubleArrayList readSMRChannel(RandomAccessFile dataFile, int channel, int epoch) {
        TDoubleArrayList tmpEpoch = new TDoubleArrayList(numberOfSamplesForOneEpoch);

//        tmpEpoch.add((double) epoch);
        int block = (epoch * numberOfSamplesForOneEpoch) / respectiveModel.getMaxData(channelsToRead.get(0));

        int sampleNumberInBlock = 0;
        if (epoch != 0) {
            sampleNumberInBlock = (epoch - 1) * numberOfSamplesForOneEpoch;
            sampleNumberInBlock = sampleNumberInBlock - (block * respectiveModel.getMaxData(channelsToRead.get(0)));
            sampleNumberInBlock = sampleNumberInBlock + numberOfSamplesForOneEpoch;
        }

        int countBlock = 0;
        int channelPosition = respectiveModel.getFirstBlock(channel);

        try {

            while (block != countBlock) {

                FileChannel inChannel = dataFile.getChannel();
                inChannel = inChannel.position(channelPosition + 4);
                ByteBuffer buf = ByteBuffer.allocate(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                int bytesRead = inChannel.read(buf);
                buf.flip();

                channelPosition = buf.getInt();
                countBlock++;

            }

            FileChannel inChannel = dataFile.getChannel();
            inChannel = inChannel.position(channelPosition);
            ByteBuffer buf = ByteBuffer.allocate(respectiveModel.getMaxData(channelsToRead.get(0)) * 4);					// * 2 because we have two bytes for each sample
            buf.order(ByteOrder.LITTLE_ENDIAN);
            int bytesRead = inChannel.read(buf);
            buf.flip();

            buf.position(buf.position() + 4);											// + 4 because we skip lastBlock element
            channelPosition = buf.getInt();
            buf.position(buf.position() + 12 + (sampleNumberInBlock * 2));				// + 12 because we skip channelNumbers and ItemsInBlock elements

            int runIndex = (respectiveModel.getMaxData(channelsToRead.get(0)) - sampleNumberInBlock);

            if (runIndex > numberOfSamplesForOneEpoch) {
                runIndex = numberOfSamplesForOneEpoch;

                for (int i = 0; i < runIndex; i++) {
                    tmpEpoch.add((double) buf.getShort());
                }

            } else {

                for (int i = 0; i < runIndex; i++) {
                    tmpEpoch.add((double) buf.getShort());
                }

                int nextBlock = channelPosition;
                while (nextBlock != -1) {
                    nextBlock = getWholeDataFromOneSMRChannel(buf, channel, nextBlock);
                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tmpEpoch;

    }

    private int getWholeDataFromOneSMRChannel(ByteBuffer buf, int channel, int succBlock) throws IOException {

        FileChannel inChannel = dataFile.getChannel();
        inChannel = inChannel.position(succBlock);
        int lastBlock = buf.getInt();
        int nextBlock = buf.getInt();

        buf.position(buf.position() + 8);
        int channelNumber = buf.getShort();
        int itemsInBlock = buf.getShort();

        int x = 0;
        while ((x < itemsInBlock) && (tmpEpoch.size() < (numberOfSamplesForOneEpoch + 1))) {
            Double value = (double) (buf.getShort() * respectiveModel.getScale(channelsToRead.get(0)));
            value = value / 6553.6;
            value = value + respectiveModel.getOffset(channelsToRead.get(0));

            // Rounded a mantisse with value 3
            double rValue = Math.round(value * Math.pow(10d, 3));
            rValue = rValue / Math.pow(10d, 3);
            tmpEpoch.add(rValue);

            x++;
        }

        if (tmpEpoch.size() == (numberOfSamplesForOneEpoch + 1)) {
            nextBlock = -1;
        }

        // Header information for this block
//		System.out.println("LastBlock: " + lastBlock);
//		System.out.println("NextBlock: " + nextBlock);
//
//		System.out.println("ChannelNumer: " + channelNumber);
//		System.out.println("Items in Block: " + itemsInBlock);	
        return nextBlock;
    }

    /**
     * Reads the header file and select the necessary information.
     */
    private void readHeaderFromVHDR() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String zeile = null;
            String dataFileLocation = null;

            while ((zeile = in.readLine()) != null) {

                // Open DataFile
                if (zeile.startsWith("DataFile=")) {
                    dataFileLocation = file.getParent() + File.separator + zeile.substring(9);
                    dataFile = new RandomAccessFile(dataFileLocation, "rw");
                    respectiveModel.setDataFile(dataFile);
                }

                // Open MarkerFile
                if (zeile.startsWith("MarkerFile=")) {
                    markerFile = new File(file.getParent() + File.separator + zeile.substring(11));
                }

                // Read DataFormat
                if (zeile.startsWith("DataFormat=")) {
                    switch (zeile.substring(11)) {
                        case "BINARY":
                            dataFormat = DataFormat.BINARY;
                            break;
                        //case "ASCII": dataFormat = DataFormat.ASCII;
                        //			dataFileForAscii = new File(dataFileLocation);
                        //	break;
                        default:
                            dataFormat = DataFormat.UNKNOWN;
                            break;
                    }
                }

                // Read DataOrientation
                if (zeile.startsWith("DataOrientation=")) {
                    switch (zeile.substring(16)) {
                        case "MULTIPLEXED":
                            dataOrientation = DataOrientation.MULTIPLEXED;
                            break;
                        case "VECTORIZED":
                            dataOrientation = DataOrientation.VECTORIZED;
                            break;
                        default:
                            dataOrientation = DataOrientation.UNKNOWN;
                            break;
                    }
                }

                // Read DataType
                if (zeile.startsWith("DataType=")) {
                    switch (zeile.substring(9)) {
                        case "TIMEDOMAIN":
                            dataType = DataType.TIMEDOMAIN;
                            break;
                        default:
                            dataType = DataType.UNKNOWN;
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
                        case "INT_16":
                            binaryFormat = BinaryFormat.INT_16;
                            break;
                        case "IEEE_FLOAT_32":
                            binaryFormat = BinaryFormat.IEEE_FLOAT_32;
                            break;
                        default:
                            binaryFormat = BinaryFormat.UNKNOWN;
                            break;
                    }
                }

                respectiveModel.setBinaryFormat(binaryFormat);

                // Read endian order
                if (zeile.startsWith("UseBigEndianOrder=")) {
                    switch (zeile.substring(18)) {
                        case "NO":
                            useBigEndianOrder = false;
                            break;
                        case "YES":
                            useBigEndianOrder = true;
                            break;
                        default:
                            useBigEndianOrder = false;
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
                        int stringIndex = tmp[0].indexOf("=");
                        channelNames[countChannels] = tmp[0].substring(stringIndex + 1);
                        if (tmp[2].isEmpty()) {
                            channelResolution = 1.0;
                        } else {
                            channelResolution = Double.parseDouble(tmp[2]);
                        }
                        countChannels++;
                    }
                }
                respectiveModel.setChannelNames(channelNames);

            }

            respectiveModel.setReadingHeaderComplete(true);

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
     * @param epoch the epoch which have to be read.
     * @return
     */
    public TDoubleArrayList readDataFileInt(RandomAccessFile dataFile, int channelToRead, int epochToRead) {

        TDoubleArrayList tmpEpochInt = new TDoubleArrayList(numberOfSamplesForOneEpoch);

        try {
            /* ---- Start just for testing ---- */
//			File file = new File("/Users/Nils/Desktop/Decodierte Ascii Werte.txt");
//			FileWriter writer = new FileWriter(file, true);
			/* ---- End just for testing ---- */

//			long start = new Date().getTime();
//			long time;
//            tmpEpochInt.add((double) epochToRead);
            FileChannel inChannel = dataFile.getChannel();
            inChannel.position((epochToRead * (numberOfSamplesForOneEpoch * 2) * respectiveModel.getNumberOfChannels()) + (channelToRead * 2));

            ByteBuffer buf = ByteBuffer.allocate((numberOfSamplesForOneEpoch) * 2 * respectiveModel.getNumberOfChannels());

            //ByteBuffer buf = ByteBuffer.allocate((int) dataFile.length());
            buf.order(ByteOrder.LITTLE_ENDIAN);

            int bytesRead = inChannel.read(buf);

            // This function has to be called here, because you now know how big the matrix have to be
            //respectiveModel.createDataMatrix();
            //while (bytesRead != -1) {
            // Make buffer ready for read
            buf.flip();

            // Set the start position in the file
            //buf.position((epochToRead * (numberOfSamplesForOneEpoch * 2)) + (channelToRead * 2));
            while (buf.hasRemaining()) {
                Double value = (buf.getShort() * channelResolution);

                // Rounded a mantisse with value 3
                double rValue = Math.round(value * Math.pow(10d, 3));
                rValue = rValue / Math.pow(10d, 3);

                tmpEpochInt.add(rValue);

                // This is the next sample in this epoch for the given channel
                if (buf.hasRemaining()) {
                    buf.position(buf.position() + (respectiveModel.getNumberOfChannels() * 2) - 2);
                }
            }

            buf.clear();
//				inChannel.close();

//				time = new Date().getTime() - start;
//				System.out.println("RunTime: " + time);
            /*while (buf.hasRemaining()) {
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
            /*}
				
             buf.clear(); //make buffer ready for writing
             bytesRead = inChannel.read(buf);
             }*/
//			dataFile.close();
//			writer.close();  // Just for testing
        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmpEpochInt;
    }

    /**
     * Reads the given data file and prints it on hard disk
     *
     * @param dataFile
     * @return
     */
    public TDoubleArrayList readDataFileFloat(RandomAccessFile dataFile, int channelToRead, int epochToRead) {

        TDoubleArrayList tmpEpochFloat = new TDoubleArrayList();

        try {
            /* ---- Start just for testing ---- */
//			File file = new File("/Users/Nils/Desktop/Decodierte Float Werte.txt");
//			FileWriter writer = new FileWriter(file, true);
			/* ---- End just for testing ---- */

//            tmpEpochFloat.add((double) epochToRead);
            FileChannel inChannel = dataFile.getChannel();
            inChannel.position((epochToRead * (numberOfSamplesForOneEpoch * 4) * respectiveModel.getNumberOfChannels()) + (channelToRead * 4));

            ByteBuffer buf = ByteBuffer.allocate((numberOfSamplesForOneEpoch * 4) * respectiveModel.getNumberOfChannels());
            buf.order(ByteOrder.LITTLE_ENDIAN);

            int bytesRead = inChannel.read(buf);

            // Make buffer ready for read
            buf.flip();
            while (buf.hasRemaining()) {
                Double value = (buf.getFloat() * channelResolution);

                // Rounded a mantisse with value 3
                double rValue = Math.round(value * Math.pow(10d, 3));
                rValue = rValue / Math.pow(10d, 3);

                tmpEpochFloat.add(rValue);

                // This is the next sample in this epoch for the given channel
                if (buf.hasRemaining()) {
                    buf.position(buf.position() + (respectiveModel.getNumberOfChannels() * 4) - 4);
                }
            }

            buf.clear();

            /*
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
            /*}
				
             buf.clear(); //make buffer ready for writing
             bytesRead = inChannel.read(buf);
             }
			
             dataFile.close();*/
//			writer.close(); //Just for testing
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmpEpochFloat;

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
    private void printPropertiesVHDR() {

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
     * Returns all channel information for the given parameter.
     *
     * @param channel the channel from which you want to get the channel
     * information.
     */
    private void printChannelInformationSMR(int channel) {
        System.out.println("NextDelBlock: " + respectiveModel.getNextDelBlock(channel));
        System.out.println("FirstBlock: " + respectiveModel.getFirstBlock(channel));
        System.out.println("LastBlock: " + respectiveModel.getLastBlock(channel));
        System.out.println("Blocks: " + respectiveModel.getBlocks(channel));
        System.out.println("nExtra: " + respectiveModel.getnExtra(channel));
        System.out.println("PreTrig: " + respectiveModel.getPreTrig(channel));
        System.out.println("free0: " + respectiveModel.getFree0(channel));
        System.out.println("phySz: " + respectiveModel.getPhySz(channel));
        System.out.println("MaxData: " + respectiveModel.getMaxData(channel));
        System.out.println("MaxChanTime: " + respectiveModel.getMaxChanTime(channel));
        System.out.println("lChanDvD: " + respectiveModel.getlChanDvd(channel));
        System.out.println("phyChan: " + respectiveModel.getPhyChan(channel));
        System.out.println("Title: " + respectiveModel.getTitel(channel));
        System.out.println("IdealRate: " + respectiveModel.getIdealRate(channel));
        System.out.println("Kind: " + respectiveModel.getKind(channel));
        System.out.println("Pad: " + respectiveModel.getPad(channel));
        System.out.println("Scale: " + respectiveModel.getScale(channel));
        System.out.println("Offset: " + respectiveModel.getOffset(channel));

        System.out.println("NumberOfChannels: " + respectiveModel.getNumberOfChannels());

        // To avoid errors we do not print divide- and interleave-information
        //System.out.println("Divide: " + divide.get(channel));
    }

    public static class Reader {

        public TDoubleArrayList read(int channel, int epoch) {
            return null;
        }
    }

    public RawDataModel getDataModel() {
        return respectiveModel;
    }
}
