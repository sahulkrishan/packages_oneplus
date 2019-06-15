package com.oneplus.settings.utils;

import android.content.Context;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import com.oneplus.util.RingtoneManagerUtils;
import java.util.Arrays;

public final class OPVibrateUtils {
    private static final String KEY_INCOMING_CALL_VIBRATE = "incoming_call_vibrate_mode";
    public static final int OP_THREE_KEY_MODE_MUTE = 1;
    public static final int OP_THREE_KEY_MODE_RINGTONE = 3;
    public static final int OP_THREE_KEY_MODE_VIBRATE = 2;
    private static final String TAG = "OPVibrateUtils";
    public static final int VIBRATE_INTERVAL = 1000;
    public static final int VIBRATE_MODE_DYNAMIC = 5;
    public static final int VIBRATE_MODE_FIVE = 4;
    public static final int VIBRATE_MODE_FOUR = 3;
    public static final int VIBRATE_MODE_ONE = 0;
    public static final int VIBRATE_MODE_THREE = 2;
    public static final int VIBRATE_MODE_TWO = 1;
    public static final int VIBRATE_REPEAT_TYPE = 0;
    private static final long[] mAlarmVibratePattern = new long[]{500, 500};
    private static long[][] sVibratePatternrhythm = new long[][]{new long[]{-2, 0, 1000, 1000, 1000, 1000}, new long[]{-2, 0, 500, 250, 10, 1000, 500, 250, 10, 1000}, new long[]{-2, 0, 300, 400, 300, 400, 300, 1000, 300, 400, 300, 400, 300, 1000}, new long[]{-2, 0, 30, 80, 30, 80, 50, 180, 600, 1000, 30, 80, 30, 80, 50, 180, 600, 1000}, new long[]{-2, 0, 80, 200, 600, 150, 10, 1000, 80, 200, 600, 150, 10, 1000}};

    public static void startVibrateByType(Vibrator vibrator, int type) {
        if (vibrator != null) {
            int intensityvalue = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "incoming_call_vibrate_intensity", -1);
            vibrator.cancel();
            if (type >= 5) {
                type = 0;
            }
            if (intensityvalue == 0) {
                sVibratePatternrhythm[type][0] = -1;
            } else if (intensityvalue == 1) {
                sVibratePatternrhythm[type][0] = -2;
            } else if (intensityvalue == 2) {
                sVibratePatternrhythm[type][0] = -3;
            }
            vibrator.vibrate(sVibratePatternrhythm[type], 0);
        }
    }

    public static void startVibrateByType(Vibrator vibrator) {
        if (vibrator != null) {
            int intensityvalue = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "incoming_call_vibrate_intensity", -1);
            vibrator.cancel();
            int type = getRingtoneVibrateMode(SettingsBaseApplication.mApplication);
            if (type >= 5) {
                type = 0;
            }
            if (intensityvalue == 0) {
                sVibratePatternrhythm[type][0] = -1;
            } else if (intensityvalue == 1) {
                sVibratePatternrhythm[type][0] = -2;
            } else if (intensityvalue == 2) {
                sVibratePatternrhythm[type][0] = -3;
            }
            vibrator.vibrate(sVibratePatternrhythm[type], 0);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("startVibrateByType--type:");
            stringBuilder.append(type);
            stringBuilder.append(" pattern:");
            stringBuilder.append(Arrays.toString(sVibratePatternrhythm[type]));
            Log.d(str, stringBuilder.toString());
        }
    }

    public static void startVibrateByIndex(Vibrator vibrator, int index) {
        if (vibrator != null) {
            int intensityvalue = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "incoming_call_vibrate_intensity", -1);
            vibrator.cancel();
            if (!OPUtils.isSupportXVibrate()) {
                if (index >= 5) {
                    index = 0;
                }
                if (intensityvalue == 0) {
                    sVibratePatternrhythm[index][0] = -1;
                } else if (intensityvalue == 1) {
                    sVibratePatternrhythm[index][0] = -2;
                } else if (intensityvalue == 2) {
                    sVibratePatternrhythm[index][0] = -3;
                }
                vibrator.vibrate(sVibratePatternrhythm[index], -1);
            } else if (index != 0) {
                if (intensityvalue == 0) {
                    sVibratePatternrhythm[index - 1][0] = -1;
                } else if (intensityvalue == 1) {
                    sVibratePatternrhythm[index - 1][0] = -2;
                } else if (intensityvalue == 2) {
                    sVibratePatternrhythm[index - 1][0] = -3;
                }
                vibrator.vibrate(sVibratePatternrhythm[index - 1], -1);
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("startVibrateByIndex--index:");
                stringBuilder.append(index);
                stringBuilder.append(" pattern:");
                stringBuilder.append(Arrays.toString(sVibratePatternrhythm[index - 1]));
                Log.d(str, stringBuilder.toString());
            }
        }
    }

    public static boolean isDynamicVibrateMode(Context context) {
        return System.getInt(context.getContentResolver(), KEY_INCOMING_CALL_VIBRATE, 0) == 5;
    }

    public static int getRingtoneVibrateMode(Context context) {
        return System.getInt(context.getContentResolver(), KEY_INCOMING_CALL_VIBRATE, 0);
    }

    public static boolean isThreeKeyVibrateMode(Context context) {
        boolean z = true;
        if (Global.getInt(context.getContentResolver(), "three_Key_mode", 1) != 2) {
            z = false;
        }
        return z;
    }

    public static boolean isThreeKeyMuteMode(Context context) {
        boolean z = true;
        if (Global.getInt(context.getContentResolver(), "three_Key_mode", 1) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isThreeKeyRingMode(Context context) {
        boolean z = true;
        if (Global.getInt(context.getContentResolver(), "three_Key_mode", 1) != 3) {
            z = false;
        }
        return z;
    }

    public static boolean isVibrateWhenRinging(Context context) {
        boolean z = true;
        if (System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isSystemRingtone(Uri uri, int type) {
        return OPRingtoneManager.isSystemRingtone(SettingsBaseApplication.mApplication, uri, type);
    }

    public static void startVibrateForRingtone(Context context, Uri uri, Vibrator vibrator) {
        if (!OPUtils.isSupportXVibrate() || !isSystemRingtone(uri, 1) || isThreeKeyMuteMode(context) || !isVibrateWhenRinging(context)) {
            return;
        }
        if (!(isThreeKeyVibrateMode(context) && isDynamicVibrateMode(context)) && isThreeKeyRingMode(context)) {
            if (isDynamicVibrateMode(context)) {
                startVibrateWithRingtoneUri(context, uri, vibrator);
            } else {
                startVibrateByType(vibrator);
            }
        }
    }

    public static void startVibrateForSms(Context context, Uri uri, Vibrator vibrator) {
        if (!(isThreeKeyVibrateMode(context) || !OPUtils.isSupportXVibrate() || isThreeKeyMuteMode(context))) {
            if (isSystemRingtone(uri, 8)) {
                startVibrateWithRingtoneUri(context, uri, vibrator);
            } else if (!isThreeKeyVibrateMode(context)) {
                Log.d(TAG, "startVibrateForSms--normal--vibrate");
            }
        }
    }

    public static void startVibrateForNotification(Context context, Uri uri, Vibrator vibrator) {
        if (!(isThreeKeyVibrateMode(context) || !OPUtils.isSupportXVibrate() || isThreeKeyMuteMode(context))) {
            if (isSystemRingtone(uri, 2)) {
                startVibrateWithRingtoneUri(context, uri, vibrator);
            } else if (!isThreeKeyVibrateMode(context)) {
                Log.d(TAG, "startVibrateForNotification--normal-vibrate");
            }
        }
    }

    public static void startVibrateForAlarm(Context context, Uri uri, Vibrator vibrator) {
        if (((AudioManager) context.getSystemService("audio")).getStreamVolume(4) != 0 && OPUtils.isSupportXVibrate() && isThreeKeyRingMode(context)) {
            if (isSystemRingtone(uri, 4)) {
                startVibrateWithRingtoneUri(context, uri, vibrator);
            } else {
                Log.d(TAG, "startVibrateForAlarm--normal-vibrate");
                if (VERSION.SDK_INT >= 21) {
                    vibrator.vibrate(mAlarmVibratePattern, 0, new Builder().setUsage(4).setContentType(4).build());
                } else {
                    vibrator.vibrate(mAlarmVibratePattern, 0);
                }
            }
        }
    }

    public static void startVibrateWithRingtoneUri(Context context, Uri uri, Vibrator vibrator) {
        vibrator.cancel();
        int sceneId = RingtoneManagerUtils.getVibratorSceneId(context, uri);
        int vibrateTime = vibrator.setVibratorEffect(sceneId);
        long[] pattern = new long[]{(long) getVibrateLevel(System.getInt(context.getContentResolver(), "incoming_call_vibrate_intensity", 1)), 0, (long) vibrateTime};
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OPVibrateUtils--sceneId:");
        stringBuilder.append(sceneId);
        stringBuilder.append(" ringtoneUri:");
        stringBuilder.append(uri);
        stringBuilder.append(" vibrateTime:");
        stringBuilder.append(vibrateTime);
        stringBuilder.append(" delayTime:");
        stringBuilder.append(0);
        stringBuilder.append(" vibrateLevel:");
        stringBuilder.append(getVibrateLevel(vibrateLevel));
        Log.d(str, stringBuilder.toString());
        vibrator.vibrate(pattern, 0);
    }

    private static int getVibrateLevel(int levelValue) {
        switch (levelValue) {
            case 0:
                return -1;
            case 1:
                return -2;
            case 2:
                return -3;
            default:
                return -2;
        }
    }

    public static int getRealXVibrateIndexToValue(int value) {
        switch (value) {
            case 0:
                return 5;
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            default:
                return 5;
        }
    }

    public static int getRealXVibrateValueToIndex(int value) {
        switch (value) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 0;
            default:
                return 0;
        }
    }
}
