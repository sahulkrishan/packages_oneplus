package com.android.settings.network;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import java.util.List;

public class MobilePlanPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnSaveInstanceState {
    private static final String KEY_MANAGE_MOBILE_PLAN = "manage_mobile_plan";
    public static final int MANAGE_MOBILE_PLAN_DIALOG_ID = 1;
    private static final String SAVED_MANAGE_MOBILE_PLAN_MSG = "mManageMobilePlanMessage";
    private static final String TAG = "MobilePlanPrefContr";
    private ConnectivityManager mCm;
    private final MobilePlanPreferenceHost mHost;
    private final boolean mIsSecondaryUser = (this.mUserManager.isAdminUser() ^ 1);
    private String mMobilePlanDialogMessage;
    private TelephonyManager mTm;
    private final UserManager mUserManager;

    public interface MobilePlanPreferenceHost {
        void showMobilePlanMessageDialog();
    }

    public MobilePlanPreferenceController(Context context, MobilePlanPreferenceHost host) {
        super(context);
        this.mHost = host;
        this.mCm = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTm = (TelephonyManager) context.getSystemService("phone");
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (this.mHost != null && KEY_MANAGE_MOBILE_PLAN.equals(preference.getKey())) {
            this.mMobilePlanDialogMessage = null;
            onManageMobilePlanClick();
        }
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mMobilePlanDialogMessage = savedInstanceState.getString(SAVED_MANAGE_MOBILE_PLAN_MSG);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onCreate: mMobilePlanDialogMessage=");
        stringBuilder.append(this.mMobilePlanDialogMessage);
        Log.d(str, stringBuilder.toString());
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(this.mMobilePlanDialogMessage)) {
            outState.putString(SAVED_MANAGE_MOBILE_PLAN_MSG, this.mMobilePlanDialogMessage);
        }
    }

    public String getMobilePlanDialogMessage() {
        return this.mMobilePlanDialogMessage;
    }

    public void setMobilePlanDialogMessage(String messasge) {
        this.mMobilePlanDialogMessage = messasge;
    }

    public boolean isAvailable() {
        boolean isPrefAllowedOnDevice = this.mContext.getResources().getBoolean(R.bool.config_show_mobile_plan);
        boolean isPrefAllowedForUser = (this.mIsSecondaryUser || Utils.isWifiOnly(this.mContext) || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_mobile_networks", UserHandle.myUserId())) ? false : true;
        if (isPrefAllowedForUser && isPrefAllowedOnDevice) {
            return true;
        }
        return false;
    }

    public String getPreferenceKey() {
        return KEY_MANAGE_MOBILE_PLAN;
    }

    private void onManageMobilePlanClick() {
        Resources resources = this.mContext.getResources();
        NetworkInfo ni = this.mCm.getActiveNetworkInfo();
        if (this.mTm.hasIccCard() && ni != null) {
            Intent provisioningIntent = new Intent("android.intent.action.CARRIER_SETUP");
            List<String> carrierPackages = this.mTm.getCarrierPackageNamesForIntent(provisioningIntent);
            if (carrierPackages == null || carrierPackages.isEmpty()) {
                String url = this.mCm.getMobileProvisioningUrl();
                if (TextUtils.isEmpty(url)) {
                    if (TextUtils.isEmpty(this.mTm.getSimOperatorName())) {
                        if (TextUtils.isEmpty(this.mTm.getNetworkOperatorName())) {
                            this.mMobilePlanDialogMessage = resources.getString(R.string.mobile_unknown_sim_operator);
                        } else {
                            this.mMobilePlanDialogMessage = resources.getString(R.string.mobile_no_provisioning_url, new Object[]{this.mTm.getNetworkOperatorName()});
                        }
                    } else {
                        this.mMobilePlanDialogMessage = resources.getString(R.string.mobile_no_provisioning_url, new Object[]{this.mTm.getSimOperatorName()});
                    }
                } else {
                    Intent intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
                    intent.setData(Uri.parse(url));
                    intent.setFlags(272629760);
                    try {
                        this.mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("onManageMobilePlanClick: startActivity failed");
                        stringBuilder.append(e);
                        Log.w(str, stringBuilder.toString());
                    }
                }
            } else {
                if (carrierPackages.size() != 1) {
                    Log.w(TAG, "Multiple matching carrier apps found, launching the first.");
                }
                provisioningIntent.setPackage((String) carrierPackages.get(0));
                this.mContext.startActivity(provisioningIntent);
                return;
            }
        } else if (this.mTm.hasIccCard()) {
            this.mMobilePlanDialogMessage = resources.getString(R.string.mobile_connect_to_internet);
        } else {
            this.mMobilePlanDialogMessage = resources.getString(R.string.mobile_insert_sim_card);
        }
        if (!TextUtils.isEmpty(this.mMobilePlanDialogMessage)) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("onManageMobilePlanClick: message=");
            stringBuilder2.append(this.mMobilePlanDialogMessage);
            Log.d(str2, stringBuilder2.toString());
            if (this.mHost != null) {
                this.mHost.showMobilePlanMessageDialog();
            } else {
                Log.d(TAG, "Missing host fragment, cannot show message dialog.");
            }
        }
    }
}
