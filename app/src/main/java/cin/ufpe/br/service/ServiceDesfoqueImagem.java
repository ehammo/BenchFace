package cin.ufpe.br.service;

import android.graphics.Bitmap;


import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import org.opencv.android.Utils;

public class ServiceDesfoqueImagem {
	
	public Mat DesfocarImagem(Mat mat){
		
		mat = Desfocar(mat);

		return mat;
	}
	
	private Mat Desfocar(Mat image){
			      
		Mat destination = new Mat(image.rows(),image.cols(),image.type());
			      
		Imgproc.GaussianBlur(image, destination,new Size(45,45), 0);
		
		return destination;
	}
	
}
