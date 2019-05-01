package cin.ufpe.br.service;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cin.ufpe.br.interfaces.CloudletUpdateService;

public class UpdateService implements CloudletUpdateService {

    @Override
    public HashMap<String, byte[]> updateClassificators(String[] newInstances) {
        Log.d("teste", "local");
        return null;
    }
}
