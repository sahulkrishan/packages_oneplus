package com.android.settingslib.development;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.R;
import com.android.settingslib.core.ConfirmationDialogController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnDestroy;

public abstract class AbstractLogpersistPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, LifecycleObserver, OnCreate, OnDestroy, ConfirmationDialogController {
    @VisibleForTesting
    static final String ACTUAL_LOGPERSIST_PROPERTY = "logd.logpersistd";
    @VisibleForTesting
    static final String ACTUAL_LOGPERSIST_PROPERTY_BUFFER = "logd.logpersistd.buffer";
    private static final String ACTUAL_LOGPERSIST_PROPERTY_ENABLE = "logd.logpersistd.enable";
    private static final String SELECT_LOGPERSIST_KEY = "select_logpersist";
    private static final String SELECT_LOGPERSIST_PROPERTY = "persist.logd.logpersistd";
    private static final String SELECT_LOGPERSIST_PROPERTY_BUFFER = "persist.logd.logpersistd.buffer";
    private static final String SELECT_LOGPERSIST_PROPERTY_CLEAR = "clear";
    @VisibleForTesting
    static final String SELECT_LOGPERSIST_PROPERTY_SERVICE = "logcatd";
    private static final String SELECT_LOGPERSIST_PROPERTY_STOP = "stop";
    private ListPreference mLogpersist;
    private boolean mLogpersistCleared;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AbstractLogpersistPreferenceController.this.onLogdSizeSettingUpdate(intent.getStringExtra(AbstractLogdSizePreferenceController.EXTRA_CURRENT_LOGD_VALUE));
        }
    };

    public AbstractLogpersistPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (isAvailable() && lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return TextUtils.equals(SystemProperties.get("ro.debuggable", "0"), "1");
    }

    public String getPreferenceKey() {
        return SELECT_LOGPERSIST_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mLogpersist = (ListPreference) screen.findPreference(SELECT_LOGPERSIST_KEY);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mLogpersist) {
            return false;
        }
        writeLogpersistOption(newValue, false);
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mReceiver, new IntentFilter(AbstractLogdSizePreferenceController.ACTION_LOGD_SIZE_UPDATED));
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mReceiver);
    }

    public void enablePreference(boolean enabled) {
        if (isAvailable()) {
            this.mLogpersist.setEnabled(enabled);
        }
    }

    private void onLogdSizeSettingUpdate(String currentValue) {
        if (this.mLogpersist != null) {
            String currentLogpersistEnable = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY_ENABLE);
            if (currentLogpersistEnable == null || !currentLogpersistEnable.equals("true") || currentValue.equals("32768")) {
                writeLogpersistOption(null, true);
                this.mLogpersist.setEnabled(false);
            } else if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mContext)) {
                this.mLogpersist.setEnabled(true);
            }
        }
    }

    public void updateLogpersistValues() {
        if (this.mLogpersist != null) {
            String currentValue = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY);
            if (currentValue == null) {
                currentValue = "";
            }
            String currentBuffers = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY_BUFFER);
            if (currentBuffers == null || currentBuffers.length() == 0) {
                currentBuffers = "all";
            }
            int index = 0;
            if (currentValue.equals(SELECT_LOGPERSIST_PROPERTY_SERVICE)) {
                index = 1;
                if (currentBuffers.equals("kernel")) {
                    index = 3;
                } else if (!currentBuffers.equals("all") && !currentBuffers.contains("radio") && currentBuffers.contains("security") && currentBuffers.contains("kernel")) {
                    index = 2;
                    if (!currentBuffers.contains("default")) {
                        for (String type : new String[]{"main", "events", "system", "crash"}) {
                            if (!currentBuffers.contains(type)) {
                                index = 1;
                                break;
                            }
                        }
                    }
                }
            }
            this.mLogpersist.setValue(this.mContext.getResources().getStringArray(R.array.select_logpersist_values)[index]);
            this.mLogpersist.setSummary(this.mContext.getResources().getStringArray(R.array.select_logpersist_summaries)[index]);
            if (index != 0) {
                this.mLogpersistCleared = false;
            } else if (!this.mLogpersistCleared) {
                SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY, SELECT_LOGPERSIST_PROPERTY_CLEAR);
                SystemPropPoker.getInstance().poke();
                this.mLogpersistCleared = true;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void setLogpersistOff(boolean update) {
        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY_BUFFER, "");
        SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY_BUFFER, "");
        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY, "");
        SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY, update ? "" : SELECT_LOGPERSIST_PROPERTY_STOP);
        SystemPropPoker.getInstance().poke();
        if (update) {
            updateLogpersistValues();
            return;
        }
        int i = 0;
        while (i < 3) {
            String currentValue = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY);
            if (currentValue != null && !currentValue.equals("")) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                i++;
            } else {
                return;
            }
        }
    }

    public void writeLogpersistOption(Object newValue, boolean skipWarning) {
        if (this.mLogpersist != null) {
            String currentTag = SystemProperties.get("persist.log.tag");
            if (currentTag != null && currentTag.startsWith("Settings")) {
                newValue = null;
                skipWarning = true;
            }
            int i = 0;
            if (newValue == null || newValue.toString().equals("")) {
                if (skipWarning) {
                    this.mLogpersistCleared = false;
                } else if (!this.mLogpersistCleared) {
                    String currentValue = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY);
                    if (currentValue != null && currentValue.equals(SELECT_LOGPERSIST_PROPERTY_SERVICE)) {
                        showConfirmationDialog(this.mLogpersist);
                        return;
                    }
                }
                setLogpersistOff(true);
                return;
            }
            String currentBuffer = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY_BUFFER);
            if (!(currentBuffer == null || currentBuffer.equals(newValue.toString()))) {
                setLogpersistOff(false);
            }
            SystemProperties.set(SELECT_LOGPERSIST_PROPERTY_BUFFER, newValue.toString());
            SystemProperties.set(SELECT_LOGPERSIST_PROPERTY, SELECT_LOGPERSIST_PROPERTY_SERVICE);
            SystemPropPoker.getInstance().poke();
            while (i < 3) {
                String currentValue2 = SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY);
                if (currentValue2 != null && currentValue2.equals(SELECT_LOGPERSIST_PROPERTY_SERVICE)) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                i++;
            }
            updateLogpersistValues();
        }
    }
}
