package com.android.settings.dashboard.conditional;

import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.R;
import com.android.settings.Settings.AccountDashboardActivity;
import java.util.List;

public class WorkModeCondition extends Condition {
    private UserManager mUm = ((UserManager) this.mManager.getContext().getSystemService("user"));
    private UserHandle mUserHandle;

    public WorkModeCondition(ConditionManager conditionManager) {
        super(conditionManager);
    }

    private void updateUserHandle() {
        List<UserInfo> profiles = this.mUm.getProfiles(UserHandle.myUserId());
        int profilesCount = profiles.size();
        this.mUserHandle = null;
        for (int i = 0; i < profilesCount; i++) {
            UserInfo userInfo = (UserInfo) profiles.get(i);
            if (userInfo.isManagedProfile()) {
                this.mUserHandle = userInfo.getUserHandle();
                return;
            }
        }
    }

    public void refreshState() {
        updateUserHandle();
        boolean z = this.mUserHandle != null && this.mUm.isQuietModeEnabled(this.mUserHandle);
        setActive(z);
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_signal_workmode_enable);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_work_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_work_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_on)};
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent(this.mManager.getContext(), AccountDashboardActivity.class).addFlags(268435456));
    }

    public void onActionClick(int index) {
        if (index == 0) {
            if (this.mUserHandle != null) {
                this.mUm.requestQuietModeEnabled(false, this.mUserHandle);
            }
            setActive(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 383;
    }
}
