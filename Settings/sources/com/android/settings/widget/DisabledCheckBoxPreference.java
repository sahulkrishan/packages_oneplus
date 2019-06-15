package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.R;

public class DisabledCheckBoxPreference extends CheckBoxPreference {
    private View mCheckBox;
    private boolean mEnabledCheckBox;
    private PreferenceViewHolder mViewHolder;

    public DisabledCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupDisabledCheckBoxPreference(context, attrs, defStyleAttr, defStyleRes);
    }

    public DisabledCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupDisabledCheckBoxPreference(context, attrs, defStyleAttr, 0);
    }

    public DisabledCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDisabledCheckBoxPreference(context, attrs, 0, 0);
    }

    public DisabledCheckBoxPreference(Context context) {
        super(context);
        setupDisabledCheckBoxPreference(context, null, 0, 0);
    }

    private void setupDisabledCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == 2) {
                this.mEnabledCheckBox = a.getBoolean(attr, true);
            }
        }
        a.recycle();
        super.setEnabled(true);
        enableCheckbox(this.mEnabledCheckBox);
    }

    public void enableCheckbox(boolean enabled) {
        this.mEnabledCheckBox = enabled;
        if (this.mViewHolder != null && this.mCheckBox != null) {
            this.mCheckBox.setEnabled(this.mEnabledCheckBox);
            this.mViewHolder.itemView.setEnabled(this.mEnabledCheckBox);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mViewHolder = holder;
        this.mCheckBox = holder.findViewById(16908289);
        enableCheckbox(this.mEnabledCheckBox);
    }

    /* Access modifiers changed, original: protected */
    public void performClick(View view) {
        if (this.mEnabledCheckBox) {
            super.performClick(view);
        }
    }
}
