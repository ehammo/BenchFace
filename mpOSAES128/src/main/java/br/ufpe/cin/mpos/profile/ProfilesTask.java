package br.ufpe.cin.mpos.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import br.ufc.mdcc.mpos.util.device.DeviceController;
import br.ufpe.cin.mpos.profile.Model.Model;

/**
 * Created by eduar on 30/01/2017.
 */

public class ProfilesTask extends AsyncTask<Void, String, Model> {
    public static String[] appTypes = {"BenchFace", "BenchImage", "CollisionBalls"};
    Model result;
    Context mContext;
    TaskResultAdapter taskResultAdapter;

    public ProfilesTask(TaskResultAdapter TRA, Context context) {
        taskResultAdapter = TRA;
        mContext = context;
        result = new Model();
    }

    protected Model doInBackground(Void... params) {
        DeviceController deviceController = MposFramework.getInstance().getDeviceController();
        result.AppName = getAppLable(mContext);
        result.Battery = getBattery().name();
        result.year = getYear(deviceController.getDevice().getYear()).name();
        float cpuSmart = getCPUStatistic();
        result.CPU = getCPULabel(cpuSmart).name();
        Log.d("cpuSmart", "" + cpuSmart);
        result.RSSI = getRSSI().name();
        return result;
    }

    private ResultTypes.ResultTypesPhone getYear(int year) {
        ResultTypes.ResultTypesPhone resp;
        switch (year) {
            case 2016:
                resp = ResultTypes.ResultTypesPhone.Potente;
                break;
            case 2015:
                resp = ResultTypes.ResultTypesPhone.Intermediario_Avancado;
                break;
            case 2014:
                resp = ResultTypes.ResultTypesPhone.Intermediario;
                break;
            case 2013:
                resp = ResultTypes.ResultTypesPhone.Intermediario;
                break;
            case 2012:
                resp = ResultTypes.ResultTypesPhone.Basico;
                break;
            case 2011:
                resp = ResultTypes.ResultTypesPhone.Fraco;
                break;
            default:
                resp = ResultTypes.ResultTypesPhone.Fraco;
                break;
        }

        return resp;
    }

    private String getAppLable(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }

        if (applicationInfo != null && packageManager.getApplicationLabel(applicationInfo).toString().contains("Collision")) {
            return "CollisionBalls";
        }

        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    protected void onPostExecute(Model result) {
        taskResultAdapter.completedTask(result);
    }

    private ResultTypes.ResultTypesBateria getBattery() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
        float percentage = batteryPct * 100;

        ResultTypes.ResultTypesBateria result;
        if (percentage <= 15) {
            result = ResultTypes.ResultTypesBateria.Fraca;
        } else if (percentage > 15 && percentage <= 50) {
            result = ResultTypes.ResultTypesBateria.Razoavel;
        } else if (percentage > 50 && percentage <= 84) {
            result = ResultTypes.ResultTypesBateria.Boa;
        } else {
            result = ResultTypes.ResultTypesBateria.Forte;
        }

        return result;
    }

    public ResultTypes.ResultTypesCpu getCPULabel(float total) {
        ResultTypes.ResultTypesCpu ret;
        if (total < 45) {
            ret = ResultTypes.ResultTypesCpu.Relaxado;
        } else if (total >= 45 && total < 75) {
            ret = ResultTypes.ResultTypesCpu.Carga_Normal;
        } else if (total == (-1)) {
            ret = ResultTypes.ResultTypesCpu.Desconhecido;
        } else {
            ret = ResultTypes.ResultTypesCpu.Estressado;
        }
        return ret;
    }

    public int getCPUStatistic() {
        // nao funciona mais
//        String tempString = getCPU();
//        Log.d("CPUTask", "stringFull: "+tempString);
//        tempString = tempString.replaceAll(",", "");
//        tempString = tempString.replaceAll("User", "");
//        tempString = tempString.replaceAll("System", "");
//        tempString = tempString.replaceAll("IOW", "");
//        tempString = tempString.replaceAll("IRQ", "");
//        tempString = tempString.replaceAll("%", "");
//        for (int i = 0; i < 10; i++) {
//            tempString = tempString.replaceAll("  ", " ");
//        }
//        tempString = tempString.trim();
//        String[] myString = tempString.split(" ");
//        int total = 0;
//        int[] cpuUsageAsInt = new int[myString.length];
//        for (int i = 0; i < myString.length; i++) {
//            myString[i] = myString[i].trim();
//            cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
//            total += cpuUsageAsInt[i];
//        }
//
//        return total;
        return 55;
    }

    private String getCPU() {
        java.lang.Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            Log.e("executeTop", "error in getting first line of top");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                p.destroy();
            } catch (IOException e) {
                Log.e("executeTop",
                        "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }

    private int getRSSIWifi() {
        Log.d("teste", "WIFI");
        WifiManager wifiManager = (WifiManager) mContext
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 4;
        int rssi = 0;
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            rssi = wifiInfo.getRssi();
        }
        return WifiManager.calculateSignalLevel(rssi, numberOfLevels);
    }

    private int getRSSI4G() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (mContext.getApplicationContext().checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }
        List<CellInfo> CellInfo_list = telephonyManager.getAllCellInfo();
        int rssi = 0;

        if(CellInfo_list.get(0) instanceof CellInfoLte){
            CellInfoLte cellinfo = (CellInfoLte) CellInfo_list.get(0);
            CellSignalStrengthLte cellSignalStrength = cellinfo.getCellSignalStrength();
            rssi = cellSignalStrength.getLevel();

        }else if(CellInfo_list.get(0) instanceof CellInfoWcdma){
            CellInfoWcdma cellinfo = (CellInfoWcdma) CellInfo_list.get(0);
            CellSignalStrengthWcdma cellSignalStrength = cellinfo.getCellSignalStrength();
            rssi = cellSignalStrength.getLevel();

        } else if (CellInfo_list.get(0) instanceof CellInfoCdma) {
            CellInfoCdma cellinfo = (CellInfoCdma) CellInfo_list.get(0);
            CellSignalStrengthCdma cellSignalStrength = cellinfo.getCellSignalStrength();
            rssi = cellSignalStrength.getLevel();

        } else if (CellInfo_list.get(0) instanceof CellInfoGsm) {
            CellInfoGsm cellinfo = (CellInfoGsm) CellInfo_list.get(0);
            CellSignalStrengthGsm cellSignalStrength = cellinfo.getCellSignalStrength();
            rssi = cellSignalStrength.getLevel();

        }

        return rssi;
    }

    private ResultTypes.ResultTypesRSSI getRSSI(){
        int wifi = getRSSIWifi();
        int level;
        if(wifi>0){
            level = wifi;
        }else{
            level = getRSSI4G();
        }
        switch (level) {
            case -1:
                return ResultTypes.ResultTypesRSSI.Sem_Permissao;
            case 0:
                return ResultTypes.ResultTypesRSSI.Sem_Sinal;
            case 1:
                return ResultTypes.ResultTypesRSSI.Pobre;
            case 2:
                return ResultTypes.ResultTypesRSSI.Bom;
            case 3:
                return ResultTypes.ResultTypesRSSI.Otimo;
            default:
                return ResultTypes.ResultTypesRSSI.Sem_Sinal;
        }


    }

    public int getRSSIPuro(){
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        int rssi = -1;
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            rssi = wifiInfo.getRssi();
        }
        return rssi;
    }

}
