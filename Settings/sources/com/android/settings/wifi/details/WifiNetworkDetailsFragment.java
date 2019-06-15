package com.android.settings.wifi.details;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.wifi.WifiDialog;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.wifi.AccessPoint;
import java.util.ArrayList;
import java.util.List;

public class WifiNetworkDetailsFragment extends DashboardFragment {
    private static final String TAG = "WifiNetworkDetailsFrg";
    private AccessPoint mAccessPoint;
    private WifiDetailPreferenceController mWifiDetailPreferenceController;

    public void onAttach(Context context) {
        this.mAccessPoint = new AccessPoint(context, getArguments());
        super.onAttach(context);
    }

    public int getMetricsCategory() {
        return 849;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.wifi_network_details_fragment;
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == 1) {
            return 603;
        }
        return 0;
    }

    public Dialog onCreateDialog(int dialogId) {
        if (getActivity() == null || this.mWifiDetailPreferenceController == null || this.mAccessPoint == null) {
            return null;
        }
        return WifiDialog.createModal(getActivity(), this.mWifiDetailPreferenceController, this.mAccessPoint, 2);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0, 1, 0, R.string.wifi_modify);
        item.setIcon(R.drawable.ic_menu_edit);
        item.setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 1) {
            return super.onOptionsItemSelected(menuItem);
        }
        if (this.mWifiDetailPreferenceController.canModifyNetwork()) {
            showDialog(1);
        } else {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtils.getDeviceOwner(getContext()));
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mWifiDetailPreferenceController = WifiDetailPreferenceController.newInstance(this.mAccessPoint, (ConnectivityManager) context.getSystemService(ConnectivityManager.class), context, this, new Handler(Looper.getMainLooper()), getLifecycle(), (WifiManager) context.getSystemService(WifiManager.class), this.mMetricsFeatureProvider);
        controllers.add(this.mWifiDetailPreferenceController);
        controllers.add(new WifiMeteredPreferenceController(context, this.mAccessPoint.getConfig(), getLifecycle()));
        return controllers;
    }
}
