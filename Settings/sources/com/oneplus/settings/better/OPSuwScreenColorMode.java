package com.oneplus.settings.better;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.oneplus.settings.ui.OPSuwSeekBarPreference;
import com.oneplus.settings.ui.OPSuwSeekBarPreference.OPColorModeSeekBarChangeListener;
import com.oneplus.settings.utils.OPUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OPSuwScreenColorMode extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener, OPColorModeSeekBarChangeListener, Indexable {
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
            if (!OPSuwScreenColorMode.isSupportDcip3) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_DCI_P3_SETTINGS);
            }
            if (!OPSuwScreenColorMode.isSupportAdaptive) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS);
            }
            if (!OPSuwScreenColorMode.isSupportSoft) {
                result.add(OPSuwScreenColorMode.KEY_SCREEN_COLOR_MODE_SOFT_SETTINGS);
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
        this.mOPScreenColorModeSummary = (OPScreenColorModeSummary) findPreference(KEY_SCREEN_COLOR_MODE_TITLE_SUMMARY);
        this.mSeekBarpreference = (OPSuwSeekBarPreference) findPreference(KEY_SCREEN_COLOR_MODE_SEEKBAR);
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
                this.mHandler.sendEmptyMessageDelayed(3, 50);
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
    }
}
