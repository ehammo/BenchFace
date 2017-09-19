package cin.ufpe.br.Interfaces;

import org.opencv.core.MatOfRect;

import java.util.List;

import br.ufpe.cin.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 13/06/2017.
 */

public interface DynamicDetectFacesJRIP extends DetectFaces {

    @Remotable(value = Remotable.Offload.DYNAMIC, status = true, classifier = Remotable.Classifier.JRIP)
    PropriedadesFace detectarFaces(String cascadeClassifier, byte[] originalImage);

    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);

}
