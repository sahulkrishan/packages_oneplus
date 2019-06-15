package com.android.settings.notification;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.VibratorSceneUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.lib.widget.button.OPSwitch;
import com.oneplus.settings.utils.OPUtils;

public class NotificationAppPreference extends MasterSwitchPreference {
    private boolean mChecked;
    private boolean mEnableSwitch = true;
    private OPSwitch mSwitch;

    public NotificationAppPreference(Context context) {
        super(context);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    public NotificationAppPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    public NotificationAppPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    public NotificationAppPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_master_switch;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View widgetView = view.findViewById(16908312);
        if (widgetView != null) {
            widgetView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NotificationAppPreference.this.mSwitch == null || NotificationAppPreference.this.mSwitch.isEnabled()) {
                        if (VibratorSceneUtils.systemVibrateEnabled(NotificationAppPreference.this.getContext())) {
                            NotificationAppPreference.this.mVibratePattern = VibratorSceneUtils.getVibratorScenePattern(NotificationAppPreference.this.getContext(), NotificationAppPreference.this.mVibrator, 1003);
                            VibratorSceneUtils.vibrateIfNeeded(NotificationAppPreference.this.mVibratePattern, NotificationAppPreference.this.mVibrator);
                        }
                        NotificationAppPreference.this.setChecked(NotificationAppPreference.this.mChecked ^ 1);
                        if (NotificationAppPreference.this.callChangeListener(Boolean.valueOf(NotificationAppPreference.this.mChecked))) {
                            NotificationAppPreference.this.persistBoolean(NotificationAppPreference.this.mChecked);
                        } else {
                            NotificationAppPreference.this.setChecked(NotificationAppPreference.this.mChecked ^ 1);
                        }
                    }
                }
            });
        }
        this.mSwitch = (OPSwitch) view.findViewById(R.id.switchWidget);
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
