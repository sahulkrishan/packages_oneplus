package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.TogglePreferenceController;
import java.time.LocalTime;

public class NightDisplayActivationPreferenceController extends TogglePreferenceController {
    private ColorDisplayController mController;
    private final OnClickListener mListener = new OnClickListener() {
        public void onClick(View v) {
            NightDisplayActivationPreferenceController.this.mController.setActivated(NightDisplayActivationPreferenceController.this.mController.isActivated() ^ 1);
            NightDisplayActivationPreferenceController.this.updateStateInternal();
        }
    };
    private NightDisplayTimeFormatter mTimeFormatter;
    private Button mTurnOffButton;
    private Button mTurnOnButton;

    public NightDisplayActivationPreferenceController(Context context, String key) {
        super(context, key);
        this.mController = new ColorDisplayController(context);
        this.mTimeFormatter = new NightDisplayTimeFormatter(context);
    }

    public int getAvailabilityStatus() {
        return ColorDisplayController.isAvailable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "night_display_activated");
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        LayoutPreference preference = (LayoutPreference) screen.findPreference(getPreferenceKey());
        this.mTurnOnButton = (Button) preference.findViewById(R.id.night_display_turn_on_button);
        this.mTurnOnButton.setOnClickListener(this.mListener);
        this.mTurnOffButton = (Button) preference.findViewById(R.id.night_display_turn_off_button);
        this.mTurnOffButton.setOnClickListener(this.mListener);
    }

    public final void updateState(Preference preference) {
        updateStateInternal();
    }

    public boolean isChecked() {
        return this.mController.isActivated();
    }

    public boolean setChecked(boolean isChecked) {
        return this.mController.setActivated(isChecked);
    }

    public CharSequence getSummary() {
        return this.mTimeFormatter.getAutoModeTimeSummary(this.mContext, this.mController);
    }

    private void updateStateInternal() {
        if (this.mTurnOnButton != null && this.mTurnOffButton != null) {
            String buttonText;
            boolean isActivated = this.mController.isActivated();
            int autoMode = this.mController.getAutoMode();
            int i;
            if (autoMode == 1) {
                int i2;
                LocalTime customStartTime;
                Context context = this.mContext;
                if (isActivated) {
                    i2 = R.string.night_display_activation_off_custom;
                } else {
                    i2 = R.string.night_display_activation_on_custom;
                }
                Object[] objArr = new Object[1];
                NightDisplayTimeFormatter nightDisplayTimeFormatter = this.mTimeFormatter;
                if (isActivated) {
                    customStartTime = this.mController.getCustomStartTime();
                } else {
                    customStartTime = this.mController.getCustomEndTime();
                }
                objArr[0] = nightDisplayTimeFormatter.getFormattedTimeString(customStartTime);
                buttonText = context.getString(i2, objArr);
            } else if (autoMode == 2) {
                buttonText = this.mContext;
                if (isActivated) {
                    i = R.string.night_display_activation_off_twilight;
                } else {
                    i = R.string.night_display_activation_on_twilight;
                }
                buttonText = buttonText.getString(i);
            } else {
                Context context2 = this.mContext;
                if (isActivated) {
                    i = R.string.night_display_activation_off_manual;
                } else {
                    i = R.string.night_display_activation_on_manual;
                }
                buttonText = context2.getString(i);
            }
            if (isActivated) {
                this.mTurnOnButton.setVisibility(8);
                this.mTurnOffButton.setVisibility(0);
                this.mTurnOffButton.setText(buttonText);
            } else {
                this.mTurnOnButton.setVisibility(0);
                this.mTurnOffButton.setVisibility(8);
                this.mTurnOnButton.setText(buttonText);
            }
        }
    }
}
