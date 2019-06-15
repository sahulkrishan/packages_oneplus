package com.airbnb.lottie.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieImageAsset;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ImageAssetManager {
    private final Map<String, Bitmap> bitmaps = new HashMap();
    private final Context context;
    @Nullable
    private ImageAssetDelegate delegate;
    private final Map<String, LottieImageAsset> imageAssets;
    private String imagesFolder;

    public ImageAssetManager(Callback callback, String imagesFolder, ImageAssetDelegate delegate, Map<String, LottieImageAsset> imageAssets) {
        this.imagesFolder = imagesFolder;
        if (!(TextUtils.isEmpty(imagesFolder) || this.imagesFolder.charAt(this.imagesFolder.length() - 1) == '/')) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.imagesFolder);
            stringBuilder.append('/');
            this.imagesFolder = stringBuilder.toString();
        }
        if (callback instanceof View) {
            this.context = ((View) callback).getContext();
            this.imageAssets = imageAssets;
            setDelegate(delegate);
            return;
        }
        Log.w(L.TAG, "LottieDrawable must be inside of a view for images to work.");
        this.imageAssets = new HashMap();
        this.context = null;
    }

    public void setDelegate(@Nullable ImageAssetDelegate assetDelegate) {
        this.delegate = assetDelegate;
    }

    @Nullable
    public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
        return (Bitmap) this.bitmaps.put(id, bitmap);
    }

    @Nullable
    public Bitmap bitmapForId(String id) {
        Bitmap bitmap = (Bitmap) this.bitmaps.get(id);
        if (bitmap == null) {
            LottieImageAsset imageAsset = (LottieImageAsset) this.imageAssets.get(id);
            if (imageAsset == null) {
                return null;
            }
            if (this.delegate != null) {
                bitmap = this.delegate.fetchBitmap(imageAsset);
                if (bitmap != null) {
                    this.bitmaps.put(id, bitmap);
                }
                return bitmap;
            }
            try {
                if (TextUtils.isEmpty(this.imagesFolder)) {
                    throw new IllegalStateException("You must set an images folder before loading an image. Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
                }
                InputStream is = this.context.getAssets();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(this.imagesFolder);
                stringBuilder.append(imageAsset.getFileName());
                is = is.open(stringBuilder.toString());
                Options opts = new Options();
                opts.inScaled = true;
                opts.inDensity = 160;
                bitmap = BitmapFactory.decodeStream(is, null, opts);
                this.bitmaps.put(id, bitmap);
            } catch (IOException e) {
                Log.w(L.TAG, "Unable to open asset.", e);
                return null;
            }
        }
        return bitmap;
    }

    public void recycleBitmaps() {
        Iterator<Entry<String, Bitmap>> it = this.bitmaps.entrySet().iterator();
        while (it.hasNext()) {
            ((Bitmap) ((Entry) it.next()).getValue()).recycle();
            it.remove();
        }
    }

    public boolean hasSameContext(Context context) {
        return (context == null && this.context == null) || (context != null && this.context.equals(context));
    }
}
