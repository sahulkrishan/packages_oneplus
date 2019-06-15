package com.android.settings.network;

import android.content.Context;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settings.widget.RadioButtonPreference.OnClickListener;
import java.util.List;

public class NetworkScorerPicker extends InstrumentedPreferenceFragment implements OnClickListener {
    private NetworkScoreManager mNetworkScoreManager;

    public int getMetricsCategory() {
        return 861;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        updateCandidates();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mNetworkScoreManager = createNetworkScorerManager(context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return view;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.network_scorer_picker_prefs;
    }

    @VisibleForTesting
    public void updateCandidates() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        List<NetworkScorerAppData> scorers = this.mNetworkScoreManager.getAllValidScorers();
        String defaultAppKey = getActiveScorerPackage();
        RadioButtonPreference nonePref = new RadioButtonPreference(getPrefContext());
        nonePref.setTitle((int) R.string.network_scorer_picker_none_preference);
        if (scorers.isEmpty()) {
            nonePref.setChecked(true);
        } else {
            nonePref.setKey(null);
            nonePref.setChecked(TextUtils.isEmpty(defaultAppKey));
            nonePref.setOnClickListener(this);
        }
        screen.addPreference(nonePref);
        int numScorers = scorers.size();
        for (int i = 0; i < numScorers; i++) {
            RadioButtonPreference pref = new RadioButtonPreference(getPrefContext());
            NetworkScorerAppData appData = (NetworkScorerAppData) scorers.get(i);
            String appKey = appData.getRecommendationServicePackageName();
            pref.setTitle((CharSequence) appData.getRecommendationServiceLabel());
            pref.setKey(appKey);
            pref.setChecked(TextUtils.equals(defaultAppKey, appKey));
            pref.setOnClickListener(this);
            screen.addPreference(pref);
        }
    }

    private String getActiveScorerPackage() {
        return this.mNetworkScoreManager.getActiveScorerPackage();
    }

    private boolean setActiveScorer(String key) {
        if (TextUtils.equals(key, getActiveScorerPackage())) {
            return false;
        }
        return this.mNetworkScoreManager.setActiveScorer(key);
    }

    public void onRadioButtonClicked(RadioButtonPreference selected) {
        String selectedKey = selected.getKey();
        if (setActiveScorer(selectedKey)) {
            updateCheckedState(selectedKey);
        }
    }

    private void updateCheckedState(String selectedKey) {
        PreferenceScreen screen = getPreferenceScreen();
        int count = screen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = screen.getPreference(i);
            if (pref instanceof RadioButtonPreference) {
                ((RadioButtonPreference) pref).setChecked(TextUtils.equals(pref.getKey(), selectedKey));
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public NetworkScoreManager createNetworkScorerManager(Context context) {
        return (NetworkScoreManager) context.getSystemService("network_score");
    }
}
