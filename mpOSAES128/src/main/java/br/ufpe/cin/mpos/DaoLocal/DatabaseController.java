package br.ufpe.cin.mpos.DaoLocal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import br.ufpe.cin.mpos.profile.Model.Model;

/**
 * Created by eduar on 30/01/2017.
 */

public class DatabaseController {
    private SQLiteDatabase db;
    private DatabaseManager dm;

    public DatabaseController(Context context){
        dm = new DatabaseManager(context);
    }

    public String insertData(Model model){
        ContentValues valores;
        long resultado;

        db = dm.getWritableDatabase();
        valores = new ContentValues();
        valores.put(dm.IDC, model.IDC);
        valores.put(dm.Tech, model.Tech);
        valores.put(dm.AppName, model.AppName);
        valores.put(dm.Carrier, model.Carrier);
        valores.put(dm.Battery, model.Battery);
        valores.put(dm.fCPU, model.fCPU);
        valores.put(dm.CPU, model.CPU);
        valores.put(dm.SizeInput, model.SizeInput);
        valores.put(dm.Bandwidth, model.Bandwidth);
        valores.put(dm.RSSI, model.RSSI);
        valores.put(dm.CPUNuvem, model.CPUNuvem);

        resultado = db.insert(dm.TABELA, null, valores);
        db.close();

        if (resultado ==-1)
            return "Erro ao inserir registro";
        else
            return "Registro Inserido com sucesso";

    }

    public Cursor getData(){
        String info = "";
        Cursor oldestDateCursor = db.query(dm.TABELA, null, null, null, null, null, dm.Date +" ASC LIMIT 1");
        return oldestDateCursor;
    }

}
