package com.android.settings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.icu.text.DecimalFormatSymbols;
import android.support.annotation.ColorRes;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.Utils;

public class DonutView extends View {
    private static final int LINE_CHARACTER_LIMIT = 10;
    private static final int TOP = -90;
    private Paint mBackgroundCircle;
    private TextPaint mBigNumberPaint;
    private Paint mFilledArc;
    private String mFullString;
    private int mMeterBackgroundColor;
    private int mMeterConsumedColor;
    private double mPercent;
    private String mPercentString;
    private boolean mShowPercentString = true;
    private float mStrokeWidth;
    private TextPaint mTextPaint;

    public DonutView(Context context) {
        super(context);
    }

    public DonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMeterBackgroundColor = context.getColor(R.color.meter_background_color);
        this.mMeterConsumedColor = Utils.getDefaultColor(this.mContext, R.color.meter_consumed_color);
        boolean applyColorAccent = true;
        Resources resources = context.getResources();
        this.mStrokeWidth = resources.getDimension(R.dimen.storage_donut_thickness);
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.DonutView);
            this.mMeterBackgroundColor = styledAttrs.getColor(1, this.mMeterBackgroundColor);
            this.mMeterConsumedColor = styledAttrs.getColor(2, this.mMeterConsumedColor);
            applyColorAccent = styledAttrs.getBoolean(0, true);
            this.mShowPercentString = styledAttrs.getBoolean(3, true);
            this.mStrokeWidth = (float) styledAttrs.getDimensionPixelSize(4, (int) this.mStrokeWidth);
            styledAttrs.recycle();
        }
        this.mBackgroundCircle = new Paint();
        this.mBackgroundCircle.setAntiAlias(true);
        this.mBackgroundCircle.setStrokeCap(Cap.BUTT);
        this.mBackgroundCircle.setStyle(Style.STROKE);
        this.mBackgroundCircle.setStrokeWidth(this.mStrokeWidth);
        this.mBackgroundCircle.setColor(this.mMeterBackgroundColor);
        this.mFilledArc = new Paint();
        this.mFilledArc.setAntiAlias(true);
        this.mFilledArc.setStrokeCap(Cap.BUTT);
        this.mFilledArc.setStyle(Style.STROKE);
        this.mFilledArc.setStrokeWidth(this.mStrokeWidth);
        this.mFilledArc.setColor(this.mMeterConsumedColor);
        if (applyColorAccent) {
            ColorFilter mAccentColorFilter = new PorterDuffColorFilter(Utils.getColorAttr(context, 16843829), Mode.SRC_IN);
            this.mBackgroundCircle.setColorFilter(mAccentColorFilter);
            this.mFilledArc.setColorFilter(mAccentColorFilter);
        }
        int bidiFlags = TextUtils.getLayoutDirectionFromLocale(resources.getConfiguration().locale) == 0 ? 0 : 1;
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setColor(Utils.getColorAccent(getContext()));
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(resources.getDimension(R.dimen.storage_donut_view_label_text_size));
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mTextPaint.setBidiFlags(bidiFlags);
        this.mBigNumberPaint = new TextPaint();
        this.mBigNumberPaint.setColor(Utils.getColorAccent(getContext()));
        this.mBigNumberPaint.setAntiAlias(true);
        this.mBigNumberPaint.setTextSize(resources.getDimension(R.dimen.storage_donut_view_percent_text_size));
        this.mBigNumberPaint.setTypeface(Typeface.create(context.getString(17039704), 0));
        this.mBigNumberPaint.setBidiFlags(bidiFlags);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDonut(canvas);
        if (this.mShowPercentString) {
            drawInnerText(canvas);
        }
    }

    private void drawDonut(Canvas canvas) {
        Canvas canvas2 = canvas;
        canvas2.drawArc(0.0f + this.mStrokeWidth, 0.0f + this.mStrokeWidth, ((float) getWidth()) - this.mStrokeWidth, ((float) getHeight()) - this.mStrokeWidth, -90.0f, 360.0f, false, this.mBackgroundCircle);
        canvas2.drawArc(0.0f + this.mStrokeWidth, 0.0f + this.mStrokeWidth, ((float) getWidth()) - this.mStrokeWidth, ((float) getHeight()) - this.mStrokeWidth, -90.0f, 360.0f * ((float) this.mPercent), false, this.mFilledArc);
    }

    private void drawInnerText(Canvas canvas) {
        Canvas canvas2 = canvas;
        float centerX = (float) (getWidth() / 2);
        float totalHeight = getTextHeight(this.mTextPaint) + getTextHeight(this.mBigNumberPaint);
        float startY = (totalHeight / 2.0f) + ((float) (getHeight() / 2));
        String localizedPercentSign = new DecimalFormatSymbols().getPercentString();
        canvas.save();
        StaticLayout staticLayout = new StaticLayout(getPercentageStringSpannable(getResources(), this.mPercentString, localizedPercentSign), this.mBigNumberPaint, getWidth(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas2.translate(0.0f, (((float) getHeight()) - totalHeight) / 2.0f);
        staticLayout.draw(canvas2);
        canvas.restore();
        canvas2.drawText(this.mFullString, centerX, startY - this.mTextPaint.descent(), this.mTextPaint);
    }

    public void setPercentage(double percent) {
        this.mPercent = percent;
        this.mPercentString = Utils.formatPercentage(this.mPercent);
        this.mFullString = getContext().getString(R.string.storage_percent_full);
        if (this.mFullString.length() > 10) {
            this.mTextPaint.setTextSize(getContext().getResources().getDimension(R.dimen.storage_donut_view_shrunken_label_text_size));
        }
        setContentDescription(getContext().getString(R.string.join_many_items_middle, new Object[]{this.mPercentString, this.mFullString}));
        invalidate();
    }

    @ColorRes
    public int getMeterBackgroundColor() {
        return this.mMeterBackgroundColor;
    }

    public void setMeterBackgroundColor(@ColorRes int meterBackgroundColor) {
        this.mMeterBackgroundColor = meterBackgroundColor;
        this.mBackgroundCircle.setColor(meterBackgroundColor);
        invalidate();
    }

    @ColorRes
    public int getMeterConsumedColor() {
        return this.mMeterConsumedColor;
    }

    public void setMeterConsumedColor(@ColorRes int meterConsumedColor) {
        this.mMeterConsumedColor = meterConsumedColor;
        this.mFilledArc.setColor(meterConsumedColor);
        invalidate();
    }

    @VisibleForTesting
    static Spannable getPercentageStringSpannable(Resources resources, String percentString, String percentageSignString) {
        float fontProportion = resources.getDimension(R.dimen.storage_donut_view_percent_sign_size) / resources.getDimension(R.dimen.storage_donut_view_percent_text_size);
        Spannable percentStringSpan = new SpannableString(percentString);
        int startIndex = percentString.indexOf(percentageSignString);
        int endIndex = percentageSignString.length() + startIndex;
        if (startIndex < 0) {
            startIndex = 0;
            endIndex = percentString.length();
        }
        percentStringSpan.setSpan(new RelativeSizeSpan(fontProportion), startIndex, endIndex, 34);
        return percentStringSpan;
    }

    private float getTextHeight(TextPaint paint) {
        return paint.descent() - paint.ascent();
    }
}
