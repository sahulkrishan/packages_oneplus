package com.android.settings.search;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.List;

public class IndexDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_INDEX_TABLE = "CREATE VIRTUAL TABLE prefs_index USING fts4(locale, data_rank, data_title, data_title_normalized, data_summary_on, data_summary_on_normalized, data_summary_off, data_summary_off_normalized, data_entries, data_keywords, screen_title, class_name, icon, intent_action, intent_target_package, intent_target_class, enabled, data_key_reference, user_id, payload_type, payload);";
    private static final String CREATE_META_TABLE = "CREATE TABLE meta_index(build VARCHAR(32) NOT NULL)";
    private static final String CREATE_SAVED_QUERIES_TABLE = "CREATE TABLE saved_queries(query VARCHAR(64) NOT NULL, timestamp INTEGER)";
    private static final String CREATE_SITE_MAP_TABLE = "CREATE VIRTUAL TABLE site_map USING fts4(parent_class, child_class, parent_title, child_title)";
    private static final String DATABASE_NAME = "search_index.db";
    private static final int DATABASE_VERSION = 118;
    private static final String INSERT_BUILD_VERSION;
    private static final String PREF_KEY_INDEXED_PROVIDERS = "indexed_providers";
    private static final String SELECT_BUILD_VERSION = "SELECT build FROM meta_index LIMIT 1;";
    private static final String SHARED_PREFS_TAG = "indexing_manager";
    private static final String TAG = "IndexDatabaseHelper";
    private static IndexDatabaseHelper sSingleton;
    private final Context mContext;

    public interface IndexColumns {
        public static final String CLASS_NAME = "class_name";
        public static final String DATA_ENTRIES = "data_entries";
        public static final String DATA_KEYWORDS = "data_keywords";
        public static final String DATA_KEY_REF = "data_key_reference";
        public static final String DATA_RANK = "data_rank";
        public static final String DATA_SUMMARY_OFF = "data_summary_off";
        public static final String DATA_SUMMARY_OFF_NORMALIZED = "data_summary_off_normalized";
        public static final String DATA_SUMMARY_ON = "data_summary_on";
        public static final String DATA_SUMMARY_ON_NORMALIZED = "data_summary_on_normalized";
        public static final String DATA_TITLE = "data_title";
        public static final String DATA_TITLE_NORMALIZED = "data_title_normalized";
        public static final String DOCID = "docid";
        public static final String ENABLED = "enabled";
        public static final String ICON = "icon";
        public static final String INTENT_ACTION = "intent_action";
        public static final String INTENT_TARGET_CLASS = "intent_target_class";
        public static final String INTENT_TARGET_PACKAGE = "intent_target_package";
        public static final String LOCALE = "locale";
        public static final String PAYLOAD = "payload";
        public static final String PAYLOAD_TYPE = "payload_type";
        public static final String SCREEN_TITLE = "screen_title";
        public static final String USER_ID = "user_id";
    }

    public interface MetaColumns {
        public static final String BUILD = "build";
    }

    public interface SavedQueriesColumns {
        public static final String QUERY = "query";
        public static final String TIME_STAMP = "timestamp";
    }

    public interface Tables {
        public static final String TABLE_META_INDEX = "meta_index";
        public static final String TABLE_PREFS_INDEX = "prefs_index";
        public static final String TABLE_SAVED_QUERIES = "saved_queries";
        public static final String TABLE_SITE_MAP = "site_map";
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO meta_index VALUES ('");
        stringBuilder.append(VERSION.INCREMENTAL);
        stringBuilder.append("');");
        INSERT_BUILD_VERSION = stringBuilder.toString();
    }

    public static synchronized IndexDatabaseHelper getInstance(Context context) {
        IndexDatabaseHelper indexDatabaseHelper;
        synchronized (IndexDatabaseHelper.class) {
            if (sSingleton == null) {
                sSingleton = new IndexDatabaseHelper(context);
            }
            indexDatabaseHelper = sSingleton;
        }
        return indexDatabaseHelper;
    }

    public IndexDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 118);
        this.mContext = context.getApplicationContext();
    }

    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    private void bootstrapDB(SQLiteDatabase db) {
        db.execSQL(CREATE_INDEX_TABLE);
        db.execSQL(CREATE_META_TABLE);
        db.execSQL(CREATE_SAVED_QUERIES_TABLE);
        db.execSQL(CREATE_SITE_MAP_TABLE);
        db.execSQL(INSERT_BUILD_VERSION);
        Log.i(TAG, "Bootstrapped database");
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Using schema version: ");
        stringBuilder.append(db.getVersion());
        Log.i(str, stringBuilder.toString());
        if (VERSION.INCREMENTAL.equals(getBuildVersion(db))) {
            Log.i(TAG, "Index is fine");
            return;
        }
        Log.w(TAG, "Index needs to be rebuilt as build-version is not the same");
        reconstruct(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 118) {
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
        this.mContext.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().clear().commit();
        dropTables(db);
        bootstrapDB(db);
    }

    /* JADX WARNING: Missing block: B:6:0x0016, code skipped:
            if (r2 != null) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:7:0x0018, code skipped:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:12:0x0026, code skipped:
            if (r2 == null) goto L_0x0029;
     */
    /* JADX WARNING: Missing block: B:13:0x0029, code skipped:
            return r0;
     */
    private java.lang.String getBuildVersion(android.database.sqlite.SQLiteDatabase r6) {
        /*
        r5 = this;
        r0 = 0;
        r1 = 0;
        r2 = r1;
        r3 = "SELECT build FROM meta_index LIMIT 1;";
        r1 = r6.rawQuery(r3, r1);	 Catch:{ Exception -> 0x001e }
        r2 = r1;
        r1 = r2.moveToFirst();	 Catch:{ Exception -> 0x001e }
        if (r1 == 0) goto L_0x0016;
    L_0x0010:
        r1 = 0;
        r1 = r2.getString(r1);	 Catch:{ Exception -> 0x001e }
        r0 = r1;
    L_0x0016:
        if (r2 == 0) goto L_0x0029;
    L_0x0018:
        r2.close();
        goto L_0x0029;
    L_0x001c:
        r1 = move-exception;
        goto L_0x002a;
    L_0x001e:
        r1 = move-exception;
        r3 = "IndexDatabaseHelper";
        r4 = "Cannot get build version from Index metadata";
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x001c }
        if (r2 == 0) goto L_0x0029;
    L_0x0028:
        goto L_0x0018;
    L_0x0029:
        return r0;
    L_0x002a:
        if (r2 == 0) goto L_0x002f;
    L_0x002c:
        r2.close();
    L_0x002f:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.search.IndexDatabaseHelper.getBuildVersion(android.database.sqlite.SQLiteDatabase):java.lang.String");
    }

    @VisibleForTesting
    static String buildProviderVersionedNames(List<ResolveInfo> providers) {
        StringBuilder sb = new StringBuilder();
        for (ResolveInfo info : providers) {
            sb.append(info.providerInfo.packageName);
            sb.append(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            sb.append(info.providerInfo.applicationInfo.longVersionCode);
            sb.append(',');
        }
        return sb.toString();
    }

    static void setLocaleIndexed(Context context, String locale) {
        context.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().putBoolean(locale, true).apply();
    }

    static void setProvidersIndexed(Context context, String providerVersionedNames) {
        context.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().putString(PREF_KEY_INDEXED_PROVIDERS, providerVersionedNames).apply();
    }

    static boolean isLocaleAlreadyIndexed(Context context, String locale) {
        return context.getSharedPreferences(SHARED_PREFS_TAG, 0).getBoolean(locale, false);
    }

    static boolean areProvidersIndexed(Context context, String providerVersionedNames) {
        return TextUtils.equals(context.getSharedPreferences(SHARED_PREFS_TAG, 0).getString(PREF_KEY_INDEXED_PROVIDERS, null), providerVersionedNames);
    }

    static boolean isBuildIndexed(Context context, String buildNo) {
        return context.getSharedPreferences(SHARED_PREFS_TAG, 0).getBoolean(buildNo, false);
    }

    static void setBuildIndexed(Context context, String buildNo) {
        context.getSharedPreferences(SHARED_PREFS_TAG, 0).edit().putBoolean(buildNo, true).apply();
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS meta_index");
        db.execSQL("DROP TABLE IF EXISTS prefs_index");
        db.execSQL("DROP TABLE IF EXISTS saved_queries");
        db.execSQL("DROP TABLE IF EXISTS site_map");
    }
}
