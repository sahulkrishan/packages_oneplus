package com.oneplus.settings.gestures;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.os.Process;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.settings.R;
import com.oneplus.settings.utils.OPConstants;
import java.util.List;

public class OPGestureUtils {
    private static final String TAG = "OPGestureUtils";

    public static String getGestureTypebyGestureKey(String key) {
        String gestureType = "";
        if (key.equals(OPConstants.KEY_DRAW_O_START_APP)) {
            return OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_O;
        }
        if (key.equals(OPConstants.KEY_DRAW_V_START_APP)) {
            return OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_V;
        }
        if (key.equals(OPConstants.KEY_DRAW_S_START_APP)) {
            return OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_S;
        }
        if (key.equals(OPConstants.KEY_DRAW_M_START_APP)) {
            return OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_M;
        }
        if (key.equals(OPConstants.KEY_DRAW_W_START_APP)) {
            return OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_W;
        }
        return gestureType;
    }

    public static String getGesturePackageName(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary)) {
            return context.getString(R.string.oneplus_draw_gesture_start_none);
        }
        String[] actionString = actionSummary.split(";");
        String actionPackageName = actionString[null];
        if (actionPackageName.startsWith(OPConstants.OPEN_APP)) {
            return actionPackageName.substring(OPConstants.OPEN_APP.length());
        }
        if (!actionPackageName.startsWith(OPConstants.OPEN_SHORTCUT)) {
            return "";
        }
        actionPackageName = actionPackageName.substring(OPConstants.OPEN_SHORTCUT.length());
        if (hasShortCutsId(context, actionPackageName, actionString[1])) {
            return actionPackageName;
        }
        return context.getString(R.string.oneplus_draw_gesture_start_none);
    }

    public static String getGesturePacakgeUid(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary)) {
            return "";
        }
        String[] actionString = actionSummary.split(";");
        String actionPackageName = actionString[null];
        String uid;
        if (actionPackageName.startsWith(OPConstants.OPEN_APP)) {
            uid = "";
            if (actionString.length > 1) {
                uid = actionString[1];
            }
            return uid;
        } else if (!actionPackageName.startsWith(OPConstants.OPEN_SHORTCUT)) {
            return "";
        } else {
            actionPackageName = actionPackageName.substring(OPConstants.OPEN_SHORTCUT.length());
            uid = "";
            if (actionString.length > 2) {
                uid = actionString[2];
            }
            return uid;
        }
    }

    public static String getGestureSummarybyGestureKey(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary)) {
            return context.getString(R.string.oneplus_draw_gesture_start_none);
        }
        if (OPConstants.OPEN_BACK_CAMERA.equals(actionSummary)) {
            return context.getString(R.string.oneplus_gestures_open_camera);
        }
        if (OPConstants.OPEN_FRONT_CAMERA.equals(actionSummary)) {
            return context.getString(R.string.oneplus_gestures_open_front_camera);
        }
        if (OPConstants.OPEN_TAKE_VIDEO.equals(actionSummary)) {
            return context.getString(R.string.oneplus_gestures_take_video);
        }
        if (OPConstants.OPEN_FLASH_LIGHT.equals(actionSummary)) {
            return context.getString(R.string.oneplus_gestures_open_flashlight);
        }
        if (OPConstants.OPEN_SHELF.equals(actionSummary)) {
            return context.getString(R.string.hardware_keys_action_shelf);
        }
        String[] actionString = actionSummary.split(";");
        String actionPackageName = actionString[null];
        if (actionPackageName.startsWith(OPConstants.OPEN_APP)) {
            actionPackageName = actionPackageName.substring(OPConstants.OPEN_APP.length());
        } else if (actionPackageName.startsWith(OPConstants.OPEN_SHORTCUT)) {
            actionPackageName = actionPackageName.substring(OPConstants.OPEN_SHORTCUT.length());
            String actionShortcutId = actionString[1];
            if (!hasShortCutsId(context, actionPackageName, actionShortcutId)) {
                return context.getString(R.string.oneplus_draw_gesture_start_none);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getAppNameByPackageName(context, actionPackageName));
            stringBuilder.append("/");
            stringBuilder.append(getShortCutsNameByID(context, actionPackageName, actionShortcutId));
            return stringBuilder.toString();
        }
        return getAppNameByPackageName(context, actionPackageName);
    }

    public static String getShortCutNameByGestureKey(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary)) {
            return context.getString(R.string.oneplus_draw_gesture_start_none);
        }
        String[] actionString = actionSummary.split(";");
        String actionPackageName = actionString[null];
        if (actionPackageName.startsWith(OPConstants.OPEN_APP)) {
            return getAppNameByPackageName(context, actionPackageName.substring(OPConstants.OPEN_APP.length()));
        }
        if (actionPackageName.startsWith(OPConstants.OPEN_SHORTCUT)) {
            return actionString[1];
        }
        return "";
    }

    public static String getShortCutIdByGestureKey(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary)) {
            return context.getString(R.string.oneplus_draw_gesture_start_none);
        }
        String[] actionString = actionSummary.split(";");
        if (actionString[null].startsWith(OPConstants.OPEN_SHORTCUT)) {
            return actionString[1];
        }
        return "";
    }

    public static boolean hasShortCutsGesture(Context context, String key) {
        String realKey;
        String noneSummary = context.getString(R.string.oneplus_draw_gesture_start_none);
        if (key.startsWith(OPConstants.OEM_ACC_BLACKSCREEN_GESTURE)) {
            realKey = key;
        } else {
            realKey = getGestureTypebyGestureKey(key);
        }
        String actionSummary = System.getString(context.getContentResolver(), realKey);
        if (TextUtils.isEmpty(actionSummary) || !actionSummary.contains(OPConstants.OPEN_SHORTCUT)) {
            return false;
        }
        return true;
    }

    public static String getAppNameByPackageName(Context context, String mPackageName) {
        PackageManager tempPackageManager = context.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        mainIntent.setPackage(mPackageName);
        List<ResolveInfo> resolveInfo = tempPackageManager.queryIntentActivities(mainIntent, 0);
        if (resolveInfo.size() > 0) {
            return (String) ((ResolveInfo) resolveInfo.get(0)).loadLabel(tempPackageManager);
        }
        return context.getString(R.string.oneplus_draw_gesture_start_none);
    }

    public static int getIndexByGestureValueKey(String key) {
        if (key.equals(OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_O)) {
            return 6;
        }
        if (key.equals(OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_V)) {
            return 0;
        }
        if (key.equals(OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_S)) {
            return 8;
        }
        if (key.equals(OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_M)) {
            return 9;
        }
        if (key.equals(OPConstants.OEM_ACC_BLACKSCREEN_GESTRUE_W)) {
            return 10;
        }
        return 0;
    }

    public static int get(int num, int index) {
        return ((1 << index) & num) >> index;
    }

    public static int set1(Context context, int index) {
        int mul;
        if (index != 15) {
            switch (index) {
                case 0:
                    mul = 1;
                    break;
                case 1:
                    mul = 2;
                    break;
                case 2:
                    mul = 4;
                    break;
                case 3:
                    mul = 8;
                    break;
                case 4:
                    mul = 16;
                    break;
                case 5:
                    mul = 32;
                    break;
                case 6:
                    mul = 64;
                    break;
                case 7:
                    mul = 128;
                    break;
                case 8:
                    mul = 256;
                    break;
                case 9:
                    mul = 512;
                    break;
                case 10:
                    mul = 1024;
                    break;
                case 11:
                    mul = 2048;
                    break;
                default:
                    mul = 0;
                    break;
            }
        }
        mul = 32768;
        int gestureValue = System.getInt(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0);
        System.putInt(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", gestureValue | mul);
        return gestureValue | mul;
    }

    public static int set0(Context context, int index) {
        int mul;
        if (index != 15) {
            switch (index) {
                case 0:
                    mul = 65534;
                    break;
                case 1:
                    mul = 65533;
                    break;
                case 2:
                    mul = 65531;
                    break;
                case 3:
                    mul = 65527;
                    break;
                case 4:
                    mul = 65519;
                    break;
                case 5:
                    mul = 65503;
                    break;
                case 6:
                    mul = 65471;
                    break;
                case 7:
                    mul = 65407;
                    break;
                case 8:
                    mul = 65279;
                    break;
                case 9:
                    mul = 65023;
                    break;
                case 10:
                    mul = 64511;
                    break;
                case 11:
                    mul = 63487;
                    break;
                default:
                    mul = 65535;
                    break;
            }
        }
        mul = 32767;
        int gestureValue = System.getInt(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0);
        System.putInt(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", gestureValue & mul);
        return gestureValue & mul;
    }

    public static List<ShortcutInfo> loadShortCuts(Context context, String packageName) {
        LauncherApps mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
        ShortcutQuery mQuery = new ShortcutQuery();
        mQuery.setQueryFlags(11);
        mQuery.setPackage(packageName);
        return mLauncherApps.getShortcuts(mQuery, Process.myUserHandle());
    }

    public static boolean hasShortCuts(Context context, String packageName) {
        List<ShortcutInfo> shortcutInfo = loadShortCuts(context, packageName);
        boolean z = false;
        if (shortcutInfo == null) {
            return false;
        }
        if (shortcutInfo.size() > 0) {
            z = true;
        }
        return z;
    }

    public static boolean hasShortCutsId(Context context, String packageName, String shortCutId) {
        List<ShortcutInfo> shortcutInfo = loadShortCuts(context, packageName);
        if (shortcutInfo == null) {
            return false;
        }
        boolean hasShortcutId = false;
        for (ShortcutInfo s : shortcutInfo) {
            if (s.getId().equals(shortCutId)) {
                hasShortcutId = true;
                break;
            }
        }
        return hasShortcutId;
    }

    public static String getShortCutsNameByID(Context context, String packageName, String shortCutId) {
        List<ShortcutInfo> shortcutInfo = loadShortCuts(context, packageName);
        String shortCutName = "";
        if (shortcutInfo != null) {
            for (ShortcutInfo s : shortcutInfo) {
                if (s.getId().equals(shortCutId)) {
                    CharSequence label = s.getShortLabel();
                    if (TextUtils.isEmpty(label)) {
                        label = shortCutId;
                    }
                    shortCutName = label.toString();
                }
            }
        }
        return shortCutName;
    }
}
