package com.android.settings.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;

public class ZenOnboardingActivity extends Activity {
    @VisibleForTesting
    static final long ALWAYS_SHOW_THRESHOLD = 1209600000;
    @VisibleForTesting
    static final String PREF_KEY_SUGGESTION_FIRST_DISPLAY_TIME = "pref_zen_suggestion_first_display_time_ms";
    private static final String TAG = "ZenOnboardingActivity";
    View mKeepCurrentSetting;
    RadioButton mKeepCurrentSettingButton;
    private MetricsLogger mMetrics;
    View mNewSetting;
    RadioButton mNewSettingButton;
    private NotificationManager mNm;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNotificationManager((NotificationManager) getSystemService(NotificationManager.class));
        setMetricsLogger(new MetricsLogger());
        Global.putInt(getApplicationContext().getContentResolver(), "zen_settings_suggestion_viewed", 1);
        setupUI();
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void setupUI() {
        setContentView(R.layout.zen_onboarding);
        this.mNewSetting = findViewById(R.id.zen_onboarding_new_setting);
        this.mKeepCurrentSetting = findViewById(R.id.zen_onboarding_current_setting);
        this.mNewSettingButton = (RadioButton) findViewById(R.id.zen_onboarding_new_setting_button);
        this.mKeepCurrentSettingButton = (RadioButton) findViewById(R.id.zen_onboarding_current_setting_button);
        OnClickListener newSettingClickListener = new OnClickListener() {
            public void onClick(View v) {
                ZenOnboardingActivity.this.mKeepCurrentSettingButton.setChecked(false);
                ZenOnboardingActivity.this.mNewSettingButton.setChecked(true);
            }
        };
        OnClickListener currentSettingClickListener = new OnClickListener() {
            public void onClick(View v) {
                ZenOnboardingActivity.this.mKeepCurrentSettingButton.setChecked(true);
                ZenOnboardingActivity.this.mNewSettingButton.setChecked(false);
            }
        };
        this.mNewSetting.setOnClickListener(newSettingClickListener);
        this.mNewSettingButton.setOnClickListener(newSettingClickListener);
        this.mKeepCurrentSetting.setOnClickListener(currentSettingClickListener);
        this.mKeepCurrentSettingButton.setOnClickListener(currentSettingClickListener);
        this.mKeepCurrentSettingButton.setChecked(true);
        this.mMetrics.visible(1380);
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void setNotificationManager(NotificationManager nm) {
        this.mNm = nm;
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void setMetricsLogger(MetricsLogger ml) {
        this.mMetrics = ml;
    }

    public void launchSettings(View button) {
        this.mMetrics.action(1379);
        Intent settings = new Intent("android.settings.ZEN_MODE_SETTINGS");
        settings.addFlags(268468224);
        startActivity(settings);
    }

    public void save(View button) {
        Policy policy = this.mNm.getNotificationPolicy();
        if (this.mNewSettingButton.isChecked()) {
            this.mNm.setNotificationPolicy(new Policy(16 | policy.priorityCategories, 2, policy.priorityMessageSenders, Policy.getAllSuppressedVisualEffects()));
            this.mMetrics.action(1378);
        } else {
            this.mMetrics.action(1406);
        }
        Global.putInt(getApplicationContext().getContentResolver(), "zen_settings_updated", 1);
        finishAndRemoveTask();
    }

    public static boolean isSuggestionComplete(Context context) {
        if (wasZenUpdated(context)) {
            return true;
        }
        if (showSuggestion(context) || withinShowTimeThreshold(context)) {
            return false;
        }
        return true;
    }

    private static boolean wasZenUpdated(Context context) {
        if (Policy.areAllVisualEffectsSuppressed(((NotificationManager) context.getSystemService(NotificationManager.class)).getNotificationPolicy().suppressedVisualEffects)) {
            Global.putInt(context.getContentResolver(), "zen_settings_updated", 1);
        }
        if (Global.getInt(context.getContentResolver(), "zen_settings_updated", 0) != 0) {
            return true;
        }
        return false;
    }

    private static boolean showSuggestion(Context context) {
        return Global.getInt(context.getContentResolver(), "show_zen_settings_suggestion", 0) != 0;
    }

    private static boolean withinShowTimeThreshold(Context context) {
        long firstDisplayTimeMs;
        SharedPreferences prefs = FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context);
        long currentTimeMs = System.currentTimeMillis();
        if (prefs.contains(PREF_KEY_SUGGESTION_FIRST_DISPLAY_TIME)) {
            firstDisplayTimeMs = prefs.getLong(PREF_KEY_SUGGESTION_FIRST_DISPLAY_TIME, -1);
        } else {
            firstDisplayTimeMs = currentTimeMs;
            prefs.edit().putLong(PREF_KEY_SUGGESTION_FIRST_DISPLAY_TIME, currentTimeMs).commit();
        }
        boolean stillShow = currentTimeMs < ALWAYS_SHOW_THRESHOLD + firstDisplayTimeMs;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("still show zen suggestion based on time: ");
        stringBuilder.append(stillShow);
        Log.d(str, stringBuilder.toString());
        return stillShow;
    }
}
