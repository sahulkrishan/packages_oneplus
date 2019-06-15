package android.support.v17.leanback.widget;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.content.res.Resources;
import android.support.v17.leanback.R;
import android.support.v17.leanback.graphics.ColorOverlayDimmer;
import android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class FocusHighlightHelper {

    static class FocusAnimator implements TimeListener {
        private final TimeAnimator mAnimator = new TimeAnimator();
        private final ColorOverlayDimmer mDimmer;
        private final int mDuration;
        private float mFocusLevel = 0.0f;
        private float mFocusLevelDelta;
        private float mFocusLevelStart;
        private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
        private final float mScaleDiff;
        private final View mView;
        private final ShadowOverlayContainer mWrapper;

        /* Access modifiers changed, original: 0000 */
        public void animateFocus(boolean select, boolean immediate) {
            endAnimation();
            float end = select ? 1.0f : 0.0f;
            if (immediate) {
                setFocusLevel(end);
            } else if (this.mFocusLevel != end) {
                this.mFocusLevelStart = this.mFocusLevel;
                this.mFocusLevelDelta = end - this.mFocusLevelStart;
                this.mAnimator.start();
            }
        }

        FocusAnimator(View view, float scale, boolean useDimmer, int duration) {
            this.mView = view;
            this.mDuration = duration;
            this.mScaleDiff = scale - 1.0f;
            if (view instanceof ShadowOverlayContainer) {
                this.mWrapper = (ShadowOverlayContainer) view;
            } else {
                this.mWrapper = null;
            }
            this.mAnimator.setTimeListener(this);
            if (useDimmer) {
                this.mDimmer = ColorOverlayDimmer.createDefault(view.getContext());
            } else {
                this.mDimmer = null;
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setFocusLevel(float level) {
            this.mFocusLevel = level;
            float scale = 1.0f + (this.mScaleDiff * level);
            this.mView.setScaleX(scale);
            this.mView.setScaleY(scale);
            if (this.mWrapper != null) {
                this.mWrapper.setShadowFocusLevel(level);
            } else {
                ShadowOverlayHelper.setNoneWrapperShadowFocusLevel(this.mView, level);
            }
            if (this.mDimmer != null) {
                this.mDimmer.setActiveLevel(level);
                int color = this.mDimmer.getPaint().getColor();
                if (this.mWrapper != null) {
                    this.mWrapper.setOverlayColor(color);
                } else {
                    ShadowOverlayHelper.setNoneWrapperOverlayColor(this.mView, color);
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public float getFocusLevel() {
            return this.mFocusLevel;
        }

        /* Access modifiers changed, original: 0000 */
        public void endAnimation() {
            this.mAnimator.end();
        }

        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= ((long) this.mDuration)) {
                fraction = 1.0f;
                this.mAnimator.end();
            } else {
                fraction = (float) (((double) totalTime) / ((double) this.mDuration));
            }
            if (this.mInterpolator != null) {
                fraction = this.mInterpolator.getInterpolation(fraction);
            }
            setFocusLevel(this.mFocusLevelStart + (this.mFocusLevelDelta * fraction));
        }
    }

    static class BrowseItemFocusHighlight implements FocusHighlightHandler {
        private static final int DURATION_MS = 150;
        private int mScaleIndex;
        private final boolean mUseDimmer;

        BrowseItemFocusHighlight(int zoomIndex, boolean useDimmer) {
            if (FocusHighlightHelper.isValidZoomIndex(zoomIndex)) {
                this.mScaleIndex = zoomIndex;
                this.mUseDimmer = useDimmer;
                return;
            }
            throw new IllegalArgumentException("Unhandled zoom index");
        }

        private float getScale(Resources res) {
            if (this.mScaleIndex == 0) {
                return 1.0f;
            }
            return res.getFraction(FocusHighlightHelper.getResId(this.mScaleIndex), 1, 1);
        }

        public void onItemFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            getOrCreateAnimator(view).animateFocus(hasFocus, false);
        }

        public void onInitializeView(View view) {
            getOrCreateAnimator(view).animateFocus(false, true);
        }

        private FocusAnimator getOrCreateAnimator(View view) {
            FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
            if (animator != null) {
                return animator;
            }
            animator = new FocusAnimator(view, getScale(view.getResources()), this.mUseDimmer, 150);
            view.setTag(R.id.lb_focus_animator, animator);
            return animator;
        }
    }

    static class HeaderItemFocusHighlight implements FocusHighlightHandler {
        private int mDuration;
        private boolean mInitialized;
        boolean mScaleEnabled;
        private float mSelectScale;

        static class HeaderFocusAnimator extends FocusAnimator {
            ViewHolder mViewHolder;

            HeaderFocusAnimator(View view, float scale, int duration) {
                super(view, scale, false, duration);
                ViewParent parent = view.getParent();
                while (parent != null && !(parent instanceof RecyclerView)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    this.mViewHolder = (ViewHolder) ((RecyclerView) parent).getChildViewHolder(view);
                }
            }

            /* Access modifiers changed, original: 0000 */
            public void setFocusLevel(float level) {
                Presenter presenter = this.mViewHolder.getPresenter();
                if (presenter instanceof RowHeaderPresenter) {
                    ((RowHeaderPresenter) presenter).setSelectLevel((RowHeaderPresenter.ViewHolder) this.mViewHolder.getViewHolder(), level);
                }
                super.setFocusLevel(level);
            }
        }

        HeaderItemFocusHighlight(boolean scaleEnabled) {
            this.mScaleEnabled = scaleEnabled;
        }

        /* Access modifiers changed, original: 0000 */
        public void lazyInit(View view) {
            if (!this.mInitialized) {
                Resources res = view.getResources();
                TypedValue value = new TypedValue();
                if (this.mScaleEnabled) {
                    res.getValue(R.dimen.lb_browse_header_select_scale, value, true);
                    this.mSelectScale = value.getFloat();
                } else {
                    this.mSelectScale = 1.0f;
                }
                res.getValue(R.dimen.lb_browse_header_select_duration, value, true);
                this.mDuration = value.data;
                this.mInitialized = true;
            }
        }

        private void viewFocused(View view, boolean hasFocus) {
            lazyInit(view);
            view.setSelected(hasFocus);
            FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
            if (animator == null) {
                animator = new HeaderFocusAnimator(view, this.mSelectScale, this.mDuration);
                view.setTag(R.id.lb_focus_animator, animator);
            }
            animator.animateFocus(hasFocus, false);
        }

        public void onItemFocused(View view, boolean hasFocus) {
            viewFocused(view, hasFocus);
        }

        public void onInitializeView(View view) {
        }
    }

    static boolean isValidZoomIndex(int zoomIndex) {
        return zoomIndex == 0 || getResId(zoomIndex) > 0;
    }

    static int getResId(int zoomIndex) {
        switch (zoomIndex) {
            case 1:
                return R.fraction.lb_focus_zoom_factor_small;
            case 2:
                return R.fraction.lb_focus_zoom_factor_medium;
            case 3:
                return R.fraction.lb_focus_zoom_factor_large;
            case 4:
                return R.fraction.lb_focus_zoom_factor_xsmall;
            default:
                return 0;
        }
    }

    public static void setupBrowseItemFocusHighlight(ItemBridgeAdapter adapter, int zoomIndex, boolean useDimmer) {
        adapter.setFocusHighlight(new BrowseItemFocusHighlight(zoomIndex, useDimmer));
    }

    @Deprecated
    public static void setupHeaderItemFocusHighlight(VerticalGridView gridView) {
        setupHeaderItemFocusHighlight(gridView, true);
    }

    @Deprecated
    public static void setupHeaderItemFocusHighlight(VerticalGridView gridView, boolean scaleEnabled) {
        if (gridView != null && (gridView.getAdapter() instanceof ItemBridgeAdapter)) {
            ((ItemBridgeAdapter) gridView.getAdapter()).setFocusHighlight(new HeaderItemFocusHighlight(scaleEnabled));
        }
    }

    public static void setupHeaderItemFocusHighlight(ItemBridgeAdapter adapter) {
        setupHeaderItemFocusHighlight(adapter, true);
    }

    public static void setupHeaderItemFocusHighlight(ItemBridgeAdapter adapter, boolean scaleEnabled) {
        adapter.setFocusHighlight(new HeaderItemFocusHighlight(scaleEnabled));
    }
}
