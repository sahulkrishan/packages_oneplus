package com.android.settingslib.animation;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settingslib.animation.AppearAnimationUtils.RowTranslationScaler;

public class DisappearAnimationUtils extends AppearAnimationUtils {
    private static final RowTranslationScaler ROW_TRANSLATION_SCALER = new RowTranslationScaler() {
        public float getRowTranslationScale(int row, int numRows) {
            return (float) (Math.pow((double) (numRows - row), 2.0d) / ((double) numRows));
        }
    };

    public DisappearAnimationUtils(Context ctx) {
        this(ctx, 220, 1.0f, 1.0f, AnimationUtils.loadInterpolator(ctx, AndroidResources.FAST_OUT_LINEAR_IN));
    }

    public DisappearAnimationUtils(Context ctx, long duration, float translationScaleFactor, float delayScaleFactor, Interpolator interpolator) {
        this(ctx, duration, translationScaleFactor, delayScaleFactor, interpolator, ROW_TRANSLATION_SCALER);
    }

    public DisappearAnimationUtils(Context ctx, long duration, float translationScaleFactor, float delayScaleFactor, Interpolator interpolator, RowTranslationScaler rowScaler) {
        super(ctx, duration, translationScaleFactor, delayScaleFactor, interpolator);
        this.mRowTranslationScaler = rowScaler;
        this.mAppearing = false;
    }

    /* Access modifiers changed, original: protected */
    public long calculateDelay(int row, int col) {
        return (long) ((((double) (row * 60)) + ((((double) col) * (Math.pow((double) row, 0.4d) + 0.4d)) * 10.0d)) * ((double) this.mDelayScale));
    }
}
