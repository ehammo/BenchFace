package br.ufpe.cin.mpos.DaoLocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String NOME_BANCO = "banco.db";
    public static final String TABELA = "AllProfiles";
    public static final String IDC = "_id";
    public static final String Tech = "Tech";
    public static final String AppName = "App";
    public static final String Carrier = "Carrier";
    public static final String Battery = "Battery";
    public static final String fCPU = "CPU_frequency";
    public static final String CPU = "CPU_usage";
    public static final String SizeInput = "Size_Input";
    public static final String Bandwidth = "Bandwidth";
    public static final String RSSI = "RSSI";
    public static final String CPUNuvem = "CPU_usage_cloud";

    private static final int VERSAO = 1;

    public DatabaseManager(Context context){
        super(context, NOME_BANCO,null,VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE"+TABELA+"("
                + IDC + "integer primary key autoincrement,"
                + Tech + "text,"
                + AppName + "text,"
                + Carrier  + "text,"
                + Battery  + "text,"
                + fCPU  + "text,"
                + CPU  + "text,"
                + SizeInput  + "integer,"
                + Bandwidth  + "integer,"
                + RSSI  + "text,"
                + CPUNuvem  + "text"
                +")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABELA);
        onCreate(db);
    }
}
