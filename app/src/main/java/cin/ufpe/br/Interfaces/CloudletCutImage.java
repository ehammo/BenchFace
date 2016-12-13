package cin.ufpe.br.Interfaces;

import android.graphics.Bitmap;

import java.util.List;

import br.ufpe.cin.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletCutImage extends CutImage {
    List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, byte[] imagem);
}
