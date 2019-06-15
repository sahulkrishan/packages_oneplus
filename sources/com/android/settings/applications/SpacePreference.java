package com.android.settings.applications;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import com.android.settings.R;

public class SpacePreference extends Preference {
    private int mHeight;

    public SpacePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, 16842894));
    }

    public SpacePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SpacePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.space_preference);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{16842997}, defStyleAttr, defStyleRes);
        this.mHeight = a.getDimensionPixelSize(0, 0);
        a.recycle();
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setLayoutParams(new LayoutParams(-1, this.mHeight));
    }
}
