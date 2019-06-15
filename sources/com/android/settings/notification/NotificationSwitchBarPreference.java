package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.ToggleSwitch;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class NotificationSwitchBarPreference extends LayoutPreference {
    private boolean mChecked;
    private boolean mEnableSwitch = true;
    private ToggleSwitch mSwitch;

    public NotificationSwitchBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mSwitch = (ToggleSwitch) holder.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        if (this.mSwitch != null) {
            this.mSwitch.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NotificationSwitchBarPreference.this.mSwitch.isEnabled()) {
                        NotificationSwitchBarPreference.this.mChecked = NotificationSwitchBarPreference.this.mChecked ^ 1;
                        NotificationSwitchBarPreference.this.setChecked(NotificationSwitchBarPreference.this.mChecked);
                        if (!NotificationSwitchBarPreference.this.callChangeListener(Boolean.valueOf(NotificationSwitchBarPreference.this.mChecked))) {
                            NotificationSwitchBarPreference.this.setChecked(NotificationSwitchBarPreference.this.mChecked ^ 1);
                        }
                    }
                }
            });
            this.mSwitch.setChecked(this.mChecked);
            this.mSwitch.setEnabled(this.mEnableSwitch);
        }
    }

    public boolean isChecked() {
        return this.mSwitch != null && this.mSwitch.isEnabled() && this.mChecked;
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
}
