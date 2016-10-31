package cin.ufpe.br.Interfaces;

import android.content.Context;

import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface Cascade {
    CascadeClassifier loadCascade(int alg, String algorithm, Context mContext) throws Exception;
}
