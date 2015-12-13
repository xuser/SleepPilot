/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Arne
 */
public class AsciiReader {
    private File dataFileForAscii;
    
        // [ASCII Infos]
    private int skipLines = 0;
    private int skipColumns = 0;
        /**
     * Reads the given ascii file and prints it on hard disk.
     *
     * @param dataFile
     */
//    private void readDataFileAscii(File dataFile) {
//        try {
//            /* ---- Start just for testing ---- */
////			File file = new File("/Users/Nils/Desktop/Ascii Werte.txt");
////			FileWriter writer = new FileWriter(file, true);
//			/* ---- End just for testing ---- */
//
//            BufferedReader in = new BufferedReader(new FileReader(dataFile));
//            String zeile = null;
//
//            // This function has to be called here, because you now know how big the matrix have to be
//            dataModel.createDataMatrix();
//
//            for (int i = 0; i < skipLines; i++) {
//                zeile = in.readLine();
//            }
//
//            while ((zeile = in.readLine()) != null) {
//
//                String[] tmp = zeile.split(" ");
//
//                for (int i = 0; i < tmp.length; i++) {
//
//                    tmp[i] = tmp[i].replaceAll(",", ".");
//
//                    /* ---- Start just for testing ---- */
////					writer.write(Double.valueOf(tmp[i]) + " ");
////					writer.flush();
//					/* ---- End just for testing ---- */
//                    dataModel.setDataPoints(Double.valueOf(tmp[i]), row, i);
//
//                }
//
//                row = row + 1;
//                dataModel.setRowInSampleFile(row);
//
//                /* ---- Start just for testing ---- */
////				writer.write(System.getProperty("line.separator"));
////				writer.flush();
//				/* ---- End just for testing ---- */
//            }
//
//            in.close();
////			writer.close();		// Just for testing
//
//        } catch (FileNotFoundException e) {
//            System.err.println("No file found on current location.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    
}
