package com.oneplus.settings.aboutphone;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.BidiFormatter;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.HardwareInfoDialogFragment;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.utils.FileSizeFormatter;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AboutPhonePresenter implements Presenter {
    private static final String ONEPLUS_A3000 = "ONEPLUS A3000";
    private static final String ONEPLUS_A3003 = "ONEPLUS A3003";
    private static final String ONEPLUS_A3010 = "ONEPLUS A3010";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    private static final String ONEPLUS_A6010 = "ONEPLUS A6010";
    private static final String ONEPLUS_A6013 = "ONEPLUS A6013";
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    static final int TAPS_TO_BE_A_DEVELOPER = 7;
    private Activity mActivity;
    private EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;
    private int mDevHitCountdown;
    private Fragment mFragment;
    private List<SoftwareInfoEntity> mList = new ArrayList();
    public boolean mProcessingLastDevHit;
    private final UserManager mUm;
    private View mView;

    public AboutPhonePresenter(Activity context, Fragment fragment, View view) {
        this.mActivity = context;
        this.mView = view;
        this.mFragment = fragment;
        this.mUm = (UserManager) this.mActivity.getSystemService("user");
        this.mDebuggingFeaturesDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mActivity, "no_debugging_features", UserHandle.myUserId());
        this.mDevHitCountdown = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mActivity) ? -1 : 7;
        this.mDebuggingFeaturesDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mActivity, "no_debugging_features", UserHandle.myUserId());
    }

    public void onResume() {
        showHardwareInfo();
        showSoftwareInfo();
    }

    private static boolean isGuaLiftCameraProject() {
        String[] gualiftcameraproject = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_guacamole_lift_camera_project);
        int i = 0;
        while (i < gualiftcameraproject.length) {
            if (gualiftcameraproject[i] != null && gualiftcameraproject[i].equalsIgnoreCase(Build.MODEL)) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void showHardwareInfo() {
        int i;
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A6003)) {
            i = R.drawable.oneplus_6;
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000")) {
            i = R.drawable.oneplus_5;
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            i = R.drawable.oneplus_5t;
        } else if (OPUtils.isOP3()) {
            i = R.drawable.oneplus_3;
        } else if (OPUtils.isOP3T()) {
            i = R.drawable.oneplus_3t;
        } else if (isGuaLiftCameraProject()) {
            i = R.drawable.oneplus_gua_lift_camera;
        } else if (OPUtils.is18857Project()) {
            i = R.drawable.oneplus_18857;
        } else if (isOlder6tProducts()) {
            i = R.drawable.oneplus_6;
        } else {
            i = R.drawable.oneplus_other;
        }
        this.mView.displayHardWarePreference(i, getCameraInfo(), getCpuName(), getScreenInfo(), getTotalMemory());
    }

    private void showSoftwareInfo() {
        this.mList.clear();
        addDeviceName();
        if (isNeedAddAuthenticationInfo()) {
            addAuthenticationInfo();
        }
        addAndroidVersion();
        if (!OPUtils.isSM8150Products()) {
            addOneplusSystemVersion();
        }
        if (!OPUtils.isSupportUss()) {
            addVersionNumber();
        }
        addDeviceModel();
        addLegalInfo();
        addStatusInfo();
        if (isOlder6tProducts()) {
            addProductIntroduce();
        } else if (OPUtils.isO2() || !OPUtils.isSurportProductInfo(this.mActivity)) {
            addAwardInfo();
        } else {
            addProductIntroduce();
            addAwardInfo();
        }
        if (OPUtils.isSupportUss()) {
            addSoftwareVersion();
            addHardwareVersion();
        }
        this.mView.displaySoftWarePreference(this.mList);
    }

    private void addDeviceName() {
        SoftwareInfoEntity deviceName = new SoftwareInfoEntity();
        deviceName.setTitle(this.mActivity.getString(R.string.my_device_info_device_name_preference_title));
        deviceName.setSummary(System.getString(this.mActivity.getContentResolver(), "oem_oneplus_devicename"));
        deviceName.setResIcon(R.drawable.op_device_name);
        deviceName.setIntent("com.oneplus.intent.OPDeviceNameActivity");
        this.mList.add(deviceName);
    }

    private void addAuthenticationInfo() {
        SoftwareInfoEntity authentication = new SoftwareInfoEntity();
        authentication.setSummary(this.mActivity.getString(R.string.oneplus_regulatory_information));
        authentication.setResIcon(R.drawable.op_authentication_information);
        String title = "";
        String intentString = "";
        if (Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equals(ONEPLUS_A6000) || Build.MODEL.equals("ONEPLUS A5010") || Build.MODEL.equals("ONEPLUS A5000")) {
            if (OPUtils.isO2()) {
                intentString = "android.settings.SHOW_REGULATORY_INFO";
                title = this.mActivity.getString(R.string.regulatory_labels);
            } else {
                intentString = "com.oneplus.intent.OPAuthenticationInformationSettings";
                title = this.mActivity.getString(R.string.oneplus_authentication_information);
            }
        } else if (Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_model_for_europe_and_america)) || Build.MODEL.equals(ONEPLUS_A6003)) {
            intentString = "android.settings.SHOW_REGULATORY_INFO";
            title = this.mActivity.getString(R.string.regulatory_labels);
        } else if (OPUtils.isOP3() || OPUtils.isOP3T()) {
            if (SystemProperties.get("ro.rf_version").contains("Am")) {
                intentString = "android.settings.SHOW_REGULATORY_INFO";
                title = this.mActivity.getString(R.string.regulatory_labels);
            } else {
                intentString = "com.oneplus.intent.OPAuthenticationInformationSettings";
                title = this.mActivity.getString(R.string.oneplus_authentication_information);
            }
        } else if (Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18821_for_eu)) || Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18821_for_us)) || Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18831_for_us)) || Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18857_for_us)) || Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18825_for_us))) {
            intentString = "android.settings.SHOW_REGULATORY_INFO";
            title = this.mActivity.getString(R.string.regulatory_labels);
        } else if (Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18821_for_cn)) || Build.MODEL.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18857_for_cn))) {
            intentString = "com.oneplus.intent.OPAuthenticationInformationSettings";
            title = this.mActivity.getString(R.string.oneplus_authentication_information);
        }
        authentication.setTitle(title);
        authentication.setIntent(intentString);
        this.mList.add(authentication);
    }

    private void addAndroidVersion() {
        SoftwareInfoEntity android = new SoftwareInfoEntity();
        android.setTitle(this.mActivity.getString(R.string.firmware_version));
        android.setSummary(VERSION.RELEASE);
        android.setResIcon(R.drawable.op_android_version);
        android.setIntent("com.android.FirmwareVersionDialogFragment");
        this.mList.add(android);
    }

    private void addOneplusSystemVersion() {
        int resId;
        String title;
        String summary;
        SoftwareInfoEntity system = new SoftwareInfoEntity();
        if (OpFeatures.isSupport(new int[]{1})) {
            resId = R.drawable.op_o2_version;
            title = this.mActivity.getResources().getString(R.string.oneplus_oxygen_version);
            summary = SystemProperties.get("ro.oxygen.version", this.mActivity.getResources().getString(R.string.device_info_default)).replace("O2", "O₂");
        } else {
            resId = R.drawable.op_h2_version;
            title = this.mActivity.getResources().getString(R.string.oneplus_hydrogen_version).replace("H2", "H₂");
            summary = SystemProperties.get("ro.rom.version", this.mActivity.getResources().getString(R.string.device_info_default)).replace("H2", "H₂");
        }
        system.setTitle(title);
        system.setSummary(summary);
        system.setResIcon(resId);
        system.setIntent(null);
        this.mList.add(system);
    }

    private void addVersionNumber() {
        SoftwareInfoEntity version = new SoftwareInfoEntity();
        version.setTitle(this.mActivity.getString(R.string.build_number));
        String buildNumber = BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY);
        if (OPUtils.isSM8150Products()) {
            buildNumber = SystemProperties.get("ro.rom.version", this.mActivity.getResources().getString(R.string.device_info_default));
        }
        version.setSummary(buildNumber);
        version.setResIcon(R.drawable.op_soft_version);
        version.setIntent("build.number");
        this.mList.add(version);
    }

    private void addDeviceModel() {
        SoftwareInfoEntity model = new SoftwareInfoEntity();
        model.setTitle(this.mActivity.getString(R.string.model_info));
        model.setResIcon(R.drawable.op_model);
        model.setIntent(null);
        if (Build.MODEL.contains("A30") || Build.MODEL.contains("A50") || Build.MODEL.contains("A60")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ONEPLUS\n");
            stringBuilder.append(Build.MODEL.replaceAll("ONEPLUS ", ""));
            model.setSummary(stringBuilder.toString());
        } else {
            model.setSummary(Build.MODEL);
        }
        this.mList.add(model);
    }

    private void addLegalInfo() {
        SoftwareInfoEntity legal = new SoftwareInfoEntity();
        legal.setTitle(this.mActivity.getString(R.string.legal_information));
        legal.setSummary(this.mActivity.getString(R.string.oneplus_legal_summary));
        legal.setResIcon(R.drawable.op_legal_settings);
        legal.setIntent("com.oneplus.intent.LegalSettingsActivity");
        this.mList.add(legal);
    }

    private void addStatusInfo() {
        SoftwareInfoEntity status = new SoftwareInfoEntity();
        status.setTitle(this.mActivity.getString(R.string.device_status));
        status.setSummary(this.mActivity.getString(R.string.oneplus_status_summary));
        status.setResIcon(R.drawable.op_status_settings);
        status.setIntent("com.oneplus.intent.MyDeviceInfoFragmentActivity");
        this.mList.add(status);
    }

    private void addAwardInfo() {
        SoftwareInfoEntity award = new SoftwareInfoEntity();
        award.setTitle(this.mActivity.getString(R.string.oneplus_forum_award_title));
        if (OPUtils.isO2()) {
            award.setSummary(this.mActivity.getString(R.string.oneplus_o2_contributors));
        } else {
            award.setSummary(this.mActivity.getString(R.string.oneplus_h2_contributors));
        }
        award.setResIcon(R.drawable.op_award_icon);
        award.setIntent("com.oneplus.intent.OPForumContributorsActivity");
        this.mList.add(award);
    }

    private void addProductIntroduce() {
        SoftwareInfoEntity introduce = new SoftwareInfoEntity();
        introduce.setTitle(this.mActivity.getString(R.string.oneplus_product_info));
        introduce.setSummary(this.mActivity.getString(R.string.oneplus_product_info_summary));
        introduce.setResIcon(R.drawable.op_product_info);
        introduce.setIntent("com.oneplus.action.PRODUCT_INFO");
        this.mList.add(introduce);
    }

    private void addSoftwareVersion() {
        SoftwareInfoEntity sw = new SoftwareInfoEntity();
        sw.setTitle(this.mActivity.getString(R.string.onplus_software_version_info));
        sw.setResIcon(R.drawable.op_software_icon);
        String buildNumber = BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY);
        if (OPUtils.isSM8150Products()) {
            buildNumber = SystemProperties.get("ro.rom.version", this.mActivity.getResources().getString(R.string.device_info_default));
        }
        sw.setSummary(buildNumber);
        sw.setIntent("build.number");
        this.mList.add(sw);
    }

    private void addHardwareVersion() {
        SoftwareInfoEntity hw = new SoftwareInfoEntity();
        hw.setTitle(this.mActivity.getString(R.string.onplus_hardware_version_info));
        hw.setResIcon(R.drawable.op_hardware_icon);
        String defaultVersion = this.mActivity.getResources().getString(R.string.device_info_default);
        hw.setSummary("31");
        hw.setIntent(null);
        this.mList.add(hw);
    }

    private String getCpuName() {
        String CPUinfo = "none";
        if (Build.MODEL.startsWith("ONEPLUS A60")) {
            CPUinfo = "Snapdragon™ 845";
        } else if (Build.MODEL.startsWith("ONEPLUS A50")) {
            CPUinfo = "Snapdragon™ 835";
        } else if (OPUtils.isOP3T()) {
            CPUinfo = "Snapdragon™ 821";
        } else if (OPUtils.isOP3()) {
            CPUinfo = "Snapdragon™ 820";
        }
        if (OPUtils.isGuaProject()) {
            return "Snapdragon™ 855";
        }
        return CPUinfo;
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
        if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6010) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A6013) || OPUtils.is18857Project()) {
            return "Optic AMOLED 6.41” Display";
        }
        if (OPUtils.isGuaProject()) {
            return this.mActivity.getString(R.string.oneplus_7_screen_info);
        }
        return screeninfo;
    }

    private String getCameraInfo() {
        String camerainfo = "none";
        if (Build.MODEL.contains("A60") || Build.MODEL.contains("A50")) {
            return "16 + 20 MP Dual Camera";
        }
        if (OPUtils.isOP3T()) {
            return this.mActivity.getString(R.string.oneplus_3t_camera_info);
        }
        if (OPUtils.isOP3()) {
            return this.mActivity.getString(R.string.oneplus_3_camera_info);
        }
        if (OPUtils.is18857Project()) {
            return this.mActivity.getString(R.string.oneplus_18857_camera_info);
        }
        if (OPUtils.isGuaProject()) {
            return this.mActivity.getString(R.string.oneplus_7_camera_info);
        }
        return camerainfo;
    }

    private boolean isOlder6tProducts() {
        return Build.MODEL.contains("A30") || Build.MODEL.contains("A50") || Build.MODEL.contains("A600");
    }

    private boolean isNeedAddAuthenticationInfo() {
        String model = Build.MODEL;
        if ((model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18821_for_eu)) && OPUtils.isEUVersion()) || model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18821_for_in)) || model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18857_for_in)) || model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18857_for_eu)) || model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18827_for_eu)) || model.equals(this.mActivity.getString(R.string.oneplus_oneplus_model_18857_for_us)) || model.equals(ONEPLUS_A3003)) {
            return false;
        }
        if (model.equals(ONEPLUS_A3000) || model.equals(ONEPLUS_A3010)) {
            String contry = SystemProperties.get("ro.rf_version");
            if (contry.contains("Eu") || contry.contains("In")) {
                return false;
            }
        }
        return true;
    }

    public void enableDevelopmentSettings() {
        this.mDevHitCountdown = 0;
        this.mProcessingLastDevHit = false;
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(this.mActivity, true);
        this.mView.cancelToast();
        if (OPUtils.isSupportXVibrate()) {
            this.mView.performHapticFeedback();
        }
        this.mView.showLongToast((int) R.string.show_dev_on);
    }

    public void onItemClick(int position) {
        String intent = ((SoftwareInfoEntity) this.mList.get(position)).getIntent();
        if (intent != null && !"".equals(intent)) {
            if ("com.android.FirmwareVersionDialogFragment".equals(intent)) {
                FirmwareVersionDialogFragment.show(this.mFragment);
            } else if ("build.number".equals(intent)) {
                if (!Utils.isMonkeyRunning()) {
                    if ((this.mUm.isAdminUser() || this.mUm.isDemoUser()) && Utils.isDeviceProvisioned(this.mActivity)) {
                        if (this.mUm.hasUserRestriction("no_debugging_features")) {
                            if (this.mUm.isDemoUser()) {
                                ComponentName componentName = Utils.getDeviceOwnerComponent(this.mActivity);
                                if (componentName != null) {
                                    Intent requestDebugFeatures = new Intent().setPackage(componentName.getPackageName()).setAction("com.android.settings.action.REQUEST_DEBUG_FEATURES");
                                    if (this.mActivity.getPackageManager().resolveActivity(requestDebugFeatures, 0) != null) {
                                        this.mActivity.startActivity(requestDebugFeatures);
                                        return;
                                    }
                                }
                            }
                            if (!(this.mDebuggingFeaturesDisallowedAdmin == null || this.mDebuggingFeaturesDisallowedBySystem)) {
                                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mActivity, this.mDebuggingFeaturesDisallowedAdmin);
                            }
                        }
                        if (this.mDevHitCountdown > 0) {
                            this.mDevHitCountdown--;
                            if (this.mDevHitCountdown == 0 && !this.mProcessingLastDevHit) {
                                this.mDevHitCountdown++;
                                this.mProcessingLastDevHit = new ChooseLockSettingsHelper(this.mActivity, this.mFragment).launchConfirmationActivity(100, this.mActivity.getString(R.string.unlock_set_unlock_launch_picker_title));
                                if (!this.mProcessingLastDevHit) {
                                    enableDevelopmentSettings();
                                }
                            } else if (this.mDevHitCountdown > 0 && this.mDevHitCountdown < 5) {
                                this.mView.cancelToast();
                                this.mView.showLongToast(this.mActivity.getResources().getQuantityString(R.plurals.show_dev_countdown, this.mDevHitCountdown, new Object[]{Integer.valueOf(this.mDevHitCountdown)}));
                            }
                        } else if (this.mDevHitCountdown < 0) {
                            this.mView.cancelToast();
                            this.mView.showLongToast((int) R.string.show_dev_already);
                        }
                    }
                }
            } else if ("build.model".equals(intent)) {
                HardwareInfoDialogFragment.newInstance().show(this.mFragment.getFragmentManager(), HardwareInfoDialogFragment.TAG);
            } else {
                this.mFragment.startActivity(new Intent(intent));
            }
        }
    }
}
