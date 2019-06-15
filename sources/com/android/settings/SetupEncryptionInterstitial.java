package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.android.settings.EncryptionInterstitial.EncryptionInterstitialFragment;
import com.oneplus.settings.utils.OPUtils;

public class SetupEncryptionInterstitial extends EncryptionInterstitial {

    public static class SetupEncryptionInterstitialFragment extends EncryptionInterstitialFragment {
    }

    public static Intent createStartIntent(Context ctx, int quality, boolean requirePasswordDefault, Intent unlockMethodIntent) {
        Intent startIntent = EncryptionInterstitial.createStartIntent(ctx, quality, requirePasswordDefault, unlockMethodIntent);
        startIntent.setClass(ctx, SetupEncryptionInterstitial.class);
        startIntent.putExtra("extra_prefs_show_button_bar", false).putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1);
        return startIntent;
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SetupEncryptionInterstitialFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return SetupEncryptionInterstitialFragment.class.getName().equals(fragmentName);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_parent);
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
            layout.setFitsSystemWindows(false);
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(8192);
        layout.setFitsSystemWindows(true);
    }
}
