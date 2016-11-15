package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import cin.ufpe.br.Interfaces.BlurImage;
import cin.ufpe.br.Interfaces.CutImage;
import cin.ufpe.br.Interfaces.DetectFaces;
import cin.ufpe.br.Interfaces.Overlay;
import cin.ufpe.br.Util.Input;
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
        MatOfRect matOfRect = serviceExtractFaces.detectarFaces(new Input(cascadeClassifier, mat));

        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
        List<PropriedadesFace> propsFaces = serviceExtractFaces.obterDadosFaces(matOfRect);

        //desfoca a imagem
        Bitmap imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

        //corta os rostos da imagem desfocada,
       // serviceCrop = new CutImageService();
        byte[] z = serviceCrop.soma(1,1);
        Log.d(TAG, ""+z);
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
