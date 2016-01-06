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
package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class holds the epochList, that has been read by the
 * DataReaderController. Model for the DataReaderController and the
 * FilterController.
 *
 * @author Arne Weigenand
 * @author Nils Finke
 */
public class DataModel implements Serializable {

    private File file;

    private Reader reader;

    /**
     * This list keeps lists with the epochs (raw epochList). IMPORTANT: The
     * head of the inter list keeps the assosiated epoch.
     */
    public ArrayList<float[]> epochList = new ArrayList();
    public float[][] data;

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
     * @param pnts Total number of data points in dataset
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

    public void addEpoch(float[] data) {
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

    public ArrayList<float[]> getEpochList() {
        return epochList;
    }

    public interface Reader {

        public float[] read(int channel, int epoch, float[] target);

        public void close();
    }

}
