package cin.ufpe.br.model;

import android.content.Context;

/**
 * Created by eduardo on 10/06/2017.
 */

public class ToLoadCascadeModel {

    private Context mContext;
    private int ElementIndex;
    private String algorithm;

    public ToLoadCascadeModel(Context x, int alg, String algorithm){
        mContext=x;
        ElementIndex=alg;
        this.algorithm=algorithm;
    }

    public Context getmContext(){
        return mContext;
    }

    public int getElementIndex(){
        return ElementIndex;
    }

    public String getAlgorithm(){
        return algorithm;
    }

}
