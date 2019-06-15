package com.android.settings.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.drawer.CategoryManager;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.ProfileSelectDialog;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.drawer.TileUtils;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFeatureProviderImpl implements DashboardFeatureProvider {
    private static final String DASHBOARD_TILE_PREF_KEY_PREFIX = "dashboard_tile_pref_";
    private static final String META_DATA_KEY_INTENT_ACTION = "com.android.settings.intent.action";
    @VisibleForTesting
    static final String META_DATA_KEY_ORDER = "com.android.settings.order";
    private static final String TAG = "DashboardFeatureImpl";
    private static volatile ExecutorService sSingleThreadExecutor;
    private final CategoryManager mCategoryManager;
    protected final Context mContext;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final PackageManager mPackageManager;

    public DashboardFeatureProviderImpl(Context context) {
        this.mContext = context.getApplicationContext();
        this.mCategoryManager = CategoryManager.get(context, getExtraIntentAction());
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mPackageManager = context.getPackageManager();
    }

    public DashboardCategory getTilesForCategory(String key) {
        return this.mCategoryManager.getTilesByCategory(this.mContext, key);
    }

    public List<Preference> getPreferencesForCategory(Activity activity, Context context, int sourceMetricsCategory, String key) {
        DashboardCategory category = getTilesForCategory(key);
        if (category == null) {
            Log.d(TAG, "NO dashboard tiles for DashboardFeatureImpl");
            return null;
        }
        List<Tile> tiles = category.getTiles();
        Context context2;
        if (tiles == null || tiles.isEmpty()) {
            context2 = context;
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("tile list is empty, skipping category ");
            stringBuilder.append(category.title);
            Log.d(str, stringBuilder.toString());
            return null;
        }
        ArrayList preferences = new ArrayList();
        for (Tile tile : tiles) {
            Preference pref = new Preference(context);
            bindPreferenceToTile(activity, sourceMetricsCategory, pref, tile, null, Integer.MAX_VALUE);
            preferences.add(pref);
        }
        context2 = context;
        return preferences;
    }

    public List<DashboardCategory> getAllCategories() {
        return this.mCategoryManager.getCategories(this.mContext);
    }

    public boolean shouldTintIcon() {
        return this.mContext.getResources().getBoolean(R.bool.config_tintSettingIcon);
    }

    public String getDashboardKeyForTile(Tile tile) {
        if (tile == null || tile.intent == null) {
            return null;
        }
        if (!TextUtils.isEmpty(tile.key)) {
            return tile.key;
        }
        StringBuilder sb = new StringBuilder(DASHBOARD_TILE_PREF_KEY_PREFIX);
        sb.append(tile.intent.getComponent().getClassName());
        return sb.toString();
    }

    public void bindPreferenceToTile(Activity activity, int sourceMetricsCategory, Preference pref, Tile tile, String key, int baseOrder) {
        Preference preference = pref;
        Tile tile2 = tile;
        int i = baseOrder;
        if (preference != null) {
            preference.setTitle(tile2.title);
            if (TextUtils.isEmpty(key)) {
                String str = key;
                preference.setKey(getDashboardKeyForTile(tile2));
            } else {
                preference.setKey(key);
            }
            bindSummary(preference, tile2);
            bindIcon(preference, tile2);
            Bundle metadata = tile2.metaData;
            String clsName = null;
            String action = null;
            Integer order = null;
            if (metadata != null) {
                clsName = metadata.getString(SettingsActivity.META_DATA_KEY_FRAGMENT_CLASS);
                action = metadata.getString(META_DATA_KEY_INTENT_ACTION);
                if (metadata.containsKey(META_DATA_KEY_ORDER) && (metadata.get(META_DATA_KEY_ORDER) instanceof Integer)) {
                    order = Integer.valueOf(metadata.getInt(META_DATA_KEY_ORDER));
                }
            }
            String clsName2 = clsName;
            String action2 = action;
            Integer order2 = order;
            if (!TextUtils.isEmpty(clsName2)) {
                preference.setFragment(clsName2);
            } else if (tile2.intent != null) {
                Intent intent = new Intent(tile2.intent);
                intent.putExtra(VisibilityLoggerMixin.EXTRA_SOURCE_METRICS_CATEGORY, sourceMetricsCategory);
                if (action2 != null) {
                    intent.setAction(action2);
                }
                -$$Lambda$DashboardFeatureProviderImpl$EctMPOsKyfRtceDMH6yiU0UQS8U -__lambda_dashboardfeatureproviderimpl_ectmposkyfrtcedmh6yiu0uqs8u = r0;
                -$$Lambda$DashboardFeatureProviderImpl$EctMPOsKyfRtceDMH6yiU0UQS8U -__lambda_dashboardfeatureproviderimpl_ectmposkyfrtcedmh6yiu0uqs8u2 = new -$$Lambda$DashboardFeatureProviderImpl$EctMPOsKyfRtceDMH6yiU0UQS8U(this, activity, tile2, intent, sourceMetricsCategory);
                preference.setOnPreferenceClickListener(-__lambda_dashboardfeatureproviderimpl_ectmposkyfrtcedmh6yiu0uqs8u);
            }
            clsName = activity.getPackageName();
            if (order2 == null && tile2.priority != 0) {
                order2 = Integer.valueOf(-tile2.priority);
            }
            if (order2 != null) {
                boolean shouldSkipBaseOrderOffset = false;
                if (tile2.intent != null) {
                    shouldSkipBaseOrderOffset = TextUtils.equals(clsName, tile2.intent.getComponent().getPackageName());
                }
                if (shouldSkipBaseOrderOffset || i == Integer.MAX_VALUE) {
                    preference.setOrder(order2.intValue());
                } else {
                    preference.setOrder(order2.intValue() + i);
                }
            }
        }
    }

    public String getExtraIntentAction() {
        return null;
    }

    public void openTileIntent(Activity activity, Tile tile) {
        if (tile == null) {
            this.mContext.startActivity(new Intent("android.settings.SETTINGS").addFlags(32768));
        } else if (tile.intent != null) {
            launchIntentOrSelectProfile(activity, tile, new Intent(tile.intent).putExtra(VisibilityLoggerMixin.EXTRA_SOURCE_METRICS_CATEGORY, 35).addFlags(32768), 35);
        }
    }

    private void bindSummary(Preference preference, Tile tile) {
        if (tile.summary != null) {
            preference.setSummary(tile.summary);
        } else if (tile.metaData == null || !tile.metaData.containsKey(TileUtils.META_DATA_PREFERENCE_SUMMARY_URI)) {
            preference.setSummary((int) R.string.summary_placeholder);
        } else {
            preference.setSummary((int) R.string.summary_placeholder);
            postOnBackgroundThread(new -$$Lambda$DashboardFeatureProviderImpl$eT0JYpovsB0-eUpWXkBH1qYJv_I(this, tile, preference));
        }
    }

    public static /* synthetic */ void lambda$bindSummary$2(DashboardFeatureProviderImpl dashboardFeatureProviderImpl, Tile tile, Preference preference) {
        Log.d(TAG, "postOnBackgroundThread bindSummary start");
        Map<String, IContentProvider> providerMap = new ArrayMap();
        ThreadUtils.postOnMainThread(new -$$Lambda$DashboardFeatureProviderImpl$f6w3zqqhleyaUiHJCm70VP43jfI(preference, TileUtils.getTextFromUri(dashboardFeatureProviderImpl.mContext, tile.metaData.getString(TileUtils.META_DATA_PREFERENCE_SUMMARY_URI), providerMap, TileUtils.META_DATA_PREFERENCE_SUMMARY)));
        Log.d(TAG, "postOnBackgroundThread bindSummary end");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void bindIcon(Preference preference, Tile tile) {
        if (tile.icon != null) {
            preference.setIcon(tile.icon.loadDrawable(preference.getContext()));
        } else if (tile.metaData != null && tile.metaData.containsKey(TileUtils.META_DATA_PREFERENCE_ICON_URI)) {
            postOnBackgroundThread(new -$$Lambda$DashboardFeatureProviderImpl$6nCUbNprlrw--1aNwFQYcoGh4Oc(this, tile, preference));
        }
    }

    public static /* synthetic */ void lambda$bindIcon$4(DashboardFeatureProviderImpl dashboardFeatureProviderImpl, Tile tile, Preference preference) {
        Log.d(TAG, "postOnBackgroundThread bindIcon start");
        String packageName = null;
        if (tile.intent != null) {
            Intent intent = tile.intent;
            if (!TextUtils.isEmpty(intent.getPackage())) {
                packageName = intent.getPackage();
            } else if (intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }
        }
        Map<String, IContentProvider> providerMap = new ArrayMap();
        String uri = tile.metaData.getString(TileUtils.META_DATA_PREFERENCE_ICON_URI);
        Pair<String, Integer> iconInfo = TileUtils.getIconFromUri(dashboardFeatureProviderImpl.mContext, packageName, uri, providerMap);
        if (iconInfo == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to get icon from uri ");
            stringBuilder.append(uri);
            Log.w(str, stringBuilder.toString());
            return;
        }
        ThreadUtils.postOnMainThread(new -$$Lambda$DashboardFeatureProviderImpl$GREAS10FflfW9_XoMVZ4GOVTVF8(preference, Icon.createWithResource((String) iconInfo.first, ((Integer) iconInfo.second).intValue())));
        Log.d(TAG, "postOnBackgroundThread bindIcon end");
    }

    private void launchIntentOrSelectProfile(Activity activity, Tile tile, Intent intent, int sourceMetricCategory) {
        if (isIntentResolvable(intent)) {
            ProfileSelectDialog.updateUserHandlesIfNeeded(this.mContext, tile);
            if (tile.userHandle == null) {
                this.mMetricsFeatureProvider.logDashboardStartIntent(this.mContext, intent, sourceMetricCategory);
                activity.startActivityForResult(intent, 0);
            } else if (tile.userHandle.size() == 1) {
                this.mMetricsFeatureProvider.logDashboardStartIntent(this.mContext, intent, sourceMetricCategory);
                activity.startActivityForResultAsUser(intent, 0, (UserHandle) tile.userHandle.get(0));
            } else {
                ProfileSelectDialog.show(activity.getFragmentManager(), tile);
            }
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Cannot resolve intent, skipping. ");
        stringBuilder.append(intent);
        Log.w(str, stringBuilder.toString());
    }

    private boolean isIntentResolvable(Intent intent) {
        return this.mPackageManager.resolveActivity(intent, 0) != null;
    }

    public static void postOnBackgroundThread(Runnable runnable) {
        if (sSingleThreadExecutor == null) {
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        sSingleThreadExecutor.execute(runnable);
    }
}
