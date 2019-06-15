package com.android.settings.slices;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import java.util.Locale;

public class SlicesDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_SLICES_TABLE = "CREATE VIRTUAL TABLE slices_index USING fts4(key, title, summary, screentitle, keywords, icon, fragment, controller, platform_slice, slice_type);";
    private static final String DATABASE_NAME = "slices_index.db";
    private static final int DATABASE_VERSION = 2;
    private static final String SHARED_PREFS_TAG = "slices_shared_prefs";
    private static final String TAG = "SlicesDatabaseHelper";
    private static SlicesDatabaseHelper sSingleton;
    private final Context mContext;

    public interface IndexColumns {
        public static final String CONTROLLER = "controller";
        public static final String FRAGMENT = "fragment";
        public static final String ICON_RESOURCE = "icon";
        public static final String KEY = "key";
        public static final String KEYWORDS = "keywords";
        public static final String PLATFORM_SLICE = "platform_slice";
        public static final String SCREENTITLE = "screentitle";
        public static final String SLICE_TYPE = "slice_type";
        public static final String SUMMARY = "summary";
        public static final String TITLE = "title";
    }

    public interface Tables {
        public static final String TABLE_SLICES_INDEX = "slices_index";
    }

    public static synchronized SlicesDatabaseHelper getInstance(Context context) {
        SlicesDatabaseHelper slicesDatabaseHelper;
        synchronized (SlicesDatabaseHelper.class) {
            if (sSingleton == null) {
                sSingleton = new SlicesDatabaseHelper(context.getApplicationContext());
            }
            slicesDatabaseHelper = sSingleton;
        }
        return slicesDatabaseHelper;
    }

    private SlicesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        createDatabases(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Reconstructing DB from ");
            stringBuilder.append(oldVersion);
            stringBuilder.append("to ");
            stringBuilder.append(newVersion);
            Log.d(str, stringBuilder.toString());
            reconstruct(db);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void reconstruct(SQLiteDatabase db) {
        this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().clear().apply();
        dropTables(db);
        createDatabases(db);
    }

    public void setIndexedState() {
        setBuildIndexed();
        setLocaleIndexed();
    }

    public boolean isSliceDataIndexed() {
        return isBuildIndexed() && isLocaleIndexed();
    }

    private void createDatabases(SQLiteDatabase db) {
        db.execSQL(CREATE_SLICES_TABLE);
        Log.d(TAG, "Created databases");
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS slices_index");
    }

    private void setBuildIndexed() {
        this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().putBoolean(getBuildTag(), true).apply();
    }

    private void setLocaleIndexed() {
        this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().putBoolean(Locale.getDefault().toString(), true).apply();
    }

    private boolean isBuildIndexed() {
        if (this.mContext != null) {
            return this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).getBoolean(getBuildTag(), false);
        }
        return false;
    }

    private boolean isLocaleIndexed() {
        return this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).getBoolean(Locale.getDefault().toString(), false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getBuildTag() {
        return Build.FINGERPRINT;
    }
}
