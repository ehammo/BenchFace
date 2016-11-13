package cin.ufpe.br.Interfaces;

import android.graphics.Bitmap;

import java.util.List;

import br.ufc.mdcc.mpos.offload.Remotable;
import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletCutImage extends CutImage {

    byte[] soma(int x, int y);

    @Remotable(value = Remotable.Offload.STATIC, status = true) 
    List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, Bitmap imagem);
}
