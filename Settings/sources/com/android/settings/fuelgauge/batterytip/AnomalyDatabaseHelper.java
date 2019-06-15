package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnomalyDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_ANOMALY_TABLE = "CREATE TABLE anomaly(uid INTEGER NOT NULL, package_name TEXT, anomaly_type INTEGER NOT NULL, anomaly_state INTEGER NOT NULL, time_stamp_ms INTEGER NOT NULL,  PRIMARY KEY (uid,anomaly_type,anomaly_state,time_stamp_ms))";
    private static final String DATABASE_NAME = "battery_settings.db";
    private static final int DATABASE_VERSION = 4;
    private static final String TAG = "BatteryDatabaseHelper";
    private static AnomalyDatabaseHelper sSingleton;

    public interface AnomalyColumns {
        public static final String ANOMALY_STATE = "anomaly_state";
        public static final String ANOMALY_TYPE = "anomaly_type";
        public static final String PACKAGE_NAME = "package_name";
        public static final String TIME_STAMP_MS = "time_stamp_ms";
        public static final String UID = "uid";
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        public static final int AUTO_HANDLED = 2;
        public static final int HANDLED = 1;
        public static final int NEW = 0;
    }

    public interface Tables {
        public static final String TABLE_ANOMALY = "anomaly";
    }

    public static synchronized AnomalyDatabaseHelper getInstance(Context context) {
        AnomalyDatabaseHelper anomalyDatabaseHelper;
        synchronized (AnomalyDatabaseHelper.class) {
            if (sSingleton == null) {
                sSingleton = new AnomalyDatabaseHelper(context.getApplicationContext());
            }
            anomalyDatabaseHelper = sSingleton;
        }
        return anomalyDatabaseHelper;
    }

    private AnomalyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    private void bootstrapDB(SQLiteDatabase db) {
        db.execSQL(CREATE_ANOMALY_TABLE);
        Log.i(TAG, "Bootstrapped database");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Detected schema version '");
            stringBuilder.append(oldVersion);
            stringBuilder.append("'. Index needs to be rebuilt for schema version '");
            stringBuilder.append(newVersion);
            stringBuilder.append("'.");
            Log.w(str, stringBuilder.toString());
            reconstruct(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Detected schema version '");
        stringBuilder.append(oldVersion);
        stringBuilder.append("'. Index needs to be rebuilt for schema version '");
        stringBuilder.append(newVersion);
        stringBuilder.append("'.");
        Log.w(str, stringBuilder.toString());
        reconstruct(db);
    }

    public void reconstruct(SQLiteDatabase db) {
        dropTables(db);
        bootstrapDB(db);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS anomaly");
    }
}
