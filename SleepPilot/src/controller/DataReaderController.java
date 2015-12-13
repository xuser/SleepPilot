package controller;

import java.io.File;
import java.io.IOException;

import model.RawDataModel;

public class DataReaderController {

    private RawDataModel dataModel;

    private int numberOfSamplesForOneEpoch;

    // [Common Infos]
    private File file;

    private Reader reader;

    /**
     * Constructor which initialize this reader class.
     *
     * @param fileLocation
     * @throws IOException
     */
    public DataReaderController(File fileLocation) throws IOException {
        file = fileLocation;
        dataModel = new RawDataModel();
        dataModel.setOrgFile(file);

        init();
    }

    // TODO: DataFile have to be closed at the end!
    public void readAll(int channel) {
        dataModel.rawEpochs.clear();
        for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
            double[] target = new double[numberOfSamplesForOneEpoch];
            dataModel.addRawEpoch(read(channel, i, target));
        }
        dataModel.setReadingComplete(true);
    }

    public double[] read(int channel, int epoch, double[] target) {
        return reader.read(channel, epoch, target);
    }

    public final void init() {
        if (file.getName().toLowerCase().endsWith(".smr")) {

        } else if (file.getName().toLowerCase().endsWith(".vhdr")) {
            /**
             * Load file and fill dataModel with necessary information.
             */
            BrainVisionReader bv = new BrainVisionReader(file);
            dataModel.setNumberOfDataPoints(bv.getPnts());
            dataModel.setNumberOfChannels(bv.getNbchan());
            dataModel.setChannelNames(bv.getChannelNames());
            dataModel.setSamplingIntervall(bv.getSamplingIntervall());
            dataModel.setReadingHeaderComplete(true);

            numberOfSamplesForOneEpoch = (int) (30 * bv.getSrate());
            dataModel.rawEpoch = new double[dataModel.getNumberOfChannels()][numberOfSamplesForOneEpoch];

            /**
             * Implement interface so that SleepPilot can use generic "read"
             * command for loading epochs
             */
            reader = new DataReaderController.Reader() {

                @Override
                public double[] read(int channel, int epoch, double[] target) {
                    int from = (int) (epoch * numberOfSamplesForOneEpoch);
                    int to = (int) (from + numberOfSamplesForOneEpoch);

                    bv.readDataFile(channel, from, to);
                    System.arraycopy(bv.getData(), 0, target, 0, target.length);
                    return target;
                }
            };

//           
        }
    }

    public static class Reader {

        public double[] read(int channel, int epoch, double[] target) {
            return target;
        }
    }

    public RawDataModel getDataModel() {
        return dataModel;
    }
}
