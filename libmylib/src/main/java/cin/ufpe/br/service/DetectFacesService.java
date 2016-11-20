package cin.ufpe.br.service;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.portable.InputStream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;

import cin.ufpe.br.Interfaces.CloudletDetectFaces;
import cin.ufpe.br.model.PropriedadesFace;
import sun.rmi.runtime.Log;

/***
 * 
 * @author Rafael Guinho
 *
 */
public class DetectFacesService implements CloudletDetectFaces {
	private static final String TAG="log";

	public MatOfRect detectarFaces(String alg, byte[] originalImageBytes){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        MatOfRect matOfRect = new MatOfRect();
		try {
            CascadeService cascadeService = new CascadeService();
            CascadeClassifier cascadeClassifier = cascadeService.loadCascade(alg);
			ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
			BufferedImage originalImage = ImageIO.read(in);
			Mat mat = converterParaMat(originalImage);
			cascadeClassifier.detectMultiScale(mat, matOfRect);
			System.out.print("\nEle executou todo na nuvem e detectou "+matOfRect.size()+" faces\n");
		}catch(Exception e){
            System.out.println(e.getStackTrace());
		}
		finally {
			return matOfRect;
		}
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

	public static Mat converterParaMat(BufferedImage im) {

		byte[] pixels = ((DataBufferByte)im.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, pixels);

		return mat;
	}
}
