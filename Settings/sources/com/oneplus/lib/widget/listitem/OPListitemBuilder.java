package com.oneplus.lib.widget.listitem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;

public final class OPListitemBuilder {
    private static final boolean DEBUG = false;
    private static final String TAG = "OPListitem";
    private boolean mActionButtonEnabled = false;
    private Context mContext = null;
    private boolean mIconEnabled = false;
    private boolean mPrimaryTextEnabled = false;
    private boolean mSecondaryTextEnabled = false;
    private boolean mStampEnabled = false;

    private class OPListitemImpl extends OPListitem {
        private int mActionBtnSize = -1;
        private ImageView mActionButton = null;
        private Context mContext = null;
        private ImageView mIcon = null;
        private int mIconSize = -1;
        private int mMarginM1 = 0;
        private TextView mPrimaryText = null;
        private int mRemainHeight = 0;
        private Resources mResources = null;
        private TextView mSecondaryText = null;
        private TextView mStamp = null;

        public OPListitemImpl(Context context) {
            super(context);
            this.mContext = context;
            init();
        }

        private void init() {
            if (this.mContext != null) {
                this.mResources = this.mContext.getResources();
                this.mMarginM1 = this.mResources.getDimensionPixelOffset(R.dimen.margin_m1);
                if (OPListitemBuilder.this.mIconEnabled) {
                    this.mIcon = new ImageView(this.mContext);
                    this.mIconSize = this.mResources.getDimensionPixelOffset(R.dimen.listitem_icon_size);
                    this.mIcon.setLayoutParams(new LayoutParams(this.mIconSize, this.mIconSize));
                    addView(this.mIcon);
                }
                if (OPListitemBuilder.this.mPrimaryTextEnabled) {
                    this.mPrimaryText = new TextView(this.mContext, null, 0, R.style.listitem_primary_text_font);
                    this.mPrimaryText.setLayoutParams(new LayoutParams(-2, -2));
                    addView(this.mPrimaryText);
                }
                if (OPListitemBuilder.this.mSecondaryTextEnabled) {
                    this.mSecondaryText = new TextView(this.mContext, null, 0, R.style.listitem_secondary_text_font);
                    this.mSecondaryText.setLayoutParams(new LayoutParams(-2, -2));
                    addView(this.mSecondaryText);
                }
                if (OPListitemBuilder.this.mStampEnabled) {
                    this.mStamp = new TextView(this.mContext, null, 0, R.style.listitem_stamp_font);
                    this.mStamp.setLayoutParams(new LayoutParams(-2, -2));
                    addView(this.mStamp);
                }
                if (OPListitemBuilder.this.mActionButtonEnabled) {
                    this.mActionButton = new ImageView(this.mContext);
                    this.mActionBtnSize = this.mResources.getDimensionPixelOffset(R.dimen.listitem_actionbutton_size);
                    this.mActionButton.setLayoutParams(new LayoutParams(this.mActionBtnSize, this.mActionBtnSize));
                    addView(this.mActionButton);
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
            int itemWidth = MeasureSpec.getSize(widthMeasureSpec);
            int itemHeight = MeasureSpec.getSize(heightMeasureSpec);
            int remaindWidth = itemWidth;
            int remainHeight = itemHeight;
            if (this.mIcon != null) {
                this.mIcon.measure(MeasureSpec.makeMeasureSpec(this.mIconSize, Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec(this.mIconSize, Ints.MAX_POWER_OF_TWO));
                remaindWidth = (remaindWidth - this.mIconSize) - this.mMarginM1;
            }
            if (this.mActionButton != null) {
                this.mActionButton.measure(MeasureSpec.makeMeasureSpec(this.mActionBtnSize, Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec(this.mActionBtnSize, Ints.MAX_POWER_OF_TWO));
                remaindWidth = (remaindWidth - this.mActionBtnSize) - this.mMarginM1;
            }
            if (this.mStamp != null) {
                this.mStamp.measure(MeasureSpec.makeMeasureSpec(remaindWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(itemHeight, Integer.MIN_VALUE));
                remaindWidth = (remaindWidth - this.mStamp.getMeasuredWidth()) - this.mMarginM1;
            }
            if (this.mPrimaryText != null) {
                this.mPrimaryText.measure(MeasureSpec.makeMeasureSpec(remaindWidth - (this.mMarginM1 * 2), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(remainHeight, Integer.MIN_VALUE));
                remainHeight -= this.mPrimaryText.getMeasuredHeight();
            }
            if (this.mSecondaryText != null) {
                if (this.mStamp != null) {
                    remaindWidth += this.mStamp.getMeasuredWidth();
                }
                this.mSecondaryText.measure(MeasureSpec.makeMeasureSpec(remaindWidth - (2 * this.mMarginM1), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(remainHeight, Integer.MIN_VALUE));
                remainHeight -= this.mSecondaryText.getMeasuredHeight();
            }
            this.mRemainHeight = remainHeight;
        }

        /* Access modifiers changed, original: protected */
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            layoutLTR(l, t, r, b);
        }

        /* Access modifiers changed, original: protected */
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
        }

        private void layoutLTR(int l, int t, int r, int b) {
            int left;
            int top;
            int right;
            int itemheight = b - t;
            int currentLeft = l;
            if (this.mIcon != null) {
                left = this.mMarginM1 + l;
                top = (itemheight - this.mIconSize) / 2;
                right = this.mIconSize + left;
                this.mIcon.layout(left, top, right, this.mIconSize + top);
                currentLeft = right;
            }
            if (this.mActionButton != null) {
                left = r - this.mMarginM1;
                right = (itemheight - this.mActionBtnSize) / 2;
                this.mActionButton.layout(left - this.mActionBtnSize, right, left, this.mActionBtnSize + right);
            }
            if (this.mSecondaryText != null) {
                left = itemheight - (this.mRemainHeight / 2);
                right = this.mMarginM1 + currentLeft;
                this.mSecondaryText.layout(right, left - this.mSecondaryText.getMeasuredHeight(), this.mSecondaryText.getMeasuredWidth() + right, left);
            }
            if (this.mPrimaryText != null) {
                left = this.mMarginM1 + currentLeft;
                right = this.mRemainHeight / 2;
                this.mPrimaryText.layout(left, right, this.mPrimaryText.getMeasuredWidth() + left, this.mPrimaryText.getMeasuredHeight() + right);
            }
            if (this.mStamp != null) {
                left = r - this.mMarginM1;
                top = left - this.mStamp.getMeasuredWidth();
                if (this.mSecondaryText != null) {
                    right = (this.mRemainHeight / 2) + ((this.mPrimaryText.getMeasuredHeight() - this.mStamp.getMeasuredHeight()) / 2);
                } else {
                    right = (itemheight - this.mStamp.getMeasuredHeight()) / 2;
                }
                this.mStamp.layout(top, right, left, this.mStamp.getMeasuredHeight() + right);
            }
        }

        public TextView getPrimaryText() {
            return this.mPrimaryText;
        }

        public TextView getSecondaryText() {
            return this.mSecondaryText;
        }

        public TextView getStamp() {
            return this.mStamp;
        }

        public ImageView getIcon() {
            return this.mIcon;
        }

        public ImageView getActionButton() {
            return this.mActionButton;
        }
    }

    public OPListitemBuilder(Context context) {
        this.mContext = context;
    }

    public OPListitem build() {
        OPListitemImpl item = new OPListitemImpl(this.mContext);
        item.setLayoutParams(new AbsListView.LayoutParams(-1, 216));
        return item;
    }

    public OPListitemBuilder setIconEnabled(boolean enabled) {
        this.mIconEnabled = enabled;
        return this;
    }

    public OPListitemBuilder setPrimaryTextEnabled(boolean enabled) {
        this.mPrimaryTextEnabled = enabled;
        return this;
    }

    public OPListitemBuilder setSecondaryTextEnabled(boolean enabled) {
        this.mSecondaryTextEnabled = enabled;
        return this;
    }

    public OPListitemBuilder setStampEnabled(boolean enabled) {
        this.mStampEnabled = enabled;
        this.mActionButtonEnabled = enabled ^ 1;
        return this;
    }

    public OPListitemBuilder setActionButtonEnabled(boolean enabled) {
        this.mActionButtonEnabled = enabled;
        this.mStampEnabled = enabled ^ 1;
        return this;
    }

    public OPListitemBuilder reset() {
        this.mIconEnabled = false;
        this.mPrimaryTextEnabled = false;
        this.mSecondaryTextEnabled = false;
        this.mStampEnabled = false;
        this.mActionButtonEnabled = false;
        return this;
    }
}
