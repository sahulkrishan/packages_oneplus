package com.android.settings.network;

import android.content.Context;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class NetworkScorerPickerPreferenceController extends BasePreferenceController {
    private final NetworkScoreManager mNetworkScoreManager = ((NetworkScoreManager) this.mContext.getSystemService("network_score"));

    public NetworkScorerPickerPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        boolean enabled = this.mNetworkScoreManager.getAllValidScorers().isEmpty() ^ 1;
        preference.setEnabled(enabled);
        if (enabled) {
            NetworkScorerAppData scorer = this.mNetworkScoreManager.getActiveScorer();
            if (scorer == null) {
                preference.setSummary(this.mContext.getString(R.string.network_scorer_picker_none_preference));
            } else {
                preference.setSummary(scorer.getRecommendationServiceLabel());
            }
            return;
        }
        preference.setSummary(null);
    }
}
