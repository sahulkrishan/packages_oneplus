package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v4.app.NotificationCompat;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPScreenRefreshRate extends SettingsPreferenceFragment implements OnClickListener, Indexable {
    private static final String KEY_OP_60HZ_MODE = "op_60hz_mode";
    private static final String KEY_OP_90HZ_MODE = "op_90hz_mode";
    private static final String KEY_OP_AUTO_MODE = "op_auto_mode";
    private static final String ONEPLUS_SCREEN_REFRESH_RATE = "oneplus_screen_refresh_rate";
    private static final int OP_60HZ_MODE_VALUE = 1;
    private static final int OP_90HZ_MODE_VALUE = 0;
    private static final int OP_AUTO_MODE_VALUE = 2;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_screen_refresh_rate_select;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    private RadioButtonPreference m60HzMode;
    private RadioButtonPreference mAutoMode;
    private Context mContext;
    private int mEnterValue;
    private Handler mHandler = new Handler();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_screen_refresh_rate_select);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mAutoMode = (RadioButtonPreference) findPreference(KEY_OP_AUTO_MODE);
        this.m60HzMode = (RadioButtonPreference) findPreference(KEY_OP_60HZ_MODE);
        this.mAutoMode.setOnClickListener(this);
        this.m60HzMode.setOnClickListener(this);
        this.mEnterValue = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_REFRESH_RATE, 2);
    }

    public void onResume() {
        super.onResume();
        int value = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_REFRESH_RATE, 2);
        boolean z = false;
        this.mAutoMode.setChecked(value == 2);
        RadioButtonPreference radioButtonPreference = this.m60HzMode;
        if (value == 1) {
            z = true;
        }
        radioButtonPreference.setChecked(z);
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mAutoMode) {
            this.mAutoMode.setChecked(true);
            this.m60HzMode.setChecked(false);
            Global.putInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_REFRESH_RATE, 2);
        } else if (emiter == this.m60HzMode) {
            this.mAutoMode.setChecked(false);
            this.m60HzMode.setChecked(true);
            Global.putInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_REFRESH_RATE, 1);
        }
        delayRefreshUI();
    }

    private void delayRefreshUI() {
        this.mAutoMode.setEnabled(false);
        this.m60HzMode.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                OPScreenRefreshRate.this.mAutoMode.setEnabled(true);
                OPScreenRefreshRate.this.m60HzMode.setEnabled(true);
            }
        }, 1000);
    }

    public void onDestroy() {
        super.onDestroy();
        int mExitValue = Global.getInt(this.mContext.getContentResolver(), ONEPLUS_SCREEN_REFRESH_RATE, 2);
        if (mExitValue == this.mEnterValue) {
            return;
        }
        if (mExitValue == 2) {
            OPUtils.sendAnalytics("refresh rate", NotificationCompat.CATEGORY_STATUS, "0");
        } else if (mExitValue == 1) {
            OPUtils.sendAnalytics("refresh rate", NotificationCompat.CATEGORY_STATUS, "1");
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
