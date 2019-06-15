package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.voice.VoiceInteractionServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.AssistUtils;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.widget.CandidateInfo;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class DefaultAssistPicker extends DefaultAppPickerFragment {
    private static final Intent ASSIST_ACTIVITY_PROBE = new Intent("android.intent.action.ASSIST");
    private static final Intent ASSIST_SERVICE_PROBE = new Intent("android.service.voice.VoiceInteractionService");
    private static final String TAG = "DefaultAssistPicker";
    private AssistUtils mAssistUtils;
    private final List<Info> mAvailableAssistants = new ArrayList();

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

    public int getMetricsCategory() {
        return 843;
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldShowItemNone() {
        return true;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mAssistUtils = new AssistUtils(context);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_assist_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        this.mAvailableAssistants.clear();
        addAssistServices();
        addAssistActivities();
        List<String> packages = new ArrayList();
        List<DefaultAppInfo> candidates = new ArrayList();
        for (Info info : this.mAvailableAssistants) {
            String packageName = info.component.getPackageName();
            if (!packages.contains(packageName)) {
                packages.add(packageName);
                candidates.add(new DefaultAppInfo(getContext(), this.mPm, this.mUserId, info.component));
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        ComponentName cn = getCurrentAssist();
        if (cn != null) {
            return new DefaultAppInfo(getContext(), this.mPm, this.mUserId, cn).getKey();
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public String getConfirmationMessage(CandidateInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        return getContext().getString(R.string.assistant_security_warning, new Object[]{appInfo.loadLabel()});
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        String flag = getActivity().getIntent().getExtras().getString("assistntapp");
        if (TextUtils.isEmpty(key)) {
            setAssistNone();
            if ("switch".equals(flag) || "onClick".equals(flag)) {
                System.putInt(getContext().getContentResolver(), "quick_turn_on_voice_assistant", 0);
                OPUtils.sendAppTracker("quick_turn_on_voice_assistant", "off");
            }
            return true;
        }
        Info info = findAssistantByPackageName(ComponentName.unflattenFromString(key).getPackageName());
        if (info == null) {
            setAssistNone();
            if ("switch".equals(flag) || "onClick".equals(flag)) {
                System.putInt(getContext().getContentResolver(), "quick_turn_on_voice_assistant", 0);
                OPUtils.sendAppTracker("quick_turn_on_voice_assistant", "off");
            }
            return true;
        }
        if (info.isVoiceInteractionService()) {
            setAssistService(info);
        } else {
            setAssistActivity(info);
        }
        if ("switch".equals(flag)) {
            System.putInt(getContext().getContentResolver(), "quick_turn_on_voice_assistant", 1);
            OPUtils.sendAppTracker("quick_turn_on_voice_assistant", "on");
        }
        return true;
    }

    public ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(this.mUserId);
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

    private Info findAssistantByPackageName(String packageName) {
        for (Info info : this.mAvailableAssistants) {
            if (TextUtils.equals(info.component.getPackageName(), packageName)) {
                return info;
            }
        }
        return null;
    }

    private void setAssistNone() {
        Secure.putString(getContext().getContentResolver(), "assistant", "");
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
    }

    private void setAssistService(Info serviceInfo) {
        String serviceComponentName = serviceInfo.component.flattenToShortString();
        String serviceRecognizerName = new ComponentName(serviceInfo.component.getPackageName(), serviceInfo.voiceInteractionServiceInfo.getRecognitionService()).flattenToShortString();
        Secure.putString(getContext().getContentResolver(), "assistant", serviceComponentName);
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", serviceComponentName);
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", serviceRecognizerName);
    }

    private void setAssistActivity(Info activityInfo) {
        Secure.putString(getContext().getContentResolver(), "assistant", activityInfo.component.flattenToShortString());
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
    }

    private String getDefaultRecognizer() {
        ResolveInfo resolveInfo = this.mPm.getPackageManager().resolveService(new Intent("android.speech.RecognitionService"), 128);
        if (resolveInfo != null && resolveInfo.serviceInfo != null) {
            return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name).flattenToShortString();
        }
        Log.w(TAG, "Unable to resolve default voice recognition service.");
        return "";
    }
}
