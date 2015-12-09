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
public class SonReadOnlyException extends Exception {
    public SonReadOnlyException(){}
    
    public SonReadOnlyException(String path){
        super("Attempt to write to a file in read only mode! File: " + path + 
                " Make sure no other programms is accessing this file at the"
                + "same time and that the file exists!");
    }
}
