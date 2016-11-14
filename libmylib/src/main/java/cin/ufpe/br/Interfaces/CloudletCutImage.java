package cin.ufpe.br.Interfaces;


import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletCutImage extends CutImage {

    byte[] soma(int x, int y);

//    List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, Bitmap imagem);
}
