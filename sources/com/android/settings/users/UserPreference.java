package com.android.settings.users;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import java.util.Comparator;

public class UserPreference extends RestrictedPreference {
    private static final int ALPHA_DISABLED = 102;
    private static final int ALPHA_ENABLED = 255;
    static final int DELETE_ID = 2131363254;
    public static final Comparator<UserPreference> SERIAL_NUMBER_COMPARATOR = new Comparator<UserPreference>() {
        public int compare(UserPreference p1, UserPreference p2) {
            int sn1 = p1.getSerialNumber();
            int sn2 = p2.getSerialNumber();
            if (sn1 < sn2) {
                return -1;
            }
            if (sn1 > sn2) {
                return 1;
            }
            return 0;
        }
    };
    static final int SETTINGS_ID = 2131362603;
    public static final int USERID_GUEST_DEFAULTS = -11;
    public static final int USERID_UNKNOWN = -10;
    private OnClickListener mDeleteClickListener;
    private int mSerialNumber;
    private OnClickListener mSettingsClickListener;
    private int mUserId;

    public UserPreference(Context context, AttributeSet attrs) {
        this(context, attrs, -10, null, null);
    }

    UserPreference(Context context, AttributeSet attrs, int userId, OnClickListener settingsListener, OnClickListener deleteListener) {
        super(context, attrs);
        this.mSerialNumber = -1;
        this.mUserId = -10;
        if (!(deleteListener == null && settingsListener == null)) {
            setWidgetLayoutResource(R.layout.restricted_preference_user_delete_widget);
        }
        this.mDeleteClickListener = deleteListener;
        this.mSettingsClickListener = settingsListener;
        this.mUserId = userId;
        useAdminDisabledSummary(true);
    }

    private void dimIcon(boolean dimmed) {
        Drawable icon = getIcon();
        if (icon != null) {
            icon.mutate().setAlpha(dimmed ? 102 : 255);
            setIcon(icon);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldHideSecondTarget() {
        boolean z = true;
        if (isDisabledByAdmin()) {
            return true;
        }
        if (canDeleteUser()) {
            return false;
        }
        if (this.mSettingsClickListener != null) {
            z = false;
        }
        return z;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        boolean disabledByAdmin = isDisabledByAdmin();
        dimIcon(disabledByAdmin);
        View userDeleteWidget = view.findViewById(R.id.user_delete_widget);
        int i = 0;
        if (userDeleteWidget != null) {
            userDeleteWidget.setVisibility(disabledByAdmin ? 8 : 0);
        }
        if (!disabledByAdmin) {
            View deleteDividerView = view.findViewById(R.id.divider_delete);
            View manageDividerView = view.findViewById(R.id.divider_manage);
            View deleteView = view.findViewById(R.id.trash_user);
            if (deleteView != null) {
                if (canDeleteUser()) {
                    deleteView.setVisibility(0);
                    deleteDividerView.setVisibility(0);
                    deleteView.setOnClickListener(this.mDeleteClickListener);
                    deleteView.setTag(this);
                } else {
                    deleteView.setVisibility(8);
                    deleteDividerView.setVisibility(8);
                }
            }
            ImageView manageView = (ImageView) view.findViewById(R.id.manage_user);
            if (manageView == null) {
                return;
            }
            if (this.mSettingsClickListener != null) {
                manageView.setVisibility(0);
                if (this.mDeleteClickListener != null) {
                    i = 8;
                }
                manageDividerView.setVisibility(i);
                manageView.setOnClickListener(this.mSettingsClickListener);
                manageView.setTag(this);
                return;
            }
            manageView.setVisibility(8);
            manageDividerView.setVisibility(8);
        }
    }

    private boolean canDeleteUser() {
        return (this.mDeleteClickListener == null || RestrictedLockUtils.hasBaseUserRestriction(getContext(), "no_remove_user", UserHandle.myUserId())) ? false : true;
    }

    private int getSerialNumber() {
        if (this.mUserId == UserHandle.myUserId()) {
            return Integer.MIN_VALUE;
        }
        if (this.mSerialNumber < 0) {
            if (this.mUserId == -10) {
                return Integer.MAX_VALUE;
            }
            if (this.mUserId == -11) {
                return 2147483646;
            }
            this.mSerialNumber = ((UserManager) getContext().getSystemService("user")).getUserSerialNumber(this.mUserId);
            if (this.mSerialNumber < 0) {
                return this.mUserId;
            }
        }
        return this.mSerialNumber;
    }

    public int getUserId() {
        return this.mUserId;
    }
}
