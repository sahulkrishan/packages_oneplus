package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
import android.support.annotation.VisibleForTesting;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public abstract class Condition {
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_LAST_STATE = "last_state";
    private static final String KEY_SILENCE = "silence";
    private boolean mIsActive;
    private boolean mIsSilenced;
    private long mLastStateChange;
    protected final ConditionManager mManager;
    protected final MetricsFeatureProvider mMetricsFeatureProvider;
    protected boolean mReceiverRegistered;

    public abstract CharSequence[] getActions();

    public abstract Drawable getIcon();

    public abstract int getMetricsConstant();

    public abstract CharSequence getSummary();

    public abstract CharSequence getTitle();

    public abstract void onActionClick(int i);

    public abstract void onPrimaryClick();

    public abstract void refreshState();

    Condition(ConditionManager manager) {
        this(manager, FeatureFactory.getFactory(manager.getContext()).getMetricsFeatureProvider());
    }

    Condition(ConditionManager manager, MetricsFeatureProvider metricsFeatureProvider) {
        this.mManager = manager;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
    }

    /* Access modifiers changed, original: 0000 */
    public void restoreState(PersistableBundle bundle) {
        this.mIsSilenced = bundle.getBoolean(KEY_SILENCE);
        this.mIsActive = bundle.getBoolean(KEY_ACTIVE);
        this.mLastStateChange = bundle.getLong(KEY_LAST_STATE);
    }

    public void setSilenced() {
        this.mIsSilenced = false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean saveState(PersistableBundle bundle) {
        if (this.mIsSilenced) {
            bundle.putBoolean(KEY_SILENCE, this.mIsSilenced);
        }
        if (this.mIsActive) {
            bundle.putBoolean(KEY_ACTIVE, this.mIsActive);
            bundle.putLong(KEY_LAST_STATE, this.mLastStateChange);
        }
        return this.mIsSilenced || this.mIsActive;
    }

    /* Access modifiers changed, original: protected */
    public void notifyChanged() {
        this.mManager.notifyChanged(this);
    }

    public boolean isSilenced() {
        return this.mIsSilenced;
    }

    public boolean isActive() {
        return this.mIsActive;
    }

    /* Access modifiers changed, original: protected */
    public void setActive(boolean active) {
        if (this.mIsActive != active) {
            this.mIsActive = active;
            this.mLastStateChange = System.currentTimeMillis();
            if (this.mIsSilenced && !active) {
                this.mIsSilenced = false;
                onSilenceChanged(this.mIsSilenced);
            }
            notifyChanged();
        }
    }

    public void silence() {
        if (!this.mIsSilenced) {
            this.mIsSilenced = true;
            this.mMetricsFeatureProvider.action(this.mManager.getContext(), 372, getMetricsConstant());
            onSilenceChanged(this.mIsSilenced);
            notifyChanged();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onSilenceChanged(boolean silenced) {
        BroadcastReceiver receiver = getReceiver();
        if (receiver != null) {
            if (silenced) {
                if (!this.mReceiverRegistered) {
                    this.mManager.getContext().registerReceiver(receiver, getIntentFilter());
                    this.mReceiverRegistered = true;
                }
            } else if (this.mReceiverRegistered) {
                this.mManager.getContext().unregisterReceiver(receiver);
                this.mReceiverRegistered = false;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public BroadcastReceiver getReceiver() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public IntentFilter getIntentFilter() {
        return null;
    }

    public boolean shouldShow() {
        return isActive() && !isSilenced();
    }

    /* Access modifiers changed, original: 0000 */
    public long getLastChange() {
        return this.mLastStateChange;
    }

    public void onResume() {
    }

    public void onPause() {
    }
}
