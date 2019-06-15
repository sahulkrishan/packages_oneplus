package com.android.setupwizardlib.items;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.InflateException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

public abstract class ReflectionInflater<T> extends SimpleInflater<T> {
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<?>> sConstructorMap = new HashMap();
    @NonNull
    private final Context mContext;
    @Nullable
    private String mDefaultPackage;
    private final Object[] mTempConstructorArgs = new Object[2];

    protected ReflectionInflater(@NonNull Context context) {
        super(context.getResources());
        this.mContext = context;
    }

    @NonNull
    public Context getContext() {
        return this.mContext;
    }

    @NonNull
    public final T createItem(String tagName, String prefix, AttributeSet attrs) {
        String qualifiedName = tagName;
        if (prefix != null && qualifiedName.indexOf(46) == -1) {
            qualifiedName = prefix.concat(qualifiedName);
        }
        Constructor<? extends T> constructor = (Constructor) sConstructorMap.get(qualifiedName);
        if (constructor == null) {
            try {
                constructor = this.mContext.getClassLoader().loadClass(qualifiedName).getConstructor(CONSTRUCTOR_SIGNATURE);
                constructor.setAccessible(true);
                sConstructorMap.put(tagName, constructor);
            } catch (Exception e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(attrs.getPositionDescription());
                stringBuilder.append(": Error inflating class ");
                stringBuilder.append(qualifiedName);
                throw new InflateException(stringBuilder.toString(), e);
            }
        }
        this.mTempConstructorArgs[0] = this.mContext;
        this.mTempConstructorArgs[1] = attrs;
        T item = constructor.newInstance(this.mTempConstructorArgs);
        this.mTempConstructorArgs[0] = null;
        this.mTempConstructorArgs[1] = null;
        return item;
    }

    /* Access modifiers changed, original: protected */
    public T onCreateItem(String tagName, AttributeSet attrs) {
        return createItem(tagName, this.mDefaultPackage, attrs);
    }

    public void setDefaultPackage(@Nullable String defaultPackage) {
        this.mDefaultPackage = defaultPackage;
    }

    @Nullable
    public String getDefaultPackage() {
        return this.mDefaultPackage;
    }
}
