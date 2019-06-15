package com.android.settings.enterprise;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.IconDrawableFactory;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ApplicationFeatureProvider.ListOfAppsCallback;
import com.android.settings.applications.UserAppInfo;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public class ApplicationListPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, ListOfAppsCallback {
    private SettingsPreferenceFragment mParent;
    private final PackageManager mPm;

    public interface ApplicationListBuilder {
        void buildApplicationList(Context context, ListOfAppsCallback listOfAppsCallback);
    }

    public ApplicationListPreferenceController(Context context, ApplicationListBuilder builder, PackageManager packageManager, SettingsPreferenceFragment parent) {
        super(context);
        this.mPm = packageManager;
        this.mParent = parent;
        builder.buildApplicationList(context, this);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void onListOfAppsResult(List<UserAppInfo> result) {
        PreferenceScreen screen = this.mParent.getPreferenceScreen();
        if (screen != null) {
            IconDrawableFactory iconDrawableFactory = IconDrawableFactory.newInstance(this.mContext);
            Context prefContext = this.mParent.getPreferenceManager().getContext();
            for (int position = 0; position < result.size(); position++) {
                UserAppInfo item = (UserAppInfo) result.get(position);
                Preference preference = new AppPreference(prefContext);
                preference.setTitle(item.appInfo.loadLabel(this.mPm));
                preference.setIcon(iconDrawableFactory.getBadgedIcon(item.appInfo));
                preference.setOrder(position);
                preference.setSelectable(false);
                screen.addPreference(preference);
            }
        }
    }
}
