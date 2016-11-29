package cin.ufpe.br.service;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import cin.ufpe.br.model.PropriedadesFace2;

public class DetectFacesService implements CloudletDetectFaces {
	private static final String TAG="log";

    public PropriedadesFace detectarFaces(String alg, byte[] originalImageBytes){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		PropriedadesFace ret = new PropriedadesFace();
		try {
            CascadeService cascadeService = new CascadeService();
            CascadeClassifier cascadeClassifier = cascadeService.loadCascade(alg);
			ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
			BufferedImage originalImage = ImageIO.read(in);
			Mat mat = converterParaMat(originalImage);
            MatOfRect matOfRect = new MatOfRect();
            System.out.print("Detecting faces");
			cascadeClassifier.detectMultiScale(mat, matOfRect);
			System.out.print("\nEle executou todo na nuvem e detectou "+matOfRect.size()+" faces\n");


			BlurImageService serviceBlur = new BlurImageService();

			//desfoca a imagem
            System.out.print("Bluring image");
			BufferedImage imagemBorrada = serviceBlur.DesfocarImagem(mat);

			//corta os rostos da imagem desfocada,
			CutImageService serviceCrop = new CutImageService();
            System.out.print("Cutting blurred faces");
			List<PropriedadesFace2> propsFaces = serviceCrop.CortarImagem(obterDadosFaces(matOfRect), imagemBorrada);
			OverlayService serviceOverlay = new OverlayService();

			//"cola" os rostos desfocados sobre a imagem original
            System.out.print("Overlaying");
			BufferedImage imagemCorteDesfoque = serviceOverlay.juntarImagens(propsFaces, originalImage);
			ret.setFaces(propsFaces.size());

            System.out.print("Transforming to bytes");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( imagemCorteDesfoque, "jpg", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();

            ret.setImagemFinal(imageInByte);

		}catch(Exception e){
            System.out.println(e.getStackTrace());
		}
		finally {
			return ret;
		}
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

	public static Mat converterParaMat(BufferedImage im) {

		byte[] pixels = ((DataBufferByte)im.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, pixels);

		return mat;
	}
}
