package com.oneplus.settings.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.android.settings.datausage.OPDataUsageUtils;

public class OPFirewallUtils {
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_PACKAGE = "pkg";
    public static final String COLUMN_WLAN = "wlan";
    public static final String OPSAFE_AUTHORITY = "com.oneplus.security.database.SafeProvider";
    public static final String TABLE_NETWORK_RESTRICT = "network_restrict";
    private static final String TAG = "OPFirewallUtils";
    public static final Uri URI_NETWORK_RESTRICT = Uri.withAppendedPath(URI_OPSAFE_BASE, TABLE_NETWORK_RESTRICT);
    public static final Uri URI_OPSAFE_BASE = Uri.parse(OPDataUsageUtils.ONEPLUS_SECURITY_URI);
    public static final String _ID = "_id";

    public static void addOrUpdateRole(Context context, OPFirewallRule firewallRule) {
        ContentValues values = new ContentValues();
        if (firewallRule.getWlan() != null) {
            values.put(COLUMN_WLAN, Integer.valueOf(firewallRule.getWlan().intValue() == 0 ? 0 : 1));
        }
        if (firewallRule.getMobile() != null) {
            values.put(COLUMN_MOBILE, Integer.valueOf(firewallRule.getMobile().intValue() == 0 ? 0 : 1));
        }
        if (selectFirewallRuleByPkg(context, firewallRule.getPkg()) == null) {
            try {
                values.put("pkg", firewallRule.getPkg());
                context.getContentResolver().insert(URI_NETWORK_RESTRICT, values);
                return;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return;
            }
        }
        context.getContentResolver().update(URI_NETWORK_RESTRICT, values, "pkg = ? ", new String[]{firewallRule.getPkg()});
    }

    /* JADX WARNING: Missing block: B:11:0x0062, code skipped:
            if (r0 != null) goto L_0x0064;
     */
    /* JADX WARNING: Missing block: B:12:0x0064, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:17:0x0074, code skipped:
            if (r0 == null) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:18:0x0077, code skipped:
            return r1;
     */
    public static java.util.List<com.oneplus.settings.utils.OPFirewallRule> selectAllFirewallRules(android.content.Context r10) {
        /*
        r0 = 0;
        r1 = 0;
        r2 = r10.getContentResolver();	 Catch:{ Exception -> 0x006a }
        r3 = URI_NETWORK_RESTRICT;	 Catch:{ Exception -> 0x006a }
        r4 = 0;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x006a }
        r0 = r2;
        if (r0 == 0) goto L_0x0062;
    L_0x0013:
        r2 = r0.getCount();	 Catch:{ Exception -> 0x006a }
        if (r2 <= 0) goto L_0x0062;
    L_0x0019:
        r2 = new java.util.ArrayList;	 Catch:{ Exception -> 0x006a }
        r2.<init>();	 Catch:{ Exception -> 0x006a }
        r1 = r2;
    L_0x001f:
        r2 = r0.moveToNext();	 Catch:{ Exception -> 0x006a }
        if (r2 == 0) goto L_0x0062;
    L_0x0025:
        r2 = "_id";
        r2 = r0.getColumnIndex(r2);	 Catch:{ Exception -> 0x006a }
        r2 = r0.getInt(r2);	 Catch:{ Exception -> 0x006a }
        r3 = "pkg";
        r3 = r0.getColumnIndex(r3);	 Catch:{ Exception -> 0x006a }
        r3 = r0.getString(r3);	 Catch:{ Exception -> 0x006a }
        r4 = "wlan";
        r4 = r0.getColumnIndex(r4);	 Catch:{ Exception -> 0x006a }
        r4 = r0.getInt(r4);	 Catch:{ Exception -> 0x006a }
        r5 = "mobile";
        r5 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x006a }
        r5 = r0.getInt(r5);	 Catch:{ Exception -> 0x006a }
        r6 = new com.oneplus.settings.utils.OPFirewallRule;	 Catch:{ Exception -> 0x006a }
        r7 = java.lang.Integer.valueOf(r2);	 Catch:{ Exception -> 0x006a }
        r8 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x006a }
        r9 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x006a }
        r6.<init>(r7, r3, r8, r9);	 Catch:{ Exception -> 0x006a }
        r1.add(r6);	 Catch:{ Exception -> 0x006a }
        goto L_0x001f;
    L_0x0062:
        if (r0 == 0) goto L_0x0077;
    L_0x0064:
        r0.close();
        goto L_0x0077;
    L_0x0068:
        r2 = move-exception;
        goto L_0x0078;
    L_0x006a:
        r2 = move-exception;
        r3 = "OPFirewallUtils";
        r4 = r2.getMessage();	 Catch:{ all -> 0x0068 }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x0068 }
        if (r0 == 0) goto L_0x0077;
    L_0x0076:
        goto L_0x0064;
    L_0x0077:
        return r1;
    L_0x0078:
        if (r0 == 0) goto L_0x007d;
    L_0x007a:
        r0.close();
    L_0x007d:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPFirewallUtils.selectAllFirewallRules(android.content.Context):java.util.List");
    }

    /* JADX WARNING: Missing block: B:13:0x0066, code skipped:
            if (r1 != null) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:14:0x0068, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:19:0x0078, code skipped:
            if (r1 == null) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:20:0x007b, code skipped:
            return null;
     */
    public static com.oneplus.settings.utils.OPFirewallRule selectFirewallRuleByPkg(android.content.Context r10, java.lang.String r11) {
        /*
        r0 = 0;
        r1 = r0;
        r2 = r10.getContentResolver();	 Catch:{ Exception -> 0x006e }
        r3 = URI_NETWORK_RESTRICT;	 Catch:{ Exception -> 0x006e }
        r4 = 0;
        r5 = "pkg = ? ";
        r6 = 1;
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x006e }
        r7 = 0;
        r6[r7] = r11;	 Catch:{ Exception -> 0x006e }
        r7 = 0;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x006e }
        r1 = r2;
        if (r1 == 0) goto L_0x0066;
    L_0x0019:
        r2 = r1.getCount();	 Catch:{ Exception -> 0x006e }
        if (r2 <= 0) goto L_0x0066;
    L_0x001f:
        r2 = r1.moveToNext();	 Catch:{ Exception -> 0x006e }
        if (r2 == 0) goto L_0x0066;
    L_0x0025:
        r2 = "_id";
        r2 = r1.getColumnIndex(r2);	 Catch:{ Exception -> 0x006e }
        r2 = r1.getInt(r2);	 Catch:{ Exception -> 0x006e }
        r3 = "pkg";
        r3 = r1.getColumnIndex(r3);	 Catch:{ Exception -> 0x006e }
        r3 = r1.getString(r3);	 Catch:{ Exception -> 0x006e }
        r4 = "wlan";
        r4 = r1.getColumnIndex(r4);	 Catch:{ Exception -> 0x006e }
        r4 = r1.getInt(r4);	 Catch:{ Exception -> 0x006e }
        r5 = "mobile";
        r5 = r1.getColumnIndex(r5);	 Catch:{ Exception -> 0x006e }
        r5 = r1.getInt(r5);	 Catch:{ Exception -> 0x006e }
        r6 = new com.oneplus.settings.utils.OPFirewallRule;	 Catch:{ Exception -> 0x006e }
        r7 = java.lang.Integer.valueOf(r2);	 Catch:{ Exception -> 0x006e }
        r8 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x006e }
        r9 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x006e }
        r6.<init>(r7, r3, r8, r9);	 Catch:{ Exception -> 0x006e }
        r0 = r6;
        if (r1 == 0) goto L_0x0065;
    L_0x0062:
        r1.close();
    L_0x0065:
        return r0;
    L_0x0066:
        if (r1 == 0) goto L_0x007b;
    L_0x0068:
        r1.close();
        goto L_0x007b;
    L_0x006c:
        r0 = move-exception;
        goto L_0x007c;
    L_0x006e:
        r2 = move-exception;
        r3 = "OPFirewallUtils";
        r4 = r2.getMessage();	 Catch:{ all -> 0x006c }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x006c }
        if (r1 == 0) goto L_0x007b;
    L_0x007a:
        goto L_0x0068;
    L_0x007b:
        return r0;
    L_0x007c:
        if (r1 == 0) goto L_0x0081;
    L_0x007e:
        r1.close();
    L_0x0081:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPFirewallUtils.selectFirewallRuleByPkg(android.content.Context, java.lang.String):com.oneplus.settings.utils.OPFirewallRule");
    }
}
