package com.oneplus.lib.app.appcompat;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import java.lang.ref.WeakReference;

public class TintResources extends ResourcesWrapper {
    private final WeakReference<Context> mContextRef;

    public TintResources(@NonNull Context context, @NonNull Resources res) {
        super(res);
        this.mContextRef = new WeakReference(context);
    }

    public Drawable getDrawable(int id) throws NotFoundException {
        Drawable d = super.getDrawable(id);
        this.mContextRef.get();
        return d;
    }
}
