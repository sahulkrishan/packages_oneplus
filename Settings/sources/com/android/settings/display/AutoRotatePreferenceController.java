package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AutoRotatePreferenceController extends TogglePreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private Preference mPreference;
    private RotationPolicyListener mRotationPolicyListener;

    public AutoRotatePreferenceController(Context context, String key) {
        super(context, key);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public void updateState(Preference preference) {
        this.mPreference = preference;
        super.updateState(preference);
    }

    public void onResume() {
        if (this.mRotationPolicyListener == null) {
            this.mRotationPolicyListener = new RotationPolicyListener() {
                public void onChange() {
                    if (AutoRotatePreferenceController.this.mPreference != null) {
                        AutoRotatePreferenceController.this.updateState(AutoRotatePreferenceController.this.mPreference);
                    }
                }
            };
        }
        RotationPolicy.registerRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
    }

    public void onPause() {
        if (this.mRotationPolicyListener != null) {
            RotationPolicy.unregisterRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
        }
    }

    public int getAvailabilityStatus() {
        return RotationPolicy.isRotationLockToggleVisible(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "auto_rotate");
    }

    public boolean isChecked() {
        return RotationPolicy.isRotationLocked(this.mContext) ^ 1;
    }

    public boolean setChecked(boolean isChecked) {
        boolean isLocked = isChecked ^ 1;
        this.mMetricsFeatureProvider.action(this.mContext, 203, isLocked);
        RotationPolicy.setRotationLock(this.mContext, isLocked);
        return true;
    }
}
