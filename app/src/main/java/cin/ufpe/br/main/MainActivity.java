package cin.ufpe.br.main;

//android
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
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

//file_related
import java.io.File;
import java.io.FileOutputStream;
import java.util.TimeZone;

@MposConfig(endpointSecondary = "150.161.70.205")
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
    private String execution = "";
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                    if(TotalTimeBenchmarking>=60){
                        int min = (int) Math.floor(TotalTimeBenchmarking/60);
                        double sec = TotalTime - (min*60);
                        mTextView.setText(min+"min e "+sec+"s");
                    }else{
                        mTextView.setText(TotalTimeBenchmarking+"s");
                    }
                    String now = getNow();
                    data.setName(id);
                    data.setFaces(0);
                    data.setAlgorithm(algorithm);
                    data.setExecution(execution);
                    data.setoRes("Todos");
                    data.setTime(now);
                    data.setResult();
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

    public String getNow(){
        TimeZone tz = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(tz);
        Date now = new Date();
        calendar.setTime(now);
        return now.toString();
    }

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
                        Originalresolution = "1.5MP";
                        d = ContextCompat.getDrawable(mContext, R.drawable.facedetection_1_5mp);
                        b = ((BitmapDrawable)d).getBitmap();
                        originalImageByte = Bitmap2Byte(b);
                        data.setSize(originalImageByte.length+" Bytes");
                        break;
                    case 1:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_3mp, 200,200);
                        Originalresolution = "3MP";
                        d = ContextCompat.getDrawable(mContext, R.drawable.facedetection_3mp);
                        b = ((BitmapDrawable)d).getBitmap();
                        originalImageByte = Bitmap2Byte(b);
                        data.setSize(originalImageByte.length+" Bytes");
                        break;
                    case 2:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_6_5mp, 200,200);
                        Originalresolution = "6MP";
                        d = ContextCompat.getDrawable(mContext, R.drawable.facedetection_6_5mp);
                        b = ((BitmapDrawable)d).getBitmap();
                        originalImageByte = Bitmap2Byte(b);
                        data.setSize(originalImageByte.length+" Bytes");
                        break;
                    case 3:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_8_5mp, 200,200);
                        Originalresolution = "8.5MP";
                        d = ContextCompat.getDrawable(mContext, R.drawable.facedetection_8_5mp);
                        b = ((BitmapDrawable)d).getBitmap();
                        originalImageByte = Bitmap2Byte(b);
                        data.setSize(originalImageByte.length+" Bytes");
                        break;
                    case 4:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_13_5mp, 200,200);
                        Originalresolution = "13.5MP";
                        d = ContextCompat.getDrawable(mContext, R.drawable.facedetection_13_5mp);
                        b = ((BitmapDrawable)d).getBitmap();
                        originalImageByte = Bitmap2Byte(b);
                        data.setSize(originalImageByte.length+" Bytes");
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
        verifyStoragePermissions(this);

        MenuInflater menuInflater = getMenuInflater();
        //Menu menu =
     //  menuInflater.inflate(R.menu.main,menu);

        MposFramework.getInstance().start(this);
        Log.d(TAG,"middleware started");
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
        data.setpRes(value+"MP");
        data.setTotalTime(timeText);
        data.setTime(getNow());
        data.setResult();

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

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
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

}
