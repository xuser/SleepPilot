/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package son32java.son32reader;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

/**
 *This interface represents the "map" between the son32.cll (c code) and the
 * Son32Reader (java code). All used methodes are declared here according to their
 * signature in the son32.h header file. The interface can not be implemented, because
 * this would force to override the methodes.
 * @author Matthias Steffen
 */
public interface Son32JavaInterface extends Library{
    /**
     * Create a Singelton instance of the interface. This instance will be used
     * to make all function calls.
     */
//    public Son32JavaInterface INSTANCE = (Son32JavaInterface)Native.loadLibrary(
//            "D:\\\\SON Library\\son32.dll", Son32JavaInterface.class);
    
    public Son32JavaInterface INSTANCE = (Son32JavaInterface)Native.loadLibrary(
            "son32.dll", Son32JavaInterface.class);
    
     /**
     * Opens a .smr file.
     * @param path Path to the .smr file
     * @param mode File open mode. 0 write/read 1 read 2 attempt write/read
     * @return fh Returns a file decriptor. If an error occured this is negative.
     */
    short SONOpenOldFile(String path, int mode);
    
    /**
     * This function is used to close a file that has been opened for
     * reading or for writing
     * @param fh The file handle
     * @return  The function returns 0 if all went well or an error code if
     * the file close operation failed. 
     */
    short SONCloseFile(short fh);
    
    /**
     * Returns the version of the opened .smr file.  The function returns the
     * file version of -1 if no file is open. 
     * @param fh The file handle to the .smr file
     * @return Verion of the .smr file
     */
    int SONGetVersion(short fh);
    
    /**
     * Returns the number of channels for the .smf file. This can be between
     * 32 and 451.
     * @param fh The file handle to the .smr file
     * @return The number of channels for the .smr file.
     */
    int SONMaxChans(short fh);
    
    /**
     *  This function gets and/or sets the base time units for the file (default
     * is 1.0E-6 seconds).
     * @param fh The file handle to the .smr file
     * @param dTB New value for the time base, ignored if <=0.0
     * @return The base time unit of the .smr file.
     */
    double SONTimeBase(short fh, double dTB);

    /**
     * This function returns the file clock tick interval in base time units as defined by 
     * SONTimeBase()
     * @param fh File descriptor for the .smr file.
     * @return The number of base time units in the clock tick interval.
     */
    short SONGetusPerTime(short fh);
    
    /**
     * Returns  the interval, in clock ticks, between waveform conversions on
     * this channel. If this is not a waveform channel, then the return 
     * value is 1. 
     * @param fh The file descriptor for the .smr file
     * @param chan The number of the channel (counting starts with 0). 
     * @return Interval for waveform conversion in clock ticks.
     */
    NativeLong SONChanDivide(short fh, short chan);
    
    /**
     * Returns the kind of the given chanel as a short. The mapping between the
     * short value and the string description of the chanel takes place in a 
     * separate enum.
     * @param fh The file handle to the .smr file.
     * @param chan The number of the channel (counting starts with 0).
     * @return The code for the channel kind
     */
    short SONChanKind(short fh, short chan);
    
    /**
     * This function will return the channel title for the given channel.
     * @param fh The file handle to the .smr file.
     * @param chan The number of the channel (counting starts with 0).
     * @param pcTitle Pointer to a char array to hold the returned title
     */
    void SONGetChanTitle(short fh, short chan, Memory pcTitle);
    
    /**
     * Returns the number of data blocks for the channel that are on disk or 0
     * if any error
     * @param fh The file handle to the .smr file.
     * @param chan The number of the channel (counting starts with 0).
     * @return The number of blocks for this channel
     */
    int SONBlocks(short fh, short chan);
    
    /**
     * Returns the last time value for this channel in clock ticks.
     * @param fh The file descriptor for the .smr file
     * @param chan The number of the channel (counting starts with 0). 
     * @return The last time value for the channel.
     */
    NativeLong SONChanMaxTime(short fh, int chan); 
    
    /**
     * This function reads contiguous waveform data from the given channel 
     * between two set times.
     * @param fh The file descriptor for the .smr file
     * @param chan The number of the channel (counting starts with 0). 
     * @param pFloat Points to the area of memory in which any data 
     *               found is to be returned.
     * @param max The max number of data point that may be returned.
     * @param sTime The strat time in clock ticks.
     * @param eTime The end time in clock ticks.
     * @param pbTime The time of the first returned data point in clock ticks.
     * @param pFltMask Filter for AdcMark channels, ignored if null.
     * @return The number of returned data points.
     */
    NativeLong SONGetRealData(short fh, short chan, Memory pFloat, NativeLong max,
                NativeLong sTime, NativeLong eTime, Memory pbTime, Memory pFltMask);
}