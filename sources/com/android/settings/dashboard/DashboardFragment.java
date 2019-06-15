package com.android.settings.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerListHelper;
import com.android.settings.dashboard.SummaryLoader.SummaryConsumer;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.drawer.TileUtils;
import com.oneplus.setting.lib.SettingsDrawerActivity;
import com.oneplus.setting.lib.SettingsDrawerActivity.CategoryListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DashboardFragment extends SettingsPreferenceFragment implements CategoryListener, Indexable, SummaryConsumer {
    private static final String TAG = "DashboardFragment";
    private DashboardFeatureProvider mDashboardFeatureProvider;
    private final Set<String> mDashboardTilePrefKeys = new ArraySet();
    private boolean mListeningToCategoryChange;
    private DashboardTilePlaceholderPreferenceController mPlaceholderPreferenceController;
    private final Map<Class, List<AbstractPreferenceController>> mPreferenceControllers = new ArrayMap();
    private SummaryLoader mSummaryLoader;

    public abstract String getLogTag();

    public abstract int getPreferenceScreenResId();

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(context).getDashboardFeatureProvider(context);
        List<AbstractPreferenceController> controllers = new ArrayList();
        List<AbstractPreferenceController> controllersFromCode = createPreferenceControllers(context);
        List<BasePreferenceController> uniqueControllerFromXml = PreferenceControllerListHelper.filterControllers(PreferenceControllerListHelper.getPreferenceControllersFromXml(context, getPreferenceScreenResId()), controllersFromCode);
        if (controllersFromCode != null) {
            controllers.addAll(controllersFromCode);
        }
        controllers.addAll(uniqueControllerFromXml);
        uniqueControllerFromXml.stream().filter(-$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y.INSTANCE).forEach(new -$$Lambda$DashboardFragment$iYpWkssUBFPuOKWOC_GeIjRUfdk(getLifecycle()));
        this.mPlaceholderPreferenceController = new DashboardTilePlaceholderPreferenceController(context);
        controllers.add(this.mPlaceholderPreferenceController);
        for (AbstractPreferenceController controller : controllers) {
            addPreferenceController(controller);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            updatePreferenceStates();
        }
    }

    public void onCategoriesChanged() {
        if (this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey()) != null) {
            refreshDashboardTiles(getLogTag());
        }
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        refreshAllPreferences(getLogTag());
    }

    public void onStart() {
        super.onStart();
        if (this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey()) != null) {
            if (this.mSummaryLoader != null) {
                this.mSummaryLoader.setListening(true);
            }
            Activity activity = getActivity();
            if (activity instanceof SettingsDrawerActivity) {
                this.mListeningToCategoryChange = true;
                ((SettingsDrawerActivity) activity).addCategoryListener(this);
            }
        }
    }

    public void notifySummaryChanged(Tile tile) {
        Preference pref = getPreferenceScreen().findPreference(this.mDashboardFeatureProvider.getDashboardKeyForTile(tile));
        if (pref == null) {
            Log.d(getLogTag(), String.format("Can't find pref by key %s, skipping update summary %s/%s", new Object[]{key, tile.title, tile.summary}));
            return;
        }
        pref.setSummary(tile.summary);
    }

    public void onResume() {
        super.onResume();
        updatePreferenceStates();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Collection<List<AbstractPreferenceController>> controllers = this.mPreferenceControllers.values();
        this.mMetricsFeatureProvider.logDashboardStartIntent(getContext(), preference.getIntent(), getMetricsCategory());
        for (List<AbstractPreferenceController> controllerList : controllers) {
            for (AbstractPreferenceController controller : controllerList) {
                if (controller.handlePreferenceTreeClick(preference)) {
                    return true;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onStop() {
        super.onStop();
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.setListening(false);
        }
        if (this.mListeningToCategoryChange) {
            Activity activity = getActivity();
            if (activity instanceof SettingsDrawerActivity) {
                ((SettingsDrawerActivity) activity).remCategoryListener(this);
            }
            this.mListeningToCategoryChange = false;
        }
    }

    /* Access modifiers changed, original: protected */
    public <T extends AbstractPreferenceController> T use(Class<T> clazz) {
        List<AbstractPreferenceController> controllerList = (List) this.mPreferenceControllers.get(clazz);
        if (controllerList == null) {
            return null;
        }
        if (controllerList.size() > 1) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Multiple controllers of Class ");
            stringBuilder.append(clazz.getSimpleName());
            stringBuilder.append(" found, returning first one.");
            Log.w(str, stringBuilder.toString());
        }
        return (AbstractPreferenceController) controllerList.get(0);
    }

    /* Access modifiers changed, original: protected */
    public void addPreferenceController(AbstractPreferenceController controller) {
        if (this.mPreferenceControllers.get(controller.getClass()) == null) {
            this.mPreferenceControllers.put(controller.getClass(), new ArrayList());
        }
        ((List) this.mPreferenceControllers.get(controller.getClass())).add(controller);
    }

    @VisibleForTesting
    public String getCategoryKey() {
        return (String) DashboardFragmentRegistry.PARENT_TO_CATEGORY_KEY_MAP.get(getClass().getName());
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean displayTile(Tile tile) {
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean tintTileIcon(Tile tile) {
        boolean z = false;
        if (tile.icon == null) {
            return false;
        }
        Bundle metadata = tile.metaData;
        if (metadata != null && metadata.containsKey(TileUtils.META_DATA_PREFERENCE_ICON_TINTABLE)) {
            return metadata.getBoolean(TileUtils.META_DATA_PREFERENCE_ICON_TINTABLE);
        }
        String pkgName = getContext().getPackageName();
        if (!(pkgName == null || tile.intent == null || pkgName.equals(tile.intent.getComponent().getPackageName()))) {
            z = true;
        }
        return z;
    }

    private void displayResourceTiles() {
        int resId = getPreferenceScreenResId();
        if (resId > 0) {
            addPreferencesFromResource(resId);
            this.mPreferenceControllers.values().stream().flatMap(-$$Lambda$seyL25CSW2NInOydsTbSDrNW6pM.INSTANCE).forEach(new -$$Lambda$DashboardFragment$wmCpqAavTrPCWLW0gqd6-3n9DOU(getPreferenceScreen()));
        }
    }

    /* Access modifiers changed, original: protected */
    public void updatePreferenceStates() {
        PreferenceScreen screen = getPreferenceScreen();
        for (List<AbstractPreferenceController> controllerList : this.mPreferenceControllers.values()) {
            for (AbstractPreferenceController controller : controllerList) {
                if (controller.isAvailable()) {
                    Preference preference = screen.findPreference(controller.getPreferenceKey());
                    if (preference == null) {
                        Log.d(TAG, String.format("Cannot find preference with key %s in Controller %s", new Object[]{key, controller.getClass().getSimpleName()}));
                    } else {
                        controller.updateState(preference);
                    }
                }
            }
        }
    }

    private void refreshAllPreferences(String TAG) {
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        displayResourceTiles();
        refreshDashboardTiles(TAG);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting(otherwise = 2)
    public void refreshDashboardTiles(String TAG) {
        String str = TAG;
        PreferenceScreen screen = getPreferenceScreen();
        DashboardCategory category = this.mDashboardFeatureProvider.getTilesForCategory(getCategoryKey());
        if (category == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("NO dashboard tiles for ");
            stringBuilder.append(str);
            Log.d(str, stringBuilder.toString());
            return;
        }
        List<Tile> tiles = category.getTiles();
        if (tiles == null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("tile list is empty, skipping category ");
            stringBuilder2.append(category.title);
            Log.d(str, stringBuilder2.toString());
            return;
        }
        String key;
        List<String> remove = new ArrayList(this.mDashboardTilePrefKeys);
        if (this.mSummaryLoader != null) {
            this.mSummaryLoader.release();
        }
        Context context = getContext();
        this.mSummaryLoader = new SummaryLoader(getActivity(), getCategoryKey());
        this.mSummaryLoader.setSummaryConsumer(this);
        TypedArray a = context.obtainStyledAttributes(new int[]{16843817});
        int tintColor = a.getColor(0, context.getColor(17170443));
        a.recycle();
        for (Tile tile : tiles) {
            String key2 = this.mDashboardFeatureProvider.getDashboardKeyForTile(tile);
            if (TextUtils.isEmpty(key2)) {
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("tile does not contain a key, skipping ");
                stringBuilder3.append(tile);
                Log.d(str, stringBuilder3.toString());
            } else if (displayTile(tile)) {
                if (tintTileIcon(tile)) {
                    tile.icon.setTint(tintColor);
                } else if (tile.icon != null) {
                    tile.icon.setTint(context.getColor(R.color.oneplus_contorl_icon_color_active_default));
                }
                if (this.mDashboardTilePrefKeys.contains(key2)) {
                    Preference preference = screen.findPreference(key2);
                    key = key2;
                    this.mDashboardFeatureProvider.bindPreferenceToTile(getActivity(), getMetricsCategory(), preference, tile, key, this.mPlaceholderPreferenceController.getOrder());
                } else {
                    key = key2;
                    Preference preference2 = new Preference(getPrefContext());
                    Preference pref = preference2;
                    this.mDashboardFeatureProvider.bindPreferenceToTile(getActivity(), getMetricsCategory(), preference2, tile, key, this.mPlaceholderPreferenceController.getOrder());
                    screen.addPreference(pref);
                    this.mDashboardTilePrefKeys.add(key);
                }
                remove.remove(key);
                str = TAG;
            }
        }
        for (String key3 : remove) {
            this.mDashboardTilePrefKeys.remove(key3);
            Preference preference3 = screen.findPreference(key3);
            if (preference3 != null) {
                screen.removePreference(preference3);
            }
        }
        this.mSummaryLoader.setListening(true);
    }
}
