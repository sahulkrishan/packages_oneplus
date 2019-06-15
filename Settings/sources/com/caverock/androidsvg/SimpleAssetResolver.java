package com.caverock.androidsvg;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SimpleAssetResolver extends SVGExternalFileResolver {
    private static final String TAG = SimpleAssetResolver.class.getSimpleName();
    private static final Set<String> supportedFormats = new HashSet(8);
    private AssetManager assetManager;

    public SimpleAssetResolver(AssetManager assetManager) {
        supportedFormats.add("image/svg+xml");
        supportedFormats.add("image/jpeg");
        supportedFormats.add("image/png");
        supportedFormats.add("image/pjpeg");
        supportedFormats.add("image/gif");
        supportedFormats.add("image/bmp");
        supportedFormats.add("image/x-windows-bmp");
        if (VERSION.SDK_INT >= 14) {
            supportedFormats.add("image/webp");
        }
        this.assetManager = assetManager;
    }

    public Typeface resolveFont(String fontFamily, int fontWeight, String fontStyle) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder("resolveFont(");
        stringBuilder.append(fontFamily);
        stringBuilder.append(",");
        stringBuilder.append(fontWeight);
        stringBuilder.append(",");
        stringBuilder.append(fontStyle);
        stringBuilder.append(")");
        Log.i(str, stringBuilder.toString());
        AssetManager assetManager;
        try {
            assetManager = this.assetManager;
            stringBuilder = new StringBuilder(String.valueOf(fontFamily));
            stringBuilder.append(".ttf");
            return Typeface.createFromAsset(assetManager, stringBuilder.toString());
        } catch (Exception e) {
            try {
                assetManager = this.assetManager;
                stringBuilder = new StringBuilder(String.valueOf(fontFamily));
                stringBuilder.append(".otf");
                return Typeface.createFromAsset(assetManager, stringBuilder.toString());
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public Bitmap resolveImage(String filename) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder("resolveImage(");
        stringBuilder.append(filename);
        stringBuilder.append(")");
        Log.i(str, stringBuilder.toString());
        try {
            return BitmapFactory.decodeStream(this.assetManager.open(filename));
        } catch (IOException e) {
            return null;
        }
    }

    public boolean isFormatSupported(String mimeType) {
        return supportedFormats.contains(mimeType);
    }
}
