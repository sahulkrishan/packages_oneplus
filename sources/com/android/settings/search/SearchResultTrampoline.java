package com.android.settings.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;
import com.android.settings.SubSettings;
import com.android.settings.overlay.FeatureFactory;

public class SearchResultTrampoline extends Activity {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FeatureFactory.getFactory(this).getSearchFeatureProvider().verifyLaunchSearchResultPageCaller(this, getCallingActivity());
            Intent intent = getIntent();
            String settingKey = intent.getStringExtra(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY);
            Bundle args = new Bundle();
            args.putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, settingKey);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
            intent.setClass(this, SubSettings.class).addFlags(33554432);
            startActivity(intent);
            finish();
        } catch (SecurityException e) {
            e.printStackTrace();
            finish();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            finish();
        }
    }
}
