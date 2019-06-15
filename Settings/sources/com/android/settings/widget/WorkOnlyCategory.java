package com.android.settings.widget;

import android.content.Context;
import android.os.UserManager;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
import com.android.settings.SelfAvailablePreference;
import com.android.settings.Utils;

public class WorkOnlyCategory extends PreferenceCategory implements SelfAvailablePreference {
    public WorkOnlyCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isAvailable(Context context) {
        return Utils.getManagedProfile(UserManager.get(context)) != null;
    }
}
