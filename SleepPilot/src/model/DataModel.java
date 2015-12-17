package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class holds the epochList, that has been read by the DataReaderController.
 * Model for the DataReaderController and the FilterController.
 *
 * @author Nils Finke
 */
public class DataModel implements Serializable {

    private File file;

    private Reader reader;

    /**
     * This list keeps lists with the epochs (raw epochList). IMPORTANT: The head of
     * the inter list keeps the assosiated epoch.
     */
    public ArrayList<double[]> epochList = new ArrayList();
    public double[][] data;

    // *** Brainvision Format ***
    // [Common Infos]
    private int pnts;
    private int nbchan = 0;					// This variable will be used for the Spike2 Format as well
    private double srate;
    private String[] channelNames;
    public int numberOfSamplesForOneEpoch;

    /**
     * @return the pnts
     */
    public int getPnts() {
        return pnts;
    }

    /**
     * @param pnts the pnts to set
     */
    public void setPnts(int pnts) {
        this.pnts = pnts;
    }

    /**
     * @return the nbchan
     */
    public int getNbchan() {
        return nbchan;
    }

    /**
     * @param nbchan the nbchan to set
     */
    public void setNbchan(int nbchan) {
        this.nbchan = nbchan;
    }

    public void setSrate(double srate) {
        this.srate = srate;
    }

    /**
     * @return the srate
     */
    public double getSrate() {
        return srate;
    }

    /**
     * INFO: ChannelName for channel one is at position 0 etc. Shift!
     *
     * @return the channelNames
     */
    public String[] getChannelNames() {
        return channelNames;
    }

    /**
     * @param channelNames the channelNames to set
     */
    public void setChannelNames(String[] channelNames) {
        this.channelNames = channelNames;
    }

    public int getNumberOf30sEpochs() {
        return (int) (pnts / (30 * srate));
    }

    public void addEpoch(double[] data) {
        this.epochList.add(data);
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Reader getReader() {
        return reader;
    }

    public ArrayList<double[]> getEpochList() {
        return epochList;
    }

    public interface Reader {

        public double[] read(int channel, int epoch, double[] target);

        public void close();
    }

    
}
