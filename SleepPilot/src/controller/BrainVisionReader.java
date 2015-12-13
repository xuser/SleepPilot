/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arne
 */
public class BrainVisionReader {

    private File file;
    private File markerFile;
    private String dataFileLocation;
    private RandomAccessFile dataFile;
    private DataFormat dataFormat;
    private DataOrientation dataOrientation;
    private DataType dataType;    
    private int samplingIntervall;
    private BinaryFormat binaryFormat;
    private boolean useBigEndianOrder;
    private int skipLines;
    private int skipColumns;
    private double channelResolution;
    
    private int nbchan;
    private int pnts;
    private double srate;
    private double[] data;
    private String[] channelNames;

    private ByteBuffer buf;
    private long nSamples;
    private int bytes;
    

    public BrainVisionReader(File file) {
        this.file = file;
        if (file.getName().toLowerCase().endsWith(".vhdr")) {
            readHeaderFromVHDR();
        }
        /**
         * Has to be set to 0 initially, reflects changes in buffer size
         */
        nSamples=1;
        init();
    }

    /**
     * Testfunction: Proof manually, if properties are correct.
     */
    private void printPropertiesVHDR() {

        System.out.println("DataFormat: " + dataFormat);
        System.out.println("DataOrientation: " + dataOrientation);
        System.out.println("DataType: " + dataType);
        System.out.println("NumberOfChannels: " + nbchan);
        System.out.println("DataPoints: " + pnts);
        System.out.println("SamplingIntervall: " + samplingIntervall);
        System.out.println("BinaryFormat: " + binaryFormat);
        System.out.println("SkipLines: " + skipLines);
        System.out.println("SkipColumns: " + skipColumns);
        System.out.println("UseBigEndianOrdner: " + useBigEndianOrder);
        System.out.println("ChannelResolution: " + channelResolution);
        String[] tmp = channelNames;
        System.out.print("ChannelNames:");
        for (int i = 0; i < tmp.length; i++) {
            System.out.print(" " + tmp[i]);
        }
        System.out.println("SamplingRate in Hertz: " + srate);

    }

    /**
     * Reads the first data value of channel 1
     *
     * @param dataFile file with data content
     * @param channel
     * @param epochToRead the epoch which have to be read.
     * @return
     */
    public final double[] readDataFile(int channel, int from, int to) {

        //TODO: check bounds!
        
        int nSamples = to - from;
        if (this.nSamples != nSamples) {
            prepareBuffers(nSamples);
        }
        

        try {
            FileChannel inChannel = dataFile.getChannel();
            // Set the start position in the file
            inChannel.position((from * bytes * nbchan) + (channel * bytes));
            buf.order(ByteOrder.LITTLE_ENDIAN);
            inChannel.read(buf);
            // Make buffer ready for read
            buf.flip();

            final int increment = (nbchan * bytes) - bytes;

            double value;
            int i = 0;
            while (buf.hasRemaining()) {
                value = buf.getShort() * channelResolution;

                // Rounded a mantisse with value 3
                value = Math.round(value * 1000.);
                value /= 1000.;

                data[i] = value;

                // This is the next sample in this epoch for the given channel  
                if (buf.hasRemaining()) {
                    buf.position(buf.position() + increment);
                }
                i++;
            }
            buf.clear();

        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private void readHeaderFromVHDR() {
        int countChannels=0;
        
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String zeile = null;

                while ((zeile = in.readLine()) != null) {

                    // Open DataFile
                    if (zeile.startsWith("DataFile=")) {
                        dataFileLocation = file.getParent() + File.separator + zeile.substring(9);
                        dataFile = new RandomAccessFile(dataFileLocation, "r");
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
                        nbchan = Integer.parseInt(zeile.substring(17));
                        channelNames = new String[nbchan];
                    }

                    // Read number of data points
                    if (zeile.startsWith("DataPoints=")) {
                        pnts = Integer.parseInt(zeile.substring(11));
                    }

                    // Read sampling intervall
                    if (zeile.startsWith("SamplingInterval")) {
                        samplingIntervall = Integer.parseInt(zeile.substring(17));
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

                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        srate = 1e6/samplingIntervall;
    }

    private void prepareBuffers(int nSamples) {
        this.nSamples = nSamples;
        
        data = new double[nSamples];
        
        if (dataOrientation.equals(DataOrientation.MULTIPLEXED) && dataType.equals(DataType.TIMEDOMAIN) && skipColumns == 0) {

            if (dataFormat.equals(DataFormat.BINARY)) {
                switch (binaryFormat) {
                    case INT_16:
                        buf = ByteBuffer.allocate(bytes * nSamples * nbchan);
                        break;
                    case IEEE_FLOAT_32:
                        buf = ByteBuffer.allocate(bytes * nSamples * nbchan);
                        break;
                    default:
                        System.err.println("No compatible binary format!");
                        break;

                };
                //} else if (dataFormat.equals(DataFormat.ASCII)) {
                //	readDataFileAscii(dataFileForAscii);
            } else {
                System.err.println("No supported data orientation, data type or count of skip columns! ");
            }
        }
    }

    private void init() {
        bytes = 2;
        if (dataFormat.equals(DataFormat.BINARY)) {
            if (binaryFormat == BinaryFormat.INT_16) {
                bytes = 2;
            }
            if (binaryFormat == BinaryFormat.IEEE_FLOAT_32) {
                bytes = 4;
            }
        }

        if (pnts == 0) {
            try {
                pnts = (int) (dataFile.length() / bytes / (long) nbchan);
            } catch (IOException ex) {
                Logger.getLogger(DataReaderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (dataType == null) {
            dataType = DataType.TIMEDOMAIN;
        }

        printPropertiesVHDR();
    }
    
    public double getSrate() {
        return srate;
    }

    public int getNbchan() {
        return nbchan;
    }

    public double[] getData() {
        return data;
    }

    public int getPnts() {
        return pnts;
    }

    public String[] getChannelNames() {
        return channelNames;
    }

    public int getSamplingIntervall() {
        return samplingIntervall;
    }
 
}
//    reader = new DataReaderController.Reader() {
//                    @Override
//                    public double[] read(int channel, int epoch, double[] data) {
//                        return readDataFile(dataFile, channel, epoch, data, binaryFormat);
//                    }
//    };