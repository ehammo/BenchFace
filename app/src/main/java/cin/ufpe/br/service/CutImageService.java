package cin.ufpe.br.service;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

public class CutImageService {

	public List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, Bitmap imagem){
		
		for(PropriedadesFace dado : dados){
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Bitmap.createBitmap(imagem, dado.getX(), dado.getY(), dado.getWidth(), dado.getHeight()).compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			dado.setImageCortada(byteArray);
		}
		
		return dados;
	}

}
