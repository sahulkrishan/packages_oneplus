package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.DefaultRingtonePreference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WorkSoundPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    public static final String KEY_WORK_ALARM_RINGTONE = "work_alarm_ringtone";
    public static final String KEY_WORK_CATEGORY = "sound_work_settings_section";
    public static final String KEY_WORK_NOTIFICATION_RINGTONE = "work_notification_ringtone";
    public static final String KEY_WORK_PHONE_RINGTONE = "work_ringtone";
    public static final String KEY_WORK_USE_PERSONAL_SOUNDS = "work_use_personal_sounds";
    private static final String TAG = "WorkSoundPrefController";
    private final AudioHelper mHelper;
    private int mManagedProfileId;
    private final BroadcastReceiver mManagedProfileReceiver;
    private final SoundSettings mParent;
    private final UserManager mUserManager;
    private final boolean mVoiceCapable;
    private Preference mWorkAlarmRingtonePreference;
    private Preference mWorkNotificationRingtonePreference;
    private Preference mWorkPhoneRingtonePreference;
    private PreferenceGroup mWorkPreferenceCategory;
    private TwoStatePreference mWorkUsePersonalSounds;

    public static class UnifyWorkDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
        private static final int REQUEST_CODE = 200;
        private static final String TAG = "UnifyWorkDialogFragment";

        public int getMetricsCategory() {
            return 553;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setTitle(R.string.work_sync_dialog_title).setMessage(R.string.work_sync_dialog_message).setPositiveButton(R.string.work_sync_dialog_yes, this).setNegativeButton(17039369, null).create();
        }

        public static void show(SoundSettings parent) {
            FragmentManager fm = parent.getFragmentManager();
            if (fm.findFragmentByTag(TAG) == null) {
                UnifyWorkDialogFragment fragment = new UnifyWorkDialogFragment();
                fragment.setTargetFragment(parent, 200);
                fragment.show(fm, TAG);
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            SoundSettings soundSettings = (SoundSettings) getTargetFragment();
            if (soundSettings.isAdded()) {
                soundSettings.enableWorkSync();
            }
        }
    }

    public WorkSoundPreferenceController(Context context, SoundSettings parent, Lifecycle lifecycle) {
        this(context, parent, lifecycle, new AudioHelper(context));
    }

    @VisibleForTesting
    WorkSoundPreferenceController(Context context, SoundSettings parent, Lifecycle lifecycle, AudioHelper helper) {
        super(context);
        this.mManagedProfileReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:12:0x0037 A:{RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:15:0x003e  */
            /* JADX WARNING: Removed duplicated region for block: B:13:0x0038  */
            /* JADX WARNING: Removed duplicated region for block: B:12:0x0037 A:{RETURN} */
            /* JADX WARNING: Removed duplicated region for block: B:15:0x003e  */
            /* JADX WARNING: Removed duplicated region for block: B:13:0x0038  */
            public void onReceive(android.content.Context r5, android.content.Intent r6) {
                /*
                r4 = this;
                r0 = "android.intent.extra.USER";
                r0 = r6.getExtra(r0);
                r0 = (android.os.UserHandle) r0;
                r0 = r0.getIdentifier();
                r1 = r6.getAction();
                r2 = r1.hashCode();
                r3 = -385593787; // 0xffffffffe9044e45 float:-9.996739E24 double:NaN;
                if (r2 == r3) goto L_0x0029;
            L_0x0019:
                r3 = 1051477093; // 0x3eac4465 float:0.3364593 double:5.19498709E-315;
                if (r2 == r3) goto L_0x001f;
            L_0x001e:
                goto L_0x0033;
            L_0x001f:
                r2 = "android.intent.action.MANAGED_PROFILE_REMOVED";
                r1 = r1.equals(r2);
                if (r1 == 0) goto L_0x0033;
            L_0x0027:
                r1 = 1;
                goto L_0x0034;
            L_0x0029:
                r2 = "android.intent.action.MANAGED_PROFILE_ADDED";
                r1 = r1.equals(r2);
                if (r1 == 0) goto L_0x0033;
            L_0x0031:
                r1 = 0;
                goto L_0x0034;
            L_0x0033:
                r1 = -1;
            L_0x0034:
                switch(r1) {
                    case 0: goto L_0x003e;
                    case 1: goto L_0x0038;
                    default: goto L_0x0037;
                };
            L_0x0037:
                return;
            L_0x0038:
                r1 = com.android.settings.notification.WorkSoundPreferenceController.this;
                r1.onManagedProfileRemoved(r0);
                return;
            L_0x003e:
                r1 = com.android.settings.notification.WorkSoundPreferenceController.this;
                r1.onManagedProfileAdded(r0);
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.WorkSoundPreferenceController$AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        };
        this.mUserManager = UserManager.get(context);
        this.mVoiceCapable = Utils.isVoiceCapable(this.mContext);
        this.mParent = parent;
        this.mHelper = helper;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mWorkPreferenceCategory = (PreferenceGroup) screen.findPreference(KEY_WORK_CATEGORY);
        if (this.mWorkPreferenceCategory != null) {
            this.mWorkPreferenceCategory.setVisible(isAvailable());
        }
    }

    public void onResume() {
        IntentFilter managedProfileFilter = new IntentFilter();
        managedProfileFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        managedProfileFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mContext.registerReceiver(this.mManagedProfileReceiver, managedProfileFilter);
        this.mManagedProfileId = this.mHelper.getManagedProfileId(this.mUserManager);
        updateWorkPreferences();
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mManagedProfileReceiver);
    }

    public String getPreferenceKey() {
        return KEY_WORK_CATEGORY;
    }

    public boolean isAvailable() {
        return this.mHelper.getManagedProfileId(this.mUserManager) != -10000 && shouldShowRingtoneSettings();
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int ringtoneType;
        if (KEY_WORK_PHONE_RINGTONE.equals(preference.getKey())) {
            ringtoneType = 1;
        } else if (KEY_WORK_NOTIFICATION_RINGTONE.equals(preference.getKey())) {
            ringtoneType = 2;
        } else if (!KEY_WORK_ALARM_RINGTONE.equals(preference.getKey())) {
            return true;
        } else {
            ringtoneType = 4;
        }
        preference.setSummary(updateRingtoneName(getManagedProfileContext(), ringtoneType));
        return true;
    }

    private boolean shouldShowRingtoneSettings() {
        return this.mHelper.isSingleVolume() ^ 1;
    }

    private CharSequence updateRingtoneName(Context context, int type) {
        if (context == null || !this.mHelper.isUserUnlocked(this.mUserManager, context.getUserId())) {
            return this.mContext.getString(R.string.managed_profile_not_available_label);
        }
        return Ringtone.getTitle(context, RingtoneManager.getActualDefaultRingtoneUri(context, type), false, true);
    }

    private Context getManagedProfileContext() {
        if (this.mManagedProfileId == -10000) {
            return null;
        }
        return this.mHelper.createPackageContextAsUser(this.mManagedProfileId);
    }

    private DefaultRingtonePreference initWorkPreference(PreferenceGroup root, String key) {
        DefaultRingtonePreference pref = (DefaultRingtonePreference) root.findPreference(key);
        pref.setOnPreferenceChangeListener(this);
        pref.setUserId(this.mManagedProfileId);
        return pref;
    }

    private void updateWorkPreferences() {
        if (this.mWorkPreferenceCategory != null) {
            boolean isAvailable = isAvailable();
            this.mWorkPreferenceCategory.setVisible(isAvailable);
            if (isAvailable) {
                if (this.mWorkUsePersonalSounds == null) {
                    this.mWorkUsePersonalSounds = (TwoStatePreference) this.mWorkPreferenceCategory.findPreference(KEY_WORK_USE_PERSONAL_SOUNDS);
                    this.mWorkUsePersonalSounds.setOnPreferenceChangeListener(new -$$Lambda$WorkSoundPreferenceController$XBbO1oM_StZ54wAnUJEnnExa5OU(this));
                }
                if (this.mWorkPhoneRingtonePreference == null) {
                    this.mWorkPhoneRingtonePreference = initWorkPreference(this.mWorkPreferenceCategory, KEY_WORK_PHONE_RINGTONE);
                }
                if (this.mWorkNotificationRingtonePreference == null) {
                    this.mWorkNotificationRingtonePreference = initWorkPreference(this.mWorkPreferenceCategory, KEY_WORK_NOTIFICATION_RINGTONE);
                }
                if (this.mWorkAlarmRingtonePreference == null) {
                    this.mWorkAlarmRingtonePreference = initWorkPreference(this.mWorkPreferenceCategory, KEY_WORK_ALARM_RINGTONE);
                    this.mWorkAlarmRingtonePreference.setVisible(false);
                }
                if (!this.mVoiceCapable) {
                    this.mWorkPhoneRingtonePreference.setVisible(false);
                    this.mWorkPhoneRingtonePreference = null;
                }
                if (Secure.getIntForUser(getManagedProfileContext().getContentResolver(), "sync_parent_sounds", 0, this.mManagedProfileId) == 1) {
                    enableWorkSyncSettings();
                } else {
                    disableWorkSyncSettings();
                }
            }
        }
    }

    public static /* synthetic */ boolean lambda$updateWorkPreferences$0(WorkSoundPreferenceController workSoundPreferenceController, Preference p, Object value) {
        if (((Boolean) value).booleanValue()) {
            UnifyWorkDialogFragment.show(workSoundPreferenceController.mParent);
            return false;
        }
        workSoundPreferenceController.disableWorkSync();
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void enableWorkSync() {
        RingtoneManager.enableSyncFromParent(getManagedProfileContext());
        enableWorkSyncSettings();
    }

    private void enableWorkSyncSettings() {
        this.mWorkUsePersonalSounds.setChecked(true);
        if (this.mWorkPhoneRingtonePreference != null) {
            this.mWorkPhoneRingtonePreference.setSummary((int) R.string.work_sound_same_as_personal);
        }
        this.mWorkNotificationRingtonePreference.setSummary((int) R.string.work_sound_same_as_personal);
        this.mWorkAlarmRingtonePreference.setSummary((int) R.string.work_sound_same_as_personal);
    }

    private void disableWorkSync() {
        RingtoneManager.disableSyncFromParent(getManagedProfileContext());
        disableWorkSyncSettings();
    }

    private void disableWorkSyncSettings() {
        if (this.mWorkPhoneRingtonePreference != null) {
            this.mWorkPhoneRingtonePreference.setEnabled(true);
        }
        this.mWorkNotificationRingtonePreference.setEnabled(true);
        this.mWorkAlarmRingtonePreference.setEnabled(true);
        updateWorkRingtoneSummaries();
    }

    private void updateWorkRingtoneSummaries() {
        Context managedProfileContext = getManagedProfileContext();
        if (this.mWorkPhoneRingtonePreference != null) {
            this.mWorkPhoneRingtonePreference.setSummary(updateRingtoneName(managedProfileContext, 1));
        }
        this.mWorkNotificationRingtonePreference.setSummary(updateRingtoneName(managedProfileContext, 2));
        this.mWorkAlarmRingtonePreference.setSummary(updateRingtoneName(managedProfileContext, 4));
    }

    public void onManagedProfileAdded(int profileId) {
        if (this.mManagedProfileId == -10000) {
            this.mManagedProfileId = profileId;
            updateWorkPreferences();
        }
    }

    public void onManagedProfileRemoved(int profileId) {
        if (this.mManagedProfileId == profileId) {
            this.mManagedProfileId = this.mHelper.getManagedProfileId(this.mUserManager);
            updateWorkPreferences();
        }
    }
}
