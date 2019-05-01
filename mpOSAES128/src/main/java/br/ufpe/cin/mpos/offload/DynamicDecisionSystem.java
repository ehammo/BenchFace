/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufpe.cin.mpos.offload;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;

import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufpe.cin.mpos.DaoLocal.DatabaseController;
import br.ufpe.cin.mpos.DaoLocal.DatabaseManager;
import br.ufpe.cin.mpos.profile.ResultTypes;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


/**
 * This implementation made decision about local or remote execution,
 * based in profile results (ping tcp).
 * 
 * @author Philipp B. Costa
 */
public class DynamicDecisionSystem extends TimerTask {
    private final String clsName = DynamicDecisionSystem.class.getName();
    private final Object mutex = new Object();
    private final long PING_TOLERANCE = 50;//ms
    private Classifier classifier;
    private String classifierModel = "j48.model";
    private ServerContent server;
    private Context mContext;
    private DatabaseController dc;
    // private ProfileNetworkDAO profileDao;

    public DynamicDecisionSystem(Context context, ServerContent server) {
        dc = new DatabaseController(context);
        mContext = context.getApplicationContext();
        setServer(server);
        // profileDao = new ProfileNetworkDAO(context);
    }

    private void loadClassifier(Remotable.Classifier classifierRemotable) throws IOException, ClassNotFoundException {
        File directory = mContext.getFilesDir();
        File file = new File(directory, classifierRemotable.toString());
        Log.d("classificacao","File_path="+file.getPath());
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file.getPath());
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            this.classifier = (Classifier) objectIn.readObject();
        } catch (FileNotFoundException e) {
            Log.d("classificacao","Loaded from assets");
            ObjectInputStream objectInputStream = new
                    ObjectInputStream(mContext.getAssets().open(classifierRemotable.toString()));
            this.classifier = (Classifier) objectInputStream.readObject();
        }
    }

    public ServerContent getServer() {
        synchronized (mutex) {
            return server != null ? server.newInstance() : null; //immutable operation
        }
    }

    public synchronized void setServer(ServerContent server) {
        synchronized (mutex) {
            this.server = server;
        }
    }

    public synchronized boolean isRemoteAdvantage(int InputSize, Remotable.Classifier classifierRemotable) {
        boolean resp = false;
        try {
            if ((!(this.classifierModel.equals(classifierRemotable.toString()))) || this.classifier == null) {
                Log.d("classificacao", "classificador=" + classifierRemotable.toString());
                this.classifierModel = classifierRemotable.toString();
                loadClassifier(classifierRemotable);
            }
            Cursor c = dc.getData();
            Log.d("classificacao", "getData");
            int colunas = c.getColumnCount();
            Instance instance = new DenseInstance(colunas-2);
            ArrayList<String> values = new ArrayList<String>();
            ArrayList<Attribute> atts = new ArrayList<Attribute>();
            String id = null;
            ContentValues newValues = new ContentValues();
            if(c.moveToFirst()) {
                id = c.getString(0);
                for (int i = 1; i <= colunas - 2; i++) {
                    String feature = c.getColumnName(i);
                    String value = c.getString(i);
                    Log.d("classificacao", feature + ": " + value);
                    Attribute attribute = null;
                    if (feature.equals(DatabaseManager.InputSize)) {
                        value = "" + InputSize;
                        attribute = new Attribute(DatabaseManager.InputSize);
                        newValues.put(DatabaseManager.InputSize, value);
                    } else if (value != null) {
                        String[] strings = populateAttributes(feature);
                        ArrayList<String> attValues = new ArrayList<String>(Arrays.asList(strings));
                        attribute = new Attribute(feature, attValues);
                    }
                    if (value != null && attribute != null) {
                        values.add(value);
                        atts.add(attribute);
                    }
                }
                Instances instances = new Instances("header",atts,atts.size());
                instances.setClassIndex(instances.numAttributes()-1);
                instance.setDataset(instances);
                for (int i = 0; i < atts.size(); i++) {
                    if (i == 9) {
                        instance.setMissing(atts.get(9));
                        Log.d("classificacao", "missing");
                        break;
                    }
                    if (atts.get(i).name().equals(DatabaseManager.InputSize)) {
                        instance.setValue(atts.get(i), InputSize);
                    } else {
                        instance.setValue(atts.get(i),values.get(i));
                    }
                }
                double value = -1;
                Log.d("classificacao", "classify");
                double[] teste = classifier.distributionForInstance(instance);
                Log.d("classificacao", "teste = " + teste.length);
                value = teste[0];
                Log.d("classificacao", instance.toString() + " classifiquei com o seguinte valor" + value);
                resp = (0.7 <= value);
                newValues.put(DatabaseManager.result, resp);
                dc.updateData(id, newValues);
                if (resp) {
                    Log.d("classificacao", "sim");
                    Log.d("Finalizado", "classifiquei " + instance.toString() + " com sim");
                } else {
                    Log.d("classificacao", "nao");
                    Log.d("Finalizado", "classifiquei " + instance.toString() + " com nao");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("sqlLite", e.getMessage());
            Log.e("classificacao", e.getMessage());
            Log.e("sqlLite", "Causa: "+e.getCause());
        }
        return resp;
    }

    private String[] populateAttributes(String feature){
        switch (feature){
            case DatabaseManager.AppName:
                return Arrays.toString(ResultTypes.ResultTypesApps.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.year:
                return Arrays.toString(ResultTypes.ResultTypesPhone.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.Battery:
                return Arrays.toString(ResultTypes.ResultTypesBateria.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.CPU:
                return Arrays.toString(ResultTypes.ResultTypesCpu.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.Tech:
                return Arrays.toString(ResultTypes.ResultTypesRede.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.Bandwidth:
                return Arrays.toString(ResultTypes.ResultTypesLarguraRede.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.RSSI:
                return Arrays.toString(ResultTypes.ResultTypesRSSI.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.CPUNuvem:
                return Arrays.toString(ResultTypes.ResultTypesCpuNuvem.values()).replaceAll("^.|.$", "").split(", ");
            case DatabaseManager.result:
                return Arrays.toString(ResultTypes.ResultTypesResult.values()).replaceAll("^.|.$", "").split(", ");
        }
        return null;
    }

    @Override
    public void run() {
        try {
            ServerContent server = getServer();
            if (server != null) {
                Log.d("teste","Calling analysis");
                MposFramework.getInstance().getProfileController().networkAnalysis(server);
            }else{
                Log.i(clsName, "Waiting for new endpoint...");
            }
        } catch (MissedEventException e) {
            Log.e(clsName, "Forgot the event?", e);
        } catch (NetworkException e) {
            Log.w(clsName, e);
        }
    }
}