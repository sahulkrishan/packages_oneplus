package com.android.settings.development;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import java.util.List;

public class EmulateDisplayCutoutPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String KEY = "display_cutout_emulation";
    private final boolean mAvailable;
    private final OverlayManagerWrapper mOverlayManager;
    private PackageManager mPackageManager;
    private ListPreference mPreference;

    @VisibleForTesting
    EmulateDisplayCutoutPreferenceController(Context context, PackageManager packageManager, OverlayManagerWrapper overlayManager) {
        super(context);
        this.mOverlayManager = overlayManager;
        this.mPackageManager = packageManager;
        boolean z = overlayManager != null && getOverlayInfos().length > 0;
        this.mAvailable = z;
    }

    public EmulateDisplayCutoutPreferenceController(Context context) {
        this(context, context.getPackageManager(), new OverlayManagerWrapper());
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        setPreference((ListPreference) screen.findPreference(getPreferenceKey()));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreference(ListPreference preference) {
        this.mPreference = preference;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return setEmulationOverlay((String) newValue);
    }

    private boolean setEmulationOverlay(String packageName) {
        String currentPackageName = null;
        for (OverlayInfo o : getOverlayInfos()) {
            if (o.isEnabled()) {
                currentPackageName = o.packageName;
            }
        }
        if ((TextUtils.isEmpty(packageName) && TextUtils.isEmpty(currentPackageName)) || TextUtils.equals(packageName, currentPackageName)) {
            return true;
        }
        boolean result;
        if (TextUtils.isEmpty(packageName)) {
            result = this.mOverlayManager.setEnabled(currentPackageName, false, 0);
        } else {
            result = this.mOverlayManager.setEnabledExclusiveInCategory(packageName, 0);
        }
        updateState(this.mPreference);
        return result;
    }

    public void updateState(Preference preference) {
        OverlayInfo[] overlays = getOverlayInfos();
        int i = 1;
        CharSequence[] pkgs = new CharSequence[(overlays.length + 1)];
        CharSequence[] labels = new CharSequence[pkgs.length];
        pkgs[0] = "";
        labels[0] = this.mContext.getString(R.string.display_cutout_emulation_none);
        int current = 0;
        for (int i2 = 0; i2 < overlays.length; i2++) {
            OverlayInfo o = overlays[i2];
            pkgs[i2 + 1] = o.packageName;
            if (o.isEnabled()) {
                current = i2 + 1;
            }
        }
        while (i < pkgs.length) {
            try {
                labels[i] = this.mPackageManager.getApplicationInfo(pkgs[i].toString(), 0).loadLabel(this.mPackageManager);
            } catch (NameNotFoundException e) {
                labels[i] = pkgs[i];
            }
            i++;
        }
        this.mPreference.setEntries(labels);
        this.mPreference.setEntryValues(pkgs);
        this.mPreference.setValueIndex(current);
        this.mPreference.setSummary(labels[current]);
    }

    private OverlayInfo[] getOverlayInfos() {
        List<OverlayInfo> overlayInfos = this.mOverlayManager.getOverlayInfosForTarget("android", 0);
        for (int i = overlayInfos.size() - 1; i >= 0; i--) {
            if (!"com.android.internal.display_cutout_emulation".equals(((OverlayInfo) overlayInfos.get(i)).category)) {
                overlayInfos.remove(i);
            }
        }
        return (OverlayInfo[]) overlayInfos.toArray(new OverlayInfo[overlayInfos.size()]);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        setEmulationOverlay("");
        updateState(this.mPreference);
    }
}
