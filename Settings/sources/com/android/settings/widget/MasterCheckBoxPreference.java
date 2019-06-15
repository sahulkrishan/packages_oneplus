package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import com.android.settings.R;
import com.android.settingslib.TwoTargetPreference;

public class MasterCheckBoxPreference extends TwoTargetPreference {
    private CheckBox mCheckBox;
    private boolean mChecked;
    private boolean mEnableCheckBox = true;

    public MasterCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MasterCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MasterCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MasterCheckBoxPreference(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_master_checkbox;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View widgetView = holder.findViewById(16908312);
        if (widgetView != null) {
            widgetView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (MasterCheckBoxPreference.this.mCheckBox == null || MasterCheckBoxPreference.this.mCheckBox.isEnabled()) {
                        MasterCheckBoxPreference.this.setChecked(MasterCheckBoxPreference.this.mChecked ^ 1);
                        if (MasterCheckBoxPreference.this.callChangeListener(Boolean.valueOf(MasterCheckBoxPreference.this.mChecked))) {
                            MasterCheckBoxPreference.this.persistBoolean(MasterCheckBoxPreference.this.mChecked);
                        } else {
                            MasterCheckBoxPreference.this.setChecked(MasterCheckBoxPreference.this.mChecked ^ 1);
                        }
                    }
                }
            });
        }
        this.mCheckBox = (CheckBox) holder.findViewById(R.id.checkboxWidget);
        if (this.mCheckBox != null) {
            this.mCheckBox.setContentDescription(getTitle());
            this.mCheckBox.setChecked(this.mChecked);
            this.mCheckBox.setEnabled(this.mEnableCheckBox);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCheckBoxEnabled(enabled);
    }

    public boolean isChecked() {
        return this.mCheckBox != null && this.mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (this.mCheckBox != null) {
            this.mCheckBox.setChecked(checked);
        }
    }

    public void setCheckBoxEnabled(boolean enabled) {
        this.mEnableCheckBox = enabled;
        if (this.mCheckBox != null) {
            this.mCheckBox.setEnabled(enabled);
        }
    }

    public CheckBox getCheckBox() {
        return this.mCheckBox;
    }
}
