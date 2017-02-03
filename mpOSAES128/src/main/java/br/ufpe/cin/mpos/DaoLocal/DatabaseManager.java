package br.ufpe.cin.mpos.DaoLocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    public static final String BandwidthDown = "BandwidthDown";
    public static final String BandwidthUp = "BandwidthUp";
    public static final String RSSI = "RSSI";
    public static final String CPUNuvem = "CPU_usage_cloud";
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
        String sql = "CREATE TABLE "+TABELA+"("
                + IDC + " integer primary key autoincrement,"
                + Tech + " text,"
                + AppName + " text,"
                + Carrier  + " text,"
                + Battery  + " text,"
                + fCPU  + " text,"
                + CPU  + " text,"
                + SizeInput  + " text,"
                + BandwidthDown  + " text,"
                + BandwidthUp  + " text,"
                + RSSI  + " text,"
                + Date + " text,"
                + CPUNuvem  + " text"
                +")";
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
