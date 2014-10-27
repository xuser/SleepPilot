package model;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import help.ChannelNames;
import java.util.Arrays;
import org.jdsp.iirfilterdesigner.model.FilterCoefficients;

/**
 * This class is the respective model for the FeatureExtractionController.
 *
 * @author Nils Finke
 *
 */
public class FeatureExtractionModel implements Serializable {

    /**
     * This matrix saves continuously for each epoch their permutation
     * entropies. The first value (y-axis) represent the PE values for the
     * different epochs and the second value (x-axis) represent the different
     * channels.
     *
     * IMPORTANT: The first column is for training mode. Holds the manual
     * classified sleep stage.
     */
    private float[][] features;
    private int[] labels;
    private int[] artefacts;
    private int[] arousals;
    private int[] stimulation;
    private double[][] tsneFeatures;

    /**
     * This HashMap keeps additional information for some epochs. The key keeps
     * the respective epoch number and the value holds an array with the
     * information.
     *
     * IMPORTANT: Only add values to the hashmap if any additional information
     * has to be set
     *
     * 1st in array: 1 if artefact, else 0 2nd in array: 1 if arrousal, else 0
     * 3rd in array: 1 if stimulation, else 0
     */
    private HashMap<Integer, Integer[]> epochProperties = new HashMap<Integer, Integer[]>();

    private int countWake = 0;
    private int countS1 = 0;
    private int countS2 = 0;
    private int countN = 0;
    private int countREM = 0;
    private int countA = 0;
    private int countMA = 0;
    private int countS = 0;

    /**
     * Predict probabilities for the different classes. Each row keeps one array
     * with the probabilities for the epoch with the rowindex.
     */
    private double[][] predictProbabilities;

    /**
     * This variable keeps the number of the epoch to which the calculation of
     * the PE has been done.
     */
    private int numberOfcalculatedEpoch;

    /**
     * If automode is true, then the classification automation was used.
     */
    private boolean autoMode = false;

    private String selectedModel;

    /**
     * The path to the eeg data.
     */
    private File fileLocation;

    /**
     * Keeps the number of feature vectors.
     */
    private int numberOfFeatures;

    /**
     * Keeps the number of channels from input data.
     */
    private int numberOfChannels;

    /**
     * This variable holds the actual positon in the PE matrix.
     */
    private int rowPosition = 0;

    /**
     * Keeps the status, if the TrainController has finished reading and
     * calculating.
     */
    private boolean readingAndCalculatingDone = false;

    /**
     * Keeps the status, if the SVMController has finished the classification.
     */
    private boolean classificationDone = false;

    /**
     * Tells whether channel data has been read
     */
    private boolean readinDone = false;

    /**
     * Tells whether features have been computed
     */
    private boolean featuresComputed = false;

    /**
     * Tells whether dimension reduction of features has been computed
     */
    private boolean tsneComputed = false;

    /**
     * The number of samples for one epoch.
     */
    private int lengthOfOneEpoch;

    /**
     * The number of epochs for one record.
     */
    private int numberOfEpochs;

    /**
     * The channel name of the samples in the training file.
     */
    private LinkedList<ChannelNames> channelNames = new LinkedList<ChannelNames>();

    private int featureChannel;

    private FilterCoefficients highpassCoefficients;

    private FilterCoefficients lowpassCoefficients;

    /**
     * Creates the feature value matrix with the needed size. The first column
     * holds the classified sleep stage. Test mode: The first column has the
     * default value 99.00 Traing mode: The first column has the respective
     * value for the actual sleep stage
     *
     * NOTATION: 1	Wake 2	Sleep stage 1 3	Sleep stage 2 4	Sleep stage 3 5	REM
     * sleep stage 99	Unscored
     */
    public void init(int rows) {
        numberOfEpochs = rows;
        labels = new int[rows];
        Arrays.fill(labels, -1);

        artefacts = new int[rows];
        arousals = new int[rows];
        stimulation = new int[rows];
        predictProbabilities = new double[rows][];
    }

    public void addArtefactToEpochProperty(int epoch) {
        if (artefacts[epoch] == 0) {
            artefacts[epoch] = 1;
        } else {
            artefacts[epoch] = 0;
        }
    }

    public void addArousalToEpochProperty(int epoch) {
        if (arousals[epoch] == 0) {
            arousals[epoch] = 1;
        } else {
            arousals[epoch] = 0;
        }
    }

    public void addStimulationToEpochProperty(int epoch) {
        if (stimulation[epoch] == 0) {
            stimulation[epoch] = 1;
        } else {
            stimulation[epoch] = 0;
        }
    }

    public void clearProperties(int epoch) {
        stimulation[epoch] = 0;
        arousals[epoch] = 0;
        artefacts[epoch] = 0;
        labels[epoch] = 0;
    }

    /**
     * This method builds the needed string for using the svm_scale method.
     *
     * @return the features for one epoch (values for training)
     */
    public String getFeatureValuePE() {

        StringBuilder featureVector = new StringBuilder();

        if (rowPosition < numberOfFeatures) {

            // Get class label.
            featureVector.append((int) (features[rowPosition][0]));

            // Get feature values.
            for (int i = 1; i <= numberOfChannels; i++) {
                featureVector.append(" " + i + ":" + features[rowPosition][i]);
            }

            rowPosition = rowPosition + 1;

            String featureVectorString = featureVector.toString();
            return featureVectorString;

        } else {
            return null;
        }
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(int row, int column, float feature) {
        this.features[row][column] = feature;
    }

    /**
     * @param features the features to set
     */
    public void setFeatures(float[][] features) {
        this.features = features;
        numberOfFeatures = features[0].length;
    }

    /**
     * @param row the row position of the value.
     * @param channel the channel of the value.
     * @return the needed PE feature value
     */
    public float getFeature(int row, int channel) {
        return features[row][channel];
    }

    /**
     * @return the features
     */
    public float[][] getFeatures() {
        return features;
    }

    /**
     * Set the scored class label for a feature vector in the matrix.
     *
     * @param row the number of scored feature value.
     * @param label the class label to set.
     */
    public void setLabel(int row, int label) {
        labels[row] = label;
    }

    public void setLabels(int[] labels) {
        this.labels = labels;
    }
    
    public int[] getLabels() {
        return labels;
    }

    public void setArtefacts(int[] artefacts) {
        this.artefacts = artefacts;
    }
    
    public int[] getArtefacts() {
        return artefacts;
    }
    
    public void setArousals(int[] arousals) {
        this.arousals = arousals;
    }
    
    public int[] getArousals() {
        return arousals;
    }
    
    public void setStimulation(int[] stimulation) {
        this.stimulation = stimulation;
    }
    
    public int[] getStimulation() {
        return stimulation;
    }
    /**
     * Get the scored class label for a feature vector in the matrix.
     *
     * @param row the epoch of scored feature value.
     */
    public int getLabel(int row) {
        return labels[row];
    }

    /**
     * Rewind the row in data matrics. This is needed in scaling of the data.
     */
    public void rewindRowPosition() {
        rowPosition = 0;
    }

    /**
     * Set the number of the epoch from which the PE has been calculated.
     *
     * @param value number of the epoch
     */
//    public void setNumberOfcalculatedEpoch(int value) {
//        numberOfcalculatedEpoch.;
//    }
    /**
     * @return	the number of the epoch from which the PE has been calculated.
     */
//    public int getNumberOfcalculatedEpoch() {
//        return numberOfcalculatedEpoch;
//    }
    /**
     * @return	the number of feature vectors in the matrix.
     */
    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    /**
     * @return	the number of channels for the input data.
     */
    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    /**
     * @return	if the TrainController has finished reading and calculating.
     */
    public boolean getReadingAndCalculatingDone() {
        return readingAndCalculatingDone;
    }

    /**
     * Set the status of the readingAndCalculatingDone Flag.
     *
     * @param status true, if the TrainController has finished reading and
     * calculating.
     */
    public void setReadingAndCalculatingDone(boolean status) {
        readingAndCalculatingDone = status;
    }

    /**
     * Set the statis of the classificationDone Flag.
     *
     * @param status true, if SVM Controller has finished the classification
     */
    public void setClassificationDone(boolean status) {
        classificationDone = status;
    }

    /**
     * @return	the status flag, if the SVM Controller has finished
     * classification.
     */
    public boolean isClassificationDone() {
        return classificationDone;
    }

    /**
     * @param row the needed epoch (row) from the matrix.
     * @return	the feature vector from needed epoch (row).
     */
    public float[] getFeatureVector(int row) {
        return features[row];
    }

    /**
     * @return the lengthOfOneEpoch
     */
    public int getLengthOfOneEpoch() {
        return lengthOfOneEpoch;
    }

    /**
     * @param lengthOfOneEpoch the lengthOfOneEpoch to set
     */
    public void setLengthOfOneEpoch(int lengthOfOneEpoch) {
        this.lengthOfOneEpoch = lengthOfOneEpoch;
    }

    /**
     * @return the numberOfEpochs
     */
    public int getNumberOfEpochs() {
        return numberOfEpochs;
    }

    /**
     * @param numberOfEpochs the numberOfEpochs to set
     */
    public void setNumberOfEpochs(int numberOfEpochs) {
        this.numberOfEpochs = numberOfEpochs;
    }

    /**
     * @return the channelNames, which have been calculated.
     */
    public LinkedList<ChannelNames> getChannelName() {
        return channelNames;
    }

    /**
     * @param channelName the channelName, which have been calculated.
     */
    public void setChannelName(ChannelNames channelName) {
        channelNames.add(channelName);
    }

    /**
     * Set the array with the predict probabilities for the current epoch.
     *
     * @param row the epoch for the predict probabilities.
     * @param predictProbability the array with the predict probabilities.
     */
    public void setPredictProbabilities(int row, double[] predictProbability) {
        predictProbabilities[row] = predictProbability;
    }

    /**
     * Get the array with the predict probabilities for the current epoch.
     *
     * @param row the epoch for the predict probabilities.
     * @return
     */
    public double[] getPredictProbabilities(int row) {
        return predictProbabilities[row];
    }

    /**
     * @return the countWake
     */
    public int getCountWake() {
        return countWake;
    }

    /**
     * @return the countS1
     */
    public int getCountS1() {
        return countS1;
    }

    /**
     * @return the countS2
     */
    public int getCountS2() {
        return countS2;
    }

    /**
     * @return the countN
     */
    public int getCountN() {
        return countN;
    }

    /**
     * @return the countREM
     */
    public int getCountREM() {
        return countREM;
    }

    /**
     * @return the countA
     */
    public int getCountA() {
        return countA;
    }

    /**
     * @return the countMA
     */
    public int getCountMA() {
        return countMA;
    }

    /**
     * @return the countS
     */
    public int getCountS() {
        return countS;
    }

    /**
     * @return the selectedModel
     */
    public String getSelectedModel() {
        return selectedModel;
    }

    /**
     * @param selectedModel the selectedModel to set
     */
    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }

    /**
     * @return the epochProperties
     */
    public HashMap<Integer, Integer[]> getEpochProperties() {
        return epochProperties;
    }

    /**
     * @param epochProperties the epochProperties to set
     */
    public void setEpochProperties(HashMap<Integer, Integer[]> epochProperties) {
        this.epochProperties = epochProperties;
    }

    /**
     * @return the predictProbabilities
     */
    public double[][] getPredictProbabilities() {
        return predictProbabilities;
    }

    /**
     * @param predictProbabilities the predictProbabilities to set
     */
    public void setPredictProbabilities(double[][] predictProbabilities) {
        this.predictProbabilities = predictProbabilities;
    }

    /**
     * @return the channelNames
     */
    public LinkedList<ChannelNames> getChannelNames() {
        return channelNames;
    }

    /**
     * @param channelNames the channelNames to set
     */
    public void setChannelNames(LinkedList<ChannelNames> channelNames) {
        this.channelNames = channelNames;
    }

    /**
     * @param countWake the countWake to set
     */
    public void setCountWake(int countWake) {
        this.countWake = countWake;
    }

    /**
     * @param countS1 the countS1 to set
     */
    public void setCountS1(int countS1) {
        this.countS1 = countS1;
    }

    /**
     * @param countS2 the countS2 to set
     */
    public void setCountS2(int countS2) {
        this.countS2 = countS2;
    }

    /**
     * @param countN the countN to set
     */
    public void setCountN(int countN) {
        this.countN = countN;
    }

    /**
     * @param countREM the countREM to set
     */
    public void setCountREM(int countREM) {
        this.countREM = countREM;
    }

    /**
     * @param countA the countA to set
     */
    public void setCountA(int countA) {
        this.countA = countA;
    }

    /**
     * @param countMA the countMA to set
     */
    public void setCountMA(int countMA) {
        this.countMA = countMA;
    }

    /**
     * @param countS the countS to set
     */
    public void setCountS(int countS) {
        this.countS = countS;
    }

    /**
     * @param numberOfChannels the numberOfChannels to set
     */
    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    /**
     * @return the autoMode
     */
    public boolean isAutoMode() {
        return autoMode;
    }

    /**
     * @param autoMode the autoMode to set
     */
    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    /**
     * @return the fileLocation
     */
    public File getFileLocation() {
        return fileLocation;
    }

    /**
     * @param fileLocation the fileLocation to set
     */
    public void setFileLocation(File fileLocation) {
        this.fileLocation = fileLocation;
    }

    public boolean isFeaturesComputed() {
        return featuresComputed;
    }

    public boolean isReadinDone() {
        return readinDone;
    }

    public boolean isTsneComputed() {
        return tsneComputed;
    }

    public void setReadinDone(boolean readinDone) {
        this.readinDone = readinDone;
    }

    public void setFeaturesComputed(boolean featuresComputed) {
        this.featuresComputed = featuresComputed;
    }

    public void setTsneComputed(boolean tsneComputed) {
        this.tsneComputed = tsneComputed;
    }

    public double[][] getTsneFeatures() {
        return tsneFeatures;
    }

    public void setTsneFeatures(double[][] tsneFeatures) {
        this.tsneFeatures = tsneFeatures;
    }

    public int getFeatureChannel() {
        return featureChannel;
    }

    public void setFeatureChannel(int featureChannel) {
        this.featureChannel = featureChannel;
    }

    public void setHighpassCoefficients(FilterCoefficients highpassCoefficients) {
        this.highpassCoefficients = highpassCoefficients;
    }

    public FilterCoefficients getHighpassCoefficients() {
        return highpassCoefficients;
    }

    public void setLowpassCoefficients(FilterCoefficients lowpassCoefficients) {
        this.lowpassCoefficients = lowpassCoefficients;
    }

    public FilterCoefficients getLowpassCoefficients() {
        return lowpassCoefficients;
    }

    public int getArousal(int i) {
        return arousals[i];
    }

    public int getStimulation(int i) {
        return stimulation[i];
    }

    public int getArtefact(int i) {
        return artefacts[i];
    }

    public void setArousal(int i, int a) {
        arousals[i] = a;
    }

    public void setStimulation(int i, int a) {
        stimulation[i] = a;
    }

    public void setArtefact(int i, int a) {
        artefacts[i] = a;
    }
}
