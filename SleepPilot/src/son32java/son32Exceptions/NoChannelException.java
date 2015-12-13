/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package son32java.son32Exceptions;

/**
 *
 * @author Matthias Steffen
 */
public class NoChannelException extends Exception {
    public NoChannelException(){}
    
    public NoChannelException(int channelNumber){
        super("Attempt to access channel number " + 
                    channelNumber + "! There is no channel with this number!");
    }
}
