package com.android.settings.nfc;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import com.android.internal.content.PackageMonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PaymentBackend {
    public static final String TAG = "Settings.PaymentBackend";
    private Fragment fragment;
    private final NfcAdapter mAdapter;
    private ArrayList<PaymentAppInfo> mAppInfos;
    private ArrayList<Callback> mCallbacks = new ArrayList();
    private final CardEmulation mCardEmuManager;
    private final Context mContext;
    private PaymentAppInfo mDefaultAppInfo;
    private final Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            PaymentBackend.this.refresh();
        }
    };
    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor(this, null);

    public interface Callback {
        void onPaymentAppsChanged();
    }

    public static class PaymentAppInfo {
        Drawable banner;
        public ComponentName componentName;
        CharSequence description;
        boolean isDefault;
        public CharSequence label;
        public ComponentName settingsComponent;
    }

    private class SettingsPackageMonitor extends PackageMonitor {
        private SettingsPackageMonitor() {
        }

        /* synthetic */ SettingsPackageMonitor(PaymentBackend x0, AnonymousClass1 x1) {
            this();
        }

        public void onPackageAdded(String packageName, int uid) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageAppeared(String packageName, int reason) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageRemoved(String packageName, int uid) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }
    }

    public PaymentBackend(Context context) {
        this.mContext = context;
        this.mAdapter = NfcAdapter.getDefaultAdapter(context);
        this.mCardEmuManager = CardEmulation.getInstance(this.mAdapter);
        refresh();
    }

    public void onPause() {
        this.mSettingsPackageMonitor.unregister();
    }

    public void onResume() {
        this.mSettingsPackageMonitor.register(this.mContext, this.mContext.getMainLooper(), false);
        refresh();
    }

    public void refresh() {
        PackageManager pm = this.mContext.getPackageManager();
        List<ApduServiceInfo> serviceInfos = this.mCardEmuManager.getServices("payment");
        if (this.fragment == null || (this.fragment.getActivity() != null && this.fragment.isAdded())) {
            ArrayList<PaymentAppInfo> appInfos = new ArrayList();
            if (serviceInfos == null) {
                makeCallbacks();
                return;
            }
            ComponentName defaultAppName = getDefaultPaymentApp();
            PaymentAppInfo foundDefaultApp = null;
            for (ApduServiceInfo service : serviceInfos) {
                PaymentAppInfo appInfo = new PaymentAppInfo();
                appInfo.label = service.loadLabel(pm);
                if (appInfo.label == null) {
                    appInfo.label = service.loadAppLabel(pm);
                }
                appInfo.isDefault = service.getComponent().equals(defaultAppName);
                if (appInfo.isDefault) {
                    foundDefaultApp = appInfo;
                }
                appInfo.componentName = service.getComponent();
                String settingsActivity = service.getSettingsActivityName();
                if (settingsActivity != null) {
                    appInfo.settingsComponent = new ComponentName(appInfo.componentName.getPackageName(), settingsActivity);
                } else {
                    appInfo.settingsComponent = null;
                }
                appInfo.description = service.getDescription();
                appInfo.banner = service.loadBanner(pm);
                appInfos.add(appInfo);
            }
            this.mAppInfos = appInfos;
            this.mDefaultAppInfo = foundDefaultApp;
            makeCallbacks();
        }
    }

    public void registerCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void unregisterCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public List<PaymentAppInfo> getPaymentAppInfos() {
        return this.mAppInfos;
    }

    public PaymentAppInfo getDefaultApp() {
        return this.mDefaultAppInfo;
    }

    /* Access modifiers changed, original: 0000 */
    public void makeCallbacks() {
        Iterator it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            ((Callback) it.next()).onPaymentAppsChanged();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Drawable loadDrawableForPackage(String pkgName, int drawableResId) {
        try {
            return this.mContext.getPackageManager().getResourcesForApplication(pkgName).getDrawable(drawableResId);
        } catch (NotFoundException e) {
            return null;
        } catch (NameNotFoundException e2) {
            return null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isForegroundMode() {
        boolean z = false;
        try {
            if (Secure.getInt(this.mContext.getContentResolver(), "nfc_payment_foreground") != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setForegroundMode(boolean foreground) {
        Secure.putInt(this.mContext.getContentResolver(), "nfc_payment_foreground", foreground);
    }

    /* Access modifiers changed, original: 0000 */
    public ComponentName getDefaultPaymentApp() {
        String componentString = Secure.getString(this.mContext.getContentResolver(), "nfc_payment_default_component");
        if (componentString != null) {
            return ComponentName.unflattenFromString(componentString);
        }
        return null;
    }

    public void setDefaultPaymentApp(ComponentName app) {
        Secure.putString(this.mContext.getContentResolver(), "nfc_payment_default_component", app != null ? app.flattenToString() : "");
        refresh();
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}
