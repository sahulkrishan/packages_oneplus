package com.android.settings.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;

public class ColorModePreferenceFragment extends RadioButtonPickerFragment implements Callback {
    @VisibleForTesting
    static final String KEY_COLOR_MODE_AUTOMATIC = "color_mode_automatic";
    @VisibleForTesting
    static final String KEY_COLOR_MODE_BOOSTED = "color_mode_boosted";
    @VisibleForTesting
    static final String KEY_COLOR_MODE_NATURAL = "color_mode_natural";
    @VisibleForTesting
    static final String KEY_COLOR_MODE_SATURATED = "color_mode_saturated";
    private ColorDisplayController mController;

    @VisibleForTesting
    static class ColorModeCandidateInfo extends CandidateInfo {
        private final String mKey;
        private final CharSequence mLabel;

        ColorModeCandidateInfo(CharSequence label, String key, boolean enabled) {
            super(enabled);
            this.mLabel = label;
            this.mKey = key;
        }

        public CharSequence loadLabel() {
            return this.mLabel;
        }

        public Drawable loadIcon() {
            return null;
        }

        public String getKey() {
            return this.mKey;
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mController = new ColorDisplayController(context);
        this.mController.setListener(this);
    }

    public void onDetach() {
        super.onDetach();
        if (this.mController != null) {
            this.mController.setListener(null);
            this.mController = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.color_mode_settings;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void configureAndInstallPreview(LayoutPreference preview, PreferenceScreen screen) {
        preview.setSelectable(false);
        screen.addPreference(preview);
    }

    /* Access modifiers changed, original: protected */
    public void addStaticPreferences(PreferenceScreen screen) {
        configureAndInstallPreview(new LayoutPreference(screen.getContext(), (int) R.layout.color_mode_preview), screen);
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        Context c = getContext();
        int[] availableColorModes = c.getResources().getIntArray(17235987);
        List<ColorModeCandidateInfo> candidates = new ArrayList();
        if (availableColorModes != null) {
            for (int colorMode : availableColorModes) {
                if (colorMode == 0) {
                    candidates.add(new ColorModeCandidateInfo(c.getText(R.string.color_mode_option_natural), KEY_COLOR_MODE_NATURAL, true));
                } else if (colorMode == 1) {
                    candidates.add(new ColorModeCandidateInfo(c.getText(R.string.color_mode_option_boosted), KEY_COLOR_MODE_BOOSTED, true));
                } else if (colorMode == 2) {
                    candidates.add(new ColorModeCandidateInfo(c.getText(R.string.color_mode_option_saturated), KEY_COLOR_MODE_SATURATED, true));
                } else if (colorMode == 3) {
                    candidates.add(new ColorModeCandidateInfo(c.getText(R.string.color_mode_option_automatic), KEY_COLOR_MODE_AUTOMATIC, true));
                }
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        int colorMode = this.mController.getColorMode();
        if (colorMode == 3) {
            return KEY_COLOR_MODE_AUTOMATIC;
        }
        if (colorMode == 2) {
            return KEY_COLOR_MODE_SATURATED;
        }
        if (colorMode == 1) {
            return KEY_COLOR_MODE_BOOSTED;
        }
        return KEY_COLOR_MODE_NATURAL;
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a  */
    public boolean setDefaultKey(java.lang.String r7) {
        /*
        r6 = this;
        r0 = r7.hashCode();
        r1 = -2029194174; // 0xffffffff870cf442 float:-1.060421E-34 double:NaN;
        r2 = 3;
        r3 = 0;
        r4 = 2;
        r5 = 1;
        if (r0 == r1) goto L_0x003b;
    L_0x000d:
        r1 = -739564821; // 0xffffffffd3eb22eb float:-2.01980628E12 double:NaN;
        if (r0 == r1) goto L_0x0031;
    L_0x0012:
        r1 = -365217559; // 0xffffffffea3b38e9 float:-5.658447E25 double:NaN;
        if (r0 == r1) goto L_0x0027;
    L_0x0017:
        r1 = 765917269; // 0x2da6f855 float:1.8982297E-11 double:3.7841341E-315;
        if (r0 == r1) goto L_0x001d;
    L_0x001c:
        goto L_0x0045;
    L_0x001d:
        r0 = "color_mode_saturated";
        r0 = r7.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0025:
        r0 = r4;
        goto L_0x0046;
    L_0x0027:
        r0 = "color_mode_natural";
        r0 = r7.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x002f:
        r0 = r3;
        goto L_0x0046;
    L_0x0031:
        r0 = "color_mode_automatic";
        r0 = r7.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0039:
        r0 = r2;
        goto L_0x0046;
    L_0x003b:
        r0 = "color_mode_boosted";
        r0 = r7.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0043:
        r0 = r5;
        goto L_0x0046;
    L_0x0045:
        r0 = -1;
    L_0x0046:
        switch(r0) {
            case 0: goto L_0x005c;
            case 1: goto L_0x0056;
            case 2: goto L_0x0050;
            case 3: goto L_0x004a;
            default: goto L_0x0049;
        };
    L_0x0049:
        goto L_0x0062;
    L_0x004a:
        r0 = r6.mController;
        r0.setColorMode(r2);
        goto L_0x0062;
    L_0x0050:
        r0 = r6.mController;
        r0.setColorMode(r4);
        goto L_0x0062;
    L_0x0056:
        r0 = r6.mController;
        r0.setColorMode(r5);
        goto L_0x0062;
    L_0x005c:
        r0 = r6.mController;
        r0.setColorMode(r3);
    L_0x0062:
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.display.ColorModePreferenceFragment.setDefaultKey(java.lang.String):boolean");
    }

    public int getMetricsCategory() {
        return 1143;
    }

    public void onAccessibilityTransformChanged(boolean state) {
        if (state) {
            getActivity().onBackPressed();
        }
    }
}
