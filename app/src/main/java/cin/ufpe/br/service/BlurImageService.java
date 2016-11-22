package cin.ufpe.br.service;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import cin.ufpe.br.Interfaces.CloudletBlurImage;

public class BlurImageService implements CloudletBlurImage {
	
	public Mat DesfocarImagem(Mat mat){
		
		mat = Desfocar(mat);

		return mat;
	}
	
	public Mat Desfocar(Mat image){
			      
		Mat destination = new Mat(image.rows(),image.cols(),image.type());
			      
		Imgproc.GaussianBlur(image, destination,new Size(45,45), 0);
		
		return destination;
	}

	public byte[] DesfocarImagem(byte[] im,int x, int y, int z){

		return null;
	}

	public byte[] Desfocar(byte[] image,int x, int y, int z){


		return null;
	}
	
}
