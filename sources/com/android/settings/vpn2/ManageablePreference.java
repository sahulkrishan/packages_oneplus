package com.android.settings.vpn2;

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settings.widget.GearPreference;

public abstract class ManageablePreference extends GearPreference {
    public static int STATE_NONE = -1;
    boolean mIsAlwaysOn = false;
    int mState = STATE_NONE;
    int mUserId;

    public ManageablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setOrder(0);
        setUserId(UserHandle.myUserId());
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
        checkRestrictionAndSetDisabled("no_config_vpn", userId);
    }

    public boolean isAlwaysOn() {
        return this.mIsAlwaysOn;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            updateSummary();
            notifyHierarchyChanged();
        }
    }

    public void setAlwaysOn(boolean isEnabled) {
        if (this.mIsAlwaysOn != isEnabled) {
            this.mIsAlwaysOn = isEnabled;
            updateSummary();
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateSummary() {
        Resources res = getContext().getResources();
        String summary = this.mState == STATE_NONE ? "" : res.getStringArray(2130903252)[this.mState];
        if (this.mIsAlwaysOn) {
            summary = TextUtils.isEmpty(summary) ? res.getString(R.string.vpn_always_on_summary_active) : res.getString(R.string.join_two_unrelated_items, new Object[]{summary, res.getString(R.string.vpn_always_on_summary_active)});
        }
        setSummary((CharSequence) summary);
    }
}
