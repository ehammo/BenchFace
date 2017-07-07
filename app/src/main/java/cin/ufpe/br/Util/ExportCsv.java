package cin.ufpe.br.Util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by eduardo on 14/12/2016.
 */

public class ExportCsv {
    private String TAG="teste";

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    public void exportCsv(String dataString) {
        //TODO:Remove MPs
        String columnString = "\"ID\";\"Quantity of faces\";\"Time\";\"Algorithm\";\"Execution\";\"Size (KBytes)\";\"Total Time (s)\";\"Upload Time\";\"Download Time\";\"Bandwidth\";\"CPUSmart\";\"CPUNuvem\"";
        String combinedString = columnString + "\n" + dataString;
        File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File file = new File(sdcard + "/BenchFace_Data.csv");
        Log.d(TAG, file.getAbsolutePath());
        FileOutputStream out = null;
        PrintWriter pw = null;
        String oldData = "";
        byte[] Data = null;

        try{
            if(file.exists()){
                oldData = readFile(file.getAbsolutePath());
                Data = (oldData + dataString).getBytes();
            }else{
                Data = combinedString.getBytes();
            }
            out = new FileOutputStream(file);
            out.write(Data);
            dataString="";
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("BROKEN", "Could not write file " + e.getMessage());
        }finally {
            // pw.close();
            try{
                out.close();
            }catch(Exception e){
                e.printStackTrace();
                Log.e("BROKEN", "Could not write file " + e.getMessage());
            }
        }

    }


}
