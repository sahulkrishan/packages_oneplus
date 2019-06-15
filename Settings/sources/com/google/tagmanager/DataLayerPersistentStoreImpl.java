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
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.tagmanager.DataLayer.PersistentStore.Callback;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class DataLayerPersistentStoreImpl implements PersistentStore {
    private static final String CREATE_MAPS_TABLE = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' STRING NOT NULL, '%s' BLOB NOT NULL, '%s' INTEGER NOT NULL);", new Object[]{MAPS_TABLE, ID_FIELD, "key", VALUE_FIELD, EXPIRE_FIELD});
    private static final String DATABASE_NAME = "google_tagmanager.db";
    private static final String EXPIRE_FIELD = "expires";
    private static final String ID_FIELD = "ID";
    private static final String KEY_FIELD = "key";
    private static final String MAPS_TABLE = "datalayer";
    private static final int MAX_NUM_STORED_ITEMS = 2000;
    private static final String VALUE_FIELD = "value";
    private Clock mClock;
    private final Context mContext;
    private DatabaseHelper mDbHelper;
    private final Executor mExecutor;
    private int mMaxNumStoredItems;

    @VisibleForTesting
    class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context, String databaseName) {
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
            SQLiteDatabase db = null;
            try {
                db = super.getWritableDatabase();
            } catch (SQLiteException e) {
                DataLayerPersistentStoreImpl.this.mContext.getDatabasePath(DataLayerPersistentStoreImpl.DATABASE_NAME).delete();
            }
            if (db == null) {
                return super.getWritableDatabase();
            }
            return db;
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
            if (tablePresent(DataLayerPersistentStoreImpl.MAPS_TABLE, db)) {
                validateColumnsPresent(db);
            } else {
                db.execSQL(DataLayerPersistentStoreImpl.CREATE_MAPS_TABLE);
            }
        }

        private void validateColumnsPresent(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT * FROM datalayer WHERE 0", null);
            Set<String> columns = new HashSet();
            try {
                String[] columnNames = c.getColumnNames();
                for (Object add : columnNames) {
                    columns.add(add);
                }
                if (!columns.remove("key") || !columns.remove(DataLayerPersistentStoreImpl.VALUE_FIELD) || !columns.remove(DataLayerPersistentStoreImpl.ID_FIELD) || !columns.remove(DataLayerPersistentStoreImpl.EXPIRE_FIELD)) {
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

    private static class KeyAndSerialized {
        final String mKey;
        final byte[] mSerialized;

        KeyAndSerialized(String key, byte[] serialized) {
            this.mKey = key;
            this.mSerialized = serialized;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("KeyAndSerialized: key = ");
            stringBuilder.append(this.mKey);
            stringBuilder.append(" serialized hash = ");
            stringBuilder.append(Arrays.hashCode(this.mSerialized));
            return stringBuilder.toString();
        }
    }

    public DataLayerPersistentStoreImpl(Context context) {
        this(context, new Clock() {
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        }, DATABASE_NAME, MAX_NUM_STORED_ITEMS, Executors.newSingleThreadExecutor());
    }

    @VisibleForTesting
    DataLayerPersistentStoreImpl(Context context, Clock clock, String databaseName, int maxNumStoredItems, Executor executor) {
        this.mContext = context;
        this.mClock = clock;
        this.mMaxNumStoredItems = maxNumStoredItems;
        this.mExecutor = executor;
        this.mDbHelper = new DatabaseHelper(this.mContext, databaseName);
    }

    public void saveKeyValues(List<KeyValue> keysAndValues, final long lifetimeInMillis) {
        final List<KeyAndSerialized> serializedKeysAndValues = serializeValues(keysAndValues);
        this.mExecutor.execute(new Runnable() {
            public void run() {
                DataLayerPersistentStoreImpl.this.saveSingleThreaded(serializedKeysAndValues, lifetimeInMillis);
            }
        });
    }

    public void loadSaved(final Callback callback) {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                callback.onKeyValuesLoaded(DataLayerPersistentStoreImpl.this.loadSingleThreaded());
            }
        });
    }

    public void clearKeysWithPrefix(final String keyPrefix) {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                DataLayerPersistentStoreImpl.this.clearKeysWithPrefixSingleThreaded(keyPrefix);
            }
        });
    }

    private List<KeyValue> loadSingleThreaded() {
        try {
            deleteEntriesOlderThan(this.mClock.currentTimeMillis());
            List<KeyValue> unserializeValues = unserializeValues(loadSerialized());
            return unserializeValues;
        } finally {
            closeDatabaseConnection();
        }
    }

    private List<KeyValue> unserializeValues(List<KeyAndSerialized> serialized) {
        List<KeyValue> result = new ArrayList();
        for (KeyAndSerialized keyAndSerialized : serialized) {
            result.add(new KeyValue(keyAndSerialized.mKey, unserialize(keyAndSerialized.mSerialized)));
        }
        return result;
    }

    private List<KeyAndSerialized> serializeValues(List<KeyValue> keysAndValues) {
        List<KeyAndSerialized> result = new ArrayList();
        for (KeyValue keyAndValue : keysAndValues) {
            result.add(new KeyAndSerialized(keyAndValue.mKey, serialize(keyAndValue.mValue)));
        }
        return result;
    }

    private Object unserialize(byte[] bytes) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(byteStream);
            Object readObject = objectInput.readObject();
            try {
                objectInput.close();
                byteStream.close();
            } catch (IOException e) {
            }
            return readObject;
        } catch (IOException e2) {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException e3) {
                    return null;
                }
            }
            byteStream.close();
            return null;
        } catch (ClassNotFoundException e4) {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException e5) {
                    return null;
                }
            }
            byteStream.close();
            return null;
        } catch (Throwable th) {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException e6) {
                }
            }
            byteStream.close();
        }
    }

    private byte[] serialize(Object o) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(byteStream);
            objectOutput.writeObject(o);
            byte[] toByteArray = byteStream.toByteArray();
            try {
                objectOutput.close();
                byteStream.close();
            } catch (IOException e) {
            }
            return toByteArray;
        } catch (IOException e2) {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException e3) {
                    return null;
                }
            }
            byteStream.close();
            return null;
        } catch (Throwable th) {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException e4) {
                }
            }
            byteStream.close();
        }
    }

    private synchronized void saveSingleThreaded(List<KeyAndSerialized> keysAndValues, long lifetimeInMillis) {
        try {
            long now = this.mClock.currentTimeMillis();
            deleteEntriesOlderThan(now);
            makeRoomForEntries(keysAndValues.size());
            writeEntriesToDatabase(keysAndValues, now + lifetimeInMillis);
            closeDatabaseConnection();
        } catch (Throwable th) {
            closeDatabaseConnection();
        }
    }

    private List<KeyAndSerialized> loadSerialized() {
        SQLiteDatabase db = getWritableDatabase("Error opening database for loadSerialized.");
        ArrayList list = new ArrayList();
        if (db == null) {
            return list;
        }
        SQLiteDatabase results = db;
        Cursor results2 = results.query(MAPS_TABLE, new String[]{"key", VALUE_FIELD}, null, null, null, null, ID_FIELD, null);
        while (results2.moveToNext()) {
            try {
                list.add(new KeyAndSerialized(results2.getString(0), results2.getBlob(1)));
            } finally {
                results2.close();
            }
        }
        return list;
    }

    private void writeEntriesToDatabase(List<KeyAndSerialized> keysAndValues, long expireTime) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for writeEntryToDatabase.");
        if (db != null) {
            for (KeyAndSerialized keyAndValue : keysAndValues) {
                ContentValues values = new ContentValues();
                values.put(EXPIRE_FIELD, Long.valueOf(expireTime));
                values.put("key", keyAndValue.mKey);
                values.put(VALUE_FIELD, keyAndValue.mSerialized);
                db.insert(MAPS_TABLE, null, values);
            }
        }
    }

    private void makeRoomForEntries(int count) {
        int entrysOverLimit = (getNumStoredEntries() - this.mMaxNumStoredItems) + count;
        if (entrysOverLimit > 0) {
            List<String> entrysToDelete = peekEntryIds(entrysOverLimit);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DataLayer store full, deleting ");
            stringBuilder.append(entrysToDelete.size());
            stringBuilder.append(" entries to make room.");
            Log.i(stringBuilder.toString());
            deleteEntries((String[]) entrysToDelete.toArray(new String[0]));
        }
    }

    private void clearKeysWithPrefixSingleThreaded(String keyPrefix) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for clearKeysWithPrefix.");
        if (db != null) {
            StringBuilder stringBuilder;
            try {
                String[] strArr = new String[2];
                strArr[0] = keyPrefix;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(keyPrefix);
                stringBuilder2.append(".%");
                strArr[1] = stringBuilder2.toString();
                int deleted = db.delete(MAPS_TABLE, "key = ? OR key LIKE ?", strArr);
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cleared ");
                stringBuilder.append(deleted);
                stringBuilder.append(" items");
                Log.v(stringBuilder.toString());
            } catch (SQLiteException e) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Error deleting entries with key prefix: ");
                stringBuilder.append(keyPrefix);
                stringBuilder.append(" (");
                stringBuilder.append(e);
                stringBuilder.append(").");
                Log.w(stringBuilder.toString());
            } catch (Throwable th) {
                closeDatabaseConnection();
            }
            closeDatabaseConnection();
        }
    }

    private void deleteEntriesOlderThan(long timeInMillis) {
        SQLiteDatabase db = getWritableDatabase("Error opening database for deleteOlderThan.");
        if (db != null) {
            try {
                int deleted = db.delete(MAPS_TABLE, "expires <= ?", new String[]{Long.toString(timeInMillis)});
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Deleted ");
                stringBuilder.append(deleted);
                stringBuilder.append(" expired items");
                Log.v(stringBuilder.toString());
            } catch (SQLiteException e) {
                Log.w("Error deleting old entries.");
            }
        }
    }

    private void deleteEntries(String[] entryIds) {
        if (entryIds != null && entryIds.length != 0) {
            SQLiteDatabase db = getWritableDatabase("Error opening database for deleteEntries.");
            if (db != null) {
                try {
                    db.delete(MAPS_TABLE, String.format("%s in (%s)", new Object[]{ID_FIELD, TextUtils.join(",", Collections.nCopies(entryIds.length, "?"))}), entryIds);
                } catch (SQLiteException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error deleting entries ");
                    stringBuilder.append(Arrays.toString(entryIds));
                    Log.w(stringBuilder.toString());
                }
            }
        }
    }

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
    private java.util.List<java.lang.String> peekEntryIds(int r14) {
        /*
        r13 = this;
        r0 = new java.util.ArrayList;
        r0.<init>();
        if (r14 > 0) goto L_0x000d;
    L_0x0007:
        r1 = "Invalid maxEntries specified. Skipping.";
        com.google.tagmanager.Log.w(r1);
        return r0;
    L_0x000d:
        r1 = "Error opening database for peekEntryIds.";
        r1 = r13.getWritableDatabase(r1);
        if (r1 != 0) goto L_0x0016;
    L_0x0015:
        return r0;
    L_0x0016:
        r2 = 0;
        r11 = r2;
        r3 = "datalayer";
        r2 = "ID";
        r4 = new java.lang.String[]{r2};	 Catch:{ SQLiteException -> 0x005b }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r2 = "%s ASC";
        r9 = 1;
        r9 = new java.lang.Object[r9];	 Catch:{ SQLiteException -> 0x005b }
        r10 = "ID";
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
        r4 = "Error in peekEntries fetching entryIds: ";
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.DataLayerPersistentStoreImpl.peekEntryIds(int):java.util.List");
    }

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
    private int getNumStoredEntries() {
        /*
        r6 = this;
        r0 = 0;
        r1 = "Error opening database for getNumStoredEntries.";
        r1 = r6.getWritableDatabase(r1);
        if (r1 != 0) goto L_0x000a;
    L_0x0009:
        return r0;
    L_0x000a:
        r2 = 0;
        r3 = r2;
        r4 = "SELECT COUNT(*) from datalayer";
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
        r4 = "Error getting numStoredEntries";
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.DataLayerPersistentStoreImpl.getNumStoredEntries():int");
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

    private void closeDatabaseConnection() {
        try {
            this.mDbHelper.close();
        } catch (SQLiteException e) {
        }
    }
}
