package com.android.settingslib.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;

public class IconCache {
    private final Context mContext;
    @VisibleForTesting
    final ArrayMap<Icon, Drawable> mMap = new ArrayMap();

    public IconCache(Context context) {
        this.mContext = context;
    }

    public Drawable getIcon(Icon icon) {
        if (icon == null) {
            return null;
        }
        Drawable drawable = (Drawable) this.mMap.get(icon);
        if (drawable == null) {
            drawable = icon.loadDrawable(this.mContext);
            updateIcon(icon, drawable);
        }
        return drawable;
    }

    public void updateIcon(Icon icon, Drawable drawable) {
        this.mMap.put(icon, drawable);
    }
}
