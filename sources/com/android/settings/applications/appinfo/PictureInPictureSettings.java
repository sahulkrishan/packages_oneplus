package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.IconDrawableFactory;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PictureInPictureSettings extends EmptyTextSettings {
    @VisibleForTesting
    static final List<String> IGNORE_PACKAGE_LIST = new ArrayList();
    private static final String TAG = PictureInPictureSettings.class.getSimpleName();
    private Context mContext;
    private IconDrawableFactory mIconDrawableFactory;
    private PackageManagerWrapper mPackageManager;
    private UserManager mUserManager;

    static class AppComparator implements Comparator<Pair<ApplicationInfo, Integer>> {
        private final Collator mCollator = Collator.getInstance();
        private final PackageManager mPm;

        public AppComparator(PackageManager pm) {
            this.mPm = pm;
        }

        public final int compare(Pair<ApplicationInfo, Integer> a, Pair<ApplicationInfo, Integer> b) {
            CharSequence sa = ((ApplicationInfo) a.first).loadLabel(this.mPm);
            if (sa == null) {
                sa = ((ApplicationInfo) a.first).name;
            }
            CharSequence sb = ((ApplicationInfo) b.first).loadLabel(this.mPm);
            if (sb == null) {
                sb = ((ApplicationInfo) b.first).name;
            }
            int nameCmp = this.mCollator.compare(sa.toString(), sb.toString());
            if (nameCmp != 0) {
                return nameCmp;
            }
            return ((Integer) a.second).intValue() - ((Integer) b.second).intValue();
        }
    }

    static {
        IGNORE_PACKAGE_LIST.add(SliceBroadcastRelay.SYSTEMUI_PACKAGE);
    }

    public static boolean checkPackageHasPictureInPictureActivities(String packageName, ActivityInfo[] activities) {
        if (!(IGNORE_PACKAGE_LIST.contains(packageName) || activities == null)) {
            for (int i = activities.length - 1; i >= 0; i--) {
                if (activities[i].supportsPictureInPicture()) {
                    return true;
                }
            }
        }
        return false;
    }

    public PictureInPictureSettings(PackageManagerWrapper pm, UserManager um) {
        this.mPackageManager = pm;
        this.mUserManager = um;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(this.mContext);
    }

    public void onResume() {
        super.onResume();
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        PackageManager pm = this.mPackageManager.getPackageManager();
        ArrayList<Pair<ApplicationInfo, Integer>> pipApps = collectPipApps(UserHandle.myUserId());
        Collections.sort(pipApps, new AppComparator(pm));
        Context prefContext = getPrefContext();
        Iterator it = pipApps.iterator();
        while (it.hasNext()) {
            Pair<ApplicationInfo, Integer> appData = (Pair) it.next();
            final ApplicationInfo appInfo = appData.first;
            int userId = ((Integer) appData.second).intValue();
            UserHandle user = UserHandle.of(userId);
            final String packageName = appInfo.packageName;
            CharSequence label = appInfo.loadLabel(pm);
            Preference pref = new AppPreference(prefContext);
            pref.setIcon(this.mIconDrawableFactory.getBadgedIcon(appInfo, userId));
            pref.setTitle(pm.getUserBadgedLabel(label, user));
            pref.setSummary(PictureInPictureDetails.getPreferenceSummary(prefContext, appInfo.uid, packageName));
            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AppInfoBase.startAppInfoFragment(PictureInPictureDetails.class, R.string.picture_in_picture_app_detail_title, packageName, appInfo.uid, PictureInPictureSettings.this, -1, PictureInPictureSettings.this.getMetricsCategory());
                    return true;
                }
            });
            screen.addPreference(pref);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(R.string.picture_in_picture_empty_text);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.picture_in_picture_settings;
    }

    public int getMetricsCategory() {
        return 812;
    }

    /* Access modifiers changed, original: 0000 */
    public ArrayList<Pair<ApplicationInfo, Integer>> collectPipApps(int userId) {
        ArrayList<Pair<ApplicationInfo, Integer>> pipApps = new ArrayList();
        ArrayList<Integer> userIds = new ArrayList();
        for (UserInfo user : this.mUserManager.getProfiles(userId)) {
            userIds.add(Integer.valueOf(user.id));
        }
        Iterator it = userIds.iterator();
        while (it.hasNext()) {
            int id = ((Integer) it.next()).intValue();
            for (PackageInfo packageInfo : this.mPackageManager.getInstalledPackagesAsUser(1, id)) {
                if (checkPackageHasPictureInPictureActivities(packageInfo.packageName, packageInfo.activities)) {
                    pipApps.add(new Pair(packageInfo.applicationInfo, Integer.valueOf(id)));
                }
            }
        }
        return pipApps;
    }
}
