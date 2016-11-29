package cin.ufpe.br.service;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BlurImageService {

	public BufferedImage DesfocarImagem(Mat mat){

		mat = Desfocar(mat);

		return converterParaImage(mat);
	}

	private Mat Desfocar(Mat image){

		Mat destination = new Mat(image.rows(),image.cols(),image.type());

		Imgproc.GaussianBlur(image, destination,new Size(45,45), 0);

		return destination;
	}

	public static BufferedImage converterParaImage(Mat image){

		MatOfByte bytemat = new MatOfByte();

		Imgcodecs.imencode(".jpg", image, bytemat);

		byte[] bytes = bytemat.toArray();

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);

		BufferedImage img=null;

		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return img;
	}

	public static Mat converterParaMat(byte[] pixels,int height, int width) {

		Mat mat = new Mat(height, width, CvType.CV_8UC3);
		mat.put(0, 0, pixels);

		return mat;
	}
}
