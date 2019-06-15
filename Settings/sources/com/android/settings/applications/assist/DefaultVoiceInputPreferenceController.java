package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.app.AssistUtils;
import com.android.settings.applications.assist.DefaultVoiceInputPicker.VoiceInputDefaultAppInfo;
import com.android.settings.applications.assist.VoiceInputHelper.BaseInfo;
import com.android.settings.applications.assist.VoiceInputHelper.InteractionInfo;
import com.android.settings.applications.assist.VoiceInputHelper.RecognizerInfo;
import com.android.settings.applications.defaultapps.DefaultAppPreferenceController;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.Iterator;
import java.util.List;

public class DefaultVoiceInputPreferenceController extends DefaultAppPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_VOICE_INPUT = "voice_input_settings";
    private AssistUtils mAssistUtils;
    private VoiceInputHelper mHelper;
    private Preference mPreference;
    private PreferenceScreen mScreen;
    private SettingObserver mSettingObserver = new SettingObserver();

    class SettingObserver extends AssistSettingObserver {
        SettingObserver() {
        }

        /* Access modifiers changed, original: protected */
        public List<Uri> getSettingUris() {
            return null;
        }

        public void onSettingChange() {
            DefaultVoiceInputPreferenceController.this.updatePreference();
        }
    }

    public DefaultVoiceInputPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mAssistUtils = new AssistUtils(context);
        this.mHelper = new VoiceInputHelper(context);
        this.mHelper.buildUi();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return DefaultVoiceInputPicker.isCurrentAssistVoiceService(this.mAssistUtils.getAssistComponentForUser(this.mUserId), DefaultVoiceInputPicker.getCurrentService(this.mHelper)) ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_VOICE_INPUT;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mScreen = screen;
        this.mPreference = screen.findPreference(getPreferenceKey());
        super.displayPreference(screen);
    }

    public void onResume() {
        this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        updatePreference();
    }

    public void updateState(Preference preference) {
        super.updateState(this.mPreference);
        updatePreference();
    }

    public void onPause() {
        this.mSettingObserver.register(this.mContext.getContentResolver(), false);
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        String defaultKey = getDefaultAppKey();
        if (defaultKey == null) {
            return null;
        }
        BaseInfo info;
        Iterator it = this.mHelper.mAvailableInteractionInfos.iterator();
        while (it.hasNext()) {
            info = (InteractionInfo) it.next();
            if (TextUtils.equals(defaultKey, info.key)) {
                return new VoiceInputDefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, info, true);
            }
        }
        it = this.mHelper.mAvailableRecognizerInfos.iterator();
        while (it.hasNext()) {
            info = (RecognizerInfo) it.next();
            if (TextUtils.equals(defaultKey, info.key)) {
                return new VoiceInputDefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, info, true);
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Intent getSettingIntent(DefaultAppInfo info) {
        DefaultAppInfo appInfo = getDefaultAppInfo();
        if (appInfo == null || !(appInfo instanceof VoiceInputDefaultAppInfo)) {
            return null;
        }
        return ((VoiceInputDefaultAppInfo) appInfo).getSettingIntent();
    }

    private void updatePreference() {
        if (this.mPreference != null) {
            this.mHelper.buildUi();
            if (!isAvailable()) {
                this.mScreen.removePreference(this.mPreference);
            } else if (this.mScreen.findPreference(getPreferenceKey()) == null) {
                this.mScreen.addPreference(this.mPreference);
            }
        }
    }

    private String getDefaultAppKey() {
        ComponentName currentService = DefaultVoiceInputPicker.getCurrentService(this.mHelper);
        if (currentService == null) {
            return null;
        }
        return currentService.flattenToShortString();
    }
}
