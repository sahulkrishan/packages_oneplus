package com.oneplus.settings.better;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.SeekBar;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.ui.OPSuwPreferenceCategory;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.settings.OneplusColorManager;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPScreenColorModeSummary;
import com.oneplus.settings.ui.OPSuwSeekBarPreference;
import com.oneplus.settings.ui.OPSuwSeekBarPreference.OPColorModeSeekBarChangeListener;
import com.oneplus.settings.utils.OPUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OPSuwScreenColorMode extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener, OPColorModeSeekBarChangeListener, Indexable {
    public static final int DEFAULT_COLOR_PROGRESS = (OPUtils.isSupportReadingModeInterpolater() ? 20 : 43);
    private static final String KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS = "screen_color_mode_adaptive_model_settings";
    private static final String KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS_DIVIDER = "screen_color_mode_adaptive_model_settings_divider";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS = "screen_color_mode_advanced_settings";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_DISPLAY_P3 = "screen_color_mode_advanced_settings_display_p3";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_NTSC = "screen_color_mode_advanced_settings_ntsc";
    private static final String KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS_SRGB = "screen_color_mode_advanced_settings_srgb";
    private static final String KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS = "screen_color_mode_auto_settings";
    private static final String KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS_DIVIDER = "screen_color_mode_auto_settings_divider";
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
            if (!OPSuwScreenColorMode.isSupportDcip3) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            }
            if (!OPSuwScreenColorMode.isSupportAdaptive) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
            }
            if (!OPSuwScreenColorMode.isSupportSoft) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
            }
            if ("1".equals(OPSuwScreenColorMode.isNoSensor)) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS);
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            }
            return result;
        }
    };
    private static final String TAG = "OPSuwScreenColorMode";
    private static String isNoSensor = null;
    private static boolean isSupportAdaptive = false;
    private static boolean isSupportDcip3 = false;
    private static boolean isSupportSoft = false;
    private static final String sDCI_P3Path = "/sys/devices/virtual/graphics/fb0/DCI_P3";
    private static final String sRGBPath = "/sys/devices/virtual/graphics/fb0/SRGB";
    private static final String s_OPEN_VALUE = "mode = 1";
    private boolean isSupportReadingMode;
    private OneplusColorManager mCM;
    private Context mContext;
    private boolean mDeviceProvision = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 3) {
                OPSuwScreenColorMode.this.scrollToPreference(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_SEEKBAR);
            }
        }
    };
    private OPScreenColorModeSummary mOPScreenColorModeSummary;
    private RadioButtonPreference mScreenColorModeAdaptiveModelSettings;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsDisplayP3;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsNTSC;
    private RadioButtonPreference mScreenColorModeAdvancedSettingsSRGB;
    private RadioButtonPreference mScreenColorModeAutoSettings;
    private RadioButtonPreference mScreenColorModeBasicSettings;
    private ContentObserver mScreenColorModeContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            boolean enabled = true;
            boolean nightmodeenabled = Secure.getInt(OPSuwScreenColorMode.this.getContentResolver(), "night_display_activated", 0) != 1;
            boolean readingmodeenabled = System.getInt(OPSuwScreenColorMode.this.getContentResolver(), OPReadingMode.READING_MODE_STATUS_MANUAL, 0) != 1;
            if (!(nightmodeenabled && readingmodeenabled)) {
                enabled = false;
            }
            OPSuwScreenColorMode.this.mScreenColorModeDefaultSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeBasicSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeDefinedSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeDciP3Settings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeAdaptiveModelSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeSoftSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeAutoSettings.setEnabled(enabled);
            OPSuwScreenColorMode.this.mScreenColorModeCustomPreferenceCategory.setEnabled(enabled);
            OPSuwScreenColorMode.this.mSeekBarpreference.setEnabled(enabled);
            if (OPSuwScreenColorMode.this.mOPScreenColorModeSummary != null) {
                if (!nightmodeenabled) {
                    OPSuwScreenColorMode.this.mOPScreenColorModeSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_title_summary));
                }
                if (!readingmodeenabled) {
                    OPSuwScreenColorMode.this.mOPScreenColorModeSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_reading_mode_on_summary));
                }
                if (enabled) {
                    OPSuwScreenColorMode.this.getPreferenceScreen().removePreference(OPSuwScreenColorMode.this.mOPScreenColorModeSummary);
                } else {
                    OPSuwScreenColorMode.this.getPreferenceScreen().addPreference(OPSuwScreenColorMode.this.mOPScreenColorModeSummary);
                }
            }
        }
    };
    private OPSuwPreferenceCategory mScreenColorModeCustomPreferenceCategory;
    private RadioButtonPreference mScreenColorModeDciP3Settings;
    private RadioButtonPreference mScreenColorModeDefaultSettings;
    private RadioButtonPreference mScreenColorModeDefinedSettings;
    private RadioButtonPreference mScreenColorModeSoftSettings;
    private int mScreenColorModeValue;
    private SeekBar mSeekBar;
    private OPSuwSeekBarPreference mSeekBarpreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_suw_screen_color_mode);
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
        this.mScreenColorModeCustomPreferenceCategory = (OPSuwPreferenceCategory) findPreference(KEY_SCREEN_COLOR_MODE_ADVANCED_SETTINGS);
        this.mOPScreenColorModeSummary = (OPScreenColorModeSummary) findPreference(KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY);
        this.mSeekBarpreference = (OPSuwSeekBarPreference) findPreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
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
            removePreference(KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS_DIVIDER);
            removePreference("screen_color_mode_soft_settings_divider");
            removePreference("oneplus_screen_color_mode_basic_divider");
            removePreference("screen_color_mode_advanced_settings_divider");
            removePreference(KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS_DIVIDER);
        } else {
            removePreference(KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS);
            removePreference(KEY_SCREEN_COLOR_MODE_AUTO_SETTINGS_DIVIDER);
            getPreferenceScreen().removePreference(this.mScreenColorModeCustomPreferenceCategory);
        }
        getPreferenceScreen().removePreference(this.mOPScreenColorModeSummary);
        this.mCM = new OneplusColorManager(this.mContext);
        isSupportDcip3 = this.mContext.getPackageManager().hasSystemFeature("oem.dcip3.support");
        if (!isSupportDcip3) {
            removePreference(KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            removePreference("oneplus_screen_color_mode_basic_divider");
            removePreference("screen_color_mode_defined_settings_divider");
        }
        isSupportAdaptive = this.mContext.getPackageManager().hasSystemFeature("oem.display.adaptive.mode.support");
        if (!isSupportAdaptive) {
            removePreference(KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
        }
        isSupportSoft = this.mContext.getPackageManager().hasSystemFeature("oem.display.soft.support");
        if (!isSupportSoft) {
            removePreference(KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
            removePreference("screen_color_mode_soft_settings_divider");
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
    }

    public void onResume() {
        super.onResume();
        updateRadioButtons(getScreenColorModeSettingsValue());
        getContentResolver().registerContentObserver(Secure.getUriFor("night_display_activated"), true, this.mScreenColorModeContentObserver, -1);
        getContentResolver().registerContentObserver(System.getUriFor(OPReadingMode.READING_MODE_STATUS_MANUAL), true, this.mScreenColorModeContentObserver, -1);
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

    public boolean onPreferenceClick(Preference preference) {
        return true;
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
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
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
                this.mHandler.sendEmptyMessageDelayed(3, 50);
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
    }
}