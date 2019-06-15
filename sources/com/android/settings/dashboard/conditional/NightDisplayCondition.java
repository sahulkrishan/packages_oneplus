package com.android.settings.dashboard.conditional;

import android.graphics.drawable.Drawable;
import android.provider.Settings.Secure;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.oneplus.settings.better.OPNightMode;

public final class NightDisplayCondition extends Condition implements Callback {
    private ColorDisplayController mController;

    NightDisplayCondition(ConditionManager manager) {
        super(manager);
        this.mController = new ColorDisplayController(manager.getContext());
        this.mController.setListener(this);
    }

    public int getMetricsConstant() {
        return 492;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_settings_night_display);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.oneplus_condition_night_mode_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_night_display_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        new SubSettingLauncher(this.mManager.getContext()).setDestination(OPNightMode.class.getName()).setSourceMetricsCategory(35).setTitle((int) R.string.night_display_title).addFlags(268435456).launch();
    }

    public void onActionClick(int index) {
        if (index == 0) {
            this.mController.setActivated(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void refreshState() {
        boolean z = false;
        boolean isDisplayDaltonizeEnabled = Secure.getInt(this.mManager.getContext().getContentResolver(), "accessibility_display_daltonizer_enabled", 12) == 1;
        boolean isDisplayInversionEnabled = Secure.getInt(this.mManager.getContext().getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1;
        if (!(!this.mController.isActivated() || isDisplayDaltonizeEnabled || isDisplayInversionEnabled)) {
            z = true;
        }
        setActive(z);
    }

    public void onActivated(boolean activated) {
        refreshState();
    }
}
