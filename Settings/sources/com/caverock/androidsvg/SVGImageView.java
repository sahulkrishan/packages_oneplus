package com.caverock.androidsvg;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class SVGImageView extends ImageView {
    private static Method setLayerTypeMethod = null;

    public SVGImageView(Context context) {
        super(context);
        try {
            setLayerTypeMethod = View.class.getMethod("setLayerType", new Class[]{Integer.TYPE, Paint.class});
        } catch (NoSuchMethodException e) {
        }
    }

    public SVGImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        try {
            setLayerTypeMethod = View.class.getMethod("setLayerType", new Class[]{Integer.TYPE, Paint.class});
        } catch (NoSuchMethodException e) {
        }
        init(attrs, 0);
    }

    public SVGImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        try {
            setLayerTypeMethod = View.class.getMethod("setLayerType", new Class[]{Integer.TYPE, Paint.class});
        } catch (NoSuchMethodException e) {
        }
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
        try {
            int resourceId = a.getResourceId(0, -1);
            if (resourceId != -1) {
                setImageResource(resourceId);
                return;
            }
            String url = a.getString(0);
            if (internalSetImageURI(Uri.parse(url))) {
                a.recycle();
                return;
            }
            setImageAsset(url);
            a.recycle();
        } finally {
            a.recycle();
        }
    }

    public void setImageResource(int resourceId) {
        try {
            SVG svg = SVG.getFromResource(getContext(), resourceId);
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(svg.renderToPicture()));
        } catch (SVGParseException e) {
            StringBuilder stringBuilder = new StringBuilder("Unable to find resource: ");
            stringBuilder.append(resourceId);
            Log.w("SVGImageView", stringBuilder.toString(), e);
        }
    }

    public void setImageURI(Uri uri) {
        internalSetImageURI(uri);
    }

    public void setImageAsset(String filename) {
        try {
            SVG svg = SVG.getFromAsset(getContext().getAssets(), filename);
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(svg.renderToPicture()));
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder("Unable to find asset file: ");
            stringBuilder.append(filename);
            Log.w("SVGImageView", stringBuilder.toString(), e);
        }
    }

    private boolean internalSetImageURI(Uri uri) {
        InputStream is = null;
        try {
            is = getContext().getContentResolver().openInputStream(uri);
            SVG svg = SVG.getFromInputStream(is);
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(svg.renderToPicture()));
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            return true;
        } catch (Exception e2) {
            StringBuilder stringBuilder = new StringBuilder("Unable to open content: ");
            stringBuilder.append(uri);
            Log.w("ImageView", stringBuilder.toString(), e2);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
        }
    }

    private void setSoftwareLayerType() {
        if (setLayerTypeMethod != null) {
            try {
                setLayerTypeMethod.invoke(this, new Object[]{Integer.valueOf(1), null});
            } catch (Exception e) {
                Log.w("SVGImageView", "Unexpected failure calling setLayerType", e);
            }
        }
    }
}
