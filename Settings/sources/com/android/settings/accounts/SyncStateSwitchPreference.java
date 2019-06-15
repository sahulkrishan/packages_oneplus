package com.android.settings.accounts;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.widget.AnimatedImageView;

public class SyncStateSwitchPreference extends SwitchPreference {
    private Account mAccount;
    private String mAuthority;
    private boolean mFailed;
    private boolean mIsActive;
    private boolean mIsPending;
    private boolean mOneTimeSyncMode;
    private String mPackageName;
    private int mUid;

    public SyncStateSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0, R.style.SyncSwitchPreference);
        this.mIsActive = false;
        this.mIsPending = false;
        this.mFailed = false;
        this.mOneTimeSyncMode = false;
        this.mAccount = null;
        this.mAuthority = null;
        this.mPackageName = null;
        this.mUid = 0;
    }

    public SyncStateSwitchPreference(Context context, Account account, String authority, String packageName, int uid) {
        super(context, null, 0, R.style.SyncSwitchPreference);
        this.mIsActive = false;
        this.mIsPending = false;
        this.mFailed = false;
        this.mOneTimeSyncMode = false;
        setup(account, authority, packageName, uid);
    }

    public void setup(Account account, String authority, String packageName, int uid) {
        this.mAccount = account;
        this.mAuthority = authority;
        this.mPackageName = packageName;
        this.mUid = uid;
        setVisible(TextUtils.isEmpty(this.mAuthority) ^ 1);
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        AnimatedImageView syncActiveView = (AnimatedImageView) view.findViewById(R.id.sync_active);
        View syncFailedView = view.findViewById(R.id.sync_failed);
        boolean activeVisible = this.mIsActive || this.mIsPending;
        syncActiveView.setVisibility(activeVisible ? 0 : 8);
        syncActiveView.setAnimating(this.mIsActive);
        boolean failedVisible = this.mFailed && !activeVisible;
        syncFailedView.setVisibility(failedVisible ? 0 : 8);
        View switchView = view.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        if (this.mOneTimeSyncMode) {
            switchView.setVisibility(8);
            ((TextView) view.findViewById(16908304)).setText(getContext().getString(R.string.sync_one_time_sync, new Object[]{getSummary()}));
            return;
        }
        switchView.setVisibility(0);
    }

    public void setActive(boolean isActive) {
        this.mIsActive = isActive;
        notifyChanged();
    }

    public void setPending(boolean isPending) {
        this.mIsPending = isPending;
        notifyChanged();
    }

    public void setFailed(boolean failed) {
        this.mFailed = failed;
        notifyChanged();
    }

    public void setOneTimeSyncMode(boolean oneTimeSyncMode) {
        this.mOneTimeSyncMode = oneTimeSyncMode;
        notifyChanged();
    }

    public boolean isOneTimeSyncMode() {
        return this.mOneTimeSyncMode;
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        if (!this.mOneTimeSyncMode) {
            if (ActivityManager.isUserAMonkey()) {
                Log.d("SyncState", "ignoring monkey's attempt to flip sync state");
            } else {
                super.onClick();
            }
        }
    }

    public Account getAccount() {
        return this.mAccount;
    }

    public String getAuthority() {
        return this.mAuthority;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getUid() {
        return this.mUid;
    }
}
