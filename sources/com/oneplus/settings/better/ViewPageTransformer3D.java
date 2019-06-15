package com.oneplus.settings.better;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class ViewPageTransformer3D implements PageTransformer {
    private static final float MAX_ROTATION = 20.0f;
    private static final float MAX_TRANSLATE = 20.0f;
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View page, float position) {
        float scale;
        if (position < -1.0f) {
            page.setTranslationX(20.0f);
            page.setScaleX(0.75f);
            page.setScaleY(0.75f);
            page.setRotationY(-20.0f);
        } else if (position <= 0.0f) {
            page.setTranslationX(-20.0f * position);
            scale = 0.75f + (0.25f * (1.0f + position));
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setRotationY(20.0f * position);
        } else if (position <= 1.0f) {
            page.setTranslationX(-20.0f * position);
            scale = 0.75f + (0.25f * (1.0f - position));
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setRotationY(20.0f * position);
        } else {
            page.setTranslationX(-20.0f);
            page.setScaleX(0.75f);
            page.setScaleY(0.75f);
            page.setRotationY(20.0f);
        }
    }
}
