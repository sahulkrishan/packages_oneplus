package com.android.settings.development;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class WindowAnimationScalePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final float DEFAULT_VALUE = 1.0f;
    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
    @VisibleForTesting
    static final int WINDOW_ANIMATION_SCALE_SELECTOR = 0;
    private final String[] mListSummaries;
    private final String[] mListValues;
    private final IWindowManager mWindowManager = Stub.asInterface(ServiceManager.getService("window"));

    public WindowAnimationScalePreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.window_animation_scale_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.window_animation_scale_entries);
    }

    public String getPreferenceKey() {
        return WINDOW_ANIMATION_SCALE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeAnimationScaleOption(newValue);
        return true;
    }

    public void updateState(Preference preference) {
        updateAnimationScaleValue();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeAnimationScaleOption(null);
    }

    private void writeAnimationScaleOption(Object newValue) {
        float scale;
        if (newValue != null) {
            try {
                scale = Float.parseFloat(newValue.toString());
            } catch (RemoteException e) {
                return;
            }
        }
        scale = 1.0f;
        this.mWindowManager.setAnimationScale(0, scale);
        updateAnimationScaleValue();
    }

    private void updateAnimationScaleValue() {
        try {
            int i = 0;
            float scale = this.mWindowManager.getAnimationScale(0);
            int index = 0;
            while (i < this.mListValues.length) {
                if (scale <= Float.parseFloat(this.mListValues[i])) {
                    index = i;
                    break;
                }
                i++;
            }
            ListPreference listPreference = this.mPreference;
            listPreference.setValue(this.mListValues[index]);
            listPreference.setSummary(this.mListSummaries[index]);
        } catch (RemoteException e) {
        }
    }
}
