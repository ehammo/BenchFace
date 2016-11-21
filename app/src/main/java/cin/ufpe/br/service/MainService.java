package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.util.List;

import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import cin.ufpe.br.Interfaces.BlurImage;
import cin.ufpe.br.Interfaces.CloudletDetectFaces;
import cin.ufpe.br.Interfaces.CutImage;
import cin.ufpe.br.Interfaces.DetectFaces;
import cin.ufpe.br.Interfaces.Overlay;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public final class MainService extends AsyncTask<Void, String, Bitmap> {

    private String TAG = "teste";
    public int faces;
    Bitmap originalImage;
    DetectFaces serviceExtractFaces;
    BlurImage serviceBlur;
    CutImage serviceCrop;
    Overlay serviceOverlay;
    CascadeClassifier cascadeClassifier;
    TaskResultAdapter taskResultAdapter;
    Bitmap result;

    public MainService(Bitmap originalImage, cin.ufpe.br.Interfaces.DetectFaces serviceExtractFaces, BlurImage serviceBlur, CutImage serviceCrop, Overlay serviceOverlay, CascadeClassifier cascadeClassifier, TaskResultAdapter taskAdapter){
        this.originalImage=originalImage;
        this.serviceBlur=serviceBlur;
        this.serviceCrop=serviceCrop;
        this.serviceExtractFaces=serviceExtractFaces;
        this.serviceOverlay = serviceOverlay;
        this.cascadeClassifier=cascadeClassifier;
        taskResultAdapter=taskAdapter;
    }

    protected Bitmap doInBackground(Void... params) {
        result=begin();
        return result;
    }

    protected void onPostExecute(Bitmap result) {
        taskResultAdapter.completedTask(result);
    }

    public Bitmap begin(){
        Log.d(TAG, "\nRunning FaceDetector");
        Mat mat = new Mat();
        Utils.bitmapToMat(originalImage, mat);
        //mat = Utils.loadResource(mContext,R.drawable.facedetection_13_5mp);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.originalImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        MatOfRect matOfRect = new MatOfRect();
        List<PropriedadesFace> propsFaces=null;
        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
        if(serviceExtractFaces instanceof CloudletDetectFaces) {
            propsFaces = serviceExtractFaces.detectarFaces("haarcascade_frontalface_alt_tree", byteArray);
            Log.d(TAG, "faces detected1 "+propsFaces.size());
        }else {
            matOfRect = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);
            Log.d(TAG, "faces detected2 "+matOfRect.toArray().length);
        }
        Log.d(TAG, "faces detected3 "+propsFaces.size());

        //desfoca a imagem
        Bitmap imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

        //corta os rostos da imagem desfocada,
       // serviceCrop = new CutImageService();
        propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

        serviceOverlay = new OverlayService();

        //"cola" os rostos desfocados sobre a imagem original
        imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);
        Log.d("teste","deu tudo certo");
        Log.d("teste","qtd de faces: "+propsFaces.size());
        faces = propsFaces.size();
        return imagemCorteDesfoque;
    }

    public int getNumFaces(){
        return faces;
    }

}
