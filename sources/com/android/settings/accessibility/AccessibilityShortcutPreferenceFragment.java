package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.view.accessibility.AccessibilityManager;
import android.widget.Switch;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.accessibility.AccessibilityShortcutController.ToggleableFrameworkFeatureInfo;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.accessibility.AccessibilityUtils;

public class AccessibilityShortcutPreferenceFragment extends ToggleFeaturePreferenceFragment implements Indexable {
    public static final String ON_LOCK_SCREEN_KEY = "accessibility_shortcut_on_lock_screen";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return false;
        }
    };
    public static final String SHORTCUT_SERVICE_KEY = "accessibility_shortcut_service";
    private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            AccessibilityShortcutPreferenceFragment.this.updatePreferences();
        }
    };
    private SwitchPreference mOnLockScreenSwitchPreference;
    private Preference mServicePreference;

    public int getMetricsCategory() {
        return 6;
    }

    public int getHelpResource() {
        return R.string.help_url_accessibility_shortcut;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mServicePreference = findPreference(SHORTCUT_SERVICE_KEY);
        this.mOnLockScreenSwitchPreference = (SwitchPreference) findPreference(ON_LOCK_SCREEN_KEY);
        this.mOnLockScreenSwitchPreference.setOnPreferenceChangeListener(new -$$Lambda$AccessibilityShortcutPreferenceFragment$v5UnURHl-V2dl7gTZw_kdUDDZ6E(this));
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.accessibility_shortcut_description);
    }

    public void onResume() {
        super.onResume();
        updatePreferences();
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_shortcut_dialog_shown"), false, this.mContentObserver);
    }

    public void onPause() {
        getContentResolver().unregisterContentObserver(this.mContentObserver);
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_shortcut_settings;
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mSwitchBar.addOnSwitchChangeListener(new -$$Lambda$AccessibilityShortcutPreferenceFragment$B1JGpZUcoOdF9ofKXLGiPDgZ6Bo(this));
    }

    public static /* synthetic */ void lambda$onInstallSwitchBarToggleSwitch$1(AccessibilityShortcutPreferenceFragment accessibilityShortcutPreferenceFragment, Switch switchView, boolean enabled) {
        Context context = accessibilityShortcutPreferenceFragment.getContext();
        if (!enabled || shortcutFeatureAvailable(context)) {
            accessibilityShortcutPreferenceFragment.onPreferenceToggled("accessibility_shortcut_enabled", enabled);
            return;
        }
        Secure.putInt(accessibilityShortcutPreferenceFragment.getContentResolver(), "accessibility_shortcut_enabled", 1);
        accessibilityShortcutPreferenceFragment.mServicePreference.setEnabled(true);
        accessibilityShortcutPreferenceFragment.mServicePreference.performClick();
    }

    /* Access modifiers changed, original: protected */
    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Secure.putInt(getContentResolver(), preferenceKey, enabled);
        updatePreferences();
    }

    private void updatePreferences() {
        ContentResolver cr = getContentResolver();
        Context context = getContext();
        this.mServicePreference.setSummary(getServiceName(context));
        boolean enabledFromLockScreen = false;
        if (!shortcutFeatureAvailable(context)) {
            Secure.putInt(getContentResolver(), "accessibility_shortcut_enabled", 0);
        }
        this.mSwitchBar.setChecked(Secure.getInt(cr, "accessibility_shortcut_enabled", 1) == 1);
        if (Secure.getInt(cr, ON_LOCK_SCREEN_KEY, Secure.getInt(cr, "accessibility_shortcut_dialog_shown", 0)) == 1) {
            enabledFromLockScreen = true;
        }
        this.mOnLockScreenSwitchPreference.setChecked(enabledFromLockScreen);
        this.mServicePreference.setEnabled(this.mToggleSwitch.isChecked());
        this.mOnLockScreenSwitchPreference.setEnabled(this.mToggleSwitch.isChecked());
    }

    public static CharSequence getServiceName(Context context) {
        if (!shortcutFeatureAvailable(context)) {
            return context.getString(R.string.accessibility_no_service_selected);
        }
        AccessibilityServiceInfo shortcutServiceInfo = getServiceInfo(context);
        if (shortcutServiceInfo != null) {
            return shortcutServiceInfo.getResolveInfo().loadLabel(context.getPackageManager());
        }
        return ((ToggleableFrameworkFeatureInfo) AccessibilityShortcutController.getFrameworkShortcutFeaturesMap().get(getShortcutComponent(context))).getLabel(context);
    }

    private static AccessibilityServiceInfo getServiceInfo(Context context) {
        return AccessibilityManager.getInstance(context).getInstalledServiceInfoWithComponentName(getShortcutComponent(context));
    }

    private static boolean shortcutFeatureAvailable(Context context) {
        ComponentName shortcutFeature = getShortcutComponent(context);
        boolean z = false;
        if (shortcutFeature == null) {
            return false;
        }
        if (AccessibilityShortcutController.getFrameworkShortcutFeaturesMap().containsKey(shortcutFeature)) {
            return true;
        }
        if (getServiceInfo(context) != null) {
            z = true;
        }
        return z;
    }

    private static ComponentName getShortcutComponent(Context context) {
        String componentNameString = AccessibilityUtils.getShortcutTargetServiceComponentNameString(context, UserHandle.myUserId());
        if (componentNameString == null) {
            return null;
        }
        return ComponentName.unflattenFromString(componentNameString);
    }
}
