package model;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This class is the respective model for the FeatureExtractionController.
 *
 * @author Nils Finke
 *
 */
public class FeatureModel implements Serializable {

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
    private float[] kcPercentage;

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

    /**
     * Predict probabilities for the different classes. Each row keeps one array
     * with the probabilities for the epoch with the rowindex.
     */
    private double[][] predictProbabilities;

    /**
     * If automode is true, then the classification automation was used.
     */
    private boolean autoMode = false;

    private String selectedModel;

    /**
     * Path to the eeg data file (Brainvision, Spike2, EDF etc.).
     */
    private File dataFileLocation;

    /**
     * Path to the SleepPilot project file.
     */
    private File projectFile;

    /**
     * Keeps the number of feature vectors.
     */
    private int numberOfFeatures;

    /**
     * Keeps the number of channels from input epochList.
     */
    private int numberOfChannels;

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
     * Tells whether channel epochList has been read
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

    private int featureChannel;

    private int currentEpoch;

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
        if (features != null) {
            numberOfFeatures = features[0].length;
        }

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
     * @return	the number of feature vectors in the matrix.
     */
    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    /**
     * @return	the number of channels for the input epochList.
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
     * @return the dataFileLocation
     */
    public File getDataFileLocation() {
        return dataFileLocation;
    }

    /**
     * @param dataFileLocation the dataFileLocation to set
     */
    public void setDataFileLocation(File dataFileLocation) {
        this.dataFileLocation = dataFileLocation;
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

    public void setCurrentEpoch(int currentEpoch) {
        this.currentEpoch = currentEpoch;
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }

    public float[] getKcPercentage() {
        return kcPercentage;
    }

    public void setKcPercentage(float[] kcPercentage) {
        this.kcPercentage = kcPercentage;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public File getProjectFile() {
        return projectFile;
    }

}
