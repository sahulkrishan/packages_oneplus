package com.android.settings.accessibility;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.view.Menu;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.oneplus.settings.utils.OPUtils;

public class AccessibilitySettingsForSetupWizardActivity extends SettingsActivity {
    private static final String SAVE_KEY_TITLE = "activity_title";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle savedState) {
        savedState.putCharSequence(SAVE_KEY_TITLE, getTitle());
        super.onSaveInstanceState(savedState);
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        setTitle(savedState.getCharSequence(SAVE_KEY_TITLE));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onNavigateUp() {
        onBackPressed();
        getWindow().getDecorView().sendAccessibilityEvent(32);
        return true;
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        Bundle args = pref.getExtras();
        if (args == null) {
            args = new Bundle();
        }
        int i = 0;
        args.putInt(HelpResourceProvider.HELP_URI_RESOURCE_KEY, 0);
        args.putBoolean(SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        SubSettingLauncher arguments = new SubSettingLauncher(this).setDestination(pref.getFragment()).setArguments(args);
        if (caller instanceof Instrumentable) {
            i = ((Instrumentable) caller).getMetricsCategory();
        }
        arguments.setSourceMetricsCategory(i).launch();
        return true;
    }
}
