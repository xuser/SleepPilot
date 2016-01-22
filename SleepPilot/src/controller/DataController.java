package controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.DataModel;
import model.DataModel.Reader;
import ru.mipt.edf.EDFHeader;
import ru.mipt.edf.EDFParser;
import ru.mipt.edf.EDFParserException;

import son32java.son32reader.Son32Reader;
import son32java.son32Exceptions.NoChannelException;
import son32java.son32reader.Son32Channel;

public class DataController {

    private DataModel dataModel;
    private File file;
    private Reader reader;

    /**
     * Constructor which initialize this reader class.
     *
     * @throws IOException
     */
    public DataController(File file) throws IOException {
        this.file = file;
        dataModel = new DataModel();
        dataModel.setFile(file);

        init();
    }

    public void readAll(int channel) {
        dataModel.epochList.clear();
        for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
            float[] target = new float[dataModel.numberOfSamplesForOneEpoch];
            dataModel.addEpoch(read(channel, i, target));
        }
    }

    public float[] read(int channel, int epoch, float[] target) {
        return reader.read(channel, epoch, target);
    }

    public final void init() {

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
            Son32Channel son32Channel;
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

                son32Channel = smr.getChannel(0);
                dataModel.setSrate(son32Channel.getSamplingRateHz());

                //get the last time for the channel in clock ticks
                long maxTime = son32Channel.getChanMaxTime();
                //convert the max time from clock ticks to sec
                maxTime = (long) smr.getSecFromCT(maxTime);
                dataModel.setPnts((int) (maxTime * dataModel.getSrate())); //gibt es hier eine andere Art (mit ticks) um auf Pnts zu kommen? 

                dataModel.numberOfSamplesForOneEpoch = son32Channel.calculateArraySizeByTime(30);
                dataModel.data = new float[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            } catch (NoChannelException ex) {
                System.out.println(ex);
            }

            reader = new DataModel.Reader() {

                Son32Channel son32Channel;
                float[] buffer = new float[dataModel.numberOfSamplesForOneEpoch];

                @Override
                public void close() {
                    smr.SONCloseFile();
                }

                @Override
                public float[] read(int channel, int epoch, float[] target) {

                    try {
                        son32Channel = smr.getChannel(channel);
                        double sTime = epoch * 30;
                        double eTime = (epoch + 1) * 30;
                        son32Channel.getRealDataByTime(sTime, eTime, buffer);

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
        }
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
