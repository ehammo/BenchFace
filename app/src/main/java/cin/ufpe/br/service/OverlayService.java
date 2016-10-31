package cin.ufpe.br.service;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.List;

import cin.ufpe.br.Interfaces.CloudletOverlay;
import cin.ufpe.br.model.PropriedadesFace;

public class OverlayService implements CloudletOverlay {
	
	public Bitmap juntarImagens(List<PropriedadesFace> dados, Bitmap imagemPrincipal){
		
		for(PropriedadesFace dado: dados){
			imagemPrincipal = juntarUmaImage(imagemPrincipal, dado.getImageCortada(),dado.getX(),dado.getY());
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
	
}
