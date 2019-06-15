package com.android.settings.overlay;

import android.app.AppGlobals;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.UserManager;
import android.support.annotation.Keep;
import com.android.settings.accounts.AccountFeatureProvider;
import com.android.settings.accounts.AccountFeatureProviderImpl;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.applications.ApplicationFeatureProviderImpl;
import com.android.settings.bluetooth.BluetoothFeatureProvider;
import com.android.settings.bluetooth.BluetoothFeatureProviderImpl;
import com.android.settings.connecteddevice.dock.DockUpdaterFeatureProviderImpl;
import com.android.settings.dashboard.DashboardFeatureProvider;
import com.android.settings.dashboard.DashboardFeatureProviderImpl;
import com.android.settings.dashboard.suggestions.SuggestionFeatureProvider;
import com.android.settings.dashboard.suggestions.SuggestionFeatureProviderImpl;
import com.android.settings.enterprise.EnterprisePrivacyFeatureProvider;
import com.android.settings.enterprise.EnterprisePrivacyFeatureProviderImpl;
import com.android.settings.fuelgauge.PowerUsageFeatureProvider;
import com.android.settings.fuelgauge.PowerUsageFeatureProviderImpl;
import com.android.settings.gestures.AssistGestureFeatureProvider;
import com.android.settings.gestures.AssistGestureFeatureProviderImpl;
import com.android.settings.localepicker.LocaleFeatureProvider;
import com.android.settings.localepicker.LocaleFeatureProviderImpl;
import com.android.settings.search.DeviceIndexFeatureProvider;
import com.android.settings.search.DeviceIndexFeatureProviderImpl;
import com.android.settings.search.SearchFeatureProvider;
import com.android.settings.search.SearchFeatureProviderImpl;
import com.android.settings.security.SecurityFeatureProvider;
import com.android.settings.security.SecurityFeatureProviderImpl;
import com.android.settings.slices.SlicesFeatureProvider;
import com.android.settings.slices.SlicesFeatureProviderImpl;
import com.android.settings.users.UserFeatureProvider;
import com.android.settings.users.UserFeatureProviderImpl;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.wrapper.PackageManagerWrapper;

@Keep
public class FeatureFactoryImpl extends FeatureFactory {
    private AccountFeatureProvider mAccountFeatureProvider;
    private ApplicationFeatureProvider mApplicationFeatureProvider;
    private AssistGestureFeatureProvider mAssistGestureFeatureProvider;
    private BluetoothFeatureProvider mBluetoothFeatureProvider;
    private DashboardFeatureProviderImpl mDashboardFeatureProvider;
    private DeviceIndexFeatureProviderImpl mDeviceIndexFeatureProvider;
    private DockUpdaterFeatureProvider mDockUpdaterFeatureProvider;
    private EnterprisePrivacyFeatureProvider mEnterprisePrivacyFeatureProvider;
    private LocaleFeatureProvider mLocaleFeatureProvider;
    private MetricsFeatureProvider mMetricsFeatureProvider;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;
    private SearchFeatureProvider mSearchFeatureProvider;
    private SecurityFeatureProvider mSecurityFeatureProvider;
    private SlicesFeatureProvider mSlicesFeatureProvider;
    private SuggestionFeatureProvider mSuggestionFeatureProvider;
    private UserFeatureProvider mUserFeatureProvider;

    public SupportFeatureProvider getSupportFeatureProvider(Context context) {
        return null;
    }

    public MetricsFeatureProvider getMetricsFeatureProvider() {
        if (this.mMetricsFeatureProvider == null) {
            this.mMetricsFeatureProvider = new MetricsFeatureProvider();
        }
        return this.mMetricsFeatureProvider;
    }

    public PowerUsageFeatureProvider getPowerUsageFeatureProvider(Context context) {
        if (this.mPowerUsageFeatureProvider == null) {
            this.mPowerUsageFeatureProvider = new PowerUsageFeatureProviderImpl(context);
        }
        return this.mPowerUsageFeatureProvider;
    }

    public DashboardFeatureProvider getDashboardFeatureProvider(Context context) {
        if (this.mDashboardFeatureProvider == null) {
            this.mDashboardFeatureProvider = new DashboardFeatureProviderImpl(context);
        }
        return this.mDashboardFeatureProvider;
    }

    public DockUpdaterFeatureProvider getDockUpdaterFeatureProvider() {
        if (this.mDockUpdaterFeatureProvider == null) {
            this.mDockUpdaterFeatureProvider = new DockUpdaterFeatureProviderImpl();
        }
        return this.mDockUpdaterFeatureProvider;
    }

    public ApplicationFeatureProvider getApplicationFeatureProvider(Context context) {
        if (this.mApplicationFeatureProvider == null) {
            this.mApplicationFeatureProvider = new ApplicationFeatureProviderImpl(context, new PackageManagerWrapper(context.getPackageManager()), AppGlobals.getPackageManager(), (DevicePolicyManager) context.getSystemService("device_policy"));
        }
        return this.mApplicationFeatureProvider;
    }

    public LocaleFeatureProvider getLocaleFeatureProvider() {
        if (this.mLocaleFeatureProvider == null) {
            this.mLocaleFeatureProvider = new LocaleFeatureProviderImpl();
        }
        return this.mLocaleFeatureProvider;
    }

    public EnterprisePrivacyFeatureProvider getEnterprisePrivacyFeatureProvider(Context context) {
        if (this.mEnterprisePrivacyFeatureProvider == null) {
            this.mEnterprisePrivacyFeatureProvider = new EnterprisePrivacyFeatureProviderImpl(context, (DevicePolicyManager) context.getSystemService("device_policy"), new PackageManagerWrapper(context.getPackageManager()), UserManager.get(context), (ConnectivityManager) context.getSystemService("connectivity"), context.getResources());
        }
        return this.mEnterprisePrivacyFeatureProvider;
    }

    public SearchFeatureProvider getSearchFeatureProvider() {
        if (this.mSearchFeatureProvider == null) {
            this.mSearchFeatureProvider = new SearchFeatureProviderImpl();
        }
        return this.mSearchFeatureProvider;
    }

    public SurveyFeatureProvider getSurveyFeatureProvider(Context context) {
        return null;
    }

    public SecurityFeatureProvider getSecurityFeatureProvider() {
        if (this.mSecurityFeatureProvider == null) {
            this.mSecurityFeatureProvider = new SecurityFeatureProviderImpl();
        }
        return this.mSecurityFeatureProvider;
    }

    public SuggestionFeatureProvider getSuggestionFeatureProvider(Context context) {
        if (this.mSuggestionFeatureProvider == null) {
            this.mSuggestionFeatureProvider = new SuggestionFeatureProviderImpl(context);
        }
        return this.mSuggestionFeatureProvider;
    }

    public UserFeatureProvider getUserFeatureProvider(Context context) {
        if (this.mUserFeatureProvider == null) {
            this.mUserFeatureProvider = new UserFeatureProviderImpl(context);
        }
        return this.mUserFeatureProvider;
    }

    public BluetoothFeatureProvider getBluetoothFeatureProvider(Context context) {
        if (this.mBluetoothFeatureProvider == null) {
            this.mBluetoothFeatureProvider = new BluetoothFeatureProviderImpl();
        }
        return this.mBluetoothFeatureProvider;
    }

    public AssistGestureFeatureProvider getAssistGestureFeatureProvider() {
        if (this.mAssistGestureFeatureProvider == null) {
            this.mAssistGestureFeatureProvider = new AssistGestureFeatureProviderImpl();
        }
        return this.mAssistGestureFeatureProvider;
    }

    public SlicesFeatureProvider getSlicesFeatureProvider() {
        if (this.mSlicesFeatureProvider == null) {
            this.mSlicesFeatureProvider = new SlicesFeatureProviderImpl();
        }
        return this.mSlicesFeatureProvider;
    }

    public AccountFeatureProvider getAccountFeatureProvider() {
        if (this.mAccountFeatureProvider == null) {
            this.mAccountFeatureProvider = new AccountFeatureProviderImpl();
        }
        return this.mAccountFeatureProvider;
    }

    public DeviceIndexFeatureProvider getDeviceIndexFeatureProvider() {
        if (this.mDeviceIndexFeatureProvider == null) {
            this.mDeviceIndexFeatureProvider = new DeviceIndexFeatureProviderImpl();
        }
        return this.mDeviceIndexFeatureProvider;
    }
}
