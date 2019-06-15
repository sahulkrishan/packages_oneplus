package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.ArrayMap;
import com.android.settings.fuelgauge.batterytip.AnomalyDatabaseHelper.AnomalyColumns;
import com.android.settings.fuelgauge.batterytip.AnomalyDatabaseHelper.Tables;
import com.android.settings.fuelgauge.batterytip.AppInfo.Builder;
import java.util.ArrayList;
import java.util.List;

public class BatteryDatabaseManager {
    private static BatteryDatabaseManager sSingleton;
    private AnomalyDatabaseHelper mDatabaseHelper;

    private BatteryDatabaseManager(Context context) {
        this.mDatabaseHelper = AnomalyDatabaseHelper.getInstance(context);
    }

    public static BatteryDatabaseManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new BatteryDatabaseManager(context);
        }
        return sSingleton;
    }

    /* JADX WARNING: Missing block: B:19:0x0051, code skipped:
            if (r0 != null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:21:?, code skipped:
            $closeResource(r1, r0);
     */
    public synchronized boolean insertAnomaly(int r8, java.lang.String r9, int r10, int r11, long r12) {
        /*
        r7 = this;
        monitor-enter(r7);
        r0 = r7.mDatabaseHelper;	 Catch:{ all -> 0x0057 }
        r0 = r0.getWritableDatabase();	 Catch:{ all -> 0x0057 }
        r1 = 0;
        r2 = new android.content.ContentValues;	 Catch:{ Throwable -> 0x004f }
        r2.<init>();	 Catch:{ Throwable -> 0x004f }
        r3 = "uid";
        r4 = java.lang.Integer.valueOf(r8);	 Catch:{ Throwable -> 0x004f }
        r2.put(r3, r4);	 Catch:{ Throwable -> 0x004f }
        r3 = "package_name";
        r2.put(r3, r9);	 Catch:{ Throwable -> 0x004f }
        r3 = "anomaly_type";
        r4 = java.lang.Integer.valueOf(r10);	 Catch:{ Throwable -> 0x004f }
        r2.put(r3, r4);	 Catch:{ Throwable -> 0x004f }
        r3 = "anomaly_state";
        r4 = java.lang.Integer.valueOf(r11);	 Catch:{ Throwable -> 0x004f }
        r2.put(r3, r4);	 Catch:{ Throwable -> 0x004f }
        r3 = "time_stamp_ms";
        r4 = java.lang.Long.valueOf(r12);	 Catch:{ Throwable -> 0x004f }
        r2.put(r3, r4);	 Catch:{ Throwable -> 0x004f }
        r3 = "anomaly";
        r4 = 4;
        r3 = r0.insertWithOnConflict(r3, r1, r2, r4);	 Catch:{ Throwable -> 0x004f }
        r5 = -1;
        r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1));
        if (r3 == 0) goto L_0x0045;
    L_0x0043:
        r3 = 1;
        goto L_0x0046;
    L_0x0045:
        r3 = 0;
    L_0x0046:
        if (r0 == 0) goto L_0x004b;
    L_0x0048:
        $closeResource(r1, r0);	 Catch:{ all -> 0x0057 }
    L_0x004b:
        monitor-exit(r7);
        return r3;
    L_0x004d:
        r2 = move-exception;
        goto L_0x0051;
    L_0x004f:
        r1 = move-exception;
        throw r1;	 Catch:{ all -> 0x004d }
    L_0x0051:
        if (r0 == 0) goto L_0x0056;
    L_0x0053:
        $closeResource(r1, r0);	 Catch:{ all -> 0x0057 }
    L_0x0056:
        throw r2;	 Catch:{ all -> 0x0057 }
    L_0x0057:
        r8 = move-exception;
        monitor-exit(r7);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager.insertAnomaly(int, java.lang.String, int, int, long):boolean");
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
                return;
            } catch (Throwable th) {
                x0.addSuppressed(th);
                return;
            }
        }
        x1.close();
    }

    public synchronized List<AppInfo> queryAllAnomalies(long timestampMsAfter, int state) {
        ArrayList appInfos;
        Cursor cursor;
        Throwable th;
        Throwable th2;
        Throwable th3;
        synchronized (this) {
            appInfos = new ArrayList();
            SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
            try {
                String[] projection = new String[]{"package_name", AnomalyColumns.ANOMALY_TYPE, "uid"};
                String orderBy = "time_stamp_ms DESC";
                ArrayMap mAppInfoBuilders = new ArrayMap();
                String selection = "time_stamp_ms > ? AND anomaly_state = ? ";
                cursor = db.query(Tables.TABLE_ANOMALY, projection, "time_stamp_ms > ? AND anomaly_state = ? ", new String[]{String.valueOf(timestampMsAfter), String.valueOf(state)}, null, null, "time_stamp_ms DESC");
                while (cursor.moveToNext()) {
                    try {
                        int uid = cursor.getInt(cursor.getColumnIndex("uid"));
                        if (!mAppInfoBuilders.containsKey(Integer.valueOf(uid))) {
                            mAppInfoBuilders.put(Integer.valueOf(uid), new Builder().setUid(uid).setPackageName(cursor.getString(cursor.getColumnIndex("package_name"))));
                        }
                        ((Builder) mAppInfoBuilders.get(Integer.valueOf(uid))).addAnomalyType(cursor.getInt(cursor.getColumnIndex(AnomalyColumns.ANOMALY_TYPE)));
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                if (cursor != null) {
                    $closeResource(null, cursor);
                }
                for (Integer cursor2 : mAppInfoBuilders.keySet()) {
                    appInfos.add(((Builder) mAppInfoBuilders.get(cursor2)).build());
                }
                if (db != null) {
                    $closeResource(null, db);
                }
            } catch (Throwable th5) {
                if (db != null) {
                    $closeResource(th3, db);
                }
            }
        }
        return appInfos;
        if (cursor != null) {
            $closeResource(th, cursor);
        }
        throw th2;
    }

    /* JADX WARNING: Missing block: B:15:0x0024, code skipped:
            if (r0 != null) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:17:?, code skipped:
            $closeResource(r1, r0);
     */
    public synchronized void deleteAllAnomaliesBeforeTimeStamp(long r8) {
        /*
        r7 = this;
        monitor-enter(r7);
        r0 = r7.mDatabaseHelper;	 Catch:{ all -> 0x002a }
        r0 = r0.getWritableDatabase();	 Catch:{ all -> 0x002a }
        r1 = 0;
        r2 = "anomaly";
        r3 = "time_stamp_ms < ?";
        r4 = 1;
        r4 = new java.lang.String[r4];	 Catch:{ Throwable -> 0x0022 }
        r5 = 0;
        r6 = java.lang.String.valueOf(r8);	 Catch:{ Throwable -> 0x0022 }
        r4[r5] = r6;	 Catch:{ Throwable -> 0x0022 }
        r0.delete(r2, r3, r4);	 Catch:{ Throwable -> 0x0022 }
        if (r0 == 0) goto L_0x001e;
    L_0x001b:
        $closeResource(r1, r0);	 Catch:{ all -> 0x002a }
    L_0x001e:
        monitor-exit(r7);
        return;
    L_0x0020:
        r2 = move-exception;
        goto L_0x0024;
    L_0x0022:
        r1 = move-exception;
        throw r1;	 Catch:{ all -> 0x0020 }
    L_0x0024:
        if (r0 == 0) goto L_0x0029;
    L_0x0026:
        $closeResource(r1, r0);	 Catch:{ all -> 0x002a }
    L_0x0029:
        throw r2;	 Catch:{ all -> 0x002a }
    L_0x002a:
        r8 = move-exception;
        monitor-exit(r7);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager.deleteAllAnomaliesBeforeTimeStamp(long):void");
    }

    /* JADX WARNING: Missing block: B:18:0x0067, code skipped:
            if (r2 != null) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:20:?, code skipped:
            $closeResource(r3, r2);
     */
    public synchronized void updateAnomalies(java.util.List<com.android.settings.fuelgauge.batterytip.AppInfo> r11, int r12) {
        /*
        r10 = this;
        monitor-enter(r10);
        r0 = r11.isEmpty();	 Catch:{ all -> 0x006f }
        if (r0 != 0) goto L_0x006d;
    L_0x0007:
        r0 = r11.size();	 Catch:{ all -> 0x006f }
        r1 = new java.lang.String[r0];	 Catch:{ all -> 0x006f }
        r2 = 0;
    L_0x000e:
        if (r2 >= r0) goto L_0x001d;
    L_0x0010:
        r3 = r11.get(r2);	 Catch:{ all -> 0x006f }
        r3 = (com.android.settings.fuelgauge.batterytip.AppInfo) r3;	 Catch:{ all -> 0x006f }
        r3 = r3.packageName;	 Catch:{ all -> 0x006f }
        r1[r2] = r3;	 Catch:{ all -> 0x006f }
        r2 = r2 + 1;
        goto L_0x000e;
    L_0x001d:
        r2 = r10.mDatabaseHelper;	 Catch:{ all -> 0x006f }
        r2 = r2.getWritableDatabase();	 Catch:{ all -> 0x006f }
        r3 = 0;
        r4 = new android.content.ContentValues;	 Catch:{ Throwable -> 0x0065 }
        r4.<init>();	 Catch:{ Throwable -> 0x0065 }
        r5 = "anomaly_state";
        r6 = java.lang.Integer.valueOf(r12);	 Catch:{ Throwable -> 0x0065 }
        r4.put(r5, r6);	 Catch:{ Throwable -> 0x0065 }
        r5 = "anomaly";
        r6 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0065 }
        r6.<init>();	 Catch:{ Throwable -> 0x0065 }
        r7 = "package_name IN (";
        r6.append(r7);	 Catch:{ Throwable -> 0x0065 }
        r7 = ",";
        r8 = r11.size();	 Catch:{ Throwable -> 0x0065 }
        r9 = "?";
        r8 = java.util.Collections.nCopies(r8, r9);	 Catch:{ Throwable -> 0x0065 }
        r7 = android.text.TextUtils.join(r7, r8);	 Catch:{ Throwable -> 0x0065 }
        r6.append(r7);	 Catch:{ Throwable -> 0x0065 }
        r7 = ")";
        r6.append(r7);	 Catch:{ Throwable -> 0x0065 }
        r6 = r6.toString();	 Catch:{ Throwable -> 0x0065 }
        r2.update(r5, r4, r6, r1);	 Catch:{ Throwable -> 0x0065 }
        if (r2 == 0) goto L_0x006d;
    L_0x005f:
        $closeResource(r3, r2);	 Catch:{ all -> 0x006f }
        goto L_0x006d;
    L_0x0063:
        r4 = move-exception;
        goto L_0x0067;
    L_0x0065:
        r3 = move-exception;
        throw r3;	 Catch:{ all -> 0x0063 }
    L_0x0067:
        if (r2 == 0) goto L_0x006c;
    L_0x0069:
        $closeResource(r3, r2);	 Catch:{ all -> 0x006f }
    L_0x006c:
        throw r4;	 Catch:{ all -> 0x006f }
    L_0x006d:
        monitor-exit(r10);
        return;
    L_0x006f:
        r11 = move-exception;
        monitor-exit(r10);
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager.updateAnomalies(java.util.List, int):void");
    }
}
