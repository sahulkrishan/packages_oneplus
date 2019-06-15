package com.android.settings.widget;

import android.widget.Switch;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class SwitchBarController extends SwitchWidgetController implements OnSwitchChangeListener {
    private final SwitchBar mSwitchBar;

    public SwitchBarController(SwitchBar switchBar) {
        this.mSwitchBar = switchBar;
    }

    public void setupView() {
        this.mSwitchBar.show();
    }

    public void teardownView() {
        this.mSwitchBar.hide();
    }

    public void updateTitle(boolean isChecked) {
        this.mSwitchBar.setTextViewLabelAndBackground(isChecked);
    }

    public void startListening() {
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    public void stopListening() {
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    public void setChecked(boolean checked) {
        this.mSwitchBar.setChecked(checked);
    }

    public boolean isChecked() {
        return this.mSwitchBar.isChecked();
    }

    public void setEnabled(boolean enabled) {
        this.mSwitchBar.setEnabled(enabled);
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (this.mListener != null) {
            this.mListener.onSwitchToggled(isChecked);
        }
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        this.mSwitchBar.setDisabledByAdmin(admin);
    }
}
