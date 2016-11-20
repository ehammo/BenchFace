package cin.ufpe.br.Interfaces;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import br.ufc.mdcc.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletDetectFaces extends DetectFaces {


    @Remotable(value = Remotable.Offload.STATIC, status = true)
    MatOfRect detectarFaces(String c, byte[] mat);

    List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
