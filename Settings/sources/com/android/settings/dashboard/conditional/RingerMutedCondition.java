package com.android.settings.dashboard.conditional;

import android.app.NotificationManager;
import android.graphics.drawable.Drawable;
import com.android.settings.R;

public class RingerMutedCondition extends AbnormalRingerConditionBase {
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mManager.getContext().getSystemService("notification"));

    RingerMutedCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        int zen = 0;
        if (this.mNotificationManager != null) {
            zen = this.mNotificationManager.getZenMode();
        }
        boolean isSilent = false;
        if (zen != 0) {
            int i = 1;
        } else {
            boolean zenModeEnabled = false;
        }
        if (this.mAudioManager.getRingerModeInternal() == 0) {
            isSilent = true;
        }
        setActive(isSilent);
    }

    public int getMetricsConstant() {
        return 1368;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_notifications_off_24dp);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getText(R.string.condition_device_muted_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getText(R.string.condition_device_muted_summary);
    }
}
