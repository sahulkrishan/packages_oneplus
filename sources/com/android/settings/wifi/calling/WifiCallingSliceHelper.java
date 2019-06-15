package com.android.settings.wifi.calling;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.PersistableBundle;
import android.support.v4.graphics.drawable.IconCompat;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.slices.SettingsSliceProvider;
import com.android.settings.slices.SliceBroadcastReceiver;
import com.android.settings.slices.SliceBuilderUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WifiCallingSliceHelper {
    public static final String ACTION_WIFI_CALLING_CHANGED = "com.android.settings.wifi.calling.action.WIFI_CALLING_CHANGED";
    public static final String ACTION_WIFI_CALLING_SETTINGS_ACTIVITY = "android.settings.WIFI_CALLING_SETTINGS";
    public static final String PATH_WIFI_CALLING = "wifi_calling";
    private static final String TAG = "WifiCallingSliceHelper";
    private static final int TIMEOUT_MILLIS = 2000;
    public static final Uri WIFI_CALLING_URI = new Builder().scheme("content").authority(SettingsSliceProvider.SLICE_AUTHORITY).appendPath(PATH_WIFI_CALLING).build();
    private final Context mContext;
    protected SubscriptionManager mSubscriptionManager;

    @VisibleForTesting
    public WifiCallingSliceHelper(Context context) {
        this.mContext = context;
    }

    public Slice createWifiCallingSlice(Uri sliceUri) {
        int subId = getDefaultVoiceSubId();
        String carrierName = getSimCarrierName();
        if (subId <= -1) {
            Log.d(TAG, "Invalid subscription Id");
            return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_not_supported, new Object[]{carrierName}), sliceUri, getSettingsIntent(this.mContext));
        }
        ImsManager imsManager = getImsManager(subId);
        if (imsManager.isWfcEnabledByPlatform() && imsManager.isWfcProvisionedOnDevice()) {
            try {
                boolean isWifiCallingEnabled = isWifiCallingEnabled(imsManager);
                if (getWifiCallingCarrierActivityIntent(subId) == null || isWifiCallingEnabled) {
                    return getWifiCallingSlice(sliceUri, this.mContext, isWifiCallingEnabled);
                }
                Log.d(TAG, "Needs Activation");
                return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_settings_activation_instructions), sliceUri, getActivityIntent(ACTION_WIFI_CALLING_SETTINGS_ACTIVITY));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.e(TAG, "Unable to read the current WiFi calling status", e);
                return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_turn_on), sliceUri, getActivityIntent(ACTION_WIFI_CALLING_SETTINGS_ACTIVITY));
            }
        }
        Log.d(TAG, "Wifi calling is either not provisioned or not enabled by Platform");
        return getNonActionableWifiCallingSlice(this.mContext.getString(R.string.wifi_calling_settings_title), this.mContext.getString(R.string.wifi_calling_not_supported, new Object[]{carrierName}), sliceUri, getSettingsIntent(this.mContext));
    }

    private boolean isWifiCallingEnabled(final ImsManager imsManager) throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<Boolean> isWifiOnTask = new FutureTask(new Callable<Boolean>() {
            public Boolean call() {
                return Boolean.valueOf(imsManager.isWfcEnabledByUser());
            }
        });
        Executors.newSingleThreadExecutor().execute(isWifiOnTask);
        Boolean isWifiEnabledByUser = Boolean.valueOf(false);
        if (((Boolean) isWifiOnTask.get(2000, TimeUnit.MILLISECONDS)).booleanValue() && imsManager.isNonTtyOrTtyOnVolteEnabled()) {
            return true;
        }
        return false;
    }

    private Slice getWifiCallingSlice(Uri sliceUri, Context mContext, boolean isWifiCallingEnabled) {
        IconCompat icon = IconCompat.createWithResource(mContext, (int) R.drawable.wifi_signal);
        return new ListBuilder(mContext, sliceUri, -1).setColor(R.color.material_blue_500).addRow(new -$$Lambda$WifiCallingSliceHelper$zbtZymRgbM5OtQTuVraAeUKJDfQ(this, mContext.getString(R.string.wifi_calling_settings_title), isWifiCallingEnabled, icon)).build();
    }

    /* Access modifiers changed, original: protected */
    public ImsManager getImsManager(int subId) {
        return ImsManager.getInstance(this.mContext, SubscriptionManager.getPhoneId(subId));
    }

    private Integer getWfcMode(final ImsManager imsManager) throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<Integer> wfcModeTask = new FutureTask(new Callable<Integer>() {
            public Integer call() {
                return Integer.valueOf(imsManager.getWfcMode(false));
            }
        });
        Executors.newSingleThreadExecutor().execute(wfcModeTask);
        return (Integer) wfcModeTask.get(2000, TimeUnit.MILLISECONDS);
    }

    public void handleWifiCallingChanged(Intent intent) {
        int subId = getDefaultVoiceSubId();
        if (subId > -1) {
            ImsManager imsManager = getImsManager(subId);
            if (imsManager.isWfcEnabledByPlatform() || imsManager.isWfcProvisionedOnDevice()) {
                boolean currentValue = imsManager.isWfcEnabledByUser() && imsManager.isNonTtyOrTtyOnVolteEnabled();
                boolean newValue = intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", currentValue);
                Intent activationAppIntent = getWifiCallingCarrierActivityIntent(subId);
                if ((!newValue || activationAppIntent == null) && newValue != currentValue) {
                    imsManager.setWfcSetting(newValue);
                }
            }
        }
        this.mContext.getContentResolver().notifyChange(SliceBuilderUtils.getUri(PATH_WIFI_CALLING, false), null);
    }

    private Slice getNonActionableWifiCallingSlice(String title, String subtitle, Uri sliceUri, PendingIntent primaryActionIntent) {
        return new ListBuilder(this.mContext, sliceUri, -1).setColor(R.color.material_blue_500).addRow(new -$$Lambda$WifiCallingSliceHelper$6JNBI7DQgipwzIQhGGlqsYB5PlI(title, subtitle, primaryActionIntent, IconCompat.createWithResource(this.mContext, (int) R.drawable.wifi_signal))).build();
    }

    private boolean isCarrierConfigManagerKeyEnabled(Context mContext, String key, int subId, boolean defaultValue) {
        CarrierConfigManager configManager = getCarrierConfigManager(mContext);
        if (configManager == null) {
            return false;
        }
        PersistableBundle bundle = configManager.getConfigForSubId(subId);
        if (bundle != null) {
            return bundle.getBoolean(key, defaultValue);
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public CarrierConfigManager getCarrierConfigManager(Context mContext) {
        return (CarrierConfigManager) mContext.getSystemService(CarrierConfigManager.class);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultVoiceSubId() {
        if (this.mSubscriptionManager == null) {
            this.mSubscriptionManager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
        }
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }

    /* Access modifiers changed, original: protected */
    public Intent getWifiCallingCarrierActivityIntent(int subId) {
        CarrierConfigManager configManager = getCarrierConfigManager(this.mContext);
        if (configManager == null) {
            return null;
        }
        PersistableBundle bundle = configManager.getConfigForSubId(subId);
        if (bundle == null) {
            return null;
        }
        String carrierApp = bundle.getString("wfc_emergency_address_carrier_app_string");
        if (TextUtils.isEmpty(carrierApp)) {
            return null;
        }
        ComponentName componentName = ComponentName.unflattenFromString(carrierApp);
        if (componentName == null) {
            return null;
        }
        Intent intent = new Intent();
        intent.setComponent(componentName);
        return intent;
    }

    public static PendingIntent getSettingsIntent(Context context) {
        return PendingIntent.getActivity(context, 0, new Intent("android.settings.SETTINGS"), 0);
    }

    private PendingIntent getBroadcastIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClass(this.mContext, SliceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456);
    }

    private PendingIntent getActivityIntent(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(268435456);
        return PendingIntent.getActivity(this.mContext, 0, intent, 0);
    }

    private String getSimCarrierName() {
        CharSequence carrierName = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getSimCarrierIdName();
        if (carrierName == null) {
            return this.mContext.getString(R.string.carrier);
        }
        return carrierName.toString();
    }
}
