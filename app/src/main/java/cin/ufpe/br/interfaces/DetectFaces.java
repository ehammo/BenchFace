package cin.ufpe.br.interfaces;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface DetectFaces {
    PropriedadesFace detectarFaces(String cascadeClassifier, byte[] originalImage);
    List<PropriedadesFace> detectarFaces(CascadeClassifier cascadeClassifier, Mat mat);
    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
