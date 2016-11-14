package cin.ufpe.br.Interfaces;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import cin.ufpe.br.Util.Input;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface DetectFaces {

    MatOfRect detectarFaces(Input i);
    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
