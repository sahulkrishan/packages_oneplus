package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class ShadowOverlayContainer extends FrameLayout {
    public static final int SHADOW_DYNAMIC = 3;
    public static final int SHADOW_NONE = 1;
    public static final int SHADOW_STATIC = 2;
    private static final Rect sTempRect = new Rect();
    private float mFocusedZ;
    private boolean mInitialized;
    int mOverlayColor;
    private Paint mOverlayPaint;
    private int mRoundedCornerRadius;
    private boolean mRoundedCorners;
    private Object mShadowImpl;
    private int mShadowType;
    private float mUnfocusedZ;
    private View mWrappedView;

    public ShadowOverlayContainer(Context context) {
        this(context, null, 0);
    }

    public ShadowOverlayContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowOverlayContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mShadowType = 1;
        useStaticShadow();
        useDynamicShadow();
    }

    ShadowOverlayContainer(Context context, int shadowType, boolean hasColorDimOverlay, float unfocusedZ, float focusedZ, int roundedCornerRadius) {
        super(context);
        this.mShadowType = 1;
        this.mUnfocusedZ = unfocusedZ;
        this.mFocusedZ = focusedZ;
        initialize(shadowType, hasColorDimOverlay, roundedCornerRadius);
    }

    public static boolean supportsShadow() {
        return StaticShadowHelper.supportsShadow();
    }

    public static boolean supportsDynamicShadow() {
        return ShadowHelper.supportsDynamicShadow();
    }

    public static void prepareParentForShadow(ViewGroup parent) {
        StaticShadowHelper.prepareParent(parent);
    }

    public void useDynamicShadow() {
        useDynamicShadow(getResources().getDimension(R.dimen.lb_material_shadow_normal_z), getResources().getDimension(R.dimen.lb_material_shadow_focused_z));
    }

    public void useDynamicShadow(float unfocusedZ, float focusedZ) {
        if (this.mInitialized) {
            throw new IllegalStateException("Already initialized");
        } else if (supportsDynamicShadow()) {
            this.mShadowType = 3;
            this.mUnfocusedZ = unfocusedZ;
            this.mFocusedZ = focusedZ;
        }
    }

    public void useStaticShadow() {
        if (this.mInitialized) {
            throw new IllegalStateException("Already initialized");
        } else if (supportsShadow()) {
            this.mShadowType = 2;
        }
    }

    public int getShadowType() {
        return this.mShadowType;
    }

    @Deprecated
    public void initialize(boolean hasShadow, boolean hasColorDimOverlay) {
        initialize(hasShadow, hasColorDimOverlay, true);
    }

    @Deprecated
    public void initialize(boolean hasShadow, boolean hasColorDimOverlay, boolean roundedCorners) {
        int shadowType;
        if (hasShadow) {
            shadowType = this.mShadowType;
        } else {
            shadowType = 1;
        }
        initialize(shadowType, hasColorDimOverlay, roundedCorners ? getContext().getResources().getDimensionPixelSize(R.dimen.lb_rounded_rect_corner_radius) : 0);
    }

    /* Access modifiers changed, original: 0000 */
    public void initialize(int shadowType, boolean hasColorDimOverlay, int roundedCornerRadius) {
        if (this.mInitialized) {
            throw new IllegalStateException();
        }
        this.mInitialized = true;
        this.mRoundedCornerRadius = roundedCornerRadius;
        this.mRoundedCorners = roundedCornerRadius > 0;
        this.mShadowType = shadowType;
        switch (this.mShadowType) {
            case 2:
                this.mShadowImpl = StaticShadowHelper.addStaticShadow(this);
                break;
            case 3:
                this.mShadowImpl = ShadowHelper.addDynamicShadow(this, this.mUnfocusedZ, this.mFocusedZ, this.mRoundedCornerRadius);
                break;
        }
        if (hasColorDimOverlay) {
            setWillNotDraw(false);
            this.mOverlayColor = 0;
            this.mOverlayPaint = new Paint();
            this.mOverlayPaint.setColor(this.mOverlayColor);
            this.mOverlayPaint.setStyle(Style.FILL);
            return;
        }
        setWillNotDraw(true);
        this.mOverlayPaint = null;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mOverlayPaint != null && this.mOverlayColor != 0) {
            canvas.drawRect((float) this.mWrappedView.getLeft(), (float) this.mWrappedView.getTop(), (float) this.mWrappedView.getRight(), (float) this.mWrappedView.getBottom(), this.mOverlayPaint);
        }
    }

    public void setShadowFocusLevel(float level) {
        if (this.mShadowImpl != null) {
            ShadowOverlayHelper.setShadowFocusLevel(this.mShadowImpl, this.mShadowType, level);
        }
    }

    public void setOverlayColor(@ColorInt int overlayColor) {
        if (this.mOverlayPaint != null && overlayColor != this.mOverlayColor) {
            this.mOverlayColor = overlayColor;
            this.mOverlayPaint.setColor(overlayColor);
            invalidate();
        }
    }

    public void wrap(View view) {
        if (this.mInitialized && this.mWrappedView == null) {
            LayoutParams lp = view.getLayoutParams();
            if (lp != null) {
                LayoutParams wrapped_lp = new FrameLayout.LayoutParams(lp.width, lp.height);
                int i = -2;
                lp.width = lp.width == -1 ? -1 : -2;
                if (lp.height == -1) {
                    i = -1;
                }
                lp.height = i;
                setLayoutParams(lp);
                addView(view, wrapped_lp);
            } else {
                addView(view);
            }
            if (this.mRoundedCorners && this.mShadowType != 3) {
                RoundedRectHelper.setClipToRoundedOutline(this, true);
            }
            this.mWrappedView = view;
            return;
        }
        throw new IllegalStateException();
    }

    public View getWrappedView() {
        return this.mWrappedView;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && this.mWrappedView != null) {
            sTempRect.left = (int) this.mWrappedView.getPivotX();
            sTempRect.top = (int) this.mWrappedView.getPivotY();
            offsetDescendantRectToMyCoords(this.mWrappedView, sTempRect);
            setPivotX((float) sTempRect.left);
            setPivotY((float) sTempRect.top);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
