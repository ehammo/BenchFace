package cin.ufpe.br.Interfaces;

import org.opencv.core.Mat;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletBlurImage extends BlurImage {
    byte[] DesfocarImagem(byte[] image,int height, int width,int type);
    byte[] Desfocar(byte[] image,int height, int width,int type);
}
