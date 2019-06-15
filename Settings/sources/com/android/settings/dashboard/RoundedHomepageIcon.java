package com.android.settings.dashboard;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;

public class RoundedHomepageIcon extends LayerDrawable {
    @VisibleForTesting(otherwise = 5)
    int mBackgroundColor = -1;

    public RoundedHomepageIcon(Context context, Drawable foreground) {
        super(new Drawable[]{context.getDrawable(R.drawable.ic_homepage_generic_background), foreground});
        int insetPx = context.getResources().getDimensionPixelSize(R.dimen.dashboard_tile_foreground_image_inset);
        setLayerInset(1, insetPx, insetPx, insetPx, insetPx);
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        getDrawable(0).setColorFilter(color, Mode.SRC_ATOP);
    }
}
