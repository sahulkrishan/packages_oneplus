package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.Arrays;
import java.util.List;

public class AssistFlashScreenPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_FLASH = "flash";
    private final AssistUtils mAssistUtils;
    private Preference mPreference;
    private PreferenceScreen mScreen;
    private final SettingObserver mSettingObserver = new SettingObserver();

    class SettingObserver extends AssistSettingObserver {
        private final Uri CONTEXT_URI = Secure.getUriFor("assist_structure_enabled");
        private final Uri URI = Secure.getUriFor("assist_disclosure_enabled");

        SettingObserver() {
        }

        /* Access modifiers changed, original: protected */
        public List<Uri> getSettingUris() {
            return Arrays.asList(new Uri[]{this.URI, this.CONTEXT_URI});
        }

        public void onSettingChange() {
            AssistFlashScreenPreferenceController.this.updatePreference();
        }
    }

    public AssistFlashScreenPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mAssistUtils = new AssistUtils(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return getCurrentAssist() != null && allowDisablingAssistDisclosure();
    }

    public String getPreferenceKey() {
        return KEY_FLASH;
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
        updatePreference();
    }

    public void onPause() {
        this.mSettingObserver.register(this.mContext.getContentResolver(), false);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "assist_disclosure_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }

    private void updatePreference() {
        if (this.mPreference != null && (this.mPreference instanceof TwoStatePreference)) {
            if (!isAvailable()) {
                this.mScreen.removePreference(this.mPreference);
            } else if (this.mScreen.findPreference(getPreferenceKey()) == null) {
                this.mScreen.addPreference(this.mPreference);
            }
            ComponentName assistant = getCurrentAssist();
            boolean isContextChecked = AssistContextPreferenceController.isChecked(this.mContext);
            Preference preference = this.mPreference;
            boolean z = isContextChecked && isPreInstalledAssistant(assistant);
            preference.setEnabled(z);
            ((TwoStatePreference) this.mPreference).setChecked(willShowFlash(assistant));
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean willShowFlash(ComponentName assistant) {
        return AssistUtils.shouldDisclose(this.mContext, assistant);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isPreInstalledAssistant(ComponentName assistant) {
        return AssistUtils.isPreinstalledAssistant(this.mContext, assistant);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean allowDisablingAssistDisclosure() {
        return AssistUtils.allowDisablingAssistDisclosure(this.mContext);
    }

    private ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
    }
}
