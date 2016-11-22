package cin.ufpe.br.service;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayInputStream;

import cin.ufpe.br.Interfaces.CloudletBlurImage;

public class BlurImageService implements CloudletBlurImage {
	
	public byte[] DesfocarImagem(byte[] mat,int height, int width,int type){
		
		mat = Desfocar(mat,height, width,type);

		return mat;
	}
	
	public byte[] Desfocar(byte[] image,int height, int width, int type){


		Mat destination = new Mat(height,width,type);

		Imgproc.GaussianBlur(converterParaMat(image,height,width), destination,new Size(45,45), 0);
		
		return converterParaImage(destination);
	}

	public static byte[] converterParaImage(Mat image){

		MatOfByte bytemat = new MatOfByte();

		Imgcodecs.imencode(".jpg", image, bytemat);

		byte[] bytes = bytemat.toArray();

		return bytes;
	}

	public static Mat converterParaMat(byte[] pixels,int height, int width) {

		Mat mat = new Mat(height, width, CvType.CV_8UC3);
		mat.put(0, 0, pixels);

		return mat;
	}
}
