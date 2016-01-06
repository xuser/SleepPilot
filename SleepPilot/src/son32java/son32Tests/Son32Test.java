/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package son32java.son32Tests;

import java.nio.file.Path;
import java.nio.file.Paths;
import son32java.son32reader.Son32Reader;
import son32java.son32reader.Son32Channel;
import son32java.son32Exceptions.*;

/**
 * Atm, this is the place for simple manual unit and implemenation test.
 * In the future proper testing should be added.
 * @author Matthias Steffen
 */
public class Son32Test {
    public static void main(String args[]){
        //String path = System.getProperty("user.dir") + "\\test_data\\chan1_1sec.smr";
        String path = "C:\\Users\\matthias\\Documents\\NetBeansProjects\\Son32Java\\test_data\\sample01.smr";
        Son32Reader reader = new Son32Reader(path, 2);
        try{
            Son32Channel channel = reader.getChannel(3);
            int x = channel.calculateArraySizeByTime(30);
            float[] target = new float[x];
            channel.getRealDataByTime(0, 30, target);
            channel.getRealDataByTime(90, 120, target);
                     
        } catch(Exception e){
            System.out.println(e);
        }
        reader.SONCloseFile();
      
//        long  duration = timeEpochLoading(reader);
//        System.out.format("It took %d ms to fetch ~30sec of data.%n",duration);
    }
    
    public static void printAllChannleKinds(Son32Reader reader){
        try{
            for(int i=0;i<reader.getNumberOfChannels();i++){
                System.out.println("Channel " + i + ": " + reader.getChannel(i).getChannelKind());
            }
        } catch(NoChannelException ex){
            System.out.println(ex);
        }
    }
    
    public static void printChannelData(float[] data){
        for(int i=0;i<data.length;i++){
            double val = data[i];
            System.out.format("%.6f%n",val);
        }
    }
    
    public static double timeRealDataLoading(Son32Reader reader, long intervall, 
            boolean print){
        try{
            Son32Channel channel = reader.getChannel(0);
            float[] target = new float[channel.calculateArraySizeByTime(1)];
            long sTime = System.nanoTime();
            channel.getRealDataByTime(0, intervall, target);
            long eTime = System.nanoTime();
            //time is in s 1e-9 divide by 1e-3 => 1e-6
            double duration = (eTime - sTime)/1000;
            if(print){
                printChannelData(target);
            }
            System.out.format("It took %f2 us to fetch one data for "
                    + "%d seconds.%n",duration, intervall);
            return duration;
        } catch(Exception ex){
            System.out.println(ex);
            System.exit(1);
            return 0;
        }
    }
}
