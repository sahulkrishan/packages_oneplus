package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.TwoTargetPreference;
import com.oneplus.lib.widget.button.OPSwitch;

public class MasterSwitchPreference extends TwoTargetPreference {
    private boolean mChecked;
    private boolean mEnableSwitch = true;
    private OPSwitch mSwitch;

    public MasterSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.op_preference_two_target);
    }

    public MasterSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.op_preference_two_target);
    }

    public MasterSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.op_preference_two_target);
    }

    public MasterSwitchPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.op_preference_two_target);
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_master_switch;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View widgetView = holder.findViewById(16908312);
        if (widgetView != null) {
            widgetView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (MasterSwitchPreference.this.mSwitch == null || MasterSwitchPreference.this.mSwitch.isEnabled()) {
                        MasterSwitchPreference.this.setChecked(MasterSwitchPreference.this.mChecked ^ 1);
                        if (MasterSwitchPreference.this.callChangeListener(Boolean.valueOf(MasterSwitchPreference.this.mChecked))) {
                            MasterSwitchPreference.this.persistBoolean(MasterSwitchPreference.this.mChecked);
                        } else {
                            MasterSwitchPreference.this.setChecked(MasterSwitchPreference.this.mChecked ^ 1);
                        }
                    }
                }
            });
        }
        this.mSwitch = (OPSwitch) holder.findViewById(R.id.switchWidget);
        if (this.mSwitch != null) {
            this.mSwitch.setContentDescription(getTitle());
            this.mSwitch.setChecked(this.mChecked);
            this.mSwitch.setEnabled(this.mEnableSwitch);
        }
    }

    public boolean isChecked() {
        return this.mSwitch != null && this.mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (this.mSwitch != null) {
            this.mSwitch.setChecked(checked);
        }
    }

    public void setSwitchEnabled(boolean enabled) {
        this.mEnableSwitch = enabled;
        if (this.mSwitch != null) {
            this.mSwitch.setEnabled(enabled);
        }
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        setSwitchEnabled(admin == null);
    }

    public OPSwitch getSwitch() {
        return this.mSwitch;
    }
}
