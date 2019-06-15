package com.android.settings.notification;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.UserHandle;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.utils.VoiceSettingsActivity;
import java.util.Locale;

public class ZenModeVoiceActivity extends VoiceSettingsActivity {
    private static final int MINUTES_MS = 60000;
    private static final String TAG = "ZenModeVoiceActivity";

    /* Access modifiers changed, original: protected */
    public boolean onVoiceSettingInteraction(Intent intent) {
        if (intent.hasExtra("android.settings.extra.do_not_disturb_mode_enabled")) {
            int minutes = intent.getIntExtra("android.settings.extra.do_not_disturb_mode_minutes", -1);
            Condition condition = null;
            int mode = 0;
            if (intent.getBooleanExtra("android.settings.extra.do_not_disturb_mode_enabled", false)) {
                if (minutes > 0) {
                    condition = ZenModeConfig.toTimeCondition(this, minutes, UserHandle.myUserId());
                }
                mode = 3;
            }
            setZenModeConfig(mode, condition);
            AudioManager audioManager = (AudioManager) getSystemService("audio");
            if (audioManager != null) {
                audioManager.adjustStreamVolume(5, 0, 1);
            }
            notifySuccess(getChangeSummary(mode, minutes));
        } else {
            Log.v(TAG, "Missing extra android.provider.Settings.EXTRA_DO_NOT_DISTURB_MODE_ENABLED");
            finish();
        }
        return false;
    }

    private void setZenModeConfig(int mode, Condition condition) {
        if (condition != null) {
            NotificationManager.from(this).setZenMode(mode, condition.id, TAG);
        } else {
            NotificationManager.from(this).setZenMode(mode, null, TAG);
        }
    }

    private CharSequence getChangeSummary(int mode, int minutes) {
        int i = mode;
        int i2 = minutes;
        int indefinite = -1;
        int byMinute = -1;
        int byHour = -1;
        int byTime = -1;
        if (i == 0) {
            indefinite = R.string.zen_mode_summary_always;
        } else if (i == 3) {
            indefinite = R.string.zen_mode_summary_alarms_only_indefinite;
            byMinute = R.plurals.zen_mode_summary_alarms_only_by_minute;
            byHour = R.plurals.zen_mode_summary_alarms_only_by_hour;
            byTime = R.string.zen_mode_summary_alarms_only_by_time;
        }
        if (i2 < 0 || i == 0) {
            return getString(indefinite);
        }
        CharSequence formattedTime = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(this, UserHandle.myUserId()) ? "Hm" : "hma"), System.currentTimeMillis() + ((long) (60000 * i2)));
        Resources res = getResources();
        if (i2 < 60) {
            return res.getQuantityString(byMinute, i2, new Object[]{Integer.valueOf(minutes), formattedTime});
        } else if (i2 % 60 != 0) {
            return res.getString(byTime, new Object[]{formattedTime});
        } else {
            return res.getQuantityString(byHour, i2 / 60, new Object[]{Integer.valueOf(i2 / 60), formattedTime});
        }
    }
}
