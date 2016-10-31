package cin.ufpe.br.service;

import android.content.Context;

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
            InputStream is = mContext.getResources().openRawResource(alg);
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, algorithm);
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            CascadeClassifier cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeClassifier.load(mCascadeFile.getAbsolutePath());
            return  cascadeClassifier;
    }

}
