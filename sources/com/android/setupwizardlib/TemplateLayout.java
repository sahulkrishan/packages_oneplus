package com.android.setupwizardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Keep;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import com.android.setupwizardlib.template.Mixin;
import com.android.setupwizardlib.util.FallbackThemeWrapper;
import java.util.HashMap;
import java.util.Map;

public class TemplateLayout extends FrameLayout {
    private ViewGroup mContainer;
    private Map<Class<? extends Mixin>, Mixin> mMixins = new HashMap();
    private OnPreDrawListener mPreDrawListener;
    private float mXFraction;

    public TemplateLayout(Context context, int template, int containerId) {
        super(context);
        init(template, containerId, null, R.attr.suwLayoutTheme);
    }

    public TemplateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(0, 0, attrs, R.attr.suwLayoutTheme);
    }

    @TargetApi(11)
    public TemplateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(0, 0, attrs, defStyleAttr);
    }

    private void init(int template, int containerId, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SuwTemplateLayout, defStyleAttr, 0);
        if (template == 0) {
            template = a.getResourceId(R.styleable.SuwTemplateLayout_android_layout, 0);
        }
        if (containerId == 0) {
            containerId = a.getResourceId(R.styleable.SuwTemplateLayout_suwContainer, 0);
        }
        inflateTemplate(template, containerId);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public <M extends Mixin> void registerMixin(Class<M> cls, M mixin) {
        this.mMixins.put(cls, mixin);
    }

    public <T extends View> T findManagedViewById(int id) {
        return findViewById(id);
    }

    public <M extends Mixin> M getMixin(Class<M> cls) {
        return (Mixin) this.mMixins.get(cls);
    }

    public void addView(View child, int index, LayoutParams params) {
        this.mContainer.addView(child, index, params);
    }

    private void addViewInternal(View child) {
        super.addView(child, -1, generateDefaultLayoutParams());
    }

    private void inflateTemplate(int templateResource, int containerId) {
        addViewInternal(onInflateTemplate(LayoutInflater.from(getContext()), templateResource));
        this.mContainer = findContainer(containerId);
        if (this.mContainer != null) {
            onTemplateInflated();
            return;
        }
        throw new IllegalArgumentException("Container cannot be null in TemplateLayout");
    }

    /* Access modifiers changed, original: protected */
    public View onInflateTemplate(LayoutInflater inflater, @LayoutRes int template) {
        return inflateTemplate(inflater, 0, template);
    }

    /* Access modifiers changed, original: protected|final */
    public final View inflateTemplate(LayoutInflater inflater, @StyleRes int fallbackTheme, @LayoutRes int template) {
        if (template != 0) {
            if (fallbackTheme != 0) {
                inflater = LayoutInflater.from(new FallbackThemeWrapper(inflater.getContext(), fallbackTheme));
            }
            return inflater.inflate(template, this, false);
        }
        throw new IllegalArgumentException("android:layout not specified for TemplateLayout");
    }

    /* Access modifiers changed, original: protected */
    public ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = getContainerId();
        }
        return (ViewGroup) findViewById(containerId);
    }

    /* Access modifiers changed, original: protected */
    public void onTemplateInflated() {
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public int getContainerId() {
        return 0;
    }

    @Keep
    @TargetApi(11)
    public void setXFraction(float fraction) {
        this.mXFraction = fraction;
        int width = getWidth();
        if (width != 0) {
            setTranslationX(((float) width) * fraction);
        } else if (this.mPreDrawListener == null) {
            this.mPreDrawListener = new OnPreDrawListener() {
                public boolean onPreDraw() {
                    TemplateLayout.this.getViewTreeObserver().removeOnPreDrawListener(TemplateLayout.this.mPreDrawListener);
                    TemplateLayout.this.setXFraction(TemplateLayout.this.mXFraction);
                    return true;
                }
            };
            getViewTreeObserver().addOnPreDrawListener(this.mPreDrawListener);
        }
    }

    @Keep
    @TargetApi(11)
    public float getXFraction() {
        return this.mXFraction;
    }
}
