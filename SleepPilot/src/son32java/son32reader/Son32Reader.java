/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package son32java.son32reader;

import son32java.son32Exceptions.*;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import java.lang.ArrayIndexOutOfBoundsException;


/**
 * This class will contain an instance of the Son32JavaInterface, which contains
 * all the son32.dll functions in abstract form. The interface maps the function
 * signature to the actual implementation in the C code.
 * One of the reasons, why this class exists is to detect errros. The C functions
 * just return int error codes. In this class the methodes will check for this
 * error codes and throw the corresponding java exceptions.
 * @author Matthias steffen
 */
public class Son32Reader {
    private final static Son32JavaInterface INSTANCE = Son32JavaInterface.INSTANCE;
    private short fileHandle;
    private final Son32Channel[] channels;
    private final int numberOfChannels;
    private final double timeBase;
    private final short usPerTime;
    
    /**
     * 
     * @param path
     * @param mode 
     */
    public Son32Reader(String path, int mode){
        try{
            this.fileHandle = this.SONOpenOldFile(path, mode);
        } catch(SonNoFileException|SonOutOfMemoryException|SonNoAccessException
                |SonReadOnlyException ex){
           System.out.println(ex);
           System.exit(1);
        }
        //System.out.println("FH: "+this.fileHandle);
        this.numberOfChannels = this.SONMaxChans();
        this.channels = new Son32Channel[this.numberOfChannels];
        this.timeBase = this.SONTimeBase(0.0);
        this.usPerTime = this.SONGetusPerTime(this.fileHandle);
        this.initializeAllChannels();
    }
    
    /**
     * This mehode is used to perform a setup for all channels of the .smr file.
     * The channel objects a stored in an array. Each channel stores his own
     * channel specific informations and a reference to its parent Son32Reader
     * instance.
     */
    private void initializeAllChannels(){
        for(int i=0;i<this.numberOfChannels;i++){
            short chanKind = this.SONChanKind((short)i);
            String title = this.SONGetChanTitle(this.fileHandle, (short)i);
            long chanDiv = this.SONChanDivide(this.fileHandle, (short)i);
            long chanMaxTime = this.SONChanMaxTime(this.fileHandle,(short) i);
            int blocks = this.SONBlocks(this.fileHandle, (short)i);
            this.channels[i] = new Son32Channel(this, i, chanKind, title,
                    chanDiv, chanMaxTime, blocks);
        }
    }
    
    //*********************Methodes of the Son32 library************************
     /**
     * Opens a .smr file.
     * @param path Path to the .smr file
     * @param mode File open mode. 0 write/read 1 read 2 attempt write/read
     * @throws SonNoFileException
     * @throws SonOutOfMemoryException
     * @throws SonNoAccessException
     * @return fh Returns a file decriptor. If an error occured this is negative.
     */
    private short SONOpenOldFile(String path, int mode) throws SonNoFileException,
            SonOutOfMemoryException, SonNoAccessException, SonReadOnlyException{
        short fh = INSTANCE.SONOpenOldFile(path, mode);
        switch(fh){
            case -1: throw new SonNoFileException(path);
            case -4: throw new SonNoAccessException(path);
            case -8: throw new SonOutOfMemoryException();
            case -21: throw new SonReadOnlyException(path);
        }
        return fh;
    }
    
    // SON_NO_FILE SON_BAD_READ SON_BAD_WRITE
    public void SONCloseFile(){
        INSTANCE.SONCloseFile(this.fileHandle);
    }
    
     /**
     * Returns the version of the opened .smr file.
     * @param fh The file handle to the .smr file
     * @return Verion of the .smr file
     * @throws SonNoFileException
     */
    private int SONGetVersion(short fh) throws SonNoFileException{
        if(fh < 0){
            throw new SonNoFileException("");
        }
        int version = INSTANCE.SONGetVersion(fh);
        if(version == -1){
            throw new SonNoFileException("");
        }
        else{
            return version;
        }
    }
    
    /**
     * Returns the number of chancels for this .smr file.
     * @return maxChannels The number of channels for the .smr file.
     */
    private int SONMaxChans(){
        return INSTANCE.SONMaxChans(this.fileHandle);
    }
    
    /**
     * Returns the base time unit of the .smr file.
     * @param newTimeBase Sets a new time base, ignored if <= 0.9
     * @return The time base unit of the .smr file.
     */
    private double SONTimeBase(double newTimeBase){
        return INSTANCE.SONTimeBase(this.fileHandle, newTimeBase);
    }
    
    /**
     * This function returns the file clock tick interval in base time units as defined by 
     * SONTimeBase()
     * @param fh File descriptor for the .smr file.
     * @return The number of base time units in the clock tick interval.
     */
    private short SONGetusPerTime(short fh){
        return INSTANCE.SONGetusPerTime(fh);
    }
    
    /**
     * Returns the chanel kind for the given chanel, of this .smr file.
     * @param chan The channel number (counting starts with 0).
     * @return The channel kind code for the channel. 
     */
    private short SONChanKind(short chan){
        return INSTANCE.SONChanKind(this.fileHandle, chan);
    }
    
    /**
     * Get the title of the given channel.
     * @param fh The file handle to the .smr file.
     * @param chan The number of the channel (counting starts with 0).
     * @return The channel title
     */
    private String SONGetChanTitle(short fh, short chan){
        //allocate 10 bytes for the char array, the constant (maximum)
        //SON_TITLESZ is 9 allocate 1 additional byte for the c specific
        //string terminator \0
        Memory pcTitle = new Memory(10);
        INSTANCE.SONGetChanTitle(this.fileHandle, chan, pcTitle);
        String title = pcTitle.getString((long)0);
        return title;
    }
    
    /**
     * Returns the number of data blocks for the channel that are on disk or 0
     * if any error
     * @param fh The file handle to the .smr file.
     * @param chan The number of the channel (counting starts with 0).
     * @return The number of blocks for this channel
     */
    private int SONBlocks(short fh, short chan){
        return INSTANCE.SONBlocks(fh, chan);
    }
    
    /**
     * Returns  the interval, in clock ticks, between waveform conversions on
     * this channel. If this is not a waveform channel, then the return 
     * value is 1. 
     * This function takes care of the mapping between the C long type (32bit)
     * and the Java long type (64bit).
     * @param fh The file descriptor for the .smr file
     * @param chan The number of the channel (counting starts with 0). 
     * @return Interval for waveform conversion in clock ticks.
     */
    private long SONChanDivide(short fh, short chan){
        return INSTANCE.SONChanDivide(fh, chan).longValue();
    }
    
    /**
     * Returns the last time value for this channel in clock ticks.
      * @param fh The file descriptor for the .smr file
     * @param chan The number of the channel (counting starts with 0). 
     * @return The last time value for the channel.
     */
    private long SONChanMaxTime(short fh, int chan){
        return INSTANCE.SONChanMaxTime(fh, chan).longValue();
    }
    
    /**
     * This function reads contiguous waveform data from the given channel 
     * between two set times.
     * @param chan The channel number (counting starts with 0).
     * @param max The maximum number of data point to be returned
     * @param sTime The strat time in clock ticks.
     * @param eTime The end time in clock ticks.
     * @param target The target array for the data
     */
     public void SONGetRealData(short chan, long max, int sTime,
             int eTime, float[] target) throws ArrayIndexOutOfBoundsException{
        if(target.length < max){
            throw new ArrayIndexOutOfBoundsException("Error reading data from"
                    + " the file into the target array! Make sure the array"
                    + " length is at least equal to the number of data points"
                    + " you want to get!");
        }
         //allocate 32bits(4bytes) for each data point
        Memory pFloatMem = new Memory(max*4);
        pFloatMem.clear();
        //allocate 4bytes for the first returned time
        Memory pbTimeMem = new Memory(4);
        pbTimeMem.clear();
        //convert Java long type to NativeLong, so that jna can take care
        //of appropriate mapping to  C native long type
        NativeLong maxNL = new NativeLong(max);
        NativeLong sTimeNL = new NativeLong(sTime);
        NativeLong eTiemeNL = new NativeLong(eTime);
        
        NativeLong numberOfDataPointsNL = INSTANCE.SONGetRealData(this.fileHandle,
                chan, pFloatMem, maxNL, sTimeNL, eTiemeNL, pbTimeMem, null);
        //convert NativeLong type back to Java long
//        long pbTime = pbTimeMem.getInt((long)0);
        long numberOfDataPoints = numberOfDataPointsNL.longValue();
        
//        long channelDivide = this.channels[chan].getChannelDivide();
        //calculate the base unit of the x axis scale, every x value is a
        //multiple of this value
//        double timePerConversion = channelDivide*this.timeBase*this.usPerTime;
        for(int i=0;i<numberOfDataPoints;i++){
//            double time = timePerConversion*i;
            target[i] = pFloatMem.getFloat((long)4*i);
            //System.out.println(dataValue);
        }
     }
    
    //***********************converter methodes*********************/
   
    /**
     * Calculate the time in clock ticks from a time given in seconds
     * @param timeInSec
     * @return The time in clock ticks
     */
    public long getCTFromSec(double timeInSec){
        return (long)(timeInSec/(this.timeBase*this.usPerTime));
    } 
    
    public double getSecFromCT(long timeInCT){
        return (double)(timeInCT*this.timeBase*this.usPerTime);
    }
    //*******************Getter methodes for the private fields****/
   
    /**
     * @return The number of channels for the Son32Reader instance.
     */
    public int getNumberOfChannels(){
        return this.numberOfChannels;
    }
    
    /**
     * Returns a channel from the .smr file by the given channel number.
     * Note: Counting starts witch channel number 0 !
     * @param channelNumber
     * @return Returns one channel of the .smr file.
     * @throws NoChannelException 
     */
    public Son32Channel getChannel(int channelNumber) throws NoChannelException{
        if(channelNumber > this.numberOfChannels){
            throw new NoChannelException(channelNumber);
        } else{
            return this.channels[channelNumber];
        }
    }
    /**
     * @return The time base for the Son32Reader instance.
     */
    public double getTimeBase(){
        return this.timeBase;
    }
    
    /**
     * @return The number of time base units in the clock tick interval.
     */
    public short getUsPerTime(){
        return this.usPerTime;
    }
}
