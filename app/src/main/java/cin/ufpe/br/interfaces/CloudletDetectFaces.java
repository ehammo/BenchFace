package cin.ufpe.br.interfaces;

import org.opencv.core.MatOfRect;

import java.util.List;

import br.ufpe.cin.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletDetectFaces extends DetectFaces {

    @Remotable(value = Remotable.Offload.STATIC, status = true)
    PropriedadesFace detectarFaces(String cascadeClassifier, byte[] originalImage);

    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
