package cin.ufpe.br.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.mpos.offload.Remotable;

public interface CloudletUpdateService {

    @Remotable(value = Remotable.Offload.STATIC, status = true)
    HashMap<String, byte[]> updateClassificators(String[] newInstances);
}
