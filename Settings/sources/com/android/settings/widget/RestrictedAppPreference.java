package com.android.settings.widget;

import android.content.Context;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreferenceHelper;

public class RestrictedAppPreference extends AppPreference {
    private RestrictedPreferenceHelper mHelper;
    private String userRestriction;

    public RestrictedAppPreference(Context context) {
        super(context);
        initialize(null, null);
    }

    public RestrictedAppPreference(Context context, String userRestriction) {
        super(context);
        initialize(null, userRestriction);
    }

    public RestrictedAppPreference(Context context, AttributeSet attrs, String userRestriction) {
        super(context, attrs);
        initialize(attrs, userRestriction);
    }

    private void initialize(AttributeSet attrs, String userRestriction) {
        setWidgetLayoutResource(R.layout.restricted_icon);
        this.mHelper = new RestrictedPreferenceHelper(getContext(), this, attrs);
        this.userRestriction = userRestriction;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
        View restrictedIcon = holder.findViewById(R.id.restricted_icon);
        if (restrictedIcon != null) {
            restrictedIcon.setVisibility(isDisabledByAdmin() ? 0 : 8);
        }
    }

    public void performClick() {
        if (!this.mHelper.performClick()) {
            super.performClick();
        }
    }

    public void setEnabled(boolean enabled) {
        if (!isDisabledByAdmin() || !enabled) {
            super.setEnabled(enabled);
        }
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        if (this.mHelper.setDisabledByAdmin(admin)) {
            notifyChanged();
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }

    public void useAdminDisabledSummary(boolean useSummary) {
        this.mHelper.useAdminDisabledSummary(useSummary);
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    public void checkRestrictionAndSetDisabled() {
        if (!TextUtils.isEmpty(this.userRestriction)) {
            this.mHelper.checkRestrictionAndSetDisabled(this.userRestriction, UserHandle.myUserId());
        }
    }

    public void checkRestrictionAndSetDisabled(String userRestriction) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, UserHandle.myUserId());
    }

    public void checkRestrictionAndSetDisabled(String userRestriction, int userId) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, userId);
    }
}
