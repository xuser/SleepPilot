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
public class NoEpochException extends Exception {
    public NoEpochException(){}
    
    public NoEpochException(int channelNumber, int epochNumber){
        super("The channel " + channelNumber + " does not contain an "
                + "epoch with the number: " + epochNumber);
    }
}
