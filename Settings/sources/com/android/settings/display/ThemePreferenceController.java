package com.android.settings.display;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_THEME = "theme";
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final OverlayManagerWrapper mOverlayService;
    private final PackageManager mPackageManager;

    public ThemePreferenceController(Context context) {
        this(context, ServiceManager.getService("overlay") != null ? new OverlayManagerWrapper() : null);
    }

    @VisibleForTesting
    ThemePreferenceController(Context context, OverlayManagerWrapper overlayManager) {
        super(context);
        this.mOverlayService = overlayManager;
        this.mPackageManager = context.getPackageManager();
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public String getPreferenceKey() {
        return "theme";
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if ("theme".equals(preference.getKey())) {
            this.mMetricsFeatureProvider.action(this.mContext, 816, new Pair[0]);
        }
        return false;
    }

    public void updateState(Preference preference) {
        ListPreference pref = (ListPreference) preference;
        CharSequence[] pkgs = getAvailableThemes();
        CharSequence[] labels = new CharSequence[pkgs.length];
        int i = 0;
        for (int i2 = 0; i2 < pkgs.length; i2++) {
            try {
                labels[i2] = this.mPackageManager.getApplicationInfo(pkgs[i2], 0).loadLabel(this.mPackageManager);
            } catch (NameNotFoundException e) {
                labels[i2] = pkgs[i2];
            }
        }
        pref.setEntries(labels);
        pref.setEntryValues(pkgs);
        String theme = getCurrentTheme();
        CharSequence themeLabel = null;
        while (i < pkgs.length) {
            if (TextUtils.equals(pkgs[i], theme)) {
                themeLabel = labels[i];
                break;
            }
            i++;
        }
        if (TextUtils.isEmpty(themeLabel)) {
            themeLabel = this.mContext.getString(R.string.default_theme);
        }
        pref.setSummary(themeLabel);
        pref.setValue(theme);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Objects.equals(newValue, getTheme())) {
            return true;
        }
        this.mOverlayService.setEnabledExclusiveInCategory((String) newValue, UserHandle.myUserId());
        return true;
    }

    private boolean isTheme(OverlayInfo oi) {
        boolean z = false;
        if (!OverlayInfo.CATEGORY_THEME.equals(oi.category)) {
            return false;
        }
        try {
            PackageInfo pi = this.mPackageManager.getPackageInfo(oi.packageName, 0);
            if (!(pi == null || pi.isStaticOverlayPackage())) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private String getTheme() {
        List<OverlayInfo> infos = this.mOverlayService.getOverlayInfosForTarget("android", UserHandle.myUserId());
        int i = 0;
        int size = infos.size();
        while (i < size) {
            if (((OverlayInfo) infos.get(i)).isEnabled() && isTheme((OverlayInfo) infos.get(i))) {
                return ((OverlayInfo) infos.get(i)).packageName;
            }
            i++;
        }
        return null;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (this.mOverlayService == null) {
            return false;
        }
        String[] themes = getAvailableThemes();
        if (themes != null && themes.length > 1) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getCurrentTheme() {
        return getTheme();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String[] getAvailableThemes() {
        List<OverlayInfo> infos = this.mOverlayService.getOverlayInfosForTarget("android", UserHandle.myUserId());
        List<String> pkgs = new ArrayList(infos.size());
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            if (isTheme((OverlayInfo) infos.get(i))) {
                pkgs.add(((OverlayInfo) infos.get(i)).packageName);
            }
        }
        return (String[]) pkgs.toArray(new String[pkgs.size()]);
    }
}
