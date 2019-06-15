package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class UsbDetailsFragment extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.usb_details_fragment;
            return Lists.newArrayList(new SearchIndexableResource[]{res});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return super.getNonIndexableKeys(context);
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return new ArrayList(UsbDetailsFragment.createControllerList(context, new UsbBackend(context), null));
        }
    };
    private static final String TAG = UsbDetailsFragment.class.getSimpleName();
    private List<UsbDetailsController> mControllers;
    private UsbBackend mUsbBackend;
    private UsbConnectionListener mUsbConnectionListener = new -$$Lambda$UsbDetailsFragment$0qs6NXPaSCNUBBPVeTrwViGe6pk(this);
    @VisibleForTesting
    UsbConnectionBroadcastReceiver mUsbReceiver;

    public static /* synthetic */ void lambda$new$0(UsbDetailsFragment usbDetailsFragment, boolean connected, long functions, int powerRole, int dataRole) {
        for (UsbDetailsController controller : usbDetailsFragment.mControllers) {
            controller.refresh(connected, functions, powerRole, dataRole);
        }
    }

    public int getMetricsCategory() {
        return 1291;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.usb_details_fragment;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
    }

    public boolean isConnected() {
        return this.mUsbReceiver.isConnected();
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mUsbBackend = new UsbBackend(context);
        this.mControllers = createControllerList(context, this.mUsbBackend, this);
        this.mUsbReceiver = new UsbConnectionBroadcastReceiver(context, this.mUsbConnectionListener, this.mUsbBackend);
        getLifecycle().addObserver(this.mUsbReceiver);
        return new ArrayList(this.mControllers);
    }

    private static List<UsbDetailsController> createControllerList(Context context, UsbBackend usbBackend, UsbDetailsFragment fragment) {
        List<UsbDetailsController> ret = new ArrayList();
        ret.add(new UsbDetailsHeaderController(context, fragment, usbBackend));
        ret.add(new UsbDetailsDataRoleController(context, fragment, usbBackend));
        ret.add(new UsbDetailsFunctionsController(context, fragment, usbBackend));
        ret.add(new UsbDetailsPowerRoleController(context, fragment, usbBackend));
        return ret;
    }
}
