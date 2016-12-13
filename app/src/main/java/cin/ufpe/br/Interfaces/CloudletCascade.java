package cin.ufpe.br.Interfaces;

import android.content.Context;

import org.opencv.objdetect.CascadeClassifier;

import br.ufpe.cin.mpos.offload.Remotable;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletCascade extends Cascade {

    //@Remotable(value = Remotable.Offload.STATIC, status = true)
    CascadeClassifier loadCascade(int alg, String algorithm, Context mContext) throws Exception;
}
