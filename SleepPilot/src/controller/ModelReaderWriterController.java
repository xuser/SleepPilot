/*
 * Copyright (C) 2016 Arne Weigenand
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.FeatureModel;

/**
 * This controller is used to write the current system state to hard disk.
 *
 * @author Arne Weigenand
 * @author Nils Finke
 */
public class ModelReaderWriterController extends Thread {

    private Thread t;

    private FeatureModel featureModel;
    private File file;

    /**
     * True, if Writing or False, if Reading
     */
    private boolean readWriteFlag;

    private ObjectOutputStream oos = null;
    private FileOutputStream fos = null;

    private ObjectInputStream ois = null;
    private FileInputStream fis = null;

    public ModelReaderWriterController(FeatureModel featureModel, File file, boolean readWriteFlag) {

        this.featureModel = featureModel;
        this.file = file;
        this.readWriteFlag = readWriteFlag;

    }

    /**
     * Read or write model to hard disk depending on the readWriteFlag.
     */
    public void run() {

        //WRITING
        if (readWriteFlag) {
            try {
                fos = new FileOutputStream(file);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(featureModel);

            } catch (IOException e) {
                System.err.println("Error occured during saving models!");
                e.printStackTrace();
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        System.err.println("Error occured during closing OOS!");
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        System.err.println("Error occured during closing FOS!");

                    }
                }
            }

            System.out.println("Finished writing model on hard disk.");

            // READING
        } else {

            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                Object obj = ois.readObject();

                if (obj instanceof FeatureModel) {

                    FeatureModel fem = (FeatureModel) obj;
                    fem.setProjectFile(file);
                    MainController.setFeatureModel(fem);
                    MainController.recreateSystemState(file);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        System.err.println("Error occured during closing OIS!");
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        System.err.println("Error occured during closing FIS!");
                    }
                }
            }

            System.out.println("Finished reading model from hard disk.");
        }
    }

    /**
     * This method starts the Data Read/Write Thread.
     */
    public void start() {
        System.out.println("Starting Data Reader/Writer Thread");
        if (t == null) {
            t = new Thread(this, "DataReadWrite");
            t.start();
        }
    }

}
