package com.oneplus.settings.better;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
    private static final String KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS = "screen_color_mode_adaptive_model_settings";
    private static final String KEY_SCREEN_COLOR_MODE_BASIC_SETTINGS = "screen_color_mode_basic_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS = "screen_color_mode_dci_p3_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DEFAULT_SETTINGS = "screen_color_mode_default_settings";
    private static final String KEY_SCREEN_COLOR_MODE_DEFINED_SETTINGS = "screen_color_mode_defined_settings";
    private static final String KEY_SCREEN_COLOR_MODE_SEEKBAR = "screen_color_mode_seekbar";
    private static final String KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS = "screen_color_mode_soft_settings";
    private static final String KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY = "oneplus_screen_color_mode_title_summary";
    public static final String NIGHT_MODE_ENABLED = "night_mode_enabled";
    private static final int SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS_VALUE = 5;
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
            return result;
        }
    };
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
                OPSeekBarPreference access$900 = OPScreenColorMode.this.mSeekBarpreference;
                if (!(isDisplayDaltonizeEnabled || isDisplayInversionEnabled)) {
                    z = true;
                }
                access$900.setEnabled(z);
            }
        }
    };
    private OneplusColorManager mCM;
    private Context mContext;
    private boolean mDeviceProvision = true;
    private OPScreenColorModeSummary mOPScreenColorModeSummary;
    private RadioButtonPreference mScreenColorModeAdaptiveModelSettings;
    private RadioButtonPreference mScreenColorModeBasicSettings;
    private ContentObserver mScreenColorModeContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            OPScreenColorMode.this.updatePreferenceStatus();
        }
    };
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
        this.mOPScreenColorModeSummary = (OPScreenColorModeSummary) findPreference(KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY);
        this.mSeekBarpreference = (OPSeekBarPreference) findPreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        this.mSeekBarpreference.setOPColorModeSeekBarChangeListener(this);
        this.mScreenColorModeDefaultSettings.setOnClickListener(this);
        this.mScreenColorModeBasicSettings.setOnClickListener(this);
        this.mScreenColorModeDefinedSettings.setOnClickListener(this);
        this.mScreenColorModeDciP3Settings.setOnClickListener(this);
        this.mScreenColorModeAdaptiveModelSettings.setOnClickListener(this);
        this.mScreenColorModeSoftSettings.setOnClickListener(this);
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
        this.mScreenColorModeValue = System.getInt(getContentResolver(), "oem_screen_better_value", 57);
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

    private void updateRadioButtons(int value) {
        if (1 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(true);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (2 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(true);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (3 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(true);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            getPreferenceScreen().addPreference(this.mSeekBarpreference);
        } else if (4 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(true);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (5 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(true);
            this.mScreenColorModeSoftSettings.setChecked(false);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (6 == value) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(true);
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
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
        } else if (emiter == this.mScreenColorModeDefaultSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(true);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 1) {
                onSaveScreenColorModeSettingsValue(1);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (emiter == this.mScreenColorModeBasicSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(true);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 2) {
                onSaveScreenColorModeSettingsValue(2);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (emiter == this.mScreenColorModeDefinedSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(true);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 3) {
                onSaveScreenColorModeSettingsValue(3);
            }
            getPreferenceScreen().addPreference(this.mSeekBarpreference);
        } else if (emiter == this.mScreenColorModeDciP3Settings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(true);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 4) {
                onSaveScreenColorModeSettingsValue(4);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (emiter == this.mScreenColorModeAdaptiveModelSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(true);
            this.mScreenColorModeSoftSettings.setChecked(false);
            if (getScreenColorModeSettingsValue() != 5) {
                onSaveScreenColorModeSettingsValue(5);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        } else if (emiter == this.mScreenColorModeSoftSettings) {
            this.mScreenColorModeDefaultSettings.setChecked(false);
            this.mScreenColorModeBasicSettings.setChecked(false);
            this.mScreenColorModeDefinedSettings.setChecked(false);
            this.mScreenColorModeDciP3Settings.setChecked(false);
            this.mScreenColorModeAdaptiveModelSettings.setChecked(false);
            this.mScreenColorModeSoftSettings.setChecked(true);
            if (getScreenColorModeSettingsValue() != 6) {
                onSaveScreenColorModeSettingsValue(6);
            }
            removePreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
        }
        if (!this.mDeviceProvision) {
            this.mCM.revertStatus();
        }
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
        return System.getInt(this.mContext.getContentResolver(), "screen_color_mode_settings_value", 1);
    }

    public void onSaveScreenColorModeSettingsValue(int value) {
        System.putInt(getContentResolver(), "screen_color_mode_settings_value", value);
        OPUtils.sendAppTrackerForScreenColorMode();
    }

    private void resetDefinedScreenColorModeValue() {
        int value = System.getInt(this.mContext.getContentResolver(), "oem_screen_better_value", 43);
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
