package com.oneplus.lib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.provider.Settings.System;
import java.lang.reflect.Method;

public class VibratorSceneUtils {
    public static final int VIBRATOR_SCENE_CALENDAR_DATE = 3;
    public static final int VIBRATOR_SCENE_CHARGER = 21;
    public static final int VIBRATOR_SCENE_CLOCK_MINUTE = 1030;
    public static final int VIBRATOR_SCENE_CLOCK_TICK = 2;
    public static final int VIBRATOR_SCENE_DASH_CHARGER = 22;
    public static final int VIBRATOR_SCENE_GESTURE_KEY = 24;
    public static final int VIBRATOR_SCENE_INVALID = -1;
    public static final int VIBRATOR_SCENE_LONG_PRESS = 4;
    public static final int VIBRATOR_SCENE_POWER_KEY_ASSIST = 23;
    public static final int VIBRATOR_SCENE_POWER_KEY_MENU = 20;
    public static final int VIBRATOR_SCENE_SWITCH = 1003;
    public static final int VIBRATOR_SCENE_SWITCH_FOR_CLOCK = 1002;
    public static final int VIBRATOR_SCENE_VIRTUAL_KEY = 1;

    private VibratorSceneUtils() {
    }

    public static long[] getVibratorScenePattern(Context context, Vibrator vibratorService, int vibratorSceneId) {
        int intensity = new int[]{-1, -2, -3}[System.getInt(context.getContentResolver(), "vibrate_on_touch_intensity", 0)];
        if (vibratorService != null) {
            try {
                Method sSetVibratorEffect = vibratorService.getClass().getDeclaredMethod("setVibratorEffect", new Class[]{Integer.TYPE});
                if (sSetVibratorEffect != null) {
                    sSetVibratorEffect.setAccessible(true);
                    int duration = ((Integer) sSetVibratorEffect.invoke(vibratorService, new Object[]{Integer.valueOf(vibratorSceneId)})).intValue();
                    return new long[]{(long) intensity, 0, (long) duration};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressLint({"MissingPermission"})
    public static void vibrateIfNeeded(long[] pattern, Vibrator vibratorService) {
        if (vibratorService != null && pattern != null) {
            try {
                vibratorService.vibrate(pattern, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean systemVibrateEnabled(Context context) {
        return System.getInt(context.getContentResolver(), "haptic_feedback_enabled", 0) == 1;
    }
}
