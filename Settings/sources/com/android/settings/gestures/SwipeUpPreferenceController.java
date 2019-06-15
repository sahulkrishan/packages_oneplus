package com.android.settings.gestures;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;

public class SwipeUpPreferenceController extends GesturePreferenceController {
    private static final String ACTION_QUICKSTEP = "android.intent.action.QUICKSTEP_SERVICE";
    private static final String PREF_KEY_VIDEO = "gesture_swipe_up_video";
    private final int OFF = 0;
    private final int ON = 1;
    private final UserManager mUserManager;

    public SwipeUpPreferenceController(Context context, String key) {
        super(context, key);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    static boolean isGestureAvailable(Context context) {
        if (!context.getResources().getBoolean(17957054)) {
            return false;
        }
        if (context.getPackageManager().resolveService(new Intent(ACTION_QUICKSTEP).setPackage(ComponentName.unflattenFromString(context.getString(17039733)).getPackageName()), 1048576) == null) {
            return false;
        }
        return true;
    }

    public int getAvailabilityStatus() {
        return isGestureAvailable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_swipe_up");
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean setChecked(boolean isChecked) {
        setSwipeUpPreference(this.mContext, this.mUserManager, isChecked);
        return true;
    }

    public static void setSwipeUpPreference(Context context, UserManager userManager, int enabled) {
        Secure.putInt(context.getContentResolver(), "swipe_up_to_switch_apps_enabled", enabled);
    }

    public boolean isChecked() {
        return Secure.getInt(this.mContext.getContentResolver(), "swipe_up_to_switch_apps_enabled", this.mContext.getResources().getBoolean(17957053)) != 0;
    }
}
