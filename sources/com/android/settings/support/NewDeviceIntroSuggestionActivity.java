package com.android.settings.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.overlay.SupportFeatureProvider;
import java.util.List;

public class NewDeviceIntroSuggestionActivity extends Activity {
    @VisibleForTesting
    static final long PERMANENT_DISMISS_THRESHOLD = 1209600000;
    @VisibleForTesting
    static final String PREF_KEY_SUGGGESTION_COMPLETE = "pref_new_device_intro_suggestion_complete";
    @VisibleForTesting
    static final String PREF_KEY_SUGGGESTION_FIRST_DISPLAY_TIME = "pref_new_device_intro_suggestion_first_display_time_ms";
    private static final String TAG = "NewDeviceIntroSugg";
    public static final String TIPS_PACKAGE_NAME = "com.google.android.apps.tips";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getLaunchIntent(this);
        if (intent != null) {
            FeatureFactory.getFactory(this).getSuggestionFeatureProvider(this).getSharedPrefs(this).edit().putBoolean(PREF_KEY_SUGGGESTION_COMPLETE, true).commit();
            startActivity(intent);
        }
        finish();
    }

    public static boolean isSuggestionComplete(Context context) {
        return isTipsInstalledAsSystemApp(context) || !isSupported(context) || isExpired(context) || hasLaunchedBefore(context) || !canOpenUrlInBrowser(context);
    }

    private static boolean isSupported(Context context) {
        return context.getResources().getBoolean(R.bool.config_new_device_intro_suggestion_supported);
    }

    private static boolean isExpired(Context context) {
        long firstDisplayTimeMs;
        SharedPreferences prefs = FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context);
        long currentTimeMs = System.currentTimeMillis();
        if (prefs.contains(PREF_KEY_SUGGGESTION_FIRST_DISPLAY_TIME)) {
            firstDisplayTimeMs = prefs.getLong(PREF_KEY_SUGGGESTION_FIRST_DISPLAY_TIME, -1);
        } else {
            firstDisplayTimeMs = currentTimeMs;
            prefs.edit().putLong(PREF_KEY_SUGGGESTION_FIRST_DISPLAY_TIME, currentTimeMs).commit();
        }
        boolean expired = currentTimeMs > PERMANENT_DISMISS_THRESHOLD + firstDisplayTimeMs;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("is suggestion expired: ");
        stringBuilder.append(expired);
        Log.d(str, stringBuilder.toString());
        return expired;
    }

    private static boolean canOpenUrlInBrowser(Context context) {
        Intent intent = getLaunchIntent(context);
        boolean z = false;
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!(resolveInfos == null || resolveInfos.size() == 0)) {
            z = true;
        }
        return z;
    }

    private static boolean hasLaunchedBefore(Context context) {
        return FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context).getBoolean(PREF_KEY_SUGGGESTION_COMPLETE, false);
    }

    @VisibleForTesting
    static Intent getLaunchIntent(Context context) {
        SupportFeatureProvider supportProvider = FeatureFactory.getFactory(context).getSupportFeatureProvider(context);
        if (supportProvider == null) {
            return null;
        }
        String url = supportProvider.getNewDeviceIntroUrl(context);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return new Intent().setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse(url));
    }

    private static boolean isTipsInstalledAsSystemApp(@NonNull Context context) {
        boolean z = false;
        try {
            if (context.getPackageManager().getPackageInfo(TIPS_PACKAGE_NAME, 1048576) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Cannot find the package: com.google.android.apps.tips", e);
            return false;
        }
    }
}
