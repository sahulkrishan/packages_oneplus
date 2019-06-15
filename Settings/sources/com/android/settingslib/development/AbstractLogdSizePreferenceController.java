package com.android.settingslib.development;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.R;

public abstract class AbstractLogdSizePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener {
    public static final String ACTION_LOGD_SIZE_UPDATED = "com.android.settingslib.development.AbstractLogdSizePreferenceController.LOGD_SIZE_UPDATED";
    @VisibleForTesting
    static final String DEFAULT_SNET_TAG = "I";
    public static final String EXTRA_CURRENT_LOGD_VALUE = "CURRENT_LOGD_VALUE";
    @VisibleForTesting
    static final String LOW_RAM_CONFIG_PROPERTY_KEY = "ro.config.low_ram";
    private static final String SELECT_LOGD_DEFAULT_SIZE_PROPERTY = "ro.logd.size";
    @VisibleForTesting
    static final String SELECT_LOGD_DEFAULT_SIZE_VALUE = "262144";
    @VisibleForTesting
    static final String SELECT_LOGD_MINIMUM_SIZE_VALUE = "65536";
    static final String SELECT_LOGD_OFF_SIZE_MARKER_VALUE = "32768";
    private static final String SELECT_LOGD_RUNTIME_SNET_TAG_PROPERTY = "log.tag.snet_event_log";
    private static final String SELECT_LOGD_SIZE_KEY = "select_logd_size";
    @VisibleForTesting
    static final String SELECT_LOGD_SIZE_PROPERTY = "persist.logd.size";
    @VisibleForTesting
    static final String SELECT_LOGD_SNET_TAG_PROPERTY = "persist.log.tag.snet_event_log";
    private static final String SELECT_LOGD_SVELTE_DEFAULT_SIZE_VALUE = "65536";
    static final String SELECT_LOGD_TAG_PROPERTY = "persist.log.tag";
    static final String SELECT_LOGD_TAG_SILENCE = "Settings";
    private ListPreference mLogdSize;

    public AbstractLogdSizePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SELECT_LOGD_SIZE_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mLogdSize = (ListPreference) screen.findPreference(SELECT_LOGD_SIZE_KEY);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mLogdSize) {
            return false;
        }
        writeLogdSizeOption(newValue);
        return true;
    }

    public void enablePreference(boolean enabled) {
        if (isAvailable()) {
            this.mLogdSize.setEnabled(enabled);
        }
    }

    private String defaultLogdSizeValue() {
        String defaultValue = SystemProperties.get(SELECT_LOGD_DEFAULT_SIZE_PROPERTY);
        if (defaultValue != null && defaultValue.length() != 0) {
            return defaultValue;
        }
        if (SystemProperties.get(LOW_RAM_CONFIG_PROPERTY_KEY).equals("true")) {
            return "65536";
        }
        return SELECT_LOGD_DEFAULT_SIZE_VALUE;
    }

    public void updateLogdSizeValues() {
        if (this.mLogdSize != null) {
            String currentTag = SystemProperties.get(SELECT_LOGD_TAG_PROPERTY);
            String currentValue = SystemProperties.get(SELECT_LOGD_SIZE_PROPERTY);
            if (currentTag != null && currentTag.startsWith(SELECT_LOGD_TAG_SILENCE)) {
                currentValue = SELECT_LOGD_OFF_SIZE_MARKER_VALUE;
            }
            LocalBroadcastManager.getInstance(this.mContext).sendBroadcastSync(new Intent(ACTION_LOGD_SIZE_UPDATED).putExtra(EXTRA_CURRENT_LOGD_VALUE, currentValue));
            if (currentValue == null || currentValue.length() == 0) {
                currentValue = defaultLogdSizeValue();
            }
            String[] values = this.mContext.getResources().getStringArray(R.array.select_logd_size_values);
            String[] titles = this.mContext.getResources().getStringArray(R.array.select_logd_size_titles);
            int index = 2;
            if (SystemProperties.get(LOW_RAM_CONFIG_PROPERTY_KEY).equals("true")) {
                this.mLogdSize.setEntries(R.array.select_logd_size_lowram_titles);
                titles = this.mContext.getResources().getStringArray(R.array.select_logd_size_lowram_titles);
                index = 1;
            }
            String[] summaries = this.mContext.getResources().getStringArray(R.array.select_logd_size_summaries);
            int i = 0;
            while (i < titles.length) {
                if (currentValue.equals(values[i]) || currentValue.equals(titles[i])) {
                    index = i;
                    break;
                }
                i++;
            }
            this.mLogdSize.setValue(values[index]);
            this.mLogdSize.setSummary(summaries[index]);
        }
    }

    public void writeLogdSizeOption(Object newValue) {
        String snetValue;
        boolean disable = newValue != null && newValue.toString().equals(SELECT_LOGD_OFF_SIZE_MARKER_VALUE);
        String currentTag = SystemProperties.get(SELECT_LOGD_TAG_PROPERTY);
        if (currentTag == null) {
            currentTag = "";
        }
        String newTag = currentTag.replaceAll(",+Settings", "").replaceFirst("^Settings,*", "").replaceAll(",+", ",").replaceFirst(",+$", "");
        if (disable) {
            StringBuilder stringBuilder;
            newValue = "65536";
            snetValue = SystemProperties.get(SELECT_LOGD_SNET_TAG_PROPERTY);
            if (snetValue == null || snetValue.length() == 0) {
                snetValue = SystemProperties.get(SELECT_LOGD_RUNTIME_SNET_TAG_PROPERTY);
                if (snetValue == null || snetValue.length() == 0) {
                    SystemProperties.set(SELECT_LOGD_SNET_TAG_PROPERTY, DEFAULT_SNET_TAG);
                }
            }
            if (newTag.length() != 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(",");
                stringBuilder.append(newTag);
                newTag = stringBuilder.toString();
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(SELECT_LOGD_TAG_SILENCE);
            stringBuilder.append(newTag);
            newTag = stringBuilder.toString();
        }
        if (!newTag.equals(currentTag)) {
            SystemProperties.set(SELECT_LOGD_TAG_PROPERTY, newTag);
        }
        snetValue = defaultLogdSizeValue();
        String size = (newValue == null || newValue.toString().length() == 0) ? snetValue : newValue.toString();
        SystemProperties.set(SELECT_LOGD_SIZE_PROPERTY, snetValue.equals(size) ? "" : size);
        SystemProperties.set("ctl.start", "logd-reinit");
        SystemPropPoker.getInstance().poke();
        updateLogdSizeValues();
    }
}
