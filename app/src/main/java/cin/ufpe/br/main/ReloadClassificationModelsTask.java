package cin.ufpe.br.main;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cin.ufpe.br.interfaces.CloudletUpdateService;

public class ReloadClassificationModelsTask extends AsyncTask<Void, Void, Void> {

    UpdatedCallBack callBack;
    CloudletUpdateService updateService;
    HashMap<String, byte[]> classificators;
    Context context;

    ReloadClassificationModelsTask(Context context, CloudletUpdateService updateService, UpdatedCallBack callBack) {
        this.callBack = callBack;
        this.updateService = updateService;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("teste","reload start");

        //todo:get_data_from_db
        classificators = updateService.updateClassificators(new String[0]);

        if (classificators!=null) {
            for(String filename : classificators.keySet()) {
                //save files on storage
                try {
                    Log.d("teste", "filename:" + filename);
                    FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    byte[] file = classificators.get(filename);
                    if (file != null) out.write(file);
                    out.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        Log.d("teste","offload end");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("teste","callback");
        callBack.onCompleted(classificators!=null);
    }
}
