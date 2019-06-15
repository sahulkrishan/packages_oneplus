package com.android.settings.gestures;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;

public class DoubleTwistPreferenceController extends GesturePreferenceController {
    private static final String PREF_KEY_VIDEO = "gesture_double_twist_video";
    private final int OFF = 0;
    private final int ON = 1;
    private final String mDoubleTwistPrefKey;
    private final UserManager mUserManager;

    public DoubleTwistPreferenceController(Context context, String key) {
        super(context, key);
        this.mDoubleTwistPrefKey = key;
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        if (!isGestureAvailable(context) || prefs.getBoolean(DoubleTwistGestureSettings.PREF_KEY_SUGGESTION_COMPLETE, false)) {
            return true;
        }
        return false;
    }

    public static boolean isGestureAvailable(Context context) {
        Resources resources = context.getResources();
        String name = resources.getString(R.string.gesture_double_twist_sensor_name);
        String vendor = resources.getString(R.string.gesture_double_twist_sensor_vendor);
        if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(vendor))) {
            for (Sensor s : ((SensorManager) context.getSystemService("sensor")).getSensorList(-1)) {
                if (name.equals(s.getName()) && vendor.equals(s.getVendor())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getAvailabilityStatus() {
        return isGestureAvailable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_twist");
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public String getPreferenceKey() {
        return this.mDoubleTwistPrefKey;
    }

    public boolean setChecked(boolean isChecked) {
        setDoubleTwistPreference(this.mContext, this.mUserManager, isChecked);
        return true;
    }

    public static void setDoubleTwistPreference(Context context, UserManager userManager, int enabled) {
        Secure.putInt(context.getContentResolver(), "camera_double_twist_to_flip_enabled", enabled);
        int managedProfileUserId = getManagedProfileId(userManager);
        if (managedProfileUserId != -10000) {
            Secure.putIntForUser(context.getContentResolver(), "camera_double_twist_to_flip_enabled", enabled, managedProfileUserId);
        }
    }

    public boolean isChecked() {
        if (Secure.getInt(this.mContext.getContentResolver(), "camera_double_twist_to_flip_enabled", 1) != 0) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public static int getManagedProfileId(UserManager userManager) {
        return Utils.getManagedProfileId(userManager, UserHandle.myUserId());
    }
}
