package com.android.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class RestrictedSwitchPreference extends SwitchPreference {
    RestrictedPreferenceHelper mHelper;
    CharSequence mRestrictedSwitchSummary;
    boolean mUseAdditionalSummary;

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mUseAdditionalSummary = false;
        setWidgetLayoutResource(R.layout.restricted_switch_widget);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RestrictedSwitchPreference);
            TypedValue useAdditionalSummary = attributes.peekValue(R.styleable.RestrictedSwitchPreference_useAdditionalSummary);
            if (useAdditionalSummary != null) {
                boolean z = useAdditionalSummary.type == 18 && useAdditionalSummary.data != 0;
                this.mUseAdditionalSummary = z;
            }
            TypedValue restrictedSwitchSummary = attributes.peekValue(R.styleable.RestrictedSwitchPreference_restrictedSwitchSummary);
            if (restrictedSwitchSummary != null && restrictedSwitchSummary.type == 3) {
                if (restrictedSwitchSummary.resourceId != 0) {
                    this.mRestrictedSwitchSummary = context.getText(restrictedSwitchSummary.resourceId);
                } else {
                    this.mRestrictedSwitchSummary = restrictedSwitchSummary.string;
                }
            }
        }
        if (this.mUseAdditionalSummary) {
            setLayoutResource(R.layout.restricted_switch_preference);
            useAdminDisabledSummary(false);
        }
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.switchPreferenceStyle, 16843629));
    }

    public RestrictedSwitchPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        CharSequence switchSummary;
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
        if (this.mRestrictedSwitchSummary == null) {
            switchSummary = getContext().getText(isChecked() ? R.string.enabled_by_admin : R.string.disabled_by_admin);
        } else {
            switchSummary = this.mRestrictedSwitchSummary;
        }
        View restrictedIcon = holder.findViewById(R.id.restricted_icon);
        View switchWidget = holder.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        if (restrictedIcon != null) {
            restrictedIcon.setVisibility(isDisabledByAdmin() ? 0 : 8);
        }
        if (switchWidget != null) {
            switchWidget.setVisibility(isDisabledByAdmin() ? 8 : 0);
        }
        if (this.mUseAdditionalSummary) {
            TextView additionalSummaryView = (TextView) holder.findViewById(R.id.additional_summary);
            if (additionalSummaryView == null) {
                return;
            }
            if (isDisabledByAdmin()) {
                additionalSummaryView.setText(switchSummary);
                additionalSummaryView.setVisibility(0);
                return;
            }
            additionalSummaryView.setVisibility(8);
            return;
        }
        TextView summaryView = (TextView) holder.findViewById(16908304);
        if (summaryView != null && isDisabledByAdmin()) {
            summaryView.setText(switchSummary);
            summaryView.setVisibility(0);
        }
    }

    public void performClick() {
        if (!this.mHelper.performClick()) {
            super.performClick();
        }
    }

    public void useAdminDisabledSummary(boolean useSummary) {
        this.mHelper.useAdminDisabledSummary(useSummary);
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    public void checkRestrictionAndSetDisabled(String userRestriction) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, UserHandle.myUserId());
    }

    public void checkRestrictionAndSetDisabled(String userRestriction, int userId) {
        this.mHelper.checkRestrictionAndSetDisabled(userRestriction, userId);
    }

    public void setEnabled(boolean enabled) {
        if (enabled && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
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
}
