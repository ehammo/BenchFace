package br.ufpe.cin.mpos.profile.net;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.config.MposConfig;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

/**
 * Created by eduar on 26/03/2017.
 */

public class ProfileNetworkNew {
    SpeedTestSocket speedTestSocket = new SpeedTestSocket();
    URLConnection connection;
    final BigDecimal toMBits = new BigDecimal("1000000");
    List<BigDecimal> uploadF = new ArrayList<BigDecimal>();
    List<BigDecimal> downloadF = new ArrayList<BigDecimal>();
    long timeStarted;
    long timeSpent;


    public void start(final ServerContent server) {
        
        try {
            Log.d("teste", server.getIp());
            Log.d("newSpeed", "SpeedTestStart");
            timeStarted = System.currentTimeMillis();
// add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onDownloadFinished(SpeedTestReport report) {
                    // called when download is finished
                    BigDecimal result = (report.getTransferRateBit().divide(toMBits, 2, RoundingMode.HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    downloadF.add(result);
                    Log.d("newSpeed", "[DL FINISHED] rate in bit/s   : " + result);
                    Log.d("newSpeed", "[DL AVERAGE] rate in bit/s   : " + avg(downloadF));
                    Log.d("newSpeedFinal", "[DL AVERAGE] rate in bit/s   : " + avg(downloadF));
                    downloadF = new ArrayList<BigDecimal>();
                    speedTestSocket.startUpload(server.getIp(), "/", 1750000);
                }

                @Override
                public void onDownloadError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download error occur
                    Log.e("newSpeed", "DownloadError");
                    Log.e("newSpeed", speedTestError.toString());
                    Log.e("newSpeed", " " + errorMessage);
                    Log.d("newSpeed", "[DL AVERAGE] rate in bit/s   : " + avg(downloadF));
                    Log.d("newSpeedFinal", "[DL AVERAGE] rate in bit/s   : " + avg(downloadF));
                    downloadF = new ArrayList<BigDecimal>();
                    speedTestSocket.startUpload(server.getIp(), "/", 1750000);
                }

                @Override
                public void onUploadFinished(SpeedTestReport report) {
                    // called when an upload is finished
                    BigDecimal result = (report.getTransferRateBit().divide(toMBits, 2, RoundingMode.HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    uploadF.add(result);
                    Log.d("newSpeed", "[UL FINISHED] rate in Mbit/s   : " + result);
                    Log.d("newSpeed", "[UL AVERAGE] rate in Mbit/s   : " + avg(uploadF));
                    Log.d("newSpeedFinal", "[UL AVERAGE] rate in Mbit/s   : " + avg(uploadF));
                    uploadF = new ArrayList<BigDecimal>();
                    timeSpent = (System.currentTimeMillis() - timeStarted)/1000;
                    Log.d("newSpeed","TimeSpent: "+timeSpent);
                    Log.d("newSpeedFinal","TimeSpent: "+timeSpent);
                }

                @Override
                public void onUploadError(SpeedTestError speedTestError, String errorMessage) {
                    // called when an upload error occur
                    Log.e("newSpeed", "UploadError");
                    Log.e("newSpeed", speedTestError.toString());
                    Log.e("newSpeed", errorMessage);
                    Log.d("newSpeed", "[UL AVERAGE] rate in Mbit/s   : " + avg(uploadF));
                    Log.d("newSpeedFinal", "[UL AVERAGE] rate in Mbit/s   : " + avg(uploadF));
                    uploadF = new ArrayList<BigDecimal>();
                    timeSpent = (System.currentTimeMillis() - timeStarted)/1000;
                    Log.d("newSpeed","TimeSpent: "+timeSpent);
                    Log.d("newSpeedFinal","TimeSpent: "+timeSpent);

                }

                @Override
                public void onDownloadProgress(float percent, SpeedTestReport report) {
                    // called to notify download progress
                    Log.d("newSpeed", "[DL PROGRESS] progress : " + percent + "%");
                    BigDecimal result = (report.getTransferRateBit().divide(toMBits, 2, RoundingMode.HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    downloadF.add(result);
                    Log.d("newSpeed", "[DL PROGRESS] rate in Mbit/s   : " + result);

                }

                @Override
                public void onUploadProgress(float percent, SpeedTestReport report) {
                    // called to notify upload progress
                    Log.d("newSpeed", "[UL PROGRESS] progress : " + percent + "%");
                    BigDecimal result = (report.getTransferRateBit().divide(toMBits, 2, RoundingMode.HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    uploadF.add(result);
                    Log.d("newSpeed", "[UL PROGRESS] rate in Mbit/s   : " + result);
                }

                @Override
                public void onInterruption() {
                    // triggered when forceStopTask is called
                    Log.e("newSpeed", "ForceStop");
                }
            });


        speedTestSocket.startDownload(server.getIp(), "/1Mo.dat");
        }catch(Exception e){
            Log.e("newSpeed", e.getStackTrace()+"");
        }
    }

    public BigDecimal avg(List<BigDecimal> list){
        BigDecimal result = new BigDecimal("0");
        for(int i=0;i<list.size();i++){
            if(i>2){
                result = result.add(list.get(i));
            }
        }
        if(list.size()>3) {
            result = result.divide(new BigDecimal("" + (list.size()-3)), 2, RoundingMode.HALF_UP);
        }
        return result;
    }

}
