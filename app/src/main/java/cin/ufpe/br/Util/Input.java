package cin.ufpe.br.Util;

import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.io.Serializable;

/**
 * Created by eduardo on 14/11/2016.
 */

public class Input implements Serializable {
    private static final long serialVersionUID = -3076977522780308455L;

    private CascadeClassifier cascadeClassifier;
    private Mat mat;

    public Input(CascadeClassifier cc, Mat mat){
        cascadeClassifier = cc;
        this.mat=mat;
    }

    public CascadeClassifier getCascadeClassifier(){
        return cascadeClassifier;
    }

    public Mat getMat(){
        return mat;
    }


}
