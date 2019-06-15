package com.android.settings.utils;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageItemInfo.DisplayNameComparator;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceScreen;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.widget.AppSwitchPreference;
import com.android.settingslib.applications.ServiceListing;
import java.util.List;

public abstract class ManagedServiceSettings extends EmptyTextSettings {
    private static final String TAG = "ManagedServiceSettings";
    private final Config mConfig = getConfig();
    protected Context mContext;
    private DevicePolicyManager mDpm;
    private IconDrawableFactory mIconDrawableFactory;
    private PackageManager mPm;
    private ServiceListing mServiceListing;

    public static class Config {
        public final int emptyText;
        public final String intentAction;
        public final String noun;
        public final String permission;
        public final String setting;
        public final String tag;
        public final int warningDialogSummary;
        public final int warningDialogTitle;

        public static class Builder {
            private int mEmptyText;
            private String mIntentAction;
            private String mNoun;
            private String mPermission;
            private String mSetting;
            private String mTag;
            private int mWarningDialogSummary;
            private int mWarningDialogTitle;

            public Builder setTag(String tag) {
                this.mTag = tag;
                return this;
            }

            public Builder setSetting(String setting) {
                this.mSetting = setting;
                return this;
            }

            public Builder setIntentAction(String intentAction) {
                this.mIntentAction = intentAction;
                return this;
            }

            public Builder setPermission(String permission) {
                this.mPermission = permission;
                return this;
            }

            public Builder setNoun(String noun) {
                this.mNoun = noun;
                return this;
            }

            public Builder setWarningDialogTitle(int warningDialogTitle) {
                this.mWarningDialogTitle = warningDialogTitle;
                return this;
            }

            public Builder setWarningDialogSummary(int warningDialogSummary) {
                this.mWarningDialogSummary = warningDialogSummary;
                return this;
            }

            public Builder setEmptyText(int emptyText) {
                this.mEmptyText = emptyText;
                return this;
            }

            public Config build() {
                return new Config(this.mTag, this.mSetting, this.mIntentAction, this.mPermission, this.mNoun, this.mWarningDialogTitle, this.mWarningDialogSummary, this.mEmptyText);
            }
        }

        private Config(String tag, String setting, String intentAction, String permission, String noun, int warningDialogTitle, int warningDialogSummary, int emptyText) {
            this.tag = tag;
            this.setting = setting;
            this.intentAction = intentAction;
            this.permission = permission;
            this.noun = noun;
            this.warningDialogTitle = warningDialogTitle;
            this.warningDialogSummary = warningDialogSummary;
            this.emptyText = emptyText;
        }
    }

    public static class ScaryWarningDialogFragment extends InstrumentedDialogFragment {
        private static final String KEY_COMPONENT = "c";
        private static final String KEY_LABEL = "l";

        public int getMetricsCategory() {
            return 557;
        }

        public ScaryWarningDialogFragment setServiceInfo(ComponentName cn, String label, Fragment target) {
            Bundle args = new Bundle();
            args.putString(KEY_COMPONENT, cn.flattenToString());
            args.putString(KEY_LABEL, label);
            setArguments(args);
            setTargetFragment(target, 0);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String label = args.getString(KEY_LABEL);
            ComponentName cn = ComponentName.unflattenFromString(args.getString(KEY_COMPONENT));
            ManagedServiceSettings parent = (ManagedServiceSettings) getTargetFragment();
            return new Builder(getContext()).setMessage(getResources().getString(parent.mConfig.warningDialogSummary, new Object[]{label})).setTitle(getResources().getString(parent.mConfig.warningDialogTitle, new Object[]{label})).setCancelable(true).setPositiveButton(R.string.allow, new -$$Lambda$ManagedServiceSettings$ScaryWarningDialogFragment$GfuRaJIB12V_MS8RLGOsdgpO8G0(parent, cn)).setNegativeButton(R.string.deny, -$$Lambda$ManagedServiceSettings$ScaryWarningDialogFragment$zGrX-jMl8gPwJu7rfyhg512VL6Y.INSTANCE).create();
        }

        static /* synthetic */ void lambda$onCreateDialog$1(DialogInterface dialog, int id) {
        }
    }

    public abstract Config getConfig();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPm = this.mContext.getPackageManager();
        this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(this.mContext);
        this.mServiceListing = new ServiceListing.Builder(this.mContext).setPermission(this.mConfig.permission).setIntentAction(this.mConfig.intentAction).setNoun(this.mConfig.noun).setSetting(this.mConfig.setting).setTag(this.mConfig.tag).build();
        this.mServiceListing.addCallback(new -$$Lambda$ManagedServiceSettings$6gJSYmD-m4iGVFUdlUroaoAptMw(this));
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this.mContext));
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(this.mConfig.emptyText);
    }

    public void onResume() {
        super.onResume();
        if (ActivityManager.isLowRamDeviceStatic()) {
            setEmptyText(R.string.disabled_low_ram_device);
            return;
        }
        this.mServiceListing.reload();
        this.mServiceListing.setListening(true);
    }

    public void onPause() {
        super.onPause();
        this.mServiceListing.setListening(false);
    }

    private void updateList(List<ServiceInfo> services) {
        int managedProfileId = Utils.getManagedProfileId((UserManager) this.mContext.getSystemService("user"), UserHandle.myUserId());
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        services.sort(new DisplayNameComparator(this.mPm));
        for (ServiceInfo service : services) {
            ComponentName cn = new ComponentName(service.packageName, service.name);
            CharSequence title = null;
            try {
                title = this.mPm.getApplicationInfoAsUser(service.packageName, 0, getCurrentUser(managedProfileId)).loadLabel(this.mPm);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "can't find package name", e);
            }
            String summary = service.loadLabel(this.mPm).toString();
            SwitchPreference pref = new AppSwitchPreference(getPrefContext());
            pref.setPersistent(false);
            pref.setIcon(this.mIconDrawableFactory.getBadgedIcon(service, service.applicationInfo, UserHandle.getUserId(service.applicationInfo.uid)));
            if (title == null || title.equals(summary)) {
                pref.setTitle((CharSequence) summary);
            } else {
                pref.setTitle(title);
                pref.setSummary((CharSequence) summary);
            }
            pref.setKey(cn.flattenToString());
            pref.setChecked(isServiceEnabled(cn));
            if (!(managedProfileId == -10000 || this.mDpm.isNotificationListenerServicePermitted(service.packageName, managedProfileId))) {
                pref.setSummary((int) R.string.work_profile_notification_access_blocked_summary);
            }
            pref.setOnPreferenceChangeListener(new -$$Lambda$ManagedServiceSettings$qzumG4qfCDX22E2-mvpKDzSZyck(this, cn, summary));
            pref.setKey(cn.flattenToString());
            screen.addPreference(pref);
        }
        highlightPreferenceIfNeeded();
    }

    private int getCurrentUser(int managedProfileId) {
        if (managedProfileId != -10000) {
            return managedProfileId;
        }
        return UserHandle.myUserId();
    }

    /* Access modifiers changed, original: protected */
    public boolean isServiceEnabled(ComponentName cn) {
        return this.mServiceListing.isEnabled(cn);
    }

    /* Access modifiers changed, original: protected */
    public boolean setEnabled(ComponentName service, String title, boolean enable) {
        if (!enable) {
            this.mServiceListing.setEnabled(service, false);
            return true;
        } else if (this.mServiceListing.isEnabled(service)) {
            return true;
        } else {
            new ScaryWarningDialogFragment().setServiceInfo(service, title, this).show(getFragmentManager(), "dialog");
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public void enable(ComponentName service) {
        this.mServiceListing.setEnabled(service, true);
    }
}
