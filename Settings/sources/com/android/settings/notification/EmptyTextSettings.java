package com.android.settings.notification;

import android.os.Bundle;
import android.support.v7.preference.AndroidResources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment;

public abstract class EmptyTextSettings extends SettingsPreferenceFragment {
    private TextView mEmpty;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mEmpty = new TextView(getContext());
        this.mEmpty.setGravity(17);
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(16842817, value, true);
        this.mEmpty.setTextAppearance(value.resourceId);
        ((ViewGroup) view.findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER)).addView(this.mEmpty, new LayoutParams(-1, -1));
        setEmptyView(this.mEmpty);
    }

    /* Access modifiers changed, original: protected */
    public void setEmptyText(int text) {
        this.mEmpty.setText(text);
    }
}
