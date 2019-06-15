package com.oneplus.lib.widget.listview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.oneplus.commonctrl.R;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class OPListView extends ListView {
    static final String TAG = "OPListView";
    private boolean mAnimRunning;
    private ArrayList<ObjectAnimator> mAnimatorList;
    AnimatorUpdateListener mAnimatorUpdateListener;
    private DecelerateInterpolator mDecelerateInterpolator;
    AnimatorSet mDelAniSet;
    private boolean mDelAnimationFlag;
    private ArrayList<Integer> mDelOriViewTopList;
    private ArrayList<Integer> mDelPosList;
    private ArrayList<View> mDelViewList;
    private DeleteAnimationListener mDeleteAnimationListener;
    private boolean mDisableTouchEvent;
    private Drawable mDivider;
    private IOPDividerController mDividerController;
    private int mDividerHeight;
    private boolean mFooterDividersEnabled;
    private boolean mHeaderDividersEnabled;
    private boolean mInDeleteAnimation;
    private boolean mIsClipToPadding;
    private boolean mIsDisableAnimation;
    private ArrayList<View> mNowViewList;
    private int mOriBelowLeftCount;
    private int mOriCurDeleteCount;
    private int mOriCurLeftCount;
    private int mOriFirstPosition;
    private boolean mOriLastPage;
    private int mOriUpperDeleteCount;
    Rect mTempRect;

    public interface DeleteAnimationListener {
        void onAnimationEnd();

        void onAnimationStart();

        void onAnimationUpdate();
    }

    public OPListView(Context context) {
        this(context, null);
    }

    public OPListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public OPListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDividerHeight = 1;
        this.mIsDisableAnimation = true;
        this.mDelViewList = null;
        this.mDelPosList = null;
        this.mNowViewList = null;
        this.mDelOriViewTopList = null;
        this.mDelAniSet = null;
        this.mDecelerateInterpolator = new DecelerateInterpolator(1.2f);
        this.mAnimatorList = new ArrayList();
        this.mTempRect = new Rect();
        this.mHeaderDividersEnabled = true;
        this.mFooterDividersEnabled = true;
        this.mIsClipToPadding = true;
        this.mDividerController = null;
        this.mAnimatorUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                OPListView.this.invalidate();
            }
        };
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPListView, R.attr.OPListViewStyle, 0);
        Drawable d = a.getDrawable(R.styleable.OPListView_android_divider);
        Drawable bg = a.getDrawable(R.styleable.OPListView_android_background);
        if (d != null) {
            setDivider(d);
        }
        if (bg != null) {
            setBackground(bg);
        }
        this.mDividerHeight = getResources().getDimensionPixelSize(R.dimen.listview_divider_height);
        setOverScrollMode(0);
        super.setDivider(new ColorDrawable(17170445));
        setDividerHeight(this.mDividerHeight);
        setFooterDividersEnabled(false);
        a.recycle();
    }

    public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
        this.mHeaderDividersEnabled = headerDividersEnabled;
    }

    public void setFooterDividersEnabled(boolean footerDividersEnabled) {
        this.mFooterDividersEnabled = footerDividersEnabled;
    }

    /* Access modifiers changed, original: protected */
    public void dispatchDraw(Canvas canvas) {
        Canvas canvas2 = canvas;
        super.dispatchDraw(canvas);
        Drawable overscrollHeader = getOverscrollHeader();
        Drawable overscrollFooter = getOverscrollFooter();
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = getDivider() != null;
        boolean drawOverscrollHeader2;
        boolean z;
        Drawable overscrollFooter2;
        if (drawDividers || drawOverscrollHeader || drawOverscrollFooter) {
            Rect bounds = this.mTempRect;
            bounds.left = getPaddingLeft();
            bounds.right = (getRight() - getLeft()) - getPaddingRight();
            boolean count = getChildCount();
            boolean headerCount = getHeaderViewsCount();
            int itemCount = getCount();
            boolean footerLimit = itemCount - getFooterViewsCount();
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            int first = getFirstVisiblePosition();
            ListAdapter adapter = getAdapter();
            int effectivePaddingTop = 0;
            int effectivePaddingBottom = 0;
            if (isClipToPadding()) {
                effectivePaddingTop = getListPaddingTop();
                effectivePaddingBottom = getListPaddingBottom();
            }
            int effectivePaddingTop2 = effectivePaddingTop;
            int listBottom = ((getBottom() - getTop()) - effectivePaddingBottom) + getScrollY();
            int effectivePaddingTop3;
            int i;
            if (isStackFromBottom()) {
                int listBottom2;
                int scrollY;
                boolean start;
                effectivePaddingTop3 = effectivePaddingTop2;
                drawOverscrollHeader2 = drawOverscrollHeader;
                z = drawOverscrollFooter;
                i = itemCount;
                int scrollY2 = getScrollY();
                boolean start2 = drawOverscrollHeader2;
                boolean i2 = start2;
                while (i2 < count) {
                    drawOverscrollHeader = first + i2;
                    drawOverscrollFooter = drawOverscrollHeader < headerCount;
                    boolean isFooter = drawOverscrollHeader >= footerLimit;
                    if ((headerDividers || !drawOverscrollFooter) && (footerDividers || !isFooter)) {
                        listBottom2 = listBottom;
                        View child = getChildAt(i2);
                        scrollY = scrollY2;
                        scrollY2 = child.getTop();
                        if (drawDividers && shouldDrawDivider(i2)) {
                            listBottom = effectivePaddingTop3;
                            if (scrollY2 > listBottom) {
                                boolean isFirstItem = i2 == start2;
                                start = start2;
                                start2 = drawOverscrollHeader - 1;
                                if ((headerDividers || (!drawOverscrollFooter && start2 >= headerCount)) && (isFirstItem || footerDividers || (!isFooter && start2 < footerLimit))) {
                                    boolean previousIndex = start2;
                                    bounds.top = scrollY2 - getDividerHeight();
                                    bounds.bottom = scrollY2;
                                    drawDivider(canvas2, bounds, i2 - 1);
                                }
                            } else {
                                start = start2;
                            }
                        } else {
                            start = start2;
                            listBottom = effectivePaddingTop3;
                        }
                    } else {
                        scrollY = scrollY2;
                        start = start2;
                        listBottom2 = listBottom;
                        listBottom = effectivePaddingTop3;
                    }
                    i2++;
                    effectivePaddingTop3 = listBottom;
                    listBottom = listBottom2;
                    scrollY2 = scrollY;
                    start2 = start;
                }
                scrollY = scrollY2;
                start = start2;
                listBottom2 = listBottom;
                if (count <= false && scrollY > 0 && drawDividers) {
                    scrollY2 = listBottom2;
                    bounds.top = scrollY2;
                    bounds.bottom = getDividerHeight() + scrollY2;
                    drawDivider(canvas2, bounds, -1);
                }
            } else {
                int scrollY3 = getScrollY();
                if (count <= false && scrollY3 < 0 && drawDividers) {
                    bounds.bottom = 0;
                    bounds.top = -getDividerHeight();
                    drawDivider(canvas2, bounds, -1);
                }
                boolean i3 = false;
                while (i3 < count) {
                    overscrollFooter2 = overscrollFooter;
                    overscrollFooter = first + i3;
                    boolean isHeader = overscrollFooter < headerCount;
                    boolean isFooter2 = overscrollFooter >= footerLimit;
                    if ((headerDividers || !isHeader) && (footerDividers || !isFooter2)) {
                        i = itemCount;
                        View child2 = getChildAt(i3);
                        effectivePaddingTop3 = effectivePaddingTop2;
                        effectivePaddingTop2 = child2.getBottom();
                        drawOverscrollHeader2 = drawOverscrollHeader;
                        drawOverscrollHeader = i3 == count + -1;
                        if (!drawDividers || !shouldDrawDivider(i3) || child2.getHeight() <= 0 || effectivePaddingTop2 >= listBottom) {
                            z = drawOverscrollFooter;
                        } else if (drawOverscrollFooter && drawOverscrollHeader) {
                            z = drawOverscrollFooter;
                        } else {
                            z = drawOverscrollFooter;
                            drawOverscrollFooter = overscrollFooter + 1;
                            if ((headerDividers || (!isHeader && drawOverscrollFooter >= headerCount)) && (drawOverscrollHeader || footerDividers || (!isFooter2 && drawOverscrollFooter < footerLimit))) {
                                int itemIndex = overscrollFooter;
                                overscrollFooter = (int) child2.getTranslationY();
                                bounds.top = effectivePaddingTop2 + overscrollFooter;
                                bounds.bottom = (getDividerHeight() + effectivePaddingTop2) + overscrollFooter;
                                drawDivider(canvas2, bounds, i3);
                            }
                        }
                        effectivePaddingTop = effectivePaddingTop2;
                    } else {
                        effectivePaddingTop3 = effectivePaddingTop2;
                        drawOverscrollHeader2 = drawOverscrollHeader;
                        z = drawOverscrollFooter;
                        i = itemCount;
                    }
                    i3++;
                    overscrollFooter = overscrollFooter2;
                    itemCount = i;
                    effectivePaddingTop2 = effectivePaddingTop3;
                    drawOverscrollHeader = drawOverscrollHeader2;
                    drawOverscrollFooter = z;
                }
                effectivePaddingTop3 = effectivePaddingTop2;
                drawOverscrollHeader2 = drawOverscrollHeader;
                z = drawOverscrollFooter;
                i = itemCount;
            }
        } else {
            Drawable drawable = overscrollHeader;
            overscrollFooter2 = overscrollFooter;
            drawOverscrollHeader2 = drawOverscrollHeader;
            z = drawOverscrollFooter;
        }
        if (this.mDelAnimationFlag) {
            this.mDelAnimationFlag = false;
            startDelDropAnimation();
        }
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDivider(Drawable divider) {
        this.mDivider = divider;
        requestLayout();
        invalidate();
    }

    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    private boolean isClipToPadding() {
        return this.mIsClipToPadding;
    }

    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        this.mIsClipToPadding = clipToPadding;
    }

    /* Access modifiers changed, original: 0000 */
    public void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = getDivider();
        int dividerType = getDividerType(getFirstVisiblePosition() + childIndex);
        if (this.mDividerController != null) {
            if (dividerType == 1) {
                bounds.left = 0;
                bounds.right = getWidth();
            } else if (dividerType == 2) {
                bounds.left = 100;
                bounds.right = getWidth() - 32;
            }
        }
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public void setDividerController(IOPDividerController dividerController) {
        this.mDividerController = dividerController;
    }

    private int getDividerType(int position) {
        if (this.mDividerController == null) {
            return -1;
        }
        return this.mDividerController.getDividerType(position);
    }

    private boolean shouldDrawDivider(int childIndex) {
        return this.mDividerController == null || (this.mDividerController != null && getDividerType(getFirstVisiblePosition() + childIndex) > 0);
    }

    private ObjectAnimator getAnimator(int index, View child, float startValue) {
        ObjectAnimator animator;
        if (index >= this.mAnimatorList.size()) {
            PropertyValuesHolder y = PropertyValuesHolder.ofFloat("y", new float[]{startValue, (float) child.getTop()});
            animator = ObjectAnimator.ofPropertyValuesHolder(child, new PropertyValuesHolder[]{y});
            this.mAnimatorList.add(animator);
            return animator;
        }
        animator = (ObjectAnimator) this.mAnimatorList.get(index);
        animator.getValues()[0].setFloatValues(new float[]{startValue, (float) child.getTop()});
        animator.setTarget(child);
        return animator;
    }

    public void setDeleteAnimationListener(DeleteAnimationListener listener) {
        this.mDeleteAnimationListener = listener;
    }

    public void setDelPositionsList(ArrayList<Integer> deleteList) {
        int i = 0;
        if (deleteList == null) {
            this.mDisableTouchEvent = false;
            throw new InvalidParameterException("The input parameter d is null!");
        } else if (this.mAnimRunning) {
            this.mDisableTouchEvent = false;
        } else if (isDeleteAnimationEnabled()) {
            int listLength = deleteList.size();
            if (listLength == 0) {
                this.mDisableTouchEvent = false;
                return;
            }
            this.mAnimRunning = true;
            if (this.mDeleteAnimationListener != null) {
                this.mDeleteAnimationListener.onAnimationStart();
            }
            this.mInDeleteAnimation = true;
            this.mOriFirstPosition = getFirstVisiblePosition();
            int childCount = getChildCount();
            if (this.mOriFirstPosition + childCount == getAdapter().getCount() + listLength) {
                this.mOriLastPage = true;
            } else {
                this.mOriLastPage = false;
            }
            this.mOriUpperDeleteCount = 0;
            this.mOriCurDeleteCount = 0;
            this.mOriCurLeftCount = 0;
            this.mOriBelowLeftCount = 0;
            if (this.mDelOriViewTopList == null) {
                this.mDelOriViewTopList = new ArrayList();
            } else {
                this.mDelOriViewTopList.clear();
            }
            if (this.mDelViewList == null) {
                this.mDelViewList = new ArrayList();
            } else {
                this.mDelViewList.clear();
            }
            if (this.mDelPosList == null) {
                this.mDelPosList = new ArrayList();
            } else {
                this.mDelPosList.clear();
            }
            int belowDeleteCount = 0;
            int delPos = 0;
            for (int i2 = 0; i2 < listLength; i2++) {
                delPos = ((Integer) deleteList.get(i2)).intValue();
                if (delPos < this.mOriFirstPosition) {
                    this.mOriUpperDeleteCount++;
                } else if (delPos < this.mOriFirstPosition + childCount) {
                    this.mDelPosList.add(Integer.valueOf(delPos));
                    this.mDelViewList.add(getChildAt(delPos - this.mOriFirstPosition));
                    this.mOriCurDeleteCount++;
                } else {
                    belowDeleteCount++;
                }
            }
            boolean isDel = false;
            if (this.mOriUpperDeleteCount > 0 || this.mDelPosList.size() > 0) {
                isDel = true;
            }
            if (isDel) {
                int size = this.mDelPosList.size();
                while (i < childCount) {
                    View child;
                    if (size > 0) {
                        if (!this.mDelPosList.contains(Integer.valueOf(this.mOriFirstPosition + i))) {
                            child = getChildAt(i);
                            if (child != null) {
                                this.mDelOriViewTopList.add(Integer.valueOf(child.getTop()));
                            }
                        }
                    } else {
                        child = getChildAt(i);
                        if (child != null) {
                            this.mDelOriViewTopList.add(Integer.valueOf(child.getTop()));
                        }
                    }
                    i++;
                }
                this.mOriCurLeftCount = getChildCount() - this.mOriCurDeleteCount;
                this.mOriBelowLeftCount = (((getAdapter().getCount() + listLength) - getLastVisiblePosition()) - 1) - belowDeleteCount;
                startDelGoneAnimation();
            } else {
                this.mAnimRunning = false;
                this.mInDeleteAnimation = false;
                this.mDisableTouchEvent = false;
                if (this.mDeleteAnimationListener != null) {
                    this.mDeleteAnimationListener.onAnimationUpdate();
                    this.mDeleteAnimationListener.onAnimationEnd();
                }
            }
        } else {
            if (this.mDeleteAnimationListener != null) {
                this.mDeleteAnimationListener.onAnimationUpdate();
                this.mDeleteAnimationListener.onAnimationStart();
                this.mDeleteAnimationListener.onAnimationEnd();
            }
            this.mDisableTouchEvent = false;
        }
    }

    private void startDelGoneAnimation() {
        this.mAnimRunning = true;
        int size = this.mDelViewList.size();
        if (size == 0) {
            this.mDelAnimationFlag = true;
            if (this.mDeleteAnimationListener != null) {
                this.mDeleteAnimationListener.onAnimationUpdate();
            }
            this.mDisableTouchEvent = false;
            return;
        }
        this.mDelAniSet = new AnimatorSet();
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", new float[]{1.0f, 0.0f});
        for (int i = 0; i < size; i++) {
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder((View) this.mDelViewList.get(i), new PropertyValuesHolder[]{pvhAlpha});
            anim.setDuration((long) 200);
            anim.setInterpolator(this.mDecelerateInterpolator);
            anim.addUpdateListener(this.mAnimatorUpdateListener);
            this.mDelAniSet.playTogether(new Animator[]{anim});
        }
        this.mDelAniSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                int size = OPListView.this.mDelViewList.size();
                View child = null;
                for (int i = 0; i < size; i++) {
                    ((View) OPListView.this.mDelViewList.get(i)).setAlpha(1.0f);
                }
                if (OPListView.this.getAdapter() == null || (OPListView.this.getAdapter().getCount() != 0 && (OPListView.this.getEmptyView() == null || !OPListView.this.getAdapter().isEmpty()))) {
                    OPListView.this.mDelAnimationFlag = true;
                    if (OPListView.this.mDeleteAnimationListener != null) {
                        OPListView.this.mDeleteAnimationListener.onAnimationUpdate();
                        return;
                    }
                    return;
                }
                OPListView.this.mAnimRunning = false;
                OPListView.this.mInDeleteAnimation = false;
                OPListView.this.mDisableTouchEvent = false;
                OPListView.this.mDelPosList.clear();
                OPListView.this.mDelOriViewTopList.clear();
                OPListView.this.mDelViewList.clear();
                if (OPListView.this.mDeleteAnimationListener != null) {
                    OPListView.this.mDeleteAnimationListener.onAnimationUpdate();
                    OPListView.this.mDeleteAnimationListener.onAnimationEnd();
                }
            }
        });
        this.mDelAniSet.start();
    }

    private void startDelDropAnimation() {
        this.mDelAniSet = new AnimatorSet();
        setDelViewLocation();
        int time = 200;
        for (int i = 0; i < this.mNowViewList.size(); i++) {
            ObjectAnimator anim = getAnimator(i, (View) this.mNowViewList.get(i), (float) ((Integer) this.mDelOriViewTopList.get(i)).intValue());
            anim.setDuration((long) 200);
            anim.setInterpolator(this.mDecelerateInterpolator);
            anim.addUpdateListener(this.mAnimatorUpdateListener);
            this.mDelAniSet.playTogether(new Animator[]{anim});
        }
        this.mDelAniSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                OPListView.this.mAnimRunning = false;
                OPListView.this.mInDeleteAnimation = false;
                OPListView.this.mDisableTouchEvent = false;
                OPListView.this.mDelPosList.clear();
                OPListView.this.mDelOriViewTopList.clear();
                OPListView.this.mDelViewList.clear();
                OPListView.this.mNowViewList.clear();
                OPListView.this.invalidate();
                if (OPListView.this.mDeleteAnimationListener != null) {
                    OPListView.this.mDeleteAnimationListener.onAnimationEnd();
                }
            }
        });
        this.mDelAniSet.start();
    }

    private void setDelViewLocation() {
        int i;
        int i2;
        int i3;
        int diff;
        int nowFirstPosition = getFirstVisiblePosition();
        int nowCurChildCount = getChildCount();
        boolean nowLastPage = false;
        if (getLastVisiblePosition() == getAdapter().getCount() - 1) {
            nowLastPage = true;
        }
        boolean nowFirstPage = false;
        if (nowFirstPosition == 0) {
            nowFirstPage = true;
        }
        int top = getTop();
        int bottom = getBottom();
        int childCount = getChildCount();
        if (this.mNowViewList == null) {
            this.mNowViewList = new ArrayList();
        } else {
            this.mNowViewList.clear();
        }
        int height = 0;
        View child = null;
        for (i = 0; i < childCount; i++) {
            child = getChildAt(i);
            this.mNowViewList.add(child);
            if (i == 0 && child != null) {
                height = child.getHeight();
            }
        }
        if (this.mOriLastPage) {
            if (this.mOriUpperDeleteCount == 0) {
                if (this.mOriCurDeleteCount != 0) {
                    Log.d(TAG, "DeleteAnimation Case 14 ");
                }
            } else if (this.mOriCurDeleteCount == 0) {
                if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                    Log.d(TAG, "DeleteAnimation Case 12 ");
                    this.mDelOriViewTopList.clear();
                } else {
                    Log.d(TAG, "DeleteAnimation Case 13 ");
                    for (i2 = 0; i2 < this.mOriUpperDeleteCount; i2++) {
                        this.mDelOriViewTopList.remove(0);
                    }
                }
            } else if (nowFirstPage) {
                Log.d(TAG, "DeleteAnimation Case 17 ");
            } else if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                Log.d(TAG, "DeleteAnimation Case 15 ");
            } else {
                Log.d(TAG, "DeleteAnimation Case 16 ");
            }
            i = 1;
            while (nowCurChildCount > this.mDelOriViewTopList.size()) {
                this.mDelOriViewTopList.add(0, Integer.valueOf((-height) * i));
                i++;
            }
        } else if (nowLastPage) {
            if (nowFirstPage) {
                if (this.mOriCurDeleteCount == 0) {
                    Log.d(TAG, "DeleteAnimation Case 11 ");
                } else if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                    Log.d(TAG, "DeleteAnimation Case 7 ");
                } else {
                    Log.d(TAG, "DeleteAnimation Case 8 ");
                }
            } else if (this.mOriUpperDeleteCount == 0) {
                Log.d(TAG, "DeleteAnimation Case 4 ");
            } else if (this.mOriCurDeleteCount == 0) {
                if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                    Log.d(TAG, "DeleteAnimation Case 9 ");
                } else {
                    Log.d(TAG, "DeleteAnimation Case 10 ");
                }
            } else if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                Log.d(TAG, "DeleteAnimation Case 5 ");
            } else {
                Log.d(TAG, "DeleteAnimation Case 6 ");
            }
            for (i3 = 0; i3 < this.mOriBelowLeftCount; i3++) {
                this.mDelOriViewTopList.add(Integer.valueOf(bottom + ((i3 + 1) * height)));
            }
            diff = this.mDelOriViewTopList.size() - nowCurChildCount;
            for (i2 = 0; i2 < diff; i2++) {
                this.mDelOriViewTopList.remove(0);
            }
            i = 1;
            while (nowCurChildCount > this.mDelOriViewTopList.size()) {
                this.mDelOriViewTopList.add(0, Integer.valueOf((-height) * i));
                i++;
            }
        } else if (this.mOriUpperDeleteCount == 0) {
            Log.d(TAG, "DeleteAnimation Case 1");
        } else if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
            Log.d(TAG, "DeleteAnimation Case 3 ");
            this.mDelOriViewTopList.clear();
        } else {
            Log.d(TAG, "DeleteAnimation Case 2 ");
            for (i3 = 0; i3 < this.mOriUpperDeleteCount; i3++) {
                this.mDelOriViewTopList.remove(0);
            }
        }
        i2 = this.mNowViewList.size() - this.mDelOriViewTopList.size();
        for (diff = 0; diff < i2; diff++) {
            this.mDelOriViewTopList.add(Integer.valueOf(((diff + 1) * height) + bottom));
        }
        diff = 0;
        for (i3 = childCount - 1; i3 >= 0; i3--) {
            if (((View) this.mNowViewList.get(i3)).getTop() == ((Integer) this.mDelOriViewTopList.get(i3)).intValue()) {
                this.mNowViewList.remove(i3);
                this.mDelOriViewTopList.remove(i3);
            } else if (((Integer) this.mDelOriViewTopList.get(i3)).intValue() < ((View) this.mNowViewList.get(i3)).getTop()) {
                diff++;
            }
        }
        int nowCurChildCount2;
        boolean nowLastPage2;
        if (diff > 1) {
            ArrayList<View> tmpViewList = (ArrayList) this.mNowViewList.clone();
            ArrayList<Integer> tmpOriTopList = (ArrayList) this.mDelOriViewTopList.clone();
            this.mNowViewList.clear();
            this.mDelOriViewTopList.clear();
            int i4 = 0;
            while (true) {
                int nowFirstPosition2 = nowFirstPosition;
                nowCurChildCount2 = nowCurChildCount;
                nowCurChildCount = i4;
                if (nowCurChildCount < tmpViewList.size()) {
                    if (nowCurChildCount < diff) {
                        nowFirstPosition = (diff - nowCurChildCount) - 1;
                    } else {
                        nowFirstPosition = nowCurChildCount;
                    }
                    int tmpPos = nowFirstPosition;
                    nowLastPage2 = nowLastPage;
                    this.mNowViewList.add((View) tmpViewList.get(tmpPos));
                    this.mDelOriViewTopList.add((Integer) tmpOriTopList.get(tmpPos));
                    i4 = nowCurChildCount + 1;
                    nowFirstPosition = nowFirstPosition2;
                    nowCurChildCount = nowCurChildCount2;
                    nowLastPage = nowLastPage2;
                } else {
                    return;
                }
            }
        }
        nowCurChildCount2 = nowCurChildCount;
        nowLastPage2 = nowLastPage;
    }

    public void setDeleteAnimationEnabled(boolean enabled) {
        this.mIsDisableAnimation = enabled;
    }

    public boolean isDeleteAnimationEnabled() {
        return this.mIsDisableAnimation;
    }
}
