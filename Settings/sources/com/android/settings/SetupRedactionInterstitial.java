package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.UserManager;
import android.widget.LinearLayout;
import com.android.settings.notification.RedactionInterstitial;
import com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment;
import com.oneplus.settings.utils.OPUtils;

public class SetupRedactionInterstitial extends RedactionInterstitial {

    public static class SetupRedactionInterstitialFragment extends RedactionInterstitialFragment {
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, SetupRedactionInterstitial.class), enabled ? 1 : 2, 1);
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

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    public static Intent createStartIntent(Context ctx, int userId) {
        int i;
        Intent intent = new Intent(ctx, SetupRedactionInterstitial.class);
        String str = SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID;
        if (UserManager.get(ctx).isManagedProfile(userId)) {
            i = R.string.lock_screen_notifications_interstitial_title_profile;
        } else {
            i = R.string.lock_screen_notifications_interstitial_title;
        }
        return intent.putExtra(str, i).putExtra("android.intent.extra.USER_ID", userId);
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SetupRedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return SetupRedactionInterstitialFragment.class.getName().equals(fragmentName);
    }
}
