package com.android.settings.development;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;

public class HdcpCheckingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String HDCP_CHECKING_KEY = "hdcp_checking";
    @VisibleForTesting
    static final String HDCP_CHECKING_PROPERTY = "persist.sys.hdcp_checking";
    @VisibleForTesting
    static final String USER_BUILD_TYPE = "user";
    private final String[] mListSummaries = this.mContext.getResources().getStringArray(R.array.hdcp_checking_summaries);
    private final String[] mListValues = this.mContext.getResources().getStringArray(R.array.hdcp_checking_values);

    public HdcpCheckingPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return TextUtils.equals(USER_BUILD_TYPE, getBuildType()) ^ 1;
    }

    public String getPreferenceKey() {
        return HDCP_CHECKING_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(HDCP_CHECKING_PROPERTY, newValue.toString());
        updateHdcpValues((ListPreference) this.mPreference);
        SystemPropPoker.getInstance().poke();
        return true;
    }

    public void updateState(Preference preference) {
        updateHdcpValues((ListPreference) this.mPreference);
    }

    private void updateHdcpValues(ListPreference preference) {
        String currentValue = SystemProperties.get(HDCP_CHECKING_PROPERTY);
        int index = 1;
        for (int i = 0; i < this.mListValues.length; i++) {
            if (TextUtils.equals(currentValue, this.mListValues[i])) {
                index = i;
                break;
            }
        }
        preference.setValue(this.mListValues[index]);
        preference.setSummary(this.mListSummaries[index]);
    }

    @VisibleForTesting
    public String getBuildType() {
        return Build.TYPE;
    }
}
