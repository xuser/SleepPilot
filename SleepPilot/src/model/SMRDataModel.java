/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import son32java.son32reader.Son32Reader;

/**
 *
 * @author matthias
 */
public class SMRDataModel extends RawDataModel{
    private final Son32Reader reader;
    public SMRDataModel(String path, int mode){
        super();
        this.reader = new Son32Reader(path, mode);
    }
}
