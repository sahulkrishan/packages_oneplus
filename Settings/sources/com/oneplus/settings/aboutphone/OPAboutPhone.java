package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.BidiFormatter;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.deviceinfo.DeviceModelPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.utils.FileSizeFormatter;
import com.oneplus.settings.utils.OPUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OPAboutPhone extends DashboardFragment implements Indexable {
    private static final String KEY_HARDWARE_VIEW = "hardware_view";
    private static final String KEY_SOFT_VIEW_1 = "soft_view_1";
    private static final String KEY_SOFT_VIEW_2 = "soft_view_2";
    private static final String KEY_SOFT_VIEW_3 = "soft_view_3";
    private static final String KEY_SOFT_VIEW_4 = "soft_view_4";
    private static final String KEY_SOFT_VIEW_5 = "soft_view_5";
    private static final String ONEPLUS_A3000 = "ONEPLUS A3000";
    private static final String ONEPLUS_A3010 = "ONEPLUS A3010";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_about_phone;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$OPAboutPhone$RKK-fwpKlUC1qzXgjTLs813iYKo.INSTANCE;
    private static final String TAG = "OPAboutPhone";
    LayoutPreference SoftWare5Preference;
    private Context mContext;
    OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController3;
    OPAboutPhoneDivider mOPPreferenceDivider;

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

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_about_phone;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.SoftWare5Preference = (LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_5);
        this.mOPPreferenceDivider = (OPAboutPhoneDivider) getPreferenceScreen().findPreference("preference_divider_line_4");
    }

    public String getCpuName() {
        String CPUinfo = "none";
        if (Build.MODEL.startsWith("ONEPLUS A60")) {
            return "Snapdragon™ 845";
        }
        if (Build.MODEL.startsWith("ONEPLUS A50")) {
            return "Snapdragon™ 835";
        }
        if (OPUtils.isOP3T()) {
            return "Snapdragon™ 821";
        }
        if (OPUtils.isOP3()) {
            return "Snapdragon™ 820";
        }
        return CPUinfo;
    }

    public static String showROMStorage() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long size = (((long) statFs.getBlockSize()) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) * ((long) statFs.getBlockCount());
        if (size >= 268435456) {
            return "512GB";
        }
        if (size >= 134217728) {
            return "256GB";
        }
        if (size >= 67108864) {
            return "128GB";
        }
        if (size >= 33554432) {
            return "64GB";
        }
        if (size >= 16777216) {
            return "32GB";
        }
        return "16GB";
    }

    private static String formatMemoryDisplay(long size) {
        long mega = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID * size) / FileSizeFormatter.MEGABYTE_IN_BYTES;
        int mul = (int) (mega / 512);
        int modulus = (int) (mega % 512);
        StringBuilder stringBuilder;
        if (mul == 0) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(mega);
            stringBuilder.append("");
            return stringBuilder.toString();
        } else if (modulus > 256) {
            mul++;
            if (mul % 2 == 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append((int) (0.5f * ((float) mul)));
                stringBuilder.append("");
                return stringBuilder.toString();
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(0.5f * ((float) mul));
            stringBuilder.append("");
            return stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append((0.5f * ((float) mul)) + 0.25f);
            stringBuilder.append("");
            return stringBuilder.toString();
        }
    }

    private static String getTotalMemory() {
        IOException e;
        String str2 = "";
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try {
            fr = new FileReader("/proc/meminfo");
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine().substring(10).trim();
            str2 = str2.substring(0, str2.length() - 2);
            str2 = str2.trim();
            try {
                localBufferedReader.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                fr.close();
            } catch (IOException e3) {
                e2 = e3;
            }
        } catch (IOException e4) {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e5) {
                    e22 = e5;
                }
            }
        } catch (Throwable th) {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e62) {
                    e62.printStackTrace();
                }
            }
        }
        return formatMemoryDisplay(Long.parseLong(str2));
        e22.printStackTrace();
        return formatMemoryDisplay(Long.parseLong(str2));
    }

    private void displayHardWarePreference() {
        OPAboutPhoneHardWareController mOPAboutPhoneHardWareController = OPAboutPhoneHardWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_HARDWARE_VIEW)).findViewById(R.id.phone_hardware_info));
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A6003)) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_6)).done();
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000")) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_5)).done();
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_5t)).done();
        } else if (OPUtils.isOP3()) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_3)).done();
        } else if (OPUtils.isOP3T()) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_3t)).done();
        } else if (isOlder6tProducts()) {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_6)).done();
        } else {
            mOPAboutPhoneHardWareController.setPhoneImage(this.mContext.getDrawable(R.drawable.oneplus_other)).done();
        }
        mOPAboutPhoneHardWareController.setCameraMessage(getCameraInfo());
        mOPAboutPhoneHardWareController.setCpuMessage(getCpuName());
        mOPAboutPhoneHardWareController.setScreenMessage(getScreenInfo());
        int ramsize = (int) Math.ceil((double) Float.valueOf(getTotalMemory()).floatValue());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ramsize);
        stringBuilder.append("GB RAM + ");
        stringBuilder.append(OPUtils.showROMStorage());
        stringBuilder.append(" ROM");
        mOPAboutPhoneHardWareController.setStorageMessage(stringBuilder.toString());
        mOPAboutPhoneHardWareController.done();
    }

    private String getScreenInfo() {
        String screeninfo = "none";
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A6003)) {
            return "Optic AMOLED 6.28” Display";
        }
        if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            return "Optic AMOLED 6.01” Display";
        }
        if (Build.MODEL.contains("A50") || Build.MODEL.contains("A30")) {
            return "Optic AMOLED 5.5” Display";
        }
        if (isOlder6tProducts()) {
            return screeninfo;
        }
        return this.mContext.getString(R.string.other_display_info);
    }

    private String getCameraInfo() {
        String camerainfo = "none";
        if (Build.MODEL.contains("A60") || Build.MODEL.contains("A50")) {
            return "16 + 20 MP Dual Camera";
        }
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A3010)) {
            return this.mContext.getString(R.string.oneplus_3t_camera_info);
        }
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A3000)) {
            return this.mContext.getString(R.string.oneplus_3_camera_info);
        }
        return camerainfo;
    }

    private boolean isSupportRegulatoryLabels() {
        String contry = SystemProperties.get("ro.rf_version");
        return Build.MODEL.equals(getString(R.string.oneplus_model_for_europe_and_america)) || Build.MODEL.equals(ONEPLUS_A6003) || contry.contains("Eu") || contry.contains("Am");
    }

    private boolean isSupportAuthenticationInformation() {
        String contry = SystemProperties.get("ro.rf_version");
        return Build.MODEL.equals(getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equals(ONEPLUS_A6000) || contry.contains("In") || contry.contains("Ch");
    }

    private void showVerificationInfo(OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController) {
        if (Build.MODEL.equals(getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equals(ONEPLUS_A6000) || Build.MODEL.equals("ONEPLUS A5010") || Build.MODEL.equals("ONEPLUS A5000")) {
            if (OPUtils.isO2()) {
                mOPAboutPhoneSoftWareController.setRightIntentString("android.settings.SHOW_REGULATORY_INFO");
                mOPAboutPhoneSoftWareController.setRightTitle(this.mContext.getString(R.string.regulatory_labels));
                return;
            }
            mOPAboutPhoneSoftWareController.setRightIntentString("com.oneplus.intent.OPAuthenticationInformationSettings");
            mOPAboutPhoneSoftWareController.setRightTitle(this.mContext.getString(R.string.oneplus_authentication_information));
        } else if (Build.MODEL.equals(getString(R.string.oneplus_model_for_europe_and_america)) || Build.MODEL.equals(ONEPLUS_A6003)) {
            mOPAboutPhoneSoftWareController.setRightIntentString("android.settings.SHOW_REGULATORY_INFO");
            mOPAboutPhoneSoftWareController.setRightTitle(this.mContext.getString(R.string.regulatory_labels));
        }
    }

    private void displaySoftWare1Preference() {
        OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController1 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_1)).findViewById(R.id.phone_software_info));
        mOPAboutPhoneSoftWareController1.setLeftTitle(this.mContext.getString(R.string.my_device_info_device_name_preference_title));
        mOPAboutPhoneSoftWareController1.setLeftIntentString("com.oneplus.intent.OPDeviceNameActivity");
        mOPAboutPhoneSoftWareController1.setLefImage(this.mContext.getDrawable(R.drawable.op_device_name));
        mOPAboutPhoneSoftWareController1.setLeftSummary(System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename"));
        mOPAboutPhoneSoftWareController1.setRightImage(this.mContext.getDrawable(R.drawable.op_authentication_information));
        showVerificationInfo(mOPAboutPhoneSoftWareController1);
        mOPAboutPhoneSoftWareController1.setRightSummary(this.mContext.getString(R.string.oneplus_regulatory_information));
        mOPAboutPhoneSoftWareController1.done();
    }

    private void displaySoftWare4Preference() {
        OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController4 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_4)).findViewById(R.id.phone_software_info));
        mOPAboutPhoneSoftWareController4.setLeftTitle(this.mContext.getString(R.string.legal_information));
        mOPAboutPhoneSoftWareController4.setLeftIntentString("com.oneplus.intent.LegalSettingsActivity");
        mOPAboutPhoneSoftWareController4.setLefImage(this.mContext.getDrawable(R.drawable.op_legal_settings));
        mOPAboutPhoneSoftWareController4.setLeftSummary(this.mContext.getString(R.string.oneplus_legal_summary));
        mOPAboutPhoneSoftWareController4.setRightImage(this.mContext.getDrawable(R.drawable.op_status_settings));
        mOPAboutPhoneSoftWareController4.setRightTitle(this.mContext.getString(R.string.device_status));
        mOPAboutPhoneSoftWareController4.setRightSummary(this.mContext.getString(R.string.oneplus_status_summary));
        mOPAboutPhoneSoftWareController4.setRightIntentString("com.oneplus.intent.MyDeviceInfoFragmentActivity");
        mOPAboutPhoneSoftWareController4.done();
    }

    private void displaySoftWare2Preference() {
        OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController2 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_2)).findViewById(R.id.phone_software_info));
        mOPAboutPhoneSoftWareController2.setLeftTitle(this.mContext.getString(R.string.firmware_version));
        mOPAboutPhoneSoftWareController2.setLeftIntentString("com.android.FirmwareVersionDialogFragment");
        mOPAboutPhoneSoftWareController2.setLefImage(this.mContext.getDrawable(R.drawable.op_android_version));
        mOPAboutPhoneSoftWareController2.setLeftSummary(VERSION.RELEASE);
        if (OpFeatures.isSupport(new int[]{1})) {
            mOPAboutPhoneSoftWareController2.setRightImage(this.mContext.getDrawable(R.drawable.op_o2_version));
            mOPAboutPhoneSoftWareController2.setRightTitle(this.mContext.getResources().getString(R.string.oneplus_oxygen_version));
            mOPAboutPhoneSoftWareController2.setRightSummary(SystemProperties.get("ro.oxygen.version", this.mContext.getResources().getString(R.string.device_info_default)).replace("O2", "O₂"));
        } else {
            mOPAboutPhoneSoftWareController2.setRightImage(this.mContext.getDrawable(R.drawable.op_h2_version));
            mOPAboutPhoneSoftWareController2.setRightTitle(this.mContext.getResources().getString(R.string.oneplus_hydrogen_version).replace("H2", "H₂"));
            mOPAboutPhoneSoftWareController2.setRightSummary(SystemProperties.get("ro.rom.version", this.mContext.getResources().getString(R.string.device_info_default)).replace("H2", "H₂"));
        }
        mOPAboutPhoneSoftWareController2.setRightIntentString(null);
        mOPAboutPhoneSoftWareController2.done();
    }

    private void displaySoftWare3Preference() {
        this.mOPAboutPhoneSoftWareController3 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, ((LayoutPreference) getPreferenceScreen().findPreference(KEY_SOFT_VIEW_3)).findViewById(R.id.phone_software_info));
        this.mOPAboutPhoneSoftWareController3.setLefImage(this.mContext.getDrawable(R.drawable.op_soft_version));
        this.mOPAboutPhoneSoftWareController3.setLeftTitle(this.mContext.getString(R.string.build_number));
        this.mOPAboutPhoneSoftWareController3.setLeftSummary(BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY));
        this.mOPAboutPhoneSoftWareController3.setLeftIntentString("build.number");
        this.mOPAboutPhoneSoftWareController3.setRightImage(this.mContext.getDrawable(R.drawable.op_model));
        this.mOPAboutPhoneSoftWareController3.setRightTitle(this.mContext.getString(R.string.model_info));
        OPAboutPhoneSoftWareController oPAboutPhoneSoftWareController = this.mOPAboutPhoneSoftWareController3;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ONEPLUS\n");
        stringBuilder.append(Build.MODEL.replaceAll("ONEPLUS ", ""));
        oPAboutPhoneSoftWareController.setRightSummary(stringBuilder.toString());
        this.mOPAboutPhoneSoftWareController3.setRightIntentString(null);
        this.mOPAboutPhoneSoftWareController3.done();
    }

    private void displaySoftWare5Preference() {
        OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController5 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, this.SoftWare5Preference.findViewById(R.id.phone_software_info));
        if (OPUtils.isO2() || !OPUtils.isSurportProductInfo(this.mContext)) {
            mOPAboutPhoneSoftWareController5.setLefImage(this.mContext.getDrawable(R.drawable.op_award_icon));
            mOPAboutPhoneSoftWareController5.setLeftTitle(this.mContext.getString(R.string.oneplus_forum_award_title));
            mOPAboutPhoneSoftWareController5.setLeftSummary(this.mContext.getString(R.string.oneplus_o2_contributors));
            mOPAboutPhoneSoftWareController5.setLeftIntentString("com.oneplus.intent.OPForumContributorsActivity");
            mOPAboutPhoneSoftWareController5.setRightImage(null);
            mOPAboutPhoneSoftWareController5.setRightTitle(null);
            mOPAboutPhoneSoftWareController5.setRightSummary(null);
            mOPAboutPhoneSoftWareController5.setRightIntentString(null);
        } else {
            mOPAboutPhoneSoftWareController5.setLefImage(this.mContext.getDrawable(R.drawable.op_product_info));
            mOPAboutPhoneSoftWareController5.setLeftTitle(this.mContext.getString(R.string.oneplus_product_info));
            mOPAboutPhoneSoftWareController5.setLeftSummary(this.mContext.getString(R.string.oneplus_product_info_summary));
            mOPAboutPhoneSoftWareController5.setLeftIntentString("com.oneplus.action.PRODUCT_INFO");
            mOPAboutPhoneSoftWareController5.setRightImage(this.mContext.getDrawable(R.drawable.op_award_icon));
            mOPAboutPhoneSoftWareController5.setRightTitle(this.mContext.getString(R.string.oneplus_forum_award_title));
            mOPAboutPhoneSoftWareController5.setRightSummary(this.mContext.getString(R.string.oneplus_h2_contributors));
            mOPAboutPhoneSoftWareController5.setRightIntentString("com.oneplus.intent.OPForumContributorsActivity");
        }
        mOPAboutPhoneSoftWareController5.done();
    }

    private void displaynoContributors() {
        if (OPUtils.isO2() || !OPUtils.isSurportProductInfo(this.mContext)) {
            this.SoftWare5Preference.setVisible(false);
            this.mOPPreferenceDivider.setVisible(false);
            return;
        }
        OPAboutPhoneSoftWareController mOPAboutPhoneSoftWareController5 = OPAboutPhoneSoftWareController.newInstance(getActivity(), this, this.SoftWare5Preference.findViewById(R.id.phone_software_info));
        mOPAboutPhoneSoftWareController5.setLefImage(this.mContext.getDrawable(R.drawable.op_product_info));
        mOPAboutPhoneSoftWareController5.setLeftTitle(this.mContext.getString(R.string.oneplus_product_info));
        mOPAboutPhoneSoftWareController5.setLeftSummary(this.mContext.getString(R.string.oneplus_product_info_summary));
        mOPAboutPhoneSoftWareController5.setLeftIntentString("com.oneplus.action.PRODUCT_INFO");
        mOPAboutPhoneSoftWareController5.setRightImage(null);
        mOPAboutPhoneSoftWareController5.setRightTitle(null);
        mOPAboutPhoneSoftWareController5.setRightSummary(null);
        mOPAboutPhoneSoftWareController5.setRightIntentString(null);
        mOPAboutPhoneSoftWareController5.done();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == -1) {
                this.mOPAboutPhoneSoftWareController3.enableDevelopmentSettings();
            }
            this.mOPAboutPhoneSoftWareController3.mProcessingLastDevHit = false;
        }
    }

    private boolean isOlder6tProducts() {
        return Build.MODEL.contains("A30") || Build.MODEL.contains("A50") || Build.MODEL.contains("A600");
    }

    public void onResume() {
        super.onResume();
        displayHardWarePreference();
        displaySoftWare1Preference();
        displaySoftWare2Preference();
        displaySoftWare3Preference();
        displaySoftWare4Preference();
        if (isOlder6tProducts()) {
            displaynoContributors();
        } else {
            displaySoftWare5Preference();
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
