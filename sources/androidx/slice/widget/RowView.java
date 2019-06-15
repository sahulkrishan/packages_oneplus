package androidx.slice.widget;

import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceHints;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import androidx.slice.widget.SliceView.OnSliceActionListener;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import com.google.common.primitives.Ints;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class RowView extends SliceChildView implements OnClickListener {
    private static final int MAX_END_ITEMS = 3;
    private static final String TAG = "RowView";
    private LinearLayout mContent;
    private View mDivider;
    private LinearLayout mEndContainer;
    private List<SliceAction> mHeaderActions;
    private int mIconSize = getContext().getResources().getDimensionPixelSize(R.dimen.abc_slice_icon_size);
    private int mImageSize = getContext().getResources().getDimensionPixelSize(R.dimen.abc_slice_small_image_size);
    private boolean mIsHeader;
    private boolean mIsSingleItem;
    private TextView mLastUpdatedText;
    private TextView mPrimaryText;
    private ProgressBar mRangeBar;
    private int mRangeHeight;
    private LinearLayout mRootView;
    private SliceActionImpl mRowAction;
    private RowContent mRowContent;
    private int mRowIndex;
    private TextView mSecondaryText;
    private View mSeeMoreView;
    private LinearLayout mStartContainer;
    private ArrayMap<SliceActionImpl, SliceActionView> mToggles = new ArrayMap();

    public RowView(Context context) {
        super(context);
        this.mRootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.abc_slice_small_template, this, false);
        addView(this.mRootView);
        this.mStartContainer = (LinearLayout) findViewById(R.id.icon_frame);
        this.mContent = (LinearLayout) findViewById(16908290);
        this.mPrimaryText = (TextView) findViewById(16908310);
        this.mSecondaryText = (TextView) findViewById(16908304);
        this.mLastUpdatedText = (TextView) findViewById(R.id.last_updated);
        this.mDivider = findViewById(R.id.divider);
        this.mEndContainer = (LinearLayout) findViewById(16908312);
        this.mRangeHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_row_range_height);
    }

    public void setSingleItem(boolean isSingleItem) {
        this.mIsSingleItem = isSingleItem;
    }

    public int getSmallHeight() {
        return (this.mRowContent == null || !this.mRowContent.isValid()) ? 0 : this.mRowContent.getSmallHeight();
    }

    public int getActualHeight() {
        if (this.mIsSingleItem) {
            return getSmallHeight();
        }
        int actualHeight = (this.mRowContent == null || !this.mRowContent.isValid()) ? 0 : this.mRowContent.getActualHeight();
        return actualHeight;
    }

    private int getRowContentHeight() {
        int rowHeight;
        if (getMode() == 1 || this.mIsSingleItem) {
            rowHeight = getSmallHeight();
        } else {
            rowHeight = getActualHeight();
        }
        if (this.mRangeBar != null) {
            return rowHeight - this.mRangeHeight;
        }
        return rowHeight;
    }

    public void setTint(@ColorInt int tintColor) {
        super.setTint(tintColor);
        if (this.mRowContent != null) {
            populateViews();
        }
    }

    public void setSliceActions(List<SliceAction> actions) {
        this.mHeaderActions = actions;
        if (this.mRowContent != null) {
            populateViews();
        }
    }

    public void setShowLastUpdated(boolean showLastUpdated) {
        super.setShowLastUpdated(showLastUpdated);
        if (this.mRowContent != null) {
            populateViews();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHeight = getMode() == 1 ? getSmallHeight() : getActualHeight();
        int rowHeight = getRowContentHeight();
        if (rowHeight != 0) {
            this.mRootView.setVisibility(0);
            measureChild(this.mRootView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(rowHeight, Ints.MAX_POWER_OF_TWO));
        } else {
            this.mRootView.setVisibility(8);
        }
        if (this.mRangeBar != null) {
            measureChild(this.mRangeBar, widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mRangeHeight, Ints.MAX_POWER_OF_TWO));
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, Ints.MAX_POWER_OF_TWO));
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mRootView.layout(0, 0, this.mRootView.getMeasuredWidth(), getRowContentHeight());
        if (this.mRangeBar != null) {
            this.mRangeBar.layout(0, getRowContentHeight(), this.mRangeBar.getMeasuredWidth(), getRowContentHeight() + this.mRangeHeight);
        }
    }

    public void setSliceItem(SliceItem slice, boolean isHeader, int index, int rowCount, OnSliceActionListener observer) {
        setSliceActionListener(observer);
        this.mRowIndex = index;
        this.mIsHeader = ListContent.isValidHeader(slice);
        this.mHeaderActions = null;
        this.mRowContent = new RowContent(getContext(), slice, this.mIsHeader);
        populateViews();
    }

    private void populateViews() {
        resetView();
        if (this.mRowContent.getLayoutDirItem() != null) {
            setLayoutDirection(this.mRowContent.getLayoutDirItem().getInt());
        }
        if (this.mRowContent.isDefaultSeeMore()) {
            showSeeMore();
            return;
        }
        SliceItem subtitleItem;
        CharSequence contentDescr = this.mRowContent.getContentDescription();
        if (contentDescr != null) {
            this.mContent.setContentDescription(contentDescr);
        }
        SliceItem startItem = this.mRowContent.getStartItem();
        boolean z = false;
        boolean showStart = startItem != null && this.mRowIndex > 0;
        if (showStart) {
            showStart = addItem(startItem, this.mTintColor, true);
        }
        this.mStartContainer.setVisibility(showStart ? 0 : 8);
        SliceItem titleItem = this.mRowContent.getTitleItem();
        if (titleItem != null) {
            this.mPrimaryText.setText(titleItem.getText());
        }
        this.mPrimaryText.setTextSize(0, (float) (this.mIsHeader ? this.mHeaderTitleSize : this.mTitleSize));
        this.mPrimaryText.setTextColor(this.mTitleColor);
        this.mPrimaryText.setVisibility(titleItem != null ? 0 : 8);
        if (getMode() == 1) {
            subtitleItem = this.mRowContent.getSummaryItem();
        } else {
            subtitleItem = this.mRowContent.getSubtitleItem();
        }
        addSubtitle(subtitleItem);
        SliceItem primaryAction = this.mRowContent.getPrimaryAction();
        if (!(primaryAction == null || primaryAction == startItem)) {
            this.mRowAction = new SliceActionImpl(primaryAction);
            if (this.mRowAction.isToggle()) {
                addAction(this.mRowAction, this.mTintColor, this.mEndContainer, false);
                setViewClickable(this.mRootView, true);
                return;
            }
        }
        SliceItem range = this.mRowContent.getRange();
        if (range != null) {
            if (this.mRowAction != null) {
                setViewClickable(this.mRootView, true);
            }
            addRange(range);
            return;
        }
        List endItems = this.mRowContent.getEndItems();
        if (this.mHeaderActions != null && this.mHeaderActions.size() > 0) {
            endItems = this.mHeaderActions;
        }
        boolean firstItemIsADefaultToggle = false;
        SliceItem endAction = null;
        int endItemCount = 0;
        int i = 0;
        while (i < endItems.size()) {
            SliceItem endItem;
            if (endItems.get(i) instanceof SliceItem) {
                endItem = (SliceItem) endItems.get(i);
            } else {
                endItem = ((SliceActionImpl) endItems.get(i)).getSliceItem();
            }
            if (endItemCount < 3 && addItem(endItem, this.mTintColor, z)) {
                if (endAction == null && SliceQuery.find(endItem, "action") != null) {
                    endAction = endItem;
                }
                endItemCount++;
                if (endItemCount == 1) {
                    z = !this.mToggles.isEmpty() && SliceQuery.find(endItem.getSlice(), "image") == null;
                    firstItemIsADefaultToggle = z;
                }
            }
            i++;
            z = false;
        }
        View view = this.mDivider;
        int i2 = (this.mRowAction == null || !firstItemIsADefaultToggle) ? 8 : 0;
        view.setVisibility(i2);
        z = (startItem == null || SliceQuery.find(startItem, "action") == null) ? false : true;
        boolean hasEndItemAction = endAction != null;
        if (this.mRowAction != null) {
            View view2 = (hasEndItemAction || z) ? this.mContent : this.mRootView;
            setViewClickable(view2, true);
        } else if (hasEndItemAction != z && (endItemCount == 1 || z)) {
            if (this.mToggles.isEmpty()) {
                this.mRowAction = new SliceActionImpl(endAction != null ? endAction : startItem);
            } else {
                this.mRowAction = (SliceActionImpl) this.mToggles.keySet().iterator().next();
            }
            setViewClickable(this.mRootView, true);
        }
    }

    private void addSubtitle(SliceItem subtitleItem) {
        CharSequence subtitleTimeString = null;
        boolean subtitleExists = true;
        int i = 0;
        if (this.mShowLastUpdated && this.mLastUpdated != -1) {
            subtitleTimeString = getResources().getString(R.string.abc_slice_updated, new Object[]{SliceViewUtil.getRelativeTimeString(this.mLastUpdated)});
        }
        CharSequence subtitle = subtitleItem != null ? subtitleItem.getText() : null;
        if (TextUtils.isEmpty(subtitle) && (subtitleItem == null || !subtitleItem.hasHint("partial"))) {
            subtitleExists = false;
        }
        if (subtitleExists) {
            this.mSecondaryText.setText(subtitle);
            this.mSecondaryText.setTextSize(0, (float) (this.mIsHeader ? this.mHeaderSubtitleSize : this.mSubtitleSize));
            this.mSecondaryText.setTextColor(this.mSubtitleColor);
            this.mSecondaryText.setPadding(0, this.mIsHeader ? this.mVerticalHeaderTextPadding : this.mVerticalTextPadding, 0, 0);
        }
        if (subtitleTimeString != null) {
            if (!TextUtils.isEmpty(subtitle)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(" · ");
                stringBuilder.append(subtitleTimeString);
                subtitleTimeString = stringBuilder.toString();
            }
            SpannableString sp = new SpannableString(subtitleTimeString);
            sp.setSpan(new StyleSpan(2), 0, subtitleTimeString.length(), 0);
            this.mLastUpdatedText.setText(sp);
            this.mLastUpdatedText.setTextSize(0, (float) (this.mIsHeader ? this.mHeaderSubtitleSize : this.mSubtitleSize));
            this.mLastUpdatedText.setTextColor(this.mSubtitleColor);
        }
        this.mLastUpdatedText.setVisibility(TextUtils.isEmpty(subtitleTimeString) ? 8 : 0);
        TextView textView = this.mSecondaryText;
        if (!subtitleExists) {
            i = 8;
        }
        textView.setVisibility(i);
        this.mSecondaryText.requestLayout();
        this.mLastUpdatedText.requestLayout();
    }

    private void addRange(final SliceItem range) {
        ProgressBar progressBar;
        boolean isSeekBar = "action".equals(range.getFormat());
        if (isSeekBar) {
            progressBar = new SeekBar(getContext());
        } else {
            progressBar = new ProgressBar(getContext(), null, 16842872);
        }
        Drawable progressDrawable = DrawableCompat.wrap(progressBar.getProgressDrawable());
        if (!(this.mTintColor == -1 || progressDrawable == null)) {
            DrawableCompat.setTint(progressDrawable, this.mTintColor);
            progressBar.setProgressDrawable(progressDrawable);
        }
        SliceItem min = SliceQuery.findSubtype(range, "int", SliceHints.SUBTYPE_MIN);
        int minValue = 0;
        if (min != null) {
            minValue = min.getInt();
        }
        SliceItem max = SliceQuery.findSubtype(range, "int", "max");
        if (max != null) {
            progressBar.setMax(max.getInt() - minValue);
        }
        SliceItem progress = SliceQuery.findSubtype(range, "int", "value");
        if (progress != null) {
            progressBar.setProgress(progress.getInt() - minValue);
        }
        progressBar.setVisibility(0);
        addView(progressBar);
        this.mRangeBar = progressBar;
        if (isSeekBar) {
            Drawable d;
            SliceItem thumb = this.mRowContent.getInputRangeThumb();
            SeekBar seekBar = this.mRangeBar;
            if (thumb != null) {
                d = thumb.getIcon().loadDrawable(getContext());
                if (d != null) {
                    seekBar.setThumb(d);
                }
            }
            d = DrawableCompat.wrap(seekBar.getThumb());
            if (!(this.mTintColor == -1 || d == null)) {
                DrawableCompat.setTint(d, this.mTintColor);
                seekBar.setThumb(d);
            }
            final int finalMinValue = minValue;
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    try {
                        range.fireAction(RowView.this.getContext(), new Intent().putExtra("android.app.slice.extra.RANGE_VALUE", progress + finalMinValue));
                    } catch (CanceledException e) {
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    private void addAction(SliceActionImpl actionContent, int color, ViewGroup container, boolean isStart) {
        SliceActionView sav = new SliceActionView(getContext());
        container.addView(sav);
        boolean isToggle = actionContent.isToggle();
        EventInfo info = new EventInfo(getMode(), isToggle ^ 1, isToggle ? 3 : 0, this.mRowIndex);
        if (isStart) {
            info.setPosition(0, 0, 1);
        }
        sav.setAction(actionContent, info, this.mObserver, color);
        if (isToggle) {
            this.mToggles.put(actionContent, sav);
        }
    }

    private boolean addItem(SliceItem sliceItem, int color, boolean isStart) {
        IconCompat icon = null;
        int imageMode = 0;
        SliceItem timeStamp = null;
        ViewGroup container = isStart ? this.mStartContainer : this.mEndContainer;
        boolean z = true;
        if ("slice".equals(sliceItem.getFormat()) || "action".equals(sliceItem.getFormat())) {
            if (sliceItem.hasHint(UserDictionaryAddWordContents.EXTRA_SHORTCUT)) {
                addAction(new SliceActionImpl(sliceItem), color, container, isStart);
                return true;
            }
            sliceItem = (SliceItem) sliceItem.getSlice().getItems().get(0);
        }
        if ("image".equals(sliceItem.getFormat())) {
            icon = sliceItem.getIcon();
            imageMode = sliceItem.hasHint("no_tint");
        } else if ("long".equals(sliceItem.getFormat())) {
            timeStamp = sliceItem;
        }
        View addedView = null;
        if (icon != null) {
            boolean isIcon = imageMode == 0;
            View iv = new ImageView(getContext());
            iv.setImageDrawable(icon.loadDrawable(getContext()));
            if (isIcon && color != -1) {
                iv.setColorFilter(color);
            }
            container.addView(iv);
            LayoutParams lp = (LayoutParams) iv.getLayoutParams();
            lp.width = this.mImageSize;
            lp.height = this.mImageSize;
            iv.setLayoutParams(lp);
            int p = isIcon ? this.mIconSize / 2 : 0;
            iv.setPadding(p, p, p, p);
            addedView = iv;
        } else if (timeStamp != null) {
            View tv = new TextView(getContext());
            tv.setText(SliceViewUtil.getRelativeTimeString(sliceItem.getTimestamp()));
            tv.setTextSize(0, (float) this.mSubtitleSize);
            tv.setTextColor(this.mSubtitleColor);
            container.addView(tv);
            addedView = tv;
        }
        if (addedView == null) {
            z = false;
        }
        return z;
    }

    private void showSeeMore() {
        Button b = (Button) LayoutInflater.from(getContext()).inflate(R.layout.abc_slice_row_show_more, this, false);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (RowView.this.mObserver != null) {
                        RowView.this.mObserver.onSliceAction(new EventInfo(RowView.this.getMode(), 4, 0, RowView.this.mRowIndex), RowView.this.mRowContent.getSlice());
                    }
                    RowView.this.mRowContent.getSlice().fireAction(null, null);
                } catch (CanceledException e) {
                    Log.e(RowView.TAG, "PendingIntent for slice cannot be sent", e);
                }
            }
        });
        if (this.mTintColor != -1) {
            b.setTextColor(this.mTintColor);
        }
        this.mSeeMoreView = b;
        this.mRootView.addView(this.mSeeMoreView);
    }

    public void onClick(View view) {
        if (this.mRowAction != null && this.mRowAction.getActionItem() != null) {
            if (!this.mRowAction.isToggle() || (view instanceof SliceActionView)) {
                try {
                    this.mRowAction.getActionItem().fireAction(null, null);
                    if (this.mObserver != null) {
                        this.mObserver.onSliceAction(new EventInfo(getMode(), 3, 0, this.mRowIndex), this.mRowAction.getSliceItem());
                        return;
                    }
                    return;
                } catch (CanceledException e) {
                    Log.e(TAG, "PendingIntent for slice cannot be sent", e);
                    return;
                }
            }
            SliceActionView sav = (SliceActionView) this.mToggles.get(this.mRowAction);
            if (sav != null) {
                sav.toggle();
            }
        }
    }

    private void setViewClickable(View layout, boolean isClickable) {
        Drawable drawable = null;
        layout.setOnClickListener(isClickable ? this : null);
        if (isClickable) {
            drawable = SliceViewUtil.getDrawable(getContext(), 16843534);
        }
        layout.setBackground(drawable);
        layout.setClickable(isClickable);
    }

    public void resetView() {
        this.mRootView.setVisibility(0);
        setLayoutDirection(2);
        setViewClickable(this.mRootView, false);
        setViewClickable(this.mContent, false);
        this.mStartContainer.removeAllViews();
        this.mEndContainer.removeAllViews();
        this.mPrimaryText.setText(null);
        this.mSecondaryText.setText(null);
        this.mLastUpdatedText.setText(null);
        this.mLastUpdatedText.setVisibility(8);
        this.mToggles.clear();
        this.mRowAction = null;
        this.mDivider.setVisibility(8);
        if (this.mRangeBar != null) {
            removeView(this.mRangeBar);
            this.mRangeBar = null;
        }
        if (this.mSeeMoreView != null) {
            this.mRootView.removeView(this.mSeeMoreView);
            this.mSeeMoreView = null;
        }
    }
}
