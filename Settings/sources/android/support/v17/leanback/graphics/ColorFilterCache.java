package android.support.v17.leanback.graphics;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.util.SparseArray;

public final class ColorFilterCache {
    private static final SparseArray<ColorFilterCache> sColorToFiltersMap = new SparseArray();
    private final PorterDuffColorFilter[] mFilters = new PorterDuffColorFilter[256];

    public static ColorFilterCache getColorFilterCache(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        color = Color.rgb(r, g, b);
        ColorFilterCache filters = (ColorFilterCache) sColorToFiltersMap.get(color);
        if (filters != null) {
            return filters;
        }
        filters = new ColorFilterCache(r, g, b);
        sColorToFiltersMap.put(color, filters);
        return filters;
    }

    private ColorFilterCache(int r, int g, int b) {
        for (int i = 0; i <= 255; i++) {
            this.mFilters[i] = new PorterDuffColorFilter(Color.argb(i, r, g, b), Mode.SRC_ATOP);
        }
    }

    public ColorFilter getFilterForLevel(float level) {
        if (level < 0.0f || ((double) level) > 1.0d) {
            return null;
        }
        return this.mFilters[(int) (1132396544 * level)];
    }
}
