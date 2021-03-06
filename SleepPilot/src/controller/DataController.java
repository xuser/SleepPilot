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

import bv.BrainVisionReader;
import cntFile.cntFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.DataModel;
import model.DataModel.Reader;
import neuroJExceptions.BadChannelException;
import neuroJExceptions.BadIntervallException;
import ru.mipt.edf.EDFHeader;
import ru.mipt.edf.EDFParser;
import ru.mipt.edf.EDFParserException;

import son32java.son32reader.Son32Reader;
import son32java.son32Exceptions.NoChannelException;

public class DataController {

    private final DataModel dataModel;
    private Reader reader;

    /**
     * Constructor which initialize this reader class.
     *
     * @throws IOException
     */
    public DataController(File file) throws IOException {
        dataModel = new DataModel();
        dataModel.setFile(file);

        init();
    }

    public ArrayList<float[]> readAll(int channel) {
        dataModel.epochList.clear();
        for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
            float[] target = new float[dataModel.numberOfSamplesForOneEpoch];
            dataModel.addEpoch(read(channel, i, target));
        }
        return dataModel.getEpochList();
    }

    public float[] read(int channel, int epoch, float[] target) {
        target = reader.read(channel, epoch, target);
        return target;
    }

    public final void init() {

        File file = dataModel.getFile();

        if (file.getName().toLowerCase().endsWith(".edf")) {

            final EDFParser edfParser = new EDFParser(file);
            final EDFHeader edfHeader = edfParser.getHeader();

            int numberOfChannels = edfHeader.getNumberOfChannels();

            /*
             Subtract 1 from the number of channels, because the edf parser
             adds an extra channel for annotation, which does not contain
             actual data.
             */
            dataModel.setNbchan(numberOfChannels - 1);

            String[] channelNames = edfHeader.getChannelLabels();

            //remove the annotation channel
            dataModel.setChannelNames(Arrays.copyOf(channelNames, dataModel.getNbchan()));

            double duration = edfHeader.getDurationOfRecords();     //in seconds?
            double samplingRate = edfHeader.getNumberOfSamples()[0] / duration;
            dataModel.setSrate(samplingRate);
            dataModel.setPnts((int) (edfHeader.getNumberOfRecords() * edfHeader.getNumberOfSamples()[0]));

            dataModel.numberOfSamplesForOneEpoch = (int) (samplingRate * 30);
            dataModel.data = new float[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            reader = new DataModel.Reader() {
                int lastEpoch = -1;
                float[][] data;

                @Override
                public void close() {
                }

                @Override
                public float[] read(int channel, int epoch, float[] target) {
                    long from = epoch * dataModel.numberOfSamplesForOneEpoch;
                    long to = (epoch + 1) * dataModel.numberOfSamplesForOneEpoch;

                    if (epoch != lastEpoch) {
                        try {
                            data = edfParser.read(channel, from, to);
                        } catch (EDFParserException ex) {
                            Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.arraycopy(data[channel], 0, target, 0, target.length);
                    return target;
                }
            };
            dataModel.setReader(reader);

        } else if (file.getName().toLowerCase().endsWith(".smr")) {

            Son32Reader smr = new Son32Reader(file.getPath(), 1);
            /**
             * Read necessary information from SMR reader
             */

            dataModel.setNbchan(smr.getNumberOfChannels());
            try {
                String[] channelNames = new String[dataModel.getNbchan()];
                for (int i = 0; i < dataModel.getNbchan(); i++) {
                    channelNames[i] = smr.getChannel(i).getChannelTitle();
                }
                dataModel.setChannelNames(channelNames);

                dataModel.setSrate(smr.getChannel(0).getSamplingRateHz());

                //get the last time for the channel in clock ticks
                long maxTime = smr.getChannel(0).getChanMaxTime();
                //convert the max time from clock ticks to sec
                maxTime = (long) smr.getSecFromCT(maxTime);
                dataModel.setPnts((int) (maxTime * dataModel.getSrate())); //gibt es hier eine andere Art (mit ticks) um auf Pnts zu kommen? 

                dataModel.numberOfSamplesForOneEpoch = smr.getChannel(0).calculateArraySizeByTime(30);
                dataModel.data = new float[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            } catch (NoChannelException ex) {
                System.out.println(ex);
            }

            reader = new DataModel.Reader() {
                @Override
                public void close() {
                    smr.close();
                }

                @Override
                public float[] read(int channel, int epoch, float[] target) {

                    try {
                        double sTime = epoch * 30;
                        double eTime = (epoch + 1) * 30;
                        float[] buffer = smr.read(channel, sTime, eTime);
                        System.arraycopy(buffer, 0, target, 0, target.length);
                        return target;
                    } catch (NoChannelException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    return target;
                }
            };
            dataModel.setReader(reader);

        } else if (file.getName().toLowerCase().endsWith(".vhdr")) {
            /**
             * Load file and fill dataModel with necessary information.
             */
            BrainVisionReader bv = new BrainVisionReader(file);
            dataModel.setPnts(bv.getPnts());
            dataModel.setNbchan(bv.getNbchan());
            dataModel.setChannelNames(bv.getChannelNames());
            dataModel.setSrate(bv.getSrate());
            dataModel.numberOfSamplesForOneEpoch = (int) (30 * bv.getSrate());
            dataModel.data = new float[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            /**
             * Implement interface so that SleepPilot can use generic "read"
             * command for loading epochs
             */
            reader = new DataModel.Reader() {

                @Override
                public void close() {
                    try {
                        bv.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public float[] read(int channel, int epoch, float[] target) {
                    int from = (int) (epoch * dataModel.numberOfSamplesForOneEpoch);
                    int to = (int) (from + dataModel.numberOfSamplesForOneEpoch);
                    bv.read(channel, from, to);
                    System.arraycopy(bv.getData(), 0, target, 0, target.length);
                    return target;
                }
            };
            dataModel.setReader(reader);

        } else if (file.getName().toLowerCase().endsWith(".cnt")) {
            cntFile cnt1 = null;
            try {
                //make new cntFile
                cnt1 = new cntFile(file.getCanonicalPath());
            } catch (IOException ex) {
                System.out.println(ex);
            }

            final cntFile cnt = cnt1;

            dataModel.setPnts(cnt.getNumberOfSamples());
            dataModel.setNbchan(cnt.getNumberOfChannels());
            dataModel.setChannelNames(cnt.getChannelNames());
            dataModel.setSrate(cnt.getSamplingRate());

            dataModel.numberOfSamplesForOneEpoch = (int) (30 * dataModel.getSrate());
            dataModel.data = new float[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            /**
             * Implement interface so that SleepPilot can use generic "read"
             * command for loading epochs
             */
            reader = new DataModel.Reader() {

                @Override
                public void close() {
                    cnt.close();
                }

                @Override
                public float[] read(int channel, int epoch, float[] target) {
                    int from = (int) (epoch * 30);
                    int to = (int) (from + 30);

                    try {
                        float[] tmp = cnt.readRawIntervallData(channel, (double) from, (double) to);
                        System.arraycopy(tmp, 0, target, 0, target.length);
                    } catch (IOException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (BadChannelException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (BadIntervallException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    return target;
                }
            };
            dataModel.setReader(reader);

        }
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
