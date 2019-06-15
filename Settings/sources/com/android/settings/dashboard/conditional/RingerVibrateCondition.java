package com.android.settings.dashboard.conditional;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.android.settings.R;

public class RingerVibrateCondition extends AbnormalRingerConditionBase {
    RingerVibrateCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mAudioManager.getRingerModeInternal()  = ");
        stringBuilder.append(this.mAudioManager.getRingerModeInternal());
        Log.v("RingerVibrateCondition", stringBuilder.toString());
        boolean z = true;
        if (this.mAudioManager.getRingerModeInternal() != 1) {
            z = false;
        }
        setActive(z);
    }

    public int getMetricsConstant() {
        return 1369;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_volume_ringer_vibrate);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getText(R.string.condition_device_vibrate_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getText(R.string.condition_device_vibrate_summary);
    }
}
