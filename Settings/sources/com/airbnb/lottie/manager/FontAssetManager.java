package com.airbnb.lottie.manager;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import com.airbnb.lottie.FontAssetDelegate;
import com.airbnb.lottie.L;
import com.airbnb.lottie.model.MutablePair;
import java.util.HashMap;
import java.util.Map;

public class FontAssetManager {
    private final AssetManager assetManager;
    private String defaultFontFileExtension = ".ttf";
    @Nullable
    private FontAssetDelegate delegate;
    private final Map<String, Typeface> fontFamilies = new HashMap();
    private final Map<MutablePair<String>, Typeface> fontMap = new HashMap();
    private final MutablePair<String> tempPair = new MutablePair();

    public FontAssetManager(Callback callback, @Nullable FontAssetDelegate delegate) {
        this.delegate = delegate;
        if (callback instanceof View) {
            this.assetManager = ((View) callback).getContext().getAssets();
            return;
        }
        Log.w(L.TAG, "LottieDrawable must be inside of a view for images to work.");
        this.assetManager = null;
    }

    public void setDelegate(@Nullable FontAssetDelegate assetDelegate) {
        this.delegate = assetDelegate;
    }

    public void setDefaultFontFileExtension(String defaultFontFileExtension) {
        this.defaultFontFileExtension = defaultFontFileExtension;
    }

    public Typeface getTypeface(String fontFamily, String style) {
        this.tempPair.set(fontFamily, style);
        Typeface typeface = (Typeface) this.fontMap.get(this.tempPair);
        if (typeface != null) {
            return typeface;
        }
        typeface = typefaceForStyle(getFontFamily(fontFamily), style);
        this.fontMap.put(this.tempPair, typeface);
        return typeface;
    }

    private Typeface getFontFamily(String fontFamily) {
        Typeface defaultTypeface = (Typeface) this.fontFamilies.get(fontFamily);
        if (defaultTypeface != null) {
            return defaultTypeface;
        }
        Typeface typeface = null;
        if (this.delegate != null) {
            typeface = this.delegate.fetchFont(fontFamily);
        }
        if (this.delegate != null && typeface == null) {
            String path = this.delegate.getFontPath(fontFamily);
            if (path != null) {
                typeface = Typeface.createFromAsset(this.assetManager, path);
            }
        }
        if (typeface == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("fonts/");
            stringBuilder.append(fontFamily);
            stringBuilder.append(this.defaultFontFileExtension);
            typeface = Typeface.createFromAsset(this.assetManager, stringBuilder.toString());
        }
        this.fontFamilies.put(fontFamily, typeface);
        return typeface;
    }

    private Typeface typefaceForStyle(Typeface typeface, String style) {
        int styleInt = 0;
        boolean containsItalic = style.contains("Italic");
        boolean containsBold = style.contains("Bold");
        if (containsItalic && containsBold) {
            styleInt = 3;
        } else if (containsItalic) {
            styleInt = 2;
        } else if (containsBold) {
            styleInt = 1;
        }
        if (typeface.getStyle() == styleInt) {
            return typeface;
        }
        return Typeface.create(typeface, styleInt);
    }
}
