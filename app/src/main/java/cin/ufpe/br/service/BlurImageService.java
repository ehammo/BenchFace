package cin.ufpe.br.service;


import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class BlurImageService {
	
	public Mat DesfocarImagem(Mat mat){
		
		mat = Desfocar(mat);

		return mat;
	}
	
	public Mat Desfocar(Mat image){
			      
		Mat destination = new Mat(image.rows(),image.cols(),image.type());
			      
		Imgproc.GaussianBlur(image, destination,new Size(45,45), 0);
		
		return destination;
	}

	
}
