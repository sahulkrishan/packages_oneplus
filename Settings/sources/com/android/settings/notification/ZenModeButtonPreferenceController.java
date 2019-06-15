package com.android.settings.notification;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeButtonPreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    protected static final String KEY = "zen_mode_settings_button_container";
    private static final String TAG = "EnableZenModeButton";
    private FragmentManager mFragment;
    private Button mZenButtonOff;
    private Button mZenButtonOn;

    public ZenModeButtonPreferenceController(Context context, Lifecycle lifecycle, FragmentManager fragment) {
        super(context, KEY, lifecycle);
        this.mFragment = fragment;
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (this.mZenButtonOn == null) {
            this.mZenButtonOn = (Button) ((LayoutPreference) preference).findViewById(R.id.zen_mode_settings_turn_on_button);
            updateZenButtonOnClickListener();
        }
        if (this.mZenButtonOff == null) {
            this.mZenButtonOff = (Button) ((LayoutPreference) preference).findViewById(R.id.zen_mode_settings_turn_off_button);
            this.mZenButtonOff.setOnClickListener(new -$$Lambda$ZenModeButtonPreferenceController$RnfY8k3LZN005jbH9s0d6akYfFk(this));
        }
        updateButtons();
    }

    public static /* synthetic */ void lambda$updateState$0(ZenModeButtonPreferenceController zenModeButtonPreferenceController, View v) {
        zenModeButtonPreferenceController.mMetricsFeatureProvider.action(zenModeButtonPreferenceController.mContext, 1268, false);
        zenModeButtonPreferenceController.mBackend.setZenMode(0);
    }

    private void updateButtons() {
        switch (getZenMode()) {
            case 1:
            case 2:
            case 3:
                this.mZenButtonOff.setVisibility(0);
                this.mZenButtonOn.setVisibility(8);
                return;
            default:
                this.mZenButtonOff.setVisibility(8);
                updateZenButtonOnClickListener();
                this.mZenButtonOn.setVisibility(0);
                return;
        }
    }

    private void updateZenButtonOnClickListener() {
        int zenDuration = getZenDuration();
        switch (zenDuration) {
            case -1:
                this.mZenButtonOn.setOnClickListener(new -$$Lambda$ZenModeButtonPreferenceController$KAk_Mj51Obvq4mW4RobrcR4_CRM(this));
                return;
            case 0:
                this.mZenButtonOn.setOnClickListener(new -$$Lambda$ZenModeButtonPreferenceController$16-xvFNOTseGHNtlUJrmr4Oa8o8(this));
                return;
            default:
                this.mZenButtonOn.setOnClickListener(new -$$Lambda$ZenModeButtonPreferenceController$NQfCfaUFz6J6tbPXZDP09CGnoAo(this, zenDuration));
                return;
        }
    }

    public static /* synthetic */ void lambda$updateZenButtonOnClickListener$1(ZenModeButtonPreferenceController zenModeButtonPreferenceController, View v) {
        zenModeButtonPreferenceController.mMetricsFeatureProvider.action(zenModeButtonPreferenceController.mContext, 1268, false);
        new SettingsEnableZenModeDialog().show(zenModeButtonPreferenceController.mFragment, TAG);
    }

    public static /* synthetic */ void lambda$updateZenButtonOnClickListener$2(ZenModeButtonPreferenceController zenModeButtonPreferenceController, View v) {
        zenModeButtonPreferenceController.mMetricsFeatureProvider.action(zenModeButtonPreferenceController.mContext, 1268, false);
        zenModeButtonPreferenceController.mBackend.setZenMode(1);
    }

    public static /* synthetic */ void lambda$updateZenButtonOnClickListener$3(ZenModeButtonPreferenceController zenModeButtonPreferenceController, int zenDuration, View v) {
        zenModeButtonPreferenceController.mMetricsFeatureProvider.action(zenModeButtonPreferenceController.mContext, 1268, false);
        zenModeButtonPreferenceController.mBackend.setZenModeForDuration(zenDuration);
    }
}
