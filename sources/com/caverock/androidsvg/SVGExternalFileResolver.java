package com.caverock.androidsvg;

import android.graphics.Bitmap;
import android.graphics.Typeface;

public abstract class SVGExternalFileResolver {
    public Typeface resolveFont(String fontFamily, int fontWeight, String fontStyle) {
        return null;
    }

    public Bitmap resolveImage(String filename) {
        return null;
    }

    public boolean isFormatSupported(String mimeType) {
        return false;
    }
}
