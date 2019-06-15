package com.android.settings.notification;

import android.content.Intent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment;

public class RedactionSettingsStandalone extends SettingsActivity {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, RedactionInterstitialFragment.class.getName()).putExtra("extra_prefs_show_button_bar", true).putExtra("extra_prefs_set_back_text", (String) null).putExtra("extra_prefs_set_next_text", getString(R.string.app_notifications_dialog_done));
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return RedactionInterstitialFragment.class.getName().equals(fragmentName);
    }
}
