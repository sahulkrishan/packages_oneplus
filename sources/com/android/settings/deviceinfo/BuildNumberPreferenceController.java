package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DevelopmentSettingsEnabler;

public class BuildNumberPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume {
    private static final String KEY_BUILD_NUMBER = "build_number";
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    static final int TAPS_TO_BE_A_DEVELOPER = 7;
    private final Activity mActivity;
    private EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;
    private int mDevHitCountdown;
    private Toast mDevHitToast;
    private final Fragment mFragment;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private boolean mProcessingLastDevHit;
    private final UserManager mUm;

    public BuildNumberPreferenceController(Context context, Activity activity, Fragment fragment, Lifecycle lifecycle) {
        super(context);
        this.mActivity = activity;
        this.mFragment = fragment;
        this.mUm = (UserManager) context.getSystemService("user");
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(KEY_BUILD_NUMBER);
        if (preference != null) {
            try {
                preference.setSummary(BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY));
                preference.setEnabled(true);
            } catch (Exception e) {
                preference.setSummary((int) R.string.device_info_default);
            }
        }
    }

    public String getPreferenceKey() {
        return KEY_BUILD_NUMBER;
    }

    public boolean isAvailable() {
        return false;
    }

    public void onResume() {
        this.mDebuggingFeaturesDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDebuggingFeaturesDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDevHitCountdown = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mContext) ? -1 : 7;
        this.mDevHitToast = null;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_BUILD_NUMBER) || Utils.isMonkeyRunning()) {
            return false;
        }
        if (!this.mUm.isAdminUser() && !this.mUm.isDemoUser()) {
            this.mMetricsFeatureProvider.action(this.mContext, 847, new Pair[0]);
            return false;
        } else if (!Utils.isDeviceProvisioned(this.mContext)) {
            this.mMetricsFeatureProvider.action(this.mContext, 847, new Pair[0]);
            return false;
        } else if (this.mUm.hasUserRestriction("no_debugging_features")) {
            if (this.mUm.isDemoUser()) {
                ComponentName componentName = Utils.getDeviceOwnerComponent(this.mContext);
                if (componentName != null) {
                    Intent requestDebugFeatures = new Intent().setPackage(componentName.getPackageName()).setAction("com.android.settings.action.REQUEST_DEBUG_FEATURES");
                    if (this.mContext.getPackageManager().resolveActivity(requestDebugFeatures, 0) != null) {
                        this.mContext.startActivity(requestDebugFeatures);
                        return false;
                    }
                }
            }
            if (!(this.mDebuggingFeaturesDisallowedAdmin == null || this.mDebuggingFeaturesDisallowedBySystem)) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mDebuggingFeaturesDisallowedAdmin);
            }
            this.mMetricsFeatureProvider.action(this.mContext, 847, new Pair[0]);
            return false;
        } else {
            if (this.mDevHitCountdown > 0) {
                this.mDevHitCountdown--;
                if (this.mDevHitCountdown == 0 && !this.mProcessingLastDevHit) {
                    this.mDevHitCountdown++;
                    this.mProcessingLastDevHit = new ChooseLockSettingsHelper(this.mActivity, this.mFragment).launchConfirmationActivity(100, this.mContext.getString(R.string.unlock_set_unlock_launch_picker_title));
                    if (!this.mProcessingLastDevHit) {
                        enableDevelopmentSettings();
                    }
                    this.mMetricsFeatureProvider.action(this.mContext, 847, Pair.create(Integer.valueOf(848), Integer.valueOf(this.mProcessingLastDevHit ^ 1)));
                } else if (this.mDevHitCountdown > 0 && this.mDevHitCountdown < 5) {
                    if (this.mDevHitToast != null) {
                        this.mDevHitToast.cancel();
                    }
                    this.mDevHitToast = Toast.makeText(this.mContext, this.mContext.getResources().getQuantityString(R.plurals.show_dev_countdown, this.mDevHitCountdown, new Object[]{Integer.valueOf(this.mDevHitCountdown)}), 0);
                    this.mDevHitToast.show();
                }
                this.mMetricsFeatureProvider.action(this.mContext, 847, Pair.create(Integer.valueOf(848), Integer.valueOf(0)));
            } else if (this.mDevHitCountdown < 0) {
                if (this.mDevHitToast != null) {
                    this.mDevHitToast.cancel();
                }
                this.mDevHitToast = Toast.makeText(this.mContext, R.string.show_dev_already, 1);
                this.mDevHitToast.show();
                this.mMetricsFeatureProvider.action(this.mContext, 847, Pair.create(Integer.valueOf(848), Integer.valueOf(1)));
            }
            return true;
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100) {
            return false;
        }
        if (resultCode == -1) {
            enableDevelopmentSettings();
        }
        this.mProcessingLastDevHit = false;
        return true;
    }

    private void enableDevelopmentSettings() {
        this.mDevHitCountdown = 0;
        this.mProcessingLastDevHit = false;
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(this.mContext, true);
        if (this.mDevHitToast != null) {
            this.mDevHitToast.cancel();
        }
        this.mDevHitToast = Toast.makeText(this.mContext, R.string.show_dev_on, 1);
        this.mDevHitToast.show();
    }
}
