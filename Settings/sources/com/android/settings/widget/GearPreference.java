package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;

public class GearPreference extends RestrictedPreference implements OnClickListener {
    private OnGearClickListener mOnGearClickListener;

    public interface OnGearClickListener {
        void onGearClick(GearPreference gearPreference);
    }

    public GearPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnGearClickListener(OnGearClickListener l) {
        this.mOnGearClickListener = l;
        notifyChanged();
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_gear;
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldHideSecondTarget() {
        return this.mOnGearClickListener == null;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View gear = holder.findViewById(R.id.settings_button);
        if (this.mOnGearClickListener != null) {
            gear.setVisibility(0);
            gear.setOnClickListener(this);
        } else {
            gear.setVisibility(8);
            gear.setOnClickListener(null);
        }
        gear.setEnabled(true);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.settings_button && this.mOnGearClickListener != null) {
            this.mOnGearClickListener.onGearClick(this);
        }
    }
}
