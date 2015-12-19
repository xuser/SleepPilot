package controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.DataModel;
import model.DataModel.Reader;
import ru.mipt.edf.EDFParser;
import ru.mipt.edf.EDFParserException;
import ru.mipt.edf.EDFParserResult;

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
        this.file=file;
        dataModel = new DataModel();
        dataModel.setFile(file);

        init();
    }


    public void readAll(int channel) {
        dataModel.rawEpochs.clear();
        for (int i = 0; i < dataModel.getNumberOf30sEpochs(); i++) {
            double[] target = new double[dataModel.numberOfSamplesForOneEpoch];
            dataModel.addRawEpoch(read(channel, i, target));
        }
    }

    public double[] read(int channel, int epoch, double[] target) {
        return reader.read(channel, epoch, target);
    }

    public final void init() {
        if(file.getName().toLowerCase().endsWith(".edf")){
            EDFParserResult edfResult = null;
            try{
                String path = file.getPath();
                InputStream is = new BufferedInputStream(
                        new FileInputStream(new File(path)));
                 edfResult = EDFParser.parseEDF(is);          
            }catch(FileNotFoundException|EDFParserException e){
                System.out.println(e);
                //return due to failed file open
                return;
            }
            int numberOfChannels = edfResult.getHeader().getNumberOfChannels();
            /*
            Subtract 1 from the number of channels, because the edf parser
            adds an extra channel for annotation, which does not contain
            actual data.
            */
            dataModel.setNbchan(numberOfChannels - 1);
            
            String[] channelNames = edfResult.getHeader().getChannelLabels();
            //remove the annotation channel
            Arrays.copyOf(channelNames, channelNames.length-1);
            dataModel.setChannelNames(channelNames);
            
            int numberOfSamples = edfResult.getHeader().getNumberOfSamples()[0];
            double duration = edfResult.getHeader().getDurationOfRecords();
            
            double samplingRate = numberOfSamples/duration;
            dataModel.setSrate(samplingRate);
            dataModel.setPnts(numberOfSamples);
            
            int numberOfSamplesForOneEpoch = (int)(samplingRate*30);
            dataModel.numberOfSamplesForOneEpoch = numberOfSamplesForOneEpoch;
            dataModel.rawEpoch = new double[numberOfChannels-1][numberOfSamplesForOneEpoch];
            
            reader = new DataModel.Reader() {
                EDFParserResult edfResult = setEDFParserResult();
                double[] buffer = new double[dataModel.numberOfSamplesForOneEpoch];

                private EDFParserResult setEDFParserResult(){
                    EDFParserResult edfResult = null;
                    try{
                        String path = file.getPath();
                        InputStream is = new BufferedInputStream(
                                new FileInputStream(new File(path)));
                         edfResult = EDFParser.parseEDF(is); 
                         return edfResult;
                    }catch(FileNotFoundException|EDFParserException e){
                        System.out.println(e);
                        //return due to failed file open
                        return null;
                    }
                }
                @Override
                public void close() {
                   return;
                }
                
                @Override
                public double[] read(int channel, int epoch, double[] target) {
                    double sTime = epoch*30;
                    double eTime = (epoch+1)*30;                 
                    
                    double intervall = eTime - sTime;
                    if(target.length < (int)Math.floor(intervall*samplingRate)){
                        System.out.println("Target array is to small!");
                        return target;
                    }
                    int lowerBound = (int)Math.floor(sTime*samplingRate);
                    int upperBound = (int)Math.floor(eTime*samplingRate);
                    short[] data = edfResult.getSignal().getDigitalValues()[channel];
                    
                    /*
                    Here we got an error. The epoch number 0 just loads fine.
                    However if we want to skip to the next epoch usin the right
                    arrow key in the gui, the parameter "epoch" is -1. This
                    causes an array index out of bounds exception.
                    */
                    System.out.println("epoch: "+epoch);
                    
                    
                    for(int i=0;i<(upperBound-lowerBound);i++){
                        //just return the requested data
                        target[i] = (double)data[lowerBound+i];
                    }

                    return target;
                }
            };
        }
        else if (file.getName().toLowerCase().endsWith(".smr")) {

            Son32Reader smr = new Son32Reader(file.getPath(), 1);
            /**
             * Read necessary information from SMR reader
             */
            Son32Channel son32Channel = null;
            dataModel.setNbchan(smr.getNumberOfChannels());
            try {
                String[] channelNames = new String[dataModel.getNbchan()];
                for (int i = 0; i < dataModel.getNbchan(); i++) {
                    channelNames[i] = smr.getChannel(i).getChannelTitle();
                }
                dataModel.setChannelNames(channelNames);

                son32Channel = smr.getChannel(0);
                dataModel.setSrate((int) son32Channel.getSamplingRateHz());

                //get the last time for the channel in clock ticks
                long maxTime = son32Channel.getChanMaxTime();
                //convert the max time from clock ticks to sec
                maxTime = (long) smr.getSecFromCT(maxTime);
                dataModel.setPnts((int) (maxTime * dataModel.getSrate()));

                dataModel.numberOfSamplesForOneEpoch = son32Channel.calculateArraySizeByTime(30);
                dataModel.rawEpoch = new double[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

            } catch (NoChannelException ex) {
                System.out.println(ex);
            }

            reader = new DataModel.Reader() {
                Son32Channel son32Channel;
                double[] buffer = new double[dataModel.numberOfSamplesForOneEpoch];

                @Override
                public void close() {
                    smr.SONCloseFile();
                }

                @Override
                public double[] read(int channel, int epoch, double[] target) {
                    try {
                        System.out.println("epoch: "+epoch);
                        son32Channel = smr.getChannel(channel);
                        double sTime = epoch*30;
                        double eTime = (epoch+1)*30;
                        son32Channel.getRealDataByTime(sTime, eTime, buffer);
//                for (int x = 0; x < newTarget.length; x++) {
//                    //System.out.println(newTarget[x]);
//                }
                        System.arraycopy(buffer, 0, target, 0, target.length);
                        return target;
                    } catch (NoChannelException ex) {
                        Logger.getLogger(DataController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    return target;
                }
            };

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
            dataModel.rawEpoch = new double[dataModel.getNbchan()][dataModel.numberOfSamplesForOneEpoch];

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
                public double[] read(int channel, int epoch, double[] target) {
                    int from = (int) (epoch * dataModel.numberOfSamplesForOneEpoch);
                    int to = (int) (from + dataModel.numberOfSamplesForOneEpoch);
                    bv.readDataFile(channel, from, to);
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
