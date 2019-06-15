package com.android.settings.notification;

import android.app.NotificationManager;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import java.util.Objects;

public class RingVolumePreferenceController extends VolumeSeekBarPreferenceController {
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String TAG = "RingVolumeController";
    private final H mHandler;
    private int mMuteIcon;
    private final RingReceiver mReceiver;
    private int mRingerMode;
    private ComponentName mSuppressor;
    private Vibrator mVibrator;

    private final class H extends Handler {
        private static final int UPDATE_EFFECTS_SUPPRESSOR = 1;
        private static final int UPDATE_RINGER_MODE = 2;

        private H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RingVolumePreferenceController.this.updateEffectsSuppressor();
                    return;
                case 2:
                    RingVolumePreferenceController.this.updateRingerMode();
                    return;
                default:
                    return;
            }
        }
    }

    private class RingReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        private RingReceiver() {
        }

        public void register(boolean register) {
            if (this.mRegistered != register) {
                if (register) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                    filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                    RingVolumePreferenceController.this.mContext.registerReceiver(this, filter);
                } else {
                    RingVolumePreferenceController.this.mContext.unregisterReceiver(this);
                }
                this.mRegistered = register;
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(action)) {
                RingVolumePreferenceController.this.mHandler.sendEmptyMessage(1);
            } else if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
                RingVolumePreferenceController.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    public RingVolumePreferenceController(Context context) {
        this(context, KEY_RING_VOLUME);
    }

    public RingVolumePreferenceController(Context context, String key) {
        super(context, key);
        this.mRingerMode = -1;
        this.mReceiver = new RingReceiver();
        this.mHandler = new H();
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (!(this.mVibrator == null || this.mVibrator.hasVibrator())) {
            this.mVibrator = null;
        }
        updateRingerMode();
    }

    @OnLifecycleEvent(Event.ON_RESUME)
    public void onResume() {
        super.onResume();
        this.mReceiver.register(true);
        updateEffectsSuppressor();
        updatePreferenceIcon();
    }

    @OnLifecycleEvent(Event.ON_PAUSE)
    public void onPause() {
        super.onPause();
        this.mReceiver.register(false);
    }

    public String getPreferenceKey() {
        return KEY_RING_VOLUME;
    }

    public int getAvailabilityStatus() {
        return (!Utils.isVoiceCapable(this.mContext) || this.mHelper.isSingleVolume()) ? 2 : 0;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_RING_VOLUME);
    }

    public int getAudioStream() {
        return 2;
    }

    public int getMuteIcon() {
        return this.mMuteIcon;
    }

    private void updateRingerMode() {
        int ringerMode = this.mHelper.getRingerModeInternal();
        if (this.mRingerMode != ringerMode) {
            this.mRingerMode = ringerMode;
            updatePreferenceIcon();
        }
    }

    private void updateEffectsSuppressor() {
        ComponentName suppressor = NotificationManager.from(this.mContext).getEffectsSuppressor();
        if (!Objects.equals(suppressor, this.mSuppressor)) {
            this.mSuppressor = suppressor;
            if (this.mPreference != null) {
                this.mPreference.setSuppressionText(SuppressorHelper.getSuppressionText(this.mContext, suppressor));
            }
            updatePreferenceIcon();
        }
    }

    private void updatePreferenceIcon() {
        if (this.mPreference == null) {
            return;
        }
        if (this.mRingerMode == 1) {
            this.mMuteIcon = R.drawable.ic_volume_ringer_vibrate;
            this.mPreference.showIcon(R.drawable.ic_volume_ringer_vibrate);
        } else if (this.mRingerMode == 0) {
            this.mMuteIcon = R.drawable.ic_notifications_off_24dp;
            this.mPreference.showIcon(R.drawable.ic_notifications_off_24dp);
        } else {
            this.mPreference.showIcon(R.drawable.ic_notifications);
        }
    }
}
