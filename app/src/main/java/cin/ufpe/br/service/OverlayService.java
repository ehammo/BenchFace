package cin.ufpe.br.service;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.util.List;

import cin.ufpe.br.Interfaces.CloudletOverlay;
import cin.ufpe.br.model.PropriedadesFace;

public class OverlayService implements CloudletOverlay {
	
	public Bitmap juntarImagens(List<PropriedadesFace> dados, Bitmap imagemPrincipal){
		
		for(PropriedadesFace dado: dados){
			imagemPrincipal = juntarUmaImage(imagemPrincipal, BitmapFactory.decodeByteArray(dado.getImageCortada(),0,dado.getImageCortada().length),dado.getX(),dado.getY());
		}
		
		return imagemPrincipal;
		
	}

	public Bitmap juntarUmaImage(Bitmap imagemPrincipal, Bitmap imagemCortada, int x, int y) {
		Bitmap overlayBitmap = Bitmap.createBitmap(imagemPrincipal.getWidth(), imagemPrincipal.getHeight(), imagemPrincipal.getConfig());
		Canvas canvas = new Canvas(overlayBitmap);
        canvas.drawBitmap(imagemPrincipal,0,0,null);
        canvas.drawBitmap(imagemCortada,x,y,null);
        return overlayBitmap;
    }

	public byte[] juntarImagens(List<PropriedadesFace> dados, byte[] imagemPrincipal){

		return null;

	}

	public byte[] juntarUmaImage(byte[] imagemPrincipal, byte[] imagemCortada, int x, int y) {

		return null;
	}

}
