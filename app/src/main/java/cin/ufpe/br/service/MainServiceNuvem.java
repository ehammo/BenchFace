package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Mat;

import java.text.DecimalFormat;
import java.util.List;

import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import cin.ufpe.br.Interfaces.DetectFaces;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public final class MainServiceNuvem extends AsyncTask<Void, String, Bitmap> {

    public int faces;
    byte[] originalImage;
    DetectFaces serviceExtractFaces;
    BlurImageService serviceBlur;
    CutImageService serviceCrop;
    OverlayService serviceOverlay;
    String cascadeClassifier;
    TaskResultAdapter taskResultAdapter;
    Bitmap result;
    private String TAG = "teste";
    private DecimalFormat precision = new DecimalFormat("0.0000");

    public MainServiceNuvem(byte[] originalImage, DetectFaces detectFaces, String algorithm, TaskResultAdapter taskAdapter) {
        this.originalImage=originalImage;
        this.serviceBlur=new BlurImageService();
        this.serviceCrop=new CutImageService();
        this.serviceExtractFaces = detectFaces;
        this.serviceOverlay = new OverlayService();
        this.cascadeClassifier = algorithm;
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
        List<PropriedadesFace> propsFaces=null;

        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
        PropriedadesFace b = serviceExtractFaces.detectarFaces(this.cascadeClassifier, this.originalImage);
        faces = b.getFaces();
        Bitmap ret = BitmapFactory.decodeByteArray(b.getImagemFinal(),0,b.getImagemFinal().length);
        return ret;
    }

    public int getNumFaces(){
        return faces;
    }

}
