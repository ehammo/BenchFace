package cin.ufpe.br.Interfaces;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface DetectFaces {

 //   MatOfRect detectarFaces(CascadeClassifier cascadeClassifier, byte[] originalImage);
        List<PropriedadesFace> detectarFaces(String cascadeClassifier, byte[] originalImage);
//    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
