package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import cin.ufpe.br.Interfaces.CloudletDetectFaces;
import cin.ufpe.br.Util.Util;
import cin.ufpe.br.main.MainActivity;
import cin.ufpe.br.model.PropriedadesFace;

public class DetectFacesService implements CloudletDetectFaces{
	private static final String TAG="log";


	public PropriedadesFace detectarFaces(String s, byte[] image){
		try {
            Log.d("teste", "DEU MERDA RODOU LOCAL");
            ByteArrayInputStream in = new ByteArrayInputStream(image);
			Mat mat = new Mat();
			Utils.bitmapToMat(BitmapFactory.decodeStream(in), mat);
            CascadeClassifier c = MainActivity.cascadeClassifier;
            List<PropriedadesFace> propsFaces =  detectarFaces(c, mat);
			//desfoca a imagem
			BlurImageService serviceBlur = new BlurImageService();
			Bitmap imagemCorteDesfoque = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(serviceBlur.DesfocarImagem(mat), imagemCorteDesfoque);

			//corta os rostos da imagem desfocada,
			CutImageService serviceCrop = new CutImageService();
			propsFaces = serviceCrop.CortarImagem(propsFaces, imagemCorteDesfoque);

			OverlayService serviceOverlay = new OverlayService();

			//"cola" os rostos desfocados sobre a imagem original
			imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, Util.Byte2Bitmap(image));
			Log.d("teste","deu tudo certo");
			Log.d("teste","qtd de faces: "+propsFaces.size());
            PropriedadesFace p = new PropriedadesFace();
            p.setFaces(propsFaces.size());
            p.setImagemFinal(Util.Bitmap2Byte(imagemCorteDesfoque));
            return p;
        }catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public List<PropriedadesFace> detectarFaces(CascadeClassifier cascadeClassifier, Mat mat){
		MatOfRect matOfRect = new MatOfRect();
		cascadeClassifier.detectMultiScale(mat, matOfRect);
        Log.d(TAG, "Detected "+matOfRect.toArray().length+" faces");

		return obterDadosFaces(matOfRect);
	}

	public List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect){
		
		List<PropriedadesFace> dados = new ArrayList<PropriedadesFace>();
		
		for (Rect rect : matOfRect.toArray()) {
			
			PropriedadesFace prop = new PropriedadesFace();
			prop.setX(rect.x);
			prop.setY(rect.y);
			prop.setHeight(rect.height);
			prop.setWidth(rect.width);
			
			dados.add(prop);

		}
		
		return dados;
	}
}
