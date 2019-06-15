package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

@RestrictTo({Scope.LIBRARY_GROUP})
public class ThumbsBar extends LinearLayout {
    final SparseArray<Bitmap> mBitmaps;
    int mHeroThumbHeightInPixel;
    int mHeroThumbWidthInPixel;
    private boolean mIsUserSets;
    int mMeasuredMarginInPixel;
    int mNumOfThumbs;
    int mThumbHeightInPixel;
    int mThumbWidthInPixel;

    public ThumbsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbsBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mNumOfThumbs = -1;
        this.mBitmaps = new SparseArray();
        this.mIsUserSets = false;
        this.mThumbWidthInPixel = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_transport_thumbs_width);
        this.mThumbHeightInPixel = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_transport_thumbs_height);
        this.mHeroThumbHeightInPixel = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_transport_hero_thumbs_width);
        this.mHeroThumbWidthInPixel = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_transport_hero_thumbs_height);
        this.mMeasuredMarginInPixel = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_transport_thumbs_margin);
    }

    public int getHeroIndex() {
        return getChildCount() / 2;
    }

    public void setThumbSize(int width, int height) {
        this.mThumbHeightInPixel = height;
        this.mThumbWidthInPixel = width;
        int heroIndex = getHeroIndex();
        for (int i = 0; i < getChildCount(); i++) {
            if (heroIndex != i) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                boolean changed = false;
                if (lp.height != height) {
                    lp.height = height;
                    changed = true;
                }
                if (lp.width != width) {
                    lp.width = width;
                    changed = true;
                }
                if (changed) {
                    child.setLayoutParams(lp);
                }
            }
        }
    }

    public void setHeroThumbSize(int width, int height) {
        this.mHeroThumbHeightInPixel = height;
        this.mHeroThumbWidthInPixel = width;
        int heroIndex = getHeroIndex();
        for (int i = 0; i < getChildCount(); i++) {
            if (heroIndex == i) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                boolean changed = false;
                if (lp.height != height) {
                    lp.height = height;
                    changed = true;
                }
                if (lp.width != width) {
                    lp.width = width;
                    changed = true;
                }
                if (changed) {
                    child.setLayoutParams(lp);
                }
            }
        }
    }

    public void setThumbSpace(int spaceInPixel) {
        this.mMeasuredMarginInPixel = spaceInPixel;
        requestLayout();
    }

    public void setNumberOfThumbs(int numOfThumbs) {
        this.mIsUserSets = true;
        this.mNumOfThumbs = numOfThumbs;
        setNumberOfThumbsInternal();
    }

    private void setNumberOfThumbsInternal() {
        while (getChildCount() > this.mNumOfThumbs) {
            removeView(getChildAt(getChildCount() - 1));
        }
        while (getChildCount() < this.mNumOfThumbs) {
            addView(createThumbView(this), new LayoutParams(this.mThumbWidthInPixel, this.mThumbHeightInPixel));
        }
        int heroIndex = getHeroIndex();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (heroIndex == i) {
                lp.width = this.mHeroThumbWidthInPixel;
                lp.height = this.mHeroThumbHeightInPixel;
            } else {
                lp.width = this.mThumbWidthInPixel;
                lp.height = this.mThumbHeightInPixel;
            }
            child.setLayoutParams(lp);
        }
    }

    private static int roundUp(int num, int divisor) {
        return ((num + divisor) - 1) / divisor;
    }

    private int calculateNumOfThumbs(int widthInPixel) {
        int nonHeroThumbNum = roundUp(widthInPixel - this.mHeroThumbWidthInPixel, this.mThumbWidthInPixel + this.mMeasuredMarginInPixel);
        if (nonHeroThumbNum < 2) {
            nonHeroThumbNum = 2;
        } else if ((nonHeroThumbNum & 1) != 0) {
            nonHeroThumbNum++;
        }
        return nonHeroThumbNum + 1;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        if (!this.mIsUserSets) {
            int numOfThumbs = calculateNumOfThumbs(width);
            if (this.mNumOfThumbs != numOfThumbs) {
                this.mNumOfThumbs = numOfThumbs;
                setNumberOfThumbsInternal();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        View child;
        super.onLayout(changed, l, t, r, b);
        int heroIndex = getHeroIndex();
        View heroView = getChildAt(heroIndex);
        int heroLeft = (getWidth() / 2) - (heroView.getMeasuredWidth() / 2);
        int heroRight = (getWidth() / 2) + (heroView.getMeasuredWidth() / 2);
        heroView.layout(heroLeft, getPaddingTop(), heroRight, getPaddingTop() + heroView.getMeasuredHeight());
        int heroCenter = getPaddingTop() + (heroView.getMeasuredHeight() / 2);
        for (i = heroIndex - 1; i >= 0; i--) {
            heroLeft -= this.mMeasuredMarginInPixel;
            child = getChildAt(i);
            child.layout(heroLeft - child.getMeasuredWidth(), heroCenter - (child.getMeasuredHeight() / 2), heroLeft, (child.getMeasuredHeight() / 2) + heroCenter);
            heroLeft -= child.getMeasuredWidth();
        }
        for (i = heroIndex + 1; i < this.mNumOfThumbs; i++) {
            heroRight += this.mMeasuredMarginInPixel;
            child = getChildAt(i);
            child.layout(heroRight, heroCenter - (child.getMeasuredHeight() / 2), child.getMeasuredWidth() + heroRight, (child.getMeasuredHeight() / 2) + heroCenter);
            heroRight += child.getMeasuredWidth();
        }
    }

    /* Access modifiers changed, original: protected */
    public View createThumbView(ViewGroup parent) {
        return new ImageView(parent.getContext());
    }

    public void clearThumbBitmaps() {
        for (int i = 0; i < getChildCount(); i++) {
            setThumbBitmap(i, null);
        }
        this.mBitmaps.clear();
    }

    public Bitmap getThumbBitmap(int index) {
        return (Bitmap) this.mBitmaps.get(index);
    }

    public void setThumbBitmap(int index, Bitmap bitmap) {
        this.mBitmaps.put(index, bitmap);
        ((ImageView) getChildAt(index)).setImageBitmap(bitmap);
    }
}
