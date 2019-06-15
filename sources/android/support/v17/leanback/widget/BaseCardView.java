package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import com.google.common.primitives.Ints;
import java.util.ArrayList;

public class BaseCardView extends FrameLayout {
    public static final int CARD_REGION_VISIBLE_ACTIVATED = 1;
    public static final int CARD_REGION_VISIBLE_ALWAYS = 0;
    public static final int CARD_REGION_VISIBLE_SELECTED = 2;
    public static final int CARD_TYPE_INFO_OVER = 1;
    public static final int CARD_TYPE_INFO_UNDER = 2;
    public static final int CARD_TYPE_INFO_UNDER_WITH_EXTRA = 3;
    private static final int CARD_TYPE_INVALID = 4;
    public static final int CARD_TYPE_MAIN_ONLY = 0;
    private static final boolean DEBUG = false;
    private static final int[] LB_PRESSED_STATE_SET = new int[]{16842919};
    private static final String TAG = "BaseCardView";
    private final int mActivatedAnimDuration;
    private Animation mAnim;
    private final Runnable mAnimationTrigger;
    private int mCardType;
    private boolean mDelaySelectedAnim;
    ArrayList<View> mExtraViewList;
    private int mExtraVisibility;
    float mInfoAlpha;
    float mInfoOffset;
    ArrayList<View> mInfoViewList;
    float mInfoVisFraction;
    private int mInfoVisibility;
    private ArrayList<View> mMainViewList;
    private int mMeasuredHeight;
    private int mMeasuredWidth;
    private final int mSelectedAnimDuration;
    private int mSelectedAnimationDelay;

    class AnimationBase extends Animation {
        AnimationBase() {
        }

        /* Access modifiers changed, original: final */
        @VisibleForTesting
        public final void mockStart() {
            getTransformation(0, null);
        }

        /* Access modifiers changed, original: final */
        @VisibleForTesting
        public final void mockEnd() {
            applyTransformation(1.0f, null);
            BaseCardView.this.cancelAnimations();
        }
    }

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public static final int VIEW_TYPE_EXTRA = 2;
        public static final int VIEW_TYPE_INFO = 1;
        public static final int VIEW_TYPE_MAIN = 0;
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "MAIN"), @IntToString(from = 1, to = "INFO"), @IntToString(from = 2, to = "EXTRA")})
        public int viewType = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.lbBaseCardView_Layout);
            this.viewType = a.getInt(R.styleable.lbBaseCardView_Layout_layout_viewType, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.viewType = source.viewType;
        }
    }

    final class InfoAlphaAnimation extends AnimationBase {
        private float mDelta;
        private float mStartValue;

        public InfoAlphaAnimation(float start, float end) {
            super();
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        /* Access modifiers changed, original: protected */
        public void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoAlpha = this.mStartValue + (this.mDelta * interpolatedTime);
            for (int i = 0; i < BaseCardView.this.mInfoViewList.size(); i++) {
                ((View) BaseCardView.this.mInfoViewList.get(i)).setAlpha(BaseCardView.this.mInfoAlpha);
            }
        }
    }

    final class InfoHeightAnimation extends AnimationBase {
        private float mDelta;
        private float mStartValue;

        public InfoHeightAnimation(float start, float end) {
            super();
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        /* Access modifiers changed, original: protected */
        public void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoVisFraction = this.mStartValue + (this.mDelta * interpolatedTime);
            BaseCardView.this.requestLayout();
        }
    }

    final class InfoOffsetAnimation extends AnimationBase {
        private float mDelta;
        private float mStartValue;

        public InfoOffsetAnimation(float start, float end) {
            super();
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        /* Access modifiers changed, original: protected */
        public void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoOffset = this.mStartValue + (this.mDelta * interpolatedTime);
            BaseCardView.this.requestLayout();
        }
    }

    public BaseCardView(Context context) {
        this(context, null);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.baseCardViewStyle);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mAnimationTrigger = new Runnable() {
            public void run() {
                BaseCardView.this.animateInfoOffset(true);
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.lbBaseCardView, defStyleAttr, 0);
        try {
            this.mCardType = a.getInteger(R.styleable.lbBaseCardView_cardType, 0);
            Drawable cardForeground = a.getDrawable(R.styleable.lbBaseCardView_cardForeground);
            if (cardForeground != null) {
                setForeground(cardForeground);
            }
            Drawable cardBackground = a.getDrawable(R.styleable.lbBaseCardView_cardBackground);
            if (cardBackground != null) {
                setBackground(cardBackground);
            }
            this.mInfoVisibility = a.getInteger(R.styleable.lbBaseCardView_infoVisibility, 1);
            this.mExtraVisibility = a.getInteger(R.styleable.lbBaseCardView_extraVisibility, 2);
            if (this.mExtraVisibility < this.mInfoVisibility) {
                this.mExtraVisibility = this.mInfoVisibility;
            }
            this.mSelectedAnimationDelay = a.getInteger(R.styleable.lbBaseCardView_selectedAnimationDelay, getResources().getInteger(R.integer.lb_card_selected_animation_delay));
            this.mSelectedAnimDuration = a.getInteger(R.styleable.lbBaseCardView_selectedAnimationDuration, getResources().getInteger(R.integer.lb_card_selected_animation_duration));
            this.mActivatedAnimDuration = a.getInteger(R.styleable.lbBaseCardView_activatedAnimationDuration, getResources().getInteger(R.integer.lb_card_activated_animation_duration));
            this.mDelaySelectedAnim = true;
            this.mMainViewList = new ArrayList();
            this.mInfoViewList = new ArrayList();
            this.mExtraViewList = new ArrayList();
            this.mInfoOffset = 0.0f;
            this.mInfoVisFraction = getFinalInfoVisFraction();
            this.mInfoAlpha = getFinalInfoAlpha();
        } finally {
            a.recycle();
        }
    }

    public void setSelectedAnimationDelayed(boolean delay) {
        this.mDelaySelectedAnim = delay;
    }

    public boolean isSelectedAnimationDelayed() {
        return this.mDelaySelectedAnim;
    }

    public void setCardType(int type) {
        if (this.mCardType != type) {
            if (type < 0 || type >= 4) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid card type specified: ");
                stringBuilder.append(type);
                stringBuilder.append(". Defaulting to type CARD_TYPE_MAIN_ONLY.");
                Log.e(str, stringBuilder.toString());
                this.mCardType = 0;
            } else {
                this.mCardType = type;
            }
            requestLayout();
        }
    }

    public int getCardType() {
        return this.mCardType;
    }

    public void setInfoVisibility(int visibility) {
        if (this.mInfoVisibility != visibility) {
            cancelAnimations();
            this.mInfoVisibility = visibility;
            this.mInfoVisFraction = getFinalInfoVisFraction();
            requestLayout();
            float newInfoAlpha = getFinalInfoAlpha();
            if (newInfoAlpha != this.mInfoAlpha) {
                this.mInfoAlpha = newInfoAlpha;
                for (int i = 0; i < this.mInfoViewList.size(); i++) {
                    ((View) this.mInfoViewList.get(i)).setAlpha(this.mInfoAlpha);
                }
            }
        }
    }

    /* Access modifiers changed, original: final */
    public final float getFinalInfoVisFraction() {
        return (this.mCardType == 2 && this.mInfoVisibility == 2 && !isSelected()) ? 0.0f : 1.0f;
    }

    /* Access modifiers changed, original: final */
    public final float getFinalInfoAlpha() {
        return (this.mCardType == 1 && this.mInfoVisibility == 2 && !isSelected()) ? 0.0f : 1.0f;
    }

    public int getInfoVisibility() {
        return this.mInfoVisibility;
    }

    @Deprecated
    public void setExtraVisibility(int visibility) {
        if (this.mExtraVisibility != visibility) {
            this.mExtraVisibility = visibility;
        }
    }

    @Deprecated
    public int getExtraVisibility() {
        return this.mExtraVisibility;
    }

    public void setActivated(boolean activated) {
        if (activated != isActivated()) {
            super.setActivated(activated);
            applyActiveState(isActivated());
        }
    }

    public void setSelected(boolean selected) {
        if (selected != isSelected()) {
            super.setSelected(selected);
            applySelectedState(isSelected());
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int state;
        boolean infoAnimating = false;
        this.mMeasuredWidth = 0;
        this.mMeasuredHeight = 0;
        int mainHeight = 0;
        int extraHeight = 0;
        findChildrenViews();
        int unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int state2 = 0;
        for (i = 0; i < this.mMainViewList.size(); i++) {
            View mainView = (View) this.mMainViewList.get(i);
            if (mainView.getVisibility() != 8) {
                measureChild(mainView, unspecifiedSpec, unspecifiedSpec);
                this.mMeasuredWidth = Math.max(this.mMeasuredWidth, mainView.getMeasuredWidth());
                mainHeight += mainView.getMeasuredHeight();
                state2 = View.combineMeasuredStates(state2, mainView.getMeasuredState());
            }
        }
        setPivotX((float) (this.mMeasuredWidth / 2));
        setPivotY((float) (mainHeight / 2));
        i = MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, Ints.MAX_POWER_OF_TWO);
        if (hasInfoRegion()) {
            int i2;
            View infoView;
            state = state2;
            state2 = 0;
            for (i2 = 0; i2 < this.mInfoViewList.size(); i2++) {
                infoView = (View) this.mInfoViewList.get(i2);
                if (infoView.getVisibility() != 8) {
                    measureChild(infoView, i, unspecifiedSpec);
                    if (this.mCardType != 1) {
                        state2 += infoView.getMeasuredHeight();
                    }
                    state = View.combineMeasuredStates(state, infoView.getMeasuredState());
                }
            }
            if (hasExtraRegion()) {
                for (i2 = 0; i2 < this.mExtraViewList.size(); i2++) {
                    infoView = (View) this.mExtraViewList.get(i2);
                    if (infoView.getVisibility() != 8) {
                        measureChild(infoView, i, unspecifiedSpec);
                        extraHeight += infoView.getMeasuredHeight();
                        state = View.combineMeasuredStates(state, infoView.getMeasuredState());
                    }
                }
            }
        } else {
            state = state2;
            state2 = 0;
        }
        if (hasInfoRegion() && this.mInfoVisibility == 2) {
            infoAnimating = true;
        }
        this.mMeasuredHeight = (int) (((((float) mainHeight) + (infoAnimating ? ((float) state2) * this.mInfoVisFraction : (float) state2)) + ((float) extraHeight)) - (infoAnimating ? 0.0f : this.mInfoOffset));
        setMeasuredDimension(View.resolveSizeAndState((this.mMeasuredWidth + getPaddingLeft()) + getPaddingRight(), widthMeasureSpec, state), View.resolveSizeAndState((this.mMeasuredHeight + getPaddingTop()) + getPaddingBottom(), heightMeasureSpec, state << 16));
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int i;
        float currBottom = (float) getPaddingTop();
        for (i = 0; i < this.mMainViewList.size(); i++) {
            View mainView = (View) this.mMainViewList.get(i);
            if (mainView.getVisibility() != 8) {
                mainView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) mainView.getMeasuredHeight()) + currBottom));
                currBottom += (float) mainView.getMeasuredHeight();
            }
        }
        if (hasInfoRegion()) {
            float infoHeight = 0.0f;
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                infoHeight += (float) ((View) this.mInfoViewList.get(i)).getMeasuredHeight();
            }
            if (this.mCardType == 1) {
                currBottom -= infoHeight;
                if (currBottom < 0.0f) {
                    currBottom = 0.0f;
                }
            } else if (this.mCardType != 2) {
                currBottom -= this.mInfoOffset;
            } else if (this.mInfoVisibility == 2) {
                infoHeight *= this.mInfoVisFraction;
            }
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                View infoView = (View) this.mInfoViewList.get(i);
                if (infoView.getVisibility() != 8) {
                    int viewHeight = infoView.getMeasuredHeight();
                    if (((float) viewHeight) > infoHeight) {
                        viewHeight = (int) infoHeight;
                    }
                    infoView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) viewHeight) + currBottom));
                    currBottom += (float) viewHeight;
                    infoHeight -= (float) viewHeight;
                    if (infoHeight <= 0.0f) {
                        break;
                    }
                }
            }
            if (hasExtraRegion()) {
                for (i = 0; i < this.mExtraViewList.size(); i++) {
                    View extraView = (View) this.mExtraViewList.get(i);
                    if (extraView.getVisibility() != 8) {
                        extraView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) extraView.getMeasuredHeight()) + currBottom));
                        currBottom += (float) extraView.getMeasuredHeight();
                    }
                }
            }
        }
        onSizeChanged(0, 0, right - left, bottom - top);
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mAnimationTrigger);
        cancelAnimations();
    }

    private boolean hasInfoRegion() {
        return this.mCardType != 0;
    }

    private boolean hasExtraRegion() {
        return this.mCardType == 3;
    }

    private boolean isRegionVisible(int regionVisibility) {
        switch (regionVisibility) {
            case 0:
                return true;
            case 1:
                return isActivated();
            case 2:
                return isSelected();
            default:
                return false;
        }
    }

    private boolean isCurrentRegionVisible(int regionVisibility) {
        boolean z = true;
        switch (regionVisibility) {
            case 0:
                return true;
            case 1:
                return isActivated();
            case 2:
                if (this.mCardType != 2) {
                    return isSelected();
                }
                if (this.mInfoVisFraction <= 0.0f) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    private void findChildrenViews() {
        this.mMainViewList.clear();
        this.mInfoViewList.clear();
        this.mExtraViewList.clear();
        int count = getChildCount();
        boolean infoVisible = hasInfoRegion() && isCurrentRegionVisible(this.mInfoVisibility);
        boolean extraVisible = hasExtraRegion() && this.mInfoOffset > 0.0f;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int i2 = 8;
                if (lp.viewType == 1) {
                    child.setAlpha(this.mInfoAlpha);
                    this.mInfoViewList.add(child);
                    if (infoVisible) {
                        i2 = 0;
                    }
                    child.setVisibility(i2);
                } else if (lp.viewType == 2) {
                    this.mExtraViewList.add(child);
                    if (extraVisible) {
                        i2 = 0;
                    }
                    child.setVisibility(i2);
                } else {
                    this.mMainViewList.add(child);
                    child.setVisibility(0);
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] s = super.onCreateDrawableState(extraSpace);
        int N = s.length;
        boolean pressed = false;
        boolean enabled = false;
        for (int i = 0; i < N; i++) {
            if (s[i] == 16842919) {
                pressed = true;
            }
            if (s[i] == 16842910) {
                enabled = true;
            }
        }
        if (pressed && enabled) {
            return View.PRESSED_ENABLED_STATE_SET;
        }
        if (pressed) {
            return LB_PRESSED_STATE_SET;
        }
        if (enabled) {
            return View.ENABLED_STATE_SET;
        }
        return View.EMPTY_STATE_SET;
    }

    private void applyActiveState(boolean active) {
        if (hasInfoRegion() && this.mInfoVisibility == 1) {
            setInfoViewVisibility(isRegionVisible(this.mInfoVisibility));
        }
    }

    private void setInfoViewVisibility(boolean visible) {
        int i = 0;
        int i2;
        if (this.mCardType == 3) {
            if (visible) {
                for (i2 = 0; i2 < this.mInfoViewList.size(); i2++) {
                    ((View) this.mInfoViewList.get(i2)).setVisibility(0);
                }
                return;
            }
            for (i2 = 0; i2 < this.mInfoViewList.size(); i2++) {
                ((View) this.mInfoViewList.get(i2)).setVisibility(8);
            }
            while (true) {
                i2 = i;
                if (i2 < this.mExtraViewList.size()) {
                    ((View) this.mExtraViewList.get(i2)).setVisibility(8);
                    i = i2 + 1;
                } else {
                    this.mInfoOffset = 0.0f;
                    return;
                }
            }
        } else if (this.mCardType == 2) {
            if (this.mInfoVisibility == 2) {
                animateInfoHeight(visible);
                return;
            }
            for (i2 = 0; i2 < this.mInfoViewList.size(); i2++) {
                ((View) this.mInfoViewList.get(i2)).setVisibility(visible ? 0 : 8);
            }
        } else if (this.mCardType == 1) {
            animateInfoAlpha(visible);
        }
    }

    private void applySelectedState(boolean focused) {
        removeCallbacks(this.mAnimationTrigger);
        if (this.mCardType == 3) {
            if (!focused) {
                animateInfoOffset(false);
            } else if (this.mDelaySelectedAnim) {
                postDelayed(this.mAnimationTrigger, (long) this.mSelectedAnimationDelay);
            } else {
                post(this.mAnimationTrigger);
                this.mDelaySelectedAnim = true;
            }
        } else if (this.mInfoVisibility == 2) {
            setInfoViewVisibility(focused);
        }
    }

    private void cancelAnimations() {
        if (this.mAnim != null) {
            this.mAnim.cancel();
            this.mAnim = null;
            clearAnimation();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void animateInfoOffset(boolean shown) {
        cancelAnimations();
        int extraHeight = 0;
        if (shown) {
            int widthSpec = MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, Ints.MAX_POWER_OF_TWO);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, 0);
            int extraHeight2 = 0;
            for (extraHeight = 0; extraHeight < this.mExtraViewList.size(); extraHeight++) {
                View extraView = (View) this.mExtraViewList.get(extraHeight);
                extraView.setVisibility(0);
                extraView.measure(widthSpec, heightSpec);
                extraHeight2 = Math.max(extraHeight2, extraView.getMeasuredHeight());
            }
            extraHeight = extraHeight2;
        }
        this.mAnim = new InfoOffsetAnimation(this.mInfoOffset, shown ? (float) extraHeight : 0.0f);
        this.mAnim.setDuration((long) this.mSelectedAnimDuration);
        this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (BaseCardView.this.mInfoOffset == 0.0f) {
                    for (int i = 0; i < BaseCardView.this.mExtraViewList.size(); i++) {
                        ((View) BaseCardView.this.mExtraViewList.get(i)).setVisibility(8);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    private void animateInfoHeight(boolean shown) {
        cancelAnimations();
        if (shown) {
            for (int i = 0; i < this.mInfoViewList.size(); i++) {
                ((View) this.mInfoViewList.get(i)).setVisibility(0);
            }
        }
        float targetFraction = shown ? 1.0f : 0.0f;
        if (this.mInfoVisFraction != targetFraction) {
            this.mAnim = new InfoHeightAnimation(this.mInfoVisFraction, targetFraction);
            this.mAnim.setDuration((long) this.mSelectedAnimDuration);
            this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mAnim.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (BaseCardView.this.mInfoVisFraction == 0.0f) {
                        for (int i = 0; i < BaseCardView.this.mInfoViewList.size(); i++) {
                            ((View) BaseCardView.this.mInfoViewList.get(i)).setVisibility(8);
                        }
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            startAnimation(this.mAnim);
        }
    }

    private void animateInfoAlpha(boolean shown) {
        cancelAnimations();
        if (shown) {
            for (int i = 0; i < this.mInfoViewList.size(); i++) {
                ((View) this.mInfoViewList.get(i)).setVisibility(0);
            }
        }
        float f = 0.0f;
        if ((shown ? 1.0f : 0.0f) != this.mInfoAlpha) {
            float f2 = this.mInfoAlpha;
            if (shown) {
                f = 1.0f;
            }
            this.mAnim = new InfoAlphaAnimation(f2, f);
            this.mAnim.setDuration((long) this.mActivatedAnimDuration);
            this.mAnim.setInterpolator(new DecelerateInterpolator());
            this.mAnim.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (((double) BaseCardView.this.mInfoAlpha) == 0.0d) {
                        for (int i = 0; i < BaseCardView.this.mInfoViewList.size(); i++) {
                            ((View) BaseCardView.this.mInfoViewList.get(i)).setVisibility(8);
                        }
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            startAnimation(this.mAnim);
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    /* Access modifiers changed, original: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public String toString() {
        return super.toString();
    }
}
