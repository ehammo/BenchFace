package cin.ufpe.br.Interfaces;

import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletCutImage {
    List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, byte[] imagem);
}
