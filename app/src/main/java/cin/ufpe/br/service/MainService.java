package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import cin.ufpe.br.Interfaces.BlurImage;
import cin.ufpe.br.Interfaces.CloudletMain;
import cin.ufpe.br.Interfaces.CutImage;
import cin.ufpe.br.Interfaces.Overlay;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public class MainService implements CloudletMain{

    private String TAG = "teste";
    public int faces;

    public Bitmap start(Bitmap originalImage, cin.ufpe.br.Interfaces.DetectFaces serviceExtractFaces, BlurImage serviceBlur, CutImage serviceCrop, Overlay serviceOverlay, CascadeClassifier cascadeClassifier){
        Log.d(TAG, "\nRunning FaceDetector");
        Mat mat = new Mat();
        Utils.bitmapToMat(originalImage, mat);
        //mat = Utils.loadResource(mContext,R.drawable.facedetection_13_5mp);
        serviceExtractFaces = new DetectFacesService();
        MatOfRect matOfRect = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);

        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
        List<PropriedadesFace> propsFaces = serviceExtractFaces.obterDadosFaces(matOfRect);

        //desfoca a imagem
        serviceBlur = new BlurImageService();

        Bitmap imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

        //corta os rostos da imagem desfocada,
        serviceCrop = new CutImageService();
        propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

        serviceOverlay = new OverlayService();

        //"cola" os rostos desfocados sobre a imagem original
        imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);
        faces = propsFaces.size();
        return imagemCorteDesfoque;
    }

}
