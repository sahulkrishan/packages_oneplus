package com.android.settings.deviceinfo.aboutphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.view.View;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.bluetooth.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController;
import com.android.settings.deviceinfo.BrandNamePreferenceController;
import com.android.settings.deviceinfo.BrandedAccountPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.DeviceModelPreferenceController;
import com.android.settings.deviceinfo.DeviceNamePreferenceController;
import com.android.settings.deviceinfo.DeviceNamePreferenceController.DeviceNamePreferenceHost;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.IpAddressPreferenceController;
import com.android.settings.deviceinfo.ManualPreferenceController;
import com.android.settings.deviceinfo.PhoneNumberPreferenceController;
import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController;
import com.android.settings.deviceinfo.SafetyInfoPreferenceController;
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionPreferenceController;
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.OPCarrierConfigVersionPreferenceController;
import com.oneplus.settings.aboutphone.OPBaseBandPreferenceController;
import com.oneplus.settings.aboutphone.OPKernelVersionPreferenceController;
import com.oneplus.settings.aboutphone.OPSerialNumberPreferenceController;
import com.oneplus.settings.aboutphone.OPUptimePreferenceController;
import com.oneplus.settings.product.OPAuthenticationInformationPreferenceController;
import com.oneplus.settings.product.OPDDRInfoController;
import com.oneplus.settings.product.OPMemoryInfoController;
import com.oneplus.settings.product.OPProductInfoPreferenceController;
import com.oneplus.settings.product.OPVersionInfoController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDeviceInfoFragment extends DashboardFragment implements DeviceNamePreferenceHost {
    private static final String KEY_DDR_MEMORY_CAPACITY = "ddr_memory_capacity";
    private static final String KEY_LEGAL_CONTAINER = "legal_container";
    private static final String KEY_MANUAL = "manual";
    private static final String KEY_MBN_VERSION = "mbn_version";
    private static final String KEY_MEMORY_CAPACITY = "memory_capacity";
    private static final String KEY_MOBILE_DEVICE_NAME = "mobile_device_name";
    private static final String KEY_MY_DEVICE_INFO_HEADER = "my_device_info_header";
    private static final String KEY_ONEPLUS_AUTHENTICATION_INFORMATION = "oneplus_authentication_information";
    private static final String KEY_ONEPLUS_OOS_VERSION = "oneplus_oos_version";
    private static final String KEY_ONEPLUS_PRE_APPLICATION = "oneplus_pre_application";
    private static final String KEY_ONEPLUS_PRODUCT_INFO = "oneplus_product_info";
    private static final String KEY_OP_ELECTRONIC_CARD = "op_electronic_card";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_STATUS_INFO = "status_info";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String LOG_TAG = "MyDeviceInfoFragment";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.my_device_info;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return MyDeviceInfoFragment.buildPreferenceControllers(context, null, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(MyDeviceInfoFragment.KEY_LEGAL_CONTAINER);
            String contry = SystemProperties.get("ro.rf_version");
            keys.add(MyDeviceInfoFragment.KEY_MBN_VERSION);
            keys.add(MyDeviceInfoFragment.KEY_SYSTEM_UPDATE_SETTINGS);
            if (!context.getPackageManager().hasSystemFeature("oem.authentication_information.support") || Build.MODEL.equalsIgnoreCase(MyDeviceInfoFragment.ONEPLUS_A6003) || Build.MODEL.equalsIgnoreCase(context.getString(R.string.oneplus_model_for_europe_and_america))) {
                keys.add(MyDeviceInfoFragment.KEY_ONEPLUS_AUTHENTICATION_INFORMATION);
            } else if (contry.contains("Eu") || contry.contains("In") || contry.contains("Am")) {
                keys.add(MyDeviceInfoFragment.KEY_ONEPLUS_AUTHENTICATION_INFORMATION);
            }
            Intent intent = new Intent("android.settings.SHOW_REGULATORY_INFO");
            String model = SystemProperties.get("ro.product.model");
            if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty() || !context.getResources().getBoolean(R.bool.config_show_regulatory_info) || model.contains("A3003") || !contry.contains("Am")) {
                keys.add(MyDeviceInfoFragment.KEY_REGULATORY_INFO);
            } else if (contry.contains("Eu") || contry.contains("In") || contry.contains("Ch")) {
                keys.add(MyDeviceInfoFragment.KEY_REGULATORY_INFO);
            }
            keys.add(MyDeviceInfoFragment.KEY_MANUAL);
            keys.add(MyDeviceInfoFragment.KEY_OP_ELECTRONIC_CARD);
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$MyDeviceInfoFragment$pzCelMuIMGm16asu34w_Ge8IYsk.INSTANCE;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(SummaryLoader summaryLoader) {
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, DeviceModelPreferenceController.getDeviceModel());
            }
        }
    }

    public int getMetricsCategory() {
        return 40;
    }

    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    public void onResume() {
        super.onResume();
        initHeader();
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return LOG_TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Activity activity, MyDeviceInfoFragment fragment, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new PhoneNumberPreferenceController(context));
        controllers.add(new BrandedAccountPreferenceController(context));
        DeviceNamePreferenceController deviceNamePreferenceController = new DeviceNamePreferenceController(context);
        deviceNamePreferenceController.setLocalBluetoothManager(Utils.getLocalBtManager(context));
        deviceNamePreferenceController.setHost(fragment);
        if (lifecycle != null) {
            lifecycle.addObserver(deviceNamePreferenceController);
        }
        controllers.add(deviceNamePreferenceController);
        controllers.add(new SimStatusPreferenceController(context, fragment));
        controllers.add(new DeviceModelPreferenceController(context, fragment));
        controllers.add(new ImeiInfoPreferenceController(context, fragment));
        controllers.add(new FirmwareVersionPreferenceController(context, fragment));
        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        controllers.add(new OPAuthenticationInformationPreferenceController(context));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new BuildNumberPreferenceController(context, activity, fragment, lifecycle));
        controllers.add(new OPProductInfoPreferenceController(context));
        controllers.add(new OPVersionInfoController(context));
        controllers.add(new OPDDRInfoController(context));
        controllers.add(new OPMemoryInfoController(context));
        controllers.add(new OPSerialNumberPreferenceController(context));
        controllers.add(new OPBaseBandPreferenceController(context));
        controllers.add(new OPKernelVersionPreferenceController(context));
        controllers.add(new OPUptimePreferenceController(context, lifecycle));
        controllers.add(new OPCarrierConfigVersionPreferenceController(context));
        controllers.add(new BrandNamePreferenceController(context));
        return controllers;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!((BuildNumberPreferenceController) use(BuildNumberPreferenceController.class)).onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initHeader() {
        LayoutPreference headerPreference = (LayoutPreference) getPreferenceScreen().findPreference(KEY_MY_DEVICE_INFO_HEADER);
        View appSnippet = headerPreference.findViewById(R.id.entity_header);
        Activity context = getActivity();
        Bundle bundle = getArguments();
        EntityHeaderController controller = EntityHeaderController.newInstance(context, this, appSnippet).setRecyclerView(getListView(), getLifecycle()).setButtonActions(0, 0);
        if (bundle.getInt("icon_id", 0) == 0) {
            UserManager userManager = (UserManager) getActivity().getSystemService("user");
            UserInfo info = com.android.settings.Utils.getExistingUser(userManager, Process.myUserHandle());
            controller.setLabel(info.name);
            controller.setIcon(com.android.settingslib.Utils.getUserIcon(getActivity(), userManager, info));
        }
        controller.done(context, true);
        headerPreference.setVisible(false);
    }

    public void showDeviceNameWarningDialog(String deviceName) {
        DeviceNameWarningDialog.show(this);
    }

    public void onSetDeviceNameConfirm() {
        ((DeviceNamePreferenceController) use(DeviceNamePreferenceController.class)).confirmDeviceName();
    }

    public void onSetDeviceNameCancel() {
        ((DeviceNamePreferenceController) use(DeviceNamePreferenceController.class)).cancelDeviceName();
    }
}
