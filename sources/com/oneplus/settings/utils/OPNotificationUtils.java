package com.oneplus.settings.utils;

import android.content.ContentProvider;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import com.oneplus.settings.ringtone.OPMyLog;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import libcore.io.IoUtils;

public class OPNotificationUtils {
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX_RO = "ro.config.";
    private static final String[] MEDIA_COLUMNS = new String[]{OPFirewallUtils._ID, "_data", "title"};
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    static final String[] OriginName = new String[]{"beep", "capriccioso", "Cloud", "echo", "In_high_spirit", "Journey", "longing", "Old_telephone", "oneplus_tune", "Rotation", "Innocence", "Talk_about", "Ding", "Distant", "Drops", "Elegant", "Free", "harp", "Linger", "Meet", "Quickly", "surprise", "Tactfully", "Wind_chime", "A_starry_night", "alarm_clock1", "alarm_clock2", "flyer", "Spring", "Walking_in_the_rain"};
    private static final String TAG = "OPNotificationUtils";
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_MMS_NOTIFICATION = 8;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE = 1;

    public static String replaceWith(Context context, String notification, String settingsName) {
        String mUnkownRingtone = context.getResources().getString(R.string.oneplus_unknown_ringtone);
        int type = 1;
        if (settingsName.endsWith("notification_sound") || settingsName.endsWith("mms_notification")) {
            type = 2;
        } else if (settingsName.endsWith("alarm_alert")) {
            type = 4;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type:");
        stringBuilder.append(type);
        stringBuilder.append(" settingsName:");
        stringBuilder.append(settingsName);
        OPMyLog.d("", stringBuilder.toString());
        notification = getRingtoneAlias(context, type, notification);
        if (!notification.contains(mUnkownRingtone)) {
            return notification;
        }
        restoreRingtoneIfNotExist(context, settingsName);
        if (settingsName.endsWith("ringtone")) {
            restoreRingtoneIfNotExist(context, OPRingtoneManager.RINGTONE_2);
        }
        if (settingsName.endsWith("ringtone")) {
            return context.getResources().getString(R.string.oneplus_ringtones_oneplus_tune);
        }
        if (settingsName.endsWith("notification_sound")) {
            return context.getResources().getString(R.string.oneplus_notifications_meet);
        }
        return context.getResources().getString(R.string.oneplus_notifications_free);
    }

    public static String getRingtoneAlias(Context context, int type, String strRingtoneTitle) {
        if (type < 1 || type > 4) {
            return strRingtoneTitle;
        }
        if (!OpFeatures.isSupport(new int[]{15})) {
            return strRingtoneTitle;
        }
        String[] strRingtoneType = new String[]{"", "oos_ring_ringtones_", "oos_ring_notifications_", "", "oos_ring_alarms_"};
        if (strRingtoneType[type] == "" || strRingtoneTitle == null) {
            return strRingtoneTitle;
        }
        String strOOS;
        String actualTitle = null;
        if (strRingtoneTitle.startsWith(context.getString(17040833))) {
            actualTitle = Ringtone.getTitle(context, RingtoneManager.getActualDefaultRingtoneUri(context, type), false, false);
            strOOS = actualTitle.toLowerCase();
        } else {
            strOOS = strRingtoneTitle.toLowerCase();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(strRingtoneType[type]);
        stringBuilder.append(strOOS.replace(" ", "_"));
        strOOS = stringBuilder.toString();
        int resId = Resources.getSystem().getIdentifier(strOOS, "string", "android");
        if (resId > 0) {
            strRingtoneTitle = context.getString(resId);
            if (actualTitle == null) {
                return strRingtoneTitle;
            }
            return context.getString(17040834, new Object[]{strRingtoneTitle});
        }
        String str = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getRingtoneAlias: resource not found - ");
        stringBuilder2.append(strOOS);
        Log.v(str, stringBuilder2.toString());
        return strRingtoneTitle;
    }

    /* JADX WARNING: Missing block: B:26:0x0078, code skipped:
            if (r10 != null) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:27:0x007a, code skipped:
            r10.close();
     */
    /* JADX WARNING: Missing block: B:45:0x00a2, code skipped:
            if (r10 == null) goto L_0x007d;
     */
    /* JADX WARNING: Missing block: B:46:0x00a5, code skipped:
            if (r7 != null) goto L_0x00b3;
     */
    /* JADX WARNING: Missing block: B:47:0x00a7, code skipped:
            r7 = r12.getLastPathSegment();
     */
    public static java.lang.String getTitle(android.content.Context r11, android.net.Uri r12, boolean r13, boolean r14) {
        /*
        r6 = r11.getContentResolver();
        r7 = 0;
        if (r12 == 0) goto L_0x00ac;
    L_0x0007:
        r0 = r12.getAuthority();
        r8 = android.content.ContentProvider.getAuthorityWithoutUserId(r0);
        r0 = "settings";
        r0 = r0.equals(r8);
        r9 = 1;
        if (r0 == 0) goto L_0x0045;
    L_0x0018:
        if (r13 == 0) goto L_0x00ab;
    L_0x001a:
        r0 = android.media.RingtoneManager.getDefaultType(r12);
        if (r0 != r9) goto L_0x002a;
        r0 = getDefaultRingtoneSubIdByUri(r12);
        r0 = getActualRingtoneUriBySubId(r11, r0);
        goto L_0x0033;
        r0 = android.media.RingtoneManager.getDefaultType(r12);
        r0 = android.media.RingtoneManager.getActualDefaultRingtoneUri(r11, r0);
    L_0x0033:
        r1 = 0;
        r2 = getTitle(r11, r0, r1, r14);
        r3 = 17040834; // 0x10405c2 float:2.4248702E-38 double:8.4192907E-317;
        r4 = new java.lang.Object[r9];
        r4[r1] = r2;
        r7 = r11.getString(r3, r4);
        goto L_0x00ab;
    L_0x0045:
        r0 = 0;
        r10 = r0;
        r1 = "media";
        r1 = r1.equals(r8);	 Catch:{ SecurityException -> 0x0081 }
        if (r1 == 0) goto L_0x0078;
    L_0x004f:
        if (r14 == 0) goto L_0x0053;
    L_0x0051:
        r3 = r0;
        goto L_0x0056;
    L_0x0053:
        r0 = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
        goto L_0x0051;
    L_0x0056:
        r2 = MEDIA_COLUMNS;	 Catch:{ SecurityException -> 0x0081 }
        r4 = 0;
        r5 = 0;
        r0 = r6;
        r1 = r12;
        r0 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SecurityException -> 0x0081 }
        r10 = r0;
        if (r10 == 0) goto L_0x0078;
    L_0x0063:
        r0 = r10.getCount();	 Catch:{ SecurityException -> 0x0081 }
        if (r0 != r9) goto L_0x0078;
    L_0x0069:
        r10.moveToFirst();	 Catch:{ SecurityException -> 0x0081 }
        r0 = 2;
        r0 = r10.getString(r0);	 Catch:{ SecurityException -> 0x0081 }
        if (r10 == 0) goto L_0x0076;
    L_0x0073:
        r10.close();
    L_0x0076:
        r1 = 0;
        return r0;
    L_0x0078:
        if (r10 == 0) goto L_0x007d;
    L_0x007a:
        r10.close();
    L_0x007d:
        r0 = 0;
        goto L_0x00a5;
    L_0x007f:
        r0 = move-exception;
        goto L_0x009a;
    L_0x0081:
        r0 = move-exception;
        r1 = 0;
        if (r14 == 0) goto L_0x0092;
    L_0x0085:
        r2 = "audio";
        r2 = r11.getSystemService(r2);	 Catch:{ all -> 0x007f }
        r2 = (android.media.AudioManager) r2;	 Catch:{ all -> 0x007f }
        r3 = r2.getRingtonePlayer();	 Catch:{ all -> 0x007f }
        r1 = r3;
    L_0x0092:
        if (r1 == 0) goto L_0x00a2;
    L_0x0094:
        r2 = r1.getTitle(r12);	 Catch:{ RemoteException -> 0x00a1 }
        r7 = r2;
        goto L_0x00a2;
    L_0x009a:
        if (r10 == 0) goto L_0x009f;
    L_0x009c:
        r10.close();
    L_0x009f:
        r1 = 0;
        throw r0;
    L_0x00a1:
        r2 = move-exception;
    L_0x00a2:
        if (r10 == 0) goto L_0x007d;
    L_0x00a4:
        goto L_0x007a;
    L_0x00a5:
        if (r7 != 0) goto L_0x00ab;
    L_0x00a7:
        r7 = r12.getLastPathSegment();
    L_0x00ab:
        goto L_0x00b3;
    L_0x00ac:
        r0 = 17040838; // 0x10405c6 float:2.4248713E-38 double:8.4192926E-317;
        r7 = r11.getString(r0);
    L_0x00b3:
        if (r7 != 0) goto L_0x00c0;
    L_0x00b5:
        r0 = 17040839; // 0x10405c7 float:2.4248716E-38 double:8.419293E-317;
        r7 = r11.getString(r0);
        if (r7 != 0) goto L_0x00c0;
    L_0x00be:
        r7 = "";
    L_0x00c0:
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPNotificationUtils.getTitle(android.content.Context, android.net.Uri, boolean, boolean):java.lang.String");
    }

    public static int getDefaultRingtoneSubIdByUri(Uri defaultRingtoneUri) {
        if (defaultRingtoneUri == null) {
            return -1;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_RINGTONE_URI)) {
            return 0;
        }
        String uriString = defaultRingtoneUri.toString();
        if (uriString.startsWith(System.DEFAULT_RINGTONE_URI.toString())) {
            int parsedSubId = Integer.parseInt(uriString.substring(uriString.lastIndexOf("_") + 1));
            if (parsedSubId > 0 && parsedSubId <= 2) {
                return parsedSubId - 1;
            }
        }
        return -1;
    }

    public static Uri getActualRingtoneUriBySubId(Context context, int subId) {
        Cursor cursor = null;
        if (subId < 0 || subId >= 2) {
            return null;
        }
        String setting;
        if (subId == 0) {
            setting = "ringtone";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ringtone_");
            stringBuilder.append(subId + 1);
            setting = stringBuilder.toString();
        }
        String uriString = System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        if (uriString == null) {
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getActualRingtoneUriBySubId(");
            stringBuilder2.append(subId);
            stringBuilder2.append(") = ");
            stringBuilder2.append(uriString);
            Log.d(str, stringBuilder2.toString());
            return null;
        }
        String str2;
        StringBuilder stringBuilder3;
        Uri ringToneUri = getStaticDefaultRingtoneUri(context);
        try {
            cursor = context.getContentResolver().query(Uri.parse(uriString), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                ringToneUri = Uri.parse(uriString);
            }
        } catch (SQLiteException ex) {
            String str3 = TAG;
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("ex ");
            stringBuilder4.append(ex);
            Log.e(str3, stringBuilder4.toString());
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
        }
        IoUtils.closeQuietly(cursor);
        if (ringToneUri == null) {
            str2 = TAG;
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("getActualRingtoneUriBySubId(");
            stringBuilder3.append(subId);
            stringBuilder3.append(") failed.");
            Log.w(str2, stringBuilder3.toString());
            if (validForProvider(context, ringToneUri)) {
                ringToneUri = getActualDefaultRingtoneUri(context, 1);
            } else {
                ringToneUri = getDefaultUri(1);
            }
        }
        str2 = TAG;
        stringBuilder3 = new StringBuilder();
        stringBuilder3.append("getActualRingtoneUriBySubId(");
        stringBuilder3.append(subId);
        stringBuilder3.append(") of user[");
        stringBuilder3.append(context.getUserId());
        stringBuilder3.append("] = ");
        stringBuilder3.append(ringToneUri);
        Log.d(str2, stringBuilder3.toString());
        return ringToneUri;
    }

    /* JADX WARNING: Missing block: B:10:0x0019, code skipped:
            if (r0 != null) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:11:0x001b, code skipped:
            r0.release();
     */
    /* JADX WARNING: Missing block: B:17:0x0027, code skipped:
            if (r0 == null) goto L_0x002a;
     */
    /* JADX WARNING: Missing block: B:18:0x002a, code skipped:
            android.util.Log.w(TAG, "validForProvider: false");
     */
    /* JADX WARNING: Missing block: B:19:0x0032, code skipped:
            return false;
     */
    public static boolean validForProvider(android.content.Context r3, android.net.Uri r4) {
        /*
        r0 = 0;
        r1 = r3.getContentResolver();	 Catch:{ Exception -> 0x0026, all -> 0x001f }
        r1 = r1.acquireUnstableContentProviderClient(r4);	 Catch:{ Exception -> 0x0026, all -> 0x001f }
        r0 = r1;
        if (r0 == 0) goto L_0x0019;
    L_0x000c:
        r1 = r0.getLocalContentProvider();	 Catch:{ Exception -> 0x0026, all -> 0x001f }
        if (r1 == 0) goto L_0x0019;
    L_0x0012:
        r1 = 1;
        if (r0 == 0) goto L_0x0018;
    L_0x0015:
        r0.release();
    L_0x0018:
        return r1;
    L_0x0019:
        if (r0 == 0) goto L_0x002a;
    L_0x001b:
        r0.release();
        goto L_0x002a;
    L_0x001f:
        r1 = move-exception;
        if (r0 == 0) goto L_0x0025;
    L_0x0022:
        r0.release();
    L_0x0025:
        throw r1;
    L_0x0026:
        r1 = move-exception;
        if (r0 == 0) goto L_0x002a;
    L_0x0029:
        goto L_0x001b;
    L_0x002a:
        r1 = "OPNotificationUtils";
        r2 = "validForProvider: false";
        android.util.Log.w(r1, r2);
        r1 = 0;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPNotificationUtils.validForProvider(android.content.Context, android.net.Uri):boolean");
    }

    public static Uri getDefaultUri(int type) {
        if ((type & 1) != 0) {
            return System.DEFAULT_RINGTONE_URI;
        }
        if ((type & 2) != 0) {
            return System.DEFAULT_NOTIFICATION_URI;
        }
        if ((type & 8) != 0) {
            return System.getUriFor("mms_notification");
        }
        if ((type & 4) != 0) {
            return System.DEFAULT_ALARM_ALERT_URI;
        }
        return null;
    }

    public static Uri getActualDefaultRingtoneUri(Context context, int type) {
        String setting = getSettingForType(type);
        Uri ringtoneUri = null;
        if (setting == null) {
            return null;
        }
        String uriString = System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        if (uriString != null) {
            ringtoneUri = Uri.parse(uriString);
        }
        if (ringtoneUri != null && ContentProvider.getUserIdFromUri(ringtoneUri) == context.getUserId()) {
            ringtoneUri = ContentProvider.getUriWithoutUserId(ringtoneUri);
        }
        return ringtoneUri;
    }

    private static String getSettingForType(int type) {
        if ((type & 1) != 0) {
            return "ringtone";
        }
        if ((type & 2) != 0) {
            return "notification_sound";
        }
        if ((type & 4) != 0) {
            return "alarm_alert";
        }
        if ((type & 8) != 0) {
            return "mms_notification";
        }
        return null;
    }

    public static Uri getStaticDefaultRingtoneUri(Context context) {
        String uriString = System.getStringForUser(context.getContentResolver(), "ringtone_default", context.getUserId());
        return uriString != null ? Uri.parse(uriString) : null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008b  */
    /* JADX WARNING: Missing block: B:17:0x0073, code skipped:
            if (r9 != null) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:18:0x0075, code skipped:
            r9.close();
     */
    /* JADX WARNING: Missing block: B:28:0x008e, code skipped:
            if (r9 == null) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:29:0x0091, code skipped:
            return;
     */
    private static void restoreRingtoneIfNotExist(android.content.Context r12, java.lang.String r13) {
        /*
        r0 = r12.getContentResolver();
        r0 = android.provider.Settings.System.getString(r0, r13);
        if (r0 != 0) goto L_0x000c;
    L_0x000b:
        return;
    L_0x000c:
        r8 = r12.getContentResolver();
        r1 = 0;
        r2 = 0;
        r9 = r2;
        r2 = r12.getContentResolver();	 Catch:{ Exception -> 0x007f, all -> 0x007b }
        r3 = android.net.Uri.parse(r0);	 Catch:{ Exception -> 0x007f, all -> 0x007b }
        r4 = "title";
        r4 = new java.lang.String[]{r4};	 Catch:{ Exception -> 0x007f, all -> 0x007b }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x007f, all -> 0x007b }
        r10 = r2;
        r1 = hasData(r10);	 Catch:{ Exception -> 0x0079 }
        if (r1 != 0) goto L_0x006e;
    L_0x002f:
        r2 = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x0079 }
        r1 = "_id";
        r3 = new java.lang.String[]{r1};	 Catch:{ Exception -> 0x0079 }
        r4 = "_display_name=?";
        r1 = 1;
        r5 = new java.lang.String[r1];	 Catch:{ Exception -> 0x0079 }
        r1 = getDefaultRingtoneFileName(r12, r13);	 Catch:{ Exception -> 0x0079 }
        r11 = 0;
        r5[r11] = r1;	 Catch:{ Exception -> 0x0079 }
        r6 = 0;
        r7 = 0;
        r1 = r8;
        r1 = r1.query(r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0079 }
        r9 = r1;
        r1 = hasData(r9);	 Catch:{ Exception -> 0x0079 }
        if (r1 == 0) goto L_0x006e;
    L_0x0051:
        r1 = r9.moveToFirst();	 Catch:{ Exception -> 0x0079 }
        if (r1 == 0) goto L_0x006e;
    L_0x0057:
        r1 = r9.getInt(r11);	 Catch:{ Exception -> 0x0079 }
        r2 = r12.getContentResolver();	 Catch:{ Exception -> 0x0079 }
        r3 = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x0079 }
        r4 = (long) r1;	 Catch:{ Exception -> 0x0079 }
        r3 = android.content.ContentUris.withAppendedId(r3, r4);	 Catch:{ Exception -> 0x0079 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0079 }
        android.provider.Settings.System.putString(r2, r13, r3);	 Catch:{ Exception -> 0x0079 }
    L_0x006e:
        if (r10 == 0) goto L_0x0073;
    L_0x0070:
        r10.close();
    L_0x0073:
        if (r9 == 0) goto L_0x0091;
    L_0x0075:
        r9.close();
        goto L_0x0091;
    L_0x0079:
        r1 = move-exception;
        goto L_0x0082;
    L_0x007b:
        r2 = move-exception;
        r10 = r1;
        r1 = r2;
        goto L_0x0093;
    L_0x007f:
        r2 = move-exception;
        r10 = r1;
        r1 = r2;
    L_0x0082:
        r2 = "OPNotificationUtils";
        r3 = "RemoteException in restoreRingtoneIfNotExist()";
        android.util.Log.e(r2, r3, r1);	 Catch:{ all -> 0x0092 }
        if (r10 == 0) goto L_0x008e;
    L_0x008b:
        r10.close();
    L_0x008e:
        if (r9 == 0) goto L_0x0091;
    L_0x0090:
        goto L_0x0075;
    L_0x0091:
        return;
    L_0x0092:
        r1 = move-exception;
    L_0x0093:
        if (r10 == 0) goto L_0x0098;
    L_0x0095:
        r10.close();
    L_0x0098:
        if (r9 == 0) goto L_0x009d;
    L_0x009a:
        r9.close();
    L_0x009d:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPNotificationUtils.restoreRingtoneIfNotExist(android.content.Context, java.lang.String):void");
    }

    private static boolean hasData(Cursor c) {
        return c != null && c.getCount() > 0;
    }

    private static String getDefaultRingtoneFileName(Context mContext, String settingName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DEFAULT_RINGTONE_PROPERTY_PREFIX_RO);
        stringBuilder.append(settingName);
        return SystemProperties.get(stringBuilder.toString());
    }
}