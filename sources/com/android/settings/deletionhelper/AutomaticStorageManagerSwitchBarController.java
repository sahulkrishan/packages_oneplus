package com.android.settings.deletionhelper;

import android.app.FragmentManager;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.widget.Switch;
import com.android.internal.util.Preconditions;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.Utils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class AutomaticStorageManagerSwitchBarController implements OnSwitchChangeListener {
    private static final String STORAGE_MANAGER_ENABLED_BY_DEFAULT_PROPERTY = "ro.storage_manager.enabled";
    private Context mContext;
    private Preference mDaysToRetainPreference;
    private FragmentManager mFragmentManager;
    private MetricsFeatureProvider mMetrics;
    private SwitchBar mSwitchBar;

    public AutomaticStorageManagerSwitchBarController(Context context, SwitchBar switchBar, MetricsFeatureProvider metrics, Preference daysToRetainPreference, FragmentManager fragmentManager) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mSwitchBar = (SwitchBar) Preconditions.checkNotNull(switchBar);
        this.mMetrics = (MetricsFeatureProvider) Preconditions.checkNotNull(metrics);
        this.mDaysToRetainPreference = (Preference) Preconditions.checkNotNull(daysToRetainPreference);
        this.mFragmentManager = (FragmentManager) Preconditions.checkNotNull(fragmentManager);
        initializeCheckedStatus();
    }

    private void initializeCheckedStatus() {
        this.mSwitchBar.setChecked(Utils.isStorageManagerEnabled(this.mContext));
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        this.mMetrics.action(this.mContext, 489, isChecked);
        this.mDaysToRetainPreference.setEnabled(isChecked);
        Secure.putInt(this.mContext.getContentResolver(), "automatic_storage_manager_enabled", isChecked);
        if (isChecked) {
            maybeShowWarning();
        }
    }

    public void tearDown() {
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    private void maybeShowWarning() {
        if (!SystemProperties.getBoolean(STORAGE_MANAGER_ENABLED_BY_DEFAULT_PROPERTY, false)) {
            ActivationWarningFragment.newInstance().show(this.mFragmentManager, ActivationWarningFragment.TAG);
        }
    }
}
