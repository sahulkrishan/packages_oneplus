package com.android.settings.security;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import java.util.Arrays;
import java.util.List;

public class ScreenPinningSettings extends SettingsPreferenceFragment implements OnSwitchChangeListener, Indexable {
    private static final int CHANGE_LOCK_METHOD_REQUEST = 43;
    private static final CharSequence KEY_USE_SCREEN_LOCK = "use_screen_lock";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.screen_pinning_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private LockPatternUtils mLockPatternUtils;
    private SwitchBar mSwitchBar;
    private SwitchPreference mUseScreenLock;

    public int getMetricsCategory() {
        return 86;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        activity.setTitle(R.string.screen_pinning_title);
        this.mLockPatternUtils = new LockPatternUtils(activity);
        this.mSwitchBar = activity.getSwitchBar();
        this.mSwitchBar.addOnSwitchChangeListener(this);
        this.mSwitchBar.show();
        this.mSwitchBar.setChecked(isLockToAppEnabled(getActivity()));
        if (System.getInt(activity.getContentResolver(), "op_navigation_bar_type", 1) == 3) {
            this.mSwitchBar.setEnabled(false);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_screen_pinning;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup parent = (ViewGroup) view.findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER);
        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.screen_pinning_instructions, parent, false);
        parent.addView(emptyView);
        setEmptyView(emptyView);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mSwitchBar.hide();
    }

    private static boolean isLockToAppEnabled(Context context) {
        return System.getInt(context.getContentResolver(), "lock_to_app_enabled", 0) != 0;
    }

    private void setLockToAppEnabled(boolean isEnabled) {
        System.putInt(getContentResolver(), "lock_to_app_enabled", isEnabled);
        if (isEnabled) {
            setScreenLockUsedSetting(isScreenLockUsed());
        }
    }

    private boolean isScreenLockUsed() {
        if (Secure.getInt(getContentResolver(), "lock_to_app_exit_locked", getCurrentSecurityTitle() != R.string.screen_pinning_unlock_none ? 1 : 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean setScreenLockUsed(boolean isEnabled) {
        if (isEnabled && new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) == 0) {
            Intent chooseLockIntent = new Intent("android.app.action.SET_NEW_PASSWORD");
            chooseLockIntent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
            startActivityForResult(chooseLockIntent, 43);
            return false;
        }
        setScreenLockUsedSetting(isEnabled);
        return true;
    }

    private void setScreenLockUsedSetting(boolean isEnabled) {
        Secure.putInt(getContentResolver(), "lock_to_app_exit_locked", isEnabled);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 43) {
            boolean validPassQuality = new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) != 0;
            setScreenLockUsed(validPassQuality);
            this.mUseScreenLock.setChecked(validPassQuality);
        }
    }

    private int getCurrentSecurityTitle() {
        int quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId());
        if (quality != 65536) {
            if (quality == 131072 || quality == 196608) {
                return R.string.screen_pinning_unlock_pin;
            }
            if (quality == 262144 || quality == 327680 || quality == 393216 || quality == 524288) {
                return R.string.screen_pinning_unlock_password;
            }
        } else if (this.mLockPatternUtils.isLockPatternEnabled(UserHandle.myUserId())) {
            return R.string.screen_pinning_unlock_pattern;
        }
        return R.string.screen_pinning_unlock_none;
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        setLockToAppEnabled(isChecked);
        updateDisplay();
    }

    public void updateDisplay() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        if (isLockToAppEnabled(getActivity())) {
            addPreferencesFromResource(R.xml.screen_pinning_settings);
            this.mUseScreenLock = (SwitchPreference) getPreferenceScreen().findPreference(KEY_USE_SCREEN_LOCK);
            this.mUseScreenLock.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return ScreenPinningSettings.this.setScreenLockUsed(((Boolean) newValue).booleanValue());
                }
            });
            this.mUseScreenLock.setChecked(isScreenLockUsed());
            this.mUseScreenLock.setTitle(getCurrentSecurityTitle());
            if (System.getInt(getActivity().getContentResolver(), "op_navigation_bar_type", 1) == 3) {
                this.mUseScreenLock.setEnabled(false);
            }
        }
    }
}
