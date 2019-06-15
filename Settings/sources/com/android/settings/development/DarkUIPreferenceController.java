package com.android.settings.development;

import android.app.UiModeManager;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class DarkUIPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String DARK_UI_KEY = "dark_ui_mode";
    private final UiModeManager mUiModeManager;

    public DarkUIPreferenceController(Context context) {
        this(context, (UiModeManager) context.getSystemService(UiModeManager.class));
    }

    @VisibleForTesting
    DarkUIPreferenceController(Context context, UiModeManager uiModeManager) {
        super(context);
        this.mUiModeManager = uiModeManager;
    }

    public String getPreferenceKey() {
        return DARK_UI_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mUiModeManager.setNightMode(modeToInt((String) newValue));
        updateSummary(preference);
        return true;
    }

    public void updateState(Preference preference) {
        updateSummary(preference);
    }

    private void updateSummary(Preference preference) {
        int mode = this.mUiModeManager.getNightMode();
        ((ListPreference) preference).setValue(modeToString(mode));
        preference.setSummary(modeToDescription(mode));
    }

    private String modeToDescription(int mode) {
        String[] values = this.mContext.getResources().getStringArray(R.array.dark_ui_mode_entries);
        if (mode == 0) {
            return values[0];
        }
        if (mode != 2) {
            return values[2];
        }
        return values[1];
    }

    private String modeToString(int mode) {
        if (mode == 0) {
            return "auto";
        }
        if (mode != 2) {
            return "no";
        }
        return "yes";
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039 A:{RETURN} */
    private int modeToInt(java.lang.String r6) {
        /*
        r5 = this;
        r0 = r6.hashCode();
        r1 = 3521; // 0xdc1 float:4.934E-42 double:1.7396E-320;
        r2 = 2;
        r3 = 1;
        r4 = 0;
        if (r0 == r1) goto L_0x002a;
    L_0x000b:
        r1 = 119527; // 0x1d2e7 float:1.67493E-40 double:5.9054E-319;
        if (r0 == r1) goto L_0x0020;
    L_0x0010:
        r1 = 3005871; // 0x2dddaf float:4.212122E-39 double:1.4850976E-317;
        if (r0 == r1) goto L_0x0016;
    L_0x0015:
        goto L_0x0034;
    L_0x0016:
        r0 = "auto";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0034;
    L_0x001e:
        r0 = r4;
        goto L_0x0035;
    L_0x0020:
        r0 = "yes";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0034;
    L_0x0028:
        r0 = r3;
        goto L_0x0035;
    L_0x002a:
        r0 = "no";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0034;
    L_0x0032:
        r0 = r2;
        goto L_0x0035;
    L_0x0034:
        r0 = -1;
    L_0x0035:
        switch(r0) {
            case 0: goto L_0x003a;
            case 1: goto L_0x0039;
            default: goto L_0x0038;
        };
    L_0x0038:
        return r3;
    L_0x0039:
        return r2;
    L_0x003a:
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.development.DarkUIPreferenceController.modeToInt(java.lang.String):int");
    }
}
