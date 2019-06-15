package com.android.settings.dream;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.dream.DreamBackend;

public class StartNowPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String PREF_KEY = "dream_start_now_button_container";
    private static final String TAG = "StartNowPreferenceController";
    private final DreamBackend mBackend;

    public StartNowPreferenceController(Context context) {
        super(context);
        this.mBackend = DreamBackend.getInstance(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ((Button) ((LayoutPreference) screen.findPreference(getPreferenceKey())).findViewById(R.id.dream_start_now_button)).setOnClickListener(new -$$Lambda$StartNowPreferenceController$bNNILqA5JAxzjWV5EYdSnVpdHoI(this));
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ((Button) ((LayoutPreference) preference).findViewById(R.id.dream_start_now_button)).setEnabled(this.mBackend.getWhenToDreamSetting() != 3);
    }
}
