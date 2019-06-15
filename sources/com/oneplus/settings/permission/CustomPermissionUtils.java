package com.oneplus.settings.permission;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class CustomPermissionUtils {
    public static final String CUSTOM_PERMISSION_CONTROL_BLUETOOTH = "CUSTOM_PERMISSION_CONTROL_BLUETOOTH";
    public static final String CUSTOM_PERMISSION_CONTROL_READ_BOOKMARKS = "CUSTOM_PERMISSION_CONTROL_READ_BOOKMARKS";
    public static final String CUSTOM_PERMISSION_CONTROL_WIFI = "CUSTOM_PERMISSION_CONTROL_WIFI";
    public static final String CUSTOM_PERMISSION_READ_MMS = "CUSTOM_PERMISSION_READ_MMS";
    public static final String CUSTOM_PERMISSION_SEND_MMS = "CUSTOM_PERMISSION_SEND_MMS";
    public static LinkedHashMap<String, String> mCustomPermissionToOriginalGroup = new LinkedHashMap();

    private CustomPermissionUtils() {
    }

    static {
        mCustomPermissionToOriginalGroup.put(CUSTOM_PERMISSION_READ_MMS, "android.permission-group.SMS");
        mCustomPermissionToOriginalGroup.put(CUSTOM_PERMISSION_SEND_MMS, "android.permission-group.SMS");
    }

    public static LinkedHashMap<String, String> getCustomPermissionsForOriginalGroup() {
        return mCustomPermissionToOriginalGroup;
    }

    public static boolean isCustomPermission(String permissionName) {
        return permissionName != null && permissionName.startsWith("CUSTOM_PERMISSION");
    }

    public static String getGroupForCustomPermission(String customPermissionName) {
        String affectedGroupName = "";
        for (Entry<String, String> entry : mCustomPermissionToOriginalGroup.entrySet()) {
            if (customPermissionName.equals((String) entry.getKey())) {
                return (String) entry.getValue();
            }
        }
        return affectedGroupName;
    }

    public static ArrayList<String> getAffectedCustomPermissionsForGroup(String groupName) {
        ArrayList<String> result = new ArrayList();
        for (Entry<String, String> entry : mCustomPermissionToOriginalGroup.entrySet()) {
            if (groupName.equals(entry.getValue())) {
                result.add((String) entry.getKey());
            }
        }
        return result;
    }

    public static java.lang.String getGroupLabelForCustomPermission(android.content.Context r3, java.lang.String r4) {
        /*
        r0 = -1;
        r1 = r4.hashCode();
        r2 = -1;
        switch(r1) {
            case -619278727: goto L_0x0032;
            case -555081698: goto L_0x0028;
            case -423499350: goto L_0x001e;
            case 1196086488: goto L_0x0014;
            case 1624896300: goto L_0x000a;
            default: goto L_0x0009;
        };
    L_0x0009:
        goto L_0x003c;
    L_0x000a:
        r1 = "CUSTOM_PERMISSION_READ_MMS";
        r1 = r4.equals(r1);
        if (r1 == 0) goto L_0x003c;
    L_0x0012:
        r1 = 0;
        goto L_0x003d;
    L_0x0014:
        r1 = "CUSTOM_PERMISSION_CONTROL_READ_BOOKMARKS";
        r1 = r4.equals(r1);
        if (r1 == 0) goto L_0x003c;
    L_0x001c:
        r1 = 4;
        goto L_0x003d;
    L_0x001e:
        r1 = "CUSTOM_PERMISSION_CONTROL_BLUETOOTH";
        r1 = r4.equals(r1);
        if (r1 == 0) goto L_0x003c;
    L_0x0026:
        r1 = 3;
        goto L_0x003d;
    L_0x0028:
        r1 = "CUSTOM_PERMISSION_SEND_MMS";
        r1 = r4.equals(r1);
        if (r1 == 0) goto L_0x003c;
    L_0x0030:
        r1 = 1;
        goto L_0x003d;
    L_0x0032:
        r1 = "CUSTOM_PERMISSION_CONTROL_WIFI";
        r1 = r4.equals(r1);
        if (r1 == 0) goto L_0x003c;
    L_0x003a:
        r1 = 2;
        goto L_0x003d;
    L_0x003c:
        r1 = r2;
    L_0x003d:
        switch(r1) {
            case 0: goto L_0x004d;
            case 1: goto L_0x004d;
            case 2: goto L_0x0049;
            case 3: goto L_0x0045;
            case 4: goto L_0x0041;
            default: goto L_0x0040;
        };
    L_0x0040:
        goto L_0x0051;
    L_0x0041:
        r0 = 2131887227; // 0x7f12047b float:1.9409055E38 double:1.0532922397E-314;
        goto L_0x0051;
    L_0x0045:
        r0 = 2131887226; // 0x7f12047a float:1.9409053E38 double:1.053292239E-314;
        goto L_0x0051;
    L_0x0049:
        r0 = 2131887229; // 0x7f12047d float:1.940906E38 double:1.0532922407E-314;
        goto L_0x0051;
    L_0x004d:
        r0 = 2131887228; // 0x7f12047c float:1.9409057E38 double:1.05329224E-314;
    L_0x0051:
        if (r0 != r2) goto L_0x0056;
    L_0x0053:
        r1 = "";
        return r1;
    L_0x0056:
        r1 = r3.getResources();
        r1 = r1.getString(r0);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.permission.CustomPermissionUtils.getGroupLabelForCustomPermission(android.content.Context, java.lang.String):java.lang.String");
    }

    public static boolean isOtherCustomPermission(String customPerm) {
        return TextUtils.isEmpty(getGroupForCustomPermission(customPerm));
    }
}
