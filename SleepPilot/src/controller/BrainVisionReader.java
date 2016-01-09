/*
 * Copyright (C) 2016 Arne Weigenand
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package controller;

import com.google.common.primitives.Doubles;
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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import tools.Util;

/**
 *
 * @author Arne
 * @author Nils Finke
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
    private float channelResolution;

    private int nbchan;
    private int pnts;
    private double srate;
    private float[] data;
    private String[] channelNames;

    private ByteBuffer buf;
    private long nSamples;
    private int bytes;
    private boolean isAsciiRead;
    private List<float[]>asciiData;

    public BrainVisionReader(File file) {
        this.file = file;
        if (file.getName().toLowerCase().endsWith(".vhdr")) {
            readHeaderFromVHDR();
        }
        /**
         * Has to be set to 0 initially, reflects changes in buffer size
         */
        nSamples = 1;
        isAsciiRead = false;
    }

    public void read(int channel, int from, int to) {
        if (dataFormat.equals(DataFormat.BINARY)) {
            readBinary(channel, from, to);
        } else if (dataFormat.equals(DataFormat.ASCII)) {
            if (isAsciiRead) {
                asciiData = readAscii(file);
                isAsciiRead=true;
            }
            int j=0;
            for (int i = from; i < to; i++) {
                data[j] = asciiData.get(channel)[i];
                j++;
            }
        }
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
    public final float[] readBinary(int channel, int from, int to) {

        //TODO: check bounds!
        int nSamples = to - from;
        if (this.nSamples != nSamples) {
            prepareBuffers(nSamples);
        }

        try {
            FileChannel inChannel = dataFile.getChannel();
            // Set the start position in the file

            buf.clear();
            if (dataOrientation.equals(DataOrientation.MULTIPLEXED)) {
                inChannel.position((from * bytes * nbchan) + (channel * bytes));
            } else if (dataOrientation.equals(DataOrientation.VECTORIZED)) {
                inChannel.position((nbchan * pnts + from) * bytes);

            }
            inChannel.read(buf);
            // Make buffer ready for read
            buf.flip();

            final int increment = (nbchan * bytes) - bytes;
            final boolean flag = dataOrientation.equals(DataOrientation.MULTIPLEXED);

            int i = 0;
            while (buf.hasRemaining()) {
                if (bytes == 2) {
                    data[i] = buf.getShort() * channelResolution;
                } else if (bytes == 4) {
                    data[i] = buf.getFloat();
                } else if (bytes == 8) {
                    data[i] = (float) buf.getDouble();
                }

                if (flag) {
                    // This is the next sample in this epoch for the given channel  
                    if (buf.hasRemaining()) {
                        buf.position(buf.position() + increment);
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public final List<float[]> readAscii(File dataFile) {
        List<float[]> out = null;
        try (BufferedReader in = new BufferedReader(new FileReader(dataFile))) {
            out = in.lines()
                    .map(e -> e.replaceAll(",", "."))
                    .map(e -> e.split(" "))
                    .map(e -> Util.doubleToFloat(Arrays.stream(e).mapToDouble(i -> Double.parseDouble(i)).toArray()))
                    .collect(Collectors.toList()).subList(skipLines, pnts);
        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private void readHeaderFromVHDR() {
        int countChannels = 0;

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
                        case "ASCII":
                            dataFormat = DataFormat.ASCII;
                            break;
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
                    bytes = 2; // default
                    switch (zeile.substring(13)) {
                        case "UINT_16":
                            binaryFormat = BinaryFormat.UINT_16;
                            bytes = 2;
                            break;
                        case "INT_16":
                            binaryFormat = BinaryFormat.INT_16;
                            bytes = 2;
                            break;
                        case "IEEE_FLOAT_32":
                            binaryFormat = BinaryFormat.IEEE_FLOAT_32;
                            bytes = 4;
                            break;
                        case "IEEE_FLOAT_64":
                            binaryFormat = BinaryFormat.IEEE_FLOAT_64;
                            bytes = 8;
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
                            channelResolution = 1;
                        } else {
                            channelResolution = (float) Double.parseDouble(tmp[2]);
                        }
                        countChannels++;
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set some standard values, if header is not complete
        srate = 1e6 / samplingIntervall;

        if (pnts == 0) {
            try {
                pnts = (int) (dataFile.length() / bytes / (long) nbchan);
            } catch (IOException ex) {
                Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (dataType == null) {
            dataType = DataType.TIMEDOMAIN;
        }

        printPropertiesVHDR();
    }

    private void prepareBuffers(int nSamples) {
        this.nSamples = nSamples;
        data = new float[nSamples];
        if (dataFormat.equals(DataFormat.BINARY) && dataType.equals(DataType.TIMEDOMAIN)) {
            if (dataOrientation.equals(DataOrientation.MULTIPLEXED)) {
                buf = ByteBuffer.allocateDirect(bytes * nSamples * nbchan);
            } else if (dataOrientation.equals(DataOrientation.VECTORIZED)) {
                buf = ByteBuffer.allocateDirect(bytes * nSamples);
            }
        } else {
            System.out.println("Cannot recognize specific BrainVision format");
        }
        buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void close() throws IOException {
        dataFile.close();
    }

    public double getSrate() {
        return srate;
    }

    public int getNbchan() {
        return nbchan;
    }

    public float[] getData() {
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
