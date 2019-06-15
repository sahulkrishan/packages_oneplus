package com.android.settings.applications.assist;

import android.content.Context;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.internal.app.AssistUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.Arrays;
import java.util.List;

public class AssistScreenshotPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_SCREENSHOT = "screenshot";
    private final AssistUtils mAssistUtils;
    private Preference mPreference;
    private PreferenceScreen mScreen;
    private final SettingObserver mSettingObserver = new SettingObserver();

    class SettingObserver extends AssistSettingObserver {
        private final Uri CONTEXT_URI = Secure.getUriFor("assist_structure_enabled");
        private final Uri URI = Secure.getUriFor("assist_screenshot_enabled");

        SettingObserver() {
        }

        /* Access modifiers changed, original: protected */
        public List<Uri> getSettingUris() {
            return Arrays.asList(new Uri[]{this.URI, this.CONTEXT_URI});
        }

        public void onSettingChange() {
            AssistScreenshotPreferenceController.this.updatePreference();
        }
    }

    public AssistScreenshotPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mAssistUtils = new AssistUtils(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId()) != null;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mScreen = screen;
        this.mPreference = screen.findPreference(getPreferenceKey());
        super.displayPreference(screen);
    }

    public String getPreferenceKey() {
        return KEY_SCREENSHOT;
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
        Secure.putInt(this.mContext.getContentResolver(), "assist_screenshot_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }

    private void updatePreference() {
        if (this.mPreference != null && (this.mPreference instanceof TwoStatePreference)) {
            if (!isAvailable()) {
                this.mScreen.removePreference(this.mPreference);
            } else if (this.mScreen.findPreference(getPreferenceKey()) == null) {
                this.mScreen.addPreference(this.mPreference);
            }
            boolean contextChecked = false;
            ((TwoStatePreference) this.mPreference).setChecked(Secure.getInt(this.mContext.getContentResolver(), "assist_screenshot_enabled", 1) != 0);
            if (Secure.getInt(this.mContext.getContentResolver(), "assist_structure_enabled", 1) != 0) {
                contextChecked = true;
            }
            this.mPreference.setEnabled(contextChecked);
        }
    }
}
