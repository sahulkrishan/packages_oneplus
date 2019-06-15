package com.google.tagmanager;

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
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.tagmanager.SimpleNetworkDispatcher.DispatchListener;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.impl.client.DefaultHttpClient;

class PersistentHitStore implements HitStore {
    private static final String CREATE_HITS_TABLE = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' INTEGER NOT NULL, '%s' TEXT NOT NULL,'%s' INTEGER NOT NULL);", new Object[]{HITS_TABLE, HIT_ID, HIT_TIME, HIT_URL, HIT_FIRST_DISPATCH_TIME});
    private static final String DATABASE_FILENAME = "gtm_urls.db";
    @VisibleForTesting
    static final String HITS_TABLE = "gtm_hits";
    static final long HIT_DISPATCH_RETRY_WINDOW = 14400000;
    @VisibleForTesting
    static final String HIT_FIRST_DISPATCH_TIME = "hit_first_send_time";
    @VisibleForTesting
    static final String HIT_ID = "hit_id";
    private static final String HIT_ID_WHERE_CLAUSE = "hit_id=?";
    @VisibleForTesting
    static final String HIT_TIME = "hit_time";
    @VisibleForTesting
    static final String HIT_URL = "hit_url";
    private Clock mClock;
    private final Context mContext;
    private final String mDatabaseName;
    private final UrlDatabaseHelper mDbHelper;
    private volatile Dispatcher mDispatcher;
    private long mLastDeleteStaleHitsTime;
    private final HitStoreStateListener mListener;

    @VisibleForTesting
    class UrlDatabaseHelper extends SQLiteOpenHelper {
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

        UrlDatabaseHelper(Context context, String databaseName) {
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
            if (!this.mBadDatabase || this.mLastDatabaseCheckTime + 3600000 <= PersistentHitStore.this.mClock.currentTimeMillis()) {
                SQLiteDatabase db = null;
                this.mBadDatabase = true;
                this.mLastDatabaseCheckTime = PersistentHitStore.this.mClock.currentTimeMillis();
                try {
                    db = super.getWritableDatabase();
                } catch (SQLiteException e) {
                    PersistentHitStore.this.mContext.getDatabasePath(PersistentHitStore.this.mDatabaseName).delete();
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
            if (tablePresent(PersistentHitStore.HITS_TABLE, db)) {
                validateColumnsPresent(db);
            } else {
                db.execSQL(PersistentHitStore.CREATE_HITS_TABLE);
            }
        }

        private void validateColumnsPresent(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT * FROM gtm_hits WHERE 0", null);
            Set<String> columns = new HashSet();
            try {
                String[] columnNames = c.getColumnNames();
                for (Object add : columnNames) {
                    columns.add(add);
                }
                if (!columns.remove(PersistentHitStore.HIT_ID) || !columns.remove(PersistentHitStore.HIT_URL) || !columns.remove(PersistentHitStore.HIT_TIME) || !columns.remove(PersistentHitStore.HIT_FIRST_DISPATCH_TIME)) {
                    throw new SQLiteException("Database column missing");
                } else if (!columns.isEmpty()) {
                    throw new SQLiteException("Database has extra columns");
                }
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

    @VisibleForTesting
    class StoreDispatchListener implements DispatchListener {
        StoreDispatchListener() {
        }

        public void onHitDispatched(Hit hit) {
            PersistentHitStore.this.deleteHit(hit.getHitId());
        }

        public void onHitPermanentDispatchFailure(Hit hit) {
            PersistentHitStore.this.deleteHit(hit.getHitId());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Permanent failure dispatching hitId: ");
            stringBuilder.append(hit.getHitId());
            Log.v(stringBuilder.toString());
        }

        public void onHitTransientDispatchFailure(Hit hit) {
            long firstDispatchTime = hit.getHitFirstDispatchTime();
            if (firstDispatchTime == 0) {
                PersistentHitStore.this.setHitFirstDispatchTime(hit.getHitId(), PersistentHitStore.this.mClock.currentTimeMillis());
            } else if (PersistentHitStore.HIT_DISPATCH_RETRY_WINDOW + firstDispatchTime < PersistentHitStore.this.mClock.currentTimeMillis()) {
                PersistentHitStore.this.deleteHit(hit.getHitId());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Giving up on failed hitId: ");
                stringBuilder.append(hit.getHitId());
                Log.v(stringBuilder.toString());
            }
        }
    }

    PersistentHitStore(HitStoreStateListener listener, Context ctx) {
        this(listener, ctx, DATABASE_FILENAME);
    }

    @VisibleForTesting
    PersistentHitStore(HitStoreStateListener listener, Context ctx, String databaseName) {
        this.mContext = ctx.getApplicationContext();
        this.mDatabaseName = databaseName;
        this.mListener = listener;
        this.mClock = new Clock() {
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        };
        this.mDbHelper = new UrlDatabaseHelper(this.mContext, this.mDatabaseName);
        this.mDispatcher = new SimpleNetworkDispatcher(new DefaultHttpClient(), this.mContext, new StoreDispatchListener());
        this.mLastDeleteStaleHitsTime = 0;
    }

    @VisibleForTesting
    public void setClock(Clock clock) {
        this.mClock = clock;
    }

    @VisibleForTesting
    public UrlDatabaseHelper getDbHelper() {
        return this.mDbHelper;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setDispatcher(Dispatcher dispatcher) {
        this.mDispatcher = dispatcher;
    }

    public void putHit(long hitTimeInMilliseconds, String path) {
        deleteStaleHits();
        removeOldHitIfFull();
        writeHitToDatabase(hitTimeInMilliseconds, path);
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

    private void writeHitToDatabase(long hitTimeInMilliseconds, String path) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for putHit");
        if (db != null) {
            ContentValues content = new ContentValues();
            content.put(HIT_TIME, Long.valueOf(hitTimeInMilliseconds));
            content.put(HIT_URL, path);
            content.put(HIT_FIRST_DISPATCH_TIME, Integer.valueOf(0));
            try {
                db.insert(HITS_TABLE, null, content);
                this.mListener.reportStoreIsEmpty(false);
            } catch (SQLiteException e) {
                Log.w("Error storing hit");
            }
        }
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
        com.google.tagmanager.Log.w(r1);
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
        r3 = "gtm_hits";
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
        com.google.tagmanager.Log.w(r3);	 Catch:{ all -> 0x0059 }
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.PersistentHitStore.peekHitIds(int):java.util.List");
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0111 A:{Catch:{ all -> 0x00e6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0132  */
    public java.util.List<com.google.tagmanager.Hit> peekHits(int r18) {
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
        r4 = "gtm_hits";
        r0 = "hit_id";
        r3 = "hit_time";
        r5 = "hit_first_send_time";
        r5 = new java.lang.String[]{r0, r3, r5};	 Catch:{ SQLiteException -> 0x0138 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r0 = "%s ASC";
        r14 = 1;
        r3 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x0138 }
        r10 = "hit_id";
        r15 = 0;
        r3[r15] = r10;	 Catch:{ SQLiteException -> 0x0138 }
        r10 = java.lang.String.format(r0, r3);	 Catch:{ SQLiteException -> 0x0138 }
        r11 = java.lang.Integer.toString(r18);	 Catch:{ SQLiteException -> 0x0138 }
        r3 = r12;
        r0 = r3.query(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ SQLiteException -> 0x0138 }
        r13 = r0;
        r0 = new java.util.ArrayList;	 Catch:{ SQLiteException -> 0x0138 }
        r0.<init>();	 Catch:{ SQLiteException -> 0x0138 }
        r1 = r0;
        r0 = r13.moveToFirst();	 Catch:{ SQLiteException -> 0x0138 }
        if (r0 == 0) goto L_0x0063;
    L_0x0047:
        r0 = new com.google.tagmanager.Hit;	 Catch:{ SQLiteException -> 0x0138 }
        r4 = r13.getLong(r15);	 Catch:{ SQLiteException -> 0x0138 }
        r6 = r13.getLong(r14);	 Catch:{ SQLiteException -> 0x0138 }
        r3 = 2;
        r8 = r13.getLong(r3);	 Catch:{ SQLiteException -> 0x0138 }
        r3 = r0;
        r3.<init>(r4, r6, r8);	 Catch:{ SQLiteException -> 0x0138 }
        r1.add(r0);	 Catch:{ SQLiteException -> 0x0138 }
        r0 = r13.moveToNext();	 Catch:{ SQLiteException -> 0x0138 }
        if (r0 != 0) goto L_0x0047;
    L_0x0063:
        if (r13 == 0) goto L_0x0068;
    L_0x0065:
        r13.close();
    L_0x0068:
        r16 = r15;
        r4 = "gtm_hits";
        r0 = "hit_id";
        r3 = "hit_url";
        r5 = new java.lang.String[]{r0, r3};	 Catch:{ SQLiteException -> 0x00e8 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r0 = "%s ASC";
        r3 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x00e8 }
        r10 = "hit_id";
        r3[r15] = r10;	 Catch:{ SQLiteException -> 0x00e8 }
        r10 = java.lang.String.format(r0, r3);	 Catch:{ SQLiteException -> 0x00e8 }
        r11 = java.lang.Integer.toString(r18);	 Catch:{ SQLiteException -> 0x00e8 }
        r3 = r12;
        r0 = r3.query(r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ SQLiteException -> 0x00e8 }
        r13 = r0;
        r0 = r13.moveToFirst();	 Catch:{ SQLiteException -> 0x00e8 }
        if (r0 == 0) goto L_0x00df;
    L_0x0094:
        r3 = r16;
    L_0x0096:
        r0 = r13;
        r0 = (android.database.sqlite.SQLiteCursor) r0;	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r0 = r0.getWindow();	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r4 = r0.getNumRows();	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        if (r4 <= 0) goto L_0x00b1;
    L_0x00a3:
        r4 = r1.get(r3);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r4 = (com.google.tagmanager.Hit) r4;	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r5 = r13.getString(r14);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r4.setHitUrl(r5);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        goto L_0x00cc;
    L_0x00b1:
        r4 = "HitString for hitId %d too large.  Hit will be deleted.";
        r5 = new java.lang.Object[r14];	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r6 = r1.get(r3);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r6 = (com.google.tagmanager.Hit) r6;	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r6 = r6.getHitId();	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r6 = java.lang.Long.valueOf(r6);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r5[r15] = r6;	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        r4 = java.lang.String.format(r4, r5);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        com.google.tagmanager.Log.w(r4);	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
    L_0x00cc:
        r3 = r3 + 1;
        r0 = r13.moveToNext();	 Catch:{ SQLiteException -> 0x00db, all -> 0x00d7 }
        if (r0 != 0) goto L_0x0096;
    L_0x00d4:
        r16 = r3;
        goto L_0x00df;
    L_0x00d7:
        r0 = move-exception;
        r16 = r3;
        goto L_0x0130;
    L_0x00db:
        r0 = move-exception;
        r16 = r3;
        goto L_0x00e9;
        if (r13 == 0) goto L_0x00e5;
    L_0x00e2:
        r13.close();
    L_0x00e5:
        return r1;
    L_0x00e6:
        r0 = move-exception;
        goto L_0x0130;
    L_0x00e8:
        r0 = move-exception;
    L_0x00e9:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00e6 }
        r3.<init>();	 Catch:{ all -> 0x00e6 }
        r4 = "Error in peekHits fetching hit url: ";
        r3.append(r4);	 Catch:{ all -> 0x00e6 }
        r4 = r0.getMessage();	 Catch:{ all -> 0x00e6 }
        r3.append(r4);	 Catch:{ all -> 0x00e6 }
        r3 = r3.toString();	 Catch:{ all -> 0x00e6 }
        com.google.tagmanager.Log.w(r3);	 Catch:{ all -> 0x00e6 }
        r3 = new java.util.ArrayList;	 Catch:{ all -> 0x00e6 }
        r3.<init>();	 Catch:{ all -> 0x00e6 }
        r4 = 0;
        r5 = r1.iterator();	 Catch:{ all -> 0x00e6 }
    L_0x010b:
        r6 = r5.hasNext();	 Catch:{ all -> 0x00e6 }
        if (r6 == 0) goto L_0x0129;
    L_0x0111:
        r6 = r5.next();	 Catch:{ all -> 0x00e6 }
        r6 = (com.google.tagmanager.Hit) r6;	 Catch:{ all -> 0x00e6 }
        r7 = r6.getHitUrl();	 Catch:{ all -> 0x00e6 }
        r7 = android.text.TextUtils.isEmpty(r7);	 Catch:{ all -> 0x00e6 }
        if (r7 == 0) goto L_0x0125;
    L_0x0121:
        if (r4 == 0) goto L_0x0124;
    L_0x0123:
        goto L_0x0129;
    L_0x0124:
        r4 = 1;
    L_0x0125:
        r3.add(r6);	 Catch:{ all -> 0x00e6 }
        goto L_0x010b;
        if (r13 == 0) goto L_0x012f;
    L_0x012c:
        r13.close();
    L_0x012f:
        return r3;
    L_0x0130:
        if (r13 == 0) goto L_0x0135;
    L_0x0132:
        r13.close();
    L_0x0135:
        throw r0;
    L_0x0136:
        r0 = move-exception;
        goto L_0x0158;
    L_0x0138:
        r0 = move-exception;
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0136 }
        r3.<init>();	 Catch:{ all -> 0x0136 }
        r4 = "Error in peekHits fetching hitIds: ";
        r3.append(r4);	 Catch:{ all -> 0x0136 }
        r4 = r0.getMessage();	 Catch:{ all -> 0x0136 }
        r3.append(r4);	 Catch:{ all -> 0x0136 }
        r3 = r3.toString();	 Catch:{ all -> 0x0136 }
        com.google.tagmanager.Log.w(r3);	 Catch:{ all -> 0x0136 }
        if (r13 == 0) goto L_0x0157;
    L_0x0154:
        r13.close();
    L_0x0157:
        return r1;
    L_0x0158:
        if (r13 == 0) goto L_0x015d;
    L_0x015a:
        r13.close();
    L_0x015d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.PersistentHitStore.peekHits(int):java.util.List");
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
        HitStoreStateListener hitStoreStateListener = this.mListener;
        if (getNumStoredHits() == 0) {
            z = true;
        }
        hitStoreStateListener.reportStoreIsEmpty(z);
        return rslt;
    }

    /* Access modifiers changed, original: 0000 */
    public void deleteHits(String[] hitIds) {
        if (hitIds != null && hitIds.length != 0) {
            SQLiteDatabase db = getWritableDatabase("Error opening database for deleteHits.");
            if (db != null) {
                boolean z = true;
                try {
                    db.delete(HITS_TABLE, String.format("HIT_ID in (%s)", new Object[]{TextUtils.join(",", Collections.nCopies(hitIds.length, "?"))}), hitIds);
                    HitStoreStateListener hitStoreStateListener = this.mListener;
                    if (getNumStoredHits() != 0) {
                        z = false;
                    }
                    hitStoreStateListener.reportStoreIsEmpty(z);
                } catch (SQLiteException e) {
                    Log.w("Error deleting hits");
                }
            }
        }
    }

    private void deleteHit(long hitId) {
        deleteHits(new String[]{String.valueOf(hitId)});
    }

    private void setHitFirstDispatchTime(long hitId, long firstDispatchTime) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for getNumStoredHits.");
        if (db != null) {
            ContentValues cv = new ContentValues();
            cv.put(HIT_FIRST_DISPATCH_TIME, Long.valueOf(firstDispatchTime));
            try {
                db.update(HITS_TABLE, cv, HIT_ID_WHERE_CLAUSE, new String[]{String.valueOf(hitId)});
            } catch (SQLiteException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error setting HIT_FIRST_DISPATCH_TIME for hitId: ");
                stringBuilder.append(hitId);
                Log.w(stringBuilder.toString());
                deleteHit(hitId);
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
        r4 = "SELECT COUNT(*) from gtm_hits";
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
        com.google.tagmanager.Log.w(r4);	 Catch:{ all -> 0x0025 }
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.PersistentHitStore.getNumStoredHits():int");
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:7:0x0027, code skipped:
            if (r10 != null) goto L_0x0029;
     */
    /* JADX WARNING: Missing block: B:8:0x0029, code skipped:
            r10.close();
     */
    /* JADX WARNING: Missing block: B:13:0x0035, code skipped:
            if (r10 == null) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:14:0x0038, code skipped:
            return r0;
     */
    public int getNumStoredUntriedHits() {
        /*
        r11 = this;
        r0 = 0;
        r1 = "Error opening database for getNumStoredHits.";
        r1 = r11.getWritableDatabase(r1);
        if (r1 != 0) goto L_0x000a;
    L_0x0009:
        return r0;
    L_0x000a:
        r2 = 0;
        r10 = r2;
        r3 = "gtm_hits";
        r2 = "hit_id";
        r4 = "hit_first_send_time";
        r4 = new java.lang.String[]{r2, r4};	 Catch:{ SQLiteException -> 0x002f }
        r5 = "hit_first_send_time=0";
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r2 = r1;
        r2 = r2.query(r3, r4, r5, r6, r7, r8, r9);	 Catch:{ SQLiteException -> 0x002f }
        r10 = r2;
        r2 = r10.getCount();	 Catch:{ SQLiteException -> 0x002f }
        r0 = r2;
        if (r10 == 0) goto L_0x0038;
    L_0x0029:
        r10.close();
        goto L_0x0038;
    L_0x002d:
        r2 = move-exception;
        goto L_0x0039;
    L_0x002f:
        r2 = move-exception;
        r3 = "Error getting num untried hits";
        com.google.tagmanager.Log.w(r3);	 Catch:{ all -> 0x002d }
        if (r10 == 0) goto L_0x0038;
    L_0x0037:
        goto L_0x0029;
    L_0x0038:
        return r0;
    L_0x0039:
        if (r10 == 0) goto L_0x003e;
    L_0x003b:
        r10.close();
    L_0x003e:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.PersistentHitStore.getNumStoredUntriedHits():int");
    }

    public void dispatch() {
        Log.v("GTM Dispatch running...");
        if (this.mDispatcher.okToDispatch()) {
            List<Hit> hits = peekHits(40);
            if (hits.isEmpty()) {
                Log.v("...nothing to dispatch");
                this.mListener.reportStoreIsEmpty(true);
                return;
            }
            this.mDispatcher.dispatchHits(hits);
            if (getNumStoredUntriedHits() > 0) {
                ServiceManagerImpl.getInstance().dispatch();
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
    public UrlDatabaseHelper getHelper() {
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
