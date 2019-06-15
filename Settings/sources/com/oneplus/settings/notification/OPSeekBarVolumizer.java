package com.oneplus.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.VolumePreference.VolumeStore;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.three_key.ThreeKeyManager;

public class OPSeekBarVolumizer implements OnSeekBarChangeListener, android.os.Handler.Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String OEM_HEADSET_PLUG_ACTION = "android.intent.action.HEADSET_PLUG";
    private static final String TAG = "OPSeekBarVolumizer";
    private static final int THREE_KEY_SILENT_VALUE = 1;
    private static final int THREE_KEY_VIBRATE_VALUE = 2;
    private boolean mAffectedByRingerMode;
    private boolean mAllowAlarms;
    private boolean mAllowMedia;
    private final AudioManager mAudioManager;
    private final Callback mCallback;
    private final Context mContext;
    private final Uri mDefaultUri;
    private Handler mHandler;
    private int mLastAudibleStreamVolume;
    private int mLastProgress = -1;
    private final int mMaxStreamVolume;
    private boolean mMuted;
    private final NotificationManager mNotificationManager;
    private boolean mNotificationOrRing;
    private Policy mNotificationPolicy;
    private int mOriginalStreamVolume;
    private final Receiver mReceiver = new Receiver();
    private int mRingerMode;
    private Ringtone mRingtone;
    private SeekBar mSeekBar;
    private final int mStreamType;
    private final H mUiHandler = new H();
    private int mVolumeBeforeMute = -1;
    private Observer mVolumeObserver;
    private int mZenMode;

    public interface Callback {
        void onMuted(boolean z, boolean z2);

        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onSampleStarting(OPSeekBarVolumizer oPSeekBarVolumizer);
    }

    private final class H extends Handler {
        private static final int UPDATE_SLIDER = 1;

        private H() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1 && OPSeekBarVolumizer.this.mSeekBar != null) {
                OPSeekBarVolumizer.this.mLastProgress = msg.arg1;
                OPSeekBarVolumizer.this.mLastAudibleStreamVolume = Math.abs(msg.arg2);
                if (msg.arg2 >= 0) {
                    z = false;
                }
                boolean muted = z;
                if (muted != OPSeekBarVolumizer.this.mMuted) {
                    OPSeekBarVolumizer.this.mMuted = muted;
                    if (OPSeekBarVolumizer.this.mCallback != null) {
                        OPSeekBarVolumizer.this.mCallback.onMuted(OPSeekBarVolumizer.this.mMuted, OPSeekBarVolumizer.this.isZenMuted());
                    }
                }
                OPSeekBarVolumizer.this.updateSeekBar();
            }
        }

        public void postUpdateSlider(int volume, int lastAudibleVolume, boolean mute) {
            obtainMessage(1, volume, (mute ? -1 : 1) * lastAudibleVolume).sendToTarget();
        }
    }

    private final class Observer extends ContentObserver {
        public Observer(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            OPSeekBarVolumizer.this.updateSlider();
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mListening;

        private Receiver() {
        }

        public void setListening(boolean listening) {
            if (this.mListening != listening) {
                this.mListening = listening;
                if (listening) {
                    IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
                    filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                    filter.addAction(OPSeekBarVolumizer.OEM_HEADSET_PLUG_ACTION);
                    filter.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED");
                    filter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
                    OPSeekBarVolumizer.this.mContext.registerReceiver(this, filter);
                } else {
                    OPSeekBarVolumizer.this.mContext.unregisterReceiver(this);
                }
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int streamType;
            int streamValue;
            if ("android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                streamValue = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                if (OPSeekBarVolumizer.this.mStreamType != 3 || !OPSeekBarVolumizer.this.mAudioManager.isWiredHeadsetOn() || OPSeekBarVolumizer.this.mAudioManager.isMusicActive()) {
                    updateVolumeSlider(streamType, streamValue);
                }
            } else if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
                if (OPSeekBarVolumizer.this.mNotificationOrRing) {
                    OPSeekBarVolumizer.this.mRingerMode = OPSeekBarVolumizer.this.mAudioManager.getRingerModeInternal();
                }
                if (!(!OPSeekBarVolumizer.this.mAffectedByRingerMode || OPSeekBarVolumizer.this.mAudioManager.isWiredHeadsetOn() || OPSeekBarVolumizer.this.mAudioManager.isMusicActive())) {
                    OPSeekBarVolumizer.this.updateSlider();
                }
            } else if ("android.media.STREAM_DEVICES_CHANGED_ACTION".equals(action)) {
                streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                streamValue = OPSeekBarVolumizer.this.mAudioManager.getStreamVolume(streamType);
                if (((OPSeekBarVolumizer.this.mStreamType != 4 && OPSeekBarVolumizer.this.mStreamType != 3) || !OPSeekBarVolumizer.this.mAudioManager.isWiredHeadsetOn() || OPSeekBarVolumizer.this.mAudioManager.isMusicActive()) && !OPSeekBarVolumizer.isNotificationOrRing(streamType)) {
                    updateVolumeSlider(streamType, streamValue);
                }
            } else if ("android.app.action.INTERRUPTION_FILTER_CHANGED".equals(action)) {
                OPSeekBarVolumizer.this.mZenMode = OPSeekBarVolumizer.this.mNotificationManager.getZenMode();
                OPSeekBarVolumizer.this.updateSlider();
            } else if (OPSeekBarVolumizer.OEM_HEADSET_PLUG_ACTION.equals(action)) {
                if (!(OPSeekBarVolumizer.this.mSeekBar == null || OPSeekBarVolumizer.this.mAudioManager == null)) {
                    String str = OPSeekBarVolumizer.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("VOLUME_CHANGED_ACTION mStreamType : ");
                    stringBuilder.append(OPSeekBarVolumizer.this.mStreamType);
                    Log.w(str, stringBuilder.toString());
                    if (OPSeekBarVolumizer.this.mStreamType == 3) {
                        streamType = OPSeekBarVolumizer.this.mAudioManager.getStreamVolume(OPSeekBarVolumizer.this.mStreamType);
                        boolean isStreamMute = OPSeekBarVolumizer.this.mAudioManager.isStreamMute(OPSeekBarVolumizer.this.mStreamType);
                        String str2 = OPSeekBarVolumizer.TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("volume = ");
                        stringBuilder2.append(streamType);
                        Log.w(str2, stringBuilder2.toString());
                        OPSeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamType, OPSeekBarVolumizer.this.mLastAudibleStreamVolume, false);
                    }
                }
            } else if ("android.app.action.NOTIFICATION_POLICY_CHANGED".equals(action)) {
                OPSeekBarVolumizer.this.mNotificationPolicy = OPSeekBarVolumizer.this.mNotificationManager.getNotificationPolicy();
                boolean z = false;
                OPSeekBarVolumizer.this.mAllowAlarms = (OPSeekBarVolumizer.this.mNotificationPolicy.priorityCategories & 32) != 0;
                OPSeekBarVolumizer oPSeekBarVolumizer = OPSeekBarVolumizer.this;
                if ((OPSeekBarVolumizer.this.mNotificationPolicy.priorityCategories & 64) != 0) {
                    z = true;
                }
                oPSeekBarVolumizer.mAllowMedia = z;
                OPSeekBarVolumizer.this.updateSlider();
            }
        }

        private void updateVolumeSlider(int streamType, int streamValue) {
            boolean muted = false;
            boolean streamMatch = OPSeekBarVolumizer.this.mNotificationOrRing ? OPSeekBarVolumizer.isNotificationOrRing(streamType) : streamType == OPSeekBarVolumizer.this.mStreamType;
            if (OPSeekBarVolumizer.this.mSeekBar != null && streamMatch && streamValue != -1) {
                if (OPSeekBarVolumizer.this.mAudioManager.isStreamMute(OPSeekBarVolumizer.this.mStreamType) || streamValue == 0) {
                    muted = true;
                }
                OPSeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamValue, OPSeekBarVolumizer.this.mLastAudibleStreamVolume, muted);
            }
        }
    }

    public OPSeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mNotificationPolicy = this.mNotificationManager.getNotificationPolicy();
        boolean z = false;
        this.mAllowAlarms = (this.mNotificationPolicy.priorityCategories & 32) != 0;
        if ((this.mNotificationPolicy.priorityCategories & 64) != 0) {
            z = true;
        }
        this.mAllowMedia = z;
        this.mStreamType = streamType;
        this.mAffectedByRingerMode = this.mAudioManager.isStreamAffectedByRingerMode(this.mStreamType);
        this.mNotificationOrRing = isNotificationOrRing(this.mStreamType);
        if (this.mNotificationOrRing) {
            this.mRingerMode = this.mAudioManager.getRingerModeInternal();
        }
        this.mZenMode = this.mNotificationManager.getZenMode();
        this.mMaxStreamVolume = this.mAudioManager.getStreamMaxVolume(this.mStreamType);
        this.mCallback = callback;
        this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
        this.mLastAudibleStreamVolume = this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType);
        this.mMuted = this.mAudioManager.isStreamMute(this.mStreamType);
        if (this.mCallback != null) {
            this.mCallback.onMuted(this.mMuted, isZenMuted());
        }
        if (defaultUri == null) {
            if (this.mStreamType == 2) {
                defaultUri = System.DEFAULT_RINGTONE_URI;
            } else if (this.mStreamType == 5) {
                defaultUri = System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
    }

    private static boolean isNotificationOrRing(int stream) {
        return stream == 2 || stream == 5;
    }

    private static boolean isAlarmsStream(int stream) {
        return stream == 4;
    }

    private static boolean isMediaStream(int stream) {
        return stream == 3;
    }

    public boolean isZenModeEnabled(int zenMode) {
        switch (zenMode) {
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    public void setSeekBar(SeekBar seekBar) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = seekBar;
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mSeekBar.setMax(this.mMaxStreamVolume);
        updateSeekBar();
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    public boolean isZenMuted() {
        boolean z = false;
        if (OPUtils.isSupportSocTriState()) {
            if (getThreeKeyStatus(this.mContext) == 1 || getThreeKeyStatus(this.mContext) == 2) {
                z = true;
            }
            return z;
        }
        if (getThreeKeyStatus(this.mContext) == 1) {
            z = true;
        }
        return z;
    }

    public int getThreeKeyStatus(Context context) {
        int threeKeyStatus = 0;
        if (context == null) {
            Log.e(TAG, "getThreeKeyStatus error, context is null");
            return 0;
        }
        ThreeKeyManager threeKeyManager = (ThreeKeyManager) context.getSystemService("threekey");
        if (threeKeyManager != null) {
            try {
                threeKeyStatus = threeKeyManager.getThreeKeyStatus();
            } catch (Exception e) {
                Log.e(TAG, "Exception occurs, Three Key Service may not ready", e);
                return 0;
            }
        }
        return threeKeyStatus;
    }

    /* Access modifiers changed, original: protected */
    public void updateSeekBar() {
        boolean zenMuted = isZenMuted();
        if (this.mNotificationOrRing) {
            this.mSeekBar.setEnabled(zenMuted ^ 1);
        }
        if (isAlarmsStream(this.mStreamType)) {
            if (!isZenModeEnabled(this.mZenMode) || this.mAllowAlarms) {
                this.mSeekBar.setEnabled(true);
            } else {
                this.mSeekBar.setEnabled(false);
            }
        }
        if (isMediaStream(this.mStreamType)) {
            if (!isZenModeEnabled(this.mZenMode) || this.mAllowMedia) {
                this.mSeekBar.setEnabled(true);
            } else {
                this.mSeekBar.setEnabled(false);
            }
        }
        if (this.mNotificationOrRing && this.mRingerMode == 1) {
            this.mSeekBar.setProgress(0);
        } else if (this.mMuted) {
            this.mSeekBar.setProgress(0);
        } else {
            this.mSeekBar.setProgress(this.mLastProgress > -1 ? this.mLastProgress : this.mOriginalStreamVolume);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (this.mMuted && this.mLastProgress > 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, 100, 0);
                } else if (!this.mMuted && this.mLastProgress == 0) {
                    this.mAudioManager.adjustStreamVolume(this.mStreamType, -100, 0);
                }
                this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 1024);
                break;
            case 1:
                onStartSample();
                break;
            case 2:
                onStopSample();
                break;
            case 3:
                onInitSample();
                break;
            default:
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("invalid SeekBarVolumizer message: ");
                stringBuilder.append(msg.what);
                Log.e(str, stringBuilder.toString());
                break;
        }
        return true;
    }

    private void onInitSample() {
        this.mRingtone = RingtoneManager.getRingtone(this.mContext, this.mDefaultUri);
        if (this.mRingtone != null) {
            try {
                this.mRingtone.setStreamType(this.mStreamType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void postStartSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), isSamplePlaying() ? 1000 : 0);
        }
    }

    private void onStartSample() {
        if (!isSamplePlaying()) {
            if (this.mCallback != null) {
                this.mCallback.onSampleStarting(this);
            }
            if (this.mRingtone != null) {
                try {
                    this.mRingtone.setAudioAttributes(new Builder(this.mRingtone.getAudioAttributes()).setFlags(128).build());
                    this.mRingtone.play();
                } catch (Throwable e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error playing ringtone, stream ");
                    stringBuilder.append(this.mStreamType);
                    Log.w(str, stringBuilder.toString(), e);
                }
            }
        }
    }

    private void postStopSample() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        }
    }

    private void onStopSample() {
        try {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (this.mHandler != null) {
            postStopSample();
            this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
            this.mReceiver.setListening(false);
            this.mSeekBar.setOnSeekBarChangeListener(null);
            this.mHandler.getLooper().quitSafely();
            this.mHandler = null;
            this.mVolumeObserver = null;
        }
    }

    public void start() {
        if (this.mHandler == null) {
            HandlerThread thread = new HandlerThread("OPSeekBarVolumizer.CallbackHandler");
            thread.start();
            this.mHandler = new Handler(thread.getLooper(), this);
            this.mHandler.sendEmptyMessage(3);
            this.mVolumeObserver = new Observer(this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("three_Key_mode"), false, this.mVolumeObserver);
            this.mReceiver.setListening(true);
        }
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (!this.mMuted && this.mNotificationOrRing && progress < 1) {
            progress = 1;
            seekBar.setProgress(1);
        }
        if (this.mCallback != null) {
            this.mCallback.onProgressChanged(seekBar, progress, fromTouch);
        }
    }

    private void postSetVolume(int progress) {
        if (this.mHandler != null) {
            this.mLastProgress = progress;
            this.mHandler.removeMessages(0);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        postSetVolume(seekBar.getProgress());
        postStartSample();
    }

    public boolean isSamplePlaying() {
        boolean z = true;
        try {
            if (this.mRingtone == null || !this.mRingtone.isPlaying()) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public void startSample() {
        postStartSample();
    }

    public void stopSample() {
        postStopSample();
    }

    public SeekBar getSeekBar() {
        return this.mSeekBar;
    }

    public void changeVolumeBy(int amount) {
        this.mSeekBar.incrementProgressBy(amount);
        postSetVolume(this.mSeekBar.getProgress());
        postStartSample();
        this.mVolumeBeforeMute = -1;
    }

    public void muteVolume() {
        if (this.mVolumeBeforeMute != -1) {
            this.mSeekBar.setProgress(this.mVolumeBeforeMute);
            postSetVolume(this.mVolumeBeforeMute);
            postStartSample();
            this.mVolumeBeforeMute = -1;
            return;
        }
        this.mVolumeBeforeMute = this.mSeekBar.getProgress();
        this.mSeekBar.setProgress(0);
        postStopSample();
        postSetVolume(0);
    }

    public void onSaveInstanceState(VolumeStore volumeStore) {
        if (this.mLastProgress >= 0) {
            volumeStore.volume = this.mLastProgress;
            volumeStore.originalVolume = this.mOriginalStreamVolume;
        }
    }

    public void onRestoreInstanceState(VolumeStore volumeStore) {
        if (volumeStore.volume != -1) {
            this.mOriginalStreamVolume = volumeStore.originalVolume;
            this.mLastProgress = volumeStore.volume;
            postSetVolume(this.mLastProgress);
        }
    }

    private void updateSlider() {
        if (this.mSeekBar != null && this.mAudioManager != null) {
            int volume = this.mAudioManager.getStreamVolume(this.mStreamType);
            int lastAudibleVolume = this.mAudioManager.getLastAudibleStreamVolume(this.mStreamType);
            boolean mute = this.mAudioManager.isStreamMute(this.mStreamType);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" volume = ");
            stringBuilder.append(volume);
            stringBuilder.append(" lastAudibleVolume = ");
            stringBuilder.append(lastAudibleVolume);
            stringBuilder.append(" mute = ");
            stringBuilder.append(mute);
            Log.i(str, stringBuilder.toString());
            this.mUiHandler.postUpdateSlider(volume, lastAudibleVolume, mute);
        }
    }
}
