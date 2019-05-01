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
package br.ufc.mdcc.mpos.net.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufc.mdcc.mpos.config.ProfileNetwork;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.persistence.ProfileNetworkDao;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import br.ufpe.cin.mpos.DaoLocal.DatabaseController;
import br.ufpe.cin.mpos.profile.Model.Model;
import br.ufpe.cin.mpos.profile.ProfilesTask;

/**
 * This class control the profile services
 * 
 * @author Philipp B. Costa
 */
public final class ProfileController {
    public String CPU_Nuvem = "-1";
    Model model = new Model();
    ServerContent server = null;
    private TaskResult<Network> taskResultEvent;
    private Context mContext;
    private ProfileNetworkDao profileDao;
    private ProfileNetworkTask taskNetwork;
    private ProfilesTask taskProfiles;
    private ProfileNetwork profileNetwork;
    private DatabaseController dc;
    private Model rawModel = new Model();
    private String bandwidthDown="-1";
    private String bandwidthUp="-1";

    private TaskResultAdapter<Model> taskResultAdapter = new TaskResultAdapter<Model>() {
        @Override
        public void completedTask(Model obj) {
            Log.d("CarrierInfo", "Apos a tarefa: " + obj.Carrier);
            model = obj;
            bandwidthDown = "0";
            bandwidthUp = "0";
            if(server != null) {
                try {
                    if (profileNetwork == ProfileNetwork.LIGHT) {
                        taskNetwork = new ProfileNetworkLight(persistNetworkResults(taskResultEvent), server);
                    } else if (profileNetwork == ProfileNetwork.DEFAULT) {
                        taskNetwork = new ProfileNetworkDefault(persistNetworkResults(taskResultEvent), server);
                    } else if (profileNetwork == ProfileNetwork.FULL) {
                        taskNetwork = new ProfileNetworkFull(persistNetworkResults(taskResultEvent), server);
                    } else {
                        taskNetwork = new ProfileNetworkFull(persistNetworkResults(taskResultEvent), server);
                    }
                    taskNetwork.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {
                    Log.e("teste", "ProfileController: "+e.getMessage());
                }
            }

            //testing.run();
        }
    };

    public ProfileController(Context context, ProfileNetwork profile) {
        this.profileNetwork = profile;
        profileDao = new ProfileNetworkDao(context);
        dc = new DatabaseController(context);
        mContext = context;
        rawModel.CPU = -1 + "";
        rawModel.CPUNuvem = -1 + "";
        rawModel.Bandwidth = -1 + "";
        Log.i(ProfileController.class.getName(), "MpOS Profile Started!");
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public Model getRawModel() {
        return rawModel;
    }

    public float getCPUNuvem(){
        try{
            return Float.parseFloat(CPU_Nuvem);
        }catch (Exception e) {
            return -2;
        }
    }

    public void setTaskResultEvent(TaskResult<Network> taskResultEvent) {
        this.taskResultEvent = taskResultEvent;
    }

    public void networkAnalysis(ServerContent server) throws MissedEventException, NetworkException {
        networkAnalysis(server, profileNetwork);
    }

    public void networkAnalysis(ServerContent server, ProfileNetwork profileNetwork) throws MissedEventException, NetworkException {
        Log.d("teste", "Vou executar as tarefas");
        this.server = server;
        Network network = new Network();
        bandwidthDown=network.getBandwidthDownload();
        bandwidthUp=network.getBandwidthUpload();
        salvaBanco();
        taskProfiles = new ProfilesTask(taskResultAdapter, mContext);
        taskProfiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String getBandwidthLabel(float f) {
        return getBandwidthLabel(f, model.Tech);
    }

    private String getBandwidthLabel(float f, String tech) {
        String resp = "";
        if(tech.equalsIgnoreCase("WIFI")) {
            Log.d("year", "year: "+model.year);
            if ("Potente".equalsIgnoreCase(model.year) || "Intermediario_Avancado".equalsIgnoreCase(model.year)) {
                Log.d("year", "entrei");
                if (f > 20) {
                    resp = "Livre";
                }else if(Math.abs(f-9.14)<0.001){
                    resp = "Incerto";
                }else if (f > 2) {
                    resp = "Mediano";
                } else {
                    resp = "Congestionado";
                }
            }else{
                if (f > 15) {
                    resp = "Livre";
                }else if(Math.abs(f-9.14)<0.001){
                    resp = "Incerto";
                }else if (f > 2) {
                    resp = "Mediano";
                } else {
                    resp = "Congestionado";
                }
            }
        } else {
            resp = "Sem limiar";
        }
        return resp;
    }

    private void salvaBanco(){
        String date = getCurrentTimeStamp();
        model.Date = date;
        model.Tech = "Wifi";
        String bandwidthLabel="";
        float down = Float.parseFloat(bandwidthDown);
        float up = Float.parseFloat(bandwidthUp);
        Log.d("rede","Download "+down);
        Log.d("rede","upload "+up);
        if(down<up){
            bandwidthLabel = getBandwidthLabel(down);
            rawModel.Bandwidth = down + "";
        }else {
            bandwidthLabel = getBandwidthLabel(up);
            rawModel.Bandwidth = up + "";
        }

        model.Bandwidth = bandwidthLabel;
        ProfilesTask pt = new ProfilesTask(null, null);
        model.CPUNuvem = pt.getCPULabel(Float.parseFloat(CPU_Nuvem)).name();
        rawModel.CPUNuvem = CPU_Nuvem;
        Log.d("csvTest", "CPUNumvem label: " + rawModel.CPUNuvem);
        rawModel.CPU = pt.getCPUStatistic() + "";
        if (model.AppName != null) dc.insertData(model);
        bandwidthDown="-1";
        bandwidthUp="-1";
        Log.d("finalizado", "Finalizado:\n" + model.toString());

    }

    private String formatString(String s){
        String resp = s;
        int ponto = s.indexOf('.');
        if(ponto>=0&&ponto+3<=s.length()){
            resp = s.substring(0,ponto+3);
        }

        return resp;
    }

    private TaskResult<Network> persistNetworkResults(final TaskResult<Network> interceptedResults) {
        // interception pattern
        return new TaskResultAdapter<Network>() {
            @Override
            public void completedTask(final Network network) {
                if (network != null) {
                    // local persistence
                    profileDao.add(network);
                    bandwidthDown = formatString(network.getBandwidthDownload());
                    bandwidthUp = formatString(network.getBandwidthUpload());
                    salvaBanco();
                }
                interceptedResults.completedTask(network);
            }

            @Override
            public void taskOnGoing(int completed) {
                interceptedResults.taskOnGoing(completed);
            }
        };
    }

    public void destroy() {
        if (taskNetwork != null) {
            taskNetwork.halt();
            taskNetwork = null;
        }
        profileNetwork = null;
    }
}