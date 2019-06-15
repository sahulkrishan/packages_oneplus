package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.setupwizardlib.R;

public class StatusBarBackgroundLayout extends FrameLayout {
    private Object mLastInsets;
    private Drawable mStatusBarBackground;

    public StatusBarBackgroundLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public StatusBarBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @TargetApi(11)
    public StatusBarBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwStatusBarBackgroundLayout, defStyleAttr, 0);
        setStatusBarBackground(a.getDrawable(R.styleable.SuwStatusBarBackgroundLayout_suwStatusBarBackground));
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (VERSION.SDK_INT >= 21 && this.mLastInsets == null) {
            requestApplyInsets();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (VERSION.SDK_INT >= 21 && this.mLastInsets != null) {
            int insetTop = ((WindowInsets) this.mLastInsets).getSystemWindowInsetTop();
            if (insetTop > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), insetTop);
                this.mStatusBarBackground.draw(canvas);
            }
        }
    }

    public void setStatusBarBackground(Drawable background) {
        this.mStatusBarBackground = background;
        if (VERSION.SDK_INT >= 21) {
            boolean z = false;
            setWillNotDraw(background == null);
            if (background != null) {
                z = true;
            }
            setFitsSystemWindows(z);
            invalidate();
        }
    }

    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mLastInsets = insets;
        return super.onApplyWindowInsets(insets);
    }
}
