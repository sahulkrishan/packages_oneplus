package com.android.settings.dashboard.conditional;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
import android.service.notification.ZenModeConfig;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.notification.ZenModeSettings;

public class DndCondition extends Condition {
    private static final boolean DEBUG_LOG = true;
    @VisibleForTesting
    static final IntentFilter DND_FILTER = new IntentFilter("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL");
    private static final String KEY_STATE = "state";
    private static final String TAG = "DndCondition";
    @VisibleForTesting
    protected ZenModeConfig mConfig;
    private final Receiver mReceiver = new Receiver();
    private boolean mRegistered;
    private int mZen;

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL".equals(intent.getAction())) {
                Condition condition = ConditionManager.get(context).getCondition(DndCondition.class);
                if (condition != null) {
                    condition.refreshState();
                }
            }
        }
    }

    public DndCondition(ConditionManager manager) {
        super(manager);
        this.mManager.getContext().registerReceiver(this.mReceiver, DND_FILTER);
        this.mRegistered = true;
    }

    public void refreshState() {
        Log.i(TAG, "DndCondition refresh");
        NotificationManager notificationManager = (NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class);
        this.mZen = notificationManager.getZenMode();
        boolean zenModeEnabled = this.mZen != 0;
        if (zenModeEnabled) {
            this.mConfig = notificationManager.getZenModeConfig();
        } else {
            this.mConfig = null;
        }
        if (this.mConfig != null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("DndCondition rule:");
            stringBuilder.append(this.mConfig.manualRule);
            Log.i(str, stringBuilder.toString());
        }
        setActive(zenModeEnabled);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean saveState(PersistableBundle bundle) {
        bundle.putInt(KEY_STATE, this.mZen);
        return super.saveState(bundle);
    }

    /* Access modifiers changed, original: 0000 */
    public void restoreState(PersistableBundle bundle) {
        super.restoreState(bundle);
        this.mZen = bundle.getInt(KEY_STATE, 0);
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_do_not_disturb_on_24dp);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_zen_title);
    }

    public CharSequence getSummary() {
        Log.i(TAG, "DndCondition getSummary");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("summary:");
        boolean z = false;
        stringBuilder.append(ZenModeConfig.getDescription(this.mManager.getContext(), this.mZen != 0, this.mConfig, true));
        Log.i(str, stringBuilder.toString());
        Context context = this.mManager.getContext();
        if (this.mZen != 0) {
            z = true;
        }
        return ZenModeConfig.getDescription(context, z, this.mConfig, true);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        new SubSettingLauncher(this.mManager.getContext()).setDestination(ZenModeSettings.class.getName()).setSourceMetricsCategory(35).setTitle((int) R.string.zen_mode_settings_title).addFlags(268435456).launch();
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ((NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class)).setZenMode(0, null, TAG);
            setActive(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 381;
    }

    public void onResume() {
        if (!this.mRegistered) {
            this.mManager.getContext().registerReceiver(this.mReceiver, DND_FILTER);
            this.mRegistered = true;
        }
    }

    public void onPause() {
        if (this.mRegistered) {
            this.mManager.getContext().unregisterReceiver(this.mReceiver);
            this.mRegistered = false;
        }
    }
}
