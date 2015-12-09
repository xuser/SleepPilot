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
public class SonOutOfMemoryException extends Exception {
   
    public SonOutOfMemoryException(){
        super("The System could not allocate enough memory");
    }
}
