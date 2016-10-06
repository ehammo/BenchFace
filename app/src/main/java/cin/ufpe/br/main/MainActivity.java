package cin.ufpe.br.main;

//android
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

//code
import cin.ufpe.br.service.ServiceCorteImagem;
import cin.ufpe.br.service.ServiceDesfoqueImagem;
import cin.ufpe.br.service.ServiceDeteccaoFacesImagem;
import cin.ufpe.br.model.PropriedadesFace;
import cin.ufpe.br.service.ServiceSobreposicaoImagem;

//openCV
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

//file_related
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends Activity {

    private static final String TAG = "log";

    File mCascadeFile;
    private CascadeClassifier cascadeClassifier;

    private Button btn;
    private Button btnG;
    private Bitmap originalImage;
    private ImageView imageView;
    private Long TimeStarted;
    private Double TotalTime;
    private TextView time;
    private TextView battery;
    private TextView statusTextView;
    private Intent batteryStatus;
    private float batteryValue;
    private String path;
    private Uri UriPath;
    private Context mContext;
    private DecimalFormat precision = new DecimalFormat("0.0000");
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG, "OpenCV loaded successfully");
                    try {
                    // Load native library after(!) OpenCV initialization
                    InputStream is = getResources().openRawResource( R.raw.haarcascade_frontalface_alt_tree);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt_tree.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();
                    cascadeClassifier =  new CascadeClassifier(  mCascadeFile.getAbsolutePath());
                    cascadeClassifier.load(mCascadeFile.getAbsolutePath());
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    Log.d(TAG,"\nRunning FaceDetector");
//                    Mat mat = Imgcodecs.imread(path);

                    Mat mat=new Mat();

                    Utils.bitmapToMat(originalImage,mat);

                    if(mat.empty()){
                       Log.d(TAG,"matriz de foto vazia");
                    }else {
                        statusTextView.setText("\nRunning FaceDetector");

                        //faz a detecção facial
                        statusTextView.setText("Face detection");
                        ServiceDeteccaoFacesImagem serviceExtractFaces = new ServiceDeteccaoFacesImagem();
                        MatOfRect matOfRect = serviceExtractFaces.detectarFaces(cascadeClassifier, mat);

                        statusTextView.setText("get data from face detection");

                        //obtem os dados de onde estão as faces (altura, largura, posição x e y)
                        List<PropriedadesFace> propsFaces = serviceExtractFaces.obterDadosFaces(matOfRect);

                        statusTextView.setText("blur");

                        //desfoca a imagem
                        ServiceDesfoqueImagem serviceBlur = new ServiceDesfoqueImagem();
                        Bitmap imagemCorteDesfoque;
                        imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

                        statusTextView.setText("cut blured image");

                        //corta os rostos da imagem desfocada,
                        ServiceCorteImagem serviceCrop = new ServiceCorteImagem();
                        propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

                        ServiceSobreposicaoImagem serviceOverlay = new ServiceSobreposicaoImagem();

                        statusTextView.setText("draw rect");

                        //desenha os retangulos
                        for (Rect rect : matOfRect.toArray()) {
                            Imgproc.rectangle(mat, new Point(rect.x - 1, rect.y - 1), new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(0, 255, 0), 3);
                        }

                        statusTextView.setText("overlay");

                        //"cola" os rostos desfocados sobre a imagem original
                        imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);

                        statusTextView.setText("Detected "+propsFaces.size()+" faces");

                        imageView.setImageBitmap(imagemCorteDesfoque);

                        TotalTime = (double)(System.nanoTime() - TimeStarted)/1000000000.0;
                        batteryValue = calcBattery(batteryValue);
                        battery.setText("Battery level spent: " + batteryValue);
                        Log.d(TAG,""+TotalTime);
                        Log.d(TAG,"Time spent "+precision.format(TotalTime));
                        String timeText = "Time spent "+precision.format(TotalTime)+"s";
                        time.setText(timeText);
                    }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally{
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                        }

                        }
                        break;
                }
                    default:
                {
                    Log.d(TAG,"3");
                    super.onManagerConnected(status);
                    Log.d(TAG,"failed");
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.btnHide);
        btnG = (Button)findViewById(R.id.btnG);
        imageView = (ImageView) findViewById(R.id.imageView);
        time = (TextView) findViewById(R.id.textTime);
        battery = (TextView) findViewById(R.id.textBattery);
        statusTextView = (TextView) findViewById(R.id.textStatus);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, ifilter);
        mContext=this;

        btnG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                method();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null){
           Uri path = data.getData();
           this.UriPath = path;
           this.path=path.getPath();
           setImage();
        }

    }


    public void choosePic(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void setImage(){
        try {
            ContentResolver cr = getContentResolver();
            InputStream in = cr.openInputStream(UriPath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            originalImage = BitmapFactory.decodeStream(in, null, options);
            imageView.setImageBitmap(originalImage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void method(){
        TimeStarted = System.nanoTime();
        batteryValue = calcBattery((float)0.0);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallback);
    }

    public float calcBattery(float init){
        float batteryValue;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryValue = (init - (level / (float)10000));
        if (batteryValue<0) batteryValue*=-1;
        return batteryValue;
    }
}
