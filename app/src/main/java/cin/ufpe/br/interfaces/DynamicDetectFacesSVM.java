package cin.ufpe.br.interfaces;

import org.opencv.core.MatOfRect;

import java.util.List;

import br.ufpe.cin.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 13/06/2017.
 */

public interface DynamicDetectFacesSVM extends DetectFaces {

    @Remotable(value = Remotable.Offload.DYNAMIC, status = true, classifier = Remotable.Classifier.SVM)
    PropriedadesFace detectarFaces(String cascadeClassifier, byte[] originalImage);

    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);

}
