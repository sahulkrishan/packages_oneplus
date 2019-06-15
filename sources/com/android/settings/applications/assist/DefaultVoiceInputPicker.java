package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.internal.app.AssistUtils;
import com.android.settings.R;
import com.android.settings.applications.assist.VoiceInputHelper.BaseInfo;
import com.android.settings.applications.assist.VoiceInputHelper.InteractionInfo;
import com.android.settings.applications.assist.VoiceInputHelper.RecognizerInfo;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultVoiceInputPicker extends DefaultAppPickerFragment {
    private String mAssistRestrict;
    private AssistUtils mAssistUtils;
    private VoiceInputHelper mHelper;

    public static class VoiceInputDefaultAppInfo extends DefaultAppInfo {
        public BaseInfo mInfo;

        public VoiceInputDefaultAppInfo(Context context, PackageManagerWrapper pm, int userId, BaseInfo info, boolean enabled) {
            super(context, pm, userId, info.componentName, null, enabled);
            this.mInfo = info;
        }

        public String getKey() {
            return this.mInfo.key;
        }

        public CharSequence loadLabel() {
            if (this.mInfo instanceof InteractionInfo) {
                return this.mInfo.appLabel;
            }
            return this.mInfo.label;
        }

        public Intent getSettingIntent() {
            if (this.mInfo.settings == null) {
                return null;
            }
            return new Intent("android.intent.action.MAIN").setComponent(this.mInfo.settings);
        }
    }

    public int getMetricsCategory() {
        return 844;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mAssistUtils = new AssistUtils(context);
        this.mHelper = new VoiceInputHelper(context);
        this.mHelper.buildUi();
        ComponentName assist = getCurrentAssist();
        if (isCurrentAssistVoiceService(assist, getCurrentService(this.mHelper))) {
            this.mAssistRestrict = assist.flattenToShortString();
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_voice_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<VoiceInputDefaultAppInfo> getCandidates() {
        boolean enabled;
        List<VoiceInputDefaultAppInfo> candidates = new ArrayList();
        Context context = getContext();
        Iterator it = this.mHelper.mAvailableInteractionInfos.iterator();
        boolean hasEnabled = true;
        while (it.hasNext()) {
            BaseInfo info = (InteractionInfo) it.next();
            enabled = TextUtils.equals(info.key, this.mAssistRestrict);
            hasEnabled |= enabled;
            candidates.add(new VoiceInputDefaultAppInfo(context, this.mPm, this.mUserId, info, enabled));
        }
        enabled = !hasEnabled;
        Iterator it2 = this.mHelper.mAvailableRecognizerInfos.iterator();
        while (it2.hasNext()) {
            candidates.add(new VoiceInputDefaultAppInfo(context, this.mPm, this.mUserId, (RecognizerInfo) it2.next(), !enabled));
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        ComponentName currentService = getCurrentService(this.mHelper);
        if (currentService == null) {
            return null;
        }
        return currentService.flattenToShortString();
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String value) {
        Iterator it = this.mHelper.mAvailableInteractionInfos.iterator();
        while (it.hasNext()) {
            InteractionInfo info = (InteractionInfo) it.next();
            if (TextUtils.equals(value, info.key)) {
                Secure.putString(getContext().getContentResolver(), "voice_interaction_service", value);
                Secure.putString(getContext().getContentResolver(), "voice_recognition_service", new ComponentName(info.service.packageName, info.serviceInfo.getRecognitionService()).flattenToShortString());
                return true;
            }
        }
        it = this.mHelper.mAvailableRecognizerInfos.iterator();
        while (it.hasNext()) {
            if (TextUtils.equals(value, ((RecognizerInfo) it.next()).key)) {
                Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
                Secure.putString(getContext().getContentResolver(), "voice_recognition_service", value);
                return true;
            }
        }
        return true;
    }

    public static ComponentName getCurrentService(VoiceInputHelper helper) {
        if (helper.mCurrentVoiceInteraction != null) {
            return helper.mCurrentVoiceInteraction;
        }
        if (helper.mCurrentRecognizer != null) {
            return helper.mCurrentRecognizer;
        }
        return null;
    }

    private ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(this.mUserId);
    }

    public static boolean isCurrentAssistVoiceService(ComponentName currentAssist, ComponentName currentVoiceService) {
        return (currentAssist == null && currentVoiceService == null) || (currentAssist != null && currentAssist.equals(currentVoiceService));
    }
}
