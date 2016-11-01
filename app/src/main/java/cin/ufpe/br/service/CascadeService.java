package cin.ufpe.br.service;

import android.content.Context;
import android.util.Log;

import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cin.ufpe.br.Interfaces.CloudletCascade;

/**
 * Created by eduardo on 31/10/2016.
 */

public class CascadeService implements CloudletCascade {

    public CascadeClassifier loadCascade(int alg,String algorithm, Context mContext) throws Exception{
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
