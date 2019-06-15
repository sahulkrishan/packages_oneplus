package com.android.settings.dream;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.GearPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;
import java.util.Optional;

public class CurrentDreamPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String CURRENT_SCREENSAVER = "current_screensaver";
    private static final String TAG = "CurrentDreamPreferenceController";
    private final DreamBackend mBackend;

    public CurrentDreamPreferenceController(Context context) {
        super(context);
        this.mBackend = DreamBackend.getInstance(context);
    }

    public boolean isAvailable() {
        return this.mBackend.getDreamInfos().size() > 0;
    }

    public String getPreferenceKey() {
        return CURRENT_SCREENSAVER;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(this.mBackend.getActiveDreamName());
        setGearClickListenerForPreference(preference);
    }

    private void setGearClickListenerForPreference(Preference preference) {
        if (preference instanceof GearPreference) {
            GearPreference gearPreference = (GearPreference) preference;
            Optional<DreamInfo> info = getActiveDreamInfo();
            if (!info.isPresent() || ((DreamInfo) info.get()).settingsComponentName == null) {
                gearPreference.setOnGearClickListener(null);
            } else {
                gearPreference.setOnGearClickListener(new -$$Lambda$CurrentDreamPreferenceController$faOOwvjkeM0i38i1bxACLza6vQ4(this));
            }
        }
    }

    private void launchScreenSaverSettings() {
        Optional<DreamInfo> info = getActiveDreamInfo();
        if (info.isPresent()) {
            this.mBackend.launchSettings((DreamInfo) info.get());
        }
    }

    private Optional<DreamInfo> getActiveDreamInfo() {
        return this.mBackend.getDreamInfos().stream().filter(-$$Lambda$CurrentDreamPreferenceController$JJd0D4Ql1FstWgOpYrMCLEB2pnU.INSTANCE).findFirst();
    }
}
