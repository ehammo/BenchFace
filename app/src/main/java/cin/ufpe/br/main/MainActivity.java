package cin.ufpe.br.main;

//android
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

//code
import cin.ufpe.br.service.ServiceCorteImagem;
import cin.ufpe.br.service.ServiceDesfoqueImagem;
import cin.ufpe.br.service.ServiceDeteccaoFacesImagem;
import cin.ufpe.br.model.PropriedadesFace;
import cin.ufpe.br.service.ServiceSobreposicaoImagem;
import cin.ufpe.br.service.TutorialOnFaceDetect1;

//openCV
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

//file_related
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.TimeZone;


public class MainActivity extends Activity {

    private static final String TAG = "teste";

    private File mCascadeFile;
    private TutorialOnFaceDetect1 fd;
    private CascadeClassifier cascadeClassifier;
    private int alg = 0;
    private String algorithm = "";
    private String execution = "";
    private Button btn;
    private String dataString = "";
    private int id = 0;
    private String timeText;
    private Bitmap originalImage;
    private Mat mat;
    private ImageView imageView;
    private Long TimeStarted;
    private Double TotalTime;
    private String Originalresolution;
    private String resolution;
    private TextView time;
    private TextView statusTextView;
    private Context mContext;
    private DecimalFormat precision = new DecimalFormat("0.0000");

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    try{
                        InputStream is = getResources().openRawResource(alg);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, algorithm);
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
                        Log.d(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                        Log.d(TAG, "\nRunning FaceDetector");
                        mat = new Mat();
                        int value = (originalImage.getHeight() * originalImage.getWidth())/1000000;
                        resolution = value+"MP";
                        Utils.bitmapToMat(originalImage, mat);
                        //mat = Utils.loadResource(mContext,R.drawable.facedetection_13_5mp);
                        ServiceDeteccaoFacesImagem serviceExtractFaces = new ServiceDeteccaoFacesImagem();
                        MatOfRect matOfRect = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);

                        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
                        List<PropriedadesFace> propsFaces = serviceExtractFaces.obterDadosFaces(matOfRect);

                        //desfoca a imagem
                        ServiceDesfoqueImagem serviceBlur = new ServiceDesfoqueImagem();
                        Bitmap imagemCorteDesfoque;
                        imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

                        //corta os rostos da imagem desfocada,
                        ServiceCorteImagem serviceCrop = new ServiceCorteImagem();
                        propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

                        ServiceSobreposicaoImagem serviceOverlay = new ServiceSobreposicaoImagem();

                        //"cola" os rostos desfocados sobre a imagem original
                        imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);
                        statusTextView.setText("Detected " + propsFaces.size() + " faces");
                        imageView.setImageBitmap(imagemCorteDesfoque);
                        TotalTime = (double) (System.nanoTime() - TimeStarted) / 1000000000.0;
                        Log.d(TAG, "" + TotalTime);
                        Log.d(TAG, "Time spent " + precision.format(TotalTime));
                        timeText = "Time spent " + precision.format(TotalTime) + "s";
                        time.setText(timeText);
                        TimeZone tz = TimeZone.getDefault();
                        Calendar calendar = new GregorianCalendar(tz);
                        Date now = new Date();
                        calendar.setTime(now);
                        dataString += "\"" + id + "\",\"" + propsFaces.size() + "\",\"" + Originalresolution + "\",\"" + resolution + "\",\"" + "??" + "\",\"" + timeText + "\",\"" + "??" + "\", \"" + now.toString() + "\", \"" + algorithm + "\", \"" + execution + "\"";
                        dataString += "\n";
                        id++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                            break;
                        }
                    }
                }
                default: {
                    super.onManagerConnected(status);
                    Log.d(TAG, "failed");
                }
                break;
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
   private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btnHide);
        imageView = (ImageView) findViewById(R.id.imageView);
        time = (TextView) findViewById(R.id.textTime);
        statusTextView = (TextView) findViewById(R.id.textStatus);
        mContext = this;

        fd = new TutorialOnFaceDetect1();

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
                switch (pos) {
                    case 0:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_1_5mp,200,200);
                        Originalresolution = "1.5MP";
                        break;
                    case 1:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_3mp, 200,200);
                        Originalresolution = "3MP";
                        break;
                    case 2:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_6_5mp, 200,200);
                        Originalresolution = "6MP";
                        break;
                    case 3:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_8_5mp, 200,200);
                        Originalresolution = "8.5MP";
                        break;
                    case 4:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_13_5mp, 200,200);
                        Originalresolution = "13.5MP";
                        break;
                    default:
                        decodeSampledBitmapFromResource(mContext.getResources(), R.drawable.facedetection_1_5mp,200,200);
                        break;
                }
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
                    default:
                        break;
                }
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void method() {
        TimeStarted = System.nanoTime();
        statusTextView.setText("Processing");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

//    public float calcBattery(float init) {
//        float batteryValue;
//        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        batteryValue = (init - (level / scale));
//        if (batteryValue < 0) batteryValue *= -1;
//        return batteryValue;
//    }

    public void exportCsv() {
        String columnString = "\"Name\",\"Quantity of faces\",\"Original Resolution\",\"Processed Resolution\",\"Ilumination\",\"TimeSpent\",\"Battery\",\"Time\",\"Algorithm\",\"Execution\"";
        String combinedString = columnString + "\n" + dataString;
        File file = new File(this.getExternalCacheDir() + File.separator + "Data.csv");


        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(combinedString.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BROKEN", "Could not write file " + e.getMessage());
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
                exportCsv();
                finish();
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

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        RadioButton clicked = ((RadioButton) view);
        RadioButton nuvem = ((RadioButton)findViewById(R.id.RBnuvem));
        RadioButton local = ((RadioButton)findViewById(R.id.RBlocal));
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.RBlocal:
                if (clicked.isChecked()&&nuvem.isChecked()) {
                    execution = "LocalBased Execution";
                    nuvem.setChecked(false);
                }else if(clicked.isChecked()){
                    Log.d(TAG, "entrei no 1.2 if");
                    execution = "LocalBased Execution";
                }
                    break;
            case R.id.RBnuvem:
                if (clicked.isChecked()&&local.isChecked()){
                    execution = "CloudBased Execution";
                    local.setChecked(false);
                }else if(clicked.isChecked()){
                    execution = "CloudBased Execution";
                }
                    break;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
