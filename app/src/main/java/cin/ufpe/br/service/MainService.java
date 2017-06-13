package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.text.DecimalFormat;
import java.util.List;

import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import cin.ufpe.br.Interfaces.DetectFaces;
import cin.ufpe.br.main.MainActivity;
import cin.ufpe.br.model.PropriedadesFace;
import cin.ufpe.br.model.ToLoadCascadeModel;

/**
 * Created by eduardo on 31/10/2016.
 */

public final class MainService extends AsyncTask<Void, String, Bitmap> {

    public int faces;
    Bitmap originalImage;
    DetectFaces serviceExtractFaces;
    BlurImageService serviceBlur;
    CutImageService serviceCrop;
    OverlayService serviceOverlay;
    CascadeClassifier cascadeClassifier;
    TaskResultAdapter taskResultAdapter;
    Bitmap result;
    ToLoadCascadeModel model;
    private String TAG = "teste";
    private DecimalFormat precision = new DecimalFormat("0.0000");

    public MainService(Bitmap originalImage, TaskResultAdapter taskAdapter) {
        this.originalImage=originalImage;
        this.serviceBlur=new BlurImageService();
        this.serviceCrop=new CutImageService();
        this.serviceExtractFaces=new DetectFacesService();
        this.serviceOverlay = new OverlayService();
        this.cascadeClassifier = MainActivity.cascadeClassifier;
        taskResultAdapter=taskAdapter;
    }

    protected Bitmap doInBackground(Void... params) {
        try{
            result = begin();
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Bitmap result) {
        taskResultAdapter.completedTask(result);
    }

    public Bitmap begin() throws Exception{
        Log.d(TAG, "\nRunning FaceDetector");
        Mat mat = new Mat();
        Utils.bitmapToMat(originalImage, mat);
        List<PropriedadesFace> propsFaces=null;
        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
        propsFaces = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);
        Log.d(TAG, "faces detected "+propsFaces.size());

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
