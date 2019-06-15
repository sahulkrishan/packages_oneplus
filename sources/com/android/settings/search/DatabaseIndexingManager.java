package com.android.settings.search;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.search.IndexDatabaseHelper.IndexColumns;
import com.android.settings.search.IndexDatabaseHelper.Tables;
import com.android.settings.search.indexing.IndexData;
import com.android.settings.search.indexing.IndexDataConverter;
import com.android.settings.search.indexing.PreIndexData;
import com.android.settings.search.indexing.PreIndexDataCollector;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DatabaseIndexingManager {
    private static final String LOG_TAG = "DatabaseIndexingManager";
    private PreIndexDataCollector mCollector;
    private Context mContext;
    private IndexDataConverter mConverter;

    public DatabaseIndexingManager(Context context) {
        this.mContext = context;
    }

    public void performIndexing() {
        long startTime = System.currentTimeMillis();
        List<ResolveInfo> providers = this.mContext.getPackageManager().queryIntentContentProviders(new Intent("android.content.action.SEARCH_INDEXABLES_PROVIDER"), 0);
        String localeStr = Locale.getDefault().toString();
        String fingerprint = Build.FINGERPRINT;
        String providerVersionedNames = IndexDatabaseHelper.buildProviderVersionedNames(providers);
        boolean isFullIndex = isFullIndex(this.mContext, localeStr, fingerprint, providerVersionedNames);
        if (isFullIndex) {
            rebuildDatabase();
        }
        PreIndexData indexData = getIndexDataFromProviders(providers, isFullIndex);
        long updateDatabaseStartTime = System.currentTimeMillis();
        updateDatabase(indexData, isFullIndex);
        IndexDatabaseHelper.setLocaleIndexed(this.mContext, localeStr);
        IndexDatabaseHelper.setBuildIndexed(this.mContext, fingerprint);
        IndexDatabaseHelper.setProvidersIndexed(this.mContext, providerVersionedNames);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public PreIndexData getIndexDataFromProviders(List<ResolveInfo> providers, boolean isFullIndex) {
        if (this.mCollector == null) {
            this.mCollector = new PreIndexDataCollector(this.mContext);
        }
        return this.mCollector.collectIndexableData(providers, isFullIndex);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isFullIndex(Context context, String locale, String fingerprint, String providerVersionedNames) {
        return (IndexDatabaseHelper.isLocaleAlreadyIndexed(context, locale) && IndexDatabaseHelper.isBuildIndexed(context, fingerprint) && IndexDatabaseHelper.areProvidersIndexed(context, providerVersionedNames)) ? false : true;
    }

    private void rebuildDatabase() {
        IndexDatabaseHelper.getInstance(this.mContext).reconstruct(getWritableDatabase());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateDatabase(PreIndexData preIndexData, boolean needsReindexing) {
        Map<String, Set<String>> nonIndexableKeys = preIndexData.nonIndexableKeys;
        SQLiteDatabase database = getWritableDatabase();
        if (database == null) {
            Log.w(LOG_TAG, "Cannot indexDatabase Index as I cannot get a writable database");
            return;
        }
        try {
            database.beginTransaction();
            insertIndexData(database, getIndexData(preIndexData));
            if (!needsReindexing) {
                updateDataInDatabase(database, nonIndexableKeys);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<IndexData> getIndexData(PreIndexData data) {
        if (this.mConverter == null) {
            this.mConverter = new IndexDataConverter(this.mContext);
        }
        return this.mConverter.convertPreIndexDataToIndexData(data);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void insertIndexData(SQLiteDatabase database, List<IndexData> indexData) {
        for (IndexData dataRow : indexData) {
            if (!TextUtils.isEmpty(dataRow.normalizedTitle)) {
                ContentValues values = new ContentValues();
                values.put(IndexColumns.DOCID, Integer.valueOf(dataRow.getDocId()));
                values.put("locale", dataRow.locale);
                values.put(IndexColumns.DATA_TITLE, dataRow.updatedTitle);
                values.put(IndexColumns.DATA_TITLE_NORMALIZED, dataRow.normalizedTitle);
                values.put(IndexColumns.DATA_SUMMARY_ON, dataRow.updatedSummaryOn);
                values.put(IndexColumns.DATA_SUMMARY_ON_NORMALIZED, dataRow.normalizedSummaryOn);
                values.put(IndexColumns.DATA_ENTRIES, dataRow.entries);
                values.put(IndexColumns.DATA_KEYWORDS, dataRow.spaceDelimitedKeywords);
                values.put(IndexColumns.CLASS_NAME, dataRow.className);
                values.put(IndexColumns.SCREEN_TITLE, dataRow.screenTitle);
                values.put(IndexColumns.INTENT_ACTION, dataRow.intentAction);
                values.put(IndexColumns.INTENT_TARGET_PACKAGE, dataRow.intentTargetPackage);
                values.put(IndexColumns.INTENT_TARGET_CLASS, dataRow.intentTargetClass);
                values.put("icon", Integer.valueOf(dataRow.iconResId));
                values.put(IndexColumns.ENABLED, Boolean.valueOf(dataRow.enabled));
                values.put(IndexColumns.DATA_KEY_REF, dataRow.key);
                values.put("user_id", Integer.valueOf(dataRow.userId));
                values.put(IndexColumns.PAYLOAD_TYPE, Integer.valueOf(dataRow.payloadType));
                values.put(IndexColumns.PAYLOAD, dataRow.payload);
                database.replaceOrThrow(Tables.TABLE_PREFS_INDEX, null, values);
                if (!(TextUtils.isEmpty(dataRow.className) || TextUtils.isEmpty(dataRow.childClassName))) {
                    ContentValues siteMapPair = new ContentValues();
                    siteMapPair.put("parent_class", dataRow.className);
                    siteMapPair.put("parent_title", dataRow.screenTitle);
                    siteMapPair.put("child_class", dataRow.childClassName);
                    siteMapPair.put("child_title", dataRow.updatedTitle);
                    database.replaceOrThrow(Tables.TABLE_SITE_MAP, null, siteMapPair);
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateDataInDatabase(SQLiteDatabase database, Map<String, Set<String>> nonIndexableKeys) {
        SQLiteDatabase sQLiteDatabase = database;
        Map<String, Set<String>> map = nonIndexableKeys;
        String whereEnabled = "enabled = 1";
        String whereDisabled = "enabled = 0";
        Cursor enabledResults = sQLiteDatabase.query(Tables.TABLE_PREFS_INDEX, DatabaseResultLoader.SELECT_COLUMNS, "enabled = 1", null, null, null, null);
        ContentValues enabledToDisabledValue = new ContentValues();
        enabledToDisabledValue.put(IndexColumns.ENABLED, Integer.valueOf(0));
        while (enabledResults.moveToNext()) {
            String packageName = enabledResults.getString(8);
            if (packageName == null) {
                packageName = this.mContext.getPackageName();
            }
            String key = enabledResults.getString(10);
            Set<String> packageKeys = (Set) map.get(packageName);
            if (packageKeys != null && packageKeys.contains(key)) {
                String whereClause = new StringBuilder();
                whereClause.append("docid = ");
                whereClause.append(enabledResults.getInt(0));
                sQLiteDatabase.update(Tables.TABLE_PREFS_INDEX, enabledToDisabledValue, whereClause.toString(), null);
            }
        }
        enabledResults.close();
        int i = 8;
        Cursor disabledResults = sQLiteDatabase.query(Tables.TABLE_PREFS_INDEX, DatabaseResultLoader.SELECT_COLUMNS, "enabled = 0", null, null, null, null);
        ContentValues disabledToEnabledValue = new ContentValues();
        disabledToEnabledValue.put(IndexColumns.ENABLED, Integer.valueOf(1));
        while (disabledResults.moveToNext()) {
            String packageName2 = disabledResults.getString(i);
            if (packageName2 == null) {
                packageName2 = this.mContext.getPackageName();
            }
            String key2 = disabledResults.getString(10);
            Set<String> packageKeys2 = (Set) map.get(packageName2);
            if (packageKeys2 != null && !packageKeys2.contains(key2)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("docid = ");
                stringBuilder.append(disabledResults.getInt(0));
                sQLiteDatabase.update(Tables.TABLE_PREFS_INDEX, disabledToEnabledValue, stringBuilder.toString(), null);
            }
        }
        disabledResults.close();
    }

    private SQLiteDatabase getWritableDatabase() {
        try {
            return IndexDatabaseHelper.getInstance(this.mContext).getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "Cannot open writable database", e);
            return null;
        }
    }
}
