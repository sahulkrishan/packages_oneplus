package com.android.settings.wallpaper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class WallpaperTypeSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> rList = pm.queryIntentActivities(intent, 65536);
            String wallpaperPickerPackage = context.getString(R.string.config_wallpaper_picker_package);
            for (ResolveInfo info : rList) {
                if (wallpaperPickerPackage.equals(info.activityInfo.packageName)) {
                    CharSequence label = info.loadLabel(pm);
                    if (label == null) {
                        label = info.activityInfo.packageName;
                    }
                    SearchIndexableRaw data = new SearchIndexableRaw(context);
                    data.title = label.toString();
                    data.key = "wallpaper_type_settings";
                    data.screenTitle = context.getResources().getString(R.string.wallpaper_settings_fragment_title);
                    data.intentAction = "android.intent.action.SET_WALLPAPER";
                    data.intentTargetPackage = info.activityInfo.packageName;
                    data.intentTargetClass = info.activityInfo.name;
                    data.keywords = context.getString(R.string.keywords_wallpaper);
                    result.add(data);
                }
            }
            return result;
        }
    };

    public int getMetricsCategory() {
        return 101;
    }

    public int getHelpResource() {
        return R.string.help_uri_wallpaper;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_settings);
        populateWallpaperTypes();
    }

    private void populateWallpaperTypes() {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> rList = pm.queryIntentActivities(intent, 65536);
        PreferenceScreen parent = getPreferenceScreen();
        parent.setOrderingAsAdded(false);
        for (ResolveInfo info : rList) {
            Preference pref = new Preference(getPrefContext());
            Intent prefIntent = new Intent(intent).addFlags(33554432);
            prefIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            pref.setIntent(prefIntent);
            CharSequence label = info.loadLabel(pm);
            if (label == null) {
                label = info.activityInfo.packageName;
            }
            pref.setTitle(label);
            pref.setIcon(info.loadIcon(pm));
            parent.addPreference(pref);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getIntent() == null) {
            return super.onPreferenceTreeClick(preference);
        }
        startActivity(preference.getIntent());
        finish();
        return true;
    }
}
