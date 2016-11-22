package cin.ufpe.br.service;


import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace2;

public class OverlayService {

	public BufferedImage juntarImagens(List<PropriedadesFace2> dados, BufferedImage imagemPrincipal){

		for(PropriedadesFace2 dado: dados){
			imagemPrincipal = juntarUmaImage(imagemPrincipal, dado.getImageCortada(),dado.getX(),dado.getY());
		}

		return imagemPrincipal;

	}

	public static BufferedImage juntarUmaImage(BufferedImage imagemPrincipal,
											   BufferedImage imagemCortada, int x, int y) {

		Graphics2D g = imagemPrincipal.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(imagemPrincipal, 0,0, null);

		g.drawImage(imagemCortada, x, y, null);

		g.dispose();
		return imagemPrincipal;
	}

}
