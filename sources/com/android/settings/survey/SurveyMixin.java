package com.android.settings.survey;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.overlay.SurveyFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class SurveyMixin implements LifecycleObserver, OnResume, OnPause {
    private Fragment mFragment;
    private String mName;
    private BroadcastReceiver mReceiver;

    public SurveyMixin(Fragment fragment, String fragmentName) {
        this.mName = fragmentName;
        this.mFragment = fragment;
    }

    public void onResume() {
        Activity activity = this.mFragment.getActivity();
        if (activity != null) {
            SurveyFeatureProvider provider = FeatureFactory.getFactory(activity).getSurveyFeatureProvider(activity);
            if (provider != null) {
                String id = provider.getSurveyId(activity, this.mName);
                if (provider.getSurveyExpirationDate(activity, id) <= -1) {
                    this.mReceiver = provider.createAndRegisterReceiver(activity);
                    provider.downloadSurvey(activity, id, null);
                    return;
                }
                provider.showSurveyIfAvailable(activity, id);
            }
        }
    }

    public void onPause() {
        Activity activity = this.mFragment.getActivity();
        if (this.mReceiver != null && activity != null) {
            SurveyFeatureProvider.unregisterReceiver(activity, this.mReceiver);
            this.mReceiver = null;
        }
    }
}
