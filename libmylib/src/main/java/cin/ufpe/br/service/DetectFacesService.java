package cin.ufpe.br.service;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import cin.ufpe.br.Interfaces.CloudletDetectFaces;
import cin.ufpe.br.Util.Input;
import cin.ufpe.br.model.PropriedadesFace;

/***
 * 
 * @author Rafael Guinho
 *
 */
public class DetectFacesService implements CloudletDetectFaces {
	private static final String TAG="log";

	public MatOfRect detectarFaces(Input i){
		CascadeClassifier cascadeClassifier = i.getCascadeClassifier();
		Mat mat = i.getMat();
		MatOfRect matOfRect = new MatOfRect();
		cascadeClassifier.detectMultiScale(mat, matOfRect);
 //       Log.d(TAG, "Detected "+matOfRect.toArray().length+" faces");

		return matOfRect;
	}
	
	/***
	 * 
	 * @param matOfRect
	 * @return
	 */
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
