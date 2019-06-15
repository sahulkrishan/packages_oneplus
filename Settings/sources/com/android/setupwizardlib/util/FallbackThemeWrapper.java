package com.android.setupwizardlib.util;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.support.annotation.StyleRes;
import android.view.ContextThemeWrapper;

public class FallbackThemeWrapper extends ContextThemeWrapper {
    public FallbackThemeWrapper(Context base, @StyleRes int themeResId) {
        super(base, themeResId);
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resId, boolean first) {
        theme.applyStyle(resId, false);
    }
}
