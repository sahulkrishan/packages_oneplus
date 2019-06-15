package com.oneplus.settings.better;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.widget.SeekBar;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.settings.OneplusColorManager;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPScreenColorModeSummary;
import com.oneplus.settings.ui.OPSeekBarPreference;
import com.oneplus.settings.ui.OPSeekBarPreference.OPColorModeSeekBarChangeListener;
import com.oneplus.settings.utils.OPUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OPScreenColorMode extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener, OPColorModeSeekBarChangeListener, Indexable {
    public static final int DEFAULT_COLOR_PROGRESS = (OPUtils.isSupportReadingModeInterpolater() ? 20 : 43);
    private static final String KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS = "screen_color_mode_adaptive_model_settings";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS = "screen_color_mode_advanced_settings";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_DISPLAY_P3 = "screen_color_mode_advanced_settings_display_p3";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_NTSC = "screen_color_mode_advanced_settings_ntsc";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_SRGB = "screen_color_mode_advanced_settings_srgb";
    private static final String KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS = "screen_color_mode_auto_settings";
    private static final String KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS = "screen_color_mode_basic_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS = "screen_color_mode_dci_p3_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DEFAULT_SETTINGS = "screen_color_mode_default_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DEFINED_SETTINGS = "screen_color_mode_defined_settings";
    private static final String KEY_SCREEN_COLOR_MODE_SEEKBAR = "screen_color_mode_seekbar";
    private static final String KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS = "screen_color_mode_soft_settings";
    private static final String KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY = "oneplus_screen_color_mode_title_summary";
    public static final String NIGHT_MODE_ENABLED = "night_mode_enabled";
    private static final int SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS_VALUE = 5;
    private static final int SCREEN_COLOR_MODE_AUTO_SETTINGS_VALUE = 10;
    private static final int SCREEN_COLOR_MODE_BASIC_SETTINGS_VALUE = 2;
    private static final int SCREEN_COLOR_MODE_DCI_P3_SETTINGS_VALUE = 4;
    private static final int SCREEN_COLOR_MODE_DEFAULT_SETTINGS_VALUE = 1;
    private static final int SCREEN_COLOR_MODE_DEFINED_SETTINGS_VALUE = 3;
    private static final int SCREEN_COLOR_MODE_SOFT_SETTINGS_VALUE = 6;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.op_screen_color_mode;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (!OPScreenColorMode.isSupportDcip3) {
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            }
            if (!OPScreenColorMode.isSupportAdaptive) {
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
            }
            if (!OPScreenColorMode.isSupportSoft) {
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
            }
            if ("1".equals(OPScreenColorMode.isNoSensor)) {
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS);
                result.add(OPScreenColorMode.KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            }
            return result;
        }
    };
    private static final String TAG = "ScreenColorMode";
    private static String isNoSensor = null;
    private static boolean isSupportAdaptive = false;
    private static boolean isSupportDcip3 = false;
    private static boolean isSupportSoft = false;
    private static final String sDCI_P3Path = "/sys/devices/virtual/graphics/fb0/DCI_P3";
    private static final String sRGBPath = "/sys/devices/virtual/graphics/fb0/SRGB";
    private static final String s_OPEN_VALUE = "mode = 1";
    private boolean isSupportReadingMode;
    private ContentObserver mAccessibilityDisplayDaltonizerAndInversionContentObserver = new ContentObserver(new Handler()) {
        private final Uri ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED_URI = Secure.getUriFor("accessibility_display_daltonizer_enabled");
        private final Uri ACCESSIBILITY_DISPLAY_INVERSION_ENABLED_URI = Secure.getUriFor("accessibility_display_inversion_enabled");

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED_URI.equals(uri) || this.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED_URI.equals(uri)) {
                boolean z = false;
                boolean isDisplayDaltonizeEnabled = Secure.getInt(OPScreenColorMode.this.getContentResolver(), "accessibility_display_daltonizer_enabled", 12) == 1;
                boolean isDisplayInversionEnabled = Secure.getInt(OPScreenColorMode.this.getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1;
                RadioButtonPreference access$300 = OPScreenColorMode.this.mScreenColorModeDefaultSettings;
                boolean z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeBasicSettings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeDefinedSettings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeDciP3Settings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeAdaptiveModelSettings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeSoftSettings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                access$300 = OPScreenColorMode.this.mScreenColorModeAutoSettings;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$300.setEnabled(z2);
                PreferenceCategory access$1000 = OPScreenColorMode.this.mScreenColorModeCustomPreferenceCategory;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled) ? false : true;
                access$1000.setEnabled(z2);
                OPSeekBarPreference access$1100 = OPScreenColorMode.this.mSeekBarpreference;
                if (!(isDisplayDaltonizeEnabled || isDisplayInversionEnabled)) {
                    z = true;
                }
                access$1100.setEnabled(z);
            }
        }
    };
    private OneplusColorManager mCM;
    private Context mContext;
    private boolean mDeviceProvision = true;
    private int mEnterAdvancedValue;
    private int mEnterValue;
    private OPScreenColorModeSummary mOPScreenColorModeSummary;
    private RadioButtonPreference mScreenColorModeAdaptiveModelSettings;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsDisplayP3;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsNTSC;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsSRGB;
    private RadioButtonPreference mScreenColorModeAutoSettings;
    private RadioButtonPreference mScreenColorModeBasicSettings;
    private ContentObserver mScreenColorModeContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            OPScreenColorMode.this.updatePreferenceStatus();
        }
    };
    private PreferenceCategory mScreenColorModeCustomPreferenceCategory;
    private RadioButtonPreference mScreenColorModeDciP3Settings;
    private RadioButtonPreference mScreenColorModeDefaultSettings;
    private RadioButtonPreference mScreenColorModeDefinedSettings;
    private RadioButtonPreference mScreenColorModeSoftSettings;
    private int mScreenColorModeValue;
    private SeekBar mSeekBar;
    private OPSeekBarPreference mSeekBarpreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_screen_color_mode);
        this.mContext = SettingsBaseApplication.mApplication;
        this.isSupportReadingMode = this.mContext.getPackageManager().hasSystemFeature("oem.read_mode.support");
        this.mScreenColorModeDefaultSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_DEFAULT_SETTINGS);
        this.mScreenColorModeBasicSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS);
        this.mScreenColorModeDefinedSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_DEFINED_SETTINGS);
        this.mScreenColorModeDciP3Settings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
        this.mScreenColorModeAdaptiveModelSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
        this.mScreenColorModeSoftSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
        this.mScreenColorModeAutoSettings = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS);
        this.mScreenColorModeAdvancedSettingsNTSC = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_NTSC);
        this.mScreenColorModeAdvancedSettingsSRGB = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_SRGB);
        this.mScreenColorModeAdvancedSettingsDisplayP3 = (RadioButtonPreference) findPreference(KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_DISPLAY_P3);
        this.mScreenColorModeCustomPreferenceCategory = (PreferenceCategory) findPreference(KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS);
        this.mOPScreenColorModeSummary = (OPScreenColorModeSummary) findPreference(KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY);
        this.mSeekBarpreference = (OPSeekBarPreference) findPreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        this.mSeekBarpreference.setOPColorModeSeekBarChangeListener(this);
        this.mScreenColorModeDefaultSettings.setOnClickListener(this);
        this.mScreenColorModeBasicSettings.setOnClickListener(this);
        this.mScreenColorModeDefinedSettings.setOnClickListener(this);
        this.mScreenColorModeDciP3Settings.setOnClickListener(this);
        this.mScreenColorModeAdaptiveModelSettings.setOnClickListener(this);
        this.mScreenColorModeSoftSettings.setOnClickListener(this);
        this.mScreenColorModeAutoSettings.setOnClickListener(this);
        this.mScreenColorModeAdvancedSettingsNTSC.setOnClickListener(this);
        this.mScreenColorModeAdvancedSettingsSRGB.setOnClickListener(this);
        this.mScreenColorModeAdvancedSettingsDisplayP3.setOnClickListener(this);
        isNoSensor = SystemProperties.get("ro.sensor.not_support_rbg", "0");
        if ("1".equals(isNoSensor)) {
            this.mScreenColorModeDefaultSettings.setTitle((int) R.string.screen_color_mode_vivid);
            this.mScreenColorModeDefinedSettings.setTitle((int) R.string.screen_color_mode_advanced);
            removePreference(KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
            removePreference(KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS);
            removePreference(KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
        } else {
            removePreference(KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS);
            getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
        }
        getPreferenceScreen().removePreference(this.mOPScreenColorModeSummary);
        this.mCM = new OneplusColorManager(this.mContext);
        isSupportDcip3 = this.mContext.getPackageManager().hasSystemFeature("oem.dcip3.support");
        if (!isSupportDcip3) {
            removePreference(KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
        }
        isSupportAdaptive = this.mContext.getPackageManager().hasSystemFeature("oem.display.adaptive.mode.support");
        if (!isSupportAdaptive) {
            removePreference(KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
        }
        isSupportSoft = this.mContext.getPackageManager().hasSystemFeature("oem.display.soft.support");
        if (!isSupportSoft) {
            removePreference(KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
        }
        boolean z = true;
        if (Global.getInt(getContentResolver(), WizardManagerHelper.SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 1) {
            z = false;
        }
        this.mDeviceProvision = z;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mDeviceProvision = ");
        stringBuilder.append(this.mDeviceProvision);
        Log.i("OPScreenColorMode", stringBuilder.toString());
        this.mScreenColorModeValue = System.getInt(getContentResolver(), "oem_screen_better_value", DEFAULT_COLOR_PROGRESS);
        this.mEnterValue = getScreenColorModeSettingsValue();
        this.mEnterAdvancedValue = System.getIntForUser(this.mContext.getContentResolver(), "screen_color_mode_advanced_settings_value", 0, -2);
    }

    public void onDestroy() {
        super.onDestroy();
        int mExitValue = getScreenColorModeSettingsValue();
        if (mExitValue != this.mEnterValue) {
            if (mExitValue == 1) {
                OPUtils.sendAnalytics("screen_calibration", NotificationCompat.CATEGORY_STATUS, "1");
            } else if (mExitValue == 10) {
                OPUtils.sendAnalytics("screen_calibration", NotificationCompat.CATEGORY_STATUS, "2");
            } else if (mExitValue == 3) {
                OPUtils.sendAnalytics("screen_calibration", NotificationCompat.CATEGORY_STATUS, "3");
            }
        }
        if (mExitValue == 3) {
            int mExitAdvancedValue = System.getIntForUser(this.mContext.getContentResolver(), "screen_color_mode_advanced_settings_value", 0, -2);
            if (mExitAdvancedValue != this.mEnterAdvancedValue) {
                if (mExitAdvancedValue == 0) {
                    OPUtils.sendAnalytics("screen_calibration", "advanced", "1");
                } else if (mExitAdvancedValue == 1) {
                    OPUtils.sendAnalytics("screen_calibration", "advanced", "2");
                } else if (mExitAdvancedValue == 2) {
                    OPUtils.sendAnalytics("screen_calibration", "advanced", "3");
                }
            }
            int mExitScreenColorModeValue = System.getInt(getContentResolver(), "oem_screen_better_value", DEFAULT_COLOR_PROGRESS);
            if (mExitScreenColorModeValue == this.mScreenColorModeValue) {
                return;
            }
            if (((double) mExitScreenColorModeValue) <= ((double) this.mSeekBarpreference.getSeekBarMax()) * 0.33d) {
                OPUtils.sendAnalytics("screen_calibration", "custom", "1");
            } else if (((double) mExitScreenColorModeValue) <= ((double) this.mSeekBarpreference.getSeekBarMax()) * 0.66d) {
                OPUtils.sendAnalytics("screen_calibration", "custom", "2");
            } else if (mExitScreenColorModeValue <= this.mSeekBarpreference.getSeekBarMax()) {
                OPUtils.sendAnalytics("screen_calibration", "custom", "3");
            }
        }
    }

    private void updatePreferenceStatus() {
        boolean enabled = true;
        boolean nightmodeenabled = Secure.getInt(getContentResolver(), "night_display_activated", 0) != 1;
        boolean readingmodeenabled = System.getInt(getContentResolver(), OPReadingMode.READING_MODE_STATUS_MANUAL, 0) != 1;
        if (!(nightmodeenabled && readingmodeenabled)) {
            enabled = false;
        }
        this.mScreenColorModeDefaultSettings.setEnabled(enabled);
        this.mScreenColorModeBasicSettings.setEnabled(enabled);
        this.mScreenColorModeDefinedSettings.setEnabled(enabled);
        this.mScreenColorModeDciP3Settings.setEnabled(enabled);
        this.mScreenColorModeAdaptiveModelSettings.setEnabled(enabled);
        this.mScreenColorModeSoftSettings.setEnabled(enabled);
        this.mScreenColorModeAutoSettings.setEnabled(enabled);
        this.mScreenColorModeCustomPreferenceCategory.setEnabled(enabled);
        this.mSeekBarpreference.setEnabled(enabled);
        if (this.mOPScreenColorModeSummary != null) {
            if (!nightmodeenabled) {
                this.mOPScreenColorModeSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_title_summary));
            }
            if (!readingmodeenabled) {
                this.mOPScreenColorModeSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_reading_mode_on_summary));
            }
            if (enabled) {
                getPreferenceScreen().removePreference(this.mOPScreenColorModeSummary);
            } else {
                getPreferenceScreen().addPreference(this.mOPScreenColorModeSummary);
            }
        }
    }

    public void onResume() {
        super.onResume();
        updateRadioButtons(getScreenColorModeSettingsValue());
        getContentResolver().registerContentObserver(Secure.getUriFor("night_display_activated"), true, this.mScreenColorModeContentObserver, -1);
        getContentResolver().registerContentObserver(System.getUriFor(OPReadingMode.READING_MODE_STATUS_MANUAL), true, this.mScreenColorModeContentObserver, -1);
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_daltonizer_enabled"), true, this.mAccessibilityDisplayDaltonizerAndInversionContentObserver, -1);
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_inversion_enabled"), true, this.mAccessibilityDisplayDaltonizerAndInversionContentObserver, -1);
        updatePreferenceStatus();
        updateAdvancedSettingsRadioButtons();
    }

    public String readFile(String path) {
        String value = "0";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            value = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
        return value;
    }

    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(this.mScreenColorModeContentObserver);
        getContentResolver().unregisterContentObserver(this.mAccessibilityDisplayDaltonizerAndInversionContentObserver);
    }

    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    private void updateAdvancedSettingsRadioButtons() {
        boolean z = false;
        int value = System.getIntForUser(this.mContext.getContentResolver(), "screen_color_mode_advanced_settings_value", 0, -2);
        this.mScreenColorModeAdvancedSettingsNTSC.setChecked(value == 0);
        this.mScreenColorModeAdvancedSettingsSRGB.setChecked(value == 1);
        RadioButtonPreference radioButtonPreference = this.mScreenColorModeAdvancedSettingsDisplayP3;
        if (value == 2) {
            z = true;
        }
        radioButtonPreference.setChecked(z);
    }

    private void updateRadioButtons(int value) {
        if (1 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(true);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (2 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(true);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (3 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(true);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            getPreferenceScreen().addPreference(this.mSeekBarpreference);
            if (this.mScreenColorModeCustomPreferenceCategory != null && "1".equals(isNoSensor)) {
                getPreferenceScreen().addPreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (4 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(true);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (5 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(true);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (6 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(true);
            this.mScreenColorModeAutoSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (10 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(true);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == null) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
        } else if (emiter == this.mScreenColorModeDefaultSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(true);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 1) {
                onSaveScreenColorModeSettingsValue(1);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeBasicSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(true);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 2) {
                onSaveScreenColorModeSettingsValue(2);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeDefinedSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(true);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 3) {
                onSaveScreenColorModeSettingsValue(3);
            }
            getPreferenceScreen().addPreference(this.mSeekBarpreference);
            if (this.mScreenColorModeCustomPreferenceCategory != null && "1".equals(isNoSensor)) {
                getPreferenceScreen().addPreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeDciP3Settings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(true);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 4) {
                onSaveScreenColorModeSettingsValue(4);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeAdaptiveModelSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(true);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 5) {
                onSaveScreenColorModeSettingsValue(5);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeSoftSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(true);
            this.mScreenColorModeAutoSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 6) {
                onSaveScreenColorModeSettingsValue(6);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeAutoSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            this.mScreenColorModeAutoSettings.setChecked(true);
            if (getScreenColorModeSettingsValue() != 10) {
                onSaveScreenColorModeSettingsValue(10);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
            if (this.mScreenColorModeCustomPreferenceCategory != null) {
                getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
            }
        } else if (emiter == this.mScreenColorModeAdvancedSettingsNTSC) {
            System.putIntForUser(getContentResolver(), "screen_color_mode_advanced_settings_value", 0, -2);
            this.mScreenColorModeAdvancedSettingsNTSC.setChecked(true);
            this.mScreenColorModeAdvancedSettingsSRGB.setChecked(false);
            this.mScreenColorModeAdvancedSettingsDisplayP3.setChecked(false);
        } else if (emiter == this.mScreenColorModeAdvancedSettingsSRGB) {
            System.putIntForUser(getContentResolver(), "screen_color_mode_advanced_settings_value", 1, -2);
            this.mScreenColorModeAdvancedSettingsNTSC.setChecked(false);
            this.mScreenColorModeAdvancedSettingsSRGB.setChecked(true);
            this.mScreenColorModeAdvancedSettingsDisplayP3.setChecked(false);
        } else if (emiter == this.mScreenColorModeAdvancedSettingsDisplayP3) {
            System.putIntForUser(getContentResolver(), "screen_color_mode_advanced_settings_value", 2, -2);
            this.mScreenColorModeAdvancedSettingsNTSC.setChecked(false);
            this.mScreenColorModeAdvancedSettingsSRGB.setChecked(false);
            this.mScreenColorModeAdvancedSettingsDisplayP3.setChecked(true);
        }
        if (!this.mDeviceProvision) {
            this.mCM.revertStatus();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("the screen color mode settings value = ");
        stringBuilder.append(getScreenColorModeSettingsValue());
        Log.d(str, stringBuilder.toString());
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.mScreenColorModeValue = progress;
        if (this.mCM == null) {
            return;
        }
        if (this.isSupportReadingMode) {
            this.mCM.setColorBalance(100 - this.mScreenColorModeValue);
        } else {
            this.mCM.setColorBalance((100 - this.mScreenColorModeValue) + 512);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        onSaveScreenColorModeValue(this.mScreenColorModeValue);
        this.mCM.saveScreenBetter();
    }

    public int getScreenColorModeSettingsValue() {
        return System.getIntForUser(this.mContext.getContentResolver(), "screen_color_mode_settings_value", 1, -2);
    }

    public void onSaveScreenColorModeSettingsValue(int value) {
        System.putIntForUser(getContentResolver(), "screen_color_mode_settings_value", value, -2);
        OPUtils.sendAppTrackerForScreenColorMode();
    }

    private void resetDefinedScreenColorModeValue() {
        int value = System.getInt(this.mContext.getContentResolver(), "oem_screen_better_value", DEFAULT_COLOR_PROGRESS);
        if (this.mCM != null) {
            if (this.isSupportReadingMode) {
                this.mCM.setActiveMode(0);
                this.mCM.setColorBalance(100 - value);
            } else {
                this.mCM.setColorBalance((100 - value) + 512);
            }
            this.mCM.saveScreenBetter();
        }
    }

    public void onSaveScreenColorModeValue(int value) {
        System.putInt(getContentResolver(), "oem_screen_better_value", value);
        OPUtils.sendAppTrackerForScreenCustomColorMode();
    }
}
