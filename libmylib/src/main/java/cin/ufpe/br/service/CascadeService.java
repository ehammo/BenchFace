package cin.ufpe.br.service;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by eduardo on 31/10/2016.
 */

public class CascadeService {

    public CascadeClassifier loadCascade(String algorithm) throws Exception{
       System.out.print("Creating Cascade Classifier");
       CascadeClassifier cascadeClassifier = new CascadeClassifier(System.getProperty("user.dir") + "\\" +algorithm+".xml");
       return  cascadeClassifier;
    }
}
