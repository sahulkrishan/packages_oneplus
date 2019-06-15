package com.oneplus.settings.ringtone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.System;
import android.util.Log;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;

public class OPRingtoneManager {
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX_RO = "ro.config.";
    public static final String EXTRA_RINGTONE_DEFAULT_URI = "android.intent.extra.ringtone.DEFAULT_URI";
    public static final String EXTRA_RINGTONE_EXISTING_URI = "android.intent.extra.ringtone.EXISTING_URI";
    public static final String EXTRA_RINGTONE_FOR_CONTACTS = "ringtone_for_contacts";
    public static final String EXTRA_RINGTONE_PICKED_URI = "android.intent.extra.ringtone.PICKED_URI";
    public static final String EXTRA_RINGTONE_SHOW_DEFAULT = "android.intent.extra.ringtone.SHOW_DEFAULT";
    public static final String EXTRA_RINGTONE_SIMID = "oneplus.intent.extra.ringtone.simid";
    public static final String EXTRA_RINGTONE_TITLE = "android.intent.extra.ringtone.TITLE";
    public static final String EXTRA_RINGTONE_TYPE = "android.intent.extra.ringtone.TYPE";
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS;
    public static final int MAX_NUM_RINGTONES = 2;
    private static final String OP_RINGTONE1_DEFUALT = "op_ringtone1_df";
    private static final String OP_RINGTONE2_DEFUALT = "op_ringtone2_df";
    private static final String OP_RINGTONE_DEFUALT = "op_ringtone_df";
    private static final String OP_SIM_SWITCH = "op_sim_sw";
    public static final String RINGTONE_2 = "ringtone_2";
    private static final String TAG = "RingtoneManager";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_ALL = 15;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_SMS = 8;
    public static final int URI_COLUMN_INDEX = 2;
    private static Uri mDefaultUri = null;
    private Activity mActivity;
    private Context mContext;
    private Cursor mCursor;
    private final List<String> mFilterColumns = new ArrayList();
    private int mType = 1;

    public static class ResultRing {
        Uri ringUri;
        String title;

        public ResultRing(String t, Uri uri) {
            this.title = t;
            this.ringUri = uri;
        }
    }

    static {
        String[] strArr = new String[5];
        strArr[0] = OPFirewallUtils._ID;
        strArr[1] = "title";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"");
        stringBuilder.append(Media.INTERNAL_CONTENT_URI);
        stringBuilder.append("\"");
        strArr[2] = stringBuilder.toString();
        strArr[3] = "title_key";
        strArr[4] = "_data";
        INTERNAL_COLUMNS = strArr;
    }

    public OPRingtoneManager(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity;
        setType(this.mType);
    }

    public OPRingtoneManager(Context context) {
        this.mContext = context;
        setType(this.mType);
    }

    public void setType(int type) {
        if (this.mCursor == null) {
            this.mType = type;
            setFilterColumnsList(type);
            return;
        }
        throw new IllegalStateException("Setting filter columns should be done before querying for ringtones.");
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = this.mFilterColumns;
        columns.clear();
        if ((type & 1) != 0) {
            columns.add("is_ringtone");
        }
        if ((type & 2) != 0) {
            columns.add("is_notification");
        }
        if ((type & 8) != 0) {
            columns.add("is_notification");
        }
        if ((type & 4) != 0) {
            columns.add("is_alarm");
        }
    }

    public int inferStreamType() {
        int i = this.mType;
        if (i != 2) {
            if (i == 4) {
                return 4;
            }
            if (i != 8) {
                return 2;
            }
        }
        return 5;
    }

    public Cursor getCursor() {
        if (this.mCursor != null && this.mCursor.requery()) {
            return this.mCursor;
        }
        Cursor internalRingtones = getInternalRingtones();
        this.mCursor = internalRingtones;
        return internalRingtones;
    }

    private Cursor getInternalRingtones() {
        String whereString = new StringBuilder();
        whereString.append(constructBooleanTrueWhereClause(this.mFilterColumns));
        whereString.append(" and (");
        whereString.append("_data");
        whereString.append(" like ? or ");
        whereString.append("_data");
        whereString.append(" like ? )");
        return query(Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, whereString.toString(), constructWhereClauseWithOP1(this.mType), "title");
    }

    private static String constructBooleanTrueWhereClause(List<String> columns) {
        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append((String) columns.get(i));
            sb.append("=1 or ");
        }
        if (columns.size() > 0) {
            sb.setLength(sb.length() - 4);
        }
        sb.append(") ");
        return sb.toString();
    }

    private static String[] constructWhereClauseWithOP1(int type) {
        String pathForType;
        String[] strArr;
        StringBuilder stringBuilder;
        String systemPath = "/system/media/audio/";
        String op1Path = "/op1/";
        if (type != 2) {
            if (type == 4) {
                pathForType = "alarms/%";
            } else if (type != 8) {
                pathForType = "ringtones/%";
            }
            strArr = new String[2];
            stringBuilder = new StringBuilder();
            stringBuilder.append(systemPath);
            stringBuilder.append(pathForType);
            strArr[0] = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            stringBuilder.append(op1Path);
            stringBuilder.append(pathForType);
            strArr[1] = stringBuilder.toString();
            return strArr;
        }
        pathForType = "notifications/%";
        strArr = new String[2];
        stringBuilder = new StringBuilder();
        stringBuilder.append(systemPath);
        stringBuilder.append(pathForType);
        strArr[0] = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(op1Path);
        stringBuilder.append(pathForType);
        strArr[1] = stringBuilder.toString();
        return strArr;
    }

    private static String[] constructWhereClause(int type) {
        StringBuilder sb = new StringBuilder();
        if (type != 2) {
            if (type == 4) {
                sb.append("/system/media/audio/alarms/%");
            } else if (type != 8) {
                sb.append("/system/media/audio/ringtones/%");
            }
            return new String[]{sb.toString()};
        }
        sb.append("/system/media/audio/notifications/%");
        return new String[]{sb.toString()};
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (this.mActivity != null) {
            return this.mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
        }
        return this.mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    /* JADX WARNING: Missing block: B:27:0x0077, code skipped:
            if (r0 != null) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:28:0x0079, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:33:0x0096, code skipped:
            if (r0 == null) goto L_0x0099;
     */
    /* JADX WARNING: Missing block: B:34:0x0099, code skipped:
            return r4;
     */
    public static android.net.Uri getActualRingtoneUriBySubId(android.content.Context r11, int r12) {
        /*
        r0 = 0;
        if (r12 < 0) goto L_0x00a0;
    L_0x0003:
        r1 = 2;
        if (r12 < r1) goto L_0x0008;
    L_0x0006:
        goto L_0x00a0;
    L_0x0008:
        if (r12 != 0) goto L_0x000f;
    L_0x000a:
        r1 = "ringtone";
        r2 = "op_ringtone1_df";
        goto L_0x0024;
    L_0x000f:
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "ringtone_";
        r1.append(r2);
        r2 = r12 + 1;
        r1.append(r2);
        r1 = r1.toString();
        r2 = "op_ringtone2_df";
    L_0x0024:
        r3 = isRingSimSwitchOn(r11);
        if (r3 == 0) goto L_0x003f;
    L_0x002a:
        r3 = r11.getContentResolver();
        r3 = android.provider.Settings.System.getString(r3, r1);
        r4 = r11.getContentResolver();
        if (r3 != 0) goto L_0x003b;
    L_0x0038:
        r5 = "none";
        goto L_0x003c;
    L_0x003b:
        r5 = r3;
    L_0x003c:
        android.provider.Settings.System.putString(r4, r2, r5);
        r3 = r11.getContentResolver();
        r3 = android.provider.Settings.System.getString(r3, r2);
        r4 = getStaticDefaultRingtoneUri(r11);
        if (r3 != 0) goto L_0x004f;
    L_0x004e:
        return r4;
    L_0x004f:
        r5 = "none";
        r5 = r3.equals(r5);
        if (r5 == 0) goto L_0x0058;
    L_0x0057:
        return r0;
        r5 = r11.getContentResolver();	 Catch:{ SQLiteException -> 0x007f }
        r6 = android.net.Uri.parse(r3);	 Catch:{ SQLiteException -> 0x007f }
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r10 = 0;
        r5 = r5.query(r6, r7, r8, r9, r10);	 Catch:{ SQLiteException -> 0x007f }
        r0 = r5;
        if (r0 == 0) goto L_0x0077;
    L_0x006c:
        r5 = r0.getCount();	 Catch:{ SQLiteException -> 0x007f }
        if (r5 <= 0) goto L_0x0077;
    L_0x0072:
        r5 = android.net.Uri.parse(r3);	 Catch:{ SQLiteException -> 0x007f }
        r4 = r5;
    L_0x0077:
        if (r0 == 0) goto L_0x0099;
    L_0x0079:
        r0.close();
        goto L_0x0099;
    L_0x007d:
        r5 = move-exception;
        goto L_0x009a;
    L_0x007f:
        r5 = move-exception;
        r6 = "RingtoneManager";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x007d }
        r7.<init>();	 Catch:{ all -> 0x007d }
        r8 = "ex ";
        r7.append(r8);	 Catch:{ all -> 0x007d }
        r7.append(r5);	 Catch:{ all -> 0x007d }
        r7 = r7.toString();	 Catch:{ all -> 0x007d }
        android.util.Log.e(r6, r7);	 Catch:{ all -> 0x007d }
        if (r0 == 0) goto L_0x0099;
    L_0x0098:
        goto L_0x0079;
    L_0x0099:
        return r4;
    L_0x009a:
        if (r0 == 0) goto L_0x009f;
    L_0x009c:
        r0.close();
    L_0x009f:
        throw r5;
    L_0x00a0:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.getActualRingtoneUriBySubId(android.content.Context, int):android.net.Uri");
    }

    public static void setActualRingtoneUriBySubId(Context context, int subId, Uri ringtoneUri) {
        if (subId >= 0 && subId < 2) {
            String setting;
            if (subId == 0) {
                setting = "ringtone";
                System.putString(context.getContentResolver(), OP_RINGTONE1_DEFUALT, ringtoneUri != null ? ringtoneUri.toString() : "none");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("ringtone_");
                stringBuilder.append(subId + 1);
                setting = stringBuilder.toString();
                System.putString(context.getContentResolver(), OP_RINGTONE2_DEFUALT, ringtoneUri != null ? ringtoneUri.toString() : "none");
            }
            RingtoneManager.setActualRingtoneUriBySubId(context, subId, ringtoneUri);
        }
    }

    /* JADX WARNING: Missing block: B:28:0x006a, code skipped:
            if (r1 != null) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:29:0x006c, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:34:0x0089, code skipped:
            if (r1 == null) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:35:0x008c, code skipped:
            return r3;
     */
    public static android.net.Uri getActualDefaultRingtoneUri(android.content.Context r10, int r11) {
        /*
        r0 = getSettingForType(r11);
        r1 = 0;
        if (r0 != 0) goto L_0x0008;
    L_0x0007:
        return r1;
    L_0x0008:
        r2 = r11 & 1;
        if (r2 == 0) goto L_0x002c;
    L_0x000c:
        r0 = "op_ringtone_df";
        r2 = isRingSimSwitchOn(r10);
        if (r2 != 0) goto L_0x002c;
        r2 = r10.getContentResolver();
        r3 = "ringtone";
        r2 = android.provider.Settings.System.getString(r2, r3);
        r3 = r10.getContentResolver();
        if (r2 != 0) goto L_0x0028;
    L_0x0025:
        r4 = "none";
        goto L_0x0029;
    L_0x0028:
        r4 = r2;
    L_0x0029:
        android.provider.Settings.System.putString(r3, r0, r4);
        r2 = r10.getContentResolver();
        r2 = android.provider.Settings.System.getString(r2, r0);
        if (r2 == 0) goto L_0x0040;
    L_0x0037:
        r3 = "none";
        r3 = r2.equals(r3);
        if (r3 == 0) goto L_0x0040;
    L_0x003f:
        return r1;
    L_0x0040:
        if (r2 == 0) goto L_0x0093;
    L_0x0042:
        r3 = r11 & 1;
        if (r3 != 0) goto L_0x0047;
    L_0x0046:
        goto L_0x0093;
    L_0x0047:
        r3 = getStaticDefaultRingtoneUri(r10);
        r4 = r10.getContentResolver();	 Catch:{ SQLiteException -> 0x0072 }
        r5 = android.net.Uri.parse(r2);	 Catch:{ SQLiteException -> 0x0072 }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r4 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ SQLiteException -> 0x0072 }
        r1 = r4;
        if (r1 == 0) goto L_0x006a;
    L_0x005f:
        r4 = r1.getCount();	 Catch:{ SQLiteException -> 0x0072 }
        if (r4 <= 0) goto L_0x006a;
    L_0x0065:
        r4 = android.net.Uri.parse(r2);	 Catch:{ SQLiteException -> 0x0072 }
        r3 = r4;
    L_0x006a:
        if (r1 == 0) goto L_0x008c;
    L_0x006c:
        r1.close();
        goto L_0x008c;
    L_0x0070:
        r4 = move-exception;
        goto L_0x008d;
    L_0x0072:
        r4 = move-exception;
        r5 = "RingtoneManager";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0070 }
        r6.<init>();	 Catch:{ all -> 0x0070 }
        r7 = "ex ";
        r6.append(r7);	 Catch:{ all -> 0x0070 }
        r6.append(r4);	 Catch:{ all -> 0x0070 }
        r6 = r6.toString();	 Catch:{ all -> 0x0070 }
        android.util.Log.e(r5, r6);	 Catch:{ all -> 0x0070 }
        if (r1 == 0) goto L_0x008c;
    L_0x008b:
        goto L_0x006c;
    L_0x008c:
        return r3;
    L_0x008d:
        if (r1 == 0) goto L_0x0092;
    L_0x008f:
        r1.close();
    L_0x0092:
        throw r4;
    L_0x0093:
        if (r2 == 0) goto L_0x009a;
    L_0x0095:
        r1 = android.net.Uri.parse(r2);
    L_0x009a:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.getActualDefaultRingtoneUri(android.content.Context, int):android.net.Uri");
    }

    public static String getSettingForType(int type) {
        if (type == 1) {
            return "ringtone";
        }
        if (type == 2) {
            return "notification_sound";
        }
        if (type == 4) {
            return "alarm_alert";
        }
        if (type == 8) {
            return "mms_notification";
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:12:0x0044, code skipped:
            if (r0 != null) goto L_0x0046;
     */
    /* JADX WARNING: Missing block: B:13:0x0046, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:18:0x0054, code skipped:
            if (r0 == null) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:20:0x0059, code skipped:
            return mDefaultUri;
     */
    public static android.net.Uri getStaticDefaultRingtoneUri(android.content.Context r10) {
        /*
        r0 = 0;
        r1 = mDefaultUri;
        if (r1 == 0) goto L_0x0008;
    L_0x0005:
        r1 = mDefaultUri;
        return r1;
        r2 = r10.getContentResolver();	 Catch:{ Exception -> 0x004c }
        r3 = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x004c }
        r1 = "_id";
        r4 = new java.lang.String[]{r1};	 Catch:{ Exception -> 0x004c }
        r5 = "_display_name=?";
        r1 = 1;
        r6 = new java.lang.String[r1];	 Catch:{ Exception -> 0x004c }
        r1 = "ringtone";
        r1 = getDefaultRingtoneFileName(r10, r1);	 Catch:{ Exception -> 0x004c }
        r9 = 0;
        r6[r9] = r1;	 Catch:{ Exception -> 0x004c }
        r7 = 0;
        r8 = 0;
        r1 = r2.query(r3, r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x004c }
        r0 = r1;
        if (r0 == 0) goto L_0x0044;
    L_0x002c:
        r1 = r0.getCount();	 Catch:{ Exception -> 0x004c }
        if (r1 <= 0) goto L_0x0044;
    L_0x0032:
        r1 = r0.moveToFirst();	 Catch:{ Exception -> 0x004c }
        if (r1 == 0) goto L_0x0044;
    L_0x0038:
        r1 = r0.getLong(r9);	 Catch:{ Exception -> 0x004c }
        r3 = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x004c }
        r3 = android.content.ContentUris.withAppendedId(r3, r1);	 Catch:{ Exception -> 0x004c }
        mDefaultUri = r3;	 Catch:{ Exception -> 0x004c }
    L_0x0044:
        if (r0 == 0) goto L_0x0057;
    L_0x0046:
        r0.close();
        goto L_0x0057;
    L_0x004a:
        r1 = move-exception;
        goto L_0x005a;
    L_0x004c:
        r1 = move-exception;
        r2 = "RingtoneManager";
        r3 = "RemoteException: ";
        com.oneplus.settings.ringtone.OPMyLog.e(r2, r3, r1);	 Catch:{ all -> 0x004a }
        if (r0 == 0) goto L_0x0057;
    L_0x0056:
        goto L_0x0046;
    L_0x0057:
        r1 = mDefaultUri;
        return r1;
    L_0x005a:
        if (r0 == 0) goto L_0x005f;
    L_0x005c:
        r0.close();
    L_0x005f:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.getStaticDefaultRingtoneUri(android.content.Context):android.net.Uri");
    }

    private static String getDefaultRingtoneFileName(Context mContext, String settingName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DEFAULT_RINGTONE_PROPERTY_PREFIX_RO);
        stringBuilder.append(settingName);
        return SystemProperties.get(stringBuilder.toString());
    }

    public static Uri getUriFromCursor(Cursor cursor) {
        return ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0));
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri) {
        return RingtoneManager.getRingtone(context, ringtoneUri);
    }

    /* JADX WARNING: Missing block: B:23:0x004a, code skipped:
            if (r1 != null) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:24:0x004c, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:29:0x0069, code skipped:
            if (r1 == null) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:30:0x006c, code skipped:
            return false;
     */
    public static boolean isSystemRingtone(android.content.Context r9, android.net.Uri r10, int r11) {
        /*
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE.MCL;
        r1 = com.oneplus.custom.utils.OpCustomizeSettings.getCustomType();
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0011;
    L_0x000c:
        r0 = isSystemRingtoneForMCL(r9, r10, r11);
        return r0;
    L_0x0011:
        r0 = 0;
        if (r10 != 0) goto L_0x0015;
    L_0x0014:
        return r0;
    L_0x0015:
        r1 = 0;
        r2 = "media";
        r3 = r10.getAuthority();	 Catch:{ SQLiteException -> 0x0052 }
        r2 = r2.equals(r3);	 Catch:{ SQLiteException -> 0x0052 }
        if (r2 != 0) goto L_0x0029;
        if (r1 == 0) goto L_0x0028;
    L_0x0025:
        r1.close();
    L_0x0028:
        return r0;
    L_0x0029:
        r3 = r9.getContentResolver();	 Catch:{ SQLiteException -> 0x0052 }
        r5 = 0;
        r6 = "_data like ?";
        r7 = constructWhereClause(r11);	 Catch:{ SQLiteException -> 0x0052 }
        r8 = 0;
        r4 = r10;
        r2 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x0052 }
        r1 = r2;
        if (r1 == 0) goto L_0x004a;
    L_0x003d:
        r2 = r1.getCount();	 Catch:{ SQLiteException -> 0x0052 }
        if (r2 <= 0) goto L_0x004a;
    L_0x0043:
        r0 = 1;
        if (r1 == 0) goto L_0x0049;
    L_0x0046:
        r1.close();
    L_0x0049:
        return r0;
    L_0x004a:
        if (r1 == 0) goto L_0x006c;
    L_0x004c:
        r1.close();
        goto L_0x006c;
    L_0x0050:
        r0 = move-exception;
        goto L_0x006d;
    L_0x0052:
        r2 = move-exception;
        r3 = "RingtoneManager";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0050 }
        r4.<init>();	 Catch:{ all -> 0x0050 }
        r5 = "ex ";
        r4.append(r5);	 Catch:{ all -> 0x0050 }
        r4.append(r2);	 Catch:{ all -> 0x0050 }
        r4 = r4.toString();	 Catch:{ all -> 0x0050 }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x0050 }
        if (r1 == 0) goto L_0x006c;
    L_0x006b:
        goto L_0x004c;
    L_0x006c:
        return r0;
    L_0x006d:
        if (r1 == 0) goto L_0x0072;
    L_0x006f:
        r1.close();
    L_0x0072:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.isSystemRingtone(android.content.Context, android.net.Uri, int):boolean");
    }

    /* JADX WARNING: Missing block: B:19:0x0039, code skipped:
            if (r1 != null) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:20:0x003b, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:25:0x0058, code skipped:
            if (r1 == null) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:26:0x005b, code skipped:
            return false;
     */
    public static boolean isSystemRingtoneForMCL(android.content.Context r9, android.net.Uri r10, int r11) {
        /*
        r0 = 0;
        if (r10 != 0) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r1 = 0;
        r2 = "media";
        r3 = r10.getAuthority();	 Catch:{ SQLiteException -> 0x0041 }
        r2 = r2.equals(r3);	 Catch:{ SQLiteException -> 0x0041 }
        if (r2 != 0) goto L_0x0018;
        if (r1 == 0) goto L_0x0017;
    L_0x0014:
        r1.close();
    L_0x0017:
        return r0;
    L_0x0018:
        r6 = "_data like ? or _data like ? ";
        r3 = r9.getContentResolver();	 Catch:{ SQLiteException -> 0x0041 }
        r5 = 0;
        r7 = constructWhereClauseWithOP1(r11);	 Catch:{ SQLiteException -> 0x0041 }
        r8 = 0;
        r4 = r10;
        r2 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x0041 }
        r1 = r2;
        if (r1 == 0) goto L_0x0039;
    L_0x002c:
        r2 = r1.getCount();	 Catch:{ SQLiteException -> 0x0041 }
        if (r2 <= 0) goto L_0x0039;
    L_0x0032:
        r0 = 1;
        if (r1 == 0) goto L_0x0038;
    L_0x0035:
        r1.close();
    L_0x0038:
        return r0;
    L_0x0039:
        if (r1 == 0) goto L_0x005b;
    L_0x003b:
        r1.close();
        goto L_0x005b;
    L_0x003f:
        r0 = move-exception;
        goto L_0x005c;
    L_0x0041:
        r2 = move-exception;
        r3 = "RingtoneManager";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x003f }
        r4.<init>();	 Catch:{ all -> 0x003f }
        r5 = "ex ";
        r4.append(r5);	 Catch:{ all -> 0x003f }
        r4.append(r2);	 Catch:{ all -> 0x003f }
        r4 = r4.toString();	 Catch:{ all -> 0x003f }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x003f }
        if (r1 == 0) goto L_0x005b;
    L_0x005a:
        goto L_0x003b;
    L_0x005b:
        return r0;
    L_0x005c:
        if (r1 == 0) goto L_0x0061;
    L_0x005e:
        r1.close();
    L_0x0061:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.isSystemRingtoneForMCL(android.content.Context, android.net.Uri, int):boolean");
    }

    public static Uri ringtoneRestoreFromDefault(Context context, int type, Uri sound_uri) {
        String RO_PREFIX = DEFAULT_RINGTONE_PROPERTY_PREFIX_RO;
        String settingKey = getSettingForType(type);
        Uri defaultUri = sound_uri;
        String ringerType = null;
        if ((type & 1) != 0) {
            ringerType = "is_ringtone";
        }
        if ((type & 2) != 0) {
            ringerType = "is_notification";
        }
        if ((type & 4) != 0) {
            ringerType = "is_alarm";
        }
        if (ringerType != null) {
            if (settingKey.startsWith("ringtone")) {
                settingKey = "ringtone";
            }
            String settingKey2 = settingKey;
            settingKey = new StringBuilder();
            settingKey.append(RO_PREFIX);
            settingKey.append("ringtone");
            String defaultRingtoneName = SystemProperties.get(settingKey.toString());
            settingKey = new StringBuilder();
            settingKey.append(RO_PREFIX);
            settingKey.append(settingKey2);
            String fileName = SystemProperties.get(settingKey.toString(), defaultRingtoneName);
            String title = fileName.substring(0, fileName.lastIndexOf("."));
            settingKey = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ringtoneRestoreFromDefault: title = ");
            stringBuilder.append(title);
            OPMyLog.d(settingKey, stringBuilder.toString());
            Cursor c = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri = Media.INTERNAL_CONTENT_URI;
                String[] strArr = new String[]{OPFirewallUtils._ID};
                settingKey = new StringBuilder();
                settingKey.append(ringerType);
                settingKey.append("=1 and ");
                settingKey.append("title");
                settingKey.append("=?");
                c = contentResolver.query(uri, strArr, settingKey.toString(), new String[]{title}, null, null);
                if (!(c == null || c.getCount() <= null || c.moveToFirst() == null)) {
                    defaultUri = ContentUris.withAppendedId(Media.INTERNAL_CONTENT_URI, c.getLong(0));
                    settingKey = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("ringtoneRestoreFromDefault: [");
                    stringBuilder2.append(settingKey2);
                    stringBuilder2.append("] = ");
                    stringBuilder2.append(defaultUri.toString());
                    Log.d(settingKey, stringBuilder2.toString());
                }
            } catch (Exception settingKey3) {
                Log.w(TAG, "RemoteException: ", settingKey3);
            } catch (Throwable th) {
                IoUtils.closeQuietly(c);
            }
            IoUtils.closeQuietly(c);
        }
        return defaultUri;
    }

    /* JADX WARNING: Missing block: B:20:0x0092, code skipped:
            if (r1 != null) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:30:0x00cb, code skipped:
            if (r1 == null) goto L_0x00ce;
     */
    public static com.oneplus.settings.ringtone.OPRingtoneManager.ResultRing getLocatRingtoneTitle(android.content.Context r9, android.net.Uri r10, int r11, int r12) {
        /*
        r0 = new com.oneplus.settings.ringtone.OPRingtoneManager$ResultRing;
        r1 = 0;
        r0.<init>(r1, r10);
        if (r10 == 0) goto L_0x00d5;
    L_0x0008:
        r2 = android.media.RingtoneManager.isDefault(r10);
        if (r2 == 0) goto L_0x0010;
    L_0x000e:
        goto L_0x00d5;
    L_0x0010:
        r2 = "RingtoneManager";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "ringtoneUri:";
        r3.append(r4);
        r3.append(r10);
        r3 = r3.toString();
        com.oneplus.settings.ringtone.OPMyLog.d(r2, r3);
        r2 = isSystemRingtone(r9, r10, r11);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        if (r2 == 0) goto L_0x0045;
    L_0x002d:
        r3 = r9.getContentResolver();	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r2 = "title";
        r4 = "_data";
        r5 = "mime_type";
        r5 = new java.lang.String[]{r2, r4, r5};	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r4 = r10;
        r2 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r1 = r2;
        goto L_0x005e;
    L_0x0045:
        r3 = r9.getContentResolver();	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r2 = "_display_name";
        r4 = "_data";
        r5 = "mime_type";
        r6 = "title";
        r5 = new java.lang.String[]{r2, r4, r5, r6};	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r4 = r10;
        r2 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r1 = r2;
    L_0x005e:
        if (r1 == 0) goto L_0x0092;
    L_0x0060:
        r2 = r1.moveToFirst();	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        if (r2 == 0) goto L_0x0092;
    L_0x0066:
        r2 = updateRingtoneForInternal(r9, r10, r1, r11, r12);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r3 = 0;
        r3 = r1.getString(r3);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r3 = com.oneplus.settings.utils.OPUtils.getFileNameNoEx(r3);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r0.title = r3;	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r3 = r0.title;	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r3 = android.text.TextUtils.isEmpty(r3);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        if (r3 == 0) goto L_0x0089;
    L_0x007d:
        r3 = "title";
        r3 = r1.getColumnIndex(r3);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r3 = r1.getString(r3);	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        r0.title = r3;	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
    L_0x0089:
        r0.ringUri = r2;	 Catch:{ SQLiteException -> 0x00b4, SecurityException -> 0x009a }
        if (r1 == 0) goto L_0x0091;
    L_0x008e:
        r1.close();
    L_0x0091:
        return r0;
    L_0x0092:
        if (r1 == 0) goto L_0x00ce;
    L_0x0094:
        r1.close();
        goto L_0x00ce;
    L_0x0098:
        r2 = move-exception;
        goto L_0x00cf;
    L_0x009a:
        r2 = move-exception;
        r3 = "RingtoneManager";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0098 }
        r4.<init>();	 Catch:{ all -> 0x0098 }
        r5 = "SecurityException ex ";
        r4.append(r5);	 Catch:{ all -> 0x0098 }
        r4.append(r2);	 Catch:{ all -> 0x0098 }
        r4 = r4.toString();	 Catch:{ all -> 0x0098 }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x0098 }
        if (r1 == 0) goto L_0x00ce;
    L_0x00b3:
        goto L_0x0094;
    L_0x00b4:
        r2 = move-exception;
        r3 = "RingtoneManager";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0098 }
        r4.<init>();	 Catch:{ all -> 0x0098 }
        r5 = "ex ";
        r4.append(r5);	 Catch:{ all -> 0x0098 }
        r4.append(r2);	 Catch:{ all -> 0x0098 }
        r4 = r4.toString();	 Catch:{ all -> 0x0098 }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x0098 }
        if (r1 == 0) goto L_0x00ce;
    L_0x00cd:
        goto L_0x0094;
    L_0x00ce:
        return r0;
    L_0x00cf:
        if (r1 == 0) goto L_0x00d4;
    L_0x00d1:
        r1.close();
    L_0x00d4:
        throw r2;
    L_0x00d5:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.getLocatRingtoneTitle(android.content.Context, android.net.Uri, int, int):com.oneplus.settings.ringtone.OPRingtoneManager$ResultRing");
    }

    public static Uri updateRingtoneForInternal(Context context, Uri ringtoneUri, Cursor cusor, int type, int simid) {
        Context context2 = context;
        Cursor cursor = cusor;
        int i = type;
        if (!ringtoneUri.toString().contains(Media.INTERNAL_CONTENT_URI.toString())) {
            return ringtoneUri;
        }
        String path = cursor.getString(1);
        if (path == null || ((path.startsWith("/system/media/audio/ringtones/") || path.startsWith("/op1/ringtones/")) && i == 1)) {
            return ringtoneUri;
        }
        Uri ringtoneUri2;
        path = path.replace("/storage/emulated/legacy", Environment.getExternalStorageDirectory().getAbsolutePath());
        String title = cursor.getString(0);
        String mimetype = cursor.getString(2);
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        String[] strArr = new String[]{path};
        Uri uri2 = uri;
        Cursor cursor1 = context.getContentResolver().query(uri, new String[]{OPFirewallUtils._ID}, "_data=?", strArr, null);
        ContentValues values;
        if (cursor1 == null || !cursor1.moveToFirst()) {
            values = new ContentValues();
            values.put("_data", path);
            values.put("title", title);
            values.put("mime_type", mimetype);
            if (i == 1) {
                values.put("is_ringtone", Boolean.valueOf(true));
            } else if (i == 2 || i == 8) {
                values.put("is_notification", Boolean.valueOf(true));
            } else {
                values.put("is_alarm", Boolean.valueOf(true));
            }
            ContentResolver contentResolver = context.getContentResolver();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("_data=\"");
            stringBuilder.append(path);
            stringBuilder.append("\"");
            contentResolver.delete(uri2, stringBuilder.toString(), null);
            ringtoneUri2 = context.getContentResolver().insert(uri2, values);
        } else {
            ringtoneUri2 = ContentUris.withAppendedId(uri2, cursor1.getLong(0));
            values = new ContentValues();
            if (i == 1) {
                values.put("is_ringtone", Boolean.valueOf(true));
            } else if (i == 2 || i == 8) {
                values.put("is_notification", Boolean.valueOf(true));
            } else {
                values.put("is_alarm", Boolean.valueOf(true));
            }
            context.getContentResolver().update(ringtoneUri2, values, null, null);
        }
        if (cursor1 != null) {
            cursor1.close();
        }
        if (simid > 0) {
            setActualRingtoneUriBySubId(context2, simid - 1, ringtoneUri2);
        } else {
            setActualDefaultRingtoneUri(context2, i, ringtoneUri2);
        }
        return ringtoneUri2;
    }

    public static long transToId(String str) {
        try {
            return Long.valueOf(str).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        if (getSettingForType(type) != null) {
            if (type == 1) {
                System.putString(context.getContentResolver(), OP_RINGTONE_DEFUALT, ringtoneUri != null ? ringtoneUri.toString() : "none");
                System.putString(context.getContentResolver(), RINGTONE_2, ringtoneUri != null ? ringtoneUri.toString() : null);
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, type, ringtoneUri);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0048  */
    public static void updateActualRingtone(android.content.Context r6) {
        /*
        r0 = r6.getContentResolver();
        r1 = "op_ringtone_df";
        r0 = android.provider.Settings.System.getString(r0, r1);
        r1 = getStaticDefaultRingtoneUri(r6);
        r2 = r6.getContentResolver();
        r3 = "ringtone";
        r4 = 0;
        if (r0 == 0) goto L_0x0027;
    L_0x0018:
        r5 = "none";
        r5 = r0.equals(r5);
        if (r5 == 0) goto L_0x0022;
    L_0x0020:
        r5 = r4;
        goto L_0x002f;
    L_0x0022:
        r5 = r0.toString();
        goto L_0x002f;
    L_0x0027:
        if (r1 == 0) goto L_0x002e;
    L_0x0029:
        r5 = r1.toString();
        goto L_0x002f;
    L_0x002e:
        goto L_0x0020;
    L_0x002f:
        android.provider.Settings.System.putString(r2, r3, r5);
        r2 = r6.getContentResolver();
        r3 = "ringtone_2";
        if (r0 == 0) goto L_0x0048;
    L_0x003a:
        r5 = "none";
        r5 = r0.equals(r5);
        if (r5 == 0) goto L_0x0043;
    L_0x0042:
        goto L_0x004f;
    L_0x0043:
        r4 = r0.toString();
        goto L_0x004f;
    L_0x0048:
        if (r1 == 0) goto L_0x004f;
    L_0x004a:
        r4 = r1.toString();
    L_0x004f:
        android.provider.Settings.System.putString(r2, r3, r4);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.updateActualRingtone(android.content.Context):void");
    }

    /* JADX WARNING: Missing block: B:10:0x0033, code skipped:
            if (r3 != null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:11:0x0035, code skipped:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:16:0x0052, code skipped:
            if (r3 == null) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:17:0x0055, code skipped:
            r4 = r10.getContentResolver();
            r5 = OP_RINGTONE_DEFUALT;
     */
    /* JADX WARNING: Missing block: B:18:0x005b, code skipped:
            if (r1 == null) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:19:0x005d, code skipped:
            r6 = r1.toString();
     */
    /* JADX WARNING: Missing block: B:20:0x0062, code skipped:
            r6 = "none";
     */
    /* JADX WARNING: Missing block: B:21:0x0064, code skipped:
            android.provider.Settings.System.putString(r4, r5, r6);
     */
    /* JADX WARNING: Missing block: B:22:0x0067, code skipped:
            if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:27:?, code skipped:
            return r1;
     */
    /* JADX WARNING: Missing block: B:28:?, code skipped:
            return null;
     */
    public static android.net.Uri updateSigleRingtone(android.content.Context r10) {
        /*
        r0 = r10.getContentResolver();
        r1 = "ringtone";
        r0 = android.provider.Settings.System.getString(r0, r1);
        r1 = getStaticDefaultRingtoneUri(r10);
        r2 = 0;
        r3 = r2;
        if (r0 != 0) goto L_0x0015;
    L_0x0013:
        r1 = 0;
        goto L_0x0033;
    L_0x0015:
        r4 = r10.getContentResolver();	 Catch:{ SQLiteException -> 0x003b }
        r5 = android.net.Uri.parse(r0);	 Catch:{ SQLiteException -> 0x003b }
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r4 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ SQLiteException -> 0x003b }
        r3 = r4;
        if (r3 == 0) goto L_0x0033;
    L_0x0028:
        r4 = r3.getCount();	 Catch:{ SQLiteException -> 0x003b }
        if (r4 <= 0) goto L_0x0033;
    L_0x002e:
        r4 = android.net.Uri.parse(r0);	 Catch:{ SQLiteException -> 0x003b }
        r1 = r4;
    L_0x0033:
        if (r3 == 0) goto L_0x0055;
    L_0x0035:
        r3.close();
        goto L_0x0055;
    L_0x0039:
        r2 = move-exception;
        goto L_0x006c;
    L_0x003b:
        r4 = move-exception;
        r5 = "RingtoneManager";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0039 }
        r6.<init>();	 Catch:{ all -> 0x0039 }
        r7 = "ex ";
        r6.append(r7);	 Catch:{ all -> 0x0039 }
        r6.append(r4);	 Catch:{ all -> 0x0039 }
        r6 = r6.toString();	 Catch:{ all -> 0x0039 }
        android.util.Log.e(r5, r6);	 Catch:{ all -> 0x0039 }
        if (r3 == 0) goto L_0x0055;
    L_0x0054:
        goto L_0x0035;
    L_0x0055:
        r4 = r10.getContentResolver();
        r5 = "op_ringtone_df";
        if (r1 == 0) goto L_0x0062;
    L_0x005d:
        r6 = r1.toString();
        goto L_0x0064;
    L_0x0062:
        r6 = "none";
    L_0x0064:
        android.provider.Settings.System.putString(r4, r5, r6);
        if (r1 == 0) goto L_0x006b;
    L_0x0069:
        r2 = r1;
    L_0x006b:
        return r2;
    L_0x006c:
        if (r3 == 0) goto L_0x0071;
    L_0x006e:
        r3.close();
    L_0x0071:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.updateSigleRingtone(android.content.Context):android.net.Uri");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0058  */
    public static void updateActualRingtone2(android.content.Context r8) {
        /*
        r0 = r8.getContentResolver();
        r1 = "op_ringtone1_df";
        r0 = android.provider.Settings.System.getString(r0, r1);
        r1 = r8.getContentResolver();
        r2 = "op_ringtone2_df";
        r1 = android.provider.Settings.System.getString(r1, r2);
        r2 = getStaticDefaultRingtoneUri(r8);
        r3 = 0;
        if (r0 == 0) goto L_0x002c;
    L_0x001d:
        r4 = "none";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0027;
    L_0x0025:
        r4 = r3;
        goto L_0x0034;
    L_0x0027:
        r4 = r0.toString();
        goto L_0x0034;
    L_0x002c:
        if (r2 == 0) goto L_0x0033;
    L_0x002e:
        r4 = r2.toString();
        goto L_0x0034;
    L_0x0033:
        goto L_0x0025;
    L_0x0034:
        if (r1 == 0) goto L_0x0045;
    L_0x0036:
        r5 = "none";
        r5 = r1.equals(r5);
        if (r5 == 0) goto L_0x0040;
    L_0x003e:
        r5 = r3;
        goto L_0x004d;
    L_0x0040:
        r5 = r1.toString();
        goto L_0x004d;
    L_0x0045:
        if (r2 == 0) goto L_0x004c;
    L_0x0047:
        r5 = r2.toString();
        goto L_0x004d;
    L_0x004c:
        goto L_0x003e;
    L_0x004d:
        if (r4 != 0) goto L_0x0051;
    L_0x004f:
        r6 = r3;
        goto L_0x0055;
    L_0x0051:
        r6 = android.net.Uri.parse(r4);
    L_0x0055:
        if (r5 != 0) goto L_0x0058;
    L_0x0057:
        goto L_0x005c;
    L_0x0058:
        r3 = android.net.Uri.parse(r5);
    L_0x005c:
        r7 = 0;
        android.media.RingtoneManager.setActualRingtoneUriBySubId(r8, r7, r6);
        r7 = 1;
        android.media.RingtoneManager.setActualRingtoneUriBySubId(r8, r7, r3);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.ringtone.OPRingtoneManager.updateActualRingtone2(android.content.Context):void");
    }

    public static void updateDefaultRingtone(Context context) {
        String ringtoneUri = System.getString(context.getContentResolver(), "ringtone");
        String ringtoneUri1 = System.getString(context.getContentResolver(), RINGTONE_2);
        System.putString(context.getContentResolver(), OP_RINGTONE1_DEFUALT, ringtoneUri != null ? ringtoneUri.toString() : "none");
        System.putString(context.getContentResolver(), OP_RINGTONE2_DEFUALT, ringtoneUri1 != null ? ringtoneUri1.toString() : "none");
    }

    public static void updateDb(Context context, Uri ringUri, int type) {
        if (ringUri != null) {
            ContentValues values = new ContentValues();
            if (type != 2) {
                if (type == 4) {
                    values.put("is_alarm", Boolean.valueOf(true));
                } else if (type != 8) {
                    values.put("is_ringtone", Boolean.valueOf(true));
                }
                context.getContentResolver().update(ringUri, values, null, null);
            }
            values.put("is_notification", Boolean.valueOf(true));
            context.getContentResolver().update(ringUri, values, null, null);
        }
    }

    public static void setRingSimSwitch(Context context, int value) {
        System.putInt(context.getContentResolver(), OP_SIM_SWITCH, value);
    }

    public static boolean isRingSimSwitchOn(Context context) {
        return System.getInt(context.getContentResolver(), OP_SIM_SWITCH, 0) == 1;
    }

    public static boolean isDefault(Uri ringtoneUri) {
        return RingtoneManager.isDefault(ringtoneUri);
    }
}
