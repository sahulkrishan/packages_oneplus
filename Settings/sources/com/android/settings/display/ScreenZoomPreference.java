package com.android.settings.display;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settingslib.display.DisplayDensityUtils;

public class ScreenZoomPreference extends Preference {
    public ScreenZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, 16842894));
        DisplayDensityUtils density = new DisplayDensityUtils(context);
        if (density.getCurrentIndex() < 0) {
            setVisible(false);
            setEnabled(false);
        } else if (TextUtils.isEmpty(getSummary())) {
            setSummary(density.getEntries()[density.getCurrentIndex()]);
        }
    }
}
