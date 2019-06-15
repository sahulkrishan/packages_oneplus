package com.android.settings.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.TwoStatePreference;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.notification.VolumeSeekBarPreference.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.notification.OPSeekBarVolumizer;
import com.oneplus.settings.notification.SoundVolumePreferenceCategoryController;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import com.oneplus.settings.ui.OPListDialog;
import com.oneplus.settings.ui.OPListDialog.OnDialogListItemClickListener;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.three_key.ThreeKeyManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SoundSettings extends DashboardFragment implements OnDialogListItemClickListener, OnPreferenceChangeListener {
    private static final int DEFAULT_ON = 0;
    private static final String KEY_ALARM_RINGTONE = "alarm_ringtone";
    private static final String KEY_ALARM_VOLUME = "alarm_volume";
    private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";
    private static final String KEY_CHARGING_SOUNDS = "charging_sounds";
    private static final String KEY_DIAL_PAD_TONES = "dial_pad_tones";
    private static final String KEY_DO_NOT_DISTURB_SETTINGS = "do_not_disturb_settings";
    private static final String KEY_EARPHONE = "earphone";
    private static final String KEY_EARPHONE_MODE = "earphone_mode";
    private static final String KEY_INCOMING_CALL_VIBRATE = "incoming_call_vibrate_mode";
    private static final String KEY_MEDIA_VOLUME = "media_volume";
    private static final String KEY_MMS_RINGTONE = "message_ringtone";
    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    private static final String KEY_NOTIFICATION_VOLUME = "notification_volume";
    private static final String KEY_OP_SOUND_DIRECT = "op_sound_direct";
    private static final String KEY_PHONE_RINGTONE = "ringtone";
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String KEY_SCREENSHOT_SOUNDS = "screenshot_sounds";
    private static final String KEY_SCREEN_LOCKING_SOUNDS = "screen_locking_sounds";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SOUND_DIRECT = "sound_direct";
    private static final String KEY_SYSTEM = "other_sounds_and_vibrations_category";
    private static final String KEY_TOUCH_SOUNDS = "touch_sounds";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_VIBRATE_INTENSITY = "vibrate_intensity";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final String KEY_VIBRATE_ON_TOUCH_FOR_VIBRATE = "vibrate_on_touch_for_vibrate";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    private static final String KEY_VIBRATE_WHEN_RINGING_FOR_VIBRATE = "vibrate_when_ringing_for_vibrate";
    private static final String KEY_VOLUME_KEYS_ADJUST = "volume_keys_adjust";
    private static final String KEY_VOLUME_KEYS_ADJUST_VALUE = "volume_keys_adjust_value";
    private static final String KEY_ZEN_MODE = "zen_mode";
    private static final SettingPref[] PREFS = new SettingPref[]{PREF_DIAL_PAD_TONES, PREF_SCREEN_LOCKING_SOUNDS, PREF_SCREENSHOT_SOUNDS, PREF_CHARGING_SOUNDS, PREF_TOUCH_SOUNDS, PREF_VIBRATE_ON_TOUCH, PREF_VIBRATE_ON_TOUCH_FOR_VIBRATE};
    private static final SettingPref PREF_CHARGING_SOUNDS = new SettingPref(1, KEY_CHARGING_SOUNDS, "charging_sounds_enabled", 0, new int[0]);
    private static final SettingPref PREF_DIAL_PAD_TONES = new SettingPref(2, KEY_DIAL_PAD_TONES, "dtmf_tone", 0, new int[0]) {
        public boolean isApplicable(Context context) {
            return Utils.isVoiceCapable(context);
        }
    };
    private static final SettingPref PREF_SCREENSHOT_SOUNDS = new SettingPref(2, KEY_SCREENSHOT_SOUNDS, "oem_screenshot_sound_enable", 0, new int[0]);
    private static final SettingPref PREF_SCREEN_LOCKING_SOUNDS = new SettingPref(2, KEY_SCREEN_LOCKING_SOUNDS, "lockscreen_sounds_enabled", 0, new int[0]);
    private static final SettingPref PREF_TOUCH_SOUNDS = new SettingPref(2, KEY_TOUCH_SOUNDS, "sound_effects_enabled", 0, new int[0]) {
        /* Access modifiers changed, original: protected */
        public boolean setSetting(Context context, int value) {
            return super.setSetting(context, value);
        }
    };
    private static final SettingPref PREF_VIBRATE_ON_TOUCH = new SettingPref(2, KEY_VIBRATE_ON_TOUCH, "haptic_feedback_enabled", 0, new int[0]) {
        public boolean isApplicable(Context context) {
            return SoundSettings.hasHaptic(context);
        }
    };
    private static final SettingPref PREF_VIBRATE_ON_TOUCH_FOR_VIBRATE = new SettingPref(2, KEY_VIBRATE_ON_TOUCH_FOR_VIBRATE, "haptic_feedback_enabled", 0, new int[0]) {
        public boolean isApplicable(Context context) {
            return SoundSettings.hasHaptic(context);
        }
    };
    private static final int REQUEST_CODE = 200;
    @VisibleForTesting
    private static final String[] RESTRICTED_KEYS = new String[]{KEY_MEDIA_VOLUME, KEY_ALARM_VOLUME, KEY_RING_VOLUME, KEY_NOTIFICATION_VOLUME, KEY_ZEN_MODE};
    private static final int SAMPLE_CUTOFF = 2000;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.sound_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return SoundSettings.buildPreferenceControllers(context, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            if (Utils.isVoiceCapable(context)) {
                keys.add(SoundSettings.KEY_NOTIFICATION_VOLUME);
            } else {
                keys.add(SoundSettings.KEY_RING_VOLUME);
                keys.add(SoundSettings.KEY_PHONE_RINGTONE);
                keys.add(SoundSettings.KEY_VIBRATE_WHEN_RINGING);
            }
            PackageManager pm = context.getPackageManager();
            context.getSystemService("user");
            if (context.getResources().getBoolean(17956915)) {
                try {
                    if (pm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver") == 2) {
                    }
                } catch (IllegalArgumentException e) {
                }
            }
            if (!OPUtils.isAppPakExist(context, "com.oneplus.dirac.simplemanager")) {
                keys.add(SoundSettings.KEY_SOUND_DIRECT);
            }
            SoundSettings.isSupportLinearMotor = context.getPackageManager().hasSystemFeature("oem.linear.motor.support");
            if (OPUtils.isGuestMode() || !SoundSettings.isSupportLinearMotor) {
                keys.add(SoundSettings.KEY_VIBRATE);
                keys.add(SoundSettings.KEY_VIBRATE_WHEN_RINGING_FOR_VIBRATE);
                keys.add(SoundSettings.KEY_INCOMING_CALL_VIBRATE);
                keys.add(SoundSettings.KEY_VIBRATE_ON_TOUCH_FOR_VIBRATE);
                keys.add(SoundSettings.KEY_VIBRATE_INTENSITY);
            }
            if (!keys.contains(SoundSettings.KEY_VIBRATE_INTENSITY)) {
                if (!OpFeatures.isSupport(new int[]{40})) {
                    keys.add(SoundSettings.KEY_VIBRATE_INTENSITY);
                }
            }
            if (!OPUtils.isSupportSocTriState()) {
                keys.add(SoundSettings.KEY_DO_NOT_DISTURB_SETTINGS);
            }
            keys.add(SoundSettings.KEY_VOLUME_KEYS_ADJUST);
            if (!OPUtils.isOpBluetoothHeadset()) {
                keys.add(SoundSettings.KEY_EARPHONE);
                keys.add(SoundSettings.KEY_EARPHONE_MODE);
            }
            if (!OPUtils.isSupportEarphoneMode()) {
                keys.add(SoundSettings.KEY_EARPHONE_MODE);
            }
            if (Utils.getManagedProfile(UserManager.get(context)) == null) {
                keys.add(WorkSoundPreferenceController.KEY_WORK_ALARM_RINGTONE);
                keys.add(WorkSoundPreferenceController.KEY_WORK_NOTIFICATION_RINGTONE);
                keys.add(WorkSoundPreferenceController.KEY_WORK_PHONE_RINGTONE);
                keys.add(WorkSoundPreferenceController.KEY_WORK_USE_PERSONAL_SOUNDS);
            }
            return keys;
        }
    };
    private static final String SELECTED_PREFERENCE_KEY = "selected_preference";
    @VisibleForTesting
    static final int STOP_SAMPLE = 3;
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "SoundSettings";
    private static final int THREE_KEY_SILENT_VALUE = 1;
    public static final int TYPE_MMS_NOTIFICATION = 8;
    private static boolean isSupportAptxHdSupport;
    private static boolean isSupportLinearMotor;
    private Preference mAlarmRingtonePreference;
    private AudioManager mAudioManager;
    private Context mContext;
    private PreferenceCategory mEarPhoneCategory;
    private Preference mEarphoneModePreference;
    private final H mHandler = new H(this, null);
    private Preference mIncomingCallVibrateModePreference;
    private final Runnable mLookupRingtoneNames = new Runnable() {
        public void run() {
            CharSequence summary;
            if (SoundSettings.this.mPhoneRingtonePreference != null) {
                summary = SoundSettings.this.updateRingtoneName(SoundSettings.this.mContext, 1);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(1, summary).sendToTarget();
                }
            }
            if (SoundSettings.this.mNotificationRingtonePreference != null) {
                summary = SoundSettings.this.updateRingtoneName(SoundSettings.this.mContext, 2);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(2, summary).sendToTarget();
                }
            }
            if (SoundSettings.this.mAlarmRingtonePreference != null) {
                summary = SoundSettings.this.updateRingtoneName(SoundSettings.this.mContext, 4);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(6, summary).sendToTarget();
                }
            }
            if (SoundSettings.this.mSmsRingtonePreference != null) {
                summary = SoundSettings.this.updateRingtoneName(SoundSettings.this.mContext, 8);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(7, summary).sendToTarget();
                }
            }
        }
    };
    private VolumeSeekBarPreference mMediaVolumePreference;
    private Preference mNotificationRingtonePreference;
    private OPListDialog mOPListDialog;
    private Preference mOPSoundDirectPreference;
    private Preference mPhoneRingtonePreference;
    private PackageManager mPm;
    private final Receiver mReceiver = new Receiver(this, null);
    private RingtonePreference mRequestPreference;
    private VolumeSeekBarPreference mRingOrNotificationPreference;
    private int mRingerMode = -1;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                SoundSettings.this.lookupRingtoneNames();
            }
        }
    };
    private Preference mSmsRingtonePreference;
    private ComponentName mSuppressor;
    private TelephonyManager mTelephonyManager;
    private UserManager mUserManager;
    private TwoStatePreference mVibrateWhenRinging;
    private TwoStatePreference mVibrateWhenRingingForVibrate;
    private Preference mVibrateWhenRingingPreference;
    private final Runnable mVibrateWhenRingingRunnable = new -$$Lambda$SoundSettings$3WFktXqTAuUwKBOCajSDKQ9QhQc(this);
    private int mVibrateWhenRingingValue = 0;
    private Vibrator mVibrator;
    private boolean mVoiceCapable;
    @VisibleForTesting
    final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    private ListPreference mVolumeKeysAdjust;
    private final ArrayList<VolumeSeekBarPreference> mVolumePrefs = new ArrayList();
    private long[][] sVibratePatternrhythm = new long[][]{new long[]{-2, 0, 1000, 1000, 1000}, new long[]{-2, 0, 500, 250, 10, 1000, 500, 250, 10}, new long[]{-2, 0, 300, 400, 300, 400, 300, 1000, 300, 400, 300, 400, 300}, new long[]{-2, 0, 30, 80, 30, 80, 50, 180, 600, 1000, 30, 80, 30, 80, 50, 180, 600}, new long[]{-2, 0, 80, 200, 600, 150, 10, 1000, 80, 200, 600, 150, 10}};

    private final class H extends Handler {
        private static final int STOP_SAMPLE = 3;
        private static final int UPDATE_ALARM_RINGTONE = 6;
        private static final int UPDATE_EFFECTS_SUPPRESSOR = 4;
        private static final int UPDATE_NOTIFICATION_RINGTONE = 2;
        private static final int UPDATE_PHONE_RINGTONE = 1;
        private static final int UPDATE_RINGER_MODE = 5;
        private static final int UPDATE_SMS_RINGTONE = 7;

        /* synthetic */ H(SoundSettings x0, AnonymousClass1 x1) {
            this();
        }

        private H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SoundSettings.this.mPhoneRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                case 2:
                    SoundSettings.this.mNotificationRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                case 3:
                    SoundSettings.this.mVolumeCallback.stopSample();
                    return;
                case 4:
                    SoundSettings.this.updateEffectsSuppressor();
                    return;
                case 5:
                    SoundSettings.this.updateRingerMode();
                    return;
                case 6:
                    SoundSettings.this.mAlarmRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                case 7:
                    SoundSettings.this.mSmsRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        /* synthetic */ Receiver(SoundSettings x0, AnonymousClass1 x1) {
            this();
        }

        public void register(boolean register) {
            if (this.mRegistered != register) {
                if (register) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                    filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                    SoundSettings.this.mContext.registerReceiver(this, filter);
                } else {
                    SoundSettings.this.mContext.unregisterReceiver(this);
                }
                this.mRegistered = register;
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(action)) {
                SoundSettings.this.mHandler.sendEmptyMessage(4);
            } else if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
                SoundSettings.this.mHandler.sendEmptyMessage(5);
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri THREE_KEY_MODE_URI = Global.getUriFor("three_Key_mode");
        private final Uri VIBRATE_WHEN_RINGING_URI = System.getUriFor(SoundSettings.KEY_VIBRATE_WHEN_RINGING);

        public SettingsObserver() {
            super(SoundSettings.this.mHandler);
        }

        public void register(boolean register) {
            ContentResolver cr = SoundSettings.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.VIBRATE_WHEN_RINGING_URI, false, this);
                cr.registerContentObserver(this.THREE_KEY_MODE_URI, false, this);
                return;
            }
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                SoundSettings.this.updateVibrateWhenRinging();
                SoundSettings.this.updateVibrateWhenRingingForVibrate();
            } else if (this.THREE_KEY_MODE_URI.equals(uri)) {
                Log.d(SoundSettings.TAG, "three key mode change");
                SoundSettings.this.updateRingOrNotificationPreference();
            }
        }
    }

    private static class SummaryProvider extends BroadcastReceiver implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            String summary;
            String earphoneModeSummary;
            if (OPUtils.isGuestMode()) {
                summary = this.mContext.getString(R.string.op_dnd_summary);
                earphoneModeSummary = this.mContext.getString(R.string.oneplus_earphone_mode).toLowerCase();
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, earphoneModeSummary}));
            } else if (!listening) {
            } else {
                if (OPUtils.isSupportSocTriState()) {
                    summary = this.mContext.getString(R.string.op_dnd_summary);
                    earphoneModeSummary = this.mContext.getString(R.string.oneplus_earphone_mode).toLowerCase();
                    summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, earphoneModeSummary});
                    String vibrateSummary = this.mContext.getString(R.string.oneplus_vibrate).toLowerCase();
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, vibrateSummary}));
                    return;
                }
                summary = this.mContext.getString(R.string.oneplus_earphone_mode).toLowerCase();
                earphoneModeSummary = this.mContext.getString(R.string.oneplus_vibrate).toLowerCase();
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, earphoneModeSummary}));
            }
        }

        public void onReceive(Context context, Intent intent) {
        }
    }

    final class VolumePreferenceCallback implements Callback {
        private OPSeekBarVolumizer mCurrent;

        VolumePreferenceCallback() {
        }

        public void onSampleStarting(OPSeekBarVolumizer sbv) {
            if (!(this.mCurrent == null || this.mCurrent == sbv)) {
                this.mCurrent.stopSample();
            }
            this.mCurrent = sbv;
            if (this.mCurrent != null) {
                SoundSettings.this.mHandler.removeMessages(3);
                SoundSettings.this.mHandler.sendEmptyMessageDelayed(3, 2000);
            }
        }

        public void onStreamValueChanged(int stream, int progress) {
            if (this.mCurrent != null) {
                SoundSettings.this.mHandler.removeMessages(3);
                SoundSettings.this.mHandler.sendEmptyMessageDelayed(3, 2000);
            }
        }

        public void stopSample() {
            if (this.mCurrent != null) {
                this.mCurrent.stopSample();
            }
        }
    }

    private static boolean hasHaptic(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        return vibrator != null && vibrator.hasVibrator();
    }

    public int getMetricsCategory() {
        return 336;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String selectedPreference = savedInstanceState.getString(SELECTED_PREFERENCE_KEY, null);
            if (!TextUtils.isEmpty(selectedPreference)) {
                this.mRequestPreference = (RingtonePreference) findPreference(selectedPreference);
            }
        }
        this.mContext = getActivity();
        this.mPm = getPackageManager();
        this.mUserManager = UserManager.get(getContext());
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mVibrator = (Vibrator) getActivity().getSystemService("vibrator");
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mVoiceCapable = Utils.isVoiceCapable(this.mContext);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mVoiceCapable:");
        stringBuilder.append(this.mVoiceCapable);
        Log.d("volume", stringBuilder.toString());
        isSupportLinearMotor = this.mContext.getPackageManager().hasSystemFeature("oem.linear.motor.support");
        if (!(this.mVibrator == null || this.mVibrator.hasVibrator())) {
            this.mVibrator = null;
        }
        PreferenceCategory sound = (PreferenceCategory) findPreference(KEY_SOUND);
        this.mVolumeKeysAdjust = (ListPreference) findPreference(KEY_VOLUME_KEYS_ADJUST);
        this.mIncomingCallVibrateModePreference = findPreference(KEY_INCOMING_CALL_VIBRATE);
        this.mOPSoundDirectPreference = findPreference(KEY_OP_SOUND_DIRECT);
        this.mEarphoneModePreference = findPreference(KEY_EARPHONE_MODE);
        PreferenceCategory earphoneCategory = (PreferenceCategory) findPreference(KEY_EARPHONE);
        PreferenceCategory system = (PreferenceCategory) findPreference(KEY_SYSTEM);
        PreferenceCategory mVibrateCategory = (PreferenceCategory) findPreference(KEY_VIBRATE);
        this.mVibrateWhenRingingPreference = findPreference(KEY_VIBRATE_WHEN_RINGING);
        this.mMediaVolumePreference = initVolumePreference(KEY_MEDIA_VOLUME, 3, R.drawable.op_ic_audio_media_mute);
        initVolumePreference(KEY_ALARM_VOLUME, 4, R.drawable.op_ic_audio_alarm_mute);
        if (this.mVoiceCapable) {
            this.mRingOrNotificationPreference = initVolumePreference(KEY_RING_VOLUME, 2, R.drawable.op_ic_audio_ring_notif_mute);
            sound.removePreference(findPreference(KEY_NOTIFICATION_VOLUME));
        } else {
            this.mRingOrNotificationPreference = initVolumePreference(KEY_NOTIFICATION_VOLUME, 5, R.drawable.op_ic_audio_ring_notif);
            sound.removePreference(findPreference(KEY_RING_VOLUME));
        }
        if (this.mVolumeKeysAdjust != null) {
            this.mVolumeKeysAdjust.setOnPreferenceChangeListener(this);
            this.mVolumeKeysAdjust.setVisible(false);
        }
        updateVibratePreferenceDescription(KEY_INCOMING_CALL_VIBRATE, System.getInt(getActivity().getContentResolver(), KEY_INCOMING_CALL_VIBRATE, 0));
        this.mOPSoundDirectPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intents = new Intent();
                intents.setComponent(new ComponentName("com.oneplus.dirac.simplemanager", "com.oneplus.dirac.simplemanager.SimpleManager"));
                SoundSettings.this.getActivity().startActivity(intents);
                return true;
            }
        });
        this.mEarphoneModePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$OPEarphoneModeActivity"));
                SoundSettings.this.getActivity().startActivity(intent);
                OPUtils.sendAppTracker("ear.entrance", 1);
                return true;
            }
        });
        if (!(OPUtils.isAppPakExist(getActivity(), "com.oneplus.dirac.simplemanager") || this.mOPSoundDirectPreference == null)) {
            earphoneCategory.removePreference(this.mOPSoundDirectPreference);
        }
        if (OPUtils.isGuestMode()) {
            if (this.mVibrateWhenRingingPreference != null) {
                system.removePreference(this.mVibrateWhenRingingPreference);
            }
            system.removePreference(findPreference(KEY_SCREEN_LOCKING_SOUNDS));
        }
        if (isSupportLinearMotor) {
            if (this.mVibrateWhenRingingPreference != null) {
                system.removePreference(this.mVibrateWhenRingingPreference);
            }
            system.removePreference(findPreference(KEY_VIBRATE_ON_TOUCH));
        } else {
            getPreferenceScreen().removePreference(mVibrateCategory);
        }
        if (OPUtils.isGuestMode() && mVibrateCategory != null) {
            getPreferenceScreen().removePreference(mVibrateCategory);
        }
        if (!OpFeatures.isSupport(new int[]{40})) {
            removePreference(KEY_VIBRATE_INTENSITY);
        }
        initRingtones();
        initVibrateWhenRinging();
        initVibrateWhenRingingForVibrate();
        updateRingerMode();
        updateEffectsSuppressor();
        this.mSettingsObserver.register(true);
        PreferenceCategory mDNDPreferenceGroup = (PreferenceCategory) findPreference("do_not_disturb");
        if (!(mDNDPreferenceGroup == null || OPUtils.isSupportSocTriState())) {
            mDNDPreferenceGroup.setVisible(false);
        }
        if (!OPUtils.isSupportEarphoneMode()) {
            this.mEarphoneModePreference.setVisible(false);
        }
    }

    public Uri getDefaultPhoneRingUri(Context context) {
        if (OPRingtoneManager.isRingSimSwitchOn(context) && !getSim1Enable() && getSim2Enable()) {
            return OPRingtoneManager.getActualRingtoneUriBySubId(context, 1);
        }
        return RingtoneManager.getActualDefaultRingtoneUri(context, 1);
    }

    public boolean isMultiSimEnabled() {
        return this.mTelephonyManager.isMultiSimEnabled();
    }

    public boolean getSim1Enable() {
        return this.mTelephonyManager.hasIccCard(0);
    }

    public boolean getSim2Enable() {
        return this.mTelephonyManager.hasIccCard(1);
    }

    private VolumeSeekBarPreference initVolumePreference(String key, int stream, int muteIcon) {
        VolumeSeekBarPreference volumePref = (VolumeSeekBarPreference) findPreference(key);
        volumePref.setCallback(this.mVolumeCallback);
        volumePref.setStream(stream);
        this.mVolumePrefs.add(volumePref);
        volumePref.setMuteIcon(muteIcon);
        return volumePref;
    }

    private void initRingtones() {
        this.mPhoneRingtonePreference = getPreferenceScreen().findPreference(KEY_PHONE_RINGTONE);
        if (!(this.mPhoneRingtonePreference == null || this.mVoiceCapable)) {
            getPreferenceScreen().removePreference(this.mPhoneRingtonePreference);
            this.mPhoneRingtonePreference = null;
        }
        this.mNotificationRingtonePreference = getPreferenceScreen().findPreference(KEY_NOTIFICATION_RINGTONE);
        this.mAlarmRingtonePreference = getPreferenceScreen().findPreference(KEY_ALARM_RINGTONE);
        this.mSmsRingtonePreference = getPreferenceScreen().findPreference(KEY_MMS_RINGTONE);
    }

    private void lookupRingtoneNames() {
        AsyncTask.execute(this.mLookupRingtoneNames);
    }

    /* JADX WARNING: Missing block: B:28:0x00b3, code skipped:
            if (r0 != null) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:29:0x00b5, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:35:0x00c1, code skipped:
            if (r0 == null) goto L_0x00c8;
     */
    /* JADX WARNING: Missing block: B:37:0x00c5, code skipped:
            if (r0 == null) goto L_0x00c8;
     */
    private java.lang.CharSequence updateRingtoneName(android.content.Context r10, int r11) {
        /*
        r9 = this;
        r0 = 0;
        if (r10 != 0) goto L_0x000b;
    L_0x0003:
        r1 = "SoundSettings";
        r2 = "Unable to update ringtone name, no context provided";
        android.util.Log.e(r1, r2);
        return r0;
    L_0x000b:
        r1 = 1;
        if (r1 != r11) goto L_0x0013;
    L_0x000e:
        r1 = r9.getDefaultPhoneRingUri(r10);
        goto L_0x0017;
    L_0x0013:
        r1 = android.media.RingtoneManager.getActualDefaultRingtoneUri(r10, r11);
    L_0x0017:
        r2 = 17040834; // 0x10405c2 float:2.4248702E-38 double:8.4192907E-317;
        r8 = r10.getString(r2);
        if (r1 != 0) goto L_0x002a;
    L_0x0020:
        r0 = 17040833; // 0x10405c1 float:2.42487E-38 double:8.41929E-317;
        r0 = r10.getString(r0);
        r8 = r0;
        goto L_0x00c8;
        r2 = "media";
        r3 = r1.getAuthority();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = r2.equals(r3);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        if (r2 == 0) goto L_0x0067;
    L_0x0037:
        r2 = com.oneplus.settings.ringtone.OPRingtoneManager.isSystemRingtone(r10, r1, r11);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        if (r2 == 0) goto L_0x0051;
    L_0x003d:
        r2 = r10.getContentResolver();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r3 = "title";
        r4 = new java.lang.String[]{r3};	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r3 = r1;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r0 = r2;
        goto L_0x0088;
    L_0x0051:
        r2 = r10.getContentResolver();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r3 = "_display_name";
        r4 = "title";
        r4 = new java.lang.String[]{r3, r4};	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r3 = r1;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r0 = r2;
        goto L_0x0088;
    L_0x0067:
        r2 = "content";
        r3 = r1.getScheme();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = r2.equals(r3);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        if (r2 == 0) goto L_0x0088;
    L_0x0073:
        r2 = r10.getContentResolver();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r3 = "_display_name";
        r4 = "title";
        r4 = new java.lang.String[]{r3, r4};	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r3 = r1;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r0 = r2;
    L_0x0088:
        if (r0 == 0) goto L_0x00b3;
    L_0x008a:
        r2 = r0.moveToFirst();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        if (r2 == 0) goto L_0x00b3;
    L_0x0090:
        r2 = 0;
        r2 = r0.getString(r2);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = r2.toString();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = com.oneplus.settings.utils.OPUtils.getFileNameNoEx(r2);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r8 = r2;
        r2 = android.text.TextUtils.isEmpty(r8);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        if (r2 == 0) goto L_0x00b3;
    L_0x00a4:
        r2 = "title";
        r2 = r0.getColumnIndex(r2);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = r0.getString(r2);	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r2 = r2.toString();	 Catch:{ SQLiteException -> 0x00c4, IllegalArgumentException -> 0x00c0, all -> 0x00b9 }
        r8 = r2;
    L_0x00b3:
        if (r0 == 0) goto L_0x00c8;
    L_0x00b5:
        r0.close();
        goto L_0x00c8;
    L_0x00b9:
        r2 = move-exception;
        if (r0 == 0) goto L_0x00bf;
    L_0x00bc:
        r0.close();
    L_0x00bf:
        throw r2;
    L_0x00c0:
        r2 = move-exception;
        if (r0 == 0) goto L_0x00c8;
    L_0x00c3:
        goto L_0x00b5;
    L_0x00c4:
        r2 = move-exception;
        if (r0 == 0) goto L_0x00c8;
    L_0x00c7:
        goto L_0x00b5;
    L_0x00c8:
        return r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.SoundSettings.updateRingtoneName(android.content.Context, int):java.lang.CharSequence");
    }

    private void updateRingOrNotificationPreference() {
        boolean isSupportSocTriState = OPUtils.isSupportSocTriState();
        int i = R.drawable.op_ic_audio_ring_notif;
        int value;
        if (isSupportSocTriState) {
            try {
                value = Global.getInt(getContentResolver(), "three_Key_mode");
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("three key mode change value:");
                stringBuilder.append(value);
                Log.d(str, stringBuilder.toString());
                if (value == 1) {
                    this.mRingOrNotificationPreference.showIcon(R.drawable.op_ic_audio_ring_notif_mute);
                    return;
                } else if (value == 2) {
                    this.mRingOrNotificationPreference.showIcon(R.drawable.op_ic_audio_ring_notif_vibrate);
                    return;
                } else if (value == 3) {
                    this.mRingOrNotificationPreference.showIcon(R.drawable.op_ic_audio_ring_notif);
                    return;
                } else {
                    return;
                }
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        value = System.getInt(this.mContext.getContentResolver(), "oem_vibrate_under_silent", 0);
        VolumeSeekBarPreference volumeSeekBarPreference = this.mRingOrNotificationPreference;
        if (isZenMuted()) {
            i = value != 0 ? R.drawable.op_ic_audio_ring_notif_vibrate : R.drawable.op_ic_audio_ring_notif_mute;
        }
        volumeSeekBarPreference.showIcon(i);
    }

    private void updateDoNotDisturbPreference() {
        boolean z = false;
        int zen = Global.getInt(this.mContext.getContentResolver(), "zen_mode_car", 0);
        Preference pref = findPreference(KEY_ZEN_MODE);
        if (pref != null) {
            if (zen == 0) {
                z = true;
            }
            pref.setEnabled(z);
        }
    }

    public boolean isZenMuted() {
        return getThreeKeyStatus(this.mContext) == 1;
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

    private void updateRingerMode() {
        int ringerMode = this.mAudioManager.getRingerModeInternal();
        if (this.mRingerMode != ringerMode) {
            this.mRingerMode = ringerMode;
            updateRingOrNotificationPreference();
        }
    }

    private void updateEffectsSuppressor() {
        ComponentName suppressor = NotificationManager.from(this.mContext).getEffectsSuppressor();
        if (!Objects.equals(suppressor, this.mSuppressor)) {
            this.mSuppressor = suppressor;
            if (this.mRingOrNotificationPreference != null) {
                String text;
                if (suppressor != null) {
                    text = this.mContext.getString(17040359, new Object[]{getSuppressorCaption(suppressor)});
                } else {
                    text = null;
                }
                this.mRingOrNotificationPreference.setSuppressionText(text);
            }
            updateRingOrNotificationPreference();
        }
    }

    private String getSuppressorCaption(ComponentName suppressor) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            ServiceInfo info = pm.getServiceInfo(suppressor, null);
            if (info != null) {
                CharSequence seq = info.loadLabel(pm);
                if (seq != null) {
                    String str = seq.toString().trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error loading suppressor caption", e);
        }
        return suppressor.getPackageName();
    }

    private void initVibrateWhenRinging() {
        this.mVibrateWhenRinging = (TwoStatePreference) getPreferenceScreen().findPreference(KEY_VIBRATE_WHEN_RINGING);
        if (this.mVibrateWhenRinging == null) {
            Log.i(TAG, "Preference not found: vibrate_when_ringing");
        } else if (this.mVoiceCapable) {
            this.mVibrateWhenRinging.setPersistent(false);
            updateVibrateWhenRinging();
            this.mVibrateWhenRinging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SoundSettings.this.mVibrateWhenRingingValue = ((Boolean) newValue).booleanValue();
                    SoundSettings.this.mHandler.removeCallbacks(SoundSettings.this.mVibrateWhenRingingRunnable);
                    SoundSettings.this.mHandler.postDelayed(SoundSettings.this.mVibrateWhenRingingRunnable, 500);
                    return true;
                }
            });
        } else {
            getPreferenceScreen().removePreference(this.mVibrateWhenRinging);
            this.mVibrateWhenRinging = null;
        }
    }

    private void updateVibrateWhenRinging() {
        if (this.mVibrateWhenRinging != null) {
            TwoStatePreference twoStatePreference = this.mVibrateWhenRinging;
            boolean z = false;
            if (System.getInt(getContentResolver(), KEY_VIBRATE_WHEN_RINGING, 0) != 0) {
                z = true;
            }
            twoStatePreference.setChecked(z);
        }
    }

    private void initVibrateWhenRingingForVibrate() {
        this.mVibrateWhenRingingForVibrate = (TwoStatePreference) getPreferenceScreen().findPreference(KEY_VIBRATE_WHEN_RINGING_FOR_VIBRATE);
        if (this.mVibrateWhenRingingForVibrate != null) {
            if (this.mVoiceCapable) {
                this.mVibrateWhenRingingForVibrate.setPersistent(false);
                updateVibrateWhenRingingForVibrate();
                this.mVibrateWhenRingingForVibrate.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        return System.putInt(SoundSettings.this.getContentResolver(), SoundSettings.KEY_VIBRATE_WHEN_RINGING, ((Boolean) newValue).booleanValue());
                    }
                });
                return;
            }
            getPreferenceScreen().removePreference(this.mVibrateWhenRingingForVibrate);
            this.mVibrateWhenRingingForVibrate = null;
        }
    }

    private void updateVibrateWhenRingingForVibrate() {
        if (this.mVibrateWhenRingingForVibrate != null) {
            TwoStatePreference twoStatePreference = this.mVibrateWhenRingingForVibrate;
            boolean z = false;
            if (System.getInt(getContentResolver(), KEY_VIBRATE_WHEN_RINGING, 0) != 0) {
                z = true;
            }
            twoStatePreference.setChecked(z);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_sound;
    }

    public void onPause() {
        super.onPause();
        Iterator it = this.mVolumePrefs.iterator();
        while (it.hasNext()) {
            ((VolumeSeekBarPreference) it.next()).onActivityPause();
        }
        this.mVolumeCallback.stopSample();
        this.mReceiver.register(false);
    }

    public void onStop() {
        if (isMultiSimEnabled()) {
            getContext().unregisterReceiver(this.mSimStateReceiver);
        }
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSettingsObserver.register(false);
    }

    public void onStart() {
        super.onStart();
        if (isMultiSimEnabled()) {
            getContext().registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    public void onResume() {
        super.onResume();
        lookupRingtoneNames();
        this.mReceiver.register(true);
        updateRingOrNotificationPreference();
        updateEffectsSuppressor();
        Iterator it = this.mVolumePrefs.iterator();
        while (it.hasNext()) {
            ((VolumeSeekBarPreference) it.next()).onActivityResume();
        }
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_adjust_volume", UserHandle.myUserId());
        boolean hasBaseRestriction = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_adjust_volume", UserHandle.myUserId());
        for (String key : RESTRICTED_KEYS) {
            Preference pref = findPreference(key);
            if (pref != null) {
                pref.setEnabled(hasBaseRestriction ^ 1);
            }
            if ((pref instanceof RestrictedPreference) && !hasBaseRestriction) {
                ((RestrictedPreference) pref).setDisabledByAdmin(admin);
            }
        }
        RestrictedPreference broadcastSettingsPref = (RestrictedPreference) findPreference(KEY_CELL_BROADCAST_SETTINGS);
        if (broadcastSettingsPref != null) {
            broadcastSettingsPref.checkRestrictionAndSetDisabled("no_config_cell_broadcasts");
        }
        updateVibrateWhenRinging();
        for (SettingPref pref2 : PREFS) {
            if (pref2 != null) {
                pref2.init(this);
            }
        }
        this.mMediaVolumePreference.setSeekbar(this.mAudioManager.getStreamVolume(3));
        updateVolumeKeysAdjustSummary(System.getIntForUser(getContentResolver(), KEY_VOLUME_KEYS_ADJUST_VALUE, 0, -2));
        updateDoNotDisturbPreference();
    }

    private void updateVolumeKeysAdjustSummary(int value) {
        this.mVolumeKeysAdjust.setValue(String.valueOf(value));
        this.mVolumeKeysAdjust.setSummary(getContext().getResources().getStringArray(2130903234)[value]);
    }

    private void updateVibratePreferenceDescription(String key, int value) {
        Preference incomingCallVibrateModePreference = findPreference(key);
        if (incomingCallVibrateModePreference != null) {
            incomingCallVibrateModePreference.setSummary(getActivity().getResources().getStringArray(R.array.incoming_call_vibrate_mode)[value]);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (KEY_INCOMING_CALL_VIBRATE.equals(preference.getKey())) {
            this.mOPListDialog = new OPListDialog(this.mContext, preference.getTitle(), this.mContext.getResources().getStringArray(R.array.incoming_call_vibrate_mode_values), this.mContext.getResources().getStringArray(R.array.incoming_call_vibrate_mode));
            this.mOPListDialog.setOnDialogListItemClickListener(this);
            this.mOPListDialog.setVibrateKey(KEY_INCOMING_CALL_VIBRATE);
            this.mOPListDialog.show();
            return true;
        } else if (!(preference instanceof RingtonePreference)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            this.mRequestPreference = (RingtonePreference) preference;
            this.mRequestPreference.onPrepareRingtonePickerIntent(this.mRequestPreference.getIntent());
            if (this.mRequestPreference.getIntent() != null) {
                startActivityForResultAsUser(this.mRequestPreference.getIntent(), 200, null, UserHandle.of(this.mRequestPreference.getUserId()));
            }
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        int value = Integer.parseInt((String) objValue);
        if (KEY_INCOMING_CALL_VIBRATE.equals(key) && this.mVibrator != null) {
            System.putInt(getActivity().getContentResolver(), KEY_INCOMING_CALL_VIBRATE, value);
            updateVibratePreferenceDescription(KEY_INCOMING_CALL_VIBRATE, value);
            int intensityvalue = System.getInt(getContentResolver(), "incoming_call_vibrate_intensity", -1);
            this.mVibrator.cancel();
            if (intensityvalue == 0) {
                this.sVibratePatternrhythm[value][0] = -1;
            } else if (intensityvalue == 1) {
                this.sVibratePatternrhythm[value][0] = -2;
            } else if (intensityvalue == 2) {
                this.sVibratePatternrhythm[value][0] = -3;
            }
            this.mVibrator.vibrate(this.sVibratePatternrhythm[value], -1);
        } else if (KEY_VOLUME_KEYS_ADJUST.equals(key)) {
            System.putIntForUser(getActivity().getContentResolver(), KEY_VOLUME_KEYS_ADJUST_VALUE, value, -2);
            updateVolumeKeysAdjustSummary(value);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.sound_settings;
    }

    public void OnDialogListItemClick(int index) {
        if (this.mVibrator != null) {
            int intensityvalue = System.getInt(getContentResolver(), "incoming_call_vibrate_intensity", -1);
            this.mVibrator.cancel();
            if (intensityvalue == 0) {
                this.sVibratePatternrhythm[index][0] = -1;
            } else if (intensityvalue == 1) {
                this.sVibratePatternrhythm[index][0] = -2;
            } else if (intensityvalue == 2) {
                this.sVibratePatternrhythm[index][0] = -3;
            }
            this.mVibrator.vibrate(this.sVibratePatternrhythm[index], -1);
        }
    }

    public void OnDialogListConfirmClick(int index) {
        System.putInt(getActivity().getContentResolver(), KEY_INCOMING_CALL_VIBRATE, index);
        updateVibratePreferenceDescription(KEY_INCOMING_CALL_VIBRATE, index);
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
    }

    public void OnDialogListCancelClick() {
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this, getLifecycle());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mRequestPreference != null) {
            this.mRequestPreference.onActivityResult(requestCode, resultCode, data);
            this.mRequestPreference = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRequestPreference != null) {
            outState.putString(SELECTED_PREFERENCE_KEY, this.mRequestPreference.getKey());
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AlarmVolumePreferenceController) use(AlarmVolumePreferenceController.class)).setCallback(this.mVolumeCallback);
        ((MediaVolumePreferenceController) use(MediaVolumePreferenceController.class)).setCallback(this.mVolumeCallback);
        ((RingVolumePreferenceController) use(RingVolumePreferenceController.class)).setCallback(this.mVolumeCallback);
        ((SoundVolumePreferenceCategoryController) use(SoundVolumePreferenceCategoryController.class)).setChildren(Arrays.asList(new AbstractPreferenceController[]{alarm, media, ring}));
        ((NotificationVolumePreferenceController) use(NotificationVolumePreferenceController.class)).setCallback(this.mVolumeCallback);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, SoundSettings fragment, Lifecycle lifecycle) {
        Context context2 = context;
        SoundSettings soundSettings = fragment;
        Lifecycle lifecycle2 = lifecycle;
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new OPPreferenceDividerLine(context2));
        List<AbstractPreferenceController> doNotDisturbControllers = new ArrayList();
        doNotDisturbControllers.add(new ZenModePreferenceController(context2, lifecycle2, KEY_ZEN_MODE));
        controllers.add(new PreferenceCategoryController(context2, "do_not_disturb").setChildren(doNotDisturbControllers));
        controllers.addAll(doNotDisturbControllers);
        controllers.add(new PhoneRingtonePreferenceController(context2));
        controllers.add(new AlarmRingtonePreferenceController(context2));
        controllers.add(new NotificationRingtonePreferenceController(context2));
        controllers.add(new WorkSoundPreferenceController(context2, soundSettings, lifecycle2));
        DialPadTonePreferenceController dialPadTonePreferenceController = new DialPadTonePreferenceController(context2, soundSettings, lifecycle2);
        ScreenLockSoundPreferenceController screenLockSoundPreferenceController = new ScreenLockSoundPreferenceController(context2, soundSettings, lifecycle2);
        ChargingSoundPreferenceController chargingSoundPreferenceController = new ChargingSoundPreferenceController(context2, soundSettings, lifecycle2);
        DockingSoundPreferenceController dockingSoundPreferenceController = new DockingSoundPreferenceController(context2, soundSettings, lifecycle2);
        TouchSoundPreferenceController touchSoundPreferenceController = new TouchSoundPreferenceController(context2, soundSettings, lifecycle2);
        VibrateOnTouchPreferenceController vibrateOnTouchPreferenceController = new VibrateOnTouchPreferenceController(context2, soundSettings, lifecycle2);
        DockAudioMediaPreferenceController dockAudioMediaPreferenceController = new DockAudioMediaPreferenceController(context2, soundSettings, lifecycle2);
        BootSoundPreferenceController bootSoundPreferenceController = new BootSoundPreferenceController(context2);
        EmergencyTonePreferenceController emergencyTonePreferenceController = new EmergencyTonePreferenceController(context2, soundSettings, lifecycle2);
        controllers.add(dialPadTonePreferenceController);
        controllers.add(screenLockSoundPreferenceController);
        controllers.add(chargingSoundPreferenceController);
        controllers.add(dockingSoundPreferenceController);
        controllers.add(touchSoundPreferenceController);
        controllers.add(vibrateOnTouchPreferenceController);
        controllers.add(dockAudioMediaPreferenceController);
        controllers.add(bootSoundPreferenceController);
        controllers.add(emergencyTonePreferenceController);
        controllers.add(new PreferenceCategoryController(context2, KEY_SYSTEM).setChildren(Arrays.asList(new AbstractPreferenceController[]{dialPadTonePreferenceController, screenLockSoundPreferenceController, chargingSoundPreferenceController, dockingSoundPreferenceController, touchSoundPreferenceController, vibrateOnTouchPreferenceController, dockAudioMediaPreferenceController, bootSoundPreferenceController, emergencyTonePreferenceController})));
        return controllers;
    }

    /* Access modifiers changed, original: 0000 */
    public void enableWorkSync() {
        WorkSoundPreferenceController workSoundController = (WorkSoundPreferenceController) use(WorkSoundPreferenceController.class);
        if (workSoundController != null) {
            workSoundController.enableWorkSync();
        }
    }
}
