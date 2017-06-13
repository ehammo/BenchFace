package cin.ufpe.br.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cin.ufpe.br.main.R;
import cin.ufpe.br.model.ToLoadCascadeModel;

/**
 * Created by eduardo on 31/10/2016.
 */

public class CascadeService extends Activity{

        Context mContext;
        int alg;
        String algorithm;


    public CascadeClassifier loadCascade(String algorithm)throws Exception{
        switch (algorithm){
            case "haarcascade_frontalface_alt_tree":
                mContext = getApplicationContext();
                alg = R.raw.haarcascade_frontalface_alt_tree;
                break;
            case "haarcascade_frontalface_alt":
                mContext = getApplicationContext();
                alg = R.raw.haarcascade_frontalface_alt;
                break;
            case "haarcascade_frontalface_alt2":
                mContext = getApplicationContext();
                alg = R.raw.haarcascade_frontalface_alt2;
                break;
            case "haarcascade_frontalface_default":
                mContext = getApplicationContext();
                alg = R.raw.haarcascade_frontalface_default;
                break;
        }
        return loadCascade(new ToLoadCascadeModel(mContext,alg,algorithm));
    }

    public CascadeClassifier loadCascade(ToLoadCascadeModel model) throws Exception{
            mContext = model.getmContext();
            alg = model.getElementIndex();
            algorithm = model.getAlgorithm();
            Log.d("teste","entrei no cascade");
            InputStream is = mContext.getResources().openRawResource(alg);
            Log.d("teste","peguei o recurso");
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, algorithm);
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            Log.d("teste","trabalhando o arquivo");
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            Log.d("teste","criando classifier");
            CascadeClassifier cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            Log.d("teste","dando load no classifier");
            cascadeClassifier.load(mCascadeFile.getAbsolutePath());
            return  cascadeClassifier;
    }


}
