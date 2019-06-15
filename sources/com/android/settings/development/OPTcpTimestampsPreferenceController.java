package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class OPTcpTimestampsPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener {
    private static final String OP_TCP_TIMESTAMPS_KEY = "op_tcp_timestamps_randomization";
    private static final String OP_TCP_TIMESTAMPS_VALUE = "op_tcp_timestamps_value";
    private static final boolean mIsH2Version = OpFeatures.isSupport(new int[]{0});
    private final String[] mListSummaries = this.mContext.getResources().getStringArray(R.array.op_tcp_timestamps_summaries);
    private final String[] mListValues = this.mContext.getResources().getStringArray(R.array.op_tcp_timestamps_randomization_values);
    private ListPreference mTcpTimestampsFlag;

    public OPTcpTimestampsPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return OP_TCP_TIMESTAMPS_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mTcpTimestampsFlag = (ListPreference) screen.findPreference(OP_TCP_TIMESTAMPS_KEY);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mTcpTimestampsFlag) {
            return false;
        }
        writeTcpTimestampsFlagOption(newValue);
        return true;
    }

    public void updateState(Preference preference) {
        if (preference == this.mTcpTimestampsFlag) {
            updateSummary(preference);
        }
    }

    private void updateSummary(Preference preference) {
        int tcpFlag = Global.getInt(this.mContext.getContentResolver(), OP_TCP_TIMESTAMPS_VALUE, mIsH2Version ? 2 : 1);
        if (this.mTcpTimestampsFlag != null) {
            this.mTcpTimestampsFlag.setValue(this.mListValues[tcpFlag]);
            this.mTcpTimestampsFlag.setSummary(this.mListSummaries[tcpFlag]);
        }
    }

    public void enablePreference(boolean enabled) {
        if (isAvailable()) {
            this.mTcpTimestampsFlag.setEnabled(enabled);
        }
    }

    public void writeTcpTimestampsFlagOption(Object newValue) {
        if (newValue != null) {
            int tcpFlag = Integer.parseInt(newValue.toString());
            Global.putInt(this.mContext.getContentResolver(), OP_TCP_TIMESTAMPS_VALUE, tcpFlag);
            if (this.mTcpTimestampsFlag != null) {
                this.mTcpTimestampsFlag.setValue(this.mListValues[tcpFlag]);
                this.mTcpTimestampsFlag.setSummary(this.mListSummaries[tcpFlag]);
            }
        }
    }
}
