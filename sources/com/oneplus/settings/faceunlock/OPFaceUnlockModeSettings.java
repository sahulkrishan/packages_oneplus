package com.oneplus.settings.faceunlock;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPFaceUnlockModeLottieViewCategory;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPFaceUnlockModeSettings extends SettingsPreferenceFragment implements OnClickListener, Indexable {
    private static final String KEY_FACEUNLOCK_MODE_RETAIN_VIEW = "key_faceunlock_mode_retain_view";
    private static final String KEY_FACEUNLOCK_SWIPE_UP_MODE = "key_faceunlock_swipe_up_mode";
    private static final String KEY_FACEUNLOCK_USE_POWER_BUTTON_MODE = "key_faceunlock_use_power_button_mode";
    public static final String ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE = "oneplus_face_unlock_powerkey_recognize_enable";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_face_unlock_mode_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    public static final int SWIPE_UP_MODE = 0;
    public static final int USE_POWER_BUTTON_MODE = 1;
    private Context mContext;
    private OPFaceUnlockModeLottieViewCategory mRetainModeView;
    private RadioButtonPreference mSwipeUpMode;
    private RadioButtonPreference mUsePowerButton;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_face_unlock_mode_settings);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mSwipeUpMode = (RadioButtonPreference) findPreference(KEY_FACEUNLOCK_SWIPE_UP_MODE);
        this.mUsePowerButton = (RadioButtonPreference) findPreference(KEY_FACEUNLOCK_USE_POWER_BUTTON_MODE);
        this.mSwipeUpMode.setOnClickListener(this);
        this.mUsePowerButton.setOnClickListener(this);
        this.mRetainModeView = (OPFaceUnlockModeLottieViewCategory) findPreference(KEY_FACEUNLOCK_MODE_RETAIN_VIEW);
    }

    public void onResume() {
        if (!(this.mSwipeUpMode == null || this.mUsePowerButton == null)) {
            boolean z = false;
            int value = System.getInt(this.mContext.getContentResolver(), ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 0);
            this.mSwipeUpMode.setChecked(value == 0);
            RadioButtonPreference radioButtonPreference = this.mUsePowerButton;
            if (value == 1) {
                z = true;
            }
            radioButtonPreference.setChecked(z);
        }
        super.onResume();
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mSwipeUpMode) {
            this.mSwipeUpMode.setChecked(true);
            this.mUsePowerButton.setChecked(false);
            System.putInt(this.mContext.getContentResolver(), ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 0);
            OPUtils.sendAppTracker("pop_up_face_unlock", 0);
            setRetainViewMode(0);
        } else if (emiter == this.mUsePowerButton) {
            this.mSwipeUpMode.setChecked(false);
            this.mUsePowerButton.setChecked(true);
            System.putInt(this.mContext.getContentResolver(), ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 1);
            OPUtils.sendAppTracker("pop_up_face_unlock", 1);
            setRetainViewMode(1);
        }
    }

    private void setRetainViewMode(int mode) {
        if (this.mRetainModeView != null) {
            this.mRetainModeView.setViewType(getUnlockMode());
        }
    }

    public int getUnlockMode() {
        return System.getInt(this.mContext.getContentResolver(), ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 0);
    }

    public void onPause() {
        super.onPause();
        if (this.mRetainModeView != null) {
            this.mRetainModeView.stopAnim();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRetainModeView != null) {
            this.mRetainModeView.releaseAnim();
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
