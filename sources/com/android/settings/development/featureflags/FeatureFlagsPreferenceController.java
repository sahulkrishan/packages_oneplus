package com.android.settings.development.featureflags;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import android.util.FeatureFlagUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import java.util.Map;

public class FeatureFlagsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart {
    private PreferenceScreen mScreen;

    public FeatureFlagsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mScreen = screen;
    }

    public void onStart() {
        if (this.mScreen != null) {
            Map<String, String> featureMap = FeatureFlagUtils.getAllFeatureFlags();
            if (featureMap != null) {
                this.mScreen.removeAll();
                Context prefContext = this.mScreen.getContext();
                for (String feature : featureMap.keySet()) {
                    this.mScreen.addPreference(new FeatureFlagPreference(prefContext, feature));
                }
            }
        }
    }
}
