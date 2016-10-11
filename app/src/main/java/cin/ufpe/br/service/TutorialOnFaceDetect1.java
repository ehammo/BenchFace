package cin.ufpe.br.service;

/*
 * TutorialOnFaceDetect1
 * 
 * [AUTHOR]: Chunyen Liu
 * [SDK   ]: Android SDK 2.1 and up
 * [NOTE  ]: developer.com tutorial, "Face Detection with Android APIs"
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import cin.ufpe.br.model.MyImageView;

public class TutorialOnFaceDetect1 {
	private MyImageView mIV;
	private Bitmap mFaceBitmap;
	private int mFaceWidth = 200;
	private int mFaceHeight = 200;   
	private static final int MAX_FACES = 10;
	private static String TAG = "FaceDetect";
	private Boolean done = false;


    public Bitmap loadPhoto(int drawable, Context c){
        mIV = new MyImageView(c);

        // load the photo
        Bitmap b = BitmapFactory.decodeResource(c.getResources(), drawable);
        mFaceBitmap = b.copy(Bitmap.Config.RGB_565, true);
        b.recycle();

        mFaceWidth = mFaceBitmap.getWidth();
        mFaceHeight = mFaceBitmap.getHeight();
        mIV.setImageBitmap(mFaceBitmap);

        //perform face detection and set the feature points
        setFace();
        Log.d("log","Vou dar get");
        return mIV.getBitmap();
    }

	public void setFace() {
		FaceDetector fd;
		FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
		PointF midpoint = new PointF();
		int [] fpx = null;
		int [] fpy = null;
		int count = 0;

		try {
			fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);        
			count = fd.findFaces(mFaceBitmap, faces);
		} catch (Exception e) {
			Log.e(TAG, "setFace(): " + e.toString());
			return;
		}

		// check if we detect any faces
		if (count > 0) {
			fpx = new int[count];
			fpy = new int[count];

			for (int i = 0; i < count; i++) { 
				try {                 
					faces[i].getMidPoint(midpoint);                  

					fpx[i] = (int)midpoint.x;
					fpy[i] = (int)midpoint.y;
				} catch (Exception e) { 
					Log.e(TAG, "setFace(): face " + i + ": " + e.toString());
				}            
			}      
		}

		mIV.setDisplayPoints(fpx, fpy, count, 0);
	}

}