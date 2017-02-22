package br.ufpe.cin.mpos.DaoLocal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.ufpe.cin.mpos.profile.Model.Model;

/**
 * Created by eduar on 30/01/2017.
 */

public class DatabaseController {
    public SQLiteDatabase db;
    public DatabaseManager dm;

    public DatabaseController(Context context){
        dm = DatabaseManager.getInstance(context);
    }

    public String insertData(Model model){
        ContentValues valores;
        long resultado;

        db = dm.getWritableDatabase();
        valores = new ContentValues();
        valores.put(dm.Tech, model.Tech);
        valores.put(dm.AppName, model.AppName);
        Log.d("CarrierInfo", "No Banco: "+model.Carrier);
        valores.put(dm.Carrier, model.Carrier);
        valores.put(dm.Battery, model.Battery);
        valores.put(dm.year, model.year);
        valores.put(dm.CPU, model.CPU);
        valores.put(dm.Bandwidth, model.Bandwidth);
        valores.put(dm.RSSI, model.RSSI);
        valores.put(dm.CPUNuvem, model.CPUNuvem);
        valores.put(dm.Date, model.Date);

        resultado = db.insert(dm.TABELA, null, valores);
        db.close();

        if (resultado ==-1)
            return "Erro ao inserir registro";
        else
            return "Registro Inserido com sucesso";

    }

    public Cursor getData(){
        db = dm.getReadableDatabase();
        Cursor oldestDateCursor = db.query(dm.TABELA, null, null, null, null, null, dm.Date +" DESC LIMIT 1");
        return oldestDateCursor;
    }

}
