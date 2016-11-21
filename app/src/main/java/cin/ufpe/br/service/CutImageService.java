package cin.ufpe.br.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import cin.ufpe.br.Interfaces.CloudletCutImage;
import cin.ufpe.br.model.PropriedadesFace;

public class CutImageService implements CloudletCutImage {

	public List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, Bitmap imagem){
		
		for(PropriedadesFace dado : dados){
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Bitmap.createBitmap(imagem, dado.getX(), dado.getY(), dado.getWidth(), dado.getHeight()).compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			dado.setImageCortada(byteArray);
		}
		
		return dados;
	}

	public byte[] soma(int x, int y){
		byte[] data = ByteBuffer.allocate(4).putInt(x).array();
        return data;
	}
}
