package android.support.v17.leanback.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.BackgroundHelper;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import java.lang.ref.WeakReference;

public final class BackgroundManager {
    private static final int CHANGE_BG_DELAY_MS = 500;
    static final boolean DEBUG = false;
    private static final int FADE_DURATION = 500;
    private static final String FRAGMENT_TAG = BackgroundManager.class.getCanonicalName();
    static final int FULL_ALPHA = 255;
    static final String TAG = "BackgroundManager";
    private final Interpolator mAccelerateInterpolator;
    private final AnimatorListener mAnimationListener = new AnimatorListener() {
        final Runnable mRunnable = new Runnable() {
            public void run() {
                BackgroundManager.this.postChangeRunnable();
            }
        };

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (BackgroundManager.this.mLayerDrawable != null) {
                BackgroundManager.this.mLayerDrawable.clearDrawable(R.id.background_imageout, BackgroundManager.this.mContext);
            }
            BackgroundManager.this.mHandler.post(this.mRunnable);
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private final AnimatorUpdateListener mAnimationUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            int fadeInAlpha = ((Integer) animation.getAnimatedValue()).intValue();
            if (BackgroundManager.this.mImageInWrapperIndex != -1) {
                BackgroundManager.this.mLayerDrawable.setWrapperAlpha(BackgroundManager.this.mImageInWrapperIndex, fadeInAlpha);
            }
        }
    };
    final ValueAnimator mAnimator;
    private boolean mAttached;
    private boolean mAutoReleaseOnStop = true;
    int mBackgroundColor;
    Drawable mBackgroundDrawable;
    private View mBgView;
    ChangeBackgroundRunnable mChangeRunnable;
    private boolean mChangeRunnablePending;
    Activity mContext;
    private final Interpolator mDecelerateInterpolator;
    private BackgroundFragment mFragmentState;
    Handler mHandler;
    private int mHeightPx;
    int mImageInWrapperIndex;
    int mImageOutWrapperIndex;
    private long mLastSetTime;
    TranslucentLayerDrawable mLayerDrawable;
    private BackgroundContinuityService mService;
    private int mThemeDrawableResourceId;
    private int mWidthPx;

    private static class BackgroundContinuityService {
        private static final boolean DEBUG = false;
        private static final String TAG = "BackgroundContinuity";
        private static BackgroundContinuityService sService = new BackgroundContinuityService();
        private int mColor;
        private int mCount;
        private Drawable mDrawable;
        private int mLastThemeDrawableId;
        private WeakReference<ConstantState> mLastThemeDrawableState;

        private BackgroundContinuityService() {
            reset();
        }

        private void reset() {
            this.mColor = 0;
            this.mDrawable = null;
        }

        public static BackgroundContinuityService getInstance() {
            BackgroundContinuityService backgroundContinuityService = sService;
            int i = backgroundContinuityService.mCount;
            backgroundContinuityService.mCount = i + 1;
            int count = i;
            return sService;
        }

        public void unref() {
            if (this.mCount > 0) {
                int i = this.mCount - 1;
                this.mCount = i;
                if (i == 0) {
                    reset();
                    return;
                }
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Can't unref, count ");
            stringBuilder.append(this.mCount);
            throw new IllegalStateException(stringBuilder.toString());
        }

        public int getColor() {
            return this.mColor;
        }

        public Drawable getDrawable() {
            return this.mDrawable;
        }

        public void setColor(int color) {
            this.mColor = color;
            this.mDrawable = null;
        }

        public void setDrawable(Drawable drawable) {
            this.mDrawable = drawable;
        }

        public Drawable getThemeDrawable(Context context, int themeDrawableId) {
            Drawable drawable = null;
            if (this.mLastThemeDrawableState != null && this.mLastThemeDrawableId == themeDrawableId) {
                ConstantState drawableState = (ConstantState) this.mLastThemeDrawableState.get();
                if (drawableState != null) {
                    drawable = drawableState.newDrawable();
                }
            }
            if (drawable != null) {
                return drawable;
            }
            drawable = ContextCompat.getDrawable(context, themeDrawableId);
            this.mLastThemeDrawableState = new WeakReference(drawable.getConstantState());
            this.mLastThemeDrawableId = themeDrawableId;
            return drawable;
        }
    }

    static class BitmapDrawable extends Drawable {
        boolean mMutated;
        ConstantState mState;

        static final class ConstantState extends android.graphics.drawable.Drawable.ConstantState {
            final Bitmap mBitmap;
            final Matrix mMatrix;
            final Paint mPaint = new Paint();

            ConstantState(Bitmap bitmap, Matrix matrix) {
                this.mBitmap = bitmap;
                this.mMatrix = matrix != null ? matrix : new Matrix();
                this.mPaint.setFilterBitmap(true);
            }

            ConstantState(ConstantState copyFrom) {
                this.mBitmap = copyFrom.mBitmap;
                this.mMatrix = copyFrom.mMatrix != null ? new Matrix(copyFrom.mMatrix) : new Matrix();
                if (copyFrom.mPaint.getAlpha() != 255) {
                    this.mPaint.setAlpha(copyFrom.mPaint.getAlpha());
                }
                if (copyFrom.mPaint.getColorFilter() != null) {
                    this.mPaint.setColorFilter(copyFrom.mPaint.getColorFilter());
                }
                this.mPaint.setFilterBitmap(true);
            }

            public Drawable newDrawable() {
                return new BitmapDrawable(this);
            }

            public int getChangingConfigurations() {
                return 0;
            }
        }

        BitmapDrawable(Resources resources, Bitmap bitmap) {
            this(resources, bitmap, null);
        }

        BitmapDrawable(Resources resources, Bitmap bitmap, Matrix matrix) {
            this.mState = new ConstantState(bitmap, matrix);
        }

        BitmapDrawable(ConstantState state) {
            this.mState = state;
        }

        /* Access modifiers changed, original: 0000 */
        public Bitmap getBitmap() {
            return this.mState.mBitmap;
        }

        public void draw(Canvas canvas) {
            if (this.mState.mBitmap != null) {
                if (this.mState.mPaint.getAlpha() >= 255 || this.mState.mPaint.getColorFilter() == null) {
                    canvas.drawBitmap(this.mState.mBitmap, this.mState.mMatrix, this.mState.mPaint);
                    return;
                }
                throw new IllegalStateException("Can't draw with translucent alpha and color filter");
            }
        }

        public int getOpacity() {
            return -3;
        }

        public void setAlpha(int alpha) {
            mutate();
            if (this.mState.mPaint.getAlpha() != alpha) {
                this.mState.mPaint.setAlpha(alpha);
                invalidateSelf();
            }
        }

        public void setColorFilter(ColorFilter cf) {
            mutate();
            this.mState.mPaint.setColorFilter(cf);
            invalidateSelf();
        }

        public ColorFilter getColorFilter() {
            return this.mState.mPaint.getColorFilter();
        }

        public ConstantState getConstantState() {
            return this.mState;
        }

        @NonNull
        public Drawable mutate() {
            if (!this.mMutated) {
                this.mMutated = true;
                this.mState = new ConstantState(this.mState);
            }
            return this;
        }
    }

    final class ChangeBackgroundRunnable implements Runnable {
        final Drawable mDrawable;

        ChangeBackgroundRunnable(Drawable drawable) {
            this.mDrawable = drawable;
        }

        public void run() {
            runTask();
            BackgroundManager.this.mChangeRunnable = null;
        }

        private void runTask() {
            if (BackgroundManager.this.mLayerDrawable != null) {
                DrawableWrapper imageInWrapper = BackgroundManager.this.getImageInWrapper();
                if (imageInWrapper != null) {
                    if (!BackgroundManager.this.sameDrawable(this.mDrawable, imageInWrapper.getDrawable())) {
                        BackgroundManager.this.mLayerDrawable.clearDrawable(R.id.background_imagein, BackgroundManager.this.mContext);
                        BackgroundManager.this.mLayerDrawable.updateDrawable(R.id.background_imageout, imageInWrapper.getDrawable());
                    } else {
                        return;
                    }
                }
                applyBackgroundChanges();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void applyBackgroundChanges() {
            if (BackgroundManager.this.mAttached) {
                if (BackgroundManager.this.getImageInWrapper() == null && this.mDrawable != null) {
                    DrawableWrapper imageInWrapper = BackgroundManager.this.mLayerDrawable.updateDrawable(R.id.background_imagein, this.mDrawable);
                    BackgroundManager.this.mLayerDrawable.setWrapperAlpha(BackgroundManager.this.mImageInWrapperIndex, 0);
                }
                BackgroundManager.this.mAnimator.setDuration(500);
                BackgroundManager.this.mAnimator.start();
            }
        }
    }

    static final class DrawableWrapper {
        int mAlpha = 255;
        final Drawable mDrawable;

        public DrawableWrapper(Drawable drawable) {
            this.mDrawable = drawable;
        }

        public DrawableWrapper(DrawableWrapper wrapper, Drawable drawable) {
            this.mDrawable = drawable;
            this.mAlpha = wrapper.mAlpha;
        }

        public Drawable getDrawable() {
            return this.mDrawable;
        }

        public void setColor(int color) {
            ((ColorDrawable) this.mDrawable).setColor(color);
        }
    }

    static final class TranslucentLayerDrawable extends LayerDrawable {
        int mAlpha = 255;
        WeakReference<BackgroundManager> mManagerWeakReference;
        boolean mSuspendInvalidation;
        DrawableWrapper[] mWrapper;

        TranslucentLayerDrawable(BackgroundManager manager, Drawable[] drawables) {
            super(drawables);
            this.mManagerWeakReference = new WeakReference(manager);
            int count = drawables.length;
            this.mWrapper = new DrawableWrapper[count];
            for (int i = 0; i < count; i++) {
                this.mWrapper[i] = new DrawableWrapper(drawables[i]);
            }
        }

        public void setAlpha(int alpha) {
            if (this.mAlpha != alpha) {
                this.mAlpha = alpha;
                invalidateSelf();
                BackgroundManager manager = (BackgroundManager) this.mManagerWeakReference.get();
                if (manager != null) {
                    manager.postChangeRunnable();
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setWrapperAlpha(int wrapperIndex, int alpha) {
            if (this.mWrapper[wrapperIndex] != null) {
                this.mWrapper[wrapperIndex].mAlpha = alpha;
                invalidateSelf();
            }
        }

        public int getAlpha() {
            return this.mAlpha;
        }

        public Drawable mutate() {
            Drawable drawable = super.mutate();
            int count = getNumberOfLayers();
            for (int i = 0; i < count; i++) {
                if (this.mWrapper[i] != null) {
                    this.mWrapper[i] = new DrawableWrapper(this.mWrapper[i], getDrawable(i));
                }
            }
            return drawable;
        }

        public int getOpacity() {
            return -3;
        }

        public boolean setDrawableByLayerId(int id, Drawable drawable) {
            return updateDrawable(id, drawable) != null;
        }

        public DrawableWrapper updateDrawable(int id, Drawable drawable) {
            super.setDrawableByLayerId(id, drawable);
            for (int i = 0; i < getNumberOfLayers(); i++) {
                if (getId(i) == id) {
                    this.mWrapper[i] = new DrawableWrapper(drawable);
                    invalidateSelf();
                    return this.mWrapper[i];
                }
            }
            return null;
        }

        public void clearDrawable(int id, Context context) {
            for (int i = 0; i < getNumberOfLayers(); i++) {
                if (getId(i) == id) {
                    this.mWrapper[i] = null;
                    if (!(getDrawable(i) instanceof EmptyDrawable)) {
                        super.setDrawableByLayerId(id, BackgroundManager.createEmptyDrawable(context));
                        return;
                    }
                    return;
                }
            }
        }

        public int findWrapperIndexById(int id) {
            for (int i = 0; i < getNumberOfLayers(); i++) {
                if (getId(i) == id) {
                    return i;
                }
            }
            return -1;
        }

        public void invalidateDrawable(Drawable who) {
            if (!this.mSuspendInvalidation) {
                super.invalidateDrawable(who);
            }
        }

        public void draw(Canvas canvas) {
            for (int i = 0; i < this.mWrapper.length; i++) {
                if (this.mWrapper[i] != null) {
                    Drawable drawable = this.mWrapper[i].getDrawable();
                    Drawable d = drawable;
                    if (drawable != null) {
                        int alpha = VERSION.SDK_INT >= 19 ? DrawableCompat.getAlpha(d) : 255;
                        int savedAlpha = alpha;
                        int multiple = 0;
                        if (this.mAlpha < 255) {
                            alpha *= this.mAlpha;
                            multiple = 0 + 1;
                        }
                        if (this.mWrapper[i].mAlpha < 255) {
                            alpha *= this.mWrapper[i].mAlpha;
                            multiple++;
                        }
                        if (multiple == 0) {
                            d.draw(canvas);
                        } else {
                            if (multiple == 1) {
                                alpha /= 255;
                            } else if (multiple == 2) {
                                alpha /= 65025;
                            }
                            try {
                                this.mSuspendInvalidation = true;
                                d.setAlpha(alpha);
                                d.draw(canvas);
                                d.setAlpha(savedAlpha);
                            } finally {
                                this.mSuspendInvalidation = false;
                            }
                        }
                    }
                }
            }
        }
    }

    static class EmptyDrawable extends BitmapDrawable {
        EmptyDrawable(Resources res) {
            super(res, (Bitmap) null);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public TranslucentLayerDrawable createTranslucentLayerDrawable(LayerDrawable layerDrawable) {
        int numChildren = layerDrawable.getNumberOfLayers();
        Drawable[] drawables = new Drawable[numChildren];
        int i = 0;
        for (int i2 = 0; i2 < numChildren; i2++) {
            drawables[i2] = layerDrawable.getDrawable(i2);
        }
        TranslucentLayerDrawable result = new TranslucentLayerDrawable(this, drawables);
        while (i < numChildren) {
            result.setId(i, layerDrawable.getId(i));
            i++;
        }
        return result;
    }

    /* Access modifiers changed, original: 0000 */
    public Drawable getDefaultDrawable() {
        if (this.mBackgroundColor != 0) {
            return new ColorDrawable(this.mBackgroundColor);
        }
        return getThemeDrawable();
    }

    private Drawable getThemeDrawable() {
        Drawable drawable = null;
        if (this.mThemeDrawableResourceId != -1) {
            drawable = this.mService.getThemeDrawable(this.mContext, this.mThemeDrawableResourceId);
        }
        if (drawable == null) {
            return createEmptyDrawable(this.mContext);
        }
        return drawable;
    }

    public static BackgroundManager getInstance(Activity activity) {
        BackgroundFragment fragment = (BackgroundFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            BackgroundManager manager = fragment.getBackgroundManager();
            if (manager != null) {
                return manager;
            }
        }
        return new BackgroundManager(activity);
    }

    private BackgroundManager(Activity activity) {
        this.mContext = activity;
        this.mService = BackgroundContinuityService.getInstance();
        this.mHeightPx = this.mContext.getResources().getDisplayMetrics().heightPixels;
        this.mWidthPx = this.mContext.getResources().getDisplayMetrics().widthPixels;
        this.mHandler = new Handler();
        Interpolator defaultInterpolator = new FastOutLinearInInterpolator();
        this.mAccelerateInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17432581);
        this.mDecelerateInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17432582);
        this.mAnimator = ValueAnimator.ofInt(new int[]{0, 255});
        this.mAnimator.addListener(this.mAnimationListener);
        this.mAnimator.addUpdateListener(this.mAnimationUpdateListener);
        this.mAnimator.setInterpolator(defaultInterpolator);
        TypedArray ta = activity.getTheme().obtainStyledAttributes(new int[]{16842836});
        this.mThemeDrawableResourceId = ta.getResourceId(0, -1);
        int i = this.mThemeDrawableResourceId;
        ta.recycle();
        createFragment(activity);
    }

    private void createFragment(Activity activity) {
        BackgroundFragment fragment = (BackgroundFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new BackgroundFragment();
            activity.getFragmentManager().beginTransaction().add(fragment, FRAGMENT_TAG).commit();
        } else if (fragment.getBackgroundManager() != null) {
            throw new IllegalStateException("Created duplicated BackgroundManager for same activity, please use getInstance() instead");
        }
        fragment.setBackgroundManager(this);
        this.mFragmentState = fragment;
    }

    /* Access modifiers changed, original: 0000 */
    public DrawableWrapper getImageInWrapper() {
        return this.mLayerDrawable == null ? null : this.mLayerDrawable.mWrapper[this.mImageInWrapperIndex];
    }

    /* Access modifiers changed, original: 0000 */
    public DrawableWrapper getImageOutWrapper() {
        return this.mLayerDrawable == null ? null : this.mLayerDrawable.mWrapper[this.mImageOutWrapperIndex];
    }

    /* Access modifiers changed, original: 0000 */
    public void onActivityStart() {
        updateImmediate();
    }

    /* Access modifiers changed, original: 0000 */
    public void onStop() {
        if (isAutoReleaseOnStop()) {
            release();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onResume() {
        postChangeRunnable();
    }

    private void syncWithService() {
        Drawable drawable;
        int color = this.mService.getColor();
        Drawable drawable2 = this.mService.getDrawable();
        this.mBackgroundColor = color;
        if (drawable2 == null) {
            drawable = null;
        } else {
            drawable = drawable2.getConstantState().newDrawable().mutate();
        }
        this.mBackgroundDrawable = drawable;
        updateImmediate();
    }

    public void attach(Window window) {
        attachToViewInternal(window.getDecorView());
    }

    public void setThemeDrawableResourceId(int resourceId) {
        this.mThemeDrawableResourceId = resourceId;
    }

    public void attachToView(View sceneRoot) {
        attachToViewInternal(sceneRoot);
        this.mContext.getWindow().getDecorView().setBackground(VERSION.SDK_INT >= 26 ? null : new ColorDrawable(0));
    }

    /* Access modifiers changed, original: 0000 */
    public void attachToViewInternal(View sceneRoot) {
        if (this.mAttached) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Already attached to ");
            stringBuilder.append(this.mBgView);
            throw new IllegalStateException(stringBuilder.toString());
        }
        this.mBgView = sceneRoot;
        this.mAttached = true;
        syncWithService();
    }

    public boolean isAttached() {
        return this.mAttached;
    }

    /* Access modifiers changed, original: 0000 */
    public void detach() {
        release();
        this.mBgView = null;
        this.mAttached = false;
        if (this.mService != null) {
            this.mService.unref();
            this.mService = null;
        }
    }

    public void release() {
        if (this.mChangeRunnable != null) {
            this.mHandler.removeCallbacks(this.mChangeRunnable);
            this.mChangeRunnable = null;
        }
        if (this.mAnimator.isStarted()) {
            this.mAnimator.cancel();
        }
        if (this.mLayerDrawable != null) {
            this.mLayerDrawable.clearDrawable(R.id.background_imagein, this.mContext);
            this.mLayerDrawable.clearDrawable(R.id.background_imageout, this.mContext);
            this.mLayerDrawable = null;
        }
        this.mBackgroundDrawable = null;
    }

    @Deprecated
    public void setDimLayer(Drawable drawable) {
    }

    @Deprecated
    public Drawable getDimLayer() {
        return null;
    }

    @Deprecated
    public Drawable getDefaultDimLayer() {
        return ContextCompat.getDrawable(this.mContext, R.color.lb_background_protection);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:12:0x003b, code skipped:
            return;
     */
    public void postChangeRunnable() {
        /*
        r4 = this;
        r0 = r4.mChangeRunnable;
        if (r0 == 0) goto L_0x003b;
    L_0x0004:
        r0 = r4.mChangeRunnablePending;
        if (r0 != 0) goto L_0x0009;
    L_0x0008:
        goto L_0x003b;
    L_0x0009:
        r0 = r4.mAnimator;
        r0 = r0.isStarted();
        if (r0 == 0) goto L_0x0012;
    L_0x0011:
        goto L_0x003a;
    L_0x0012:
        r0 = r4.mFragmentState;
        r0 = r0.isResumed();
        if (r0 != 0) goto L_0x001b;
    L_0x001a:
        goto L_0x003a;
    L_0x001b:
        r0 = r4.mLayerDrawable;
        r0 = r0.getAlpha();
        r1 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r0 >= r1) goto L_0x0026;
    L_0x0025:
        goto L_0x003a;
    L_0x0026:
        r0 = r4.getRunnableDelay();
        r2 = java.lang.System.currentTimeMillis();
        r4.mLastSetTime = r2;
        r2 = r4.mHandler;
        r3 = r4.mChangeRunnable;
        r2.postDelayed(r3, r0);
        r2 = 0;
        r4.mChangeRunnablePending = r2;
    L_0x003a:
        return;
    L_0x003b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.BackgroundManager.postChangeRunnable():void");
    }

    private void lazyInit() {
        if (this.mLayerDrawable == null) {
            this.mLayerDrawable = createTranslucentLayerDrawable((LayerDrawable) ContextCompat.getDrawable(this.mContext, R.drawable.lb_background).mutate());
            this.mImageInWrapperIndex = this.mLayerDrawable.findWrapperIndexById(R.id.background_imagein);
            this.mImageOutWrapperIndex = this.mLayerDrawable.findWrapperIndexById(R.id.background_imageout);
            BackgroundHelper.setBackgroundPreservingAlpha(this.mBgView, this.mLayerDrawable);
        }
    }

    private void updateImmediate() {
        if (this.mAttached) {
            lazyInit();
            if (this.mBackgroundDrawable == null) {
                this.mLayerDrawable.updateDrawable(R.id.background_imagein, getDefaultDrawable());
            } else {
                this.mLayerDrawable.updateDrawable(R.id.background_imagein, this.mBackgroundDrawable);
            }
            this.mLayerDrawable.clearDrawable(R.id.background_imageout, this.mContext);
        }
    }

    public void setColor(@ColorInt int color) {
        this.mService.setColor(color);
        this.mBackgroundColor = color;
        this.mBackgroundDrawable = null;
        if (this.mLayerDrawable != null) {
            setDrawableInternal(getDefaultDrawable());
        }
    }

    public void setDrawable(Drawable drawable) {
        this.mService.setDrawable(drawable);
        this.mBackgroundDrawable = drawable;
        if (this.mLayerDrawable != null) {
            if (drawable == null) {
                setDrawableInternal(getDefaultDrawable());
            } else {
                setDrawableInternal(drawable);
            }
        }
    }

    public void clearDrawable() {
        setDrawable(null);
    }

    private void setDrawableInternal(Drawable drawable) {
        if (this.mAttached) {
            if (this.mChangeRunnable != null) {
                if (!sameDrawable(drawable, this.mChangeRunnable.mDrawable)) {
                    this.mHandler.removeCallbacks(this.mChangeRunnable);
                    this.mChangeRunnable = null;
                } else {
                    return;
                }
            }
            this.mChangeRunnable = new ChangeBackgroundRunnable(drawable);
            this.mChangeRunnablePending = true;
            postChangeRunnable();
            return;
        }
        throw new IllegalStateException("Must attach before setting background drawable");
    }

    private long getRunnableDelay() {
        return Math.max(0, (this.mLastSetTime + 500) - System.currentTimeMillis());
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            setDrawable(null);
        } else if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            Matrix matrix = null;
            if (!(bitmap.getWidth() == this.mWidthPx && bitmap.getHeight() == this.mHeightPx)) {
                float scale;
                int dwidth = bitmap.getWidth();
                int dheight = bitmap.getHeight();
                if (this.mHeightPx * dwidth > this.mWidthPx * dheight) {
                    scale = ((float) this.mHeightPx) / ((float) dheight);
                } else {
                    scale = ((float) this.mWidthPx) / ((float) dwidth);
                }
                int dx = Math.max(0, (dwidth - Math.min((int) (((float) this.mWidthPx) / scale), dwidth)) / 2);
                matrix = new Matrix();
                matrix.setScale(scale, scale);
                matrix.preTranslate((float) (-dx), 0.0f);
            }
            setDrawable(new BitmapDrawable(this.mContext.getResources(), bitmap, matrix));
        }
    }

    public void setAutoReleaseOnStop(boolean autoReleaseOnStop) {
        this.mAutoReleaseOnStop = autoReleaseOnStop;
    }

    public boolean isAutoReleaseOnStop() {
        return this.mAutoReleaseOnStop;
    }

    @ColorInt
    public final int getColor() {
        return this.mBackgroundColor;
    }

    public Drawable getDrawable() {
        return this.mBackgroundDrawable;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean sameDrawable(Drawable first, Drawable second) {
        if (first == null || second == null) {
            return false;
        }
        if (first == second) {
            return true;
        }
        if ((first instanceof BitmapDrawable) && (second instanceof BitmapDrawable) && ((BitmapDrawable) first).getBitmap().sameAs(((BitmapDrawable) second).getBitmap())) {
            return true;
        }
        if ((first instanceof ColorDrawable) && (second instanceof ColorDrawable) && ((ColorDrawable) first).getColor() == ((ColorDrawable) second).getColor()) {
            return true;
        }
        return false;
    }

    static Drawable createEmptyDrawable(Context context) {
        return new EmptyDrawable(context.getResources());
    }
}
