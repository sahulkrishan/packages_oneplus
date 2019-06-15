package com.android.setupwizardlib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.view.View;

public class DrawableLayoutDirectionHelper {
    @SuppressLint({"InlinedApi"})
    public static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, View view) {
        boolean z = true;
        if (VERSION.SDK_INT < 17 || view.getLayoutDirection() != 1) {
            z = false;
        }
        return createRelativeInsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom, z);
    }

    @SuppressLint({"InlinedApi"})
    public static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, Context context) {
        boolean isRtl = false;
        if (VERSION.SDK_INT >= 17) {
            boolean z = true;
            if (context.getResources().getConfiguration().getLayoutDirection() != 1) {
                z = false;
            }
            isRtl = z;
        }
        return createRelativeInsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom, isRtl);
    }

    @SuppressLint({"InlinedApi"})
    public static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, int layoutDirection) {
        boolean z = true;
        if (layoutDirection != 1) {
            z = false;
        }
        return createRelativeInsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom, z);
    }

    private static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, boolean isRtl) {
        if (isRtl) {
            return new InsetDrawable(drawable, insetEnd, insetTop, insetStart, insetBottom);
        }
        return new InsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom);
    }
}
