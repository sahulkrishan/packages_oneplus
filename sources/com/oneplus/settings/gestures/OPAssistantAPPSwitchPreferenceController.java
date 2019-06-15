package com.oneplus.settings.gestures;

import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.service.voice.VoiceInteractionServiceInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.app.AssistUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.MasterSwitchController;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPAssistantAPPSwitchPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnSwitchChangeListener {
    private static final Intent ASSIST_ACTIVITY_PROBE = new Intent("android.intent.action.ASSIST");
    private static final Intent ASSIST_SERVICE_PROBE = new Intent("android.service.voice.VoiceInteractionService");
    private static final String KEY_QUICK_TURN_ON_ASSISTANT_APP = "quick_turn_on_assistant_app";
    private AssistUtils mAssistUtils;
    private final List<Info> mAvailableAssistants = new ArrayList();
    protected PackageManagerWrapper mPm;
    private MasterSwitchPreference mSwitch;
    private MasterSwitchController mSwitchController;

    static class Info {
        public final ComponentName component;
        public final VoiceInteractionServiceInfo voiceInteractionServiceInfo;

        Info(ComponentName component) {
            this.component = component;
            this.voiceInteractionServiceInfo = null;
        }

        Info(ComponentName component, VoiceInteractionServiceInfo voiceInteractionServiceInfo) {
            this.component = component;
            this.voiceInteractionServiceInfo = voiceInteractionServiceInfo;
        }

        public boolean isVoiceInteractionService() {
            return this.voiceInteractionServiceInfo != null;
        }
    }

    public OPAssistantAPPSwitchPreferenceController(Context context) {
        super(context, KEY_QUICK_TURN_ON_ASSISTANT_APP);
        this.mPm = new PackageManagerWrapper(context.getPackageManager());
        this.mAssistUtils = new AssistUtils(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitch = (MasterSwitchPreference) screen.findPreference(KEY_QUICK_TURN_ON_ASSISTANT_APP);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_QUICK_TURN_ON_ASSISTANT_APP;
    }

    public ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
    }

    private String getDefaultKey() {
        ComponentName cn = getCurrentAssist();
        if (cn == null || !OPUtils.isAppExist(this.mContext, cn.getPackageName())) {
            return null;
        }
        return new DefaultAppInfo(this.mContext, this.mPm, UserHandle.myUserId(), cn).getKey();
    }

    private boolean isEnabledApp(String packagename) {
        ApplicationInfo appInfo;
        boolean isExist = false;
        try {
            appInfo = SettingsBaseApplication.mApplication.getPackageManager().getApplicationInfoAsUser(packagename, 0, UserHandle.myUserId());
        } catch (NameNotFoundException e1) {
            appInfo = null;
            e1.printStackTrace();
        }
        if (appInfo != null) {
            return true;
        }
        return isExist;
    }

    public void onResume() {
        if (isAvailable()) {
            List<DefaultAppInfo> candidates = getCandidates();
            boolean z = true;
            MasterSwitchPreference masterSwitchPreference;
            if (candidates.size() > 0 && getDefaultKey() == null) {
                masterSwitchPreference = this.mSwitch;
                if (System.getInt(this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0) != 1) {
                    z = false;
                }
                masterSwitchPreference.setChecked(z);
            } else if (candidates.size() == 0 || getDefaultKey() == null) {
                this.mSwitch.setChecked(false);
                System.putInt(this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0);
            } else if (isEnabledApp(getDefaultKey().split("/")[0])) {
                masterSwitchPreference = this.mSwitch;
                if (System.getInt(this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0) != 1) {
                    z = false;
                }
                masterSwitchPreference.setChecked(z);
            } else {
                this.mSwitch.setChecked(false);
                System.putInt(this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0);
            }
            if (this.mSwitch != null) {
                this.mSwitchController = new MasterSwitchController(this.mSwitch);
                this.mSwitchController.setListener(this);
                this.mSwitchController.startListening();
            }
        }
    }

    private void noAssistantAppDialog() {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    System.putInt(OPAssistantAPPSwitchPreferenceController.this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0);
                    OPUtils.sendAppTracker("quick_turn_on_voice_assistant", "off");
                }
            }
        };
        new Builder(this.mContext).setMessage(R.string.oneplus_no_assistant_app_dialog).setPositiveButton(17039370, onClickListener).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                MasterSwitchPreference access$200 = OPAssistantAPPSwitchPreferenceController.this.mSwitch;
                boolean z = false;
                if (System.getInt(OPAssistantAPPSwitchPreferenceController.this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0) != 0) {
                    z = true;
                }
                access$200.setChecked(z);
            }
        }).create().show();
    }

    private void toSelectAssistantAppDialog() {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    Intent intent = new Intent();
                    intent.setAction("com.oneplus.intent.DefaultAssistPicker");
                    intent.putExtra("assistntapp", "switch");
                    OPAssistantAPPSwitchPreferenceController.this.mContext.startActivity(intent);
                } else if (which == -2) {
                    System.putInt(OPAssistantAPPSwitchPreferenceController.this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0);
                    OPUtils.sendAppTracker("quick_turn_on_voice_assistant", "off");
                }
            }
        };
        new Builder(this.mContext).setMessage(R.string.oneplus_to_select_assistant_app_dialog).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                MasterSwitchPreference access$200 = OPAssistantAPPSwitchPreferenceController.this.mSwitch;
                boolean z = false;
                if (System.getInt(OPAssistantAPPSwitchPreferenceController.this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", 0) != 0) {
                    z = true;
                }
                access$200.setChecked(z);
            }
        }).create().show();
    }

    private void addAssistServices() {
        PackageManager pm = this.mPm.getPackageManager();
        for (ResolveInfo resolveInfo : pm.queryIntentServices(ASSIST_SERVICE_PROBE, 128)) {
            VoiceInteractionServiceInfo voiceInteractionServiceInfo = new VoiceInteractionServiceInfo(pm, resolveInfo.serviceInfo);
            if (voiceInteractionServiceInfo.getSupportsAssist()) {
                this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name), voiceInteractionServiceInfo));
            }
        }
    }

    private void addAssistActivities() {
        for (ResolveInfo resolveInfo : this.mPm.getPackageManager().queryIntentActivities(ASSIST_ACTIVITY_PROBE, 65536)) {
            this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)));
        }
    }

    private List<DefaultAppInfo> getCandidates() {
        this.mAvailableAssistants.clear();
        addAssistServices();
        addAssistActivities();
        List<String> packages = new ArrayList();
        List<DefaultAppInfo> candidates = new ArrayList();
        for (Info info : this.mAvailableAssistants) {
            String packageName = info.component.getPackageName();
            if (!packages.contains(packageName)) {
                packages.add(packageName);
                candidates.add(new DefaultAppInfo(this.mContext, this.mPm, UserHandle.myUserId(), info.component));
            }
        }
        return candidates;
    }

    public boolean onSwitchToggled(boolean isChecked) {
        List<DefaultAppInfo> candidates = getCandidates();
        if (candidates.size() == 0) {
            noAssistantAppDialog();
        } else if (candidates.size() > 0) {
            if (getDefaultKey() != null) {
                System.putInt(this.mContext.getContentResolver(), "quick_turn_on_voice_assistant", isChecked);
                OPUtils.sendAppTracker("quick_turn_on_voice_assistant", isChecked ? "on" : "off");
            } else {
                toSelectAssistantAppDialog();
            }
        }
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_QUICK_TURN_ON_ASSISTANT_APP.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent();
        intent.setAction("com.oneplus.intent.DefaultAssistPicker");
        intent.putExtra("assistntapp", "onClick");
        this.mContext.startActivity(intent);
        return true;
    }
}
