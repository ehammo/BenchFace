package cin.ufpe.br.service;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;
import cin.ufpe.br.model.PropriedadesFace2;

public class CutImageService {

	public List<PropriedadesFace2> CortarImagem(List<PropriedadesFace> dados, BufferedImage imagem){
		List<PropriedadesFace2> resp = new ArrayList<PropriedadesFace2>();
		for(PropriedadesFace dado : dados){
			PropriedadesFace2 resp2 = new PropriedadesFace2();
			resp2.setImageCortada(imagem.getSubimage(dado.getX(), dado.getY(), dado.getWidth(), dado.getHeight()));
            resp2.setX(dado.getX());
            resp2.setY(dado.getY());
            resp2.setHeight(dado.getHeight());
            resp2.setWidth(dado.getWidth());
			resp.add(resp2);
		}

		return resp;
	}

}
