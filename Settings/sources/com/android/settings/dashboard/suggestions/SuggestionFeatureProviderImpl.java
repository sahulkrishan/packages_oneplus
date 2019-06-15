package com.android.settings.dashboard.suggestions;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import com.android.settings.Settings.NightDisplaySuggestionActivity;
import com.android.settings.display.NightDisplayPreferenceController;
import com.android.settings.fingerprint.FingerprintEnrollSuggestionActivity;
import com.android.settings.fingerprint.FingerprintSuggestionActivity;
import com.android.settings.notification.ZenOnboardingActivity;
import com.android.settings.notification.ZenSuggestionActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ScreenLockSuggestionActivity;
import com.android.settings.support.NewDeviceIntroSuggestionActivity;
import com.android.settings.wallpaper.WallpaperSuggestionActivity;
import com.android.settings.wifi.calling.WifiCallingSuggestionActivity;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.suggestions.SuggestionControllerMixin;
import java.util.List;

public class SuggestionFeatureProviderImpl implements SuggestionFeatureProvider {
    private static final int EXCLUSIVE_SUGGESTION_MAX_COUNT = 3;
    private static final String SHARED_PREF_FILENAME = "suggestions";
    private static final String TAG = "SuggestionFeature";
    private final MetricsFeatureProvider mMetricsFeatureProvider;

    public boolean isSuggestionEnabled(Context context) {
        context.getSystemService("activity");
        return false;
    }

    public ComponentName getSuggestionServiceComponent() {
        return new ComponentName("com.android.settings.intelligence", "com.android.settings.intelligence.suggestions.SuggestionService");
    }

    public boolean isSmartSuggestionEnabled(Context context) {
        return false;
    }

    public boolean isSuggestionComplete(Context context, @NonNull ComponentName component) {
        String className = component.getClassName();
        if (className.equals(WallpaperSuggestionActivity.class.getName())) {
            return WallpaperSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(FingerprintSuggestionActivity.class.getName())) {
            return FingerprintSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(FingerprintEnrollSuggestionActivity.class.getName())) {
            return FingerprintEnrollSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(ScreenLockSuggestionActivity.class.getName())) {
            return ScreenLockSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(WifiCallingSuggestionActivity.class.getName())) {
            return WifiCallingSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(NightDisplaySuggestionActivity.class.getName())) {
            return NightDisplayPreferenceController.isSuggestionComplete(context);
        }
        if (className.equals(NewDeviceIntroSuggestionActivity.class.getName())) {
            return NewDeviceIntroSuggestionActivity.isSuggestionComplete(context);
        }
        if (className.equals(ZenSuggestionActivity.class.getName())) {
            return ZenOnboardingActivity.isSuggestionComplete(context);
        }
        return false;
    }

    public SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREF_FILENAME, 0);
    }

    public SuggestionFeatureProviderImpl(Context context) {
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context.getApplicationContext()).getMetricsFeatureProvider();
    }

    public void filterExclusiveSuggestions(List<Tile> suggestions) {
        if (suggestions != null) {
            int i = suggestions.size();
            while (true) {
                i--;
                if (i >= 3) {
                    Log.d(TAG, "Removing exclusive suggestion");
                    suggestions.remove(i);
                } else {
                    return;
                }
            }
        }
    }

    public void dismissSuggestion(Context context, SuggestionControllerMixin mixin, Suggestion suggestion) {
        if (mixin != null && suggestion != null && context != null) {
            this.mMetricsFeatureProvider.action(context, 387, suggestion.getId(), new Pair[0]);
            mixin.dismissSuggestion(suggestion);
        }
    }

    public Pair<Integer, Object>[] getLoggingTaggedData(Context context) {
        boolean isSmartSuggestionEnabled = isSmartSuggestionEnabled(context);
        return new Pair[]{Pair.create(Integer.valueOf(1097), Integer.valueOf(isSmartSuggestionEnabled))};
    }
}
