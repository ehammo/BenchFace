package cin.ufpe.br.Interfaces;

import org.opencv.core.MatOfRect;

import java.io.File;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletDetectFaces extends DetectFaces {
    MatOfRect detectarFaces(String cascadeClassifier, byte[] originalImageBtes);

   // List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
