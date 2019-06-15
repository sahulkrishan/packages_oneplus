package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import com.android.settings.R;

public abstract class AbnormalRingerConditionBase extends Condition {
    protected final AudioManager mAudioManager = ((AudioManager) this.mManager.getContext().getSystemService("audio"));
    private final IntentFilter mFilter = new IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
    private final RingerModeChangeReceiver mReceiver = new RingerModeChangeReceiver(this);

    static class RingerModeChangeReceiver extends BroadcastReceiver {
        private final AbnormalRingerConditionBase mCondition;

        public RingerModeChangeReceiver(AbnormalRingerConditionBase condition) {
            this.mCondition = condition;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(intent.getAction())) {
                this.mCondition.refreshState();
            }
        }
    }

    AbnormalRingerConditionBase(ConditionManager manager) {
        super(manager);
        manager.getContext().registerReceiver(this.mReceiver, this.mFilter);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getText(R.string.condition_device_muted_action_turn_on_sound)};
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent("android.settings.SOUND_SETTINGS").addFlags(268435456));
    }

    public void onActionClick(int index) {
        this.mAudioManager.setRingerModeInternal(2);
        this.mAudioManager.setStreamVolume(2, 1, 0);
        refreshState();
    }
}
