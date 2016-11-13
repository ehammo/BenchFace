package cin.ufpe.br.Interfaces;

import android.graphics.Bitmap;

import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CutImage {
    byte[] soma(int x, int y);
    List<PropriedadesFace> CortarImagem(List<PropriedadesFace> dados, Bitmap imagem);
}
