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
public class SonNoAccessException extends Exception {
    public SonNoAccessException(){}
    
    public SonNoAccessException(String path){
        super("Access to the file " + path +
                " was denied! Check for insufficient privileges.");
    }
}
