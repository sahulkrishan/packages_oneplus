package com.google.analytics.tracking.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.android.settingslib.datetime.ZoneGetter;
import com.android.settingslib.net.UidDetailProvider;
import com.google.android.gms.analytics.internal.Command;
import com.google.android.gms.common.util.VisibleForTesting;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.impl.client.DefaultHttpClient;

class PersistentAnalyticsStore implements AnalyticsStore {
    private static final String CREATE_HITS_TABLE = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' INTEGER NOT NULL, '%s' TEXT NOT NULL, '%s' TEXT NOT NULL, '%s' INTEGER);", new Object[]{HITS_TABLE, HIT_ID, HIT_TIME, HIT_URL, HIT_STRING, HIT_APP_ID});
    private static final String DATABASE_FILENAME = "google_analytics_v2.db";
    @VisibleForTesting
    static final String HITS_TABLE = "hits2";
    @VisibleForTesting
    static final String HIT_APP_ID = "hit_app_id";
    @VisibleForTesting
    static final String HIT_ID = "hit_id";
    @VisibleForTesting
    static final String HIT_STRING = "hit_string";
    @VisibleForTesting
    static final String HIT_TIME = "hit_time";
    @VisibleForTesting
    static final String HIT_URL = "hit_url";
    private Clock mClock;
    private final Context mContext;
    private final String mDatabaseName;
    private final AnalyticsDatabaseHelper mDbHelper;
    private volatile Dispatcher mDispatcher;
    private long mLastDeleteStaleHitsTime;
    private final AnalyticsStoreStateListener mListener;

    @VisibleForTesting
    class AnalyticsDatabaseHelper extends SQLiteOpenHelper {
        private boolean mBadDatabase;
        private long mLastDatabaseCheckTime = 0;

        /* Access modifiers changed, original: 0000 */
        public boolean isBadDatabase() {
            return this.mBadDatabase;
        }

        /* Access modifiers changed, original: 0000 */
        public void setBadDatabase(boolean badDatabase) {
            this.mBadDatabase = badDatabase;
        }

        AnalyticsDatabaseHelper(Context context, String databaseName) {
            super(context, databaseName, null, 1);
        }

        private boolean tablePresent(String table, SQLiteDatabase db) {
            Cursor cursor = null;
            try {
                SQLiteDatabase sQLiteDatabase = db;
                cursor = sQLiteDatabase.query("SQLITE_MASTER", new String[]{ZoneGetter.KEY_DISPLAYNAME}, "name=?", new String[]{table}, null, null, null);
                boolean moveToFirst = cursor.moveToFirst();
                if (cursor != null) {
                    cursor.close();
                }
                return moveToFirst;
            } catch (SQLiteException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error querying for table ");
                stringBuilder.append(table);
                Log.w(stringBuilder.toString());
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        public SQLiteDatabase getWritableDatabase() {
            if (!this.mBadDatabase || this.mLastDatabaseCheckTime + 3600000 <= PersistentAnalyticsStore.this.mClock.currentTimeMillis()) {
                SQLiteDatabase db = null;
                this.mBadDatabase = true;
                this.mLastDatabaseCheckTime = PersistentAnalyticsStore.this.mClock.currentTimeMillis();
                try {
                    db = super.getWritableDatabase();
                } catch (SQLiteException e) {
                    PersistentAnalyticsStore.this.mContext.getDatabasePath(PersistentAnalyticsStore.this.mDatabaseName).delete();
                }
                if (db == null) {
                    db = super.getWritableDatabase();
                }
                this.mBadDatabase = false;
                return db;
            }
            throw new SQLiteException("Database creation failed");
        }

        public void onOpen(SQLiteDatabase db) {
            if (VERSION.SDK_INT < 15) {
                Cursor cursor = db.rawQuery("PRAGMA journal_mode=memory", null);
                try {
                    cursor.moveToFirst();
                } finally {
                    cursor.close();
                }
            }
            if (tablePresent(PersistentAnalyticsStore.HITS_TABLE, db)) {
                validateColumnsPresent(db);
            } else {
                db.execSQL(PersistentAnalyticsStore.CREATE_HITS_TABLE);
            }
        }

        private void validateColumnsPresent(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT * FROM hits2 WHERE 0", null);
            Set<String> columns = new HashSet();
            try {
                String[] columnNames = c.getColumnNames();
                for (Object add : columnNames) {
                    columns.add(add);
                }
                if (columns.remove(PersistentAnalyticsStore.HIT_ID) && columns.remove(PersistentAnalyticsStore.HIT_URL) && columns.remove(PersistentAnalyticsStore.HIT_STRING) && columns.remove(PersistentAnalyticsStore.HIT_TIME)) {
                    boolean needsAppId = columns.remove(PersistentAnalyticsStore.HIT_APP_ID) ^ 1;
                    if (!columns.isEmpty()) {
                        throw new SQLiteException("Database has extra columns");
                    } else if (needsAppId) {
                        db.execSQL("ALTER TABLE hits2 ADD COLUMN hit_app_id");
                        return;
                    } else {
                        return;
                    }
                }
                throw new SQLiteException("Database column missing");
            } finally {
                c.close();
            }
        }

        public void onCreate(SQLiteDatabase db) {
            FutureApis.setOwnerOnlyReadWrite(db.getPath());
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    PersistentAnalyticsStore(AnalyticsStoreStateListener listener, Context ctx) {
        this(listener, ctx, DATABASE_FILENAME);
    }

    @VisibleForTesting
    PersistentAnalyticsStore(AnalyticsStoreStateListener listener, Context ctx, String databaseName) {
        this.mContext = ctx.getApplicationContext();
        this.mDatabaseName = databaseName;
        this.mListener = listener;
        this.mClock = new Clock() {
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        };
        this.mDbHelper = new AnalyticsDatabaseHelper(this.mContext, this.mDatabaseName);
        this.mDispatcher = new SimpleNetworkDispatcher(new DefaultHttpClient(), this.mContext);
        this.mLastDeleteStaleHitsTime = 0;
    }

    @VisibleForTesting
    public void setClock(Clock clock) {
        this.mClock = clock;
    }

    @VisibleForTesting
    public AnalyticsDatabaseHelper getDbHelper() {
        return this.mDbHelper;
    }

    public void setDispatch(boolean dispatch) {
        this.mDispatcher = dispatch ? new SimpleNetworkDispatcher(new DefaultHttpClient(), this.mContext) : new NoopDispatcher();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setDispatcher(Dispatcher dispatcher) {
        this.mDispatcher = dispatcher;
    }

    public void clearHits(long appId) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for clearHits");
        if (db != null) {
            boolean z = false;
            if (appId == 0) {
                db.delete(HITS_TABLE, null, null);
            } else {
                db.delete(HITS_TABLE, "hit_app_id = ?", new String[]{Long.valueOf(appId).toString()});
            }
            AnalyticsStoreStateListener analyticsStoreStateListener = this.mListener;
            if (getNumStoredHits() == 0) {
                z = true;
            }
            analyticsStoreStateListener.reportStoreIsEmpty(z);
        }
    }

    public void putHit(Map<String, String> wireFormatParams, long hitTimeInMilliseconds, String path, Collection<Command> commands) {
        deleteStaleHits();
        removeOldHitIfFull();
        fillVersionParameter(wireFormatParams, commands);
        writeHitToDatabase(wireFormatParams, hitTimeInMilliseconds, path);
    }

    private void fillVersionParameter(Map<String, String> wireFormatParams, Collection<Command> commands) {
        String clientVersionParam = "&_v".substring(1);
        if (commands != null) {
            for (Command command : commands) {
                if (Command.APPEND_VERSION.equals(command.getId())) {
                    wireFormatParams.put(clientVersionParam, command.getValue());
                    return;
                }
            }
        }
    }

    private void removeOldHitIfFull() {
        int hitsOverLimit = (getNumStoredHits() + UidDetailProvider.OTHER_USER_RANGE_START) + 1;
        if (hitsOverLimit > 0) {
            List<String> hitsToDelete = peekHitIds(hitsOverLimit);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Store full, deleting ");
            stringBuilder.append(hitsToDelete.size());
            stringBuilder.append(" hits to make room.");
            Log.v(stringBuilder.toString());
            deleteHits((String[]) hitsToDelete.toArray(new String[0]));
        }
    }

    private void writeHitToDatabase(Map<String, String> hit, long hitTimeInMilliseconds, String path) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for putHit");
        if (db != null) {
            ContentValues content = new ContentValues();
            content.put(HIT_STRING, generateHitString(hit));
            content.put(HIT_TIME, Long.valueOf(hitTimeInMilliseconds));
            long appSystemId = 0;
            if (hit.containsKey(Fields.ANDROID_APP_UID)) {
                try {
                    appSystemId = Long.parseLong((String) hit.get(Fields.ANDROID_APP_UID));
                } catch (NumberFormatException e) {
                }
            }
            content.put(HIT_APP_ID, Long.valueOf(appSystemId));
            if (path == null) {
                path = "http://www.google-analytics.com/collect";
            }
            if (path.length() == 0) {
                Log.w("Empty path: not sending hit");
                return;
            }
            content.put(HIT_URL, path);
            try {
                db.insert(HITS_TABLE, null, content);
                this.mListener.reportStoreIsEmpty(false);
            } catch (SQLiteException e2) {
                Log.w("Error storing hit");
            }
        }
    }

    static String generateHitString(Map<String, String> urlParams) {
        List<String> keyAndValues = new ArrayList(urlParams.size());
        for (Entry<String, String> entry : urlParams.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(HitBuilder.encode((String) entry.getKey()));
            stringBuilder.append("=");
            stringBuilder.append(HitBuilder.encode((String) entry.getValue()));
            keyAndValues.add(stringBuilder.toString());
        }
        return TextUtils.join("&", keyAndValues);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:13:0x0053, code skipped:
            if (r11 != null) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:14:0x0055, code skipped:
            r11.close();
     */
    /* JADX WARNING: Missing block: B:19:0x0074, code skipped:
            if (r11 == null) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:20:0x0077, code skipped:
            return r0;
     */
    public java.util.List<java.lang.String> peekHitIds(int r14) {
        /*
        r13 = this;
        r0 = new java.util.ArrayList;
        r0.<init>();
        if (r14 > 0) goto L_0x000d;
    L_0x0007:
        r1 = "Invalid maxHits specified. Skipping";
        com.google.analytics.tracking.android.Log.w(r1);
        return r0;
    L_0x000d:
        r1 = "Error opening database for peekHitIds.";
        r1 = r13.getWritableDatabase(r1);
        if (r1 != 0) goto L_0x0016;
    L_0x0015:
        return r0;
    L_0x0016:
        r2 = 0;
        r11 = r2;
        r3 = "hits2";
        r2 = "hit_id";
        r4 = new java.lang.String[]{r2};	 Catch:{ SQLiteException -> 0x005b }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r2 = "%s ASC";
        r9 = 1;
        r9 = new java.lang.Object[r9];	 Catch:{ SQLiteException -> 0x005b }
        r10 = "hit_id";
        r12 = 0;
        r9[r12] = r10;	 Catch:{ SQLiteException -> 0x005b }
        r9 = java.lang.String.format(r2, r9);	 Catch:{ SQLiteException -> 0x005b }
        r10 = java.lang.Integer.toString(r14);	 Catch:{ SQLiteException -> 0x005b }
        r2 = r1;
        r2 = r2.query(r3, r4, r5, r6, r7, r8, r9, r10);	 Catch:{ SQLiteException -> 0x005b }
        r11 = r2;
        r2 = r11.moveToFirst();	 Catch:{ SQLiteException -> 0x005b }
        if (r2 == 0) goto L_0x0053;
    L_0x0042:
        r2 = r11.getLong(r12);	 Catch:{ SQLiteException -> 0x005b }
        r2 = java.lang.String.valueOf(r2);	 Catch:{ SQLiteException -> 0x005b }
        r0.add(r2);	 Catch:{ SQLiteException -> 0x005b }
        r2 = r11.moveToNext();	 Catch:{ SQLiteException -> 0x005b }
        if (r2 != 0) goto L_0x0042;
    L_0x0053:
        if (r11 == 0) goto L_0x0077;
    L_0x0055:
        r11.close();
        goto L_0x0077;
    L_0x0059:
        r2 = move-exception;
        goto L_0x0078;
    L_0x005b:
        r2 = move-exception;
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0059 }
        r3.<init>();	 Catch:{ all -> 0x0059 }
        r4 = "Error in peekHits fetching hitIds: ";
        r3.append(r4);	 Catch:{ all -> 0x0059 }
        r4 = r2.getMessage();	 Catch:{ all -> 0x0059 }
        r3.append(r4);	 Catch:{ all -> 0x0059 }
        r3 = r3.toString();	 Catch:{ all -> 0x0059 }
        com.google.analytics.tracking.android.Log.w(r3);	 Catch:{ all -> 0x0059 }
        if (r11 == 0) goto L_0x0077;
    L_0x0076:
        goto L_0x0055;
    L_0x0077:
        return r0;
    L_0x0078:
        if (r11 == 0) goto L_0x007d;
    L_0x007a:
        r11.close();
    L_0x007d:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.PersistentAnalyticsStore.peekHitIds(int):java.util.List");
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x011b A:{Catch:{ all -> 0x00f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0136  */
    public java.util.List<com.google.analytics.tracking.android.Hit> peekHits(int r18) {
        /*
        r17 = this;
        r0 = new java.util.ArrayList;
        r0.<init>();
        r1 = r0;
        r0 = "Error opening database for peekHits";
        r2 = r17;
        r12 = r2.getWritableDatabase(r0);
        if (r12 != 0) goto L_0x0011;
    L_0x0010:
        return r1;
    L_0x0011:
        r0 = 0;
        r13 = r0;
        r4 = "hits2";
        r0 = "hit_id";
        r3 = "hit_time";
        r5 = new java.lang.String[]{r0, r3};	 Catch:{ SQLiteException -> 0x0142 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r0 = "%s ASC";
        r14 = 1;
        r3 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x0142 }
        r10 = "hit_id";
        r15 = 0;
        r3[r15] = r10;	 Catch:{ SQLiteException -> 0x0142 }
        r10 = java.lang.String.format(r0, r3);	 Catch:{ SQLiteException -> 0x0142 }
        r11 = java.lang.Integer.toString(r18);	 Catch:{ SQLiteException -> 0x0142 }
        r3 = r12;
        r0 = r3.query(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ SQLiteException -> 0x0142 }
        r13 = r0;
        r0 = new java.util.ArrayList;	 Catch:{ SQLiteException -> 0x0142 }
        r0.<init>();	 Catch:{ SQLiteException -> 0x0142 }
        r1 = r0;
        r0 = r13.moveToFirst();	 Catch:{ SQLiteException -> 0x0142 }
        if (r0 == 0) goto L_0x005d;
    L_0x0045:
        r0 = new com.google.analytics.tracking.android.Hit;	 Catch:{ SQLiteException -> 0x0142 }
        r4 = 0;
        r5 = r13.getLong(r15);	 Catch:{ SQLiteException -> 0x0142 }
        r7 = r13.getLong(r14);	 Catch:{ SQLiteException -> 0x0142 }
        r3 = r0;
        r3.<init>(r4, r5, r7);	 Catch:{ SQLiteException -> 0x0142 }
        r1.add(r0);	 Catch:{ SQLiteException -> 0x0142 }
        r0 = r13.moveToNext();	 Catch:{ SQLiteException -> 0x0142 }
        if (r0 != 0) goto L_0x0045;
    L_0x005d:
        if (r13 == 0) goto L_0x0062;
    L_0x005f:
        r13.close();
    L_0x0062:
        r16 = r15;
        r4 = "hits2";
        r0 = "hit_id";
        r3 = "hit_string";
        r5 = "hit_url";
        r5 = new java.lang.String[]{r0, r3, r5};	 Catch:{ SQLiteException -> 0x00f2 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r0 = "%s ASC";
        r3 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x00f2 }
        r10 = "hit_id";
        r3[r15] = r10;	 Catch:{ SQLiteException -> 0x00f2 }
        r10 = java.lang.String.format(r0, r3);	 Catch:{ SQLiteException -> 0x00f2 }
        r11 = java.lang.Integer.toString(r18);	 Catch:{ SQLiteException -> 0x00f2 }
        r3 = r12;
        r0 = r3.query(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ SQLiteException -> 0x00f2 }
        r13 = r0;
        r0 = r13.moveToFirst();	 Catch:{ SQLiteException -> 0x00f2 }
        if (r0 == 0) goto L_0x00e9;
    L_0x0090:
        r3 = r16;
    L_0x0092:
        r0 = r13;
        r0 = (android.database.sqlite.SQLiteCursor) r0;	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r0 = r0.getWindow();	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4 = r0.getNumRows();	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        if (r4 <= 0) goto L_0x00bb;
    L_0x009f:
        r4 = r1.get(r3);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4 = (com.google.analytics.tracking.android.Hit) r4;	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r5 = r13.getString(r14);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4.setHitString(r5);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4 = r1.get(r3);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4 = (com.google.analytics.tracking.android.Hit) r4;	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r5 = 2;
        r5 = r13.getString(r5);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4.setHitUrl(r5);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        goto L_0x00d6;
    L_0x00bb:
        r4 = "HitString for hitId %d too large.  Hit will be deleted.";
        r5 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r6 = r1.get(r3);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r6 = (com.google.analytics.tracking.android.Hit) r6;	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r6 = r6.getHitId();	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r6 = java.lang.Long.valueOf(r6);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r5[r15] = r6;	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        r4 = java.lang.String.format(r4, r5);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        com.google.analytics.tracking.android.Log.w(r4);	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
    L_0x00d6:
        r3 = r3 + 1;
        r0 = r13.moveToNext();	 Catch:{ SQLiteException -> 0x00e5, all -> 0x00e1 }
        if (r0 != 0) goto L_0x0092;
    L_0x00de:
        r16 = r3;
        goto L_0x00e9;
    L_0x00e1:
        r0 = move-exception;
        r16 = r3;
        goto L_0x013a;
    L_0x00e5:
        r0 = move-exception;
        r16 = r3;
        goto L_0x00f3;
        if (r13 == 0) goto L_0x00ef;
    L_0x00ec:
        r13.close();
    L_0x00ef:
        return r1;
    L_0x00f0:
        r0 = move-exception;
        goto L_0x013a;
    L_0x00f2:
        r0 = move-exception;
    L_0x00f3:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00f0 }
        r3.<init>();	 Catch:{ all -> 0x00f0 }
        r4 = "Error in peekHits fetching hitString: ";
        r3.append(r4);	 Catch:{ all -> 0x00f0 }
        r4 = r0.getMessage();	 Catch:{ all -> 0x00f0 }
        r3.append(r4);	 Catch:{ all -> 0x00f0 }
        r3 = r3.toString();	 Catch:{ all -> 0x00f0 }
        com.google.analytics.tracking.android.Log.w(r3);	 Catch:{ all -> 0x00f0 }
        r3 = new java.util.ArrayList;	 Catch:{ all -> 0x00f0 }
        r3.<init>();	 Catch:{ all -> 0x00f0 }
        r4 = 0;
        r5 = r1.iterator();	 Catch:{ all -> 0x00f0 }
    L_0x0115:
        r6 = r5.hasNext();	 Catch:{ all -> 0x00f0 }
        if (r6 == 0) goto L_0x0133;
    L_0x011b:
        r6 = r5.next();	 Catch:{ all -> 0x00f0 }
        r6 = (com.google.analytics.tracking.android.Hit) r6;	 Catch:{ all -> 0x00f0 }
        r7 = r6.getHitParams();	 Catch:{ all -> 0x00f0 }
        r7 = android.text.TextUtils.isEmpty(r7);	 Catch:{ all -> 0x00f0 }
        if (r7 == 0) goto L_0x012f;
    L_0x012b:
        if (r4 == 0) goto L_0x012e;
    L_0x012d:
        goto L_0x0133;
    L_0x012e:
        r4 = 1;
    L_0x012f:
        r3.add(r6);	 Catch:{ all -> 0x00f0 }
        goto L_0x0115;
        if (r13 == 0) goto L_0x0139;
    L_0x0136:
        r13.close();
    L_0x0139:
        return r3;
    L_0x013a:
        if (r13 == 0) goto L_0x013f;
    L_0x013c:
        r13.close();
    L_0x013f:
        throw r0;
    L_0x0140:
        r0 = move-exception;
        goto L_0x0162;
    L_0x0142:
        r0 = move-exception;
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0140 }
        r3.<init>();	 Catch:{ all -> 0x0140 }
        r4 = "Error in peekHits fetching hitIds: ";
        r3.append(r4);	 Catch:{ all -> 0x0140 }
        r4 = r0.getMessage();	 Catch:{ all -> 0x0140 }
        r3.append(r4);	 Catch:{ all -> 0x0140 }
        r3 = r3.toString();	 Catch:{ all -> 0x0140 }
        com.google.analytics.tracking.android.Log.w(r3);	 Catch:{ all -> 0x0140 }
        if (r13 == 0) goto L_0x0161;
    L_0x015e:
        r13.close();
    L_0x0161:
        return r1;
    L_0x0162:
        if (r13 == 0) goto L_0x0167;
    L_0x0164:
        r13.close();
    L_0x0167:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.PersistentAnalyticsStore.peekHits(int):java.util.List");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setLastDeleteStaleHitsTime(long timeInMilliseconds) {
        this.mLastDeleteStaleHitsTime = timeInMilliseconds;
    }

    /* Access modifiers changed, original: 0000 */
    public int deleteStaleHits() {
        long now = this.mClock.currentTimeMillis();
        boolean z = false;
        if (now <= this.mLastDeleteStaleHitsTime + SettingsUtil.MILLIS_OF_DAY) {
            return 0;
        }
        this.mLastDeleteStaleHitsTime = now;
        SQLiteDatabase db = getWritableDatabase("Error opening database for deleteStaleHits.");
        if (db == null) {
            return 0;
        }
        long lastGoodTime = this.mClock.currentTimeMillis() - 2592000000L;
        int rslt = db.delete(HITS_TABLE, "HIT_TIME < ?", new String[]{Long.toString(lastGoodTime)});
        AnalyticsStoreStateListener analyticsStoreStateListener = this.mListener;
        if (getNumStoredHits() == 0) {
            z = true;
        }
        analyticsStoreStateListener.reportStoreIsEmpty(z);
        return rslt;
    }

    /* Access modifiers changed, original: 0000 */
    @Deprecated
    public void deleteHits(Collection<Hit> hits) {
        if (hits == null || hits.isEmpty()) {
            Log.w("Empty/Null collection passed to deleteHits.");
            return;
        }
        String[] hitIds = new String[hits.size()];
        int i = 0;
        for (Hit h : hits) {
            int i2 = i + 1;
            hitIds[i] = String.valueOf(h.getHitId());
            i = i2;
        }
        deleteHits(hitIds);
    }

    /* Access modifiers changed, original: 0000 */
    public void deleteHits(String[] hitIds) {
        if (hitIds == null || hitIds.length == 0) {
            Log.w("Empty hitIds passed to deleteHits.");
            return;
        }
        SQLiteDatabase db = getWritableDatabase("Error opening database for deleteHits.");
        if (db != null) {
            boolean z = true;
            try {
                db.delete(HITS_TABLE, String.format("HIT_ID in (%s)", new Object[]{TextUtils.join(",", Collections.nCopies(hitIds.length, "?"))}), hitIds);
                AnalyticsStoreStateListener analyticsStoreStateListener = this.mListener;
                if (getNumStoredHits() != 0) {
                    z = false;
                }
                analyticsStoreStateListener.reportStoreIsEmpty(z);
            } catch (SQLiteException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error deleting hits ");
                stringBuilder.append(hitIds);
                Log.w(stringBuilder.toString());
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:9:0x001f, code skipped:
            if (r3 != null) goto L_0x0021;
     */
    /* JADX WARNING: Missing block: B:10:0x0021, code skipped:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:15:0x002d, code skipped:
            if (r3 == null) goto L_0x0030;
     */
    /* JADX WARNING: Missing block: B:16:0x0030, code skipped:
            return r0;
     */
    public int getNumStoredHits() {
        /*
        r6 = this;
        r0 = 0;
        r1 = "Error opening database for getNumStoredHits.";
        r1 = r6.getWritableDatabase(r1);
        if (r1 != 0) goto L_0x000a;
    L_0x0009:
        return r0;
    L_0x000a:
        r2 = 0;
        r3 = r2;
        r4 = "SELECT COUNT(*) from hits2";
        r2 = r1.rawQuery(r4, r2);	 Catch:{ SQLiteException -> 0x0027 }
        r3 = r2;
        r2 = r3.moveToFirst();	 Catch:{ SQLiteException -> 0x0027 }
        if (r2 == 0) goto L_0x001f;
    L_0x0019:
        r2 = 0;
        r4 = r3.getLong(r2);	 Catch:{ SQLiteException -> 0x0027 }
        r0 = (int) r4;
    L_0x001f:
        if (r3 == 0) goto L_0x0030;
    L_0x0021:
        r3.close();
        goto L_0x0030;
    L_0x0025:
        r2 = move-exception;
        goto L_0x0031;
    L_0x0027:
        r2 = move-exception;
        r4 = "Error getting numStoredHits";
        com.google.analytics.tracking.android.Log.w(r4);	 Catch:{ all -> 0x0025 }
        if (r3 == 0) goto L_0x0030;
    L_0x002f:
        goto L_0x0021;
    L_0x0030:
        return r0;
    L_0x0031:
        if (r3 == 0) goto L_0x0036;
    L_0x0033:
        r3.close();
    L_0x0036:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.PersistentAnalyticsStore.getNumStoredHits():int");
    }

    public void dispatch() {
        Log.v("Dispatch running...");
        if (this.mDispatcher.okToDispatch()) {
            List<Hit> hits = peekHits(40);
            if (hits.isEmpty()) {
                Log.v("...nothing to dispatch");
                this.mListener.reportStoreIsEmpty(true);
                return;
            }
            int hitsDispatched = this.mDispatcher.dispatchHits(hits);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sent ");
            stringBuilder.append(hitsDispatched);
            stringBuilder.append(" of ");
            stringBuilder.append(hits.size());
            stringBuilder.append(" hits");
            Log.v(stringBuilder.toString());
            deleteHits(hits.subList(0, Math.min(hitsDispatched, hits.size())));
            if (hitsDispatched == hits.size() && getNumStoredHits() > 0) {
                GAServiceManager.getInstance().dispatchLocalHits();
            }
        }
    }

    public Dispatcher getDispatcher() {
        return this.mDispatcher;
    }

    public void close() {
        try {
            this.mDbHelper.getWritableDatabase().close();
            this.mDispatcher.close();
        } catch (SQLiteException e) {
            Log.w("Error opening database for close");
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AnalyticsDatabaseHelper getHelper() {
        return this.mDbHelper;
    }

    private SQLiteDatabase getWritableDatabase(String errorMessage) {
        SQLiteDatabase db = null;
        try {
            return this.mDbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.w(errorMessage);
            return null;
        }
    }
}
