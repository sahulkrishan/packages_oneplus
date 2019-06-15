package com.android.settingslib;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TwoTargetPreference extends Preference {
    public static final int ICON_SIZE_DEFAULT = 0;
    public static final int ICON_SIZE_MEDIUM = 1;
    public static final int ICON_SIZE_SMALL = 2;
    private int mIconSize;
    private int mMediumIconSize;
    private int mSmallIconSize;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IconSize {
    }

    public TwoTargetPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public TwoTargetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TwoTargetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TwoTargetPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setLayoutResource(R.layout.preference_two_target);
        this.mSmallIconSize = context.getResources().getDimensionPixelSize(R.dimen.two_target_pref_small_icon_size);
        this.mMediumIconSize = context.getResources().getDimensionPixelSize(R.dimen.two_target_pref_medium_icon_size);
        int secondTargetResId = getSecondTargetResId();
        if (secondTargetResId != 0) {
            setWidgetLayoutResource(secondTargetResId);
        }
    }

    public void setIconSize(int iconSize) {
        this.mIconSize = iconSize;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView icon = (ImageView) holder.itemView.findViewById(16908294);
        switch (this.mIconSize) {
            case 1:
                icon.setLayoutParams(new LayoutParams(this.mMediumIconSize, this.mMediumIconSize));
                break;
            case 2:
                icon.setLayoutParams(new LayoutParams(this.mSmallIconSize, this.mSmallIconSize));
                break;
        }
        View divider = holder.findViewById(R.id.two_target_divider);
        View widgetFrame = holder.findViewById(16908312);
        boolean shouldHideSecondTarget = shouldHideSecondTarget();
        int i = 0;
        if (divider != null) {
            divider.setVisibility(shouldHideSecondTarget ? 8 : 0);
        }
        if (widgetFrame != null) {
            if (shouldHideSecondTarget) {
                i = 8;
            }
            widgetFrame.setVisibility(i);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldHideSecondTarget() {
        return getSecondTargetResId() == 0;
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return 0;
    }
}
