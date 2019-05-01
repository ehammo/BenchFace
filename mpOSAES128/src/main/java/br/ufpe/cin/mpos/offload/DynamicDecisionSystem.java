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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;

import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
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

    private TaskResultAdapter<Network> event = new TaskResultAdapter<Network>() {
        @Override
        public void completedTask(Network network) {
            if (network != null) {
                network.generatingPingTcpStats();

                Log.i(clsName, "Decision Maker -> Ping max: " + network.getPingMaxTcp() + ", med: " + network.getPingMedTcp() + ", min: " + network.getPingMinTcp());
                MposFramework.getInstance().getEndpointController()
                        .setRemoteAdvantageExecution(network.getPingMedTcp() < PING_TOLERANCE);
            } else {
                setServer(null);
                MposFramework.getInstance().getEndpointController()
                        .setRemoteAdvantageExecution(false);
                Log.e(clsName, "Some problem in ping test!");
            }
        }
    };

    public DynamicDecisionSystem(Context context, ServerContent server) {
        dc = new DatabaseController(context);
        mContext = context.getApplicationContext();
        setServer(server);
        MposFramework.getInstance().getProfileController().setTaskResultEvent(event);
        // profileDao = new ProfileNetworkDAO(context);
    }

    private void loadClassifier(Remotable.Classifier classifierRemotable) throws Exception {
        File directory = mContext.getFilesDir();
        File file = new File(directory, classifierRemotable.toString());
        Log.d("classificacao","File_path="+file.getPath());
        FileInputStream fileIn = new FileInputStream(file.getPath());
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        this.classifier = (Classifier) objectIn.readObject();
        if (this.classifier == null) {
            Log.d("classificacao","Loaded from assets");
            ObjectInputStream objectInputStream = new
                    ObjectInputStream(mContext.getAssets().open(classifierRemotable.toString()));
            this.classifier = (Classifier) objectInputStream.readObject();
        } else {
            Log.d("classificacao","Loaded from storage");
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
            Log.d("classificacao", "loaded");
            Cursor c = dc.getData();
            Log.d("classificacao", "getData");
            int colunas = c.getColumnCount();
            Instance instance = new DenseInstance(colunas-2);
            ArrayList<String> values = new ArrayList<String>();
            ArrayList<Attribute> atts = new ArrayList<Attribute>();
            if(c.moveToFirst()) {
                Log.d("classificacao", "on db");

                for (int i = 1; i <= colunas - 2; i++) {
                    String feature = c.getColumnName(i);
                    String value = c.getString(i);
                    Log.d("classificacao", feature+": "+value);
                    Attribute attribute = null;
                    if (feature.equals(DatabaseManager.InputSize)) {
                        values.add(""+InputSize);
                        attribute = new Attribute(DatabaseManager.InputSize);
                    } else if (value != null) {
                        String[] strings = populateAttributes(feature);
                        ArrayList<String> attValues = new ArrayList<String>(Arrays.asList(strings));
                        attribute = new Attribute(feature,attValues);
                        values.add(value);
                    }
                    if (value != null && attribute != null) atts.add(attribute);
                }
                Log.d("classificacao", "new instances");
                Instances instances = new Instances("header",atts,atts.size());
                instances.setClassIndex(instances.numAttributes()-1);
                instance.setDataset(instances);
                Log.d("classificacao", "para cada atributo");
                for(int i=0;i<instances.numAttributes();i++){
                    if(i==9 || i>=atts.size()){
                        instance.setMissing(atts.get(9));
                        Log.d("classificacao", "missing");
                        break;
                    }
                    Log.d("classificacao", "i="+i+","+atts.get(i).name()+"="+values.get(i));
                    if (atts.get(i).name().equals(DatabaseManager.InputSize)) {
                        instance.setValue(atts.get(i),InputSize);
                        Log.d("classificacao", "InputSize");
                    } else {
                        instance.setValue(atts.get(i),values.get(i));
                        Log.d("classificacao", "set");
                    }
                }
                Log.d("classificacao", "setData");
                double value = -1;
                Log.d("classificacao", "classify");
                value = classifier.distributionForInstance(instance)[0];
                Log.d("classificacao", instance.toString() + " classifiquei com o seguinte valor" + value);
                resp = (0.7 <= value);
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