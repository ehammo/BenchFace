package cin.ufpe.br.Interfaces;

import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletOverlay {
    byte[] juntarImagens(List<PropriedadesFace> dados, byte[] imagemPrincipal);
    byte[] juntarUmaImage(byte[] imagemPrincipal, byte[] imagemCortada, int x, int y);
}
