package com.android.settings.development;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class StrictModePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final String STRICT_MODE_DISABLED = "";
    @VisibleForTesting
    static final String STRICT_MODE_ENABLED = "1";
    private static final String STRICT_MODE_KEY = "strict_mode";
    private static final String WINDOW_MANAGER_KEY = "window";
    private final IWindowManager mWindowManager = Stub.asInterface(ServiceManager.getService(WINDOW_MANAGER_KEY));

    public StrictModePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return STRICT_MODE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeStrictModeVisualOptions(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(isStrictModeEnabled());
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeStrictModeVisualOptions(false);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    private boolean isStrictModeEnabled() {
        return SystemProperties.getBoolean("persist.sys.strictmode.visual", false);
    }

    private void writeStrictModeVisualOptions(boolean isEnabled) {
        try {
            this.mWindowManager.setStrictModeVisualIndicatorPreference(isEnabled ? STRICT_MODE_ENABLED : "");
        } catch (RemoteException e) {
        }
    }
}
