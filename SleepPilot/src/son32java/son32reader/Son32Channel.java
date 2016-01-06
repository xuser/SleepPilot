/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package son32java.son32reader;

import java.util.HashMap; 
import java.util.Map; 

/**
 * This class is a model for a single Son32 channel. The Son32Reader will contian
 * a reference to all channels.
 * @author Matthias Steffen
 */
public class Son32Channel {
    /**
     * This enum represents the relation between the channel type and the
     * short value it is represented by. It is the java equivalent of the
     * TDataKind from the son32.h
     */
    public static enum ChannelKind{
        ChanOff((short)0),            /* the channel is OFF - */
        Adc((short)1),                /* a 16-bit waveform channel */
        EventFall((short)2),          /* Event times (falling edges) */
        EventRise((short)3),          /* Event times (rising edges) */
        EventBoth((short)4),          /* Event times (both edges) */
        Marker((short)5),             /* Event time plus 4 8-bit codes */
        AdcMark((short)6),            /* Marker plus Adc waveform data */
        RealMark((short)7),           /* Marker plus float numbers */
        TextMark((short)8),           /* Marker plus text string */
        RealWave((short)9);           /* waveform of float numbers */
        
        private short channelCode;
        
        /**
         * Channel code is returned by Son32Reader.SONChanKind().
         * @param channelCode 
         */
        ChannelKind(short channelCode) {
            this.channelCode = channelCode;
        }
        /*
        Create a hashmap so that we can get the constants from the enum by the
        short value.
        */
        final static Map<Short, ChannelKind> map = new HashMap<>();

        static {
           for (ChannelKind channelKind : ChannelKind.values()) {
              map.put(channelKind.channelCode, channelKind);
           }
        }
        /**
         * Get the sting description for the channel by the channel code.
         * @param channelCode
         * @return 
         */
        public static ChannelKind getByChannelCode(short channelCode) {
           return map.get(channelCode);
        }
        
        /**
         * Get the channel code from the enum constant.
         * @return 
         */
        public short getChannelCode(){
            return this.channelCode;
        }
        
        /**
         * Get the description of the channel kind.
         * @return 
         */
        public String getChannelKindDescription(){
            return this.name();
        }
    }
    
    private final Son32Reader parentReader;
    private final int channelNumber;
    private final ChannelKind channelKind;
    private final String title;
    private final long channelDivide;
    private final long chanMaxTime;
    private final int blocks;
    //the time between two data point in 1e-6 sec
    public final double samplingIntervallMs;
    public final double samplingRateHz;
    
    public Son32Channel(Son32Reader reader, int channelNumber,
            short channelKind, String title, long chanDiv, long chanMaxTime,
            int blocks){
        this.parentReader = reader;
        this.channelNumber = channelNumber;
        this.channelKind = ChannelKind.getByChannelCode(channelKind);
        this.title = title;
        this.channelDivide = chanDiv;
        this.chanMaxTime = chanMaxTime;
        this.blocks = blocks;

        this.samplingIntervallMs = this.parentReader.getUsPerTime()*1e6
                *this.parentReader.getTimeBase();
        this.samplingRateHz = 1/(this.channelDivide*1e-6*
                this.samplingIntervallMs);
    }
    
    /**
     * This methode will calculate the size of an array that can be filled with
     * all data point from the given intervall. The intervall is given in sconds
     * @param intervall The duration of the time intervall
     * @return The optimal array size.
     */
    public int calculateArraySizeByTime(double intervall){
        double timePerConversion = channelDivide
                *this.parentReader.getTimeBase()
                *this.parentReader.getUsPerTime();
        //make sure to stay IN the intervall
        return (int)Math.floor(intervall/timePerConversion);
    }
    
    /**
    * This function reads contiguous waveform data from this channel. It is
    * supposed to be used intern and workf with clock tick as time unit.
     * between two set times.
     * @param max The maximum number of data point to be returned
     * @param sTime The strat time in clock ticks.
     * @param eTime The end time in clock ticks.
     * @param target he target array for the data.
     */
    private void getRealData(long max, int sTime, int eTime, float[] target){
        this.parentReader.SONGetRealData((short)this.channelNumber, max,
                sTime, eTime, target);
    }
    
    /**
     * Get data for this channel form the .smr file. No data after sTime in
     * seconds will be returned. The number of data points returned is equal
     * to the length of the target array, which will be compltly filled.
     * @param sTime No data before this starting time in second will be returned.
     * @param target The target array for the data.
     */
    public void getRealDataByDP(double sTime, float target[]){
        int sTimeInCT = (int)this.parentReader.getCTFromSec(sTime);
        double timePerConversionInSec = channelDivide
                *this.parentReader.getTimeBase()
                *this.parentReader.getUsPerTime();
        int eTimeInCT = (int)(sTimeInCT + 
                this.parentReader.getCTFromSec(timePerConversionInSec*target.length));
        this.getRealData(target.length, sTimeInCT, eTimeInCT, target);
        
    }
    
    /**
     * Get data for this channel from the .smr file. All data from the given 
     * time intervall will be returned. Make sure the traget array can store
     * all data points. The methode will fetch as much data point, as the
     * array can store.
     * @param sTime The start time of the intervall.
     * @param eTime The end time of the intervall.
     * @param target The target array for the data.
     */
    public void getRealDataByTime(double sTime, double eTime, float[] target){
        int sTimeInCT = (int)this.parentReader.getCTFromSec(sTime);
        int eTimeInCT = (int)this.parentReader.getCTFromSec(eTime);
        this.getRealData(target.length, sTimeInCT, eTimeInCT, target);
    }
    
    /**
     * Get the enum object that decribes the kind of this channel.
     * @return
     */
    public ChannelKind getChannelKind(){
        return this.channelKind;
    }
    
    /**
     * Get the title of this channel.
     * @return 
     */
    public String getChannelTitle(){
        return this.title;
    }
    
    /**
     * @return The cannel divide value.
     */
    public long getChannelDivide(){
        return this.channelDivide;
    }
    
    /**
     * @return The max time for this channel.
     */
    public long getChanMaxTime(){
        return this.chanMaxTime;
    }
    
    /**
     * Returns the number of blocks for this channel.
     * @return 
     */
    public int getBlocks(){
        return this.blocks;
    }
    
    /**
     * @return The sampling intervall im ms
     */
    public double getSamplingIntervallMs(){
        return this.samplingIntervallMs;
    }
    /**
     * @return The sampling rate for this channel in Hz
     */
    public double getSamplingRateHz(){
        return this.samplingRateHz;
    }    
}

/**
 * Some more code that is not used atm, but could be usefull
 * 
 * this.numberOfEpochs = (int)Math.ceil((this.parentReader.getTimeBase()
                *this.parentReader.getUsPerTime()*this.chanMaxTime)/30);
                * 

    public double[][] getEpoch(int epochNumber) 
            throws son32Exceptions.NoEpochException{
        if(epochNumber < 1 || epochNumber > this.numberOfEpochs){
            throw new son32Exceptions.NoEpochException(this.channelNumber,
                    epochNumber);
        }
        double timePerConversion = channelDivide*this.parentReader.getTimeBase()
                *this.parentReader.getUsPerTime();
        long max = (long)Math.ceil(30/timePerConversion);
* 
//   *this.parentReader.getUsPerTime());
        return new double[1][1];
    }
 */