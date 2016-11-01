package cin.ufpe.br.Interfaces;

import android.graphics.Bitmap;

import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletMain extends MainInterface {
    int getNumFaces();

    Bitmap start(Bitmap originalImage, cin.ufpe.br.Interfaces.DetectFaces serviceExtractFaces, BlurImage serviceBlur, CutImage serviceCrop, Overlay serviceOverlay, CascadeClassifier cascadeClassifier);
}
