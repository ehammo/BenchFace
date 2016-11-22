package cin.ufpe.br.Interfaces;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletBlurImage  {
    byte[] DesfocarImagem(byte[] image,int height, int width,int type);
    byte[] Desfocar(byte[] image,int height, int width,int type);
}
