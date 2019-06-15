package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import com.android.settings.R;
import com.android.settingslib.TwoTargetPreference;

public class ZenCustomRadioButtonPreference extends TwoTargetPreference implements OnClickListener {
    private RadioButton mButton;
    private boolean mChecked;
    private OnGearClickListener mOnGearClickListener;
    private OnRadioButtonClickListener mOnRadioButtonClickListener;

    public interface OnGearClickListener {
        void onGearClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference);
    }

    public interface OnRadioButtonClickListener {
        void onRadioButtonClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    public ZenCustomRadioButtonPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_two_target_radio);
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_gear;
    }

    public void setOnGearClickListener(OnGearClickListener l) {
        this.mOnGearClickListener = l;
        notifyChanged();
    }

    public void setOnRadioButtonClickListener(OnRadioButtonClickListener l) {
        this.mOnRadioButtonClickListener = l;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View buttonFrame = holder.findViewById(R.id.checkbox_frame);
        if (buttonFrame != null) {
            buttonFrame.setOnClickListener(this);
        }
        this.mButton = (RadioButton) holder.findViewById(16908289);
        if (this.mButton != null) {
            this.mButton.setChecked(this.mChecked);
        }
        View gear = holder.findViewById(16908312);
        View divider = holder.findViewById(R.id.two_target_divider);
        if (this.mOnGearClickListener != null) {
            divider.setVisibility(0);
            gear.setVisibility(0);
            gear.setOnClickListener(this);
            return;
        }
        divider.setVisibility(8);
        gear.setVisibility(8);
        gear.setOnClickListener(null);
    }

    public boolean isChecked() {
        return this.mButton != null && this.mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (this.mButton != null) {
            this.mButton.setChecked(checked);
        }
    }

    public RadioButton getRadioButton() {
        return this.mButton;
    }

    public void onClick() {
        if (this.mOnRadioButtonClickListener != null) {
            this.mOnRadioButtonClickListener.onRadioButtonClick(this);
        }
    }

    public void onClick(View v) {
        if (v.getId() == 16908312) {
            if (this.mOnGearClickListener != null) {
                this.mOnGearClickListener.onGearClick(this);
            }
        } else if (v.getId() == R.id.checkbox_frame && this.mOnRadioButtonClickListener != null) {
            this.mOnRadioButtonClickListener.onRadioButtonClick(this);
        }
    }
}
