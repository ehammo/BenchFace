package cin.ufpe.br.main;

//android
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

//code
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.config.Inject;
import br.ufc.mdcc.mpos.config.MposConfig;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;
import cin.ufpe.br.Interfaces.*;
import cin.ufpe.br.Interfaces.Cascade;
import cin.ufpe.br.Util.Data;
import cin.ufpe.br.Util.ExportCsv;
import cin.ufpe.br.service.*;
import cin.ufpe.br.service.DetectFacesService;

//openCV
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;
import java.util.TimeZone;

@MposConfig(endpointSecondary = "192.168.254.48")
//@MposConfig
public class MainActivity extends Activity {

    private static final String TAG = "teste";

    //OpenCv-Related
    private CascadeClassifier cascadeClassifier;
    private BlurImage desfoqueLocal;
    private CutImage corteLocal;
    private DetectFaces detectLocal;
    private Overlay overlayLocal;
    private Cascade cascadeLocal;
    private MainService main;
    private MainServiceNuvem mainNuvem;
    private CloudletBlurImage desfoqueNuvem;
    private CloudletCutImage corteNuvem;
    private CloudletOverlay overlayNuvem;
    private CloudletCascade cascadeNuvem;

    @Inject(DetectFacesService.class)
    private CloudletDetectFaces detectNuvem;

    //CSV-Related
    private int alg;
    private String algorithm = "";
    private String execution  = "LocalBased Execution";
    private int id;
    private String dataString = "";
    private String Originalresolution;
    private Data data;

    //Android-Related
    private Button btn;
    private String timeText;
    private ImageView imageView;
    private Context mContext;
    private ProgressDialog mProgressDialog;

    //Others
    private Bitmap originalImage;
    private byte[] originalImageByte;
    private Bitmap imagemCorteDesfoque;
    private Long TimeStarted;
    private Double TotalTimeBenchmarking=(double)0;
    private Double TotalTime;
    private TextView mTextView;
    private TextView statusTextView;
    private DecimalFormat precision = new DecimalFormat("0.0000");
    private boolean quit;
    private int config;
    private int faces;
    private int benchmarking=32;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    try{
                        Log.d(TAG, "config: "+config);
                        mainNuvem = new MainServiceNuvem(Bitmap2Byte(originalImage), detectNuvem, desfoqueNuvem,corteNuvem,overlayNuvem,algorithm,taskAdapter);
                        switch (config) {
                            case 0:
                                TimeStarted = System.nanoTime();
                                cascadeClassifier = cascadeLocal.loadCascade(alg, algorithm, mContext);
                                main = new MainService(originalImage, detectLocal, desfoqueLocal, corteLocal, overlayLocal, cascadeClassifier, taskAdapter);
                                if (cascadeClassifier != null) {
                                    Log.d(TAG, "Loaded cascade classifier");
                                    main.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                                break;
                            case 1:
                                TimeStarted = System.nanoTime();
                                mainNuvem.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                break;
                            case 2:
                                TimeStarted = System.nanoTime();
                                cascadeClassifier = cascadeLocal.loadCascade(alg, algorithm, mContext);
                                main = new MainService(originalImage, detectLocal, desfoqueLocal, corteLocal, overlayLocal, cascadeClassifier, taskAdapter);
                                if (cascadeClassifier != null) {
                                    Log.d(TAG, "Loaded cascade classifier");
                                    main.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                                break;
                        }
                    } catch (Exception e) {
                        statusTextView.setText("Failed");
                        e.printStackTrace();
                    } finally {
                        if ((cascadeClassifier==null||cascadeClassifier.empty())&&config!=1) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                            statusTextView.setText("failed");
                            break;
                        }
                    }
                }
                default: {
                    super.onManagerConnected(status);
                    if(status!=LoaderCallbackInterface.SUCCESS){
                        Log.d(TAG, "failed");
                    }

                }
                break;
            }
        }
    };

    private TaskResultAdapter<Bitmap> taskAdapter = new TaskResultAdapter<Bitmap>() {
        @Override
        public void completedTask(Bitmap obj) {
            if (obj != null) {
                imagemCorteDesfoque = obj;
                originalImageByte = Bitmap2Byte(obj);
                data.setSize(originalImageByte.length+"");
                mProgressDialog.dismiss();
                imageView.setVisibility(View.VISIBLE);
                if(config==1){
                    faces = mainNuvem.getNumFaces();
                    data.setFaces(mainNuvem.getNumFaces());
                }else{
                    faces = main.getNumFaces();
                    data.setFaces(main.getNumFaces());

                }
                changeCSV();
                if(benchmarking<30){
                    benchmarking++;
                    mTextView.setText("Time: ");
                    mProgressDialog.show();
                    imageView.setVisibility(View.INVISIBLE);
                    statusTextView.setText("["+benchmarking+"/30]"+"\nProcessing");
                    imageView.setImageBitmap(originalImage);
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, mContext, mLoaderCallback);
                }else if(benchmarking==30){
                    timeText = precision.format(TotalTimeBenchmarking) + "s";
                    timeText.replace(",", ".");
                    if(TotalTimeBenchmarking>=60){
                        int min = (int) Math.floor(TotalTimeBenchmarking/60);
                        int sec = (int) Math.ceil(TotalTimeBenchmarking - (min*60));
                        mTextView.setText(min+"min e "+sec+"s");
                    }else{
                        mTextView.setText(TotalTimeBenchmarking+"s");
                    }
                    String now = getCurrentTimeStamp();
                    data.setName(id);
                    data.setFaces(0);
                    data.setAlgorithm(algorithm);
                    data.setExecution(execution);
                    data.setTotalTime(timeText);
                    data.setoRes("Todos");
                    data.setTime(now);
                    data.setResult();
                    Log.i(TAG,data.getData());
                    TotalTimeBenchmarking=(double)0;
                    id++;
                    benchmarking=32;
                }
            } else {
                TextView tv_status = (TextView) findViewById(R.id.textStatus);
                tv_status.setText("Status: Algum Error na transmissão!");
            }
        }
    };

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    /*
    public float getBattery(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        return batteryPct*100;
    }*/

    /*
    public int getRSSI(){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 6;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        return level;
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btnHide);
        imageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textTime);
        statusTextView = (TextView) findViewById(R.id.textStatus);
        mContext = this;
        quit=false;

        data = new Data();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Processing........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        ((RadioButton)findViewById(R.id.RBlocal)).setChecked(true);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerAlg);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.algorithm_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Spinner photoSpinner = (Spinner) findViewById(R.id.spinnerPhoto);
        ArrayAdapter<CharSequence> photoAdapter = ArrayAdapter.createFromResource(this, R.array.photo_array, android.R.layout.simple_spinner_item);
        photoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        photoSpinner.setAdapter(photoAdapter);

        photoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Drawable d;
                Bitmap b;
                switch (pos) {
                    case 0:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_1_5mp,200,200);
                        Originalresolution = "1.5";
                        break;
                    case 1:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_3mp, 200,200);
                        Originalresolution = "3";
                        break;
                    case 2:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_6_5mp, 200,200);
                        Originalresolution = "6";
                        break;
                    case 3:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_8_5mp, 200,200);
                        Originalresolution = "8.5";
                        break;
                    case 4:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_13_5mp, 200,200);
                        Originalresolution = "13.5";
                        break;
                    default:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_1_5mp,200,200);
                        break;
                }
                data.setoRes(Originalresolution);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch (pos) {
                    case 0:
                        alg = R.raw.haarcascade_frontalface_alt_tree;
                        algorithm = "haarcascade_frontalface_alt_tree";
                        Log.d(TAG, "" + alg);
                        break;
                    case 1:
                        alg = R.raw.haarcascade_frontalface_alt;
                        algorithm = "haarcascade_frontalface_alt";
                        Log.d(TAG, "" + alg);
                        break;
                    case 2:
                        alg = R.raw.haarcascade_frontalface_alt2;
                        algorithm = "haarcascade_frontalface_alt2";
                        Log.d(TAG, "" + alg);
                        break;
                    case 3:
                        alg = R.raw.haarcascade_frontalface_default;
                        algorithm = "haarcascade_frontalface_default";
                        Log.d(TAG, "" + alg);
                        break;
                    case 4:
                        alg = R.raw.haarcascade_frontalface_default;
                        algorithm = "haarcascade_frontalface_default";
                        Log.d(TAG, "" + alg);
                        benchmarking=1;
                        break;

                    default:
                        break;
                }
                data.setAlgorithm(algorithm);
                if(pos!=4) benchmarking=32;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method();

            }
        });

        cascadeLocal = new CascadeService();
        cascadeNuvem = new CascadeService();
        desfoqueLocal = new BlurImageService();
        desfoqueNuvem = new BlurImageService();
        corteLocal = new CutImageService();
        corteNuvem = new CutImageService();
        detectLocal = new DetectFacesService();
        detectNuvem = new DetectFacesService();
        overlayLocal = new OverlayService();
        overlayNuvem = new OverlayService();
        verifyStoragePermissions(this, PERMISSIONS_STORAGE);


        MposFramework.getInstance().start(this);
        Log.d(TAG,"middleware started");
/*
        Log.d(TAG, "Core 0: "  + getCurrentFrequency(0)+"Ghz");
        Log.d(TAG, "Core 1: "  + getCurrentFrequency(1)+"Ghz");
        Log.d(TAG, "Core 2: "  + getCurrentFrequency(2)+"Ghz");
        Log.d(TAG, "Core 3: "  + getCurrentFrequency(3)+"Ghz");
        Log.d(TAG, "Core 4: "  + getCurrentFrequency(4)+"Ghz");
        Log.d(TAG, "Core 5: "  + getCurrentFrequency(5)+"Ghz");
        Log.d(TAG, "Core 6: "  + getCurrentFrequency(6)+"Ghz");
        Log.d(TAG, "Core 7: "  + getCurrentFrequency(7)+"Ghz");
*/
       /* Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        //Log.d(TAG,""+getRSSI());
                        //Log.d(TAG, "Bateria: "+getBattery()+"%");
                        //Log.d(TAG, "CPU: "+ getCPUStatistic());
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();*/


    }

    protected void onDestroy(){
        super.onDestroy();
        if(quit) {
            MposFramework.getInstance().stop();
            Log.d(TAG,"middleware ended");
        }
    }

    public void method() {
        TimeStarted = System.nanoTime();
        imageView.setVisibility(View.INVISIBLE);
        mProgressDialog.show();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        mTextView.setText("Time: ");
        statusTextView.setText("Processing");
        if(benchmarking!=32) statusTextView.setText("["+benchmarking+"/30]"+ "\nProcessing ");
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader (file));
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

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize += 1;
            }
        }

        Log.d(TAG,"sampleSize: " + inSampleSize);
        return inSampleSize;
    }

    public void decodeSampledBitmapFromResource(Resources pRes, int pResId,
                                                         int pReqWidth, int pReqHeight) {
        final Resources res = pRes;
        final int resId = pResId;
        final int reqWidth = pReqWidth;
        final int reqHeight = pReqHeight;
        new Thread() {
            @Override
            public void run() {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(res, resId, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                originalImage = BitmapFactory.decodeResource(res, resId, options);
                runOnUiThread(new Runnable(){
                    public void run() {
                        // First decode with inJustDecodeBounds=true to check dimensions
                        //setImageBitmap(originalImage, imageView);
                        imageView.setImageBitmap(originalImage);

                    }
                });
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Want to quit and export Csv file?").setTitle("Exit");
        quit=true;

        //Since the order that they appear is Neutral>Negative>Positive I change the content of each one
        //So The negative is my neutral, the neutral is my positive and at last the positive is my negative
        builder.setNegativeButton("Just Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ExportCsv exportModule = new ExportCsv();
                exportModule.exportCsv(data.getData());
                finish();
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                quit=false;
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void changeCSV(){
        TotalTime = (double) (System.nanoTime() - TimeStarted) / 1000000000.0;
        timeText = precision.format(TotalTime);
        statusTextView.setText(faces + " faces");
        imageView.setImageBitmap(imagemCorteDesfoque);
        int value = (originalImage.getHeight() * originalImage.getWidth())/1000000;

        TotalTimeBenchmarking += TotalTime;
        mTextView.setText(timeText+"s");
        if(TotalTime>=60){
            int min = (int) Math.floor(TotalTime/60);
            double sec = TotalTime - (min*60);
            mTextView.setText(min+"min e "+sec+"s");
        }

        long cpu_time = MposFramework.getInstance().getEndpointController().rpcProfile.getExecutionCpuTime();
        long download_time = MposFramework.getInstance().getEndpointController().rpcProfile.getDonwloadTime();
        long upload_time = MposFramework.getInstance().getEndpointController().rpcProfile.getUploadTime();

        data.setCpuTime(cpu_time);
        data.setDownloadTime(download_time);
        data.setUploadTime(upload_time);
        data.setName(id);
        data.setpRes(value+"");
        data.setTotalTime(timeText);
        data.setTime(getCurrentTimeStamp());
        data.setExecution(execution);
        data.setResult();
        Log.i(TAG, data.getData());

    }

    public byte[] Bitmap2Byte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return stream.toByteArray();

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        RadioButton clicked = ((RadioButton) view);
        RadioButton nuvem = ((RadioButton)findViewById(R.id.RBnuvem));
        RadioButton local = ((RadioButton)findViewById(R.id.RBlocal));
        RadioButton dynamic = ((RadioButton)findViewById(R.id.RBdynamic));
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.RBlocal:
                if (clicked.isChecked()&&(nuvem.isChecked()||dynamic.isChecked())) {
                    execution = "LocalBased Execution";
                    nuvem.setChecked(false);
                    dynamic.setChecked(false);
                    config = 0;
                }else if(clicked.isChecked()){
                    execution = "LocalBased Execution";
                    config = 0;
                }
                    break;
            case R.id.RBnuvem:
                if (clicked.isChecked()&&(local.isChecked()||dynamic.isChecked())){
                    execution = "CloudBased Execution";
                    local.setChecked(false);
                    dynamic.setChecked(false);
                    config = 1;

                }else if(clicked.isChecked()){
                    execution = "CloudBased Execution";
                    config = 1;
                }
                    break;
            case R.id.RBdynamic:
                if (clicked.isChecked()&&(local.isChecked()||nuvem.isChecked())) {
                    execution = "LocalBased Execution";
                    nuvem.setChecked(false);
                    local.setChecked(false);
                    config = 2;
                }else if(clicked.isChecked()){
                    execution = "LocalBased Execution";
                    config = 2;
                }
                break;
        }
        data.setExecution(execution);
    }

    public static void verifyStoragePermissions(Activity activity, String[] per) {
        // Check if we have write permission
        for(int i=0;i<per.length;i++){
            int permission = ActivityCompat.checkSelfPermission(activity, per[i]);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        1
                );
                i=per.length;
            }

        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Want to export Csv file?").setTitle("Export");
        //Since the order that they appear is Neutral>Negative>Positive I change the content of each one
        //So The negative is my positive and the positive is my negative
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ExportCsv exportModule = new ExportCsv();
                exportModule.exportCsv(data.getData());
                Toast.makeText(mContext, "Exported", Toast.LENGTH_LONG);
          }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

   /* private String getCPUStatistic() {

        String tempString = getCPU();

        tempString = tempString.replaceAll(",", "");
        tempString = tempString.replaceAll("User", "");
        tempString = tempString.replaceAll("System", "");
        tempString = tempString.replaceAll("IOW", "");
        tempString = tempString.replaceAll("IRQ", "");
        tempString = tempString.replaceAll("%", "");
        for (int i = 0; i < 10; i++) {
            tempString = tempString.replaceAll("  ", " ");
        }
        tempString = tempString.trim();
        String[] myString = tempString.split(" ");
        int total =0;
        int[] cpuUsageAsInt = new int[myString.length];
        for (int i = 0; i < myString.length; i++) {
            myString[i] = myString[i].trim();
            cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
            total+=cpuUsageAsInt[i];
        }
        return total+"%";
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

    private static final int INSERTION_POINT = 27;

    private static String getCurFrequencyFilePath(int whichCpuCore){
        StringBuilder filePath = new StringBuilder("/sys/devices/system/cpu/cpu/cpufreq/scaling_cur_freq");
        filePath.insert(INSERTION_POINT, whichCpuCore);
        return filePath.toString();
    }

    public float getCurrentFrequency(int whichCpuCore){

        float curFrequency = -1;
        String cpuCoreCurFreqFilePath = getCurFrequencyFilePath(whichCpuCore);

        if(new File(cpuCoreCurFreqFilePath).exists()){

            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(cpuCoreCurFreqFilePath)));
                String aLine;
                while ((aLine = br.readLine()) != null) {

                    try{
                        curFrequency = Integer.parseInt(aLine);
                    }
                    catch(NumberFormatException e){

                        Log.e(getPackageName(), e.toString());
                    }

                }
                if (br != null) {
                    br.close();
                }
            }
            catch (IOException e) {
                Log.e(getPackageName(), e.toString());
            }

        }

        curFrequency = curFrequency/(float)1000000;

        if(curFrequency<0)
            curFrequency=-1;

        return curFrequency;
    }*/


}
