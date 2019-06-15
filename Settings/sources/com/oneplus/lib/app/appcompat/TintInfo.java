package com.oneplus.lib.app.appcompat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;

public class TintInfo {
    public boolean mHasTintList;
    public boolean mHasTintMode;
    public ColorStateList mTintList;
    public Mode mTintMode;

    /* Access modifiers changed, original: 0000 */
    public void clear() {
        this.mTintList = null;
        this.mHasTintList = false;
        this.mTintMode = null;
        this.mHasTintMode = false;
    }
}
