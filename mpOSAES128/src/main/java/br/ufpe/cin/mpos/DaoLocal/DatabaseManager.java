package br.ufpe.cin.mpos.DaoLocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String NOME_BANCO = "banco.db";
    public static final String TABELA = "AllProfiles";
    public static final String IDC = "_id";
    public static final String Tech = "TipoRede";
    public static final String InputSize = "TamanhoDados(Kb)";
    public static final String AppName = "Aplicativo";
    public static final String Battery = "BateriaSmartphone";
    public static final String year = "HardSmartphone";
    public static final String CPU = "CPUSmartphone";
    public static final String Bandwidth = "LarguraBandaRede";
    public static final String RSSI = "RSSIRede";
    public static final String CPUNuvem = "CPUNuvem";
    public static final String result = "Resultado";
    public static final String Date = "date_column";

    private static DatabaseManager sInstance;

    private static final int VERSAO = 1;

    public DatabaseManager(Context context){
        super(context, "/mnt/sdcard/"+NOME_BANCO,null,VERSAO);
    }

    public static synchronized DatabaseManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("sqlLite","tentando criar banco");
        String sql = "CREATE TABLE "+TABELA+"("
                + IDC + " integer primary key autoincrement,"
                + AppName + " text,"
                + "["+InputSize + "] int,"
                + year + " text,"
                + Battery  + " text,"
                + CPU  + " text,"
                + Tech + " text,"
                + Bandwidth  + " text,"
                + RSSI  + " text,"
                + CPUNuvem + " text,"
                + result + " text,"
                + Date + " text"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("dropando","Vou executar o sql");
        db.execSQL("DROP TABLE IF EXISTS " + TABELA);
        Log.d("dropando","Excecutei o sql");
        onCreate(db);
    }


}
