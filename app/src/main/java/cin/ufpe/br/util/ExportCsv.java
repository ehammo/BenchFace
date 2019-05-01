package cin.ufpe.br.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by eduardo on 14/12/2016.
 */

public class ExportCsv {

    private String TAG = "ExportCsv";
    private String columnString =
            "\"ID\";\"Time\";\"Quantity of faces\";"+
                    "\"Algorithm\";\"Execution\";\"offloaded\";\"Size (KBytes)\";" +
            "\"Total Time (s)\";\"Upload Time\";\"Download Time\";"+
            "\"Bandwidth\";\"CPUSmart\";\"CPUNuvem\"";

    private String readFile(String file) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }
    }

    public void exportCsv(String dataString) {
        String combinedString = columnString + "\n" + dataString;
        File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File file = new File(sdcard + "/BenchFace_Data.csv");
        Log.d(TAG, file.getAbsolutePath());
        FileOutputStream out;
        String oldData;
        byte[] Data;

        try{
            if(file.exists()){
                oldData = readFile(file.getAbsolutePath());
                Data = (oldData + dataString).getBytes();
            }else{
                Data = combinedString.getBytes();
            }
            out = new FileOutputStream(file);
            out.write(Data);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("BROKEN", "Could not write file " + e.getMessage());
        }
    }
}
