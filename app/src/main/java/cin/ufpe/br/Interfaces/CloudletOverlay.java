package cin.ufpe.br.Interfaces;

import android.graphics.Bitmap;

import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletOverlay extends Overlay {
    Bitmap juntarImagens(List<PropriedadesFace> dados, Bitmap imagemPrincipal);
    Bitmap juntarUmaImage(Bitmap imagemPrincipal, Bitmap imagemCortada, int x, int y);
}
