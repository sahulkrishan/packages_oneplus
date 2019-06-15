package com.oneplus.lib.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;
import java.util.ArrayList;

public class OPPageIndicator extends ViewGroup {
    private static final long ANIMATION_DURATION = 250;
    private static final boolean DEBUG = false;
    private static final float MINOR_ALPHA = 0.3f;
    private static final float SINGLE_SCALE = 0.4f;
    private static final String TAG = "OPPageIndicator";
    private boolean mAnimating;
    private final Runnable mAnimationDone = new Runnable() {
        public void run() {
            OPPageIndicator.this.mAnimating = false;
            if (OPPageIndicator.this.mQueuedPositions.size() != 0) {
                OPPageIndicator.this.setPosition(((Integer) OPPageIndicator.this.mQueuedPositions.remove(0)).intValue());
            }
        }
    };
    private final int mPageDotWidth = ((int) (((float) this.mPageIndicatorWidth) * 0.4f));
    private final int mPageIndicatorHeight = ((int) getContext().getResources().getDimension(R.dimen.op_qs_page_indicator_height));
    private final int mPageIndicatorWidth = ((int) getContext().getResources().getDimension(R.dimen.op_qs_page_indicator_width));
    private int mPosition = -1;
    private final ArrayList<Integer> mQueuedPositions = new ArrayList();

    public OPPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNumPages(int numPages) {
        setVisibility(numPages > 1 ? 0 : 4);
        if (this.mAnimating) {
            Log.w(TAG, "setNumPages during animation");
        }
        while (numPages < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        TypedArray array = getContext().obtainStyledAttributes(new int[]{16842800});
        int color = array.getColor(0, 0);
        array.recycle();
        while (numPages > getChildCount()) {
            ImageView v = new ImageView(getContext());
            v.setImageResource(R.drawable.op_minor_a_b);
            v.setImageTintList(ColorStateList.valueOf(color));
            addView(v, new LayoutParams(this.mPageIndicatorWidth, this.mPageIndicatorHeight));
        }
        setIndex(this.mPosition >> 1);
    }

    public void setLocation(float location) {
        int index = (int) location;
        Context context = getContext();
        int i = R.string.op_accessibility_quick_settings_page;
        r3 = new Object[2];
        int i2 = 0;
        r3[0] = Integer.valueOf(index + 1);
        r3[1] = Integer.valueOf(getChildCount());
        setContentDescription(context.getString(i, r3));
        int position = index << 1;
        if (location != ((float) index)) {
            i2 = 1;
        }
        position |= i2;
        i = this.mPosition;
        if (this.mQueuedPositions.size() != 0) {
            i = ((Integer) this.mQueuedPositions.get(this.mQueuedPositions.size() - 1)).intValue();
        }
        if (position != i) {
            if (this.mAnimating) {
                this.mQueuedPositions.add(Integer.valueOf(position));
            } else {
                setPosition(position);
            }
        }
    }

    private void setPosition(int position) {
        if (Math.abs(this.mPosition - position) == 1) {
            animate(this.mPosition, position);
        } else {
            setIndex(position >> 1);
        }
        this.mPosition = position;
    }

    private void setIndex(int index) {
        int N = getChildCount();
        int i = 0;
        while (i < N) {
            ImageView v = (ImageView) getChildAt(i);
            v.setTranslationX(0.0f);
            v.setImageResource(R.drawable.op_major_a_b);
            v.setAlpha(getAlpha(i == index));
            i++;
        }
    }

    private void animate(int from, int to) {
        int fromIndex = from >> 1;
        int toIndex = to >> 1;
        setIndex(fromIndex);
        boolean fromTransition = (from & 1) != 0;
        boolean isAState = fromTransition ? from <= to : from >= to;
        int firstIndex = Math.min(fromIndex, toIndex);
        int secondIndex = Math.max(fromIndex, toIndex);
        if (secondIndex == firstIndex) {
            secondIndex++;
        }
        ImageView first = (ImageView) getChildAt(firstIndex);
        ImageView second = (ImageView) getChildAt(secondIndex);
        if (first != null && second != null) {
            second.setTranslationX(first.getX() - second.getX());
            playAnimation(first, getTransition(fromTransition, isAState, false));
            first.setAlpha(getAlpha(false));
            playAnimation(second, getTransition(fromTransition, isAState, true));
            second.setAlpha(getAlpha(true));
            this.mAnimating = true;
        }
    }

    private float getAlpha(boolean isMajor) {
        return isMajor ? 1.0f : 0.3f;
    }

    private void playAnimation(ImageView imageView, int res) {
        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getContext().getDrawable(res);
        imageView.setImageDrawable(avd);
        avd.start();
        postDelayed(this.mAnimationDone, ANIMATION_DURATION);
    }

    private int getTransition(boolean fromB, boolean isMajorAState, boolean isMajor) {
        if (isMajor) {
            if (fromB) {
                if (isMajorAState) {
                    return R.drawable.op_major_b_a_animation;
                }
                return R.drawable.op_major_b_c_animation;
            } else if (isMajorAState) {
                return R.drawable.op_major_a_b_animation;
            } else {
                return R.drawable.op_major_c_b_animation;
            }
        } else if (fromB) {
            if (isMajorAState) {
                return R.drawable.op_minor_b_c_animation;
            }
            return R.drawable.op_minor_b_a_animation;
        } else if (isMajorAState) {
            return R.drawable.op_minor_c_b_animation;
        } else {
            return R.drawable.op_minor_a_b_animation;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int N = getChildCount();
        if (N == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthChildSpec = MeasureSpec.makeMeasureSpec(this.mPageIndicatorWidth, Ints.MAX_POWER_OF_TWO);
        int heightChildSpec = MeasureSpec.makeMeasureSpec(this.mPageIndicatorHeight, Ints.MAX_POWER_OF_TWO);
        for (int i = 0; i < N; i++) {
            getChildAt(i).measure(widthChildSpec, heightChildSpec);
        }
        setMeasuredDimension(((this.mPageIndicatorWidth - this.mPageDotWidth) * (N - 1)) + this.mPageDotWidth, this.mPageIndicatorHeight);
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int N = getChildCount();
        if (N != 0) {
            for (int i = 0; i < N; i++) {
                int left = (this.mPageIndicatorWidth - this.mPageDotWidth) * i;
                getChildAt(i).layout(left, 0, this.mPageIndicatorWidth + left, this.mPageIndicatorHeight);
            }
        }
    }
}
