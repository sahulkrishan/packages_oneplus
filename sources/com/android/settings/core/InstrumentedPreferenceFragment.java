package com.android.settings.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.XmlRes;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.survey.SurveyMixin;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;

public abstract class InstrumentedPreferenceFragment extends ObservablePreferenceFragment implements Instrumentable {
    private static final String TAG = "InstrumentedPrefFrag";
    protected final int PLACEHOLDER_METRIC = MediaPlayerGlue.FAST_FORWARD_REWIND_STEP;
    protected MetricsFeatureProvider mMetricsFeatureProvider;
    private VisibilityLoggerMixin mVisibilityLoggerMixin;

    public void onAttach(Context context) {
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mVisibilityLoggerMixin = new VisibilityLoggerMixin(getMetricsCategory(), this.mMetricsFeatureProvider);
        getLifecycle().addObserver(this.mVisibilityLoggerMixin);
        getLifecycle().addObserver(new SurveyMixin(this, getClass().getSimpleName()));
        super.onAttach(context);
    }

    public void onResume() {
        this.mVisibilityLoggerMixin.setSourceMetricsCategory(getActivity());
        super.onResume();
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int resId = getPreferenceScreenResId();
        if (resId > 0) {
            addPreferencesFromResource(resId);
        }
    }

    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        updateActivityTitleWithScreenTitle(getPreferenceScreen());
    }

    /* Access modifiers changed, original: protected|final */
    public final Context getPrefContext() {
        return getPreferenceManager().getContext();
    }

    /* Access modifiers changed, original: protected|final */
    public final VisibilityLoggerMixin getVisibilityLogger() {
        return this.mVisibilityLoggerMixin;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return -1;
    }

    private void updateActivityTitleWithScreenTitle(PreferenceScreen screen) {
        if (screen != null) {
            CharSequence title = screen.getTitle();
            if (TextUtils.isEmpty(title)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Screen title missing for fragment ");
                stringBuilder.append(getClass().getName());
                Log.w(str, stringBuilder.toString());
                return;
            }
            getActivity().setTitle(title);
        }
    }
}
