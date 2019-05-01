package cin.ufpe.br.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by eduardo on 10/06/2017.
 */

public class Util {

    public static byte[] Bitmap2Byte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return stream.toByteArray();

    }

    public static Bitmap Byte2Bitmap(byte[] bitmapdata){
        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
    }


}
